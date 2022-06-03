package com.sixsimplex.treasurehunt.revelocore.surveyDetails;

import android.app.Activity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.view.ISurveyDetails;
import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SurveyPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SurveyDetails {

    private String className = "SurveyDetails";

    public SurveyDetails(Activity activity, int requestType,String surveyName, ISurveyDetails iSurveyDetails) {
        getSurveyDetails(surveyName,requestType, activity, iSurveyDetails);
    }

    private void getSurveyDetails(String surveyName,int requestType, Activity activity, ISurveyDetails iSurveyDetails) {

        String surveyUrl = UrlStore.surveyUrl(surveyName);
        if (surveyUrl.trim().length() == 0) {
            iSurveyDetails.onError("Project details url not found for project "+surveyName);
            ReveloLogger.error(className, "getSurveyDetails", "Survey details url not found for project "+surveyName);
        } else {

//            if (progressDialog == null || !progressDialog.isShowing()) {
//                progressDialog = ProgressUtility.showProgressDialog(activity, "Retrieving project details",
//                        activity.getResources().getString(R.string.progress_message_login));
//            } else if(requestType!=AppConstants.REFRESH_DATA_REQUEST){
//                ProgressUtility.changeProgressDialogMessage(progressDialog, "Retrieving project details",
//                        activity.getResources().getString(R.string.progress_message_login));
//            }
//
//            ProgressDialog finalProgressDialog = progressDialog;

            try {


                StringRequest myReq = new StringRequest(Request.Method.GET, surveyUrl, response -> {

                    try {

                        JSONObject surveyJsonObject = new JSONObject(response);

                        String surveyStatus = surveyJsonObject.has("status") ? surveyJsonObject.getString("status") : "";

                        if (surveyStatus.equalsIgnoreCase("Started")) {

                            int id = surveyJsonObject.has("surveyId") ? surveyJsonObject.getInt("surveyId") : -1;
                            String name = surveyJsonObject.has("name") ? surveyJsonObject.getString("name") : "";
                            String label = surveyJsonObject.has("label") ? surveyJsonObject.getString("label") : "";
                            String conceptModelName = surveyJsonObject.has("conceptModelName") ? surveyJsonObject.getString("conceptModelName") : "";
                            String phases = surveyJsonObject.has("phases") ? surveyJsonObject.getString("phases") : "";
                            boolean hasPhases = surveyJsonObject.has("phases") && !phases.isEmpty();

                            int bufferDistance = 0;

                            JSONObject preferencesObject = surveyJsonObject.has("preferences") ? surveyJsonObject.getJSONObject("preferences") : null;
                            if (preferencesObject != null) {
                                bufferDistance = preferencesObject.has("bufferDistance") ? preferencesObject.getInt("bufferDistance") : 0;
                            }

                            Survey survey = new Survey();
                            survey.setId(id);
                            survey.setName(name);
                            survey.setLabel(label);
                            survey.setConceptModelName(conceptModelName);
                            survey.setBufferDistance(bufferDistance);
                            survey.setPhasesJsonString(phases);
                            survey.setHasPhases(hasPhases);
                            SurveyPreferenceUtility.storeBufferDistance(name,bufferDistance);
                            SurveyPreferenceUtility.storeSurvey(survey);

                            UserInfoPreferenceUtility.storePreviousSurveyName(UserInfoPreferenceUtility.getSurveyName());
                            UserInfoPreferenceUtility.storePreviousSurveyNameLabel(UserInfoPreferenceUtility.getSurveyNameLabel());
                            UserInfoPreferenceUtility.storeSurveyName(name);
                            UserInfoPreferenceUtility.storeSurveyNameLabel(label);

                            iSurveyDetails.onSuccess();

                        } else {
                            String message = "Survey " + surveyName + " has not started yet. Please contact your survey admin";
                            iSurveyDetails.onError(message);
                            ReveloLogger.error(className, "getSurveyDetails", message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        iSurveyDetails.onError("Something went wrong, Please try again later.");
                        ReveloLogger.error(className, "getSurveyDetails", "Something went wrong, Please try again later.");
                    }
                }, error -> {
                    String errorDescription = NetworkUtility.getErrorFromVolleyError(error);
                    iSurveyDetails.onError(errorDescription);
                    ReveloLogger.error(className, "getSurveyDetails", errorDescription);
                }) {
                    @Override
                    public Map<String, String> getHeaders() {

                        String accessToken = SecurityPreferenceUtility.getAccessToken();

                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", AppConstants.CONTENT_TYPE_APPLICATION_JSON);
                        params.put("Authorization", "Bearer " + accessToken);

                        return params;
                    }
                };

                myReq.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 1));
                RequestQueue queue = Volley.newRequestQueue(activity);
                queue.add(myReq);

            } catch (Exception e) {
                e.printStackTrace();
                iSurveyDetails.onError(e.toString());
                ReveloLogger.error(className, "getSurveyDetails", e.getMessage());
            }
        }
    }
}