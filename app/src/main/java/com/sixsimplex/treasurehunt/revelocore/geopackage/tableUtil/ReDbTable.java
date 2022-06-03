package com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.sixsimplex.treasurehunt.revelocore.data.GeoJsonUtils;
import com.sixsimplex.treasurehunt.revelocore.data.SpatialiteInterface;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageManagerAgent;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageRWAgent;
import com.sixsimplex.treasurehunt.revelocore.layer.GeometryEngine;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.OrgBoundaryConceptModel;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jsqlite.Callback;
import jsqlite.Constants;
import jsqlite.Database;
import jsqlite.Stmt;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionTransform;

public class ReDbTable {

    private static String RE_GP_TABLE_NAME = "w9obre";
    private static final String className = "ReDbTable";
    private static ProjectionTransform transform4326;
    private static FeatureDao reGpFeatureDao;
//    private static mil.nga.sf.Geometry cacheGeom;
    private static Map<String, Object> cacheJurisdiction;
    private static Geometry cachePolygon;
    private static Handler mHandler = new Handler(Looper.getMainLooper());
    private static Map<String, Object> jurisdictionValuesMap = null;
    private static Geometry userJurisdictionGeom = null;
    private static boolean stopThreadVar = false;
    private static Database redb;
    private static boolean isRedbOpen;
    private static JSONObject getDatasetInfo() {

        JSONObject datasetInfo = new JSONObject();
        try {
                ReveloLogger.info(className, "getdatasetinfo", "getting dataset info for "+RE_GP_TABLE_NAME);
                datasetInfo.put("datasetName", RE_GP_TABLE_NAME);
                datasetInfo.put("datasetType", "spatial");
                datasetInfo.put("geometryType", "MultiPolygon");

            //datasetInfo.put("datasetType", featureLayer.getType());
            //datasetInfo.put("geometryType", featureLayer.getGeometryType());
            datasetInfo.put("idPropertyName","fid");
            datasetInfo.put("w9IdPropertyName", "fid");
        } catch (JSONException e) {
            ReveloLogger.error(className, "getDatasetInfo", "error initializing getDatasetInfo json for dataset: " + RE_GP_TABLE_NAME + ". Exception - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return datasetInfo;
    }


    private static FeatureDao getReGpFeatureDao(Context context) {

        if (reGpFeatureDao == null) {
            try {
                GeoPackage reGeoPackage = GeoPackageManagerAgent.getReGeoPackage(context, DbRelatedConstants.getPropertiesJsonForREGpkg(context));
                reGpFeatureDao = reGeoPackage.getFeatureDao(RE_GP_TABLE_NAME);

                Projection projection = reGpFeatureDao.getProjection();
                transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
            }catch (Exception e){
                e.printStackTrace();
            }

            try {
                String queryinit = "SELECT InitSpatialMetaData();";

                Database gdb = SpatialiteInterface.openGdb(context,AppFolderStructure.getReGpFilePath(context));
                if (gdb != null) {
                    gdb.exec("BEGIN;", null);
                    gdb.exec(queryinit, null);
                    gdb.exec("COMMIT;", null);
                    gdb.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "create Spatialite Specific Tables", e.getMessage());
            }
            try {
                String querygeomcolumn = "SELECT EnableGpkgAmphibiousMode();";

                Database gdb = SpatialiteInterface.openGdb(context, AppFolderStructure.getReGpFilePath(context));
                if (gdb != null) {
                    gdb.exec("BEGIN;", null);
                    gdb.exec(querygeomcolumn, null);
                    gdb.exec("COMMIT;", null);
                    gdb.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "create Spatialite Specific Tables", e.getMessage());
            }
            try {
                String querypath= "SELECT PROJ_SetDatabasePath('"+AppFolderStructure.getReGpFilePath(context)+"');";

                Database gdb = SpatialiteInterface.openGdb(context,AppFolderStructure.getReGpFilePath(context));
                if (gdb != null) {
                    gdb.exec("BEGIN;", null);
                    gdb.exec(querypath, null);
                    gdb.exec("COMMIT;", null);
                    gdb.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "create Spatialite Specific Tables", e.getMessage());
            }
            try {
                String querypath= "SELECT HasProj();";
                boolean hasproj4 =false;
                Database gdb = SpatialiteInterface.openGdb(context,AppFolderStructure.getReGpFilePath(context));
                try {


                    Stmt stmt = gdb.prepare(querypath);

                    while (stmt.step()) {
                       Log.i("ddddd","hasproj4 "+stmt.column_int(1));
                    }

                    closeGdb();
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "create Spatialite Specific Tables", e.getMessage());
            }

        }

        return reGpFeatureDao;
    }


    public static Map<String, Object> getJurisdictionFromPoint_new(Context context, Point p) {
        Map<String, Object> jurisdictionValuesMap = null;

        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();
            JSONObject conditionObj = new JSONObject();

                conditionObj.put("value", UserInfoPreferenceUtility.getJurisdictionName());
                conditionObj.put("operator", "=");
                conditionObj.put("columnType", "string");
                conditionMap.put(UserInfoPreferenceUtility.getJurisdictionType(), conditionObj);

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForREGpkg(context), new ReveloLogger(), context);

            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForREGpkg(context),
                    getDatasetInfo(), null, conditionMap, "", true, -1, true);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        for (int i = 0; i < featuresJArray.length(); i++) {
                            JSONObject featureJObj = featuresJArray.getJSONObject(i);
                            JSONObject propertiesJobjReceived = featureJObj.getJSONObject("properties");
                            if (featureJObj.has("geometry")) {
                                JSONObject geometryJson = featureJObj.getJSONObject("geometry");
                               Geometry jtsGeom = GeoJsonUtils.convertToJTSGeometry(geometryJson);
                               if(jtsGeom!=null){
                                   if(jtsGeom.contains(p)){
                                       Iterator<String> propertyItr = propertiesJobjReceived.keys();
                                       while (propertyItr.hasNext()){
                                           String propertyName = propertyItr.next();
                                           String value = propertiesJobjReceived.getString(propertyName);
                                           if(!propertyName.equalsIgnoreCase("the_geom")) {
                                               if (jurisdictionValuesMap == null) {
                                                   jurisdictionValuesMap = new HashMap<>();
                                               }
                                               jurisdictionValuesMap.put(propertyName, value);
                                           }
                                       }
                                       break;
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



        GeoPackageGeometryData oldGeometryData = new GeoPackageGeometryData(p.getSRID());
        try {
            oldGeometryData.setGeometryFromWkt(p.toText());
            oldGeometryData.getOrBuildEnvelope();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jurisdictionValuesMap;

    }

    public static Map<String, Object> getJurisdictionFromPoint_spatialite(Context context, Geometry point){
        String taskName ="getJurisdictionFromPoint_spatialite";
        ReveloLogger.info(className, taskName, "finding jurisdiction values from redb matching to above point");
        final Map<String, Object>[] jvm = new Map[]{null};
        try {
        FeatureDao redbFeatureDao = getReGpFeatureDao(context);
           // Database database = SpatialiteInterface.openGdb(context, AppFolderStructure.getReGpFilePath(context));
            HashMap<String,String> jurisNameIdMap = OrgBoundaryConceptModel.createJurisdictionNamesIdMap();
            String columnNames = "";
            for(String id:jurisNameIdMap.values()){
                columnNames+=id+",";
            }
            if (columnNames.endsWith(",")) {
                columnNames = columnNames.substring(0, columnNames.lastIndexOf(","));
            }
            String query= "select "+columnNames+" from " +RE_GP_TABLE_NAME + " where ";
            //Transform(GeomFromText('POINT(21.865466 43.327717)', 4326), 3035)
            //query = "select 'division','beat','range','block','state','circle' from w9obre where contains(transform(GeomFromGPB(the_geom),4326), GeomFromText('POINT (77.09559941365971 30.28552047255579)',4326)) ;";

            String whereClause = " contains(GeomFromGPB(the_geom), " + "GeomFromText('" + point + "',3857)) ;";
          //  String whereClause = " contains(GeomFromGPB(the_geom), " + "transform(GeomFromText('" + point + "',4326),3857)) ;";
            //working=select * from w9obre_old where contains(GeomFromGPB(the_geom),transform(st_GeomFromText('POINT(76.785507 30.379391)',4326),3857))
            final String[][] columnsArray = {new String[jurisNameIdMap.size()]};
            final String[][] valuesArray = {new String[jurisNameIdMap.size()]};
            final int[] numRows = {-1};
            QueryCallback callback = new QueryCallback(jurisNameIdMap.size());
            JSONObject jsonObject = runQuery(context, AppFolderStructure.getReGpFilePath(context), query+whereClause,callback );


            if(jsonObject.has("status") &&jsonObject.getString("status").equalsIgnoreCase("success") && jsonObject.has("message")){
                Object msg = jsonObject.get("message");
                if(msg instanceof QueryCallback){
                    QueryCallback callback1 = (QueryCallback) msg;
                    callback1.getNumrows();
                    return callback1.jvm;
                }
            }

            /*Stmt stmt = database.prepare(query+whereClause);

            while (stmt.step()) {
                Object w9Id = null;




                    String dataype = attributes.get(w9IdPropertyName);
                    if (dataype.equalsIgnoreCase("string")
                            || dataype.equalsIgnoreCase("date")
                            || dataype.equalsIgnoreCase("text")) {
                        w9Id = stmt.column_string(0);
                    } else if (dataype.equalsIgnoreCase("double") || dataype.equalsIgnoreCase("double precision")) {
                        w9Id = stmt.column_double(0);
                    } else if (dataype.equalsIgnoreCase("integer")) {
                        w9Id = stmt.column_int(0);
                    }
        try {
                    String columnObject = String.valueOf(object);
                    if (!columnObject.equalsIgnoreCase(featureRow.getGeometryColumnName())) {
                        String value = String.valueOf(featureRow.getValue(columnObject));
                        jvm.put(columnObject, value);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            SpatialiteInterface.closeGdb(database);*/
           /* ReveloLogger.info(className, taskName, numRows[0]+ "records found that contains point "+point.toText());
            if(numRows[0]==-1 || numRows[0]>1){
                ReveloLogger.info(className, taskName, "Trying geopackage method..");
               return getJurisdictionFromPoint(context,point);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
           return getJurisdictionFromPoint(context,point);
        }
        return jvm[0];
        }

    public static class QueryCallback implements Callback {
        public QueryCallback(int numRequiredColumns){
            this.numRequiredColumns=numRequiredColumns;
        }
        int numrows=-1;
        int numRequiredColumns=1;
        String[]coldata = new String[numRequiredColumns];
        String[]rowData = new String[numRequiredColumns];
        Map<String, Object> jvm = new HashMap<>();


        public int getNumrows() {
            return numrows;
        }

        public String[] getColdata() {
            return coldata;
        }

        public String[] getRowData() {
            return rowData;
        }

        public Map<String, Object> getJvm() {
            return jvm;
        }

        @Override
        public void columns(String[] coldata) {
            // numRows[0]++;
            // columnsArray[0] =coldata;
            this.coldata=coldata;
            this.numrows++;
            Log.i("dddddddddddddddata","column data "+coldata);
        }

        @Override
        public void types(String[] types) {

        }

        @Override
        public boolean newrow(String[] rowdata) {
            // numRows[0]++;
            this.numrows++;
            this.rowData=rowdata;
            //for(int i=0;i<rowdata.length;i++){
            //    if(jvm[0] ==null){
            //        jvm[0] =new HashMap<>();
            //    }
            //    jvm[0].put(columnsArray[0][i],rowdata[i]);
            //}


            for(int i=0;i<rowdata.length;i++){
                jvm.put(coldata[i],rowdata[i]);
            }
            Log.i("ddddddddddddddddata","row data "+rowdata);
            return false;
        }
    }

    public static Map<String, Object> getJurisdictionFromPoint(Context context, Geometry point) {
        String taskName ="getJurisdictionFromPoint";
        ReveloLogger.info(className, taskName, "finding jurisdiction values from redb matching to above point");
        Map<String, Object> jvm = null;

        try {
            FeatureDao redbFeatureDao = getReGpFeatureDao(context);
        if(point!=null) {
                try (FeatureCursor featureCursor = redbFeatureDao.queryForAll()) {
                while (featureCursor.moveToNext()) {

                    try {
                            GeoPackageGeometryData geometryData = featureCursor.getGeometry();
                            Projection projection = ((FeatureDao) redbFeatureDao).getProjection();
                            ProjectionTransform transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
                            geometryData = geometryData.transform(transform4326);

                            if (geometryData != null && !geometryData.isEmpty()) {
                                mil.nga.sf.Geometry geom4326 = null;
                                geometryData.setSrsId(4326);
                                geom4326 = geometryData.getGeometry();
                                if (geom4326 != null) {
                                    Geometry geometryV = GeometryEngine.convertNGAGeomToJTSGeom(geometryData);
                        boolean ifContains = false;
                                    FeatureRow featureRow = featureCursor.getRow();

                                    if (geometryV != null) {
                                        ifContains = geometryV.contains(point);
                            }
                        if (ifContains) {


                            Object[] columnName = featureRow.getColumnNames();

                            for (Object object : columnName) {
                                if (object != null) {
                                    try {
                                        String columnObject = String.valueOf(object);
                                        if (!columnObject.equalsIgnoreCase(featureRow.getGeometryColumnName())) {
                                            String value = String.valueOf(featureRow.getValue(columnObject));
                                                        if(jvm==null){
                                                            jvm = new HashMap<>();
                                                        }
                                                        jvm.put(columnObject, value);
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            break;

                                    }

                                }
                        }
                    } catch (Exception ex) {
                            ReveloLogger.error(className, taskName, "Exception occurred while entering jurisdiction val in map...returning null.."+ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            } catch (Exception e) {
                    ReveloLogger.error(className, taskName, "Exception occurred querying for jurisdiction val...returning null.."+e.getMessage());
                e.printStackTrace();
            }
            }else {
                ReveloLogger.error(className, taskName, "null point geom received...returning null..");
        }
        } catch (Exception e) {
            ReveloLogger.error(className, taskName, "Exception occurred while getting jurisdiction val...returning null.."+e.getMessage());
            e.printStackTrace();
        }
        return jvm;

    }

    private static boolean isInSameJurisdiction(Geometry point) {
        boolean ifContains = false;

        if (cacheJurisdiction != null && !cacheJurisdiction.isEmpty() && cachePolygon != null) {
            ifContains = cachePolygon.contains(point);
            return ifContains;
        } else {
            return ifContains;
        }
    }

    private static String buildEqual(String key, String value) {

        StringBuilder selection = new StringBuilder();

        if (value != null) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }

            selection.append(CoreSQLUtils.quoteWrap(key)).append(" ").append("='").append(value).append("'");
        }

        return selection.toString();

    }

    public static Map<String, Object> upperHierarchyMap(Vertex rootVertex, String idProperty,
                                                        String idPropertyDataType,
                                                        String assignJurisdictionName,
                                                        String assignJurisdictionType,
                                                        Context context) {

        Map<String, Object> upperHierarchyMap = new HashMap<>();

        Map<String, String> columnNameMap = getColumnNames(rootVertex, assignJurisdictionType, null);
        List<String> columnNamesList = new ArrayList<>();
        for (String columnName : columnNameMap.keySet()) {
            columnNamesList.add(columnNameMap.get(columnName));
        }

        HashMap<String, JSONObject> conditionMap;
        conditionMap = new HashMap<>();
        try {
            JSONObject conditionObj = new JSONObject();
            conditionObj.put("value", assignJurisdictionName);
            conditionObj.put("isCheckEquals", true);
            conditionObj.put("columnType", idPropertyDataType);
            conditionMap.put(idProperty, conditionObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForREGpkg(context), new ReveloLogger(), context);
        JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForREGpkg(context),
                DbRelatedConstants.getDataSetInfoForTable("w9obre"),
                columnNamesList, conditionMap, "", true, 1,false);

        try {
            if (respJObj.has("status")) {
                if (respJObj.getString("status").equalsIgnoreCase("success")) {
                    if (respJObj.has("features")) {
                        JSONObject responseFeatures = respJObj.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject propertyJson = featuresJArray.getJSONObject(i).getJSONObject("properties");
                                Iterator<String> itrKeys = propertyJson.keys();
                                while (itrKeys.hasNext()){
                                    String key = itrKeys.next();
                                    upperHierarchyMap.put(key,propertyJson.get(key));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
       /* FeatureDao redbFeatureDao = getReGpFeatureDao(context);

        String where = redbFeatureDao.buildWhere(idProperty, assignJurisdictionName);
        String[] s = {assignJurisdictionName};
        String[] arr = columnNameMap.values().toArray(new String[0]);

        UserQuery userQuery = new UserQuery(RE_GP_TABLE_NAME, arr, where, s, null, null, null, "1");
        try (FeatureCursor featureCursor = redbFeatureDao.query(userQuery)) {

            while (featureCursor.moveToNext()) {

                try {

                    FeatureRow featureRow = featureCursor.getRow();
                    Object[] values = featureRow.getValues();

                    Object[] ColumnNames = columnNameMap.keySet().toArray();

                    for (int i = 0; i < ColumnNames.length; i++) {

                        String columnName = String.valueOf(ColumnNames[i]);

                        try {
                            Object value = values[i];
                            upperHierarchyMap.put(columnName, value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        return upperHierarchyMap;
    }

    public static List<String> getEntityValues(String whereColumnName, String value, String requiredColumn, Context context) {

        List<String> entityValueList = new ArrayList<>();
        entityValueList.add("All");

        String tableName = "w9obre";


        HashMap<String, JSONObject> conditionMap;
        conditionMap = new HashMap<>();
        try {
            JSONObject conditionObj = new JSONObject();
            conditionObj.put("value", value);
            conditionObj.put("isCheckEquals", true);
            conditionObj.put("columnType", "string");
            conditionMap.put(whereColumnName, conditionObj);
        }catch (Exception e){
            e.printStackTrace();
        }
        List<String> reqdColumnNamesList = new ArrayList<>();
        reqdColumnNamesList.add(requiredColumn);

        GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForREGpkg(context),new ReveloLogger(),context);
        JSONObject respJObj = gpkgRWAgent.getDatasetContent(context,DbRelatedConstants.getDataSourceInfoForREGpkg(context),
                DbRelatedConstants.getDataSetInfoForTable("w9obre"),
                reqdColumnNamesList, conditionMap, "", true, -1,false);

        try {
            if (respJObj.has("status")) {
                if (respJObj.getString("status").equalsIgnoreCase("success")) {
                    if (respJObj.has("features")) {
                        JSONObject responseFeatures = respJObj.getJSONObject("features");
                        if (responseFeatures.has("features")) {
                            JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject propertyJson = featuresJArray.getJSONObject(i).getJSONObject("properties");
                                Iterator<String> itrKeys = propertyJson.keys();
                                while (itrKeys.hasNext()){
                                    String key = itrKeys.next();
                                    entityValueList.add(propertyJson.get(key).toString());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


       /* FeatureDao redbFeatureDao = getReGpFeatureDao(context);

        String where = redbFeatureDao.buildWhere(whereColumnName, value);
        String[] valueArray = {value};
        String[] requiredColumns = {requiredColumn};

        //String sqlQuery = "SELECT DISTINCT " + requiredColumn + " FROM " + tableName + " WHERE " + whereColumnName + " = '" + value + "'";

        UserQuery userQuery = new UserQuery(tableName, requiredColumns, where, valueArray, null, null, null, null);
        //UserQuery userQuery = new UserQuery(sqlQuery, valueArray);
        try (FeatureCursor featureCursor = redbFeatureDao.query(userQuery)) {

            while (featureCursor.moveToNext()) {

                try {

                    FeatureRow featureRow = featureCursor.getRow();
                    Object[] values = featureRow.getValues();

                    for (Object object : values) {
                        if (object != null) {
                            try {
                                String valueObject = String.valueOf(object);
                                if (!entityValueList.contains(valueObject)) {
                                    entityValueList.add(valueObject);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return entityValueList;
    }

    private static Map<String, String> getColumnNames(Vertex rootVertex, String jurisdictionType, Map<String, String> columnNameMap) {

        String name = rootVertex.getProperty(GraphConstants.NAME);
        String idProperty = rootVertex.getProperty(GraphConstants.ID_PROPERTY);


        if (columnNameMap == null) {
            columnNameMap = new HashMap<>();

        }
        columnNameMap.put(name, idProperty);

        if (!name.equalsIgnoreCase(jurisdictionType)) {
            Iterable<Vertex> vertexIterable = rootVertex.getVertices(Direction.OUT);
            for (Vertex internalVertex : vertexIterable) {
                getColumnNames(internalVertex, jurisdictionType, columnNameMap);
            }
        }

        return columnNameMap;
    }

    public static void clearReGpFeatureDao() {
        if (reGpFeatureDao != null) {
            reGpFeatureDao = null;
        }
    }

    private static void addPolygonToList(List<Polygon> list, Geometry polygon) {
        Polygon p = (Polygon) polygon;
        list.add(p);
    }

    private static void addMultipolygonToList(List<Polygon> list, Geometry geometry) {
        MultiPolygon multiPolygon = (MultiPolygon) geometry;
        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Geometry polyGeom = multiPolygon.getGeometryN(i);
            if (polyGeom instanceof Polygon) {
                addPolygonToList(list, polyGeom);
            } else if (polyGeom instanceof MultiPolygon) {
                addMultipolygonToList(list, polyGeom);
            }
        }
    }



    private static   JSONObject runQuery(Context context,String filePath, String query, Callback callback) throws Exception {

        JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("status", "failure");
            String errorMsg = "unknown error";

            try {

                openGdb(context,filePath);
                if (redb != null && isRedbOpen) {
                    redb.exec("BEGIN;", null);
                    redb.exec(query, callback);
                    redb.exec("COMMIT;", null);

                    jsonObject.put("status", "success");
                    jsonObject.put("message", callback);
                } else {
                    jsonObject.put("status", "failure");
                    jsonObject.put("message", "unable to open database");
                }
            } catch (Exception e) {
                errorMsg = e.getMessage();
                e.printStackTrace();
                ReveloLogger.error(className, "Run Query", e.getMessage());
                jsonObject.put("message", errorMsg);
            } finally {
                if (redb != null)
                    redb.close();
            }



        } catch (java.lang.Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "Run Query", e.getMessage());
        }
        return jsonObject;
    }

    private static   void openGdb(Context context, String filepath) throws Exception {
        try {
            if (!isRedbOpen) {
                if(redb==null) {
                    redb = new Database();
                }
                int mode = Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE;
                redb.open(filepath, mode);
                isRedbOpen = true;
            }
        } catch (Exception je) {
            ReveloLogger.error(className, "open Gdb", "error opening gdb: " + je.getMessage());
            isRedbOpen = false;
            throw je;
        }
    }

    private static  void closeGdb() throws Exception {

        try {
            if (isRedbOpen && redb!=null) {
                redb.close();
                isRedbOpen = false;
            }else if(redb==null){
                isRedbOpen=false;
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "close Gdb", "error closing gdb: " + e.getMessage());
            if (e.getMessage().contains("closed")) {
                isRedbOpen = false;
            } else {
                isRedbOpen = true;
            }

            throw e;
        }
    }

}