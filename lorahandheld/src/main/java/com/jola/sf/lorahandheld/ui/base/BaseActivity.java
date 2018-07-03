package com.jola.sf.lorahandheld.ui.base;

import android.support.v7.app.AppCompatActivity;

import com.jola.sf.lorahandheld.widget.LoadingDialog;

/**
 * 大多数Activity公用的内容放在BaseActivity，减少代码冗余
 */

public abstract class BaseActivity extends AppCompatActivity{

    private LoadingDialog mLoadingDialog;


//    /**
//     * 搜索可连接的蓝牙设备
//     */
//    public void showSearchView(){
//        if (null == mLoadingDialog){
//            mLoadingDialog = new LoadingDialog.Builder(this)
//                    .setMessage("蓝牙搜索中...")
//                    .setCancelable(true)
//                    .setCancelOutside(true)
//                    .create();
//        }
//        if (!mLoadingDialog.isShowing()){
//            mLoadingDialog.show();
//        }
//    }

    public void showLoadingDialog(String loadingTip) {
        if (null == mLoadingDialog) {
            mLoadingDialog = (new LoadingDialog.Builder(BaseActivity.this)
                    .setMessage(loadingTip)
                    .setCancelable(true)).create();
        } else {
            mLoadingDialog.setTipMsg(loadingTip);
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }


    /**
     * 隐藏
     */
    public void hideLoadingDialog(){
        if (null != mLoadingDialog){
            mLoadingDialog.dismiss();
        }
    }




}
