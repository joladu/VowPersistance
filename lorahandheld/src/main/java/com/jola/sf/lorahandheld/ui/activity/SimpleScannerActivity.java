package com.jola.sf.lorahandheld.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.jola.sf.lorahandheld.R;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


/**
 * Created by lenovo on 2018/5/18.
 */

public class SimpleScannerActivity extends Activity implements ZBarScannerView.ResultHandler {
    private static final int REQUEST_ACCESS_CAMERA = 301;
    private ZBarScannerView mScannerView;

   private boolean hasPermission;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
//        requestPermission
        hasPermission = hasCameraPermission();
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    private boolean hasCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //判断是否有权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        REQUEST_ACCESS_CAMERA);
                //判断是否需要 向用户解释，为什么要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(this, R.string.tip_request_camera, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            mScannerView.startCamera();          // Start camera on resume
        } else {
            Toast.makeText(this,"未同意相机权限，无法扫描条码！",Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        if (hasPermission){
            mScannerView.startCamera();          // Start camera on resume
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
//        Log.e("jola11", rawResult.getContents()); // Prints scan results
//        Log.e("jola11", rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

        String contents = rawResult.getContents();
        if (null != contents && contents.length() > 0){
            Toast.makeText(this,"扫描内容："+contents,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SimpleScannerActivity.this, CommunicationActivity.class);
            intent.putExtra("result_code",contents);
            this.setResult(CommunicationActivity.Result_Bar_Code,intent);
            this.finish();
        }else{
            // If you would like to resume scanning, call this method below:
            mScannerView.resumeCameraPreview(this);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScannerView = null;
    }
}
