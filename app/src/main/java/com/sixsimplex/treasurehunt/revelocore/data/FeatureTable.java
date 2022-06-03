package com.sixsimplex.treasurehunt.revelocore.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel.Interaction;
import com.sixsimplex.treasurehunt.revelocore.editing.model.Attachment;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageRWAgent;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageUtils;
import com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil.EditMetaDataTable;
import com.sixsimplex.treasurehunt.revelocore.geopackage.utils.AttachmentConstant;
import com.sixsimplex.treasurehunt.revelocore.geopackage.utils.MetadataTableConstant;
import com.sixsimplex.treasurehunt.revelocore.layer.Attribute;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;
import com.sixsimplex.treasurehunt.revelocore.layer.GeometryEngine;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.DatePickerMethods;
import com.sixsimplex.treasurehunt.revelocore.util.ToastUtility;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jsqlite.Callback;
import jsqlite.Constants;
import jsqlite.Database;
import jsqlite.Stmt;

public class FeatureTable {

    private final FeatureLayer featureLayer;
    private final String className = "FeatureTable";

    //--------COMMON METHODS-------------------------------------------------------------------------------------------------------------------------------------------------
    public FeatureTable(FeatureLayer featureLayer) {
        this.featureLayer = featureLayer;
    }

    public int getFeatureCount(Context context, boolean addShadowTableCount, boolean returnShadowTableOnly) {
        int count = 0;
        try {
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);


            if (returnShadowTableOnly) {
                count += gpkgRWAgent.getDatasetItemCount(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(true));
                return count;
            }

            count = gpkgRWAgent.getDatasetItemCount(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false));
            if (addShadowTableCount && featureLayer.isHasShadowTable()) {
                count += gpkgRWAgent.getDatasetItemCount(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(true));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    private JSONObject getDatasetInfo(boolean getDatasetInfoForShadowTable) {

        JSONObject datasetInfo = new JSONObject();
        try {
            if (getDatasetInfoForShadowTable) {
                ReveloLogger.info(className, "getDatasetInfo", "getting dataset info for " + featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + "_shadow");
                datasetInfo.put("datasetName", featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + "_shadow");
                datasetInfo.put("datasetType", "table");
                datasetInfo.put("geometryType", "");
            }
            else {
                ReveloLogger.info(className, "getDatasetInfo", "getting dataset info for " + featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName());
                datasetInfo.put("datasetName", featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName());
                datasetInfo.put("datasetType", featureLayer.getType());
                datasetInfo.put("geometryType", featureLayer.getGeometryType());
            }
            //datasetInfo.put("datasetType", featureLayer.getType());
            //datasetInfo.put("geometryType", featureLayer.getGeometryType());
            datasetInfo.put("idPropertyName", featureLayer.getW9IdProperty());
            datasetInfo.put("w9IdPropertyName", featureLayer.getW9IdProperty());
        } catch (JSONException e) {
            ReveloLogger.error(className, "getDatasetInfo", "error initializing getDatasetInfo json for dataset: " + featureLayer.getName() + ". Exception - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return datasetInfo;
    }

    public int getFeatureCount(Context context, boolean addShadowTableCount, boolean returnShadowTableOnly, JSONObject whereClauseJson) {
        int count = 0;
        try {
            JSONArray contionArray = new JSONArray();
            String andOR = "OR";

            if (whereClauseJson != null) {
                contionArray = whereClauseJson.getJSONArray("clauses");
                andOR = whereClauseJson.getString("andOR");
            }
            else {
                contionArray = null;
                andOR = "";
            }

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);


            if (returnShadowTableOnly) {
                count += gpkgRWAgent.getDatasetItemCount(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(true), contionArray, andOR);
                return count;
            }

            count = gpkgRWAgent.getDatasetItemCount(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), contionArray, andOR);
            if (addShadowTableCount && featureLayer.isHasShadowTable()) {
                count += gpkgRWAgent.getDatasetItemCount(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(true), contionArray, andOR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    //traversal special
    public int getFeatureCountNEW(Context context, boolean countFromMainTable, boolean countFromShadowTable, JSONArray ORClausesArray, JSONArray ANDClausesArray, String ANDorOR) {
        int count = 0;
        try {

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);


            if (countFromMainTable) {
                count += gpkgRWAgent.getDatasetItemCountNEW(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(true), ORClausesArray, ANDClausesArray, ANDorOR);
            }

            if (countFromShadowTable && featureLayer.isHasShadowTable()) {
                count += gpkgRWAgent.getDatasetItemCountNEW(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), ORClausesArray, ANDClausesArray, ANDorOR);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public boolean doesFeatureExists(String columnName, Object columnValue, Context context, boolean checkInMainTable, boolean checkInShadowTable) {
        try {

            Feature feature = getFeature(columnName, columnValue, context, checkInMainTable, checkInShadowTable);
            return feature != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //--------GET FEATURES - SINGLE-------------------------------------------------------------------------------------------------------------------------------------------------
    public Feature getFeature(String columnName, Object columnValue, Context context, boolean searchInMainTable, boolean searchInShadowTable) {

        Feature feature = null;
        boolean queryGeometry = false;
        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();
            JSONObject conditionObj = new JSONObject();
            conditionObj.put("value", columnValue);
            conditionObj.put("operator", "=");
            conditionObj.put("columnType", featureLayer.getPropertiesHashMap().get(columnName).getType());
            conditionMap.put(columnName, conditionObj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);

            if (searchInMainTable) {
                if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                    queryGeometry = true;
                }
                JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), null, conditionMap, "", true, 1, queryGeometry);

                if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                    if (respJObj.has("features")) {
                        JSONObject responseFeatures = respJObj.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject featureJObj = featuresJArray.getJSONObject(i);
                                try {
                                    feature = constructFeatureFromJson(featureJObj);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
            }
            else {
                feature = null;
            }

            if (searchInShadowTable && feature == null) {
                queryGeometry = false;
                JSONObject respJObjShadow = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(true), null, conditionMap, "", true, 1, queryGeometry);

                if (respJObjShadow.has("status") && respJObjShadow.getString("status").equalsIgnoreCase("success")) {

                    if (respJObjShadow.has("features")) {
                        JSONObject responseFeatures = respJObjShadow.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject featureJObj = featuresJArray.getJSONObject(i);
                                feature = constructFeatureFromJson(featureJObj);
                            }
                        }
                    }

                }
            }
            else {
                ReveloLogger.error(className, "getFeature", "feature construction failed, reason - null value received.");
            }
        } catch (java.lang.Exception e) {
            ReveloLogger.error(className, "getFeature", "feature construction failed, reason -Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return feature;
    }

    private Feature constructFeatureFromJson(JSONObject featureJObj) {
        Feature feature = null;
        try {
            JSONObject propertiesJobjReceived = featureJObj.getJSONObject("properties");
            HashMap<String, Object> featurePropHashMap = new HashMap<>();
            Object w9Id = null;
            String label = "";
            for (Attribute attribute : featureLayer.getProperties()) {
                String attributeName = attribute.getName();
                String attributeType = attribute.getType();
                if (attributeName.equalsIgnoreCase(featureLayer.getW9IdProperty())) {
                    w9Id = propertiesJobjReceived.get(attributeName);
                }
                if (propertiesJobjReceived.has(featureLayer.getLabelPropertyName())) {
                    Object labelObj = propertiesJobjReceived.get(featureLayer.getLabelPropertyName());
                    if (labelObj != null && ! String.valueOf(labelObj).equalsIgnoreCase("null") && ! String.valueOf(labelObj).isEmpty() && ! String.valueOf(labelObj).equalsIgnoreCase("na")) {
                        label = String.valueOf(labelObj);
                    }
                    else {
                        label = "";
                    }
                }
                if (label.isEmpty() && w9Id != null) {
                    label = featureLayer.getLabel() + " - " + w9Id;
                }


                if (attributeType.equalsIgnoreCase("text") || attributeType.equalsIgnoreCase("string") || attributeType.equalsIgnoreCase("timestamp") || attributeType.equalsIgnoreCase("date")) {
                    if (propertiesJobjReceived.has(attributeName)) {
                        String value = propertiesJobjReceived.getString(attributeName);
                        featurePropHashMap.put(attributeName, value);
                    }
                    else {
                        featurePropHashMap.put(attributeName, attribute.getDefaultValue());
                    }
                }
                else if (attributeType.equalsIgnoreCase("int") || attributeType.equalsIgnoreCase("integer")) {
                    if (propertiesJobjReceived.has(attributeName)) {
                        int value = propertiesJobjReceived.getInt(attributeName);
                        featurePropHashMap.put(attributeName, value);
                    }
                    else {
                        featurePropHashMap.put(attributeName, attribute.getDefaultValue());
                    }
                }
                else if (attributeType.equalsIgnoreCase("double") || attributeType.equalsIgnoreCase("float")) {

                    if (propertiesJobjReceived.has(attributeName)) {
                        double value = propertiesJobjReceived.getDouble(attributeName);
                        featurePropHashMap.put(attributeName, value);
                    }
                    else {
                        featurePropHashMap.put(attributeName, attribute.getDefaultValue());
                    }
                }
                else if (attributeType.equalsIgnoreCase("boolean")) {
                    if (propertiesJobjReceived.has(attributeName)) {
                        boolean value = false;
                        Object valueObj = propertiesJobjReceived.get(attributeName);
                        if (valueObj instanceof Integer) {
                            int valueInt = (Integer) valueObj;
                            value = valueInt != 0;
                        }
                        else if (valueObj instanceof String) {
                            String valueStr = (String) valueObj;
                            value = ! valueStr.equalsIgnoreCase("false");
                        }
                        else if (valueObj instanceof Boolean) {
                            value = propertiesJobjReceived.getBoolean(attributeName);
                        }

                        featurePropHashMap.put(attributeName, value);
                    }
                    else {

                        boolean value = false;
                        Object valueObj = attribute.getDefaultValue();
                        if (valueObj instanceof Integer) {
                            int valueInt = (Integer) valueObj;
                            value = valueInt != 0;
                        }
                        else if (valueObj instanceof String) {
                            String valueStr = (String) valueObj;
                            value = ! valueStr.equalsIgnoreCase("false");
                        }
                        else if (valueObj instanceof Boolean) {
                            value = (boolean) attribute.getDefaultValue();
                        }
                        featurePropHashMap.put(attributeName, value);
                    }
                }
            }

            if (! featurePropHashMap.containsKey(AppConstants.W9_METADATA)) {
                if (propertiesJobjReceived.has(AppConstants.W9_METADATA)) {
                    String value = propertiesJobjReceived.getString(AppConstants.W9_METADATA);
                    featurePropHashMap.put(AppConstants.W9_METADATA, value);
                }
                else {
                    featurePropHashMap.put(AppConstants.W9_METADATA, getw9MetadataJsonStructure());
                }
            }

            feature = new Feature();//maintain this sequence, we need entity,attr and expression to set feature label
            feature.setFeatureId(w9Id);
            feature.setAttributes(featurePropHashMap);
            feature.setEntityName(featureLayer.getName());
            feature.setFeatureLabelExpression(featureLayer.getLabelPropertyName());
            feature.setFeatureLabel("");//"" or null sets lable acc to expression or feature id

            if (featureJObj.has("geometry")) {
                feature.setGeoJsonGeometry(featureJObj.getJSONObject("geometry"));
                feature.setGeometryType(GeoJsonUtils.getGeometryType(featureJObj.getJSONObject("geometry")));
            }
            else {
                feature.setGeoJsonGeometry(null);
                feature.setGeometryType("");
            }

        } catch (java.lang.Exception e) {
            e.printStackTrace();
            feature = null;
            ReveloLogger.error(className, "constructing gdbfeature from json", "feature construction failed, reason: " + e.getMessage());
        }
        if (feature == null) {
            ReveloLogger.error(className, "constructing gdbfeature from json", "feature construction failed");
        }
        return feature;
    }

    public JSONObject getw9MetadataJsonStructure() {
        JSONObject metadataJSON = new JSONObject();
        try {
            metadataJSON.put(AppConstants.W9_LATITUDE, 0.0);
            metadataJSON.put(AppConstants.W9_LONGITUDE, 0.0);
            metadataJSON.put(AppConstants.W9_ACCURACY, 0.0);


            String date = DatePickerMethods.getCurrentDateString_metadata();
            metadataJSON.put(AppConstants.W9_UPDATE_DATE, date);
            metadataJSON.put(AppConstants.W9_UPDATE_BY, UserInfoPreferenceUtility.getUserName());
//            metadataJSON.put(AppConstants.W9_ATTACHMENTS_INFO, new JSONArray());

            return metadataJSON;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metadataJSON;
    }

    public HashMap<String, Integer> getCategoryPropertyCountMap(Context context, String categoryPropertyName) {
        HashMap<String, Integer> categoryPropertyCountMap = new HashMap<>();
        try {
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), null, null, "", true, categoryPropertyName, null, true, 1, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return categoryPropertyCountMap;
    }

    public Feature getLastFeature(Context context) {
        Feature feature = null;
        try {
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            boolean queryGeometry = false;
            if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = true;
            }
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), null, null, "", true, null, "fid DESC", true, 1, queryGeometry);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            feature = constructFeatureFromJson(featureJObj);
                        }
                    }
                }

            }
        } catch (java.lang.Exception e) {
            ReveloLogger.error(className, "getFeature", "feature construction failed, reason -Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return feature;
    }

    //----GET FEATURES - MULTIPLE---------------------------------------------------------------------------------------------------------------------------------------------
    public List<Feature> getGeometryAndIdFeatureList(String w9IdPropertyName, Context context) {

        List<Feature> featureList = new ArrayList<>();

        try {
            List<String> requiredColumnsList = new ArrayList<>();
            requiredColumnsList.add(w9IdPropertyName);
            requiredColumnsList.add(featureLayer.getLabelPropertyName());
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);

            boolean queryGeometry = false;
            if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = true;
            }
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), requiredColumnsList, null, "", true, - 1, queryGeometry);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = new Feature();
                            try {
                                JSONObject propertiesJobjReceived = featureJObj.getJSONObject("properties");
                                HashMap<String, Object> featurePropHashMap = new HashMap<>();
                                Object w9Id = null;
                                String label = "";

                                if (propertiesJobjReceived.has(w9IdPropertyName)) {
                                    w9Id = propertiesJobjReceived.get(w9IdPropertyName);
                                    featurePropHashMap.put(w9IdPropertyName, w9Id);
                                }
                                if (propertiesJobjReceived.has(featureLayer.getLabelPropertyName())) {
                                    Object labelObj = propertiesJobjReceived.get(featureLayer.getLabelPropertyName());
                                    if (labelObj != null && ! String.valueOf(labelObj).equalsIgnoreCase("null") && ! String.valueOf(labelObj).isEmpty() && ! String.valueOf(labelObj).equalsIgnoreCase("na")) {
                                        label = String.valueOf(labelObj);
                                    }
                                    else {
                                        label = "";
                                    }
                                }
                                if (label.isEmpty() && w9Id != null) {
                                    label = featureLayer.getLabel() + " - " + w9Id;
                                }
                                featurePropHashMap.put(featureLayer.getLabelPropertyName(), label);
                                if (w9Id != null) {
                                    feature = new Feature();//maintain this sequence, we need entity,attr and expression to set feature label
                                    feature.setFeatureId(w9Id);
                                    feature.setFeatureLabelExpression(featureLayer.getLabelPropertyName());
                                    feature.setAttributes(featurePropHashMap);
                                    feature.setEntityName(featureLayer.getName());
                                    feature.setFeatureLabel("");//"" or null sets lable acc to expression or feature id

                                    if (featureJObj.has("geometry")) {
                                        feature.setGeoJsonGeometry(featureJObj.getJSONObject("geometry"));
                                        feature.setGeometryType(GeoJsonUtils.getGeometryType(featureJObj.getJSONObject("geometry")));
                                    }
                                    featureList.add(feature);
                                }
                            } catch (java.lang.Exception e) {
                                e.printStackTrace();
                                ReveloLogger.error(className, "constructing gdbfeature from json", "feature construction failed, reason: " + e.getMessage());
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ReveloLogger.info(className, "getGeometryAndIdFeatureList", "Feature list size :" + featureList.size());
        return featureList;
    }

    public List<Feature> getGeometryAndIdFeatureListAsLike(String w9IdPropertyName, Context context, String andOrOr, Map<String, Object> conditionMap) {

        List<Feature> featureList = new ArrayList<>();

        try {
            List<String> requiredColumnsList = new ArrayList<>();
            requiredColumnsList.add(w9IdPropertyName);
            requiredColumnsList.add(featureLayer.getLabelPropertyName());
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);

            boolean queryGeometry = false;
            if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = true;
            }
            HashMap<String, JSONObject> columnNameValueConditionMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : conditionMap.entrySet()) {
                JSONObject conditionObj = new JSONObject();
                conditionObj.put("value", entry.getValue());
                conditionObj.put("operator", "=");
                conditionObj.put("columnType", Objects.requireNonNull(featureLayer.getPropertiesHashMap().get(entry.getKey())).getType());
                columnNameValueConditionMap.put(entry.getKey(), conditionObj);
            }
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), requiredColumnsList, columnNameValueConditionMap, andOrOr, true, - 1, queryGeometry);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = new Feature();
                            try {
                                JSONObject propertiesJobjReceived = featureJObj.getJSONObject("properties");
                                HashMap<String, Object> featurePropHashMap = new HashMap<>();
                                Object w9Id = null;
                                String label = "";

                                if (propertiesJobjReceived.has(w9IdPropertyName)) {
                                    w9Id = propertiesJobjReceived.get(w9IdPropertyName);
                                    featurePropHashMap.put(w9IdPropertyName, w9Id);
                                }
                                if (propertiesJobjReceived.has(featureLayer.getLabelPropertyName())) {
                                    Object labelObj = propertiesJobjReceived.get(featureLayer.getLabelPropertyName());
                                    if (labelObj != null && ! String.valueOf(labelObj).equalsIgnoreCase("null") && ! String.valueOf(labelObj).isEmpty() && ! String.valueOf(labelObj).equalsIgnoreCase("na")) {
                                        label = String.valueOf(labelObj);
                                    }
                                    else {
                                        label = "";
                                    }
                                }
                                if (label.isEmpty() && w9Id != null) {
                                    label = featureLayer.getLabel() + " - " + w9Id;
                                }
                                featurePropHashMap.put(featureLayer.getLabelPropertyName(), label);
                                if (w9Id != null) {
                                    feature = new Feature();//maintain this sequence, we need entity,attr and expression to set feature label
                                    feature.setFeatureId(w9Id);
                                    feature.setAttributes(featurePropHashMap);
                                    feature.setEntityName(featureLayer.getName());
                                    feature.setFeatureLabelExpression(featureLayer.getLabelPropertyName());
                                    feature.setFeatureLabel("");//"" or null sets lable acc to expression or feature id

                                    if (featureJObj.has("geometry")) {
                                        feature.setGeoJsonGeometry(featureJObj.getJSONObject("geometry"));
                                        feature.setGeometryType(GeoJsonUtils.getGeometryType(featureJObj.getJSONObject("geometry")));
                                    }
                                    featureList.add(feature);
                                }
                            } catch (java.lang.Exception e) {
                                e.printStackTrace();
                                ReveloLogger.error(className, "constructing gdbfeature from json", "feature construction failed, reason: " + e.getMessage());
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ReveloLogger.info(className, "getGeometryAndIdFeatureList", "Feature list size :" + featureList.size());
        return featureList;
    }

    public JSONObject getQueryResponse(Context context, List<String> requiredColumnsList, JSONArray whereclauseArray, String ANDorOR, JSONArray compulsoryConditionsArray, boolean isDistinct, int limit, boolean getFromShadowTable, boolean queryGeometry, boolean transformGeometry) {

        JSONObject respJObj = new JSONObject();
        try {
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(getFromShadowTable), null, null, "", true, - 1, queryGeometry);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respJObj;
    }

    public List<Feature> getallFeaturesList(Context context, List<String> requiredFields, boolean getFromShadowTable) {

        List<Feature> featureList = new ArrayList<>();
        try {

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);

            boolean queryGeometry = false;
            if (! getFromShadowTable && featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = true;
            }

            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(getFromShadowTable), null, null, "", true, - 1, queryGeometry);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = constructFeatureFromJson(featureJObj);
                            if (feature != null) {
                                featureList.add(feature);
                            }
                            else {
                                ReveloLogger.error(className, "getGeometryAndIdFeatureList", "feature not added to list as null value for feature received");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ReveloLogger.info(className, "getGeometryAndIdFeatureList", "Exception occurred while creating features list - Exception: " + e.getMessage());
            e.printStackTrace();
        }
        ReveloLogger.info(className, "getGeometryAndIdFeatureList", "Feature list size :" + featureList.size());
        return featureList;
    }

    public List<Feature> getallFeaturesList(Context context, List<String> requiredFields, HashMap<String, Object> fieldValueMap, boolean getFromShadowTable, boolean queryGeometry) {

        List<Feature> featureList = new ArrayList<>();
        try {
            HashMap<String, JSONObject> conditionMap = null;
            if (fieldValueMap != null && fieldValueMap.size() > 0) {
                conditionMap = new HashMap<>();
                JSONObject conditionObj = new JSONObject();
                for (String columnName : fieldValueMap.keySet()) {
                    conditionObj.put("value", fieldValueMap.get(columnName));
                    conditionObj.put("operator", "=");
                    conditionObj.put("columnType", featureLayer.getPropertiesHashMap().get(columnName).getType());
                    conditionMap.put(columnName, conditionObj);

                }
            }
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);

            //boolean queryGeometry = false;
            if (! featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = false;
            }
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(getFromShadowTable), requiredFields, conditionMap, "", true, - 1, queryGeometry);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = constructFeatureFromJson(featureJObj);
                            if (feature != null) {
                                featureList.add(feature);
                            }
                            else {
                                ReveloLogger.error(className, "getallFeaturesList", "not adding feature to list. Reason - null value received");
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            ReveloLogger.error(className, "getallFeaturesList", "feature construction failed, reason -Exception: " + e.getMessage());
            e.printStackTrace();
        }
        ReveloLogger.info(className, "getallFeaturesList", "Feature list size :" + featureList.size());
        return featureList;
    }

    public List<Feature> getallFeaturesList(Context context, List<String> requiredFields, JSONArray fieldValueWhereClauseOR, JSONArray fieldValueWhereClauseAND, boolean getFromShadowTable, boolean queryGeometry) {

        List<Feature> featureList = new ArrayList<>();
        try {

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);

            //boolean queryGeometry = false;
            if (! featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = false;
            }

            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(getFromShadowTable), requiredFields, fieldValueWhereClauseOR, "OR", fieldValueWhereClauseAND, true, - 1, queryGeometry, true);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = constructFeatureFromJson(featureJObj);
                            featureList.add(feature);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ReveloLogger.info(className, "getGeometryAndIdFeatureList", "Feature list size :" + featureList.size());
        return featureList;
    }

    public List<Feature> getFeaturesAsLike(List<String> requiredFields, Map<String, Object> fields, Context context) {

        List<Feature> featureList = new ArrayList<>();

        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();
            /*JSONObject conditionObj = new JSONObject();
            for (String columnName : fields.keySet()) {
                conditionObj.put("value", fields.get(columnName));
                conditionObj.put("operator", "LIKE");
                conditionMap.put(columnName, conditionObj);
            }*/
            JSONObject conditionObj = new JSONObject();
            for (String columnName : fields.keySet()) {
                conditionObj.put("value", fields.get(columnName));
                conditionObj.put("operator", "=");
                conditionObj.put("columnType", featureLayer.getPropertiesHashMap().get(columnName).getType());
                conditionMap.put(columnName, conditionObj);
            }

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            boolean queryGeometry = false;
            if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = true;
            }
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), requiredFields, conditionMap, "", true, - 1, queryGeometry);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = constructFeatureFromJson(featureJObj);
                            if (feature != null) {
                                featureList.add(feature);
                            }
                            else {
                                ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - null value received");
                            }
                        }
                    }
                }

            }

        } catch (java.lang.Exception e) {
            ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return featureList;
    }

    public List<Feature> getFeaturesAsLike(List<String> requiredFields, Map<Attribute, Object> fields, String ANDorOR, Map<Attribute, Object> compulsoryConditions, Context context, boolean canGetFromShadowTable) {

        List<Feature> featureList = new ArrayList<>();

        try {
            JSONArray whereClauseArray = new JSONArray();
            if (fields != null && ! fields.isEmpty()) {
                for (Attribute attribute : fields.keySet()) {
                    if (fields.get(attribute) != null) {
                        for (int i = 0; i < 4; i++) {
                            String value = String.valueOf(fields.get(attribute));
                            switch (i) {
                                case 0:
                                    value = String.valueOf(fields.get(attribute));
                                    break;
                                case 1:
                                    value = "%" + fields.get(attribute);
                                    break;
                                case 2:
                                    value = fields.get(attribute) + "%";
                                    break;
                                case 3:
                                    value = "%" + fields.get(attribute) + "%";
                                    break;
                            }
                            JSONObject conditionJobj = new JSONObject();
                            conditionJobj.put("conditionType", "attribute");
                            conditionJobj.put("columnName", attribute.getName());
                            conditionJobj.put("valueDataType", attribute.getType());
                            conditionJobj.put("value", value);
                            conditionJobj.put("operator", "LIKE");
                            whereClauseArray.put(conditionJobj);
                        }
                    }
                }
            }
            else {
                whereClauseArray = null;
            }
            JSONArray compulsorywhereClauseArray = new JSONArray();
            if (compulsoryConditions != null && ! compulsoryConditions.isEmpty()) {
                for (Attribute attribute : compulsoryConditions.keySet()) {
                    if (compulsoryConditions.get(attribute) != null) {
                        JSONObject conditionJobj = new JSONObject();
                        conditionJobj.put("conditionType", "attribute");
                        conditionJobj.put("columnName", attribute.getName());
                        conditionJobj.put("valueDataType", attribute.getType());
                        conditionJobj.put("value", compulsoryConditions.get(attribute));
                        conditionJobj.put("operator", "=");
                        compulsorywhereClauseArray.put(conditionJobj);
                    }
                }
            }
            else {
                compulsorywhereClauseArray = null;
            }
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            boolean queryGeometry = false;
            if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = true;
            }
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(canGetFromShadowTable), requiredFields, whereClauseArray, ANDorOR, compulsorywhereClauseArray, true, - 1, queryGeometry, true);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = constructFeatureFromJson(featureJObj);
                            if (feature != null) {
                                featureList.add(feature);
                            }
                            else {
                                ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - null value received");
                            }
                        }
                    }
                }

            }
        } catch (java.lang.Exception e) {
            ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - Exception :" + e.getMessage());
            e.printStackTrace();
        }

        return featureList;
    }
    public List<Feature> getFeaturesAsLike(List<String> requiredFields,JSONArray whereClauseArray, String ANDorOR, Map<Attribute, Object> compulsoryConditions, Context context, boolean canGetFromShadowTable) {

        List<Feature> featureList = new ArrayList<>();

        try {
            JSONArray compulsorywhereClauseArray = new JSONArray();
            if (compulsoryConditions != null && ! compulsoryConditions.isEmpty()) {
                for (Attribute attribute : compulsoryConditions.keySet()) {
                    if (compulsoryConditions.get(attribute) != null) {
                        JSONObject conditionJobj = new JSONObject();
                        conditionJobj.put("conditionType", "attribute");
                        conditionJobj.put("columnName", attribute.getName());
                        conditionJobj.put("valueDataType", attribute.getType());
                        conditionJobj.put("value", compulsoryConditions.get(attribute));
                        conditionJobj.put("operator", "=");
                        compulsorywhereClauseArray.put(conditionJobj);
                    }
                }
            }
            else {
                compulsorywhereClauseArray = null;
            }
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            boolean queryGeometry = false;
            if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = true;
            }
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(canGetFromShadowTable), requiredFields, whereClauseArray, ANDorOR, compulsorywhereClauseArray, true, - 1, queryGeometry, true);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = constructFeatureFromJson(featureJObj);
                            if (feature != null) {
                                featureList.add(feature);
                            }
                            else {
                                ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - null value received");
                            }
                        }
                    }
                }

            }
        } catch (java.lang.Exception e) {
            ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - Exception :" + e.getMessage());
            e.printStackTrace();
        }

        return featureList;
    }

    public List<Feature> getFeaturesAsLike(List<String> requiredFields, Map<Attribute, Object> fields, String ANDorOR, Map<Attribute, Object> compulsoryConditions, Context context) {

        List<Feature> featureList = new ArrayList<>();

        try {
            JSONArray whereClauseArray = new JSONArray();
            if (fields != null && ! fields.isEmpty()) {
                for (Attribute attribute : fields.keySet()) {
                    if (fields.get(attribute) != null) {
                        for (int i = 0; i < 4; i++) {
                            String value = String.valueOf(fields.get(attribute));
                            switch (i) {
                                case 0:
                                    value = String.valueOf(fields.get(attribute));
                                    break;
                                case 1:
                                    value = "%" + fields.get(attribute);
                                    break;
                                case 2:
                                    value = fields.get(attribute) + "%";
                                    break;
                                case 3:
                                    value = "%" + fields.get(attribute) + "%";
                                    break;
                            }
                            JSONObject conditionJobj = new JSONObject();
                            conditionJobj.put("conditionType", "attribute");
                            conditionJobj.put("columnName", attribute.getName());
                            conditionJobj.put("valueDataType", attribute.getType());
                            conditionJobj.put("value", value);
                            conditionJobj.put("operator", "LIKE");
                            whereClauseArray.put(conditionJobj);
                        }
                    }
                }
            }
            else {
                whereClauseArray = null;
            }
            JSONArray compulsorywhereClauseArray = new JSONArray();
            if (compulsoryConditions != null && ! compulsoryConditions.isEmpty()) {
                for (Attribute attribute : compulsoryConditions.keySet()) {
                    if (compulsoryConditions.get(attribute) != null) {
                        JSONObject conditionJobj = new JSONObject();
                        conditionJobj.put("conditionType", "attribute");
                        conditionJobj.put("columnName", attribute.getName());
                        conditionJobj.put("valueDataType", attribute.getType());
                        conditionJobj.put("value", compulsoryConditions.get(attribute));
                        conditionJobj.put("operator", "=");
                        compulsorywhereClauseArray.put(conditionJobj);
                    }
                }
            }
            else {
                compulsorywhereClauseArray = null;
            }
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            boolean queryGeometry = false;
            if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = true;
            }
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), requiredFields, whereClauseArray, ANDorOR, compulsorywhereClauseArray, true, - 1, queryGeometry, true);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = constructFeatureFromJson(featureJObj);
                            if (feature != null) {
                                featureList.add(feature);
                            }
                            else {
                                ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - null value received");
                            }
                        }
                    }
                }

            }
        } catch (java.lang.Exception e) {

            ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - Exception : " + e.getMessage());

            e.printStackTrace();
        }

        return featureList;
    }

    public List<Feature> getFeaturesAsLike(List<String> requiredFields, JSONArray whereClauseArray, String ANDorOR, Context context) {

        List<Feature> featureList = new ArrayList<>();

        try {
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            boolean queryGeometry = false;
            if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                queryGeometry = true;
            }
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), null, whereClauseArray, ANDorOR, true, - 1, queryGeometry, true);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Feature feature = constructFeatureFromJson(featureJObj);
                            if (feature != null) {
                                featureList.add(feature);
                            }
                            else {
                                ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - null value received");
                            }
                        }
                    }
                }

            }
        } catch (java.lang.Exception e) {

            ReveloLogger.error(className, "getFeaturesAsLike", "not adding feature to list. Reason - Exception: " + e.getMessage());

            e.printStackTrace();
        }

        ReveloLogger.info(className, "getFeaturesAsLike", "returning list of size - " + featureList.size());

        return featureList;
    }

    //TODO CHANGE - method takes jts geom, converts it to mil nga geometry and updates the featureDao
    public JSONObject insertAddRecordInDbAndMap(Map<String, Object> attributeValueMap, JSONObject geometry, Context context, BottomSheetDialog fragmentBottomSheet, String w9Id, String label, List<Attachment> attachmentList, Location location, JSONObject permissionJson, String submitStatus) {
        JSONObject resultJson = new JSONObject();
        String message = "Unable to add feature.";
        try {
            resultJson.put("status", "failure");
            resultJson.put("message", message + " " + "Reason: Unknown");


            ReveloLogger.info(className, "insertAddRecordInDbAndMap", "inserting rec for w9id = " + w9Id + " in " + featureLayer.getName());
            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();
            for (String attributeName : attributeValueMap.keySet()) {
                JSONObject attributeObj = new JSONObject();
                attributeObj.put("name", attributeName);
                if (attributeValueMap.get(attributeName) == null) {
                    attributeValueMap.put(attributeName, featureLayer.getPropertiesHashMap().get(attributeName).getDefaultValue());
                }
                if (attributeValueMap.get(attributeName) != null) {
                    attributeObj.put("value", attributeValueMap.get(attributeName));
                    attributesJArray.put(attributeObj);
                }
            }
            dataObject.put("attributes", attributesJArray);
            ReveloLogger.info(className, "insertAddRecordInDbAndMap", "added attributes array for w9id = " + w9Id + " in json");
            if (geometry != null) {
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "geometry for w9id = " + w9Id + " not null, adding it in json");
                dataObject.put("geometry", geometry);
            }
            else {
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "geometry for w9id = " + w9Id + " is null, not adding it in json");
            }
            ReveloLogger.info(className, "insertAddRecordInDbAndMap", "adding data json for w9id = " + w9Id + " in data array");
            dataJsonArray.put(dataObject);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getDatasetInfo(false);

            JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "positive response received from db insert query");
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "proceeding to add metadata and attachments");
                boolean isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "is flows applicable to entity " + featureLayer.getName() + "? -" + isFlowsApplicable);
                boolean procceedFuther = false;
                if (isFlowsApplicable) {

                    ReveloLogger.info(className, "insertAddRecordInDbAndMap", "flows applicable. adding metadata entry for " + w9Id + " interaction name: " + permissionJson.getString("currentInteractionName"));
                    boolean isSuccessfullyAdded = insertMetadataEntry(context, featureLayer.getName(), w9Id, permissionJson.getString("currentFlowName"), permissionJson.getString("currentInteractionName"), submitStatus);
                    if (isSuccessfullyAdded) {
                        ReveloLogger.info(className, "insertAddRecordInDbAndMap", "metadata added successfully");
                        procceedFuther = true;
                    }
                }
                else {
                    ReveloLogger.info(className, "insertAddRecordInDbAndMap", "flows not applicable,moving to add attachments");
                    procceedFuther = true;
                }

                if (procceedFuther) {

                    //insert attachment record.
                    int attachmentsAdded = - 1;
                    if (attachmentList != null && attachmentList.size() > 0) {
                        ReveloLogger.info(className, "insertAddRecordInDbAndMap", "begin to add attachments");
                        attachmentsAdded = insertAddAttachmentRecord(attachmentList, w9Id, featureLayer.getName(), context, location);
                    }
                    else {
                        ReveloLogger.info(className, "insertAddRecordInDbAndMap", "no attachments found to add. Skipping this step");
                        attachmentsAdded = 0;
                    }
                    ReveloLogger.info(className, "insertAddRecordInDbAndMap", "begin to add editmetadata -operation add w9id " + w9Id + " layer- " + featureLayer.getName());
                    EditMetaDataTable.insertAddRecord(w9Id, label, context, featureLayer.getName());  // insert add entry to editmetadata.

                    message = "A new " + featureLayer.getLabel() + " called " + label + " added";
                    if (attachmentsAdded != - 1) {
                        message += " with " + attachmentsAdded + " attachments";
                    }
                   /* try {
                        ReveloLogger.info(className, "insertAddRecordInDbAndMap", "sending response to activity-operation add w9id " + w9Id + " layer- " + featureLayer.getName());
                        ((IAddFeatureToDb) context).addFeatureResponse(context, message, featureLayer, geometry, w9Id, label);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fragmentBottomSheet != null)
                        fragmentBottomSheet.dismiss();
                    return true;*/
                    resultJson.put("status", "success");
                    resultJson.put("message", message);
                }
                else {
                    ReveloLogger.error(className, "insertAddRecordInDbAndMap", "adding feature failed.");
                    ToastUtility.toast(featureLayer.getLabel() + " not added. Please try after sometime.", context, false);
                    /*if (fragmentBottomSheet != null)
                        fragmentBottomSheet.dismiss();
                    return false;*/
                    message += " " + "Reason: Unknown";
                    if (respJObj.has("message") && ! respJObj.getString("message").isEmpty()) {
                        message = respJObj.getString("message");
                    }
                    resultJson.put("status", "failure");
                    resultJson.put("message", message);
                }

            }
            else {
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "negative response received from db insert query");
                ToastUtility.toast(featureLayer.getLabel() + " not added. Please try after sometime.", context, false);
                /*return false;*/
                message += " " + "Reason: Unknown";
                if (respJObj.has("message") && ! respJObj.getString("message").isEmpty()) {
                    message = respJObj.getString("message");
                }
                resultJson.put("status", "failure");
                resultJson.put("message", message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                resultJson.put("status", "failure");
                resultJson.put("message", message + " " + "Reason: exception - " + e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return resultJson;
    }
    public JSONObject insertAddRecordInDbAndMapPhantomCustom(Map<String, Object> attributeValueMap, JSONObject geometry, Context context, String w9Id, String label, List<Attachment> attachmentList, Location location, JSONObject permissionJson, String submitStatus) {
        JSONObject resultJson = new JSONObject();
        String message = "Unable to add feature.";
        try {
            resultJson.put("status", "failure");
            resultJson.put("message", message + " " + "Reason: Unknown");
            ReveloLogger.info(className, "insertAddRecordInDbAndMap", "inserting rec for w9id = " + w9Id + " in " + featureLayer.getName());
            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();
            for (String attributeName : attributeValueMap.keySet()) {
                JSONObject attributeObj = new JSONObject();
                attributeObj.put("name", attributeName);
                if (attributeValueMap.get(attributeName) == null) {
                    attributeValueMap.put(attributeName, featureLayer.getPropertiesHashMap().get(attributeName).getDefaultValue());
                }
                if (attributeValueMap.get(attributeName) != null) {
                    attributeObj.put("value", attributeValueMap.get(attributeName));
                    attributesJArray.put(attributeObj);
                }
            }
            dataObject.put("attributes", attributesJArray);
            ReveloLogger.info(className, "insertAddRecordInDbAndMap", "added attributes array for w9id = " + w9Id + " in json");
            if (geometry != null) {
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "geometry for w9id = " + w9Id + " not null, adding it in json");
                dataObject.put("geometry", geometry);
            }
            else {
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "geometry for w9id = " + w9Id + " is null, not adding it in json");
            }
            ReveloLogger.info(className, "insertAddRecordInDbAndMap", "adding data json for w9id = " + w9Id + " in data array");
            dataJsonArray.put(dataObject);



            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);

            JSONObject datasetInfo = getDatasetInfo(false);

            JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "positive response received from db insert query");
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "proceeding to add metadata and attachments");
//                boolean isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
//                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "is flows applicable to entity " + featureLayer.getName() + "? -" + isFlowsApplicable);
                boolean procceedFuther = true;
