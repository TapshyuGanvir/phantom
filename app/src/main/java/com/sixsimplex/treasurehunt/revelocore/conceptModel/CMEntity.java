package com.sixsimplex.treasurehunt.revelocore.conceptModel;

import static com.sixsimplex.treasurehunt.revelocore.util.sort.ReveloFeatureComparator.AlphabeticalSort;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel.Flow;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel.Interaction;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel.InteractionLink;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.data.FeatureTable;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageRWAgent;
import com.sixsimplex.treasurehunt.revelocore.graph.GraphFactory;
import com.sixsimplex.treasurehunt.revelocore.graph.flowinteractiongraph.FlowGraph;
import com.sixsimplex.treasurehunt.revelocore.graph.jsongraph.JSONGraph;
import com.sixsimplex.treasurehunt.revelocore.layer.Attribute;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;
import com.sixsimplex.treasurehunt.revelocore.layer.PropertyGroupsModel;
import com.sixsimplex.treasurehunt.revelocore.layer.SimpleStyleModel;
import com.sixsimplex.treasurehunt.revelocore.layer.UniqueStyleValueModel;
import com.sixsimplex.treasurehunt.Phantom1.traversalgraph.TraversalGraph;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sort.ReveloFeatureComparator;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.geopackage.GeoPackage;

public class CMEntity /*implements Serializable*/ {
    private final String className = "CMEntity";
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
    private boolean hasShadowTable = false;
    private boolean hasFlows = false;
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
    private JSONArray flowsJArray;
    private HashMap<String, FlowGraph> flowNameGraphMap;
    private String idGenerationType;
    private String traversalGraphStr;
    private TraversalGraph traversalGraph;
    private FeatureTable featureTable;
    //private FeatureDao featureDao;
    //private AttributesDao attributesDao;
    private FeatureLayer featureLayer;


    public CMEntity(String name, String label, String type, String geometryType, String w9IdProperty, String labelPropertyName, String categoryPropertyName, String abbr, String isLocked, String hasShadowTable, String selectedRendererName, String idGenRules, String simpleStyle, String uniqueValueStyle, String properties, String propertyGroups, String domainValues, String dependantPropertiesGraphStr, String categories, String flows, String traversalGraphStr, GeoPackage dataGeopackage) {
        ReveloLogger.trace(className, "CMEntity", "TimeLogs " + "entity initialization started - " + SystemUtils.getCurrentDateTimeMiliSec());
        setName(name);
        setLabel(label);
        setType(type);
        setGeometryType(geometryType);
        setW9IdProperty(w9IdProperty);
        setLabelPropertyName(labelPropertyName);
        setCategoryPropertyName(categoryPropertyName);
        setAbbr(abbr);
        setLocked(isLocked);
        setSelectedRendererName(selectedRendererName);
        setIdGenRules(idGenRules);
        setSimpleStyle(simpleStyle);
        setUniqueValueStyle(uniqueValueStyle);
        setProperties(properties);
        setPropertyGroups(propertyGroups);
        setDependantPropertiesJGraph(dependantPropertiesGraphStr);
        //setDomainValues(domainValues);
        setCategories(categories);
        setHasShadowTable(hasShadowTable);
        setFlowsJArray(flows);
        setTraversalGraph(traversalGraphStr);
        featureLayer = new FeatureLayer();
        featureLayer.setName(name);
        featureLayer.setLabel(label);
        featureLayer.setType(type);
        featureLayer.setGeometryType(geometryType);
        featureLayer.setW9IdProperty(w9IdProperty);
        featureLayer.setLabelPropertyName(labelPropertyName);
        featureLayer.setCategoryPropertyName(categoryPropertyName);
        featureLayer.setAbbr(abbr);
        featureLayer.setLocked(isLocked);
        featureLayer.setSelectedRendererName(selectedRendererName);
        featureLayer.setIdGenRules(idGenRules);
        featureLayer.setSimpleStyle(simpleStyle);
        featureLayer.setUniqueValueStyle(uniqueValueStyle);
        featureLayer.setProperties(properties);
        featureLayer.setPropertyGroups(propertyGroups);
        featureLayer.setDomainValues(domainValues);
        featureLayer.setCategories(categories);
        featureLayer.setHasShadowTable(hasShadowTable);
        //setDAOs(dataGeopackage);
        constructFeatureTable();

        ReveloLogger.trace(className, "CMEntity", "TimeLogs " + "entity initialization ended - " + SystemUtils.getCurrentDateTimeMiliSec());
    }

    public void constructFeatureTable() {
       /* if (type.equalsIgnoreCase("spatial")) {
            if (featureDao != null && featureLayer!=null) {
                featureTable = new FeatureTable(featureDao, featureLayer);
            }
        }else {
            if (attributesDao != null && featureLayer!=null) {
                featureTable = new FeatureTable(attributesDao, featureLayer);
            }
        }*/
        featureTable = new FeatureTable(featureLayer);
    }

    public CMEntity(JSONObject entityJson) {
    }

    public String getTraversalGraphStr() {
        return traversalGraphStr;
    }

    private void setDAOs(GeoPackage dataGeoPackage) {
        /*if (type.equalsIgnoreCase("spatial")) {
            featureDao = dataGeoPackage.getFeatureDao(name+"_"+ UserInfoPreferenceUtility.getSurveyName());
        }else {
            attributesDao = dataGeoPackage.getAttributesDao(name+"_"+ UserInfoPreferenceUtility.getSurveyName());
        }*/
    }

    public JSONGraph getDependantPropertiesJGraph() {
        return dependantPropertiesJGraph;
    }

