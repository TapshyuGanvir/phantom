package com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage;

import android.content.Context;
import android.util.Log;

import com.sixsimplex.treasurehunt.revelocore.data.GeoJsonUtils;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.vividsolutions.jts.geom.Geometry;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesColumns;
import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesRow;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.features.user.FeatureColumns;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.UserCoreResult;
import mil.nga.geopackage.user.UserCoreRow;
import mil.nga.geopackage.user.UserDao;
import mil.nga.sf.GeometryType;
import mil.nga.sf.LineString;
import mil.nga.sf.MultiLineString;
import mil.nga.sf.MultiPolygon;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionTransform;

import static com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants.className;


public class GeoPackageUtils {


    //start region GeoPackage

    /**
     * Creates GeoPackage
     *
     * @param propertiesJSON
     * @param logger
     * @return
     */
    public static JSONObject createGeoPackage(JSONObject propertiesJSON, ReveloLogger logger, Context context) {
        GeoPackage geoPackage = null;
        try {
            JSONObject responseJSON = new JSONObject();
            responseJSON.put("status", "failure");

            if (propertiesJSON.has("dbPath")) {
                String dbPath = propertiesJSON.getString("dbPath");
                if (dbPath.trim().isEmpty()) {
                    responseJSON.put("message", "Path to geopackage file is empty.");
                    return responseJSON;
                }
                File gpkgFile = new File(propertiesJSON.getString("dbPath"));
                if (!gpkgFile.exists()) {
                    gpkgFile.createNewFile();
                }
                geoPackage = GeoPackageManagerAgent.getDataGeoPackage(context, propertiesJSON);


                responseJSON.put("status", "success");
            } else {
                responseJSON.put("message", "No path to geopackage file provided.");
            }

            return responseJSON;
        } catch (JSONException | IOException e) {
            return SystemUtils.logAndReturnErrorMessage(e.getMessage(), e);
        }
		/*finally {
			if(geoPackage != null) {
				geoPackage.close();
			}
		}*/
    }

