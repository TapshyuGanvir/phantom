package com.sixsimplex.treasurehunt.revelocore.util.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;

import com.sixsimplex.treasurehunt.revelocore.util.AppController;

public class UrlPreferenceUtility {

    private static String URL_EDITOR = "securityEditor";

    private static boolean isConfigurable=false;
    private static String isConfigurableStr="isConfigurable";

    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(URL_EDITOR, Context.MODE_PRIVATE);
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

    private static String SECURITY_REALM_NAME = "security_realm_name";
    private static String SECURITY_SERVER_IP = "security_server_ip";
    private static String APP_SERVER_IP = "app_server_ip";

    public static void saveAppServerIp(String serverIp) {
        if (serverIp.lastIndexOf("/") == serverIp.length() - 1) {
            serverIp = serverIp.substring(0, serverIp.length() - 1);
        }
        putString(APP_SERVER_IP, serverIp);
    }

    public static String getAppServerIp() {
        //for ajmera
        if(isConfigurable){
        return getString(APP_SERVER_IP);
        }else {
            return "http://137.59.53.68:7075";
        }
    }

    public static void saveSecurityServerIP(String securityServerIP) {
        if (securityServerIP.lastIndexOf("/") == securityServerIP.length() - 1) {
            securityServerIP = securityServerIP.substring(0, securityServerIP.length() - 1);
        }
        putString(SECURITY_SERVER_IP, securityServerIP);
    }

    public static String getSecurityServerIP() {
        //for ajmera

        if(isConfigurable){
        return getString(SECURITY_SERVER_IP);
        }else {
            return "http://137.59.53.68:9090";
        }
    }

    public static void saveSecurityRealmName(String realmName) {
        putString(SECURITY_REALM_NAME,realmName);
    }

    public static String getSecurityRealmName() {
        //for ajmera

        if(isConfigurable){
        return getString(SECURITY_REALM_NAME);
        }else {
            return "revelo3";
        }
    }
}
