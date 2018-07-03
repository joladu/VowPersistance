package com.jola.nfc_nb;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jola.nfc_nb.base.AppComponent;
import com.jola.nfc_nb.base.BaseNfcActivity;
import com.jola.nfc_nb.base.DaggerAppComponent;
import com.jola.nfc_nb.base.IPresenter;
import com.jola.nfc_nb.base.IView;
import com.jola.nfc_nb.base.MyModule;
import com.jola.nfc_nb.base.MyPresenter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * nfc——nb
 * 通过nfc 对nb的表进行 近端调试
 * 调试命令
 */
public class MainActivity extends BaseNfcActivity implements IView {

    public static final String Tag = "MainActivity";

    @Inject
    MyPresenter iPresenter;

    @BindView(R.id.text_view)
    TextView mainTv;

    @BindView(R.id.updateBtn)
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        完成注入
        init();
    }



    private void init() {
        AppComponent build = DaggerAppComponent.builder()
                .myModule(new MyModule(this))
                .build();
        build.inject(this);
    }

    @OnClick(R.id.updateBtn)
    void doTest(View view){
        iPresenter.loadData();
        Log.e(Tag,"iPresenter.loadData()");
    }


    @Override
    public void updateUi(String data) {
        mainTv.setText(data);
    }
}
