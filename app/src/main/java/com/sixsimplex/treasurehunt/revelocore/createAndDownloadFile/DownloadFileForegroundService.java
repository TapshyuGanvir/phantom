package com.sixsimplex.treasurehunt.revelocore.createAndDownloadFile;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.initialsetup.InitializationActivity;
import com.sixsimplex.treasurehunt.revelocore.util.AppController;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.DataFormatException;
import java.util.zip.ZipException;

import javax.net.ssl.HttpsURLConnection;


public class DownloadFileForegroundService extends Service {

    public static final int CHANNEL_ID = 123;
    private static Timer timer;

    private ResultReceiver receiver;
    private static long previousTotal, currentTotal, speed;
    private Notification notification;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private String fileName;

    public static final int UPDATE_PROGRESS = 8344;
    public static final int DOWNLOAD_SPEED_SIZE = 8343;
    public static final int FINAL_DOWNLOAD = 8345;
    public static final int FAIL_DOWNLOAD = 8346;

    public static final int CREATE_DATA_DB_CHANGE_PROGRESS_MESSAGE = 51;
    public static final int CREATED_META_DATA_DB_SUCCESS = 52;
    public static final int CREATED_DATA_DB_SUCCESS = 59;
    public static final int ERROR_CREATING_DATA_DB = 53;
    public static final int ERROR_CREATING_META_DATA_DB = 54;

    public static final int ERROR_DOWNLOADING_REGP = 56;
    public static final int ERROR_DOWNLOADING_DATAGP = 57;
    public static final int ERROR_DOWNLOADING_METADATAGP = 58;

    public static final String JURISDICTION = "jurisdiction";
    public static final String CREATE_FILE = "createFile";

    private String className = "DownloadFileForegroundService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ReveloLogger.debug(className, "onStartCommand", "Start command received for..");
        String message = "Creating Data DB... Please wait.";
        notificationBuilder = showNotification(message);
        downloadFile(intent);

