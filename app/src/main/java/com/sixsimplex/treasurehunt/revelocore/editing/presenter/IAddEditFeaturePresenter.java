package com.sixsimplex.treasurehunt.revelocore.editing.presenter;

import android.app.Activity;
import android.location.Location;
import android.view.View;
import android.widget.AutoCompleteTextView;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.editing.model.Attachment;
import com.sixsimplex.treasurehunt.revelocore.editing.view.IAddEditFeatureView;
import com.sixsimplex.treasurehunt.revelocore.layer.Attribute;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface IAddEditFeaturePresenter {

    void createForm(Activity activity, String formType, FeatureLayer featureLayer,
                    CMEntity cmEntity, Feature feature, JSONObject geometry,
                     String measurementString,
                             double measurementValue,
                    Map<String, String> jurisdictionNamesIdMap, Map<String, Object> jurisdictionValuesMap, Location location, JSONObject permissionJson);

    void createAttachmentView(String formType, Feature feature);

    void saveEditFeature(Activity activity, List<Attachment> attachmentLis, List<Attachment> getSelectForDeleteAttachmentFileList, Map<Integer, Attribute> attributeViewModelMap, String formType, Feature feature, JSONObject geometry, boolean featureIsInAdd, ViewPager viewPager, CMEntity cmEntity, BottomSheetDialog bottomSheetDialog, Location location, JSONObject permissionJson, View buttonView, IAddEditFeatureView iAddEditFeatureView);

    void autocompleteClick(Activity activity, String selectedItemValue, AutoCompleteTextView autoCompleteTextView);
}
