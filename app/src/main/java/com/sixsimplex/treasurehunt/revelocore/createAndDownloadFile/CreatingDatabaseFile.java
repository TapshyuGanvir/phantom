package com.sixsimplex.treasurehunt.revelocore.createAndDownloadFile;

import android.content.Context;
import android.text.TextUtils;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CreatingDatabaseFile {

    private String className = "CreatingDatabaseFile";

    public CreatingDatabaseFile(String url, JSONObject jsonObject, Context context, int operationType,String fileType,
                                IDatabaseCreateInterface fileDownloadInterface) {
        createDataGpFile(url, jsonObject, context,operationType, fileType, fileDownloadInterface);
    }

    private void createDataGpFile(String url, JSONObject jurisdictionData, Context context,
                                  int operationType,String fileType,
                                  IDatabaseCreateInterface fileDownloadInterface) {

        try {
            ReveloLogger.debug(className, "CreatingDatabaseFile", "request received for - \n" +
                    "operation type "+operationType+",\nfile type "+fileType+",\njurisdiction data "+jurisdictionData);
            String requestBody = null;

            String accessToken = SecurityPreferenceUtility.getAccessToken();

            RequestQueue queue = Volley.newRequestQueue(context);
            if (jurisdictionData != null) {
                requestBody = jurisdictionData.toString();
            }

            String finalRequestBody = requestBody;
            StringRequest jsonObjRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    ReveloLogger.debug(className, "CreatingDatabaseFile", "positive response received for create db request..");

                    long dataDbSize = 102400000;//100Mb
                    try {
//{"dbFileName":"reveloentities_2021_04_16_12_56_53.gpkg","dbFileSize":"131072"}
                        JSONObject createDbObject = new JSONObject(response);
                        String size = createDbObject.getString("dbFileSize");
                        if (!TextUtils.isEmpty(size)) {
                            dataDbSize = Long.parseLong(size);
                        }
                        if (fileType.equalsIgnoreCase("DataDb")) {
                            String datadbName = createDbObject.getString("dbFileName");
                            UserInfoPreferenceUtility.setDataDbName(datadbName);
                            ReveloLogger.debug(className, "CreatingDatabaseFile", "Setting datadbname " + datadbName);
                            File databaseFolder = new File(AppFolderStructure.getDataBaseFolderPath(context));
                            if (databaseFolder.isDirectory() && databaseFolder.listFiles() != null) {
                                File[] filesInDbFolder = databaseFolder.listFiles();
                                assert filesInDbFolder != null;
                                for (File file : filesInDbFolder) {
                                    if (file.getName().contains(AppConstants.DATA_GP_FILE)) {
                                        ReveloLogger.warn(className, "CreatingDatabaseFile", "old data database found..named "+file.getName()+"..deleting it..then adding new one");
                                        FileUtils.deleteQuietly(file);//delete data dbs if any
                                    }
                                }
                            }
                        } else if (fileType.equalsIgnoreCase("MetaData")) {
                            String metadatadbName = createDbObject.getString("dbFileName");
                            UserInfoPreferenceUtility.setMetatdataDbName(metadatadbName);
                            ReveloLogger.debug(className, "CreatingDatabaseFile", "Setting metadatadbname " + metadatadbName);

                            File databaseFolder = new File(AppFolderStructure.getDataBaseFolderPath(context));
                            if (databaseFolder!=null && databaseFolder.isDirectory() && databaseFolder.listFiles() != null) {
                                File[] filesInDbFolder = databaseFolder.listFiles();
                                for (File file : filesInDbFolder) {
                                    ReveloLogger.warn(className, "CreatingDatabaseFile", "old metd database found..named "+file.getName()+"..deleting it..then adding new one");
                                    if (file.getName().contains(AppConstants.METADATA_FILE)) {
                                        FileUtils.deleteQuietly(file);//delete meta dbs if any
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        ReveloLogger.error(className, "createDataGpFile", String.valueOf(e.getCause()));
                        e.printStackTrace();
                    }


                    long totalDatabaseMemoryInByte = dataDbSize + 102400000;//add 100Mb padding
                    ReveloLogger.warn(className, "CreatingDatabaseFile", "db plus additional 10Mb buffer makes a "+totalDatabaseMemoryInByte+" bytes");
                    long availableMemoryInByte = SystemUtils.getStorageAvailableMemoryInByte();
                    ReveloLogger.warn(className, "CreatingDatabaseFile", "available memory in user's phone - "+availableMemoryInByte);
                    boolean isMemoryAvailable = availableMemoryInByte > totalDatabaseMemoryInByte;

                    if (isMemoryAvailable) {
                        ReveloLogger.warn(className, "CreatingDatabaseFile", "Sufficient memory available..allowing user to download db");
                        ReveloLogger.debug(className, "CreatingDatabaseFile", "Return create db successfull..");
                        fileDownloadInterface.successFileDownload(operationType,"File Created Successfully");

                    } else {
                        ReveloLogger.warn(className, "CreatingDatabaseFile", "Insufficient Memory!! not allowing user to download db");
                        long freeUpMemory = (totalDatabaseMemoryInByte / 1024L * 1024L) - (availableMemoryInByte / 1024L * 1024L);
                        ReveloLogger.warn(className, "CreatingDatabaseFile", "returning insufficent memory .. memory needed = "+freeUpMemory);
                        fileDownloadInterface.errorFileDownload(operationType,-1,"You have insufficient memory. Free " + freeUpMemory + " MB space from storage");
                        ReveloLogger.debug(className, "CreatingDatabaseFile", "Returned create db failed due to low memory ..Free " + freeUpMemory + " MB space from storage");
                    }
                }
            }, error -> {

                String errorDescription = NetworkUtility.getErrorFromVolleyError(error);

                if (TextUtils.isEmpty(errorDescription)) {
                    errorDescription = "Something went wrong while getting project's data..";
                }
                fileDownloadInterface.errorFileDownload(operationType,-1,errorDescription);
                ReveloLogger.error(className,"createDataGpFile", "Error creating db..message="+errorDescription);
            }) {

                @Override
                public byte[] getBody() {
                    try {
                        return finalRequestBody == null ? null : finalRequestBody.getBytes(StandardCharsets.UTF_8);
                    } catch (Exception uee) {
                        fileDownloadInterface.errorFileDownload(operationType,-1,"Failed to create database.\nPlease try again later");
                        return null;
                    }
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", AppConstants.CONTENT_TYPE_APPLICATION_JSON);
                    params.put("Authorization", "Bearer " + accessToken);
                    return params;
                }
            };

            jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(1500000, 1, 1));
            queue.add(jsonObjRequest);
        } catch (Exception e) {
            String errorMsg = "Failed to create database" + e.getMessage();
            fileDownloadInterface.errorFileDownload(operationType,-1,errorMsg);
            ReveloLogger.error(className,"createDataGpFile","Error creating db..message="+ errorMsg);
        }
    }
}