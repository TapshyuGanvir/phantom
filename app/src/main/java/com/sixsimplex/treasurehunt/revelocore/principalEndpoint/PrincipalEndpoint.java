package com.sixsimplex.treasurehunt.revelocore.principalEndpoint;

import android.app.Activity;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.principalEndpoint.view.IPrincipleEndpointView;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.JurisdictionInfoPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PrincipalEndpoint {

    private final String className = "PrincipalEndpoint";

    public PrincipalEndpoint(Activity activity, int request, String limitToResponse, String accessToken, IPrincipleEndpointView iPrincipleEndpointView) {
        principleEndpoint(activity, request, limitToResponse, accessToken,iPrincipleEndpointView);
    }

    private void principleEndpoint(Activity activity, int request, String limitToResponse, String accessToken, IPrincipleEndpointView iPrincipleEndpointView) {
        try {
            ReveloLogger.debug(className, "principleEndpoint", "Call principle endpoint.");

            File reDbFile = AppFolderStructure.getReGp(activity);
            String firstTimeDownload = "false";
            String reDBTimeStamp = "";

            if (reDbFile != null) {

                reDBTimeStamp = UserInfoPreferenceUtility.getRedbTimestamp();

                if (reDBTimeStamp.isEmpty()) {

                    Date lastModDate = new Date(reDbFile.lastModified());//cant get creation date time because of API 21

                    String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"; //2018-08-02T15:55:10.439715Z
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
                    reDBTimeStamp = simpleDateFormat.format(lastModDate);
                }
            } else {
                firstTimeDownload = "true";
            }
            ReveloLogger.info(className, "principleEndpoint", "setting limitToResponse to all, firstTimeDownload="+firstTimeDownload+" reDBTimeStamp="+reDBTimeStamp);
            callPrincipleEndpoint(accessToken, limitToResponse, firstTimeDownload, reDBTimeStamp, request, activity,iPrincipleEndpointView);

        } catch (Exception e) {
            e.printStackTrace();
            iPrincipleEndpointView.onPrincipalEndPointError(request,e.toString());
            ReveloLogger.error(className, "principleEndpoint", String.valueOf(e.getCause()));
        }
    }

    private void callPrincipleEndpoint(String accessToken, String limitToResponse, String firstTimeDownload,
                                       String reDBTimeStamp, int request,
                                       Activity activityReference, IPrincipleEndpointView iPrincipleEndpointView) {

        String principalUrl = UrlStore.principalUrl(limitToResponse, firstTimeDownload, reDBTimeStamp);

        if (TextUtils.isEmpty(principalUrl)) {
            iPrincipleEndpointView.onPrincipalEndPointError(request,"");
            ReveloLogger.error(className, "callPrincipleEndpoint", "Url not found");

        } else {
//            if(progressDialog==null) {
//                progressDialog = ProgressUtility.showProgressDialog(activityReference, "User profile",
//                        activityReference.getResources().getString(R.string.progress_message_login));
//            }

            try {

//                ProgressDialog finalProgressDialog = progressDialog;
                StringRequest myReq = new StringRequest(Request.Method.GET, principalUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            /*----principal json keys------*/
                            String userInfo = "userInfo";
                        String orgUXInfo = "orgUXInfo";
                            String orgAdmin = "orgadmin";
                            String isReDbRequired = "isREDBDownloadRequired";
                            /*----------------------------------------------------*/

                            JSONObject principalJsonObject = new JSONObject(response);

                            String status = principalJsonObject.has(AppConstants.STATUS) ? principalJsonObject.getString(AppConstants.STATUS) : "";

                            if (!TextUtils.isEmpty(status) && status.equalsIgnoreCase(AppConstants.SUCCESS)) {
                                JSONObject orgUxJsonObject = principalJsonObject.has(orgUXInfo) ?
                                        principalJsonObject.getJSONObject(orgUXInfo) : null;

                                if (orgUxJsonObject != null) {


                                    String orgLabel = orgUxJsonObject.has(UserInfoPreferenceUtility.ORG_LABEL) ?
                                            orgUxJsonObject.getString(UserInfoPreferenceUtility.ORG_LABEL) : "";

                                    String tagline = orgUxJsonObject.has(UserInfoPreferenceUtility.TAG_LINE) ?
                                            orgUxJsonObject.getString(UserInfoPreferenceUtility.TAG_LINE) : "";

                                    String appName = orgUxJsonObject.has(UserInfoPreferenceUtility.APP_NAME) ?
                                            orgUxJsonObject.getString(UserInfoPreferenceUtility.APP_NAME) : "";

                                    UserInfoPreferenceUtility.storeOrgLabel(orgLabel);
                                    UserInfoPreferenceUtility.storeTagLine(tagline);
                                    UserInfoPreferenceUtility.storeAppName(appName);
                                } else {
                                    iPrincipleEndpointView.onPrincipalEndPointError(request,"Unable to get Organization logo..");
                                    ReveloLogger.error(className, "callPrincipleEndpoint", "\"Unable to get Ux values");
                                }

                                ReveloLogger.info(className, "principleEndpoint", "Successful response received..");
                                JSONObject userInfoJsonObject = principalJsonObject.has(userInfo) ?
                                        principalJsonObject.getJSONObject(userInfo) : null;

                                if (userInfoJsonObject != null) {
                                    ReveloLogger.info(className, "principleEndpoint", "valid userinfo object found..Checking if logged in user is orgadmin role type");
                                    String role = userInfoJsonObject.has(UserInfoPreferenceUtility.ROLE) ?
                                            userInfoJsonObject.getString(UserInfoPreferenceUtility.ROLE) : "";

                                    String userName = UserInfoPreferenceUtility.getUserName();
                                    if (!TextUtils.isEmpty(role) && role.equalsIgnoreCase(orgAdmin)) {
                                        ReveloLogger.info(className, "principleEndpoint", "User is orgadmin type,i.e. unauthorized to use aplication with this userid password. throwing error..");
                                        String msg = "User " + userName + " is an org admin user and is not authorized to use mobile application.";
                                        iPrincipleEndpointView.onPrincipalEndPointError(request, msg);

                                    } else {
                                        ReveloLogger.info(className, "principleEndpoint", "User is Not orgadmin type, moving forward to get assigned projects.");
                                        JSONArray assignedProjectsJsonArray = userInfoJsonObject.has(UserInfoPreferenceUtility.ASSIGNED_PROJECTS) ?
                                                userInfoJsonObject.getJSONArray(UserInfoPreferenceUtility.ASSIGNED_PROJECTS) : null;

                                        Set<Survey> projectSet = new HashSet<>();
//                                    {"name":"meclsurvey_hoff_maharashtra","label":"MECL Survey"}
                                        if (assignedProjectsJsonArray != null) {
                                            for (int i = 0; i < assignedProjectsJsonArray.length(); i++) {
                                                JSONObject jsonObject = assignedProjectsJsonArray.getJSONObject(i);

                                                String name = jsonObject.has("name") ? jsonObject.getString("name") : "";
                                                String label = jsonObject.has("label") ? jsonObject.getString("label") : "";

                                                if (name.contains("_" + userName)) {
                                                    name = name.replace("_" + userName, "");

                                                }
                                                Survey survey = new Survey();
                                                survey.setName(name);
                                                survey.setLabel(label);
                                                projectSet.add(survey);
                                            }
                                        }

                                        if (projectSet.isEmpty() || assignedProjectsJsonArray == null) {
                                            iPrincipleEndpointView.onPrincipalEndPointError(request, "You don't have any project assigned. " +
                                                    "Please contact your survey admin.");
                                            ReveloLogger.error(className, "callPrincipleEndpoint", "You don't have any project assigned. Please contact your survey admin.");

                                        } else {
                                            ReveloLogger.info(className, "principleEndpoint", "User " + userName + " has " + projectSet.size() + " projects assigned..moving to check org name");
                                            String orgName = principalJsonObject.has(UserInfoPreferenceUtility.ORG_NAME) ?
                                                    principalJsonObject.getString(UserInfoPreferenceUtility.ORG_NAME) : "";

                                            UserInfoPreferenceUtility.storeOrgName(orgName);
                                            new DownloadLogoAsyncTask(activityReference).execute();
                                            AppFolderStructure.createOrgFolder(activityReference);
                                            AppFolderStructure.createUserFolder(activityReference);

                                            ReveloLogger.info(className, "principleEndpoint", "User " + userName + " belongs to org " + orgName + ". Saving in pref, creating org and user folders");

                                            String redbTimestamp = principalJsonObject.has(UserInfoPreferenceUtility.REDB_TIMESTAMP) ?
                                                    principalJsonObject.getString(UserInfoPreferenceUtility.REDB_TIMESTAMP) : "";

                                            UserInfoPreferenceUtility.storeRedbTimestamp(redbTimestamp);
                                            ReveloLogger.info(className, "principleEndpoint", "redb time stamp received in principal json -  " + redbTimestamp);

                                            boolean isREDBRequired = principalJsonObject.has(isReDbRequired) && principalJsonObject.getBoolean(isReDbRequired);
                                            ReveloLogger.info(className, "principleEndpoint", "redb download required? as given in principal json -  " + isREDBRequired);

                                            ReveloLogger.info(className, "principleEndpoint", "Storing user role,survey names list and redbrequired...");
                                            PrincipalEndpoint.this.storeUserInfo(request, role, userInfoJsonObject, iPrincipleEndpointView);

                                            UserInfoPreferenceUtility.setSurveyNameList(projectSet);
                                            UserInfoPreferenceUtility.setReBbRequired(isREDBRequired);


                                            ReveloLogger.info(className, "principleEndpoint", "User details saved..getting locale info from principal json..");
                                            JSONObject localeInfoJsonObject = principalJsonObject.has("localeInfo") ?
                                                    principalJsonObject.getJSONObject("localeInfo") : null;

                                            if (localeInfoJsonObject != null) {
                                                JSONObject timeFormatJsonObject = localeInfoJsonObject.has("timeFormat") ?
                                                        localeInfoJsonObject.getJSONObject("timeFormat") : null;
                                                if (timeFormatJsonObject != null) {
                                                    String timeFormat = timeFormatJsonObject.has("timeFormat") ?
                                                            timeFormatJsonObject.getString("timeFormat") : "HH:mm:ss";
                                                    String dateFormat = timeFormatJsonObject.has("dateFormat") ?
                                                            timeFormatJsonObject.getString("dateFormat") : "dd-MM-yyyy";
                                                    String timestampFormat = timeFormatJsonObject.has("timeStampFormat") ?
                                                            timeFormatJsonObject.getString("timeStampFormat") : "dd-MM-yyyy HH:mm:ss";

                                                    UserInfoPreferenceUtility.setTimeFormat(timeFormat);
                                                    UserInfoPreferenceUtility.setDateFormat(dateFormat);
                                                    UserInfoPreferenceUtility.setTimeStampFormat(timestampFormat);
                                                    ReveloLogger.info(className, "principleEndpoint", "saving locale info..time format: " + timeFormat + "; date format: " + dateFormat + "; timestamp format: " + timestampFormat);
                                                }
                                            }

                                            boolean downloadRedb = false;
                                            if (firstTimeDownload.equalsIgnoreCase("true") || isREDBRequired) {
                                                downloadRedb = true;
                                            }
                                            ReveloLogger.info(className, "principleEndpoint", "redb download needed? " + downloadRedb + ".. sending this info in principal endpoint sucess message..");
                                            iPrincipleEndpointView.onPrincipalEndPointSuccess(request, downloadRedb);
                                        }
                                    }
                                    if(principalJsonObject.has("userLocationWSName")) {
                                        String userLocationWSName = principalJsonObject.getString("userLocationWSName");
                                        UserInfoPreferenceUtility.setUserLocationWSSName(userLocationWSName);
                                        UserInfoPreferenceUtility.setUserLocationWSName(userLocationWSName);
                                    }
                                } else {
                                    iPrincipleEndpointView.onPrincipalEndPointError(request, "Unable to get your profile.");
                                    ReveloLogger.error(className, "callPrincipleEndpoint", "Unable to get your profile. userinfo json object not found");
                                }
                            } else {
                                iPrincipleEndpointView.onPrincipalEndPointError(request, "failed");
                                String message = principalJsonObject.has(AppConstants.ERROR_MESSAGE) ? principalJsonObject.getString(AppConstants.ERROR_MESSAGE) : "No information available";
                                ReveloLogger.error(className, "callPrincipleEndpoint", "failed to get priciple details. details - " +message);
                            }
                        } catch (Exception e) {
                            iPrincipleEndpointView.onPrincipalEndPointError(request, "failed");
                            ReveloLogger.error(className, "callPrincipleEndpoint", "failed to get priciple details. details - " +String.valueOf(e.getCause()));
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String errorDescription = NetworkUtility.getErrorFromVolleyError(error);

                        if (TextUtils.isEmpty(errorDescription)) {
                            errorDescription = "Server not responding properly";
                        }

                        iPrincipleEndpointView.onPrincipalEndPointError(request, errorDescription);
                        ReveloLogger.error(className, "callPrincipleEndpoint", "failed to get priciple details. details - " +errorDescription);

                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", AppConstants.CONTENT_TYPE_APPLICATION_JSON);
                        params.put("Authorization", "Bearer " + accessToken);
                        return params;
                    }
                };

                myReq.setRetryPolicy(new DefaultRetryPolicy(150000, 1, 1));
                RequestQueue queue = Volley.newRequestQueue(activityReference);
                queue.add(myReq);

            } catch (Exception e) {
                e.printStackTrace();
                iPrincipleEndpointView.onPrincipalEndPointError(request,e.toString());
                ReveloLogger.error(className, "callPrincipleEndpoint", "failed to get priciple details. details - " +e.toString());
            }
        }
    }

    private void storeUserInfo(int request,String role, JSONObject userInfoJsonObject, IPrincipleEndpointView iPrincipleEndpointView) {

        try {
            ReveloLogger.error(className, "callPrincipleEndpoint", "Saving user info..");
            /*--------user info json keys------------*/
            String jurisdiction = "jurisdictions";
            String jurisdictionName = "name";
            String jurisdictionType = "type";
            String privileges = "privileges";
            String viewer = "viewer";
            String editor = "editor";
            String dashboard = "dashboard";
            String mobileEnabled = "mobileEnabled";
            String enabled = "enabled";
            String features = "features";
            String geometry = "geometry";
            String add = "add";
            String update = "update";
            String delete = "delete";
            String attributes = "attributes";
            /*---------------------------------------*/

            String firstName = userInfoJsonObject.has(UserInfoPreferenceUtility.FIRST_NAME) ? userInfoJsonObject.getString(UserInfoPreferenceUtility.FIRST_NAME) : "";
            UserInfoPreferenceUtility.storeFirstName(firstName);
            ReveloLogger.debug(className, "storeUserInfo", "Saving firstname.."+firstName);

            String lastName = userInfoJsonObject.has(UserInfoPreferenceUtility.LAST_NAME) ? userInfoJsonObject.getString(UserInfoPreferenceUtility.LAST_NAME) : "";
            UserInfoPreferenceUtility.storeLastName(lastName);
            ReveloLogger.debug(className, "storeUserInfo", "Saving lastName.."+lastName);

            String phoneNumber = userInfoJsonObject.has(UserInfoPreferenceUtility.PHONE_NUMBER) ? userInfoJsonObject.getString(UserInfoPreferenceUtility.PHONE_NUMBER) : "";
            UserInfoPreferenceUtility.storePhoneNumber(phoneNumber);
            ReveloLogger.debug(className, "storeUserInfo", "Saving phoneNumber.."+phoneNumber);

            String position = userInfoJsonObject.has(UserInfoPreferenceUtility.POSITION) ? userInfoJsonObject.getString(UserInfoPreferenceUtility.POSITION) : "";
            UserInfoPreferenceUtility.storePosition(position);
            ReveloLogger.debug(className, "storeUserInfo", "Saving position.."+position);

            UserInfoPreferenceUtility.storeRole(role);
            ReveloLogger.debug(className, "storeUserInfo", "Saving role.."+role);

            JSONArray jurisdictionArray = userInfoJsonObject.has(jurisdiction) ? userInfoJsonObject.getJSONArray(jurisdiction) : null;

            if (jurisdictionArray!=null && (!jurisdictionArray.isNull(0))) {
                JSONObject jurisdictionObject = jurisdictionArray.getJSONObject(0);

                if (jurisdictionObject != null) {
                    jurisdictionName = jurisdictionObject.getString(jurisdictionName);
                    jurisdictionType = jurisdictionObject.getString(jurisdictionType);

                    UserInfoPreferenceUtility.storeJurisdictionName(jurisdictionName);
                    UserInfoPreferenceUtility.storeJurisdictionType(jurisdictionType);
                    ReveloLogger.debug(className, "storeUserInfo", "Saving assigned jurisdiction name and type as .."+jurisdictionName+" - "+jurisdictionType);

                    JurisdictionInfoPreferenceUtility.storeSelectedJurisdictionName(jurisdictionName);
                    JurisdictionInfoPreferenceUtility.storeSelectedJurisdictionType(jurisdictionType);
                    ReveloLogger.debug(className, "storeUserInfo", "setting selected jurisdiction to same i.e. name and type as .."+jurisdictionName+" - "+jurisdictionType);

                }else {
                    ReveloLogger.error(className, "storeUserInfo", "NOT Saving jurisdiction.. as jurisdiction is empty in userinfoobject");
                }
            }else {
                ReveloLogger.error(className, "storeUserInfo", "NOT Saving jurisdiction.. as jurisdiction is empty in userinfoobject");
            }


        } catch (Exception e) {
            e.printStackTrace();
            iPrincipleEndpointView.onPrincipalEndPointError(request,e.getMessage());
            ReveloLogger.error(className, "callPrincipleEndpoint", String.valueOf(e.getCause()));
        }
    }
}