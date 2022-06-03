package com.sixsimplex.treasurehunt.Phantom1.appfragment.map;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.app.view.DeliveryMainActivity;
import com.sixsimplex.treasurehunt.Phantom1.deliveryservice.DeliveryService;
import com.sixsimplex.treasurehunt.Phantom1.direction.DirectionModel;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.Phantom1.utils.Utils;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.InfoBottomSheet;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.InfoBottomSheetInterface;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.locationModule.GetUserLocation;
import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class MapFragment extends Fragment implements InfoBottomSheetInterface {
    Context context;
    MapFragmentPresenter mapFragmentPresenter;
    GetUserLocation getUserLocation;
    CardView locationCv;
    ImageView locationIv;
    LinearLayout targetFeatureTv;
    TextView targetNameTv, targetAddressTv, targetMobileTv,targetCountTv;
    Toolbar mapToolBar;
    FolderOverlay featuresOverlay = null;
    FolderOverlay directionFolderOverlay = null;
    Overlay targetOverlay = null;
    SimpleFastPointOverlay simpleFastLabelOverlay = null;
    Overlay directionGeometryOverlay = null;
    Overlay directionGeometryStartOverlay = null;
    Overlay directionGeometryEndOverlay = null;
    LinkedList<DirectionModel> direcionsDetailsLinkedList = null;
    Handler mHandler = new Handler(Looper.getMainLooper());
    BoundingBox routeBoundingBox = null;
    LinearLayout transparentViewMap;
    private MapView mapView = null;
    private boolean zoomOnDirection=true;
    List<Overlay> selectedFeatureOverlays=new ArrayList<>();
    public List<Overlay> getSelectedFeatureOverlays() {
        return selectedFeatureOverlays;
    }

    public void setSelectedFeatureOverlays(List<Overlay> selectedFeatureOverlays) {
        this.selectedFeatureOverlays = selectedFeatureOverlays;
    }

    ImapFragmentView imapFragmentView = new ImapFragmentView() {
        @Override
        public void showFeaturesOnUI(LinkedHashMap<String, FeatureLayer> featureLayerLinkedHashMap, CMGraph cmGraph) {
            if (mapView != null) {
                for (FeatureLayer featureLayer : featureLayerLinkedHashMap.values()) {

                    if (featureLayer != null) {
                        if (featuresOverlay != null) {
                            mapView.getOverlayManager().remove(featuresOverlay);
                        }
                        if (simpleFastLabelOverlay != null) {
                            mapView.getOverlayManager().remove(simpleFastLabelOverlay);
                        }

                        featuresOverlay = featureLayer.getFeatureOverlay();
                        simpleFastLabelOverlay = featureLayer.getLabelOverlay();
                        if (featuresOverlay != null) {
                            mapView.getOverlayManager().add(featuresOverlay);
                        }
                        if (simpleFastLabelOverlay != null) {
                            mapView.getOverlayManager().add(simpleFastLabelOverlay);
                        }
                    }
                }
                mapView.invalidate();
            }
        }

        @Override
        public void onGetSelectedFeatures(List<Overlay> selectedFeatures) {
            try {
                setSelectedFeatureOverlays(selectedFeatures);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        RefreshDeliveryState(selectedFeatures);
                    }
                }).start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void RefreshDeliveryState(List<Overlay> selectedFeatures) {
        try {
            if(selectedFeatures != null){
                if(selectedFeatures.size() == 1){
                    Overlay selectedFeature=selectedFeatures.get(0);
                    if(selectedFeature instanceof Marker){
                        Marker marker = (Marker) selectedFeature;
                        for(Feature feature: DeliveryDataModel.getFeatureList()){
                            if(feature.getFeatureId().equals(marker.getId())){
                                if(DeliveryDataModel.getInstance().getTargetFeature() != null){
                                    if(!feature.getFeatureId().equals(DeliveryDataModel.getInstance().getTargetFeature().getFeatureId())) {
                                        ((DeliveryMainActivity) requireActivity()).getDeliveryPresenter().startDeliveryService(context, feature);
                                        zoomOnDirection=false;
                                    }
                                }else{
                                    ((DeliveryMainActivity) requireActivity()).getDeliveryPresenter().startDeliveryService(context, feature);
                                    zoomOnDirection=false;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }catch ( Exception e){
            e.printStackTrace();
        }
    }

    private FolderOverlay locationBufferFolderOverlay = null;
    private Overlay bufferGeometryOverlay = null;
    private boolean zoomToLocation = true;
    private DeliveryService deliveryService;

    public MapFragment(Context context, MapFragmentPresenter mapFragmentPresenter) {
        // Required empty public constructor
        this.context = context;
        this.mapFragmentPresenter = mapFragmentPresenter;
        mapFragmentPresenter.setImapViewCallBack(imapFragmentView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        configureViews(view);
        showDeliveryFeatureOnMap();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        showOptionMenu();
        checkIfDeliveryServiceRunningAndUpdateUI();
//        zoomMap();
    }

    private void showOptionMenu() {
        try {
            if (mapToolBar != null) {
                ((DeliveryMainActivity) requireActivity()).setSupportActionBar(mapToolBar);
                Objects.requireNonNull(((DeliveryMainActivity) requireActivity()).getSupportActionBar()).show();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @SuppressLint("UseSupportActionBar")
    private void configureViews(View view) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        mapView = (MapView) view.findViewById(R.id.map_d_view);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setTilesScaledToDpi(true);
        mapView.setHasTransientState(true);
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setScrollableAreaLimitLatitude(MapView.getTileSystem().getMaxLatitude(), MapView.getTileSystem().getMinLatitude(), 0);
        IMapController mapController = mapView.getController();
        mapController.setZoom(11.0);
        GeoPoint startPoint = new GeoPoint(21.1458, 79.0882);
        mapController.setCenter(startPoint);

        mapToolBar = view.findViewById(R.id.map_toolbar);
        mapToolBar.setTitle("Drop Off");

        transparentViewMap = view.findViewById(R.id.transparentViewMap);


        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(context, mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().add(mRotationGestureOverlay);


        targetFeatureTv = view.findViewById(R.id.target_feature_tv);
        targetNameTv = view.findViewById(R.id.name_tv);
        targetAddressTv = view.findViewById(R.id.address_tv);
        targetMobileTv = view.findViewById(R.id.mobile_number_tv);
        targetCountTv=view.findViewById(R.id.target_count_tv);

        locationCv = view.findViewById(R.id.locationCv);
        locationIv = view.findViewById(R.id.locationIv);
        locationCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLocation();
            }
        });
        getUserLocation = new GetUserLocation(getActivity(), mapView, locationIv, null, null, this::onLocationChange);
        enableLocationDialog();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.mapbarmenu, menu);
        try {
            MenuItem item = menu.getItem(0);
            if (DeliveryDataModel.getInstance().getDeliveryState() != null) {
                if (DeliveryDataModel.getInstance().getDeliveryState()) {
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    public void showDeliveryFeatureOnMap() {
        if (targetOverlay != null) {
            featuresOverlay.remove(targetOverlay);
        }
        if (directionFolderOverlay == null) {
            directionFolderOverlay = new FolderOverlay();
            directionFolderOverlay.setName("directions");
            mapView.getOverlayManager().add(directionFolderOverlay);
        }

        mapFragmentPresenter.showDeliveryFeatureOnMap(context, mapView, imapFragmentView);
    }

    private void enableLocationDialog() {
        if (getUserLocation != null) {
            boolean isLocationEnable = getUserLocation.checkLocationIsEnable();
            if (!isLocationEnable) {

                InfoBottomSheet infoBottomSheet = InfoBottomSheet.geInstance(getActivity(),
                        "Open Location Settings", "No", "Enable device location.", "",
                        AppConstants.LOCATION_REQUEST, 1, "");

                infoBottomSheet.setCancelable(false);
                infoBottomSheet.show(requireActivity().getSupportFragmentManager(), "Location Request");
            }
        }
    }

    private void userLocation() {
        if (getUserLocation != null) {
            getUserLocation.zoomLocation(null);
        }
        enableLocationDialog();
    }


    private void onLocationChange(Location location) {

        if (getUserLocation != null && zoomToLocation) {
            zoomToLocation = false;
            getUserLocation.zoomLocation(null);
            getUserLocation.showUserCurrentLatLong();
        }
    }

    @Override
    public void onOkInfoBottomSheetResult(int requestCode, int errorCode, String jurisdictions) {
        if (requestCode == AppConstants.LOCATION_REQUEST) {
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }
    }

    @Override
    public void onCancelOkBottomSheetResult(int requestCode, int errorCode) {

    }

    private void checkIfDeliveryServiceRunningAndUpdateUI() {
        if (DeliveryDataModel.getInstance().getDeliveryState() != null && DeliveryDataModel.getInstance().getDeliveryState()) {
            if (DeliveryDataModel.getInstance().getRoute() != null) {
                showRouteOnMap(DeliveryDataModel.getInstance().getRoute());
                showTargetTv(DeliveryDataModel.getInstance().getTargetFeature());
                updateTargetFeatureUiOnMap(DeliveryDataModel.getInstance().getTargetFeature());
            }
        }
        updateMapFragmentUIIfDeliveryIsOnGoing(DeliveryDataModel.getInstance().getDeliveryState());
    }


    private void showStopOptionInMenu() {
        try {
            if (DeliveryDataModel.getInstance().getDeliveryState() != null) {
                if (DeliveryDataModel.getInstance().getDeliveryState()) {
                    ((DeliveryMainActivity) requireActivity()).showHideStopItemInMenu(true);
                } else {
                    ((DeliveryMainActivity) requireActivity()).showHideStopItemInMenu(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    public void showTargetTv(Feature targetFeature) {
        try {
            if (targetFeature != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Feature feature = mapFragmentPresenter.getConsumerFeatureForTargetDelivery(getActivity(), targetFeature);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (feature != null) {
                                        int position = DeliveryDataModel.getFeatureList().indexOf(targetFeature) + 1;
//                                        String firstname = (String) feature.getAttributes().get("firstname");
//                                        String lastname = (String) feature.getAttributes().get("lastname");

                                        String name =(String) feature.getAttributes().get("cusname");
                                        String address = (String) targetFeature.getAttributes().get("address");
                                        String mobileNumber = String.valueOf(feature.getAttributes().get("mobno"));

                                        targetNameTv.setText(name);
                                        targetAddressTv.setText(address);
                                        targetFeatureTv.setVisibility(View.VISIBLE);
                                        targetCountTv.setText(String.valueOf(position));

                                        targetMobileTv.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobileNumber, null));
                                                startActivity(intent);
                                            }
                                        });
                                    } else {
                                        String address = (String) targetFeature.getAttributes().get("address");
                                        targetNameTv.setText("Your Customer");
                                        targetAddressTv.setText(address);
                                        targetCountTv.setText(String.valueOf(1));
                                        targetFeatureTv.setVisibility(View.VISIBLE);

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }).start();
            } else {

                targetNameTv.setText("");
                targetAddressTv.setText("");
                targetCountTv.setText("");
                targetFeatureTv.setVisibility(View.GONE);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTargetFeatureUiOnMap(Feature targetFeature) {
        try {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    selectTargetFeature(targetFeature);
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectTargetFeature(Feature targetFeature) {
        try {

            if (targetFeature != null) {
                if (featuresOverlay != null) {
                    List<Overlay> overlays = featuresOverlay.getItems();
                    for (Overlay overlay : overlays) {
                        if (overlay instanceof Marker) {
                            Marker marker = (Marker) overlay;
                            if (targetFeature.getFeatureId().equals(marker.getId())) {
                                String customFillColor = "";
                                boolean isDelivered = (boolean) targetFeature.getAttributes().get("isdelivered");
//                                boolean isVisited = (boolean) targetFeature.getAttributes().get("isvisited");
                                boolean isSkipped = (boolean) targetFeature.getAttributes().get("skipped");

                                if (!isSkipped && !isDelivered) {
                                    customFillColor = requireActivity().getResources().getString(R.string.pending);
                                } else if (isSkipped && !isDelivered) {
                                    customFillColor = requireActivity().getResources().getString(R.string.incomplete);
                                } else if (isSkipped && isDelivered) {
                                    customFillColor = requireActivity().getResources().getString(R.string.complete);
                                }
//                                mapFragmentPresenter.selectFeature(requireActivity(),overlay,customFillColor);
                                if(getSelectedFeatureOverlays() != null){
                                    if(!getSelectedFeatureOverlays().contains(overlay)){
                                        mapFragmentPresenter.selectFeature(requireActivity(),overlay,customFillColor);
                                    }
                                }
//                                targetOverlay = overlay;
//                                Drawable mDrawable = VectorDrawableUtils.getDrawable(requireActivity(), R.drawable.ic_geom_marker);
//                                Drawable mDrawableNew = mDrawable.getConstantState().newDrawable().mutate();
//                                mHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        marker.setIcon(mDrawableNew);
//                                    }
//                                });

//                                marker.setInfoWindow();

                                break;
                            }
                        }
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mapView.invalidate();
                    }
                });
            } else {
                if (((DeliveryMainActivity) requireActivity()).getDeliveryPresenter() != null) {
                    ((DeliveryMainActivity) requireActivity()).getDeliveryPresenter().updateTraversalFeatureList(context, -1);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void removeDirectionFromMap() {
        if (directionFolderOverlay != null) {
            List<Overlay> overlays = directionFolderOverlay.getItems();
            if (overlays != null && overlays.size() > 0) {
                directionFolderOverlay.remove(directionGeometryOverlay);
                directionFolderOverlay.remove(directionGeometryStartOverlay);
                directionFolderOverlay.remove(directionGeometryEndOverlay);
                directionGeometryOverlay = null;
                directionGeometryStartOverlay = null;
                directionGeometryEndOverlay = null;
                routeBoundingBox = null;
            }
        }
        mapView.invalidate();
    }

    public void showRouteOnMap(JSONObject routeJsonObj) {
        try {

            if (routeJsonObj != null) {
                JSONObject route = new JSONObject();
                if (routeJsonObj.has("route")) {
                    route = routeJsonObj.getJSONObject("route");
                }
                Location startLocation = (Location) routeJsonObj.get("startLocation");
                Location endLocation = (Location) routeJsonObj.get("endLocation");
//                mapView.getOverlayManager().remove(directionFolderOverlay);
                Polyline polyline = Utils.toOSMPolyline(route);
                Polyline startLine = Utils.toStartLine(route, startLocation);
                Polyline endLine = Utils.toEndLine(route, endLocation);
                if (polyline != null) {

                    if (directionFolderOverlay == null) {
                        directionFolderOverlay = new FolderOverlay();
                        directionFolderOverlay.setName("directions");
                    } else {
                        removeDirectionFromMap();
                    }
                    try {

                        // route Line
                        polyline.setId("directions");
                        polyline.setVisible(true);
                        Paint linePaint = polyline.getOutlinePaint();
                        linePaint.setColor(Color.parseColor("#000000"));
                        linePaint.setStrokeWidth(10);
                        linePaint.setStyle(Paint.Style.STROKE);
                        linePaint.setStrokeCap(Paint.Cap.SQUARE);
//                    linePaint.setPathEffect(new ComposePathEffect());
                           /* polyline.getOutlinePaint().setColor(Color.parseColor("#1890FF"));
                            polyline.getOutlinePaint().setStrokeWidth(20);*/
                        directionFolderOverlay.add(polyline);
                        directionGeometryOverlay = polyline;

                        //start line
                        startLine.setId("directions");
                        startLine.setVisible(true);
                        Paint startLinePaint = startLine.getOutlinePaint();
                        startLinePaint.setColor(Color.parseColor("#000000"));
                        startLinePaint.setStrokeWidth(10);
                        startLinePaint.setStyle(Paint.Style.STROKE);
                        startLinePaint.setStrokeCap(Paint.Cap.SQUARE);
                        startLinePaint.setPathEffect(new DashPathEffect(new float[]{8, 15}, 20));
//                    linePaint.setPathEffect(new ComposePathEffect());
                           /* polyline.getOutlinePaint().setColor(Color.parseColor("#1890FF"));
                            polyline.getOutlinePaint().setStrokeWidth(20);*/
                        directionFolderOverlay.add(startLine);
                        directionGeometryStartOverlay = startLine;


                        //end line
                        endLine.setId("directions");
                        endLine.setVisible(true);
                        Paint endLinePaint = endLine.getOutlinePaint();
                        endLinePaint.setColor(Color.parseColor("#000000"));
                        endLinePaint.setStrokeWidth(10);
                        endLinePaint.setStyle(Paint.Style.STROKE);
                        endLinePaint.setStrokeCap(Paint.Cap.SQUARE);
                        endLinePaint.setPathEffect(new DashPathEffect(new float[]{8, 15}, 20));
//                    linePaint.setPathEffect(new ComposePathEffect());
                           /* polyline.getOutlinePaint().setColor(Color.parseColor("#1890FF"));
                            polyline.getOutlinePaint().setStrokeWidth(20);*/
                        directionFolderOverlay.add(endLine);
                        directionGeometryEndOverlay = endLine;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    mapView.getOverlayManager().add(directionFolderOverlay);
                    if (directionFolderOverlay != null) {
                        routeBoundingBox = directionFolderOverlay.getBounds();
                        if(zoomOnDirection){
                            mapView.zoomToBoundingBox(directionFolderOverlay.getBounds(), true, 100, mapView.getMaxZoomLevel(), null);
                        }else{
                            zoomOnDirection=true;
                        }
                    }
                    mapView.invalidate();
                }
                if (direcionsDetailsLinkedList == null) {
                    direcionsDetailsLinkedList = new LinkedList<>();
                } else {
                    direcionsDetailsLinkedList.clear();
                }
                if (route.has("features")) {
                    JSONArray features = route.getJSONArray("features");
                    for (int i = 0; i < features.length(); i++) {
                        JSONObject featureJson = features.getJSONObject(i);
                        if (featureJson.has("properties")) {
                            JSONObject propertyJson = featureJson.getJSONObject("properties");
                            JSONArray segmentsJson = propertyJson.getJSONArray("segments");
                            JSONObject directionJson = segmentsJson.getJSONObject(0);
                            String distance = directionJson.getString("distance") + "km";
                            JSONArray directionSteps = directionJson.getJSONArray("steps");

                            for (int j = 0; j < directionSteps.length(); j++) {
                                JSONObject directionStepsObject = directionSteps.getJSONObject(j);
                                DirectionModel directionModel = new DirectionModel();
                                Double distancePerSteps = directionStepsObject.getDouble("distance");
                                String instructionPerSteps = directionStepsObject.getString("instruction");
                                int directionType = directionStepsObject.getInt("type");
                                directionModel.setIndex(j);
                                directionModel.setDistance(distancePerSteps);
                                directionModel.setInstruction(instructionPerSteps);
                                directionModel.setType(directionType);
                                direcionsDetailsLinkedList.add(directionModel);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zoomMap() {
        try {
            if (mapView != null) {
                if (routeBoundingBox != null) {
                    mapView.zoomToBoundingBox(routeBoundingBox, true, 100, 10, null);
                } else {
                    if (featuresOverlay != null) {
                        mapView.zoomToBoundingBox(featuresOverlay.getBounds(), true, 100, mapView.getMaxZoomLevel(), null);
                    }
                }
                mapView.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showBufferOnMap(Geometry bufferGeometry) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Polygon> polygons = Utils.convertGeometry(bufferGeometry);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (polygons != null && polygons.size() > 0) {

                                    if (locationBufferFolderOverlay == null) {
                                        locationBufferFolderOverlay = new FolderOverlay();
                                        locationBufferFolderOverlay.setName("LocationBuffer");
                                        for (Polygon polygon : polygons) {
                                            try {
                                                polygon.setId("locationBuffer");
                                                locationBufferFolderOverlay.add(polygon);
                                                bufferGeometryOverlay = polygon;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        if (featuresOverlay != null) {
                                            mapView.getOverlayManager().remove(featuresOverlay);
                                        }
                                        if (simpleFastLabelOverlay != null) {
                                            mapView.getOverlayManager().remove(simpleFastLabelOverlay);
                                        }

                                        mapView.getOverlayManager().add(locationBufferFolderOverlay);
                                        if (featuresOverlay != null) {
                                            mapView.getOverlayManager().add(featuresOverlay);
                                        }
                                        if (simpleFastLabelOverlay != null) {
                                            mapView.getOverlayManager().add(simpleFastLabelOverlay);
                                        }
                                    } else {
                                        removeExistingBuffer();
                                        for (Polygon polygon : polygons) {
                                            try {
                                                polygon.setId("locationBuffer");
                                                locationBufferFolderOverlay.add(polygon);
                                                bufferGeometryOverlay = polygon;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    mapView.invalidate();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeExistingBufferOnMap() {
        try {
            removeExistingBuffer();
            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void removeExistingBuffer() {
        try {
            if (locationBufferFolderOverlay != null && bufferGeometryOverlay != null) {
                List<Overlay> overlays = locationBufferFolderOverlay.getItems();
                if (overlays != null && overlays.size() > 0) {
                    locationBufferFolderOverlay.remove(bufferGeometryOverlay);
                    bufferGeometryOverlay = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        showStopOptionInMenu();
        if (hidden) {
            ((DeliveryMainActivity) requireActivity()).showHideButtonTabLayout(false);
        } else {
            ((DeliveryMainActivity) requireActivity()).showHideButtonTabLayout(true);
            if(DeliveryDataModel.getInstance().getDeliveryState() != null){
                ((DeliveryMainActivity) requireActivity()).configureUi(DeliveryDataModel.getInstance().getDeliveryState());
            }else{
                ((DeliveryMainActivity) requireActivity()).configureUi(false);
            }
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public void updateMapFragmentUIIfDeliveryIsOnGoing(Boolean deliveryState) {
        try {
            if (deliveryState != null) {
                if (deliveryState) {
                    if (transparentViewMap != null) {
                        transparentViewMap.setVisibility(View.GONE);
                    }
                } else {
                    if (transparentViewMap != null) {
                        transparentViewMap.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (transparentViewMap != null) {
                    transparentViewMap.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deSelectTheSelectingFeature() {
        return mapFragmentPresenter.delselectTHeSelectedFeature(requireActivity());
    }

    public void setSelectableFeature(List<Feature> inRangeTargetList) {
        try {
            List<Feature> excludedFeatureList = new ArrayList<>();
            if (DeliveryDataModel.getInstance().getTargetFeature() != null) {
                excludedFeatureList.add(DeliveryDataModel.getInstance().getTargetFeature());
            }
            mapFragmentPresenter.setSelectableFeature(requireActivity(),inRangeTargetList, excludedFeatureList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}