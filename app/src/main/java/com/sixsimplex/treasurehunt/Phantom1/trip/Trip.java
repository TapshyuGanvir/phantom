package com.sixsimplex.treasurehunt.Phantom1.trip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.sixsimplex.treasurehunt.Phantom1.CURD.AddAndUpload;
import com.sixsimplex.treasurehunt.Phantom1.CURD.EditAndUpload;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.Phantom1.CURD.upload.UploadInterface;
import com.sixsimplex.treasurehunt.revelocore.util.DatePickerMethods;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONArray;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Trip {
    public static final String ADD ="add" ;
    public static final String EDIT ="edit" ;
    private static final String DELIVERY_BOY_ID_IN_TRIPS_COLUMN_NAME ="delvboyid" ;
    private static final String TRIP_ID_COLUMN_NAME_IN_TRIP_ITEM_TABLE ="tripid" ;

    public static void addUpdateTrip(Context context, String operationType, Location locationL, ITripCallback iTripCallback, IdeliveryActivityView ideliveryActivityView) {
        long startTime = System.nanoTime();
        try {
            if (operationType.equalsIgnoreCase("ADD")) {
                if(TripDataModel.getInstance().getTodayTripAdded() == null){
                    TripDataModel.getInstance().setTodayTripAdded(isTripAdded(context));
                }
                if (!TripDataModel.getInstance().getTodayTripAdded()) {
                    addNewTripEntry(context, locationL, new ITripCallback() {
                        @Override
                        public void onSuccessTripAddUpdate() {
                            ideliveryActivityView.showTripItemSelectionDialog(iTripCallback);
                        }

                        @Override
                        public void onFailureTripAddUpdate(String message) {

                        }
                    });
                }
                else {
                    if(TripDataModel.getInstance().getInventoryItemAddedForCurrentTrip() == null){
                        TripDataModel.getInstance().setInventoryItemAddedForCurrentTrip(isInventoryAddedForCurrentTrip(context,getCurrentTripFeature(context)));
                    }
                    if(!TripDataModel.getInstance().getInventoryItemAddedForCurrentTrip()){
                        Log.d("timecheck", "beforeDialogShow: "+(System.nanoTime()-startTime));
                        ideliveryActivityView.showTripItemSelectionDialog(iTripCallback);
                        Log.d("timecheck", "afterDialogShow: "+(System.nanoTime()-startTime));
                    }else{
                        iTripCallback.onSuccessTripAddUpdate();
                    }
                }
            }
            else {
                editCurrentTripEntry(context,operationType,locationL,iTripCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                String message="failed "+ "Reason: exception - " + e.getMessage();
                iTripCallback.onFailureTripAddUpdate(message);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private static void editCurrentTripEntry(Context context, String operationType, Location locationL, ITripCallback iTripCallback) {

        try {
            UploadInterface uploadInterface=new UploadInterface() {
                @Override
                public void OnUploadStarted() {

                }

                @Override
                public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                }
            };
            Feature feature = getCurrentTripFeature(context);
            if (feature != null) {
                Map<String, Object> attributeValueMap = new HashMap<>();
                attributeValueMap.put("endloc", "[" + String.valueOf(locationL.getLatitude() + "," + String.valueOf(locationL.getLongitude())) + "]");
                Date dateCurrent = new Date();
                String modifiedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dateCurrent);
                attributeValueMap.put("endtime", modifiedDate);


                JSONObject tripEditResponseJson= EditAndUpload.perform(context,
                        AppConstants.EDIT,
                        DeliveryDataModel.getInstance().getTripEntity(),
                        attributeValueMap,
                        null,
                        DeliveryDataModel.getInstance().getTripEntity().getFeatureTable(),
                        null,
                        String.valueOf(feature.getFeatureId()),
                        String.valueOf(feature.getFeatureLabel()),
                        locationL,
                        null,
                        "",
                        uploadInterface,true);

                if (tripEditResponseJson != null) {
                    if (tripEditResponseJson.has("status")) {
                        if (tripEditResponseJson.getString("status").equalsIgnoreCase("success")) {
                            initiateTripData(context);
                            iTripCallback.onSuccessTripAddUpdate();
                        } else {
                            String message="";
                            if (tripEditResponseJson.has("message")) {
                                message= tripEditResponseJson.getString("message");
                            } else {
                                message=  "trip edit fail";
                            }
                            iTripCallback.onFailureTripAddUpdate(message);
                        }
                    }
                }
            }
        }catch (Exception e){
            String message=e.getMessage();
            iTripCallback.onFailureTripAddUpdate(message);
            e.printStackTrace();
        }

    }

    private static void addNewTripEntry(Context context, Location locationL, ITripCallback iTripCallback) {
        try {
            Map<String, Object> attributeValueMap = new HashMap<>();
            String w91d = generateAutomaticId();
            Date dateCurrent = new Date();
            String modifiedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dateCurrent);
            attributeValueMap.put("starttime", modifiedDate);
            attributeValueMap.put("tripid", w91d);
            attributeValueMap.put("endtime", "01-01-1990 00:00:00");
            attributeValueMap.put("endloc", "na");
            attributeValueMap.put("startloc", "[" + String.valueOf(locationL.getLatitude() + "," + String.valueOf(locationL.getLongitude())) + "]");
            attributeValueMap.put("delvboyid", UserInfoPreferenceUtility.getUserName());
            attributeValueMap.put("trailid", "");
            attributeValueMap.put(AppConstants.W9_ENTITY_CLASS_NAME, DeliveryDataModel.getInstance().getTripEntity().getName());
            try {
                JSONObject metadataJSON = new JSONObject();
                if (locationL != null) {
                    metadataJSON.put(AppConstants.W9_LATITUDE, locationL.getLatitude());
                    metadataJSON.put(AppConstants.W9_LONGITUDE, locationL.getLongitude());
                    metadataJSON.put(AppConstants.W9_ACCURACY, locationL.getAccuracy());
                } else {
                    metadataJSON.put(AppConstants.W9_LATITUDE, 0.0);
                    metadataJSON.put(AppConstants.W9_LONGITUDE, 0.0);
                    metadataJSON.put(AppConstants.W9_ACCURACY, 0.0);
                }
                String date = DatePickerMethods.getCurrentDateString_metadata();
                metadataJSON.put(AppConstants.W9_UPDATE_DATE, date);
                metadataJSON.put(AppConstants.W9_UPDATE_BY, UserInfoPreferenceUtility.getUserName());
                attributeValueMap.put(AppConstants.W9_METADATA, metadataJSON.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject addTripFeatureResultJson =AddAndUpload.perform(context,
                    AppConstants.ADD,
                    DeliveryDataModel.getInstance().getTripEntity(),
                    attributeValueMap, null,
                    DeliveryDataModel.getInstance().getTripEntity().getFeatureTable(),
                    null,
                    w91d,
                    w91d,
                    locationL,
                    null,
                    "", new UploadInterface() {
                        @Override
                        public void OnUploadStarted() {

                        }

                        @Override
                        public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                        }
                    });


            if (addTripFeatureResultJson != null) {
                if (addTripFeatureResultJson.has("status")) {
                    if (addTripFeatureResultJson.getString("status").equalsIgnoreCase("success")) {
                        iTripCallback.onSuccessTripAddUpdate();
                        initiateTripData(context);
                    } else {
                        String message="";
                        if (addTripFeatureResultJson.has("message")) {
                            message= addTripFeatureResultJson.getString("message");
                        } else {
                            message=  "trip add fail";
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static String generateAutomaticId() {
        String w9Id = "";
        w9Id = UUID.randomUUID().toString();
        return w9Id;
    }

    private static boolean isTripAdded(Context context) {
        boolean isTripAdded = false;
        try {
            JSONArray whereClauseArray = new JSONArray();

            try {
                JSONObject conditionJobj = new JSONObject();
                conditionJobj.put("conditionType", "attribute");
                conditionJobj.put("columnName", DELIVERY_BOY_ID_IN_TRIPS_COLUMN_NAME);
                conditionJobj.put("valueDataType", "string");
                conditionJobj.put("value", UserInfoPreferenceUtility.getUserName());
                conditionJobj.put("operator", "=");
                whereClauseArray.put(conditionJobj);
            }catch (Exception e){
                e.printStackTrace();
            }

            List<Feature> featureList = DeliveryDataModel.getInstance().getTripEntity().getFeatureTable().getFeaturesAsLike(null, whereClauseArray, "AND", null, context,false);
            Calendar calendar1 = Calendar.getInstance();
            SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");
            String currentDate = formatter1.format(calendar1.getTime());
            for (Feature feature : featureList) {
                String startTime = String.valueOf(feature.getAttributes().get("starttime"));
                Log.d("starttime", "isTripAdded: " + feature.getAttributes().get("starttime"));
                try {
                    if(startTime.contains("T")){
                        if (startTime.contains("T")) {
                            startTime = startTime.replace("T", " ");
                        }
                        if (startTime.contains("Z")) {
                            startTime = startTime.replace("Z", "");
                        }
                        @SuppressLint("SimpleDateFormat") Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
                        @SuppressLint("SimpleDateFormat") String newstring = new SimpleDateFormat("dd-MM-yyyy").format(date);
                        if (currentDate.equalsIgnoreCase(newstring)) {
                            isTripAdded = true;
                            break;
                        }
                    }else{
                        @SuppressLint("SimpleDateFormat") Date date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(startTime);
                        @SuppressLint("SimpleDateFormat") String newstring = new SimpleDateFormat("dd-MM-yyyy").format(date);
                        if (currentDate.equalsIgnoreCase(newstring)) {
                            isTripAdded = true;
                            break;
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return isTripAdded;
    }

    public static Feature getCurrentTripFeature(Context context) {
        Feature currentTripFeature = null;
        try {
            JSONArray whereClauseArray = new JSONArray();
            try {
                JSONObject conditionJobj = new JSONObject();
                conditionJobj.put("conditionType", "attribute");
                conditionJobj.put("columnName", DELIVERY_BOY_ID_IN_TRIPS_COLUMN_NAME);
                conditionJobj.put("valueDataType", "string");
                conditionJobj.put("value", UserInfoPreferenceUtility.getUserName());
                conditionJobj.put("operator", "=");
                whereClauseArray.put(conditionJobj);
            }catch (Exception e){
                e.printStackTrace();
            }
            List<Feature> featureList = DeliveryDataModel.getInstance().getTripEntity().getFeatureTable().getFeaturesAsLike(null, whereClauseArray, "AND", null, context,false);
            Calendar calendar1 = Calendar.getInstance();
            SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");
            String currentDate = formatter1.format(calendar1.getTime());
            for (Feature feature : featureList) {
                String startTime = String.valueOf(feature.getAttributes().get("starttime"));
                Log.d("starttime", "isTripAdded: " + feature.getAttributes().get("starttime"));
                try {
                    if(startTime.contains("T")){
                        if (startTime.contains("T")) {
                            startTime = startTime.replace("T", " ");
                        }
                        if (startTime.contains("Z")) {
                            startTime = startTime.replace("Z", "");
                        }
                        @SuppressLint("SimpleDateFormat") Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
                        @SuppressLint("SimpleDateFormat") String newstring = new SimpleDateFormat("dd-MM-yyyy").format(date);
                        if (currentDate.equalsIgnoreCase(newstring)) {
                            currentTripFeature = feature;
                            break;
                        }
                    }else{
                        @SuppressLint("SimpleDateFormat") Date date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(startTime);
                        String newstring = new SimpleDateFormat("dd-MM-yyyy").format(date);
                        if (currentDate.equalsIgnoreCase(newstring)) {
                            currentTripFeature = feature;
                            break;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return currentTripFeature;
    }

    private static boolean isInventoryAddedForCurrentTrip(Context context, Feature currentTripFeature) {
        boolean isInventoryAdded=false;
        try {
            JSONArray whereClauseArray = new JSONArray();
            try {
                JSONObject conditionJobj = new JSONObject();
                conditionJobj.put("conditionType", "attribute");
                conditionJobj.put("columnName", TRIP_ID_COLUMN_NAME_IN_TRIP_ITEM_TABLE);
                conditionJobj.put("valueDataType", "string");
                conditionJobj.put("value", currentTripFeature.getFeatureId());
                conditionJobj.put("operator", "=");
                whereClauseArray.put(conditionJobj);
            }catch (Exception e){
                e.printStackTrace();
            }
            List<Feature> featureList = DeliveryDataModel.getInstance().getTripItemEntity().getFeatureTable().getFeaturesAsLike(null, whereClauseArray, "AND", null, context,false);

            if(featureList == null || featureList.isEmpty()){
                isInventoryAdded=false;
            }else{
                isInventoryAdded=true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isInventoryAdded;
    }

    public static void initiateTripData(Context context) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TripDataModel.getInstance().setTodayTripAdded(isTripAdded(context));
                        if(TripDataModel.getInstance().getTodayTripAdded()){
                            Feature feature=getCurrentTripFeature(context);
                            TripDataModel.getInstance().setTodayTripFeature(feature);
                            if(feature!= null){
                                boolean isInventoryAddedForCurrentTrip=isInventoryAddedForCurrentTrip(context,feature);
                                TripDataModel.getInstance().setInventoryItemAddedForCurrentTrip(isInventoryAddedForCurrentTrip);
                            }else{
                                TripDataModel.getInstance().setInventoryItemAddedForCurrentTrip(false);
                            }
                        }else{
                            TripDataModel.getInstance().setInventoryItemAddedForCurrentTrip(false);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