    public void setDependantPropertiesJGraph(String dependantPropertiesGraphStr) {

        if (dependantPropertiesGraphStr == null || dependantPropertiesGraphStr.isEmpty()) {
            this.dependantPropertiesJGraph = null;
            this.dependantPropertiesJGraphStr = "";
        }
        else {
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
                }
                else {
                    this.dependantPropertiesJGraph = null;
                    this.dependantPropertiesJGraphStr = "";
                    return;
                }
                if (! nodesList.isEmpty() && inputJson.has("edges")) {
                    JSONArray edgesJArray = inputJson.getJSONArray("edges");
                    for (int i = 0; i < edgesJArray.length(); i++) {
                        JSONObject edgeJson = edgesJArray.getJSONObject(i);
                        edgesList.add(edgeJson);
                    }
                }
                else {
                    this.dependantPropertiesJGraph = null;
                    this.dependantPropertiesJGraphStr = "";
                    return;
                }

                if (nodesList.isEmpty() || edgesList.isEmpty()) {
                    this.dependantPropertiesJGraph = null;
                    this.dependantPropertiesJGraphStr = "";
                    return;
                }

                JSONGraph jsonGraph = new JSONGraph("name");
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLabelPropertyName() {
        return labelPropertyName;
    }

    public void setLabelPropertyName(String labelPropertyName) {
        this.labelPropertyName = labelPropertyName;
        String taskName = "setLabelPropertyName";
        try {
            this.labelExpressionProperties = new ArrayList<>();
            if (labelPropertyName != null && ! labelPropertyName.isEmpty()) {
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
                }
                else if (labelPropertyName.contains("{")) {
                    try {
                        String columnName = labelPropertyName.replace("{", "").replace("}", "");
                        labelExpressionProperties.add(columnName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
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

    public SimpleStyleModel getSimpleStyle() {
        return simpleStyle;
    }

    public void setSimpleStyle(String style) {

        simpleStyle = null;

        if (! TextUtils.isEmpty(style)) {
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

        if (! TextUtils.isEmpty(style)) {
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

        if (! TextUtils.isEmpty(propertyString)) {
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

        if (! TextUtils.isEmpty(propertyGroupString)) {

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
        if (! TextUtils.isEmpty(values) && featureLayer != null) {
            try {
                domainValues = new JSONObject(StringEscapeUtils.unescapeJson(values));
                featureLayer.setDomainValues(values);
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

        if (! TextUtils.isEmpty(categoryString)) {
            try {
                categories = new JSONObject(StringEscapeUtils.unescapeJson(categoryString));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JSONArray getFlowsJArray() {
        return flowsJArray;
    }

    public void setFlowsJArray(JSONArray flowsJArray) {
        this.flowsJArray = flowsJArray;
    }

    public void setFlowsJArray(String flowsString) {
        this.flowsJArray = new JSONArray();
        this.hasFlows = false;
        /*if(name.equalsIgnoreCase("boreholes")){
            flowsString = "[\n" +
                    "    {\n" +
                    "      \"name\": \"flow1\",\n" +
                    "      \"label\": \"Default Flow\",\n" +
                    "      \"description\": \"Default entity flow\",\n" +
                    "      \"interactions\": [\n" +
                    "        {\n" +
                    "          \"name\": \"interaction1_ccf_create\",\n" +
                    "          \"label\": \"Default Flow Interaction 1 ccf create depth\",\n" +
                    "          \"description\": \"Default entity flow interaction 1 ccf create depth\",\n" +
                    "          \"roleName\": \"ccf\",\n" +
                    "          \"isExecuted\": false,\n" +
                    "          \"propertiesList\": [\n" +
                    "            \"depth\"\n" +
                    "          ],\n" +
                    "          \"allowedOperationsList\": [\n" +
                    "            \"create\",\n" +
                    "            \"update\",\n" +
                    "            \"delete\"\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"name\": \"interaction3_hoff_delete\",\n" +
                    "          \"label\": \"Default Flow Interaction 3 hoff delete method\",\n" +
                    "          \"description\": \"Default entity flow interaction 3 hoff delete method\",\n" +
                    "          \"roleName\": \"hoff\",\n" +
                    "          \"isExecuted\": false,\n" +
                    "          \"propertiesList\": [\n" +
                    "            \"method\"\n" +
                    "          ],\n" +
                    "          \"allowedOperationsList\": [\n" +
                    "            \"delete\"\n" +
                    "          ]\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"name\": \"interaction2_apccf_update\",\n" +
                    "          \"label\": \"Default Flow Interaction 2 apccf update diameter\",\n" +
                    "          \"description\": \"Default entity flow interaction 2 apccf update diameter\",\n" +
                    "          \"roleName\": \"apccf\",\n" +
                    "          \"isExecuted\": false,\n" +
                    "          \"propertiesList\": [\n" +
                    "            \"diameter\"\n" +
                    "          ],\n" +
                    "          \"allowedOperationsList\": [\n" +
                    "            \"update\"\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"interactionLinks\": [\n" +
                    "        {\n" +
                    "          \"name\": \"flowlink1\",\n" +
                    "          \"label\": \"Flow Interaction Link\",\n" +
                    "          \"description\": \"Flow interaction link 1 and 2\",\n" +
                    "          \"from\": \"interaction1_ccf_create\",\n" +
                    "          \"to\": \"interaction2_apccf_update\",\n" +
                    "          \"properties\": {}\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"name\": \"flowlink2\",\n" +
                    "          \"label\": \"Flow Interaction Link\",\n" +
                    "          \"description\": \"Flow interaction link 2 and 3\",\n" +
                    "          \"from\": \"interaction2_apccf_update\",\n" +
                    "          \"to\": \"interaction3_hoff_delete\",\n" +
                    "          \"properties\": {}\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]";
        }*/
        if (flowsString != null && ! flowsString.isEmpty()) {
            try {
                flowsJArray = new JSONArray(StringEscapeUtils.unescapeJson(flowsString));
                if (flowsJArray.length() > 0) {
                    setFlowGraph(flowsJArray);
                    this.hasFlows = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setHasFlows(boolean hasFlows) {
        this.hasFlows = hasFlows;
    }

    public boolean hasFlows() {
        return hasFlows;
    }

    public FlowGraph getFlowGraph() {
        try {
            if (flowNameGraphMap == null || flowNameGraphMap.size() == 0)
                return null;
            String phaseFlowNameForEntity = UserInfoPreferenceUtility.getPhaseFlowName(name);
            if (phaseFlowNameForEntity == null || phaseFlowNameForEntity.isEmpty()) {//if we dont have phases, send the first flow
                for (String flowName : flowNameGraphMap.keySet()) {
                    return flowNameGraphMap.get(flowName);
                }
            }
            else {
                if (flowNameGraphMap.containsKey(phaseFlowNameForEntity))
                    return flowNameGraphMap.get(phaseFlowNameForEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setFlowGraph(JSONArray flowsJArray) {
        flowNameGraphMap = null;
        try {
            ReveloLogger.trace(className, "setFlowGraph", "TimeLogs " + "flowgraph initialization started - " + SystemUtils.getCurrentDateTimeMiliSec());
            if (flowsJArray != null && flowsJArray.length() > 0) {
                for (int f = 0; f < flowsJArray.length(); f++) {
                    JSONObject flowJObj = flowsJArray.getJSONObject(f);
                    String flowName = flowJObj.getString("name");
                    String flowLabel = flowJObj.getString("label");
                    String flowDescription = flowJObj.getString("description");
                    String entityName = this.name;
                    List<Interaction> interactions = new ArrayList<>();
                    JSONArray interactionArray = flowJObj.getJSONArray("interactions");
                    for (int i = 0; i < interactionArray.length(); i++) {
                        JSONObject interactionJObj = interactionArray.getJSONObject(i);
                        try {
                            Interaction interaction = new Interaction(interactionJObj, flowName);
                            interactions.add(interaction);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    List<InteractionLink> interactionLinks = new ArrayList<>();
                    JSONArray interactionLinksArray = flowJObj.getJSONArray("interactionLinks");
                    for (int i = 0; i < interactionLinksArray.length(); i++) {
                        JSONObject interactionLinkJobj = interactionLinksArray.getJSONObject(i);
                        try {
                            InteractionLink interactionLink = new InteractionLink(interactionLinkJobj, flowName);
                            interactionLinks.add(interactionLink);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    try {
                        if (interactions.size() > 0 && interactionLinks.size() > 0) {
                            Flow flow = new Flow(flowName, flowLabel, flowDescription, entityName, interactions, interactionLinks);
                            FlowGraph flowGraph = GraphFactory.createFlowGraph(flow, "");
                            if (flowNameGraphMap == null) {
                                flowNameGraphMap = new HashMap<>();
                            }
                            flowNameGraphMap.put(flowName, flowGraph);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            flowNameGraphMap = null;
        }
        ReveloLogger.trace(className, "setFlowGraph", "TimeLogs " + "flowgraph initialization ended - " + SystemUtils.getCurrentDateTimeMiliSec());
    }

    public FlowGraph getFlowGraph(String flowName) {
        ReveloLogger.trace(className, "getFlowGraph", "TimeLogs " + "getting flowgraph started - " + SystemUtils.getCurrentDateTimeMiliSec());
        if (flowNameGraphMap == null || flowNameGraphMap.size() == 0) {
            ReveloLogger.trace(className, "getFlowGraph", "TimeLogs " + "getting flowgraph ended - " + SystemUtils.getCurrentDateTimeMiliSec());
            return null;
        }
        if (flowNameGraphMap.containsKey(flowName)) {
            ReveloLogger.trace(className, "getFlowGraph", "TimeLogs " + "getting flowgraph ended - " + SystemUtils.getCurrentDateTimeMiliSec());
            return flowNameGraphMap.get(flowName);
        }
        ReveloLogger.trace(className, "getFlowGraph", "TimeLogs " + "getting flowgraph ended - " + SystemUtils.getCurrentDateTimeMiliSec());
        return null;
    }

    public FeatureLayer getFeatureLayer() {
        return featureLayer;
    }

    @Override
    public String toString() {
        return "CMEntity{" + "name='" + name + '\'' + ", label='" + label + '\'' + ", type='" + type + '\'' + ", geometryType='" + geometryType + '\'' + ", w9IdProperty='" + w9IdProperty + '\'' + ", w9IdLabel='" + labelPropertyName + '\'' + ", categoryPropertyName='" + categoryPropertyName + '\'' + ", abbr='" + abbr + '\'' + ", isLocked=" + isLocked + ", hasShadowTable=" + hasShadowTable + ", selectedRendererName='" + selectedRendererName + '\'' + ", idGenRules=" + idGenRules + ", simpleStyle=" + simpleStyle + ", uniqueValueStyle=" + uniqueValueStyle + ", properties=" + properties + ", propertiesHashMap=" + propertiesHashMap + ", propertyGroups=" + propertyGroups + ", domainValues=" + domainValues + ", perspective=" + perspective + ", categories=" + categories + ", idGenerationType='" + idGenerationType + '\'' + ", featureTable=" + featureTable + ", featureLayer=" + featureLayer +/*
                ", featureDao=" + featureDao +
                ", attributesDao=" + attributesDao +*/
                ", className='" + className + '\'' + '}';
    }

    public JSONObject toJson() {
        JSONObject entityJson = new JSONObject();
        try {
            entityJson.put("name", name);
            entityJson.put("label", label);
            entityJson.put("type", type);
            entityJson.put("geometryType", geometryType);
            entityJson.put("w9IdProperty", w9IdProperty);
            entityJson.put("w9IdLabel", labelPropertyName);
            entityJson.put("categoryPropertyName", categoryPropertyName);
            entityJson.put("abbr", abbr);
            entityJson.put("isLocked", String.valueOf(isLocked()));
            entityJson.put("hasShadowTable", String.valueOf(isHasShadowTable()));
            entityJson.put("selectedRendererName", selectedRendererName);
            entityJson.put("idGenRules", idGenRules.toString());
            entityJson.put("uniqueValueStyle", uniqueValueStyle.toString());
            entityJson.put("properties", Attribute.attributeJsonArray(properties));
            entityJson.put("idGenRules", getIdGenRules());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entityJson;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(String locked) {
        isLocked = locked.equalsIgnoreCase("true");
    }

    public boolean isHasShadowTable() {
        return hasShadowTable;
        // return false;
    }

    public JSONObject getIdGenRules() {
        return idGenRules;
    }

    public void setIdGenRules(String rule) {

        idGenRules = null;

        if (! TextUtils.isEmpty(rule)) {
            try {
                idGenRules = new JSONObject(StringEscapeUtils.unescapeJson(rule));
            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "setSimpleStyle", String.valueOf(e.getCause()));
            }
        }
        else {
            idGenRules = new JSONObject();
        }
    }

    public void setHasShadowTable(String hasShadowTable) {
        this.hasShadowTable = hasShadowTable.equalsIgnoreCase("true");
    }

    //sorted linked list //traversal special
    public LinkedList<Feature> getSortedFeaturesByQuery(Context context, boolean getTraversalFeaturesOnly, List<String> requiredFieldsList,
                                                        JSONArray ORClausesArray, JSONArray ANDClausesArray, String ANDorOR,
                                                        boolean getFromMainTable, boolean getFromShadowTable, boolean isDistinct,
                                                        int startIndex, int limit, boolean queryGeometry, boolean transformGeometry,
                                                        boolean sortList, JSONObject sortJson) {
        long startTime = System.nanoTime();
        String taskName = "getSortedFeaturesByQuery";
        LinkedList<Feature> rawFeatureList = new LinkedList<>();//list that has all features in sequence they were created
        if (getFeatureTable() == null) {
            return rawFeatureList;
        }
        if(getTraversalFeaturesOnly){
            if(getTraversalGraph() == null){
                return rawFeatureList;
            }
        }
        Log.d("traversalfeaturetime", "time1: "+((System.nanoTime())-startTime));
        //  List<Feature> unsortedFeatureList = new LinkedList<>();//features that could not be added to sortfeature list due to null/empty lable. they will be simply appended to sorted feature list.
        //   List<SortCompareUtils.AlphabeticalSortObj> sortFeatureList = new ArrayList<>();
        GeoPackageRWAgent gpkgRWAgent = null;
        try {
            if (getTraversalGraph() != null && getTraversalFeaturesOnly) {
                getTraversalFeaturesOnly = true;
            }else{
                getTraversalFeaturesOnly = false;
            }
        } catch (Exception e) {
            ReveloLogger.error(className, taskName, "exception checking if query for traversal features only- " + e.getMessage());
            e.printStackTrace();
            getTraversalFeaturesOnly = false;
        }
        Log.d("traversalfeaturetime", "time2: "+((System.nanoTime())-startTime));
        if (getTraversalFeaturesOnly) {
            if (ORClausesArray != null) {
                ReveloLogger.info(className, taskName, "Current OrClauseArray Size = " + ORClausesArray.length());
            }
            else {
                ReveloLogger.info(className, taskName, "Current OrClauseArray Size = 0(null)");
            }
            ORClausesArray = getOrCauseArrayForTraversal(ORClausesArray);
            if (ORClausesArray != null) {
                ReveloLogger.info(className, taskName, "After applying traversal, OrClauseArray Size = " + ORClausesArray.length());
            }
            else {
                ReveloLogger.info(className, taskName, "After applying traversal, OrClauseArray Size = 0(null)");
            }
        }

        try {
            Log.d("traversalfeaturetime", "time3: "+((System.nanoTime())-startTime));
            List<Feature> featuresByQuery = getFeatureTable().getFeaturesByQuery(context, requiredFieldsList, ORClausesArray, ANDClausesArray, ANDorOR, getFromMainTable, getFromShadowTable, isDistinct, startIndex, limit, queryGeometry, transformGeometry);
            Log.d("traversalfeaturetime", "time4: "+((System.nanoTime())-startTime));
            if (featuresByQuery != null && ! featuresByQuery.isEmpty()) {
                if (sortList) {
                    ReveloLogger.info(className, "taskName", "Sorting the list");
                    boolean sortAlphabetically = true;
                    if (sortJson == null) {
                        sortAlphabetically = true;
                    }
                    else {
                        sortAlphabetically = false;
                        try {
                            String sortBy = sortJson.getString("sortBy");
                            String sortByProperty = sortJson.getString("sortByProperty");
                            if (sortBy != null && sortByProperty != null && ! sortByProperty.isEmpty()) {
                                if (sortBy.equalsIgnoreCase(ReveloFeatureComparator.TraversalSort)) {
                                    ReveloLogger.info(className, "taskName", "Applying traversal sort");
                                    //Collections.sort(sortFeatureList,SortCompareUtils.alphabeticalComparator);
                                    Log.d("traversalfeaturetime", "time5: "+((System.nanoTime())-startTime));
                                    ReveloFeatureComparator reveloFeatureComparator = new ReveloFeatureComparator(ReveloFeatureComparator.TraversalSort, sortByProperty,getTraversalGraph());
                                    Collections.sort(featuresByQuery, reveloFeatureComparator);
                                    Log.d("traversalfeaturetime", "time6: "+((System.nanoTime())-startTime));
                                }
                                else if (sortBy.equalsIgnoreCase(ReveloFeatureComparator.NumericalSort)) {
                                    ReveloLogger.info(className, "taskName", "Applying numerical on list");
                                    //Collections.sort(sortFeatureList,SortCompareUtils.alphabeticalComparator);
                                    ReveloFeatureComparator reveloFeatureComparator = new ReveloFeatureComparator(ReveloFeatureComparator.NumericalSort, sortByProperty,null);
                                    Collections.sort(featuresByQuery, reveloFeatureComparator);
                                }
                                else {
                                    sortAlphabetically = true;
                                }
                            }
                            else {
                                sortAlphabetically = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            sortAlphabetically = true;
                        }
                    }

                    if (sortAlphabetically) {
                        ReveloLogger.info(className, "taskName", "Could not sort list - sortjson null or exception occurred. Applying toposort on list");
                        //Collections.sort(sortFeatureList,SortCompareUtils.alphabeticalComparator);
                        ReveloFeatureComparator reveloFeatureComparator = new ReveloFeatureComparator(AlphabeticalSort, featureLayer.getLabelPropertyName(),null);
                        Collections.sort(featuresByQuery, reveloFeatureComparator);
                    }
                    rawFeatureList.addAll(featuresByQuery);
                }
                else {
                    rawFeatureList.addAll(featuresByQuery);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        ReveloLogger.info(className, "taskName", "returning list with size " + rawFeatureList.size());
        return rawFeatureList;
    }

    public FeatureTable getFeatureTable() {
        return featureTable;
    }

    //traversal special
    public TraversalGraph getTraversalGraph() {
        return traversalGraph;
    }

    //traversal special
    private void setTraversalGraph(String traversalGraphStr) {
        JSONObject traversalInputJson = null;
        try {
            /*if(traversalGraphStr==null || traversalGraphStr.isEmpty() && name.contains("pot")){
                //return;
                traversalGraphStr="{\"properties\":{\"visibility\":{\"comment\":\"if visibleByDefault = true, then show all the features on the map initially, else use visibility property name and value to decide to show the feature. This applies to all features in this entity.\",\"visibleByDefault\":false,\"visibilityPropertyName\":\"for ex: isVisible\",\"visibilityPropertyValue\":\"for ex: true or false\"},\"approval\":{\"comment\":\"if requiresApproval = true, then field user will communicate with Admin and Admin will update the approvalPropertyName property with value = 'approvalPropertyValue'. Server will communicate this edit to mobile using web sockets and mobile app UI will take cognizance of that and update itself. This applies to all features in this entity.\",\"required\":false,\"approvalPropertyName\":\"for ex: isApproved\",\"approvalPropertyValue\":\"for ex: true or false\"}},\"graph\":{\"nodes\":{\"1001\":{\"isSkipable\":true,\"isVisited\":false},\"1002\":{\"isSkipable\":true,\"isVisited\":false},\"1003\":{\"isSkipable\":true,\"isVisited\":false},\"1004\":{\"isSkipable\":true,\"isVisited\":false},\"1005\":{\"isSkipable\":true,\"isVisited\":false},\"1006\":{\"isSkipable\":true,\"isVisited\":false},\"1007\":{\"isSkipable\":true,\"isVisited\":false},\"1008\":{\"isSkipable\":true,\"isVisited\":false},\"1009\":{\"isSkipable\":true,\"isVisited\":false}},\"edges\":[{\"from\":\"1001\",\"to\":\"1003\"},{\"from\":\"1003\",\"to\":\"1005\"},{\"from\":\"1005\",\"to\":\"1007\"},{\"from\":\"1007\",\"to\":\"1009\"},{\"from\":\"1009\",\"to\":\"1008\"},{\"from\":\"1008\",\"to\":\"1006\"},{\"from\":\"1006\",\"to\":\"1004\"},{\"from\":\"1004\",\"to\":\"1002\"}]}}";
            }*/

            traversalInputJson = new JSONObject(StringEscapeUtils.unescapeJson(traversalGraphStr.trim()));
            this.traversalGraphStr = traversalGraphStr;
            this.traversalGraph = GraphFactory.createTraversalGraph(traversalInputJson, "w9id");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /***
     * This method creates Json whereclause array to be used as or array. it gets only the features assigned to user in traversal graph
     * */
    //traversal special
    public JSONArray getOrCauseArrayForTraversal(JSONArray ORClausesArray) {
        String taskName = "getOrCauseArrayForTraversal";
        boolean getTraversalFeatures = false;
        List<String> traversalFeaturesW9Id = new ArrayList<>();
        try {
            if (getTraversalGraph() != null) {
                getTraversalFeatures = true;
                Set<JSONObject> traversalGraphSet = getTraversalGraph().getAllVertices();
                for (JSONObject travFeatureJson : traversalGraphSet) {
                    if(travFeatureJson.has("w9id")){
                        if (!traversalFeaturesW9Id.contains(travFeatureJson.getString("w9id"))) {
                            traversalFeaturesW9Id.add(travFeatureJson.getString("w9id"));


                            if (ORClausesArray == null) {
                                ORClausesArray = new JSONArray();
                            }

                            JSONObject conditionJobj = new JSONObject();
                            conditionJobj.put("conditionType", "attribute");
                            conditionJobj.put("columnName", featureLayer.getW9IdProperty());
                            conditionJobj.put("valueDataType", "string");
                            conditionJobj.put("value", travFeatureJson.getString("w9id"));
                            conditionJobj.put("operator", "=");
                            ORClausesArray.put(conditionJobj);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ReveloLogger.error(className, taskName, "exception generating list of w9ids assignd to user in traversal - " + e.getMessage());
            e.printStackTrace();
        }

//        try {
//            if (getTraversalFeatures && ! traversalFeaturesW9Id.isEmpty()) {
//                //create orclause array for these feautres
//                if (ORClausesArray == null) {
//                    ORClausesArray = new JSONArray();
//                }
//                for (String w9Id : traversalFeaturesW9Id) {
//                    try {
//                        JSONObject conditionJobj = new JSONObject();
//                        conditionJobj.put("conditionType", "attribute");
//                        conditionJobj.put("columnName", featureLayer.getW9IdProperty());
//                        conditionJobj.put("valueDataType", "string");
//                        conditionJobj.put("value", w9Id);
//                        conditionJobj.put("operator", "=");
//                        ORClausesArray.put(conditionJobj);
//                    } catch (Exception e) {
//                        ReveloLogger.error(className, taskName, "exception adding a traversal feature whereclasue in array - " + e.getMessage());
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            ReveloLogger.error(className, taskName, "exception generating whereclasue aarray for features assignd to user in traversal - " + e.getMessage());
//            e.printStackTrace();
//        }
        return ORClausesArray;
    }

    public void setFeatureTable(FeatureTable featureTable) {
        this.featureTable = featureTable;
    }

    //traversal special
    public JSONObject getCurrentlyTraversingFeature(Context context) {
        JSONObject response = new JSONObject();
        String failureMessage = "Could not get feature being traversed. Reason - ";
        try {
            response.put("status", "failure");
            response.put("message", failureMessage + " Unknown");

            if (getTraversalGraph() == null) {
                response.put("message", failureMessage + " No traversal graph found in entity " + getLabel());
            }
            if (getFeatureTable() == null) {
                response.put("message", failureMessage + " No feature table found in  " + getLabel());
            }

            JSONObject curentlyTraversingVertex = getTraversalGraph().getCurrentlyTraversingFeature();
            if (curentlyTraversingVertex == null || ! curentlyTraversingVertex.has("w9id") || curentlyTraversingVertex.getString("w9id") == null || curentlyTraversingVertex.getString("w9id").isEmpty()) {
                response.put("message", failureMessage + " No vertex was being currently traversed in " + getLabel());
            }
            String w9id = curentlyTraversingVertex.getString("w9id");
            Feature feature = getFeatureTable().getFeature(getW9IdProperty(), w9id, context, true, false, true);
            if (feature != null) {
                response.put("status", "success");
                response.put("message", feature);
            }
        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage(failureMessage, e);
        }
        return response;
    }

    public String getLabel() {
        return label;
    }

    public String getW9IdProperty() {
        return w9IdProperty;
    }

    public void setW9IdProperty(String w9IdProperty) {
        this.w9IdProperty = w9IdProperty;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    //traversal special
   public JSONObject updateFeatureTraversal(String w9id, boolean setTraversed, Context context) {
        JSONObject response = new JSONObject();
        String failureMessage = "Could not get feature being traversed. Reason - ";
        try {
            response.put("status", "failure");
            response.put("message", failureMessage + " Unknown");
            if (getTraversalGraph() == null) {
                response.put("message", failureMessage + " No traversal graph found in entity " + getLabel());
            }
            if (getFeatureTable() == null) {
                response.put("message", failureMessage + " No feature table found in entity " + getLabel());
            }
            Map<String, Object> condition = new HashMap<>();
            condition.put("w9id", w9id);
            List<JSONObject> verticesSet = getTraversalGraph().getVertices(condition);
            if (verticesSet == null || verticesSet.size() == 0) {
                response.put("message", failureMessage + " No feature was found for id " + w9id + " in " + getLabel());
                return response;
            }
            else if (verticesSet.size() > 1) {
                response.put("message", failureMessage + " More than one features was found for id " + w9id + " in " + getLabel());
                return response;
            }
            else {
                Iterator<JSONObject> vertexItr = verticesSet.iterator();

                JSONObject vertex = null;
                while (vertexItr.hasNext()) {
                    try {
                        vertex = vertexItr.next();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (vertex == null) {
                    response.put("message", failureMessage + " No feature was found in traversal for " + getLabel());
                    return response;
                }

                try {
                    JSONObject targetVertex = new JSONObject();
                    targetVertex.put("w9id",w9id);
                    targetVertex.put("isVisited",setTraversed);
                    boolean isUpdated =  traversalGraph.updateVertex(vertex,targetVertex);
                    if(isUpdated) {
                        response.put("status", "success");
                        response.put("message", "updated");
                        //update in db
                        return updateTraversalInDb(traversalGraph.toString(),context);
                    }else {
                        response.put("status", "failure");
                        response.put("message", "updation failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.put("status", "failure");
                    response.put("message", "Exception getthing level of feature with id " + w9id + " - " + e.getMessage());
                    return response;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage(failureMessage, e);
        }
        return response;
    }

    //traversalSpecial
    public JSONObject updateTraversalInDb(String traversalGraphStr,Context context){
        JSONObject respJObj =new JSONObject();
        try {
            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getEntitiesDatasetInfo();

            JSONArray dataJsonArray = new JSONArray();
            JSONObject dataObject = new JSONObject();
            JSONArray attributesJArray = new JSONArray();
            JSONObject traversalJObj = new JSONObject();
            traversalJObj.put("name", CMUtils.ENTITIES_TRAVERSAL);
            traversalJObj.put("value", traversalGraphStr);
            attributesJArray.put(traversalJObj);
            dataObject.put("attributes", attributesJArray);
            dataJsonArray.put(dataObject);


            JSONObject conditionJobj = new JSONObject();
            conditionJobj.put("conditionType", "attribute");
            conditionJobj.put("columnName", "name");
            conditionJobj.put("valueDataType", "text");
            conditionJobj.put("value", getName());
            conditionJobj.put("operator", "=");
            JSONArray whereClauseArray = new JSONArray();
            whereClauseArray.put(conditionJobj);

            respJObj = gpkgRWAgent.updateDatasetContent(DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, dataJsonArray, whereClauseArray, "AND", context);
            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                return respJObj;
            }
            else {
                String reason = "unknown";
                 if(respJObj.has("message")){
                     reason = respJObj.getString("message");
                 }
                respJObj.put("status", "failure");
                respJObj.put("message", "updation failed..reason - "+reason);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return respJObj;
    }
    private JSONObject getEntitiesDatasetInfo() {
        JSONObject datasetInfo = new JSONObject();
        try {
            datasetInfo.put("datasetName", "entities");
            datasetInfo.put("datasetType", "table");
            datasetInfo.put("geometryType", "");
            datasetInfo.put("idPropertyName", "name");
            datasetInfo.put("w9IdPropertyName", "name");
        } catch (Exception e) {
            ReveloLogger.error(className, "getDatasetInfo", "error initializing getDatasetInfo json for dataset:entities Exception - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return datasetInfo;
    }
    //traversal special
    public JSONObject getFirstFeatureInTraversal(Context context) {
        JSONObject response = new JSONObject();
        String failureMessage = "Could not get first feature of traversal sequence. Reason - ";
        try {
            response.put("status", "failure");
            response.put("message", failureMessage + " Unknown");

            if (getTraversalGraph() == null) {
                response.put("message", failureMessage + " No traversal graph found in entity " + getLabel());
            }
            if (getFeatureTable() == null) {
                response.put("message", failureMessage + " No feature table found in  " + getLabel());
            }

            Set<JSONObject> rootVerticesSet = getTraversalGraph().getRootVertices();
            if (rootVerticesSet == null || rootVerticesSet.size() == 0) {
                response.put("message", failureMessage + " No feature was found at starting point in " + getLabel());
                return response;
            }
            else if (rootVerticesSet.size() > 1) {
                response.put("message", failureMessage + " More than one features was found at starting point in " + getLabel());
                return response;
            }
            else {
                Iterator<JSONObject> rootItr = rootVerticesSet.iterator();
                String w9id = null;
                while (rootItr.hasNext()) {
                    try {
                        JSONObject rootVertex = rootItr.next();
                        w9id = rootVertex.getString("w9id");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (w9id == null) {
                    response.put("message", failureMessage + " No feature was  was found at starting point in " + getLabel());
                    return response;
                }
                Feature feature = getFeatureTable().getFeature(getW9IdProperty(), w9id, context, true, false, true);
                if (feature != null) {
                    response.put("status", "success");
                    response.put("message", feature);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage(failureMessage, e);
        }
        return response;
    }

    //traversal special
    public JSONObject getLastFeatureInTraversal(Context context) {
        JSONObject response = new JSONObject();
        String failureMessage = "Could not get last feature of traversal sequence. Reason - ";
        try {
            response.put("status", "failure");
            response.put("message", failureMessage + " Unknown");

            if (getTraversalGraph() == null) {
                response.put("message", failureMessage + " No traversal graph found in entity " + getLabel());
            }
            if (getFeatureTable() == null) {
                response.put("message", failureMessage + " No feature table found in  " + getLabel());
            }

            Set<JSONObject> rootVerticesSet = getTraversalGraph().getLeafVertices();
            if (rootVerticesSet == null || rootVerticesSet.size() == 0) {
                response.put("message", failureMessage + " No feature was found at the end point in " + getLabel());
                return response;
            }
            else if (rootVerticesSet.size() > 1) {
                response.put("message", failureMessage + " More than one features was found at end point in " + getLabel());
                return response;
            }
            else {
                Iterator<JSONObject> rootItr = rootVerticesSet.iterator();
                String w9id = null;
                while (rootItr.hasNext()) {
                    try {
                        JSONObject rootVertex = rootItr.next();
                        w9id = rootVertex.getString("w9id");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (w9id == null) {
                    response.put("message", failureMessage + " No feature was  was found at the end point in " + getLabel());
                    return response;
                }
                Feature feature = getFeatureTable().getFeature(getW9IdProperty(), w9id, context, true, false, true);
                if (feature != null) {
                    response.put("status", "success");
                    response.put("message", feature);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage(failureMessage, e);
        }
        return response;
    }

    //traversal special
    public JSONObject getNextFeatureInTraversal(String w9Id, boolean checkBackwards, Context context) {
        JSONObject response = new JSONObject();
        String failureMessage = "Could not get next feature of traversal sequence";
        if (checkBackwards) {
            failureMessage += " in backward direction for " + getLabel() + ". Reason - ";
        }
        else {
            failureMessage += " in forward direction for " + getLabel() + ". Reason - ";
        }
        try {
            response.put("status", "failure");
            response.put("message", failureMessage + " Unknown");

            if (getTraversalGraph() == null) {
                response.put("message", failureMessage + " No traversal graph found in entity " + getLabel());
            }
            if (getFeatureTable() == null) {
                response.put("message", failureMessage + " No feature table found in  " + getLabel());
            }
            Map<String, Object> condition = new HashMap<>();
            condition.put("w9id", w9Id);
            if(!checkBackwards){
             condition.put("isVisited",false);
            }
            List<JSONObject> verticesSet = getTraversalGraph().getVertices(condition);
            if (verticesSet == null || verticesSet.size() == 0) {
                response.put("message", failureMessage + " No feature was found for id " + w9Id + " in " + getLabel());
                return response;
            }
            else if (verticesSet.size() > 1) {
                response.put("message", failureMessage + " More than one features was found for id " + w9Id + " in " + getLabel());
                return response;
            }
            else {
                Iterator<JSONObject> rootItr = verticesSet.iterator();
                String w9id = null;
                JSONObject vertex = null;
                while (rootItr.hasNext()) {
                    try {
                        vertex = rootItr.next();
                        try {
                        w9id = vertex.getString("w9id");
                    } catch (Exception e) {
                        e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (w9id == null) {
                    response.put("message", failureMessage + " No feature was found in traversal for " + getLabel());
                    return response;
                }


                if (getTraversalGraph().isRootVertex(vertex) && checkBackwards) {
                    response.put("status", "failure");
                    response.put("message", "Cannot move back , " + w9Id + " is starting feature");
                    return response;
                }
                else if (getTraversalGraph().isLeafVertex(vertex) && ! checkBackwards) {
                    response.put("status", "failure");
                    response.put("message", "Cannot move forward , " + w9Id + " is feature at end");
                    return response;
                }
                else {
                    if (checkBackwards) {
                        Set<JSONObject> ancestorVertexSet = getTraversalGraph().getAncestors(vertex);
                        if (ancestorVertexSet == null || ancestorVertexSet.size() == 0) {
                            response.put("message", failureMessage + " No feature was found before id " + w9Id + " in " + getLabel());
                            return response;
                        }
                        else if (ancestorVertexSet.size() > 1) {
                            response.put("message", failureMessage + " More than one features was found before id " + w9Id + " in " + getLabel());
                            return response;
                        }
                        else {
                            Iterator<JSONObject> ancestorItr = ancestorVertexSet.iterator();
                            String w9idAncestor = null;
                            JSONObject vertexAncestor = null;
                            while (ancestorItr.hasNext()) {
                                try {
                                    vertexAncestor = ancestorItr.next();
                                    w9idAncestor = vertexAncestor.getString("w9id");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (w9idAncestor == null) {
                                response.put("message", failureMessage + " No feature was found before " + w9Id + " in " + getLabel());
                                return response;
                            }
                            else {
                                Feature feature = getFeatureTable().getFeature(getW9IdProperty(), w9id, context, true, false, true);
                                if (feature != null) {
                                    response.put("status", "success");
                                    response.put("message", feature);
                                }
                            }
                        }
                    }
                    else {

                        List<JSONObject> childVertices = getTraversalGraph().getChildren(vertex);
                        if (childVertices == null || childVertices.size() == 0) {
                            response.put("message", failureMessage + " No feature was found before id " + w9Id + " in " + getLabel());
                            return response;
                        }
                        else if (childVertices.size() > 1) {
                            response.put("message", failureMessage + " More than one features was found before id " + w9Id + " in " + getLabel());
                            return response;
                        }
                        else {
                            Iterator<JSONObject> childItr = childVertices.iterator();
                            String w9idchild = null;
                            JSONObject vertexChild = null;
                            while (childItr.hasNext()) {
                                try {
                                    vertexChild = childItr.next();
                                    w9idchild = vertexChild.getString("w9id");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (w9idchild == null) {
                                response.put("message", failureMessage + " No feature was found after " + w9Id + " in " + getLabel());
                                return response;
                            }
                            else {
                                Feature feature = getFeatureTable().getFeature(getW9IdProperty(), w9id, context, true, false, true);
                                if (feature != null) {
                                    response.put("status", "success");
                                    response.put("message", feature);
                                    return response;
                                }
                            }
                        }

                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage(failureMessage, e);
        }
        return response;
    }

    //traversal special
    public JSONObject isFirstInTraversal(String w9Id, Context context) {
        JSONObject response = new JSONObject();
        String failureMessage = "Could not get  feature with id " + w9Id + " in traversal sequence. Reason - ";

        try {
            response.put("status", "failure");
            response.put("message", failureMessage + " Unknown");

            if (getTraversalGraph() == null) {
                response.put("message", failureMessage + " No traversal graph found in entity " + getLabel());
            }
            if (getFeatureTable() == null) {
                response.put("message", failureMessage + " No feature table found in  " + getLabel());
            }
            Map<String, Object> condition = new HashMap<>();
            condition.put("w9id", w9Id);
            List<JSONObject> verticesSet = getTraversalGraph().getVertices(condition);
            if (verticesSet == null || verticesSet.size() == 0) {
                response.put("message", failureMessage + " No feature was found for id " + w9Id + " in " + getLabel());
                return response;
            }
            else if (verticesSet.size() > 1) {
                response.put("message", failureMessage + " More than one features was found for id " + w9Id + " in " + getLabel());
                return response;
            }
            else {
                Iterator<JSONObject> rootItr = verticesSet.iterator();
                String w9id = null;
                JSONObject vertex = null;
                while (rootItr.hasNext()) {
                    try {
                        vertex = rootItr.next();
                        w9id = vertex.getString("w9id");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (w9id == null) {
                    response.put("message", failureMessage + " No feature was found in traversal for " + getLabel());
                    return response;
                }


                if (getTraversalGraph().isRootVertex(vertex)) {
                    response.put("status", "success");
                    response.put("message", true);
                    return response;
                }
                else {
                    response.put("status", "failure");
                    response.put("message", "feature with id " + w9Id + " is not root of traversal in " + getLabel());
                    return response;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage(failureMessage, e);
        }
        return response;
    }

    //traversal special
    public JSONObject isLastInTraversal(String w9Id, Context context) {
        JSONObject response = new JSONObject();
        String failureMessage = "Could not get  feature with id " + w9Id + " in traversal sequence. Reason - ";

        try {
            response.put("status", "failure");
            response.put("message", failureMessage + " Unknown");

            if (getTraversalGraph() == null) {
                response.put("message", failureMessage + " No traversal graph found in entity " + getLabel());
            }
            if (getFeatureTable() == null) {
                response.put("message", failureMessage + " No feature table found in  " + getLabel());
            }
            Map<String, Object> condition = new HashMap<>();
            condition.put("w9id", w9Id);
            List<JSONObject> verticesSet = getTraversalGraph().getVertices(condition);
            if (verticesSet == null || verticesSet.size() == 0) {
                response.put("message", failureMessage + " No feature was found for id " + w9Id + " in " + getLabel());
                return response;
            }
            else if (verticesSet.size() > 1) {
                response.put("message", failureMessage + " More than one features was found for id " + w9Id + " in " + getLabel());
                return response;
            }
            else {
                Iterator<JSONObject> rootItr = verticesSet.iterator();
                String w9id = null;
                JSONObject vertex = null;
                while (rootItr.hasNext()) {
                    try {
                        vertex = rootItr.next();
                        w9id = vertex.getString("w9id");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (w9id == null) {
                    response.put("message", failureMessage + " No feature was found in traversal for " + getLabel());
                    return response;
                }


                if (getTraversalGraph().isLeafVertex(vertex)) {
                    response.put("status", "success");
                    response.put("message", true);
                    return response;
                }
                else {
                    response.put("status", "failure");
                    response.put("message", "feature with id " + w9Id + " is not root of traversal in " + getLabel());
                    return response;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage(failureMessage, e);
        }
        return response;
    }

    public JSONObject getTraversalLevel(String w9Id, Context context) {
        JSONObject response = new JSONObject();
        String failureMessage = "Could not get  level for feature with id " + w9Id + " in traversal sequence. Reason - ";

        try {
            response.put("status", "failure");
            response.put("message", failureMessage + " Unknown");

            if (getTraversalGraph() == null) {
                response.put("message", failureMessage + " No traversal graph found in entity " + getLabel());
            }
            if (getFeatureTable() == null) {
                response.put("message", failureMessage + " No feature table found in  " + getLabel());
            }
            Map<String, Object> condition = new HashMap<>();
            condition.put("w9id", w9Id);
            List<JSONObject> verticesSet = getTraversalGraph().getVertices(condition);
            if (verticesSet == null || verticesSet.size() == 0) {
                response.put("message", failureMessage + " No feature was found for id " + w9Id + " in " + getLabel());
                return response;
            }
            else if (verticesSet.size() > 1) {
                response.put("message", failureMessage + " More than one features was found for id " + w9Id + " in " + getLabel());
                return response;
            }
            else {
                Iterator<JSONObject> rootItr = verticesSet.iterator();
                String w9id = null;
                JSONObject vertex = null;
                while (rootItr.hasNext()) {
                    try {
                        vertex = rootItr.next();
                        w9id = vertex.getString("w9id");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (w9id == null) {
                    response.put("message", failureMessage + " No feature was found in traversal for " + getLabel());
                    return response;
                }

                try {
                    int depth = getTraversalGraph().getBFSIterator().getDepth(vertex);
                    response.put("status", "success");
                    response.put("message", depth);
                } catch (Exception e) {
                    e.printStackTrace();
                    response.put("status", "failure");
                    response.put("message", "Exception getthing level of feature with id " + w9Id + " - " + e.getMessage());
                    return response;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            SystemUtils.logAndReturnErrorMessage(failureMessage, e);
        }
        return response;
    }
}
