package com.sixsimplex.treasurehunt.revelocore.util;
import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.sixsimplex.treasurehunt.BuildConfig;

import org.osmdroid.config.Configuration;

public class AppController extends Application {

    private static AppController mInstance;

    public static final String TAG = "Revelo 3.0";

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mInstance = this;
    }

}