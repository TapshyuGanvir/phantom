package com.sixsimplex.treasurehunt.revelocore.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class NetworkUtility {

    public static boolean checkNetworkConnectivity(Context activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = null;
        if (cm != null) {
            ni = cm.getActiveNetworkInfo();
        }
        return ni != null && ni.isConnected();
    }

    public static  boolean checkLocationConnectivity(Context context){
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }
    public static String getErrorFromVolleyError(VolleyError volleyError) {

        NetworkResponse networkResponse = volleyError.networkResponse;

        String errorDescription = "";

        if (networkResponse != null) {
            byte[] data = networkResponse.data;
            if (data != null) {
                try {
                    String responseBody = new String(data, StandardCharsets.UTF_8);
                    JSONObject responseObject = null;

                    try {
                        responseObject = new JSONObject(responseBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                        responseObject = new JSONObject();
                        responseObject.put("status","failure");
                        responseObject.put(AppConstants.ERROR_DESCRIPTION,responseBody);

                    }

                    if (responseObject != null) {
                        errorDescription = responseObject.has(AppConstants.ERROR_DESCRIPTION) ?
                                responseObject.getString(AppConstants.ERROR_DESCRIPTION) : errorDescription;

                        if (TextUtils.isEmpty(errorDescription)) {
                            errorDescription = responseObject.has(AppConstants.ERROR_MESSAGE) ?
                                    responseObject.getString(AppConstants.ERROR_MESSAGE) : errorDescription;
                        }
                    } else {
//                        errorDescription = responseBody;
                        errorDescription = "";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (TextUtils.isEmpty(errorDescription)) {
                errorDescription = ErrorCodeMessages.getErrorMessage(networkResponse.statusCode);
            }

        } else {

            Throwable throwable = volleyError.getCause();
            if (throwable != null) {
                errorDescription = throwable.getMessage();
                if (TextUtils.isEmpty(errorDescription)) {
                    errorDescription = throwable.getLocalizedMessage();
                }
            }
        }

        if(errorDescription!=null && errorDescription.contains("connect to")){
            errorDescription = "Connection failure..Please check your internet connection";
        }
        Log.e("Error :", errorDescription);

        return errorDescription;
    }
}
