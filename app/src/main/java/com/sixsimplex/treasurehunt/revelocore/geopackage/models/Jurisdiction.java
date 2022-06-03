package com.sixsimplex.treasurehunt.revelocore.geopackage.models;

import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.sf.Geometry;

public class Jurisdiction {

    private String name;
    private String type;
    private Geometry geometry;
    private GeoPackageGeometryData geoPackageGeometryData;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public GeoPackageGeometryData getGeoPackageGeometryData() {
        return geoPackageGeometryData;
    }

    public void setGeoPackageGeometryData(GeoPackageGeometryData geoPackageGeometryData) {
        this.geoPackageGeometryData = geoPackageGeometryData;
    }
}
