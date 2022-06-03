package com.sixsimplex.treasurehunt.Phantom1.app.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.dashboard.DashBoardFragment;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.dashboard.DashBoardFragmentPresenter;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.delproduct.ProductSheetFragment;
import com.sixsimplex.treasurehunt.Phantom1.mode.ModeUtility;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.Phantom1.app.presenter.DeliveryPresenter;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.formsheet.FormSheetFragment;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.home.HomeFragmentPresenter;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.home.IhomeFragmentView;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.profile.ProfilePresenter;
import com.sixsimplex.treasurehunt.Phantom1.deliveryservice.DeliveryService;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.Phantom1.trip.ITripCallback;
import com.sixsimplex.treasurehunt.Phantom1.trip.TripItemsForm;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.home.HomeFragment;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.map.MapFragment;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.map.MapFragmentPresenter;
import com.sixsimplex.treasurehunt.Phantom1.appfragment.profile.ProfileFragment;
import com.sixsimplex.treasurehunt.revelocore.liveLocationUpdate.SendLocationToServerService;
import com.sixsimplex.treasurehunt.revelocore.util.RuntimePermission;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.InfoBottomSheetInterface;
import com.sixsimplex.treasurehunt.revelocore.util.locationModule.GetUserLocation;
import com.vividsolutions.jts.geom.Geometry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeliveryMainActivity extends AppCompatActivity implements IhomeFragmentView, IdeliveryActivityView, InfoBottomSheetInterface {

    BottomNavigationView bottomNavigationView;

    FrameLayout flFragment, sliderContainer;
    DeliveryPresenter deliveryPresenter;
    Fragment activeFragment;
    ConstraintLayout buttonsLayoutRl;
    Button startDeliveryBtn, pauseDeliveryBtn, deliverBtn, showNextBtn;
    Location locationL;
    ServiceConnection deliveryServiceConnection;
    boolean mServiceConnected = false;
    boolean mBound = false;
    GetUserLocation getUserLocation = null;
    Menu mapOptionMenu = null;
    Dialog progressDialog;
    private DeliveryService deliveryService;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_main);
        RuntimePermission.checkPermissions(this);
        deliveryPresenter = new DeliveryPresenter(this);
        init();
        createTraversalFeatureList();
        MapFragmentPresenter mapFragmentPresenter = new MapFragmentPresenter();
        ProfilePresenter profileFragmentPresenter = new ProfilePresenter();
        HomeFragmentPresenter homeFragmentPresenter = new HomeFragmentPresenter(this);
        DashBoardFragmentPresenter dashBoardFragmentPresenter=new DashBoardFragmentPresenter();

        //        Hide the actionbar
        startService(new Intent(DeliveryMainActivity.this, SendLocationToServerService.class));
        configureNavigationPanel(dashBoardFragmentPresenter,mapFragmentPresenter, homeFragmentPresenter, profileFragmentPresenter);
        DeliveryServiceUiConnections();
        DeliveryButtonOperation();

        getUserLocation = new GetUserLocation(this, null, null, null, null, this::onLocationChange);
        if (getUserLocation != null) {
            locationL = getUserLocation.getUserCurrentLocation();
        }

    }

    public DeliveryPresenter getDeliveryPresenter() {
        return deliveryPresenter;
    }


    private void onLocationChange(Location location) {
        if (location != null) {
            locationL = location;
        }
    }


    private void init() {
        startDeliveryBtn = findViewById(R.id.start_delivery);
        pauseDeliveryBtn = findViewById(R.id.pause_delivery_button);
        deliverBtn = findViewById(R.id.deliver_btn);
        showNextBtn = findViewById(R.id.shownext_btn);
        buttonsLayoutRl = findViewById(R.id.buttonTabs);
        sliderContainer = findViewById(R.id.slide_button_container);

//        SlideButton slideButton = new SlideButton(DeliveryMainActivity.this);
//        slideButton.setThumb(ContextCompat.getDrawable(DeliveryMainActivity.this,R.drawable.marker_cluster));
//        slideButton.setText("Start Delivery");
//        slideButton.setBackgroundResource(R.drawable.back_slide_button);
//        sliderContainer.addView(slideButton);
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void showProgressDialog(String progressText) {
        DeliveryMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    progressDialog = new Dialog(DeliveryMainActivity.this);
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    progressDialog.setTitle(null);
                    progressDialog.setCancelable(false);
                    LayoutInflater inflater = DeliveryMainActivity.this.getLayoutInflater();
                    @SuppressLint("InflateParams") View content = inflater.inflate(R.layout.progress_dialog, null);
                    progressDialog.setContentView(content);
                    TextView progressTextView = (TextView) content.findViewById(R.id.progressText);
                    progressTextView.setText(progressText);
                    progressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void hideProgressDialog() {
        DeliveryMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (progressDialog != null) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void createTraversalFeatureList() {
        showProgressDialog("Fetching task");
        deliveryPresenter.createTraversalFeatureListAndStoreInDataModelAndUpdate(DeliveryMainActivity.this);
    }

    private void configureNavigationPanel(DashBoardFragmentPresenter dashBoardFragmentPresenter, MapFragmentPresenter mapFragmentPresenter, HomeFragmentPresenter homeFragmentPresenter, ProfilePresenter profileFragmentPresenter) {
        flFragment = findViewById(R.id.flFragment);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);


        Fragment dashboardFragment=new DashBoardFragment(DeliveryMainActivity.this,dashBoardFragmentPresenter);
        Fragment homeFragment = new HomeFragment(DeliveryMainActivity.this, homeFragmentPresenter, DeliveryMainActivity.this);
        Fragment mapFragment = new MapFragment(DeliveryMainActivity.this, mapFragmentPresenter);
        Fragment profileFragment = new ProfileFragment(DeliveryMainActivity.this, profileFragmentPresenter);
        activeFragment = dashboardFragment;

        getSupportFragmentManager().beginTransaction().add(R.id.flFragment, dashboardFragment, "dashboardfragmenttag").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.flFragment, homeFragment, "homefragmenttag").hide(homeFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.flFragment, mapFragment, "mapfragmenttag").hide(mapFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.flFragment, profileFragment, "profilefragmenttag").hide(profileFragment).commit();


        //    transition between pages after clicking on the button of the bottom navigation bar
        BottomNavigationView.OnNavigationItemSelectedListener navListner =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                        switch (item.getItemId()) {

                            case R.id.dashboard_d:
                                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(dashboardFragment).commit();
                                activeFragment = dashboardFragment;
                                FrameLayout.LayoutParams layoutParamsD = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                flFragment.setLayoutParams(layoutParamsD);
                                break;

                            case R.id.home_d:

                                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(homeFragment).commit();
                                activeFragment = homeFragment;
                                FrameLayout.LayoutParams layoutParamsH = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                flFragment.setLayoutParams(layoutParamsH);
                                break;


                            case R.id.map_d:
                                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(mapFragment).commit();
                                activeFragment = mapFragment;
                                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                flFragment.setLayoutParams(layoutParams);
                                break;


                            case R.id.profile_d:
                                getSupportFragmentManager().beginTransaction().hide(activeFragment).show(profileFragment).commit();
                                activeFragment = profileFragment;
                                break;

                        }
                        return true;
                    }
                };
        bottomNavigationView.setOnNavigationItemSelectedListener(navListner);
        bottomNavigationView.setSelectedItemId(R.id.dashboard_d);
    }


    private void DeliveryButtonOperation() {
        startDeliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showProgressDialog("Starting...");
                    if (getUserLocation != null && locationL == null) {
                        locationL = getUserLocation.getUserCurrentLocation();
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (deliveryPresenter.hasNextTraversingFeature()) {
                                Feature feature = deliveryPresenter.getCurrentlyTraversingFeature();
                                if (feature != null) {
                                    startDelivery(feature);
                                }
                            }


                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onStart();
                                }
                            });
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        pauseDeliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onStopDelivery();

            }
        });
        deliverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    deliveryPresenter.openInfoFillForm(DeliveryMainActivity.this, ModeUtility.SINGLE);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        showNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            deliveryPresenter.showNextDelivery(DeliveryMainActivity.this);
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startDelivery(Feature targetFeature) {
        try {
            deliveryPresenter.startDeliveryBtn(DeliveryMainActivity.this, locationL, targetFeature);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void onStopDelivery() {
        try {
            showProgressDialog("Stoping...");
            if (mBound) {
                unbindService(deliveryServiceConnection);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (locationL == null) {
            if (getUserLocation != null) {
                locationL = getUserLocation.getUserCurrentLocation();
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                deliveryPresenter.stopDelivery(DeliveryMainActivity.this, locationL);
            }
        }).start();

    }

    private void DeliveryServiceUiConnections() {
        try {
            deliveryServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    DeliveryService.LocalBinderD binder = (DeliveryService.LocalBinderD) iBinder;
                    deliveryService = binder.getService();


                    final androidx.lifecycle.Observer<Boolean> deliveryStateResult = new androidx.lifecycle.Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean deliveryState) {
                            DeliveryDataModel.getInstance().setDeliveryState(deliveryState);
                            if (deliveryState != null) {
                                try {
                                    configureUi(deliveryState);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                configureUi(false);
                            }
                            MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (deliveryState != null) {
                                if (fragment != null) {
                                    fragment.updateMapFragmentUIIfDeliveryIsOnGoing(deliveryState);
                                }
                            } else {
                                if (fragment != null) {
                                    fragment.updateMapFragmentUIIfDeliveryIsOnGoing(null);
                                }
                            }
                        }
                    };
                    deliveryService.getDeliveryOngoingStatus().observe(DeliveryMainActivity.this, deliveryStateResult);


                    final androidx.lifecycle.Observer<JSONObject> deliveryRouteResult = new androidx.lifecycle.Observer<JSONObject>() {
                        @Override
                        public void onChanged(JSONObject route) {
                            DeliveryDataModel.getInstance().setDeliveryRoute(route);
                            MapFragment fragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (route != null) {
                                if (fragment != null) {
                                    fragment.showRouteOnMap(route);
                                }
                            } else {
                                if (fragment != null) {
                                    fragment.removeDirectionFromMap();
                                }
                            }
                        }
                    };
                    deliveryService.getRouteJson().observe(DeliveryMainActivity.this, deliveryRouteResult);


                    final androidx.lifecycle.Observer<JSONObject> riderLocateResult = new androidx.lifecycle.Observer<JSONObject>() {
                        @Override
                        public void onChanged(JSONObject riderLocationState) {
                            if (riderLocationState != null) {
                                deliverBtnConfigration(riderLocationState);
                            } else {
                                deliverBtn.setVisibility(View.GONE);
                                showNextBtn.setVisibility(View.GONE);
                            }
                        }
                    };
                    deliveryService.getRiderLocationState().observe(DeliveryMainActivity.this, riderLocateResult);


                    final androidx.lifecycle.Observer<Feature> targetFeatureResult = new androidx.lifecycle.Observer<Feature>() {
                        @Override
                        public void onChanged(Feature targetFeatureData) {


                            if (targetFeatureData != null) {
                                DeliveryDataModel.getInstance().setTargetFeature(targetFeatureData);
                            } else {
                                DeliveryDataModel.getInstance().setTargetFeature(null);
                            }

                            MapFragment mapfragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (targetFeatureData != null) {
                                if (mapfragment != null) {
                                    mapfragment.showTargetTv(targetFeatureData);
                                    mapfragment.updateTargetFeatureUiOnMap(targetFeatureData);
                                }
                            } else {
                                if (mapfragment != null) {
                                    mapfragment.showTargetTv(null);
                                    mapfragment.updateTargetFeatureUiOnMap(null);
                                }
                            }

                            HomeFragment homefragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homefragmenttag");
                            if (targetFeatureData != null) {
                                if (homefragment != null) {
                                    homefragment.highlightTargetFeature(targetFeatureData);
                                }
                            } else {
                                if (homefragment != null) {
                                    homefragment.highlightTargetFeature(null);
                                }
                            }
                        }
                    };
                    deliveryService.getTargetFeatureLive().observe(DeliveryMainActivity.this, targetFeatureResult);


                    final androidx.lifecycle.Observer<List<Feature>> inRangeFeatureList = new androidx.lifecycle.Observer<List<Feature>>() {
                        @Override
                        public void onChanged(List<Feature> inRangeTargetList) {
                            DeliveryDataModel.getInstance().setInRangeFeature(inRangeTargetList);
                            MapFragment mapfragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (inRangeTargetList != null) {
                                if (mapfragment != null) {
                                    mapfragment.setSelectableFeature(inRangeTargetList);
                                }
                            } else {
                                if (mapfragment != null) {
                                    mapfragment.setSelectableFeature(null);
                                }
                            }

                            HomeFragment homefragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homefragmenttag");
                            if (inRangeTargetList != null) {
                                if (homefragment != null) {
                                    homefragment.performAutoSelectionFunctionalityInListView(DeliveryDataModel.getInstance().getInRangeFeature());
                                }
                            } else {
                                if (homefragment != null) {
                                    homefragment.performAutoSelectionFunctionalityInListView(new ArrayList<>());
                                }
                            }

                        }
                    };
                    deliveryService.getInRangeFeature().observe(DeliveryMainActivity.this, inRangeFeatureList);

                    final androidx.lifecycle.Observer<Geometry> bufferGeom = new androidx.lifecycle.Observer<Geometry>() {
                        @Override
                        public void onChanged(Geometry bufferGeometry) {
                            MapFragment mapfragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
                            if (bufferGeometry != null) {
                                if (mapfragment != null) {
                                    mapfragment.showBufferOnMap(bufferGeometry);
                                }
                            } else {
                                if (mapfragment != null) {

                                    mapfragment.removeExistingBufferOnMap();
                                }
                            }

                        }
                    };
                    deliveryService.getBufferGeom().observe(DeliveryMainActivity.this, bufferGeom);

                    mBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mBound = false;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverBtnConfigration(JSONObject riderLocationState) {


        if (riderLocationState.has("deliverobject")) {
            try {
                boolean isOrderDelivered = riderLocationState.getBoolean("deliverobject");
                boolean isReachTarget = riderLocationState.getBoolean("reachTarget");
                if (isReachTarget && !isOrderDelivered) {
                    deliverBtn.setVisibility(View.VISIBLE);
                    showNextBtn.setVisibility(View.GONE);
                } else if (isReachTarget && isOrderDelivered) {
                    deliverBtn.setVisibility(View.GONE);

                    if (deliveryPresenter.hasNextTraversingFeature()) {
                        showNextBtn.setVisibility(View.VISIBLE);
                    } else {
                        showNextBtn.setVisibility(View.GONE);
                    }
                } else if (!isReachTarget && !isOrderDelivered) {
                    deliverBtn.setVisibility(View.GONE);
                    showNextBtn.setVisibility(View.GONE);
                } else if (!isReachTarget && isOrderDelivered) {
                    deliverBtn.setVisibility(View.GONE);
                    if (DeliveryDataModel.getInstance().getTraversalEntity() != null) {
                        if (DeliveryDataModel.getInstance().getTraversalEntity().getTraversalGraph() != null) {
                            if (DeliveryDataModel.getInstance().getTraversalEntity().getTraversalGraph().getCurrentlyTraversingFeature() != null) {
                                showNextBtn.setVisibility(View.VISIBLE);
                            } else {
                                showNextBtn.setVisibility(View.GONE);
                            }
                        } else {
                            showNextBtn.setVisibility(View.GONE);
                        }
                    }
                }
            } catch (JSONException e) {
                deliverBtn.setVisibility(View.GONE);
                showNextBtn.setVisibility(View.GONE);
                e.printStackTrace();
            }
        } else {
            showNextBtn.setVisibility(View.GONE);
            deliverBtn.setVisibility(View.GONE);
        }
    }

    public void configureUi(Boolean deliveryState) {
        try {
            if (!deliveryState) {
                if (startDeliveryBtn != null) {
                    if (deliveryPresenter.hasNextTraversingFeature()) {
                        if (deliveryPresenter.getCurrentlyTraversingFeature() != null) {
                            startDeliveryBtn.setVisibility(View.VISIBLE);
                        } else {
                            startDeliveryBtn.setVisibility(View.GONE);
                        }
                    } else {
                        startDeliveryBtn.setVisibility(View.GONE);
                    }
                    ((View) startDeliveryBtn.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
                showHideStopItemInMenu(false);
            } else {
                if (startDeliveryBtn != null) {
                    startDeliveryBtn.setVisibility(View.GONE);
                    ((View) startDeliveryBtn.getParent()).setBackgroundColor(0);
                }
                showHideStopItemInMenu(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showHideStopItemInMenu(boolean b) {
        try {
            if (mapOptionMenu != null) {
                MenuItem item = mapOptionMenu.getItem(0);
                item.setVisible(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            mapOptionMenu = menu;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        try {
            if (item.getItemId() == R.id.stop_delivery) {
                onStopDelivery();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DeliveryService.isDeliveryServiceRunning((ActivityManager) getSystemService(ACTIVITY_SERVICE))) {
            mServiceConnected = bindService(new Intent(DeliveryMainActivity.this, DeliveryService.class), deliveryServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (mBound) {
                unbindService(deliveryServiceConnection);
                mBound = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTraversalDataFetchComplete() {
        try {
            hideProgressDialog();
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homefragmenttag");
            if (homeFragment != null) {
                homeFragment.notifyListData(-1);
            }
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
            if (mapFragment != null) {
                mapFragment.showDeliveryFeatureOnMap();
            }

            DashBoardFragment dashBoardFragment = (DashBoardFragment) getSupportFragmentManager().findFragmentByTag("dashboardfragmenttag");
            if (dashBoardFragment != null) {
                dashBoardFragment.updateDashBoard();
            }
            if (deliveryPresenter.hasNextTraversingFeature()) {
                if (deliveryPresenter.getCurrentlyTraversingFeature() != null) {
                    if (startDeliveryBtn != null) {
                        startDeliveryBtn.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (startDeliveryBtn != null) {
                        startDeliveryBtn.setVisibility(View.GONE);
                    }
                }
            } else {
                if (startDeliveryBtn != null) {
                    startDeliveryBtn.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void performDeliveryActionForFeature(Feature targetFeature, String mode) {
        FormSheetFragment bottomSheetDialog = new FormSheetFragment(DeliveryMainActivity.this, DeliveryMainActivity.this, targetFeature, mode);
        bottomSheetDialog.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
    }

    @Override
    public void showProductsUpdateDialogForTargetFeature(Feature targetFeature) {
        ProductSheetFragment productSheetFragment = new ProductSheetFragment(DeliveryMainActivity.this, targetFeature);
        productSheetFragment.show(getSupportFragmentManager(), "Product Sheet Dialog Fragment");
    }


    @Override
    public void onTargetFeatureUpdated(String mode, int position) {
//        JSONObject isUpdatedFeatureIsLastInTraversal=DeliveryDataModel.getInstance().getTraversalEntity().isLastInTraversal(String.valueOf(DeliveryDataModel.getInstance().getTargetFeature().getFeatureId()), DeliveryMainActivity.this);
//        try {
//            if(isUpdatedFeatureIsLastInTraversal != null &&
//                    isUpdatedFeatureIsLastInTraversal.has("status") &&
//                    isUpdatedFeatureIsLastInTraversal.getString("status").equalsIgnoreCase("success") &&
//                    isUpdatedFeatureIsLastInTraversal.has("message") &&
//                    isUpdatedFeatureIsLastInTraversal.getBoolean("message")
//            ) {
//                deliveryPresenter.markAsDeliver(DeliveryMainActivity.this, deliverBtn.getTag());
//                pauseDeliveryBtn.callOnClick();
//                startDeliveryBtn.setVisibility(View.GONE);
//
//            }else{
//
//            }

//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


        deliveryPresenter.updateTraversalFeatureList(DeliveryMainActivity.this,position);
        deliveryPresenter.markAsDeliver(DeliveryMainActivity.this, mode);
        if (mode.equals(ModeUtility.MULTI)) {
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
            if (mapFragment != null) {
                mapFragment.deSelectTheSelectingFeature();
            }
        }
    }

    @Override
    public void updateHomeAndMapUI(int position) {
        try {
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homefragmenttag");
            if (homeFragment != null) {
                homeFragment.notifyListData(position);
            }
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
            if (mapFragment != null) {
                mapFragment.showDeliveryFeatureOnMap();
            }
            DashBoardFragment dashBoardFragment = (DashBoardFragment) getSupportFragmentManager().findFragmentByTag("dashboardfragmenttag");
            if (dashBoardFragment != null) {
                dashBoardFragment.updateDashBoard();
            }
            hideProgressDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showTripItemSelectionDialog(ITripCallback iTripCallback) {
        hideProgressDialog();
        DeliveryMainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TripItemsForm tripItemsForm = new TripItemsForm(DeliveryMainActivity.this, getUserLocation.getUserCurrentLocation(), iTripCallback, DeliveryMainActivity.this);
                tripItemsForm.show(getSupportFragmentManager(), "trip items form view");
            }
        });
    }


    public void showHideButtonTabLayout(boolean show) {
        if (buttonsLayoutRl != null) {
            if (!show) {
                buttonsLayoutRl.setVisibility(View.GONE);
            } else {
                buttonsLayoutRl.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onOkInfoBottomSheetResult(int requestCode, int errorCode, String jurisdictions) {

    }

    @Override
    public void onCancelOkBottomSheetResult(int requestCode, int errorCode) {

    }


    @Override
    public void onBackPressed() {
//        boolean isDeselectionDone = false;
        boolean isDeselectionDone = true;
//        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("mapfragmenttag");
//        if (mapFragment != null) {
//            if (mapFragment.isVisible()) {
//                isDeselectionDone = mapFragment.deSelectTheSelectingFeature();
//            }
//        }
        if (!isDeselectionDone) {
            if (getFragmentManager().getBackStackEntryCount() != 0) {
                getFragmentManager().popBackStack();

            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DeliveryDataModel.getInstance().clearAll();
    }
}