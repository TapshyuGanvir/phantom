package com.sixsimplex.treasurehunt.Phantom1.model;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeliveryDataModel {



    private static final DeliveryDataModel instance  =new DeliveryDataModel();
    public static final String traversalEntityName ="dropoff";
    public static final String consumerEntityName="consumer";
    public static final String tripentityname="trip";
    public static final String tripItemEntityname="tripitem";
    public static final String productEntityName="product";
    public static final String dropOffItemEntityName="dropoffitem";

//    public static final String traversalEntityName ="dropoff";
//    public static final String consumerEntityName="consumer";
//    public static final String tripentityname="trip";
//    public static final String tripItemEntityname="tripitem";
//    public static final String productEntityName="product";
//    public static final String dropOffItemEntityName="drpoffitem";

    public static DeliveryDataModel getInstance() {
        return instance;
    }

    private static List<Feature> featureList =new ArrayList<>();

    public static List<Feature> getFeatureList() {
        return featureList;
    }

    public static void setFeatureList(List<Feature> featureList) {
        DeliveryDataModel.featureList = featureList;
    }

    private void UpdateFeatureListInDataModelClass(){
    }

    public void addInTraversalFeatureList(Feature feature) {
        featureList.add(feature);
    }

    public void clearTraversalFeatureList() {
        featureList.clear();
    }




    private  CMEntity traversalEntity=null;
    public void setTraversalEntity(CMEntity cmEntity) {
        this.traversalEntity=cmEntity;
    }
    public  CMEntity getTraversalEntity() {
        return traversalEntity;
    }

    private Boolean deliveryState=null;
    public void setDeliveryState(Boolean deliveryState) {
        this.deliveryState=deliveryState;
    }
    public Boolean getDeliveryState() {
        return deliveryState;
    }

    private JSONObject route=null;
    public void setDeliveryRoute(JSONObject route) {
        this.route=route;
    }
    public JSONObject getRoute() {
        return route;
    }


    private Feature targetFeature=null;

    public Feature getTargetFeature() {
        return targetFeature;
    }

    public void setTargetFeature(Feature targetFeature) {
        this.targetFeature = targetFeature;
    }



    private CMEntity tripEntity=null;
    public void setTripEntity(CMEntity tripEntity) {
        this.tripEntity = tripEntity;
    }
    public CMEntity getTripEntity() {
        return tripEntity;
    }

    private CMEntity tripItemEntity=null;
    public void setTripItemEntity(CMEntity tripItemEntity) {
        this.tripItemEntity = tripItemEntity;
    }
    public CMEntity getTripItemEntity() {
        return tripItemEntity;
    }

    private CMEntity productEntity=null;
    public void setProductEntity(CMEntity productEntity) {
        this.productEntity = productEntity;
    }
    public CMEntity getProductEntity() {
        return productEntity;
    }

    public CMEntity dropOffItemEntity=null;
    public void setDropOffItemEntity(CMEntity dropOffItemEntity) {
        this.dropOffItemEntity = dropOffItemEntity;
    }
    public CMEntity getDropOffItemEntity() {
        return dropOffItemEntity;
    }

    public void clearAll() {
        featureList.clear();
        traversalEntity=null;
        deliveryState=null;
        route=null;
        targetFeature=null;
        tripEntity=null;
        tripItemEntity=null;
        productEntity=null;
        dropOffItemEntity=null;
    }


    private  List<Feature> consumersList =new ArrayList<>();
    public void setConsumersList(List<Feature> consumersList) {
        this.consumersList=consumersList;
    }
    public List<Feature> getConsumersList() {
        return consumersList;
    }


    private  List<Feature> inRangeFeature =new ArrayList<>();
    public void setInRangeFeature(List<Feature> inRangeFeature) {
        this.inRangeFeature = inRangeFeature;
    }
    public List<Feature> getInRangeFeature() {
        return inRangeFeature;
    }
}
