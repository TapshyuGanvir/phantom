package com.sixsimplex.treasurehunt.Phantom1.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.sixsimplex.treasurehunt.revelocore.util.AppController;

public class TraversalPreferenceUtility {
    private static String TRAVERSAL_EDITOR = "traversalEditor";
    private static String TRAVERSAL_GRAPH_STR = "traversalGraphStr";
    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(TRAVERSAL_EDITOR, Context.MODE_PRIVATE);
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

    public static void storeTraversalGraph(String entityName, String traversalGraphStr) {
        putString(TRAVERSAL_GRAPH_STR+"_"+entityName, traversalGraphStr);
    }

    public static String getTraversalGraphStr(String entityName) {
        return getString(TRAVERSAL_GRAPH_STR+"_"+entityName);
    }



}
