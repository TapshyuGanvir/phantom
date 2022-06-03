package com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage;

import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DbRelatedConstants {

    public static final String className = "DbRelatedConstants";
    private static Map<String, JSONObject> dataSetInfoMap = null;

    public static JSONObject getPropertiesJsonForREGpkg(Context context) {
        JSONObject propertiesJsonForREGpkg;
        try {
            propertiesJsonForREGpkg = new JSONObject();
            propertiesJsonForREGpkg.put("dbName", RE_GP_NAME);
            propertiesJsonForREGpkg.put("dbPath", AppFolderStructure.getReGpFilePath(context));
        } catch (JSONException e) {
            ReveloLogger.error(className, "getPropertiesJsonForDataDb", "error initializing property json for db: " + e.getMessage());
            e.printStackTrace();
            propertiesJsonForREGpkg = null;
        }
        if (propertiesJsonForREGpkg != null) {
            return propertiesJsonForREGpkg;
        } else {
            return null;
        }
    }

    public static JSONObject getPropertiesJsonForDataGpkg(Context context) {
        JSONObject propertiesJsonForDataDb;
        try {
            propertiesJsonForDataDb = new JSONObject();
            propertiesJsonForDataDb.put("dbName", UserInfoPreferenceUtility.getDataDbName());
            propertiesJsonForDataDb.put("dbPath", AppFolderStructure.getDataGeoPackage(context).getAbsolutePath());
        } catch (JSONException e) {
            ReveloLogger.error(className, "getPropertiesJsonForDataDb", "error initializing property json for db: " + e.getMessage());
            e.printStackTrace();
            propertiesJsonForDataDb = null;
        }
        if (propertiesJsonForDataDb != null) {
            return propertiesJsonForDataDb;
        } else {
            return null;
        }
    }

    public static JSONObject getPropertiesJsonForMetdataGpkg(Context context) {
        JSONObject propertiesJsonForMetaDataDb;
        try {
            propertiesJsonForMetaDataDb = new JSONObject();
            propertiesJsonForMetaDataDb.put("dbName", UserInfoPreferenceUtility.getMetatdataDbName());
            propertiesJsonForMetaDataDb.put("dbPath", AppFolderStructure.getMetaGeoPackage(context).getAbsolutePath());
        } catch (JSONException e) {
            ReveloLogger.error(className, "getPropertiesJsonForDataDb", "error initializing property json for db: " + e.getMessage());
            e.printStackTrace();
            propertiesJsonForMetaDataDb = null;
        }
        if (propertiesJsonForMetaDataDb != null) {
            return propertiesJsonForMetaDataDb;
        } else {
            return null;
        }
    }


    public static JSONObject getDataSourceInfoForREGpkg(Context context) {
        JSONObject propertiesJsonForREGpkg;
        try {
            propertiesJsonForREGpkg = new JSONObject();
            propertiesJsonForREGpkg.put("datasourceName", RE_GP_NAME);
            propertiesJsonForREGpkg.put("dbPath",  AppFolderStructure.getReGpFilePath(context));
        } catch (JSONException e) {
            ReveloLogger.error(className, "getPropertiesJsonForDataDb", "error initializing property json for db: " + e.getMessage());
            e.printStackTrace();
            propertiesJsonForREGpkg = null;
        }
        if (propertiesJsonForREGpkg != null) {
            return propertiesJsonForREGpkg;
        } else {
            return null;
        }
    }

    public static JSONObject getDataSourceInfoForDataGpkg(Context context) {
        JSONObject propertiesJsonForDataDb;
        try {
            propertiesJsonForDataDb = new JSONObject();
            propertiesJsonForDataDb.put("datasourceName", UserInfoPreferenceUtility.getDataDbName());
            propertiesJsonForDataDb.put("dbPath",         AppFolderStructure.getDataGeoPackage(context).getAbsolutePath());
        } catch (JSONException e) {
            ReveloLogger.error(className, "getPropertiesJsonForDataDb", "error initializing property json for db: " + e.getMessage());
            e.printStackTrace();
            propertiesJsonForDataDb = null;
        }
        if (propertiesJsonForDataDb != null) {
            return propertiesJsonForDataDb;
        } else {
            return null;
        }
    }

    public static JSONObject getDataSourceInfoForMetdataGpkg(Context context) {
        JSONObject propertiesJsonForMetaDataDb;
        try {
            propertiesJsonForMetaDataDb = new JSONObject();
            propertiesJsonForMetaDataDb.put("datasourceName", UserInfoPreferenceUtility.getMetatdataDbName());
            propertiesJsonForMetaDataDb.put("dbPath",         AppFolderStructure.getMetaGeoPackage(context).getAbsolutePath());
        } catch (JSONException e) {
            ReveloLogger.error(className, "getPropertiesJsonForDataDb", "error initializing property json for db: " + e.getMessage());
            e.printStackTrace();
            propertiesJsonForMetaDataDb = null;
        }
        if (propertiesJsonForMetaDataDb != null) {
            return propertiesJsonForMetaDataDb;
        } else {
            return null;
        }
    }



    public static JSONObject getDataSetInfoForTable(String tableName) {
        Map<String, JSONObject> dataSetInfoMap = getDataSetInfoMapForMetadaDataDb();

        if (dataSetInfoMap != null) {
            return dataSetInfoMap.get(tableName);
        } else {
            return null;
        }
    }

    private static Map<String, JSONObject> getDataSetInfoMapForMetadaDataDb() {

        if (dataSetInfoMap == null) {
            dataSetInfoMap = new HashMap<>();

            //attachments table
            try {
                JSONObject datasetInfo = new JSONObject();
                datasetInfo.put("datasetName", "attachments");
                datasetInfo.put("datasetType", "table");
                datasetInfo.put("geometryType", "null");
                datasetInfo.put("idPropertyName", "featureid");

                dataSetInfoMap.put("attachments", datasetInfo);
            } catch (JSONException e) {
                ReveloLogger.error(className, "getDataSetInfoMapForMetadaDataDb", "error initializing property json for db: " + e.getMessage());
                e.printStackTrace();
                return null;
            }

            //edit metadata table
            try {
                JSONObject datasetInfo = new JSONObject();
                datasetInfo.put("datasetName", "editmetadata");
                datasetInfo.put("datasetType", "table");
                datasetInfo.put("geometryType", "null");
                datasetInfo.put("idPropertyName", "layername");

                dataSetInfoMap.put("editmetadata", datasetInfo);
            } catch (JSONException e) {
                ReveloLogger.error(className, "getDataSetInfoMapForMetadaDataDb", "error initializing property json for db: " + e.getMessage());
                e.printStackTrace();
                return null;
            }

            //relationships
            try {
                JSONObject datasetInfo = new JSONObject();
                datasetInfo.put("datasetName", "relationships");
                datasetInfo.put("datasetType", "table");
                datasetInfo.put("geometryType", "null");
                datasetInfo.put("idPropertyName", "name");

                dataSetInfoMap.put("relationships", datasetInfo);
            } catch (JSONException e) {
                ReveloLogger.error(className, "getDataSetInfoMapForMetadaDataDb", "error initializing property json for db: " + e.getMessage());
                e.printStackTrace();
                return null;
            }

            //entities
            try {
                JSONObject datasetInfo = new JSONObject();
                datasetInfo.put("datasetName", "entities");
                datasetInfo.put("datasetType", "table");
                datasetInfo.put("geometryType", "null");
                datasetInfo.put("idPropertyName", "name");

                dataSetInfoMap.put("entities", datasetInfo);
            } catch (JSONException e) {
                ReveloLogger.error(className, "getDataSetInfoMapForMetadaDataDb", "error initializing property json for db: " + e.getMessage());
                e.printStackTrace();
                return null;
            }

            //attachments table
            try {
                JSONObject datasetInfo = new JSONObject();
                datasetInfo.put("datasetName", "jurisdictions");
                datasetInfo.put("datasetType", "spatial");
                datasetInfo.put("geometryType", "polygon");
                datasetInfo.put("idPropertyName", "name");

                dataSetInfoMap.put("jurisdictions", datasetInfo);
            } catch (JSONException e) {
                ReveloLogger.error(className, "getDataSetInfoMapForMetadaDataDb", "error initializing property json for db: " + e.getMessage());
                e.printStackTrace();
                return null;
            }

           /* //trail table
            try {
                JSONObject datasetInfo = new JSONObject();
                datasetInfo.put("datasetName", AppController.TRAIL_TABLE_TABLENAME);
                datasetInfo.put("datasetType", "spatial");
                datasetInfo.put("geometryType", "multilinestring");
                datasetInfo.put("idPropertyName", AppController.TRAIL_TABLE_TRAILID);

                dataSetInfoMap.put(AppController.TRAIL_TABLE_TABLENAME, datasetInfo);
            } catch (JSONException e) {
                ReveloLogger.error(className, "getDataSetInfoMapForMetadaDataDb", "error initializing property json for db: " + e.getMessage());
                e.printStackTrace();
                return null;
            }*/

           /* //beats table
            try {
                JSONObject datasetInfo = new JSONObject();
                datasetInfo.put("datasetName", "w9obre");
                datasetInfo.put("datasetType", "spatial");
                datasetInfo.put("geometryType", "multipolygon");
                datasetInfo.put("idPropertyName", "mfbeat_nam");

                dataSetInfoMap.put("w9obre", datasetInfo);
            } catch (Exception e) {
                ReveloLogger.error(className, "getDataSetInfoMapForMetadaDataDb", "error initializing property json for db: " + e.getMessage());
                e.printStackTrace();
                return null;
            }*/

            //beats table ingpkg
            try {
                JSONObject datasetInfo = new JSONObject();
                datasetInfo.put("datasetName", "w9obre");
                datasetInfo.put("datasetType", "spatial");
                datasetInfo.put("geometryType", "multipolygon");
                datasetInfo.put("idPropertyName", "mfbeat_nam");

                dataSetInfoMap.put("w9obre", datasetInfo);
            } catch (Exception e) {
                ReveloLogger.error(className, "getDataSetInfoMapForMetadaDataDb", "error initializing property json for db: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        return dataSetInfoMap;

    }



    // start region geopackage_constants
    public static String RE_GP_NAME = "w9obregdb.gpkg";
    public static String DATA_GP_NAME = "datagp.gpkg";
    public static String META_GP_NAME = "metadata.gpkg";
    public static String REGpkg_FOLDER_NAME="Regp";
    public static String DataBase_FOLDER_NAME="DatabaseGpkg";

   /* public static JSONObject getPropertiesJsonForMetadataGeopackage() {

        JSONObject propertiesJsonForMetadataDb;

        try {
            propertiesJsonForMetadataDb = new JSONObject();
            propertiesJsonForMetadataDb.put("dbName", ReveloStore.getMetaDataDbName());
            propertiesJsonForMetadataDb.put("dbPath", SystemUtils.createDataBaseGpkgFolder());
        } catch (JSONException e) {
            ReveloLogger.error(className, "getPropertiesJsonForMetadataDb", "error initializing property json for db: " + e.getMessage());
            e.printStackTrace();
            propertiesJsonForMetadataDb = null;
        }

        if (propertiesJsonForMetadataDb != null) {
            return propertiesJsonForMetadataDb;
        } else {
            return null;
        }
    }*/


    // end region geopackage_constants
}