package com.sixsimplex.treasurehunt.revelocore.util.MapDrawTools;

import org.osmdroid.util.GeoPoint;

public class GgeoPoint extends GeoPoint {

    private boolean isMid;

    public GgeoPoint(GeoPoint aGeopoint) {
        super(aGeopoint);
    }

    public void setMid(boolean isMid) {
        this.isMid = isMid;
    }

    public boolean isMid() {
        return isMid;
    }

}
