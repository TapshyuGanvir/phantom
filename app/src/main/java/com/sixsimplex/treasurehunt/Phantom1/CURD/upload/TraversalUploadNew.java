package com.sixsimplex.treasurehunt.Phantom1.CURD.upload;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.upload.UploadLogs;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SurveyPreferenceUtility;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TraversalUploadNew extends AsyncTask<Void, Void, JSONObject> {

    private final Context context;
    private final CMGraph cmGraph;
    private final String accessToken;
    private final String surveyName;
    private final String userName;
    private final UploadInterface uploadInterface;
    private final String className = "MetadataUpload";
    int requestCode = 11;
    HashMap<Integer, JSONObject> indexTraversalMap = new HashMap<>();
    String conceptModelName;

    TraversalUploadNew(Context context, String accessToken, String userName, String surveyName, String conceptModelName, CMGraph cmGraph, UploadInterface uploadInterface) {

        this.context = context;
        this.cmGraph = cmGraph;

        this.accessToken = accessToken;
        this.surveyName = surveyName;
        this.userName = userName;
        this.conceptModelName = conceptModelName;
        this.uploadInterface = uploadInterface;
        ReveloLogger.info(className, "constructor", "metadata upload part begins -- ");
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        if (conceptModelName == null || conceptModelName.isEmpty()) {
            ReveloLogger.debug(className, "doInBackground", "getting survey");
            Survey survey = SurveyPreferenceUtility.getSurvey(surveyName);
            ReveloLogger.debug(className, "doInBackground", "getting concept model name");
            String conceptModelName = survey.getConceptModelName();
            ReveloLogger.info(className, "constructor", "metadata upload survey name:" + surveyName + " ; conceptmodelname: " + conceptModelName);
        }
        JSONObject response = uploadTraversalGraphs(context, conceptModelName, cmGraph);

        return response;
    }

    private JSONObject uploadTraversalGraphs(Context context, String conceptModelName, CMGraph cmGraph) {
        if (cmGraph == null)
            return null;
        JSONObject responseJobj = new JSONObject();
        try {
            responseJobj.put("status", "failure");
            responseJobj.put("message", "failed refreshing graph on server. Reason - Unknown");

            int index = 1;
            Set<CMEntity> cmEntitySet = cmGraph.getAllVertices();
            Iterator<CMEntity> cmEntityItr = cmEntitySet.iterator();
            while (cmEntityItr.hasNext()) {
                try {
                    CMEntity cmEntity = cmEntityItr.next();
                    if (cmEntity.getTraversalGraph() != null) {
                        JSONObject payloadJson = new JSONObject();
                        payloadJson.put("traversal", cmEntity.getTraversalGraph().getuploadJson());





                        JSONObject entityPayloadJson = new JSONObject();
                        entityPayloadJson.put("payload", payloadJson.toString());
                        entityPayloadJson.put("entity", cmEntity);

                        indexTraversalMap.put(index, entityPayloadJson);
                    }
                } catch (Exception e) {
                    ReveloLogger.error(className, "doinbackground", "Exception reading graph  - " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (indexTraversalMap != null && indexTraversalMap.size() > 0) {
                responseJobj.put("status", "success");
                responseJobj.put("message", "map added");
            }
            else {
                responseJobj.put("status", "failure");
                responseJobj.put("message", "No traversal graph found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseJobj;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ReveloLogger.debug(className, "onPreExecute", "uploading metadata begins..");
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);

        if (jsonObject != null && jsonObject.has("status")) {
            try {
                if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                    uploadGraph(1);
                }
                else {
                    if (uploadInterface != null) {
                        uploadInterface.OnUploadFinished(false, jsonObject);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            if (uploadInterface != null) {
                uploadInterface.OnUploadFinished(false, jsonObject);
            }
        }

        //new UploadLogs(context, userName, accessToken, null, null, requestCode, true).execute();
    }

    private void uploadGraph(int index) {
        try {
            String requestBody = indexTraversalMap.get(index).getString("payload");
            CMEntity cmEntity = (CMEntity) indexTraversalMap.get(index).get("entity");

            ReveloLogger.info(className, "uploadToServerData", "getting url for traversal upload ");
            String uploadUrl = UrlStore.traversalUploadUrl(userName, surveyName, conceptModelName, cmEntity.getName());

            RequestQueue mRequestQueue = Volley.newRequestQueue(context);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadUrl, response -> {
                ReveloLogger.debug(className, "uploadToServerData", "Response for layer " + cmEntity.getLabel() + ":  upload traversal graph operation." + response);
                handleSuccessResponse(index, cmEntity.getName());
            },

                                                            error -> {
                                                                ReveloLogger.error(className, "uploadToServerData", "Response for layer " + cmEntity.getLabel() + ":  upload traversal graph operation." + error);
                                                                handleTraversalUploadFailure(index, cmEntity, error);
                                                            }) {

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Accept", "application/json,text/plain");
                    params.put("Accept-Encoding", "gzip");
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", "Bearer " + accessToken);

                    return params;
                }

                @Override
                public byte[] getBody() {
                    return requestBody.getBytes(StandardCharsets.UTF_8);
                }
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(AppConstants.NETWORK_TIME_OUT_MS, 1, 1));
            mRequestQueue.add(stringRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleSuccessResponse(int currentindex, String entityName) {
        ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "Uploading traversal succeeded for layer " + entityName);
        if (currentindex == indexTraversalMap.size()) {
            ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "No more gaphs to upload, uploading logs");
            new UploadLogs(context, userName, accessToken, null, null, 11, true).execute();
        }
        else {
            ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "preparing to upload next layer's traversal");
            uploadGraph(currentindex++);
        }
    }

    private void handleTraversalUploadFailure(int currentindex, CMEntity entity, VolleyError error) {
        String taskName = "handleTraversalUploadFailure";
        ReveloLogger.error(className, taskName, "TraversalUpload failed for entity " + entity.getName() + "!!");
        JSONObject errorJson = new JSONObject();
        try {
            String errorMsg = "No information available";
            errorJson.put("message", errorMsg);

            try {

                NetworkResponse networkResponse = error.networkResponse;

                if (networkResponse != null) {

                    byte[] byteData = networkResponse.data;

                    if (byteData != null) {

                        String errorMsgData = new String(byteData, StandardCharsets.UTF_8);
                        if (! TextUtils.isEmpty(errorMsgData)) {
                            JSONObject errorObject = new JSONObject(errorMsgData);
                            if (errorObject.has("message")) {
                                errorMsg = errorObject.getString("message");
                            }
                        }
                        else {
                            errorMsg = errorMsgData;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (errorMsg == null || errorMsg.equalsIgnoreCase("No information available") || errorMsg.isEmpty()) {
                errorMsg = error.getMessage();
                if (TextUtils.isEmpty(errorMsg)) {
                    errorMsg = error.getLocalizedMessage();
                    if (TextUtils.isEmpty(errorMsg)) {
                        Throwable cause = error.getCause();
                        if (cause == null) {
                            errorMsg = "Server unable to send response message";
                        }
                        else {
                            errorMsg = cause.getMessage();
                            if (TextUtils.isEmpty(errorMsg)) {
                                errorMsg = cause.getLocalizedMessage();
                                if (TextUtils.isEmpty(errorMsg)) {
                                    errorMsg = "Server unable to send response message";
                                }
                            }
                        }
                    }
                }
            }
            errorJson.put("message", errorMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (uploadInterface != null) {
            uploadInterface.OnUploadFinished(false, errorJson);
            ReveloLogger.debug(className, taskName, "uploading logs..");
            new UploadLogs(context, userName, accessToken, null, null, 11, true).execute();
        }
    }
}
