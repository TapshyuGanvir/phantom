package com.sixsimplex.treasurehunt.revelocore.obConceptModel;

import android.app.Activity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.layer.Attribute;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.model.OBDataModel;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.model.ObReEntity;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.model.ObReRelation;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.sharedPreference.OrgBoundaryPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.view.IOrgBoundaryConceptModel;
import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.TinkerGraphUtil;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.tinkerpop.blueprints.Graph;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgBoundaryConceptModel {

    private static final String className = "OrgBoundaryConceptModel";
    private static Graph obReGraph = null;
    private static HashMap<String, String> jurisdictionNamesIdMap = null;
    public OrgBoundaryConceptModel(Activity activity, IOrgBoundaryConceptModel iOrgBoundaryConceptModel) {
        getOrgBoundaryConceptModel(activity, iOrgBoundaryConceptModel);
    }

    public static void clearObReGraph() {
        obReGraph = null;
        if(jurisdictionNamesIdMap!=null) {
            jurisdictionNamesIdMap.clear();
            jurisdictionNamesIdMap=null;
        }
    }

    private void getOrgBoundaryConceptModel(Activity activity, IOrgBoundaryConceptModel iOrgBoundaryConceptModel) {

        String orgBoundaryDataModelUrl = UrlStore.getOrgBoundaryConceptModelGraphUrl();
        if (orgBoundaryDataModelUrl.trim().length() == 0) {
            iOrgBoundaryConceptModel.onError("Organization boundary data model url not found.");
        } else {

//            if (progressDialog == null || !progressDialog.isShowing()) {
//                progressDialog = ProgressUtility.showProgressDialog(activity, "Provisioning Organization boundaries",
//                        activity.getResources().getString(R.string.progress_message_login));
//            }

//            ProgressDialog finalProgressDialog = progressDialog;
            try {

                StringRequest myReq = new StringRequest(Request.Method.GET, orgBoundaryDataModelUrl, response -> {

                    try {

                        JSONObject obCmJsonObject = new JSONObject(response);
                        JSONArray entitiesArray = obCmJsonObject.has("entities") ? obCmJsonObject.getJSONArray("entities") : null;
                        JSONArray relationShipArray = obCmJsonObject.has("relations") ? obCmJsonObject.getJSONArray("relations") : null;

                        String dataSourceName = obCmJsonObject.has("datasourceName") ? obCmJsonObject.getString("datasourceName") : "";
                        String gisServerUrl = obCmJsonObject.has("gisServerUrl") ? obCmJsonObject.getString("gisServerUrl") : "";

                        OBDataModel OBDataModel = null;
                        if (entitiesArray != null && relationShipArray != null) {
                            OBDataModel = createObRe(entitiesArray, relationShipArray);
                            OBDataModel.setDataSourceName(dataSourceName);
                            OBDataModel.setGisServerUrl(gisServerUrl);

                            if (OBDataModel != null) {
                                iOrgBoundaryConceptModel.onSuccess(OBDataModel);
                            } else {
                                iOrgBoundaryConceptModel.onError("Unable to retrieve organization boundary entities & relation, Please try again later.");
                            }
                        }

                        ReveloLogger.debug(className, "getOrgBoundaryConceptModel", "Created obre");

                    } catch (Exception e) {
                        e.printStackTrace();
                        iOrgBoundaryConceptModel.onError("failed");
                        ReveloLogger.error(className, "getOrgBoundaryConceptModel", e.getMessage());
                    }
                }, error -> {

                    String errorDescription = NetworkUtility.getErrorFromVolleyError(error);
                    iOrgBoundaryConceptModel.onError(errorDescription);

                    ReveloLogger.error(className, "getOrgBoundaryConceptModel", "Faild to load " + errorDescription);

                }) {
                    @Override
                    public Map<String, String> getHeaders() {

                        String accessToken = SecurityPreferenceUtility.getAccessToken();

                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", AppConstants.CONTENT_TYPE_APPLICATION_JSON);
                        params.put("Authorization", "Bearer " + accessToken);

                        return params;
                    }
                };

                myReq.setRetryPolicy(new DefaultRetryPolicy(15000, 1, 1));
                RequestQueue queue = Volley.newRequestQueue(activity);
                queue.add(myReq);

            } catch (Exception e) {
                e.printStackTrace();
                iOrgBoundaryConceptModel.onError(e.toString());
                ReveloLogger.error(className, "getOrgBoundaryConceptModel", "Faild to load " + e.getCause());

            }
        }
    }

    private OBDataModel createObRe(JSONArray entitiesArray, JSONArray relationShipArray) {

        OBDataModel OBDataModel = new OBDataModel();

        try {

            List<ObReEntity> obReEntities = new ArrayList<>();

            for (int i = 0; i < entitiesArray.length(); i++) {

                JSONObject entityObject = entitiesArray.getJSONObject(i);

                String name = entityObject.has("name") ? entityObject.getString("name") : "";
                String label = entityObject.has("label") ? entityObject.getString("label") : "";
                String type = entityObject.has("type") ? entityObject.getString("type") : "";
                String geometryType = entityObject.has("geometryType") ? entityObject.getString("geometryType") : "";
                String w9IdPropertyName = entityObject.has("w9IdPropertyName") ? entityObject.getString("w9IdPropertyName") : "";

                JSONObject misPropertyObject = entityObject.has("miscProperties") ? entityObject.getJSONObject("miscProperties") : null;

                boolean isReferenceEntity = false;
                if (misPropertyObject != null) {
                    isReferenceEntity = misPropertyObject.has("isReferenceEntity") && misPropertyObject.getBoolean("isReferenceEntity");
                }
                JSONArray propertiesJArray = entityObject.getJSONArray("properties");
                HashMap<String, Attribute> propertiesHashMap = new HashMap<>();
                for(int j=0;j<propertiesJArray.length();j++){
                    JSONObject propertyJobj = propertiesJArray.getJSONObject(j);
                    Attribute attribute = new Attribute();
                    attribute.setName(propertyJobj.getString("name"));
                    attribute.setType(propertyJobj.getString("type"));
                    attribute.setDefaultValue(propertyJobj.getString("defaultValue"));
                    attribute.setLabel(propertyJobj.getString("label"));
                    attribute.setEnable(propertyJobj.getBoolean("enabled"));
                    attribute.setMandatory(propertyJobj.getBoolean("isMandatory"));
                    propertiesHashMap.put(attribute.getName(),attribute);
                }
                ObReEntity obReEntity = new ObReEntity();
                obReEntity.setName(name);
                obReEntity.setLabel(label);
                obReEntity.setType(type);
                obReEntity.setGeometryType(geometryType);
                obReEntity.setW9IdPropertyName(w9IdPropertyName);
                obReEntity.setReferenceEntity(isReferenceEntity);
                obReEntity.setPropertiesHashMap(propertiesHashMap);
                obReEntities.add(obReEntity);
            }

            List<ObReRelation> obReRelations = new ArrayList<>();

            for (int i = 0; i < relationShipArray.length(); i++) {

                JSONObject relationShipObject = relationShipArray.getJSONObject(i);

                String name = relationShipObject.has("name") ? relationShipObject.getString("name") : "";
                String type = relationShipObject.has("type") ? relationShipObject.getString("type") : "";
                String from = relationShipObject.has("from") ? relationShipObject.getString("from") : "";
                String to = relationShipObject.has("to") ? relationShipObject.getString("to") : "";
                String fromId = relationShipObject.has("fromId") ? relationShipObject.getString("fromId") : "";
                String toId = relationShipObject.has("toId") ? relationShipObject.getString("toId") : "";

                ObReRelation obReRelation = new ObReRelation();
                obReRelation.setName(name);
                obReRelation.setType(type);
                obReRelation.setFrom(from);
                obReRelation.setTo(to);
                obReRelation.setFromId(fromId);
                obReRelation.setToId(toId);

                obReRelations.add(obReRelation);
            }

            OBDataModel.setObReEntityList(obReEntities);
            OBDataModel.setObReRelations(obReRelations);

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "getOrgBoundaryConceptModel", "Faild to load " + e.getCause());
        }
        return OBDataModel;
    }

    public static Graph getObReGraph() {

        if (obReGraph == null) {
            obReGraph = createGraphFromEntity();
        }

        return obReGraph;
    }

    private static Graph createGraphFromEntity() {

        Graph graph = null;

        JSONObject obDataModelGraph = new JSONObject();

        try {

            obDataModelGraph.put(GraphConstants.DIRECTED, false);
            obDataModelGraph.put(GraphConstants.TYPE, false);
            obDataModelGraph.put(GraphConstants.V_LABEL, "obConceptModelGraphJson");
            obDataModelGraph.put(GraphConstants.MODE, "NORMAL");

            JSONArray verticesJsonArray = new JSONArray();

            OBDataModel OBDataModel = OrgBoundaryPreferenceUtility.getObRe();

            if (OBDataModel != null) {

                List<ObReEntity> obReEntities = OBDataModel.getObReEntityList();

                for (ObReEntity obReEntity : obReEntities) {

                    String name = obReEntity.getName();
                    String label = obReEntity.getLabel();
                    String type = obReEntity.getType();
                    String geometryType = obReEntity.getGeometryType();
                    String w9IdPropertyName = obReEntity.getW9IdPropertyName();
                    boolean isReferenceEntity = obReEntity.isReferenceEntity();

                    JSONObject vertexObject = new JSONObject();
                    vertexObject.put(GraphConstants._ID, name);
                    vertexObject.put(GraphConstants._TYPE, GraphConstants.VERTEX);
                    vertexObject.put(GraphConstants.NAME, name);
                    vertexObject.put(GraphConstants.V_LABEL, label);
                    vertexObject.put(GraphConstants.ENTITY_TYPE, type);
                    vertexObject.put(GraphConstants.GEOMETRY_TYPE, geometryType);
                    vertexObject.put(GraphConstants.ID_PROPERTY, w9IdPropertyName);
                    vertexObject.put(GraphConstants.IS_REFERENCE, isReferenceEntity);
                    vertexObject.put(GraphConstants.IS_REFERENCE, isReferenceEntity);
                    vertexObject.put(GraphConstants.PROPERTIES_LIST, obReEntity.getPropertiesHashMap());

                    verticesJsonArray.put(vertexObject);
                }

                List<ObReRelation> obReRelations = OBDataModel.getObReRelations();
                JSONArray edgesJArray = new JSONArray();

                for (ObReRelation obReRelation : obReRelations) {

                    String name = obReRelation.getName();
                    String type = obReRelation.getType();
                    String from = obReRelation.getFrom();
                    String to = obReRelation.getTo();
                    String fromId = obReRelation.getFromId();
                    String toId = obReRelation.getToId();

                    JSONObject edge = new JSONObject();
                    edge.put(GraphConstants._ID, name);
                    edge.put(GraphConstants.NAME, name);
                    edge.put(GraphConstants.V_LABEL, name);
                    edge.put(GraphConstants.DESCRIPTION, name);
                    edge.put(GraphConstants.TYPE, type);
                    edge.put(GraphConstants._TYPE, "edge");
                    edge.put(GraphConstants.FROM_ID, fromId);
                    edge.put(GraphConstants.TO_ID, toId);
                    edge.put(GraphConstants.FROM, from);
                    edge.put(GraphConstants.TO, to);
                    edge.put(GraphConstants.IN_V, to);
                    edge.put(GraphConstants.OUT_V, from);
                    edgesJArray.put(edge);
                }

                obDataModelGraph.put(GraphConstants.VERTICES, verticesJsonArray);
                obDataModelGraph.put(GraphConstants.EDGES, edgesJArray);

                graph = TinkerGraphUtil.convertJSONToGraph(obDataModelGraph);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return graph;
    }

    public static HashMap<String, String> createJurisdictionNamesIdMap() {
        String taskName = "createJurisdictionNamesIdMap";
        if(jurisdictionNamesIdMap==null) {
            jurisdictionNamesIdMap = new HashMap<>();
            try {
                OBDataModel OBDataModel = OrgBoundaryPreferenceUtility.getObRe();
                List<ObReEntity> obReEntities = OBDataModel.getObReEntityList();

                if (obReEntities != null && obReEntities.size() != 0) {
                    for (int i = 0; i < obReEntities.size(); i++) {

                        String entityName = obReEntities.get(i).getName();
                        String w9IDFieldName = obReEntities.get(i).getW9IdPropertyName();

                        jurisdictionNamesIdMap.put(entityName, w9IDFieldName);
                    }
                }
                else {
                    ReveloLogger.error(className, taskName, "Error generating jurisdiction values map - No obcm entities found");
                }
            } catch (Exception e) {
                ReveloLogger.error(className, taskName, "Exception generating jurisdiction values map - " + e.getMessage());
                e.printStackTrace();
            }
        }
        return jurisdictionNamesIdMap;

    }
}