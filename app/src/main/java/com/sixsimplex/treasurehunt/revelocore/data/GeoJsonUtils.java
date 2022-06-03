package com.sixsimplex.treasurehunt.revelocore.data;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class GeoJsonUtils {

    //todo CHECK
    public static String getGeometryType(JSONObject geoJson){
        try {
            if (geoJson.has("features")) {
                JSONArray featuresArray = geoJson.getJSONArray("features");
                if(featuresArray.length()==0)
                    return null;
                JSONObject featureJson = featuresArray.getJSONObject(0);
                if(featureJson.has("geometry")){
                    return featureJson.getJSONObject("geometry").getString("type");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //todo CHECK
    public static List<Polygon> toOSMPolygons(JSONObject geometryGeoJson) {
        List<Polygon> polygons = new ArrayList<>();
        try {
            if(geometryGeoJson.has("features")){
                JSONArray features = geometryGeoJson.getJSONArray("features");
                for(int i=0;i<features.length();i++){
                    JSONObject featureJson = features.getJSONObject(i);
                    if(featureJson.has("geometry")){
                        JSONObject geometryJson = featureJson.getJSONObject("geometry");
                        if(geometryJson.has("coordinates")){
                            JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                           for(int j=0;j<outerCoordinatesJArray.length();j++){
                               JSONArray polygonArray = outerCoordinatesJArray.getJSONArray(j);
                               Polygon polygon = new Polygon();
                               List<GeoPoint> polygonPoints = new ArrayList<>();
                               List<List<GeoPoint>> polygonHoles = new ArrayList<>();
                               for(int k=0;k<polygonArray.length();k++){
                                   JSONArray polygonOrHoleArray = polygonArray.getJSONArray(k);
                                   if(k == 0){
                                       for(int l = 0;l<polygonOrHoleArray.length();l++){
                                           JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                           GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1),pointArray.getDouble(0));
                                           polygonPoints.add(geoPoint);
                                       }
                                   }else {
                                       List<GeoPoint> holePoints = new ArrayList<>();
                                       for(int l = 0;l<polygonOrHoleArray.length();l++){
                                           JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                           GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1),pointArray.getDouble(0));
                                           holePoints.add(geoPoint);
                                       }
                                       polygonHoles.add(holePoints);
                                   }
                               }
                               polygon.setHoles(polygonHoles);
                               polygon.setPoints(polygonPoints);
                               polygons.add(polygon);
                           }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return polygons;
    }

    //todo CHECK
    public static Polygon toOSMPolygon(JSONObject geometryGeoJson) {
        try {
            if (geometryGeoJson.has("features")) {
                JSONArray features = geometryGeoJson.getJSONArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JSONObject featureJson = features.getJSONObject(i);
                    if (featureJson.has("geometry")) {
                        JSONObject geometryJson = featureJson.getJSONObject("geometry");
                        if (geometryJson.has("coordinates")) {
                            JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                            Polygon polygon = new Polygon();
                            List<GeoPoint> polygonPoints = new ArrayList<>();
                            List<List<GeoPoint>> polygonHoles = new ArrayList<>();
                            for (int k = 0; k < outerCoordinatesJArray.length(); k++) {
                                JSONArray polygonOrHoleArray = outerCoordinatesJArray.getJSONArray(k);
                                if (k == 0) {
                                    for (int l = 0; l < polygonOrHoleArray.length(); l++) {
                                        JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                        GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                        polygonPoints.add(geoPoint);
                                    }
                                } else {
                                    List<GeoPoint> holePoints = new ArrayList<>();
                                    for (int l = 0; l < polygonOrHoleArray.length(); l++) {
                                        JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                        GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                        holePoints.add(geoPoint);
                                    }
                                    polygonHoles.add(holePoints);
                                }
                            }
                            polygon.setHoles(polygonHoles);
                            polygon.setPoints(polygonPoints);

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //todo CHECK
    public static List<Polyline> toOSMPolylines(JSONObject geometryGeoJson) {
        List<Polyline> polylines = new ArrayList<>();
        try {
            if(geometryGeoJson.has("features")){
                JSONArray features = geometryGeoJson.getJSONArray("features");
                for(int i=0;i<features.length();i++){
                    JSONObject featureJson = features.getJSONObject(i);
                    if(featureJson.has("geometry")){
                        JSONObject geometryJson = featureJson.getJSONObject("geometry");
                        if(geometryJson.has("coordinates")){
                            JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                            for (int j = 0; j < outerCoordinatesJArray.length(); j++) {
                                JSONArray polylineArray = outerCoordinatesJArray.getJSONArray(j);
                                Polyline polyline = new Polyline();
                                List<GeoPoint> polylinePoints = new ArrayList<>();
                                for (int l = 0; l < polylineArray.length(); l++) {
                                    JSONArray pointArray = polylineArray.getJSONArray(l);
                                    GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                    polylinePoints.add(geoPoint);
                                }
                                polyline.setPoints(polylinePoints);
                                polylines.add(polyline);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return polylines;
    }

    //todo CHECK
    public static Polyline toOSMPolyline(JSONObject geometryGeoJson) {
        Polyline polyline = null;
        try {
            if (geometryGeoJson.has("features")) {
                JSONArray features = geometryGeoJson.getJSONArray("features");
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

    //todo CHECK
    public static GeoPoint toOSMPoint(JSONObject geometryGeoJson) {
        try {
            if (geometryGeoJson.has("features")) {
                JSONArray features = geometryGeoJson.getJSONArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JSONObject featureJson = features.getJSONObject(i);
                    if (featureJson.has("geometry")) {
                        JSONObject geometryJson = featureJson.getJSONObject("geometry");
                        if (geometryJson.has("coordinates")) {
                            JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                            GeoPoint geoPoint = new GeoPoint(outerCoordinatesJArray.getDouble(1), outerCoordinatesJArray.getDouble(0));
                           return geoPoint;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<List<GeoPoint>> convertToOSMGeoPointsLists(JSONObject geometryGeoJson) {
        List<List<GeoPoint>> geoPointsList = new ArrayList<>();

        try {
            if(geometryGeoJson.has("features")){
                JSONArray features = geometryGeoJson.getJSONArray("features");
                for(int i=0;i<features.length();i++){
                    JSONObject featureJson = features.getJSONObject(i);
                    if(featureJson.has("geometry")){

                        JSONObject geometryJson = featureJson.getJSONObject("geometry");
                        String geometryType = geometryJson.getString("type");
                        if(geometryType.equalsIgnoreCase("point")){
                            if (geometryJson.has("coordinates")) {
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                                GeoPoint geoPoint = new GeoPoint(outerCoordinatesJArray.getDouble(1), outerCoordinatesJArray.getDouble(0));
                                List<GeoPoint> list1 = new ArrayList<>();
                                list1.add(geoPoint);
                                geoPointsList.add(list1);
                            }
                        }else if(geometryType.equalsIgnoreCase("linestring")){
                            if (geometryJson.has("coordinates")) {
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                                List<GeoPoint> polylinePoints = new ArrayList<>();
                                for (int l = 0; l < outerCoordinatesJArray.length(); l++) {
                                    JSONArray pointArray = outerCoordinatesJArray.getJSONArray(l);
                                    GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                    polylinePoints.add(geoPoint);
                                }
                                geoPointsList.add(polylinePoints);
                            }
                        }else if(geometryType.equalsIgnoreCase("multilinestring")
                        ||geometryType.equalsIgnoreCase("polyline")){
                            if(geometryJson.has("coordinates")){
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                                for (int j = 0; j < outerCoordinatesJArray.length(); j++) {
                                    JSONArray polylineArray = outerCoordinatesJArray.getJSONArray(j);
                                    List<GeoPoint> polylinePoints = new ArrayList<>();
                                    for (int l = 0; l < polylineArray.length(); l++) {
                                        JSONArray pointArray = polylineArray.getJSONArray(l);
                                        GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                        polylinePoints.add(geoPoint);
                                    }
                                   geoPointsList.add(polylinePoints);
                                }
                            }
                        }else if(geometryType.equalsIgnoreCase("polygon")){
                            if (geometryJson.has("coordinates")) {
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");

                                List<GeoPoint> polygonPoints = new ArrayList<>();
                                List<List<GeoPoint>> polygonHoles = new ArrayList<>();
                                for (int k = 0; k < outerCoordinatesJArray.length(); k++) {
                                    JSONArray polygonOrHoleArray = outerCoordinatesJArray.getJSONArray(k);
                                    if (k == 0) {
                                        for (int l = 0; l < polygonOrHoleArray.length(); l++) {
                                            JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                            GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                            polygonPoints.add(geoPoint);
                                        }
                                    } else {
                                        List<GeoPoint> holePoints = new ArrayList<>();
                                        for (int l = 0; l < polygonOrHoleArray.length(); l++) {
                                            JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                            GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1), pointArray.getDouble(0));
                                            holePoints.add(geoPoint);
                                        }
                                        polygonHoles.add(holePoints);
                                    }
                                }
                                geoPointsList.add(polygonPoints);

                            }
                        }else if(geometryType.equalsIgnoreCase("multipolygon")){
                            if(geometryJson.has("coordinates")){
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                                for(int j=0;j<outerCoordinatesJArray.length();j++){
                                    JSONArray polygonArray = outerCoordinatesJArray.getJSONArray(j);
                                    List<GeoPoint> polygonPoints = new ArrayList<>();
                                    List<List<GeoPoint>> polygonHoles = new ArrayList<>();
                                    for(int k=0;k<polygonArray.length();k++){
                                        JSONArray polygonOrHoleArray = polygonArray.getJSONArray(k);
                                        if(k == 0){
                                            for(int l = 0;l<polygonOrHoleArray.length();l++){
                                                JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                                GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1),pointArray.getDouble(0));
                                                polygonPoints.add(geoPoint);
                                            }
                                        }else {
                                            List<GeoPoint> holePoints = new ArrayList<>();
                                            for(int l = 0;l<polygonOrHoleArray.length();l++){
                                                JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                                GeoPoint geoPoint = new GeoPoint(pointArray.getDouble(1),pointArray.getDouble(0));
                                                holePoints.add(geoPoint);
                                            }
                                            polygonHoles.add(holePoints);
                                        }
                                    }
                                   geoPointsList.add(polygonPoints);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return geoPointsList;
    }
    
    public static Geometry convertToJTSGeometry(JSONObject geometryGeoJson){
       Geometry geometry = null;

        try {
            GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
            if(geometryGeoJson.has("features")){
                JSONArray features = geometryGeoJson.getJSONArray("features");
                for(int i=0;i<features.length();i++){
                    JSONObject featureJson = features.getJSONObject(i);
                    if(featureJson.has("geometry")){
                        JSONObject geometryJson = featureJson.getJSONObject("geometry");
                        String geometryType = geometryJson.getString("type");
                        if(geometryType.equalsIgnoreCase("point")){
                            if (geometryJson.has("coordinates")) {
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                                geometry = factory.createPoint(new Coordinate(outerCoordinatesJArray.getDouble(0), outerCoordinatesJArray.getDouble(1)));//TODO  CHECK
                            }
                        }
                        else if(geometryType.equalsIgnoreCase("linestring")){
                            if (geometryJson.has("coordinates")) {
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                                Coordinate[] polylineCoordArr = new Coordinate[outerCoordinatesJArray.length()];
                                for (int l = 0; l < outerCoordinatesJArray.length(); l++) {
                                    JSONArray pointArray = outerCoordinatesJArray.getJSONArray(l);
                                    Coordinate coordinate = new Coordinate(pointArray.getDouble(0), pointArray.getDouble(1));//todo CHECK
                                    polylineCoordArr[l]=coordinate;
                                }
                                geometry = factory.createLineString(polylineCoordArr);
                            }
                        }
                        else if(geometryType.equalsIgnoreCase("multilinestring")
                                ||geometryType.equalsIgnoreCase("polyline")){
                            if(geometryJson.has("coordinates")){
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                                LineString[] lineStringsArr = new LineString[outerCoordinatesJArray.length()];
                                for (int j = 0; j < outerCoordinatesJArray.length(); j++) {
                                    JSONArray polylineArray = outerCoordinatesJArray.getJSONArray(j);
                                    Coordinate[] polylineCoordArr = new Coordinate[polylineArray.length()];
                                    for (int l = 0; l < polylineArray.length(); l++) {
                                        JSONArray pointArray = polylineArray.getJSONArray(l);
                                        Coordinate coordinate = new Coordinate(pointArray.getDouble(0), pointArray.getDouble(1));//todo CHECK
                                        polylineCoordArr[l]=coordinate;
                                    }
                                   LineString lineString = factory.createLineString(polylineCoordArr);
                                    lineStringsArr[j]=lineString;
                                }
                                geometry = factory.createMultiLineString(lineStringsArr);
                            }
                        }
                        else if(geometryType.equalsIgnoreCase("polygon")){
                            if (geometryJson.has("coordinates")) {
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");

                                Coordinate[] polygonPoints = new Coordinate[outerCoordinatesJArray.getJSONArray(0).length()];
                                
                                for (int k = 0; k < outerCoordinatesJArray.length(); k++) {
                                    JSONArray polygonOrHoleArray = outerCoordinatesJArray.getJSONArray(k);
                                    if (k == 0) {
                                        for (int l = 0; l < polygonOrHoleArray.length(); l++) {
                                            JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                            Coordinate coordinate = new Coordinate(pointArray.getDouble(0), pointArray.getDouble(1));
                                           polygonPoints[l] = coordinate;
                                        }
                                        break;
                                    } 
                                }
                                
                                geometry = factory.createPolygon(polygonPoints);
                            }
                        }
                        else if(geometryType.equalsIgnoreCase("multipolygon")){
                            if(geometryJson.has("coordinates")){
                                JSONArray outerCoordinatesJArray = geometryJson.getJSONArray("coordinates");
                                com.vividsolutions.jts.geom.Polygon[] polygons = new com.vividsolutions.jts.geom.Polygon[outerCoordinatesJArray.length()];
                                for(int j=0;j<outerCoordinatesJArray.length();j++){//TODO check
                                    JSONArray polygonArray = outerCoordinatesJArray.getJSONArray(j);
                                    
                                    for(int k=0;k<polygonArray.length();k++){//TODO check
                                        Coordinate[] polygonPoints = new Coordinate[polygonArray.getJSONArray(0).length()];
                                        JSONArray polygonOrHoleArray = polygonArray.getJSONArray(k);
                                        if(k == 0){
                                            for(int l = 0;l<polygonOrHoleArray.length();l++){
                                                JSONArray pointArray = polygonOrHoleArray.getJSONArray(l);
                                                Coordinate geoPoint = new Coordinate(pointArray.getDouble(0),pointArray.getDouble(1));
                                                polygonPoints[l] = geoPoint;
                                            }

                                        }
                                        com.vividsolutions.jts.geom.Polygon polygon = factory.createPolygon(polygonPoints);
                                        polygons[j] = polygon;
                                    }
                                }
                                geometry = factory.createMultiPolygon(polygons);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return geometry;
    }
}
