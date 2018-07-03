package com.jola.nfc_nb.base;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by lenovo on 2018/6/20.
 *
 *  nfc 基础类
 *
 *  提供基础的读 写 NFC数据
 *
 */

public class BaseNfcActivity extends AppCompatActivity {

    protected NfcAdapter mNfcAdapter;
    protected PendingIntent mPendingIntent;

    public final String MIME_TYPE = "text/plain";

    @Override
    protected void onStart() {
        super.onStart();
//        获取默认的nfc 适配器
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
//        pendingintent
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != mNfcAdapter){
            mNfcAdapter.enableForegroundDispatch(this,mPendingIntent,null,null);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (null != mNfcAdapter){
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }




}
