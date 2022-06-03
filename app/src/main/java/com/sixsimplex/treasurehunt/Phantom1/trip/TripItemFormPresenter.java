package com.sixsimplex.treasurehunt.Phantom1.trip;

import android.content.Context;
import android.location.Location;

import com.sixsimplex.treasurehunt.Phantom1.CURD.AddAndUpload;
import com.sixsimplex.treasurehunt.Phantom1.CURD.upload.UploadInterface;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.revelocore.util.DatePickerMethods;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TripItemFormPresenter {

    private static String generateAutomaticId() {
        String w9Id = "";
        w9Id = UUID.randomUUID().toString();
        return w9Id;
    }

    public JSONObject addTripItemsInDataBase(Context context, String productId, Integer productQntVal, String tripid, Location location,UploadInterface uploadInterface) {
        JSONObject addTripFeatureResultJson = new JSONObject();
        Map<String, Object> attributeValueMap = new HashMap<>();
        String w91d = generateAutomaticId();

        attributeValueMap.put("quantity", productQntVal);
        attributeValueMap.put("tripitemid", w91d);
        attributeValueMap.put("tripid", tripid);
        attributeValueMap.put("productid", productId);
        attributeValueMap.put(AppConstants.W9_ENTITY_CLASS_NAME, DeliveryDataModel.getInstance().getTripItemEntity().getName());
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
        addTripFeatureResultJson = AddAndUpload.perform(context,
                AppConstants.ADD,
                DeliveryDataModel.getInstance().getTripItemEntity(),
                attributeValueMap, null,
                DeliveryDataModel.getInstance().getTripItemEntity().getFeatureTable(),
                null,
                w91d,
                w91d,
                location,
                null,
                "", uploadInterface);

        return addTripFeatureResultJson;
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
}
