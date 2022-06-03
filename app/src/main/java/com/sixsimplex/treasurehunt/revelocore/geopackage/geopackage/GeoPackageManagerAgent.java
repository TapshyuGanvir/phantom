package com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage;

import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.io.File;

import jsqlite.Constants;
import jsqlite.Database;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageFactory;
import mil.nga.geopackage.GeoPackageManager;

public class GeoPackageManagerAgent {

    private static GeoPackageManager manager = null;
    private static GeoPackage reGeoPackage;
    private static GeoPackage dataGeoPackage;
    private static GeoPackage metaGeoPackage;

    private static final String RE_GP_DATABASE_NAME = "w9obregdb";
    private static String META_DATABASE_NAME = "metadata";
    private static String DATA_DATABASE_NAME = "datagp";

    private static final String className = "GeoPackageManagerAgent";

    public static GeoPackage getGeoPackage(Context context, String geopackageName) {

        if (geopackageName.equalsIgnoreCase(UserInfoPreferenceUtility.getDataDbName())) {
            return getDataGeoPackage(context, DbRelatedConstants.getPropertiesJsonForDataGpkg(context));
        }
        else if (geopackageName.equalsIgnoreCase(UserInfoPreferenceUtility.getMetatdataDbName())) {
            return getMetaGeoPackage(context, DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context));
        }
        else if (geopackageName.equalsIgnoreCase(DbRelatedConstants.RE_GP_NAME)) {
            return getReGeoPackage(context, DbRelatedConstants.getPropertiesJsonForREGpkg(context));
        }
        return null;
    }

    public static GeoPackage getReGeoPackage(Context context, JSONObject propertiesJson) {

        if (reGeoPackage == null) {

            if (manager == null) {
                manager = provideGeoPackageManager(context);
            }

            if (manager.exists(RE_GP_DATABASE_NAME)) {
                manager.delete(RE_GP_DATABASE_NAME);
            }


            if (propertiesJson != null) {
                try {
                    String dbPath = propertiesJson.getString("dbPath");
                    File ReGpkgFile = new File(dbPath);
                    manager.importGeoPackage(ReGpkgFile);
                    reGeoPackage = manager.open(RE_GP_DATABASE_NAME, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    ReveloLogger.error(className, "getReGeoPackage", String.valueOf(e.getCause()));
                }
            }

            try {
                createSpatialiteSpecificTables(context,AppFolderStructure.getReGpFilePath(context));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return reGeoPackage;
    }

    public static GeoPackage getMetaGeoPackage(Context context, JSONObject propertiesJson) {
        ReveloLogger.debug("GeoPackageManagerAgent", "getMetaGeoPackage", "Fetching metageopackage..");
        if (metaGeoPackage == null) {

            if (manager == null) {
                manager = provideGeoPackageManager(context);
                ReveloLogger.debug("GeoPackageManagerAgent", "getMetaGeoPackage", "geopackage manager  initialized");
            }

            initMetaDbName();

            if (manager.exists(META_DATABASE_NAME)) {
                manager.delete(META_DATABASE_NAME);
                ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "meta db exits  - deleted.");
            }


            if (propertiesJson != null) {
                ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "metaGeoPackageFile found. Importing and opening..");
                try {
                    String dbPath = propertiesJson.getString("dbPath");
                    File metadataGpkgFile = new File(dbPath);
//                    manager.importGeoPackageAsExternalLink(metaGeoPackageFile,META_DATABASE_NAME);
                    manager.importGeoPackage(metadataGpkgFile, true);
                    metaGeoPackage = manager.open(META_DATABASE_NAME, true);
                    ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "metaGeoPackageFile found. Importing and opening..successfull");
                } catch (Exception e) {
                    e.printStackTrace();
                    ReveloLogger.error("GeoPackageManagerAgent", "initMetaDbName", "metaGeoPackageFile found. Importing and opening..failure - " + e.getCause());
                }
            }
            else {
                ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "metaGeoPackageFile not found. Geopackage not initalized..");
            }
        }

        return metaGeoPackage;
    }

    private static void initMetaDbName() {
        ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "meta db name initialized(extension separated from name)");
        META_DATABASE_NAME = UserInfoPreferenceUtility.getMetatdataDbName();
        if (META_DATABASE_NAME == null || META_DATABASE_NAME.isEmpty()) {
            META_DATABASE_NAME = "metadata";
        }
        else {
            META_DATABASE_NAME = META_DATABASE_NAME.split("\\.")[0];
            ReveloLogger.debug("GeoPackageManagerAgent", "initMetaDbName", "meta db name = " + META_DATABASE_NAME);
        }
    }

    public static GeoPackage getDataGeoPackage(Context context, JSONObject propertiesJson) {

        if (dataGeoPackage == null) {

            if (manager == null) {
                manager = provideGeoPackageManager(context);
            }

            initDataDbName();

            if (manager.exists(DATA_DATABASE_NAME)) {
                manager.delete(DATA_DATABASE_NAME);
            }

            if (propertiesJson != null) {
                try {
                    String dbPath = propertiesJson.getString("dbPath");
                    File dataGpkgFile = new File(dbPath);
                    manager.importGeoPackage(dataGpkgFile, true);
                    dataGeoPackage = manager.open(DATA_DATABASE_NAME, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    ReveloLogger.error(className, "getDataGeoPackage", String.valueOf(e.getCause()));
                }

                try {
                    createSpatialiteSpecificTables(context,AppFolderStructure.getDataBaseFolderPath(context));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return dataGeoPackage;
    }

    private static GeoPackageManager provideGeoPackageManager(Context context) {
        manager = GeoPackageFactory.getManager(context);
        return manager;
    }

    private static void createSpatialiteSpecificTables(Context context,String filepath) throws Exception {
        /*
         * this query makes sqlite db , a spatialite one. adds all the required meta data tables in db.
         * to make this query excute faster, we need to execute Begin and commit before and after the query
         */

        try {
            String query = "SELECT InitSpatialMetaData();";
            Database gdb = openGdb(context,filepath);
            if (gdb != null) {
                gdb.exec("BEGIN;", null);
                gdb.exec(query, null);
                gdb.exec("COMMIT;", null);
                gdb.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "create Spatialite Specific Tables", e.getMessage());
            throw e;
        }
    }

    private static Database openGdb(Context context,String filepath) {
        try {

            int mode = Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE;
            Database gdb = new Database();
//            gdb.open(AppFolderStructure.getDataGeoPackage(context).getAbsolutePath(), mode);
            gdb.open(filepath, mode);
            return gdb;

        } catch (java.lang.Exception e) {
            ReveloLogger.error(className, "Open Gdb", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static void initDataDbName() {
        DATA_DATABASE_NAME = UserInfoPreferenceUtility.getDataDbName();
        if (DATA_DATABASE_NAME == null || DATA_DATABASE_NAME.isEmpty()) {
            DATA_DATABASE_NAME = "datagp";
        }
        else {
            DATA_DATABASE_NAME = DATA_DATABASE_NAME.split("\\.")[0];
        }
    }

    public static void exportDataGeopackage(Context context) {

        try {
            initDataDbName();
            File dataGeoPackageFile = new File(AppFolderStructure.getDataBaseFolderPath(context));
            if (manager != null) {
                manager.exportGeoPackage(DATA_DATABASE_NAME, dataGeoPackageFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportMetaDataGeopackage(Context context) {

        try {
            initMetaDbName();
            File dataGeoPackageFile = new File(AppFolderStructure.getDataBaseFolderPath(context));
            if (manager != null) {
                manager.exportGeoPackage(META_DATABASE_NAME, dataGeoPackageFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearAllGeopackage() {
        clearMetaGeoPackage();
        clearDataGeoPackage();
        clearRedbGeopackage();
        if (manager != null) {
            manager = null;
        }
    }

    public static void clearRedbGeopackage() {
        if (reGeoPackage != null) {
            reGeoPackage = null;
        }
    }

    public static void clearDataGeoPackage() {
        if (dataGeoPackage != null) {
            dataGeoPackage = null;
        }
    }

    public static void clearMetaGeoPackage() {
        if (metaGeoPackage != null) {
            metaGeoPackage = null;
        }
    }

    public static void exportGeopackage(Context context, String datasourceName) {
        if (datasourceName.equalsIgnoreCase(UserInfoPreferenceUtility.getDataDbName())) {
            try {
                File dataGeoPackageFile = new File(AppFolderStructure.createDataGpFolder(context).getAbsolutePath());
                if (manager != null) {
                    manager.exportGeoPackage(DATA_DATABASE_NAME, dataGeoPackageFile);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (datasourceName.equalsIgnoreCase(UserInfoPreferenceUtility.getMetatdataDbName())) {
            try {
                File dataGeoPackageFile = new File(AppFolderStructure.createMetadataFolder(context).getAbsolutePath());
                if (manager != null) {
                    manager.exportGeoPackage(META_DATABASE_NAME, dataGeoPackageFile);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}