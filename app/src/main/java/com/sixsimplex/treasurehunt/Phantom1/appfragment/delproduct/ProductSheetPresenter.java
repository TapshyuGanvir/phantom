package com.sixsimplex.treasurehunt.Phantom1.appfragment.delproduct;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.sixsimplex.treasurehunt.Phantom1.CURD.AddAndUpload;
import com.sixsimplex.treasurehunt.Phantom1.CURD.EditAndUpload;
import com.sixsimplex.treasurehunt.Phantom1.CURD.upload.UploadInterface;
import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.Phantom1.deliveryservice.DeliveryService;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.util.DatePickerMethods;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProductSheetPresenter {


    private static String generateAutomaticId() {
        String w9Id = "";
        w9Id = UUID.randomUUID().toString();
        return w9Id;
    }

    public void updateDataToServer(Context context, IdeliveryActivityView ideliveryActivityView, Map<String, Object> attributeValueMap, Feature targetFeature, String mode) {
        try {


            JSONObject jsonObject = DeliveryDataModel.getInstance().getTraversalEntity()
                    .updateFeatureTraversal(String.valueOf(targetFeature.getFeatureId()), true, context);
            JSONObject resulJson = EditAndUpload.perform(context,
                    AppConstants.EDIT,
                    DeliveryDataModel.getInstance().getTraversalEntity(),
                    attributeValueMap,
                    null,
                    DeliveryDataModel.getInstance().getTraversalEntity().getFeatureTable(),
                    null,
                    String.valueOf(targetFeature.getFeatureId()),
                    String.valueOf(targetFeature.getFeatureLabel()),
                    null,
                    null,
                    "", new UploadInterface() {
                        @Override
                        public void OnUploadStarted() {

                        }

                        @Override
                        public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                        }
                    },true);

            if (resulJson != null && resulJson.has("status") && resulJson.getString("status").equalsIgnoreCase("success")) {

            }

            if (jsonObject != null) {
                if (jsonObject.has("status")) {
                    if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                        ideliveryActivityView.onTargetFeatureUpdated(mode, -1);
                    } else {

                        // if not update succeccfully deliver should be false
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Feature> getDeliveryItemForTargetFeature(Context context, Feature targetFeature) {
        List<Feature> featureList = new ArrayList<>();
        JSONArray whereClauseArray = new JSONArray();
        try {
            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", "dropoffid");
            conditionJobj.put("valueDataType", "string");
            conditionJobj.put("value", targetFeature.getFeatureId());
            conditionJobj.put("operator", "=");
            whereClauseArray.put(conditionJobj);
            featureList = DeliveryDataModel.getInstance().getDropOffItemEntity().getFeatureTable().getFeaturesAsLike(null, whereClauseArray, "AND", null, context, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return featureList;
    }

    public List<Feature> getProducts(Context context) {
        List<Feature> productList = new ArrayList<>();
        try {
            productList = DeliveryDataModel.getInstance().getProductEntity().getFeatureTable().getallFeaturesList(context, null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productList;
    }

    public void editQuantityOfExistingDropItem(Context context, String featureId, int productQntVal) {
        try {
            Location location = DeliveryService.lastLocation;
            if (location == null) {
                location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(0.0);
                location.setLongitude(0.0);
            }
            Map<String, Object> attributeValueMap = new HashMap<>();
            attributeValueMap.put("quantity", productQntVal);
            JSONObject resulJson =EditAndUpload.perform(context,
                    AppConstants.EDIT,
                    DeliveryDataModel.getInstance().getDropOffItemEntity(),
                    attributeValueMap,
                    null,
                    DeliveryDataModel.getInstance().getDropOffItemEntity().getFeatureTable(),
                    null,
                    featureId,
                    featureId,
                    location,
                    null,
                    "", new UploadInterface() {
                        @Override
                        public void OnUploadStarted() {

                        }

                        @Override
                        public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                        }
                    },false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addExtraDropItem(Context context, Feature product, int productQntVal, Feature targetFeature) {

        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
        try {
            String w91d = generateAutomaticId();
            Location location = DeliveryService.lastLocation;
            if (location == null) {
                location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(0.0);
                location.setLongitude(0.0);
            }
            Map<String, Object> attributeValueMap = new HashMap<>();
            attributeValueMap.put("quantity", productQntVal);
            attributeValueMap.put("dropoffid", targetFeature.getFeatureId());
            attributeValueMap.put("productid", product.getFeatureId());
            attributeValueMap.put("dropitemid", w91d);
            attributeValueMap.put(AppConstants.W9_ENTITY_CLASS_NAME, DeliveryDataModel.getInstance().getDropOffItemEntity().getName());
            try {
                JSONObject metadataJSON = new JSONObject();
                if (location != null) {
                    metadataJSON.put(AppConstants.W9_LATITUDE, location.getLatitude());
                    metadataJSON.put(AppConstants.W9_LONGITUDE, location.getLongitude());
                    metadataJSON.put(AppConstants.W9_ACCURACY, location.getAccuracy());
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
            JSONObject addTripFeatureResultJson = AddAndUpload.perform(context,
                    AppConstants.ADD,
                    DeliveryDataModel.getInstance().getDropOffItemEntity(),
                    attributeValueMap, null,
                    DeliveryDataModel.getInstance().getDropOffItemEntity().getFeatureTable(),
                    null,
                    w91d,
                    w91d,
                    location,
                    null,
                    "", new UploadInterface() {
                        @Override
                        public void OnUploadStarted() {

                        }

                        @Override
                        public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
