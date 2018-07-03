package com.jola.sf.lorahandheld.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jola.sf.lorahandheld.R;

/**
 * Created by lenovo on 2018/4/23.
 */

public class PopCommandGridView extends PopupWindow {

    private int mAttriNum;

    private LinearLayout mFirstLL;
    private LinearLayout mSecondLL;
    private LinearLayout mThirdLL;


    private TextView mFirstTitleTv;
    private TextView mSecondTitleTv;
    private TextView mThirdTitleTv;

    public String getTitleByIndex(int index) {
        switch (index) {
            case 1:
                return mFirstTitleTv.getText().toString();
            case 2:
                return mSecondTitleTv.getText().toString();
            case 3:
                return mThirdTitleTv.getText().toString();
            default:
                return null;
        }
    }

    public void setTitles(String... titleArr) {
        mAttriNum = titleArr.length;
        if (mAttriNum > 0) {
            mFirstLL.setVisibility(View.VISIBLE);
            mFirstTitleTv.setText(titleArr[0]);
        }else{
            mFirstLL.setVisibility(View.GONE);
        }
        if (mAttriNum > 1) {
            mSecondLL.setVisibility(View.VISIBLE);
            mSecondTitleTv.setText(titleArr[1]);
            mSecondEt.setText("");
        }else{
            mSecondLL.setVisibility(View.GONE);
        }
        if (mAttriNum > 2) {
            mThirdLL.setVisibility(View.VISIBLE);
            mThirdTitleTv.setText(titleArr[2]);
            mThirdEt.setText("");
        }else{
            mThirdLL.setVisibility(View.GONE);
        }
    }

    private EditText mFirstEt;
    private EditText mSecondEt;
    private EditText mThirdEt;

    public String getEtContentByIndex(int index) {
        switch (index) {
            case 1:
                return mFirstEt.getText().toString();
            case 2:
                return mSecondEt.getText().toString();
            case 3:
                return mThirdEt.getText().toString();
            default:
                return null;
        }
    }

    public void setEtContentByIndex(String contentStr,int index) {
        switch (index) {
            case 1:
                mFirstEt.setText(contentStr);
                break;
            case 2:
                mSecondEt.setText(contentStr);
                break;
            case 3:
                mThirdEt.setText(contentStr);
                break;
        }
    }

    public boolean getIsAllSelected() {
        boolean flag = true;
        if (mAttriNum > 0) {
            if (mFirstEt.getText().toString().length() == 0) {
                flag = false;
            }
        }
        if (mAttriNum > 1) {
            if (mSecondEt.getText().toString().length() == 0) {
                flag = false;
            }
        }
        if (mAttriNum > 2) {
            if (mThirdEt.getText().toString().length() == 0) {
                flag = false;
            }
        }
        return flag;
    }


//    public Spinner mFirstSp;
//    public Spinner mSecondSp;
//    public Spinner mThirdSp;


    private LinearLayout mInputLl;
    private TextView mTitleInputTv;
    private View mPopCommandGridView;
    private final String[] stringArray;

    public String getCommandStr(){
        return mTitleInputTv.getText().toString();
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void hindeOrShowInputLl(boolean flag){
        if (flag){
            mInputLl.setVisibility(View.GONE);
        }else{
            mInputLl.setVisibility(View.VISIBLE);
        }
    }

    public void setInputTitle(String titleStr){
        mTitleInputTv.setText(titleStr);
    }

    public PopCommandGridView(final Context context, AdapterView.OnItemClickListener itemClickListener, View.OnClickListener clickListener,boolean isMain) {
        mPopCommandGridView = LayoutInflater.from(context).inflate(R.layout.pop_command_grid_view, null);
        GridView gridView = mPopCommandGridView.findViewById(R.id.command_gv);
        if (isMain){
            stringArray = context.getResources().getStringArray(R.array.command_main_node);
        }else{
            stringArray = context.getResources().getStringArray(R.array.command_type);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_expandable_list_item_1, stringArray);
        gridView.setAdapter(arrayAdapter);

        mInputLl = mPopCommandGridView.findViewById(R.id.input_second_rl);
        mTitleInputTv = mPopCommandGridView.findViewById(R.id.title_second_input_tv);
        mInputLl.setVisibility(View.GONE);

        (mPopCommandGridView.findViewById(R.id.first_pulldown_iv)).setOnClickListener(clickListener);
        (mPopCommandGridView.findViewById(R.id.second_pulldown_iv)).setOnClickListener(clickListener);
        (mPopCommandGridView.findViewById(R.id.third_pulldown_iv)).setOnClickListener(clickListener);


        (mPopCommandGridView.findViewById(R.id.first_scan_iv)).setOnClickListener(clickListener);
        (mPopCommandGridView.findViewById(R.id.second_scan_iv)).setOnClickListener(clickListener);
        (mPopCommandGridView.findViewById(R.id.third_scan_iv)).setOnClickListener(clickListener);


        (mPopCommandGridView.findViewById(R.id.close_popup_iv)).setOnClickListener(clickListener);
        (mPopCommandGridView.findViewById(R.id.cancel_btn)).setOnClickListener(clickListener);
        (mPopCommandGridView.findViewById(R.id.confirm_btn)).setOnClickListener(clickListener);
        gridView.setOnItemClickListener(itemClickListener);


        mFirstLL = mPopCommandGridView.findViewById(R.id.first_line_ll);
        mSecondLL = mPopCommandGridView.findViewById(R.id.second_line_ll);
        mThirdLL = mPopCommandGridView.findViewById(R.id.third_line_ll);

        mFirstTitleTv = mPopCommandGridView.findViewById(R.id.first_title_tv);
        mSecondTitleTv = mPopCommandGridView.findViewById(R.id.second_title_tv);
        mThirdTitleTv = mPopCommandGridView.findViewById(R.id.third_title_tv);

        mFirstEt = mPopCommandGridView.findViewById(R.id.first_ed);
        mSecondEt = mPopCommandGridView.findViewById(R.id.second_ed);
        mThirdEt = mPopCommandGridView.findViewById(R.id.third_ed);

//        mFirstSp = mPopCommandGridView.findViewById(R.id.first_line_sp);
//        mSecondSp = mPopCommandGridView.findViewById(R.id.second_line_sp);
//        mThirdSp = mPopCommandGridView.findViewById(R.id.third_line_sp);


        /* 设置弹出窗口特征 */
        // 设置视图
        this.setContentView(this.mPopCommandGridView);
        // 设置弹出窗体的宽和高
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);


    }


}
