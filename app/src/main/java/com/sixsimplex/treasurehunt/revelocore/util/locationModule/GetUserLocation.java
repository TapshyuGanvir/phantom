package com.sixsimplex.treasurehunt.revelocore.util.locationModule;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class GetUserLocation implements LocationProvider.GetLocationListener, IMyLocationConsumer {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Context context;
    private MapView mapView;
    private MyLocationNewOverlay mLocationOverlay;
    private ImageView locationIndicatorIV;
    private Location userLocation = null;
    private TextView userLatLongStatusTv;
    private LocationProvider locationProvider;
    private LocationReceiverInterface locationReceiverInterface;

    private String className = "GetUserLocation";
    private Spinner optionalSpinner;
    Handler mHandler = new Handler(Looper.getMainLooper());

    public GetUserLocation(Context context, MapView mapView, ImageView locationIndicatorIV,
                           TextView userLatLongStatusTv, Spinner optionalSpinner,
                           LocationReceiverInterface locationReceiverInterface) {
        this.context = context;
        this.mapView = mapView;
        this.userLatLongStatusTv = userLatLongStatusTv;
        this.locationIndicatorIV = locationIndicatorIV;
        this.locationReceiverInterface=locationReceiverInterface;
        this.optionalSpinner = optionalSpinner;
        getUserLocation();

    }

    private void getUserLocation() {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        locationProvider = createLocationProvider();

        if(mapView!=null) {
            mLocationOverlay = new MyLocationNewOverlay(locationProvider, mapView);
            mLocationOverlay.setDirectionArrow(((BitmapDrawable)mapView.getContext().getResources().getDrawable(R.drawable.currentlocicon1)).getBitmap(),
                    ((BitmapDrawable)mapView.getContext().getResources().getDrawable(R.drawable.gdirectionmarker)).getBitmap());
            mLocationOverlay.setPersonHotspot(45,45);
            mLocationOverlay.enableMyLocation();
            mLocationOverlay.enableFollowLocation();
            //mLocationOverlay.setPersonIcon();
            mapView.getOverlays().add(mLocationOverlay);


//            mLocationOverlay = new MyLocationNewOverlay(locationProvider, mapView);
//            mLocationOverlay.enableMyLocation();
//            mLocationOverlay.enableFollowLocation();
//            //mLocationOverlay.setPersonIcon();
//            mapView.getOverlays().add(mLocationOverlay);
        }

            userLocation = locationProvider.getLastKnownLocation();
            locationProvider.startLocationProvider(this);

    }
    public void addMyLocationLayerOverlay(){
        mapView.getOverlays().add(mLocationOverlay);
    }

    public boolean checkLocationIsEnable() {
        return locationProvider.checkLocationIsEnable();
    }
    public void stopLocationUpdates() {
         locationProvider.stopLocationProvider();
    }
    private LocationProvider createLocationProvider() {
        return new LocationProvider(context, this);
    }

    private void tryForLocation() {
        LocationProvider locationProvider = createLocationProvider();
        if (mLocationOverlay != null) {
            mLocationOverlay.enableMyLocation(locationProvider);
        }
    }

    public void zoomLocation(Double zoomLevel) {
//        if (userLocation != null && mapView!=null) {
//            if(zoomLevel<=0 ||mapView.getZoomLevelDouble()>18){
//                zoomLevel  =19.50;
//            }
//            GeoPoint locationGeoPoint = new GeoPoint(userLocation);
//          //  mapView.getController().animateTo(locationGeoPoint, 16.45, null);
//            mapView.getOverlayManager().remove(mLocationOverlay);
//            mapView.getOverlayManager().add(mLocationOverlay);
//            mapView.getController().animateTo(locationGeoPoint, zoomLevel, null);
//        } else {
//            tryForLocation();
//        }


        if (userLocation != null) {
            GeoPoint locationGeoPoint = new GeoPoint(userLocation);
            if (mapView != null) {
                mapView.getController().animateTo(locationGeoPoint, 16.45, null);
            }

        } else {
            tryForLocation();
        }
    }

    public Location getUserCurrentLocation() {
        if (userLocation != null) {
            return userLocation;
        } else {
            tryForLocation();
        }
        return null;
    }

    private void locationChange(Location location) {
        try {
            if (location != null) {
                userLocation = location;
                if(mLocationOverlay!=null) {
                    mLocationOverlay.onLocationChanged(location, locationProvider);
                }
                if(locationIndicatorIV!=null) {
                    if (location.getAccuracy() < 15.0f) {
                        if(mapView!=null){
                            mapView.invalidate();
                        }
                        locationIndicatorIV.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_location_green));
                    } else {
                        locationIndicatorIV.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_location_red));
                    }
                }
                    if(mapView!=null){
                        mapView.invalidate();
                    }

            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "locationChange", String.valueOf(e.getCause()));
        }
    }

    public void showUserCurrentLatLong() {
        if (userLocation != null) {
            if (userLatLongStatusTv != null && userLatLongStatusTv.getVisibility() != View.VISIBLE && optionalSpinner != null) {
                userLatLongStatusTv.setVisibility(View.VISIBLE);
                String builder = "Lat : " +
                        userLocation.getLatitude() +
                        " Lon : " +
                        userLocation.getLongitude() +
                        " Acc : " +
                        userLocation.getAccuracy() +
                        " m";
                userLatLongStatusTv.setText(builder);
            }

            if (userLatLongStatusTv != null && optionalSpinner != null) {
            try {
                Thread background = new Thread() {
                    public void run() {
                        try {
                            sleep(10000);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    userLatLongStatusTv.setVisibility(View.GONE);
                                }
                            });
//                            context.runOnUiThread(() -> userLatLongStatusTv.setVisibility(View.GONE));
//                            context.runOnUiThread(()-> {
//                                if(updateUIInterface != null){
//                                    updateUIInterface.updateSpinner(MapActivity.SPINNER_INFO_POS_CHECK);
//                                }
//                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            ReveloLogger.error(className, "showUserCurrentLatLong", String.valueOf(e.getCause()));
                        }
                    }
                };
                background.start();
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "showUserCurrentLatLong", String.valueOf(e.getCause()));
            }
        }
        } else {
            if(userLatLongStatusTv!=null)
            userLatLongStatusTv.setVisibility(View.GONE);

        }
    }


    @Override
    public void onLocationChange(Location location) {
        locationChange(location);
        if(locationReceiverInterface!=null) {
            locationReceiverInterface.onLocationChange(location);
        }
    }

    @Override
    public void onLocationChanged(Location location, IMyLocationProvider source) {
        onLocationChange(location);
    }


}