package com.sixsimplex.treasurehunt.Phantom1.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.sixsimplex.treasurehunt.revelocore.createAndDownloadFile.DownloadFileForegroundService;
import com.sixsimplex.treasurehunt.revelocore.initialsetup.InitializationActivity;
import com.sixsimplex.treasurehunt.revelocore.principalEndpoint.PrincipalEndpoint;
import com.sixsimplex.treasurehunt.revelocore.principalEndpoint.view.IPrincipleEndpointView;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.SurveyDetails;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.view.ISurveyDetails;
import com.sixsimplex.treasurehunt.revelocore.userProfile.FetchUserProfile;
import com.sixsimplex.treasurehunt.revelocore.userProfile.FetchUserProfileListener;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.JurisdictionInfoPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class Refreshdata {
    private static IrefreshResponce irefreshResponce;
    public static void refreshData(Activity activity, int requestType, IrefreshResponce irefreshResponce, boolean refreshPrincipal, boolean refreshSurvey, IPrincipleEndpointView iPrincipleEndpointView, ISurveyDetails iSurveyDetails) {
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(refreshPrincipal){
                        String accessToken = SecurityPreferenceUtility.getAccessToken();
                        new PrincipalEndpoint(activity, 1, "all", accessToken, iPrincipleEndpointView);
                    }
                    if(refreshSurvey){
                        String surveyName = UserInfoPreferenceUtility.getSurveyName();
                        new SurveyDetails(activity, requestType, surveyName, iSurveyDetails);
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
        createGeoPackageForRefresh(activity, AppConstants.REFRESH_DATA_REQUEST);
        Refreshdata.irefreshResponce=irefreshResponce;
    }

    private static void createGeoPackageForRefresh(Context context, int requestType) {
        String filterJson = JurisdictionInfoPreferenceUtility.getJurisdictions();
        createGeoPackage(context,filterJson, AppConstants.CREATE_DATA_GP_FILE,requestType);
    }


    public static void createGeoPackage(Context context, String selectedJurisdiction, String fileType, int requestType) {
        String accessToken = SecurityPreferenceUtility.getAccessToken();
        Intent intent = new Intent(context, DownloadFileForegroundService.class);
        intent.putExtra("url", "");
        intent.putExtra("accessToken", accessToken);
        intent.putExtra("dbFolder", "");
        intent.putExtra("fileName", "createFile");
        intent.putExtra("fileType", fileType);
        intent.putExtra("operationType", requestType);
        intent.putExtra(DownloadFileForegroundService.JURISDICTION, selectedJurisdiction);
        intent.putExtra("receiver", new DownloadReceiver(context,requestType,new Handler()));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        }
        else {
            context.startService(intent);
        }
    }

    private static class DownloadReceiver extends ResultReceiver {
        Context context;
        int requestType;

        public DownloadReceiver(Context context, int requestType, Handler handler) {
            super(handler);
            this.context=context;
            this.requestType=requestType;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            super.onReceiveResult(resultCode, resultData);

            if (resultData != null) {
                String fileName = resultData.getString("fileName");
                if (! TextUtils.isEmpty(fileName)) {

                    if (fileName.equalsIgnoreCase(DownloadFileForegroundService.CREATE_FILE)) { // used for to create metadata and data db.
                        if (resultCode == DownloadFileForegroundService.CREATE_DATA_DB_CHANGE_PROGRESS_MESSAGE) {

                        }
                        else if (resultCode == DownloadFileForegroundService.CREATED_META_DATA_DB_SUCCESS) {

                            //ProgressUtility.dismissProgressDialog(progressUtility);

                            String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);
                            downloadFile(context, UserInfoPreferenceUtility.getMetatdataDbName(), requestType, jurisdictions);
                        }
                        else if (resultCode == DownloadFileForegroundService.CREATED_DATA_DB_SUCCESS) {

                            //ProgressUtility.dismissProgressDialog(progressUtility);

                            String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);
                            downloadFile(context, UserInfoPreferenceUtility.getDataDbName(), requestType, jurisdictions);
                        }
                        else if (resultCode == DownloadFileForegroundService.ERROR_CREATING_META_DATA_DB) {


                            String errorMessage = resultData.getString("errorMessage");
                            int originalRequestCode = resultData.getInt("requestCode");
                            String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);



                        }
                        else if (resultCode == DownloadFileForegroundService.ERROR_CREATING_DATA_DB) {
                            String errorMessage = resultData.getString("errorMessage");
                            int originalRequestCode = resultData.getInt("requestCode");
                            String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);
                        }


                    }
                    else {  // else part used for to downlaod file.
                        if (resultCode == DownloadFileForegroundService.UPDATE_PROGRESS) {
                            int progress = resultData.getInt("progress"); //get the progress
//                            percentageProgressBar.setDownloadProgress(progress);

                        }
                        else if (resultCode == DownloadFileForegroundService.DOWNLOAD_SPEED_SIZE) {

                            int downloadSpeed = resultData.getInt("speedData");
                            int downlaodSizeData = resultData.getInt("downlaodSizeData");
                            String finalFileSizeString = resultData.getString("finalFileSizeString");
//                            percentageProgressBar.setDownloadSize(downlaodSizeData, finalFileSizeString);
//                            percentageProgressBar.setDownloadSpeed(downloadSpeed);

                        }
                        else if (resultCode == DownloadFileForegroundService.FINAL_DOWNLOAD) {  //successfully file dowlaod

                            String downloadedFile = resultData.getString("downloadedFile");
                            String destinationFolder = resultData.getString("destinationFolder");
                            String jurisdiction = resultData.getString(DownloadFileForegroundService.JURISDICTION);
                            fileDownloadSuccess(context,downloadedFile, destinationFolder, fileName);

                            if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
                                UserInfoPreferenceUtility.setReBbRequired(false);
                                ///AppMethods.clearRedbRelatedStaticVariables();

                                if (requestType == AppConstants.REFRESH_DATA_REQUEST) {
                                    String filterJson = JurisdictionInfoPreferenceUtility.getJurisdictions();
                                    createGeoPackageForRefresh(context, requestType);
                                }
                                else {
//                                    showJurisdictionDialog();//redb downloaded.
                                }
                            }
                            else if (fileName.contains(AppConstants.DATA_GP_FILE)) {
                                AppMethods.clearDataDbRelatedStaticVariables();
                                if (requestType == AppConstants.REFRESH_DATA_REQUEST) {
                                    String filterJson = JurisdictionInfoPreferenceUtility.getJurisdictions();
                                    createGeoPackage(context,filterJson, AppConstants.CREATE_META_GP_FILE, AppConstants.REFRESH_DATA_REQUEST);
                                }
                                else {
                                    String message = "Data downloaded successfully";
                                    JurisdictionInfoPreferenceUtility.storeJurisdictions(jurisdiction);
                                }
                            }
                            else {
                                AppMethods.clearMetadataDbRelatedStaticVariables();
                                if (requestType == AppConstants.REFRESH_DATA_REQUEST) {
                                    String message = "Data refreshed successfully";
                                    irefreshResponce.onSuccessResponse();
                                    JurisdictionInfoPreferenceUtility.storeJurisdictions(jurisdiction);
                                }
                            }

                        }
                        else if (resultCode == DownloadFileForegroundService.FAIL_DOWNLOAD) {
//                            percentageProgressBar.dismiss();
                            String errorMessage = resultData.getString("errorMessage");
                            int originalRequestCode = resultData.getInt("requestCode");
                            String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);

                            if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
                                irefreshResponce.onFailedResponse(errorMessage);
//                                errorMessage(errorMessage, originalRequestCode, DownloadFileForegroundService.ERROR_DOWNLOADING_REGP, jurisdictions);
                            }
                            else if (fileName.contains(AppConstants.DATA_GP_FILE)) {
                                irefreshResponce.onFailedResponse(errorMessage);
//                                errorMessage(errorMessage, originalRequestCode, DownloadFileForegroundService.ERROR_DOWNLOADING_DATAGP, jurisdictions);
                            }
                            else if (fileName.contains(AppConstants.METADATA_FILE)) {
                                irefreshResponce.onFailedResponse(errorMessage);
//                                errorMessage(errorMessage, originalRequestCode, DownloadFileForegroundService.ERROR_DOWNLOADING_METADATAGP, jurisdictions);
                            }

                        }
                    }
                }
            }
        }
    }

    private static void fileDownloadSuccess(Context context, String downloadedFile, String destinationFolder, String fileName) {
        boolean isUnzipSuccessful = false;

        if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
            String reDbFilePath = AppFolderStructure.getReGpFilePath(context);
            File reDbFile = new File(reDbFilePath);
            if (reDbFile.exists()) {
                reDbFile.delete();
            }

            File file = new File(downloadedFile);
            try {
                isUnzipSuccessful = SystemUtils.unzip(context, file, destinationFolder, null);
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                FileUtils.forceDelete(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isUnzipSuccessful) {
                if(fileName.equalsIgnoreCase(AppConstants.METADATA_FILE)){
                    irefreshResponce.onSuccessResponse();
                }
            }
            else {
//            fileDownloadInterface.errorFileDownload("cannot unzip the downloaded zip file.");
                if(fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
                    irefreshResponce.onFailedResponse("Could not unzip " + fileName + ".");
//                    errorMessage("Could not unzip " + fileName + ".", AppConstants.DOWNLOAD_DATA_REQUEST, DownloadFileForegroundService.ERROR_DOWNLOADING_REGP, "");
                }else if(fileName.equalsIgnoreCase(AppConstants.DATA_GP_FILE)) {
                    irefreshResponce.onFailedResponse("Could not unzip " + fileName + ".");
//                    errorMessage("Could not unzip " + fileName + ".", AppConstants.DOWNLOAD_DATA_REQUEST, DownloadFileForegroundService.ERROR_CREATING_DATA_DB, "");
                }else if(fileName.equalsIgnoreCase(AppConstants.METADATA_FILE)) {
                    irefreshResponce.onFailedResponse("Could not unzip " + fileName + ".");
//                    errorMessage("Could not unzip " + fileName + ".", AppConstants.DOWNLOAD_DATA_REQUEST, DownloadFileForegroundService.ERROR_CREATING_META_DATA_DB, "");
                }
            }
        }

//        percentageProgressBar.dismiss();

    }

    private static void downloadFile(Context context, String fileName, int requestType, String jurisdictions) {

        String progressMessage = "";
        String fileDbUrl = null;
        File fileDbFolder = null;
        String surveyName = UserInfoPreferenceUtility.getSurveyName();

        if (fileName.contains(AppConstants.METADATA_FILE)) {
            if (requestType == AppConstants.REFRESH_DATA_REQUEST) {
                progressMessage = "Downloading refreshed configuration... Please wait.";
            }
            else {
                progressMessage = "Downloading configuration... Please wait.";
            }
            fileDbUrl = UrlStore.downloadMetaDatabaseUrl(surveyName);
            fileDbFolder = AppFolderStructure.createMetadataFolder(context);

        }
        else if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
            String orgLabel = UserInfoPreferenceUtility.getOrgLabel();
            if(orgLabel.isEmpty()){
                orgLabel="your Organization";
            }
            if (requestType == AppConstants.REFRESH_DATA_REQUEST) {
                progressMessage = "Downloading "+orgLabel+"'s Administrative boundaries freshly... Please wait.";
            }
            else {
                progressMessage = "Downloading "+orgLabel+"'s Administrative boundaries... Please wait.";
            }

            fileDbUrl = UrlStore.getREDatabaseUrl(surveyName);
            fileDbFolder = AppFolderStructure.createReDbFolder(context);

        }
        else if (fileName.contains(AppConstants.DATA_GP_FILE)) {
            if (requestType == AppConstants.REFRESH_DATA_REQUEST) {
                progressMessage = "Downloading refreshed data... Please wait.";
            }
            else {
                progressMessage = "Downloading "+UserInfoPreferenceUtility.getPhaseORSurveyName()+" data... Please wait.";
            }

            fileDbUrl = UrlStore.downloadDataGpUrl(surveyName);
            fileDbFolder = AppFolderStructure.createDataGpFolder(context);

        }

//        percentageProgressBar = new PercentageProgressBar(context, progressMessage, PercentageProgressBar.HORIZONTAL);
//        if (! this.isFinishing()) {
//            percentageProgressBar.show();
//        }

        String accessToken = SecurityPreferenceUtility.getAccessToken();
        Intent intent = new Intent(context, DownloadFileForegroundService.class);
        intent.putExtra("url", fileDbUrl);
        intent.putExtra("accessToken", accessToken);
        intent.putExtra(DownloadFileForegroundService.JURISDICTION, jurisdictions);
        intent.putExtra("dbFolder", fileDbFolder.getAbsolutePath());
        intent.putExtra("fileName", fileName);
        intent.putExtra("operationType", requestType);
        intent.putExtra("receiver",new DownloadReceiver(context, requestType, new Handler()));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent);
        }
        else {
            context.startService(intent);
        }
    }
}
