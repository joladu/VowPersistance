package com.jola.nfc_nb.base;

/**
 * Created by lenovo on 2018/6/22.
 */

public class MyPresenter implements IPresenter {

    private IView iView;

    public MyPresenter(IView iView){
        this.iView = iView;
    }

    @Override
    public void loadData() {
        iView.updateUi(System.currentTimeMillis()+"毫秒");
    }

}
