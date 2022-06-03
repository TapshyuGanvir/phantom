package com.sixsimplex.treasurehunt.Phantom1.CURD;

import android.content.Context;
import android.location.Location;

import com.sixsimplex.treasurehunt.Phantom1.CURD.upload.UploadAddedFeature;
import com.sixsimplex.treasurehunt.Phantom1.CURD.upload.UploadEditedFeature;
import com.sixsimplex.treasurehunt.Phantom1.CURD.upload.UploadInterface;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.data.FeatureTable;
import com.sixsimplex.treasurehunt.revelocore.editing.model.Attachment;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class EditAndUpload {
    public static JSONObject perform(Context context, String formType, CMEntity cmEntity, Map<String, Object> attributeValueMap, List<Attachment> attachmentList, FeatureTable featureTable, JSONObject geometry, String w9Id, String featureLabel, Location location, JSONObject permissionJson, String submitStatus, UploadInterface uploadInterface,boolean uploadTraversalGraph) {
        boolean addedSuccessfully = false;
        JSONObject responseJson = new JSONObject();
        try {
            responseJson = featureTable.updateDeliveryRecordAndMap(attributeValueMap, context, w9Id, featureLabel, attachmentList, location, cmEntity.getW9IdProperty(), null);
            if (responseJson.has("status") && responseJson.getString("status").equalsIgnoreCase("success")) {
                new UploadEditedFeature(context,
                        uploadInterface,
                        cmEntity.getName(),
                        null,
                        w9Id, featureLabel,
                        false,
                        uploadTraversalGraph).start();
                addedSuccessfully = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ReveloLogger.debug("AddEditFeaturePresenter", "add", "New feature addition successful? " + addedSuccessfully);
        if (addedSuccessfully && formType.equalsIgnoreCase(AppConstants.SHADOW)) {
            ReveloLogger.debug("AddEditFeaturePresenter", "add", "shadow feature converted to full feature successfully, moving to delete shadow feature entry from db");
            featureTable.deleteRecord(w9Id, featureLabel, cmEntity.getW9IdProperty(), null, context, null, permissionJson, null, true);
        }
        return responseJson;
    }
}
