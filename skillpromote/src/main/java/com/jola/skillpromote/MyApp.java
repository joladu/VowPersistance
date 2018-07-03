package com.jola.skillpromote;

import android.app.Application;

import com.taobao.sophix.PatchStatus;
import com.taobao.sophix.SophixManager;
import com.taobao.sophix.listener.PatchLoadStatusListener;

/**
 * Created by lenovo on 2018/7/2.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

//        init Sophix
//        SophixManager.getInstance().setContext(this)
//                .setAppVersion("2")
//                .setAesKey(null)
//                .setEnableDebug(true)
//                .setPatchLoadStatusStub(new PatchLoadStatusListener() {
//                    @Override
//                    public void onLoad(int mode, int code, String s, int i2) {
////                        加载成功
//                        if (code == PatchStatus.CODE_DOWNLOAD_SUCCESS){
//
//                        }else if (code == PatchStatus.CODE_LOAD_RELAUNCH){
////                            表明新补丁需要重启，才能使用
//
//                        }else{
////                            其它错误信息
//
//                        }
//                    }
//                })
//                .initialize();
    }
}
