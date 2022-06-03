package com.sixsimplex.treasurehunt.revelocore.editing.presenter;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.data.FeatureTable;
import com.sixsimplex.treasurehunt.revelocore.data.GeoJsonUtils;
import com.sixsimplex.treasurehunt.revelocore.editing.model.Attachment;
import com.sixsimplex.treasurehunt.revelocore.editing.model.AttributeTagModel;
import com.sixsimplex.treasurehunt.revelocore.editing.view.IAddEditFeatureView;
import com.sixsimplex.treasurehunt.revelocore.geopackage.utils.AttachmentConstant;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMEdge;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.graph.jsongraph.JSONGraph;
import com.sixsimplex.treasurehunt.revelocore.layer.Attribute;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;
import com.sixsimplex.treasurehunt.revelocore.layer.PropertyGroupsModel;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.DatePickerMethods;
import com.sixsimplex.treasurehunt.revelocore.util.RegexInputFilter;
import com.sixsimplex.treasurehunt.revelocore.util.ToastUtility;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class AddEditFeaturePresenter implements IAddEditFeaturePresenter {

    private static final String ClassName = "AddEditFeaturePresenter";
    private static InputFilter[] inputFilter = null;
    private final IAddEditFeatureView iAddFeatureView;
    private Activity activity;
    private CreateFormView createFormView;


    public AddEditFeaturePresenter(IAddEditFeatureView iAddFeatureView, Activity activity) {
        this.activity = activity;
        this.iAddFeatureView = iAddFeatureView;
    }

    private static InputFilter[] getInputFilter() {
        String blockCharacterSet = "~#^|$%&*!/";
        if (inputFilter == null) {
            inputFilter = new InputFilter[1];
            inputFilter[0] = new InputFilter() {

                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

                    if (source != null && blockCharacterSet.contains(("" + source))) {
                        return "";
                    }
                    return null;
                }
            };
        }
        return inputFilter;
    }



    @Override
    public void createForm(Activity activity, String formType, FeatureLayer featureLayer, CMEntity cmEntity, Feature feature, JSONObject geometryGeoJson, String measurementString, double measurementValue, Map<String, String> jurisdictionNamesIdMap, Map<String, Object> jurisdictionValuesMap, Location location, JSONObject permissionJson) {

        createFormView = new CreateFormView(activity, iAddFeatureView, formType, featureLayer, cmEntity, feature, geometryGeoJson, measurementString, measurementValue, jurisdictionNamesIdMap, jurisdictionValuesMap, location, permissionJson);
        createFormView.execute();
    }

    @Override
    public void createAttachmentView(String formType, Feature feature) {
        new CreateAttachmentViewTask(formType, feature).execute();
     /*   try {
            JSONObject cmGraphResult = CMUtils.getCMGraph(activity);
            if (cmGraphResult.has("status") && cmGraphResult.getString("status").equalsIgnoreCase("success")) {
                //if (entitiesResult.getOperationStatus().equalsIgnoreCase(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_SUCCESS)) {
                CMGraph cmGraph = (CMGraph) cmGraphResult.get("result");
                JSONObject cmEntityResult = cmGraph.getVertex("name", feature.getEntityName());
                if (cmEntityResult.has("status") && cmEntityResult.getString("status").equalsIgnoreCase("success")) {
                    CMEntity cmEntity = (CMEntity) cmEntityResult.get("result");
                    FeatureTable featureTable = cmEntity.getFeatureTable();

                    //  FeatureTable featureTable = feature.getFeatureLayer().getFeatureTable();
                    String w9Id = String.valueOf(feature.getFeatureId());
                    String layerName = feature.getEntityName();

                    List<Attachment> attachmentsList = featureTable.getAttachmentsRecord(w9Id, AttachmentConstant.ALL_ATTACHMENTS, activity);

                    iAddFeatureView.showAttachment(attachmentsList);
                } else {
                    ReveloLogger.error("AddEditFeaturePresenter", "createAttachmentView", "Could not create attachment view -  Error fetching CMEntity");
                }
            } else {
                ReveloLogger.error("AddEditFeaturePresenter", "createAttachmentView", "Could not create attachment view -  Error fetching CMGraph");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error("AddEditFeaturePresenter", "createAttachmentView", "Could not create attachment view -  Exception-  " + e.getCause());
        }*/
    }

    @Override
    public void saveEditFeature(Activity activity, List<Attachment> attachmentList,
                                List<Attachment> getSelectForDeleteAttachmentFileList,
                                Map<Integer, Attribute> attributeViewModelMap, String formType, Feature feature,
                                JSONObject geometry, boolean featureIsInAdd, ViewPager viewPager,
                                CMEntity cmEntity, BottomSheetDialog bottomSheetDialog,
                                Location location, JSONObject permissionJson, View buttonView, IAddEditFeatureView iAddEditFeatureView) {

        saveAttribute(activity, attachmentList, getSelectForDeleteAttachmentFileList, attributeViewModelMap, formType,
                feature, geometry, featureIsInAdd, viewPager, cmEntity, bottomSheetDialog, location, permissionJson,
                buttonView, iAddEditFeatureView);
    }

    @Override
    public void autocompleteClick(Activity activity, String selectedItemValue, AutoCompleteTextView autoCompleteTextView) {
        createFormView.autocompleteClickListener(activity, selectedItemValue, autoCompleteTextView);
    }

    private void saveAttribute(Activity activity, List<Attachment> attachmentList, List<Attachment> getSelectForDeleteAttachmentFileList,
                               Map<Integer, Attribute> attributeViewModelMap, String formType, Feature feature, JSONObject geometry,
                               boolean featureIsInAdd, ViewPager viewPager, CMEntity cmEntity,
                               BottomSheetDialog bottomSheetDialog, Location location,
                               JSONObject permissionJson, View buttonView, IAddEditFeatureView iAddEditFeatureView) {

        this.activity = activity;
        new SaveAttributeTask(activity, attachmentList, getSelectForDeleteAttachmentFileList, attributeViewModelMap, formType,
                feature, geometry, featureIsInAdd, viewPager, cmEntity, bottomSheetDialog, location, permissionJson, buttonView, iAddEditFeatureView).execute();
    }

    private void focusOnView(final View view) {
        try {
            if (view != null) {
                ViewParent viewParent = view.getParent();
                if (viewParent != null) {
                    viewParent.requestChildFocus(view, view);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAddedFeatureDuplicate(String formType, Feature feature, Map<String, Object> attributeValueMap, JSONObject geometry, String columnName, Object enteredValue, String w9IdPropertyLabel, String featurelabelValue, FeatureTable featureTable, CMEntity cmEntity, List<Attachment> getSelectForDeleteAttachmentFileList, List<Attachment> attachmentList, BottomSheetDialog bottomSheetDialog, Location location, Context context, JSONObject permissionJson, View buttonView) {

        boolean isDuplicateFeature = featureTable.doesFeatureExists(columnName, enteredValue, context, true, false);

        if (isDuplicateFeature) {
            String errorMsg = " Feature by the " + w9IdPropertyLabel + " " + featurelabelValue + "(" + enteredValue + ") already exists! Please try again with another " + w9IdPropertyLabel;//Site feature by the name site123 already exists
            iAddFeatureView.error(errorMsg, true);

        } else {
            /*saveFeature(formType, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList,
                    attachmentList, featureTable, bottomSheetDialog, (String) enteredValue, label, location, columnName, permissionJson, submitNow);*/
            confirmSaveFeature(activity, formType, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList, attachmentList, featureTable, cmEntity, bottomSheetDialog, (String) enteredValue, featurelabelValue, location, columnName, permissionJson, buttonView, iAddFeatureView);
        }
    }

    private void confirmSaveFeature(Activity activity, String formType, Feature feature, Map<String, Object> attributeValueMap, JSONObject geometry, List<Attachment> getSelectForDeleteAttachmentFileList, List<Attachment> attachmentList, FeatureTable featureTable, CMEntity cmEntity, BottomSheetDialog bottomSheetDialog, String w9Id, String featureLabel, Location location, String columnName, JSONObject permissionJson, View buttonView, IAddEditFeatureView iAddEditFeatureView) {
        boolean isFlowsApplicable = false;
        try {
            if (permissionJson != null && permissionJson.has("isFlowApplicable")) {
                isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
            }
            if (isFlowsApplicable) {
//                showPopup(activity, buttonView, formType, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList, attachmentList, featureTable, cmEntity, bottomSheetDialog, w9Id, featureLabel, location, columnName, permissionJson, iAddEditFeatureView);
            } else {
                saveFeature(formType, cmEntity, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList, attachmentList, featureTable, bottomSheetDialog, w9Id, featureLabel, location, columnName, permissionJson, AppConstants.SAVE_ONLY, iAddEditFeatureView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void showPopup(Context context, View v, String formType, Feature feature, Map<String, Object> attributeValueMap, JSONObject geometry, List<Attachment> getSelectForDeleteAttachmentFileList, List<Attachment> attachmentList, FeatureTable featureTable, CMEntity cmEntity, BottomSheetDialog bottomSheetDialog, String w9Id, String featureLabel, Location location, String columnName, JSONObject permissionJson, IAddEditFeatureView iAddEditFeatureView) {
//        PopupMenu popup = new PopupMenu(context, v);
//        // Inflate the menu from xml
////        popup.getMenuInflater().inflate(R.menu.popup_save_approve_disapprove, popup.getMenu());
//
//        String btnDisableReason = "";
//
//        if (formType.equalsIgnoreCase(AppConstants.EDIT)) {
//            try {
//                if (permissionJson != null && permissionJson.has("isFlowApplicable")) {
//                    boolean isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
//                    if (isFlowsApplicable) {
//                        boolean isAtBeginning = CMUtils.isInteractionAtBeginning(context, cmEntity, permissionJson.getString("currentFlowName"), permissionJson.getString("currentInteractionName"));
//                        if (isAtBeginning) {
////                            popup.getMenu().findItem(R.id.save_mark_disapproved).setVisible(false);
////                            popup.getMenu().findItem(R.id.save_now).setTitle("Save");
////                            popup.getMenu().findItem(R.id.save_mark_for_approval).setTitle("Save and Submit");
//                        } else {
////                            popup.getMenu().findItem(R.id.save_mark_disapproved).setVisible(true);
////
////                            popup.getMenu().findItem(R.id.save_mark_disapproved).setTitle("Rejected");
////                            popup.getMenu().findItem(R.id.save_now).setTitle("Save");
////                            popup.getMenu().findItem(R.id.save_mark_for_approval).setTitle("Save and Submit");
//                        }
//
//                    } else {
////                        popup.getMenu().findItem(R.id.save_mark_disapproved).setVisible(false);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
////            popup.getMenu().findItem(R.id.save_mark_disapproved).setVisible(false);
////            popup.getMenu().findItem(R.id.save_now).setTitle("Save");
////            popup.getMenu().findItem(R.id.save_mark_for_approval).setTitle("Save and Submit");
//        }
//        // Setup menu item selection
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.save_now:
//                        saveFeature(formType, cmEntity, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList, attachmentList, featureTable, bottomSheetDialog, w9Id, featureLabel, location, columnName, permissionJson, AppConstants.SAVE_ONLY, iAddEditFeatureView);
//                        return true;
//                    case R.id.save_mark_for_approval:
//                        saveFeature(formType, cmEntity, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList, attachmentList, featureTable, bottomSheetDialog, w9Id, featureLabel, location, columnName, permissionJson, AppConstants.SAVE_APPROVE, iAddEditFeatureView);
//                        return true;
//                    case R.id.save_mark_disapproved:
//                        saveFeature(formType, cmEntity, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList, attachmentList, featureTable, bottomSheetDialog, w9Id, featureLabel, location, columnName, permissionJson, AppConstants.SAVE_DISAPPROVE, iAddEditFeatureView);
//                        return true;
//                    default:
//                        saveFeature(formType, cmEntity, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList, attachmentList, featureTable, bottomSheetDialog, w9Id, featureLabel, location, columnName, permissionJson, AppConstants.SAVE_ONLY, iAddEditFeatureView);
//                        return false;
//                }
//            }
//        });
//        // Handle dismissal with: popup.setOnDismissListener(...);
//        // Show the menu
//        popup.show();
//    }

    private void saveFeature(String formType, CMEntity cmEntity, Feature feature, Map<String, Object> attributeValueMap, JSONObject geometry, List<Attachment> getSelectForDeleteAttachmentFileList, List<Attachment> attachmentList, FeatureTable featureTable, BottomSheetDialog bottomSheetDialog, String w9Id, String featureLabel, Location location, String columnName, JSONObject permissionJson, String submitStatus, IAddEditFeatureView iAddEditFeatureView) {

       /* try {

            if (formType.equalsIgnoreCase(AppConstants.EDIT)) {
                update(activity, attributeValueMap, attachmentList, featureTable, geometry, bottomSheetDialog, w9Id, featureLabel, location,
                        columnName, getSelectForDeleteAttachmentFileList, permissionJson, submitStatus);
                            } else {
                add(activity,formType, cmEntity,attributeValueMap, attachmentList, featureTable, geometry, bottomSheetDialog,
                        w9Id, featureLabel, location, permissionJson, submitStatus);
                            }

                    } catch (Exception e) {
                        e.printStackTrace();
            String errorMessage = e.getMessage();

//            AppMethods.showOkDialogBox(errorMessage, activity);
        }*/
        new SaveFeatureTask(formType, cmEntity, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList, attachmentList, featureTable, bottomSheetDialog, w9Id, featureLabel, location, columnName, permissionJson, submitStatus, iAddEditFeatureView).execute();
    }

    private JSONObject update(Activity activity, Map<String, Object> attributeValueMap, List<Attachment> attachmentList, FeatureTable featureTable, JSONObject geometry, BottomSheetDialog bottomSheetDialog, String w9Id, String featureLabel, Location location, String columnName, List<Attachment> getSelectForDeleteAttachmentFileList, JSONObject permissionJson, String submitStatus) {

        return featureTable.updateRecordAndMap(attributeValueMap, geometry, activity, bottomSheetDialog, w9Id, featureLabel, attachmentList, location, columnName, getSelectForDeleteAttachmentFileList, permissionJson, submitStatus);

    }

    private JSONObject add(Activity activity, String formType, CMEntity cmEntity, Map<String, Object> attributeValueMap, List<Attachment> attachmentList, FeatureTable featureTable, JSONObject geometry, BottomSheetDialog fragmentBottomSheet, String w9Id, String featureLabel, Location location, JSONObject permissionJson, String submitStatus) {
        boolean addedSuccessfully = false;
        JSONObject responseJson = new JSONObject();
        try {
            responseJson = featureTable.insertAddRecordInDbAndMap(attributeValueMap, geometry, activity, fragmentBottomSheet, w9Id, featureLabel, attachmentList, location, permissionJson, submitStatus);

            if (responseJson.has("status") && responseJson.getString("status").equalsIgnoreCase("success")) {
                addedSuccessfully = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ReveloLogger.debug("AddEditFeaturePresenter", "add", "New feature addition successful? " + addedSuccessfully);
        if (addedSuccessfully && formType.equalsIgnoreCase(AppConstants.SHADOW)) {
            ReveloLogger.debug("AddEditFeaturePresenter", "add", "shadow feature converted to full feature successfully, moving to delete shadow feature entry from db");
            featureTable.deleteRecord(w9Id, featureLabel, cmEntity.getW9IdProperty(), null, activity, null, permissionJson, null, true);
        }

        return responseJson;

    }

    private void collectSystemProperties(Map<String, Object> attributeValueMap, String formType, Feature feature, Location location) {

        try {

            JSONArray attachmentsInfoArray = new JSONArray();

            if (formType.equalsIgnoreCase(AppConstants.EDIT)) {
                Map<String, Object> addedAttributeValueMap = feature.getAttributes();

                if (addedAttributeValueMap.containsKey(AppConstants.W9_METADATA)) {
                    Object w9metaDataObject = addedAttributeValueMap.get(AppConstants.W9_METADATA);

                    if (w9metaDataObject != null) {

                        String w9MetaDataString = String.valueOf(w9metaDataObject);
                        JSONObject metaDataObj = null;
                        try {
                            if (!TextUtils.isEmpty(w9MetaDataString)) {
                                metaDataObj = new JSONObject(w9MetaDataString);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (metaDataObj != null) {
//                            if (metaDataObj.has(AppConstants.W9_ATTACHMENTS_INFO)) {
//                                Object w9AttachmentInfoObject = metaDataObj.get(AppConstants.FW9_ATTACHMENTS_INFO);
//                                if (w9AttachmentInfoObject != null) {
//                                    String w9AttachmentInfoArrayString = String.valueOf(w9AttachmentInfoObject);
//                                    try {
//                                        if (!TextUtils.isEmpty(w9AttachmentInfoArrayString)) {
//                                            attachmentsInfoArray = new JSONArray(w9AttachmentInfoArrayString);
//                                        }
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                        attachmentsInfoArray = new JSONArray();
//                                    }
//                                }
//                            }
                        }
                    }
                }
            }

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
//            metadataJSON.put(AppConstants.W9_ATTACHMENTS_INFO, attachmentsInfoArray);

            attributeValueMap.put(AppConstants.W9_METADATA, metadataJSON.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CreateAttachmentViewTask extends AsyncTask<JSONObject, String, JSONObject> {
        String formType;
        Feature feature;

        CreateAttachmentViewTask(String formType, Feature feature) {
            this.formType = formType;
            this.feature = feature;
        }

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            JSONObject responseJson = new JSONObject();
            try {
                responseJson.put("status", "failure");
                responseJson.put("message", "Reason Unknown");

                JSONObject cmGraphResult = CMUtils.getCMGraph(activity);
                if (cmGraphResult.has("status") && cmGraphResult.getString("status").equalsIgnoreCase("success")) {
                    //if (entitiesResult.getOperationStatus().equalsIgnoreCase(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_SUCCESS)) {
                    CMGraph cmGraph = (CMGraph) cmGraphResult.get("result");
                    JSONObject cmEntityResult = cmGraph.getVertex("name", feature.getEntityName());
                    if (cmEntityResult.has("status") && cmEntityResult.getString("status").equalsIgnoreCase("success")) {
                        CMEntity cmEntity = (CMEntity) cmEntityResult.get("result");
                        FeatureTable featureTable = cmEntity.getFeatureTable();

                        //  FeatureTable featureTable = feature.getFeatureLayer().getFeatureTable();
                        String w9Id = String.valueOf(feature.getFeatureId());
                        String layerName = feature.getEntityName();

                        List<Attachment> attachmentsList = featureTable.getAttachmentsRecord(w9Id, AttachmentConstant.ALL_ATTACHMENTS, activity);

                        responseJson.put("status", "success");
                        responseJson.put("message", attachmentsList);
                    } else {
                        responseJson.put("message", "Reason :Could not get entity");
                        ReveloLogger.error("AddEditFeaturePresenter", "createAttachmentView", "Could not create attachment view -  Error fetching CMEntity");
                    }
                } else {
                    responseJson.put("message", "Reason :Could not get graph");
                    ReveloLogger.error("AddEditFeaturePresenter", "createAttachmentView", "Could not create attachment view -  Error fetching CMGraph");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error("AddEditFeaturePresenter", "createAttachmentView", "Could not create attachment view -  Exception-  " + e.getCause());
            }
            return responseJson;
        }

        @Override
        protected void onPostExecute(JSONObject responseJson) {
            super.onPostExecute(responseJson);
            try {
                if (responseJson != null && responseJson.has("status")) {
                    if (responseJson.getString("status").equalsIgnoreCase("success")) {
                        List<Attachment> attachmentsList = (List<Attachment>) responseJson.get("message");
                        iAddFeatureView.showAttachment(attachmentsList);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class SaveAttributeTask extends AsyncTask<String, String, JSONObject> {

        Activity activity;
        List<Attachment> attachmentList;
        List<Attachment> getSelectForDeleteAttachmentFileList;
        Map<Integer, Attribute> attributeViewModelMap;
        String formType;
        Feature feature;
        JSONObject geometry;
        boolean featureIsInAdd;
        ViewPager viewPager;
        CMEntity cmEntity;
        BottomSheetDialog bottomSheetDialog;
        Location location;
        JSONObject permissionJson;
        View buttonView;
        IAddEditFeatureView iAddEditFeatureView;
        Map<String, Object> attributeValueMap;
        FeatureTable featureTable;
        String columnNameW9IdProperty, enteredValuew9Id, columnNameLabelProperty, featurelabelValue = "";
        String taskName = "SaveAttributeTask";

        public SaveAttributeTask(Activity activity, List<Attachment> attachmentList, List<Attachment> getSelectForDeleteAttachmentFileList,
                                 Map<Integer, Attribute> attributeViewModelMap, String formType, Feature feature, JSONObject geometry,
                                 boolean featureIsInAdd, ViewPager viewPager, CMEntity cmEntity,
                                 BottomSheetDialog bottomSheetDialog, Location location,
                                 JSONObject permissionJson, View buttonView, IAddEditFeatureView iAddEditFeatureView) {
            this.activity = activity;
            this.attachmentList = attachmentList;
            this.getSelectForDeleteAttachmentFileList = getSelectForDeleteAttachmentFileList;
            this.attributeViewModelMap = attributeViewModelMap;
            this.formType = formType;
            this.feature = feature;
            this.geometry = geometry;
            this.featureIsInAdd = featureIsInAdd;
            this.viewPager = viewPager;
            this.cmEntity = cmEntity;
            this.bottomSheetDialog = bottomSheetDialog;
            this.location = location;
            this.permissionJson = permissionJson;
            this.buttonView = buttonView;
            this.iAddEditFeatureView = iAddEditFeatureView;
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject resultJson = new JSONObject();
            try {
                resultJson.put("status", "failure");
                resultJson.put("message", "Something went wrong. Reason: unknown");

                attributeValueMap = new HashMap<>();
                JSONObject entityDomainValuesObj = cmEntity.getDomainValues();
                boolean isNoMandatoryAttributeRemain = true;

                Map<String, String> emptyMandatoryFields = new HashMap<>();
                Map<String, View> emptyMandatoryViewsList = new HashMap<>();


                boolean isFlowsApplicable = false;
                ArrayList<String> allowedAttributeNamesList = new ArrayList<>();
                try {
                    isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
                    JSONArray propArray = permissionJson.getJSONArray("propertiesArray");
                    for (int i = 0; i < propArray.length(); i++) {
                        allowedAttributeNamesList.add(propArray.getString(i));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isFlowsApplicable = false;
                    allowedAttributeNamesList = new ArrayList<>();
                }

                if (attributeViewModelMap != null) {

                    for (Integer index : attributeViewModelMap.keySet()) {
                        Attribute attributeViewModel = attributeViewModelMap.get(index);
                        View view = attributeViewModel.getView();
                        if (isFlowsApplicable) {
                            attributeViewModel.setEnable(allowedAttributeNamesList.contains(attributeViewModel.getName()) && attributeViewModel.isEnable());
                        }
                        Object finalValue = null;
                        String attributeValue = null;
                        boolean isEnteredProperly = false;


                        if (view instanceof TextInputLayout) {

                            EditText editText = ((TextInputLayout) view).getEditText();
                            if (editText != null) {
                                attributeValue = editText.getText().toString();
                            }

                            try {

                                if (attributeViewModel.isEnable()) {

                                    if (attributeViewModel.isMandatory()) {
                                        isEnteredProperly = attributeValue != null && !attributeValue.isEmpty();
                                    } else {
                                        isEnteredProperly = attributeValue != null;
                                    }
                                } else {
                                    if ((attributeValue == null || attributeValue.equalsIgnoreCase("null") || attributeValue.equalsIgnoreCase("na") || attributeValue.isEmpty())) {
                                        attributeValue = String.valueOf(attributeViewModel.getDefaultValue());

                                    }
                                    isEnteredProperly = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else if (view instanceof AutoCompleteTextView) {

                            AutoCompleteTextView autoCompleteTextView = ((AutoCompleteTextView) view);
                            CharSequence textValue = autoCompleteTextView.getText();
                            if (textValue != null) {
                                attributeValue = textValue.toString();
                            }

                            try {
                                if (attributeViewModel.isEnable()) {

                                    if (attributeViewModel.isMandatory()) {
                                        isEnteredProperly = attributeValue != null && !attributeValue.equalsIgnoreCase("na") && !attributeValue.isEmpty();
                                    } else {
                                        isEnteredProperly = attributeValue != null;
                                    }
                                } else {
                                    if ((attributeValue == null || attributeValue.equalsIgnoreCase("null") || attributeValue.equalsIgnoreCase("na") || attributeValue.isEmpty())) {
                                        attributeValue = String.valueOf(attributeViewModel.getDefaultValue());

                                    }

                                    isEnteredProperly = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (view instanceof RadioGroup) {

                            RadioGroup radioGroup = ((RadioGroup) view);
                            int buttonId = radioGroup.getCheckedRadioButtonId();
                            if (buttonId != -1) {
                                RadioButton valueButton = radioGroup.findViewById(buttonId);
                                String valueText = valueButton.getText().toString();
                                if (valueText.equalsIgnoreCase(activity.getResources().getString(R.string.dialog_yes))) {
                                    attributeValue = "1";
                                } else {
                                    attributeValue = "0";
                                }
                                isEnteredProperly = true;
                            } else {
                                isEnteredProperly = false;
                                attributeValue = null;
                            }

                        }

                        String type = attributeViewModel.getType();
                        try {
                            //if attribute has domain vals, set entered val to domain value, not the domain's label --saving to db
                            if (entityDomainValuesObj != null && attributeViewModel.getDomainName() != null && entityDomainValuesObj.has(attributeViewModel.getName())) {
                                JSONObject domainValJson = cmEntity.getDomainValues().getJSONObject(attributeViewModel.getName());
                                if (domainValJson.has("values")) {
                                    JSONArray valuesJArray = domainValJson.getJSONArray("values");
                                    for (int v = 0; v < valuesJArray.length(); v++) {
                                        if (attributeValue != null && !attributeValue.isEmpty() && (attributeValue.equalsIgnoreCase(valuesJArray.getJSONObject(v).getString("label")) || attributeValue.equalsIgnoreCase(valuesJArray.getJSONObject(v).getString("value")))) {
                                            isEnteredProperly = true;
                                            attributeValue = valuesJArray.getJSONObject(v).getString("value");//for saving, even if incoming value was a label/value, we save value
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (type.equalsIgnoreCase("date") || type.equalsIgnoreCase("timestamp")) {
                            try {
                                if (attributeValue != null) {
                                    if (!attributeValue.isEmpty()) {
                                        Date date = DatePickerMethods.convertStringDateToDate(attributeValue);
                                        if (date != null) {
                                            finalValue = DatePickerMethods.convertDateToStringDateTimeStamp(date);
                                        }
                                    } else {
                                        finalValue = attributeViewModel.getDefaultValue();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (type.equalsIgnoreCase("boolean")) {
                            try {
                                if (attributeValue != null) {
                                    if (!attributeValue.isEmpty()) {
                                        try {
                                            finalValue = Short.parseShort(attributeValue);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            finalValue = attributeValue;
                                        }
                                    } else {
                                        finalValue = attributeViewModel.getDefaultValue();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (type.equalsIgnoreCase("double")) {
                            try {
                                if (attributeValue != null) {
                                    if (!attributeValue.isEmpty()) {
                                        try {
                                            finalValue = Double.valueOf(attributeValue);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            finalValue = attributeValue;
                                        }
                                    } else {
                                        //finalValue =  Double.valueOf(attributeViewModel.getDefaultValue());
                                        finalValue = (attributeViewModel.getDefaultValue());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (type.equalsIgnoreCase("integer")) {
                            try {
                                if (attributeValue != null) {
                                    if (!attributeValue.isEmpty()) {
                                        try {
                                            finalValue = Integer.valueOf(attributeValue);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            finalValue = attributeValue;
                                        }
                                    } else {
                                        //finalValue =  Integer.valueOf(attributeViewModel.getDefaultValue());
                                        finalValue = (attributeViewModel.getDefaultValue());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (type.equalsIgnoreCase("string")) {
                            try {
                                if (attributeValue != null) {
                                    if (!attributeValue.isEmpty()) {
                                        finalValue = attributeValue;
                                    } else {
                                        finalValue = String.valueOf(attributeViewModel.getDefaultValue());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        String name = attributeViewModel.getName();
                        boolean isMandatory = attributeViewModel.isMandatory();
                        boolean isEnabled = attributeViewModel.isEnable();//and this value with flows
                        String alias = attributeViewModel.getLabel();
                        int groupIndex = attributeViewModel.getGroupNumber();
                        String groupLabel = attributeViewModel.getGroupLabel();

                        boolean isEmpty = false;

                        if (isFlowsApplicable) {
                            isEnabled = allowedAttributeNamesList.contains(name) && isEnabled;
                        }
                        if (attributeViewModel.isSystem()) {
                            isEnabled = false;
                            isEnteredProperly = true;
                        }


                        if (isMandatory && isEnabled) {

                            String errorMsg = "Enter " + alias;
                            if (finalValue == null || !isEnteredProperly) {

                                isNoMandatoryAttributeRemain = false;

                                isEmpty = true;

                                if (view instanceof TextInputLayout) {
                                    //((TextInputLayout) view).setError(errorMsg);
                                    isEmpty = true;
                                    emptyMandatoryViewsList.put(alias, view);
                                } else if (view instanceof AutoCompleteTextView) {
                                    // String autoCompleteError = "Select " + alias;
                                    // ((AutoCompleteTextView) view).setError(autoCompleteError);
                                    isEmpty = true;
                                    emptyMandatoryViewsList.put(alias, view);
                                } else if (view instanceof RadioGroup) {
                                    isEmpty = true;
                                }

                                // focusOnView(view);
                            } else {
                                attributeValueMap.put(name, finalValue);
                            }
                        } else {

                            if (view instanceof TextInputLayout) {
                                if (isEnteredProperly) {
                                    attributeValueMap.put(name, finalValue);
                                } else {
                                    isNoMandatoryAttributeRemain = false;
                                    // String textInputError = "Select " + alias;
                                    // ((TextInputLayout) view).setError(textInputError);
                                    isEmpty = true;
                                    // focusOnView(view);
                                    emptyMandatoryViewsList.put(alias, view);
                                }
                            } else if (view instanceof AutoCompleteTextView) {
                                if (isEnteredProperly) {
                                    attributeValueMap.put(name, finalValue);
                                } else {
                                    isNoMandatoryAttributeRemain = false;
                                    // String autoCompleteError = "Select " + alias;
                                    // ((AutoCompleteTextView) view).setError(autoCompleteError);
                                    isEmpty = true;
                                    // focusOnView(view);
                                    emptyMandatoryViewsList.put(alias, view);
                                }
                            } else {
                                attributeValueMap.put(name, finalValue);
                            }
                        }

                        if (isEmpty) {
                            String key = groupIndex + " " + groupLabel;
                            if (emptyMandatoryFields.containsKey(key)) {
                                String field = emptyMandatoryFields.get(key);
                                field += ", " + alias;
                                emptyMandatoryFields.put(key, field);
                            } else {
                                emptyMandatoryFields.put(key, alias);
                            }
                        }

                    }

                    if (isNoMandatoryAttributeRemain) {


                        attributeValueMap.put(AppConstants.W9_ENTITY_CLASS_NAME, cmEntity.getName());
                        collectSystemProperties(attributeValueMap, formType, feature, location);


                        featureTable = cmEntity.getFeatureTable();


                        columnNameW9IdProperty = cmEntity.getW9IdProperty();

                        enteredValuew9Id = String.valueOf(attributeValueMap.get(columnNameW9IdProperty));

                        columnNameLabelProperty = cmEntity.getLabelPropertyName();
                        featurelabelValue = "";
                        if (columnNameLabelProperty != null && !columnNameLabelProperty.isEmpty()) {
                            ReveloLogger.info(ClassName, taskName, "setting featurelabelvalue.. Entity.lableproperty = " + columnNameLabelProperty);
                            if (columnNameLabelProperty.contains("+")) {
                                String[] labelComponents = columnNameLabelProperty.split("\\+");

                                for (int i = 0; i < labelComponents.length; i++) {
                                    String component = labelComponents[i];
                                    if (component.contains("{")) {
                                        try {
                                            String columnName = component.replace("{", "").replace("}", "");
                                            if (attributeValueMap.containsKey(columnName)) {
                                                featurelabelValue += String.valueOf(attributeValueMap.get(columnName));
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (component.contains("'")) {
                                        try {
                                            String staticString = component.replace("'", "");
                                            featurelabelValue += staticString;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else if (columnNameLabelProperty.contains("{") || columnNameLabelProperty.contains("'")) {

                                if (columnNameLabelProperty.contains("{")) {
                                    try {
                                        String columnName = columnNameLabelProperty.replace("{", "").replace("}", "");
                                        if (attributeValueMap.containsKey(columnName)) {
                                            featurelabelValue += String.valueOf(attributeValueMap.get(columnName));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (columnNameLabelProperty.contains("'")) {
                                    try {
                                        String staticString = columnNameLabelProperty.replace("'", "");
                                        featurelabelValue += staticString;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            } else {
                                if (attributeValueMap.containsKey(columnNameLabelProperty)) {
                                    featurelabelValue = String.valueOf(attributeValueMap.get(cmEntity.getLabelPropertyName()));
                                }
                            }
                            ReveloLogger.error(ClassName, taskName, "setting featurelabelvalue = " + featurelabelValue);
                        } else {
                            ReveloLogger.error(ClassName, taskName, "No label property found. setting featurelabelValue = empty ");
                        }


                        if (featurelabelValue == null || featurelabelValue.isEmpty()) {
                            featurelabelValue = enteredValuew9Id;
                            ReveloLogger.error(ClassName, taskName, "featurelabelValue = empty, setting featurelabelValue =id i.e. " + enteredValuew9Id);
                        }
                        if (formType.equalsIgnoreCase(AppConstants.EDIT)) {
                            resultJson.put("status", "success");
                            resultJson.put("message", "");
                        } else {
                            boolean isDuplicateFeature = featureTable.doesFeatureExists(columnNameW9IdProperty, enteredValuew9Id, activity, true, false);
                            if (isDuplicateFeature) {
                                String errorMsg = " Feature by the " + columnNameLabelProperty + " " + featurelabelValue + "(" + enteredValuew9Id + ") already exists! Please try again with another " + columnNameLabelProperty;//Site feature by the name site123 already exists

                                resultJson.put("status", "failure");
                                resultJson.put("message", errorMsg);
                            } else {
                                resultJson.put("status", "success");
                                resultJson.put("message", "");
                            }
                        }
                    } else {
                        TreeMap<String, String> sorted = new TreeMap<>(emptyMandatoryFields);

                        String errorMsg = "";
                        for (String key : sorted.keySet()) {
                            errorMsg = errorMsg + "\n" + key + " : " + emptyMandatoryFields.get(key);
                        }

                        resultJson.put("status", "failure");
                        resultJson.put("message", errorMsg);
                        resultJson.put("views", emptyMandatoryViewsList);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultJson;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            iAddEditFeatureView.showProgressBar("Validating values... Please wait");
        }

        @Override
        protected void onPostExecute(JSONObject resultJsonObject) {
            super.onPostExecute(resultJsonObject);
            iAddEditFeatureView.hideProgressBar();
            if (resultJsonObject != null && resultJsonObject.has("status")) {
                try {
                    if (resultJsonObject.getString("status").equalsIgnoreCase("success")) {
                        confirmSaveFeature(activity, formType, feature, attributeValueMap, geometry, getSelectForDeleteAttachmentFileList, attachmentList, featureTable, cmEntity, bottomSheetDialog, (String) enteredValuew9Id, featurelabelValue, location, columnNameW9IdProperty, permissionJson, buttonView, iAddEditFeatureView);
                    } else {
                        if (resultJsonObject.has("message") && !resultJsonObject.getString("message").isEmpty()) {
                            iAddEditFeatureView.error(resultJsonObject.getString("message"), true);
                        } else {
                            iAddEditFeatureView.error("Something went wrong... Please retry", true);
                        }
                        if (resultJsonObject.has("views")) {
                            Map<String, View> emptyMandatoryViewsList = (HashMap<String, View>) resultJsonObject.get("views");
                            if (emptyMandatoryViewsList != null && emptyMandatoryViewsList.size() > 0) {
                                for (String alias : emptyMandatoryViewsList.keySet()) {
                                    View view = emptyMandatoryViewsList.get(alias);
                                    if (view instanceof TextInputLayout) {
                                        String errorMsg = "Enter " + alias;
                                        ((TextInputLayout) view).setError(errorMsg);
                                    } else if (view instanceof AutoCompleteTextView) {
                                        String autoCompleteError = "Select " + alias;
                                        ((AutoCompleteTextView) view).setError(autoCompleteError);
                                    } else if (view instanceof RadioGroup) {
                                    }
                                    focusOnView(view);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    iAddEditFeatureView.error("Something went wrong... Please retry", true);
                }

            } else {
                iAddEditFeatureView.error("Something went wrong... Please retry", true);
            }
        }
    }

    private class SaveFeatureTask extends AsyncTask<String, String, JSONObject> {

        String formType;
        CMEntity cmEntity;
        Feature feature;
        Map<String, Object> attributeValueMap;
        JSONObject geometry;
        List<Attachment> getSelectForDeleteAttachmentFileList;
        List<Attachment> attachmentList;
        FeatureTable featureTable;
        BottomSheetDialog bottomSheetDialog;
        String w9Id;
        String featureLabel;
        Location location;
        String columnName;
        JSONObject permissionJson;
        String submitStatus;
        IAddEditFeatureView iAddEditFeatureView;
        FeatureLayer featureLayer;

        public SaveFeatureTask(String formType, CMEntity cmEntity, Feature feature, Map<String, Object> attributeValueMap,
                               JSONObject geometry,
                               List<Attachment> getSelectForDeleteAttachmentFileList,
                               List<Attachment> attachmentList,
                               FeatureTable featureTable, BottomSheetDialog bottomSheetDialog,
                               String w9Id, String featureLabel,
                               Location location, String columnName, JSONObject permissionJson,
                               String submitStatus, IAddEditFeatureView iAddEditFeatureView) {
            this.formType = formType;
            this.cmEntity = cmEntity;
            this.feature = feature;
            this.attributeValueMap = attributeValueMap;
            this.geometry = geometry;
            this.getSelectForDeleteAttachmentFileList = getSelectForDeleteAttachmentFileList;
            this.attachmentList = attachmentList;
            this.featureTable = featureTable;
            this.bottomSheetDialog = bottomSheetDialog;
            this.w9Id = w9Id;
            this.featureLabel = featureLabel;
            this.location = location;
            this.columnName = columnName;
            this.permissionJson = permissionJson;
            this.submitStatus = submitStatus;
            this.iAddEditFeatureView = iAddEditFeatureView;
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject resultJson = new JSONObject();
            try {
                resultJson.put("status", "failure");
                resultJson.put("message", "Something went wrong. Reason: unknown");

                if (cmEntity != null) {
                    featureLayer = cmEntity.getFeatureLayer();
                }


               /* //check if values entered match with flow - if flow is applicable
               try{
                   boolean isFlowsApplicable = false;


                       if (permissionJson != null && permissionJson.has("isFlowApplicable")) {
                           isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
                       }
                  fgh
               }catch (Exception e){
                   e.printStackTrace();
               }*/


                if (formType.equalsIgnoreCase(AppConstants.EDIT)) {
                    resultJson = update(activity, attributeValueMap, attachmentList, featureTable, geometry, bottomSheetDialog, w9Id, featureLabel, location, columnName, getSelectForDeleteAttachmentFileList, permissionJson, submitStatus);
                } else {
                    resultJson = add(activity, formType, cmEntity, attributeValueMap, attachmentList, featureTable, geometry, bottomSheetDialog,
                            w9Id, featureLabel, location, permissionJson, submitStatus);
                }

            } catch (Exception e) {
                e.printStackTrace();

                String errorMessage = e.getMessage();
                try {
                    resultJson.put("status", "failure");
                    resultJson.put("message", "Something went wrong. Reason: " + errorMessage);

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            return resultJson;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            iAddEditFeatureView.showProgressBar("Saving... Please wait.");
        }

        @Override
        protected void onPostExecute(JSONObject resultJson) {
            super.onPostExecute(resultJson);
            iAddEditFeatureView.hideProgressBar();
            String message = "Operation Failed";
            if (formType.equalsIgnoreCase(AppConstants.EDIT)) {
                message = "Unable to edit. Reason : Unknown";
            } else {
                message = "Unable to add. Reason : Unknown";
            }
            if (resultJson != null && resultJson.has("status")) {
                try {
                    if (resultJson.getString("status").equalsIgnoreCase("success")) {
                        if (resultJson.has("message") && !resultJson.getString("message").isEmpty()) {
                            message = resultJson.getString("message");
                        }
                        try {
//                            ((IAddFeatureToDb) activity).updateFeatureResponse(activity, message, featureLayer, geometry, w9Id, feature);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtility.toast(message, activity, true);
                        }
                        if (bottomSheetDialog != null) {
                            bottomSheetDialog.dismiss();
                        }
                    } else {
                        if (resultJson.has("message") && !resultJson.getString("message").isEmpty()) {
                            message = resultJson.getString("message");
                            iAddEditFeatureView.error(message, true);

                        } else {
                            iAddEditFeatureView.error(message, true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    iAddEditFeatureView.error("Something went wrong. Reason: " + e.getMessage() + "... Please try again.", true);
                }
            } else {
                iAddEditFeatureView.error("Something went wrong. Please try again.", true);
            }
        }
    }

    private class CreateFormView extends AsyncTask<Void, String, LinkedHashMap<String, Attribute>> {

        private final WeakReference<Activity> activityWeakReference;
        private final String formType;
        private final IAddEditFeatureView iAddFeatureView;
        // private FeatureLayer featureLayer;
        private final CMEntity cmEntity;
        private final Feature feature;
        private final boolean featureIsInAdd = false;
        private final String pattern = "[_a-zA-Z0-9]+";
        private final Map<String, String> jurisdictionNamesIdMap;
        private final Map<String, Object> jurisdictionValuesMap;
        private final Location location;
        private final JSONObject geometryGeoJson;
        private final String measurementString;
        private final double measurementValue;
        private ProgressDialog pDialog;
        private String errorMsg;
        private JSONObject entityDomainValuesObj;
        private JSONGraph dependantPropGraph;
        private LinkedHashMap<String, Attribute> attributeViewModelMap;
        private Map<String, Object> editFeatureAttribute;
        private Map<Integer, PropertyGroupsModel> propertyGroupsModelMap;
        private String w9IdPropertyName;
        private boolean isFlowsApplicable = false;
        private List<String> allowedAttributeNamesList = new ArrayList<>();
        private List<String> otherAttributeNamesList = new ArrayList<>();
        private Geometry jtsGeom = null;

        private CreateFormView(Activity activity, IAddEditFeatureView iAddFeatureView, String formType, FeatureLayer featureLayer, CMEntity cmEntity, Feature feature, JSONObject geometryGeoJson, String measurementString, double measurementValue, Map<String, String> jurisdictionNamesIdMap, Map<String, Object> jurisdictionValuesMap, Location location, JSONObject permissionJson) {

            activityWeakReference = new WeakReference<>(activity);
            this.formType = formType;
            this.iAddFeatureView = iAddFeatureView;
            // this.featureLayer = featureLayer;
            this.feature = feature;
            this.jurisdictionNamesIdMap = jurisdictionNamesIdMap;
            this.jurisdictionValuesMap = jurisdictionValuesMap;
            this.location = location;
            this.cmEntity = cmEntity;
            this.geometryGeoJson = geometryGeoJson;
            this.measurementString = measurementString;
            this.measurementValue = measurementValue;
            setJtsGeom(geometryGeoJson);
            setPermissionJson(permissionJson);
        }

        public void setJtsGeom(JSONObject geometryGeoJson) {
            if (geometryGeoJson == null) {
                if (cmEntity.getType().equalsIgnoreCase("spatial") && feature != null) {
                    JSONObject featureGeoJsonGeometry = feature.getGeoJsonGeometry();
                    this.jtsGeom = GeoJsonUtils.convertToJTSGeometry(featureGeoJsonGeometry);
                } else {
                    this.jtsGeom = null;
                }
            } else {
                try {
                    this.jtsGeom = GeoJsonUtils.convertToJTSGeometry(geometryGeoJson);
                } catch (Exception e) {
                    e.printStackTrace();
                    jtsGeom = null;
                }
            }
        }

        private void setPermissionJson(JSONObject permissionJson) {
            try {
                isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
                JSONArray propArray = permissionJson.getJSONArray("propertiesArray");
                for (int i = 0; i < propArray.length(); i++) {
                    allowedAttributeNamesList.add(propArray.getString(i));
                }
                for (Attribute attribute : cmEntity.getProperties()) {
                    if (!allowedAttributeNamesList.contains(attribute.getName())
                            && !jurisdictionNamesIdMap.containsKey(attribute.getName())) {
                        otherAttributeNamesList.add(attribute.getName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                isFlowsApplicable = false;
                allowedAttributeNamesList = new ArrayList<>();
                otherAttributeNamesList = new ArrayList<>();
            }
        }

        @Override
        protected LinkedHashMap<String, Attribute> doInBackground(Void... params) {

            attributeViewModelMap = new LinkedHashMap<>();
            try {

                w9IdPropertyName = cmEntity.getW9IdProperty();
                entityDomainValuesObj = cmEntity.getDomainValues();
                dependantPropGraph = cmEntity.getDependantPropertiesJGraph();
                if (isFlowsApplicable) {
                    propertyGroupsModelMap = new HashMap<>();
                    PropertyGroupsModel propertyGroupsModel_enabled = new PropertyGroupsModel();
                    propertyGroupsModel_enabled.setLabel("Main Page");
                    propertyGroupsModel_enabled.setIndex(1);
                    propertyGroupsModel_enabled.setName("Main Page");
                    propertyGroupsModel_enabled.setPropertyNames(allowedAttributeNamesList);
                    propertyGroupsModelMap.put(1, propertyGroupsModel_enabled);

                    PropertyGroupsModel propertyGroupsModel_others = new PropertyGroupsModel();
                    propertyGroupsModel_others.setLabel("Not Assigned");
                    propertyGroupsModel_others.setIndex(2);
                    propertyGroupsModel_others.setName("Not Assigned");
                    propertyGroupsModel_others.setPropertyNames(otherAttributeNamesList);
                    propertyGroupsModelMap.put(2, propertyGroupsModel_others);

                } else {
                    //separate hierarchy values to make a separate group
                    Map<Integer, PropertyGroupsModel> entityPropertyGroups = cmEntity.getPropertyGroups();

                    if (entityPropertyGroups != null && jurisdictionValuesMap != null && jurisdictionNamesIdMap != null) {
                        propertyGroupsModelMap = new HashMap<>();
                        List<String> jurisdictionKeysList = new ArrayList<>(jurisdictionNamesIdMap.keySet());

                        for (Integer index : entityPropertyGroups.keySet()) {
                            PropertyGroupsModel model = entityPropertyGroups.get(index);
                            List<String> modelpropertyKeysList = model.getPropertyNames();
                            List<String> nonjurisdictionKeysList = new ArrayList<>();
                            for (String property : modelpropertyKeysList) {
                                if (!jurisdictionKeysList.contains(property)) {
                                    nonjurisdictionKeysList.add(property);
                                }
                            }
                            if (nonjurisdictionKeysList.size() > 0) {
                                PropertyGroupsModel propertyGroupsModel_NonJurisdiction = new PropertyGroupsModel();
                                propertyGroupsModel_NonJurisdiction.setLabel(model.getLabel());
                                propertyGroupsModel_NonJurisdiction.setIndex(model.getIndex());
                                propertyGroupsModel_NonJurisdiction.setName(model.getName());
                                propertyGroupsModel_NonJurisdiction.setPropertyNames(nonjurisdictionKeysList);
                                propertyGroupsModelMap.put(model.getIndex(), propertyGroupsModel_NonJurisdiction);
                            }
                        }
                    } else {
                        propertyGroupsModelMap = entityPropertyGroups;
                    }
                }

                if (jurisdictionNamesIdMap != null && jurisdictionValuesMap != null) {
                    if (propertyGroupsModelMap == null) {
                        propertyGroupsModelMap = new HashMap<>();
                    }
                    List<String> jurisdictionKeysList = new ArrayList<>(jurisdictionNamesIdMap.keySet());
                    PropertyGroupsModel propertyGroupsModel_jurisdiction = new PropertyGroupsModel();
                    propertyGroupsModel_jurisdiction.setLabel("Jurisdictions");
                    propertyGroupsModel_jurisdiction.setIndex(2);
                    propertyGroupsModel_jurisdiction.setName("Jurisdictions");
                    propertyGroupsModel_jurisdiction.setPropertyNames(jurisdictionKeysList);


                    //insert it at position 2
                    HashMap<Integer, PropertyGroupsModel> propertyGroupsModelMapNew = new HashMap<>();
                    if (propertyGroupsModelMap.size() < 2) {
                        for (Integer index : propertyGroupsModelMap.keySet()) {
                            if (index < 2) {
                                propertyGroupsModelMapNew.put(index, propertyGroupsModelMap.get(index));
                            }
                        }
                        propertyGroupsModelMapNew.put(2, propertyGroupsModel_jurisdiction);
                        propertyGroupsModelMap = propertyGroupsModelMapNew;
                    } else {
                        for (Integer index : propertyGroupsModelMap.keySet()) {
                            if (index < 2) {
                                propertyGroupsModelMapNew.put(index, propertyGroupsModelMap.get(index));
                            } else if (index == 2) {
                                propertyGroupsModelMapNew.put(2, propertyGroupsModel_jurisdiction);
                                PropertyGroupsModel modelAtIndex2 = propertyGroupsModelMap.get(index);
                                modelAtIndex2.setIndex(3);
                                propertyGroupsModelMapNew.put(3, modelAtIndex2);
                            } else {
                                PropertyGroupsModel modelAtIndex = propertyGroupsModelMap.get(index);
                                modelAtIndex.setIndex(index + 1);
                                propertyGroupsModelMapNew.put(index + 1, modelAtIndex);
                            }
                        }
                        propertyGroupsModelMap = propertyGroupsModelMapNew;
                    }
                }

                ArrayList<String> dependantPropertiesAttrNames = new ArrayList<>();

                if (dependantPropGraph != null) {
                    HashMap<String, Object> filterPropMap = new HashMap<>();
                    filterPropMap.put("nodeType", "depProp");
                    List<JSONObject> depPropNodeList = dependantPropGraph.getVertices(filterPropMap);
                    if (depPropNodeList != null) {
                        for (JSONObject depPropNode : depPropNodeList) {
                            dependantPropertiesAttrNames.add(depPropNode.getString("name"));
                        }
                    }
                }

                Activity activity = activityWeakReference.get();
                if (feature != null) {
                    editFeatureAttribute = feature.getAttributes();
                }

                if (cmEntity != null) {

                    String fromId = "";
                    String toId = "";

                    JSONObject graphResult = CMUtils.getCMGraph(activity);
                    if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                        CMGraph cmGraph = (CMGraph) graphResult.get("result");
                        String parentEntityName = "";

                        if (formType == AppConstants.EDIT || formType == AppConstants.SHADOW) {//in case of edit or shadow, feature= current feature.
                        } else {//in case of add, feature=parent feature.
                            if (feature != null) {
                                parentEntityName = feature.getEntityName();
                            }
                        }


                        JSONObject currentEntityResult = cmGraph.getVertex("name", cmEntity.getName());
                        if (currentEntityResult.has("status") && currentEntityResult.getString("status").equalsIgnoreCase("success")) {
                            CMEntity currentEntity = (CMEntity) currentEntityResult.get("result");
                            CMEntity parentEntity = null;
                            if (currentEntity != null) {

                                if (!parentEntityName.isEmpty() && formType.equalsIgnoreCase(AppConstants.ADD)) {
                                    JSONObject parentEntityResult = cmGraph.getVertex("name", parentEntityName);
                                    if (parentEntityResult.has("status") && parentEntityResult.getString("status").equalsIgnoreCase("success")) {
                                        parentEntity = (CMEntity) parentEntityResult.get("result");
                                    }
                                } else if (formType.equalsIgnoreCase(AppConstants.EDIT) || formType == AppConstants.SHADOW) {
                                    parentEntity = cmGraph.getParent(currentEntity);
                                }
                            }

                            if (currentEntity != null && parentEntity != null && !currentEntity.equals(parentEntity)) {
                                CMEdge edge = cmGraph.getEdgeBetween(parentEntity, currentEntity);
                                if (edge != null) {
                                    fromId = edge.getFromParameterName();
                                    toId = edge.getToParameterName();
                                }
                            }
                        }

                    }


                    List<Attribute> attributeList = cmEntity.getProperties();

                    if (attributeList != null) {

                        Comparator<Attribute> sortByAllowed = new Comparator<Attribute>() {
                            @Override
                            public int compare(Attribute attribute, Attribute t1) {
                                if (allowedAttributeNamesList.contains(attribute.getName()) &&
                                        !allowedAttributeNamesList.contains(t1.getName()))
                                    return -1;
                                else if (!allowedAttributeNamesList.contains(attribute.getName()) &&
                                        allowedAttributeNamesList.contains(t1.getName()))
                                    return 1;
                                else
                                    return 0;
                            }
                        };
                        Collections.sort(attributeList, sortByAllowed);

                        for (int i = 0; i < attributeList.size(); i++) {

                            Attribute attributeViewModel = attributeList.get(i);

                            if (attributeViewModel != null) {

                                String attributeName = attributeViewModel.getName();
                                attributeViewModel.setView(null);


                                attributeViewModel.setDependant(dependantPropertiesAttrNames.contains(attributeName));

                                boolean includeAttributeViewModel = true;
                                if (attributeName.contains("w9")) {
                                    includeAttributeViewModel = attributeName.equalsIgnoreCase("w9area");
                                } else {
                                    includeAttributeViewModel = true;
                                }
                                if (includeAttributeViewModel) {
                                    boolean isEnable = attributeViewModel.isEnable();
                                    //if (isEnable) {

                                    if (!TextUtils.isEmpty(attributeName)) {
                                        attributeName = attributeName.trim();
                                    }

                                    String type = "";
                                    if (!TextUtils.isEmpty(attributeViewModel.getType())) {
                                        type = attributeViewModel.getType().trim();
                                    }

                                    String alias = "";
                                    if (!TextUtils.isEmpty(attributeViewModel.getLabel())) {
                                        alias = attributeViewModel.getLabel().trim();
                                    }

                                    Object defaultValue = "";
                                    if (!TextUtils.isEmpty(String.valueOf(attributeViewModel.getDefaultValue()))) {
                                        defaultValue = attributeViewModel.getDefaultValue();
                                    }

                                    boolean isMandatory = attributeViewModel.isMandatory();
                                    boolean isEditable = true;

                                    List<String> domainDataList = new ArrayList<>();


                                    if (entityDomainValuesObj != null && attributeViewModel.getDomainName() != null && entityDomainValuesObj.has(attributeViewModel.getName())) {
                                        JSONObject domainValJson = cmEntity.getDomainValues().getJSONObject(attributeViewModel.getName());
                                        if (domainValJson.has("values")) {
                                            JSONArray valuesJArray = domainValJson.getJSONArray("values");
                                            for (int v = 0; v < valuesJArray.length(); v++) {
                                                domainDataList.add(valuesJArray.getJSONObject(v).getString("label"));
                                            }
                                        }
                                    }
                                    String value = "";
                                    if (formType.equalsIgnoreCase(AppConstants.EDIT) || formType.equalsIgnoreCase(AppConstants.SHADOW)) {
                                        if (editFeatureAttribute != null) {
                                            if (editFeatureAttribute.containsKey(attributeName)) {
                                                Object valueObject = editFeatureAttribute.get(attributeName);
                                                if (valueObject != null) {
//                                                        value = DatePickerMethods.getValidDateTimeStamp(valueObject);
                                                    value = String.valueOf(valueObject);
                                                    //if attribute has domain vals, set entered val to domain label--displaying the attribute
                                                    if (entityDomainValuesObj != null && entityDomainValuesObj.has(attributeViewModel.getName())) {
                                                        JSONObject domainValJson = entityDomainValuesObj.getJSONObject(attributeViewModel.getName());
                                                        if (domainValJson.has("values")) {
                                                            JSONArray valuesJArray = domainValJson.getJSONArray("values");
                                                            for (int v = 0; v < valuesJArray.length(); v++) {
                                                                if (value != null && !value.isEmpty() && (value.equalsIgnoreCase(valuesJArray.getJSONObject(v).getString("label")) || value.equals(valuesJArray.getJSONObject(v).getString("value")))) {
                                                                    value = valuesJArray.getJSONObject(v).getString("label");//for displaying, even if incoming value was a label/value, we show label
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }

                                                } else {
                                                    value = "";
                                                }
                                            } else {
                                                value = "";
                                            }
                                        }
                                    }

                                    if (jurisdictionNamesIdMap != null && jurisdictionNamesIdMap.containsKey(attributeName)) {
                                        isEditable = false;
                                        if (jurisdictionValuesMap != null) {
                                            String idName = jurisdictionNamesIdMap.get(attributeName);
                                            if (jurisdictionValuesMap.containsKey(idName)) {
                                                value = String.valueOf(jurisdictionValuesMap.get(idName));
                                            }
                                        }
                                    }

                                    if (attributeName.equalsIgnoreCase(toId)) {
                                        if (editFeatureAttribute != null) {
                                            //if (formType.equalsIgnoreCase(AppConstants.ADD)) {
                                            if (editFeatureAttribute.containsKey(fromId)) {
                                                value = String.valueOf(editFeatureAttribute.get(fromId));
                                                isEditable = false;
                                            }
                                            // }
                                        }
                                    }

                                    if (isFlowsApplicable) {
                                        if (allowedAttributeNamesList.contains(attributeName) && attributeViewModel.isEnable()) {
                                            isEditable = true;
                                        } else {
                                            isEditable = false;
                                            isMandatory = false;
                                        }
                                    }
                                    if (attributeName.equalsIgnoreCase(w9IdPropertyName)) {
                                        isEditable = false;
                                        if (formType.equalsIgnoreCase(AppConstants.ADD)) {
                                            value = generateAutomaticId(type);
                                        } else {
                                            //do nothing
                                        }
                                    }
                                    if (attributeName.equalsIgnoreCase("w9area")) {
                                        isEditable = false;
                                        if (measurementString != null && !measurementString.isEmpty()) {
                                            value = String.valueOf(measurementValue);
                                        } else {
                                            value = generateAreaValue();
                                        }
                                    }
                                    if (attributeName.equalsIgnoreCase(toId) && (formType.equalsIgnoreCase(AppConstants.EDIT) || formType.equalsIgnoreCase(AppConstants.SHADOW))) {
                                        isEditable = false;
                                    }


                                    if (!domainDataList.isEmpty()) {

                                        View v = createAutoCompleteTextView(activity, attributeName, alias, type, isMandatory,
                                                isEditable, value, domainDataList);
                                        attributeViewModel.setView(v);
                                        // attributeViewModelMap.put(attributeName, attributeViewModel);

                                    } else if (type.equalsIgnoreCase("boolean")) {
                                        View v = createRadioGroup(attributeName, activity, value, isMandatory);
                                        attributeViewModel.setView(v);
                                        //attributeViewModelMap.put(attributeName, attributeViewModel);

                                    } else {
                                        View v = createEditTextView(activity, attributeName, alias, type, isMandatory, isEditable, value);
                                        attributeViewModel.setView(v);
                                        // attributeViewModelMap.put(attributeName, attributeViewModel);
                                    }
                                    //}
                                }

                                attributeViewModelMap.put(attributeName, attributeViewModel);
                            }
                        }
                    } else {
                        errorMsg = activity.getResources().getString(R.string.error_selection_layer_fields_not_found);
                    }
                } else {
                    errorMsg = activity.getResources().getString(R.string.error_selection_layer_name_not_found);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return attributeViewModelMap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(activityWeakReference.get());
            pDialog.setMessage(activityWeakReference.get().getResources().getString(R.string.dialog_message_creating_form));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(LinkedHashMap<String, Attribute> hashMap) {

            if (hashMap == null || hashMap.size() == 0) {
                if (errorMsg == null || errorMsg.isEmpty()) {
                    errorMsg = activityWeakReference.get().getResources().getString(R.string.error_try_sometime);
                }
                iAddFeatureView.error(errorMsg, false);
            } else {
                iAddFeatureView.createdView(hashMap, propertyGroupsModelMap, featureIsInAdd, entityDomainValuesObj);
            }

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }

        private String generateAutomaticId(String type) {

            String w9Id = "";
            /*if (type.equalsIgnoreCase("integer")) {
                w9Id = RandomStringUtils.randomNumeric(8);

            } else if (type.equalsIgnoreCase("string")) {
                w9Id = RandomStringUtils.randomAlphanumeric(8);
            }

            Date dNow = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("hhmmss", Locale.getDefault());
            String datetime = ft.format(dNow);
            w9Id = w9Id + "_" + datetime;*/

            w9Id = UUID.randomUUID().toString()/*.replace("-","")*/;
            return w9Id;
        }

        private String generateAreaValue() {
            String value = "0.0";

            if (jtsGeom != null && cmEntity.getType().equalsIgnoreCase("spatial")) {

                if (cmEntity.getGeometryType().equalsIgnoreCase("multipolygon") || cmEntity.getGeometryType().equalsIgnoreCase("polygon")) {
                    double area = jtsGeom.getArea();
                    if (area > 0) {
                        area = area / 10000;//hectares
                        value = String.valueOf(area);
                    }
                } else if (cmEntity.getGeometryType().equalsIgnoreCase("multilinestring") || cmEntity.getGeometryType().equalsIgnoreCase("linestring") || cmEntity.getGeometryType().equalsIgnoreCase("polyline")) {
                    double length = jtsGeom.getLength();//meters
                    if (length > 0) {
                        value = String.valueOf(length);
                    }
                }

            }
            return value;
        }

        private TextInputLayout createAutoCompleteTextView(Activity activity, String name, String alias, String type, boolean isMandatory,
                                                           boolean isEditable, String value, List<String> domainDataList) {

            final TextInputLayout textInputLayout = new TextInputLayout(new ContextThemeWrapper(activity, R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox));

            LinearLayout.LayoutParams textInputParam = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

            textInputLayout.setLayoutParams(textInputParam);

            if (isMandatory) {
                textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                textInputLayout.setHint(alias + "*");
                textInputLayout.setHintTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.color_red)));
            } else {
                if (isEditable) {
                    textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorPrimaryDark)));
                    textInputLayout.setHintTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorPrimaryDark)));
                } else {
                    textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorGrey)));
                    textInputLayout.setHintTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorGrey)));
                }
                textInputLayout.setHint(alias);
            }

            textInputLayout.setPadding(0, 10, 10, 10);

            final AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(activity);
            autoCompleteTextView.setFocusable(true);
            autoCompleteTextView.setThreshold(0);
            autoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop, 0);
            autoCompleteTextView.setTextSize(18);


            try {

                Collections.sort(domainDataList);

                ArrayAdapter<String> autoArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, domainDataList);

                activity.runOnUiThread(() -> autoCompleteTextView.setAdapter(autoArrayAdapter));

                autoCompleteTextView.setMaxLines(1);

                if (type.equalsIgnoreCase("integer")) {
                    autoCompleteTextView.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else if (type.equalsIgnoreCase("double")) {
                    autoCompleteTextView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    autoCompleteTextView.setInputType(InputType.TYPE_CLASS_TEXT);
                }

                if (domainDataList != null && domainDataList.size() > 0) {
                    autoCompleteTextView.setInputType(0);//disable the editing
                    autoCompleteTextView.setThreshold(999);
                }
                autoCompleteTextView.setImeOptions(EditorInfo.IME_ACTION_DONE);

                if (!isEditable) {
                    autoCompleteTextView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    autoCompleteTextView.setFocusable(false);
                    autoCompleteTextView.setEnabled(false);
                }

                autoCompleteTextView.setOnClickListener(v -> autoCompleteTextView.showDropDown());

                autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {


                    if (hasFocus) {
                        autoCompleteTextView.showDropDown();
                    } else {
                        String autoComValue = autoCompleteTextView.getText().toString();
                        if (!TextUtils.isEmpty(autoComValue)) {
                            boolean isContain = domainDataList.contains(autoComValue);
                            if (!isContain) {
                                textInputLayout.setError("Select proper value.");
                            }
                        }
                    }

                });

                autoCompleteTextView.setOnItemClickListener((adapterView, v, position, id) -> {
                    try {

                        String selectedItemValue = adapterView.getItemAtPosition(position).toString();

                        Object tag = autoCompleteTextView.getTag();
                        if (tag instanceof AttributeTagModel) {
                            AttributeTagModel attributeTag = (AttributeTagModel) tag;
                            attributeTag.setValue(selectedItemValue);

                            autoCompleteTextView.setTag(attributeTag);
                            textInputLayout.setTag(attributeTag);
                        }

                        autocompleteClickListener(activity, selectedItemValue, autoCompleteTextView);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        if (domainDataList != null && domainDataList.size() > 0) {
                            textInputLayout.setError(null);
                        } else {
                            textInputLayout.setError(null);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        if (domainDataList != null && domainDataList.size() > 0) {
                        } else {
                            Object tagObject = autoCompleteTextView.getTag();
                            Log.e("TAG", "" + tagObject);

                            if (tagObject != null) {
                                if (tagObject instanceof AttributeTagModel) {
                                    AttributeTagModel attributeTagModel = (AttributeTagModel) tagObject;
                                    String tagName = attributeTagModel.getName();

                                    Log.e("TAG", "Remove : " + tagName);
                                    iAddFeatureView.removeLayout(activity, tagName);

                                    autoCompleteTextView.setTag(null);
                                }
                            }
                        }
                    }
                });

                textInputLayout.addView(autoCompleteTextView);

                if (!TextUtils.isEmpty(value)) {
                    if (formType.equalsIgnoreCase(AppConstants.EDIT) || formType.equalsIgnoreCase(AppConstants.SHADOW)) {
                        String finalValue = value;
                        activity.runOnUiThread(() -> autoCompleteTextView.setText(finalValue));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            AttributeTagModel attributeTagModel = new AttributeTagModel();
            attributeTagModel.setName(name);
            attributeTagModel.setValue(value);
            attributeTagModel.setView(autoCompleteTextView);

            autoCompleteTextView.setTag(attributeTagModel);
            textInputLayout.setTag(attributeTagModel);

            return textInputLayout;
        }

        private RadioGroup createRadioGroup(String name, Activity activity, String value, boolean isMandatory) {

            RadioButton yesButton = new RadioButton(activity);
            yesButton.setText(activity.getResources().getString(R.string.dialog_yes));
            yesButton.setTextSize(18);
            yesButton.setTextColor(Color.BLACK);

            RadioButton noButton = new RadioButton(activity);
            noButton.setTextSize(18);
            noButton.setTextColor(Color.BLACK);
            noButton.setText(activity.getResources().getString(R.string.dialog_no));

            RadioGroup radioGroup = new RadioGroup(activity);
            //radioGroup.setTag(name);
            radioGroup.setOrientation(RadioGroup.HORIZONTAL);
            radioGroup.setPadding(0, 5, 10, 10);

            radioGroup.addView(yesButton, 0);
            radioGroup.addView(noButton, 1);

            activity.runOnUiThread(() -> {
                if (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("true")) {
                    yesButton.setChecked(true);
                } else {
                    noButton.setChecked(true);
                }
            });

            AttributeTagModel attributeTagModel = new AttributeTagModel();
            attributeTagModel.setName(name);
            attributeTagModel.setValue(value);
            attributeTagModel.setView(radioGroup);

            radioGroup.setTag(attributeTagModel);
            return radioGroup;
        }

        private TextInputLayout createEditTextView(Activity activity, String name, String alias, String type, boolean isMandatory, boolean isEditable, String value) {

            TextInputLayout textInputLayout = new TextInputLayout(new ContextThemeWrapper(activity,
                    R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox));

            try {

                LinearLayout.LayoutParams textInputParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                textInputLayout.setLayoutParams(textInputParam);
                textInputLayout.setPadding(0, 10, 10, 10);

                final TextInputEditText editText = new TextInputEditText(activity);
                editText.setTextSize(18);

                if (isMandatory) {
                    textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(Color.RED));
                    textInputLayout.setHint(alias + "*");
                } else {
                    if (isEditable) {
                        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorPrimaryDark)));
                    } else {
                        textInputLayout.setDefaultHintTextColor(ColorStateList.valueOf(activity.getResources().getColor(R.color.colorGrey)));
                    }
                    textInputLayout.setHint(alias);
                }

                editText.setMaxLines(1);

                if (type.equalsIgnoreCase("integer")) {

                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});

                } else if (type.equalsIgnoreCase("double")) {

                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(14)});

                } else if (type.equalsIgnoreCase("date")) {

                    editText.setInputType(InputType.TYPE_NULL);
                    editText.setFocusable(false);

                    String dateString = DatePickerMethods.convertToValidDateString(value, false);

                    if (!TextUtils.isEmpty(dateString)) {
                        value = dateString;
                    }

                    editText.setOnClickListener(v -> {
                        DatePickerMethods.datePicker(activity, null, null, editText::setText);
                    });

                } else if (type.equalsIgnoreCase("string") || type.equalsIgnoreCase("text")) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setFilters(getInputFilter());
                }

                if (!TextUtils.isEmpty(value)) {
                    editText.setText(value);
                    if (type.equalsIgnoreCase("string") || type.equalsIgnoreCase("text")) {
                        editText.setSelection(value.length());
                    }
                }

                if (!isEditable) {
                    editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    editText.setFocusable(false);
                    editText.setEnabled(false);
                }

                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        textInputLayout.setError(null);
                        if (type.equalsIgnoreCase("string") || type.equalsIgnoreCase("text")) {
                            if (s != null && s.length() > 20) {
                                editText.setSingleLine(false);
                                editText.setSelection(s.length());
                            } else {
                                editText.setSingleLine(true);
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });


                if (name.equalsIgnoreCase(w9IdPropertyName) && isEditable) {
                    RegexInputFilter regexInputFilter = new RegexInputFilter(pattern, editText);
                    editText.setFilters(new InputFilter[]{regexInputFilter});
                    if (type.equalsIgnoreCase("integer") || type.equalsIgnoreCase("double")) {
                        regexInputFilter.setLength(9);
                    }
                }

                textInputLayout.addView(editText);
            } catch (Exception e) {
                e.printStackTrace();
            }

            AttributeTagModel attributeTagModel = new AttributeTagModel();
            attributeTagModel.setName(name);
            attributeTagModel.setValue(value);
            attributeTagModel.setView(textInputLayout);

            textInputLayout.setTag(attributeTagModel);

            return textInputLayout;
        }

        private void autocompleteClickListener(Activity activity, String selectedItemValue,
                                               AutoCompleteTextView autoCompleteTextView) {
            try {

                AppMethods.closeKeyboard(autoCompleteTextView, activity);
                autoCompleteTextView.setError(null);

                AttributeTagModel attributeTagModel = null;
                String tagName = "";
                Object tagObject = autoCompleteTextView.getTag();
                if (tagObject != null) {
                    if (tagObject instanceof AttributeTagModel) {
                        attributeTagModel = (AttributeTagModel) tagObject;
                        tagName = attributeTagModel.getName();
                    }
                }

                iAddFeatureView.removeLayout(activity, tagName);

                if (entityDomainValuesObj != null && entityDomainValuesObj.length() != 0) {
                    if (entityDomainValuesObj.has(selectedItemValue)) {
                        if (entityDomainValuesObj.getJSONObject(selectedItemValue).has(tagName)) {
                            JSONArray domainValuesJsonArray = entityDomainValuesObj.getJSONObject(selectedItemValue).getJSONArray(tagName);

                            List<Attribute> dependantAttributeList = new ArrayList<>();

                            for (int i = 0; i < domainValuesJsonArray.length(); i++) {

                                String attributeName = domainValuesJsonArray.getString(i);

                                if (attributeViewModelMap.containsKey(attributeName)) {
                                    Attribute attributeViewModel = attributeViewModelMap.get(attributeName);
                                    dependantAttributeList.add(attributeViewModel);
                                }
                            }
                            iAddFeatureView.showDependantPropertyView(activity, tagName, attributeTagModel, dependantAttributeList);
                        }
                    }


                    if (dependantPropGraph != null) {
                        String selectedValue = "";
                        JSONArray valuesJArray = entityDomainValuesObj.getJSONObject(tagName).getJSONArray("values");
                        for (int i = 0; i < valuesJArray.length(); i++) {
                            JSONObject jobj = valuesJArray.getJSONObject(i);
                            String label = jobj.getString("label");
                            String value = jobj.getString("value");
                            if (label.equalsIgnoreCase(selectedItemValue)) {
                                selectedValue = value;
                                break;
                            }
                        }
                        if (selectedValue.isEmpty()) {
                            return;
                        }
                        HashMap<String, Object> filterPropMap = new HashMap<>();
                        filterPropMap.put("name", selectedValue);
                        List<JSONObject> domainvalueNodeList = dependantPropGraph.getVertices(filterPropMap);
                        if (domainvalueNodeList == null || domainvalueNodeList.size() != 1) {
                            return;
                        }

                        Set<JSONObject> depPropList = dependantPropGraph.getDescendants(domainvalueNodeList.get(0));
                        if (depPropList == null || depPropList.isEmpty()) {
                            return;
                        }
                        List<Attribute> dependantAttributeList = new ArrayList<>();
                        for (JSONObject depPropJson : depPropList) {

                            String attributeName = depPropJson.getString("name");
                            if (attributeViewModelMap.containsKey(attributeName)) {
                                Attribute attributeViewModel = attributeViewModelMap.get(attributeName);
                                dependantAttributeList.add(attributeViewModel);
                            }
                        }
                        iAddFeatureView.showDependantPropertyView(activity, tagName, attributeTagModel, dependantAttributeList);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}