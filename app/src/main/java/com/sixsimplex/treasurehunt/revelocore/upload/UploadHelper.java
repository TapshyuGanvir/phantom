package com.sixsimplex.treasurehunt.revelocore.upload;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.revelocore.data.FeatureTable;
import com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil.EditMetaDataTable;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.util.ProgressUtility;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadHelper extends AsyncTask<Void, Void, JSONObject> {

    private WeakReference<Context> contextWeakReference;
    private String surveyName;
    private ProgressDialog progressDialog;
    private IUploadHelper helper;
    //private Map<String, FeatureLayer> featureLayerMap;
   // private GeoPackage dataGeoPackage;
    private CMGraph cmGraph;
    private HashMap<String,List<UploadItemModel>> operationModelMap;

    UploadHelper(Context context, String surveyName, IUploadHelper iUploadHelper) {
        contextWeakReference = new WeakReference<>(context);
        this.surveyName = surveyName;
        helper = iUploadHelper;
        operationModelMap = new HashMap<>();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context context = contextWeakReference.get();
        progressDialog = ProgressUtility.showProgressDialog(context, "Preparing to upload data", "Please wait...");
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {

        Context context = contextWeakReference.get();

       // dataGeoPackage = GeoPackageManagerAgent.getDataGeoPackage(context);

        //CMGraph cmGraph = null;
        try{
            JSONObject graphResult = CMUtils.getCMGraph(contextWeakReference.get());
            if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                // if(entitiesResult.getOperationStatus().equalsIgnoreCase(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_SUCCESS)) {
                 cmGraph = (CMGraph) graphResult.get("result");
                JSONObject deleteFeatureJsonObject = createDeletedFeature(context, cmGraph);
                JSONObject editFeatureJsonObject = createEditFeatures(context, cmGraph/*, dataGeoPackage*/);
                JSONObject addFeatureJsonObject = createAddFeatures(context, cmGraph/*, dataGeoPackage*/);

                JSONObject payloadJsonObject = new JSONObject();

                try {

                    if (deleteFeatureJsonObject != null) {
                        payloadJsonObject.put(AppConstants.DELETE, deleteFeatureJsonObject);
                    }

                    if (editFeatureJsonObject != null) {
                        payloadJsonObject.put(AppConstants.EDIT, editFeatureJsonObject);
                    }

                    if (addFeatureJsonObject != null) {
                        payloadJsonObject.put(AppConstants.ADD, addFeatureJsonObject);
                    }
                    return payloadJsonObject;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else {

            }
            }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject createDeletedFeature(Context context, CMGraph cmGraph) {

        JSONObject payload = null;

        try {

            JSONObject layerObject = null;
            JSONObject idObject = null;

            Map<String, List<EditMetaDataTable.DeleteModel>> deletedIdMap = EditMetaDataTable.getDeletedId(context);

            if (deletedIdMap.size() > 0) {
                if(operationModelMap==null){
                    operationModelMap=new HashMap<>();
                }
                if (!operationModelMap.containsKey("delete")) {
                    operationModelMap.put("delete", new ArrayList<>());
                }

                layerObject = new JSONObject();
                idObject = new JSONObject();

                for (String layerName : deletedIdMap.keySet()) {

                    List<EditMetaDataTable.DeleteModel> deleteModelList = deletedIdMap.get(layerName);

                    if (deleteModelList != null && deleteModelList.size() > 0) {

                        JSONObject featureIdJsonObject = new JSONObject();
                        JSONArray featureIdArray = new JSONArray();

                        JSONObject cmEntityResult = cmGraph.getVertex("name",layerName);
                        if (cmEntityResult.has("status") && cmEntityResult.getString("status").equalsIgnoreCase("success")) {
                            CMEntity cmEntity = (CMEntity) cmEntityResult.get("result");
                            String label = layerName;
                            if (cmEntity != null) {
                                label = cmEntity.getLabel();
                            }
                            for (EditMetaDataTable.DeleteModel deleteModel : deleteModelList) {

                                Object w9Id = deleteModel.getW9Id();
                                String w9MetaData = deleteModel.getW9MetaData();

                                JSONObject w9MetaDataJsonObject = new JSONObject();
                                w9MetaDataJsonObject.put(AppConstants.W9_METADATA, w9MetaData);

                                featureIdJsonObject.put("" + w9Id, w9MetaDataJsonObject);
                                featureIdArray.put(w9Id);

                              /*  UploadItemModel uploadItemModel= new UploadItemModel(cmEntity.getName(),cmEntity.getLabel(),
                                        "delete","unknown",w9Id);*/

                            }

                            layerObject.put(layerName, featureIdJsonObject);
                            idObject.put(label, featureIdArray);
                        }else {

                        }
                    }
                }
            }

            if (layerObject != null) {
                payload = new JSONObject();
                payload.put(AppConstants.UPLOAD_FEATURE_ENTITY_LIST, layerObject);
                payload.put(AppConstants.UPLOAD_ENTITY_ID_LIST, idObject);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return payload;
    }

    private JSONObject createEditFeatures(Context context,CMGraph cmGraph/*, GeoPackage dataGeoPackage*/) {

        JSONObject payload = null;
        try {
            Map<String, HashMap<Object,String>> editedIdMap = EditMetaDataTable.getEditedId(context);

            payload = createFeatureJson(surveyName, editedIdMap, cmGraph,AppConstants.EDIT);//create edit json

        } catch (Exception e) {
            e.printStackTrace();
        }

        return payload;
    }

    private JSONObject createAddFeatures(Context context, CMGraph cmGraph/*, GeoPackage dataGeoPackage*/) {

        JSONObject payload = null;

        try {

            Map<String, HashMap<Object,String>> addedIdMap = EditMetaDataTable.getAddedId(context);

            payload = createFeatureJson(surveyName, addedIdMap, cmGraph,AppConstants.ADD);//create add json

        } catch (Exception e) {
            e.printStackTrace();
        }
        return payload;
    }

    private JSONObject createFeatureJson(String surveyName, Map<String, HashMap<Object,String>> idMap, CMGraph cmGraph,
                                         String operationName) {

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
                            HashMap<Object,String> idList = idMap.get(layerName);

                            if (idList != null && idList.size() > 0) {

                                JSONArray featureIdJsonArray = new JSONArray();//needed to uplod data
                                JSONArray featureIdLableJsonArray = new JSONArray();//needed to show results

                                for (Object id : idList.keySet()) {
                                    try {
                                        JSONObject jsonObject = featureTable.createUploadJsonObject(id, surveyName, contextWeakReference.get(),operationName);
                                        featureJsonObject.put(surveyName + "_" + id, jsonObject);
                                        featureIdJsonArray.put(id);

                                        JSONObject featureJson = new JSONObject();
                                        featureJson.put("w9id",id);
                                        featureJson.put("featureLabel",idList.get(id));
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

    @Override
    protected void onPostExecute(JSONObject dataJson) {
        super.onPostExecute(dataJson);

        if(dataJson!=null && cmGraph!=null) {
            helper.onPayLoad(dataJson,cmGraph ,  progressDialog);
        }else {
            String errorMessage = "";
            if(dataJson==null) {
                errorMessage = "Could not read data for upload from device.";
            }
            if(cmGraph==null) {
                errorMessage += " Could not build entity graph.";
            }
            if(errorMessage.isEmpty()){
                errorMessage = "Something went wrong";
            }
            helper.onError(errorMessage,progressDialog);
            ReveloLogger.error("UploadHelper","postExecute","No data found for uploading -  could not create payload json");
        }
    }
}