package com.max.zxing.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.max.zxing.CaptureHelper;
import com.max.zxing.OnCaptureCallback;
import com.max.zxing.ViewfinderView;

/**
 * 自定义扫码：当直接使用CaptureActivity
 * 自定义扫码，切记自定义扫码需在{@link Activity}或者{@link Fragment}相对应的生命周期里面调用{@link #mCaptureHelper}对应的生命周期
 */
public class CustomActivity extends AppCompatActivity implements OnCaptureCallback {


    private CaptureHelper mCaptureHelper;

    private SurfaceView mSurfaceView;

    private ViewfinderView mViewfinderView;

    private View mIvTorch;

    private ImageView mImageView;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.custom_activity);

        initView();
    }

    private void initView() {


        mImageView = findViewById(R.id.ivCode);


        mSurfaceView = findViewById(R.id.surfaceView);
        mViewfinderView = findViewById(R.id.viewfinderView);
        mIvTorch = findViewById(R.id.ivFlash);
        mIvTorch.setVisibility(View.INVISIBLE);

        mCaptureHelper = new CaptureHelper(this, mSurfaceView, mViewfinderView, mIvTorch);
        mCaptureHelper.setOnCaptureCallback(this);
        mCaptureHelper.onCreate();
        mCaptureHelper
                .vibrate(true)
                //全屏扫码 全局扫描 减速
                .fullScreenScan(false)
                .framingRectRatio(0.625f)
                //这个并不会对扫描速度有什么影响
                .supportAutoZoom(false)
                //支持扫垂直条码，建议有此需求时才使用
                .supportVerticalCode(false)
                //是否支持识别反色码（黑白反色的码），增加识别率
                .supportLuminanceInvert(true)
                .autoRestartPreviewAndDecode(true)
                .returnBitmap(true)
                .continuousScan(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCaptureHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCaptureHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCaptureHelper.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCaptureHelper.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 扫码结果回调
     *
     * @param result 扫码结果
     * @return
     */
    @Override
    public boolean onResultCallback(String result, Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
        return false;
    }


}