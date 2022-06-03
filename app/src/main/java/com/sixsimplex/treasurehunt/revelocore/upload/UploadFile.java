package com.sixsimplex.treasurehunt.revelocore.upload;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.progressdialog.PercentageProgressBar;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import javax.net.ssl.HttpsURLConnection;

public class UploadFile {
    private static String className = "UploadFile";
    private static long previousTotal, currentTotal, speed;

    public static String uploadFile(String fileName, String token, String urlString, File sourceFile, Context context, ShowProgress showProgress) {

        String response = "";

        HttpURLConnection urlConnection;
        DataOutputStream dos;

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        byte[] buffer;

        int bytesRead, bytesAvailable, bufferSize;
        int maxBufferSize = 1024 * 1024;
        int timeout = 60000;//1Min

        try {

            if (sourceFile.exists()) {

                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(urlString);

                if (urlString.contains("https://")) {
                    urlConnection = (HttpsURLConnection) url.openConnection();
                } else {
                    urlConnection = (HttpURLConnection) url.openConnection();
                }

                if (!TextUtils.isEmpty(token)) {
                    urlConnection.setRequestProperty("Authorization", "Bearer " + token);
                }

                urlConnection.setDoInput(true); // Allow Inputs
                urlConnection.setDoOutput(true); // Allow Outputs
                urlConnection.setUseCaches(false); // Don't use a Cached Copy
                urlConnection.setConnectTimeout(timeout);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                urlConnection.setRequestProperty("uploaded_file", fileName);
                urlConnection.setRequestProperty("Accept-Encoding", "gzip");

                dos = new DataOutputStream(urlConnection.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=uploaded_file;filename=" + fileName + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                long bytesUploaded = 0;
                while (bytesRead > 0) {
                    try {
                        dos.write(buffer, 0, bufferSize);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesUploaded += bufferSize;
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    showProgress.progress(bytesUploaded, sourceFile.length());
                }

                // send multipart form data necessary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = urlConnection.getResponseCode();

                //close the streams
                fileInputStream.close();
                dos.flush();
                dos.close();

                ReveloLogger.info("UploadFile", "uploadFile", "StatusCode " + serverResponseCode);

                if (serverResponseCode == 200 || serverResponseCode == 201) {
                    response = AppConstants.SUCCESS;

                } else {
                    response = AppConstants.FAILURE;
                }
            } else {
                response = "Source file not found.";
            }

        } catch (Exception exception) {

            exception.printStackTrace();
            try {

                if (exception instanceof BindException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Bind)";
                } else if (exception instanceof ConnectException) {
                    response = context.getString(R.string.failed_to_connect_server_connect_exception) + " (Connect)";
                } else if (exception instanceof HttpRetryException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (HttpRetry)";
                } else if (exception instanceof MalformedURLException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (MalformedURL)";
                } else if (exception instanceof NoRouteToHostException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (NoRouteToHost)";
                } else if (exception instanceof PortUnreachableException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (PortUnreachable)";
                } else if (exception instanceof ProtocolException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Protocol)";
                } else if (exception instanceof SocketException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Socket)";
                } else if (exception instanceof SocketTimeoutException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (SocketTimeout)";
                } else if (exception instanceof UnknownHostException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownHost)";
                } else if (exception instanceof UnknownServiceException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownService)";
                } else if (exception instanceof DataFormatException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (DataFormat)";
                } else if (exception instanceof ZipException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Zip)";
                } else {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception);
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        return response;
    }

    public static String doPutToSendJson(String url, String token, String json, Context context) {//only use for delete attachment

        String response = "";
        HttpURLConnection urlConnection = null;

        try {

            URL getUrl = new URL(url);

            if (url.contains("https://")) {
                urlConnection = (HttpsURLConnection) getUrl.openConnection();
            } else {
                urlConnection = (HttpURLConnection) getUrl.openConnection();
            }

            urlConnection.setConnectTimeout(60000);
            urlConnection.setDoOutput(false);
            urlConnection.setRequestMethod("PUT");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip");
            urlConnection.setRequestProperty("Content-type", "application/json");

            if (!TextUtils.isEmpty(token)) {
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            }

            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(json);
            writer.flush();

            InputStream inputStream;// = new BufferedInputStream(urlConnection.getInputStream());
            int statusCode = urlConnection.getResponseCode();

            if (statusCode >= 400) {
                inputStream = urlConnection.getErrorStream();
            } else {
                inputStream = urlConnection.getInputStream();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1), 8);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            inputStream.close();
            String result = sb.toString();

            if (statusCode == 201 || statusCode == 202) {
                response = AppConstants.SUCCESS;
            } else {
                response = AppConstants.FAILURE;
            }
        } catch (Exception exception) {
            exception.printStackTrace();

            try {
                if (exception instanceof BindException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Bind)";
                } else if (exception instanceof ConnectException) {
                    response = context.getString(R.string.failed_to_connect_server_connect_exception) + " (Connect)";
                } else if (exception instanceof HttpRetryException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (HttpRetry)";
                } else if (exception instanceof MalformedURLException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (MalformedURL)";
                } else if (exception instanceof NoRouteToHostException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (NoRouteToHost)";
                } else if (exception instanceof PortUnreachableException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (PortUnreachable)";
                } else if (exception instanceof ProtocolException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Protocol)";
                } else if (exception instanceof SocketException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Socket)";
                } else if (exception instanceof SocketTimeoutException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (SocketTimeout)";
                } else if (exception instanceof UnknownHostException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownHost)";
                } else if (exception instanceof UnknownServiceException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownService)";
                } else if (exception instanceof DataFormatException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (DataFormat)";
                } else if (exception instanceof ZipException) {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Zip)";
                } else {
                    response = context.getString(R.string.failed_to_connect_server_timeout_exception);
                }

            } catch (Exception ee) {
                ee.printStackTrace();
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return response;
    }

    public static JSONObject doPostToSendJson(String url, String json, Context context) {

        JSONObject responseJsonObject = new JSONObject();
        HttpURLConnection urlConnection = null;

        try {

            responseJsonObject.put("status", "failure");
            responseJsonObject.put("message", "unknown error.");

            URL getUrl = new URL(url);

            if (url.contains("https://")) {
                urlConnection = (HttpsURLConnection) getUrl.openConnection();
                //prepareConnection((HttpsURLConnection) urlConnection);//configure for https
            } else {
                urlConnection = (HttpURLConnection) getUrl.openConnection();
            }

            urlConnection.setConnectTimeout(60000);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip");
            urlConnection.setRequestProperty("Content-type", "application/json");

            String token = SecurityPreferenceUtility.getAccessToken();
            if (!TextUtils.isEmpty(token)) {
                urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            }

            OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
            writer.write(json);
            writer.flush();

            int statusCode = urlConnection.getResponseCode();

            if (statusCode == 401) {

                responseJsonObject.put("status", "failure");
                responseJsonObject.put("message", context.getString(R.string.unauthorized_access));

                return responseJsonObject;
            } else if (statusCode == 403) {

                responseJsonObject.put("status", "failure");
                responseJsonObject.put("message", context.getString(R.string.permission_denied));

                return responseJsonObject;
            }

            /*InputStream is;
            int status = urlConnection.getResponseCode();
            if (status >= 400) {
                is = urlConnection.getErrorStream();
            } else {
                is = urlConnection.getInputStream();
               *//* if (status == 204) {
                    //no content
                } else {
                    try {
                        is = new GZIPInputStream(is);//for ("Accept-Encoding", "gzip");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }*//*
            }

            is = new GZIPInputStream(is);//for ("Accept-Encoding", "gzip");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
             */

            InputStream responseStream;
            String encoding = urlConnection.getHeaderField("Content-Encoding");
            boolean gzipped = encoding != null && encoding.toLowerCase().contains("gzip");

            try {

                InputStream inputStream = urlConnection.getInputStream();
                if (gzipped) {
                    responseStream = new BufferedInputStream(new GZIPInputStream(inputStream));
                } else {
                    responseStream = new BufferedInputStream(inputStream);
                }

            } catch (Exception e) {
                // error stream
                InputStream errorStream = urlConnection.getErrorStream();
                if (gzipped) {
                    responseStream = new BufferedInputStream(new GZIPInputStream(errorStream));
                } else {
                    responseStream = new BufferedInputStream(errorStream);
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            responseStream.close();
            String responseString = sb.toString();

            if (statusCode == 201 || statusCode == 202 || statusCode == 204) {
                ReveloLogger.info(className, "doPostToSendJson", "StatusCode " + statusCode);
                responseJsonObject.put("status", "Success");
                responseJsonObject.put("message", responseString);

                return responseJsonObject;
            } else {
                ReveloLogger.debug(className, "doPostToSendJson", "statusCode " + statusCode);
                responseJsonObject.put("status", "failure");
                responseJsonObject.put("message", responseString);

                return responseJsonObject;
            }
        } catch (Exception exception) {
            exception.printStackTrace();

            try {
                responseJsonObject.put("status", "failure");

                String errorMessage = "";

                if (exception instanceof BindException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Bind)";
                } else if (exception instanceof ConnectException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_connect_exception) + " (Connect)";
                } else if (exception instanceof HttpRetryException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (HttpRetry)";
                } else if (exception instanceof MalformedURLException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (MalformedURL)";
                } else if (exception instanceof NoRouteToHostException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (NoRouteToHost)";
                } else if (exception instanceof PortUnreachableException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (PortUnreachable)";
                } else if (exception instanceof ProtocolException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Protocol)";
                } else if (exception instanceof SocketException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Socket)";
                } else if (exception instanceof SocketTimeoutException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (SocketTimeout)";
                } else if (exception instanceof UnknownHostException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownHost)";
                } else if (exception instanceof UnknownServiceException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownService)";
                } else if (exception instanceof DataFormatException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (DataFormat)";
                } else if (exception instanceof ZipException) {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception) + " (Zip)";
                } else {
                    errorMessage = context.getString(R.string.failed_to_connect_server_timeout_exception);
                }

                responseJsonObject.put("message", errorMessage);

            } catch (Exception ee) {
                ee.printStackTrace();
            }

            return responseJsonObject;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    public static JSONObject downloadFile(String sourceUrl, String operation, boolean isZIP,
                                          File destinationFolder, final Activity activity, final PercentageProgressBar percentageProgressBar) {

        JSONObject responseJSON = new JSONObject();

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {

            responseJSON.put("status", "failure");
            responseJSON.put("message", "unknown");

            URL url = new URL(sourceUrl);

            if (sourceUrl.contains("https://")) {
                connection = (HttpsURLConnection) url.openConnection();
                //prepareConnection((HttpsURLConnection) connection);//configure for https
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.setRequestProperty("Authorization", "Bearer " + SecurityPreferenceUtility.getAccessToken());
            connection.connect();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {

                File downloadedFile = new File(destinationFolder + File.separator + operation);

                try {
                    downloadedFile.createNewFile();  //create file
                } catch (Exception e) {
                    e.printStackTrace();
                }

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

                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {

                        try {

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    speed = currentTotal - previousTotal;
                                    if (percentageProgressBar != null) {
                                        percentageProgressBar.setDownloadSpeed(Integer.parseInt(String.valueOf(speed)));
                                        percentageProgressBar.setDownloadSize(currentTotal, finalFileSizeString);
                                    }
                                    previousTotal = currentTotal;
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 0, 1000);

                while ((count = input.read(data)) != -1) {

                    total += count;
                    currentTotal = total;

                    if (fileLength != -1) {
                        progress = (int) (total * 100 / fileLength);
                        if (percentageProgressBar != null) {
                            percentageProgressBar.setDownloadProgress(progress);
                        }
                    }

                    output.write(data, 0, count);
                }

                timer.cancel();

                if (downloadedFile.exists()) {
                    if (isZIP) {
                        boolean unzipResult =false;
                        try {
                            unzipResult = SystemUtils.unzip(activity, downloadedFile, destinationFolder.getAbsolutePath(), percentageProgressBar);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        if (unzipResult) {
                            responseJSON.put("status", "success");
                            responseJSON.put("message", operation + " downloaded successfully.");
                        } else {

                            String errorMessage = "Cannot unzip the downloaded file. Try again to download data.";
                            responseJSON.put("status", "failure");
                            responseJSON.put("message", errorMessage);
                        }

                        FileUtils.forceDelete(downloadedFile);
                    } else {
                        responseJSON.put("status", "success");
                        responseJSON.put("file", downloadedFile);
                        responseJSON.put("message", operation + " downloaded successfully.");
                    }
                } else {

                    String errorMessage = "Could not find the downloaded file.";
                    responseJSON.put("status", "failure");
                    responseJSON.put("message", errorMessage);
                }
            } else {

                String responseMsg = connection.getResponseMessage();

                ReveloLogger.error(className, "Request to download " + operation, "Response code: " + responseCode + "Response Message: " + responseMsg);

                responseJSON.put("status", "failure");
                responseJSON.put("message", "Request to download data failed. " + connection.getResponseMessage());

            }
        } catch (Exception exception) {
            exception.printStackTrace();

            try {
                responseJSON.put("status", "failure");

                String errorMessage = "";

                if (exception instanceof BindException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (Bind)";
                } else if (exception instanceof ConnectException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_connect_exception) + " (Connect)";
                } else if (exception instanceof HttpRetryException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (HttpRetry)";
                } else if (exception instanceof MalformedURLException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (MalformedURL)";
                } else if (exception instanceof NoRouteToHostException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (NoRouteToHost)";
                } else if (exception instanceof PortUnreachableException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (PortUnreachable)";
                } else if (exception instanceof ProtocolException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (Protocol)";
                } else if (exception instanceof SocketException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (Socket)";
                } else if (exception instanceof SocketTimeoutException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (SocketTimeout)";
                } else if (exception instanceof UnknownHostException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownHost)";
                } else if (exception instanceof UnknownServiceException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (UnknownService)";
                } else if (exception instanceof DataFormatException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (DataFormat)";
                } else if (exception instanceof ZipException) {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception) + " (Zip)";
                } else {
                    errorMessage = activity.getString(R.string.failed_to_connect_server_timeout_exception);
                }

                responseJSON.put("message", errorMessage);

            } catch (Exception ee) {
                ee.printStackTrace();
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
            }
        }
        return responseJSON;

    }

    public interface ShowProgress {
        void progress(long bytesUploaded, long fileLength);
    }
}