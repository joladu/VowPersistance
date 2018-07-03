package com.jola.nfc_nb.base;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lenovo on 2018/6/22.
 */

@Module
public class MyModule {

    private IView iView;

    public MyModule(IView iView) {
        this.iView = iView;
    }

    @Provides
    MyPresenter provideMyPresenter(){
        return new MyPresenter(iView);
    }

}
