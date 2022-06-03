package com.sixsimplex.treasurehunt.Phantom1.app.presenter;

import static android.content.Context.ACTIVITY_SERVICE;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.sixsimplex.treasurehunt.Phantom1.mode.ModeUtility;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.Phantom1.app.view.DeliveryMainActivity;
import com.sixsimplex.treasurehunt.Phantom1.deliveryservice.DeliveryService;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.Phantom1.trip.ITripCallback;
import com.sixsimplex.treasurehunt.Phantom1.trip.Trip;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.util.sort.ReveloFeatureComparator;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DeliveryPresenter {

    IdeliveryActivityView ideliveryActivityView;
    CMGraph cmGraph;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location locationL = null;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    public DeliveryPresenter(IdeliveryActivityView ideliveryActivityView) {
        this.ideliveryActivityView = ideliveryActivityView;

    }

    public void createTraversalFeatureListAndStoreInDataModelAndUpdate(Context context) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject graphResult = CMUtils.getCMGraph(context);
                        if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                            cmGraph = (CMGraph) graphResult.get("result");
                        } else {
                            return;
                        }

                        Set<CMEntity> entities = cmGraph.getAllVertices();
                        for (CMEntity cmEntity : entities) {
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.traversalEntityName)) {
                                JSONObject sortJsonObject = new JSONObject();
                                try {
                                    sortJsonObject.put("sortBy", ReveloFeatureComparator.TraversalSort);
                                    sortJsonObject.put("sortByProperty", cmEntity.getW9IdProperty());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    sortJsonObject = null;
                                }
//                            List<Feature> featureList = cmEntity.getSortedFeaturesByQuery(context, true,
//                                    null, null, null, "OR",
//                                    true, false, true, 0,
//                                    -1, false, true, true, sortJsonObject);
                                DeliveryDataModel.getInstance().setFeatureList(
                                        cmEntity.getSortedFeaturesByQuery(context, true,
                                                null, null, null, "OR",
                                                true, false, true, 0,
                                                -1, false, true, true, sortJsonObject)
                                );
                                DeliveryDataModel.getInstance().setTraversalEntity(cmEntity);
                            }
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.tripentityname)) {
                                DeliveryDataModel.getInstance().setTripEntity(cmEntity);
                            }
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.tripItemEntityname)) {
                                DeliveryDataModel.getInstance().setTripItemEntity(cmEntity);
                            }
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.productEntityName)) {
                                DeliveryDataModel.getInstance().setProductEntity(cmEntity);
                            }
                            if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.dropOffItemEntityName)) {
                                DeliveryDataModel.getInstance().setDropOffItemEntity(cmEntity);
                            }
                            if(cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.consumerEntityName)){

                                List<String> requiredColumnList = new ArrayList<>();
                                requiredColumnList.add("customerid");
                                requiredColumnList.add("cusname");
                                requiredColumnList.add("mobno");
                                requiredColumnList.add("address");

                                DeliveryDataModel.getInstance().setConsumersList(

                                        cmEntity.getFeatureTable().getallFeaturesList(context,requiredColumnList,false)
                                );
                            }
                        }
                        Trip.initiateTripData(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ideliveryActivityView.onTraversalDataFetchComplete();
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @SuppressLint("LongLogTag")
    public void startDeliveryBtn(DeliveryMainActivity activity, Location location, Feature feature) {
        try {
            locationL = location;
            if (locationL == null) {
                locationL = new Location("");
                locationL.setLatitude(00.00);
                locationL.setLongitude(00.00);
            }
            ITripCallback iTripCallback = new ITripCallback() {
                @Override
                public void onSuccessTripAddUpdate() {
                    try {
                        startDeliveryService(activity, feature);
                        ideliveryActivityView.hideProgressDialog();
                        activity.onStart();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailureTripAddUpdate(String message) {
                    ideliveryActivityView.hideProgressDialog();
                }
            };
            Trip.addUpdateTrip(activity, Trip.ADD, locationL, iTripCallback, ideliveryActivityView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startDeliveryService(Context context, Feature feature) {
        try {
            if (feature != null) {
                DeliveryDataModel.getInstance().setTargetFeature(feature);
                Intent intent = new Intent(context, DeliveryService.class);
                intent.putExtra(DeliveryService.ACTION, DeliveryService.ACTION_START_DELIVERY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);
                } else {
                    context.startService(intent);
                }
                if (!DeliveryService.isDeliveryServiceRunning((ActivityManager) context.getSystemService(ACTIVITY_SERVICE))) {
                    DeliveryDataModel.getInstance().setTargetFeature(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void stopDelivery(Context context, Location location) {
        try {
            locationL = location;
            if (locationL == null) {
                locationL = new Location("");
                locationL.setLatitude(00.00);
                locationL.setLongitude(00.00);
            }
            ITripCallback iTripCallback = new ITripCallback() {
                @Override
                public void onSuccessTripAddUpdate() {
                    context.stopService(new Intent(context, DeliveryService.class));
                    ideliveryActivityView.hideProgressDialog();
                }

                @Override
                public void onFailureTripAddUpdate(String message) {
                    ideliveryActivityView.hideProgressDialog();
                }
            };
            Trip.addUpdateTrip(context, Trip.EDIT, locationL, iTripCallback, ideliveryActivityView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("MissingPermission")
    private void getCurrentLocation(Context context) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    locationL = location;
                }
            }
        });
    }

    private String generateAutomaticId() {
        String w9Id = "";
        w9Id = UUID.randomUUID().toString();
        return w9Id;
    }

    public void markAsDeliver(Context context, String mode) {
        Intent intent = new Intent(context, DeliveryService.class);

        if (mode.equals(ModeUtility.SINGLE)) {
            intent.putExtra(DeliveryService.ACTION, DeliveryService.ACTION_COMPLETE_DELIVERY);
        } else if (mode.equals(ModeUtility.MULTI)) {
            intent.putExtra(DeliveryService.ACTION, DeliveryService.ACTION_START_DELIVERY);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public void showNextDelivery(Context context) {
        if(hasNextTraversingFeature()){
            Feature feature=getCurrentlyTraversingFeature();
            if(feature != null){
                startDeliveryService(context,feature);
            }
        }
    }

    public void openInfoFillForm(DeliveryMainActivity deliveryMainActivity, String mode) {
        if (DeliveryDataModel.getInstance().getTargetFeature() != null) {
            ideliveryActivityView.performDeliveryActionForFeature(DeliveryDataModel.getInstance().getTargetFeature(), mode);
        }
    }

    public void updateTraversalFeatureList(Context context, int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject graphResult = CMUtils.getCMGraph(context);
                    if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                        cmGraph = (CMGraph) graphResult.get("result");
                    } else {
                        return;
                    }
                    Set<CMEntity> entities = cmGraph.getAllVertices();
                    for (CMEntity cmEntity : entities) {
                        if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.traversalEntityName)) {
                            JSONObject sortJsonObject = new JSONObject();
                            try {
                                sortJsonObject.put("sortBy", ReveloFeatureComparator.TraversalSort);
                                sortJsonObject.put("sortByProperty", cmEntity.getW9IdProperty());
                            } catch (Exception e) {
                                e.printStackTrace();
                                sortJsonObject = null;
                            }

                            DeliveryDataModel.getInstance().setFeatureList(cmEntity.getSortedFeaturesByQuery(context, true,
                                    null, null, null, "OR",
                                    true, false, true, 0,
                                    -1, false, true, true, sortJsonObject));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ideliveryActivityView.updateHomeAndMapUI(position);
                    }
                });
            }

        }).start();
    }

    public boolean hasNextTraversingFeature() {
        boolean has = false;
        try {
            if (DeliveryDataModel.getInstance().getTraversalEntity() != null) {
                if (DeliveryDataModel.getInstance().getTraversalEntity().getTraversalGraph() != null) {
                    if (DeliveryDataModel.getInstance().getTraversalEntity().getTraversalGraph().getCurrentlyTraversingFeature() != null) {
                        has = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return has;
    }

    public Feature getCurrentlyTraversingFeature() {
        try {
            if (hasNextTraversingFeature()) {
                JSONObject currentlyTraversingVertex = DeliveryDataModel.getInstance().getTraversalEntity().getTraversalGraph().getCurrentlyTraversingFeature();
                if (currentlyTraversingVertex != null && currentlyTraversingVertex.has("w9id")) {
                    if (currentlyTraversingVertex.getString("w9id") != null && !currentlyTraversingVertex.getString("w9id").isEmpty()) {
                        if (!DeliveryDataModel.getFeatureList().isEmpty()) {
                            for (Feature feature : DeliveryDataModel.getFeatureList()) {
                                if (feature.getFeatureId().equals(currentlyTraversingVertex.getString("w9id"))) {
                                    return feature;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
