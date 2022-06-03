package com.sixsimplex.treasurehunt.revelocore.editing.view;

import android.app.Activity;

import com.sixsimplex.treasurehunt.revelocore.editing.model.Attachment;
import com.sixsimplex.treasurehunt.revelocore.editing.model.AttributeTagModel;
import com.sixsimplex.treasurehunt.revelocore.layer.Attribute;
import com.sixsimplex.treasurehunt.revelocore.layer.PropertyGroupsModel;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface IAddEditFeatureView {
    void createdView(LinkedHashMap<String, Attribute> viewMap,
                     Map<Integer, PropertyGroupsModel> propertyGroupsModelMap,
                     boolean featureIsInAdd, JSONObject domains);

    void error(String msg, boolean isDialog);

    void showDependantPropertyView(Activity activity, String parentViewTag, AttributeTagModel attributeTagModel, List<Attribute> dependantAttributeList);

    void removeLayout(Activity activity, String key);

    void error(String msg);

    void showAttachment(List<Attachment> attachments);

    void showProgressBar(String message);

    void hideProgressBar();
}
