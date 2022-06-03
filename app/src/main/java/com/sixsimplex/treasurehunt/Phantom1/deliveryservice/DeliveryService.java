package com.sixsimplex.treasurehunt.Phantom1.deliveryservice;

import static com.sixsimplex.treasurehunt.revelocore.liveLocationUpdate.SendLocationToServerService.userAppUseDesciption;
import static com.sixsimplex.treasurehunt.revelocore.liveLocationUpdate.SendLocationToServerService.userAppUseType;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.sixsimplex.treasurehunt.Phantom1.mode.ModeUtility;
import com.sixsimplex.treasurehunt.Phantom1.utils.Utils;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.data.GeoJsonUtils;
import com.sixsimplex.treasurehunt.Phantom1.direction.Directions;
import com.sixsimplex.treasurehunt.Phantom1.direction.dResponse;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.revelocore.layer.GeometryEngine;
import com.sixsimplex.treasurehunt.revelocore.liveLocationUpdate.SendLocationAsyncTask;
import com.sixsimplex.treasurehunt.revelocore.liveLocationUpdate.SendLocationToServerService;
import com.sixsimplex.treasurehunt.revelocore.upload.IUpload;
import com.sixsimplex.treasurehunt.revelocore.upload.Upload;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.Utilities;
import com.sixsimplex.treasurehunt.revelocore.util.locationModule.GetUserLocation;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SurveyPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeliveryService extends Service implements IUpload {
    public static final String ACTION_START_DELIVERY = "startDelivery";
    public static final String ACTION_COMPLETE_DELIVERY = "completeDelivery";
    public static final String ACTION_REFRESH_ROUTE_DELIVERY = "refreshRouteOfDelivery";
    public static final String ACTION = "Action";
    private static final String directionUrl = "https://api.openrouteservice.org/v2/directions/driving-car/geojson?profile=driving-car&api_key=5b3ce3597851110001cf6248f4722697294247f880b0618229ce2d70";
    public static Location lastLocation = null;
    private final IBinder mBinder = new LocalBinderD();
    public boolean servicestate;
    Geometry circle;
    Geometry geofenceBuffer;
    Geometry targetGeometry;
    Handler mHandler = new Handler(Looper.getMainLooper());
    boolean isDelivered = false;
    Feature targetFeature = null;
    GetUserLocation getUserLocation;
    Location mLocation;
    List<Feature> cacheInRangeFeature = new ArrayList<>();
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private MutableLiveData<JSONObject> routeJson = new MutableLiveData<>();
    private MutableLiveData<Boolean> deliveryOngoingStatus = new MutableLiveData<>();
    private MutableLiveData<JSONObject> riderLocationState = new MutableLiveData<>();
    private MutableLiveData<Feature> targetFeatureLive = new MutableLiveData<>();
    private MutableLiveData<List<Feature>> inRangeFeature = new MutableLiveData<>();
    private MutableLiveData<Geometry> bufferGeom = new MutableLiveData<>();

    public static boolean isDeliveryServiceRunning(ActivityManager manager) {
        if ((ActivityManager) manager != null) {
            for (ActivityManager.RunningServiceInfo service : ((ActivityManager) manager).getRunningServices(Integer.MAX_VALUE)) {
                if (DeliveryService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public MutableLiveData<Feature> getTargetFeatureLive() {
        return targetFeatureLive;
    }

    public MutableLiveData<JSONObject> getRouteJson() {
        return routeJson;
    }

    public MutableLiveData<Boolean> getDeliveryOngoingStatus() {
        return deliveryOngoingStatus;
    }

    public MutableLiveData<JSONObject> getRiderLocationState() {
        return riderLocationState;
    }

    public MutableLiveData<List<Feature>> getInRangeFeature() {
        return inRangeFeature;
    }

    public MutableLiveData<Geometry> getBufferGeom() {
        return bufferGeom;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        String targetDeliveryFeatureId = intent.getStringExtra("targetDelivery");
        String action = intent.getStringExtra(DeliveryService.ACTION);
        getUserLocation = new GetUserLocation(getApplicationContext(), null, null, null, null, this::onLocationChange);
        if (getUserLocation != null) {
            mLocation = getUserLocation.getUserCurrentLocation();
        }


        if (ActivityCompat.checkSelfPermission(DeliveryService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(DeliveryService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return START_NOT_STICKY;
        }
        locationListener();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, mLocationListener);

        switch (action) {
            case DeliveryService.ACTION_START_DELIVERY:
                startDeliveryAction();
                break;
            case DeliveryService.ACTION_COMPLETE_DELIVERY:
                completeDeliveryAction(this);
                break;
            case DeliveryService.ACTION_REFRESH_ROUTE_DELIVERY:
                refreshDeliveryRoute();
                break;

        }
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void refreshDeliveryRoute() {
        try {
            if (mLocation == null) {
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            targetGeometry = GeoJsonUtils.convertToJTSGeometry(targetFeature.getGeoJsonGeometry());
            Location targetLocation = new Location(LocationManager.GPS_PROVIDER);
            targetLocation.setLatitude(targetGeometry.getCentroid().getY());
            targetLocation.setLongitude(targetGeometry.getCentroid().getX());

            new Directions.getOpenRouteDirections(this, directionUrl, mLocation, targetLocation, new dResponse.Listener<JSONObject>() {
                @Override
                public void onSuccess(JSONObject response) {
                    JSONObject routeJsonObj = new JSONObject();
                    try {
                        routeJsonObj.put("route", response);
                        routeJsonObj.put("startLocation", mLocation);
                        routeJsonObj.put("endLocation", targetLocation);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    routeJson.setValue(routeJsonObj);
                    deliveryOngoingStatus.setValue(true);
                }
            }, new dResponse.ErrorListener() {
                @Override
                public void onErrorResponse(String error) {
                    deliveryOngoingStatus.setValue(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completeDeliveryAction(Context context) {
        try {
            routeJson.setValue(null);
            targetFeatureLive.setValue(null);
            isDelivered = true;
            JSONObject data = new JSONObject();
            data.put("reachTarget", true);
            data.put("deliverobject", isDelivered);
            riderLocationState.setValue(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void startDeliveryAction() {
        Feature feature = DeliveryDataModel.getInstance().getTargetFeature();
        try {
            if (feature != null) {
                if (feature.getFeatureId() != null) {
                    targetFeature = feature;
                    DeliveryDataModel.getInstance().setTargetFeature(targetFeature);
                    targetFeatureLive.setValue(targetFeature);
                }
            }
            if (targetFeature != null) {
                if (mLocation == null) {
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                targetGeometry = GeoJsonUtils.convertToJTSGeometry(targetFeature.getGeoJsonGeometry());
                Location targetLocation = new Location(LocationManager.GPS_PROVIDER);
                targetLocation.setLatitude(targetGeometry.getCentroid().getY());
                targetLocation.setLongitude(targetGeometry.getCentroid().getX());

                new Directions.getOpenRouteDirections(this, directionUrl, mLocation, targetLocation, new dResponse.Listener<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        JSONObject routeJsonObj = new JSONObject();
                        try {
                            routeJsonObj.put("route", response);
                            routeJsonObj.put("startLocation", mLocation);
                            routeJsonObj.put("endLocation", targetLocation);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        routeJson.setValue(routeJsonObj);
                        deliveryOngoingStatus.setValue(true);
                    }
                }, new dResponse.ErrorListener() {
                    @Override
                    public void onErrorResponse(String error) {
                        deliveryOngoingStatus.setValue(true);
                    }
                });
                isDelivered = false;
                deliveryOngoingStatus.setValue(true);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onLocationChangeTasks(mLocation);
                    }
                });

//                setInRangeFeatures(mLocation, SurveyPreferenceUtility.getBufferDistance(UserInfoPreferenceUtility.getSurveyName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onLocationChange(Location location) {
        if (location != null) {
//            mLocation = location;
            onLocationChangeTasks(location);
        }
    }

    private void onLocationChangeTasks(Location location) {
        if (DeliveryService.isDeliveryServiceRunning((ActivityManager) getSystemService(ACTIVITY_SERVICE))) {
            if (location != null) {
                if (getDeliveryOngoingStatus().getValue() != null) {
                    if (getDeliveryOngoingStatus().getValue()) {
                        if (targetFeature != null) {
                            whereAmI(location, targetFeature, SurveyPreferenceUtility.getBufferDistance(UserInfoPreferenceUtility.getSurveyName()));
                            lastLocation = location;
                            sendLocationToServer(location);
                            mLocation = location;
                        }
                    }
                    setInRangeFeatures(location, SurveyPreferenceUtility.getBufferDistance(UserInfoPreferenceUtility.getSurveyName()));
                }
            }
        }
    }

    private void locationListener() {
        mLocationListener = new LocationListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private void whereAmI(Location location, Feature targetFeature, int i) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        circle = Utils.createBuffer(location.getLatitude(), location.getLongitude(), i);
                        JSONObject jsonObject = new JSONObject();
                        if (targetGeometry != null) {
                            if (GeometryEngine.intersects(circle, targetGeometry)) {
                                try {
                                    jsonObject.put("reachTarget", true);
                                    jsonObject.put("deliverobject", isDelivered);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    jsonObject.put("reachTarget", false);
                                    jsonObject.put("deliverobject", isDelivered);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                riderLocationState.setValue(jsonObject);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void setInRangeFeatures(Location location, int bufferDistance) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    geofenceBuffer = Utils.createBuffer(location.getLatitude(), location.getLongitude(), bufferDistance);
                    List<Feature> inRangeFeatureList = Utilities.getAllFeatureInBuffer(DeliveryDataModel.getFeatureList(), geofenceBuffer);
                    if (!Utils.isFeatureListEqual(cacheInRangeFeature, inRangeFeatureList) && (!cacheInRangeFeature.isEmpty() || !inRangeFeatureList.isEmpty())) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                inRangeFeature.setValue(inRangeFeatureList);
                            }
                        });
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            bufferGeom.setValue(geofenceBuffer);
                        }
                    });

                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        deliveryOngoingStatus.setValue(false);
        routeJson.setValue(null);
        riderLocationState.setValue(null);
        targetFeature = null;
        targetFeatureLive.setValue(null);
        targetGeometry = null;
        inRangeFeature.setValue(null);
        bufferGeom.setValue(null);
        cacheInRangeFeature.clear();
        if (mLocationManager != null && mLocationListener != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground();
    }

    private void startForeground() {
        String channelId = "delivery_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent resultIntent = new Intent();
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.drawable.revelo_logo);
        builder.setContentTitle("Delivery");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("you started the delivery process");
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId, "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("this channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        startForeground(2727, builder.build());
    }

    @Override
    public void showUploadResultDialog(JSONObject uploadResponseJsonObject, int requestCode) {
        Toast.makeText(getApplicationContext(), "Delivery Completed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUploadResultDialogDismissed(int requestCode) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void sendLocationToServer(Location location) {
        try {
            String locationjsonStr = createJSON(location);
            if (locationjsonStr != null && !locationjsonStr.isEmpty()) {
                String[] dataToUpload = new String[1];
                dataToUpload[0] = locationjsonStr;
                new SendLocationAsyncTask(DeliveryService.this).execute(dataToUpload);
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
            locationJSON.put("type", "Feature");
            locationJSON.put("properties", properties);
            locationJSON.put("geometry", geometry);

            JSONObject mainjson = new JSONObject();
            mainjson.put("lastKnownLocation", locationJSON);
            data = mainjson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public class LocalBinderD extends Binder {
        public DeliveryService getService() {
            return DeliveryService.this;
        }

    }
}
