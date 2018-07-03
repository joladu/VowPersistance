package com.jola.nfc_nb.base;

import com.jola.nfc_nb.MainActivity;

import dagger.Component;

/**
 * Created by lenovo on 2018/6/22.
 */

@Component(modules = MyModule.class)
public interface AppComponent {
    void inject(MainActivity mainActivity);
}