        return START_STICKY;
    }

    private void downloadFile(Intent intent) {

        String reDbUrl = intent.getStringExtra("url");
        String accessToken = intent.getStringExtra("accessToken");
        receiver = intent.getParcelableExtra("receiver");
        String dbFolderPath = intent.getStringExtra("dbFolder");
        fileName = intent.getStringExtra("fileName");
        String fileType = intent.getStringExtra("fileType");
        int operationType = intent.getIntExtra("operationType", -1);

        ReveloLogger.debug(className, "downloadFile", "Downloading file " + fileName);

        String jurisdictions = intent.getStringExtra(JURISDICTION);

        ReveloLogger.debug(className, "downloadFile", "dbfolderpath "+dbFolderPath+"\nfilename "+fileName+"\nfiletype "+fileType+" operation type "+operationType);

        if (fileName.equalsIgnoreCase(CREATE_FILE)) {
            ReveloLogger.debug(className, "downloadFile", "request received for creating file..");
            try {
                ReveloLogger.debug(className, "downloadFile", "setting notification..");
                String message = "Creating Data DB... Please wait.";
                notificationBuilder = showNotification(message);
//                changeNotificationText("Data DB Creating Please wait...");
                JSONObject jurisdictionJsonObject = new JSONObject(intent.getStringExtra(JURISDICTION));

                if (fileType.equalsIgnoreCase(AppConstants.CREATE_DATA_GP_FILE)) {
                    ReveloLogger.debug(className, "downloadFile", "request received for creating data geopackage file..");
                    callCreateDataGp(operationType,fileType, jurisdictionJsonObject);
                } else {
                    ReveloLogger.debug(className, "downloadFile", "request received for creating meta geopackage file..");
                    callCreateMetaDataFile(operationType,fileName, receiver, fileType, jurisdictionJsonObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "downloadFile", "While downloading file " + e.getCause());
            }

        } else {
            ReveloLogger.debug(className, "downloadFile", "request received for downloading re geopackage file..");
            File reDbFolder = new File(dbFolderPath);
            File downloadedFile = new File(reDbFolder + File.separator + fileName);
            try {
                downloadedFile.createNewFile();  //create file
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.debug(className, "downloadFile", "While downloading file " + e.getCause());
            }

            String message = "";

            if (fileName.contains(AppConstants.DATA_GP_FILE)) {
                message = "Downloading "+ UserInfoPreferenceUtility.getPhaseORSurveyName() +" data... Please wait.";
            } else if (fileName.contains(AppConstants.METADATA_FILE)) {
                message = "Downloading "+ UserInfoPreferenceUtility.getPhaseORSurveyName() +" configuration... Please wait.";
            } else if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
                String orgLabel = UserInfoPreferenceUtility.getOrgLabel();
                if(orgLabel.isEmpty()){
                    orgLabel="your Organization";
                }
                message = "Provisioning "+orgLabel+"'s Administrative boundaries... Please wait.";
            }
            ReveloLogger.debug(className, "downloadFile", "calling download task..");
            notificationBuilder = showNotification(message);
            new DownloadDatabaseAsyncTask(reDbUrl, accessToken,
                    receiver, downloadedFile, reDbFolder, notificationBuilder, fileName, jurisdictions, operationType).execute();
        }
    }

    private void callCreateDataGp(int operationType,String fileType, JSONObject jurisdictionJsonObject) {
        ReveloLogger.debug(className, "callCreateDataGp", "filetype "+fileType+" ; jurisdictionJObj "+jurisdictionJsonObject.toString());
        String surveyName = UserInfoPreferenceUtility.getSurveyName();
        String createDbUrl = UrlStore.createDatabaseDbUrl(surveyName);

        new CreatingDatabaseFile(createDbUrl, jurisdictionJsonObject, getApplicationContext(), operationType,"DataDb", new IDatabaseCreateInterface() {

            @Override
            public void successFileDownload(int requestCode,String message) {
                ReveloLogger.debug(className, "callCreateMetaDataFile", "Created metadaatadb file successfully..filename "+fileName);
                Bundle resultData = new Bundle();
                resultData.putString("fileName", fileName);
                resultData.putString("fileType", fileType);
                resultData.putInt("requestCode", requestCode);
                resultData.putString(JURISDICTION, jurisdictionJsonObject.toString());
                receiver.send(CREATED_DATA_DB_SUCCESS, resultData);
                stopForeground(true);
                stopSelf();
            }


            @Override
            public void errorFileDownload(int requestCode,int errorCode,String message) {
                Bundle resultData = new Bundle();
                resultData.putString("fileName", fileName);
                resultData.putString("fileType", fileType);
                resultData.putString("errorMessage", message);
                resultData.putInt("requestCode", requestCode);
                resultData.putString(JURISDICTION, jurisdictionJsonObject.toString());
                ReveloLogger.error(className, "callCreateDataGp", "error creating data geopackage on server..message = "+message);
                receiver.send(ERROR_CREATING_DATA_DB, resultData);
                stopForeground(true);
                stopSelf();
            }
        });
    }

    private void callCreateMetaDataFile(int operationType, String fileName, ResultReceiver receiver, String fileType, JSONObject jurisdictionJsonObject) {
        ReveloLogger.debug(className, "callCreateMetaDataFile", "filename "+fileName+" filetype "+fileType+" jurisdictionJobj "+jurisdictionJsonObject);
        String surveyName = UserInfoPreferenceUtility.getSurveyName();
        String createMetaDbUrl = UrlStore.createMetaDatabaseUrl(surveyName);
        ReveloLogger.debug(className, "callCreateMetaDataFile", "Creating url from surveyname..");

        Bundle resultData = new Bundle();
        resultData.putString("fileName", fileName);
        receiver.send(CREATE_DATA_DB_CHANGE_PROGRESS_MESSAGE, resultData);
        changeNotificationText("Retrieving project information... Please wait...");

        new CreatingDatabaseFile(createMetaDbUrl, jurisdictionJsonObject, getApplicationContext(),
                operationType,"MetaData", new IDatabaseCreateInterface() {

            @Override
            public void successFileDownload(int requestCode,String message) {
                ReveloLogger.debug(className, "callCreateMetaDataFile", "Created metadaatadb file successfully..filename "+fileName);
                resultData.putString("fileName", fileName);
                resultData.putString("fileType", fileType);
                resultData.putInt("requestCode", requestCode);
                resultData.putString(JURISDICTION, jurisdictionJsonObject.toString());
                receiver.send(CREATED_META_DATA_DB_SUCCESS, resultData);
                stopForeground(true);
                stopSelf();
            }

            @Override
            public void errorFileDownload(int requestCode,int errorCode,String message) {
                Bundle resultData = new Bundle();
                resultData.putString("fileName", fileName);
                resultData.putString("errorMessage", message);
                resultData.putInt("requestCode", requestCode);
                resultData.putString("fileType", fileType);
                ReveloLogger.error(className, "callCreateMetaDataFile", "Error Creating metadaatadb file successfully..message= "+message);
                receiver.send(ERROR_CREATING_META_DATA_DB, resultData);
                stopForeground(true);
                stopSelf();
            }
        });
    }

    private JSONObject downloadFile(String accessToken, String sourceUrl, File downloadedFile,
                                    ResultReceiver receiver, NotificationCompat.Builder notificationBuilder) {

        JSONObject responseJSON = new JSONObject();

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            responseJSON.put(AppConstants.STATUS, AppConstants.FAILURE);
            responseJSON.put(AppConstants.FAILURE_MESSAGE, "unknown");

            URL url = new URL(sourceUrl);

            if (sourceUrl.contains("https://")) {
                connection = (HttpsURLConnection) url.openConnection();
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.connect();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                final int fileLength = connection.getContentLength();//display download percentage might be -1 server did not report the length

                input = connection.getInputStream();
                output = new FileOutputStream(downloadedFile);

                byte[] data = new byte[4096];
                long total = 0;
                int count;
                int progress;

                String fileSizeString = "";
                if (fileLength != -1) {
                    double fileSize = Double.parseDouble(String.valueOf(fileLength / 1000.0 / 1000.0));
                    fileSizeString = new DecimalFormat("###.###").format(fileSize) + " MB";
                }

                final String finalFileSizeString = fileSizeString;

                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        try {

                            speed = currentTotal - previousTotal;
                            Bundle resultData = new Bundle();
                            resultData.putInt("speedData", Integer.parseInt(String.valueOf(speed)));
                            resultData.putInt("downlaodSizeData", Integer.parseInt(String.valueOf(currentTotal)));
                            resultData.putString("finalFileSizeString", finalFileSizeString);
                            resultData.putString("fileName", fileName);
                            receiver.send(DOWNLOAD_SPEED_SIZE, resultData);
                            previousTotal = currentTotal;


                        } catch (Exception e) {
                            e.printStackTrace();
                            ReveloLogger.error(className, "downloadFile", "While downloading file " + e.getCause());
                        }
                    }
                }, 0, 1000);

                while ((count = input.read(data)) != -1) {

                    total += count;
                    currentTotal = total;

                    if (fileLength != -1) {
                        progress = (int) (total * 100 / fileLength);
                        Bundle resultData = new Bundle();
                        resultData.putInt("progress", progress);
                        resultData.putString("fileName", fileName);

                        notificationBuilder.setProgress(100, progress, false);
                        notification = notificationBuilder.build();
                        assert mNotificationManager != null;
                        mNotificationManager.notify(CHANNEL_ID, notification);

                        receiver.send(UPDATE_PROGRESS, resultData); //pass progress result to activity.
                    }

                    output.write(data, 0, count);
                }

                timer.cancel();

                if (downloadedFile.exists()) {
                    responseJSON.put(AppConstants.STATUS, AppConstants.SUCCESS);
                } else {

                    String errorMessage = "Could not find the downloaded file.";
                    responseJSON.put(AppConstants.STATUS, AppConstants.FAILURE);
                    responseJSON.put(AppConstants.FAILURE_MESSAGE, errorMessage);
                }

            } else {

                if (timer != null) {
                    timer.cancel();
                }
                String responseMsg = "Request to download data failed. " + connection.getResponseMessage();

                responseJSON.put(AppConstants.STATUS, AppConstants.FAILURE);
                responseJSON.put(AppConstants.FAILURE_MESSAGE, responseMsg);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            ReveloLogger.error(className, "downloadFile", "While downloading file " + exception.getCause());

            if (timer != null) {
                timer.cancel();
            }

            try {

                String errorMessage = exception.getMessage();

                if (exception instanceof BindException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (Bind)";
                } else if (exception instanceof ConnectException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_connect_exception) + " (Connect)";
                } else if (exception instanceof HttpRetryException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (HttpRetry)";
                } else if (exception instanceof MalformedURLException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (MalformedURL)";
                } else if (exception instanceof NoRouteToHostException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (NoRouteToHost)";
                } else if (exception instanceof PortUnreachableException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (PortUnreachable)";
                } else if (exception instanceof ProtocolException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (Protocol)";
                } else if (exception instanceof SocketException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (Socket)";
                } else if (exception instanceof SocketTimeoutException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (SocketTimeout)";
                } else if (exception instanceof UnknownHostException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownHost)";
                } else if (exception instanceof UnknownServiceException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownService)";
                } else if (exception instanceof DataFormatException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (DataFormat)";
                } else if (exception instanceof ZipException) {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception) + " (Zip)";
                } else {
                    errorMessage = AppController.getInstance().getString(R.string.failed_to_connect_server_timeout_exception);
                }

                responseJSON.put(AppConstants.STATUS, AppConstants.FAILURE);
                responseJSON.put(AppConstants.FAILURE_MESSAGE, errorMessage);

            } catch (Exception ee) {
                ee.printStackTrace();
                ReveloLogger.error(className, "downloadFile", "While downloading file " + exception.getCause());
                if (timer != null) {
                    timer.cancel();
                }

            }

        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }

                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "downloadFile", "While downloading file " + e.getCause());
            }
        }

        return responseJSON;
    }

    public class DownloadDatabaseAsyncTask extends AsyncTask<String, String, JSONObject> {

        private String url;
        private String accessToken;
        private ResultReceiver receiver;
        private File downloadFileName;
        private File dbFolder;
        private String fileName;
        private String jurisdictions;
        private int operationType;
        private NotificationCompat.Builder notificationBuilder;


        public DownloadDatabaseAsyncTask(String url, String accessToken,
                                         ResultReceiver receiver,
                                         File downloadFileName,
                                         File dbFolder,
                                         NotificationCompat.Builder notificationBuilder,
                                         String fileName, String jurisdictions, int operationType) {

            this.accessToken = accessToken;
            this.url = url;
            this.receiver = receiver;
            this.downloadFileName = downloadFileName;
            this.dbFolder = dbFolder;
            this.fileName = fileName;
            this.operationType = operationType;
            this.jurisdictions = jurisdictions;
            this.notificationBuilder = notificationBuilder;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONObject doInBackground(String... params) {
            return downloadFile(accessToken, url, downloadFileName, receiver, notificationBuilder);
        }

        @Override
        protected void onPostExecute(JSONObject responseJsonObject) {
            super.onPostExecute(responseJsonObject);
            try {

                String status = responseJsonObject.getString(AppConstants.STATUS);

                if (status.equalsIgnoreCase(AppConstants.SUCCESS)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("downloadedFile", downloadFileName.getAbsolutePath());
                    bundle.putString("destinationFolder", dbFolder.getAbsolutePath());
                    bundle.putString("fileName", fileName);
                    bundle.putInt("requestCode", operationType);
                    bundle.putString(JURISDICTION, jurisdictions);
                    receiver.send(FINAL_DOWNLOAD, bundle);
                    stopForeground(true);
                    stopSelf();

                    if (fileName.contains(AppConstants.DATA_GP_FILE)) {

//                        new selectedJurisdictionGeometry(DownloadFileForegroundService.this, operationType).execute();

                        String notificationMessage = "Data Downloaded Successfully";
                        if (operationType == AppConstants.REFRESH_DATA_REQUEST) {
                            notificationMessage = "Refresh Data Successfully";
                        }
                        else if (operationType == AppConstants.CHANGE_JURISDICTION_REQUEST) {
                            notificationMessage = "Jurisdiction change  successfully.";
                        }
                        else if (operationType == AppConstants.CHANGE_SURVEY_REQUEST) {
                            notificationMessage = "Project changed successfully.";
                        }
                        else if (operationType == AppConstants.CHANGE_SURVEY_PHASE_REQUEST) {
                            notificationMessage = "Project's phase changed successfully.";
                        }
                        changeNotificationText(notificationMessage);

                        ReveloLogger.info(className, "DownloadDatabaseAsyncTask", notificationMessage);
                    }


                } else {
                    String errorMessage = responseJsonObject.has(AppConstants.FAILURE_MESSAGE) ?
                            responseJsonObject.getString(AppConstants.FAILURE_MESSAGE) : "UNKNOWN ERROR";
                    Bundle bundle = new Bundle();
                    bundle.putString("errorMessage", errorMessage);
                    bundle.putString("fileName", fileName);
                    bundle.putInt("requestCode", operationType);
                    bundle.putString(JURISDICTION, jurisdictions);
                    receiver.send(FAIL_DOWNLOAD, bundle);
                    stopForeground(true);
                    stopSelf();
                }

            } catch (Exception e) {
                e.printStackTrace();
                stopForeground(true);
                stopSelf();
                ReveloLogger.error(className, "DownloadDatabaseAsyncTask", "While downloading file " + e.getCause());
            }
        }

    }

    private NotificationCompat.Builder showNotification(String message) {

        Intent notificationIntent = new Intent(this, InitializationActivity.class);
        /*PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);*/
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        }
        else
        {
            pendingIntent = PendingIntent.getActivity
                    (this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        }
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {

            String notificationId = "Revelo Notify", channelName = "Revelo Channel",
                    notificationDescription = "Notification for Download File.";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(notificationId, channelName,
                        NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(notificationDescription);
                mNotificationManager.createNotificationChannel(channel);
            }

            notificationBuilder = new NotificationCompat.Builder(this, notificationId)
                    .setContentTitle(message)
                    .setSmallIcon(R.drawable.revelo_logo_small)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setNotificationSilent()
            .setOnlyAlertOnce(true);

            notification = notificationBuilder.build();
            startForeground(CHANNEL_ID, notification);
        }
        return notificationBuilder;
    }

    private void changeNotificationText(String text) {
        if (notificationBuilder != null) {
            notificationBuilder.setContentTitle(text);
            if (mNotificationManager != null) {
                mNotificationManager.notify(CHANNEL_ID, notificationBuilder.build());
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mNotificationManager!=null && notification!=null){
            mNotificationManager.cancel(CHANNEL_ID);
        }
    }
    /*

    private class selectedJurisdictionGeometry extends AsyncTask<String, String, Boolean> {

        private Context context;
        private int operationType;

        public selectedJurisdictionGeometry(Context context, int operationType) {
            this.context = context;
            this.operationType = operationType;
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            String assignedJurisdictionName = UserInfoPreferenceUtility.getJurisdictionName();
            String assignedJurisdictionType = UserInfoPreferenceUtility.getJurisdictionType();

            String selectedJurisdictionName = JurisdictionInfoPreferenceUtility.getSelectedJurisdictionName();
            String selectedJurisdictionType = JurisdictionInfoPreferenceUtility.getSelectedJurisdictionType();

            if (assignedJurisdictionName.equalsIgnoreCase(selectedJurisdictionName) && assignedJurisdictionType.equalsIgnoreCase(selectedJurisdictionType)) {

                Log.e("selected Jurisdictions", "Assigned and selected jurisdiction are same");
            } else {

                String selectedJurisdiction = JurisdictionInfoPreferenceUtility.getJurisdictions();
                JSONObject selectedJurisdictionJson = null;
                try {
                    selectedJurisdictionJson = new JSONObject(selectedJurisdiction);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Graph graph = OrgBoundaryConceptModel.getObReGraph();
                Vertex vertex = TinkerGraphUtil.findRootVertex(graph);

                Geometry geometry = ReDbTable.getUserSelectedJurisdiction(context, selectedJurisdictionJson, vertex);
                // Log.e("polygonList", polygonList.size() + "");
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            String notificationMessage = "Data Downloaded Successfully";
            if (operationType == AppConstants.REFRESH_DATA_REQUEST) {
                notificationMessage = "Refresh Data Successfully";
            }
            else if (operationType == AppConstants.CHANGE_JURISDICTION_REQUEST) {
                notificationMessage = "Jurisdiction change  successfully.";
            }
            else if (operationType == AppConstants.CHANGE_SURVEY_REQUEST) {
                notificationMessage = "Survey change successfully.";
            }
            changeNotificationText(notificationMessage);

            changeNotificationText(notificationMessage);

        }
    }
*/

}