//                if (isFlowsApplicable) {
//
//                    ReveloLogger.info(className, "insertAddRecordInDbAndMap", "flows applicable. adding metadata entry for " + w9Id + " interaction name: " + permissionJson.getString("currentInteractionName"));
//                    boolean isSuccessfullyAdded = insertMetadataEntry(context, featureLayer.getName(), w9Id, permissionJson.getString("currentFlowName"), permissionJson.getString("currentInteractionName"), submitStatus);
//                    if (isSuccessfullyAdded) {
//                        ReveloLogger.info(className, "insertAddRecordInDbAndMap", "metadata added successfully");
//                        procceedFuther = true;
//                    }
//                }
//                else {
//                    ReveloLogger.info(className, "insertAddRecordInDbAndMap", "flows not applicable,moving to add attachments");
//                    procceedFuther = true;
//                }

                if (procceedFuther) {
                    //insert attachment record.
                    int attachmentsAdded = - 1;
                    if (attachmentList != null && attachmentList.size() > 0) {
                        ReveloLogger.info(className, "insertAddRecordInDbAndMap", "begin to add attachments");
                        attachmentsAdded = insertAddAttachmentRecord(attachmentList, w9Id, featureLayer.getName(), context, location);
                    }
                    else {
                        ReveloLogger.info(className, "insertAddRecordInDbAndMap", "no attachments found to add. Skipping this step");
                        attachmentsAdded = 0;
                    }
                    ReveloLogger.info(className, "insertAddRecordInDbAndMap", "begin to add editmetadata -operation add w9id " + w9Id + " layer- " + featureLayer.getName());
                    message = "A new " + featureLayer.getLabel() + " called " + label + " added";
                    if (attachmentsAdded != - 1) {
                        message += " with " + attachmentsAdded + " attachments";
                    }
                    resultJson.put("status", "success");
                    resultJson.put("message", message);
                }
                else {
                    ReveloLogger.error(className, "insertAddRecordInDbAndMap", "adding feature failed.");
                    message += " " + "Reason: Unknown";
                    if (respJObj.has("message") && ! respJObj.getString("message").isEmpty()) {
                        message = respJObj.getString("message");
                    }
                    resultJson.put("status", "failure");
                    resultJson.put("message", message);
                }

            }
            else {
                ReveloLogger.info(className, "insertAddRecordInDbAndMap", "negative response received from db insert query");
                message += " " + "Reason: Unknown";
                if (respJObj.has("message") && ! respJObj.getString("message").isEmpty()) {
                    message = respJObj.getString("message");
                }
                resultJson.put("status", "failure");
                resultJson.put("message", message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                resultJson.put("status", "failure");
                resultJson.put("message", message + " " + "Reason: exception - " + e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return resultJson;
    }

    private boolean insertMetadataEntry(Context context, String layerName, String w9Id, String currentFlowName, String currentInteractionName, String submitNow) {

        try {
            if (submitNow.equalsIgnoreCase(AppConstants.SAVE_APPROVE)) {
                //find next node
                Interaction interaction = CMUtils.getNextInteraction(context, layerName, currentFlowName, currentInteractionName);
                if (interaction != null) {
                    currentInteractionName = interaction.getInteractionName();
                }
            }
            else if (submitNow.equalsIgnoreCase(AppConstants.SAVE_DISAPPROVE)) {
                //find next node
                Interaction interaction = CMUtils.getPreviousInteraction(context, layerName, currentFlowName, currentInteractionName);
                if (interaction != null) {
                    currentInteractionName = interaction.getInteractionName();
                }
            }

            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();

            JSONObject attributeObjw9id = new JSONObject();
            attributeObjw9id.put("name", MetadataTableConstant.W9ID);
            attributeObjw9id.put("value", w9Id);
            attributesJArray.put(attributeObjw9id);

            JSONObject attributeObjInteractionName = new JSONObject();
            attributeObjInteractionName.put("name", MetadataTableConstant.INTERACTIONNAME);
            attributeObjInteractionName.put("value", currentInteractionName);
            attributesJArray.put(attributeObjInteractionName);

            JSONObject attributeObjFlowName = new JSONObject();
            attributeObjFlowName.put("name", MetadataTableConstant.FLOWNAME);
            attributeObjFlowName.put("value", currentFlowName);
            attributesJArray.put(attributeObjFlowName);


            dataObject.put("attributes", attributesJArray);
            dataJsonArray.put(dataObject);


            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getMetadataDatasetInfo();
            JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray);

            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");


        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public int insertAddAttachmentRecord(List<Attachment> attachments, String w9Id, String layerName, Context context, Location location) {

        int attachmentCount = 0;
        try {
            ReveloLogger.info(className, "insertAddAttachmentRecord", "begin inserting attachments for feature - " + w9Id + " layer -" + layerName);
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            double zValue = location.getAltitude();
            double accuracy = location.getAccuracy();
            ReveloLogger.info(className, "insertAddAttachmentRecord", "total num of attachments - " + attachments.size());


            JSONArray dataJsonArray = new JSONArray();
            for (Attachment attachment : attachments) {
                attachmentCount++;
                ReveloLogger.info(className, "insertAddAttachmentRecord", "creating data json for attachment no " + attachmentCount);
                int fileSize = (int) attachment.getFile().length();
                byte[] attachmentFile = null;
                attachmentFile = AppMethods.readFileToByteArray(attachment.getFile());

                JSONObject dataObject = new JSONObject();
                JSONArray attributesJArray = new JSONArray();

                JSONObject attributeObjw9id = new JSONObject();
                attributeObjw9id.put("name", AttachmentConstant.W9ID);
                attributeObjw9id.put("value", w9Id);
                attributesJArray.put(attributeObjw9id);

                JSONObject attributeObjMimeType = new JSONObject();
                attributeObjMimeType.put("name", AttachmentConstant.MIMETYPE);
                attributeObjMimeType.put("value", attachment.getContentType());
                attributesJArray.put(attributeObjMimeType);

                JSONObject attributeObjUserName = new JSONObject();
                attributeObjUserName.put("name", AttachmentConstant.USERNAME);
                attributeObjUserName.put("value", UserInfoPreferenceUtility.getUserName());
                attributesJArray.put(attributeObjUserName);

                JSONObject attributeObjUserRole = new JSONObject();
                attributeObjUserRole.put("name", AttachmentConstant.USERROLE);
                attributeObjUserRole.put("value", UserInfoPreferenceUtility.getRole());
                attributesJArray.put(attributeObjUserRole);

                JSONObject attributeObjName = new JSONObject();
                attributeObjName.put("name", AttachmentConstant.NAME);
                attributeObjName.put("value", attachment.getAttachmentName());
                attributesJArray.put(attributeObjName);

                JSONObject attributeObjSize = new JSONObject();
                attributeObjSize.put("name", AttachmentConstant.SIZE);
                attributeObjSize.put("value", fileSize);
                attributesJArray.put(attributeObjSize);

                JSONObject attributeObjLat = new JSONObject();
                attributeObjLat.put("name", AttachmentConstant.LAT);
                attributeObjLat.put("value", lat);
                attributesJArray.put(attributeObjLat);

                JSONObject attributeObjLong = new JSONObject();
                attributeObjLong.put("name", AttachmentConstant.LNG);
                attributeObjLong.put("value", lng);
                attributesJArray.put(attributeObjLong);

                JSONObject attributeObjZValue = new JSONObject();
                attributeObjZValue.put("name", AttachmentConstant.ZVALUE);
                attributeObjZValue.put("value", zValue);
                attributesJArray.put(attributeObjZValue);

                JSONObject attributeObjAccuracy = new JSONObject();
                attributeObjAccuracy.put("name", AttachmentConstant.ACCURACY);
                attributeObjAccuracy.put("value", accuracy);
                attributesJArray.put(attributeObjAccuracy);

                JSONObject attributeObjCreationDate = new JSONObject();
                attributeObjCreationDate.put("name", AttachmentConstant.CREATIONDATE);
                attributeObjCreationDate.put("value", attachment.getDateTimeStamp());
                attributesJArray.put(attributeObjCreationDate);

                JSONObject attributeObjSavedDate = new JSONObject();
                attributeObjSavedDate.put("name", AttachmentConstant.SAVEDATE);
                attributeObjSavedDate.put("value", attachment.getDateTimeStamp());
                attributesJArray.put(attributeObjSavedDate);

                JSONObject attributeObjContent = new JSONObject();
                attributeObjContent.put("name", AttachmentConstant.CONTENT);
                attributeObjContent.put("value", attachmentFile);
                attributesJArray.put(attributeObjContent);

                JSONObject attributeObjIsNew = new JSONObject();
                attributeObjIsNew.put("name", AttachmentConstant.ISNEW);
                attributeObjIsNew.put("value", AttachmentConstant.ADD_OP);
                attributesJArray.put(attributeObjIsNew);

                JSONObject attributeObjCaption = new JSONObject();
                attributeObjCaption.put("name", AttachmentConstant.CAPTION);
                attributeObjCaption.put("value", attachment.getLabel());
                attributesJArray.put(attributeObjCaption);

                dataObject.put("attributes", attributesJArray);
                dataJsonArray.put(dataObject);
            }

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getAttachmentsDatasetInfo();
            JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                ReveloLogger.info(className, "insertAddAttachmentRecord", "adding attachments succeeded");
            }
            else {
                ReveloLogger.info(className, "insertAddAttachmentRecord", "adding attachment failed");
                attachmentCount=-1;
            }

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.info(className, "insertAddAttachmentRecord", "adding attachment failed : exception " + e.getMessage());
        }
        ReveloLogger.info(className, "insertAddAttachmentRecord", "attachments added: " + attachmentCount);
        return attachmentCount;
    }

    private JSONObject getMetadataDatasetInfo() {
        JSONObject datasetInfo = new JSONObject();
        try {
            datasetInfo.put("datasetName", featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + "_metadata");
            datasetInfo.put("datasetType", "table");
            datasetInfo.put("geometryType", "");
            datasetInfo.put("idPropertyName", "w9id");
            datasetInfo.put("w9IdPropertyName", "w9id");
        } catch (JSONException e) {
            ReveloLogger.error(className, "getDatasetInfo", "error initializing getDatasetInfo json for dataset: " + featureLayer.getName() + "_metadata. Exception - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return datasetInfo;
    }

//-----INSERT------------------------------------------------------------------------------------------------------------------------------------------

    private JSONObject getAttachmentsDatasetInfo() {
        ReveloLogger.info(className, "getAttachmentsDatasetInfo", "getting dataset info for " + featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + "_attachments");
        JSONObject datasetInfo = new JSONObject();
        try {
            datasetInfo.put("datasetName", featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + "_attachments");
            datasetInfo.put("datasetType", "table");
            datasetInfo.put("geometryType", "");
            datasetInfo.put("idPropertyName", AttachmentConstant.NAME);
            datasetInfo.put("w9IdPropertyName", AttachmentConstant.NAME);
        } catch (JSONException e) {
            ReveloLogger.error(className, "getDatasetInfo", "error initializing getDatasetInfo json for attachment type dataset for : " + featureLayer.getName() + ". Exception - " + e.getMessage());
            e.printStackTrace();
        }
        return datasetInfo;
    }

    //used only by trails
    public void insertAddRecordInDb(Map<String, Object> attributeValueMap, JSONObject geometry, Context context, String w9Id, List<Attachment> attachmentList, Location location) {

        try {
            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();
            for (String attributeName : attributeValueMap.keySet()) {
                JSONObject attributeObj = new JSONObject();
                attributeObj.put("name", attributeName);
                attributeObj.put("value", attributeValueMap.get(attributeName));
                attributesJArray.put(attributeObj);
            }
            dataObject.put("attributes", attributesJArray);
            if (geometry != null) {
                dataObject.put("geometry", geometry);
            }

            dataJsonArray.put(dataObject);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getDatasetInfo(false);
            datasetInfo.put("w9IdPropertyName", featureLayer.getW9IdProperty());
            JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
               /* int attachmentsAdded =-1;
                if (attachmentList != null && attachmentList.size() > 0) {
                    //insert attachment record.
                    attachmentsAdded = insertAddAttachmentRecord(attachmentList, w9Id, featureLayer.getName(), context, location);
                }else {
                    attachmentsAdded=0;
                }*/

                EditMetaDataTable.insertAddRecord(w9Id, w9Id, context, featureLayer.getName());  // insert add entry to editmetadata.

                String message = featureLayer.getLabel() + " added";
               /* if (attachmentsAdded > 0) {
                    message += " with " + attachmentsAdded + " attachments";
                }*/

            }
            else {
                ToastUtility.toast(featureLayer.getLabel() + " not added. Please try after sometime.", context, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //-----UPDATE---------------------------------------------------------------------------------------------------------------------------------------------
    public JSONObject updateRecordAndMap(Map<String, Object> attributeValueMap, JSONObject geometry, Context activity, BottomSheetDialog fragmentBottomSheet, String w9Id, String label, List<Attachment> attachmentList, Location location, String columnName, List<Attachment> getSelectForDeleteAttachmentFileList, JSONObject permissionJson, String submitStatus) {
        JSONObject resultJson = new JSONObject();
        String message = "Unable to edit feature.";
        try {
            resultJson.put("status", "failure");
            resultJson.put("message", message + " " + "Reason: Unknown");


            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesArray = new JSONArray();
            for (String attributeName : attributeValueMap.keySet()) {
                JSONObject attributesObject = new JSONObject();
                attributesObject.put("name", attributeName);
                if (attributeValueMap.get(attributeName) == null) {
                    attributeValueMap.put(attributeName, featureLayer.getPropertiesHashMap().get(attributeName).getDefaultValue());
                }
                attributesObject.put("value", attributeValueMap.get(attributeName));
                attributesArray.put(attributesObject);
            }
            dataObject.put("attributes", attributesArray);
            if (geometry != null) {
                dataObject.put("geometry", geometry);
            }
            dataJsonArray.put(dataObject);

            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", columnName);
            Attribute attribute = featureLayer.getPropertiesHashMap().get(columnName);
            conditionJobj.put("valueDataType", attribute.getType());
            conditionJobj.put("value", w9Id);
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(activity), new ReveloLogger(), activity);
            JSONObject datasetInfo = getDatasetInfo(false);
            JSONObject respJObj = gpkgRWAgent.updateDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(activity), datasetInfo, dataJsonArray, whereClauseArray, "AND", activity);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                String layerName = featureLayer.getName();


                boolean isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
                boolean procceedFuther = false;
                if (isFlowsApplicable) {
                    ReveloLogger.info(className, "insertAddRecordInDbAndMap", "adding metadata entry for " + w9Id + " interaction name: " + permissionJson.getString("currentInteractionName"));
                    boolean isSuccessfullyAdded = updateMetadataEntry(activity, featureLayer.getName(), w9Id, permissionJson.getString("currentFlowName"), permissionJson.getString("currentInteractionName"), submitStatus);
                    if (isSuccessfullyAdded) {
                        procceedFuther = true;
                    }
                }
                else {
                    procceedFuther = true;
                }

                if (procceedFuther) {
                    //delete attachment record.
                    if (getSelectForDeleteAttachmentFileList != null && getSelectForDeleteAttachmentFileList.size() != 0) {
                        for (Attachment attachment : getSelectForDeleteAttachmentFileList) {
                            String attachmentName = attachment.getAttachmentName();
                            String w9IdAttachment = attachment.getW9Id();
                            boolean isDeleteAttachment = deleteUpdateAttachmentRecord(w9IdAttachment, attachmentName, layerName, activity);
                        }
                    }

                    int attachmentsAdded = - 1;
                    if (attachmentList != null && attachmentList.size() != 0) {
                        attachmentsAdded = insertUpdateAttachmentRecord(attachmentList, w9Id, layerName, activity, location); //insert attachment record.
                    }
                    else {
                        attachmentsAdded = 0;
                    }

                    EditMetaDataTable.insertEditEntry(featureLayer.getName(), w9Id, label, activity);  // insert add entry to editmetadata.


                    message = label + " Record updated properly ";
                    if (attachmentsAdded != - 1) {
                        message += " with " + attachmentsAdded + " attachments";
                    }
                    resultJson.put("status", "success");
                    resultJson.put("message", message);

                }
                /*try {
                    // if (geometry != null)
                    ((IAddFeatureToDb) activity).updateFeatureResponse(activity, message, featureLayer, geometry, w9Id, label);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (fragmentBottomSheet != null) {
                    fragmentBottomSheet.dismiss();
                }*/
            }
            else {
                message += " " + "Reason: Unknown";
                if (respJObj.has("message") && ! respJObj.getString("message").isEmpty()) {
                    message = respJObj.getString("message");
                }
                ToastUtility.toast("Record updated not properly", activity, false);
                resultJson.put("status", "failure");
                resultJson.put("message", message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                resultJson.put("status", "failure");
                resultJson.put("message", message + " " + "Reason: exception - " + e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return resultJson;
    }

    private boolean updateMetadataEntry(Context context, String layerName, String w9Id, String currentFlowName, String currentInteractionName, String submitNow) {

        try {
            Interaction currentInteraction = null;
            if (submitNow.equalsIgnoreCase(AppConstants.SAVE_APPROVE)) {
                //find next node
                Interaction interaction = CMUtils.getNextInteraction(context, featureLayer.getName(), currentFlowName, currentInteractionName);
                if (interaction != null) {
                    currentInteractionName = interaction.getInteractionName();
                    currentInteraction = interaction;
                }
            }
            else if (submitNow.equalsIgnoreCase(AppConstants.SAVE_DISAPPROVE)) {
                //find previous node
                Interaction interaction = CMUtils.getPreviousInteraction(context, featureLayer.getName(), currentFlowName, currentInteractionName);
                if (interaction != null) {
                    currentInteractionName = interaction.getInteractionName();
                    currentInteraction = interaction;
                }
            }
            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();

           /* JSONObject attributeObjw9id = new JSONObject();
            attributeObjw9id.put("name", MetadataTableConstant.W9ID);
            attributeObjw9id.put("value",w9Id);
            attributesJArray.put(attributeObjw9id);*/

            if (currentInteraction != null) {
                currentInteractionName = currentInteraction.getInteractionName();
                currentFlowName = currentInteraction.getFlowName();
            }

            JSONObject attributeObjInteractionName = new JSONObject();
            attributeObjInteractionName.put("name", MetadataTableConstant.INTERACTIONNAME);
            attributeObjInteractionName.put("value", currentInteractionName);
            attributesJArray.put(attributeObjInteractionName);

            JSONObject attributeObjFlowName = new JSONObject();
            attributeObjFlowName.put("name", MetadataTableConstant.FLOWNAME);
            attributeObjFlowName.put("value", currentFlowName);
            attributesJArray.put(attributeObjFlowName);


            dataObject.put("attributes", attributesJArray);
            dataJsonArray.put(dataObject);

            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", MetadataTableConstant.W9ID);
            conditionJobj.put("valueDataType", "string");
            conditionJobj.put("value", w9Id);
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getMetadataDatasetInfo();
            JSONObject respJObj = gpkgRWAgent.updateDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray, whereClauseArray, "AND", context);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                return true;
            }
            else {
                //submitnow = false because submit now makes metadata point to next node. we have already calculated and sent next node to insert fucntion.
                // now we dont want to push it further to grand child node.
                return insertMetadataEntry(context, layerName, w9Id, currentFlowName, currentInteractionName, AppConstants.SAVE_ONLY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean deleteUpdateAttachmentRecord(String w9id, String attachName, String layerName, Context context) {

        boolean isDelete = false;

        try {

            if (w9id != null && attachName != null) {
                JSONArray whereClauseArrayDeleteAttach = new JSONArray();
                JSONArray whereClauseArrayUpdateIsNew = new JSONArray();

                JSONObject conditionJobjW9id = new JSONObject();
                conditionJobjW9id.put("conditionType", "attribute");
                conditionJobjW9id.put("columnName", AttachmentConstant.W9ID);
                conditionJobjW9id.put("valueDataType", "string");
                conditionJobjW9id.put("value", w9id);
                conditionJobjW9id.put("operator", "=");
                whereClauseArrayDeleteAttach.put(conditionJobjW9id);


                JSONObject conditionJobjAttachName = new JSONObject();
                conditionJobjAttachName.put("conditionType", "attribute");
                conditionJobjAttachName.put("columnName", AttachmentConstant.NAME);
                conditionJobjAttachName.put("valueDataType", "string");
                conditionJobjAttachName.put("value", attachName);
                conditionJobjAttachName.put("operator", "=");
                whereClauseArrayDeleteAttach.put(conditionJobjAttachName);


                JSONObject conditionJobjisNew = new JSONObject();
                conditionJobjisNew.put("conditionType", "attribute");
                conditionJobjisNew.put("columnName", AttachmentConstant.ISNEW);
                conditionJobjisNew.put("valueDataType", "integer");
                conditionJobjisNew.put("value", AttachmentConstant.NONE_OP);
                conditionJobjisNew.put("operator", "=");
                whereClauseArrayDeleteAttach.put(conditionJobjAttachName);

                GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
                JSONObject datasetInfo = getAttachmentsDatasetInfo();
//                JSONObject respJObj = gpkgRWAgent.deleteDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, whereClauseArrayDeleteAttach, "AND", context);
//                isDelete = respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");

                JSONArray dataJsonArray = new JSONArray();

                JSONObject dataObject = new JSONObject();
                JSONArray attributesArray = new JSONArray();

                JSONObject attributesObject = new JSONObject();
                attributesObject.put("name", AttachmentConstant.ISNEW);
                attributesObject.put("value", AttachmentConstant.DELETE_OP);
                attributesArray.put(attributesObject);

                dataObject.put("attributes", attributesArray);
                dataJsonArray.put(dataObject);
                JSONObject respUpdateJObj = gpkgRWAgent.updateDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray, whereClauseArrayDeleteAttach, "AND", context);

                isDelete = respUpdateJObj.has("status") && respUpdateJObj.getString("status").equalsIgnoreCase("success");
                return isDelete;
//                if (respUpdateJObj.has("status") && respUpdateJObj.getString("status").equalsIgnoreCase("success")) {
//                    isDelete = isDelete & true;
//                } else {
//                    isDelete = isDelete & false;
//                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isDelete;
    }

    private int insertUpdateAttachmentRecord(List<Attachment> attachments, String w9Id, String layerName,
                                             Context context, Location location) {

        int attachmentCount = 0;
        try {
            ReveloLogger.info(className, "insertUpdateAttachmentRecord", "begin inserting attachments for feature - " + w9Id + " layer -" + layerName);
            double lat = 0.0d;
            double lng = 0.0d;
            double zValue = 0.0d;
            double accuracy = 0.0d;
            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
                zValue = location.getAltitude();
                accuracy = location.getAccuracy();
            }
            ReveloLogger.info(className, "insertUpdateAttachmentRecord", "total num of attachments - " + attachments.size());

            JSONArray dataJsonArray = new JSONArray();
            for (Attachment attachment : attachments) {
                if (! TextUtils.isEmpty(attachment.getFormType()) && attachment.getFormType().equalsIgnoreCase(AppConstants.EDIT)) {
                    attachmentCount++;
                    ReveloLogger.info(className, "insertUpdateAttachmentRecord", "creating data json for attachment no " + attachmentCount);
                    int fileSize = (int) attachment.getFile().length();
                    byte[] attachmentFile = null;
                    attachmentFile = AppMethods.readFileToByteArray(attachment.getFile());

                    JSONObject dataObject = new JSONObject();
                    JSONArray attributesJArray = new JSONArray();

                    JSONObject attributeObjw9id = new JSONObject();
                    attributeObjw9id.put("name", AttachmentConstant.W9ID);
                    attributeObjw9id.put("value", w9Id);
                    attributesJArray.put(attributeObjw9id);

                    JSONObject attributeObjMimeType = new JSONObject();
                    attributeObjMimeType.put("name", AttachmentConstant.MIMETYPE);
                    attributeObjMimeType.put("value", attachment.getContentType());
                    attributesJArray.put(attributeObjMimeType);

                    JSONObject attributeObjUserName = new JSONObject();
                    attributeObjUserName.put("name", AttachmentConstant.USERNAME);
                    attributeObjUserName.put("value", UserInfoPreferenceUtility.getUserName());
                    attributesJArray.put(attributeObjUserName);

                    JSONObject attributeObjUserRole = new JSONObject();
                    attributeObjUserRole.put("name", AttachmentConstant.USERROLE);
                    attributeObjUserRole.put("value", UserInfoPreferenceUtility.getRole());
                    attributesJArray.put(attributeObjUserRole);

                    JSONObject attributeObjName = new JSONObject();
                    attributeObjName.put("name", AttachmentConstant.NAME);
                    attributeObjName.put("value", attachment.getAttachmentName());
                    attributesJArray.put(attributeObjName);

                    JSONObject attributeObjSize = new JSONObject();
                    attributeObjSize.put("name", AttachmentConstant.SIZE);
                    attributeObjSize.put("value", fileSize);
                    attributesJArray.put(attributeObjSize);

                    JSONObject attributeObjLat = new JSONObject();
                    attributeObjLat.put("name", AttachmentConstant.LAT);
                    attributeObjLat.put("value", lat);
                    attributesJArray.put(attributeObjLat);

                    JSONObject attributeObjLong = new JSONObject();
                    attributeObjLong.put("name", AttachmentConstant.LNG);
                    attributeObjLong.put("value", lng);
                    attributesJArray.put(attributeObjLong);

                    JSONObject attributeObjZValue = new JSONObject();
                    attributeObjZValue.put("name", AttachmentConstant.ZVALUE);
                    attributeObjZValue.put("value", zValue);
                    attributesJArray.put(attributeObjZValue);

                    JSONObject attributeObjAccuracy = new JSONObject();
                    attributeObjAccuracy.put("name", AttachmentConstant.ACCURACY);
                    attributeObjAccuracy.put("value", accuracy);
                    attributesJArray.put(attributeObjAccuracy);

                    JSONObject attributeObjCreationDate = new JSONObject();
                    attributeObjCreationDate.put("name", AttachmentConstant.CREATIONDATE);
                    attributeObjCreationDate.put("value", attachment.getDateTimeStamp());
                    attributesJArray.put(attributeObjCreationDate);

                    JSONObject attributeObjSavedDate = new JSONObject();
                    attributeObjSavedDate.put("name", AttachmentConstant.SAVEDATE);
                    attributeObjSavedDate.put("value", attachment.getDateTimeStamp());
                    attributesJArray.put(attributeObjSavedDate);

                    JSONObject attributeObjContent = new JSONObject();
                    attributeObjContent.put("name", AttachmentConstant.CONTENT);
                    attributeObjContent.put("value", attachmentFile);
                    attributesJArray.put(attributeObjContent);

                    JSONObject attributeObjIsNew = new JSONObject();
                    attributeObjIsNew.put("name", AttachmentConstant.ISNEW);
                    attributeObjIsNew.put("value", AttachmentConstant.ADD_OP);
                    attributesJArray.put(attributeObjIsNew);

                    JSONObject attributeObjCaption = new JSONObject();
                    attributeObjCaption.put("name", AttachmentConstant.CAPTION);
                    attributeObjCaption.put("value", attachment.getLabel());
                    attributesJArray.put(attributeObjCaption);

                    dataObject.put("attributes", attributesJArray);
                    dataJsonArray.put(dataObject);
                }
            }

            if (dataJsonArray.length() > 0) {
                GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
                JSONObject datasetInfo = getAttachmentsDatasetInfo();
                JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray);

                if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                    ReveloLogger.info(className, "insertUpdateAttachmentRecord", "adding attachments succeeded");
                }
                else {
                    ReveloLogger.info(className, "insertUpdateAttachmentRecord", "adding attachment failed");
                }
            }
            else {
                ReveloLogger.info(className, "insertUpdateAttachmentRecord", "No new attachments to add");
            }

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.info(className, "insertUpdateAttachmentRecord", "adding attachment failed : exception " + e.getMessage());
        }
        ReveloLogger.info(className, "insertUpdateAttachmentRecord", "attachments added: " + attachmentCount);
        return attachmentCount;
    }

    public JSONObject updateDeliveryRecordAndMap(Map<String, Object> attributeValueMap, Context activity,
                                                 String w9Id, String label, List<Attachment> attachmentList,
                                                 Location location, String columnName, List<Attachment> getSelectForDeleteAttachmentFileList) {
        JSONObject resultJson = new JSONObject();
        String message = "Unable to edit feature.";
        try {
            resultJson.put("status", "failure");
            resultJson.put("message", message + " " + "Reason: Unknown");


            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesArray = new JSONArray();
            for (String attributeName : attributeValueMap.keySet()) {
                JSONObject attributesObject = new JSONObject();
                attributesObject.put("name", attributeName);
                if (attributeValueMap.get(attributeName) == null) {
                    attributeValueMap.put(attributeName, featureLayer.getPropertiesHashMap().get(attributeName).getDefaultValue());
                }
                attributesObject.put("value", attributeValueMap.get(attributeName));
                attributesArray.put(attributesObject);
            }
            dataObject.put("attributes", attributesArray);
            dataJsonArray.put(dataObject);

            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", columnName);
            Attribute attribute = featureLayer.getPropertiesHashMap().get(columnName);
            conditionJobj.put("valueDataType", "string");
            conditionJobj.put("value", w9Id);
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(activity), new ReveloLogger(), activity);
            JSONObject datasetInfo = getDatasetInfo(false);
            JSONObject respJObj = gpkgRWAgent.updateDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(activity), datasetInfo, dataJsonArray, whereClauseArray, "AND", activity);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                String layerName = featureLayer.getName();

                //delete attachment record.
                if (getSelectForDeleteAttachmentFileList != null && getSelectForDeleteAttachmentFileList.size() != 0) {
                    for (Attachment attachment : getSelectForDeleteAttachmentFileList) {
                        String attachmentName = attachment.getAttachmentName();
                        String w9IdAttachment = attachment.getW9Id();
                        boolean isDeleteAttachment = deleteUpdateAttachmentRecord(w9IdAttachment, attachmentName, layerName, activity);
                    }
                }

                int attachmentsAdded = - 1;
                if (attachmentList != null && attachmentList.size() != 0) {
                    attachmentsAdded = insertUpdateAttachmentRecord(attachmentList, w9Id, layerName, activity, location); //insert attachment record.
                }
                else {
                    attachmentsAdded = 0;
                }

         // insert add entry to editmetadata.


                message = label + " Record updated properly ";
                if (attachmentsAdded != - 1) {
                    message += " with " + attachmentsAdded + " attachments";
                }
                resultJson.put("status", "success");
                resultJson.put("message", message);

            }
            else {
                message += " " + "Reason: Unknown";
                if (respJObj.has("message") && ! respJObj.getString("message").isEmpty()) {
                    message = respJObj.getString("message");
                }
                ToastUtility.toast("Record updated not properly", activity, false);
                resultJson.put("status", "failure");
                resultJson.put("message", message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                resultJson.put("status", "failure");
                resultJson.put("message", message + " " + "Reason: exception - " + e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        return resultJson;
    }

    private boolean updateMetadataEntry(Context context, String layerName, String w9Id, String updatedFlowName, String updatedInteractionName) {

        try {

            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();


            JSONObject attributeObjInteractionName = new JSONObject();
            attributeObjInteractionName.put("name", MetadataTableConstant.INTERACTIONNAME);
            attributeObjInteractionName.put("value", updatedInteractionName);
            attributesJArray.put(attributeObjInteractionName);

            JSONObject attributeObjFlowName = new JSONObject();
            attributeObjFlowName.put("name", MetadataTableConstant.FLOWNAME);
            attributeObjFlowName.put("value", updatedFlowName);
            attributesJArray.put(attributeObjFlowName);


            dataObject.put("attributes", attributesJArray);
            dataJsonArray.put(dataObject);

            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", MetadataTableConstant.W9ID);
            conditionJobj.put("valueDataType", "string");
            conditionJobj.put("value", w9Id);
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getMetadataDatasetInfo();
            JSONObject respJObj = gpkgRWAgent.updateDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray, whereClauseArray, "AND", context);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                return true;
            }
            else {
                //submitnow = false because submit now makes metadata point to next node. we have already calculated and sent next node to insert fucntion.
                // now we dont want to push it further to grand child node.
                return insertMetadataEntry(context, layerName, w9Id, updatedFlowName, updatedInteractionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean insertMetadataEntry(Context context, String layerName, String w9Id, String nextFlowName, String nextInteractionName) {

        try {

            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();

            JSONObject attributeObjw9id = new JSONObject();
            attributeObjw9id.put("name", MetadataTableConstant.W9ID);
            attributeObjw9id.put("value", w9Id);
            attributesJArray.put(attributeObjw9id);

            JSONObject attributeObjInteractionName = new JSONObject();
            attributeObjInteractionName.put("name", MetadataTableConstant.INTERACTIONNAME);
            attributeObjInteractionName.put("value", nextInteractionName);
            attributesJArray.put(attributeObjInteractionName);

            JSONObject attributeObjFlowName = new JSONObject();
            attributeObjFlowName.put("name", MetadataTableConstant.FLOWNAME);
            attributeObjFlowName.put("value", nextFlowName);
            attributesJArray.put(attributeObjFlowName);


            dataObject.put("attributes", attributesJArray);
            dataJsonArray.put(dataObject);


            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getMetadataDatasetInfo();
            JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, dataJsonArray);

            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");


        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public JSONObject getMetadataEntry(Context context, String layerName, String w9Id) {
        JSONObject metadataJson = null;
        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();
            JSONObject conditionObj = new JSONObject();
            conditionObj.put("value", w9Id);
            conditionObj.put("operator", "=");
            conditionObj.put("columnType", "string");
            conditionMap.put(MetadataTableConstant.W9ID, conditionObj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            boolean queryGeometry = false;
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getMetadataDatasetInfo(), null, conditionMap, "", true, 1, queryGeometry);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            JSONObject propertiesObject = featureJObj.getJSONObject("properties");
                            metadataJson = new JSONObject();
                            metadataJson.put("w9Id", propertiesObject.getString(MetadataTableConstant.W9ID));
                            metadataJson.put(CMUtils.OperationPermissionVariables.currentInteractionName, propertiesObject.getString(MetadataTableConstant.INTERACTIONNAME));
                            metadataJson.put(CMUtils.OperationPermissionVariables.currentFlowName, propertiesObject.getString(MetadataTableConstant.FLOWNAME));
                            //metadataJson.put(CMUtils.OperationPermissionVariables.currentInteractionRole, propertiesObject.getString(MetadataTableConstant.));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return metadataJson;
    }


//------EDITMETADATA--------------------------------------------------------------------------------------------------------------------------------------------------


    //--------ATTACHMENTS------------------------------------------------------------------------------------------------------------------------------------------
    /*public AttributesDao getAttachmentAttributesDao(Context context, String layerName) {
        if (attachmentAttributesDao == null) {
            GeoPackage dataGeoPackage = GeoPackageManagerAgent.getDataGeoPackage(context, DbRelatedConstants.getPropertiesJsonForDataGpkg(context));
            String attachmentTableName = layerName +"_"+UserInfoPreferenceUtility.getSurveyName()+ "_" + "attachments";
            attachmentAttributesDao = dataGeoPackage.getAttributesDao(attachmentTableName);
        }
        return attachmentAttributesDao;
    }*/

    //used only by trails
    public void updateRecordInDb(Map<String, Object> attributeValueMap, JSONObject geometry, Context activity, String w9Id, List<Attachment> attachmentList, Location location, String columnName, List<Attachment> getSelectForDeleteAttachmentFileList) {
        try {
            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesArray = new JSONArray();
            for (String attributeName : attributeValueMap.keySet()) {
                JSONObject attributesObject = new JSONObject();
                attributesObject.put("name", attributeName);
                if (attributeValueMap.get(attributeName) == null) {
                    attributeValueMap.put(attributeName, featureLayer.getPropertiesHashMap().get(attributeName).getDefaultValue());
                }
                attributesObject.put("value", attributeValueMap.get(attributeName));
                attributesArray.put(attributesObject);
            }
            dataObject.put("attributes", attributesArray);
            if (geometry != null) {
                dataObject.put("geometry", geometry);
            }
            dataJsonArray.put(dataObject);

            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", columnName);
            Attribute attribute = featureLayer.getPropertiesHashMap().get(columnName);
            conditionJobj.put("valueDataType", attribute.getType());
            conditionJobj.put("value", w9Id);
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(activity), new ReveloLogger(), activity);
            JSONObject datasetInfo = getDatasetInfo(false);
            datasetInfo.put("w9IdPropertyName", featureLayer.getW9IdProperty());
            JSONObject respJObj = gpkgRWAgent.updateDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(activity), datasetInfo, dataJsonArray, whereClauseArray, "AND", activity);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                String layerName = featureLayer.getName();
                //delete attachment record.
                if (getSelectForDeleteAttachmentFileList != null && getSelectForDeleteAttachmentFileList.size() != 0) {
                    for (Attachment attachment : getSelectForDeleteAttachmentFileList) {
                        String attachmentName = attachment.getAttachmentName();
                        String w9IdAttachment = attachment.getW9Id();
                        boolean isDeleteAttachment = deleteUpdateAttachmentRecord(w9IdAttachment, attachmentName, layerName, activity);
                    }
                }

                int attachmentsAdded = - 1;
                if (attachmentList != null && attachmentList.size() != 0) {
                    attachmentsAdded = insertUpdateAttachmentRecord(attachmentList, w9Id, layerName, activity, location); //insert attachment record.
                }
                else {
                    attachmentsAdded = 0;
                }

                EditMetaDataTable.insertAddRecord(w9Id, w9Id, activity, featureLayer.getName());  // insert add entry to editmetadata.

                String message = w9Id + " Record updated properly ";
                if (attachmentsAdded > 0) {
                    message += " with " + attachmentsAdded + " attachments";
                }

            }
            else {
                ToastUtility.toast("Record updated not properly", activity, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean updateAttachmentEntryIsNew(String layerName, Context context, String w9id, String attachName, int newIsNewValue) {

        boolean isUpdate = false;
        try {

            /*String query = "Update " + featureLayer.getName() + "_" + UserInfoPreferenceUtility
                    .getSurveyName() + "_attachments" + " set " + AttachmentConstant.ISNEW + " = " + newIsNewValue + " where ";
            String whereClause = AttachmentConstant.W9ID + " = '" + w9id + "' AND " + AttachmentConstant.NAME + " = '" + attachName + "';";
            String queryfull = query + whereClause;
            JSONObject jsonObject = runQuery(context, AppFolderStructure.getDataGeoPackage(context).getAbsolutePath(), queryfull, new Callback() {
                @Override
                public void columns(String[] coldata) {
                    Log.i("atttt", Arrays.toString(coldata));
                }

                @Override
                public void types(String[] types) {
                    Log.i("atttt", Arrays.toString(types));
                }

                @Override
                public boolean newrow(String[] rowdata) {
                    Log.i("atttt", Arrays.toString(rowdata));
                    return false;
                }
            });
            return jsonObject.has("status") && jsonObject.getString("status").equalsIgnoreCase("success");
*/


            JSONArray dataJsonArray = new JSONArray();

            JSONObject dataObject = new JSONObject();
            JSONArray attributesArray = new JSONArray();

            JSONObject attributesObject = new JSONObject();
            attributesObject.put("name", AttachmentConstant.ISNEW);
            attributesObject.put("value", newIsNewValue);
            attributesArray.put(attributesObject);

            dataObject.put("attributes", attributesArray);
            dataJsonArray.put(dataObject);

            JSONArray whereClauseArray = new JSONArray();

            if(w9id!=null) {
                JSONObject conditionw9idJobj = new JSONObject();
                conditionw9idJobj.put("conditionType", "attribute");
                conditionw9idJobj.put("columnName", AttachmentConstant.W9ID);
                conditionw9idJobj.put("valueDataType", "string");
                conditionw9idJobj.put("value", w9id);
                conditionw9idJobj.put("operator", "=");
                whereClauseArray.put(conditionw9idJobj);
            }

            if(attachName!=null) {
                JSONObject conditionAttachNameJobj = new JSONObject();
                conditionAttachNameJobj.put("conditionType", "attribute");
                conditionAttachNameJobj.put("columnName", AttachmentConstant.NAME);
                conditionAttachNameJobj.put("valueDataType", "string");
                conditionAttachNameJobj.put("value", attachName);
                conditionAttachNameJobj.put("operator", "=");
                whereClauseArray.put(conditionAttachNameJobj);
            }
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getAttachmentsDatasetInfo();
            JSONObject respJObj = gpkgRWAgent.updateDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context),
                    datasetInfo, dataJsonArray, whereClauseArray, "AND", context);

            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return isUpdate;
    }

    //------DELETE----------------------------------------------------------------------------------------------------------------------------------------
    //used only by trails
    public boolean deleteFeatureRecordOnly(String w9Id, String columnName, Context context) {
        try {
            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", columnName);
            Attribute attribute = featureLayer.getPropertiesHashMap().get(columnName);
            conditionJobj.put("valueDataType", attribute.getType());
            conditionJobj.put("value", w9Id);
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getDatasetInfo(false);
            datasetInfo.put("w9IdPropertyName", featureLayer.getW9IdProperty());
            JSONObject respJObj = gpkgRWAgent.deleteDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, whereClauseArray, "AND", context);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                return true;
            }
            else {
                //ToastUtility.toast("Record not deleted !", context, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRecord(String w9Id, String label, String columnName, String w9Metadata, Context context, JSONObject geometryJson, JSONObject permissionJson, BottomSheetDialog fragmentBottomSheet, boolean deleteFromShadowTable) {
        ReveloLogger.info(className, "deleteRecord", "deleting record for " + w9Id + "(" + label + "). deleting from shadow table?" + deleteFromShadowTable);
        try {
            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", columnName);
            Attribute attribute = featureLayer.getPropertiesHashMap().get(columnName);
            conditionJobj.put("valueDataType", attribute.getType());
            conditionJobj.put("value", w9Id);
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getDatasetInfo(false);
            if (deleteFromShadowTable) {
                datasetInfo = getDatasetInfo(true);
            }
            datasetInfo.put("w9IdPropertyName", featureLayer.getW9IdProperty());
            JSONObject respJObj = gpkgRWAgent.deleteDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, whereClauseArray, "AND", context);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                ReveloLogger.info(className, "deleteRecord", "delete query successful..checking for flows to delete from metadata");
                boolean isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
                boolean procceedFuther = false;
                if (deleteFromShadowTable) {
                    ReveloLogger.info(className, "deleteRecord", "since we deleted from shadow table, aborting search for metadata and attachments");
                    procceedFuther = false;
                    return true;
                }
                else {
                    if (isFlowsApplicable) {
                        ReveloLogger.info(className, "deleteRecord", "deleting metadata entry for " + w9Id + " interaction name: " + permissionJson.getString("currentInteractionName"));
                        boolean isSuccessfullyAdded = deleteMetadataEntry(context, featureLayer.getName(), w9Id, permissionJson.getString("currentFlowName"), permissionJson.getString("currentInteractionName"));
                        if (isSuccessfullyAdded) {
                            procceedFuther = true;
                        }
                    }
                    else {
                        procceedFuther = true;
                    }
                }

                if (procceedFuther) {
                    if (w9Metadata == null || w9Metadata.isEmpty()) {
                        ReveloLogger.info(className, "deleteRecord", "received null or empty w9metadta. reconstructing.. we need to enter it in editmetadata table");
                        w9Metadata = getw9MetadataJsonStructure().toString();
                    }
                    ReveloLogger.info(className, "deleteRecord", "moving to insert a delete feature entry in editmetadata table");
                    boolean isDeleteFromMetadata = EditMetaDataTable.insertDeleteEntry(featureLayer.getName(), w9Id, label, w9Metadata, context); //delete entry from editmetadata.
                    //insert entry in metadata also.
                    ReveloLogger.info(className, "deleteRecord", "moving to delete attachments if any..");
                    boolean isDeleteAttachment = deleteAttachment(featureLayer.getName(), context, w9Id, null, AttachmentConstant.ALL_ATTACHMENTS); //delete entry from attachments table.

                    String message = "Feature " + label + " deleted successfully.";
                    if (isDeleteFromMetadata && isDeleteAttachment) {
                        message = label + " was deleted successfully.";
                        try {
                            //if (geometryJson != null)
                            ReveloLogger.info(className, "deleteRecord", "moving to send respose back to activity");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                    else {
                        ReveloLogger.info(className, "deleteRecord", "inserted delete enrty?" + isDeleteFromMetadata + "... deleted attachments?" + isDeleteAttachment + ".. delete feature failed..");
                        message = "Feature " + label + " could not be deleted successfully.";
                        ToastUtility.toast(message, context, false);
                    }


                }
                if (fragmentBottomSheet != null) {
                    ReveloLogger.info(className, "deleteRecord", "dismissing bottomm fragment");
                    try {
                        fragmentBottomSheet.dismiss();
                    } catch (Exception e) {
                        ReveloLogger.error(className, "deleteRecord", "Error closing  message sheet " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            else {
                ReveloLogger.error(className, "deleteRecord", "delete feature failed..");
                if (! deleteFromShadowTable) {
                    ToastUtility.toast("Record not deleted !", context, false);
                }
            }

        } catch (Exception e) {
            ReveloLogger.error(className, "deleteRecord", "Exception received while deleting feature .." + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private boolean deleteMetadataEntry(Context context, String layerName, String w9Id, String currentFlowName, String currentInteractionName) {
        try {
            JSONArray whereClauseArray = new JSONArray();


            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", MetadataTableConstant.W9ID);
            conditionJobj.put("valueDataType", "string");
            conditionJobj.put("value", w9Id);
            conditionJobj.put("operator", "=");
            whereClauseArray.put(conditionJobj);

            if (currentFlowName != null && ! currentFlowName.isEmpty()) {
                JSONObject conditionJobjFlowName = new JSONObject();
                conditionJobjFlowName.put("conditionType", "attribute");
                conditionJobjFlowName.put("columnName", MetadataTableConstant.FLOWNAME);
                conditionJobjFlowName.put("valueDataType", "string");
                conditionJobjFlowName.put("value", currentFlowName);
                conditionJobjFlowName.put("operator", "=");
                whereClauseArray.put(conditionJobjFlowName);
            }

            if (currentInteractionName != null && ! currentInteractionName.isEmpty()) {
                JSONObject conditionJobjInteractionName = new JSONObject();
                conditionJobjInteractionName.put("conditionType", "attribute");
                conditionJobjInteractionName.put("columnName", MetadataTableConstant.INTERACTIONNAME);
                conditionJobjInteractionName.put("valueDataType", "string");
                conditionJobjInteractionName.put("value", currentInteractionName);
                conditionJobjInteractionName.put("operator", "=");
                whereClauseArray.put(conditionJobjInteractionName);
            }


            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getMetadataDatasetInfo();
            JSONObject respJObj = gpkgRWAgent.deleteDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, whereClauseArray, "AND", context);
            return respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteAttachment(String layerName, Context context, Object w9id, String attachName, int isNewValue) {

        boolean isDelete = false;

        try {
            JSONArray whereClauseArray = new JSONArray();

            if (w9id != null) {
                JSONObject conditionJobjW9id = new JSONObject();
                conditionJobjW9id.put("conditionType", "attribute");
                conditionJobjW9id.put("columnName", AttachmentConstant.W9ID);
                conditionJobjW9id.put("valueDataType", "string");
                conditionJobjW9id.put("value", w9id);
                conditionJobjW9id.put("operator", "=");
                whereClauseArray.put(conditionJobjW9id);

                if (attachName != null) {
                    JSONObject conditionJobjAttachName = new JSONObject();
                    conditionJobjAttachName.put("conditionType", "attribute");
                    conditionJobjAttachName.put("columnName", AttachmentConstant.NAME);
                    conditionJobjAttachName.put("valueDataType", "string");
                    conditionJobjAttachName.put("value", attachName);
                    conditionJobjAttachName.put("operator", "=");
                    whereClauseArray.put(conditionJobjAttachName);
                }

                if (isNewValue == AttachmentConstant.ADD_OP || isNewValue == AttachmentConstant.DELETE_OP || isNewValue == AttachmentConstant.NONE_OP) {
                    JSONObject conditionJobjisNew = new JSONObject();
                    conditionJobjisNew.put("conditionType", "attribute");
                    conditionJobjisNew.put("columnName", AttachmentConstant.ISNEW);
                    conditionJobjisNew.put("valueDataType", "integer");
                    conditionJobjisNew.put("value", isNewValue);
                    conditionJobjisNew.put("operator", "=");
                    whereClauseArray.put(conditionJobjisNew);
                }

                GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
                JSONObject datasetInfo = getAttachmentsDatasetInfo();
                JSONObject respJObj = gpkgRWAgent.deleteDatasetContent(DbRelatedConstants.getDataSourceInfoForDataGpkg(context), datasetInfo, whereClauseArray, "AND", context);
                isDelete = respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return isDelete;
    }

    //------UPLOAD--------------------------------------------------------------------------------------------------------------------------------------------
    public JSONObject createUploadJsonObject(Object id, String surveyName, Context context, String operationName) {

        JSONObject featureObject = null;
        try {
            String idPropertyName = featureLayer.getW9IdProperty();
            Feature addedFeature = getFeature(idPropertyName, id, context, true, false, false);


            if (addedFeature != null) {

                featureObject = createAttributeJson(addedFeature, featureLayer,operationName);

                try {
                    featureObject.put(AppConstants.COORDINATES, addedFeature.getGeoJsonGeometry().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            try {
                if (featureObject != null) {
                    featureObject.put(AppConstants._ID, surveyName + "_" + id);
                    featureObject.put(AppConstants._TYPE, AppConstants.VERTEX);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return featureObject;
    }

    public Feature getFeature(String columnName, Object columnValue, Context context, boolean searchInMainTable, boolean searchInShadowTable, boolean transformGeometry) {

        Feature feature = null;
        boolean queryGeometry = false;
        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();
            JSONObject conditionObj = new JSONObject();
            conditionObj.put("value", columnValue);
            conditionObj.put("operator", "=");
            conditionObj.put("columnType", featureLayer.getPropertiesHashMap().get(columnName).getType());
            conditionMap.put(columnName, conditionObj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            if (searchInMainTable) {
                if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                    queryGeometry = true;
                }
                JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), null, conditionMap, "", true, 1, queryGeometry, transformGeometry);

                if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                    if (respJObj.has("features")) {
                        JSONObject responseFeatures = respJObj.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject featureJObj = featuresJArray.getJSONObject(i);
                                feature = constructFeatureFromJson(featureJObj);
                            }
                        }
                    }

                }
            }

            if (searchInShadowTable && feature == null) {
                queryGeometry = false;
                JSONObject respJObjShadow = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(true), null, conditionMap, "", true, 1, queryGeometry);

                if (respJObjShadow.has("status") && respJObjShadow.getString("status").equalsIgnoreCase("success")) {

                    if (respJObjShadow.has("features")) {
                        JSONObject responseFeatures = respJObjShadow.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject featureJObj = featuresJArray.getJSONObject(i);
                                feature = constructFeatureFromJson(featureJObj);
                            }
                        }
                    }

                }
            }
            if (feature == null) {
                ReveloLogger.error(className, "getFeature", "feature construction failed, reason - null value recceived");
                if (searchInShadowTable) {
                    ReveloLogger.error(className, "getFeature", "searchInShadowTable =" + searchInShadowTable);
                }
                if (searchInMainTable) {
                    ReveloLogger.error(className, "getFeature", "searchInMainTable =" + searchInMainTable);
                }
            }
            else {
                ReveloLogger.info(className, "getFeature", "returning feature " + feature.getFeatureId() + " " + feature.getFeatureLabel());
            }

        } catch (java.lang.Exception e) {
            ReveloLogger.error(className, "getFeature", "feature construction failed, reason -Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return feature;
    }

    private JSONObject createAttributeJson(Feature addedFeature, FeatureLayer featureLayer, String operationName) {

        JSONObject featureObject = new JSONObject();

        try {
            Map<String, Object> attributeMap = addedFeature.getAttributes();
            List<Attribute> attributeList = featureLayer.getProperties();

            for (Attribute attribute : attributeList) {

                String attributeName = attribute.getName();

                if (attributeMap.containsKey(attributeName)) {
                    String attributeType = attribute.getType();
                    Object defaultValue = attribute.getDefaultValue();
                    Object value = attributeMap.get(attributeName);

                    if (value == null) {
                        value = defaultValue;
                    }

                    try {
                        try {
                            if (attributeType.equalsIgnoreCase("date")) {
                                if(operationName.equalsIgnoreCase(AppConstants.ADD)){
                                    String dateString = DatePickerMethods.convertToValidDateString(String.valueOf(value), false);
                                    Date d = DatePickerMethods.convertStringToDate2(dateString);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                                    value = sdf.format(d);
                                }else{
                                    String dateString = DatePickerMethods.convertToValidDateString(String.valueOf(value), false);
                                    Date d = DatePickerMethods.convertStringToDate2(dateString);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                                    value = sdf.format(d);
                                }
                                Log.i("EEEE", "upload, date - " + featureLayer.getName() + " - " + addedFeature.getFeatureId() + " - " + value);
                            }
                            else if (attributeType.equalsIgnoreCase("boolean")) {
                               if(value instanceof Integer){
                                   Integer intval = (Integer)value;
                                   if(intval==0){
                                       value = false;
                                   }else if(intval==1){
                                       value = true;
                                   }else {
                                       value=defaultValue;
                                   }
                               }else if (value instanceof  Boolean){
                                   Boolean boolval = (Boolean) value;
                                   if(boolval){
                                       value = true;
                                   }else  {
                                       value=false;
                                   }
                               }else if(value instanceof String){
                                   String strval = (String) value;
                                   if(strval.equalsIgnoreCase("true")||strval.equalsIgnoreCase("yes")){
                                       value = true;
                                   }else if(strval.equalsIgnoreCase("false")||strval.equalsIgnoreCase("no")){
                                       value = false;
                                   }else {
                                       value=String.valueOf(defaultValue);
                                   }
                               }else {
                                   value = String.valueOf(value);
                               }
                                Log.i("EEEE", "upload, boolean value - " + featureLayer.getName() + " - " + addedFeature.getFeatureId() + " - " + value);
                            }
                            else if (attributeType.equalsIgnoreCase("timestamp")) {
                                if(operationName.equalsIgnoreCase(AppConstants.ADD)){
                                    String dateString = DatePickerMethods.convertToValidDateString(String.valueOf(value), false);
                                    Date d = DatePickerMethods.convertStringToDate2(dateString);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                                    value = sdf.format(d);
                                    Log.i("EEEE", "upload, timestamp - " + featureLayer.getName() + " - " + addedFeature.getFeatureId() + " - " + value);
                                }else{
                                    String dateString = DatePickerMethods.convertToValidDateString(String.valueOf(value), false);
                                    Date d = DatePickerMethods.convertStringToDate2(dateString);
                                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                                    value = sdf.format(d);
                                    Log.i("EEEE", "upload, timestamp - " + featureLayer.getName() + " - " + addedFeature.getFeatureId() + " - " + value);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            value = defaultValue;
                            Log.e("EEEE", "upload, exception saving date, taking default val - " + featureLayer.getName() + " - " + addedFeature.getFeatureId() + " - " + value);
                        }
                        featureObject.put(attributeName, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (! featureObject.has(AppConstants.W9_METADATA)) {

                if (attributeMap.containsKey(AppConstants.W9_METADATA)) {

                    Object w9MetaDataString = attributeMap.get(AppConstants.W9_METADATA);
                    try {
                        featureObject.put(AppConstants.W9_METADATA, w9MetaDataString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return featureObject;
    }

    public Map<Object, List<Attachment>> getAllAttachmentsMap(int operation, Context context) {
        ReveloLogger.debug(className, "doInBackground-getAllAttachmentsMap", "getting  attachments for operation " + "" + operation + "(Add(" + AttachmentConstant.ADD_OP + "),deleted(" + AttachmentConstant.DELETE_OP + "),none(" + AttachmentConstant.NONE_OP + "),all(" + AttachmentConstant.ALL_ATTACHMENTS + "))");
        Map<Object, List<Attachment>> attachmentsMap = new HashMap<>();

        try {
            List<Attachment> allAttachmentsList = getAttachmentsRecord(null, operation, context);
            if (allAttachmentsList != null && allAttachmentsList.size() > 0) {
                ReveloLogger.debug(className, "doInBackground-getAllAttachmentsMap", "getting  attachments - " + allAttachmentsList.size() + " attachments found");
                for (Attachment attachment : allAttachmentsList) {
                    String w9Id = attachment.getW9Id();
                    if (attachmentsMap.containsKey(w9Id)) {
                        List<Attachment> attachmentList = attachmentsMap.get(w9Id);
                        if (attachmentList == null) {
                            attachmentList = new ArrayList<>();
                            attachmentsMap.put(w9Id, attachmentList);
                        }
                        attachmentList.add(attachment);
                    }
                    else {
                        List<Attachment> attachmentList = new ArrayList<>();
                        attachmentList.add(attachment);
                        attachmentsMap.put(w9Id, attachmentList);
                    }
                }
            }
            else {
                ReveloLogger.debug(className, "doInBackground-getAllAttachmentsMap", "getting  attachments - no attachments found");
            }


        } catch (Exception e) {
            ReveloLogger.error(className, "doInBackground-getAllAttachmentsMap", "getting  attachments - Exception occurred -  aborting. Exception- " + e.getMessage());
            e.printStackTrace();
        }

        return attachmentsMap;
    }

    public List<Attachment> getAttachmentsRecord(String w9id, int isNewValue, Context context) {
        ReveloLogger.debug(className, "getAttachmentsRecord", "getting attachments - for " + w9id + " of layer " + featureLayer.getName() + " isnew? " + isNewValue);
        List<Attachment> attachmentsList = new ArrayList<>();

        try {
//            HashMap<String, JSONObject> conditionMapAnd = new HashMap<>();
//            HashMap<String, JSONObject> conditionMapOr = new HashMap<>();

            JSONArray ANDConditions = new JSONArray();
            JSONArray ORConditions = new JSONArray();
            String AND_OR = "";
            if (w9id != null) {
                JSONObject conditionJobj = new JSONObject();
                conditionJobj.put("conditionType", "attribute");
                conditionJobj.put("columnName", AttachmentConstant.W9ID);
                conditionJobj.put("valueDataType", "string");
                conditionJobj.put("value", w9id);
                conditionJobj.put("operator", "=");
                ANDConditions.put(conditionJobj);

            }
            else {
                ReveloLogger.debug(className, "getAttachmentsRecord", "getting all attachments - of layer " + featureLayer.getName() + " isnew? " + isNewValue);
            }
            if (isNewValue == AttachmentConstant.ADD_OP || isNewValue == AttachmentConstant.DELETE_OP || isNewValue == AttachmentConstant.NONE_OP) {
                JSONObject conditionJobjisNew = new JSONObject();
                conditionJobjisNew.put("conditionType", "attribute");
                conditionJobjisNew.put("columnName", AttachmentConstant.ISNEW);
                conditionJobjisNew.put("valueDataType", "string");
                conditionJobjisNew.put("value", isNewValue);
                conditionJobjisNew.put("operator", "=");
                ORConditions.put(conditionJobjisNew);
            }
            else if (isNewValue == AttachmentConstant.ALL_VALID_ATTACHMENT) {

                JSONObject addOp = new JSONObject();
                addOp.put("conditionType", "attribute");
                addOp.put("columnName", AttachmentConstant.ISNEW);
                addOp.put("valueDataType", "string");
                addOp.put("value", AttachmentConstant.ADD_OP);
                addOp.put("operator", "=");
                ORConditions.put(addOp);

                JSONObject noneOp = new JSONObject();
                noneOp.put("conditionType", "attribute");
                noneOp.put("columnName", AttachmentConstant.ISNEW);
                noneOp.put("valueDataType", "string");
                noneOp.put("value", AttachmentConstant.NONE_OP);
                noneOp.put("operator", "=");
                ORConditions.put(noneOp);

                AND_OR = "AND";
            }

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            boolean queryGeometry = false;
//            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context),
//                    getAttachmentsDatasetInfo(), null, conditionMap, AND_OR, true, -1, queryGeometry);

            JSONObject respJObj = gpkgRWAgent.getDatasetContentNEW(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getAttachmentsDatasetInfo(), null, ORConditions, ANDConditions, AND_OR, true, - 1, - 1, queryGeometry, false);


            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        ReveloLogger.debug(className, "getAttachmentsRecord", "getting attachments - " + featuresJArray.length() + " attachment(s) found ");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            Attachment attachment = constructAttachmentFromJson(featureJObj, context);
                            if (attachment != null)
                                attachmentsList.add(attachment);
                        }
                    }
                }

            }
            else {
                ReveloLogger.debug(className, "getAttachmentsRecord", "getting attachments - no attachment found ");
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "getAttachmentsRecord", "Exception while getting attachments - for " + w9id + " of layer " + featureLayer.getName() + " isnew? " + isNewValue);
            ReveloLogger.error(className, "getAttachmentsRecord", "Exception - " + e.getMessage());
            e.printStackTrace();
        }
        return attachmentsList;
    }

    private Attachment constructAttachmentFromJson(JSONObject attachmentJObj, Context context) {
        ReveloLogger.debug(className, "constructAttachmentFromJson", "constructing attachment object ");
        Attachment attachment = null;
        try {
            JSONObject propertiesJobjReceived = attachmentJObj.getJSONObject("properties");
            Iterator<String> itrPropKey = propertiesJobjReceived.keys();
            attachment = new Attachment();

            String attachmentFolder = "", attachmentName = "";
            byte[] attachmentContent = null;
            int isNew = - 100;


            while (itrPropKey.hasNext()) {
                String property = itrPropKey.next();
                if (AttachmentConstant.W9ID.equals(property)) {
                    attachment.setW9Id(propertiesJobjReceived.getString(AttachmentConstant.W9ID));
                }
                else if (AttachmentConstant.NAME.equals(property)) {
                    attachmentName = propertiesJobjReceived.getString(AttachmentConstant.NAME);
                    attachment.setAttachmentName(attachmentName);
                }
                else if (AttachmentConstant.MIMETYPE.equals(property)) {
                    String mimetype = propertiesJobjReceived.getString(AttachmentConstant.MIMETYPE);
                    attachment.setContentType(propertiesJobjReceived.getString(AttachmentConstant.MIMETYPE));

                    if (mimetype.equalsIgnoreCase(AppConstants.imageType)) {
                        attachmentFolder = AppConstants.photo;

                    }
                    else if (mimetype.equalsIgnoreCase(AppConstants.videoType)) {
                        attachmentFolder = AppConstants.video;

                    }
                    else if (mimetype.equalsIgnoreCase(AppConstants.audioType)) {
                        attachmentFolder = AppConstants.audio;
                    }
                }
                else if (AttachmentConstant.CONTENT.equals(property)) {
                    Object attachmentObj = propertiesJobjReceived.get(AttachmentConstant.CONTENT);

                    if (attachmentObj != null) {
                        attachmentContent = (byte[]) attachmentObj;
                    }
                }
                else if (AttachmentConstant.ISNEW.equals(property)) {
                    isNew = propertiesJobjReceived.getInt(AttachmentConstant.ISNEW);
                    attachment.setIsNew(isNew);
                }

            }
            if (isNew != - 100 && ! attachmentFolder.isEmpty() && attachmentContent != null && attachmentName != null && ! attachmentName.isEmpty()) {
                if (isNew != AttachmentConstant.DELETE_OP) {
                    File attachmentFile = AppFolderStructure.createAttachmentFile(context, attachmentName, attachmentFolder);
                    AppMethods.createFileFromByte(attachmentContent, attachmentFile);
                    attachment.setFormType(AppConstants.ADD);
                    attachment.setFile(attachmentFile);
                }
            }
            if (attachment == null) {
                ReveloLogger.error(className, "constructAttachmentFromJson", "could not construct attachment object - reason unknown");
            }
            else {
                ReveloLogger.debug(className, "constructAttachmentFromJson", "constructing attachment object successful. returning object");
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "constructAttachmentFromJson", "Exception occurred while constructing attachment object - " + e.getMessage());
            e.printStackTrace();
        }
        return attachment;
    }

    //------SPATIALITE--------------------------------------------------------------------------------------------------------------------------------------------
    public JSONObject runQuery(Context context, String query, Callback callback) throws Exception {

        JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("status", "failure");
            String errorMsg = "unknown error";
            Database gdb = null;
            try {

                gdb = openGdb(context);
                if (gdb != null) {
                    gdb.exec("BEGIN;", null);
                    gdb.exec(query, callback);
                    gdb.exec("COMMIT;", null);

                    jsonObject.put("status", "success");
                }
                else {
                    jsonObject.put("status", "failure");
                    jsonObject.put("message", "unable to open database");
                }
            } catch (Exception e) {
                errorMsg = e.getMessage();
                e.printStackTrace();
                ReveloLogger.error(className, "Run Query", e.getMessage());

            } finally {
                if (gdb != null)
                    gdb.close();
            }

            jsonObject.put("message", errorMsg);

        } catch (java.lang.Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "Run Query", e.getMessage());
        }
        return jsonObject;
    }

    public Database openGdb(Context context) {
        try {

            int mode = Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE;
            Database gdb = new Database();
            gdb.open(AppFolderStructure.getDataGeoPackage(context).getAbsolutePath(), mode);
            return gdb;

        } catch (Exception e) {
            ReveloLogger.error(className, "Open Gdb", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void getfeaturesFrwmSpatialQuery(Context context, String query) {
        /*Callback callback = new Callback() {
            @Override
            public void columns(String[] coldata) {
                Log.i("eesha", "coldata "+coldata.length);
            }

            @Override
            public void types(String[] types) {
                Log.i("eesha", "types "+types.length);
            }

            @Override
            public boolean newrow(String[] rowdata) {
                Log.i("eesha", "rowdata "+rowdata.length);
                return false;
            }
        };
        try {
            runQuery(query, callback);
        }catch (Exception e){e.printStackTrace();}*/

        Database gdb = openGdb(context);
        try {
            Stmt stmt = gdb.prepare(query);

            while (stmt.step()) {
                Log.i("eesha", "" + stmt.column(0));
            }
            gdb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<Feature> getFeaturesIntersectingGeometry(Context context, String geometry) {

        List<Feature> gdbFeatureList = null;
        String geometryColumnName = "the_geom";

        String whereClause;
        String queryFeatures = "select * from " + featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + " where ";
        String queryGeometry = "select " + featureLayer.getW9IdProperty() + ",(" + geometryColumnName + ") from " + featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + " where ";

       /* if (getGeometryType().equalsIgnoreCase("polygon") || getGeometryType().equalsIgnoreCase("multipolygon")) {
            whereClause = "intersects(" + getName() + "." + AppController.geometryKeyName + ", " + "GeomFromText('" + geometry + "',4326)" + ");";
        } else if (getGeometryType().equalsIgnoreCase("linestring") || getGeometryType().equalsIgnoreCase("multilinestring")) {

            whereClause = " where " + "overlaps"
                    + "(" + getName() + "." + AppController.geometryKeyName + ","
                    + "GeomFromText('" + geometry + "',4326)"
                    + ")" +
                    ";";
        } else if (getGeometryType().equalsIgnoreCase("point")) {
            whereClause = "contains "
                    + "("
                    + "buffer"
                    + "("
                    + "GeomFromText('" + geometry + "',4326)"
                    + ",0.00001"
                    + ")"
                    + ","
                    + getName() + "." + AppController.geometryKeyName + ""
                    + ")" +
                    ";";
        } else {
            whereClause = "contains(" + getName() + "." + AppController.geometryKeyName + ", " + "GeomFromText('" + geometry + "',4326)" + ");";
        }*/

        String geomType = getGeometryType();

        if (geomType.equalsIgnoreCase("polygon") || geomType.equalsIgnoreCase("multipolygon")) {

            String value = featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + "." + geometryColumnName + ", " + "GeomFromText('" + geometry + "',4326)";

            whereClause = "within(" + value + ") || contains(" + value + ");";

        }
        else if (geomType.equalsIgnoreCase("linestring") || geomType.equalsIgnoreCase("multilinestring") || geomType.equalsIgnoreCase("polyline")) {

            whereClause = "intersects" + "(" + featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + "." + geometryColumnName + ","
                   /* + "buffer"
                    + "("*/ + "GeomFromText('" + geometry + "',4326)"
                  /*  + ",0.00004"
                    + ")"*/ + ")" + ";";
        }
        else if (geomType.equalsIgnoreCase("point")) {
            whereClause = "contains " + "("
                    /*+ "buffer"
                    + "("*/ + "GeomFromText('" + geometry + "',4326)"
                    /*+ ",0.00001"
                    + ")"*/ + "," + featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + "." + geometryColumnName + "" + ")" + ";";
        }
        else {
            whereClause = "contains(" + featureLayer.getName() + "_" + UserInfoPreferenceUtility.getSurveyName() + "." + geometryColumnName + ", " + "GeomFromText('" + geometry + "',4326)" + ");";
        }

        queryFeatures += whereClause;
        queryGeometry += whereClause;


        try {
            if (featureLayer.getType().equalsIgnoreCase("spatial")) {
                gdbFeatureList = constructGdbFeatures(getAllGdbFeaturesWithoutGeometry(context, queryFeatures), getIdGeometryMap(context, queryGeometry));
            }
            else {
                gdbFeatureList = getAllGdbFeaturesWithoutGeometry(context, queryFeatures);
            }


            JSONArray ORConditions = new JSONArray();

            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "spatial");
            conditionJobj.put("columnName", "the_geom");
            conditionJobj.put("valueDataType", "wkt");
            conditionJobj.put("value", geometry);
            conditionJobj.put("operator", "intersects");

            ORConditions.put(conditionJobj);


            gdbFeatureList = getFeaturesByQuery(context, null, ORConditions, null, "OR", true, false, true, 0, - 1, true, false);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return gdbFeatureList;
    }

    public String getGeometryType() {
        try {
            return featureLayer.getGeometryType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<Feature> constructGdbFeatures(List<Feature> gdbFeatureList, Map<Object, com.vividsolutions.jts.geom.Geometry> idGeometryMap) {
        for (Feature gdbFeature : gdbFeatureList) {
            if (idGeometryMap.containsKey(gdbFeature.getFeatureId())) {
                com.vividsolutions.jts.geom.Geometry jtsGeom = idGeometryMap.get(gdbFeature.getFeatureId());
                gdbFeature.setGeoJsonGeometry(GeoPackageUtils.convertGeometryToGeoJson(GeometryEngine.convertJTStoNGAGeom(jtsGeom)));
            }
        }
        return gdbFeatureList;
    }

    public List<Feature> getAllGdbFeaturesWithoutGeometry(Context context, String query) throws Exception {
        final List<Feature> gdbFeatureList = new ArrayList<>();
        final List<String> propertiesKeys = new ArrayList<>();
        Log.i("eesha", "query features " + query);
        try {
            Callback callback = new Callback() {
                @Override
                public void columns(String[] colData) {
                    propertiesKeys.clear();
                    Collections.addAll(propertiesKeys, colData);
                }

                @Override
                public void types(String[] types) {
                }

                @Override
                public boolean newrow(String[] rowData) {
                    Feature gdbFeature = new Feature();
                    gdbFeature.setEntityName(featureLayer.getName());
                    gdbFeature.setFeatureLabelExpression(featureLayer.getLabelPropertyName());

                    Map<String, Object> properties = new HashMap<>();
                    for (int i = 0; i < rowData.length; i++) {
                        if (rowData[i] != null && featureLayer.getPropertiesHashMap().containsKey(propertiesKeys.get(i))) {
                            // if (!propertiesKeys.get(i).equalsIgnoreCase("ID")) {
                            String dataType = featureLayer.getPropertiesHashMap().get(propertiesKeys.get(i)).getType();
                            if (dataType != null && rowData[i] != null) {
                                if (dataType.equalsIgnoreCase("double") || dataType.equalsIgnoreCase("double precision")) {
                                    Double aDoubleValue = 0d;
                                    try {
                                        aDoubleValue = Double.parseDouble(rowData[i]);
                                    } catch (java.lang.Exception e) {
                                        e.printStackTrace();
                                    }
                                    properties.put(propertiesKeys.get(i), aDoubleValue);
                                }
                                else if (dataType.equalsIgnoreCase("string") || dataType.equalsIgnoreCase("text")) {
                                    properties.put(propertiesKeys.get(i), rowData[i]);
                                }
                                else if (dataType.equalsIgnoreCase("date")) {
                                    if (rowData[i].equalsIgnoreCase("") || rowData[i].equalsIgnoreCase("null")) {
                                        properties.put(propertiesKeys.get(i), null);
                                    }
                                    else {
                                        properties.put(propertiesKeys.get(i), rowData[i]);
                                    }
                                }
                                else if (dataType.equalsIgnoreCase("numeric") || dataType.equalsIgnoreCase("integer")) {
                                    Integer intValue = 0;
                                    try {
                                        intValue = Integer.parseInt(rowData[i]);
                                    } catch (java.lang.Exception e) {
                                        e.printStackTrace();
                                    }
                                    properties.put(propertiesKeys.get(i), intValue);
                                }
                                else if (dataType.equalsIgnoreCase("long")) {
                                    Long aLongValue = 0L;
                                    try {
                                        aLongValue = Long.valueOf(rowData[i]);
                                    } catch (java.lang.Exception e) {
                                        e.printStackTrace();
                                    }
                                    properties.put(propertiesKeys.get(i), aLongValue);
                                }
                                else {
                                    properties.put(propertiesKeys.get(i), String.valueOf(rowData[i]));
                                }
                            }
                            // }
                        }
                    }
/*
                    if (getName().equalsIgnoreCase(AppController.TRAIL_TABLE_TABLENAME) ||
                            getName().equalsIgnoreCase(AppController.TRAIL_OLD_TABLE_TABLENAME)) {
                        //todo - check if here too you need to send _surveyname in the w9entityclassname
                        properties.put(DataModelUtils.W9_ENTITY_CLASS_NAME, getName());
                    }*/

                    gdbFeature.setAttributes(properties);
                    gdbFeature.setGeoJsonGeometry(null);
                    gdbFeatureList.add(gdbFeature);

                    return false;
                }
            };
            SpatialiteInterface.runQuery(context, AppFolderStructure.getDataGeoPackage(context).getAbsolutePath(), query, callback);
        } catch (Exception e) {
            ReveloLogger.error(className, "get All Gdb Features Without Geometry", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return gdbFeatureList;
    }//SPATIALITE ENDS----------------------------------------

    public Map<Object, com.vividsolutions.jts.geom.Geometry> getIdGeometryMap(Context context, String query) {
        final Map<Object, com.vividsolutions.jts.geom.Geometry> idGeometryMap = new HashMap<>();
        try {
            Log.i("eesha", "query id geom " + query);
            Database gdb = SpatialiteInterface.openGdb(context, AppFolderStructure.getDataGeoPackage(context).getAbsolutePath());
            Stmt stmt = gdb.prepare(query);

            while (stmt.step()) {
                Object w9Id = null;
                String w9IdPropertyName = featureLayer.getW9IdProperty();

//                if (getName().equalsIgnoreCase(AppController.TRAIL_TABLE_TABLENAME) || getName().equalsIgnoreCase(AppController.TRAIL_OLD_TABLE_TABLENAME)) {
//                    w9IdPropertyName = AppController.TRAIL_TABLE_TRAILID;
//                }

                if (w9IdPropertyName != null && ! w9IdPropertyName.equalsIgnoreCase("")) {

                    String dataype = featureLayer.getPropertiesHashMap().get(w9IdPropertyName).getType();
                    if (dataype.equalsIgnoreCase("string") || dataype.equalsIgnoreCase("date") || dataype.equalsIgnoreCase("text")) {
                        w9Id = stmt.column_string(0);
                    }
                    else if (dataype.equalsIgnoreCase("double") || dataype.equalsIgnoreCase("double precision")) {
                        w9Id = stmt.column_double(0);
                    }
                    else if (dataype.equalsIgnoreCase("integer")) {
                        w9Id = stmt.column_int(0);
                    }

                    com.vividsolutions.jts.geom.Geometry g = new WKBReader().read(stmt.column_bytes(1));
                    idGeometryMap.put(w9Id, g);
                }
            }

            gdb.close();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idGeometryMap;
    }

    public List<Feature> getFeaturesByQuery(Context context, List<String> requiredFieldsList, JSONArray ORClausesArray, JSONArray ANDClausesArray, String ANDorOR, boolean getFromMainTable, boolean getFromShadowTable, boolean isDistinct, int startIndex, int limit, boolean queryGeometry, boolean transformGeometry) {


        List<Feature> featureList = new ArrayList<>();
        GeoPackageRWAgent gpkgRWAgent = null;
        try {
            gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            if (getFromMainTable) {
                if (! featureLayer.getType().equalsIgnoreCase("spatial")) {
                    queryGeometry = false;
                    transformGeometry = false;
                }
                JSONObject respJObj = gpkgRWAgent.getDatasetContentNEW(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(false), requiredFieldsList, ORClausesArray, ANDClausesArray, ANDorOR, isDistinct, startIndex, limit, queryGeometry, transformGeometry);

                if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                    if (respJObj.has("features")) {
                        JSONObject responseFeatures = respJObj.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject featureJObj = featuresJArray.getJSONObject(i);
                                Feature feature = constructFeatureFromJson(featureJObj);
                                if (feature != null) {
                                    featureList.add(feature);
                                }
                                else {
                                    ReveloLogger.error(className, "getFeaturesByQuery", "not adding feature to list. Reason - null value received");
                                }
                            }
                        }
                        else {
                            ReveloLogger.error(className, "getFeaturesByQuery", "Query did not return features object");
                        }
                    }
                    else {
                        ReveloLogger.error(className, "getFeaturesByQuery", "Query did not return features object");
                    }

                }
                else {
                    ReveloLogger.error(className, "getFeaturesByQuery", "Query failed on main table ");
                }
            }
            else {
                ReveloLogger.info(className, "getFeaturesByQuery", "Skipping check on main table as get from maintable = " + getFromMainTable);
            }
        } catch (java.lang.Exception e) {
            ReveloLogger.error(className, "getFeaturesByQuery", "not adding feature to list from main table. Reason - Exception: " + e.getMessage());
            e.printStackTrace();
        }
        ReveloLogger.info(className, "getFeaturesByQuery", "main table has: " + featureList.size() + " features matching the query");
        try {
            if (gpkgRWAgent == null) {
                gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForDataGpkg(context), new ReveloLogger(), context);
            }
            if (getFromShadowTable && featureLayer.isHasShadowTable()) {

                queryGeometry = false;
                transformGeometry = false;

                JSONObject respJObj = gpkgRWAgent.getDatasetContentNEW(context, DbRelatedConstants.getDataSourceInfoForDataGpkg(context), getDatasetInfo(true), requiredFieldsList, ORClausesArray, ANDClausesArray, ANDorOR, isDistinct, startIndex, limit, queryGeometry, transformGeometry);

                if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                    if (respJObj.has("features")) {
                        JSONObject responseFeatures = respJObj.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject featureJObj = featuresJArray.getJSONObject(i);
                                Feature feature = constructFeatureFromJson(featureJObj);
                                if (feature != null) {
                                    featureList.add(feature);
                                }
                                else {
                                    ReveloLogger.error(className, "getFeaturesByQuery", "not adding feature to list. Reason - null value received");
                                }
                            }
                        }
                        else {
                            ReveloLogger.error(className, "getFeaturesByQuery", "Query did not return features object");
                        }
                    }
                    else {
                        ReveloLogger.error(className, "getFeaturesByQuery", "Query did not return features object");
                    }

                }
                else {
                    ReveloLogger.error(className, "getFeaturesByQuery", "Query failed on shadow table ");
                }
            }
            else {
                ReveloLogger.info(className, "getFeaturesByQuery", "Skipping check on main table as get from shadow = " + getFromShadowTable);
            }

        } catch (Exception e) {
            ReveloLogger.error(className, "getFeaturesByQuery", "not adding feature to list from shadow table. Reason - Exception: " + e.getMessage());
            e.printStackTrace();
        }
        ReveloLogger.info(className, "getFeaturesByQuery", "returning list with size " + featureList.size());
        return featureList;
    }

}