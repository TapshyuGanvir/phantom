package com.sixsimplex.treasurehunt.Phantom1.appfragment.map;


import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;
import com.sixsimplex.treasurehunt.Phantom1.utils.TraversalUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class MapFragmentPresenter implements ImapFragmentPresenter, FeatureLayer.GetSelectedFeature {


    CMGraph cmGraph = null;
    ImapFragmentView imapFragmentView;
    LinkedHashMap<String, FeatureLayer> featureLayerLinkedHashMap;
    public List<Overlay> getSelectedFeatures() {
        return selectedFeatures;
    }
    List<Overlay> selectedFeatures=new ArrayList<>();
    FeatureLayer featureLayer;


    public void showDeliveryFeatureOnMap(Context context, MapView mapView, ImapFragmentView imapFragmentView) {
        featureLayerLinkedHashMap = new LinkedHashMap<>();
        try {
            if(DeliveryDataModel.getFeatureList() != null && !DeliveryDataModel.getFeatureList().isEmpty()){
                JSONObject graphResult = CMUtils.getCMGraph(context);
                if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                    cmGraph = (CMGraph) graphResult.get("result");
                } else {
                    return;
                }
                if (DeliveryDataModel.getInstance().getTraversalEntity() != null) {
                    try {
                        getData(featureLayerLinkedHashMap, DeliveryDataModel.getInstance().getTraversalEntity(), cmGraph, mapView, context, DeliveryDataModel.getFeatureList());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        imapFragmentView.showFeaturesOnUI(featureLayerLinkedHashMap, cmGraph);
    }

    private void getData(LinkedHashMap<String, FeatureLayer> featureLayerLinkedHashMap, CMEntity cmEntity, CMGraph cmGraph, MapView mapView, Context context, List<Feature> featureList) {
        featureLayer= cmEntity.getFeatureLayer();
        featureLayer.setRoot(cmGraph.isRootVertex(cmEntity));
        featureLayer.setFeatureTable(cmEntity.getFeatureTable());
        featureLayer.setMapView(mapView);
        featureLayer.setSelected(true);
        featureLayer.showFeatureOnMapForDelivery((Activity) context, TraversalUtils.getEntityTraversalGraph(cmEntity), featureList, this);
        featureLayerLinkedHashMap.put(cmEntity.getName(), cmEntity.getFeatureLayer());
    }

    @Override
    public void setSelectedFeature(Context context, List<Overlay> features) {
        this.selectedFeatures=features;
        if(imapFragmentView != null){
            imapFragmentView.onGetSelectedFeatures(features);
        }
    }

    public Feature getConsumerFeatureForTargetDelivery(Context activity, Feature targetFeature) {
        List<Feature> featureList = new ArrayList<>();
        try {
            String customerid = (String) targetFeature.getAttributes().get("customerid");
            if (customerid == null) {
                return null;
            }
            JSONObject graphResult = CMUtils.getCMGraph(activity);
            if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                cmGraph = (CMGraph) graphResult.get("result");
            } else {

            }
            Set<CMEntity> rootEntities = cmGraph.getRootVertices();
            for (CMEntity cmEntity : rootEntities) {
                if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.consumerEntityName)) {
                    List<String> requiredColumnList = new ArrayList<>();
                    requiredColumnList.add("customerid");
                    requiredColumnList.add("cusname");
                    requiredColumnList.add("mobno");
                    requiredColumnList.add("address");
                    requiredColumnList.add("pincode");


                    JSONArray conditionClause = new JSONArray();
                    JSONObject conditionJobj = new JSONObject();
                    conditionJobj.put("conditionType", "attribute");
                    conditionJobj.put("columnName", "customerid");
                    conditionJobj.put("valueDataType", "String");
                    conditionJobj.put("value", customerid);
                    conditionJobj.put("operator", "=");
                    conditionClause.put(conditionJobj);

                    featureList = cmEntity.getFeatureTable().getFeaturesByQuery(activity,
                            requiredColumnList,
                            null, conditionClause,
                            "OR",
                            true,
                            false,
                            true,
                            0,
                            -1,
                            false,
                            false);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Feature feature = null;
        if (!featureList.isEmpty()) {
            feature = featureList.get(0);
        }
        return feature;
    }

    public boolean delselectTHeSelectedFeature(FragmentActivity fragmentActivity) {
        if(featureLayer != null){
            if(!getSelectedFeatures().isEmpty()){
                featureLayer.unSelectFeatures(fragmentActivity,featureLayer.getLastSelectionColor());
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public void setSelectableFeature(FragmentActivity fragmentActivity, List<Feature> inRangeTargetList, List<Feature> excludedFeatureList) {
        try{
            if(featureLayer != null){
                featureLayer.setSelectableFeatures(fragmentActivity,inRangeTargetList,excludedFeatureList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setImapViewCallBack(ImapFragmentView imapFragmentView) {
        this.imapFragmentView=imapFragmentView;
    }

    public void selectFeature(FragmentActivity activity, Overlay overlay, String customFillColor) {
        try{
            if(featureLayer != null){
                featureLayer.selectUnselectPoint(activity,overlay,customFillColor);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
