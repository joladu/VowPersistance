package com.jola.skillpromote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.taobao.sophix.SophixManager;

public class MainActivity extends AppCompatActivity {

    private TextView mShowResultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        checkPatch();
    }

    /**
     * 检查补丁包
     */
    private void checkPatch() {
        SophixManager.getInstance().queryAndLoadNewPatch();
    }

    private void initView() {
        mShowResultTv = findViewById(R.id.content_show_tv);
    }

    public void divideByZero(){
        int fenZi = 1;
        int fenMu = 1-fenZi;
        int result = fenZi/fenMu;
        mShowResultTv.setText("1/0 =  "+result);
    }

    public void fixDivideByZero(){
        int fenZi = 1;
        int fenMu = 1-fenZi + 1;
        int result = fenZi/fenMu;
        mShowResultTv.setText("1/0 = 修复后 1/1 = "+result);
    }

    public void onclick(View view){
//        divideByZero();
        fixDivideByZero();
    }

}
