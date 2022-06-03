package com.sixsimplex.treasurehunt.revelocore.util.MapDrawTools;

import static com.sixsimplex.treasurehunt.revelocore.layer.GeometryEngine.convertMetersToDecimalDegrees;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

import org.json.JSONObject;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class DrawGeometry implements Observer {

    public int POLYGON = 0, POLYLINE = 1, POINT = 2;
    public boolean isDrawingOngoing = false; // tells if drawing is ongoing. if done button pressed, taps/locations received should be ignored
   // ObservableDrawingState observableDrawingState;
    ObservableMapOperation observableMapOperation;
    private MapView mMapView;
    private Context context;
    private HashMap<Integer, Polygon> drawingPolygonMap;
    private HashMap<Integer, Polyline> drawingPolylineMap;
    private Marker pointMarker;
    private int fillColorActive = Color.argb(60, 0, 255, 0);
    private int fillColorFreezed = Color.argb(60, 255, 255, 0);
    private int strokeColorActive = Color.argb(100, 0, 255, 0);
    private int strokeColorFreezed = Color.argb(100, 255, 165, 0);
    private int strokeWidth = 4;
    private int geometryType;
    private int POS_START = 0, POS_LAST = 1, POS_EDIT = 2, POS_MIDDLE = 3, POS_REMAIN = -1, INACTIVE = 4;
    private HashMap<Integer, List<Marker>> markerListMap;//ring no - list of markers
    private HashMap<Integer, List<GeoPoint>> geoPointListMap;//rinf no- list of geopoints
    private HashMap<Integer, List<GeoPoint>> geoPointWithoutBufferListMap;//rinf no- list of geopoints
    private List<HashMap<Integer, List<GeoPoint>>> undoPointList, redoPointList;
    private List<GeoPoint> undoPointTypeList, redoPointTypeList;
    private IMeasurementInfoListner iMeasurementInfoListner;
    private MapEventsOverlay mapEventsOverlay;
    private String className = "DrawGeometry";
    private int drag_state_start = 0;
    private int drag_state_drag = 1;
    private int drag_state_end = 2;
    private int currentRingNo = -1;//changed when you drag a point int ring or press add part
    private boolean currentRingNoChangedByTapInside = false;//changed when you drag a point int ring or press add part
    private FeatureLayer currentFeatureLayer;//for geometry constraints
    private Feature currentFeature;//for geometry constraints
    private String measurementString = null;
    private double measurementValue = 0.0;

    //for editmode
    HashMap<Integer, Polygon> savedDrawingPolygonMap = new HashMap<>();
    HashMap<Integer, Polyline> savedDrawingPolylineMap = new HashMap<>();
    Marker savedPointMarker = null;
    HashMap<Integer, List<Marker>> savedMarkerListMap = new HashMap<>();//ring no - list of markers
    HashMap<Integer, List<GeoPoint>> savedGeoPointListMap = new HashMap<>();//rinf no- list of geopoints
    HashMap<Integer, List<GeoPoint>> savedGeoPointWithoutBufferListMap= new HashMap<>();;//rinf no- list of geopoints



    public DrawGeometry(Context context, MapView mMapView) {
        this.mMapView = mMapView;
        this.context = context;
        iMeasurementInfoListner = (IMeasurementInfoListner) context;
        measurementString = "";
        measurementValue = 0.0;

       // observableDrawingState = ObservableDrawingState.getInstance();
        observableMapOperation = ObservableMapOperation.getInstance();
    }

    private static Geometry getLineGeometry(List<GeoPoint> geoPoints) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        Coordinate[] coordinates = new Coordinate[geoPoints.size()];
        for (int i = 0; i < geoPoints.size(); i++) {
            coordinates[i] = new Coordinate(geoPoints.get(i).getLongitude(), geoPoints.get(i).getLatitude());
        }

        Geometry geometry = factory.createLineString(coordinates);
        return geometry;
    }
    private static Geometry getMultiLineGeometry(List<GeoPoint> geoPoints) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

        Coordinate[] coordinates = new Coordinate[geoPoints.size()];
        for (int i = 0; i < geoPoints.size(); i++) {
            coordinates[i] = new Coordinate(geoPoints.get(i).getLongitude(), geoPoints.get(i).getLatitude());
        }

        LineString lineString = factory.createLineString(coordinates);
        LineString[] lineStrings = {lineString};
        Geometry geometry = factory.createMultiLineString(lineStrings);

        return geometry;
    }
    private static Geometry getPointGeometryFromCordinates(GeoPoint geoPoint) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Geometry geometry = factory.createPoint(new Coordinate(geoPoint.getLongitude(), geoPoint.getLatitude()));
        return geometry;
    }

    public static Geometry createSubGeometryFromGeoPointsUsingBuffer(List<GeoPoint> subGeom, Double bufferDistance, String direction, boolean convertToMultiPolygon) {
        try {

            if (subGeom != null && subGeom.size() > 0) {

                GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

                //List<LineString> lineStringList = new ArrayList<>();


                Coordinate[] coordinates = new Coordinate[subGeom.size()];

                for (int i = 0; i < subGeom.size(); i++) {
                    coordinates[i] = new Coordinate(subGeom.get(i).getLongitude(), subGeom.get(i).getLatitude());
                }
                //LineString lineString = factory.createLineString(coordinates);
                //  lineStringList.add(lineString);


//            com.vividsolutions.jts.geom.LineString[] lineStrings;
//            lineStrings = lineStringList.toArray(new LineString[0]);
                Geometry geometry = null;
                if (coordinates.length == 1) {
                    geometry = factory.createPoint(coordinates[0]);
                } else {
                    geometry = factory.createLineString(coordinates);
                }

                BufferParameters bufferParameters = new BufferParameters();

                if (direction.equalsIgnoreCase("Right") || direction.equalsIgnoreCase("Left")) {
                    bufferParameters.setSingleSided(true);

                    // positive distance indicates the left-hand side
                    // negative distance indicates the right-hand side

                    if (direction.equalsIgnoreCase("Right")) {
                        bufferDistance = -bufferDistance;
                    }
                } else {
                    bufferParameters.setSingleSided(false);
                }

                bufferParameters.setEndCapStyle(BufferParameters.CAP_SQUARE);
                bufferParameters.setJoinStyle(BufferParameters.JOIN_BEVEL);

                BufferOp bufferOp = new BufferOp(geometry, bufferParameters);
                geometry = bufferOp.getResultGeometry(bufferDistance);

                // geometry = geometry.buffer(bufferDistance, BufferParameters.JOIN_MITRE, BufferParameters.CAP_SQUARE);
                if (convertToMultiPolygon) {
                    if (geometry instanceof com.vividsolutions.jts.geom.Polygon) {
                        com.vividsolutions.jts.geom.Polygon[] polygons = new com.vividsolutions.jts.geom.Polygon[1];
                        polygons[0] = (com.vividsolutions.jts.geom.Polygon) geometry;
                        geometry = factory.createMultiPolygon(polygons);
                    }
                }
                return geometry;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateCurrentRingno(boolean isBufferEnabled) {
        int maxRingNo = 0;
        if(isBufferEnabled){
            if (geoPointWithoutBufferListMap != null && !geoPointWithoutBufferListMap.isEmpty()) {
                for (Integer ringno : geoPointWithoutBufferListMap.keySet()) {
                    if (ringno > maxRingNo) {
                        maxRingNo = ringno;
                    }
                }
            }
        }else {
            if (geoPointListMap != null && !geoPointListMap.isEmpty()) {
                for (Integer ringno : geoPointListMap.keySet()) {
                    if (ringno > maxRingNo) {
                        maxRingNo = ringno;
                    }
                }
            }
        }

        currentRingNo = ++maxRingNo;
        if (undoPointTypeList != null && !undoPointTypeList.isEmpty()) {
            undoPointTypeList.clear();
        }

        if (redoPointTypeList != null && !redoPointTypeList.isEmpty()) {
            redoPointTypeList.clear();
        }

        if (undoPointList != null && !undoPointList.isEmpty()) {
            undoPointList.clear();
        }

        if (redoPointList != null && !redoPointList.isEmpty()) {
            redoPointList.clear();
        }
        makeOtherPolygonsInactive(currentRingNo,!isBufferEnabled);
    }

    public void startDrawing(int geometryType, FeatureLayer featureLayer, Feature currentFeature, boolean drawingByTap) {
        this.currentFeature = currentFeature;
        this.currentFeatureLayer = featureLayer;
        this.geometryType = geometryType;
        markerListMap = new HashMap<>();
        geoPointListMap = new HashMap<>();
        undoPointList = new ArrayList<>();
        redoPointList = new ArrayList<>();
        undoPointTypeList = new ArrayList<>();
        redoPointTypeList = new ArrayList<>();
        isDrawingOngoing = true;
        if (drawingByTap) {
            MapEventsReceiver mReceive = new MapEventsReceiver() {  //click on map and get clicked location.
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                    if (geometryType == POINT && isDrawingOngoing) {
                        currentRingNo = 0;
                        GgeoPoint ggeoPoint = new GgeoPoint(geoPoint);
                        ggeoPoint.setMid(false);
                        drawMode(currentRingNo, ggeoPoint, true, false);//draw by tap-start drawing
                    } else {
                        if (currentRingNo != -1 && !currentRingNoChangedByTapInside && isDrawingOngoing) {
                            addVertex(geoPoint, false);
                            lockUnlockOtherPolygons(currentRingNo,true);
                        }
                        else if (currentRingNoChangedByTapInside) {
                            isDrawingOngoing = true;
                            observableMapOperation.setPartSelected(true);
                            currentRingNoChangedByTapInside = false;
                            if (currentRingNo != -1) {
                                makeOtherPolygonsInactive(currentRingNo, true);
                            }
                        }
                    }
                    return true;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    return false;
                }
            };

            mapEventsOverlay = new MapEventsOverlay(mReceive);
            mMapView.getOverlays().add(mapEventsOverlay);
        }
    }

    public void lockUnlockOtherPolygons(int curRingNo, boolean lockOthers) {
        if (drawingPolygonMap != null && !drawingPolygonMap.isEmpty()) {
            for (Integer ringno : drawingPolygonMap.keySet()) {
                if (ringno != curRingNo) {
                    Polygon drawingPolygon = null;
                    if (drawingPolygonMap.containsKey(ringno)) {
                        drawingPolygon = drawingPolygonMap.get(ringno);
                    }
                    if (drawingPolygon != null) {
                        if(lockOthers) {
                            drawingPolygon.setOnClickListener(new Polygon.OnClickListener() {
                                @Override
                                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                                    return false;
                                }
                            });
                        }else {
                            drawingPolygon.setOnClickListener(new Polygon.OnClickListener() {
                                @Override
                                public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                                    currentRingNo = Integer.parseInt(polygon.getId());
                                    currentRingNoChangedByTapInside = true;
                                    //make otherpolygons immovable
                                    makeOtherPolygonsInactive(currentRingNo,! observableMapOperation.isBufferEnabled());
                                    //make this polygon green and draggable
                                    return false;
                                }
                            });
                        }
                    }
                }
            }
            mMapView.invalidate();
        }
    }

    public void addVertex(GeoPoint geoPoint, boolean isBufferEnabled) {
        observableMapOperation.setPartSelected(false);
        observableMapOperation.setAddPartClicked(false);
        observableMapOperation.setDrawingOngoing(true);
        GgeoPoint ggeoPoint = new GgeoPoint(geoPoint);
        ggeoPoint.setMid(false);
        drawMode(currentRingNo, ggeoPoint, true, isBufferEnabled);//addvertex
    }

    public void drawMode(int ringno, GeoPoint geoPoint, boolean addToUndoList, boolean isBufferEnabled) {//happens for each geopoint during init, before actual user interaction starts

        if (geometryType == POINT) {//hence before drawmode, we need to set geometry type
            drawPoint(geoPoint);
            observableMapOperation.setGeometryValid(true);
            observableMapOperation.setDrawingOngoing(true);
        } else {
            if (markerListMap != null) {
                if (!markerListMap.containsKey(ringno)) {
                    markerListMap.put(ringno, new ArrayList<Marker>());
                }
                Marker marker = createMarker(ringno, geoPoint, !isBufferEnabled, addToUndoList, isBufferEnabled);
                marker.setRelatedObject(false);

                if (markerListMap.get(ringno).isEmpty()) {
                    updateMarkerIcon(marker, POS_START);
                } else {

                    Marker lastMarker = null;

                    if (markerListMap.get(ringno).size() > 1) {
                        lastMarker = markerListMap.get(ringno).get(markerListMap.get(ringno).size() - 1);
                    }

                    if (lastMarker != null) {
                        updateMarkerIcon(lastMarker, POS_REMAIN);//change last marker color as middle

                    }

                    updateMarkerIcon(marker, POS_LAST);
                }

                mMapView.getOverlays().add(marker);
                markerListMap.get(ringno).add(marker);

                if (!geoPointListMap.containsKey(ringno)) {
                    geoPointListMap.put(ringno, new ArrayList<>());
                }

                List<GeoPoint> geoPointList = geoPointListMap.get(ringno);
                if (geoPointList.size() > 2 && geometryType == POLYGON ) {//addtoundo list nahiye, mhanje we are drawing for first time
                    GeoPoint lastPoint = geoPointList.get(geoPointList.size() - 1);
                    GeoPoint firstPoint = geoPointList.get(0);
                    //if first and last points are same, we need to add new vertex at last-1 and last's mid
                    if (firstPoint.getLatitude() == lastPoint.getLatitude()
                            && firstPoint.getLongitude() == lastPoint.getLongitude()) {
                        geoPointListMap.get(ringno).add(geoPointList.size() - 2, geoPoint);
                    } else {
                        geoPointListMap.get(ringno).add(geoPoint);
                    }
                } else {
                    geoPointListMap.get(ringno).add(geoPoint);
                }
                drawGeometry(geometryType, geoPointListMap, drag_state_end, addToUndoList, isBufferEnabled);
            }
        }

        mMapView.invalidate();
    }

    private void drawPoint(GeoPoint geoPoint) {

        if (pointMarker == null) {
            pointMarker = createMarker(0, geoPoint, false, true, false);
            updateMarkerIcon(pointMarker, POS_START);
            mMapView.getOverlays().add(pointMarker);
        }

        pointMarker.setPosition(geoPoint);
        measurementString = "Lat :" + pointMarker.getPosition().getLatitude() + "\n" + "Long :" + pointMarker.getPosition().getLongitude();
        measurementValue = 0.0d;
        iMeasurementInfoListner.measurementInfo(measurementString, AppConstants.MEASUREMENT_COORDINATES_REQUEST);
        undoPointTypeList.add(geoPoint);
        observableMapOperation.setUndoPossible(true);
    }

  /*  private void checkForGeometryConstraints() {
        if(currentFeatureLayer!=null) {
            //get all features apart from current feature being edited. in case of add, fetch all features
            FeatureTable currentFeatureTable = currentFeatureLayer.getFeatureTable();

//SELECT "fid", "dimension", "sitename", "name", "w9entityclassname", "w9metadata", "the_geom" FROM "grids"
// WHERE st_intersects(grids.the_geom,st_geomfromtext('SRID=4326; MULTIPOLYGON (((79.04920277026383 21.148229886036635, 79.0492994289884 21.149023796362574, 79.05023628199822 21.148925018600043, 79.05023457519917 21.148855061451023, 79.0501404317574 21.148176265937177, 79.04920277026383 21.148229886036635, 79.0497678340725 21.147842859137654, 79.04920277026383 21.148229886036635)))'));
            try {
             Geometry geometry = getDrawingGeometry();
             GeoPackageGeometryData oldGeometryData = new GeoPackageGeometryData(geometry.getSRID());
             oldGeometryData.setGeometryFromWkt(geometry.toText());
             oldGeometryData.getOrBuildEnvelope();

             ProjectionTransform transform = currentFeatureTable.getTransform4326().getInverseTransformation();

             GeoPackageGeometryData newGeomData = oldGeometryData.transform(transform);

             newGeomData.setSrsId(Integer.parseInt(String.valueOf(currentFeatureTable.getFeatureDao().getSrsId())));
             newGeomData.getOrBuildEnvelope();

//                Projection webMercator = ProjectionFactory.getProjection(
//                        ProjectionConstants.AUTHORITY_EPSG,
//                        ProjectionConstants.EPSG_WEB_MERCATOR);
//                Projection wgs84 = ProjectionFactory.getProjection(
//                        ProjectionConstants.AUTHORITY_EPSG,
//                        ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
//
////                ProjectionTransform transformWebMercatorToWgs84 = webMercator
////                        .getTransformation(wgs84);
//
//                ProjectionTransform transformWgs84ToWebMercator = wgs84
//                        .getTransformation(webMercator);
//                Geometry transformedGeometry = transformWgs84ToWebMercator
//                        .transform(geometry);




                *//*String whereClause = "st_intersects"
                        + "(" + currentFeatureLayer.getName() + "." + currentFeatureTable.getFeatureDao().getGeometryColumnName() + ","
                        + "st_geomfromtext('SRID=3857; " + newGeomData.getWkt() + "')"
                        + ")" +
                        ";";*//*
     *//*String whereClause = "ST_Area"
                        + "(" + currentFeatureLayer.getName() + "." + currentFeatureTable.getFeatureDao().getGeometryColumnName()
                        + ") > 1" +
                        ";";*//*
                String whereClause = "intersects" +
                        "("+currentFeatureLayer.getName()+"."+currentFeatureTable.getFeatureDao().getGeometryColumnName()+",  GeomFromText('" + newGeomData.getWkt() + "',"+newGeomData.getSrsId()+")) = 1";
               // List<Feature> featuresList = currentFeatureTable.getallFeaturesByQuery(whereClause);

                String query = "select * from "+currentFeatureLayer.getName()+" where "+whereClause;
               // currentFeatureTable.getfeaturesFrwmSpatialQuery(query);
                //List<Feature> featuresList = currentFeatureTable.getFeaturesIntersectingGeometry(newGeomData.getWkt());
                List<Feature> featuresList = currentFeatureTable.getFeaturesIntersectingGeometry(getDrawingGeometry().toText());
                //List<Feature> featuresList = new ArrayList<>();

                if (featuresList == null || featuresList.size() == 0) {
                    Toast.makeText(context, "no intersection", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "INTERSECTED!!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    private void drawGeometry(int type, HashMap<Integer, List<GeoPoint>> latLngList, int dragState, boolean addToUndoList, boolean isBufferEnabled) {
//type = geometrytype as in intended geometry shape
        if (type == POLYGON) {
            drawPolygon(latLngList, dragState, addToUndoList, isBufferEnabled);
        } else if (type == POLYLINE) {
            drawPolyline(latLngList, dragState, addToUndoList);
        }
    }

    private void drawPolyline(HashMap<Integer, List<GeoPoint>> geoPointList, int dragState, boolean addToUndoList) {

        try {
            if (drawingPolylineMap == null) {
                drawingPolylineMap = new HashMap<>();
            }

            double distanceInMeter = 0;

            for (Integer ringno : geoPointList.keySet()) {
                Polyline drawingPolyline = null;
                if (drawingPolylineMap.containsKey(ringno)) {
                    drawingPolyline = drawingPolylineMap.get(ringno);
                }
                if (drawingPolyline == null) {
                    drawingPolyline = new Polyline(mMapView);

                    Paint linePaint = drawingPolyline.getOutlinePaint();
                    linePaint.setColor(strokeColorActive);
                    linePaint.setStrokeWidth(strokeWidth);

                    mMapView.getOverlays().add(drawingPolyline);
                    drawingPolylineMap.put(ringno, drawingPolyline);
                    drawingPolyline.setOnClickListener(new Polyline.OnClickListener() {
                        @Override
                        public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                            currentRingNo = Integer.parseInt(polyline.getId());
                            currentRingNoChangedByTapInside = true;
                            //make otherpolygons immovable
                            makeOtherPolyLinesInactive(ringno);
                            //make this polygon green and draggable
                            return false;
                        }
                    });
                }

                drawingPolyline.setPoints(geoPointList.get(ringno));

                distanceInMeter = drawingPolyline.getDistance();
            }

            String distance = "";
            if (distanceInMeter > 1000.0) {
                double d = (distanceInMeter / 1000);
                distance = new DecimalFormat("##.##").format(d) + " Km";
            } else {
                distance = new DecimalFormat("##.##").format(distanceInMeter) + " m";
            }

            measurementString = distance;
            measurementValue = distanceInMeter;

            iMeasurementInfoListner.measurementInfo(distance, AppConstants.MEASUREMENT_DISTANCE_REQUEST);
            JSONObject geometryValidation = isDrawGeometryValid();
            try {
                boolean isValid = geometryValidation.has("isGeometryValid")?geometryValidation.getBoolean("isGeometryValid"):false;
               // int numGeometries = geometryValidation.has("numGeometries")?geometryValidation.getInt("numGeometries"):0;
                if (isValid) {
                    observableMapOperation.setGeometryValid(true);
                } else {
                    observableMapOperation.setGeometryValid(false);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if (dragState == 2 && addToUndoList) {
                //undoPointList.add((HashMap<Integer, List<GeoPoint>>) geoPointList.clone());
                HashMap<Integer, List<GeoPoint>> hashMap = new HashMap<>();
                for (Integer ringno : geoPointList.keySet()) {
                    List<GeoPoint> destlist = new ArrayList<>();
                    List<GeoPoint> srclist = geoPointList.get(ringno);

                    for (int i = 0; i < srclist.size(); i++) {
                        GeoPoint geoPoint = srclist.get(i);
                        destlist.add(new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()));
                    }
                    hashMap.put(ringno, destlist);
                }
                undoPointList.add(hashMap);
                observableMapOperation.setUndoPossible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "drawPolyline", String.valueOf(e.getCause()));
        }

    }

    private void drawPolygon(HashMap<Integer, List<GeoPoint>> geoPointList, int dragState,
                             boolean addToUndoList, boolean isBufferEnabled) {
        //here we draw polygon based to the geopoints list that keeps coming in. we initialize drawing polygon only once and every other time just setpoints
        //we calc area and send info to measurement listener
        try {
            if (drawingPolygonMap == null) {
                drawingPolygonMap = new HashMap<>();
            }

            double areaValue = 0;

            for (Integer ringno : geoPointList.keySet()) {
                Polygon drawingPolygon = null;
                if (drawingPolygonMap.containsKey(ringno)) {
                    drawingPolygon = drawingPolygonMap.get(ringno);
                }
                if (drawingPolygon == null) {
                    drawingPolygon = new Polygon(mMapView);
                    drawingPolygon.getFillPaint().setColor(fillColorActive);
                    drawingPolygon.setId(ringno.toString());

                    Paint polygonOutLinePaint = drawingPolygon.getOutlinePaint();
                    polygonOutLinePaint.setColor(strokeColorActive);
                    polygonOutLinePaint.setStrokeWidth(strokeWidth);

                    mMapView.getOverlays().add(drawingPolygon);
                    drawingPolygonMap.put(ringno, drawingPolygon);
                    drawingPolygon.setOnClickListener(new Polygon.OnClickListener() {
                        @Override
                        public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                            currentRingNo = Integer.parseInt(polygon.getId());
                            currentRingNoChangedByTapInside = true;
                            //make otherpolygons immovable
                            makeOtherPolygonsInactive(ringno,!isBufferEnabled);
                            //make this polygon green and draggable
                            return false;
                        }
                    });
                }

                drawingPolygon.setPoints(geoPointList.get(ringno));
                areaValue = areaValue + SystemUtils.computeArea(geoPointList.get(ringno));
                JSONObject geometryValidation = isDrawGeometryValid();
                try {
                    boolean isValid = geometryValidation.has("isGeometryValid") ? geometryValidation.getBoolean("isGeometryValid") : false;
                   // int numGeometries = geometryValidation.has("numGeometries") ? geometryValidation.getInt("numGeometries") : 0;

                    if (isValid) {
                        observableMapOperation.setGeometryValid(true);
                    } else {
                        observableMapOperation.setGeometryValid(false);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (currentRingNo != -1)
                makeOtherPolygonsInactive(currentRingNo,!isBufferEnabled);
            // if(currentFeatureLayer !=null)
            //     checkForGeometryConstraints();
            String area = "";

                if(areaValue>0) {
                    areaValue = areaValue / 10000; //hectares;
                }
                area = new DecimalFormat("##.##").format(areaValue) + " Hectares";

            measurementString = area;
            measurementValue = areaValue;

            iMeasurementInfoListner.measurementInfo(area, AppConstants.MEASUREMENT_AREA_REQUEST);

            if (dragState == 2 && addToUndoList) {
                // undoPointList.add((HashMap<Integer, List<GeoPoint>>) geoPointList.clone());
                HashMap<Integer, List<GeoPoint>> hashMap = new HashMap<>();
                if (isBufferEnabled) {
                    for (Integer ringno : geoPointWithoutBufferListMap.keySet()) {
                        List<GeoPoint> destlist = new ArrayList<>();
                        List<GeoPoint> srclist = geoPointWithoutBufferListMap.get(ringno);

                        for (int i = 0; i < srclist.size(); i++) {
                            GeoPoint geoPoint = srclist.get(i);
                            destlist.add(new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()));
                        }
                        hashMap.put(ringno, destlist);
                    }
                    if (undoPointList != null && undoPointList.size() > 0) {
                        HashMap<Integer, List<GeoPoint>> lasthashmap = undoPointList.get(undoPointList.size() - 1);
                        if (!hashMap.equals(lasthashmap)) {
                            undoPointList.add(hashMap);
                            observableMapOperation.setUndoPossible(true);
                        } else {
                            //skip
                        }
                    } else {
                        if (undoPointList == null)
                            undoPointList = new ArrayList<>();

                        undoPointList.add(hashMap);
                        observableMapOperation.setUndoPossible(true);
                    }
                } else {
                    for (Integer ringno : geoPointList.keySet()) {
                        List<GeoPoint> destlist = new ArrayList<>();
                        List<GeoPoint> srclist = geoPointList.get(ringno);

                        for (int i = 0; i < srclist.size(); i++) {
                            GeoPoint geoPoint = srclist.get(i);
                            destlist.add(new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()));
                        }
                        hashMap.put(ringno, destlist);
                    }
                    undoPointList.add(hashMap);
                    observableMapOperation.setUndoPossible(true);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "drawPolygon", String.valueOf(e.getCause()));

        }
    }

    public void makeOtherPolygonsInactive(int currentRingNo,boolean isVertexDraggable) {
        if (drawingPolygonMap != null && !drawingPolygonMap.isEmpty()) {
            for (Integer ringno : drawingPolygonMap.keySet()) {
                if (ringno != currentRingNo) {
                    Polygon drawingPolygon = null;
                    if (drawingPolygonMap.containsKey(ringno)) {
                        drawingPolygon = drawingPolygonMap.get(ringno);
                    }
                    if (drawingPolygon != null) {
                        drawingPolygon.getFillPaint().setColor(fillColorFreezed);
                        Paint polygonOutLinePaint = drawingPolygon.getOutlinePaint();
                        polygonOutLinePaint.setColor(strokeColorFreezed);
                        //make its markers non-draggable
                        makeMarkersOfRingDraggable(ringno, false);
                    }
                } else {
                    Polygon drawingPolygon = null;
                    if (drawingPolygonMap.containsKey(ringno)) {
                        drawingPolygon = drawingPolygonMap.get(ringno);
                    }
                    if (drawingPolygon != null) {
                        drawingPolygon.getFillPaint().setColor(fillColorActive);
                        Paint polygonOutLinePaint = drawingPolygon.getOutlinePaint();
                        polygonOutLinePaint.setColor(strokeColorActive);
                        //make its markers non-draggable
                        makeMarkersOfRingDraggable(ringno, isVertexDraggable);
                    }
                }
            }
            mMapView.invalidate();
        }
    }

    public void makeOtherPolyLinesInactive(int currentRingNo) {
        if (drawingPolylineMap != null && !drawingPolylineMap.isEmpty()) {
            for (Integer ringno : drawingPolylineMap.keySet()) {
                if (ringno != currentRingNo) {
                    Polyline drawingPolyline = null;
                    if (drawingPolylineMap.containsKey(ringno)) {
                        drawingPolyline = drawingPolylineMap.get(ringno);
                    }
                    if (drawingPolyline != null) {
                        Paint polygonOutLinePaint = drawingPolyline.getOutlinePaint();
                        polygonOutLinePaint.setColor(strokeColorFreezed);
                        //make its markers non-draggable
                        makeMarkersOfRingDraggable(ringno, false);
                    }
                } else {
                    Polyline drawingPolyline = null;
                    if (drawingPolylineMap.containsKey(ringno)) {
                        drawingPolyline = drawingPolylineMap.get(ringno);
                    }
                    if (drawingPolyline != null) {
                        Paint polygonOutLinePaint = drawingPolyline.getOutlinePaint();
                        polygonOutLinePaint.setColor(strokeColorActive);
                        //make its markers non-draggable
                        makeMarkersOfRingDraggable(ringno, true);
                    }
                }
            }
            mMapView.invalidate();
        }
    }

    private void makeMarkersOfRingDraggable(Integer ringno, boolean makeDraggable) {
        if (markerListMap.containsKey(ringno)) {
            List<Marker> markerList = markerListMap.get(ringno);
            for (Marker marker : markerList) {
                if (makeDraggable) {
                    updateMarkerIcon(marker, POS_START);
                    marker.setDraggable(makeDraggable);
                } else {
                    updateMarkerIcon(marker, INACTIVE);
                    marker.setDraggable(makeDraggable);
                }
            }
        }
    }

    private void updateMarkerLocation(String id_ringNo, Marker marker, int dragState, boolean addToUndoList, boolean isBufferEnabled) {
        Integer ringNo = Integer.parseInt(id_ringNo);


        int index = markerListMap.get(ringNo).indexOf(marker);
        geoPointListMap.get(ringNo).set(index, marker.getPosition());
        //  drawGeometry(geometryType, geoPointListMap, dragState, true,isBufferEnabled);
        drawGeometry(geometryType, geoPointListMap, dragState, addToUndoList, isBufferEnabled);
    }

    private Marker createMarker(Integer ringNo, GeoPoint geoPoint, boolean draggable, boolean addToUndoList, boolean isBufferEnabled) {
//when we create marker, it has got listener for drag state to use point no 1. and marker is set to draggable.
        // on marker is dragged, the point is updated in geopointlist and draw geometry is called to refresh underlying polygon/polyline
        //for polyline polygon marker is draggable. for point it is not
        Marker marker = new Marker(mMapView);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setDraggable(draggable);
        marker.setInfoWindow(null);
        marker.setId(String.valueOf(ringNo));

        if (draggable) {

        marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    lockUnlockOtherPolygons(currentRingNo,true);
                    updateMarkerLocation(marker.getId(), marker, drag_state_start, true, isBufferEnabled);//start
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    updateMarkerLocation(marker.getId(), marker, drag_state_drag, addToUndoList, isBufferEnabled);//drag
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    if(!observableMapOperation.isDrawingOngoing()){
                        observableMapOperation.setDrawingOngoing(true);
                    }
                    updateMarkerLocation(marker.getId(), marker, drag_state_end, true, isBufferEnabled);//end
                }
            });
        }
        return marker;
    }

    private void updateMarkerIcon(Marker marker, int position) {

        int icon;

        if (position == POS_START) {
            icon = R.drawable.ic_draw_start_marker;// first Marker
        } else if (position == POS_LAST) {
            // icon = R.drawable.ic_draw_last_marker;// last marker
            icon = R.drawable.ic_draw_start_marker;
        } else if (position == POS_EDIT) {
            //icon = R.drawable.ic_draw_remain_marker;// edit marker
            icon = R.drawable.ic_draw_start_marker;
        } else if (position == POS_MIDDLE) {
            // icon = R.drawable.ic_draw_mid_marker;// remaining marker
            icon = R.drawable.ic_draw_start_marker;
        } else if (position == INACTIVE) {
            // icon = R.drawable.ic_draw_edit_marker;// remaining marker
            icon = R.drawable.ic_draw_edit_marker;
        } else {
            // icon = R.drawable.ic_draw_edit_marker;// remaining marker
            icon = R.drawable.ic_draw_start_marker;
        }

        marker.setIcon(context.getResources().getDrawable(icon));
    }

    private GeoPoint midPoint(GeoPoint pt1, GeoPoint pt2) {

        // Get middle point between two coordinates
        double lat1 = pt1.getLatitude();
        double lon1 = pt1.getLongitude();
        double lat2 = pt2.getLatitude();
        double lon2 = pt2.getLongitude();

        double dLon = Math.toRadians(lon2 - lon1);
        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        return new GeoPoint(Math.toDegrees(lat3), Math.toDegrees(lon3));
    }

    public void undo(boolean isBufferEnabled, double selectedBufferWidthValue, String selectedBufferDirection) {

        if (geometryType == POINT) {

            if (undoPointTypeList != null && !undoPointTypeList.isEmpty()) {
                GeoPoint undoPoint = undoPointTypeList.remove(undoPointTypeList.size() - 1);
                redoPointTypeList.add(undoPoint);

                if (redoPointTypeList.size() == 0) {
                    observableMapOperation.setRedoPossible(false);
                } else {
                    observableMapOperation.setRedoPossible(true);
                }

                if (undoPointTypeList.isEmpty()) {

                    if (pointMarker != null) {
                        if (mMapView.getOverlays().contains(pointMarker)) {
                            pointMarker.remove(mMapView);
                            pointMarker = null;
                            observableMapOperation.setGeometryValid(false);
                        }
                    }

                    observableMapOperation.setUndoPossible(false);
                } else {

                    GeoPoint point = undoPointTypeList.get(undoPointTypeList.size() - 1);
                    drawMode(currentRingNo, point, false, isBufferEnabled);//undo
                }
            } else {

                if(observableMapOperation.getMainOperationName().equalsIgnoreCase("edit")){
                    restoreState(true,-1);
                }
                observableMapOperation.setUndoPossible(false);
            }
        } else {

            if (undoPointList != null && !undoPointList.isEmpty()) {

                HashMap<Integer, List<GeoPoint>> undoPoints = undoPointList.remove(undoPointList.size() - 1);
                redoPointList.add(undoPoints);
                if (redoPointList.size() == 0) {
                    observableMapOperation.setRedoPossible(false);
                } else {
                    observableMapOperation.setRedoPossible(true);
                }


                if (!undoPointList.isEmpty()) {
                    HashMap<Integer, List<GeoPoint>> undoPointsList = undoPointList.get(undoPointList.size() - 1);
                    for (Integer ringno : undoPointsList.keySet()) {
                        List<GeoPoint> geoPointList = undoPointsList.get(ringno);
                        /*//remove data from existing point map
                        if (!geoPointListMap.containsKey(ringno) || geoPointList.get(ringno)==null) {
                            geoPointListMap.put(ringno, new ArrayList<>());
                        }else {
                            geoPointListMap.get(ringno).clear();
                        }*/
                        if (markerListMap != null && markerListMap.containsKey(ringno)) {
                            List<Marker> markerList = markerListMap.get(ringno);
                            if (markerList != null && !markerList.isEmpty()) {
                                for (Marker marker : markerList) {
                                    marker.remove(mMapView);
                                }
                                markerList.clear();
                                markerListMap.get(ringno).clear();
                            }
                        }

                        if (geoPointListMap != null && geoPointListMap.containsKey(ringno)) {
                            geoPointListMap.get(ringno).clear();
                        }
                        if (isBufferEnabled) {

                            if(geoPointWithoutBufferListMap==null){
                                geoPointWithoutBufferListMap=new HashMap<>();
                            }
                            if (!geoPointWithoutBufferListMap.containsKey(ringno) ||geoPointWithoutBufferListMap.get(ringno)==null) {
                                geoPointWithoutBufferListMap.put(ringno, new ArrayList<>());
                            } else {
                                geoPointWithoutBufferListMap.get(ringno).clear();
                            }
                            if(geoPointList==null|| geoPointList.isEmpty()){
                                deletePart(ringno);
                            }else {
                                geoPointWithoutBufferListMap.get(ringno).addAll(geoPointList);
                                mMapView.invalidate();
                                double convertDistance = convertMetersToDecimalDegrees(selectedBufferWidthValue, geoPointList.get(0).getLatitude());

                                Geometry newGraphicGeometry = createSubGeometryFromGeoPointsUsingBuffer(geoPointList, convertDistance, selectedBufferDirection, false);
                                if (newGraphicGeometry != null) {
                                    Log.e("p", newGraphicGeometry.getCoordinates().length + "");

                                    Coordinate[] coordinates = newGraphicGeometry.getCoordinates();
                                    for (int k = 0; k < coordinates.length; k++) {
                                        GeoPoint geoPoint1 = new GeoPoint(coordinates[k].getOrdinate(1), coordinates[k].getOrdinate(0));
                                        drawMode(ringno, geoPoint1, false, isBufferEnabled);//undo
                                    }
                                }
                            }

                            mMapView.invalidate();


                        }
                        else {
                            mMapView.invalidate();
                            if(geoPointList==null||geoPointList.isEmpty()){
                                deletePart(ringno);
                            }else {
                                for (GeoPoint geoPoint : geoPointList) {
                                    drawMode(ringno, geoPoint, false, isBufferEnabled);//undo
                                }
                            }
                        }
                    }
                } else {
                    if(observableMapOperation.getMainOperationName().equalsIgnoreCase("edit")){
                        //restore initial state of current ring
                        restoreState(false,currentRingNo);
                    }else {
                        if (geoPointListMap != null && geoPointListMap.containsKey(currentRingNo)) {
                            //  geoPointListMap.get(currentRingNo).clear();
                            geoPointListMap.remove(currentRingNo);
                        }
                        if (geoPointWithoutBufferListMap != null && geoPointWithoutBufferListMap.containsKey(currentRingNo)) {
                            //geoPointWithoutBufferListMap.get(currentRingNo).clear();
                            geoPointWithoutBufferListMap.remove(currentRingNo);
                        }
                        if (drawingPolygonMap != null) {
                            if (drawingPolygonMap.containsKey(currentRingNo)) {
                                mMapView.getOverlays().remove(drawingPolygonMap.get(currentRingNo));
                                drawingPolygonMap.remove(currentRingNo);
                            }
                        }

                        if (drawingPolylineMap != null) {
                            if (drawingPolylineMap.containsKey(currentRingNo)) {
                                mMapView.getOverlays().remove(drawingPolylineMap.get(currentRingNo));
                                drawingPolylineMap.remove(currentRingNo);
                            }
                        }

                        if (pointMarker != null) {
                            pointMarker.remove(mMapView);
                            pointMarker = null;
                        }
                        if (markerListMap != null && !markerListMap.isEmpty()) {
                            List<Marker> markerList = markerListMap.get(currentRingNo);
                            if (markerList != null && !markerList.isEmpty()) {
                                for (Marker marker : markerList) {
                                    marker.remove(mMapView);
                                }
                                markerList.clear();
                                markerListMap.remove(currentRingNo);
                            }
                        }
                    }
                }

                if (undoPointList.size() == 0) {
                    //iMeasurementInfoListner.measurementUndo(false);
                    observableMapOperation.setUndoPossible(false);
                } else {
                    //iMeasurementInfoListner.measurementUndo(true);
                    observableMapOperation.setUndoPossible(true);
                }
            } else {
                //iMeasurementInfoListner.measurementUndo(false);
                observableMapOperation.setUndoPossible(false);
            }
                JSONObject geometryValidation = isDrawGeometryValid();
                try {
                    boolean isValid = geometryValidation.has("isGeometryValid")?geometryValidation.getBoolean("isGeometryValid"):false;
                    // int numGeometries = geometryValidation.has("numGeometries")?geometryValidation.getInt("numGeometries"):0;
                    if (isValid) {
                        observableMapOperation.setGeometryValid(true);
                    } else {
                        observableMapOperation.setGeometryValid(false);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
        }

        mMapView.invalidate();
    }

    public void redo(boolean isBufferEnabled, double selectedBufferWidthValue, String selectedBufferDirection) {

        if (geometryType == POINT) {

            if (redoPointTypeList != null && !redoPointTypeList.isEmpty()) {
                GeoPoint redoPoint = redoPointTypeList.remove(redoPointTypeList.size() - 1);
               // undoPointTypeList.add(redoPoint);
                //iMeasurementInfoListner.measurementUndo(true);
                /*observableMapOperation.setUndoPossible(true);
                if (pointMarker != null) {
                    pointMarker.setPosition(redoPoint);
                }
*/
                drawMode(currentRingNo, redoPoint, true, isBufferEnabled);//undo
                if (redoPointTypeList.size() == 0) {
                    //iMeasurementInfoListner.measurementRedo(false);
                    observableMapOperation.setRedoPossible(false);
                } else {
                    //iMeasurementInfoListner.measurementRedo(true);
                    observableMapOperation.setRedoPossible(true);
                }

            } else {
                //iMeasurementInfoListner.measurementRedo(false);
                observableMapOperation.setRedoPossible(false);
            }
        } else {

            if (redoPointList != null && !redoPointList.isEmpty()) {

                HashMap<Integer, List<GeoPoint>> redoPoints = redoPointList.remove(redoPointList.size() - 1);
                undoPointList.add(redoPoints);

                if (undoPointList.size() == 0) {
                    //iMeasurementInfoListner.measurementUndo(false);
                    observableMapOperation.setUndoPossible(false);
                } else {
                    //iMeasurementInfoListner.measurementUndo(true);
                    observableMapOperation.setUndoPossible(true);
                }


                //clear existing map
                if (markerListMap != null && !markerListMap.isEmpty()) {
                    for (List<Marker> markerList : markerListMap.values())
                        if (markerList != null && !markerList.isEmpty()) {
                            for (Marker marker : markerList) {
                                marker.remove(mMapView);
                            }
                            markerList.clear();
                        }
                }

                if(geoPointListMap!=null)
                geoPointListMap.clear();

                if(geoPointWithoutBufferListMap!=null)
                geoPointWithoutBufferListMap.clear();
                //


                for (Integer ringno : redoPoints.keySet()) {
                    List<GeoPoint> geoPointList = redoPoints.get(ringno);
                    if (isBufferEnabled) {


                        if (!geoPointWithoutBufferListMap.containsKey(ringno)) {
                            geoPointWithoutBufferListMap.put(ringno, new ArrayList<>());
                        } else {
                            geoPointWithoutBufferListMap.get(ringno).clear();
                        }
                        geoPointWithoutBufferListMap.get(ringno).addAll(geoPointList);

                        if (markerListMap != null && markerListMap.containsKey(ringno)) {
                            List<Marker> markerList = markerListMap.get(ringno);
                            if (markerList != null && !markerList.isEmpty()) {
                                for (Marker marker : markerList) {
                                    marker.remove(mMapView);
                                }
                                markerList.clear();
                            }
                            markerListMap.get(ringno).clear();
                        }
                        if (geoPointListMap != null && geoPointListMap.containsKey(ringno)) {
                            geoPointListMap.get(ringno).clear();
                        }
       /* if(drawingPolygonMap!=null && drawingPolygonMap.containsKey(currentRingNo)){
            mMapView.getOverlays().remove(drawingPolygonMap.get(currentRingNo));
            drawingPolygonMap.remove(currentRingNo);
        }
        if(drawingPolylineMap!=null && drawingPolylineMap.containsKey(currentRingNo)){
            mMapView.getOverlays().remove(drawingPolylineMap.get(currentRingNo));
            drawingPolylineMap.remove(currentRingNo);
        }*/
                        mMapView.invalidate();

                        double convertDistance = convertMetersToDecimalDegrees(selectedBufferWidthValue, geoPointList.get(0).getLatitude());

                        Geometry newGraphicGeometry = createSubGeometryFromGeoPointsUsingBuffer(geoPointList, convertDistance, selectedBufferDirection, false);
                        if (newGraphicGeometry != null) {
                            Log.e("p", newGraphicGeometry.getCoordinates().length + "");

                            Coordinate[] coordinates = newGraphicGeometry.getCoordinates();
                            for (int k = 0; k < coordinates.length; k++) {
                                GeoPoint geoPoint1 = new GeoPoint(coordinates[k].getOrdinate(1), coordinates[k].getOrdinate(0));
                                drawMode(ringno, geoPoint1, false, isBufferEnabled);//redo
                            }
                        }
                    } else {
                        for (GeoPoint geoPoint : geoPointList) {
                            drawMode(ringno, geoPoint, false, isBufferEnabled);//redo
                        }
                    }
                }
                if (redoPointList.size() == 0) {
                    //iMeasurementInfoListner.measurementRedo(false);
                    observableMapOperation.setRedoPossible(false);
                } else {
                    //iMeasurementInfoListner.measurementRedo(true);
                    observableMapOperation.setRedoPossible(true);
                }

            } else {
                //iMeasurementInfoListner.measurementRedo(false);
                observableMapOperation.setRedoPossible(false);
            }
                JSONObject geometryValidation = isDrawGeometryValid();
                try {
                    boolean isValid = geometryValidation.has("isGeometryValid")?geometryValidation.getBoolean("isGeometryValid"):false;
                    // int numGeometries = geometryValidation.has("numGeometries")?geometryValidation.getInt("numGeometries"):0;
                    if (isValid) {
                        observableMapOperation.setGeometryValid(true);
                    } else {
                        observableMapOperation.setGeometryValid(false);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
        }

        mMapView.invalidate();
    }

    public void clearEditingMode() {
        try {
          clearAllDrawingVariables();
            geometryType = -1;

            if (mMapView != null) {
                mMapView.getOverlays().remove(mapEventsOverlay);
                mMapView.invalidate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "clearDrawing", String.valueOf(e.getCause()));
        }
    }

    private void clearAllDrawingVariables() {
        if (drawingPolygonMap != null) {
            for (Integer ringNo : drawingPolygonMap.keySet()) {
                mMapView.getOverlays().remove(drawingPolygonMap.get(ringNo));
            }
            drawingPolygonMap = null;
        }

        if (drawingPolylineMap != null) {
            for (Integer ringNo : drawingPolylineMap.keySet()) {
                mMapView.getOverlays().remove(drawingPolylineMap.get(ringNo));
            }
            drawingPolylineMap = null;
        }

        if (pointMarker != null) {
            pointMarker.remove(mMapView);
            pointMarker = null;
        }


        if (markerListMap != null && !markerListMap.isEmpty()) {
            for (List<Marker> markerList : markerListMap.values())
                if (markerList != null && !markerList.isEmpty()) {
                    for (Marker marker : markerList) {
                        marker.remove(mMapView);
                    }
                    markerList.clear();
                }
            markerListMap = null;
        }


        if (geoPointListMap != null && !geoPointListMap.isEmpty()) {
            geoPointListMap.clear();
            geoPointListMap = null;
        }
        if (geoPointWithoutBufferListMap != null && !geoPointWithoutBufferListMap.isEmpty()) {
            geoPointWithoutBufferListMap.clear();
            geoPointWithoutBufferListMap = null;
        }


        if (undoPointTypeList != null && !undoPointTypeList.isEmpty()) {
            undoPointTypeList.clear();
            undoPointTypeList = null;
        }

        if (redoPointTypeList != null && !redoPointTypeList.isEmpty()) {
            redoPointTypeList.clear();
            redoPointTypeList = null;
        }

        if (undoPointList != null && !undoPointList.isEmpty()) {
            undoPointList.clear();
            undoPointList = null;
        }

        if (redoPointList != null && !redoPointList.isEmpty()) {
            redoPointList.clear();
            redoPointList = null;
        }
    }

    private void clearPartDrawingVariables(int currentRingNo){
        if (drawingPolygonMap != null && drawingPolygonMap.containsKey(currentRingNo)) {
            mMapView.getOverlays().remove(drawingPolygonMap.get(currentRingNo));
            drawingPolygonMap.remove(currentRingNo);
        }

        if (drawingPolylineMap != null && drawingPolylineMap.containsKey(currentRingNo)) {
            mMapView.getOverlays().remove(drawingPolylineMap.get(currentRingNo));
            drawingPolylineMap.remove(currentRingNo);
        }

        if (markerListMap != null && !markerListMap.isEmpty() && markerListMap.containsKey(currentRingNo)) {
            List<Marker> markerList = markerListMap.get(currentRingNo);
            if (markerList != null && !markerList.isEmpty()) {
                for (Marker marker : markerList) {
                    marker.remove(mMapView);
                }
                markerList.clear();
            }
            markerListMap.remove(currentRingNo);
        }


        if (geoPointListMap != null && !geoPointListMap.isEmpty() && geoPointListMap.containsKey(currentRingNo)) {
            geoPointListMap.remove(currentRingNo);
        }

        if (geoPointWithoutBufferListMap != null && !geoPointWithoutBufferListMap.isEmpty() && geoPointWithoutBufferListMap.containsKey(currentRingNo)) {
            geoPointWithoutBufferListMap.remove(currentRingNo);
        }

        if (undoPointList != null && !undoPointList.isEmpty()) {
            for(int i=0;i<undoPointList.size();i++){
                HashMap<Integer, List<GeoPoint>> hashMap = undoPointList.remove(i);
                if(hashMap!=null && !hashMap.isEmpty() && hashMap.containsKey(currentRingNo)){
                    hashMap.remove(currentRingNo);
                }
                undoPointList.add(i,hashMap);
            }
        }

        if (redoPointList != null && !redoPointList.isEmpty()) {
            for(int i=0;i<redoPointList.size();i++){
                HashMap<Integer, List<GeoPoint>> hashMap = redoPointList.remove(i);
                if(hashMap!=null && !hashMap.isEmpty() && hashMap.containsKey(currentRingNo)){
                    hashMap.remove(currentRingNo);
                }
                redoPointList.add(i,hashMap);
            }
        }
    }

    public String getMeasurementString(){
        if(measurementString==null){
        if(geometryType == POINT){
            return "";
        }else if(geometryType == POLYGON){
            return "0 Hectares";
        }
        else if (geometryType ==POLYLINE){
            return "0 meters";
        }
        }
        return measurementString;
    }
    public double getMeasurementValue(){
        return measurementValue;
    }
    public Geometry getDrawingGeometry() {

        Geometry geometry = null;

        try {
            if (geometryType == POLYGON) {

                final GeometryFactory gf = new GeometryFactory();
                List<com.vividsolutions.jts.geom.Polygon> polygons = new ArrayList<>();

                for (Integer ringNo : drawingPolygonMap.keySet()) {
                    List<GeoPoint> geoPoints = drawingPolygonMap.get(ringNo).getActualPoints();
                    if (geoPoints.size() > 2) {
                        List<Coordinate> points = new ArrayList<>();
                        for (GeoPoint geoPoint : geoPoints) {
                            points.add(new Coordinate(geoPoint.getLongitude(), geoPoint.getLatitude()));
                        }
                        if (geoPoints.get(0).getLongitude() != geoPoints.get(geoPoints.size() - 1).getLongitude()
                                || geoPoints.get(0).getLatitude() != geoPoints.get(geoPoints.size() - 1).getLatitude()) {
                            points.add(new Coordinate(geoPoints.get(0).getLongitude(), geoPoints.get(0).getLatitude())); // to close polygon attached 1st poistion cordinate.
                        }
                        polygons.add(gf.createPolygon(new LinearRing(new CoordinateArraySequence(points.toArray(new Coordinate[points.size()])), gf),
                                null));

                    }
                }
                if (polygons.size() > 0)
                    geometry = gf.createMultiPolygon(polygons.toArray(new com.vividsolutions.jts.geom.Polygon[polygons.size()]));
            } else if (geometryType == POLYLINE) {
                List<com.vividsolutions.jts.geom.LineString> linestring = new ArrayList<>();
                final GeometryFactory gf = new GeometryFactory();
                for (Integer ringNo : drawingPolylineMap.keySet()) {
                    List<GeoPoint> geoPoints = drawingPolylineMap.get(ringNo).getActualPoints();
                    if (geoPoints.size() > 1) {
                        linestring.add((LineString) getLineGeometry(geoPoints));
                    }
                }
                if (linestring.size() > 0) {
                    geometry = gf.createMultiLineString(linestring.toArray(new LineString[linestring.size()]));
                }

            } else if (geometryType == POINT) {
                if (pointMarker != null) {
                    GeoPoint geoPoint = pointMarker.getPosition();
                    geometry = getPointGeometryFromCordinates(geoPoint);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return geometry;
    }

    public JSONObject isDrawGeometryValid() {
        JSONObject geometryValidationJson = new JSONObject();
        Geometry geometry = null;
        boolean isGeometryValid = false;
        int numGeometries = 0;
        try {
            geometryValidationJson.put("isGeometryValid",isGeometryValid);
            geometryValidationJson.put("numGeometries",numGeometries);
            if (geometryType == POLYGON) {
                if(drawingPolygonMap==null || drawingPolygonMap.isEmpty()){
                    geometryValidationJson.put("isGeometryValid",false);
                    geometryValidationJson.put("numGeometries",0);
                    return geometryValidationJson;
                }else {
                    final GeometryFactory gf = new GeometryFactory();
                    List<com.vividsolutions.jts.geom.Polygon> polygons = new ArrayList<>();

                    for (Integer ringNo : drawingPolygonMap.keySet()) {
                        List<GeoPoint> geoPoints = drawingPolygonMap.get(ringNo).getActualPoints();
                        if (geoPoints.size() > 2) {
                            List<Coordinate> points = new ArrayList<>();
                            for (GeoPoint geoPoint : geoPoints) {
                                points.add(new Coordinate(geoPoint.getLongitude(), geoPoint.getLatitude()));
                            }
                            if (geoPoints.get(0).getLongitude() != geoPoints.get(geoPoints.size() - 1).getLongitude()
                                    || geoPoints.get(0).getLatitude() != geoPoints.get(geoPoints.size() - 1).getLatitude()) {
                                points.add(new Coordinate(geoPoints.get(0).getLongitude(), geoPoints.get(0).getLatitude())); // to close polygon attached 1st poistion cordinate.
                            }
                            polygons.add(gf.createPolygon(new LinearRing(new CoordinateArraySequence(points.toArray(new Coordinate[points.size()])), gf),
                                    null));
                            numGeometries++;
                        } else if(geoPoints.size()>0) {
                            geometryValidationJson.put("isGeometryValid",false);
                            geometryValidationJson.put("numGeometries",numGeometries);
                            return geometryValidationJson;
                        }
                    }
                    if (polygons.size() > 0) {
                        geometry = gf.createMultiPolygon(polygons.toArray(new com.vividsolutions.jts.geom.Polygon[polygons.size()]));
                       // return geometry.isValid();
                        geometryValidationJson.put("isGeometryValid",geometry.isValid());
                        geometryValidationJson.put("numGeometries",numGeometries);
                        return geometryValidationJson;
                    } else {
                        //return false;
                        geometryValidationJson.put("isGeometryValid",false);
                        geometryValidationJson.put("numGeometries",numGeometries);
                        return geometryValidationJson;
                    }
                }
            } else if (geometryType == POLYLINE) {
                if(drawingPolylineMap==null || drawingPolylineMap.isEmpty()){
                    //return false;
                    geometryValidationJson.put("isGeometryValid",false);
                    geometryValidationJson.put("numGeometries",0);
                    return geometryValidationJson;
                }else {
                    List<com.vividsolutions.jts.geom.LineString> linestring = new ArrayList<>();
                    final GeometryFactory gf = new GeometryFactory();
                    for (Integer ringNo : drawingPolylineMap.keySet()) {
                        List<GeoPoint> geoPoints = drawingPolylineMap.get(ringNo).getActualPoints();
                        if (geoPoints.size() > 1) {
                            linestring.add((LineString) getLineGeometry(geoPoints));
                            numGeometries++;
                        } else {
                            //return false;
                           /* geometryValidationJson.put("isGeometryValid",false);
                            geometryValidationJson.put("numGeometries",numGeometries);
                            return geometryValidationJson;*/
                        }
                    }
                    if (linestring.size() > 0) {
                        geometry = gf.createMultiLineString(linestring.toArray(new LineString[linestring.size()]));
                        //return geometry.isValid();
                        geometryValidationJson.put("isGeometryValid",geometry.isValid());
                        geometryValidationJson.put("numGeometries",numGeometries);
                        return geometryValidationJson;
                    } else {
                        //return false;
                        geometryValidationJson.put("isGeometryValid",false);
                        geometryValidationJson.put("numGeometries",numGeometries);
                        return geometryValidationJson;
                    }
                }
            } else if (geometryType == POINT) {
                if (pointMarker != null) {
                    GeoPoint geoPoint = pointMarker.getPosition();
                    geometry = getPointGeometryFromCordinates(geoPoint);
                    numGeometries++;
                    //return geometry.isValid();
                    geometryValidationJson.put("isGeometryValid",geometry.isValid());
                    geometryValidationJson.put("numGeometries",numGeometries);
                    return geometryValidationJson;
                }else {
                    //return false;
                    geometryValidationJson.put("isGeometryValid",false);
                    geometryValidationJson.put("numGeometries",numGeometries);
                    return geometryValidationJson;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //return false;
        return geometryValidationJson;
    }

    /*public boolean isDrawGeometryValid() {

        Geometry drawingGeometry = getDrawingGeometry();
        if (drawingGeometry != null) {
            boolean isPolygonValid = drawingGeometry.isValid();
            if (isPolygonValid) {
                return true;
            }
        }
        return false;
    }*/

    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof ObservableDrawingState) {
            ObservableDrawingState observableDrawingState = (ObservableDrawingState) observable;
            if (observableDrawingState.isEditingStarted()) {
                if (observableDrawingState.isEditingOngoing()) {
                    makeOtherPolygonsInactive(currentRingNo,false);//change false
                } else {
                    makeOtherPolygonsInactive(-1,false);
                }
            } else
                makeOtherPolygonsInactive(-1,false);
        }
        else if (observable instanceof ObservableMapOperation) {
            ObservableMapOperation observableMapOperation = (ObservableMapOperation) observable;
             if (observableMapOperation.isPartSelected()) {
                    makeOtherPolygonsInactive(currentRingNo,!observableMapOperation.isBufferEnabled());//change false to isbufferenabled
            } else {
                if(observableMapOperation.isDrawingOngoing()){
                    makeOtherPolygonsInactive(currentRingNo,!observableMapOperation.isBufferEnabled());//change false
                }else {
                    makeOtherPolygonsInactive(-1,false);
                }
            }
        }
    }

    public void addVertexInBuffer(double lat, double lng, double selectedBufferWidthValue, String selectedBufferDirection) {
        observableMapOperation.setPartSelected(false);
        observableMapOperation.setAddPartClicked(false);
        observableMapOperation.setDrawingOngoing(true);
        if (geoPointWithoutBufferListMap == null)
            geoPointWithoutBufferListMap = new HashMap<>();

        if (!geoPointWithoutBufferListMap.containsKey(currentRingNo)) {
            geoPointWithoutBufferListMap.put(currentRingNo, new ArrayList<>());
        }
        geoPointWithoutBufferListMap.get(currentRingNo).add(new GeoPoint(lat, lng));

        if (markerListMap != null && markerListMap.containsKey(currentRingNo)) {
            List<Marker> markerList = markerListMap.get(currentRingNo);
            if (markerList != null && !markerList.isEmpty()) {
                for (Marker marker : markerList) {
                    marker.remove(mMapView);
                }
                markerList.clear();
            }
            markerListMap.get(currentRingNo).clear();
        }
        if (geoPointListMap != null && geoPointListMap.containsKey(currentRingNo)) {
            geoPointListMap.get(currentRingNo).clear();
        }
       /* if(drawingPolygonMap!=null && drawingPolygonMap.containsKey(currentRingNo)){
            mMapView.getOverlays().remove(drawingPolygonMap.get(currentRingNo));
            drawingPolygonMap.remove(currentRingNo);
        }
        if(drawingPolylineMap!=null && drawingPolylineMap.containsKey(currentRingNo)){
            mMapView.getOverlays().remove(drawingPolylineMap.get(currentRingNo));
            drawingPolylineMap.remove(currentRingNo);
        }*/
        mMapView.invalidate();

        double convertDistance = convertMetersToDecimalDegrees(selectedBufferWidthValue, lat);

        Geometry newGraphicGeometry = createSubGeometryFromGeoPointsUsingBuffer(geoPointWithoutBufferListMap.get(currentRingNo), convertDistance, selectedBufferDirection, false);
        if (newGraphicGeometry != null) {
            Log.e("p", newGraphicGeometry.getCoordinates().length + "");

            Coordinate[] coordinates = newGraphicGeometry.getCoordinates();
            for (int k = 0; k < coordinates.length; k++) {
                GeoPoint geoPoint = new GeoPoint(coordinates[k].getOrdinate(1), coordinates[k].getOrdinate(0));
                addVertex(geoPoint, true);
            }
        }

    }

    public void deletePart(int currentRingNo) {
        isDrawingOngoing=false;
        try {
            if(currentRingNo==-1){
                currentRingNo = this.currentRingNo;
            }
            clearPartDrawingVariables(currentRingNo);
            if (mMapView != null) {
                mMapView.invalidate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "clearDrawing", String.valueOf(e.getCause()));
        }
    }

    public void startEditingFirstPart() {
        currentRingNo = 0;
       // currentRingNoChangedByTapInside = true;
        //make otherpolygons immovable
       // makeOtherPolygonsInactive(0,true);

        isDrawingOngoing = true;
        observableMapOperation.setPartSelected(true);
        currentRingNoChangedByTapInside = false;
        if (currentRingNo != -1) {
            makeOtherPolygonsInactive(currentRingNo, true);
        }
    }

    public void saveState() {
        try {

            if (drawingPolygonMap != null) {
                for (Integer ringNo : drawingPolygonMap.keySet()) {
                    Polygon polygon = drawingPolygonMap.get(ringNo);
                    savedDrawingPolygonMap.put(ringNo,polygon);
                }
            }

            if (drawingPolylineMap != null) {
                for (Integer ringNo : drawingPolylineMap.keySet()) {
                    Polyline polyline = drawingPolylineMap.get(ringNo);
                    savedDrawingPolylineMap.put(ringNo,polyline);
                }
            }

            if (pointMarker != null) {
                savedPointMarker = pointMarker;
            }


            if (markerListMap != null && !markerListMap.isEmpty()) {
                for (Integer index : markerListMap.keySet()) {
                    List<Marker> markerList = markerListMap.get(index);
                    if (markerList != null && ! markerList.isEmpty()) {
                        List<Marker> markerListCopy = new ArrayList<>();
                        for(Marker marker:markerList) {
                            markerListCopy.add(marker);
                        }
                        savedMarkerListMap.put(index, markerListCopy);
                    }else {
                        savedMarkerListMap.put(index,new ArrayList<>());
                    }
                }
            }


            if (geoPointListMap != null && !geoPointListMap.isEmpty()) {
                for (Integer index : geoPointListMap.keySet()) {
                    List<GeoPoint> geoPointList = geoPointListMap.get(index);
                    if (geoPointList != null && ! geoPointList.isEmpty()) {
                        List<GeoPoint> geoPointListCopy = new ArrayList<>();
                        for(GeoPoint geoPoint:geoPointList) {
                            geoPointListCopy.add(geoPoint);
                        }
                        savedGeoPointListMap.put(index,geoPointListCopy);
                    }else {
                        savedGeoPointListMap.put(index,new ArrayList<>());
                    }
                }
            }

            if (geoPointWithoutBufferListMap != null && !geoPointWithoutBufferListMap.isEmpty()) {
                for (Integer index : geoPointWithoutBufferListMap.keySet()) {
                    List<GeoPoint> geoPointList = geoPointWithoutBufferListMap.get(index);
                    if (geoPointList != null && ! geoPointList.isEmpty()) {
                        List<GeoPoint> geoPointListCopy = new ArrayList<>();
                        for(GeoPoint geoPoint:geoPointList) {
                            geoPointListCopy.add(geoPoint);
                        }
                        savedGeoPointWithoutBufferListMap.put(index,geoPointListCopy);
                    }else {
                        savedGeoPointWithoutBufferListMap.put(index,new ArrayList<>());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "savestate", String.valueOf(e.getCause()));
        }
    }

    public void restoreStateold(boolean restoreAll,int restoreRingNo){
        try {
            //clear undo redo list for ring no
            if(restoreAll){
                clearAllDrawingVariables();
            }else {
                clearPartDrawingVariables(restoreRingNo);
            }
            if (mMapView != null) {
                mMapView.invalidate();
            }
            observableMapOperation.setUndoPossible(false);
            observableMapOperation.setRedoPossible(false);

            if (savedDrawingPolygonMap != null) {
                for (Integer ringNo : savedDrawingPolygonMap.keySet()) {
                    if(restoreAll || ringNo==restoreRingNo) {
                        Polygon polygon = savedDrawingPolygonMap.get(ringNo);
                        drawingPolygonMap.put(ringNo, polygon);
                        if(mMapView!=null) {
                            mMapView.getOverlays().add(drawingPolygonMap.get(ringNo));
                        }
                    }
                }
            }

            if (savedDrawingPolylineMap != null) {
                for (Integer ringNo : savedDrawingPolylineMap.keySet()) {
                    if(restoreAll || ringNo==restoreRingNo) {
                        Polyline polyline = savedDrawingPolylineMap.get(ringNo);
                        drawingPolylineMap.put(ringNo, polyline);
                        if(mMapView!=null) {
                            mMapView.getOverlays().add(drawingPolylineMap.get(ringNo));
                        }
                    }
                }
            }

            if (savedPointMarker != null) {
                if(restoreAll) {
                    pointMarker = savedPointMarker;
                }
            }


            if (savedMarkerListMap != null && !savedMarkerListMap.isEmpty()) {
                for (Integer index : savedMarkerListMap.keySet()) {
                    if(restoreAll || index==restoreRingNo) {
                        List<Marker> markerList = savedMarkerListMap.get(index);
                        if (markerList != null && ! markerList.isEmpty()) {
                            markerListMap.put(index, new ArrayList<Marker>(markerList));
                            if(mMapView!=null) {
                                for (Marker marker : markerListMap.get(restoreRingNo)) {
                                    mMapView.getOverlays().add(marker);
                                }
                            }
                        }
                        else {
                            markerListMap.put(index, new ArrayList<>());
                        }
                    }
                }
            }


            if (savedGeoPointListMap != null && !savedGeoPointListMap.isEmpty()) {
                for (Integer index : savedGeoPointListMap.keySet()) {
                    if(restoreAll || index==restoreRingNo) {
                        List<GeoPoint> geoPointList = savedGeoPointListMap.get(index);
                        if (geoPointList != null && ! geoPointList.isEmpty()) {
                            List<GeoPoint> geoPointListCopy = new ArrayList<>();
                            for(GeoPoint geoPoint:geoPointList){
                                geoPointListCopy.add(geoPoint);
                            }
                            geoPointListMap.put(index, geoPointListCopy);
                        }
                        else {
                            geoPointListMap.put(index, new ArrayList<>());
                        }
                    }
                }
            }

            if (savedGeoPointWithoutBufferListMap != null && !savedGeoPointWithoutBufferListMap.isEmpty()) {
                for (Integer index : savedGeoPointWithoutBufferListMap.keySet()) {
                    if(restoreAll || index==restoreRingNo) {
                        List<GeoPoint> geoPointList = savedGeoPointWithoutBufferListMap.get(index);
                        if (geoPointList != null && ! geoPointList.isEmpty()) {
                            List<GeoPoint> geoPointListCopy = new ArrayList<>();
                            for(GeoPoint geoPoint:geoPointList){
                                geoPointListCopy.add(geoPoint);
                            }
                            geoPointWithoutBufferListMap.put(index, geoPointListCopy);
                        }
                        else {
                            geoPointWithoutBufferListMap.put(index, new ArrayList<>());
                        }
                    }
                }
            }



            if (mMapView != null) {
                mMapView.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "savestate", String.valueOf(e.getCause()));
        }
    }

    public void restoreState(boolean restoreAll,int restoreRingNo){
        try {
            //clear undo redo list for ring no
            if(restoreAll){
                clearAllDrawingVariables();
            }else {
                clearPartDrawingVariables(restoreRingNo);
            }
            if (mMapView != null) {
                mMapView.invalidate();
            }
            observableMapOperation.setUndoPossible(false);
            observableMapOperation.setRedoPossible(false);

           /* if (savedMarkerListMap != null && !savedMarkerListMap.isEmpty()) {
                for (Integer index : savedMarkerListMap.keySet()) {
                    if(restoreAll || index==restoreRingNo) {
                        if(markerListMap.containsKey(index)){
                            markerListMap.get(index).clear();
                        }
                        List<Marker> markerList = savedMarkerListMap.get(index);
                        if (markerList != null && ! markerList.isEmpty()) {
                            markerListMap.put(index, new ArrayList<Marker>(markerList));
                            if(mMapView!=null) {
                                for (Marker marker : markerListMap.get(restoreRingNo)) {
                                    mMapView.getOverlays().add(marker);
                                }
                            }
                        }
                        else {
                            markerListMap.put(index, new ArrayList<>());
                        }
                    }
                }
            }

            if (savedGeoPointListMap != null && !savedGeoPointListMap.isEmpty()) {
                for (Integer index : savedGeoPointListMap.keySet()) {
                    if(restoreAll || index==restoreRingNo) {
                        List<GeoPoint> geoPointList = savedGeoPointListMap.get(index);
                        if (geoPointList != null && ! geoPointList.isEmpty()) {
                            List<GeoPoint> geoPointListCopy = new ArrayList<>();
                            for(GeoPoint geoPoint:geoPointList){
                                geoPointListCopy.add(geoPoint);
                            }
                            geoPointListMap.put(index, geoPointListCopy);
                        }
                        else {
                            geoPointListMap.put(index, new ArrayList<>());
                        }
                    }
                }
            }

            if (savedGeoPointWithoutBufferListMap != null && !savedGeoPointWithoutBufferListMap.isEmpty()) {
                for (Integer index : savedGeoPointWithoutBufferListMap.keySet()) {
                    if(restoreAll || index==restoreRingNo) {
                        List<GeoPoint> geoPointList = savedGeoPointWithoutBufferListMap.get(index);
                        if (geoPointList != null && ! geoPointList.isEmpty()) {
                            List<GeoPoint> geoPointListCopy = new ArrayList<>();
                            for(GeoPoint geoPoint:geoPointList){
                                geoPointListCopy.add(geoPoint);
                            }
                            geoPointWithoutBufferListMap.put(index, geoPointListCopy);
                        }
                        else {
                            geoPointWithoutBufferListMap.put(index, new ArrayList<>());
                        }
                    }
                }
            }
*/
            if (geometryType == POINT) {//hence before drawmode, we need to set geometry type
                drawPoint(savedPointMarker.getPosition());
                observableMapOperation.setGeometryValid(true);
                observableMapOperation.setDrawingOngoing(true);
            }else {
                //drawGeometry(geometryType, geoPointListMap, drag_state_end, false, false);

                if (savedGeoPointListMap != null && !savedGeoPointListMap.isEmpty()) {
                    for (Integer index : savedGeoPointListMap.keySet()) {
                        if(restoreAll || index==restoreRingNo) {
                            List<GeoPoint> geoPointList = savedGeoPointListMap.get(index);
                            if (geoPointList != null && ! geoPointList.isEmpty()) {
                                List<GeoPoint> geoPointListCopy = new ArrayList<>();
                                for(GeoPoint geoPoint:geoPointList){
                                    geoPointListCopy.add(geoPoint);
                                }
                                if (geoPointListCopy.size() > 3) {
                                    GeoPoint lastPoint = geoPointListCopy.get(geoPointListCopy.size() - 1);
                                    GeoPoint firstPoint = geoPointListCopy.get(0);
                                    //if first and last points are same, we need to add new vertex at last-1 and last's mid
                                    if (firstPoint.getLatitude() == lastPoint.getLatitude() && firstPoint.getLongitude() == lastPoint.getLongitude()) {
                                        //ignore this last point
                                        geoPointListCopy.remove(geoPointListCopy.size() - 1);
                                    }
                                }
                                for (GeoPoint geoPoint : geoPointListCopy) {
                                    drawMode(index, geoPoint, false, false);//main-editfeatureByTap
                                }
                            }
                            else {
                                geoPointListMap.put(index, new ArrayList<>());
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "savestate", String.valueOf(e.getCause()));
        }
    }
}
