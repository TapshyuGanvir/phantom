package com.sixsimplex.treasurehunt.Phantom1.CURD.upload;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.revelocore.data.FeatureTable;
import com.sixsimplex.treasurehunt.revelocore.editing.model.Attachment;
import com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil.EditMetaDataTable;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.upload.UploadLogs;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SurveyPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UploadAddedFeature {
    boolean hasAttachments = false;
    boolean uploadTraversalGraph = false;
    List<Attachment> allAttachmentsList = null;
    String conceptModelName = "";
    String className = "UploadEditedFeature";
    Handler mHandler = new Handler(Looper.getMainLooper());
    CMGraph cmGraph;
    String w9Id;
    String featureLabel;
    private Context context;
    private UploadInterface uploadInterface;
    private String userName;
    private String surveyName;
    private String accessToken;
    private String entityName;


    public UploadAddedFeature(Context context, UploadInterface uploadInterface, String entityName, CMGraph cmGraph, String w9Id, String featureLabel, boolean hasAttachments, boolean uploadTraversalGraph) {
        this.context = context;
        this.uploadInterface = uploadInterface;
        this.entityName = entityName;
        this.cmGraph = cmGraph;
        this.hasAttachments = hasAttachments;
        this.uploadTraversalGraph = uploadTraversalGraph;
        this.w9Id = w9Id;
        if (featureLabel == null || featureLabel.isEmpty()) {
            this.featureLabel=w9Id;
        }else {
            this.featureLabel = featureLabel;
        }

        ReveloLogger.info(className, "UploadEditedFeature", "starting feature upload...");
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                uploadData();
            }
        }).start();
    }


    private void uploadData() {
        try{
            try {
                userName = UserInfoPreferenceUtility.getUserName();
                surveyName = UserInfoPreferenceUtility.getSurveyName();
                accessToken = SecurityPreferenceUtility.getAccessToken();
                Survey survey = SurveyPreferenceUtility.getSurvey(surveyName);
                conceptModelName = survey.getConceptModelName();
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject payloadJson = createPayloadJson(context);
            if(payloadJson.has("status")){
                if(payloadJson.getString("status").equalsIgnoreCase("success")){
                    if (!payloadJson.has("dataJson")) {
                        updateMetaDataEntryIfFailed();
                        uploadInterface.OnUploadFinished(false, payloadJson);
                    } else {
                        JSONObject dataJson = payloadJson.getJSONObject("dataJson");
                        uploadFeatureToServerData(AppConstants.ADD, dataJson);
                    }
                }else{
                    updateMetaDataEntryIfFailed();
                    uploadInterface.OnUploadFinished(false, payloadJson);
                }
            }else{
                updateMetaDataEntryIfFailed();
                uploadInterface.OnUploadFinished(false, payloadJson);
            }
        }catch (Exception e){
            updateMetaDataEntryIfFailed();
            uploadInterface.OnUploadFinished(false, new JSONObject());
            e.printStackTrace();
        }

    }

    private JSONObject createPayloadJson(Context context) {
        JSONObject processJson = new JSONObject();
        try {
            processJson.put("status", "failure");
            processJson.put("message", "unknown");
            try {

                if (cmGraph == null) {
                    JSONObject graphResult = CMUtils.getCMGraph(context);
                    if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                        cmGraph = (CMGraph) graphResult.get("result");
                    }
                }
                Map<String, HashMap<Object, String>> addedIdMap=new LinkedHashMap<>();
                try {
                    HashMap<Object, String> idList = new HashMap<>();
                    idList.put(w9Id, featureLabel);
                    addedIdMap.put(entityName, idList);
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (cmGraph != null && addedIdMap != null && !addedIdMap.isEmpty()) {
                    JSONObject payload = createFeatureJson(surveyName, addedIdMap, cmGraph, AppConstants.ADD);//create edit json
                    if (payload != null) {
                        processJson.put("status", "success");
                        processJson.put("dataJson", payload);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processJson;
    }

    private JSONObject createFeatureJson(String surveyName, Map<String, HashMap<Object, String>> idMap, CMGraph cmGraph, String operationName) {
        JSONObject graphJsonObject = null;
        if (idMap.size() > 0) {
            JSONObject featureJsonObject = new JSONObject();
            JSONObject featureIdJsonObject = new JSONObject();
            JSONObject featureIdLabelJsonObject = new JSONObject();

            for (String layerName : idMap.keySet()) {
                try {
                    JSONObject cmEntityResult = cmGraph.getVertex("name", layerName);
                    if (cmEntityResult.has("status") && cmEntityResult.getString("status").equalsIgnoreCase("success")) {
                        CMEntity cmEntity = (CMEntity) cmEntityResult.get("result");
                        FeatureTable featureTable = cmEntity.getFeatureTable();
                        if (featureTable != null) {
                            HashMap<Object, String> idList = idMap.get(layerName);
                            if (idList != null && idList.size() > 0) {
                                JSONArray featureIdJsonArray = new JSONArray();//needed to uplod data
                                JSONArray featureIdLableJsonArray = new JSONArray();//needed to show results
                                for (Object id : idList.keySet()) {
                                    try {
                                        JSONObject jsonObject = featureTable.createUploadJsonObject(id, surveyName, context, operationName);
                                        featureJsonObject.put(surveyName + "_" + id, jsonObject);
                                        featureIdJsonArray.put(id);

                                        JSONObject featureJson = new JSONObject();
                                        featureJson.put("w9id", id);
                                        featureJson.put("featureLabel", idList.get(id));
                                        featureIdLableJsonArray.put(featureJson);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                try {
                                    featureIdJsonObject.put(cmEntity.getLabel(), featureIdJsonArray);
                                    featureIdLabelJsonObject.put(cmEntity.getLabel(), featureIdLableJsonArray);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (featureJsonObject.length() > 0) {

                JSONObject verticesJsonObject = new JSONObject();
                graphJsonObject = new JSONObject();
                try {
                    graphJsonObject.put(GraphConstants.GRAPH, verticesJsonObject);
                    verticesJsonObject.put(GraphConstants.VERTICES, featureJsonObject);
                    verticesJsonObject.put(GraphConstants.EDGES, new JSONArray());
                    verticesJsonObject.put(AppConstants.UPLOAD_ENTITY_ID_LIST, featureIdJsonObject);
                    //graphJsonObject.put("UploadedFeatures",featureIdLabelJsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        return graphJsonObject;
    }

    private void uploadFeatureToServerData(String operationName, JSONObject dataJson) {
        String taskName = "uploadFeatureToServerData";
        try {


            JSONObject payloadJson = dataJson;
            String uploadUrl = UrlStore.dataUploadUrl(userName, surveyName, operationName);

            String requestBody = payloadJson.toString();

            RequestQueue mRequestQueue = Volley.newRequestQueue(context);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadUrl, response -> {
                ReveloLogger.debug(className, taskName, "Response of " + operationName + " operation." + response);
                handleFeatureEditSuccessResponse();
            },

                    error -> {
                        handleFeatureEditFailedResponse(uploadInterface, error, "");
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
            updateMetaDataEntryIfFailed();
            handleFeatureEditFailedResponse(uploadInterface, null,e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleFeatureEditSuccessResponse() {
        String taskName = "handleFeatureEditSuccessResponse";
        ReveloLogger.info(className, taskName, "Feature edit succeeded!!");



        if (hasAttachments && allAttachmentsList != null && !allAttachmentsList.isEmpty() && conceptModelName != null && !conceptModelName.isEmpty()) {
            ReveloLogger.info(className, "handleFeatureEditSuccessResponse", "preparing to upload attachments");
            new UploadEditedFeatureAttachments(context, accessToken,entityName, userName, surveyName, conceptModelName, cmGraph, uploadInterface, uploadTraversalGraph).execute();
        } else {
            ReveloLogger.error(className, "handleFeatureEditSuccessResponse", "no attachments to upload..");
            if (uploadTraversalGraph) {
                ReveloLogger.info(className, "handleFeatureEditSuccessResponse", "preparing to upload traversal");
                new TraversalUploadNew(context, accessToken, userName, surveyName, conceptModelName, cmGraph, uploadInterface).execute();
            } else {
                if (uploadInterface != null) {
                    uploadInterface.OnUploadFinished(true, new JSONObject());
                }
            }
        }
    }

    public void handleFeatureEditFailedResponse(UploadInterface uploadInterface, VolleyError error, String ExceptionMessage) {
//        EditMetaDataTable.insertAddRecord(w9Id, label, context, featureLayer.getName());
        updateMetaDataEntryIfFailed();
        String taskName = "handleFeatureEditFailedResponse";
        ReveloLogger.error(className, taskName, "Feature edit failed!!");
        JSONObject errorJson = new JSONObject();
        try {
            String errorMsg = "No information available";
            errorJson.put("message", errorMsg);
            try {
                if(error != null){
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null) {

                        byte[] byteData = networkResponse.data;

                        if (byteData != null) {

                            String errorMsgData = new String(byteData, StandardCharsets.UTF_8);
                            if (!TextUtils.isEmpty(errorMsgData)) {
                                JSONObject errorObject = new JSONObject(errorMsgData);
                                if (errorObject.has("message")) {
                                    errorMsg = errorObject.getString("message");
                                }
                            } else {
                                errorMsg = errorMsgData;
                            }
                        }
                    }
                    if (errorMsg == null || errorMsg.equalsIgnoreCase("No information available") || errorMsg.isEmpty()) {
                        errorMsg = error.getMessage();
                        if (TextUtils.isEmpty(errorMsg)) {
                            errorMsg = error.getLocalizedMessage();
                            if (TextUtils.isEmpty(errorMsg)) {
                                Throwable cause = error.getCause();
                                if (cause == null) {
                                    errorMsg = "Server unable to send response message";
                                } else {
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
                }else{
                    errorJson.put("message", ExceptionMessage);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        if (uploadInterface != null) {
            uploadInterface.OnUploadFinished(false, errorJson);
            ReveloLogger.debug(className, taskName, "uploading logs..");
            new UploadLogs(context, userName, accessToken, null, null, 11, true).execute();
        }
    }

    private void updateMetaDataEntryIfFailed(){
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    EditMetaDataTable.insertAddRecord(w9Id, featureLabel, context, entityName);  // insert add entry to editmetadata.
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
