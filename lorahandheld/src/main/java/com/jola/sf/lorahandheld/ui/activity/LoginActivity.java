package com.jola.sf.lorahandheld.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.jola.sf.lorahandheld.MainActivity;
import com.jola.sf.lorahandheld.R;
import com.jola.sf.lorahandheld.app.AppConstance;
import com.jola.sf.lorahandheld.ui.base.BaseActivity;
import com.jola.sf.lorahandheld.util.SPUtils;

public class LoginActivity extends BaseActivity {

    private EditText mAccountEt;
    private EditText mPasswordEt;
    private CheckBox mRememberCb;

    String account ;
    String password ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

    }

    private void checkHasPassword() {
        String account = SPUtils.getInstance().getString(SPUtils.Tag_Account,null);
        if (null != account){
            mAccountEt.setText(account);
            String password = SPUtils.getInstance().getString(SPUtils.Tag_Password,null);
            if (null != password){
                mPasswordEt.setText(password);
                if (account.equals(AppConstance.AdminAccount) && password.equals(AppConstance.Adminpassword)){
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    this.finish();
                }
            }
        }
    }

    private void initView() {

        mAccountEt = findViewById(R.id.account_login_et);
        mPasswordEt = findViewById(R.id.password_login_et);
        account = mAccountEt.getText().toString();
        password = mPasswordEt.getText().toString();
        mRememberCb = findViewById(R.id.remember_password_cb);
//        mRememberCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked){
//                    SpUtil.saveAccountAndPassword(account,password);
//                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideLoadingDialog();
        checkHasPassword();
    }

    public void login(View view){
        showLoadingDialog("加载中...");
        String account = mAccountEt.getText().toString();
        String password = mPasswordEt.getText().toString();
        if (account.length() > 0 && password.length() > 0){
            if (account.equals(AppConstance.AdminAccount) && password.equals(AppConstance.Adminpassword)){
                if (mRememberCb.isChecked()){
                    SPUtils.getInstance().saveAccountAndPassword(account,password);
                }
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                this.finish();
            }else{
                Toast.makeText(LoginActivity.this,"账号或密码不对",Toast.LENGTH_SHORT).show();
                hideLoadingDialog();
            }
        }
    }



}
