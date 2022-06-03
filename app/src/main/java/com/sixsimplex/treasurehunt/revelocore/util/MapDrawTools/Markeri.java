package com.sixsimplex.treasurehunt.revelocore.util.MapDrawTools;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

public class Markeri {

    private boolean isMid;
    private GeoPoint latLng;
    private int index;
    private Marker marker;

    public Markeri() {
    }

    public Markeri(boolean isMid, GeoPoint latLng, int index, Marker marker) {
        this.isMid = isMid;
        this.latLng = latLng;
        this.index = index;
        this.marker = marker;
    }

    public boolean isMid() {
        return isMid;
    }

    public void setMid(boolean mid) {
        isMid = mid;
    }

    public GeoPoint getLatLng() {
        return latLng;
    }

    public void setLatLng(GeoPoint latLng) {
        this.latLng = latLng;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
