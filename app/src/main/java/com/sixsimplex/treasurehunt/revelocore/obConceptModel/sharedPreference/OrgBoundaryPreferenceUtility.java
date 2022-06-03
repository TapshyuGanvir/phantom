package com.sixsimplex.treasurehunt.revelocore.obConceptModel.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.model.OBDataModel;
import com.sixsimplex.treasurehunt.revelocore.util.AppController;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

public class OrgBoundaryPreferenceUtility {

    private static String ORG_BOUNDARY_EDITOR = "orgBoundaryEditor";

    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(ORG_BOUNDARY_EDITOR, Context.MODE_PRIVATE);
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

    private static String ORG_BOUNDARY = "orgBoundary";
    private static String GIS_SERVER_URL = "gisServerUrl";
    private static String DATA_SOURCE_NAME = "dataSourceName";

    public static void storeGisServerUrl(String url) {
        putString(GIS_SERVER_URL, url);
    }

    public static String getGisServerUrl() {
        return getString(GIS_SERVER_URL);
    }

    public static void storeDataSourceName(String name) {
        putString(DATA_SOURCE_NAME, name);
    }

    public static String getDataSourceName() {
        return getString(DATA_SOURCE_NAME);
    }

    public static void storeObRe(OBDataModel OBDataModel) {
        Gson gson = new Gson();
        String json = gson.toJson(OBDataModel);
        putString(UserInfoPreferenceUtility.getOrgName() + "_" + ORG_BOUNDARY, json);
    }

    public static OBDataModel getObRe() {

        String stringJson = getString(UserInfoPreferenceUtility.getOrgName() + "_" + ORG_BOUNDARY);

        if (TextUtils.isEmpty(stringJson)) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.fromJson(stringJson, OBDataModel.class);
        }
    }

}
