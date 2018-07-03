package com.jola.sf.lorahandheld.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.jola.sf.lorahandheld.app.MyApplication;

import java.util.Map;

/**
 * @创建者 joladu
 * @描述 sharedPreferences工具类(单例模式)
 */
public class SPUtils {
    private static final String SP_NAME = "LoraHandheld";
    private static SPUtils mSpUtils;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public static final String Tag_Account = "account";
    public static final String Tag_Password = "password";
    public static final String Tag_Equip_Type_Pos = "equipTypePos";
    public static final String Tag_Controller_Mode_Pos = "controllerModePos";
    public static final String Tag_Controller_Rate_Pos = "controllerRatePos";

    private SPUtils(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public void saveAccountAndPassword(String account,String password){
        editor.putString(Tag_Account,account);
        editor.putString(Tag_Password,password);
        editor.apply();
    }


    public static SPUtils getInstance() {
        if (mSpUtils == null) {
            synchronized (SPUtils.class) {
                if (mSpUtils == null) {
                    mSpUtils = new SPUtils(MyApplication.getMyAppContext());
                    return mSpUtils;
                }
            }
        }
        return mSpUtils;
    }

    public void putBoolean(String key, Boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key, Boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public void putString(String key, String value) {
        if (key == null) {
            return;
        }
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public Map<String, ?> getAll() {
        return sp.getAll();
    }

    public void remove(String key) {
        sp.edit().remove(key).apply();
    }

}