    /**
     * @param propertiesJSON
     * @return
     */
    public static boolean doesGeoPackageExist(JSONObject propertiesJSON) {
        try {
            String dbPath = propertiesJSON.getString("dbPath");
            return new File(dbPath).exists();
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param propertiesJSON
     * @return
     */
    public static JSONObject deleteGeoPackage(JSONObject propertiesJSON) {
        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");
            String dbPath = propertiesJSON.getString("dbPath");
            File dbFile = new File(dbPath);
            FileUtils.deleteQuietly(dbFile);
            responseJSON.put("status", "success");
        } catch (JSONException e) {
        }/* finally {
		}*/
        return responseJSON;
    }

    public static boolean datasetExists(Context context, String datasetName_entityName, String datasourceName) {
        GeoPackage geoPackage = null;
        boolean entityExists = false;
        try {
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage != null) {//check for spatial entities: datatype features
                entityExists = geoPackage.getFeatureDao(datasetName_entityName) != null;
                if (!entityExists) {//check of non spatial entities: datatype attribute
                    entityExists = geoPackage.getAttributesDao(datasetName_entityName) != null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }/*finally {
			if(geoPackage!=null)
				geoPackage.close();
		}*/
        return entityExists;
    }

    public static JSONObject getFeatures(Context context, String datasourceName, String datasetName, String datasetType,
                                         HashMap<String, JSONObject> columnNameValueConditionMap, String ANDorOR,
                                         List<String> requiredColumnsList, boolean distinct, int startIndex,
                                         int maxFeatures, boolean queryGeometry, ReveloLogger geopackageRWLogger) {
        Log.i("eee", "get features: datasetname-" + datasetName
                + " conditions?- " + (columnNameValueConditionMap != null ? columnNameValueConditionMap.size() : "No")
                + " ANDorOR?- " + ANDorOR
                + " reqd columns?- " + (requiredColumnsList != null ? requiredColumnsList.size() : "All")
                + " query geom?- " + queryGeometry
        );
        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unavailable");

            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;
            String whereClause = null;
            if (columnNameValueConditionMap == null || columnNameValueConditionMap.isEmpty()) {
                //userCoreResult = userDao.queryForAll();
                whereClause = null;
            } else {
                //create where clause
                whereClause = "";
                for (String columnName : columnNameValueConditionMap.keySet()) {

                    JSONObject coonditionJson = columnNameValueConditionMap.get(columnName);
                    String objectVal = coonditionJson.getString("value");

                    if (coonditionJson.has("isCheckEquals")) {
                        boolean isCheckEquals = coonditionJson.getBoolean("isCheckEquals");
                        String columnType = coonditionJson.getString("columnType");
                        if (columnType.equalsIgnoreCase("string")
                                || columnType.equalsIgnoreCase("text")
                                || columnType.equalsIgnoreCase("date")) {
                            if (isCheckEquals) {
                                whereClause += columnName + " = '" + objectVal + "' " + ANDorOR + " ";
                            } else {
                                whereClause += columnName + " IS NOT '" + objectVal + "' " + ANDorOR + " ";
                            }
                        } else {
                            if (isCheckEquals) {
                                whereClause += columnName + " = " + objectVal + " " + ANDorOR + " ";
                            } else {
                                whereClause += columnName + " IS NOT " + objectVal + " " + ANDorOR + " ";
                            }
                        }
                    } else if (coonditionJson.has("operator")) {
                        String columnType = coonditionJson.getString("columnType");
                        String operator = coonditionJson.getString("operator");
                        if (columnType.equalsIgnoreCase("string")
                                || columnType.equalsIgnoreCase("text")
                                || columnType.equalsIgnoreCase("date")) {
                            whereClause += columnName + " " + operator + " '" + objectVal + "' " + ANDorOR + " ";
                        } else {
                            whereClause += columnName + " " + operator + " " + objectVal + " " + ANDorOR + " ";
                        }
                    } else if (coonditionJson.has("within")) {
                        boolean withIn = coonditionJson.getBoolean("within");
                        if (withIn) {
                            whereClause += "within(GeomFromText('POINT(" + objectVal + ")')," + columnName + ")";
                        }
                    }
                }

                if (whereClause.endsWith(ANDorOR + " ")) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
                }
            }
            String[] columnsreqd = null;
            if (requiredColumnsList != null) {
                if (!isDatasetOfAttributeType & queryGeometry) {
                    FeatureDao featureDao = (FeatureDao) userDao;
                    String geometryColumnName = featureDao.getGeometryColumnName();
                    if (!requiredColumnsList.contains(geometryColumnName)) {
                        requiredColumnsList.add(geometryColumnName);
                    }
                }
                if (userDao.getTable().hasColumn(AppConstants.W9_METADATA) && !requiredColumnsList.contains(AppConstants.W9_METADATA)) {
                    requiredColumnsList.add(AppConstants.W9_METADATA);
                }
                columnsreqd = requiredColumnsList.toArray(new String[requiredColumnsList.size()]);
            } else {
                columnsreqd = userDao.getColumnNames();
            }
            assert userDao != null;
            if (maxFeatures < 0) {
                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, null);
            } else {
                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, String.valueOf(maxFeatures));
            }
            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }


            JSONArray featuresJArray = new JSONArray();
            if (!isDatasetOfAttributeType) {
                try {
                    //int count = 0;//temporary
                    while (userCoreResult.moveToNext()/* && count<10*/) {
                        //count++;//temp
                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            FeatureRow featureRow = (FeatureRow) userCoreResult.getRow();
                            if (featureRow.hasColumn(featureRow.getGeometryColumnName())) {
                                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                                if (geometryData != null && !geometryData.isEmpty()) {
                                    mil.nga.sf.Geometry geometry = geometryData.getGeometry();
                                    Projection projection = ((FeatureDao) userDao).getProjection();
                                    ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                                    geometryData = geometryData.transform(transform4326);

                                    mil.nga.sf.Geometry geom4326 = null;
                                    if (geometryData != null) {
                                        geometryData.setSrsId(4326);
                                        geom4326 = geometryData.getGeometry();
                                    }
                                    if (geom4326 != null) {
                                        JSONObject geometryGeoJson = convertGeometryToGeoJson(geom4326);
                                        //String wkt = geometryData.getWkt();
                                        featureJobj.put("geometry", geometryGeoJson);
                                    } else {
                                        featureJobj.put("geometry", "{}");
                                    }
                                    featureJobj.put("geometryColumnName", featureRow.getGeometryColumnName());

                                }
                            }
                            Object[] values = featureRow.getValues();
                            FeatureColumns featureColumns = featureRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {
                                String columnName = ColumnNames[i];
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            } else {
                try {
                    while (userCoreResult.moveToNext()) {

                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
                            Object[] values = attributesRow.getValues();
                            AttributesColumns featureColumns = attributesRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {

                                String columnName = String.valueOf(ColumnNames[i]);
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            }
            JSONObject jsonObjectfeatures = new JSONObject();
            jsonObjectfeatures.put("features", featuresJArray);
            resultJObj.put("features", jsonObjectfeatures);
            resultJObj.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
            try {
                resultJObj.put("status", "Failure");
                resultJObj.put("message", "Reason unavailable");
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }/*finally {
			if(geoPackage!=null){
				geoPackage.close();
			}
		}*/


        return resultJObj;
    }

    public static JSONObject getFeatures(Context context, String datasourceName, String datasetName, String datasetType,
                                         HashMap<String, JSONObject> columnNameValueConditionMap, String ANDorOR,
                                         List<String> requiredColumnsList, boolean distinct, int startIndex,
                                         int maxFeatures, boolean queryGeometry, boolean transformGeometry, ReveloLogger geopackageRWLogger) {
        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unavailable");

            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;
            String whereClause = null;
            if (columnNameValueConditionMap == null || columnNameValueConditionMap.isEmpty()) {
                //userCoreResult = userDao.queryForAll();
                whereClause = null;
            } else {
                //create where clause
                whereClause = "";
                for (String columnName : columnNameValueConditionMap.keySet()) {

                    JSONObject coonditionJson = columnNameValueConditionMap.get(columnName);
                    String objectVal = coonditionJson.getString("value");

                    if (coonditionJson.has("isCheckEquals")) {
                        boolean isCheckEquals = coonditionJson.getBoolean("isCheckEquals");
                        String columnType = coonditionJson.getString("columnType");
                        if (columnType.equalsIgnoreCase("string")
                                || columnType.equalsIgnoreCase("text")
                                || columnType.equalsIgnoreCase("date")) {
                            if (isCheckEquals) {
                                whereClause += columnName + " = '" + objectVal + "' " + ANDorOR;
                            } else {
                                whereClause += columnName + " IS NOT '" + objectVal + "' " + ANDorOR;
                            }
                        } else {
                            if (isCheckEquals) {
                                whereClause += columnName + " = " + objectVal + " " + ANDorOR;
                            } else {
                                whereClause += columnName + " IS NOT " + objectVal + " " + ANDorOR;
                            }
                        }
                    } else if (coonditionJson.has("operator")) {
                        String columnType = coonditionJson.getString("columnType");
                        String operator = coonditionJson.getString("operator");
                        if (columnType.equalsIgnoreCase("string")
                                || columnType.equalsIgnoreCase("text")
                                || columnType.equalsIgnoreCase("date")) {
                            whereClause += columnName + " " + operator + " '" + objectVal + "' " + ANDorOR;
                        } else {
                            whereClause += columnName + " " + operator + " " + objectVal + " " + ANDorOR;
                        }
                    } else if (coonditionJson.has("within")) {
                        boolean withIn = coonditionJson.getBoolean("within");
                        if (withIn) {
                            whereClause += "within(GeomFromText('POINT(" + objectVal + ")')," + columnName + ")";
                        }
                    }
                }

                if (whereClause.endsWith(ANDorOR)) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
                }
            }
            String[] columnsreqd = null;
            if (requiredColumnsList != null) {
                if (!isDatasetOfAttributeType & queryGeometry) {
                    FeatureDao featureDao = (FeatureDao) userDao;
                    String geometryColumnName = featureDao.getGeometryColumnName();
                    if (!requiredColumnsList.contains(geometryColumnName)) {
                        requiredColumnsList.add(geometryColumnName);
                    }
                }
                if (userDao.getTable().hasColumn(AppConstants.W9_METADATA) && !requiredColumnsList.contains(AppConstants.W9_METADATA)) {
                    requiredColumnsList.add(AppConstants.W9_METADATA);
                }
                columnsreqd = requiredColumnsList.toArray(new String[requiredColumnsList.size()]);
            } else {
                columnsreqd = userDao.getColumnNames();
            }
            assert userDao != null;
            if (maxFeatures < 0) {
                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, null);
            } else {
                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, String.valueOf(maxFeatures));
            }
            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }


            JSONArray featuresJArray = new JSONArray();
            if (!isDatasetOfAttributeType) {
                try {
                    //int count = 0;//temporary
                    while (userCoreResult.moveToNext()/* && count<10*/) {
                        //count++;//temp
                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            FeatureRow featureRow = (FeatureRow) userCoreResult.getRow();
                            if (featureRow.hasColumn(featureRow.getGeometryColumnName())) {
                                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                                if (geometryData != null && !geometryData.isEmpty()) {
                                    if (transformGeometry) {
                                        mil.nga.sf.Geometry geometry = geometryData.getGeometry();
                                        Projection projection = ((FeatureDao) userDao).getProjection();
                                        ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                                        geometryData = geometryData.transform(transform4326);

                                        mil.nga.sf.Geometry geom4326 = null;
                                        if (geometryData != null) {
                                            geometryData.setSrsId(4326);
                                            geom4326 = geometryData.getGeometry();
                                        }
                                        if (geom4326 != null) {
                                            JSONObject geometryGeoJson = convertGeometryToGeoJson(geom4326);
                                            //String wkt = geometryData.getWkt();
                                            featureJobj.put("geometry", geometryGeoJson);
                                        } else {
                                            featureJobj.put("geometry", "{}");
                                        }
                                    } else {
                                        mil.nga.sf.Geometry geom = null;
                                        if (geometryData != null) {
                                            geom = geometryData.getGeometry();
                                        }
                                        if (geom != null) {
                                            JSONObject geometryGeoJson = convertGeometryToGeoJson(geom);
                                            //String wkt = geometryData.getWkt();
                                            featureJobj.put("geometry", geometryGeoJson);
                                        } else {
                                            featureJobj.put("geometry", "{}");
                                        }
                                    }
                                    featureJobj.put("geometryColumnName", featureRow.getGeometryColumnName());

                                }
                            }
                            Object[] values = featureRow.getValues();
                            FeatureColumns featureColumns = featureRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {
                                String columnName = ColumnNames[i];
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            } else {
                try {
                    while (userCoreResult.moveToNext()) {

                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
                            Object[] values = attributesRow.getValues();
                            AttributesColumns featureColumns = attributesRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {

                                String columnName = String.valueOf(ColumnNames[i]);
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            }
            JSONObject jsonObjectfeatures = new JSONObject();
            jsonObjectfeatures.put("features", featuresJArray);
            resultJObj.put("features", jsonObjectfeatures);
            resultJObj.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if(geoPackage!=null){
				geoPackage.close();
			}
		}*/


        return resultJObj;
    }

    public static JSONObject getFeatures(Context context, String datasourceName, String datasetName, String datasetType,
                                         JSONArray whereClauseArray, String ANDorOR, JSONArray copulsorywhereClauseArray,
                                         List<String> requiredColumnsList, boolean distinct, int startIndex,
                                         int maxFeatures, boolean queryGeometry, boolean transformGeometry,
                                         ReveloLogger geopackageRWLogger) {
        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unavailable");

            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;
            String whereClause = "";
            if (ANDorOR == null || ANDorOR.isEmpty()) {
                ANDorOR = "OR";
            }
            if (whereClauseArray != null && whereClauseArray.length() != 0) {
                for (int i = 0; i < whereClauseArray.length(); i++) {

                    JSONObject conditionJobj = whereClauseArray.getJSONObject(i);
                    String conditionType = conditionJobj.getString("conditionType");
                    String columnName = conditionJobj.getString("columnName");
                    String valueDataType = conditionJobj.getString("valueDataType");
                    Object value = conditionJobj.get("value");
                    String operator = conditionJobj.getString("operator");


                    String wherePart = "";
                    if (conditionType.equalsIgnoreCase("attribute")) {
                        if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
                            wherePart = columnName + " " + operator + " '" + value + "' ";
                        else
                            wherePart = columnName + " " + operator + " " + value + " ";


                        if (whereClause.isEmpty()) {
                            whereClause = wherePart;
                        } else {
                            whereClause = whereClause + " " + ANDorOR + " " + wherePart;
                        }

                    } else {
                        if (valueDataType.equalsIgnoreCase("geometry")) {
                            whereClause += "within(GeomFromText('POINT(" + value + ")')," + columnName + ")";
                        }
                    }

                }
                if (whereClause.endsWith(ANDorOR)) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
                }
            }

            if (copulsorywhereClauseArray != null && copulsorywhereClauseArray.length() != 0) {
                if (!whereClause.isEmpty()) {
                    whereClause = "(" + whereClause + ")" + " AND ( ";
                }
                for (int i = 0; i < copulsorywhereClauseArray.length(); i++) {

                    JSONObject conditionJobj = copulsorywhereClauseArray.getJSONObject(i);
                    String conditionType = conditionJobj.getString("conditionType");
                    String columnName = conditionJobj.getString("columnName");
                    String valueDataType = conditionJobj.getString("valueDataType");
                    Object value = conditionJobj.get("value");
                    String operator = conditionJobj.getString("operator");


                    String wherePart = "";
                    if (conditionType.equalsIgnoreCase("attribute")) {
                        if (valueDataType.equalsIgnoreCase("string")
                                || valueDataType.equalsIgnoreCase("text"))
                            wherePart = columnName + " " + operator + " '" + value + "' ";
                        else
                            wherePart = columnName + " " + operator + " " + value + " ";


                        if (i == 0) {
                            whereClause = whereClause + wherePart;
                        } else {
                            whereClause = whereClause + " AND " + wherePart;
                        }

                    } else {
                        if (valueDataType.equalsIgnoreCase("geometry")) {
                            whereClause += "within(GeomFromText('POINT(" + value + ")')," + columnName + ")";
                        }
                    }

                }
                if (whereClause.endsWith("AND")) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf("AND"));
                }
                whereClause += ")";
            }
            String[] columnsreqd = null;
            if (requiredColumnsList != null) {
                if (!isDatasetOfAttributeType & queryGeometry) {
                    FeatureDao featureDao = (FeatureDao) userDao;
                    String geometryColumnName = featureDao.getGeometryColumnName();
                    if (!requiredColumnsList.contains(geometryColumnName)) {
                        requiredColumnsList.add(geometryColumnName);
                    }
                }
                if (userDao.getTable().hasColumn(AppConstants.W9_METADATA) && !requiredColumnsList.contains(AppConstants.W9_METADATA)) {
                    requiredColumnsList.add(AppConstants.W9_METADATA);
                }
                columnsreqd = requiredColumnsList.toArray(new String[requiredColumnsList.size()]);
            } else {
                columnsreqd = userDao.getColumnNames();
            }
            assert userDao != null;
            if (maxFeatures < 0) {
                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, null);
            } else {
                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, String.valueOf(maxFeatures));
            }
            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }


            JSONArray featuresJArray = new JSONArray();
            if (!isDatasetOfAttributeType) {
                try {
                    //int count = 0;//temporary
                    while (userCoreResult.moveToNext()/* && count<10*/) {
                        //count++;//temp
                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            FeatureRow featureRow = (FeatureRow) userCoreResult.getRow();
                            if (featureRow.hasColumn(featureRow.getGeometryColumnName())) {
                                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                                if (geometryData != null && !geometryData.isEmpty()) {
                                    if (transformGeometry) {
                                        mil.nga.sf.Geometry geometry = geometryData.getGeometry();
                                        Projection projection = ((FeatureDao) userDao).getProjection();
                                        ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                                        geometryData = geometryData.transform(transform4326);

                                        mil.nga.sf.Geometry geom4326 = null;
                                        if (geometryData != null) {
                                            geometryData.setSrsId(4326);
                                            geom4326 = geometryData.getGeometry();
                                        }
                                        if (geom4326 != null) {
                                            JSONObject geometryGeoJson = convertGeometryToGeoJson(geom4326);
                                            //String wkt = geometryData.getWkt();
                                            featureJobj.put("geometry", geometryGeoJson);
                                        } else {
                                            featureJobj.put("geometry", "{}");
                                        }
                                    } else {
                                        mil.nga.sf.Geometry geom = null;
                                        if (geometryData != null) {
                                            geom = geometryData.getGeometry();
                                        }
                                        if (geom != null) {
                                            JSONObject geometryGeoJson = convertGeometryToGeoJson(geom);
                                            //String wkt = geometryData.getWkt();
                                            featureJobj.put("geometry", geometryGeoJson);
                                        } else {
                                            featureJobj.put("geometry", "{}");
                                        }
                                    }
                                    featureJobj.put("geometryColumnName", featureRow.getGeometryColumnName());

                                }
                            }
                            Object[] values = featureRow.getValues();
                            FeatureColumns featureColumns = featureRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {
                                String columnName = ColumnNames[i];
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            } else {
                try {
                    while (userCoreResult.moveToNext()) {

                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
                            Object[] values = attributesRow.getValues();
                            AttributesColumns featureColumns = attributesRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {

                                String columnName = String.valueOf(ColumnNames[i]);
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            }
            JSONObject jsonObjectfeatures = new JSONObject();
            jsonObjectfeatures.put("features", featuresJArray);
            resultJObj.put("features", jsonObjectfeatures);
            resultJObj.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if(geoPackage!=null){
				geoPackage.close();
			}
		}*/


        return resultJObj;
    }

    public static JSONObject getFeatures(Context context, String datasourceName, String datasetName, String datasetType,
                                         JSONArray whereClauseArray, String ANDorOR,
                                         List<String> requiredColumnsList, boolean distinct, int startIndex,
                                         int maxFeatures, boolean queryGeometry, boolean transformGeometry, ReveloLogger geopackageRWLogger) {
        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unavailable");

            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;
            String whereClause = "";
            if (whereClauseArray != null && whereClauseArray.length() != 0) {
                for (int i = 0; i < whereClauseArray.length(); i++) {

                    JSONObject conditionJobj = whereClauseArray.getJSONObject(i);
                    String conditionType = conditionJobj.getString("conditionType");
                    String columnName = conditionJobj.getString("columnName");
                    String valueDataType = conditionJobj.getString("valueDataType");
                    Object value = conditionJobj.get("value");
                    String operator = conditionJobj.getString("operator");


                    String wherePart = "";
                    if (conditionType.equalsIgnoreCase("attribute")) {
                        if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
                            wherePart = columnName + " " + operator + " '" + value + "' ";
                        else
                            wherePart = columnName + " " + operator + " " + value + " ";


                        if (whereClause.isEmpty()) {
                            whereClause = wherePart;
                        } else {
                            whereClause = whereClause + " " + ANDorOR + " " + wherePart;
                        }

                    } else {
                        if (valueDataType.equalsIgnoreCase("geometry")) {
                            whereClause += "within(GeomFromText('POINT(" + value + ")')," + columnName + ")";
                        }
                    }

                }
                if (whereClause.endsWith(ANDorOR)) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
                }
            }

            String[] columnsreqd = null;
            if (requiredColumnsList != null) {
                if (!isDatasetOfAttributeType & queryGeometry) {
                    FeatureDao featureDao = (FeatureDao) userDao;
                    String geometryColumnName = featureDao.getGeometryColumnName();
                    if (!requiredColumnsList.contains(geometryColumnName)) {
                        requiredColumnsList.add(geometryColumnName);
                    }
                }
                if (userDao.getTable().hasColumn(AppConstants.W9_METADATA) && !requiredColumnsList.contains(AppConstants.W9_METADATA)) {
                    requiredColumnsList.add(AppConstants.W9_METADATA);
                }
                columnsreqd = requiredColumnsList.toArray(new String[requiredColumnsList.size()]);
            } else {
                columnsreqd = userDao.getColumnNames();
            }
            assert userDao != null;
            if (maxFeatures < 0) {
                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, null);
            } else {
                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, String.valueOf(maxFeatures));
            }
            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }


            JSONArray featuresJArray = new JSONArray();
            if (!isDatasetOfAttributeType) {
                try {
                    //int count = 0;//temporary
                    while (userCoreResult.moveToNext()/* && count<10*/) {
                        //count++;//temp
                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            FeatureRow featureRow = (FeatureRow) userCoreResult.getRow();
                            if (featureRow.hasColumn(featureRow.getGeometryColumnName())) {
                                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                                if (geometryData != null && !geometryData.isEmpty()) {
                                    if (transformGeometry) {
                                        mil.nga.sf.Geometry geometry = geometryData.getGeometry();
                                        Projection projection = ((FeatureDao) userDao).getProjection();
                                        ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                                        geometryData = geometryData.transform(transform4326);

                                        mil.nga.sf.Geometry geom4326 = null;
                                        if (geometryData != null) {
                                            geometryData.setSrsId(4326);
                                            geom4326 = geometryData.getGeometry();
                                        }
                                        if (geom4326 != null) {
                                            JSONObject geometryGeoJson = convertGeometryToGeoJson(geom4326);
                                            //String wkt = geometryData.getWkt();
                                            featureJobj.put("geometry", geometryGeoJson);
                                        } else {
                                            featureJobj.put("geometry", "{}");
                                        }
                                    } else {
                                        mil.nga.sf.Geometry geom = null;
                                        if (geometryData != null) {
                                            geom = geometryData.getGeometry();
                                        }
                                        if (geom != null) {
                                            JSONObject geometryGeoJson = convertGeometryToGeoJson(geom);
                                            //String wkt = geometryData.getWkt();
                                            featureJobj.put("geometry", geometryGeoJson);
                                        } else {
                                            featureJobj.put("geometry", "{}");
                                        }
                                    }
                                    featureJobj.put("geometryColumnName", featureRow.getGeometryColumnName());

                                }
                            }
                            Object[] values = featureRow.getValues();
                            FeatureColumns featureColumns = featureRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {
                                String columnName = ColumnNames[i];
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            } else {
                try {
                    while (userCoreResult.moveToNext()) {

                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
                            Object[] values = attributesRow.getValues();
                            AttributesColumns featureColumns = attributesRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {

                                String columnName = String.valueOf(ColumnNames[i]);
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            }
            JSONObject jsonObjectfeatures = new JSONObject();
            jsonObjectfeatures.put("features", featuresJArray);
            resultJObj.put("features", jsonObjectfeatures);
            resultJObj.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if(geoPackage!=null){
				geoPackage.close();
			}
		}*/


        return resultJObj;
    }

    public static JSONObject getFeatures(Context context, String datasourceName, String datasetName, String datasetType,
                                         HashMap<String, JSONObject> columnNameValueConditionMap, String ANDorOR,
                                         List<String> requiredColumnsList, boolean distinct, String groupBy, String orderBy, boolean desc, int startIndex,
                                         int maxFeatures, boolean queryGeometry, ReveloLogger geopackageRWLogger) {
        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unvailable");

            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;
            String whereClause = null;
            if (columnNameValueConditionMap == null || columnNameValueConditionMap.isEmpty()) {
                //userCoreResult = userDao.queryForAll();
                whereClause = null;
            } else {
                //create where clause
                whereClause = "";
                for (String columnName : columnNameValueConditionMap.keySet()) {

                    JSONObject coonditionJson = columnNameValueConditionMap.get(columnName);
                    String objectVal = coonditionJson.getString("value");

                    if (coonditionJson.has("isCheckEquals")) {
                        boolean isCheckEquals = coonditionJson.getBoolean("isCheckEquals");
                        String columnType = coonditionJson.getString("columnType");
                        if (columnType.equalsIgnoreCase("string")
                                || columnType.equalsIgnoreCase("text")
                                || columnType.equalsIgnoreCase("date")) {
                            if (isCheckEquals) {
                                whereClause += columnName + " = '" + objectVal + "' " + ANDorOR;
                            } else {
                                whereClause += columnName + " IS NOT '" + objectVal + "' " + ANDorOR;
                            }
                        } else {
                            if (isCheckEquals) {
                                whereClause += columnName + " = " + objectVal + " " + ANDorOR;
                            } else {
                                whereClause += columnName + " IS NOT " + objectVal + " " + ANDorOR;
                            }
                        }
                    } else if (coonditionJson.has("operator")) {
                        String columnType = coonditionJson.getString("columnType");
                        String operator = coonditionJson.getString("operator");
                        if (columnType.equalsIgnoreCase("string")
                                || columnType.equalsIgnoreCase("text")
                                || columnType.equalsIgnoreCase("date")) {
                            whereClause += columnName + " " + operator + " '" + objectVal + "' " + ANDorOR;
                        } else {
                            whereClause += columnName + " " + operator + " " + objectVal + " " + ANDorOR;
                        }
                    } else if (coonditionJson.has("within")) {
                        boolean withIn = coonditionJson.getBoolean("within");
                        if (withIn) {
                            whereClause += "within(GeomFromText('POINT(" + objectVal + ")')," + columnName + ")";
                        }
                    }
                }

                if (whereClause.endsWith(ANDorOR)) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
                }
            }
            String[] columnsreqd = null;
            if (requiredColumnsList != null) {
                if (!isDatasetOfAttributeType & queryGeometry) {
                    FeatureDao featureDao = (FeatureDao) userDao;
                    String geometryColumnName = featureDao.getGeometryColumnName();
                    if (!requiredColumnsList.contains(geometryColumnName)) {
                        requiredColumnsList.add(geometryColumnName);
                    }
                }
                columnsreqd = requiredColumnsList.toArray(new String[requiredColumnsList.size()]);
            }
            assert userDao != null;
            userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, groupBy, null, orderBy, String.valueOf(maxFeatures));
            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }


            JSONArray featuresJArray = new JSONArray();
            if (!isDatasetOfAttributeType) {
                try {
                    //int count = 0;//temporary
                    while (userCoreResult.moveToNext()/* && count<10*/) {
                        //count++;//temp
                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            FeatureRow featureRow = (FeatureRow) userCoreResult.getRow();
                            if (featureRow.hasColumn(featureRow.getGeometryColumnName())) {
                                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                                if (geometryData != null && !geometryData.isEmpty()) {
                                    mil.nga.sf.Geometry geometry = geometryData.getGeometry();
                                    Projection projection = ((FeatureDao) userDao).getProjection();
                                    ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                                    geometryData = geometryData.transform(transform4326);

                                    mil.nga.sf.Geometry geom4326 = null;
                                    if (geometryData != null) {
                                        geometryData.setSrsId(4326);
                                        geom4326 = geometryData.getGeometry();
                                    }
                                    if (geom4326 != null) {
                                        JSONObject geometryGeoJson = convertGeometryToGeoJson(geom4326);
                                        //String wkt = geometryData.getWkt();
                                        featureJobj.put("geometry", geometryGeoJson);
                                    } else {
                                        featureJobj.put("geometry", "{}");
                                    }
                                    featureJobj.put("geometryColumnName", featureRow.getGeometryColumnName());

                                }
                            }
                            Object[] values = featureRow.getValues();
                            FeatureColumns featureColumns = featureRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {
                                String columnName = ColumnNames[i];
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            } else {
                try {
                    while (userCoreResult.moveToNext()) {

                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
                            Object[] values = attributesRow.getValues();
                            AttributesColumns featureColumns = attributesRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {

                                String columnName = String.valueOf(ColumnNames[i]);
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            }
            JSONObject jsonObjectfeatures = new JSONObject();
            jsonObjectfeatures.put("features", featuresJArray);
            resultJObj.put("features", jsonObjectfeatures);
            resultJObj.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if(geoPackage!=null){
				geoPackage.close();
			}
		}*/


        return resultJObj;
    }

    public static JSONObject getFeaturesWithoutGeometry(Context context, String datasourceName, String datasetName,
                                                        HashMap<String, JSONObject> columnNameValueConditionMap, List<String> requiredColumnsList,
                                                        int startIndex, int maxFeatures, ReveloLogger geopackageRWLogger) {
        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unvailable");
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = geoPackage.getAttributesDao(datasetName);


            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }

            UserCoreResult userCoreResult = null;
            if (columnNameValueConditionMap == null || columnNameValueConditionMap.isEmpty()) {
                userCoreResult = userDao.queryForAll();
            }
            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }


            try {
                while (userCoreResult.moveToNext()) {
                    AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
                    AttributesColumns columns = attributesRow.getColumns();

                }
            } finally {
                userCoreResult.close();
            }


        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if(geoPackage!=null)
				geoPackage.close();
		}*/


        return resultJObj;
    }

    public static JSONObject getFeatures(Context context, String datasourceName, String datasetName, String datasetType, String whereClause, ReveloLogger geopackageRWLogger) {
        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unvailable");
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;

            assert userDao != null;
            userCoreResult = userDao.query(whereClause);
            //userCoreResult = userDao.query(distinct,whereClause);

            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }


            JSONArray featuresJArray = new JSONArray();
            if (!isDatasetOfAttributeType) {
                try {
                    //int count = 0;//temporary
                    while (userCoreResult.moveToNext()/* && count<10*/) {
                        //count++;//temp
                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            FeatureRow featureRow = (FeatureRow) userCoreResult.getRow();
                            if (featureRow.hasColumn(featureRow.getGeometryColumnName())) {
                                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                                if (geometryData != null && !geometryData.isEmpty()) {
                                    String wkt = geometryData.getWkt();
                                    featureJobj.put("geometry", wkt);
                                }
                            }
                            Object[] values = featureRow.getValues();
                            FeatureColumns featureColumns = featureRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {
                                String columnName = ColumnNames[i];
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            } else {
                try {
                    while (userCoreResult.moveToNext()) {

                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
                            Object[] values = attributesRow.getValues();
                            AttributesColumns featureColumns = attributesRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {

                                String columnName = String.valueOf(ColumnNames[i]);
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            }
            JSONObject jsonObjectfeatures = new JSONObject();
            jsonObjectfeatures.put("features", featuresJArray);
            resultJObj.put("features", jsonObjectfeatures);
            resultJObj.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if (geoPackage != null)
				geoPackage.close();
		}*/


        return resultJObj;
    }

    /*
     * condition will be jsonarray -
     * array of condtition jobjs -
     * {
     * 	conditionType :  "spatial"/"attribute"
     * 	columnName : the_geom/name
     * 	value: geom / site
     * 	operator: within / =
     * }
     * */
    public static JSONObject getFeatures(Context context, String datasourceName, String datasetName,
                                         String datasetType, JSONArray whereClauseArray,
                                         String ANDorOR, List<String> requiredColumnsList, boolean distinct, int startIndex,
                                         int maxFeatures, ReveloLogger geopackageRWLogger) {
        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;

        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unvailable");
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;
            if (whereClauseArray == null || whereClauseArray.length() == 0) {
                userCoreResult = userDao.queryForAll();
            } else {
                //create where clause

                String whereClause = "";
                for (int i = 0; i < whereClauseArray.length(); i++) {

                    JSONObject conditionJobj = whereClauseArray.getJSONObject(i);
                    String conditionType = conditionJobj.getString("conditionType");
                    String columnName = conditionJobj.getString("columnName");
                    String valueDataType = conditionJobj.getString("valueDataType");
                    Object value = conditionJobj.get("value");
                    String operator = conditionJobj.getString("operator");

                    String wherePart = "";
                    if (conditionType.equalsIgnoreCase("attribute")) {
                        if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
                            wherePart = columnName + " " + operator + " '" + value + "' ";
                        else
                            wherePart = columnName + " " + operator + " " + value + " ";


                        if (whereClause.isEmpty()) {
                            whereClause = wherePart;
                        } else {
                            whereClause = whereClause + " " + ANDorOR + " " + wherePart;
                        }

                    } else {
                        if (valueDataType.equalsIgnoreCase("geometry")) {
                            Geometry jtsGeom = (Geometry) value;
                        }

                    }

                }

                if (whereClause.endsWith(ANDorOR)) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
                }
                String[] columnsreqd = requiredColumnsList.toArray(new String[0]);
                assert userDao != null;
                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, String.valueOf(maxFeatures));
                //userCoreResult = userDao.query(distinct,whereClause);
            }
            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }


            JSONArray featuresJArray = new JSONArray();
            if (!isDatasetOfAttributeType) {
                try {
                    //int count = 0;//temporary
                    while (userCoreResult.moveToNext()/* && count<10*/) {
                        //count++;//temp
                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            FeatureRow featureRow = (FeatureRow) userCoreResult.getRow();
                            if (featureRow.hasColumn(featureRow.getGeometryColumnName())) {
                                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                                if (geometryData != null && !geometryData.isEmpty()) {
                                    String wkt = geometryData.getWkt();
                                    featureJobj.put("geometry", wkt);
                                }
                            }
                            Object[] values = featureRow.getValues();
                            FeatureColumns featureColumns = featureRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {
                                String columnName = ColumnNames[i];
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            } else {
                try {
                    while (userCoreResult.moveToNext()) {

                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
                            Object[] values = attributesRow.getValues();
                            AttributesColumns featureColumns = attributesRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {

                                String columnName = String.valueOf(ColumnNames[i]);
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            }
            JSONObject jsonObjectfeatures = new JSONObject();
            jsonObjectfeatures.put("features", featuresJArray);
            resultJObj.put("features", jsonObjectfeatures);
            resultJObj.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if (geoPackage != null)
				geoPackage.close();
		}*/


        return resultJObj;
    }


//    public static JSONObject getFeaturesNEW(Context context, String datasourceName, String datasetName,
//                                            String datasetType, List<String> requiredColumnsList, JSONArray ORClausesArray, JSONArray ANDClausesArray, String ANDorOR,
//                                            boolean distinct, int startIndex, int limit, boolean queryGeometry, boolean transformGeometry) {
//        Log.i("eee", "get features: datasetname-" + datasetName
////				+" conditions?- "+(columnNameValueConditionMap!=null?columnNameValueConditionMap.size():"No")
//                        + " ANDorOR?- " + ANDorOR
//                        + " reqd columns?- " + (requiredColumnsList != null ? requiredColumnsList.size() : "All")
//                        + " query geom?- " + queryGeometry
//        );
//        JSONObject resultJObj = new JSONObject();
//        GeoPackage geoPackage = null;
//        try {
//            resultJObj.put("status", "Failure");
//            resultJObj.put("message", "Reason unavailable");
//
//            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
//            if (geoPackage == null) {
//                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
//            }
//
//            UserDao userDao = null;
//            boolean isDatasetOfAttributeType = false;
//            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
//                userDao = geoPackage.getAttributesDao(datasetName);
//                isDatasetOfAttributeType = true;
//            } else {
//                userDao = geoPackage.getFeatureDao(datasetName);
//            }
//
//            if (userDao == null) {
//                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
//            }
////{"value":"Maharashtra","isCheckEquals":true}
//            UserCoreResult userCoreResult = null;
//            String whereClause=null;
//            if ((ORClausesArray == null || ORClausesArray.length()==0)&&
//                    (ANDClausesArray == null || ANDClausesArray.length()==0)) {
//
//                userCoreResult = userDao.queryForAll();
//            }else {
//
//                String whereClause_or = "";
//                if(ORClausesArray!=null && ORClausesArray.length()>0) {
//                    for (int i = 0; i < ORClausesArray.length(); i++) {
//
//                        JSONObject conditionJobj = ORClausesArray.getJSONObject(i);
//                        String conditionType = conditionJobj.getString("conditionType");
//
//                        String wherePart = "";
//                        if (conditionType.equalsIgnoreCase("attribute")) {
//                            String columnName = conditionJobj.getString("columnName");
//                            String valueDataType = conditionJobj.getString("valueDataType");
//                            Object value = conditionJobj.get("value");
//                            String operator = conditionJobj.getString("operator");
//                            if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
//                                wherePart = columnName + " " + operator + " '" + value + "' ";
//                            else
//                                wherePart = columnName + " " + operator + " " + value + " ";
//
//                            if (whereClause_or.isEmpty()) {
//                                whereClause_or = wherePart;
//                            }
//                            else {
//                                whereClause_or = whereClause_or + " " + "OR" + " " + wherePart;
//                            }
//                        }
//                        else if(conditionType.equalsIgnoreCase("spatial")){
//                            String columnName = conditionJobj.getString("columnName");
//                            String valueDataType = conditionJobj.getString("valueDataType");
//                            Object value = conditionJobj.get("value");
//                            String operator = conditionJobj.getString("operator");
//
//                            try {
//                                String textGeomwkt = "";
//                                if(valueDataType.equalsIgnoreCase("geojson")){
//                                    JSONObject geojsonGeom = (JSONObject) value;
//                                    Geometry jtsGeom = GeoJsonUtils.convertToJTSGeometry(geojsonGeom);
//                                    textGeomwkt=jtsGeom.toText();
//                                }
//                                else if(valueDataType.equalsIgnoreCase("jts")){
//                                    Geometry jtsGeom = (Geometry) value;
//                                    textGeomwkt = jtsGeom.toText();
//                                }
//                                else if(valueDataType.equalsIgnoreCase("milnga")){
//                                    mil.nga.sf.Geometry milngaGeom = (mil.nga.sf.Geometry) value;
//                                    textGeomwkt= milngaGeom.toString();
//                                }
//                                else if(valueDataType.equalsIgnoreCase("wkt")){
//                                    textGeomwkt=(String) value;
//                                }
//                                String spavalue = userDao.getTableName()+ ".the_geom" + ", " + "GeomFromText('" + textGeomwkt + "',4326)";
//                                wherePart = operator+"("+spavalue+")";
//                                if (whereClause_or.isEmpty()) {
//                                    whereClause_or = wherePart;
//                                }
//                                else {
//                                    whereClause_or = whereClause_or + " " + "OR" + " " + wherePart;
//                                }
//
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//
//                        }
//
//                    }
//
//                    if (whereClause_or.endsWith("OR")) {
//                        whereClause_or = whereClause_or.substring(0, whereClause_or.lastIndexOf("OR"));
//                    }
//                }
//
//
//                String whereClause_and = "";
//                if(ANDClausesArray!=null && ANDClausesArray.length()>0) {
//                    for (int i = 0; i < ANDClausesArray.length(); i++) {
//
//                        JSONObject conditionJobj = ANDClausesArray.getJSONObject(i);
//                        String conditionType = conditionJobj.getString("conditionType");
//
//
//                        String wherePart = "";
//                        if (conditionType.equalsIgnoreCase("attribute")) {
//                            String columnName = conditionJobj.getString("columnName");
//                            String valueDataType = conditionJobj.getString("valueDataType");
//                            Object value = conditionJobj.get("value");
//                            String operator = conditionJobj.getString("operator");
//                            if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
//                                wherePart = columnName + " " + operator + " '" + value + "' ";
//                            else
//                                wherePart = columnName + " " + operator + " " + value + " ";
//
//
//                            if (whereClause_and.isEmpty()) {
//                                whereClause_and = wherePart;
//                            }
//                            else {
//                                whereClause_and = whereClause_and + " " + "AND" + " " + wherePart;
//                            }
//
//                        }
//                        else if(conditionType.equalsIgnoreCase("spatial")){
//                            String columnName = conditionJobj.getString("columnName");
//                            String valueDataType = conditionJobj.getString("valueDataType");
//                            Object value = conditionJobj.get("value");
//                            String operator = conditionJobj.getString("operator");
//
//                            try {
//
//                                String textGeomwkt = "";
//                                if(valueDataType.equalsIgnoreCase("geojson")){
//                                    JSONObject geojsonGeom = (JSONObject) value;
//                                    Geometry jtsGeom = GeoJsonUtils.convertToJTSGeometry(geojsonGeom);
//                                    textGeomwkt=jtsGeom.toText();
//                                }
//                                else if(valueDataType.equalsIgnoreCase("jts")){
//                                    Geometry jtsGeom = (Geometry) value;
//                                    textGeomwkt = jtsGeom.toText();
//                                }
//                                else if(valueDataType.equalsIgnoreCase("milnga")){
//                                    mil.nga.sf.Geometry milngaGeom = (mil.nga.sf.Geometry) value;
//                                    textGeomwkt= milngaGeom.toString();
//                                }
//                                else if(valueDataType.equalsIgnoreCase("wkt")){
//                                    textGeomwkt=(String) value;
//                                }
//
//                                String spavalue = userDao.getTableName()+ ".the_geom" + ", " + "GeomFromText('" + textGeomwkt + "',4326)";
//
//
//                                wherePart = operator+"("+spavalue+")";
//                                if (whereClause_and.isEmpty()) {
//                                    whereClause_and = wherePart;
//                                }
//                                else {
//                                    whereClause_and = whereClause_and + " " + "OR" + " " + wherePart;
//                                }
//
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//
//                        }
//
//                    }
//
//                    if (whereClause_and.endsWith("AND")) {
//                        whereClause_and = whereClause_and.substring(0, whereClause_and.lastIndexOf("AND"));
//                    }
//                }
//
//                if(whereClause_or.isEmpty() && whereClause_and.isEmpty()){
//                    // do nothing
//                }else {
//
//                    whereClause = "";
//                    if (ANDorOR == null || ANDorOR.isEmpty()) {
//                        ANDorOR = "OR";
//                    }
//
//                    if (!whereClause_or.isEmpty()) {
//                        whereClause = "( " + whereClause_or + " ) ";
//                    }
//                    if (!whereClause_and.isEmpty()) {
//                        if (whereClause.isEmpty()) {
//                            whereClause = "( " + whereClause_and + " ) ";
//                        } else {
//                            whereClause += ANDorOR + " ( " + whereClause_and + " )";
//                        }
//                    }
//                }
//
//            }
//
////			if (columnNameValueConditionMap == null || columnNameValueConditionMap.isEmpty()) {
////				//userCoreResult = userDao.queryForAll();
////				whereClause=null;
////			} else {
////				//create where clause
////				whereClause="";
////				for (String columnName : columnNameValueConditionMap.keySet()) {
////
////					JSONObject coonditionJson = columnNameValueConditionMap.get(columnName);
////					String objectVal = coonditionJson.getString("value");
////
////					if (coonditionJson.has("isCheckEquals")) {
////						boolean isCheckEquals = coonditionJson.getBoolean("isCheckEquals");
////						String columnType = coonditionJson.getString("columnType");
////						if (columnType.equalsIgnoreCase("string")
////								|| columnType.equalsIgnoreCase("text")
////								|| columnType.equalsIgnoreCase("date")) {
////							if (isCheckEquals) {
////								whereClause += columnName + " = '" + objectVal + "' " + ANDorOR +" ";
////							} else {
////								whereClause += columnName + " IS NOT '" + objectVal + "' " + ANDorOR+" ";
////							}
////						} else {
////							if (isCheckEquals) {
////								whereClause += columnName + " = " + objectVal + " " + ANDorOR+" ";
////							} else {
////								whereClause += columnName + " IS NOT " + objectVal + " " + ANDorOR+" ";
////							}
////						}
////					}
////					else if(coonditionJson.has("operator")){
////						String columnType = coonditionJson.getString("columnType");
////						String operator = coonditionJson.getString("operator");
////						if (columnType.equalsIgnoreCase("string")
////								|| columnType.equalsIgnoreCase("text")
////								|| columnType.equalsIgnoreCase("date")) {
////							whereClause += columnName + " "+operator+" '" + objectVal + "' " + ANDorOR+" ";
////						} else {
////							whereClause += columnName + " "+operator+" " + objectVal + " " + ANDorOR+" ";
////						}
////					}
////					else if (coonditionJson.has("within")) {
////						boolean withIn = coonditionJson.getBoolean("within");
////						if (withIn) {
////							whereClause += "within(GeomFromText('POINT(" + objectVal + ")')," + columnName + ")";
////						}
////					}
////				}
////
////				if (whereClause.endsWith(ANDorOR+" ")) {
////					whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
////				}
////			}
//            String[] columnsreqd = null;
//            if (requiredColumnsList != null) {
//                if (!isDatasetOfAttributeType & queryGeometry) {
//                    FeatureDao featureDao = (FeatureDao) userDao;
//                    String geometryColumnName = featureDao.getGeometryColumnName();
//                    if (!requiredColumnsList.contains(geometryColumnName)) {
//                        requiredColumnsList.add(geometryColumnName);
//                    }
//                }
//                if (userDao.getTable().hasColumn(AppConstants.W9_METADATA) && !requiredColumnsList.contains(AppConstants.W9_METADATA)) {
//                    requiredColumnsList.add(AppConstants.W9_METADATA);
//                }
//                columnsreqd = requiredColumnsList.toArray(new String[requiredColumnsList.size()]);
//            } else {
//                columnsreqd = userDao.getColumnNames();
//            }
//            assert userDao != null;
//            if (limit < 0) {
//                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, null);
//            } else {
//                userCoreResult = userDao.query(distinct, columnsreqd, whereClause, null, null, null, null, String.valueOf(limit));
//            }
//            if (userCoreResult == null) {
//                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
//            }
//
//
//            JSONArray featuresJArray = new JSONArray();
//            if (!isDatasetOfAttributeType) {
//                try {
//                    //int count = 0;//temporary
//                    while (userCoreResult.moveToNext()/* && count<10*/) {
//                        //count++;//temp
//                        JSONObject featureJobj = new JSONObject();
//                        JSONObject properties = new JSONObject();
//                        try {
//                            FeatureRow featureRow = (FeatureRow) userCoreResult.getRow();
//                            if (featureRow.hasColumn(featureRow.getGeometryColumnName())) {
//                                GeoPackageGeometryData geometryData = featureRow.getGeometry();
//                                if (geometryData != null && !geometryData.isEmpty()) {
//                                    mil.nga.sf.Geometry geometry = geometryData.getGeometry();
//                                    Projection projection = ((FeatureDao) userDao).getProjection();
//                                    ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
//                                    geometryData = geometryData.transform(transform4326);
//
//                                    mil.nga.sf.Geometry geom4326 = null;
//                                    if (geometryData != null) {
//                                        geometryData.setSrsId(4326);
//                                        geom4326 = geometryData.getGeometry();
//                                    }
//                                    if (geom4326 != null) {
//                                        JSONObject geometryGeoJson = convertGeometryToGeoJson(geom4326);
//                                        //String wkt = geometryData.getWkt();
//                                        featureJobj.put("geometry", geometryGeoJson);
//                                    } else {
//                                        featureJobj.put("geometry", "{}");
//                                    }
//                                    featureJobj.put("geometryColumnName", featureRow.getGeometryColumnName());
//
//                                }
//                            }
//                            Object[] values = featureRow.getValues();
//                            FeatureColumns featureColumns = featureRow.getColumns();
//                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();
//
//                            for (int i = 0; i < ColumnNames.length; i++) {
//                                String columnName = ColumnNames[i];
//                                try {
//                                    Object value = values[i];
//                                    properties.put(columnName, value);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            featureJobj.put("properties", properties);
//                            featuresJArray.put(featureJobj);
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                } finally {
//                    userCoreResult.close();
//                }
//            } else {
//                try {
//                    while (userCoreResult.moveToNext()) {
//
//                        JSONObject featureJobj = new JSONObject();
//                        JSONObject properties = new JSONObject();
//                        try {
//                            AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
//                            Object[] values = attributesRow.getValues();
//                            AttributesColumns featureColumns = attributesRow.getColumns();
//                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();
//
//                            for (int i = 0; i < ColumnNames.length; i++) {
//
//                                String columnName = String.valueOf(ColumnNames[i]);
//                                try {
//                                    Object value = values[i];
//                                    properties.put(columnName, value);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            featureJobj.put("properties", properties);
//                            featuresJArray.put(featureJobj);
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
//                    }
//                } finally {
//                    userCoreResult.close();
//                }
//            }
//            JSONObject jsonObjectfeatures = new JSONObject();
//            jsonObjectfeatures.put("features", featuresJArray);
//            resultJObj.put("features", jsonObjectfeatures);
//            resultJObj.put("status", "success");
//        } catch (JSONException e) {
//            e.printStackTrace();
//            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
//            try {
//                resultJObj.put("status", "Failure");
//                resultJObj.put("message", "Reason unavailable");
//            } catch (Exception ee) {
//                ee.printStackTrace();
//            }
//        }/*finally {
//			if(geoPackage!=null){
//				geoPackage.close();
//			}
//		}*/
//
//
//        return resultJObj;
//
//    }

    public static JSONObject getFeaturesNEW(Context context, String datasourceName, String datasetName,
                                            String datasetType, List<String> requiredColumnsList, JSONArray ORClausesArray, JSONArray ANDClausesArray, String ANDorOR,
                                            boolean isDistinct, int startIndex, int limit, boolean queryGeometry, boolean transformGeometry) {
        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {

            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unvailable");
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;

            if ((ORClausesArray == null || ORClausesArray.length() == 0) &&
                    (ANDClausesArray == null || ANDClausesArray.length() == 0)) {

                userCoreResult = userDao.queryForAll();
            } else {

                String whereClause_or = "";
                if (ORClausesArray != null && ORClausesArray.length() > 0) {
                    for (int i = 0; i < ORClausesArray.length(); i++) {

                        JSONObject conditionJobj = ORClausesArray.getJSONObject(i);
                        String conditionType = conditionJobj.getString("conditionType");

                        String wherePart = "";
                        if (conditionType.equalsIgnoreCase("attribute")) {
                            String columnName = conditionJobj.getString("columnName");
                            String valueDataType = conditionJobj.getString("valueDataType");
                            Object value = conditionJobj.get("value");
                            String operator = conditionJobj.getString("operator");
                            if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
                                wherePart = columnName + " " + operator + " '" + value + "' ";
                            else
                                wherePart = columnName + " " + operator + " " + value + " ";


                            if (whereClause_or.isEmpty()) {
                                whereClause_or = wherePart;
                            } else {
                                whereClause_or = whereClause_or + " " + "OR" + " " + wherePart;
                            }

                        } else if (conditionType.equalsIgnoreCase("spatial")) {
                            String columnName = conditionJobj.getString("columnName");
                            String valueDataType = conditionJobj.getString("valueDataType");
                            Object value = conditionJobj.get("value");
                            String operator = conditionJobj.getString("operator");

                            try {
                                String textGeomwkt = "";
                                if (valueDataType.equalsIgnoreCase("geojson")) {
                                    JSONObject geojsonGeom = (JSONObject) value;
                                    Geometry jtsGeom = GeoJsonUtils.convertToJTSGeometry(geojsonGeom);
                                    textGeomwkt = jtsGeom.toText();
                                } else if (valueDataType.equalsIgnoreCase("jts")) {
                                    Geometry jtsGeom = (Geometry) value;
                                    textGeomwkt = jtsGeom.toText();
                                } else if (valueDataType.equalsIgnoreCase("milnga")) {
                                    mil.nga.sf.Geometry milngaGeom = (mil.nga.sf.Geometry) value;
                                    textGeomwkt = milngaGeom.toString();
                                } else if (valueDataType.equalsIgnoreCase("wkt")) {
                                    textGeomwkt = (String) value;
                                }

                                String spavalue = userDao.getTableName() + ".the_geom" + ", " + "GeomFromText('" + textGeomwkt + "',4326)";


                                wherePart = operator + "(" + spavalue + ")";
                                if (whereClause_or.isEmpty()) {
                                    whereClause_or = wherePart;
                                } else {
                                    whereClause_or = whereClause_or + " " + "OR" + " " + wherePart;
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (whereClause_or.endsWith("OR")) {
                        whereClause_or = whereClause_or.substring(0, whereClause_or.lastIndexOf("OR"));
                    }
                }

                String whereClause_and = "";
                if (ANDClausesArray != null && ANDClausesArray.length() > 0) {
                    for (int i = 0; i < ANDClausesArray.length(); i++) {

                        JSONObject conditionJobj = ANDClausesArray.getJSONObject(i);
                        String conditionType = conditionJobj.getString("conditionType");


                        String wherePart = "";
                        if (conditionType.equalsIgnoreCase("attribute")) {
                            String columnName = conditionJobj.getString("columnName");
                            String valueDataType = conditionJobj.getString("valueDataType");
                            Object value = conditionJobj.get("value");
                            String operator = conditionJobj.getString("operator");
                            if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
                                wherePart = columnName + " " + operator + " '" + value + "' ";
                            else
                                wherePart = columnName + " " + operator + " " + value + " ";


                            if (whereClause_and.isEmpty()) {
                                whereClause_and = wherePart;
                            } else {
                                whereClause_and = whereClause_and + " " + "AND" + " " + wherePart;
                            }

                        } else if (conditionType.equalsIgnoreCase("spatial")) {
                            String columnName = conditionJobj.getString("columnName");
                            String valueDataType = conditionJobj.getString("valueDataType");
                            Object value = conditionJobj.get("value");
                            String operator = conditionJobj.getString("operator");

                            try {

                                String textGeomwkt = "";
                                if (valueDataType.equalsIgnoreCase("geojson")) {
                                    JSONObject geojsonGeom = (JSONObject) value;
                                    Geometry jtsGeom = GeoJsonUtils.convertToJTSGeometry(geojsonGeom);
                                    textGeomwkt = jtsGeom.toText();
                                } else if (valueDataType.equalsIgnoreCase("jts")) {
                                    Geometry jtsGeom = (Geometry) value;
                                    textGeomwkt = jtsGeom.toText();
                                } else if (valueDataType.equalsIgnoreCase("milnga")) {
                                    mil.nga.sf.Geometry milngaGeom = (mil.nga.sf.Geometry) value;
                                    textGeomwkt = milngaGeom.toString();
                                } else if (valueDataType.equalsIgnoreCase("wkt")) {
                                    textGeomwkt = (String) value;
                                }

                                String spavalue = userDao.getTableName() + ".the_geom" + ", " + "GeomFromText('" + textGeomwkt + "',4326)";


                                wherePart = operator + "(" + spavalue + ")";
                                if (whereClause_and.isEmpty()) {
                                    whereClause_and = wherePart;
                                } else {
                                    whereClause_and = whereClause_and + " " + "OR" + " " + wherePart;
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    }

                    if (whereClause_and.endsWith("AND")) {
                        whereClause_and = whereClause_and.substring(0, whereClause_and.lastIndexOf("AND"));
                    }
                }

                if (whereClause_or.isEmpty() && whereClause_and.isEmpty()) {
                    userCoreResult = userDao.queryForAll();
                } else {

                    String whereClause = "";
                    if (ANDorOR == null || ANDorOR.isEmpty()) {
                        ANDorOR = "OR";
                    }

                    if (!whereClause_or.isEmpty()) {
                        whereClause = "( " + whereClause_or + " ) ";
                    }
                    if (!whereClause_and.isEmpty()) {
                        if (whereClause.isEmpty()) {
                            whereClause = "( " + whereClause_and + " ) ";
                        } else {
                            whereClause += ANDorOR + " ( " + whereClause_and + " )";
                        }
                    }


                    String[] columnsreqd = null;
                    if (requiredColumnsList != null && requiredColumnsList.size() > 0) {
                        if (!isDatasetOfAttributeType & queryGeometry) {
                            FeatureDao featureDao = (FeatureDao) userDao;
                            String geometryColumnName = featureDao.getGeometryColumnName();
                            if (!requiredColumnsList.contains(geometryColumnName)) {
                                requiredColumnsList.add(geometryColumnName);
                            }
                        }
                        if (userDao.getTable().hasColumn(AppConstants.W9_METADATA) && !requiredColumnsList.contains(AppConstants.W9_METADATA)) {
                            requiredColumnsList.add(AppConstants.W9_METADATA);
                        }
                        columnsreqd = requiredColumnsList.toArray(new String[requiredColumnsList.size()]);
                    } else {
                        columnsreqd = userDao.getColumnNames();
                    }
                    assert userDao != null;
                    userCoreResult = userDao.query(isDistinct, columnsreqd, whereClause, null, null, null, null, String.valueOf(limit));

                }
            }
            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }
            JSONArray featuresJArray = new JSONArray();
            if (!isDatasetOfAttributeType) {
                try {
                    //int count = 0;//temporary
                    while (userCoreResult.moveToNext()/* && count<10*/) {
                        //count++;//temp
                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            FeatureRow featureRow = (FeatureRow) userCoreResult.getRow();
                            if (featureRow.hasColumn(featureRow.getGeometryColumnName())) {
                                GeoPackageGeometryData geometryData = featureRow.getGeometry();
                                if (geometryData != null && !geometryData.isEmpty()) {
                                    if (transformGeometry) {
                                        mil.nga.sf.Geometry geometry = geometryData.getGeometry();
                                        Projection projection = ((FeatureDao) userDao).getProjection();
                                        ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                                        geometryData = geometryData.transform(transform4326);

                                        mil.nga.sf.Geometry geom4326 = null;
                                        if (geometryData != null) {
                                            geometryData.setSrsId(4326);
                                            geom4326 = geometryData.getGeometry();
                                        }
                                        if (geom4326 != null) {
                                            JSONObject geometryGeoJson = convertGeometryToGeoJson(geom4326);
                                            //String wkt = geometryData.getWkt();
                                            featureJobj.put("geometry", geometryGeoJson);
                                        } else {
                                            featureJobj.put("geometry", "{}");
                                        }
                                    } else {
                                        mil.nga.sf.Geometry geom = null;
                                        if (geometryData != null) {
                                            geom = geometryData.getGeometry();
                                        }
                                        if (geom != null) {
                                            JSONObject geometryGeoJson = convertGeometryToGeoJson(geom);
                                            //String wkt = geometryData.getWkt();
                                            featureJobj.put("geometry", geometryGeoJson);
                                        } else {
                                            featureJobj.put("geometry", "{}");
                                        }
                                    }
                                    featureJobj.put("geometryColumnName", featureRow.getGeometryColumnName());
                                }
                            }
                            Object[] values = featureRow.getValues();
                            FeatureColumns featureColumns = featureRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {
                                String columnName = ColumnNames[i];
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            } else {
                try {
                    while (userCoreResult.moveToNext()) {

                        JSONObject featureJobj = new JSONObject();
                        JSONObject properties = new JSONObject();
                        try {
                            AttributesRow attributesRow = (AttributesRow) userCoreResult.getRow();
                            Object[] values = attributesRow.getValues();
                            AttributesColumns featureColumns = attributesRow.getColumns();
                            String[] ColumnNames = (String[]) featureColumns.getColumnNames();

                            for (int i = 0; i < ColumnNames.length; i++) {

                                String columnName = String.valueOf(ColumnNames[i]);
                                try {
                                    Object value = values[i];
                                    properties.put(columnName, value);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            featureJobj.put("properties", properties);
                            featuresJArray.put(featureJobj);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    userCoreResult.close();
                }
            }
            JSONObject jsonObjectfeatures = new JSONObject();
            jsonObjectfeatures.put("features", featuresJArray);
            resultJObj.put("features", jsonObjectfeatures);
            resultJObj.put("status", "success");
        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }
        return resultJObj;
    }


    public static JSONObject insertFeatures(Context context, String datasourceName, String datasetName,
                                            String datasetType, String w9IdPropertyName,
                                            JSONArray dataJSONArray,
                                            ReveloLogger geopackageRWLogger) {

        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unvailable");

            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }

            if (dataJSONArray == null || dataJSONArray.length() == 0) {
                SystemUtils.logAndReturnMessage("failure", "No data found for insertion");
            }
            if (datasetType.equalsIgnoreCase("spatial")) {
                FeatureRow row = (FeatureRow) userDao.newRow();
                for (int i = 0; i < dataJSONArray.length(); i++) {
                    JSONObject dataObj = dataJSONArray.getJSONObject(i);
                    JSONArray attributesJarray = dataObj.getJSONArray("attributes");

                    for (int j = 0; j < attributesJarray.length(); j++) {
                        JSONObject attributeJObj = attributesJarray.getJSONObject(j);
                        String key = attributeJObj.getString("name");
                        int columnType = row.getRowColumnType(key);

                        Object value = attributeJObj.get("value");
                        row.setValue(key, value);
                    }

                    if (dataObj.has("geometry")) {

                        JSONObject geoJson = dataObj.getJSONObject("geometry");

                        Geometry jtsGeom = GeoJsonUtils.convertToJTSGeometry(geoJson);
                        if (jtsGeom != null) {
                            GeoPackageGeometryData oldGeometryData = new GeoPackageGeometryData(jtsGeom.getSRID());
                            oldGeometryData.setGeometryFromWkt(jtsGeom.toText());
                            oldGeometryData.getOrBuildEnvelope();
                            Projection projection = userDao.getProjection();
                            ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                            ProjectionTransform transform = transform4326.getInverseTransformation();

                            GeoPackageGeometryData newGeomData = oldGeometryData.transform(transform);
                            try {
                                FeatureDao dao = (FeatureDao) userDao;
                                long srsid = dao.getSrsId();
                                newGeomData.setSrsId(Integer.parseInt(String.valueOf(srsid)));
                                newGeomData.getOrBuildEnvelope();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            row.setGeometry(newGeomData);
                        }
                    }
                    userDao.insert(row);
                }
                resultJObj.put("status", "success");
            } else {
                UserCoreRow row = userDao.newRow();
                for (int i = 0; i < dataJSONArray.length(); i++) {
                    JSONObject dataObj = dataJSONArray.getJSONObject(i);
                    JSONArray attributesJarray = dataObj.getJSONArray("attributes");

                    for (int j = 0; j < attributesJarray.length(); j++) {
                        JSONObject attributeJObj = attributesJarray.getJSONObject(j);
                        String key = attributeJObj.getString("name");
                        Object value = attributeJObj.get("value");
                        row.setValue(key, value);
                    }
                    userDao.insert((AttributesRow) row);
                }
                resultJObj.put("status", "success");
            }
            GeoPackageManagerAgent.exportGeopackage(context, datasourceName);   // export database here.
        } catch (Exception e) {
            e.printStackTrace();
        }/*finally {
			if (geoPackage != null)
				geoPackage.close();
		}*/

        return resultJObj;
    }


    /*
     * condition will be jsonarray -
     * array of condtition jobjs -
     * {
     * 	conditionType :  "spatial"/"attribute"
     * 	columnName : the_geom/name
     * 	value: geom / site
     * 	valueDataType: string/geometry
     * 	operator: within / =
     * }
     * */
    public static JSONObject deleteFeatures(String datasourceName, String datasetName,
                                            String datasetType, JSONArray whereClauseArray, String ANDorOR,
                                            ReveloLogger geopackageRWLogger, Context context) {

        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unavailable");

            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            if (whereClauseArray == null || whereClauseArray.length() == 0) {
                SystemUtils.logAndReturnMessage("failure", "attempt to delete all features");
            } else {
                String whereClause = "";
                for (int i = 0; i < whereClauseArray.length(); i++) {

                    JSONObject conditionJobj = whereClauseArray.getJSONObject(i);
                    String conditionType = conditionJobj.getString("conditionType");
                    String columnName = conditionJobj.getString("columnName");
                    String valueDataType = conditionJobj.getString("valueDataType");
                    Object value = conditionJobj.get("value");
                    String operator = conditionJobj.getString("operator");

                    String wherePart = "";
                    if (conditionType.equalsIgnoreCase("attribute")) {
                        if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
                            wherePart = columnName + " " + operator + " '" + value + "' ";
                        else
                            wherePart = columnName + " " + operator + " " + value + " ";


                        if (whereClause.isEmpty()) {
                            whereClause = wherePart;
                        } else {
                            whereClause = whereClause + " " + ANDorOR + " " + wherePart;
                        }

                    } else {
                        if (valueDataType.equalsIgnoreCase("geometry")) {
                            Geometry jtsGeom = (Geometry) value;
                        }
                    }

                }
                if (whereClause.endsWith(ANDorOR)) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
                }

                assert userDao != null;
                UserCoreResult resultantRecords = userDao.query(whereClause);
                int numRecords = resultantRecords.getCount();
                if (numRecords > 0) {
                    int result = userDao.delete(whereClause, null);
                    if (result > 0) {
                        resultJObj.put("status", "success");
                    } else {
                        resultJObj.put("status", "failure");
                    }
                } else {
                    resultJObj.put("status", "success");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        } /*finally {
			if (geoPackage != null)
				geoPackage.close();
		}*/


        return resultJObj;
    }

    public static JSONObject deleteFeatures(String datasourceName, String datasetName,
                                            String datasetType, String whereclause,
                                            ReveloLogger geopackageRWLogger, Context context) {

        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unvailable");
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            if (whereclause == null || whereclause.isEmpty()) {
                SystemUtils.logAndReturnMessage("failure", "attempt to delete all features");
            }
            assert userDao != null;
            int result = userDao.delete(whereclause, null);

            if (result == 1) {
                resultJObj.put("status", "success");
            } else {
                resultJObj.put("status", "failure");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        } /*finally {
			if (geoPackage != null)
				geoPackage.close();
		}*/


        return resultJObj;
    }

    public static int getFeatureCount(Context context, String datasourceName, String datasetName, String datasetType, String whereclause, ReveloLogger geopackageRWLogger) {
        int featurecount = 0;
        GeoPackage geoPackage = null;
        try {
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                return 0;
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;
            if (whereclause == null || whereclause.isEmpty()) {
                featurecount = userDao.count();
            } else {
                featurecount = userDao.count(whereclause);
            }
            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if (geoPackage != null)
				geoPackage.close();
		}*/


        return featurecount;
    }

    public static int getFeatureCountNEW(Context context, String datasourceName, String datasetName, String datasetType,
                                         JSONArray ORClausesArray, JSONArray ANDClausesArray, String ANDorOR, ReveloLogger geopackageRWLogger) {
        int featurecount = 0;
        GeoPackage geoPackage = null;

        try {
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                return 0;
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }

            UserCoreResult userCoreResult = null;

            if ((ORClausesArray == null || ORClausesArray.length() == 0) &&
                    (ANDClausesArray == null || ANDClausesArray.length() == 0)) {

                featurecount = userDao.count();
            } else {
                String whereClause_or = "";
                for (int i = 0; i < ORClausesArray.length(); i++) {

                    JSONObject conditionJobj = ORClausesArray.getJSONObject(i);
                    String conditionType = conditionJobj.getString("conditionType");
                    String columnName = conditionJobj.getString("columnName");
                    String valueDataType = conditionJobj.getString("valueDataType");
                    Object value = conditionJobj.get("value");
                    String operator = conditionJobj.getString("operator");

                    String wherePart = "";
                    if (conditionType.equalsIgnoreCase("attribute")) {
                        if (valueDataType.equalsIgnoreCase("string")
                                || valueDataType.equalsIgnoreCase("text"))
                            wherePart = columnName + " " + operator + " '" + value + "' ";
                        else
                            wherePart = columnName + " " + operator + " " + value + " ";


                        if (whereClause_or.isEmpty()) {
                            whereClause_or = wherePart;
                        } else {
                            whereClause_or = whereClause_or + " " + "OR" + " " + wherePart;
                        }

                    } else {
                        if (valueDataType.equalsIgnoreCase("geometry")) {
                            Geometry jtsGeom = (Geometry) value;
                        }

                    }

                }

                if (whereClause_or.endsWith("OR")) {
                    whereClause_or = whereClause_or.substring(0, whereClause_or.lastIndexOf("OR"));
                }

                String whereClause_and = "";
                for (int i = 0; i < ORClausesArray.length(); i++) {

                    JSONObject conditionJobj = ORClausesArray.getJSONObject(i);
                    String conditionType = conditionJobj.getString("conditionType");
                    String columnName = conditionJobj.getString("columnName");
                    String valueDataType = conditionJobj.getString("valueDataType");
                    Object value = conditionJobj.get("value");
                    String operator = conditionJobj.getString("operator");

                    String wherePart = "";
                    if (conditionType.equalsIgnoreCase("attribute")) {
                        if (valueDataType.equalsIgnoreCase("string")
                                || valueDataType.equalsIgnoreCase("text"))
                            wherePart = columnName + " " + operator + " '" + value + "' ";
                        else
                            wherePart = columnName + " " + operator + " " + value + " ";


                        if (whereClause_and.isEmpty()) {
                            whereClause_and = wherePart;
                        } else {
                            whereClause_and = whereClause_and + " " + "AND" + " " + wherePart;
                        }

                    } else {
                        if (valueDataType.equalsIgnoreCase("geometry")) {
                            Geometry jtsGeom = (Geometry) value;
                        }

                    }

                }

                if (whereClause_and.endsWith("AND")) {
                    whereClause_and = whereClause_and.substring(0, whereClause_and.lastIndexOf("AND"));
                }


                if (whereClause_or.isEmpty() && whereClause_and.isEmpty()) {
                    featurecount = userDao.count();
                } else {

                    String whereClause = "";
                    if (ANDorOR == null || ANDorOR.isEmpty()) {
                        ANDorOR = "OR";
                    }
                    if (!whereClause_or.isEmpty() && !whereClause_and.isEmpty()) {
                        whereClause = "( " + whereClause_or + " ) " + ANDorOR + " ( " + whereClause_and + " )";
                    }
                    String[] columnsreqd = null;
                    assert userDao != null;
                    featurecount = userDao.count(whereClause);

                }

            }

            if (featurecount == -1) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }
        return featurecount;
    }

    public static int getFeatureCount(Context context, String datasourceName, String datasetName, String datasetType,
                                      JSONArray whereClauseArray, String ANDorOR, ReveloLogger geopackageRWLogger) {
        int featurecount = 0;
        GeoPackage geoPackage = null;

        try {
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                return 0;
            }

            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }
//{"value":"Maharashtra","isCheckEquals":true}
            UserCoreResult userCoreResult = null;
            if (whereClauseArray == null || whereClauseArray.length() == 0) {
                featurecount = userDao.count();
            } else {
                String whereClause = "";
                for (int i = 0; i < whereClauseArray.length(); i++) {

                    JSONObject conditionJobj = whereClauseArray.getJSONObject(i);
                    String conditionType = conditionJobj.getString("conditionType");
                    String columnName = conditionJobj.getString("columnName");
                    String valueDataType = conditionJobj.getString("valueDataType");
                    Object value = conditionJobj.get("value");
                    String operator = conditionJobj.getString("operator");

                    String wherePart = "";
                    if (conditionType.equalsIgnoreCase("attribute")) {
                        if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
                            wherePart = columnName + " " + operator + " '" + value + "' ";
                        else
                            wherePart = columnName + " " + operator + " " + value + " ";


                        if (whereClause.isEmpty()) {
                            whereClause = wherePart;
                        } else {
                            whereClause = whereClause + " " + ANDorOR + " " + wherePart;
                        }

                    } else {
                        if (valueDataType.equalsIgnoreCase("geometry")) {
                            Geometry jtsGeom = (Geometry) value;
                        }
                    }

                }
                if (whereClause.endsWith(ANDorOR)) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
                }

                assert userDao != null;
                featurecount = userDao.count(whereClause);


            }

            if (userCoreResult == null) {
                SystemUtils.logAndReturnMessage("failure", "No features found in dataset " + datasetName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if (geoPackage != null)
				geoPackage.close();
		}*/

        return featurecount;
    }

    public static JSONObject updateFeatures(Context context, String datasourceName, String datasetName, String datasetType,
                                            JSONArray dataJSONArray, JSONArray whereClauseArray, String ANDorOR, ReveloLogger geopackageRWLogger) {

        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Update failed. Reason unavailable");
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }
            String whereClause = "";
            if (whereClauseArray != null && whereClauseArray.length() != 0) {
                for (int i = 0; i < whereClauseArray.length(); i++) {

                    JSONObject conditionJobj = whereClauseArray.getJSONObject(i);
                    String conditionType = conditionJobj.getString("conditionType");
                    String columnName = conditionJobj.getString("columnName");
                    String valueDataType = conditionJobj.getString("valueDataType");
                    Object value = conditionJobj.get("value");
                    String operator = conditionJobj.getString("operator");

                    String wherePart = "";
                    if (conditionType.equalsIgnoreCase("attribute")) {
                        if (valueDataType.equalsIgnoreCase("string") || valueDataType.equalsIgnoreCase("text"))
                            wherePart = columnName + " " + operator + " '" + value + "' ";
                        else
                            wherePart = columnName + " " + operator + " " + value + " ";


                        if (whereClause.isEmpty()) {
                            whereClause = wherePart;
                        } else {
                            whereClause = whereClause + " " + ANDorOR + " " + wherePart;
                        }

                    } else {
                        if (valueDataType.equalsIgnoreCase("geometry")) {
                            Geometry jtsGeom = (Geometry) value;
                        }
                    }

                }
                if (whereClause.endsWith(ANDorOR)) {
                    whereClause = whereClause.substring(0, whereClause.lastIndexOf(ANDorOR));
                }
            }
            return updateFeatures(context, datasourceName, datasetName, datasetType, dataJSONArray, whereClause, geopackageRWLogger);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                resultJObj.put("status", "Failure");
                resultJObj.put("message", "Update failed. Reason: exception- " + e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }/*finally {
			if (geoPackage != null)
			geoPackage.close();
		}*/

        return resultJObj;
    }


    public static JSONObject updateFeatures(Context context, String datasourceName, String datasetName, String datasetType,
                                            JSONArray dataJSONArray, String whereClause, ReveloLogger geopackageRWLogger) {

        JSONObject resultJObj = new JSONObject();
        GeoPackage geoPackage = null;
        try {
            resultJObj.put("status", "Failure");
            resultJObj.put("message", "Reason unavailable");
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }
            UserDao userDao = null;
            boolean isDatasetOfAttributeType = false;
            if (datasetType.equalsIgnoreCase("attribute") || datasetType.equalsIgnoreCase("table")) {
                userDao = geoPackage.getAttributesDao(datasetName);
                isDatasetOfAttributeType = true;
            } else {
                userDao = geoPackage.getFeatureDao(datasetName);
            }

            if (userDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get DAO for dataset " + datasetName);
            }

            if (dataJSONArray == null || dataJSONArray.length() == 0) {
                SystemUtils.logAndReturnMessage("failure", "No data found for insertion");
            }

            if (datasetType.equalsIgnoreCase("spatial")) {
                FeatureDao featureDao = (FeatureDao) userDao;
                FeatureCursor featureCursor = featureDao.query(whereClause);
                if (featureCursor.moveToNext()) {
                    FeatureRow row = featureCursor.getRow();
                    if (row != null) {
                        for (int i = 0; i < dataJSONArray.length(); i++) {
                            JSONObject dataObj = dataJSONArray.getJSONObject(i);
                            JSONArray attributesJarray = dataObj.getJSONArray("attributes");

                            for (int j = 0; j < attributesJarray.length(); j++) {
                                JSONObject attributeJObj = attributesJarray.getJSONObject(j);
                                String key = attributeJObj.getString("name");
                                Object value = attributeJObj.get("value");
                                row.setValue(key, value);
                            }

                            if (dataObj.has("geometry")) {
                                JSONObject geometryGeoJson = dataObj.getJSONObject("geometry");
                                Geometry jtsGeom = GeoJsonUtils.convertToJTSGeometry(geometryGeoJson);
                                GeoPackageGeometryData oldGeometryData = new GeoPackageGeometryData(jtsGeom.getSRID());
                                oldGeometryData.setGeometryFromWkt(jtsGeom.toText());
                                oldGeometryData.getOrBuildEnvelope();
                                Projection projection = userDao.getProjection();
                                ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                                ProjectionTransform transform = transform4326.getInverseTransformation();

                                GeoPackageGeometryData newGeomData = oldGeometryData.transform(transform);
                                try {
                                    FeatureDao dao = (FeatureDao) userDao;
                                    long srsid = dao.getSrsId();
                                    newGeomData.setSrsId(Integer.parseInt(String.valueOf(srsid)));
                                    newGeomData.getOrBuildEnvelope();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                row.setGeometry(newGeomData);
                            }
                            int result = userDao.update(row);
                            if (result == 0) {
                                resultJObj.put("status", "Failure");
                                resultJObj.put("message", "Update failed. Reason: Feature not found");
                            } else {
                                resultJObj.put("status", "Success");
                                resultJObj.put("message", "Reason unavailable");
                            }
                        }
                    }
                }
            } else {
                AttributesCursor attributesCursor = (AttributesCursor) userDao.query(whereClause);
                if (attributesCursor.moveToNext()) {
                    AttributesRow row = (AttributesRow) attributesCursor.getRow();
                    if (row != null) {
                        for (int i = 0; i < dataJSONArray.length(); i++) {
                            JSONObject dataObj = dataJSONArray.getJSONObject(i);
                            JSONArray attributesJarray = dataObj.getJSONArray("attributes");

                            for (int j = 0; j < attributesJarray.length(); j++) {
                                JSONObject attributeJObj = attributesJarray.getJSONObject(j);
                                String key = attributeJObj.getString("name");
                                Object value = attributeJObj.get("value");
                                row.setValue(key, value);
                            }
                            int result = userDao.update((AttributesRow) row);
                            if (result == 0) {
                                resultJObj.put("status", "Failure");
                                resultJObj.put("message", "Update failed. Reason: Feature not found");
                            } else {
                                resultJObj.put("status", "Success");
                                resultJObj.put("message", "Reason unavailable");
                            }
                        }
                    }
                }

            }
            GeoPackageManagerAgent.exportGeopackage(context, datasourceName);  // export database here.
        } catch (Exception e) {
            e.printStackTrace();
            try {
                resultJObj.put("status", "Failure");
                resultJObj.put("message", "Update failed. Reason: exception- " + e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        }/*finally {
			if (geoPackage != null)
				geoPackage.close();
		}*/

        return resultJObj;
    }


/*
	public static JSONObject addEntry(JSONObject propertiesJSON, SimpleFeatureSource simpleFeatureSource, Filter filter, ReveloLogger logger) {
		GeoPackage geoPackage = null;
		try {
			JSONObject responseJSON = new JSONObject();
			responseJSON.put("status", "failure");

			if(propertiesJSON.has("dbPath")) {
				String dbPath = propertiesJSON.getString("dbPath");
				if(dbPath.trim().isEmpty()) {
					responseJSON.put("message", "Path to geopackage file is empty.");
					return responseJSON;
				}
				File gpkgFile = new File(propertiesJSON.getString("dbPath"));
				if(!gpkgFile.exists()) {
					return SystemUtils.logAndReturnErrorMessage("GeoPackage does not exist.", null);
				}

				if(simpleFeatureSource == null) {
					return SystemUtils.logAndReturnErrorMessage("Simple feature source is null.", null);
				}

				geoPackage = new GeoPackage(gpkgFile);
				geoPackage.init();

FeatureEntry featureEntry = new FeatureEntry();
				featureEntry.setDataType(DataType.Feature);


				Entry entry = new Entry();
								entry.setDescription("myTable");

								geoPackage.add(entry, simpleFeatureSource, filter);


try (SimpleFeatureReader r = geopkg.reader(entry, null, null)) {
					while (r.hasNext()) {
						SimpleFeature sf = r.next();
						System.out.println(sf.getID());
					}
				}


				responseJSON.put("status", "success");
			}
			else {
				responseJSON.put("message", "No path to geopackage file provided.");
			}

			return responseJSON;
		} catch (JSONException | IOException e) {
			return SystemUtils.logAndReturnErrorMessage(e.getMessage(), e);
		}
		finally {
			if(geoPackage != null) {
				geoPackage.close();
			}
		}
	}

*/


    //end region GeoPackage

    public static List<String> getListOfExistingTables_contents(Context context, String datasourceName) {
        List<String> entitiesList = new ArrayList<>();
        GeoPackage geoPackage = null;
        try {
            geoPackage = GeoPackageManagerAgent.getGeoPackage(context, datasourceName);
            if (geoPackage == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get geopackage " + datasourceName);
            }

            ContentsDao contentsDao = geoPackage.getContentsDao();

            if (contentsDao == null) {
                SystemUtils.logAndReturnMessage("failure", "Could not get contents DAO for datasource " + datasourceName);
            }
            String[] datatypes = {"features", "attributes"};
            entitiesList = contentsDao.getTables(datatypes);

        } catch (SQLException e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage("Could not fetch features", e);
        }/*finally {
			if(geoPackage!=null)
				geoPackage.close();
		}*/
        return entitiesList;
    }

    //todo CHECK
    public static JSONObject convertGeometryToGeoJson(mil.nga.sf.Geometry geometry) {
        JSONObject geoJsonGeometry = new JSONObject();
        try {
            geoJsonGeometry.put("type", "feature");
            JSONArray featuresArray = new JSONArray();
            JSONObject featureJsonObj = new JSONObject();
            JSONObject featureProperties = new JSONObject();
            featureJsonObj.put("properties", featureProperties);

            //create jsonobject geometry
            JSONObject geometryJson = new JSONObject();

            GeometryType geometryType = geometry.getGeometryType();
            String geometryTypeStr = geometryType.getName();

            if (geometryType.getName().equalsIgnoreCase("polygon")) {
                geometryTypeStr = "MultiPolygon";
            }
            if (geometryType.getName().equalsIgnoreCase("polyline")
                    || geometryType.getName().equalsIgnoreCase("linestring")
                    || geometryType.getName().equalsIgnoreCase("multiLineString")) {
                geometryTypeStr = "MultiLineString";
            }
            geometryJson.put("type", geometryTypeStr);
            if (geometryType.getName().equalsIgnoreCase("polygon")) {
                Polygon polygon = (Polygon) geometry;
                JSONArray polygonJsonArray = getPolygonJsonArray(polygon);
                JSONArray multipolygonJsonArray = new JSONArray();
                try {
                    multipolygonJsonArray.put(polygonJsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                geometryJson.put("coordinates", multipolygonJsonArray);
            } else if (geometryType.getName().equalsIgnoreCase("multipolygon")) {
                MultiPolygon multiPolygon = (MultiPolygon) geometry;
                JSONArray multipolygonJsonArray = new JSONArray();
                try {
                    List<Polygon> polygonList = multiPolygon.getPolygons();
                    for (Polygon polygon : polygonList) {
                        JSONArray polygonJsonArray = getPolygonJsonArray(polygon);
                        multipolygonJsonArray.put(polygonJsonArray);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                geometryJson.put("coordinates", multipolygonJsonArray);
            } else if (geometryType.getName().equalsIgnoreCase("linestring")) {
                LineString lineString = (LineString) geometry;
                JSONArray lineStringJsonArray = getLineStringJsonArray(lineString);
                JSONArray polylineJsonArray = new JSONArray();
                try {
                    polylineJsonArray.put(lineStringJsonArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                geometryJson.put("coordinates", polylineJsonArray);
            } else if (geometryType.getName().equalsIgnoreCase("multilinestring")) {
                MultiLineString multiLineString = (MultiLineString) geometry;
                JSONArray multiLineStringJsonArray = new JSONArray();
                try {
                    List<LineString> lineStringList = multiLineString.getLineStrings();
                    for (LineString lineString : lineStringList) {
                        JSONArray lineStringJsonArray = getLineStringJsonArray(lineString);
                        multiLineStringJsonArray.put(lineStringJsonArray);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                geometryJson.put("coordinates", multiLineStringJsonArray);
            } else if (geometryType.getName().equalsIgnoreCase("point")) {
                Point point = (Point) geometry;
                JSONArray pointJsonArray = new JSONArray();
                try {
                    pointJsonArray = getPointJsonArray(point);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                geometryJson.put("coordinates", pointJsonArray);
            }


            featureJsonObj.put("geometry", geometryJson);
            featuresArray.put(featureJsonObj);
            geoJsonGeometry.put("features", featuresArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return geoJsonGeometry;
    }

    //todo CHECK
    private static JSONArray getPointJsonArray(Point point) {
        JSONArray pointJSONArray = new JSONArray();
        try {
            pointJSONArray.put(point.getX());
            pointJSONArray.put(point.getY());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pointJSONArray;
    }

    //todo CHECK
    private static JSONArray getLineStringJsonArray(LineString lineString) {
        JSONArray linestringJSONArray = new JSONArray();
        try {

            for (int i = 0; i < lineString.numPoints(); i++) {
                JSONArray pointJSONArray = getPointJsonArray(lineString.getPoint(i));
                linestringJSONArray.put(pointJSONArray);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return linestringJSONArray;
    }

    //todo CHECK
    private static JSONArray getPolygonJsonArray(Polygon polygon) {
        JSONArray polygonJSONArray = new JSONArray();
        try {
            List<LineString> rings = polygon.getRings();
            if (!rings.isEmpty()) {

                for (LineString lineString : rings) {
                    JSONArray lineStringJsonArray = new JSONArray();
                    for (int i = 0; i < lineString.numPoints(); i++) {
                        JSONArray pointJSONArray = getPointJsonArray(lineString.getPoint(i));
                        lineStringJsonArray.put(pointJSONArray);
                    }
                    polygonJSONArray.put(lineStringJsonArray);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return polygonJSONArray;
    }


    private JSONObject getDatasetInfo(String name, String type, String geometryType, String w9IdPropertyName) {

        JSONObject datasetInfo = new JSONObject();
        try {
            datasetInfo.put("datasetName", name);
            datasetInfo.put("datasetType", type);
            datasetInfo.put("geometryType", geometryType);
            datasetInfo.put("idPropertyName", w9IdPropertyName);

        } catch (JSONException e) {
            ReveloLogger.error(className, "getDatasetInfo", "error initializing getDatasetInfo json for dataset : " + name + ". Exception -" + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return datasetInfo;
    }

}