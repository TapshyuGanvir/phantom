package com.sixsimplex.treasurehunt.revelocore.liveLocationUpdate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SendLocationToServerService extends Service {
    public static String userAppUseType = "positive";
    public static String userAppUseDesciption = "Collecting Events...";
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;


    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


                        if (ActivityCompat.checkSelfPermission(SendLocationToServerService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(SendLocationToServerService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return START_STICKY;
                        }
        locationListener();
                        mLocationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 5000, 1, mLocationListener);
        return START_STICKY;
                        }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        if (mLocationManager != null && mLocationListener != null) {
                            mLocationManager.removeUpdates(mLocationListener);
            }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void locationListener() {
            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                if (location != null) {
                    if(NetworkUtility.checkNetworkConnectivity(SendLocationToServerService.this)){
                        sendLocationToServer(location);
                    }
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    private void sendLocationToServer(Location location) {
        try {
            String locationjsonStr = createJSON(location);
            if (locationjsonStr != null && !locationjsonStr.isEmpty()) {
                String[] dataToUpload = new String[1];
                dataToUpload[0] = locationjsonStr;
                new SendLocationAsyncTask(SendLocationToServerService.this).execute(dataToUpload);
        }
        } catch (Exception e) {
            e.printStackTrace();
    }
    }


    private String createJSON(Location location) {
        String data = "";
        try {
            JSONObject properties = new JSONObject();

            properties.put("type", userAppUseType);
            properties.put("description", userAppUseDesciption);
            properties.put("fieldTimeStamp", SystemUtils.getCurrentDateTime());
            properties.put("userName", UserInfoPreferenceUtility.getUserName());
            properties.put("role", UserInfoPreferenceUtility.getRole());


            JSONObject geometry = new JSONObject();
            JSONArray userLocation = new JSONArray();
            userLocation.put(location.getLongitude());
            userLocation.put(location.getLatitude());
            geometry.put("type", "Point");
            geometry.put("coordinates", userLocation);


            JSONObject locationJSON = new JSONObject();
            locationJSON.put("type","Feature");
            locationJSON.put("properties", properties);
            locationJSON.put("geometry", geometry);

            JSONObject mainjson = new JSONObject();
            mainjson.put("lastKnownLocation",locationJSON);
            data = mainjson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

}
