package com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil;

import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageRWAgent;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class EditMetaDataTable {

    private static final String LAYER_NAME = "layername";
    private static final String FEATURE_LABEL = "featurelabel";
    private static final String ADD_COL = "addcol";
    private static final String EDIT_COL = "editcol";
    private static final String DELETE_COL = "deletecol";
    private static final String W9_META_DATA_COL = "w9metadata";
    private static final String className = "EditMetaDataTable";
    private static final String EDIT_META_TABLE_NAME = "editmetadata";


    public static boolean insertAddRecord(String featureName, String featureLabel, Context context, String layerName) {
        try {
            boolean isPresentInAdds = presentInAdd(layerName, featureName, context); //check in adds from edit operation
            if (isPresentInAdds) {
                return true;
            } else {
                return insertRecord(featureName, ADD_COL, null, context, layerName, featureLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean presentInAdd(String layerName, String id, Context context) {
        return isRecordExists(id, ADD_COL, context, layerName);
    }

    private static boolean insertRecord(String featureName, String columnName,
                                        String w9Metadata, Context context,
                                        String layerName, String featureLabel) {
        try {

            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();

            JSONObject attributeObjLayerName = new JSONObject();
            attributeObjLayerName.put("name", LAYER_NAME);
            attributeObjLayerName.put("value", layerName);
            attributesJArray.put(attributeObjLayerName);

            JSONObject attributeObjColumnFeature = new JSONObject();
            attributeObjColumnFeature.put("name", columnName);
            attributeObjColumnFeature.put("value", featureName);
            attributesJArray.put(attributeObjColumnFeature);

            JSONObject featureLabelJObj = new JSONObject();
            featureLabelJObj.put("name", FEATURE_LABEL);
            featureLabelJObj.put("value", featureLabel);
            attributesJArray.put(featureLabelJObj);

            if (w9Metadata != null) {
                JSONObject attributeObjw9Metadata = new JSONObject();
                attributeObjw9Metadata.put("name", W9_META_DATA_COL);
                attributeObjw9Metadata.put("value", w9Metadata);
                attributesJArray.put(attributeObjw9Metadata);
            }

            dataObject.put("attributes", attributesJArray);
            dataJsonArray.put(dataObject);


            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo(layerName);
            JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context),
                    datasetInfo, dataJsonArray);

            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");


        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean isRecordExists(String featureName, String columnName, Context context, String layerName) {
        try {
            JSONArray whereClauseArray = new JSONArray();

            if (featureName != null) {
            JSONObject conditionJobjW9id = new JSONObject();
            conditionJobjW9id.put("conditionType", "attribute");
            conditionJobjW9id.put("columnName", columnName);
            conditionJobjW9id.put("valueDataType", "string");
            conditionJobjW9id.put("value", featureName);
            conditionJobjW9id.put("operator", "=");
            whereClauseArray.put(conditionJobjW9id);
            }

            if (layerName != null) {
            JSONObject conditionJobjLayerName = new JSONObject();
            conditionJobjLayerName.put("conditionType", "attribute");
            conditionJobjLayerName.put("columnName", LAYER_NAME);
            conditionJobjLayerName.put("valueDataType", "string");
            conditionJobjLayerName.put("value", layerName);
            conditionJobjLayerName.put("operator", "=");
            whereClauseArray.put(conditionJobjLayerName);
            }

            if (whereClauseArray.length() > 0) {
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo(layerName);
                JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, null, whereClauseArray, "AND", true, - 1, false, false);

                if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                    if (respJObj.has("features")) {
                        JSONObject responseFeatures = respJObj.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            if (featuresJArray.length() > 0) {
                                return true;
                            }
                        }
                    }
                }
                else {
                    return false;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static JSONObject getEditMetadataDatasetInfo(String entityName) {

        JSONObject datasetInfo = new JSONObject();
        try{
            ReveloLogger.info(className, "getEditMetadataDatasetInfo", "getting getEditMetadataDatasetInfo for " + entityName);
            datasetInfo.put("datasetName", EDIT_META_TABLE_NAME);
            datasetInfo.put("datasetType", "table");
            datasetInfo.put("geometryType", "");

            datasetInfo.put("idPropertyName", LAYER_NAME);
            datasetInfo.put("w9IdPropertyName", LAYER_NAME);
        } catch (JSONException e) {
            ReveloLogger.error(className, "getDatasetInfo", "error initializing getEditMetadataDatasetInfo json for " + entityName + ". Exception - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return datasetInfo;
    }

    private static boolean deleteRecord(String featureName, String columnName, Context context, String layerName) {
        try {
            JSONArray whereClauseArray = new JSONArray();
            JSONObject conditionJobjW9id = new JSONObject();
            conditionJobjW9id.put("conditionType", "attribute");
            conditionJobjW9id.put("columnName", columnName);
            conditionJobjW9id.put("valueDataType", "string");
            conditionJobjW9id.put("value", featureName);
            conditionJobjW9id.put("operator", "=");
            whereClauseArray.put(conditionJobjW9id);

            JSONObject conditionJobjLayerName = new JSONObject();
            conditionJobjLayerName.put("conditionType", "attribute");
            conditionJobjLayerName.put("columnName", LAYER_NAME);
            conditionJobjLayerName.put("valueDataType", "string");
            conditionJobjLayerName.put("value", layerName);
            conditionJobjLayerName.put("operator", "=");
            whereClauseArray.put(conditionJobjLayerName);



            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo(layerName);
            JSONObject respJObj = gpkgRWAgent.deleteDatasetContent(DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, whereClauseArray, "AND", context);
            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");


        } catch (Exception e) {
            e.printStackTrace();
        }
                return false;
            }

    public static boolean insertEditEntry(String layerName, String id, String featureLabel, Context context) {

        boolean isInsert=false;
        try {


            boolean isPresentInAdds = presentInAdd(layerName, id, context); //check in adds from edit operation

            if (isPresentInAdds) {
                isInsert = true; //already in adds
            }
            else {

                boolean isPresentInEdits = presentInEdit(layerName, id, context);//check in edit

                if (isPresentInEdits) {
                    isInsert = true;//already in edits
                }
                else {

                    try {
                        isInsert = insertRecord(id, EDIT_COL, null, context, layerName, featureLabel);
        }catch (Exception e){
                        isInsert = false;
            e.printStackTrace();
        }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isInsert = false;
        }
        return isInsert;
    }


    public static boolean insertDeleteEntry(String layerName, String id, String featureLabel, String w9MetaData, Context context) {

        boolean isDelete = false;

        try {

            boolean isPresentInAdds = presentInAdd(layerName, id, context); //check in adds from edit operation


            if (isPresentInAdds) { //already in adds
                isDelete = deleteRecord(id, ADD_COL, context, layerName);
            }
            else {

                boolean isPresentInEdits = presentInEdit(layerName, id, context);//check in edit

                if (isPresentInEdits) {
                    isDelete = deleteRecord(id, EDIT_COL, context, layerName);
                }

        try {
                    isDelete = insertRecord(id, DELETE_COL, w9MetaData, context, layerName, featureLabel);
                } catch (Exception e) {
                    isDelete = false;
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isDelete = false;
        }
        return isDelete;
            }

    private static boolean presentInEdit(String layerName, String id, Context context) {
        return isRecordExists(id, EDIT_COL, context, layerName);
            }




    public static boolean isDataForUpload(String entityName, Context context) {

        int count = 0;
        try {
                GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
            count = gpkgRWAgent.getDatasetItemCount(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), getEditMetadataDatasetInfo(entityName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count > 0;
    }


    public static Map<String, HashMap<Object, String>> getAddedId(Context context) {

        Map<String, HashMap<Object, String>> featureIdMap = new LinkedHashMap<>();
        try {
            featureIdMap = getRecord(null, ADD_COL, context, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureIdMap;
    }

    public static Map<String, HashMap<Object, String>> getAddedId(Context context,String entityName) {

        Map<String, HashMap<Object, String>> featureIdMap = new LinkedHashMap<>();
        try {

            if(entityName!=null && !entityName.isEmpty()){
                featureIdMap = getRecord(null, ADD_COL, context, entityName);
            }else {
                featureIdMap = getRecord(null, ADD_COL, context, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureIdMap;
    }

    private static Map<String, HashMap<Object, String>> getRecord(String featureName, String columnName, Context context, String layerName) {

        Map<String, HashMap<Object, String>> featureIdMap = new LinkedHashMap<>();
        try {

            JSONArray whereClauseArray = new JSONArray();

            if (featureName != null) {
                JSONObject conditionJobjW9id = new JSONObject();
                conditionJobjW9id.put("conditionType", "attribute");
                conditionJobjW9id.put("columnName", columnName);
                conditionJobjW9id.put("valueDataType", "string");
                conditionJobjW9id.put("value", featureName);
                conditionJobjW9id.put("operator", "=");
                whereClauseArray.put(conditionJobjW9id);
            } else {
                JSONObject conditionJobjW9id = new JSONObject();
                conditionJobjW9id.put("conditionType", "attribute");
                conditionJobjW9id.put("columnName", columnName);
                conditionJobjW9id.put("valueDataType", "int");
                conditionJobjW9id.put("value", "\"\"");
                conditionJobjW9id.put("operator", "!=");
                whereClauseArray.put(conditionJobjW9id);

                JSONObject conditionJobjNotNull = new JSONObject();
                conditionJobjNotNull.put("conditionType", "attribute");
                conditionJobjNotNull.put("columnName", columnName);
                conditionJobjNotNull.put("valueDataType", "int");
                conditionJobjNotNull.put("value", "NULL");
                conditionJobjNotNull.put("operator", "IS NOT");
                whereClauseArray.put(conditionJobjNotNull);
            }

            if (layerName != null) {
                JSONObject conditionJobjLayerName = new JSONObject();
                conditionJobjLayerName.put("conditionType", "attribute");
                conditionJobjLayerName.put("columnName", LAYER_NAME);
                conditionJobjLayerName.put("valueDataType", "string");
                conditionJobjLayerName.put("value", layerName);
                conditionJobjLayerName.put("operator", "=");
                whereClauseArray.put(conditionJobjLayerName);
            }

            List<String> requiredColumnsList = new ArrayList<>();
            requiredColumnsList.add(FEATURE_LABEL);
            requiredColumnsList.add(LAYER_NAME);
            requiredColumnsList.add(columnName);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo(layerName);
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context),
                    datasetInfo, requiredColumnsList, whereClauseArray, "AND", true, -1, false, false);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        if (featuresJArray.length() > 0) {

                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject recordJson = featuresJArray.getJSONObject(i);
                                JSONObject propertiesJobjReceived = recordJson.getJSONObject("properties");
                                String layerNameStr = null;
                                String w9id = null;
                                String featureLabel = null;
                                if (propertiesJobjReceived.has(FEATURE_LABEL)) {
                                    featureLabel = propertiesJobjReceived.getString(FEATURE_LABEL);
                                }
                                if (propertiesJobjReceived.has(LAYER_NAME)) {
                                    layerNameStr = propertiesJobjReceived.getString(LAYER_NAME);
                                }
                                if (propertiesJobjReceived.has(columnName)) {
                                    w9id = propertiesJobjReceived.getString(columnName);
                                }
                                if (layerNameStr != null && w9id != null) {
                                    if (featureIdMap.containsKey(layerNameStr)) {
                                        HashMap<Object, String> idList = featureIdMap.get(layerNameStr);
                                        if (idList == null) {
                                            idList = new HashMap<>();
                                        }
                                        if (!idList.containsKey(w9id)) {
                                            if (featureLabel == null || featureLabel.isEmpty()) {
                                                featureLabel = w9id;
                                            }
                                            idList.put(w9id, featureLabel);
                                        }

                                    } else {
                                        HashMap<Object, String> idList = new HashMap<>();
                                        if (featureLabel == null || featureLabel.isEmpty()) {
                                            featureLabel = w9id;
                                        }
                                        idList.put(w9id, featureLabel);
                                        featureIdMap.put(layerNameStr, idList);
                                    }
                                }
                            }

                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureIdMap;
    }

    public static Map<String, HashMap<Object, String>> getEditedId(Context context) {

        Map<String, HashMap<Object, String>> featureIdMap = new LinkedHashMap<>();

        try {
            featureIdMap = getRecord(null, EDIT_COL, context, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureIdMap;
    }

    public static Map<String, HashMap<Object, String>> getEditedId(Context context,String entityName) {

        Map<String, HashMap<Object, String>> featureIdMap = new LinkedHashMap<>();

        try {
            if(entityName!=null && !entityName.isEmpty()){
                featureIdMap = getRecord(null, EDIT_COL, context, entityName);
            }else {
                featureIdMap = getRecord(null, EDIT_COL, context, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureIdMap;
    }

    public static Map<String, List<DeleteModel>> getDeletedId(Context context) {

        Map<String, List<DeleteModel>> featureIdMap = new LinkedHashMap<>();


        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();


            JSONObject conditionJobjW9id = new JSONObject();
            conditionJobjW9id.put("columnType", "int");
            conditionJobjW9id.put("value", "\"\"");
            conditionJobjW9id.put("operator", "!=");
            conditionMap.put(DELETE_COL, conditionJobjW9id);

            JSONObject conditionJobjNotNull = new JSONObject();
            conditionJobjNotNull.put("columnType", "int");
            conditionJobjNotNull.put("value", "NULL");
            conditionJobjNotNull.put("operator", "IS NOT");
            conditionMap.put(DELETE_COL, conditionJobjNotNull);


            List<String> requiredColumnsList = new ArrayList<>();
            requiredColumnsList.add(FEATURE_LABEL);
            requiredColumnsList.add(LAYER_NAME);
            requiredColumnsList.add(DELETE_COL);
            requiredColumnsList.add(W9_META_DATA_COL);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo("");
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context),
                    datasetInfo, requiredColumnsList, conditionMap, "AND", true, -1, false, false);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        if (featuresJArray.length() > 0) {

                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject recordJson = featuresJArray.getJSONObject(i);
                                JSONObject propertiesJobjReceived = recordJson.getJSONObject("properties");
                                String layerNameStr = null;
                                String w9id = null;
                                String featureLabel = null;
                                String w9Metadata = null;
                                if (propertiesJobjReceived.has(LAYER_NAME)) {
                                    layerNameStr = propertiesJobjReceived.getString(LAYER_NAME);
                                }
                                if (propertiesJobjReceived.has(DELETE_COL)) {
                                    w9id = propertiesJobjReceived.getString(DELETE_COL);
                                }
                                if (propertiesJobjReceived.has(W9_META_DATA_COL)) {
                                    w9Metadata = propertiesJobjReceived.getString(W9_META_DATA_COL);
                                }
                                if (propertiesJobjReceived.has(FEATURE_LABEL)) {
                                    featureLabel = propertiesJobjReceived.getString(FEATURE_LABEL);
                                }

                                if (layerNameStr != null && w9id != null && w9Metadata != null) {

                                    if (featureIdMap.containsKey(layerNameStr)) {
                                        List<DeleteModel> idList = featureIdMap.get(layerNameStr);
                                        if (idList == null) {
                                            idList = new ArrayList<>();
                                        }
                                        if (featureLabel == null || featureLabel.isEmpty()) {
                                            featureLabel = w9id;
                                        }
                                        DeleteModel deleteModel = new DeleteModel(w9id, w9Metadata, featureLabel);
                                        if (!idList.contains(deleteModel)) {
                                            idList.add(deleteModel);
                                        }
                                    } else {
                                        List<DeleteModel> idList = new ArrayList<>();
                                        if (featureLabel == null || featureLabel.isEmpty()) {
                                            featureLabel = w9id;
                                        }
                                        DeleteModel deleteModel = new DeleteModel(w9id, w9Metadata, featureLabel);
                                        idList.add(deleteModel);
                                        featureIdMap.put(layerNameStr, idList);
                                    }
                                }
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return featureIdMap;
    }

    public static boolean deleteOperation(Context context, String operationName) {

        boolean isDeleted = false;
        try {
            JSONArray whereClauseArray = new JSONArray();
            JSONObject conditionJobjNotNull = new JSONObject();
            conditionJobjNotNull.put("conditionType", "attribute");
            if (operationName.equalsIgnoreCase(AppConstants.ADD)) {
                conditionJobjNotNull.put("columnName", ADD_COL);
            } else if (operationName.equalsIgnoreCase(AppConstants.EDIT)) {
                conditionJobjNotNull.put("columnName", EDIT_COL);
            } else if (operationName.equalsIgnoreCase(AppConstants.DELETE)) {
                conditionJobjNotNull.put("columnName", DELETE_COL);
            }
            conditionJobjNotNull.put("valueDataType", "int");
            conditionJobjNotNull.put("value", "NULL");
            conditionJobjNotNull.put("operator", "IS NOT");
            whereClauseArray.put(conditionJobjNotNull);


            JSONObject conditionJobjNotEmpty = new JSONObject();
            conditionJobjNotEmpty.put("conditionType", "attribute");
            if (operationName.equalsIgnoreCase(AppConstants.ADD)) {
                conditionJobjNotEmpty.put("columnName", ADD_COL);
            } else if (operationName.equalsIgnoreCase(AppConstants.EDIT)) {
                conditionJobjNotEmpty.put("columnName", EDIT_COL);
            } else if (operationName.equalsIgnoreCase(AppConstants.DELETE)) {
                conditionJobjNotEmpty.put("columnName", DELETE_COL);
            }
            conditionJobjNotEmpty.put("valueDataType", "int");
            conditionJobjNotEmpty.put("value", "\"\"");
            conditionJobjNotEmpty.put("operator", "IS NOT");
            whereClauseArray.put(conditionJobjNotNull);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getEditMetadataDatasetInfo("");
            JSONObject respJObj = gpkgRWAgent.deleteDatasetContent(DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, whereClauseArray, "AND", context);
            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static class DeleteModel {

        private final String w9MetaData;
        private final Object w9Id;
        private final String featureLabel;

        DeleteModel(Object id, String w9MetaData, String featureLabel) {
            this.w9Id = id;
            this.w9MetaData = w9MetaData;
            this.featureLabel = featureLabel;
        }

        public String getW9MetaData() {
            return w9MetaData;
        }

        public Object getW9Id() {
            return w9Id;
        }

        public String getFeatureLabel() {
            return featureLabel;
        }
    }

}