package com.max.zxing;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.max.zxing.camera.CameraManager;

import java.util.Collection;
import java.util.Map;

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class CaptureHandler extends Handler implements ResultPointCallback {

    private static final String TAG = CaptureHandler.class.getSimpleName();

    private final OnCaptureListener onCaptureListener;
    private final DecodeThread decodeThread;
    private State state;
    private final CameraManager cameraManager;
    private final Activity activity;
    private final ViewfinderView viewfinderView;
    private SurfaceView surfaceView;
    /**
     * 是否支持垂直的条形码
     */
    private boolean isSupportVerticalCode;

    /**
     * 是否返回扫码原图
     */
    private boolean isReturnBitmap;

    /**
     * 是否支持自动缩放
     */
    private boolean isSupportAutoZoom;

    /**
     *
     */
    private boolean isSupportLuminanceInvert;

    public void setSurfaceHolderTransform(SurfaceView surfaceView) {
        Matrix matrix = calculateSurfaceHolderTransform();
        float[] values = new float[9];
        matrix.getValues(values);
        surfaceView.setTranslationX(values[Matrix.MTRANS_X]);
        surfaceView.setTranslationY(values[Matrix.MTRANS_Y]);
        surfaceView.setScaleX(values[Matrix.MSCALE_X]);
        surfaceView.setScaleY(values[Matrix.MSCALE_Y]);
        surfaceView.invalidate();
    }

    public Matrix calculateSurfaceHolderTransform() {
        // 预览 View 的大小，比如 SurfaceView
        int viewHeight = cameraManager.getScreenResolution().y;
        int viewWidth = cameraManager.getScreenResolution().x;
        // 相机选择的预览尺寸
        int cameraHeight = cameraManager.getCameraResolution().x;
        int cameraWidth = cameraManager.getCameraResolution().y;
        // 计算出将相机的尺寸 => View 的尺寸需要的缩放倍数
        float ratioPreview = (float) cameraWidth / cameraHeight;
        float ratioView = (float) viewWidth / viewHeight;
        float scaleX, scaleY;
        if (ratioView < ratioPreview) {
            scaleX = ratioPreview / ratioView;
            scaleY = 1;
        } else {
            scaleX = 1;
            scaleY = ratioView / ratioPreview;
        }
        // 计算出 View 的偏移量
        float scaledWidth = viewWidth * scaleX;
        float scaledHeight = viewHeight * scaleY;
        float dx = (viewWidth - scaledWidth) / 2;
        float dy = (viewHeight - scaledHeight) / 2;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        matrix.postTranslate(dx, dy);

        return matrix;
    }


    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    CaptureHandler(Activity activity, ViewfinderView viewfinderView, OnCaptureListener onCaptureListener,
                   Collection<BarcodeFormat> decodeFormats,
                   Map<DecodeHintType, Object> baseHints,
                   String characterSet,
                   CameraManager cameraManager, SurfaceView surfaceView) {
        this.activity = activity;
        this.viewfinderView = viewfinderView;
        this.onCaptureListener = onCaptureListener;
        decodeThread = new DecodeThread(activity, cameraManager, this, decodeFormats, baseHints, characterSet, this);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        this.surfaceView = surfaceView;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.restart_preview) {
            restartPreviewAndDecode();

        } else if (message.what == R.id.decode_succeeded) {
            state = State.SUCCESS;
            Bundle bundle = message.getData();
            Bitmap barcode = null;
            float scaleFactor = 1.0f;
            if (bundle != null) {
                byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
                if (compressedBitmap != null) {
                    barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                    // Mutable copy:
                    barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
                }
                scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
            }
            onCaptureListener.onHandleDecode((Result) message.obj, barcode, scaleFactor);


        } else if (message.what == R.id.decode_failed) {// We're decoding as fast as possible, so when one decode fails, start another.
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);

        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(100L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    public void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            viewfinderView.drawViewfinder();
        }
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint point) {
        if (viewfinderView != null) {
            ResultPoint resultPoint = transform(point);
            viewfinderView.addPossibleResultPoint(resultPoint);
        }
    }

    private boolean isScreenPortrait(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point screenResolution = new Point();
        display.getSize(screenResolution);
        return screenResolution.x < screenResolution.y;
    }

    /**
     * @return
     */
    private ResultPoint transform(ResultPoint originPoint) {
        Point screenPoint = cameraManager.getScreenResolution();
        Point cameraPoint = cameraManager.getCameraResolution();

        float scaleX;
        float scaleY;
        float x;
        float y;

        if (screenPoint.x < screenPoint.y) {
            scaleX = 1.0f * screenPoint.x / cameraPoint.y;
            scaleY = 1.0f * screenPoint.y / cameraPoint.x;

            x = originPoint.getX() * scaleX - Math.max(screenPoint.x, cameraPoint.y) / 2;
            y = originPoint.getY() * scaleY - Math.min(screenPoint.y, cameraPoint.x) / 2;
        } else {
            scaleX = 1.0f * screenPoint.x / cameraPoint.x;
            scaleY = 1.0f * screenPoint.y / cameraPoint.y;

            x = originPoint.getX() * scaleX - Math.min(screenPoint.y, cameraPoint.y) / 2;
            y = originPoint.getY() * scaleY - Math.max(screenPoint.x, cameraPoint.x) / 2;
        }


        return new ResultPoint(x, y);
    }

    public boolean isSupportVerticalCode() {
        return isSupportVerticalCode;
    }

    public void setSupportVerticalCode(boolean supportVerticalCode) {
        isSupportVerticalCode = supportVerticalCode;
    }

    public boolean isReturnBitmap() {
        return isReturnBitmap;
    }

    public void setReturnBitmap(boolean returnBitmap) {
        isReturnBitmap = returnBitmap;
    }

    /**
     * @return 是否支持自动缩放
     */
    public boolean isSupportAutoZoom() {
        return isSupportAutoZoom;
    }

    /**
     * 设置是否支持自动缩放
     *
     * @param supportAutoZoom true 支持
     *                        false 不支持
     */
    public void setSupportAutoZoom(boolean supportAutoZoom) {
        isSupportAutoZoom = supportAutoZoom;
    }

    public boolean isSupportLuminanceInvert() {
        return isSupportLuminanceInvert;
    }

    public void setSupportLuminanceInvert(boolean supportLuminanceInvert) {
        isSupportLuminanceInvert = supportLuminanceInvert;
    }
}
