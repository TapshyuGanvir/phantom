package com.sixsimplex.treasurehunt.Phantom1.CURD.upload;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.data.FeatureTable;
import com.sixsimplex.treasurehunt.revelocore.editing.model.Attachment;
import com.sixsimplex.treasurehunt.revelocore.geopackage.utils.AttachmentConstant;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.upload.UploadFile;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UploadEditedFeatureAttachments extends AsyncTask<String, Integer, JSONObject> {
    String className = "UploadEditedFeatureAttachments";

    Context context;
    boolean uploadTraversalGraph;
    CMGraph cmGraph;
    String conceptModelName;
    String accessToken;
    String userName;
    String surveyName;
    String entityName;
    UploadInterface uploadInterface;

    public UploadEditedFeatureAttachments(Context context, String accessToken, String entityName,String userName,
                                          String surveyName, String conceptModelName, CMGraph cmGraph,
                                          UploadInterface uploadInterface, boolean uploadTraversalGraph) {
        this.context = context;
        this.uploadTraversalGraph = uploadTraversalGraph;
        this.conceptModelName = conceptModelName;
        this.accessToken=accessToken;
        this.userName=userName;
        this.surveyName=surveyName;
        this.uploadInterface=uploadInterface;
        this.cmGraph = cmGraph;
        this.entityName=entityName;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        JSONObject attachmentUploadResponse = uploadAttachment(context, conceptModelName, cmGraph);
        return attachmentUploadResponse;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if (uploadTraversalGraph) {
            ReveloLogger.error(className, "onPostExecute", "preparing to upload traversal");
            new TraversalUploadNew(context, accessToken, userName, surveyName, conceptModelName, cmGraph, uploadInterface).execute();
        }
      uploadInterface.OnUploadFinished(true,jsonObject);
    }


    private JSONObject uploadAttachment(Context context, String conceptModelName, CMGraph cmGraph/*, GeoPackage dataGeoPackage*/) {

        JSONObject layerAttachmentResponse = new JSONObject();
        ReveloLogger.debug(className, "doInBackground-uploadAttachment", "starting to iterate through entities, except trails");
        Set<CMEntity> cmEntitiesSet = cmGraph.getAllVertices();
        Iterator<CMEntity> iterator = cmEntitiesSet.iterator();
        while (iterator.hasNext()) {
            CMEntity cmEntity = iterator.next();
            String entity = cmEntity.getName();
            if (! entity.equalsIgnoreCase(AppConstants.TRAIL_TABLE_NAME)) {
                if(entity.equalsIgnoreCase(entityName)){
                    ReveloLogger.debug(className, "doInBackground-uploadAttachment", "checking attachments for entity- " + entity);

                    FeatureTable featureTable = cmEntity.getFeatureTable();
            /*if (featureTable == null) {
                if (dataGeoPackage.isTable(entityName)) {
                    if (dataGeoPackage.isFeatureTable(entityName)) {
                        FeatureDao featureDao = dataGeoPackage.getFeatureDao(entityName);
                        featureTable = new FeatureTable(featureDao, featureLayer);

                    } else if (dataGeoPackage.isAttributeTable(entityName)) {
                        AttributesDao attributesDao = dataGeoPackage.getAttributesDao(entityName);
                        featureTable = new FeatureTable(attributesDao, featureLayer);
                    }

                    featureLayer.setFeatureTable(featureTable);
                }
            }*/
                    if (featureTable != null) {

                        Map<Object, List<Attachment>> addedAttachmentMap = featureTable.getAllAttachmentsMap(AttachmentConstant.ADD_OP, context);
                        JSONObject addAttachmentResponse = addAttachment(featureTable, addedAttachmentMap, conceptModelName, entity, context);

                        Map<Object, List<Attachment>> deletedAttachmentMap = featureTable.getAllAttachmentsMap(AttachmentConstant.DELETE_OP, context);
                        JSONObject deleteAttachmentResponse = deleteAttachment(featureTable, deletedAttachmentMap, conceptModelName, entity, context);
                        ReveloLogger.debug(className, "doInBackground-uploadAttachment", "creating consilidated json of upload attachment responses");
                        try {

                            JSONObject operationJsonObject = new JSONObject();
                            operationJsonObject.put(AppConstants.ADD, addAttachmentResponse);
                            operationJsonObject.put(AppConstants.DELETE, deleteAttachmentResponse);
                            layerAttachmentResponse.put(cmEntity.getLabel(), operationJsonObject);

                        } catch (Exception e) {
                            ReveloLogger.error(className, "doInBackground-uploadAttachment", "could not create consolidated json of upload attachment responses - Exception " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else {
                        ReveloLogger.error(className, "doInBackground-uploadAttachment", "could not get feature table for entity- " + entity + ". Skipping this entity.");
                    }
                }
            }
        }
        ReveloLogger.debug(className, "doInBackground-uploadAttachment", "returning consolidated json of upload attachment responses");
        return layerAttachmentResponse;
    }

    private JSONObject addAttachment(FeatureTable featureTable, Map<Object, List<Attachment>> attachmentMap, String dataModelName, String layerName, Context context) {
        ReveloLogger.debug(className, "addAttachment", "uploading newly added attachments for layer " + layerName);
        JSONObject idAttachmentResponse = new JSONObject();

        try {

            if (attachmentMap != null) {
                ReveloLogger.debug(className, "addAttachment", "uploading attachments for " + attachmentMap.size() + " features");
                for (Object id : attachmentMap.keySet()) {

                    List<Attachment> attachmentList = attachmentMap.get(id);

                    if (attachmentList != null) {
                        ReveloLogger.debug(className, "addAttachment", "creating folder and description object for " + attachmentList.size() + " attachments for " + id + " feature");
                        File uploadAttachmentFolder = AppFolderStructure.createUploadAttachmentFolder(context);

                        JSONObject descriptionJsonObject = new JSONObject();

                        int attachmentCount = 0;

                        for (Attachment attachment : attachmentList) {

                            int isNew = attachment.getIsNew();//only added attachment

                            if (isNew == 1) {

                                if (attachment.getAttachmentName() != null) {
                                    String attachmentName = attachment.getAttachmentName() == null ? "attachment_" + attachmentCount : attachment.getAttachmentName();
                                    String caption = attachment.getLabel() == null ? "" : attachment.getLabel();
                                    String username = attachment.getUserName() == null ? UserInfoPreferenceUtility.getUserName() : attachment.getUserName();
                                    String userRole = attachment.getUserRole() == null ? UserInfoPreferenceUtility.getRole() : attachment.getUserRole();
                                    double lat = attachment.getLat();
                                    double lng = attachment.getLng();
                                    double zValue = attachment.getZValue();
                                    double accuracy = attachment.getAccuracy();

                                    JSONObject attachmentJsonObject = new JSONObject();
                                    attachmentJsonObject.put("caption", caption);
                                    attachmentJsonObject.put("username", username);
                                    attachmentJsonObject.put("userrole", userRole);
                                    attachmentJsonObject.put("lat", lat);
                                    attachmentJsonObject.put("lng", lng);
                                    attachmentJsonObject.put("zvalue", zValue);
                                    attachmentJsonObject.put("accuracy", accuracy);
                                    attachmentJsonObject.put("isnew", isNew);

                                    descriptionJsonObject.put(attachmentName, attachmentJsonObject);
                                    Log.i("eeee", "attachment :" + attachmentCount + " : " + attachmentJsonObject.toString());
                                    File attachmentFile = attachment.getFile();
                                    if (attachmentFile != null) {
                                        AppFolderStructure.moveFile(attachmentFile, uploadAttachmentFolder);
                                    }
                                    attachmentCount++;
                                }

                            }
                        }

                        JSONObject attachmentInfoJsonObject = new JSONObject();
                        attachmentInfoJsonObject.put("attachmentsInfo", descriptionJsonObject);
                        ReveloLogger.debug(className, "addAttachment", "writing description json to file ");
                        File descriptionFile = AppFolderStructure.createAttachmentDescriptionFile(context);
                        if (descriptionFile.exists()) {
                            FileUtils.writeStringToFile(descriptionFile, attachmentInfoJsonObject.toString());
                        }
                        ReveloLogger.debug(className, "addAttachment", "creating zip");
                        File attachmentZipFile = AppFolderStructure.createUploadAttachmentZIP(context);

                        AppFolderStructure.createZipFile(uploadAttachmentFolder, attachmentZipFile, true);
                        ReveloLogger.debug(className, "addAttachment", "getting upload url for layername-" + layerName + " , id -" + id + ", surveyname-" + surveyName + ", datamodelname-" + dataModelName);
                        String url = UrlStore.addAttachmentUrl(dataModelName, surveyName, layerName, id);
                        ReveloLogger.debug(className, "addAttachment", "Uploading zip..");
                        String result = UploadFile.uploadFile(attachmentZipFile.getName(), accessToken, url, attachmentZipFile, context, (bytesUploaded, fileLength) -> {
                        });
                        ReveloLogger.debug(className, "addAttachment", "Uploading zip..result - " + result);
                        ReveloLogger.debug(className, "addAttachment", "deleting created upload folder");
                        AppFolderStructure.deleteUploadFolder(context);

                        if (result.equalsIgnoreCase(AppConstants.SUCCESS)) {
                            ReveloLogger.debug(className, "addAttachment", "upload attachments successful for " + id + " - " + layerName);
                            String updated_for_ids = "";
                            String not_updated_for_ids = "";
                            for (Attachment attachment : attachmentList) {
                                boolean updated = featureTable.updateAttachmentEntryIsNew(layerName, context, String.valueOf(id), attachment.getAttachmentName(), AttachmentConstant.NONE_OP);
                                if (updated) {
                                    updated_for_ids += " " + attachment.getAttachmentName();
                                }
                                else {
                                    not_updated_for_ids += " " + attachment.getAttachmentName();
                                }
                            }
                            if (! updated_for_ids.isEmpty()) {
                                ReveloLogger.debug(className, "addAttachment", "after uploading successfully, entries updated in local db for : " + updated_for_ids);
                            }
                            if (! not_updated_for_ids.isEmpty()) {
                                ReveloLogger.error(className, "addAttachment", "after uploading successfully, could not update entries in local db for :  " + updated_for_ids);
                            }
                        }
                        else {
                            ReveloLogger.error(className, "addAttachment", "upload attachments failed for " + id + " - " + layerName);
                        }
                        ReveloLogger.debug(className, "addAttachment", "attachment count for " + id + " - " + layerName + "  is " + attachmentCount);
                        JSONObject statusJsonObject = new JSONObject();
                        statusJsonObject.put(AppConstants.STATUS, result);
                        statusJsonObject.put(AppConstants.ATTACHMENT_COUNT, attachmentCount);
                        idAttachmentResponse.put(id.toString(), statusJsonObject);
                    }
                    else {
                        ReveloLogger.debug(className, "addAttachment", "no attachments found for " + id + " feature");
                    }
                }
            }
            else {
                ReveloLogger.debug(className, "addAttachment", "no newly added attachments found for layer " + layerName);
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "addAttachment", "Exception occurred while uploading attachment - " + e.getMessage());
            e.printStackTrace();
        }

        return idAttachmentResponse;
    }

    private JSONObject deleteAttachment(FeatureTable featureTable, Map<Object, List<Attachment>> deletedAttachmentMap, String dataModelName, String layerName, Context context) {
        ReveloLogger.debug(className, "deleteAttachment", "uploading deleted attachments for layer " + layerName);
        JSONObject idAttachmentResponse = new JSONObject();

        try {
            if (deletedAttachmentMap != null) {
                for (Object id : deletedAttachmentMap.keySet()) {
                    ReveloLogger.debug(className, "deleteAttachment", "iterating through every feature for deleted attachments for layer " + layerName);
                    List<Attachment> attachmentList = deletedAttachmentMap.get(id);
                    if (attachmentList != null) {
                        ReveloLogger.debug(className, "deleteAttachment", attachmentList.size() + " deleted attachments found for feature " + id + " of layer " + layerName);

                        ReveloLogger.debug(className, "deleteAttachment", "creating json array of names for deleted attachments of feature " + id + " of layer " + layerName);
                        JSONArray attachmentNameArray = new JSONArray();
                        for (Attachment attachment : attachmentList) {
                            attachmentNameArray.put(attachment.getAttachmentName());
                        }

                        if (attachmentNameArray.length() > 0) {
                            ReveloLogger.debug(className, "deleteAttachment", "json array of " + attachmentNameArray.length() + " names created.");
                            JSONObject deleteAttachmentJsonObject = new JSONObject();
                            try {
                                deleteAttachmentJsonObject.put("attachmentNamesList", attachmentNameArray);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ReveloLogger.debug(className, "deleteAttachment", "getting delete url for id-" + id + ", layer-" + layerName + ", surveyname-" + surveyName + ", datamodelname- " + dataModelName);
                            String url = UrlStore.deleteAttachmentUrl(dataModelName, surveyName, layerName, id);
                            ReveloLogger.debug(className, "deleteAttachment", "uploading deleted attachments list..");
                            String response = UploadFile.doPutToSendJson(url, accessToken, deleteAttachmentJsonObject.toString(), context);

                            try {
                                if (response.equalsIgnoreCase(AppConstants.SUCCESS)) {
                                    ReveloLogger.debug(className, "deleteAttachment", "uploading deleted attachments list..successful");
                                    for (Attachment attachment : attachmentList) {
                                        featureTable.deleteAttachment(layerName, context, id, attachment.getAttachmentName(), AttachmentConstant.DELETE_OP);
                                    }
                                }
                                else {
                                    ReveloLogger.error(className, "deleteAttachment", "uploading deleted attachments list..failed");
                                }

                                idAttachmentResponse.put(id.toString(), response);
                            } catch (Exception e) {
                                ReveloLogger.error(className, "deleteAttachment", "uploading deleted attachments list..Could not read response - Exception " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        else {
                            ReveloLogger.error(className, "deleteAttachment", "could not create json array of names for deleted attachments of feature " + id + " of layer " + layerName);
                        }
                    }
                    else {
                        ReveloLogger.debug(className, "deleteAttachment", "no deleted attachments found for feature " + id + " of layer " + layerName);
                    }
                }
            }
            else {
                ReveloLogger.debug(className, "deleteAttachment", "no deleted attachments found for layer " + layerName);
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "deleteAttachment", "exception occurred while uploading deleted attachments for layer " + layerName);
            e.printStackTrace();
        }
        return idAttachmentResponse;
    }

}
