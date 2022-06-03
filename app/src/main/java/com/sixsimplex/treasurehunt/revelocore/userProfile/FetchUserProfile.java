package com.sixsimplex.treasurehunt.revelocore.userProfile;

import android.app.Activity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FetchUserProfile {
public String className = "FetchUserProfile";

    public FetchUserProfile(Activity activity, FetchUserProfileListener fetchUserProfileListener) {
        getUserProfile(activity,fetchUserProfileListener);
    }

    private void getUserProfile(Activity activity,FetchUserProfileListener fetchUserProfileListener) {
        ReveloLogger.debug(className, "getUserProfile", "Getting user profile for user "+UserInfoPreferenceUtility.getUserName());
        String userProfileUrl = UrlStore.getUserProfileUrl(UserInfoPreferenceUtility.getUserName());
        if (userProfileUrl.trim().length() != 0) {

            try {

                StringRequest myReq = new StringRequest(Request.Method.GET, userProfileUrl, response -> {

                    try {

                        UserProfileModel userProfileModel = getUserProfile(response);
                        ReveloLogger.debug(className, "getUserProfile", "saving user profile for.."+UserInfoPreferenceUtility.getUserName());
                        UserInfoPreferenceUtility.storeUserProfileDetails(activity, userProfileModel);
                        fetchUserProfileListener.onUserProfileFetchingSuccessfull();
                    } catch (Exception e) {
                        ReveloLogger.error(className, "getUserProfile", "got user profile from server, but exception while saving.."+e.getMessage());
                        e.printStackTrace();
                        //logger
                    }
                }, error -> {
                    String errorDescription = NetworkUtility.getErrorFromVolleyError(error);
                    ReveloLogger.error(className, "getUserProfile", "Error getting user profile from server.."+errorDescription);
                    fetchUserProfileListener.onUserProfileFetchingFailed();
                    //logger
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
                ReveloLogger.error(className, "getUserProfile", "Error getting user profile from server.."+e.getMessage());
                e.printStackTrace();
                //logger
            }
        }
    }

    private UserProfileModel getUserProfile(String userProfileResponse) {

        UserProfileModel userProfileModel = new UserProfileModel();

        try {
//            String userProfile = "{\"userName\":\"User1\",\"creationDate\":\"Not Available\",\"startDate\":\"Not Available\",\"endDate\":\"Not Available\",\"privileges\":{\"viewer\":{\"enabled\":true,\"features\":{}},\"editor\":{\"enabled\":true,\"features\":{\"geometry\":{\"add\":{\"enabled\":true},\"update\":{\"enabled\":true},\"delete\":{\"enabled\":false}},\"attributes\":{\"enabled\":true}}},\"dashboard\":{\"enabled\":true,\"features\":{}},\"users\":{\"enabled\":true,\"features\":{}},\"downloads\":{\"enabled\":true,\"features\":{\"attachments\":{\"images\":true,\"video\":true},\"entities\":{\"shapefile\":true,\"excel\":true}}}}}";
            JSONObject jsonObject = new JSONObject(userProfileResponse);

            String userName = jsonObject.has("userName") ? jsonObject.getString("userName") : "";
            String creationDate = jsonObject.has("creationDate") ? jsonObject.getString("creationDate") : "";
            String startDate = jsonObject.has("startDate") ? jsonObject.getString("startDate") : "";
            String endDate = jsonObject.has("endDate") ? jsonObject.getString("endDate") : "";


            JSONObject jurisdictionFilerObject = jsonObject.has("jurisdictionFilters") ? jsonObject.getJSONObject("jurisdictionFilters") : null;



            if (jurisdictionFilerObject != null) {

                           /* jurisdictionFilerObject = new JSONObject("{\"ranges\":\"INTERSECTS(the_geom,collectGeometries(queryCollection('mecljurisdictionsdsws:ranges','the_geom','name=''Maharashtra''')))\"," +
                                    "\"state\":\"name='Maharashtra'\",\"circles\":\"INTERSECTS(the_geom,collectGeometries(queryCollection('mecljurisdictionsdsws:circles','the_geom','name=''Maharashtra''')))\"," +
                                    "\"beats\":\"INTERSECTS(the_geom,collectGeometries(queryCollection('mecljurisdictionsdsws:beats','the_geom','name=''Maharashtra''')))\"," +
                                    "\"rounds\":\"INTERSECTS(the_geom,collectGeometries(queryCollection('mecljurisdictionsdsws:rounds','the_geom','name=''Maharashtra''')))\"," +
                                    "\"divisions\":\"INTERSECTS(the_geom,collectGeometries(queryCollection('mecljurisdictionsdsws:divisions','the_geom','name=''Maharashtra''')))\"}");
*/
                UserInfoPreferenceUtility.storeJurisdictionFilter(jurisdictionFilerObject);
                ReveloLogger.debug(className, "userProfileModel", "jurisdiction filter stored successfully" + jurisdictionFilerObject.toString());
            }else {
                ReveloLogger.error(className, "userProfileModel", "no jurisdiction filter received in userinfo");
            }

            userProfileModel.setUserName(userName);
            userProfileModel.setCreationDate(creationDate);
            userProfileModel.setStartDate(startDate);
            userProfileModel.setEndDate(endDate);

            JSONObject jsonObjectPrivileges = jsonObject.has("privileges") ? jsonObject.getJSONObject("privileges") : null;

            if (jsonObjectPrivileges != null) {
                JSONObject jsonObjectViewer = jsonObjectPrivileges.has("viewer") ? jsonObjectPrivileges.getJSONObject("viewer") : null;
                if (jsonObjectViewer != null) {
                    boolean isViewerEnabled = jsonObjectViewer.has("mobileEnabled") && jsonObjectViewer.getBoolean("mobileEnabled");

                    userProfileModel.setViewerEnable(isViewerEnabled);
                }

                JSONObject jsonObjectEditor = jsonObjectPrivileges.has("editor") ? jsonObjectPrivileges.getJSONObject("editor") : null;
                if (jsonObjectEditor != null) {
                    boolean isEditorEnabled = jsonObjectEditor.has("mobileEnabled") && jsonObjectEditor.getBoolean("mobileEnabled");
                    userProfileModel.setEditorEnable(isEditorEnabled);

                    JSONObject jsonObjectFeature = jsonObjectEditor.has("features") ? jsonObjectEditor.getJSONObject("features") : null;
                    if (jsonObjectFeature != null) {
                        JSONObject jsonObjectGeometry = jsonObjectFeature.has("geometry") ? jsonObjectFeature.getJSONObject("geometry") : null;
                        if (jsonObjectGeometry != null) {
                            JSONObject jsonObjectGeometryAdd = jsonObjectGeometry.has("add") ? jsonObjectGeometry.getJSONObject("add") : null;
                            if (jsonObjectGeometryAdd != null) {
                                boolean isGeometryAddEnable = jsonObjectGeometryAdd.has("enabled") && jsonObjectGeometryAdd.getBoolean("enabled");
                                userProfileModel.setGeoAdd(isGeometryAddEnable);
                            }

                            JSONObject jsonObjectGeometryUpdate = jsonObjectGeometry.has("update") ? jsonObjectGeometry.getJSONObject("update") : null;
                            if (jsonObjectGeometryUpdate != null) {
                                boolean isGeometryUpdateEnable = jsonObjectGeometryUpdate.has("enabled") && jsonObjectGeometryUpdate.getBoolean("enabled");
                                userProfileModel.setGeoUpdate(isGeometryUpdateEnable);
                            }

                            JSONObject jsonObjectGeometryDelete = jsonObjectGeometry.has("delete") ? jsonObjectGeometry.getJSONObject("delete") : null;
                            if (jsonObjectGeometryDelete != null) {
                                boolean isGeometryDeleteEnable = jsonObjectGeometryDelete.has("enabled") && jsonObjectGeometryDelete.getBoolean("enabled");
                                userProfileModel.setGeoDelete(isGeometryDeleteEnable);

                            }
                        }
                        JSONObject jsonObjectAttributes = jsonObjectFeature.has("attributes") ? jsonObjectFeature.getJSONObject("attributes") : null;
                        if (jsonObjectAttributes != null) {
                            boolean isAttributeEnable = jsonObjectAttributes.has("enabled") && jsonObjectAttributes.getBoolean("enabled");
                            userProfileModel.setAttributeEnable(isAttributeEnable);
                        }
                    }
                }

                JSONObject jsonObjectDashboard = jsonObjectPrivileges.has("dashboard") ? jsonObjectPrivileges.getJSONObject("dashboard") : null;
                if (jsonObjectDashboard != null) {
                    boolean isDashboardEnabled = jsonObjectDashboard.has("mobileEnabled") && jsonObjectDashboard.getBoolean("mobileEnabled");
                    userProfileModel.setDashboardEnable(isDashboardEnabled);
                }

                JSONObject jsonObjectUsers = jsonObjectPrivileges.has("users") ? jsonObjectPrivileges.getJSONObject("users") : null;
                if (jsonObjectUsers != null) {
                    boolean isUserEnabled = jsonObjectUsers.has("enabled") && jsonObjectUsers.getBoolean("enabled");
                    userProfileModel.setUserEnable(isUserEnabled);
                }

                JSONObject jsonObjectDownloads = jsonObjectPrivileges.has("downloads") ? jsonObjectPrivileges.getJSONObject("downloads") : null;
                if (jsonObjectDownloads != null) {
                    boolean isDownloadsEnabled = jsonObjectDownloads.has("enabled") && jsonObjectDownloads.getBoolean("enabled");
                    userProfileModel.setDownloadEnable(isDownloadsEnabled);

                    JSONObject jsonObjectDownloadFeatures = jsonObjectDownloads.has("features") ? jsonObjectDownloads.getJSONObject("features") : null;
                    if (jsonObjectDownloadFeatures != null) {
                        JSONObject jsonObjectDownloadFeaturesAttachments = jsonObjectDownloadFeatures.has("attachments") ? jsonObjectDownloadFeatures.getJSONObject("attachments") : null;
                        if (jsonObjectDownloadFeaturesAttachments != null) {
                            boolean isDownloadAttachmentsImage = jsonObjectDownloadFeaturesAttachments.has("images") && jsonObjectDownloadFeaturesAttachments.getBoolean("images");
                            boolean isDownloadAttachmentsvideo = jsonObjectDownloadFeaturesAttachments.has("video") && jsonObjectDownloadFeaturesAttachments.getBoolean("video");

                            userProfileModel.setDownloadAttachImage(isDownloadAttachmentsImage);
                            userProfileModel.setDownloadAttachVideo(isDownloadAttachmentsvideo);
                        }

                        JSONObject jsonObjectDownloadFeaturesEntities = jsonObjectDownloadFeatures.has("entities") ? jsonObjectDownloadFeatures.getJSONObject("entities") : null;
                        if (jsonObjectDownloadFeaturesEntities != null) {
                            boolean isDownloadEntitiesShapefile = jsonObjectDownloadFeaturesEntities.has("shapefile") && jsonObjectDownloadFeaturesEntities.getBoolean("shapefile");

                            boolean isDownloadEntitiesExcel = jsonObjectDownloadFeaturesEntities.has("excel") && jsonObjectDownloadFeaturesEntities.getBoolean("excel");
                            userProfileModel.setDownloadEntitiesShapefile(isDownloadEntitiesShapefile);
                            userProfileModel.setDownloadEntitiesExcel(isDownloadEntitiesExcel);
                        }
                    }

                }
            }

        } catch (Exception e) {
            ReveloLogger.error(className, "userProfileModel", "error saving user profile "+e.getMessage());
            e.printStackTrace();
        }


        return userProfileModel;
    }

}

