package com.example.marinecommercon.easyconverter;

import android.app.Application;
import android.content.Context;

import java.util.Locale;

public class App extends Application {
    private static App instance;
    private static boolean mock = false;
    public Locale locale;

    public static Context getContext() {
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            //mock = true;
        }
    }

    public static boolean isMock() {
        return mock;
    }
}