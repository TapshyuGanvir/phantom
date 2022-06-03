package com.sixsimplex.treasurehunt.revelocore.util.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;

import com.sixsimplex.treasurehunt.revelocore.util.AppController;

public class TrailPreferenceUtility {
    private static final String TRAIL_SPEED = "trailspeed";
    private static String TRAIL_INFO_EDITOR = "trailInfoEditor";

    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(TRAIL_INFO_EDITOR,
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
        return getSharedPreference().getString(key, "");
    }

    private static void putInt(String key, int value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(key, value);
        editor.apply();
    }

    private static int getInt(String key) {
        return getSharedPreference().getInt(key, -1);
    }

    private static void putFloat(String key, float value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putFloat(key, value);
        editor.apply();
    }

    private static float getFloat(String key) {
        return getSharedPreference().getFloat(key, -1f);
    }

    private static void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static boolean getBoolean(String key) {
        return getSharedPreference().getBoolean(key, false);
    }

    public static float getTrailSpeed() {
        return getFloat(TRAIL_SPEED);
    }
    public static void setTrailSpeed(float speed) {
         putFloat(TRAIL_SPEED,speed);
    }
}
