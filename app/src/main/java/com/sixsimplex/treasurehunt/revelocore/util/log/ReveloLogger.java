package com.sixsimplex.treasurehunt.revelocore.util.log;

import android.content.Context;
import android.util.Log;

import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageRWAgent;
import com.sixsimplex.treasurehunt.revelocore.util.AppController;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ReveloLogger {

    private static String className = "ReveloLogger";
    private static Context reveloLoggerContext = null;
    public static String REVELOLOGGER_TABLE_CONSTANTS_ID = "id";
    public static String REVELOLOGGER_TABLE_CONSTANTS_MESSAGE = "message";
    public static String REVELOLOGGER_TABLE_CONSTANTS_TIME = "time";
    public static String REVELOLOGGER_TABLE_CONSTANTS_LEVEL = "level";
    public static String REVELOLOGGER_TABLE_CONSTANTS_USERNAME = "userName";
    public static String REVELOLOGGER_TABLE_CONSTANTS_RESOURCENAME = "resourceName";
    public static String REVELOLOGGER_TABLE_CONSTANTS_OPERATIONNAME = "operationName";
    public static String REVELOLOGGER_TABLE_CONSTANTS_PROJECTNAME = "projectName";
    public static String REVELOLOGGER_TABLE_CONSTANTS_SOURCEAPPNAME = "sourceAppName";
    public static String REVELOLOGGER_TABLE_CONSTANTS_CLIENTIP = "clientIP";

    public static long startTime = System.nanoTime();

    public static void initialize(Context context, String userName) {
        reveloLoggerContext = context;
        startTime = System.nanoTime();
        File logFolder = AppFolderStructure.createLogFolder(context);

        if (logFolder!=null && logFolder.exists()) {//check log folder is exist or not
            File[] logFiles = logFolder.listFiles();
            if (logFiles != null) {
                if (logFiles.length == 0) { //folder is empty create log file
                    createLogFile(context, userName, logFolder.getAbsolutePath());
                }
            } else {
                createLogFile(context, userName, logFolder.getAbsolutePath());
            }
        } else if(logFolder!=null){//create log folder
            boolean logFolderIsCreate = logFolder.mkdirs();
            if (logFolderIsCreate) { //create log file
                createLogFile(context, userName, logFolder.getAbsolutePath());
            }
        }


    }

    private static void createLogFile(Context context, String userName, String logFolderPath) {

        try {

            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hh_mm_ss", Locale.getDefault());
            String currentDateString = dateFormat.format(currentDate);

            String logFileName = "Revelo_" + userName + "_" + currentDateString + ".json";

            String logFilePath = logFolderPath + File.separator + logFileName;
            File logFile = new File(logFilePath);

            boolean isCreate = logFile.createNewFile();

            if (isCreate) {
                Logger logger = java.util.logging.LogManager.getLogManager().getLogger("");
                java.util.logging.FileHandler fileHandler =new FileHandler(logFile.getAbsolutePath(), 5* 1024 * 1024/*5Mb*/, 1, true);

                ReveloLogger.debug(className, "file creation", "Log file created at "+SystemUtils.getCurrentDateTime());
                ReveloLogger.timeLog(className,"file creation", "Log file created at "+SystemUtils.getCurrentDateTime());
                String deviceInfo = DeviceInfo.getDeviceInfo(context);
                ReveloLogger.info(className, "System Info of " + userName, deviceInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteLogFolder(Context context) {
        try {

            File logFolder = AppFolderStructure.createLogFolder(context);

            if (logFolder!=null && logFolder.exists()) {//check log folder is exist or not
                File[] logFiles = logFolder.listFiles();//get all log files
                if (logFiles != null) {
                    for (File logFile : logFiles) {
                        if(logFile!=null)
                          logFile.delete();//delete all log files
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trace(String resource, String operation, String message) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        addLog("TRACE", resource, operation, message);
                        Log.v(AppController.TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void debug(String resource, String operation, String message) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        addLog("DEBUG", resource, operation, message);
                        Log.d(AppController.TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void info(String resource, String operation, String message) {

        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        addLog("INFO", resource, operation, message);
                        Log.i(AppController.TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void warn(String resource, String operation, String message) {

        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        addLog("WARN", resource, operation, message);
                        Log.w(AppController.TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void timeLog(String resource, String operation, String message) {

        try {
            //addLog("TRACE", resource, operation, message);
            Log.w(AppController.TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. "+ "TimeTAKEN :" + ((System.nanoTime()-startTime)/1000000) + "ms .. " + "Message :" + message);
            startTime=System.nanoTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void error(String resource, String operation, String message) {
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        addLog("ERROR", resource, operation, message);
                        Log.e(AppController.TAG, "Resource :" + resource + " .. " + "Operation :" + operation + " .. " + "Message :" + message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private static void addLog(String level, String resource, String operation, String message) {
        /*try {
            if(reveloLoggerContext!=null) {
                File logFile = getLogFile(reveloLoggerContext);
                if (logFile != null && logFile.exists()) {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ", Locale.getDefault());
                    String logDateTime = sdf.format(new Date());

                    JSONObject logJsonObject = new JSONObject();
                    logJsonObject.put("time", logDateTime);
                    logJsonObject.put("level", level);
                    logJsonObject.put("userName", UserInfoPreferenceUtility.getUserName());
                    logJsonObject.put("resource", resource);
                    logJsonObject.put("operation", operation);
                    logJsonObject.put("projectName", UserInfoPreferenceUtility.getSurveyName());
                    logJsonObject.put("sourceAppName", "mobile");
                    logJsonObject.put("message", message);

                    JSONObject fileJsonObject = new JSONObject();
                    fileJsonObject.put("logFilePath", logFile.getAbsolutePath());
                    fileJsonObject.put("logJsonObject", logJsonObject);

                    new InsertLogInFile().execute(fileJsonObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        if(reveloLoggerContext!=null) {
            File logFile = getLogFile(reveloLoggerContext);

            if (logFile == null || !logFile.exists()) {
                try {
                    if(AppFolderStructure.getLogFolderPath(reveloLoggerContext)!=null) {
                        File logFolder = AppFolderStructure.createLogFolder(reveloLoggerContext);
                        boolean logFolderIsCreate = logFolder.mkdirs();
                        if (logFolderIsCreate) { //create log file
                            createLogFile(reveloLoggerContext, UserInfoPreferenceUtility.getUserName(), logFolder.getAbsolutePath());
                        }
                        //createLogFile(reveloLoggerContext, UserInfoPreferenceUtility.getUserName(), AppFolderStructure.getLogFolderPath(reveloLoggerContext));
                    }
                    //logFile.createNewFile();
                     logFile = getLogFile(reveloLoggerContext);
                     if(logFile==null || !logFile.exists()){
                         return;
                     }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ", Locale.getDefault());
                String logDateTime = sdf.format(new Date());

                JSONObject logJsonObject = new JSONObject();
                logJsonObject.put("time", logDateTime);
                logJsonObject.put("level", level);
                logJsonObject.put("userName", UserInfoPreferenceUtility.getUserName());
                logJsonObject.put("resource", resource);
                logJsonObject.put("operation", operation);
                logJsonObject.put("projectName", UserInfoPreferenceUtility.getSurveyName());
                logJsonObject.put("sourceAppName", "mobile");
                logJsonObject.put("message", message);

                JSONObject fileJsonObject = new JSONObject();
                fileJsonObject.put("logFilePath", logFile.getAbsolutePath());
                fileJsonObject.put("logJsonObject", logJsonObject);
                buf.append(fileJsonObject.toString());
                buf.newLine();
                buf.close();
            } catch (IOException | JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static File getLogFile(Context context) {

        File logFile = null;

        if(AppFolderStructure.getLogFolderPath(context)==null)
            return null;

        File logFolder = new File(AppFolderStructure.getLogFolderPath(context));

        if (logFolder.exists()) {
            File[] logFileNames = logFolder.listFiles();
            if (logFileNames != null) {
                if (logFileNames.length != 0) {
                    logFile = logFileNames[0];
                }
            }//add else for 'if log file is manually deleted, it wont be created again!'
        }
        return logFile;
    }

    private JSONObject getLogsDatasetInfo() {
        JSONObject datasetInfo = new JSONObject();
        try {
            datasetInfo.put("datasetName", "logs");
            datasetInfo.put("datasetType", "table");
            datasetInfo.put("geometryType", "");
            datasetInfo.put("idPropertyName", "id");
            datasetInfo.put("w9IdPropertyName", "id");
        } catch (JSONException e) {
            ReveloLogger.error(className, "getDatasetInfo", "error initializing getDatasetInfo json for dataset: logs. Exception - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return datasetInfo;
    }

    private void insertLogInDb(Context context,String level, String resource, String operation, String message) {

            try {

                JSONArray dataJsonArray = new JSONArray();

                JSONObject dataObject = new JSONObject();
                JSONArray attributesJArray = new JSONArray();

                JSONObject attributeObjId = new JSONObject();
                attributeObjId.put("name", REVELOLOGGER_TABLE_CONSTANTS_ID);
                attributeObjId.put("value", Calendar.getInstance().getTimeInMillis());
                attributesJArray.put(attributeObjId);

                JSONObject attributeObjMessage = new JSONObject();
                attributeObjMessage.put("name", REVELOLOGGER_TABLE_CONSTANTS_MESSAGE);
                attributeObjMessage.put("value",message);
                attributesJArray.put(attributeObjMessage);

                JSONObject attributeObjTime = new JSONObject();
                attributeObjTime.put("name", REVELOLOGGER_TABLE_CONSTANTS_TIME);
                attributeObjTime.put("value", SystemUtils.getCurrentDateTime());
                attributesJArray.put(attributeObjTime);

                JSONObject attributeObjLevel = new JSONObject();
                attributeObjLevel.put("name", REVELOLOGGER_TABLE_CONSTANTS_LEVEL);
                attributeObjLevel.put("value", level);
                attributesJArray.put(attributeObjLevel);

                JSONObject attributeObjUserName = new JSONObject();
                attributeObjUserName.put("name", REVELOLOGGER_TABLE_CONSTANTS_USERNAME);
                attributeObjUserName.put("value", UserInfoPreferenceUtility.getUserName());
                attributesJArray.put(attributeObjUserName);

                JSONObject attributeObjResourceName = new JSONObject();
                attributeObjResourceName.put("name", REVELOLOGGER_TABLE_CONSTANTS_RESOURCENAME);
                attributeObjResourceName.put("value", resource);
                attributesJArray.put(attributeObjResourceName);

                JSONObject attributeObjOperationName = new JSONObject();
                attributeObjOperationName.put("name", REVELOLOGGER_TABLE_CONSTANTS_OPERATIONNAME);
                attributeObjOperationName.put("value", operation);
                attributesJArray.put(attributeObjOperationName);

                JSONObject attributeObjProjectName = new JSONObject();
                attributeObjProjectName.put("name", REVELOLOGGER_TABLE_CONSTANTS_PROJECTNAME);
                attributeObjProjectName.put("value", UserInfoPreferenceUtility.getSurveyName());
                attributesJArray.put(attributeObjProjectName);


                JSONObject attributeObjSourceAppName = new JSONObject();
                attributeObjSourceAppName.put("name", REVELOLOGGER_TABLE_CONSTANTS_SOURCEAPPNAME);
                attributeObjSourceAppName.put("value", "mobile");
                attributesJArray.put(attributeObjSourceAppName);

  JSONObject attributeObjIP = new JSONObject();
                attributeObjIP.put("name", REVELOLOGGER_TABLE_CONSTANTS_CLIENTIP);
                attributeObjIP.put("value", "unknown");
                attributesJArray.put(attributeObjIP);







                dataObject.put("attributes", attributesJArray);
                dataJsonArray.put(dataObject);


                GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
                JSONObject datasetInfo = getLogsDatasetInfo();
                if(gpkgRWAgent!=null && datasetInfo!=null) {
                    JSONObject respJObj = gpkgRWAgent.writeDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context),
                            datasetInfo, dataJsonArray);

                    if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {

                    } else {

                    }
                }else {
                    ReveloLogger.error(className, "getDatasetInfo", "Error while inserting log in db. Either RW Agent or dataset info for Logs table is null..");
                    ReveloLogger.info(className,"insertLogInDb","insertLogInDb failed, inserting log in file");
                    ReveloLogger.info(resource,operation,message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


}