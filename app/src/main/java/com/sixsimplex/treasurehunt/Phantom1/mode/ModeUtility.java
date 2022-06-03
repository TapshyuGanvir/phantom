package com.sixsimplex.treasurehunt.Phantom1.mode;

import android.content.Context;
import android.content.SharedPreferences;

import com.sixsimplex.treasurehunt.revelocore.util.AppController;

public class ModeUtility {
    private static String MODE_EDITOR="modeEditor";
    private static String MODE="mode";
    public static String SINGLE="single";
    public static  String MULTI="multi";


    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(MODE_EDITOR,
                Context.MODE_PRIVATE);
    }

    private static SharedPreferences.Editor getEditor() {
        SharedPreferences pref = getSharedPreference();
        return pref.edit();
    }

    private static void putString(String key, String value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, value);
        editor.apply();
    }

    private static String getString(String key) {
        return getSharedPreference().getString(key, MULTI);
    }

    public static void setMultiMode() {
        putString(MODE, MULTI);
    }

    public static void setSingleMode() {
        putString(MODE, SINGLE);
    }

    public static String getMode() {
        return getString(MODE);
    }
}
