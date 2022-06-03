package com.sixsimplex.treasurehunt.Phantom1.utils;

import android.graphics.Color;
import android.location.Location;

import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static Polyline toOSMPolyline(JSONObject responseDirectionJson) {
        Polyline polyline = null;
        try {
            if (responseDirectionJson.has("features")) {
                JSONArray features = responseDirectionJson.getJSONArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JSONObject featureJson = features.getJSONObject(i);
                    if (featureJson.has("geometry")) {
                        JSONObject geometryJson = featureJson.getJSONObject("geometry");
                        if (geometryJson.has("coordinates")) {
                            JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                            polyline = new Polyline();
                            List<GeoPoint> polylinePoints = new ArrayList<>();
                            for (int l = 0; l < outerCoordinatesJArray.length(); l++) {
                                JSONArray pointArray = outerCoordinatesJArray.getJSONArray(l);
                                GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                polylinePoints.add(geoPoint);
                            }
                            polyline.setPoints(polylinePoints);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return polyline;
    }

    public static Polyline toStartLine(JSONObject route, Location startLocation) {
        Polyline polyline = null;
        try {
            if (route.has("features")) {
                JSONArray features = route.getJSONArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JSONObject featureJson = features.getJSONObject(i);
                    if (featureJson.has("geometry")) {
                        JSONObject geometryJson = featureJson.getJSONObject("geometry");
                        if (geometryJson.has("coordinates")) {
                            JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                            polyline = new Polyline();
                            List<GeoPoint> polylinePoints = new ArrayList<>();
                            GeoPoint startPoint = new GeoPoint(startLocation.getLatitude(), startLocation.getLongitude());
                            polylinePoints.add(startPoint);
                            JSONArray pointArray = outerCoordinatesJArray.getJSONArray(0);
                            if (pointArray != null) {
                                GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                polylinePoints.add(geoPoint);
                            }
                            polyline.setPoints(polylinePoints);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return polyline;
    }

    public static Polyline toEndLine(JSONObject route, Location endLocation) {
        Polyline polyline = null;
        try {
            if (route.has("features")) {
                JSONArray features = route.getJSONArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JSONObject featureJson = features.getJSONObject(i);
                    if (featureJson.has("geometry")) {
                        JSONObject geometryJson = featureJson.getJSONObject("geometry");
                        if (geometryJson.has("coordinates")) {
                            JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                            polyline = new Polyline();
                            List<GeoPoint> polylinePoints = new ArrayList<>();
                            JSONArray pointArray = outerCoordinatesJArray.getJSONArray(outerCoordinatesJArray.length()-1);
                            if (pointArray != null) {
                                GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                polylinePoints.add(geoPoint);
                            }
                            GeoPoint startPoint = new GeoPoint(endLocation.getLatitude(), endLocation.getLongitude());
                            polylinePoints.add(startPoint);
                            polyline.setPoints(polylinePoints);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return polyline;
    }

    public static Geometry createBuffer(double latitude, double longitude, final double distanceinMeters) {
        GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
        shapeFactory.setCentre(new Coordinate(longitude, latitude));
//        shapeFactory.setSize(distanceinMeters * 2);
        double size = (distanceinMeters * 0.00001) / 1.11;
        //double size= (distanceinMeters);
        shapeFactory.setSize(size*2);
        return shapeFactory.createCircle();
    }


    public static List<Polygon> convertGeometry(Geometry geometry) {

        List<Polygon> polygonList = new ArrayList<>();

        if (geometry instanceof com.vividsolutions.jts.geom.Polygon) {
            com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon) geometry;
            toPolygon(polygonList, polygon);

        } else if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            int geometryNo = multiPolygon.getNumGeometries();

            for (int i = 0; i < geometryNo; i++) {
                Geometry polyGeom = multiPolygon.getGeometryN(i);
                if (polyGeom instanceof MultiPolygon) {
                    convertGeometry(polyGeom);
                } else if (polyGeom instanceof com.vividsolutions.jts.geom.Polygon) {
                    com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon) geometry;
                    toPolygon(polygonList, polygon);
                }
            }
        }

        return polygonList;
    }



    public static void toPolygon(List<Polygon> polygonList, com.vividsolutions.jts.geom.Polygon polygon) {

        Polygon polygonOverlay = new Polygon();

        List<GeoPoint> pts = new ArrayList<>();

        LineString exteriorRing = polygon.getExteriorRing();
        for (Coordinate point : exteriorRing.getCoordinates()) {
            GeoPoint latLng = new GeoPoint(point.y, point.x);
            pts.add(latLng);
        }

        List<List<GeoPoint>> holes = new ArrayList<>();
        int geometryNo = polygon.getNumInteriorRing();

        for (int i = 0; i < geometryNo; i++) {
            LineString lineString = polygon.getInteriorRingN(i);
            List<GeoPoint> holeLatLngs = new ArrayList<>();
            for (Coordinate point : lineString.getCoordinates()) {
                GeoPoint latLng = new GeoPoint(point.y, point.x);
                holeLatLngs.add(latLng);
            }
            holes.add(holeLatLngs);
        }

        polygonOverlay.setPoints(pts);
        polygonOverlay.setHoles(holes);
        polygonOverlay.setVisible(true);
//        polygonOverlay.getFillPaint().setColor(Color.parseColor("#4442c6f5"));
        polygonOverlay.getFillPaint().setColor(Color.TRANSPARENT);
        polygonOverlay.getOutlinePaint().setColor(Color.parseColor("#F95F2F"));
        polygonOverlay.getOutlinePaint().setStrokeWidth(5);


        polygonList.add(polygonOverlay);
    }

    public static boolean isFeatureListEqual(List<Feature> cacheInRangeFeature, List<Feature> inRangeFeatureList) {
        boolean isEqual=false;
        try {
            for(Feature cacheFeature:cacheInRangeFeature){
                for(Feature feature:inRangeFeatureList){
                    if(cacheFeature.getFeatureId().equals(feature.getFeatureId())){
                        isEqual =true;
                        break;
                    }else{
                        isEqual=false;
                    }
                }
                if(!isEqual){
                    return isEqual;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isEqual;
    }

    public static boolean isFeaturePresentInList(List<Feature> inRangeFeature, Feature featureInDropOff) {
        boolean isPresent=false;
        try {
            for(Feature feature:inRangeFeature){
                if(feature.getFeatureId().equals(featureInDropOff.getFeatureId())){
                    isPresent= true;
                    break;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return isPresent;
    }
}
