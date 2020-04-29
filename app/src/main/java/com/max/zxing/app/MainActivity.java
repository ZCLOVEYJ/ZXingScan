/*
 * Copyright (C) 2018 Jenly Yu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.max.zxing.app;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * 扫码Demo示例说明
 * <p>
 * 快速实现扫码有以下几种方式：
 * <p>
 * 1、直接使用CaptureActivity或者CaptureFragment。(纯洁的扫码，无任何添加剂)
 * <p>
 * 2、通过继承CaptureActivity或者CaptureFragment并自定义布局。（适用于大多场景，并无需关心扫码相关逻辑，自定义布局时需覆写getLayoutId方法）
 * <p>
 * 3、在你项目的Activity或者Fragment中创建一个CaptureHelper并在相应的生命周期中调用CaptureHelper的周期。（适用于想在扫码界面写交互逻辑，又因为项目架构或其它原因，无法直接或间接继承CaptureActivity或CaptureFragment时使用）
 * <p>
 * 4、参照CaptureHelper写一个自定义的扫码帮助类，其它步骤同方式3。（扩展高级用法，谨慎使用）
 */
public class MainActivity extends AppCompatActivity {

    RxPermissions rxPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkCameraPermissions();
    }


    private void checkCameraPermissions() {
        rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA);

    }

    private void startScan() {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.in, R.anim.out);
        Intent intent = new Intent(this, CustomActivity.class);
        ActivityCompat.startActivity(this, intent, null);
    }


    public void onClick(View v) {
        startScan();


    }
}