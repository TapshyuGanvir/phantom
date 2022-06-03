package com.sixsimplex.treasurehunt.revelocore.util.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;
import com.sixsimplex.treasurehunt.revelocore.util.AppController;

public class SecurityPreferenceUtility {

    private static String SECURITY_EDITOR = "securityEditor";

    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(SECURITY_EDITOR, Context.MODE_PRIVATE);
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

    private static void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static boolean getBoolean(String key) {
        return getSharedPreference().getBoolean(key, false);
    }


    public static String ACCESS_TOKEN = "access_token";
    public static String REFRESH_TOKEN = "refresh_token";
    private static String IS_LOGIN_USER = "is_login_user";

    public static String getAccessToken() {
        return getString(ACCESS_TOKEN);
    }

    public static void setAccessToken(String accessToken) {
        putString(ACCESS_TOKEN, accessToken);
    }

    public static String getRefreshToken() {
        return getString(REFRESH_TOKEN);
    }

    public static void setRefreshToken(String refreshToken) {
        putString(REFRESH_TOKEN, refreshToken);
    }

    public static void isLoginUser(boolean value) {
        putBoolean(IS_LOGIN_USER, value);
    }

    public static boolean getIsLoginUser() {
        return getBoolean(IS_LOGIN_USER);
    }

}
