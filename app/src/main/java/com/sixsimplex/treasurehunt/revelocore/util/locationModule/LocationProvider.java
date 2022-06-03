package com.sixsimplex.treasurehunt.revelocore.util.locationModule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.HashSet;
import java.util.Set;

public class LocationProvider implements IMyLocationProvider, LocationListener {

    private LocationManager mLocationManager;
    private Location mLocation;

    private IMyLocationConsumer mMyLocationConsumer;
    private long mLocationUpdateMinTime = 0;
    private float mLocationUpdateMinDistance = 5.0f;
    private NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();
    private final Set<String> locationSources = new HashSet<>();
    private Context context;
    private GetLocationListener getLocationListener;

    public LocationProvider(Context context, GetLocationListener getLocationListner) {
        this.context = context;
        this.getLocationListener = getLocationListner;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationSources.add(LocationManager.GPS_PROVIDER);
        locationSources.add(LocationManager.NETWORK_PROVIDER);
    }

    public boolean checkLocationIsEnable() {

        boolean isGpsOn = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkOn = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return isGpsOn && isNetworkOn;
    }

    private boolean sendExtraCommand(String command, Bundle bundle) {
        return mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER, command, bundle);
    }

    private boolean sendExtraCommandNetworkProvider(String command, Bundle bundle) {
        return mLocationManager.sendExtraCommand(LocationManager.NETWORK_PROVIDER, command, bundle);
    }

    public void clearLocationSources() {
        locationSources.clear();
    }

    public void addLocationSource(String source) {
        locationSources.add(source);
    }

    public Set<String> getLocationSources() {
        return locationSources;
    }

    public long getLocationUpdateMinTime() {
        return mLocationUpdateMinTime;
    }

    public void setLocationUpdateMinTime(final long milliSeconds) {
        mLocationUpdateMinTime = milliSeconds;
    }

    public float getLocationUpdateMinDistance() {
        return mLocationUpdateMinDistance;
    }

    public void setLocationUpdateMinDistance(final float meters) {
        mLocationUpdateMinDistance = meters;
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        mMyLocationConsumer = myLocationConsumer;
        boolean result = false;
        for (final String provider : mLocationManager.getProviders(true)) {
            if (locationSources.contains(provider)) {
                try {
                    if (!sendExtraCommand("force_time_injection", null)) ;
                    if (!sendExtraCommandNetworkProvider("force_time_injection", null)) ;

                    mLocationManager.requestLocationUpdates(provider, mLocationUpdateMinTime, mLocationUpdateMinDistance, this);
                    result = true;
                } catch (Throwable ex) {
                    Log.e(IMapView.LOGTAG, "Unable to attach listener for location provider " + provider + " check permissions?", ex);
                }
            }
        }
        return result;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void stopLocationProvider() {
        mMyLocationConsumer = null;
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(this);
            } catch (Throwable ex) {
                Log.w(IMapView.LOGTAG, "Unable to deattach location listener", ex);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public Location getLastKnownLocation() {
        if(mLocation==null)
             mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(mLocation==null)
             mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(mLocation==null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mLocation = mLocationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
            }


        return mLocation;
    }

    @Override
    public void destroy() {
        stopLocationProvider();
        mLocation = null;
        mLocationManager = null;
        mMyLocationConsumer = null;
        mIgnorer = null;
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (mIgnorer == null) {
            Log.w(IMapView.LOGTAG, "GpsMyLocation provider, mIgnore is null, unexpected. Location update will be ignored");
            return;
        }
        if (location == null || location.getProvider() == null)
            return;
        // ignore temporary non-gps fix
        if (mIgnorer.shouldIgnore(location.getProvider(), System.currentTimeMillis()))
            return;

        mLocation = location;
        if (mMyLocationConsumer != null && mLocation != null) {
            mMyLocationConsumer.onLocationChanged(mLocation, this);
            getLocationListener.onLocationChange(location);
        }
    }

    @Override
    public void onProviderDisabled(final String provider) {
    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
    }

    public interface GetLocationListener {
        void onLocationChange(Location location);
    }
}