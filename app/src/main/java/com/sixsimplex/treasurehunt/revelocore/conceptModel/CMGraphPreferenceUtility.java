package com.sixsimplex.treasurehunt.revelocore.conceptModel;

import android.content.Context;
import android.content.SharedPreferences;

import com.sixsimplex.treasurehunt.revelocore.util.AppController;

import org.json.JSONException;
import org.json.JSONObject;

public class CMGraphPreferenceUtility {


    public static final String CM_GRAPH_EDITOR="cmGraphEditor";
    public static final String cmGraph="CM_GRAPH_JSON";

    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(CM_GRAPH_EDITOR,
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

    public static void storeCMGraph(JSONObject cmGraphJsonObj) {
        if(cmGraphJsonObj != null){
            putString(cmGraph,String.valueOf(cmGraphJsonObj));
        }else{
            putString(cmGraph,"");
        }

    }
    public static JSONObject getCMGraph() {
        JSONObject cmGraphJson;
        try {
            String cmG=getString(cmGraph);
            if(!cmG.equalsIgnoreCase("")){
                cmGraphJson =new JSONObject(cmG);
            }else{
                cmGraphJson =new JSONObject();
                cmGraphJson.put("status","failure");
            }

        }catch (Exception e){
            try {
                cmGraphJson =new JSONObject();
                cmGraphJson.put("status","failure");
            } catch (JSONException jsonException) {
                cmGraphJson =new JSONObject();
                jsonException.printStackTrace();
            }
        }
        return cmGraphJson;
    }

    public static String getCMGraphString(){
        return getString(cmGraph);
    }

}
