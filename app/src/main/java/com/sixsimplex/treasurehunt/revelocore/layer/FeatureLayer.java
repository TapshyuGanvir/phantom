package com.sixsimplex.treasurehunt.revelocore.layer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import androidx.fragment.app.FragmentActivity;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.data.FeatureTable;
import com.sixsimplex.treasurehunt.revelocore.data.GeoJsonUtils;
import com.sixsimplex.treasurehunt.revelocore.gpkg.overlay.OsmMapShapeConverter;
import com.sixsimplex.treasurehunt.revelocore.graph.jsongraph.JSONGraph;
import com.sixsimplex.treasurehunt.Phantom1.traversalgraph.TraversalGraph;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.Operation;
import com.sixsimplex.treasurehunt.revelocore.util.VectorDrawableUtils;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FeatureLayer {

    private final String className = "FeatureLayer";
    private final List<Overlay> selectedFeatures = new ArrayList<>();
    private final List<Overlay> highlightedFeatures = new ArrayList<>();
    String lastSelectionColor = "";
    private String name;
    private String label;
    private String type;
    private String geometryType;
    private String w9IdProperty;
    private String labelPropertyName;
    private List<String> labelExpressionProperties;
    private String categoryPropertyName;
    private String abbr;
    private boolean isLocked;
    private boolean hasShadowTable;
    private String selectedRendererName;
    private JSONObject idGenRules;
    private SimpleStyleModel simpleStyle;
    private UniqueStyleValueModel uniqueValueStyle;
    private List<Attribute> properties;
    private HashMap<String, Attribute> propertiesHashMap;
    private Map<Integer, PropertyGroupsModel> propertyGroups;
    private JSONObject domainValues;
    private JSONGraph dependantPropertiesJGraph;
    private String dependantPropertiesJGraphStr;
    private JSONObject perspective;
    private JSONObject categories;
    private String idGenerationType;
    private boolean isInBoth = false;
    private Drawable layerLegendDrawable;
    private FolderOverlay folderOverlay;
    private SimpleFastPointOverlay labelOverlay;
    private boolean isRoot;
    private boolean isSelected;
    private MapView mapView;
    private FeatureTable featureTable;
    private List<IGeoPoint> labelList;
    private Map<String, LabelledGeoPoint> labelListMap = null;
    private Map<String, List<Overlay>> overlayMap = null;
    private List<Feature> inRangeTargetList = null;
    private List<Feature> excludedFeatureList = null;
    GetSelectedFeature getSelectedFeature;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public FeatureLayer() {
    }

    public FeatureLayer(String name, String label, String type, String geometryType, String w9IdProperty, String labelPropertyName, String categoryPropertyName, String abbr, String islocked, String selectedRendererName, String idgenrules, String simpleStyle, String uniqueStyle, String properties, String propertyGroups, String domainValues, String categories, String hasShadowTable) {
        setName(name);
        setLabel(label);
        setType(type);
        setGeometryType(geometryType);
        setW9IdProperty(w9IdProperty);
        setLabelPropertyName(labelPropertyName);
        setCategoryPropertyName(categoryPropertyName);
        setAbbr(abbr);
        setLocked(islocked);
        setSelectedRendererName(selectedRendererName);
        setIdGenRules(idgenrules);
        setSimpleStyle(simpleStyle);
        setUniqueValueStyle(uniqueStyle);
        setProperties(properties);
        setPropertyGroups(propertyGroups);
        setDomainValues(domainValues);
        setCategories(categories);
        setHasShadowTable(hasShadowTable);
    }

    public FeatureLayer(String name, String label, String type, String geometryType, String w9IdProperty, String labelPropertyName, String categoryPropertyName, String abbr, boolean islocked, String selectedRendererName, JSONObject idgenrules, SimpleStyleModel simpleStyle, UniqueStyleValueModel uniqueStyle, List<Attribute> properties, Map<Integer, PropertyGroupsModel> propertyGroups, JSONObject domainValues, String dependantPropertiesGraphStr, JSONObject categories, boolean hasShadowTable) {
        setName(name);
        setLabel(label);
        setType(type);
        setGeometryType(geometryType);
        setW9IdProperty(w9IdProperty);
        setLabelPropertyName(labelPropertyName);
        setCategoryPropertyName(categoryPropertyName);
        setDependantPropertiesJGraph(dependantPropertiesGraphStr);
        setAbbr(abbr);
        this.isLocked = islocked;
        setSelectedRendererName(selectedRendererName);
        this.idGenRules = idgenrules;
        this.simpleStyle = simpleStyle;
        this.uniqueValueStyle = uniqueStyle;
        this.properties = properties;
        this.propertyGroups = propertyGroups;
        this.domainValues = domainValues;
        this.categories = categories;
        this.hasShadowTable = hasShadowTable;
    }

    public String getLastSelectionColor() {
        return lastSelectionColor;
    }

    public JSONGraph getDependantPropertiesJGraph() {
        return dependantPropertiesJGraph;
    }

    public void setDependantPropertiesJGraph(String dependantPropertiesGraphStr) {

        if (dependantPropertiesGraphStr == null || dependantPropertiesGraphStr.isEmpty()) {
            this.dependantPropertiesJGraph = null;
            this.dependantPropertiesJGraphStr = "";
        } else {
            this.dependantPropertiesJGraphStr = dependantPropertiesGraphStr;
            try {
                JSONObject inputJson = new JSONObject(StringEscapeUtils.unescapeJson(dependantPropertiesJGraphStr));
                Map<String, JSONObject> nodesList = new HashMap<>();
                List<JSONObject> edgesList = new ArrayList<>();
                if (inputJson.has("vertices")) {
                    JSONObject verticesJobj = inputJson.getJSONObject("vertices");
                    Iterator<String> itrVertices = verticesJobj.keys();
                    while (itrVertices.hasNext()) {
                        JSONObject nodeJobj = verticesJobj.getJSONObject(itrVertices.next());
                        nodesList.put(nodeJobj.getString("name"), nodeJobj);
                    }
                } else {
                    this.dependantPropertiesJGraph = null;
                    this.dependantPropertiesJGraphStr = "";
                    return;
                }
                if (!nodesList.isEmpty() && inputJson.has("edges")) {
                    JSONArray edgesJArray = inputJson.getJSONArray("edges");
                    for (int i = 0; i < edgesJArray.length(); i++) {
                        JSONObject edgeJson = edgesJArray.getJSONObject(i);
                        edgesList.add(edgeJson);
                    }
                } else {
                    this.dependantPropertiesJGraph = null;
                    this.dependantPropertiesJGraphStr = "";
                    return;
                }

                if (nodesList.isEmpty() || edgesList.isEmpty()) {
                    this.dependantPropertiesJGraph = null;
                    this.dependantPropertiesJGraphStr = "";
                    return;
                }

                JSONGraph jsonGraph = new JSONGraph(name);
                for (JSONObject node : nodesList.values()) {
                    jsonGraph.addVertex(node);
                }
                for (JSONObject edgeJSON : edgesList) {
                    String fromNodeName = edgeJSON.getString("fromNodeName");
                    String toNodeName = edgeJSON.getString("toNodeName");
                    JSONObject properties = new JSONObject();
                    if (edgeJSON.has("properties")) {
                        properties = edgeJSON.getJSONObject("properties");
                    }
                    if (nodesList.containsKey(fromNodeName) && nodesList.containsKey(toNodeName)) {
                        JSONObject fromNode = nodesList.get(fromNodeName);
                        JSONObject toNode = nodesList.get(toNodeName);
                        jsonGraph.addEdge(fromNode, toNode, properties);
                    }
                }
                dependantPropertiesJGraph = jsonGraph;
                this.dependantPropertiesJGraphStr = dependantPropertiesGraphStr;
            } catch (Exception e) {
                e.printStackTrace();
                this.dependantPropertiesJGraph = null;
                this.dependantPropertiesJGraphStr = "";
            }
        }
    }

    public Map<String, List<Overlay>> getOverlayMap() {
        return overlayMap;
    }

    public SimpleFastPointOverlay getLabelOverlay() {
        return labelOverlay;
    }

    public FolderOverlay getFeatureOverlay() {
        return folderOverlay;
    }

    public boolean isInBoth() {
        return isInBoth;
    }

    public void setInBoth(boolean inBoth) {
        isInBoth = inBoth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    public String getW9IdProperty() {
        return w9IdProperty;
    }

    public void setW9IdProperty(String w9IdProperty) {
        this.w9IdProperty = w9IdProperty;
    }

    public String getLabelPropertyName() {
        return labelPropertyName;
    }

    public void setLabelPropertyName(String labelPropertyName) {
        this.labelPropertyName = labelPropertyName;
        String taskName = "setLabelPropertyName";
        try {
            this.labelExpressionProperties = new ArrayList<>();
            if (labelPropertyName != null && !labelPropertyName.isEmpty()) {
                ReveloLogger.info(className, taskName, "setting Labelexpression prop list.. Entity.lableproperty = " + labelPropertyName);
                if (labelPropertyName.contains("+")) {
                    String[] labelComponents = labelPropertyName.split("\\+");

                    for (int i = 0; i < labelComponents.length; i++) {
                        String component = labelComponents[i];
                        if (component.contains("{")) {
                            try {
                                String columnName = component.replace("{", "").replace("}", "");
                                labelExpressionProperties.add(columnName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (labelPropertyName.contains("{")) {
                    try {
                        String columnName = labelPropertyName.replace("{", "").replace("}", "");
                        labelExpressionProperties.add(columnName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    labelExpressionProperties.add(labelPropertyName);
                }
                ReveloLogger.error(className, taskName, "setting Labelexpression prop list = " + labelExpressionProperties);
            }
        } catch (Exception e) {
            ReveloLogger.error(className, taskName, "exception setting Labelexpression prop list  " + e.getMessage());
            e.printStackTrace();
        }

    }

    public String getCategoryPropertyName() {
        return categoryPropertyName;
    }

    public void setCategoryPropertyName(String categoryPropertyName) {
        this.categoryPropertyName = categoryPropertyName;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(String locked) {
        isLocked = locked.equalsIgnoreCase("true");
    }

    public String getSelectedRendererName() {
        return selectedRendererName;
    }

    public void setSelectedRendererName(String selectedRendererName) {
//        if(name!=null && name.equalsIgnoreCase(DeliveryDataModel.traversalEntityName)){
//            selectedRendererName = "uniqueValue";
//        }
        this.selectedRendererName = selectedRendererName;
    }

    public String getIdGenerationType() {
        return idGenerationType;
    }

    public void setIdGenerationType(String idGenerationType) {
        this.idGenerationType = idGenerationType;
    }

    public JSONObject getIdGenRules() {
        return idGenRules;
    }

    public void setIdGenRules(String rule) {

        idGenRules = null;

        if (!TextUtils.isEmpty(rule)) {
            try {
                idGenRules = new JSONObject(StringEscapeUtils.unescapeJson(rule));
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "setSimpleStyle", String.valueOf(e.getCause()));
            }
        }
    }

    public SimpleStyleModel getSimpleStyle() {
        return simpleStyle;
    }

    public void setSimpleStyle(String style) {

        simpleStyle = null;

        if (!TextUtils.isEmpty(style)) {
            try {
                JSONObject styleObject = new JSONObject(StringEscapeUtils.unescapeJson(style));
                simpleStyle = SimpleStyleModel.parseSimpleStyleObject(styleObject);
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "setSimpleStyle", String.valueOf(e.getCause()));
            }
        }
    }

    public UniqueStyleValueModel getUniqueValueStyle() {
        return uniqueValueStyle;
    }

    public void setUniqueValueStyle(String style) {

        uniqueValueStyle = null;

        if (!TextUtils.isEmpty(style)) {
            try {
                JSONObject styleObject = new JSONObject(StringEscapeUtils.unescapeJson(style));
                uniqueValueStyle = UniqueStyleValueModel.parseUniqueValueObject(styleObject);
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "setUniqueValueStyle", String.valueOf(e.getCause()));
            }
        }
    }

    public List<Attribute> getProperties() {
        return properties;
    }

    public void setProperties(String propertyString) {

        properties = null;

        if (!TextUtils.isEmpty(propertyString)) {
            try {
                JSONArray propertiesArray = new JSONArray(StringEscapeUtils.unescapeJson(propertyString));
                properties = Attribute.parseAttributeJsonArray(propertiesArray);
                if (propertiesHashMap == null)
                    propertiesHashMap = new HashMap<>();
                for (Attribute attribute : properties) {
                    propertiesHashMap.put(attribute.getName(), attribute);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "setProperties", String.valueOf(e.getCause()));
            }
        }
    }

    public HashMap<String, Attribute> getPropertiesHashMap() {
        return propertiesHashMap;
    }

    public Map<Integer, PropertyGroupsModel> getPropertyGroups() {
        return propertyGroups;
    }

    public void setPropertyGroups(String propertyGroupString) {

        propertyGroups = null;

        if (!TextUtils.isEmpty(propertyGroupString)) {

            try {

                JSONArray propertyGroupArray = new JSONArray(StringEscapeUtils.unescapeJson(propertyGroupString));

                Map<Integer, PropertyGroupsModel> propertyGroupsModelMap = new HashMap<>();

                for (int p = 0; p < propertyGroupArray.length(); p++) {

                    JSONObject propertyGroupObject = propertyGroupArray.getJSONObject(p);

                    PropertyGroupsModel propertyGroupsModel = PropertyGroupsModel.parsePropertyGroupJson(propertyGroupObject);
                    propertyGroupsModelMap.put(propertyGroupsModel.getIndex(), propertyGroupsModel);
                }

                propertyGroups = propertyGroupsModelMap;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JSONObject getDomainValues() {
        return domainValues;
    }

    public void setDomainValues(String values) {

        domainValues = null;

        if (!TextUtils.isEmpty(values)) {
            try {
                domainValues = new JSONObject(StringEscapeUtils.unescapeJson(values));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JSONObject getPerspective() {
        return perspective;
    }

    public void setPerspective(JSONObject perspective) {
        this.perspective = perspective;
    }

    public JSONObject getCategories() {
        return categories;
    }

    public void setCategories(String categoryString) {

        categories = null;

        if (!TextUtils.isEmpty(categoryString)) {
            try {
                categories = new JSONObject(StringEscapeUtils.unescapeJson(categoryString));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getFeatureTotal() {
        if (overlayMap != null) {
            return overlayMap.size();
        }
        return 0;
    }

    public FeatureTable getFeatureTable() {
        return featureTable;
    }

    public void setFeatureTable(FeatureTable featureTable) {
        this.featureTable = featureTable;
    }


    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(MapView mapView) {
        this.mapView = mapView;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public List<Overlay> getSelectedFeature() {
        return selectedFeatures;
    }

    public List<Overlay> getHighlightedFeature() {
        return highlightedFeatures;
    }

    public Drawable getLayerLegendDrawable() {
        return layerLegendDrawable;
    }

    public void setLayerLegendDrawable(Drawable layerLegendDrawable) {
        this.layerLegendDrawable = layerLegendDrawable;
    }

    //todo discuss -  should not this method be imapview.showfeatureOnMap?
    public void showFeatureOnMap(Activity activity, TraversalGraph traversalGraph, GetSelectedFeature getSelectedFeature) {
        boolean isVisibleByDefault = true;
        boolean isTraversalApplicable = false;
        try {
            if (traversalGraph != null) {
                isVisibleByDefault = traversalGraph.isVisibleByDefault();
                isTraversalApplicable = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (featureTable != null) {

                List<String> requiredColumnsList = new ArrayList<>();
                requiredColumnsList.add(w9IdProperty);
                requiredColumnsList.add("firstname");
                requiredColumnsList.add("lastname");
                if (!selectedRendererName.equalsIgnoreCase("simple")) {
                    UniqueStyleValueModel uniqueStyleValueModel = uniqueValueStyle;
                    String fieldName = uniqueStyleValueModel.getFieldName();
                    if (fieldName != null && !fieldName.isEmpty()) {
                        requiredColumnsList.add(fieldName);
                    }
                }


                OsmMapShapeConverter converter = new OsmMapShapeConverter();
                overlayMap = new HashMap<>();
                labelListMap = new HashMap<>();

                labelList = new ArrayList<>();


                folderOverlay = new FolderOverlay();
                folderOverlay.setName(name);

                if (isTraversalApplicable) {
                   /* Map<String, Object> conditionMap = new HashMap<>();
                    conditionMap.put("isVisited", true);*/

                    LinkedList<JSONObject> jsonObjectLinkedList = traversalGraph.getOrderedVerticesList(null);

                    if (jsonObjectLinkedList != null && jsonObjectLinkedList.size() > 0) {
                        int i = 1;
                        for (JSONObject vertexJson : jsonObjectLinkedList) {
                            String w9id = vertexJson.getString("w9id");
                            Feature feature = featureTable.getFeature(w9IdProperty, w9id, activity, true, false, true);
                            if (feature != null) {

                                feature.setFeatureLabel(i + " " + feature.getFeatureLabel());
                                ReveloLogger.debug(className, "addFeatureOnMap", "adding feature " + w9id + " - " + i + " " + feature.getFeatureLabel() + " on map as it is not in traversal graph");

                                addFeatureOnMap(feature.getGeoJsonGeometry(), w9id, feature, vertexJson.getBoolean("isVisited"), activity, getSelectedFeature);
                            }
                            i++;
                        }

                    } else {
                        ReveloLogger.debug(className, "addFeatureOnMap", "no features are visited..hence all are invisible");
                    }

                } else {
                    List<Feature> featureList = featureTable.getallFeaturesList(activity, requiredColumnsList, null, false, true);
                    for (Feature feature : featureList) {

                        // Geometry geometry = feature.getGeometry();
                        JSONObject geometryGeoJson = feature.getGeoJsonGeometry();
                        Object id = feature.getFeatureId();
                        String labelObj = feature.getFeatureLabel();

                        if (id != null) {
                            String w9Id = String.valueOf(id);

                            if (geometryGeoJson != null) {
                                if (isTraversalApplicable) {

                                    Map<String, Object> conditionMap = new HashMap<>();
                                    conditionMap.put("w9id", w9Id);
                                    List<JSONObject> cuurentVertexList = traversalGraph.getVertices(conditionMap);
                                    if (cuurentVertexList != null && cuurentVertexList.size() == 1) {
                                        feature.setFeatureLabel(labelObj);
                                        ReveloLogger.debug(className, "addFeatureOnMap", "adding feature " + w9Id + " - " + labelObj + " on map as it is not in traversal graph");
                                        boolean showfeature = false;
                                        if (!isVisibleByDefault) {
                                            if (cuurentVertexList.get(0).getBoolean("isVisited")) {
                                                showfeature = true;
                                            }
                                        }
                                        addFeatureOnMap(geometryGeoJson, w9Id, feature, showfeature, activity, getSelectedFeature);
                                    } else {
                                        ReveloLogger.debug(className, "addFeatureOnMap", "feature " + w9Id + " not brought on map as it is not in traversal graph");
                                    }
                                } else {
                                    addFeatureOnMap(geometryGeoJson, w9Id, feature, isVisibleByDefault, activity, getSelectedFeature);
                                }


                            }
                        }
                    }
                }


                labelOverlay = createLabelOverlay(labelList);


            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "addFeatureToMap", String.valueOf(e.getCause()));
        }
    }


    public void showFeatureOnMapForDelivery(Activity activity, TraversalGraph traversalGraph, List<Feature> featureListTraversal, GetSelectedFeature getSelectedFeature) {
        boolean isVisibleByDefault = true;
        boolean isTraversalApplicable = true;
        this.getSelectedFeature=getSelectedFeature;
        try {
            if (traversalGraph != null) {
                isVisibleByDefault = traversalGraph.isVisibleByDefault();
                isTraversalApplicable = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (featureTable != null) {

                List<String> requiredColumnsList = new ArrayList<>();
                requiredColumnsList.add(w9IdProperty);
                if (!selectedRendererName.equalsIgnoreCase("simple")) {
                    UniqueStyleValueModel uniqueStyleValueModel = uniqueValueStyle;
                    String fieldName = uniqueStyleValueModel.getFieldName();
                    if (fieldName != null && !fieldName.isEmpty()) {
                        requiredColumnsList.add(fieldName);
                    }
                }


                OsmMapShapeConverter converter = new OsmMapShapeConverter();
                overlayMap = new HashMap<>();
                labelListMap = new HashMap<>();

                labelList = new ArrayList<>();


                folderOverlay = new FolderOverlay() {
//                    @Override
//                    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView) {
//                        selectFeature(event,
//                                new FeatureLayer.GetSelectedFeature() {
//                                    @Override
//                                    public void getSelectedFeature(Context context, List<Overlay> features) {
//                                        if (!getJtsGeometryType().equalsIgnoreCase("point")) {
//                                            onFeatureSelected.onFeatureSelected(features);//for polygon and polyline
//                                        }
//                                    }
//                                });
//                        return false;
//                    };
                };
                folderOverlay.setName(name);

                if (isTraversalApplicable) {
                    int count = 0;
                    for (Feature feature : featureListTraversal) {
                        count++;
                        String featureLabel = String.valueOf(count);
                        String customFillColor = "";
                        boolean isDelivered = (boolean) feature.getAttributes().get("isdelivered");
                        boolean isSkipped = (boolean) feature.getAttributes().get("skipped");
//                        boolean isVisited = (boolean) feature.getAttributes().get("isvisited");

                        if (!isSkipped && !isDelivered) {
                            customFillColor = activity.getResources().getString(R.string.pending);
                        } else if (isSkipped && !isDelivered) {
                            customFillColor = activity.getResources().getString(R.string.incomplete);
                        } else if (!isSkipped && isDelivered) {
                            customFillColor = activity.getResources().getString(R.string.complete);
                        }
                        addFeatureOnMapCustom(feature.getGeoJsonGeometry(), String.valueOf(feature.getFeatureId()), feature, true, activity, getSelectedFeature, featureLabel, customFillColor);
                    }
                } else {
                    List<Feature> featureList = featureTable.getallFeaturesList(activity, requiredColumnsList, null, false, true);
                    for (Feature feature : featureList) {

                        // Geometry geometry = feature.getGeometry();
                        JSONObject geometryGeoJson = feature.getGeoJsonGeometry();
                        Object id = feature.getFeatureId();
                        String labelObj = feature.getFeatureLabel();

                        if (id != null) {
                            String w9Id = String.valueOf(id);

                            if (geometryGeoJson != null) {
                                if (isTraversalApplicable) {

                                    Map<String, Object> conditionMap = new HashMap<>();
                                    conditionMap.put("w9id", w9Id);
                                    List<JSONObject> cuurentVertexList = traversalGraph.getVertices(conditionMap);
                                    if (cuurentVertexList != null && cuurentVertexList.size() == 1) {
                                        feature.setFeatureLabel(labelObj);
                                        ReveloLogger.debug(className, "addFeatureOnMap", "adding feature " + w9Id + " - " + labelObj + " on map as it is not in traversal graph");
                                        boolean showfeature = false;
                                        if (!isVisibleByDefault) {
                                            if (cuurentVertexList.get(0).getBoolean("isVisited")) {
                                                showfeature = true;
                                            }
                                        }
                                        addFeatureOnMap(geometryGeoJson, w9Id, feature, showfeature, activity, getSelectedFeature);
                                    } else {
                                        ReveloLogger.debug(className, "addFeatureOnMap", "feature " + w9Id + " not brought on map as it is not in traversal graph");
                                    }
                                } else {
                                    addFeatureOnMap(geometryGeoJson, w9Id, feature, isVisibleByDefault, activity, getSelectedFeature);
                                }


                            }
                        }
                    }
                }


                labelOverlay = createLabelOverlay(labelList);


            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "addFeatureToMap", String.valueOf(e.getCause()));
        }
    }


    public JSONObject addFeatureOnMap(JSONObject geometryGeoJson, String w9Id, Feature feature, boolean isVisibleByDefault, Activity activity, GetSelectedFeature getSelectedFeature) {

        JSONObject responseJson = new JSONObject();
        try {
            responseJson.put("status", "failure");
            responseJson.put("message", "unknown");

//todo - rendering agent needed
            if (overlayMap != null && overlayMap.containsKey(w9Id)) {
                List<Overlay> overlay = overlayMap.get(w9Id);
                if (overlay != null) {
                    for (Overlay overly : overlay) {
                        folderOverlay.remove(overly);
                    }
                }
            }
            if (labelList != null && labelListMap.containsKey(w9Id)) {
                LabelledGeoPoint labelledGeoPoint = labelListMap.get(w9Id);
                labelList.remove(labelledGeoPoint);
            }


            //todo discuss - taking geomtype from geometry instead of featurelayer/entity json
            // GeometryType type = geometry.getGeometryType();
            String geometryType = GeoJsonUtils.getGeometryType(geometryGeoJson);
            if (geometryType == null || geometryType.isEmpty()) {
                responseJson.put("status", "failure");
                responseJson.put("message", "Could not determine geometry type");
                return responseJson;
            }

            List<Overlay> createdOverlay = null;
            try {
                createdOverlay = createOverlay(w9Id, feature, geometryGeoJson, isVisibleByDefault, geometryType, activity, getSelectedFeature, "");
            } catch (Exception e) {
                e.printStackTrace();
                responseJson.put("status", "failure");
                responseJson.put("message", "Could not create overlay for feature. Reason: " + e.getMessage());
                return responseJson;
            }


            if (createdOverlay == null || createdOverlay.isEmpty()) {
                responseJson.put("status", "failure");
                responseJson.put("message", "Could not create overlay for feature. Reason: unknown");
                return responseJson;
            }

            List<GeoPoint> pointsForCenter = new ArrayList<>();
            for (Overlay overlay : createdOverlay) {
                try {
                    folderOverlay.add(overlay);
                    pointsForCenter.add(new GeoPoint(overlay.getBounds().getCenterLatitude(), overlay.getBounds().getCenterLongitude()));
                } catch (Exception e) {
                    e.printStackTrace();
                    ReveloLogger.error(className, "createFolderOverlay", String.valueOf(e.getCause()));
                }
            }

            GeoPoint point = getCenter(pointsForCenter);
            if (point != null && isVisibleByDefault) {
                LabelledGeoPoint labelledGeoPoint = new LabelledGeoPoint(point);
                String labelObj = feature.getFeatureLabel();
                labelledGeoPoint.setLabel(labelObj);
                labelList.add(labelledGeoPoint);
                labelListMap.put(w9Id, labelledGeoPoint);

            }
            overlayMap.put(w9Id, createdOverlay);

            responseJson.put("status", "success");
            responseJson.put("message", "");


        } catch (Exception e) {
            e.printStackTrace();
            try {
                responseJson.put("status", "failure");
                responseJson.put("message", "Error adding feature on map : " + e.getMessage());
            } catch (Exception ee) {

            }
        }

        return responseJson;
    }

    public JSONObject addFeatureOnMapCustom(JSONObject geometryGeoJson, String w9Id, Feature feature, boolean isVisibleByDefault, Activity activity, GetSelectedFeature getSelectedFeature, String featureLabel, String customFillColor) {

        JSONObject responseJson = new JSONObject();
        try {
            responseJson.put("status", "failure");
            responseJson.put("message", "unknown");

//todo - rendering agent needed
            if (overlayMap != null && overlayMap.containsKey(w9Id)) {
                List<Overlay> overlay = overlayMap.get(w9Id);
                if (overlay != null) {
                    for (Overlay overly : overlay) {
                        folderOverlay.remove(overly);
                    }
                }
            }
            if (labelList != null && labelListMap.containsKey(w9Id)) {
                LabelledGeoPoint labelledGeoPoint = labelListMap.get(w9Id);
                labelList.remove(labelledGeoPoint);
            }


            //todo discuss - taking geomtype from geometry instead of featurelayer/entity json
            // GeometryType type = geometry.getGeometryType();
            String geometryType = GeoJsonUtils.getGeometryType(geometryGeoJson);
            if (geometryType == null || geometryType.isEmpty()) {
                responseJson.put("status", "failure");
                responseJson.put("message", "Could not determine geometry type");
                return responseJson;
            }

            List<Overlay> createdOverlay = null;
            try {
                createdOverlay = createOverlay(w9Id, feature, geometryGeoJson, isVisibleByDefault, geometryType, activity, getSelectedFeature, customFillColor);
            } catch (Exception e) {
                e.printStackTrace();
                responseJson.put("status", "failure");
                responseJson.put("message", "Could not create overlay for feature. Reason: " + e.getMessage());
                return responseJson;
            }


            if (createdOverlay == null || createdOverlay.isEmpty()) {
                responseJson.put("status", "failure");
                responseJson.put("message", "Could not create overlay for feature. Reason: unknown");
                return responseJson;
            }

            List<GeoPoint> pointsForCenter = new ArrayList<>();
            for (Overlay overlay : createdOverlay) {
                try {
                    folderOverlay.add(overlay);
                    pointsForCenter.add(new GeoPoint(overlay.getBounds().getCenterLatitude(), overlay.getBounds().getCenterLongitude()));
                } catch (Exception e) {
                    e.printStackTrace();
                    ReveloLogger.error(className, "createFolderOverlay", String.valueOf(e.getCause()));
                }
            }

            GeoPoint point = getCenter(pointsForCenter);
            if (point != null && isVisibleByDefault) {
                LabelledGeoPoint labelledGeoPoint = new LabelledGeoPoint(point);
                if (featureLabel != null) {
                    String labelObj = featureLabel;
                    labelledGeoPoint.setLabel(labelObj);
                    labelList.add(labelledGeoPoint);
                    labelListMap.put(w9Id, labelledGeoPoint);
                } else {
                    String labelObj = feature.getFeatureLabel();
                    labelledGeoPoint.setLabel(labelObj);
                    labelList.add(labelledGeoPoint);
                    labelListMap.put(w9Id, labelledGeoPoint);
                }
            }
            overlayMap.put(w9Id, createdOverlay);

            responseJson.put("status", "success");
            responseJson.put("message", "");


        } catch (Exception e) {
            e.printStackTrace();
            try {
                responseJson.put("status", "failure");
                responseJson.put("message", "Error adding feature on map : " + e.getMessage());
            } catch (Exception ee) {

            }
        }

        return responseJson;
    }

    public SimpleFastPointOverlay createLabelOverlay(List<IGeoPoint> labelPointList) {

        SimplePointTheme simplePointTheme = new SimplePointTheme(labelPointList, true);

        Paint textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
        textStyle.setColor(Color.WHITE);
        textStyle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textStyle.setTextAlign(Paint.Align.CENTER);
        textStyle.setTextSize(40);

        SimpleFastPointOverlayOptions options = SimpleFastPointOverlayOptions.getDefaultStyle().setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION).setRadius(0f).setIsClickable(false).setCellSize(20).setTextStyle(textStyle);

        return new SimpleFastPointOverlay(simplePointTheme, options);
    }

    private List<Overlay> createOverlay(String id, Feature feature, JSONObject geometryGeoJson, boolean isVisibleByDefault, String geometryType, Activity activity, GetSelectedFeature getSelectedFeature, String customFillColor) {

        List<Overlay> overlayList = new ArrayList<>();

        if (geometryType.equalsIgnoreCase("multipolygon")) {

            List<Polygon> pList = GeoJsonUtils.toOSMPolygons(geometryGeoJson);

            for (Polygon polygon : pList) {
                configureFeature(id, feature, polygon, isVisibleByDefault, activity, getSelectedFeature, "");
            }

            overlayList.addAll(pList);

        } else if (geometryType.equalsIgnoreCase("polygon")) {

            Polygon polygon = GeoJsonUtils.toOSMPolygon(geometryGeoJson);
            configureFeature(id, feature, polygon, isVisibleByDefault, activity, getSelectedFeature, "");

            overlayList.add(polygon);

        } else if (geometryType.equalsIgnoreCase("multilinestring") || geometryType.equalsIgnoreCase("polyline")) {

            List<Polyline> pList = GeoJsonUtils.toOSMPolylines(geometryGeoJson);

            for (Polyline polyline : pList) {
                configureFeature(id, feature, polyline, isVisibleByDefault, activity, getSelectedFeature, "");
            }

            overlayList.addAll(pList);

        } else if (geometryType.equalsIgnoreCase("linestring")) {

            Polyline polyline = GeoJsonUtils.toOSMPolyline(geometryGeoJson);
            configureFeature(id, feature, polyline, isVisibleByDefault, activity, getSelectedFeature, "");

            overlayList.add(polyline);

        } else if (geometryType.equalsIgnoreCase("point")) {

            GeoPoint geoPoint = GeoJsonUtils.toOSMPoint(geometryGeoJson);

            Marker marker = new Marker(mapView);
            marker.setPosition(geoPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            marker.setDraggable(false);

            configureFeature(id, feature, marker, isVisibleByDefault, activity, getSelectedFeature, customFillColor);

            overlayList.add(marker);
        }

        return overlayList;
    }

    //todo CHECK
    private GeoPoint getCenter(List<GeoPoint> pointsForCenter) {
        if (pointsForCenter == null || pointsForCenter.size() == 0) {
            return null;
        } else if (pointsForCenter.size() == 1) {
            return pointsForCenter.get(0);
        } else if (pointsForCenter.size() == 2) {
            Polyline polyline = new Polyline();
            polyline.setPoints(pointsForCenter);
            return polyline.getBounds().getCenter();
        } else {
            Polygon polygon = new Polygon();
            polygon.setPoints(pointsForCenter);
            return polygon.getBounds().getCenter();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void configureFeature(String id, Feature feature, Overlay overlay, boolean isVisibleByDefault, Activity activity, GetSelectedFeature getSelectedFeature, String customFillColor) {
        String uniqueFieldName = "";
        String uniqueFieldValue = "";
        if (!selectedRendererName.equalsIgnoreCase("simple") && uniqueValueStyle != null) {
            uniqueFieldName = uniqueValueStyle.getFieldName();
        }
        if (!uniqueFieldName.isEmpty()) {
            try {
                Object value = feature.getAttributes().get(uniqueFieldName);
                if (value != null) {
                    uniqueFieldValue = String.valueOf(value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (overlay instanceof Polygon) {

            Polygon polygon = (Polygon) overlay;
            polygon.setId(id);
            polygon.setTitle(name);
            polygon.setVisible(isVisibleByDefault);
            polygon.setRelatedObject(uniqueFieldValue);
            Log.e("Polygon " + name, id);

            polygon.setOnClickListener(null);
            polygon.onClickDefault(polygon, null, null);

        } else if (overlay instanceof Polyline) {

            Polyline polyline = (Polyline) overlay;

            polyline.setId(id);
            polyline.setTitle(name);
            polyline.setVisible(isVisibleByDefault);
            polyline.setRelatedObject(uniqueFieldValue);
            Log.e("Polyline " + name, id);

            polyline.setOnClickListener(null);
            polyline.onClickDefault(polyline, null, null);

        } else if (overlay instanceof Marker) {

            Marker marker = (Marker) overlay;
            marker.setId(id);
            marker.setTitle(name);
            marker.setVisible(isVisibleByDefault);
            marker.setRelatedObject(uniqueFieldValue);
            Log.e("Marker " + name, id);

            marker.setInfoWindow(null);

            marker.setOnMarkerClickListener((selectedMarker, mapView1) -> {

                if (isSelected) {
                    if (selectedMarker != null) {
                        if (isSelectable(feature.getFeatureId())) {
                            selectUnselectPoint(activity, selectedMarker, customFillColor);

                        }
                    }

                }
                return false;
            });
        }

        setRenderer(overlay, activity, customFillColor);//configure feature
    }

    private boolean isSelectable(Object featureId) {
        boolean isInRange = false;
        try {

            if (excludedFeatureList != null) {
                for (Feature feature : excludedFeatureList) {
                    if (feature.getFeatureId().equals(featureId)) {
                        return false;
                    }
                }
            }

            if (inRangeTargetList != null) {

                for (Feature feature : inRangeTargetList) {
                    if (feature.getFeatureId().equals(featureId)) {
                        boolean isDelivered = (boolean) feature.getAttributes().get("isdelivered");
                        boolean isSkipped = (boolean) feature.getAttributes().get("skipped");
//                        boolean isVisited = (boolean) feature.getAttributes().get("isvisited");
                        if (!isSkipped && !isDelivered) {
                            isInRange = true;
                        } else if (isSkipped && !isDelivered) {
                            isInRange = false;
                        } else if (isSkipped && isDelivered) {
                            isInRange = false;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isInRange;
    }


    public void selectUnselectPoint(Activity activity, Overlay selectedMarkerOverlay, String customFillColor) {
        if (selectedMarkerOverlay == null || !(selectedMarkerOverlay instanceof Marker)) {
            return;
        }

//        if (selectedFeatures.size() != 0) {
//            selectedFeatures.clear();
//            for(Overlay overlay:selectedFeatures){
//
//            }
//        }


        if (selectedFeatures.contains(selectedMarkerOverlay)) {
//            selectedFeatures.remove(selectedMarkerOverlay);
            selectedFeatures.clear();
            if (highlightedFeatures.contains(selectedMarkerOverlay)) {
                setMarkerHighlightRenderer(activity, selectedMarkerOverlay);
            } else {
                setRenderer(selectedMarkerOverlay, activity, customFillColor);//for marker
            }
        } else if (!selectedFeatures.contains(selectedMarkerOverlay)) {
            setRenderer(selectedMarkerOverlay, activity, "#3F7EE8");
            unSelectFeatures(activity, lastSelectionColor);//added for single selection
            selectedFeatures.clear();//added for single selection
            selectedFeatures.add(selectedMarkerOverlay);//configure marker
            lastSelectionColor = customFillColor;
        }
        getSelectedFeature.setSelectedFeature(activity, selectedFeatures);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void setRenderer(Overlay overlay, Activity activity, String customFillColor) {

        int strokeWidth = 0;
        int strokeColor = -1;
        int fillColor = -1;
        if (simpleStyle != null) {
            strokeWidth = simpleStyle.getStrokeWidth();
            String sColor = simpleStyle.getStrokeColor();

            if (!TextUtils.isEmpty(sColor)) {
                strokeColor = AppMethods.getColor(sColor);
            }

            String fColor = simpleStyle.getFillColor();

            if (!TextUtils.isEmpty(fColor)) {
                fillColor = AppMethods.getColor(fColor);
            }
        }

        if (overlay instanceof Marker) {

            Marker marker = (Marker) overlay;

            if (!selectedRendererName.equalsIgnoreCase("simple")) {
                if (uniqueValueStyle != null) {
                    String overlayUniqueValue = String.valueOf(marker.getRelatedObject());
                    if (uniqueValueStyle.containsValueInMap(overlayUniqueValue)) {
                        strokeWidth = uniqueValueStyle.getStrokeWidth(overlayUniqueValue);
                        String sColor = uniqueValueStyle.getStrokeColor(overlayUniqueValue);

                        if (!TextUtils.isEmpty(sColor)) {
                            strokeColor = AppMethods.getColor(sColor);
                        }
                        String fColor = uniqueValueStyle.getFillColor(overlayUniqueValue);

                        if (!TextUtils.isEmpty(fColor)) {
                            fillColor = AppMethods.getColor(fColor);
                        }
                    } else if (uniqueValueStyle.containsValueInMap(UniqueStyleValueModel.remainingValues)) {
                        strokeWidth = uniqueValueStyle.getStrokeWidth(UniqueStyleValueModel.remainingValues);
                        String sColor = uniqueValueStyle.getStrokeColor(UniqueStyleValueModel.remainingValues);

                        if (!TextUtils.isEmpty(sColor)) {
                            strokeColor = AppMethods.getColor(sColor);
                        }
                        String fColor = uniqueValueStyle.getFillColor(UniqueStyleValueModel.remainingValues);

                        if (!TextUtils.isEmpty(fColor)) {
                            fillColor = AppMethods.getColor(fColor);
                        }
                    }
                }
            }


            Drawable mDrawable = VectorDrawableUtils.getDrawable(activity, R.drawable.ic_geom_marker_feature);
            Drawable mDrawableNew = mDrawable.getConstantState().newDrawable().mutate();


//            VectorMasterDrawable mDrawable = new VectorMasterDrawable(activity, R.drawable.ic_geom_marker_feature);
//            PathModel pathModel = mDrawable.getPathModelByName("fill");
//
//            if (fillColor != 0) {
//                pathModel.setFillColor(fillColor);
//                if (strokeColor != 0) {
//                    try {
//                        pathModel.setStrokeColor(strokeColor);
//                        pathModel.setStrokeWidth(strokeWidth);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }


            if (!customFillColor.equalsIgnoreCase("")) {
                //for delivery app
                int CustomFillColor = AppMethods.getColor(customFillColor);
                try {
                    if (fillColor != 0) {
                        mDrawableNew = VectorDrawableUtils.getDrawable(activity, mDrawableNew, CustomFillColor);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            marker.setIcon(mDrawableNew);/*.setIcon(mDrawableNew);*/
        } else {
            Paint fillPaint = null;
            Paint outLinePaint = null;
            String overlayUniqueValue = null;
            if (overlay instanceof Polygon) {

                Polygon polygon = (Polygon) overlay;
                fillPaint = polygon.getFillPaint();
                outLinePaint = polygon.getOutlinePaint();
                if (!selectedRendererName.equalsIgnoreCase("simple")) {
                    if (uniqueValueStyle != null) {
                        overlayUniqueValue = String.valueOf(polygon.getRelatedObject());
                    }
                }
            } else if (overlay instanceof Polyline) {

                Polyline polyline = (Polyline) overlay;
                outLinePaint = polyline.getOutlinePaint();
                if (!selectedRendererName.equalsIgnoreCase("simple")) {
                    if (uniqueValueStyle != null) {
                        overlayUniqueValue = String.valueOf(polyline.getRelatedObject());
                    }
                }
            }


            if (!selectedRendererName.equalsIgnoreCase("simple")) {
                if (uniqueValueStyle != null) {
                    if (uniqueValueStyle.containsValueInMap(overlayUniqueValue)) {
                        strokeWidth = uniqueValueStyle.getStrokeWidth(overlayUniqueValue);
                        String sColor = uniqueValueStyle.getStrokeColor(overlayUniqueValue);

                        if (!TextUtils.isEmpty(sColor)) {
                            strokeColor = AppMethods.getColor(sColor);
                        }
                        String fColor = uniqueValueStyle.getFillColor(overlayUniqueValue);

                        if (!TextUtils.isEmpty(fColor)) {
                            fillColor = AppMethods.getColor(fColor);
                        }
                    } else if (uniqueValueStyle.containsValueInMap(UniqueStyleValueModel.remainingValues)) {
                        strokeWidth = uniqueValueStyle.getStrokeWidth(UniqueStyleValueModel.remainingValues);
                        String sColor = uniqueValueStyle.getStrokeColor(UniqueStyleValueModel.remainingValues);

                        if (!TextUtils.isEmpty(sColor)) {
                            strokeColor = AppMethods.getColor(sColor);
                        }
                        String fColor = uniqueValueStyle.getFillColor(UniqueStyleValueModel.remainingValues);

                        if (!TextUtils.isEmpty(fColor)) {
                            fillColor = AppMethods.getColor(fColor);
                        }
                    }
                }
            }


            if (fillPaint != null) {
                if (fillColor != -1) {
                    fillPaint.setColor(fillColor);
                } else {
                    fillPaint.setColor(Color.TRANSPARENT);
                }
            }

            if (outLinePaint != null) {

                if (strokeColor != -1) {
                    outLinePaint.setColor(strokeColor);
                } else {
                    outLinePaint.setColor(Color.BLACK);
                }

                if (strokeWidth == 0) {
                    outLinePaint.setStrokeWidth(2);
                } else {
                    outLinePaint.setStrokeWidth(strokeWidth);
                }
            }
        }


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mapView.invalidate();
            }
        });
    }

//    public BitmapDrawable writeOnDrawable(Activity context, int drawableId, String text){
//
//        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
//
//
//        Paint paint = new Paint();
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.BLACK);
//        paint.setTextSize(20);
//
//        Canvas canvas = new Canvas(bm);
//        canvas.drawText(text, 0, bm.getHeight()/2, paint);
//
//        return new BitmapDrawable(bm);
//    }

    public void setMarkerHighlightRenderer(Activity activity, Overlay overlay) {
        /*Marker marker = (Marker) overlay;

        Drawable icon = marker.getIcon();
        Drawable mDrawable = VectorDrawableUtils.getDrawable(activity,icon,Color.YELLOW);
        marker.setIcon(mDrawable);*/
        Marker marker = (Marker) overlay;

        Drawable icon = marker.getImage();
        Drawable mDrawable = VectorDrawableUtils.getDrawable(activity, icon, Color.YELLOW);
        marker.setIcon(mDrawable);
    }

    public void unSelectFeatures(Activity activity, String lastSelectionColor) {
        if (selectedFeatures != null && selectedFeatures.size() != 0) {
            if (featureTable.getGeometryType().equalsIgnoreCase("point")) {
                ReveloLogger.debug(className, "unSelectFeatures", "unselecting " + selectedFeatures.size() + " features of layer " + name);
                ReveloLogger.debug(className, "unSelectFeatures", "layer is type point, setting renderer");
                for (Overlay overlay : selectedFeatures) {
                    if (!highlightedFeatures.contains(overlay)) {
                        setRenderer(overlay, activity, lastSelectionColor);//for un select multiple markers
                    }
                }
            } else {
                ReveloLogger.debug(className, "unSelectFeatures", "unselecting" + selectedFeatures.size() + " features of layer " + name);
            }

            selectedFeatures.clear();//unselect feature
        } else {
            ReveloLogger.debug(className, "unSelectFeatures", "NOT unselecting any features of layer " + name);
        }
//        if(getSelectedFeature != null){
//            getSelectedFeature.setSelectedFeature(activity,selectedFeatures);
//        }
    }

    public String getFeatureLabel(String w9Id) {
        if (labelListMap != null && labelListMap.containsKey(w9Id)) {
            LabelledGeoPoint labelledGeoPoint = labelListMap.get(w9Id);
            return labelledGeoPoint.getLabel();
        }
        return w9Id;
    }

    public void selectFeature(Context context, String operation, MotionEvent event, GetSelectedFeature getSelectedFeature) {//from map presenter

        if (folderOverlay != null) {
            Overlay[] overlay = new Overlay[1];

            List<Overlay> overlayList = folderOverlay.getItems();

            if (!overlayList.isEmpty()) {
                String featureid = null;
                for (Overlay overlays : overlayList) {

                    if (overlays instanceof Polygon) {
                        Polygon polygon = (Polygon) overlays;
                        if (polygon.contains(event)) {
                            overlay[0] = polygon;
                            featureid = polygon.getId();
                            break;
                        }

                    } else if (overlays instanceof Polyline) {

                        Polyline polyline = (Polyline) overlays;
                        Projection projection = mapView.getProjection();
                        GeoPoint tapLocation = (GeoPoint) projection.fromPixels((int) event.getX(), (int) event.getY());

                        boolean isClose = polyline.isCloseTo(tapLocation, 10, mapView);

                        if (isClose) {
                            overlay[0] = polyline;
                            featureid = polyline.getId();
                            break;
                        }
                    }
                }
                //we got first polygon/polyline part here. now we check if we have other parts to it and highlight those too
                List<Overlay> selectedOverlays = new ArrayList<>();//list of all overlays with that feature id
                if (featureid != null) {
                    for (Overlay overlays : overlayList) {
                        if (overlays instanceof Polygon) {
                            Polygon polygon = (Polygon) overlays;
                            if (polygon.getId().equalsIgnoreCase(featureid)) {
                                selectedOverlays.add(polygon);
                            }

                        } else if (overlays instanceof Polyline) {

                            Polyline polyline = (Polyline) overlays;
                            if (polyline.getId().equalsIgnoreCase(featureid)) {
                                selectedOverlays.add(polyline);
                            }
                        }
                    }
                }


                selectedFeatures.clear();//added for single selection
                if (selectedOverlays.size() != 0) {
                    for (Overlay overlay1 : selectedOverlays) {
                        if (selectedFeatures.contains(overlay1)) {
                            selectedFeatures.remove(overlay1);//select feature
                        } else if (!selectedFeatures.contains(overlay1)) {
                            //selectedFeatures.clear();//added for single selection
                            if (operation.equalsIgnoreCase(Operation.FEATURE_DRAWING)) {
                                selectedFeatures.clear();//select feature
                            }
                            selectedFeatures.add(overlay1);//select feature
                        }
                    }
                }
            }

            getSelectedFeature.setSelectedFeature(context, selectedFeatures);

        }

        mapView.invalidate();
    }

    public void selectFeature(Context context, String id, Feature feature, GetSelectedFeature getSelectedFeature) {

        if (folderOverlay != null) {
            //Overlay[] overlay = new Overlay[1];
            List<Overlay> selectedFeatureOverlay = new ArrayList<>();
            List<Overlay> overlayList = folderOverlay.getItems();

            if (overlayList != null && !overlayList.isEmpty()) {//addednotnull

                for (Overlay overlays : overlayList) {

                    if (overlays instanceof Polygon) {
                        Polygon polygon = (Polygon) overlays;
                        if (polygon.getId().equalsIgnoreCase(id)) {
                            // overlay[0] = polygon;
                            // break;
                            polygon.setVisible(true);
                            selectedFeatureOverlay.add(polygon);
                        }

                    } else if (overlays instanceof Polyline) {

                        Polyline polyline = (Polyline) overlays;
                        if (polyline.getId().equalsIgnoreCase(id)) {
                            // overlay[0] = polyline;
                            // break;
                            polyline.setVisible(true);
                            selectedFeatureOverlay.add(polyline);
                        }
                    } else if (overlays instanceof Marker) {
                        Marker marker = (Marker) overlays;
                        if (marker.getId().equalsIgnoreCase(id)) {
                            //overlay[0]=marker;
                            //break;
                            marker.setVisible(true);
                            selectedFeatureOverlay.add(marker);
                        }
                    }
                }
                selectedFeatures.clear();//added for single selection
                if (selectedFeatureOverlay.size() != 0) {
                    BoundingBox boundingBox = null;
                    for (Overlay overlay1 : selectedFeatureOverlay) {
                        if (selectedFeatures.contains(overlay1)) {
                            selectedFeatures.remove(overlay1);//select feature
                        } else if (!selectedFeatures.contains(overlay1)) {
                            //selectedFeatures.clear();//addedfor2siteselected//removed to enable only one selection
                            if (Operation.getOperationType().equalsIgnoreCase(Operation.FEATURE_DRAWING)) {
                                selectedFeatures.clear();//select feature
                            }
                            selectedFeatures.add(overlay1);//select feature
                        }
                        if (boundingBox != null) {
                            boundingBox = boundingBox.concat(overlay1.getBounds());
                        } else {
                            boundingBox = overlay1.getBounds();
                        }
                    }
                    GeoPoint point = boundingBox.getCenter();
                    if (point != null) {
                        LabelledGeoPoint labelledGeoPoint = new LabelledGeoPoint(point);
                        String labelObj = feature.getFeatureLabel();
                        labelledGeoPoint.setLabel(labelObj);
                        labelList.add(labelledGeoPoint);
                        labelListMap.put(id, labelledGeoPoint);

                    }
                    //mapView.zoomToBoundingBox(boundingBox, true);
                    mapView.zoomToBoundingBox(boundingBox, true, 0, mapView.getMaxZoomLevel(), null);
                }
            }

            getSelectedFeature.setSelectedFeature(context, selectedFeatures);

        }

        mapView.invalidate();
    }

    public void selectFeature(Context context, String id, GetSelectedFeature getSelectedFeature) {

        if (folderOverlay != null) {
            //Overlay[] overlay = new Overlay[1];
            List<Overlay> selectedFeatureOverlay = new ArrayList<>();
            List<Overlay> overlayList = folderOverlay.getItems();

            if (overlayList != null && !overlayList.isEmpty()) {//addednotnull

                for (Overlay overlays : overlayList) {

                    if (overlays instanceof Polygon) {
                        Polygon polygon = (Polygon) overlays;
                        if (polygon.getId().equalsIgnoreCase(id)) {
                            // overlay[0] = polygon;
                            // break;
                            selectedFeatureOverlay.add(polygon);
                        }

                    } else if (overlays instanceof Polyline) {

                        Polyline polyline = (Polyline) overlays;
                        if (polyline.getId().equalsIgnoreCase(id)) {
                            // overlay[0] = polyline;
                            // break;
                            selectedFeatureOverlay.add(polyline);
                        }
                    } else if (overlays instanceof Marker) {
                        Marker marker = (Marker) overlays;
                        if (marker.getId().equalsIgnoreCase(id)) {
                            //overlay[0]=marker;
                            //break;
                            selectedFeatureOverlay.add(marker);
                        }
                    }
                }
                selectedFeatures.clear();//added for single selection
                if (selectedFeatureOverlay.size() != 0) {
                    for (Overlay overlay1 : selectedFeatureOverlay) {
                        if (selectedFeatures.contains(overlay1)) {
                            selectedFeatures.remove(overlay1);//select feature
                        } else if (!selectedFeatures.contains(overlay1)) {
                            //selectedFeatures.clear();//addedfor2siteselected//removed to enable only one selection
                            if (Operation.getOperationType().equalsIgnoreCase(Operation.FEATURE_DRAWING)) {
                                selectedFeatures.clear();//select feature
                            }
                            selectedFeatures.add(overlay1);//select feature
                        }
                    }
                }
            }

            getSelectedFeature.setSelectedFeature(context, selectedFeatures);

        }

        mapView.invalidate();
    }

//    void selectMarkerFeature(Context context,Overlay overlay ){
//
//        Marker marker1 = (Marker) overlay;
//        List<Overlay> overlayList = folderOverlay.getItems();
//        for (Overlay overlays : overlayList) {
//            if (overlays instanceof Marker) {
//                Marker marker = (Marker) overlays;
//                if (marker.getId().equalsIgnoreCase(marker1.getId())) {
//                    //overlay[0]=marker;
//                    //break;
//                    selectedFeatureOverlay.add(marker);
//                }
//            }
//        }
//    }


    public void searchFeatures(Map<Attribute, Object> fieldsMap, GetSearchedFeatures getSearchedFeatures, Context context) {

        if (featureTable != null) {
//            List<Feature> featureList = featureTable.getFeaturesAsLike(null,fieldsMap,context);
            List<Feature> featureList = featureTable.getFeaturesAsLike(null, fieldsMap, "OR", null, context, false);
            if (!featureList.isEmpty()) {
                FolderOverlay folderOverlay = new FolderOverlay();
                for (Feature feature : featureList) {

                    Object featureIdObject = feature.getFeatureId();
                    if (featureIdObject != null) {
                        String featureId = (String) featureIdObject;

                        if (overlayMap.containsKey(featureId)) {

                            List<Overlay> overlayList = overlayMap.get(featureId);

                            if (overlayList != null) {
                                folderOverlay.add(overlayList.get(0));
                                for (Overlay overlay : overlayList) {
                                    if (!highlightedFeatures.contains(overlay)) {
                                        highlightedFeatures.add(overlay);
                                    }
                                }
                            }
                        }
                    }
                }

                BoundingBox boundingBox = folderOverlay.getBounds();
                if (boundingBox != null) {
                    //mapView.zoomToBoundingBox(boundingBox, true);
                    mapView.zoomToBoundingBox(boundingBox, true, 0, mapView.getMaxZoomLevel(), null);
                }
            }

            getSearchedFeatures.getSearchedFeatures(highlightedFeatures, featureList);
        }
    }

    public void unHighlightFeatures(Activity activity) {
        if (highlightedFeatures != null && highlightedFeatures.size() != 0) {
            if (featureTable.getGeometryType().equalsIgnoreCase("point")) {
                for (Overlay overlay : highlightedFeatures) {
                    setRenderer(overlay, activity, "");//for un highlight multiple markers
                }
            }

            highlightedFeatures.clear();
        }
    }

    public boolean isLabelEnabled() {
        if (labelOverlay == null) {
            return false;
        } else {
            return labelOverlay.isEnabled();
        }
    }

    public void labelDisabled() {
        if (labelOverlay != null) {
            labelOverlay.setEnabled(false);
        }
        mapView.invalidate();
    }

    public void labelEnabled() {
        if (labelOverlay != null) {
            labelOverlay.setEnabled(true);
        }
        mapView.invalidate();
    }

    public void changeFeatureOverlay(String w9Id, Activity activity, boolean editOngoing) {

        if (overlayMap.containsKey(w9Id)) {
            List<Overlay> overlayList = overlayMap.get(w9Id);
            assert overlayList != null;
            for (Overlay overlay : overlayList) {
                /*if(editOngoing){
                    if(geometryType.equalsIgnoreCase(AppConstants.MULTIPOLYGON)){
                        assert overlay instanceof Polygon;
                        Polygon polygon = (Polygon) overlay;
                        polygon.setVisible(false);
                       *//* Paint fillPaint = polygon.getFillPaint();
                        fillPaint.setColor(Color.TRANSPARENT);
                        polygon.getOutlinePaint().setColor(Color.RED);*//*
                    }else if(geometryType.equalsIgnoreCase(AppConstants.MULTILINESTRING)){
                        assert overlay instanceof Polyline;
                        Polyline polyline = (Polyline) overlay;
                        Paint outLinePaint = polyline.getOutlinePaint();
                        outLinePaint.setStrokeWidth(0);
                        outLinePaint.setColor(Color.TRANSPARENT);
                        //outLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
                    }
                }else {
                    setRenderer(overlay,activity);
                }*/


                if (geometryType.equalsIgnoreCase(AppConstants.MULTIPOLYGON)) {
                    assert overlay instanceof Polygon;
                    Polygon polygon = (Polygon) overlay;
                    polygon.setVisible(!editOngoing);
                } else if (geometryType.equalsIgnoreCase(AppConstants.MULTILINESTRING)) {
                    assert overlay instanceof Polyline;
                    Polyline polyline = (Polyline) overlay;
                    polyline.setVisible(!editOngoing);
                   /* if(editOngoing){
                        if (selectedFeatures.contains(polyline)) {
                            selectedFeatures.remove(polyline);//select feature
                        }
                    }else {
                        if (!selectedFeatures.contains(polyline)) {
                            selectedFeatures.add(polyline);//select feature
                        }
                    }*/
                }


            }

            mapView.invalidate();

        }
    }

    public void changeOpacity(Context context, int range, String geometryType) {

        assert folderOverlay != null;

        if (!TextUtils.isEmpty(geometryType)) {
            if (geometryType.equalsIgnoreCase(AppConstants.MULTIPOLYGON) || geometryType.equalsIgnoreCase(AppConstants.POLYGON)) {

                List<Overlay> polygonList = folderOverlay.getItems();
                Overlay overyaPolygonIndex0 = polygonList.get(0);
                Polygon polygonIndex0 = (Polygon) overyaPolygonIndex0;
                Paint fillPaintIndex0 = polygonIndex0.getFillPaint();

                Paint outlinePaintIndex0 = polygonIndex0.getOutlinePaint();
                int fillPaintClour = fillPaintIndex0.getColor();
                int outlineColour = outlinePaintIndex0.getColor();

                int colorAlpha = (int) (range) * 25;

                for (Overlay overlay : polygonList) {
                    Polygon polygon = (Polygon) overlay;
                    Paint fillPaint = polygon.getFillPaint();
                    fillPaint.setColor(Color.argb(colorAlpha, Color.red(fillPaintClour), Color.green(fillPaintClour), Color.blue(fillPaintClour)));
                    Paint outLinePaint = polygon.getOutlinePaint();
                    outLinePaint.setColor(Color.argb(colorAlpha, Color.red(outlineColour), Color.green(outlineColour), Color.blue(outlineColour)));
                }

            } else if (geometryType.equalsIgnoreCase(AppConstants.MULTILINESTRING) || geometryType.equalsIgnoreCase(AppConstants.POLYLINE)) {

                List<Overlay> polylineList = folderOverlay.getItems();
                int colorAlpha = (int) (range) * 25;

                for (Overlay overlay : polylineList) {
                    Polyline polyline = (Polyline) overlay;
                    Paint outLinePaint = polyline.getOutlinePaint();
                    int outlineColour = outLinePaint.getColor();
                    outLinePaint.setColor(Color.argb(colorAlpha, Color.red(outlineColour), Color.green(outlineColour), Color.blue(outlineColour)));
                }

            } else if (geometryType.equalsIgnoreCase(AppConstants.POINT)) {

                List<Overlay> markerOverlayList = folderOverlay.getItems();
                float colorAlpha = Float.parseFloat(String.valueOf(range)) / 10;

                for (Overlay overlay : markerOverlayList) {
                    Marker marker = (Marker) overlay;
                   /* Drawable icon = marker.getIcon();
                    icon= VectorDrawableUtils.getDrawable(context,icon,Color.YELLOW);
                    marker.setIcon(icon);*/
                    /*Drawable icon = marker.getImage();
                    icon = VectorDrawableUtils.getDrawable(context, icon, Color.YELLOW);
                    marker.setIcon(icon);*/
                    marker.setAlpha(colorAlpha);
                }
            }
        }
        mapView.invalidate();
    }

    public boolean isLayerEnabled() {
        if (folderOverlay == null) {
            return false;
        } else {
            return folderOverlay.isEnabled();
        }
    }

    public void disableLayer() {
        try {

            if (folderOverlay != null) {
                folderOverlay.setEnabled(false);
            }

            if (labelOverlay != null) {
                labelOverlay.setEnabled(false);
            }

            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "disableLayer", String.valueOf(e.getCause()));
        }
    }

    public void enableLayer() {

        try {

            if (folderOverlay != null) {
                folderOverlay.setEnabled(true);
            }

            if (labelOverlay != null) {
                labelOverlay.setEnabled(true);
            }

            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "enableLayer", String.valueOf(e.getCause()));
        }

    }

    public void removeFeatureOFromMap(String w9Id, Activity activity) {

        try {
            if (overlayMap.containsKey(w9Id)) {
                List<Overlay> overlay = overlayMap.get(w9Id);
                for (Overlay overly : overlay) {
                    folderOverlay.remove(overly);
                }

                if (labelListMap.containsKey(w9Id)) {
                    LabelledGeoPoint labelledGeoPoint = labelListMap.get(w9Id);
                    labelList.remove(labelledGeoPoint);
                }
                mapView.invalidate();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeLayerFromMap() {

        for (String w9Id : overlayMap.keySet()) {
            List<Overlay> overlay = overlayMap.get(w9Id);
            for (Overlay overly : overlay) {
                folderOverlay.remove(overly);
            }

            if (labelListMap.containsKey(w9Id)) {
                LabelledGeoPoint labelledGeoPoint = labelListMap.get(w9Id);
                labelList.remove(labelledGeoPoint);
            }
        }
        mapView.invalidate();


    }

    public boolean isHasShadowTable() {
        return hasShadowTable;
    }

    public void setHasShadowTable(String hasShadowTable) {
        this.hasShadowTable = hasShadowTable.equalsIgnoreCase("true");
    }

    public void zoomToFeature(String w9Id, Context context) {
        if (w9Id == null || w9Id.isEmpty())
            return;
        if (overlayMap.containsKey(w9Id)) {
            List<Overlay> overlayList = overlayMap.get(w9Id);
            if (overlayList != null && !overlayList.isEmpty()) {
                FolderOverlay folderOverlay = new FolderOverlay();
                for (Overlay overlay : overlayList) {
                    folderOverlay.add(overlay);
                }
                mapView.zoomToBoundingBox(folderOverlay.getBounds(), true, 0, mapView.getMaxZoomLevel(), null);
            }
        }
    }

    public void setSelectableFeatures(FragmentActivity fragmentActivity, List<Feature> inRangeTargetList, List<Feature> excludedFeatureList) {
        this.inRangeTargetList = inRangeTargetList;
        this.excludedFeatureList = excludedFeatureList;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                clearUnreachableSelection(fragmentActivity);
//            }
//        }).start();
    }

    private void clearUnreachableSelection(FragmentActivity fragmentActivity) {
        try {
            if (inRangeTargetList != null) {
                boolean ispresent = false;
                loop:
                for (Feature feature : inRangeTargetList) {
                    for (Overlay selectedFeature : selectedFeatures) {
                        if (selectedFeature instanceof Marker) {
                            Marker marker = (Marker) selectedFeature;
                            if (feature.getFeatureId().equals(marker.getId())) {
                                ispresent = true;
                                break loop;
                            }
                        }
                    }
                }
                if(!ispresent){
                    unSelectFeatures(fragmentActivity, lastSelectionColor);
                    selectedFeatures.clear();
                    if(getSelectedFeature != null){
                        getSelectedFeature.setSelectedFeature(fragmentActivity, selectedFeatures);
                    }

                }
            } else {
                unSelectFeatures(fragmentActivity, lastSelectionColor);
                selectedFeatures.clear();
                if(getSelectedFeature != null){
                    getSelectedFeature.setSelectedFeature(fragmentActivity, selectedFeatures);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface GetSelectedFeature {
        void setSelectedFeature(Context context, List<Overlay> features);
    }

    public interface GetSearchedFeatures {
        void getSearchedFeatures(List<Overlay> overlayList, List<Feature> featureList);
    }
}
