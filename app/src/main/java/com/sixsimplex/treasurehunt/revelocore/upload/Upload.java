package com.sixsimplex.treasurehunt.revelocore.upload;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageManagerAgent;
import com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil.EditMetaDataTable;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.util.ProgressUtility;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Upload {

    private String userName, surveyName, accessToken;
    private Context context;
    private IUpload iUpload;
    private String className = "Upload";
    int requestCode = 11;
    boolean isInstantUpload = false;
    private JSONObject addResponseJson, editResponseJson, deleteResponseJson;

    public Upload(Context context, IUpload iUpload,int requestCode,boolean isInstantUpload) {

        try {
            userName = UserInfoPreferenceUtility.getUserName();
            surveyName = UserInfoPreferenceUtility.getSurveyName();

            this.context = context;
            this.iUpload = iUpload;
            this.requestCode=requestCode;
            this.isInstantUpload=isInstantUpload;

            accessToken = SecurityPreferenceUtility.getAccessToken();

            if(!isInstantUpload) {
                new UploadLogs(context, userName, accessToken, null, iUpload, requestCode, true).execute();
            }
            new UploadHelper(context, surveyName, new IUploadHelper() {
                @Override
                public void onPayLoad(JSONObject dataJson, CMGraph cmGraph, ProgressDialog progressDialog) {
                    Upload.this.uploadToServerData(AppConstants.DELETE, dataJson, cmGraph,  progressDialog);
                }

                @Override
                public void onError(String errorMsg, ProgressDialog progressDialog) {
                    try {


                            deleteResponseJson = new JSONObject();
                            deleteResponseJson.put(AppConstants.STATUS, AppConstants.FAILURE);
                            deleteResponseJson.put(AppConstants.FAILURE_MESSAGE, errorMsg);


                            editResponseJson = new JSONObject();
                            editResponseJson.put(AppConstants.STATUS, AppConstants.FAILURE);
                            editResponseJson.put(AppConstants.FAILURE_MESSAGE, errorMsg);


                            addResponseJson = new JSONObject();
                            addResponseJson.put(AppConstants.STATUS, AppConstants.FAILURE);
                            addResponseJson.put(AppConstants.FAILURE_MESSAGE, errorMsg);


                        JSONObject featureUploadResponseJson = new JSONObject();

                        try {
                            featureUploadResponseJson.put(AppConstants.ADD, addResponseJson);
                            featureUploadResponseJson.put(AppConstants.EDIT, editResponseJson);
                            featureUploadResponseJson.put(AppConstants.DELETE, deleteResponseJson);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }

                        new UploadLogs(context, userName, accessToken, featureUploadResponseJson,
                                iUpload,requestCode,false).execute();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadToServerData(String operationName, JSONObject dataJson,CMGraph cmGraph,
                                    ProgressDialog progressDialog) {

        try {

            String operationDone = operationName+"ed";
            if(operationName.equalsIgnoreCase("delete")){
                operationDone = "deleted";
            }
            ProgressUtility.changeProgressDialogMessage(progressDialog, "Uploading " + operationDone + " data", "Please wait...");

            if (dataJson.has(operationName)) {

                JSONObject operationJson = dataJson.getJSONObject(operationName);
                JSONObject payloadJson = new JSONObject();
                String uploadUrl = UrlStore.dataUploadUrl(userName, surveyName, operationName);

                /*if(operationJson.has(GraphConstants.GRAPH)){
                    JSONObject graphJson = operationJson.getJSONObject(GraphConstants.GRAPH);
                    payloadJson.put(GraphConstants.GRAPH,graphJson);
                    //JSONObject uploadedFeaturesJson = (JSONObject) graphJson.remove("UploadedFeatures");
                }else {
                    payloadJson=operationJson;
                }*/
                payloadJson=operationJson;
                String requestBody = payloadJson.toString();

                RequestQueue mRequestQueue = Volley.newRequestQueue(context);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadUrl,
                        response -> {
                            ReveloLogger.debug(className, "uploadToServerData", "Response of " + operationName + " operation." + response);
                            handleSuccessResponse(operationName, dataJson, operationJson, cmGraph,   progressDialog);
                        },

                        error -> {
                            handleFailedResponse(operationName, progressDialog, error);
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

            } else {

                if (operationName.equalsIgnoreCase(AppConstants.DELETE)) {
                    ReveloLogger.debug(className, "uploadToServerData", "upload " + operationName + " done..moving for "+AppConstants.EDIT);
                    uploadToServerData(AppConstants.EDIT, dataJson, cmGraph, progressDialog);
                }
                else if (operationName.equalsIgnoreCase(AppConstants.EDIT)) {
                    ReveloLogger.debug(className, "uploadToServerData", "upload " + operationName + " done..moving for "+AppConstants.ADD);
                    uploadToServerData(AppConstants.ADD, dataJson, cmGraph, progressDialog);

                }
                else if (operationName.equalsIgnoreCase(AppConstants.ADD)) {
                    ReveloLogger.debug(className, "uploadToServerData", "upload " + operationName + " done..moving for "+AppConstants.ATTACHMENT);
                    uploadAttachment(cmGraph,/* dataGeoPackage,*/ progressDialog, isInstantUpload);//added feature not found
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ProgressUtility.dismissProgressDialog(progressDialog);//dismiss when exception occurred
        }
    }

    private void handleSuccessResponse(String operationName, JSONObject dataJson, JSONObject payloadJson, CMGraph cmGraph,
                                       ProgressDialog progressDialog) {
        ReveloLogger.debug(className, "uploadToServerData", "upload " + operationName + " operation succeeded..adding into response json");
        if (operationName.equalsIgnoreCase(AppConstants.DELETE)) {

            try {

                if (payloadJson.has(AppConstants.UPLOAD_ENTITY_ID_LIST)) {
                    JSONObject entityListObject = payloadJson.getJSONObject(AppConstants.UPLOAD_ENTITY_ID_LIST);
                    deleteResponseJson = new JSONObject();
                    deleteResponseJson.put(AppConstants.UPLOAD_ENTITY_ID_LIST, entityListObject);
                    deleteResponseJson.put(AppConstants.STATUS, AppConstants.SUCCESS);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Map<String, List<EditMetaDataTable.DeleteModel>> deletedLayerNameIdsMap = EditMetaDataTable.getDeletedId(context);
                Map<String, List<EditMetaDataTable.DeleteModel>> deletedLayerLabelIdsMap = new HashMap<>();
                for (String layerName : deletedLayerNameIdsMap.keySet()) {
                    try {
                        JSONObject cmEntityResult = cmGraph.getVertex("name", layerName);
                        if (cmEntityResult.has("status") && cmEntityResult.getString("status").equalsIgnoreCase("success")) {
                            CMEntity cmEntity = (CMEntity) cmEntityResult.get("result");
                            deletedLayerLabelIdsMap.put(cmEntity.getLabel(),deletedLayerNameIdsMap.get(layerName));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                boolean isDeleted = EditMetaDataTable.deleteOperation(context, operationName);//success response - upload deletes
                if (isDeleted) {
                    if (deleteResponseJson != null && deletedLayerLabelIdsMap != null && !deletedLayerLabelIdsMap.isEmpty()) {
                        deleteResponseJson.put("featureArray", deletedLayerLabelIdsMap);
                    }
                    ReveloLogger.debug(className, "uploadToServerData", "All deleted feature entry delete from edit metadata table.");
                } else {
                    ReveloLogger.debug(className, "uploadToServerData", "failed to delete, deleted feature entry from edit metadata table.");
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            uploadToServerData(AppConstants.EDIT, dataJson, cmGraph,   progressDialog);

        }
        else if (operationName.equalsIgnoreCase(AppConstants.EDIT)) {

            try {
                if (payloadJson.has(GraphConstants.GRAPH)) {
                    JSONObject graphObject = payloadJson.getJSONObject(GraphConstants.GRAPH);
                    if (graphObject.has(AppConstants.UPLOAD_ENTITY_ID_LIST)) {
                        JSONObject entityListObject = graphObject.getJSONObject(AppConstants.UPLOAD_ENTITY_ID_LIST);
                        //JSONObject entityListObject = payloadJson.getJSONObject("UploadedFeatures");
                        editResponseJson = new JSONObject();
                        editResponseJson.put(AppConstants.UPLOAD_ENTITY_ID_LIST, entityListObject);
                        editResponseJson.put(AppConstants.STATUS, AppConstants.SUCCESS);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Map<String, HashMap<Object, String>> editedLayerNameIdsMap = EditMetaDataTable.getEditedId(context);
                Map<String, HashMap<Object, String>> editedLayerLabelIdsMap = new HashMap<>();
                for (String layerName : editedLayerNameIdsMap.keySet()) {
                    try {
                        JSONObject cmEntityResult = cmGraph.getVertex("name", layerName);
                        if (cmEntityResult.has("status") && cmEntityResult.getString("status").equalsIgnoreCase("success")) {
                            CMEntity cmEntity = (CMEntity) cmEntityResult.get("result");
                            editedLayerLabelIdsMap.put(cmEntity.getLabel(),editedLayerNameIdsMap.get(layerName));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                boolean isDeleted = EditMetaDataTable.deleteOperation(context, operationName);//success response - upload edits
                if (isDeleted) {
                    if (editResponseJson != null && editedLayerLabelIdsMap != null && !editedLayerLabelIdsMap.isEmpty()) {
                        editResponseJson.put("featureArray", editedLayerLabelIdsMap);
                    }
                    ReveloLogger.debug(className, "uploadToServerData", "All update feature entry delete from edit metadata table.");
                } else {
                    ReveloLogger.debug(className, "uploadToServerData", "failed to delete, update feature entry from edit metadata table.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            uploadToServerData(AppConstants.ADD, dataJson, cmGraph, progressDialog);

        }
        else if (operationName.equalsIgnoreCase(AppConstants.ADD)) {

            try {

                if (payloadJson.has(GraphConstants.GRAPH)) {
                    JSONObject graphObject = payloadJson.getJSONObject(GraphConstants.GRAPH);
                    if (graphObject.has(AppConstants.UPLOAD_ENTITY_ID_LIST)) {
                        JSONObject entityListObject = graphObject.getJSONObject(AppConstants.UPLOAD_ENTITY_ID_LIST);
                        //JSONObject entityListObject = payloadJson.getJSONObject("UploadedFeatures");

                        addResponseJson = new JSONObject();
                        addResponseJson.put(AppConstants.UPLOAD_ENTITY_ID_LIST, entityListObject);
                        addResponseJson.put(AppConstants.STATUS, AppConstants.SUCCESS);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Map<String, HashMap<Object, String>> addedLayerNameIdsMap = EditMetaDataTable.getAddedId(context);
                Map<String, HashMap<Object, String>> addedLayerLabelIdsMap = new HashMap<>();
                for (String layerName : addedLayerNameIdsMap.keySet()) {
                    try {
                        JSONObject cmEntityResult = cmGraph.getVertex("name", layerName);
                        if (cmEntityResult.has("status") && cmEntityResult.getString("status").equalsIgnoreCase("success")) {
                            CMEntity cmEntity = (CMEntity) cmEntityResult.get("result");
                            addedLayerLabelIdsMap.put(cmEntity.getLabel(),addedLayerNameIdsMap.get(layerName));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                boolean isDeleted = EditMetaDataTable.deleteOperation(context, operationName);//success response - upload adds
                if (isDeleted) {
                    if (addResponseJson != null && addedLayerLabelIdsMap != null && !addedLayerLabelIdsMap.isEmpty()) {
                        addResponseJson.put("featureArray", addedLayerLabelIdsMap);
                    }
                    ReveloLogger.debug(className, "uploadToServerData", "All added feature entry delete from edit metadata table.");
                } else {
                    ReveloLogger.debug(className, "uploadToServerData", "failed to delete, added feature entry from edit metadata table.");
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            uploadAttachment(cmGraph, /*dataGeoPackage, */progressDialog,isInstantUpload);//after upload add feature
        }

        GeoPackageManagerAgent.exportMetaDataGeopackage(context);  // export metadata here.
    }

    private void handleFailedResponse(String operationName, ProgressDialog progressDialog, VolleyError error) {
        ReveloLogger.debug(className, "uploadToServerData", "upload " + operationName + " operation failed..");
        ProgressUtility.dismissProgressDialog(progressDialog);//dismiss when failed to upload

        String errorMsg = "No information available";

        try {

            NetworkResponse networkResponse = error.networkResponse;

            if (networkResponse != null) {

                byte[] byteData = networkResponse.data;

                if (byteData != null) {

                    String errorMsgData = new String(byteData, StandardCharsets.UTF_8);
                    if (TextUtils.isEmpty(errorMsgData)) {
                        JSONObject errorObject = new JSONObject(errorMsgData);
                        if (errorObject.has("message")) {
                            errorMsg = errorObject.getString("message");
                        }
                    } else {
                        errorMsg = errorMsgData;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (errorMsg == null ||errorMsg.equalsIgnoreCase("No information available")||errorMsg.isEmpty()) {
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


        ReveloLogger.debug(className, "uploadToServerData", "Error: "+errorMsg);
        try {

            if (operationName.equalsIgnoreCase(AppConstants.DELETE)) {
                deleteResponseJson = new JSONObject();
                deleteResponseJson.put(AppConstants.STATUS, AppConstants.FAILURE);
                deleteResponseJson.put(AppConstants.FAILURE_MESSAGE, errorMsg);

            }
            else if (operationName.equalsIgnoreCase(AppConstants.EDIT)) {
                editResponseJson = new JSONObject();
                editResponseJson.put(AppConstants.STATUS, AppConstants.FAILURE);
                editResponseJson.put(AppConstants.FAILURE_MESSAGE, errorMsg);
            }
            else if (operationName.equalsIgnoreCase(AppConstants.ADD)) {
                addResponseJson = new JSONObject();
                addResponseJson.put(AppConstants.STATUS, AppConstants.FAILURE);
                addResponseJson.put(AppConstants.FAILURE_MESSAGE, errorMsg);
            }

            JSONObject featureUploadResponseJson = new JSONObject();

            try {
                featureUploadResponseJson.put(AppConstants.ADD, addResponseJson);
                featureUploadResponseJson.put(AppConstants.EDIT, editResponseJson);
                featureUploadResponseJson.put(AppConstants.DELETE, deleteResponseJson);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ReveloLogger.debug(className, "uploadToServerData", "uploading logs..");
            new UploadLogs(context, userName, accessToken, featureUploadResponseJson,
                    iUpload,requestCode,false).execute();

        } catch (Exception e) {
            ReveloLogger.error(className, "uploadToServerData", "exception while handling upload failure response - "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void uploadAttachment(CMGraph cmGraph, /*GeoPackage dataGeoPackage,*/ ProgressDialog progressDialog, boolean isInstantUpload) {
        ReveloLogger.debug(className, "uploadAttachment", "start process to upload attachments");
        ProgressUtility.dismissProgressDialog(progressDialog);//dismiss when exception occurred

        JSONObject featureUploadResponseJson = new JSONObject();

        try {
            ReveloLogger.debug(className, "uploadAttachment", "consolidated json created for adds, edits , deletes uploaded just now. This is needed to show upload report at the end.");
            featureUploadResponseJson.put(AppConstants.ADD, addResponseJson);
            featureUploadResponseJson.put(AppConstants.EDIT, editResponseJson);
            featureUploadResponseJson.put(AppConstants.DELETE, deleteResponseJson);

        } catch (Exception e) {
            ReveloLogger.debug(className, "uploadAttachment", "error creating consolidated json created for adds, edits , deletes uploaded just now. This is needed to show upload report at the end.. Exception: "+e.getMessage());
            e.printStackTrace();
        }

        new AttachmentUpload(context, accessToken, userName, surveyName, cmGraph, /*dataGeoPackage,*/ featureUploadResponseJson, iUpload,requestCode,isInstantUpload).execute();
    }
    private void uploadMetadata(CMGraph cmGraph, /*GeoPackage dataGeoPackage,*/ ProgressDialog progressDialog) {

        ProgressUtility.dismissProgressDialog(progressDialog);//dismiss when exception occurred

        JSONObject featureUploadResponseJson = new JSONObject();

        try {

            featureUploadResponseJson.put(AppConstants.ADD, addResponseJson);
            featureUploadResponseJson.put(AppConstants.EDIT, editResponseJson);
            featureUploadResponseJson.put(AppConstants.DELETE, deleteResponseJson);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //new MetadataUpload(context, accessToken, userName, surveyName, cmGraph, /*dataGeoPackage,*/ featureUploadResponseJson, iUpload,progressDialog).execute();
    }
}