package com.wismna.geoffroy.donext;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by bg45 on 2017-03-15.
 */

public class DoNext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
    }
}
