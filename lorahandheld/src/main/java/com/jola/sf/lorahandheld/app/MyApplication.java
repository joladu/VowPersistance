package com.jola.sf.lorahandheld.app;

import android.app.Application;
import android.content.Context;


/**
 * Created by lenovo on 2018/3/10.
 */

public class MyApplication extends Application {

    private static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = getApplicationContext();
    }

    public static Context getMyAppContext(){
        return mAppContext ;
    }


}
