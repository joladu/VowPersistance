package com.jola.sf.lorahandheld.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jola.sf.lorahandheld.R;


/**
 * Created by jola on 2017/10/2.
 */

public class LoadingDialog extends Dialog {

//    static ProgressBar mProgressBar;

    static TextView tipMsgTv;

    public void setTipMsg(@NonNull String  tipMsg){
        tipMsgTv.setText(tipMsg);
    }


    public LoadingDialog(Context context) {
        super(context);
    }

    public LoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder{

        private Context context;
        private String message;
        private boolean isCancelable=false;
        private boolean isCancelOutside=false;


        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置提示信息
         * @param message
         * @return
         */

        public Builder setMessage(String message){
            this.message=message;
            return this;
        }


        /**
         * 设置是否可以按返回键取消
         * @param isCancelable
         * @return
         */

        public Builder setCancelable(boolean isCancelable){
            this.isCancelable=isCancelable;
            return this;
        }

        /**
         * 设置是否可以取消
         * @param isCancelOutside
         * @return
         */
        public Builder setCancelOutside(boolean isCancelOutside){
            this.isCancelOutside=isCancelOutside;
            return this;
        }

        public LoadingDialog create() {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
            LoadingDialog loadingDailog = new LoadingDialog(context, R.style.MyDialogStyle);
//            mProgressBar =  view.findViewById(R.id.progressBar1);
//            TextView msgText =  view.findViewById(R.id.tipTextView);
            tipMsgTv =  view.findViewById(R.id.tipTextView);
            if (null != message && message.length() > 0) {
                tipMsgTv.setText(message);
            } else {
                tipMsgTv.setVisibility(View.GONE);
            }
            loadingDailog.setContentView(view);
            loadingDailog.setCancelable(isCancelable);
            loadingDailog.setCanceledOnTouchOutside(isCancelOutside);
            return loadingDailog;
        }
    }
    /**
     *
     */
//    public void setProgressBarGone(){
//        if (null != mProgressBar){
//            mProgressBar.setVisibility(View.GONE);
//        }
//    }


}
