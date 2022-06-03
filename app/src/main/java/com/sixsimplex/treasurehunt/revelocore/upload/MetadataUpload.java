package com.sixsimplex.treasurehunt.revelocore.upload;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.util.ProgressUtility;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SurveyPreferenceUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MetadataUpload extends AsyncTask<Void, Void, JSONObject> {

    int requestCode;
    private final WeakReference<Context> contextWeakReference;
    private final CMGraph cmGraph;
    private final String accessToken;
    private final String surveyName;
    private final String userName;
    private final JSONObject featureUploadResponseJson;
    private ProgressDialog progressDialog;
    private final IUpload iUpload;
    private final String className = "MetadataUpload";

    MetadataUpload(Context context, String accessToken, String userName, String surveyName, CMGraph cmGraph, /*GeoPackage geoPackage,*/
                   JSONObject featureUploadResponseJson, IUpload iUpload, int requestCode) {

        contextWeakReference = new WeakReference<>(context);
        this.cmGraph = cmGraph;

        this.accessToken = accessToken;
        this.surveyName = surveyName;
        this.userName = userName;

        this.iUpload = iUpload;
        this.requestCode = requestCode;
        this.featureUploadResponseJson = featureUploadResponseJson;
        ReveloLogger.info(className, "constructor", "metadata upload part begins -- ");
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {

        ReveloLogger.debug(className, "doInBackground", "getting survey");
        Survey survey = SurveyPreferenceUtility.getSurvey(surveyName);
        ReveloLogger.debug(className, "doInBackground", "getting concept model name");
        String conceptModelName = survey.getConceptModelName();
        ReveloLogger.info(className, "constructor", "metadata upload survey name:" + surveyName + " ; conceptmodelname: " + conceptModelName);
        JSONObject response = uploadMetadata(contextWeakReference.get(), conceptModelName, cmGraph);

        return response;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ReveloLogger.debug(className, "onPreExecute", "uploading metadata begins..");
        //Context context = contextWeakReference.get();
        progressDialog = ProgressUtility.showProgressDialog(contextWeakReference.get(), "Uploading metadata", "Please wait...");
    }

    @Override
    protected void onPostExecute(JSONObject layerMetadataResponse) {
        super.onPostExecute(layerMetadataResponse);

        ProgressUtility.dismissProgressDialog(progressDialog);

        try {
            featureUploadResponseJson.put(AppConstants.METADATA, layerMetadataResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ReveloLogger.info(className, "constructor", "metadata upload part ends -- moving to upload logs");
        new UploadLogs(contextWeakReference.get(), userName, accessToken, featureUploadResponseJson, iUpload, requestCode, false).execute();
    }

    private JSONObject uploadMetadata(Context context, String conceptModelName, CMGraph cmGraph) {

        JSONObject layerMetadataResponse = new JSONObject();
        try {
            //process newly added features metadata
            HashMap<String, HashMap<String, JSONObject>> layerNameFeatureMetadataMap = new HashMap<>();
            ReveloLogger.info(className, "constructor", "generating map of metadata for newly added features, if any");
            generateLayerNameFeatureMetadataMap(AppConstants.ADD, layerNameFeatureMetadataMap);
            ReveloLogger.info(className, "constructor", "generating map of metadata for recently successfully updated features, if any");
            generateLayerNameFeatureMetadataMap(AppConstants.EDIT, layerNameFeatureMetadataMap);

            if (layerNameFeatureMetadataMap.size() > 0) {
                ReveloLogger.info(className, "uploadMetadata", "iterating through layers");
                //upload
                Iterator<String> layerNameItr = layerNameFeatureMetadataMap.keySet().iterator();
                while (layerNameItr.hasNext()) {
                    String layerName = layerNameItr.next();
                    ReveloLogger.info(className, "uploadMetadata", "getting metadata map for layer " + layerName);
                    HashMap<String, JSONObject> featureIdMetadataMap = layerNameFeatureMetadataMap.get(layerName);
                    if (featureIdMetadataMap != null) {
                        ReveloLogger.info(className, "uploadMetadata", "iterating through feature ids in metadata map for layer " + layerName);
                        ReveloLogger.info(className, "uploadMetadata", "total feature ids - " + featureIdMetadataMap.size());
                        Iterator<String> featureidItr = featureIdMetadataMap.keySet().iterator();
                        while (featureidItr.hasNext()) {
                            String featureId = featureidItr.next();
                            ReveloLogger.info(className, "uploadMetadata", "uploading metadata for feature " + featureId);
                            JSONObject metadata = featureIdMetadataMap.get(featureId);
                            uploadToServerData(context, conceptModelName, layerName, featureId, metadata, progressDialog);
                        }
                        ReveloLogger.info(className, "uploadMetadata", "Uploading metadata done..");
                    }
                    else {
                        ReveloLogger.info(className, "uploadMetadata", "error getting metadata map for layer " + layerName);
                    }
                }
            }
            else {
                ReveloLogger.info(className, "uploadMetadata", "no metadata found for uploading");
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "uploadMetadata", "Exceptionn while uploading metadata done - " + e.getMessage());
            e.printStackTrace();
        }
        return layerMetadataResponse;
    }

    public void generateLayerNameFeatureMetadataMap(String operationName, HashMap<String, HashMap<String, JSONObject>> layerNameFeatureMetadataMap) {
        ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "generating metadatamap for " + operationName);
        try {
            if (layerNameFeatureMetadataMap == null) {
                ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "initializing metadatamap");
                layerNameFeatureMetadataMap = new HashMap<>();
            }
            else {
                ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "metadatamap pre-exists. size - " + layerNameFeatureMetadataMap.size());
            }

            if (featureUploadResponseJson.has(operationName)) {
                ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "upload for operation " + operationName + " was performed recently.");
                ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "getting uploaded entities list for operation " + operationName);
                JSONObject operationJsonObject = featureUploadResponseJson.getJSONObject(operationName);
                if (operationJsonObject.has(AppConstants.UPLOAD_ENTITY_ID_LIST)) {
                    JSONObject entityListObject = operationJsonObject.getJSONObject(AppConstants.UPLOAD_ENTITY_ID_LIST);
                    Iterator<String> layerNameIterator = entityListObject.keys();
                    ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "iterating through uploaded entities list");
                    while (layerNameIterator.hasNext()) {
                        String layerLabel = layerNameIterator.next();
                        ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "entity label = " + layerLabel);
                        CMEntity cmEntity = null;
                        boolean hasFlows = false;
                        String layerName = layerLabel;
                        JSONObject cmEntityResult = cmGraph.getVertex("label", layerLabel);
                        if (cmEntityResult.has("status") && cmEntityResult.getString("status").equalsIgnoreCase("success")) {
                            ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "entity with label = " + layerLabel + " found in cmgraph");
                            cmEntity = (CMEntity) cmEntityResult.get("result");
                        }
                        else {
                            ReveloLogger.error(className, "layerNameFeatureMetadataMap", "Could not find entity with label " + layerLabel + " in cmgraph");
                        }

                        if (cmEntity != null) {
                            ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "entity with label = " + layerLabel + " found in cmgraph");
                            hasFlows = cmEntity.hasFlows();
                            layerName = cmEntity.getName();
                        }
                        else {
                            ReveloLogger.error(className, "layerNameFeatureMetadataMap", "Could not find entity with label " + layerLabel + " in cmgraph");
                        }

                        if (hasFlows) {
                            ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "entity with label = " + layerLabel + " , name- " + layerName + " has flows implemented.");
                            HashMap<String, JSONObject> featureIdMetadataMap;
                            if (layerNameFeatureMetadataMap.containsKey(layerLabel)) {
                                ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "getting existing metadata for layer " + layerName + " to add new " + operationName + " ed feature's metadata");
                                featureIdMetadataMap = layerNameFeatureMetadataMap.get(layerLabel);
                            }
                            else {
                                ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "adding entry in metadata map for layer " + layerName + " to add new " + operationName + " ed feature's metadata");
                                featureIdMetadataMap = new HashMap<>();
                            }

                            //get features being added
                            //get their metadata
                            //create metadata json
                            ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "getting list for features uploaded for layer " + layerName);
                            JSONArray featureIdJsonArray = entityListObject.getJSONArray(layerLabel);
                            if (featureIdJsonArray != null && featureIdJsonArray.length() > 0) {
                                ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "list of previously uploaded " + featureIdJsonArray.length() + " features found for layer " + layerName);
                                for (int i = 0; i < featureIdJsonArray.length(); i++) {
                                    String featureId = featureIdJsonArray.getString(i);
                                    JSONObject metadataEntry = cmEntity.getFeatureTable().getMetadataEntry(contextWeakReference.get(), layerName, featureId);
                                    featureIdMetadataMap.put(featureId, metadataEntry);
                                    ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "operation:" + operationName + " layername:" + layerName + " featureid:" + featureId + " metadataentry: " + metadataEntry.toString() + " added");
                                }
                                layerNameFeatureMetadataMap.put(layerName, featureIdMetadataMap);
                            }
                            else {
                                ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "no previously uploaded features found for layer " + layerName + ". Not adding to metadatamap");
                            }
                        }
                        else {
                            ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "entity with label = " + layerLabel + " , name- " + layerName + " does not have flows. skipped.");
                        }

                    }
                }
                else {
                    ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "no uploaded entities list found for operation " + operationName);
                }
            }
            else {
                ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "upload for operation " + operationName + " was performed recently.");
                ReveloLogger.info(className, "constructor", "no " + operationName + "ed features found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadToServerData(Context context, String conceptModelName, String layerName, String featureId, JSONObject metadataJson, ProgressDialog progressDialog) {

        try {

            //  ProgressUtility.changeProgressDialogMessage(progressDialog, "Uploading metadata for layer "+layerName+": feature "+featureId + "...", "Please wait...");


            JSONObject payloadJson = new JSONObject();
            payloadJson.put("flowName", metadataJson.getString("currentFlowName"));
            payloadJson.put("currentInteractionName", metadataJson.getString("currentInteractionName"));

            ReveloLogger.info(className, "uploadToServerData", "getting url for metadata upload ");
            String uploadUrl = UrlStore.metadataUploadUrl(conceptModelName, layerName, featureId);

            String requestBody = payloadJson.toString();

            RequestQueue mRequestQueue = Volley.newRequestQueue(context);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadUrl, response -> {
                ReveloLogger.debug(className, "uploadToServerData", "Response for layer " + layerName + ": feature " + featureId + " upload metadata operation." + response);
                handleSuccessResponse(layerName, featureId, progressDialog);
            },

                    error -> {
                        ReveloLogger.debug(className, "uploadToServerData", "Response for layer " + layerName + ": feature " + featureId + " upload metadata operation." + error);
                        handleFailedResponse(layerName, featureId, progressDialog, error);
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
            ReveloLogger.info(className, "uploadToServerData", "Exception while uploading metadata for " + featureId + " - " + layerName + ". Exception - " + e.getMessage());
            e.printStackTrace();
            ProgressUtility.dismissProgressDialog(progressDialog);//dismiss when exception occurred
        }
    }

    private void handleSuccessResponse(String layername, String featureId, ProgressDialog progressDialog) {
        ProgressUtility.dismissProgressDialog(progressDialog);//dismiss when failed to upload
        ReveloLogger.info(className, "generateLayerNameFeatureMetadataMap", "Uploading feature metadata succeeded for layer " + layername + ", feature -" + featureId);
    }

    private void handleFailedResponse(String layername, String featureId, ProgressDialog progressDialog, VolleyError error) {
        ReveloLogger.error(className, "generateLayerNameFeatureMetadataMap", "Uploading feature metadata failed for layer " + layername + ", feature -" + featureId);
        try {
            ProgressUtility.dismissProgressDialog(progressDialog);//dismiss when failed to upload
            ReveloLogger.error(className, "generateLayerNameFeatureMetadataMap", "Reason - " + error.getMessage());
        } catch (Exception e) {
            ReveloLogger.error(className, "generateLayerNameFeatureMetadataMap", "Exception getting failure Reason - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
