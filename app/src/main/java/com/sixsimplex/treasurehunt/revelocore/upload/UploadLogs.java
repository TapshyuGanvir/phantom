package com.sixsimplex.treasurehunt.revelocore.upload;

import android.content.Context;
import android.os.AsyncTask;

import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

public class UploadLogs extends AsyncTask<Void, Integer, JSONObject> {

    boolean isInitialLogUpload = true;
    int requestCode;
    private final WeakReference<Context> activityWeakReference;
    private final JSONObject featureAttachmentResultJsonObject;
    private final String accessToken;
    private final String userName;
    private final IUpload iUpload;
    private final String className = "UploadLogs";

    public UploadLogs(Context context, String userName, String accessToken, JSONObject featureAttachmentResultJsonObject, IUpload iUpload, int requestCode, boolean isInitialLogUpload) {
        this.activityWeakReference = new WeakReference<>(context);
        this.featureAttachmentResultJsonObject = featureAttachmentResultJsonObject;
        this.accessToken = accessToken;
        this.userName = userName;
        this.iUpload = iUpload;
        this.requestCode = requestCode;
        this.isInitialLogUpload = isInitialLogUpload;
    }

    @Override
    protected JSONObject doInBackground(Void... objects) {
        return uploadUserLog(activityWeakReference.get());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ReveloLogger.info(className, "onPreExecute", " upload logs part begin -- ");
    }

    @Override
    protected void onPostExecute(JSONObject uploadLogResponseJson) {
        super.onPostExecute(uploadLogResponseJson);

        if (isInitialLogUpload) {
            ReveloLogger.info(className, "uploadUserLog", "finished first time log upload...");
        }
        else {
            ReveloLogger.info(className, "uploadUserLog", "moving ahead to show results..");
          if(iUpload!=null && uploadLogResponseJson!=null) {
            iUpload.showUploadResultDialog(uploadLogResponseJson, requestCode);
        }
    }
    }

    private JSONObject uploadUserLog(Context context) {

        String responseMsg = "";

        try {
            ReveloLogger.info(className, "uploadUserLog", "getting log file ");
            File logFile = ReveloLogger.getLogFile(context);

            if (logFile != null) {
                ReveloLogger.info(className, "uploadUserLog", "creating zip");
                File logZipFile = AppFolderStructure.createLogZipFile(context);

                AppFolderStructure.createZipFile(logFile, logZipFile, true);

                ReveloLogger.info(className, "uploadUserLog", "getting upload logfile url");
                String url = UrlStore.getLogUploadUrl(userName);

                responseMsg = UploadFile.uploadFile(logZipFile.getName(), accessToken, url, logZipFile, context, new UploadFile.ShowProgress() {
                    @Override
                    public void progress(long bytesUploaded, long fileLength) {
                    }
                });
            }
            else {
                responseMsg = "Log file not found.";
                ReveloLogger.error(className, "uploadUserLog", "Error getting log file");
            }

        } catch (Exception e) {
            ReveloLogger.error(className, "uploadUserLog", "Exception uploading log file - " + e.getMessage());
            e.printStackTrace();
            responseMsg = e.getMessage();
        }

        JSONObject uploadLogResponseJson = new JSONObject();

        try {

            if (responseMsg != null) {

                if (responseMsg.equalsIgnoreCase(AppConstants.SUCCESS)) {
                    ReveloLogger.info(className, "uploadUserLog", "logs uploaded successfully..");
                    uploadLogResponseJson.put(AppConstants.STATUS, AppConstants.SUCCESS);
                    AppFolderStructure.deleteLogFolder(context, ! isInitialLogUpload);

                }
                else {
                    ReveloLogger.info(className, "uploadUserLog", "logs could not be uploaded..");
                    uploadLogResponseJson.put(AppConstants.STATUS, AppConstants.FAILURE);
                    uploadLogResponseJson.put(AppConstants.FAILURE_MESSAGE, responseMsg);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (! isInitialLogUpload && featureAttachmentResultJsonObject!=null) {
                ReveloLogger.info(className, "uploadUserLog", "adding upload logs response to basic upload response/results");
                featureAttachmentResultJsonObject.put(AppConstants.LOG, uploadLogResponseJson);
            }
        } catch (Exception e) {
            ReveloLogger.info(className, "uploadUserLog", "Exception adding upload logs response to basic upload response - " + e.getMessage());
            e.printStackTrace();
        }

        return featureAttachmentResultJsonObject;
    }
}