package com.sixsimplex.treasurehunt.revelocore.service;

import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_DESCRIPTION;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_DISTANCE;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_ENDTIMESTAMP;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_ISNEW;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_JURISDICTION_INFO;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_NAME;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_STARTTIMESTAMP;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_TRAILID;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_TRANSPORT_MODE;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_USERNAME;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_W9_ENTITY_CLASS_NAME;
import static com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants.TRAIL_TABLE_W9_METADATA;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.data.FeatureTable;
import com.sixsimplex.treasurehunt.revelocore.data.GeoJsonUtils;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.initialsetup.InitializationActivity;
import com.sixsimplex.treasurehunt.revelocore.layer.GeometryEngine;

import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.DatePickerMethods;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.TrailPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationReceiverService extends Service {

    private FeatureTable trailFeatureTable =null;
    public static String trailId = null;
    private CMEntity trailCMEntity =null;
    private String CREATE_NEW_TRAIL_ENTRY = "createTrail";
    private String UPDATE_TRAIL_ENTRY = "updateTrail";
    private String PAUSE_TRAIL_ENTRY = "pauseTrail";
    private String END_TRAIL_ENTRY = "stopTrail";

    public static void setTrailLatLngs(List<GeoPoint> latLngs) {
        trailLatLngList = latLngs;
    }

    public static List<GeoPoint> getTrailLatLngs() {
        return trailLatLngList;
    }

    public interface STATE{
        String LOCATION_SERIVCE_STATE_STARTED = "started";
      //  String LOCATION_SERIVCE_STATE_PAUSED = "paused";
        String LOCATION_SERIVCE_STATE_STOPPED = "stopped";
    }
    public static String isServiceRunning = STATE.LOCATION_SERIVCE_STATE_STOPPED;
    public static final int CHANNEL_ID = 123;
    public static List<GeoPoint> trailLatLngList = new ArrayList<>();
    public static Location currentLocation = null;
    public static String trailState = "stopped", drawingFeatureState = "stopped";
    private String transportMode = "Walking";//walking,bicycle,vehicle


    private ResultReceiver receiver;
    private Notification notification;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder notificationBuilder=null;

  // public static final int UPDATE_LOCATION_STARTFOREGROUND_ACTION = 9900;
  // public static final int UPDATE_LOCATION_STOPFOREGROUND_ACTION = 9901;
    public static final int UPDATE_LOCATION_RESULTCODE_LOCATION = 9902;
    public static final int UPDATE_LOCATION_ERRORCODE_MOCK_ON = 9903;
    public interface ACTION {
        String UPDATE_LOCATION_STARTFOREGROUND_ACTION = "startForeground";
        String UPDATE_LOCATION_STOPFOREGROUND_ACTION = "stopForeground";
        String UPDATE_LOCATION_PAUSEFOREGROUND_ACTION = "pauseForeground";
        String UPDATE_LOCATION_RESUMEFOREGROUND_ACTION = "resumeForeground";
        String BROADCAST_ACTION = "broadcastEvent";
    }

    private String className = "LocationReceiverService";

    private LocationListener mLocationListener;
    private  LocationManager mLocationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION.UPDATE_LOCATION_STARTFOREGROUND_ACTION: {
                    isServiceRunning = STATE.LOCATION_SERIVCE_STATE_STARTED;
                    if (ActivityCompat.checkSelfPermission(LocationReceiverService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(LocationReceiverService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return START_NOT_STICKY;
                    }
                    locationListener();
                   // mLocationManager.removeUpdates(mLocationListener);
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, mLocationListener);

                    if (!sendExtraCommand("force_time_injection", null)) {
                    }

                    if (!sendExtraCommand("force_xtra_injection", new Bundle())) {
                    }
                    if(intent.hasExtra("receiver")){
                        receiver =(ResultReceiver) intent.getExtras().get("receiver");
                    }
                    String notificationMessage = "Reading your live location.. ";
                    if(intent.hasExtra("operationName")){
                        ReveloLogger.debug(className, "onStartCommand", "Received start Intent for operation "+intent.getStringExtra("operationName"));
                        if(intent.getStringExtra("operationName").equalsIgnoreCase("drawTrail")){
                            trailState="started";
                            trailLatLngList=new ArrayList<>();
                            notificationMessage = "Recording live location updates to collect user trail.";
                        }
                        if(intent.getStringExtra("operationName").equalsIgnoreCase("drawFeature")){
                            drawingFeatureState="started";
                            notificationMessage = "Recording live location updates to draw feature.";
                        }
                    }else {
                        ReveloLogger.debug(className, "onStartCommand", "Received start Intent, but no operation name provided ");
                    }
                    if(notificationBuilder==null){
                        notificationBuilder = showNotification(notificationMessage);
                    }else {
                        changeNotificationText(notificationMessage);
                    }
                }
                break;

                case ACTION.UPDATE_LOCATION_PAUSEFOREGROUND_ACTION:
                   // isServiceRunning = STATE.LOCATION_SERIVCE_STATE_PAUSED;
                    //SystemUtils.showShortToast("Feature creation paused.", this);
                    ReveloLogger.debug(className, "onStartCommand", "Received Pause feature creation Intent");
                    if(trailState.equalsIgnoreCase("started")){
                        ReveloLogger.debug(className, "onStartCommand", "Changing trail state from started to paused");
                        trailState = "paused";
                    }
                    //if(trailState.equalsIgnoreCase("paused") && !drawingFeatureState.equalsIgnoreCase("started"))
                   // mLocationManager.removeUpdates(mLocationListener);
                    //stopCountDownTimer();//trail pause
                    if(!trailState.equalsIgnoreCase("stopped")) {
                        ReveloLogger.debug(className, "onStartCommand", "trail state "+trailState+"..saving pause entry for trail "+trailId+"to db");
                        saveTrailToGdb(PAUSE_TRAIL_ENTRY, this);
                    }
                    changeNotificationText("Paused");//pause request

                    break;

                case ACTION.UPDATE_LOCATION_RESUMEFOREGROUND_ACTION:
                    //isServiceRunning = STATE.LOCATION_SERIVCE_STATE_STARTED;
                  //  SystemUtils.showShortToast("Feature creation resume request received. Wait for location.", this);
                    ReveloLogger.debug(className, "onStartCommand", " Received Resume feature creation intent");

                    if (ActivityCompat.checkSelfPermission(LocationReceiverService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(LocationReceiverService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return START_NOT_STICKY;
                    }
                    //2000 milliseconds and 0.0f meter
                   // mLocationManager.removeUpdates(mLocationListener);
                   // mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, mLocationListener);

                    if (!sendExtraCommand("force_time_injection", null)) {
                        ReveloLogger.error(className, "on Start Command", "Hardware does not support TS");
                    }

                    if (!sendExtraCommand("force_xtra_injection", new Bundle())) {
                        ReveloLogger.error(className, "on Start Command", "Hardware does not support Almanac");
                    }
                    if(trailState.equalsIgnoreCase("paused")){
                        ReveloLogger.debug(className, "onStartCommand", "Changing trail state from paused to started");
                        trailState= "started";
                    }
                   //locationReceiveTime = Calendar.getInstance().getTimeInMillis();
                   //resetCountDownTimer();//resume trail
                    changeNotificationText("Waiting for location");//resume request

                    break;

                case ACTION.UPDATE_LOCATION_STOPFOREGROUND_ACTION:

                    ReveloLogger.debug(className, "onStartCommand", "Received stop feature creation intent");
                    //1 - operations depend on values of trailstate and drawingFeatureState. Do not change sequence
                    String operationName="";
                    if(intent.hasExtra("operationName")) {
                        operationName  = intent.getStringExtra("operationName");
                        ReveloLogger.debug(className, "onStartCommand", "stop command received for operation "+operationName);
                    }else {
                        ReveloLogger.debug(className, "onStartCommand", "stop command received but no operation name given");
                    }

                    //2
                    if(trailState.equalsIgnoreCase("started") && operationName.equalsIgnoreCase("drawTrail")) {
                        ReveloLogger.debug(className, "onStartCommand", "trail state = started and stop entent received for trail..saving end entry in db for trail "+trailId+ " sending end location to map");
                        String w9Id = saveTrailToGdb(END_TRAIL_ENTRY, getBaseContext());
                        sendLastLocationToMap(w9Id);
                    }

                    //3
                        if(operationName.equalsIgnoreCase("drawTrail")){
                            trailState="stopped";
                        }
                        if(operationName.equalsIgnoreCase("drawFeature")){
                            drawingFeatureState="stopped";
                        }

                    //4
                    if(trailState.equalsIgnoreCase("stopped") && drawingFeatureState.equalsIgnoreCase("stopped")) {
                        isServiceRunning = STATE.LOCATION_SERIVCE_STATE_STOPPED;
                        if(mLocationListener!=null && mLocationManager!=null) {
                            mLocationManager.removeUpdates(mLocationListener);
                        }
                        stopForeground(true);
                        stopSelf();
                    }
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();

        ReveloLogger.error(className,"ondestroy", "service destroyed. trail state - "+trailState);
        if (!trailState.equalsIgnoreCase("stopped") && trailId!=null && trailLatLngList!=null && trailLatLngList.size() > 0) {
            ReveloLogger.error(className,"ondestroy", "saving ongoing trail "+trailId);
            saveTrailToGdb(END_TRAIL_ENTRY,this);
        }else {
            if(trailLatLngList!=null) {
                ReveloLogger.error(className, "ondestroy", "trail state -" + trailState + " , trailid -" + trailId + " , traillatlnglist size - " + trailLatLngList.size());
            }
            else {
                ReveloLogger.error(className, "ondestroy", "trail state -" + trailState + " , trailid -" + trailId + " , traillatlnglist size - 0");
            }
        }
        trailState = "stopped";
        drawingFeatureState = "stopped";
    }

    private void sendLastLocationToMap(String trailId) {
        if(currentLocation!=null){
            GeoPoint currentLatLng = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            String errorMessage = "Mock location on. Unable to creating feature.";
            Bundle bundle = new Bundle();
            ReveloLogger.debug(className, "sendLastLocationToMap", "current location found..creating bundle..");
            if (!drawingFeatureState.equalsIgnoreCase("stopped")) {
                ReveloLogger.debug(className, "sendLastLocationToMap", "drawingFeatureState = "+drawingFeatureState+", creating bundle..");
                bundle.putDouble("currentLat", currentLatLng.getLatitude());
                bundle.putDouble("currentLng", currentLatLng.getLongitude());
                bundle.putDouble("accuracy", currentLocation.getAccuracy());
                bundle.putString("drawingFeatureState", drawingFeatureState);
            }
            if (!trailState.equalsIgnoreCase("stopped")) {
                ReveloLogger.debug(className, "sendLastLocationToMap", "trailState = "+trailState+", creating bundle..");
                bundle.putString("trailState", trailState);
                bundle.putBoolean("stopTrail", true);
                bundle.putString("trailId", trailId);
                boolean firstLocation = true;
                if (trailLatLngList != null && trailLatLngList.size() > 0) {
                    firstLocation = false;
                }
                bundle.putBoolean("firstLocation", firstLocation);
                bundle.putSerializable("trailLatLongs", (Serializable) trailLatLngList);
                ReveloLogger.debug(className, "sendLastLocationToMap", "bundle created for trails..stoptrail=true, trail id "+trailId+" , first location "+firstLocation);
            }
            if (receiver != null) {
                ReveloLogger.debug(className, "sendLastLocationToMap", "Sending bundle to activity..");
                receiver.send(UPDATE_LOCATION_RESULTCODE_LOCATION, bundle);
            }
        }else {
            ReveloLogger.debug(className, "sendLastLocationToMap", "current location found null..not sending last location to map");
        }
    }

    private List<GeoPoint> getCurrentTrailLatLng(String trailId) {

        try {
            Feature featureTrial= trailFeatureTable.getFeature(trailCMEntity.getW9IdProperty(),trailId,this, true, false);
            if(featureTrial==null){
                return null;
            }
            else {
                if(featureTrial.getAttributes().containsKey("isnew")) {
                    String trailRunningStatus = featureTrial.getAttributes().get("isnew").toString();
                    if(trailRunningStatus.equalsIgnoreCase("started")||trailRunningStatus.equalsIgnoreCase("paused")){
                        List<List<GeoPoint>> lists= GeoJsonUtils.convertToOSMGeoPointsLists(featureTrial.getGeoJsonGeometry());
                        if(lists!=null && lists.size()>0){
                            return lists.get(0);
                        }
                    }
                }else {
                    return null;
                }
            }
        }
        catch (Exception exception){
            exception.printStackTrace();
        }
        return null;
    }

    private static Geometry getMultiLineGeometry(List<GeoPoint> geoPoints) {
        ReveloLogger.trace("LocationReceiverService","getMultiLineGeometry", "TimeLogs "+ "start creating geom "+ SystemUtils.getCurrentDateTimeMiliSec());
        long startTime =   System.nanoTime();
        if(geoPoints==null||geoPoints.size()==0){
            ReveloLogger.trace("LocationReceiverService","getMultiLineGeometry", "TimeLogs "+ "done creating geom by"+ SystemUtils.getCurrentDateTimeMiliSec()+" \n time taken(ms) = "+((System.nanoTime()-startTime)/1000000));
            return null;
        }
        try {
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

            Coordinate[] coordinates = new Coordinate[geoPoints.size()];
            for (int i = 0; i < geoPoints.size(); i++) {
                coordinates[i] = new Coordinate(geoPoints.get(i).getLongitude(), geoPoints.get(i).getLatitude());
            }

            LineString lineString = factory.createLineString(coordinates);
            LineString[] lineStrings = {lineString};
            Geometry geometry = factory.createMultiLineString(lineStrings);
            ReveloLogger.trace("LocationReceiverService","getMultiLineGeometry", "TimeLogs "+ "done creating geom by"+ SystemUtils.getCurrentDateTimeMiliSec()+" \n time taken(ms) = "+((System.nanoTime()-startTime)/1000000));
            return geometry;
        }catch (Exception e){
            ReveloLogger.trace("LocationReceiverService","getMultiLineGeometry", "Exception creating trail geom "+e.getMessage());
            e.printStackTrace();
        }
        ReveloLogger.trace("LocationReceiverService","getMultiLineGeometry", "TimeLogs "+ "done creating geom by"+ SystemUtils.getCurrentDateTimeMiliSec()+" \n time taken(ms) = "+((System.nanoTime()-startTime)/1000000));
        return null;
    }

    private String saveTrailToGdb(String operationName,Context context) {
        ReveloLogger.debug(className, "saveTrailToGdb", "save command received for operation "+operationName+" , trail id saved in service = "+trailId);
        ReveloLogger.trace(className,"saveTrailToGdb", "TimeLogs "+ "start saving "+ SystemUtils.getCurrentDateTimeMiliSec());
        long startTime =   System.nanoTime();

        //DO SOMETHING
        if (trailFeatureTable == null) {// if (spatialiteRWAgent == null || dataSetInfoTrailTable == null) {
            ReveloLogger.debug(className, "saveTrailToGdb", "initializing trail feature table");
            initTrailDbConstants();
        }

        if (operationName.equalsIgnoreCase(CREATE_NEW_TRAIL_ENTRY)) {
//            String startTimeStamp = SystemUtils.getCurrentDateTime();
//            String startTimeStamp = DatePickerMethods.getCurrentDateString_metadata();
            String startTimeStamp = DatePickerMethods.getCurrentDateString_mmddyyyy();
            try {
                Geometry trailNewGeometry = getMultiLineGeometry(trailLatLngList);
                if (trailNewGeometry != null) {

                    JSONObject geometryGeoJson = GeometryEngine.convertGeometryToGeoJson(trailNewGeometry);
                    Point point = trailNewGeometry.getCentroid();
                    ReveloLogger.trace("LocationReceiverService","getMultiLineGeometry", "TimeLogs "+ "done geting centroid and converting geom to geojson "+ SystemUtils.getCurrentDateTimeMiliSec());
                    Map<String, Object> properties = new HashMap<>();
/*
                    Map<String, Object> jurisdictionValuesMap = ReDbTable.getJurisdictionFromPoint(this, point);
                    HashMap<String,String> jurisdictionVariables = createJurisdictionNamesIdMap();
                    ReveloLogger.trace("LocationReceiverService","getMultiLineGeometry", "TimeLogs "+ "done getting jurisdiction values "+ SystemUtils.getCurrentDateTimeMiliSec());
                    Map<String, Object> properties = new HashMap<>();
                    for(String jurisdictionName:jurisdictionVariables.keySet()){
                        String jurisdictionId = jurisdictionVariables.get(jurisdictionName);
                        if(jurisdictionValuesMap.containsKey(jurisdictionId)){
                            properties.put(jurisdictionName,jurisdictionValuesMap.get(jurisdictionId));
                        }
                    }*/
                    ReveloLogger.trace("LocationReceiverService","getMultiLineGeometry", "TimeLogs "+ "done creating jurisdiction values map," +
                            "ready for db operation "+ SystemUtils.getCurrentDateTimeMiliSec());
                    //if(trailId==null) {
                        trailId = AppMethods.getTrailId();
                    ReveloLogger.debug(className, "saveTrailToGdb", "service trail is set to "+trailId);
                    //}
                    properties.put(TRAIL_TABLE_TRAILID, trailId);
                    properties.put(TRAIL_TABLE_STARTTIMESTAMP, startTimeStamp);
                    properties.put(TRAIL_TABLE_ENDTIMESTAMP, startTimeStamp);
                    properties.put(TRAIL_TABLE_ISNEW, "started");
                    properties.put(TRAIL_TABLE_DESCRIPTION, "");
                    properties.put(TRAIL_TABLE_TRANSPORT_MODE, transportMode);
                    properties.put(TRAIL_TABLE_JURISDICTION_INFO, "");
                    properties.put(TRAIL_TABLE_DISTANCE, trailNewGeometry.getLength());
                    properties.put(TRAIL_TABLE_USERNAME,  UserInfoPreferenceUtility.getUserName());
                    properties.put(TRAIL_TABLE_W9_ENTITY_CLASS_NAME, TRAIL_TABLE_NAME);
                    properties.put(TRAIL_TABLE_W9_METADATA, String.valueOf(createMetadata()));

                    trailFeatureTable.insertAddRecordInDb(properties,geometryGeoJson,this, trailId,null,currentLocation);

                    ReveloLogger.debug(className, "saveTrailToGdb", "create new trail");

                } else {
                    ReveloLogger.error(className, "saveTrailToGdb", "error updating entry for trail in gdb geometry: null");
                }
            } catch (Exception e) {
                ReveloLogger.error(className, "saveTrailToGdb", "error creating entry for trail in gdb: " + e.getMessage());
                e.printStackTrace();
            }
            ReveloLogger.trace(className,"saveTrailToGdb", "TimeLogs "+ "done saving by"+ SystemUtils.getCurrentDateTimeMiliSec()+" \n time taken(ms) = "+((System.nanoTime()-startTime)/1000000));
            //Log.e("Measure", TASK took : " +  ((System.nanoTime()-startTime)/1000000)+ "mS\n")
            return trailId;
        }
        else if (operationName.equalsIgnoreCase(UPDATE_TRAIL_ENTRY)) {

            if (trailLatLngList == null || trailLatLngList.size() == 0) {
                ReveloLogger.debug(className, "saveTrailToGdb", "no trail latlong found, asking db for latlng for trail id "+trailId+".. skipping update operation");
                trailLatLngList = getCurrentTrailLatLng(trailId);
            }else {
                Geometry trailUpdateGeometry = getMultiLineGeometry(trailLatLngList);

                if (trailUpdateGeometry != null) {
                    JSONObject geometryGeoJson = GeometryEngine.convertGeometryToGeoJson(trailUpdateGeometry);
                    try {
                   /* if(trailId==null) {
                        trailId = getTrailW9Id();
                    }*/
                    /*JSONArray dataJsonArray = new JSONArray();
                    JSONObject jsonObjectData = new JSONObject();
                    jsonObjectData.put(AppController.geometryKeyName, trailUpdateWkt);
                    jsonObjectData.put(TRAIL_TABLE_TRANSPORT_MODE, transportMode);
                    jsonObjectData.put(TRAIL_TABLE_DISTANCE, trailLengthInMeter);
                    JSONObject jsonDataObj = new JSONObject();
                    jsonDataObj.put("properties", jsonObjectData);
                    dataJsonArray.put(jsonDataObj);

                    spatialiteRWAgent.updateDatasetContentByCondition(dataSetInfoTrailTable, dataJsonArray, whereClause);*/
                        if(trailId!=null) {
                            Map<String, Object> properties = new HashMap<>();
                            properties.put(TRAIL_TABLE_TRANSPORT_MODE, transportMode);
                            properties.put(TRAIL_TABLE_DISTANCE, trailUpdateGeometry.getLength());

                            trailFeatureTable.updateRecordInDb(properties, geometryGeoJson, this,  trailId, null, currentLocation, trailCMEntity.getW9IdProperty(), null);

                            ReveloLogger.debug(className, "saveTrailToGdb", "update trail");
                        }else {
                            ReveloLogger.debug(className, "saveTrailToGdb", "no trail id found, skipping update operation. service trail id: "+trailId);
                        }
                    } catch (Exception e) {
                        ReveloLogger.error(className, "saveTrailToGdb", "error updating entry for trail in gdb: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    ReveloLogger.error(className, "saveTrailToGdb", "error updating entry for trail in gdb geometry: null");
                }
            }
        }
        else if (operationName.equalsIgnoreCase(PAUSE_TRAIL_ENTRY)) {
                try {
                  /*  if(trailId==null) {
                        trailId = getTrailW9Id();
                    }*/
                    if (trailLatLngList == null || trailLatLngList.size() == 0) {
                        ReveloLogger.debug(className, "saveTrailToGdb", "no trail latlong found, asking db for latlng for trail id "+trailId);
                        trailLatLngList = getCurrentTrailLatLng(trailId);
                    }
                    if(trailLatLngList!=null) {
                        ReveloLogger.debug(className, "saveTrailToGdb", "updating pause entry");
                        Geometry trailUpdateGeometry = getMultiLineGeometry(trailLatLngList);

                        if (trailUpdateGeometry != null) {
                            JSONObject geometryGeoJson = GeometryEngine.convertGeometryToGeoJson(trailUpdateGeometry);
                            if (trailId != null) {
                                Map<String, Object> properties = new HashMap<>();
                                properties.put(TRAIL_TABLE_ISNEW, "paused");

                                trailFeatureTable.updateRecordInDb(properties, geometryGeoJson, this, trailId, null, currentLocation, trailCMEntity.getW9IdProperty(), null);

                                ReveloLogger.debug(className, "saveTrailToGdb", "update trail");
                            }else {
                                ReveloLogger.debug(className, "saveTrailToGdb", "trailid null, not updating pause entry");
                            }
                        }
                    }else {
                        ReveloLogger.debug(className, "saveTrailToGdb", "trailLatLngList null, not updating pause entry");
                    }
                } catch (Exception e) {
                    ReveloLogger.error(className, "saveTrailToGdb", "error updating entry for trail in gdb: " + e.getMessage());
                    e.printStackTrace();
                }


        }
        else if (operationName.equalsIgnoreCase(END_TRAIL_ENTRY)) {
            if (trailLatLngList == null || trailLatLngList.size() == 0) {
                ReveloLogger.debug(className, "saveTrailToGdb", "no trail latlong found, asking db for latlng for trail id "+trailId);
                trailLatLngList = getCurrentTrailLatLng(trailId);
            }
            boolean deleteTrailEntry = false;
            if (trailLatLngList!=null && trailLatLngList.size() > 1) {
                Geometry trailEndGeometry = getMultiLineGeometry(trailLatLngList);
                if (trailEndGeometry != null) {
                    JSONObject geometryGeoJson = GeometryEngine.convertGeometryToGeoJson(trailEndGeometry);

//                    String endTimeStamp = SystemUtils.getCurrentDateTime();
//                    String endTimeStamp = DatePickerMethods.getCurrentDateString_metadata();
                    String endTimeStamp = DatePickerMethods.getCurrentDateString_mmddyyyy();

                    try {
                       /* if(trailId==null) {
                            trailId = getTrailW9Id();
                        }*/
                        if(trailId!=null && !trailId.isEmpty()) {
                            Map<String, Object> properties = new HashMap<>();
                            properties.put(TRAIL_TABLE_ENDTIMESTAMP, endTimeStamp);
                            properties.put(TRAIL_TABLE_ISNEW, "1");
                            properties.put(TRAIL_TABLE_TRANSPORT_MODE, transportMode);
                            properties.put(TRAIL_TABLE_DISTANCE, trailEndGeometry.getLength());


                            trailFeatureTable.updateRecordInDb(properties, geometryGeoJson, this,  trailId, null, currentLocation, trailCMEntity.getW9IdProperty(), null);

                            ReveloLogger.debug(className, "saveTrailToGdb", "end trail updated");
                        }
                        if(trailLatLngList!=null) {
                            ReveloLogger.debug(className, "saveTrailToGdb", "Clearing service trail latlong list");
                            trailLatLngList.clear();
                        }
                        TrailPreferenceUtility.setTrailSpeed(0f);
                    } catch (Exception e) {
                        deleteTrailEntry=true;

                        ReveloLogger.error(className, "saveTrailToGdb", "error updating entry for trail in gdb: " + e.getMessage());
                        e.printStackTrace();
                    }
                }else {
                    deleteTrailEntry=true;
                    ReveloLogger.debug(className, "saveTrailToGdb", "trail geom null, not updating end entry..moving to delete existing bad feature");
                }
            } else {
                deleteTrailEntry=true;
            }
            if(deleteTrailEntry){
                ReveloLogger.debug(className, "saveTrailToGdb", "deleting existing bad feature trail id: "+trailId);
                //delete trail entry
                try {

                    trailFeatureTable.deleteFeatureRecordOnly(trailId,trailCMEntity.getW9IdProperty(),this);

                    ReveloLogger.debug(className, "saveTrailToGdb", "deleting trail");
                    if(trailLatLngList!=null) {
                        ReveloLogger.debug(className, "saveTrailToGdb", "Clearing service trail latlong list");
                        trailLatLngList.clear();
                    }
                } catch (Exception e) {
                    ReveloLogger.error(className, "saveTrailToGdb", " error deleting empty trail entry for trail in gdb");
                    e.printStackTrace();
                }
            }
        }
        ReveloLogger.trace(className,"saveTrailToGdb", "TimeLogs "+ "done saving by"+ SystemUtils.getCurrentDateTimeMiliSec()+" \n time taken(ms) = "+((System.nanoTime()-startTime)/1000000));
        return trailId;
    }

    private JSONObject createMetadata() {

        JSONObject metadataJSON = new JSONObject();

        try {

            metadataJSON.put(AppConstants.W9_LATITUDE, currentLocation.getLatitude());
            metadataJSON.put(AppConstants.W9_LONGITUDE, currentLocation.getLongitude());
            metadataJSON.put(AppConstants.W9_ACCURACY, currentLocation.getAccuracy());

           // metadataJSON.put(AppConstants.W9_REVISIT, false);
//            metadataJSON.put(AppConstants.W9_ATTACHMENTS_INFO, new JSONArray());
            //metadataJSON.put(AppConstants.W9_REQUIRES_ADJUSTMENT, false);
//
            //metadataJSON.put(AppConstants.W9_CREATED_BY, ReveloStore.getUsername());
            //metadataJSON.put(AppConstants.W9_CREATION_DATE, SystemUtils.getCurrentDateTime());
            //metadataJSON.put(AppConstants.W9_UPDATED_BY, "");
            metadataJSON.put(AppConstants.W9_UPDATE_DATE, DatePickerMethods.getCurrentDateString_metadata());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return metadataJSON;
    }

    private void initTrailDbConstants() {
        JSONObject graphResult = CMUtils.getCMGraph(this);

        try {
            CMEntity cmTrailEntity = null;
            if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                CMGraph cmGraph = (CMGraph) graphResult.get("result");
                JSONObject cmEntityResult = cmGraph.getVertex("name", TRAIL_TABLE_NAME);
                if (cmEntityResult.has("status") && cmEntityResult.getString("status").equalsIgnoreCase("success")) {
                    cmTrailEntity = (CMEntity) cmEntityResult.get("result");
                } else {
                    ReveloLogger.error("HomePresenter", "createEntityListData", "Could not create enitities list - could not fetch entity from memory. Reason - " + graphResult.getString("message"));
                }

                if (cmTrailEntity == null) {
                    //todo stop trail service, return error
                    return;
                }
                FeatureTable trailFeatureTable = cmTrailEntity.getFeatureTable();
                if(trailFeatureTable==null){
                    //todo stop trail service, return error
                    return;
                }else {
                    this.trailFeatureTable=trailFeatureTable;
                    this.trailCMEntity = cmTrailEntity;
                }
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }

    }

    private void locationListener() {
        ReveloLogger.trace(className,"locationListener", "TimeLogs "+ "init location listener - "+ SystemUtils.getCurrentDateTimeMiliSec());
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location!=null) {
                    try {
                        boolean isMock = location.isFromMockProvider();

                        isMock = false;
                        if (!isMock) {
                            currentLocation = location;
                            Bundle bundle = null;
                            ReveloLogger.debug(className, "locationListener - onLocationChanged ", "new location received");
                            if (trailState.equalsIgnoreCase("started") || drawingFeatureState.equalsIgnoreCase("started")) {
                                if (drawingFeatureState.equalsIgnoreCase("started")) {
                                    ReveloLogger.debug(className, "locationListener - onLocationChanged ", "drawingFeatureState =  started, creating bundle for it");
                                    if (bundle == null) {
                                        bundle = new Bundle();
                                    }
                                    bundle.putDouble("currentLat", location.getLatitude());
                                    bundle.putDouble("currentLng", location.getLongitude());
                                    bundle.putDouble("accuracy", location.getAccuracy());
                                    bundle.putString("drawingFeatureState", drawingFeatureState);
                                }
                                if (trailState.equalsIgnoreCase("started")) {
                                    ReveloLogger.debug(className, "locationListener - onLocationChanged ", "trailState =  started, creating bundle for it: time: "+ SystemUtils.getCurrentDateTimeMiliSec());
                                    if (trailLatLngList == null) {
                                        trailLatLngList = new ArrayList<>();
                                    }
                                    trailLatLngList.add(new GeoPoint(location.getLatitude(), location.getLongitude()));
                                    ReveloLogger.debug(className, "locationListener - onLocationChanged ", "trailLatLngList size = "+trailLatLngList.size());
                                    if (trailLatLngList.size() > 0) {
                                        float speedPerSec = location.getSpeed();

                                        float currentSpeed = 3.6f * speedPerSec;

                                        float previousSpeed = TrailPreferenceUtility.getTrailSpeed();

                                        if (currentSpeed > previousSpeed) {
                                            TrailPreferenceUtility.setTrailSpeed(currentSpeed);

                                            if (currentSpeed < 7.0f) {
                                                transportMode = "Walking";
                                            } else if (currentSpeed > 7.0f && currentSpeed < 20.0f) {
                                                transportMode = "Bicycle";
                                            } else if (currentSpeed > 20.0f) {
                                                transportMode = "Vehicle";
                                            }
                                        }

                                        if (trailLatLngList.size() == 2) {
                                            ReveloLogger.debug(className, "locationListener - onLocationChanged ", "trailLatLngList size = 2, creating trail feature in db");
                                            saveTrailToGdb(CREATE_NEW_TRAIL_ENTRY, getBaseContext());
                                            //playTone();//tone for start of trail
                                        } else if (trailLatLngList.size() > 2) {
                                            ReveloLogger.debug(className, "locationListener - onLocationChanged ", "trailLatLngList size > 2, updating existing trail feature - id : "+trailId+"  in db");
                                            saveTrailToGdb(UPDATE_TRAIL_ENTRY, getBaseContext());
                                        }
                                        //changeNotificationText("Recording");//when get location
                                    }
                                    if (bundle == null) {
                                        bundle = new Bundle();
                                    }
                                    bundle.putBoolean("stopTrail", false);
                                    bundle.putString("trailState", trailState);
                                    bundle.putString("trailId", trailId);
                                    boolean firstLocation = true;
                                    if (trailLatLngList != null && trailLatLngList.size() > 1) {
                                        firstLocation = false;
                                    }
                                    bundle.putBoolean("firstLocation", firstLocation);
                                    bundle.putSerializable("trailLatLongs", (Serializable) trailLatLngList);
                                    ReveloLogger.debug(className, "locationListener - onLocationChanged ", "bundle created for trail, stop trail = false ,firstlocation = "+firstLocation+": endtime : "+ SystemUtils.getCurrentDateTimeMiliSec());
                                }
                                if (notificationBuilder == null) {
                                    notificationBuilder = showNotification("Captured new location with accuracy of " + location.getAccuracy() + " meters.");
                                } else {
                                    changeNotificationText("Captured new location with accuracy of " + location.getAccuracy() + " meters.");
                                }

                                if (bundle != null) {
                                    ReveloLogger.debug(className, "locationListener - onLocationChanged ", "Sending bundle to activity..");
                                    receiver.send(UPDATE_LOCATION_RESULTCODE_LOCATION, bundle);
                                }
                            }
                        } else {
                            ReveloLogger.debug(className, "locationListener - onLocationChanged ", "mock location received..warning user");

                            String errorMessage = "Mock location on. Unable to creating feature.";
                            Bundle bundle = new Bundle();
                            bundle.putString("errorMessage", errorMessage);
                            receiver.send(UPDATE_LOCATION_ERRORCODE_MOCK_ON, bundle);
                            stopForeground(true);
                            stopSelf();
                        }
                    } catch (Exception e) {
                        ReveloLogger.error(className, "on Location Changed", " error. skipping current location: exception " + e.getMessage());
                        // SystemUtils.showShortToast("Skipping current location error found.", DrawGeometryByLocationService.this);
                        e.printStackTrace();
                    }
                }
                else {
                    ReveloLogger.debug(className, "locationListener - onLocationChanged ", "null location received");
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                ReveloLogger.debug(className, "locationListener - onStatusChanged ", "s: "+s+" i: "+ i);
            }

            @Override
            public void onProviderEnabled(String s) {
                ReveloLogger.debug(className, "locationListener - onProviderEnabled ", "s: "+s);
            }

            @Override
            public void onProviderDisabled(String s) {
                ReveloLogger.debug(className, "locationListener - onProviderDisabled ", "s: "+s);
            }
        };


        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private boolean sendExtraCommand(String command, Bundle bundle) {
        return mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, command, bundle);
    }

    private NotificationCompat.Builder showNotification(String message) {

        Intent notificationIntent = new Intent(this, InitializationActivity.class);
        notificationIntent.putExtra("callingActivity","locationReceiverService");
        /*PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);*/
        PendingIntent pendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
                    .setAutoCancel(true);

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

    @SuppressLint("MissingPermission")
    private void playTone() {
        try {
            Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vib != null) {// Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vib.vibrate(500);//deprecated in API 26
                }
            }

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
            ringtone.play();
        } catch (Exception e) {

        }
    }
}
