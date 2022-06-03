package com.sixsimplex.treasurehunt.revelocore.util.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;

import com.sixsimplex.treasurehunt.revelocore.util.AppController;

public class JurisdictionInfoPreferenceUtility {

    private static String JURISDICTION_INFO = "jurisdiction_info";
    private static String SELECTED_JURISDICTION_NAME = "selected_jurisdiction_name";
    private static String JURISDICTIONS = "jurisdiction";
    private static String SELECTED_JURISDICTION_TYPE = "selected_jurisdiction_type";
    private static String FILTER_JURISDICTION="filter_jurisdiction";

    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(JURISDICTION_INFO,
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

    public static void storeSelectedJurisdictionName(String jurisdictionName) {
        putString(UserInfoPreferenceUtility.getUserName() + "_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ SELECTED_JURISDICTION_NAME, jurisdictionName);
    }

    public static String getSelectedJurisdictionName() {
        return getString(UserInfoPreferenceUtility.getUserName() + "_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ SELECTED_JURISDICTION_NAME);
    }

    public static void storeJurisdictions(String jurisdiction) {
        putString(UserInfoPreferenceUtility.getUserName() + "_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ JURISDICTIONS, jurisdiction);
        storeFilterJurisdictions("");
    }
    public static void storePreviousJurisdictions(String jurisdiction) {
        putString(UserInfoPreferenceUtility.getUserName() + "_PREVIOUS_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ JURISDICTIONS, jurisdiction);
    }

    public static String getJurisdictions() {
        return getString(UserInfoPreferenceUtility.getUserName() + "_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ JURISDICTIONS);
    }
    public static String getPreviousJurisdictions() {
        return getString(UserInfoPreferenceUtility.getUserName() + "_PREVIOUS_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ JURISDICTIONS);
    }

    private static String getString(String key) {
        return getSharedPreference().getString(key, "");
    }

    public static void storeSelectedJurisdictionType(String jurisdictionName) {
        putString(UserInfoPreferenceUtility.getUserName() + "_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ SELECTED_JURISDICTION_TYPE, jurisdictionName);
    }

    public static String getSelectedJurisdictionType() {
        return getString(UserInfoPreferenceUtility.getUserName() + "_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ SELECTED_JURISDICTION_TYPE);
    }

    public static void storeFilterJurisdictions(String jurisdiction) {
        putString(UserInfoPreferenceUtility.getUserName() + "_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ FILTER_JURISDICTION, jurisdiction);
    }

    public static String getFilterJurisdiction() {
        return getString(UserInfoPreferenceUtility.getUserName() + "_" +UserInfoPreferenceUtility.getSurveyName()+"_"+ FILTER_JURISDICTION);
    }


}
