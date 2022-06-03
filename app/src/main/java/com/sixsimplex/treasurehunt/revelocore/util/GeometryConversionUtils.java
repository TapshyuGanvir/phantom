package com.sixsimplex.treasurehunt.revelocore.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mil.nga.sf.Geometry;
import mil.nga.sf.LineString;
import mil.nga.sf.MultiLineString;
import mil.nga.sf.MultiPolygon;
import mil.nga.sf.Point;
import mil.nga.sf.Polygon;

public class GeometryConversionUtils {

    public static JSONObject convertGeometryToGeoJSON(Geometry geometry) {

        if (geometry instanceof Polygon) {
            return forPolygon((Polygon) geometry);

        } else if (geometry instanceof MultiPolygon) {
            return forMultiPolygon(geometry);

        } else if (geometry instanceof LineString) {
            return forLineString((LineString) geometry);

        } else if (geometry instanceof MultiLineString) {
            return forMultiLineString(geometry);

        } else if (geometry instanceof Point) {
            return forPoint(geometry);
        }

        return null;
    }

    private static JSONObject forPolygon(Polygon polygon) {

        JSONArray ringsArray = new JSONArray();

        createPolygonJsonArray(polygon, ringsArray);

        JSONObject polygonJson = new JSONObject();
        try {
            JSONArray coordinates = new JSONArray();

            polygonJson.put("type", "Polygon");
            polygonJson.put("coordinates", coordinates.put(ringsArray));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return polygonJson;

    }

    private static void createPolygonJsonArray(Polygon polygon, JSONArray ringsArray) {

        List<JSONArray> interiorRings = new ArrayList<>();

        for (int intRing = 0; intRing < polygon.numInteriorRings(); intRing++) {

            LineString lineString = polygon.getRing(intRing);
            interiorRings.add(createLineStringPathArray(lineString, true));//interior polygon point
        }

        LineString exteriorRing = polygon.getExteriorRing();
        JSONArray exteriorRingArray = createLineStringPathArray(exteriorRing, true);//exterior polygon point

        JSONArray polygonArray = new JSONArray();  //add exterior ring and interior rings to polygon ring
        polygonArray.put(exteriorRingArray);
        if (interiorRings.size() > 0) {

            for (int i = 0; i < interiorRings.size(); i++) { //add it to polygon ring , else don't mention
                polygonArray.put(interiorRings);
            }
        }

        ringsArray.put(polygonArray);
    }

    private static JSONObject forMultiPolygon(Geometry multipolygonGeometry) {

        MultiPolygon multiPolygon = (MultiPolygon) multipolygonGeometry;

        JSONArray ringsArray = new JSONArray();//create a ring array and add paths to it

        List<Polygon> polygonList = multiPolygon.getPolygons();

        for (Polygon polygon : polygonList) {
            createPolygonJsonArray(polygon, ringsArray);
        }

        JSONObject multiPolygonJson = new JSONObject();
        try {
            JSONArray coordinates = new JSONArray();

            multiPolygonJson.put("type", "MultiPolygon");
            multiPolygonJson.put("coordinates", coordinates.put(ringsArray));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return multiPolygonJson;
    }

    private static JSONObject forLineString(LineString lineString) {

        JSONArray polylineArray = new JSONArray(); //add exterior ring and interior rings to polygon ring

        polylineArray.put(createLineStringPathArray(lineString, false));

        JSONObject lineStringJson = new JSONObject();
        try {
            JSONArray coordinates = new JSONArray();

            lineStringJson.put("type", "LineString");
            lineStringJson.put("coordinates", coordinates.put(polylineArray));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lineStringJson;

    }

    private static JSONObject forMultiLineString(Geometry lineStringGeometry) {

        MultiLineString multiLineString = (MultiLineString) lineStringGeometry;

        JSONArray polylineArray = new JSONArray(); //add exterior ring and interior rings to polygon ring

        List<LineString> lineStringList = multiLineString.getLineStrings();

        for (LineString lineString : lineStringList) {
            polylineArray.put(createLineStringPathArray(lineString, false));
        }

        JSONObject multiLineStringJson = new JSONObject();
        try {
            JSONArray coordinates = new JSONArray();

            multiLineStringJson.put("type", "MultiLineString");
            multiLineStringJson.put("coordinates", coordinates.put(polylineArray));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return multiLineStringJson;

    }

    private static JSONObject forPoint(Geometry pointGeometry) {

        Point point = (Point) pointGeometry;

        JSONObject pointJson = new JSONObject();
        try {
            pointJson.put("type", "Point");
            pointJson.put("coordinates", createCoordinateJsonArray(point));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pointJson;
    }

    private static JSONArray createLineStringPathArray(LineString lineString, boolean isFirstLastPointSame) {

        JSONArray pathArray = new JSONArray(); //create a ring array and add paths to it

        List<Point> lineStringPoints = lineString.getPoints();

        for (Point point : lineStringPoints) {
            pathArray.put(createCoordinateJsonArray(point));
        }

        if (isFirstLastPointSame) {
            if (lineStringPoints.size() > 0) {
                Point firstPoint = lineStringPoints.get(0);
                if (lineStringPoints.size() > 2) {
                    Point lastPoint = lineStringPoints.get(lineStringPoints.size() - 1);

                    if (firstPoint.getX() != lastPoint.getX() && firstPoint.getY() != lastPoint.getY()) {

                        pathArray.put(createCoordinateJsonArray(firstPoint));  //first and last points are not same so add first point again
                    }
                }
            }
        }

        return pathArray;
    }

    private static JSONArray createCoordinateJsonArray(Point point) {
        JSONArray coordinates = new JSONArray();
        try {
            coordinates.put(point.getX());
            coordinates.put(point.getY());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coordinates;
    }

}