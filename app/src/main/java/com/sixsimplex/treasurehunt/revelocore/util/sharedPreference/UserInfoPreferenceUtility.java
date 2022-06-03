package com.sixsimplex.treasurehunt.revelocore.util.sharedPreference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sixsimplex.treasurehunt.revelocore.phaseDetails.model.Phase;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.userProfile.UserProfileModel;
import com.sixsimplex.treasurehunt.revelocore.util.AppController;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class UserInfoPreferenceUtility {

    private static String USER_INFO_EDITOR = "userInfoEditor";

    private static SharedPreferences getSharedPreference() {
        return AppController.getInstance().getApplicationContext().getSharedPreferences(USER_INFO_EDITOR,
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

    private static void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static boolean getBoolean(String key) {
        return getSharedPreference().getBoolean(key, false);
    }

    public static final String ORG_NAME = "orgName";
    public static final String ORG_LABEL="orgLabel";
    public static final String TAG_LINE="tagLine";
    public static final String APP_NAME="appName";
    public static final String SURVEY_NAME = "surveyName";
    public static final String SURVEY_NAME_LABEL = "surveyNameLabel";
    public static final String SURVEY_PHASE_NAME = "surveyPhaseName";
    public static final String SURVEY_PHASE_LABEL = "surveyPhaseLabel";
    public static final String SURVEY_PHASE_FLOW_NAME = "surveyPhaseFlowName";
    public static final String REDB_TIMESTAMP = "serverREDBTimestamp";

    private static final String USER_NAME = "userName";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String PHONE_NUMBER="phoneNumber";
    public static final String POSITION = "position";
    public static final String ROLE = "role";

    private static final String JURISDICTION_NAME = "jurisdictionName";
    private static final String JURISDICTION_TYPE = "jurisdictionType";

    public static final String IS_REDB_REQUIRED = "isREDBDownloadRequired";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String TIME_FORMAT = "timeFormat";
    public static final String TIMESTAMP_FORMAT = "timeStampFormat";

    public static final String ASSIGNED_PROJECTS = "assignedProjects";
    public static final String ASSIGNED_PROJECTS_PHASES_COUNT = "assignedProjectsPhasesCount";
    public static final String ASSIGNED_PROJECTS_PHASES_COUNT_NONE = "none";
    public static final String ASSIGNED_PROJECTS_PHASES_COUNT_SINGLE_PROJECT_SINGLE_PHASE = "singleProjectSinglePhase";
    public static final String ASSIGNED_PROJECTS_PHASES_COUNT_SINGLE_PROJECT_MULTIPLE_PHASE = "singleProjectMultiplePhase";
    public static final String ASSIGNED_PROJECTS_PHASES_COUNT_MULTIPLE_PROJECTS = "MultipleProjects";


    private static final String IS_VIEWER_ENABLED = "isViewerEnabled";
    private static final String IS_EDITOR_ENABLED = "isEditorEnabled";
    private static final String IS_DASHBOARD_ENABLED = "dashBoardEnabled";

    private static final String IS_ADD_PERMISSION = "addPermission";
    private static final String IS_UPDATE_PERMISSION = "updatePermission";
    private static final String IS_DELETE_PERMISSION = "deletePermission";
    private static final String IS_ATTRIBUTES_PERMISSION = "attributePermission";

    public static final String USER_PROFILE_OBJECT = "USER_PROFILE_OBJECT";
    public static final String USER_ENTITY_OBJECT = "USER_ENTITY_OBJECT";

    private static final String WMS_SETTING = "wms_setting";
    private static final String BASEMAP_NAME = "basemap_name";
    private static final String BASEMAP_POSITION = "basemap_position";

    private static final String JURISDICTION_FILTER = "jurisdictionFilter";

    public static final String USER_LOCATION_WS_NAME = "userLocationWSName";
    public static final String USER_LOCATION_WSS_NAME = "userLocationWSSName";




    public static void setSurveyNameList(Set<Survey> surveySet) {
        if(surveySet==null||surveySet.size()==0){
            putString(getUserName() + "_" + ASSIGNED_PROJECTS, "");
            putString(getUserName()+"_"+ASSIGNED_PROJECTS_PHASES_COUNT,ASSIGNED_PROJECTS_PHASES_COUNT_NONE);
        }else {
            Gson gson = new Gson();
            String json = gson.toJson(surveySet);
            putString(getUserName() + "_" + ASSIGNED_PROJECTS, json);
            if(surveySet.size()>1){
                putString(getUserName()+"_"+ASSIGNED_PROJECTS_PHASES_COUNT,ASSIGNED_PROJECTS_PHASES_COUNT_MULTIPLE_PROJECTS);
            }else {
                putString(getUserName()+"_"+ASSIGNED_PROJECTS_PHASES_COUNT,ASSIGNED_PROJECTS_PHASES_COUNT_SINGLE_PROJECT_SINGLE_PHASE);
            }
        }
    }

    public static List<Survey> getSurveyNameList() {

        String stringJson = getString(getUserName() + "_" + ASSIGNED_PROJECTS);

        if (TextUtils.isEmpty(stringJson)) {
            return null;
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Survey>>() {
            }.getType();
            return gson.fromJson(stringJson, type);
        }
    }

    public static void storeSurveyName(String surveyName) {
        putString(getUserName() + "_" + SURVEY_NAME, surveyName);
       if(surveyName==null||surveyName.isEmpty()){
           //putString(getUserName()+"_"+ASSIGNED_PROJECTS_PHASES_COUNT,ASSIGNED_PROJECTS_PHASES_COUNT_NONE);
           //do nothing..as here we only want to check and set value for multiple or one/zero phases
       }else {
          Survey survey = SurveyPreferenceUtility.getSurvey(surveyName);
          if(survey==null){
           //   putString(getUserName()+"_"+ASSIGNED_PROJECTS_PHASES_COUNT,ASSIGNED_PROJECTS_PHASES_COUNT_NONE);
              //do nothing..as here we only want to check and set value for multiple or one/zero phases
          }else {
              if(survey.hasPhases()){
                  HashMap<String,Phase> phaseHashMap = survey.getPhasesNameMapFromJson();
                  if(phaseHashMap==null||phaseHashMap.isEmpty()||phaseHashMap.size()==1){
                        //change only if current value for project count is not multiple projects
                      if(!getString(getUserName()+"_"+ASSIGNED_PROJECTS_PHASES_COUNT).equalsIgnoreCase(ASSIGNED_PROJECTS_PHASES_COUNT_MULTIPLE_PROJECTS)){
                          putString(getUserName()+"_"+ASSIGNED_PROJECTS_PHASES_COUNT,ASSIGNED_PROJECTS_PHASES_COUNT_SINGLE_PROJECT_SINGLE_PHASE);
                      }
                  }else {
                      //change only if current value for project count is not multiple projects
                      if(!getString(getUserName()+"_"+ASSIGNED_PROJECTS_PHASES_COUNT).equalsIgnoreCase(ASSIGNED_PROJECTS_PHASES_COUNT_MULTIPLE_PROJECTS)){
                          putString(getUserName()+"_"+ASSIGNED_PROJECTS_PHASES_COUNT,ASSIGNED_PROJECTS_PHASES_COUNT_SINGLE_PROJECT_MULTIPLE_PHASE);
                      }
                  }
              }else {

                  //do nothing..as here we only want to check and set value for multiple or one/zero phases
                  //the value might be already set to multiple survey. we dont want to disturb that
              }
          }
       }
    }

    public static String getSurveyName() {
        return getString(getUserName() + "_" + SURVEY_NAME);
    }

    public static String getAssignedProjectsPhasesCount() {
        return getString(getUserName() + "_" + ASSIGNED_PROJECTS_PHASES_COUNT);
    }

    public static void storeSurveyNameLabel(String surveyLabel) {
        putString(getUserName() + "_" + SURVEY_NAME_LABEL, surveyLabel);
        displayLabel_PhaseORSurveyLabel=surveyLabel;
    }

    public static String getSurveyNameLabel() {
        return getString(getUserName() + "_" + SURVEY_NAME_LABEL);
    }
    public static void storePreviousSurveyNameLabel(String surveyLabel) {
        putString(getUserName() + "_PREVIOUS_" + SURVEY_NAME_LABEL, surveyLabel);
    }
    public static String getPreviousSurveyNameLabel() {
        return getString(getUserName() + "_PREVIOUS_" + SURVEY_NAME_LABEL);
    }
    public static void storePreviousSurveyName(String surveyName) {
        putString(getUserName() + "_PREVIOUS_" + SURVEY_NAME, surveyName);
    }
    public static String getPreviousSurveyName() {
        return getString(getUserName() + "_PREVIOUS_" + SURVEY_NAME);
    }
    public static void resetSelectedSurveyName() {
        storeSurveyName(getPreviousSurveyName());
        storeSurveyNameLabel(getPreviousSurveyNameLabel());
    }


    public static void storeSurveyPhaseName(String surveyName,String phaseName) {
        putString(getUserName() + "_" + SURVEY_NAME+"_"+surveyName+"_"+SURVEY_PHASE_NAME, phaseName);
    }

    public static String getSurveyPhaseName(String surveyName) {
        return getString(getUserName() + "_" + SURVEY_NAME+"_"+surveyName+"_"+SURVEY_PHASE_NAME);
    }

    public static void storeSurveyPhaseLabel(String surveyName, String phaseLabel) {
        putString(getUserName() + "_" + SURVEY_NAME+"_"+surveyName+"_"+SURVEY_PHASE_LABEL, phaseLabel);
        displayLabel_PhaseORSurveyLabel = phaseLabel;
    }

    public static String getSurveyPhaseLabel(String surveyName) {
        return getString(getUserName() + "_" + SURVEY_NAME+"_"+surveyName+"_"+SURVEY_PHASE_LABEL);
    }
    public static void storePreviousSurveyPhaseLabel(String surveyName,String phaseLabel) {
        putString(getUserName() + "_PREVIOUS_" + SURVEY_NAME+"_"+surveyName+"_"+SURVEY_PHASE_LABEL, phaseLabel);
    }
    public static String getPreviousSurveyPhaseLabel(String surveyName) {
        return getString(getUserName() + "_PREVIOUS_" + SURVEY_NAME+"_"+surveyName+"_"+SURVEY_PHASE_LABEL);
    }
    public static void storePreviousSurveyPhaseName(String surveyName,String phaseName) {
        putString(getUserName() + "_PREVIOUS_" + SURVEY_NAME+"_"+surveyName+"_"+SURVEY_PHASE_NAME, phaseName);
    }
    public static String getPreviousSurveyPhaseName(String surveyName) {
        return getString(getUserName() + "_PREVIOUS_" + SURVEY_NAME+"_"+surveyName+"_"+SURVEY_PHASE_NAME);
    }
    public static void resetSelectedSurveyPhaseName(String surveyName) {
        storeSurveyPhaseName(surveyName,getPreviousSurveyPhaseName(surveyName));
        storeSurveyPhaseLabel(surveyName,getPreviousSurveyPhaseLabel(surveyName));
    }

    public static String getPhaseFlowName(String entityName) {
        try {
            String phaseName = getSurveyPhaseName(getSurveyName());
            HashMap<String, Phase> phasesList = SurveyPreferenceUtility.getSurvey(getSurveyName()).getPhasesNameMapFromJson();
            if (phasesList != null && phasesList.containsKey(phaseName)) {
                Phase phase = phasesList.get(phaseName);
                return phase.getEntityFlowNameMap().get(entityName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    private static String displayLabel_PhaseORSurveyLabel = "";
    public static String getPhaseORSurveyName() {
        if(displayLabel_PhaseORSurveyLabel==null ||displayLabel_PhaseORSurveyLabel.isEmpty()) {
            String surveyName = UserInfoPreferenceUtility.getSurveyName();
            String phaseLabel = UserInfoPreferenceUtility.getSurveyPhaseLabel(surveyName);
            displayLabel_PhaseORSurveyLabel = phaseLabel;
            if (phaseLabel.isEmpty()) {
                displayLabel_PhaseORSurveyLabel = UserInfoPreferenceUtility.getSurveyNameLabel();
            }
        }
        return displayLabel_PhaseORSurveyLabel;
    }

    public static void storeRole(String role) {
        putString(ROLE, role);
    }

    public static String getRole() {
        return getString(ROLE);
    }

    public static void storePosition(String position) {
        putString(POSITION, position);
    }

    public static String getPosition() {
        return getString(POSITION);
    }

    public static void storeLastName(String lastName) {
        putString(LAST_NAME, lastName);
    }

    public static String getLastName() {
        return getString(LAST_NAME);
    }

    public static void storeFirstName(String firstName) {
        putString(FIRST_NAME, firstName);
    }

    public static String getFirstName() {
        return getString(FIRST_NAME);
    }

    public static void storePhoneNumber(String phoneNumber) {
        putString(PHONE_NUMBER, phoneNumber);
    }

    public static String getPhoneNumber() {
        return getString(PHONE_NUMBER);
    }

    public static void storeRedbTimestamp(String timestamp) {
        putString(REDB_TIMESTAMP, timestamp);
    }

    public static String getRedbTimestamp() {
        return getString(REDB_TIMESTAMP);
    }

    public static void storeUserName(String userName) {
        putString(USER_NAME, userName);
    }

    public static String getUserName() {
        return getString(USER_NAME);
    }

    public static void storeOrgName(String orgName) {
        putString(ORG_NAME, orgName);
    }

    public static String getOrgName() {
        return getString(ORG_NAME);
    }

    public static void storeOrgLabel(String orgLabel) {
        putString(ORG_LABEL, orgLabel);
    }

    public static String getOrgLabel() {
        return getString(ORG_LABEL);
    }

    public static void storeTagLine(String tagLine) {
        putString(TAG_LINE, tagLine);
    }

    public static String getTagLine() {
        return getString(TAG_LINE);
    }

    public static void storeAppName(String appName) {
        putString(APP_NAME, appName);
    }

    public static String getAppName() {
        return getString(APP_NAME);
    }

    public static void storeJurisdictionName(String jurisdictionName) {
        putString(getUserName() + "_" + JURISDICTION_NAME, jurisdictionName);
    }

    public static String getJurisdictionName() {
        return getString(getUserName() + "_" + JURISDICTION_NAME);
    }

    public static void storeJurisdictionType(String jurisdictionType) {
        putString(getUserName() + "_" + JURISDICTION_TYPE, jurisdictionType);
    }

    public static String getJurisdictionType() {
        return getString(getUserName() + "_" + JURISDICTION_TYPE);
    }


    public static void resetAllVariables( boolean clearusername, boolean clearredbname){
        setSurveyNameList(null);
        storeSurveyName("");
        storeSurveyNameLabel("");
        storePreviousSurveyNameLabel("");
        storePreviousSurveyName("");
        resetSelectedSurveyPhaseName("");
        storeRole("");
        storePosition("");
        storeLastName("");
        storeFirstName("");
        storeOrgName("");
        storeJurisdictionName("");
        storeJurisdictionType("");
        storeOrgLabel("");
        storeTagLine("");
        storeAppName("");

        setViewerEnabled(false);
        setEditorEnabled(false);
        setUpdatePermission(false);
        setDeletePermission(false);
        setAttributesPermission(false);
        setDashboardEnabled(false);
        setReBbRequired(false);
        setDateFormat("");
        setTimeFormat("");
        setTimeStampFormat("");
        storeSelectedBaseMapName("");
        storeJurisdictionFilter(new JSONObject());
       // storeUserProfileDetails(null,null);
        setDataDbName("");
        setMetatdataDbName("");



        if(clearredbname){
            storeRedbTimestamp("");
        }
        if(clearusername){
            storeUserName("");
        }
    }


    public static void setViewerEnabled(boolean isViewer) {
        putBoolean(IS_VIEWER_ENABLED, isViewer);
    }

    public static boolean isViewerEnabled() {
        return getBoolean(IS_VIEWER_ENABLED);
    }

    public static void setEditorEnabled(boolean isEditor) {
        putBoolean(IS_EDITOR_ENABLED, isEditor);
    }

    public static boolean isReBbRequired() {
        return getBoolean(IS_REDB_REQUIRED);
    }

    public static void setReBbRequired(boolean isReBbRequired) {
        putBoolean(IS_REDB_REQUIRED, isReBbRequired);
    }

    public static String getDateFormat() {
        return getString(DATE_FORMAT);
    }

    public static String getTimeFormat() {
        return getString(TIME_FORMAT);
    }

    public static String getTimestampFormat() {
        return getString(TIMESTAMP_FORMAT);
    }

    public static void setDateFormat(String dateFormat){
        putString(DATE_FORMAT,dateFormat);
    }

    public static void setTimeFormat(String timeFormat){
        putString(TIME_FORMAT,timeFormat);
    }

    public static void setTimeStampFormat(String timeStampFormat){
        putString(TIMESTAMP_FORMAT,timeStampFormat);
    }

    public static boolean isEditorEnabled() {
        return getBoolean(IS_EDITOR_ENABLED);
    }

    public static void setAddPermission(boolean addPermission) {
        putBoolean(IS_ADD_PERMISSION, addPermission);
    }

    public static boolean isAddPermission() {
        return getBoolean(IS_ADD_PERMISSION);
    }

    public static void setUpdatePermission(boolean updatePermission) {
        putBoolean(IS_UPDATE_PERMISSION, updatePermission);
    }

    public static boolean isUpdatePermission() {
        return getBoolean(IS_UPDATE_PERMISSION);
    }

    public static void setDeletePermission(boolean deletePermission) {
        putBoolean(IS_DELETE_PERMISSION, deletePermission);
    }

    public static boolean isDeletePermission() {
        return getBoolean(IS_DELETE_PERMISSION);
    }

    public static void setAttributesPermission(boolean attributePermission) {
        putBoolean(IS_ATTRIBUTES_PERMISSION, attributePermission);
    }

    public static boolean isAttributesPermission() {
        return getBoolean(IS_ATTRIBUTES_PERMISSION);
    }

    public static void setDashboardEnabled(boolean dashboard) {
        putBoolean(IS_DASHBOARD_ENABLED, dashboard);
    }

    public static boolean isDashboardEnabled() {
        return getBoolean(IS_DASHBOARD_ENABLED);
    }

    public static void storeSelectedBasemap(int basemapPosition) {
        putInt(BASEMAP_POSITION, basemapPosition);
    }

    public static int getBasemapPosition() {
        return getInt(BASEMAP_POSITION);
    }

    public static void storeSelectedBaseMapName(String baseMapName) {
        putString(BASEMAP_NAME, baseMapName);
    }

    public static String getBaseMapName() {
        return getString(BASEMAP_NAME);
    }
    public static boolean isWmsEnabledInSettings() {
        return getBoolean(WMS_SETTING);
    }
    public static void updateWmsSetting(boolean enabled) {
         putBoolean(WMS_SETTING,enabled);
    }

    public static void storeJurisdictionFilter(JSONObject jsonObject) {
        Gson gson = new Gson();
        String json = gson.toJson(jsonObject);
        putString(UserInfoPreferenceUtility.getUserName() + "_" + JURISDICTION_FILTER, json);
    }

    public static JSONObject getJurisdictionFilterJson() {

        String stringJson = getString(UserInfoPreferenceUtility.getUserName() + "_" + JURISDICTION_FILTER);

        if (TextUtils.isEmpty(stringJson)) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.fromJson(stringJson, JSONObject.class);
        }
    }

    //=======================store user profile=================//
    public static void storeUserProfileDetails(Activity activity, UserProfileModel userProfileModel) {

        SharedPreferences mPrefsUserProfile = activity.getSharedPreferences(USER_PROFILE_OBJECT, MODE_PRIVATE);
        SharedPreferences.Editor prefsEditorUserProfile = mPrefsUserProfile.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userProfileModel);
        prefsEditorUserProfile.putString(USER_PROFILE_OBJECT, json);
        prefsEditorUserProfile.apply();
    }

    //=============Get user profile object==========//
    public static UserProfileModel getUserProfile(Activity activity) {
        SharedPreferences mPrefs = activity.getSharedPreferences(USER_PROFILE_OBJECT, MODE_PRIVATE);
        String json1 = mPrefs.getString(USER_PROFILE_OBJECT, "");
        Gson gson = new Gson();
        return gson.fromJson(json1, UserProfileModel.class);
    }


    //=============Get and set db file names==========//
    public static void setDataDbName(String datadbName) {
        String survey_phase_name = UserInfoPreferenceUtility.getSurveyName();
        if(!UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()){
            survey_phase_name+="_"+UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());
        }
        putString(UserInfoPreferenceUtility.getUserName() + "_" +survey_phase_name+"_"+ AppConstants.DATA_GP_FILE, datadbName);
    }

    public static void setMetatdataDbName(String metadatadbName) {
        String survey_phase_name = UserInfoPreferenceUtility.getSurveyName();
        if(!UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()){
            survey_phase_name+="_"+UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());
        }
        putString(UserInfoPreferenceUtility.getUserName() + "_" +survey_phase_name+"_"+ AppConstants.METADATA_FILE, metadatadbName);
    }

    public static String getDataDbName() {
        String survey_phase_name = UserInfoPreferenceUtility.getSurveyName();
        if(!UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()){
            survey_phase_name+="_"+UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());
        }
        String datadbname = getString(UserInfoPreferenceUtility.getUserName() + "_" +survey_phase_name+"_"+ AppConstants.DATA_GP_FILE);
        return datadbname;
    }

    public static String getMetatdataDbName() {
        String survey_phase_name = UserInfoPreferenceUtility.getSurveyName();
        if(!UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()){
            survey_phase_name+="_"+UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());
        }
        String metadatadbname = getString(UserInfoPreferenceUtility.getUserName() + "_"+survey_phase_name+"_" + AppConstants.METADATA_FILE);
        return metadatadbname;
    }


    //==========Get and set userLocationWSName for sending location to server==========//
    public static String getUserLocationWSName() {
        return getString(USER_LOCATION_WS_NAME);
    }
    public static void setUserLocationWSName(String userLocationWSName){
        putString(USER_LOCATION_WS_NAME,userLocationWSName);
    }
    public static String getUserLocationWSSName() {
        return getString(USER_LOCATION_WSS_NAME);
    }
    public static void setUserLocationWSSName(String userLocationWSName){
        putString(USER_LOCATION_WSS_NAME,userLocationWSName);
    }




    //--------------\\



}