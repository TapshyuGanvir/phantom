package com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageManagerAgent;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageRWAgent;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.OrgBoundaryConceptModel;
import com.sixsimplex.treasurehunt.revelocore.util.TinkerGraphUtil;
import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.CoreSQLUtils;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.user.UserQuery;
import mil.nga.sf.GeometryType;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionConstants;
import mil.nga.sf.proj.ProjectionTransform;
import mil.nga.sf.util.GeometryUtils;

public class ReDbTableOld {

    private static String RE_GP_TABLE_NAME = "w9obre";
    private static final String className = "ReDbTable";
    private static ProjectionTransform transform4326;
    private static FeatureDao reGpFeatureDao;

    private static FeatureDao getReGpFeatureDao(Context context) {

        if (reGpFeatureDao == null) {
            GeoPackage reGeoPackage = GeoPackageManagerAgent.getReGeoPackage(context, DbRelatedConstants.getPropertiesJsonForREGpkg(context));
            reGpFeatureDao = reGeoPackage.getFeatureDao(RE_GP_TABLE_NAME);

            Projection projection = reGpFeatureDao.getProjection();
            transform4326 = projection.getTransformation(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
        }

        return reGpFeatureDao;
    }

    public static Map<String, Object> getUserJurisdiction(Activity activity) {

        Map<String, Object> userJurisdictionMap = new HashMap<>();

        Map<String, String> columnNameMap = null;
        String idProperty = null;

        Graph graph = OrgBoundaryConceptModel.getObReGraph();

        if (graph != null) {
            Vertex rootVertex = TinkerGraphUtil.findRootVertex(graph);
            String assignJurisdictionType = UserInfoPreferenceUtility.getJurisdictionType();

            columnNameMap = getColumnNames(rootVertex, assignJurisdictionType, columnNameMap);

            Vertex vertex = graph.getVertex(assignJurisdictionType);
            if (vertex != null) {
                idProperty = vertex.getProperty(GraphConstants.ID_PROPERTY);
            }
        }

        if (columnNameMap != null && idProperty != null) {

            String tableName = "w9obre";

            String assignJurisdictionName = UserInfoPreferenceUtility.getJurisdictionName();

            FeatureDao redbFeatureDao = getReGpFeatureDao(activity);

            String where = redbFeatureDao.buildWhere(idProperty, assignJurisdictionName);
            String[] s = {assignJurisdictionName};
            String[] arr = columnNameMap.values().toArray(new String[0]);

            UserQuery userQuery = new UserQuery(tableName, arr, where, s, null, null, null, "1");
            try (FeatureCursor featureCursor = redbFeatureDao.query(userQuery)) {
                while (featureCursor.moveToNext()) {
                    try {
                        FeatureRow featureRow = featureCursor.getRow();
                        Object[] values = featureRow.getValues();

                        Object[] ColumnNames = columnNameMap.keySet().toArray();

                        for (int i = 0; i < ColumnNames.length; i++) {

                            String columnName = String.valueOf(ColumnNames[i]);

                            featureCursor.getGeometry().getGeometry();

                            try {
                                Object value = values[i];
                                userJurisdictionMap.put(columnName, value);
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
            }
        }

        return userJurisdictionMap;
    }

    public static Map<String, Object> getJurisdictionFromPoint(Context context, Point p) {

        FeatureDao redbFeatureDao = getReGpFeatureDao(context);
        Map<String, Object> jurisdictionValuesMap = null;

        GeoPackageGeometryData oldGeometryData = new GeoPackageGeometryData(p.getSRID());
        try {
            oldGeometryData.setGeometryFromWkt(p.toText());
            oldGeometryData.getOrBuildEnvelope();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ProjectionTransform transform = transform4326.getInverseTransformation();
        GeoPackageGeometryData newGeomData = oldGeometryData.transform(transform);

        mil.nga.sf.Point point = (mil.nga.sf.Point) newGeomData.getGeometry();

        //String where = "st_contains(" + RE_GP_TABLE_NAME + "." + dao.getGeometryColumnName() + ",st_geomfromtext('" + newGeomData.getWkt() + "'));";

        String name = UserInfoPreferenceUtility.getJurisdictionName();
        String type = UserInfoPreferenceUtility.getJurisdictionType();

        // String where = redbFeatureDao.buildWhere(type, name);
        //String where = "\""+type+"\""+"="+"\""+name+"\"";

        String where = buildEqual(type, name);

        UserQuery userQuery = new UserQuery(RE_GP_TABLE_NAME, null, where, null, null, null, null, null);

        try (FeatureCursor featureCursor = redbFeatureDao.query(true, where)) {

            while (featureCursor.moveToNext()) {

                try {

                    FeatureRow featureRow = featureCursor.getRow();
                    mil.nga.sf.Geometry geometry = featureRow.getGeometry().getGeometry();

                    boolean ifContains = false;

                    if (geometry.getGeometryType() == GeometryType.POLYGON) {

                        mil.nga.sf.Polygon polygon = (mil.nga.sf.Polygon) geometry;
                        ifContains = GeometryUtils.pointInPolygon(point, polygon);

                    } else if (geometry.getGeometryType() == GeometryType.MULTIPOLYGON) {

                        mil.nga.sf.MultiPolygon multiPolygon = (mil.nga.sf.MultiPolygon) geometry;
                        for (mil.nga.sf.Polygon polygon : multiPolygon.getPolygons()) {
                            ifContains = GeometryUtils.pointInPolygon(point, polygon);
                        }
                    }

                    //Geometry geometry = GeometryEngine.convertNGAGeomToJTSGeom(featureRow.getGeometry());
                    // if (geometry != null) {
                    //  boolean ifContains = geometry.contains(point);
                    if (ifContains) {

                        jurisdictionValuesMap = new HashMap<>();

                        Object[] columnName = featureRow.getColumnNames();

                        for (Object object : columnName) {
                            if (object != null) {
                                try {
                                    String columnObject = String.valueOf(object);
                                    if (!columnObject.equalsIgnoreCase(featureRow.getGeometryColumnName())) {
                                        String value = String.valueOf(featureRow.getValue(columnObject));
                                        jurisdictionValuesMap.put(columnObject, value);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        break;
                        // }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jurisdictionValuesMap;

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

    public static Map<String, Object> upperHierarchyMap(Vertex rootVertex, String idProperty, String idPropertyDataType, String assignJurisdictionName, String assignJurisdictionType,
                                                        Context context) {

        Map<String, Object> upperHierarchyMap = new HashMap<>();

        Map<String, String> columnNameMap = getColumnNames(rootVertex, assignJurisdictionType, null);
        List<String> columnNamesList = new ArrayList<>();
        for(String columnName:columnNameMap.keySet()){
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
            }catch (Exception e){
                e.printStackTrace();
            }
        GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForREGpkg(context),new ReveloLogger(),context);
        JSONObject respJObj = gpkgRWAgent.getDatasetContent(context,DbRelatedConstants.getDataSourceInfoForREGpkg(context),
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

    //TODO check
    public static Geometry getUserSelectedJurisdiction(Context context, JSONObject selectedJurisdictionJson, Vertex vertex) {

        Geometry multiPolygon = null;

        List<Polygon> polygonList = new ArrayList<>();

        try {
            HashMap<String, JSONObject> conditionMap = new HashMap<>();

            Map<String, String> columnNameMap = getColumnNames(vertex, "", null);

            for (Iterator<String> it = selectedJurisdictionJson.keys(); it.hasNext(); ) {
                String keys = it.next();
                String value = selectedJurisdictionJson.getString(keys);
                if (!keys.equalsIgnoreCase("downloadAttachments") && !value.equalsIgnoreCase("All")) {
                    if (columnNameMap.containsKey(keys)) {
                        String columnName = columnNameMap.get(keys);
                        try {
                            JSONObject conditionObj = new JSONObject();
                            conditionObj.put("value", value);
                            conditionObj.put("isCheckEquals", true);
                            conditionObj.put("columnType", "string");
                            conditionMap.put(columnName, conditionObj);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForREGpkg(context),new ReveloLogger(),context);
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context,DbRelatedConstants.getDataSourceInfoForREGpkg(context),
                    DbRelatedConstants.getDataSetInfoForTable("w9obre"),
                    null, conditionMap, "AND", false, -1,false);



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
                                    /*while (itrKeys.hasNext()){
                                        String key = itrKeys.next();
                                        entityValueList.add(propertyJson.get(key).toString());
                                    }*/
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }



            /*FeatureDao dao = getReGpFeatureDao(context);
            StringBuilder where = new StringBuilder();

            Map<String, String> columnNameMap = getColumnNames(vertex, "", null);

            for (Iterator<String> it = selectedJurisdictionJson.keys(); it.hasNext(); ) {
                String keys = it.next();
                String value = selectedJurisdictionJson.getString(keys);
                if (!keys.equalsIgnoreCase("downloadAttachments") && !value.equalsIgnoreCase("All")) {
                    if (columnNameMap.containsKey(keys)) {
                        String columnName = columnNameMap.get(keys);
                        if (where.length() == 0) {
                            where = new StringBuilder(buildEqual(columnName, value));
                        } else {
                            where.append(" AND ").append(buildEqual(columnName, value));
                        }
                    }
                }
            }

            Log.e("where", where.toString());

            try (FeatureCursor featureCursor = dao.query(where.toString(), null, null, null, null, null)) {

                Log.e("Start", "Start");

                while (featureCursor.moveToNext()) {

                    try {

                        FeatureRow featureRow = featureCursor.getRow();
                        GeoPackageGeometryData geoPackageGeometryData = featureRow.getGeometry();
                        if (geoPackageGeometryData != null) {
                            try {
                                Geometry geometry = new WKBReader().read(geoPackageGeometryData.getWkb());

                                if (geometry instanceof Polygon) {
                                    addPolygonToList(polygonList, geometry);
                                } else if (geometry instanceof MultiPolygon) {
                                    addMultipolygonToList(polygonList, geometry);
                                }

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
            }

            Polygon[] polygon = polygonList.toArray(new Polygon[0]);
            multiPolygon = new MultiPolygon(polygon, new GeometryFactory());
            multiPolygon = multiPolygon.union();
*/
            Log.e("End", "End");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return multiPolygon;
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
}