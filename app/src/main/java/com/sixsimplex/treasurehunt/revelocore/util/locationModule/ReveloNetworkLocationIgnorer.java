package com.sixsimplex.treasurehunt.revelocore.util.locationModule;

import android.location.LocationManager;

public class ReveloNetworkLocationIgnorer {

    //current provider
    String pProvider = LocationManager.GPS_PROVIDER;

    /** last time we got a location from the gps provider */
    private long mLastGps = 0;

    private double mAccuracyGps = 0;
    private double mAccuracyNetwork = 0;
    private double mAccuracyFused = 0;

    public ReveloNetworkLocationIgnorer(){

    }
    /*
    * Whether we should ignore this location.
Params:
pProvider – the provider that provided the location
pTime – the time of the location
Returns:
true if we should ignore this location, false if not*/
    public boolean shouldIgnore(final String pProvider, final long pTime, final double accuracy) {

        if (LocationManager.GPS_PROVIDER.equals(pProvider)) {
            mLastGps = pTime;
            mAccuracyGps=accuracy;
        } else {
            if (pTime < mLastGps + 3000 && accuracy>mAccuracyGps) {
                //wait for 3 secs for gps location to arrive.
                // if we have awaited more than 3 secs, we accept the non-gps location
                //else, ignore location = true
                return true;
            }
        }

        return false;
    }
}
