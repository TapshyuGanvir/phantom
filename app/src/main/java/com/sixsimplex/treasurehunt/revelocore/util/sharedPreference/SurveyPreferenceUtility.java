package com.sixsimplex.treasurehunt.revelocore.util.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.util.AppController;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


public class SurveyPreferenceUtility {

    private static String SURVEY_EDITOR = "surveyEditor";
    private static String BUFFERDISTANCE = "bufferDistance";

    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(SURVEY_EDITOR, Context.MODE_PRIVATE);
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

    private static void putInt(String key, int value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getBufferDistance(String surveyName) {
        return getSharedPreference().getInt(surveyName + "" + BUFFERDISTANCE, 0);
    }

    private static String getString(String key) {
        return getSharedPreference().getString(key, "");
    }

    private static String SURVEY_HASH_MAP = "surveyObjectMap";
    private static String ORG_BOUNDARY = "orgBoundary";

    private static Map<String, Survey> getSurveyHashMap() {

        String stringJson = getString(UserInfoPreferenceUtility.getUserName() + "_" + SURVEY_HASH_MAP);

        if (TextUtils.isEmpty(stringJson)) {
            return null;
        }
        else {

            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Survey>>() {
            }.getType();
            return gson.fromJson(stringJson, type);
        }
    }

    public static void storeSurvey(Survey survey) {

        Map<String, Survey> surveyHashMap = getSurveyHashMap();
        if (surveyHashMap == null) {
            surveyHashMap = new HashMap<>();
        }

        surveyHashMap.put(survey.getName(), survey);

        Gson gson = new Gson();
        String json = gson.toJson(surveyHashMap);
        putString(UserInfoPreferenceUtility.getUserName() + "_" + SURVEY_HASH_MAP, json);
    }

    public static void storeBufferDistance(String surveyName, int bufferDistance) {
        putInt(surveyName + "" + BUFFERDISTANCE, bufferDistance);
    }

    public static Survey getSurvey(String surveyName) {

        Survey survey = null;
        Map<String, Survey> surveyMap = getSurveyHashMap();
        if (surveyMap != null) {
            if (surveyMap.containsKey(surveyName)) {
                survey = surveyMap.get(surveyName);
            }
        }

        return survey;
    }
}