package com.sixsimplex.treasurehunt.revelocore.conceptModel;

import android.app.Activity;
import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel.Interaction;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageManagerAgent;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageRWAgent;
import com.sixsimplex.treasurehunt.revelocore.graph.GraphFactory;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.graph.flowinteractiongraph.FlowGraph;

import com.sixsimplex.treasurehunt.revelocore.layer.Attribute;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.sharedPreference.OrgBoundaryPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.phaseDetails.model.Phase;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.userProfile.UserProfileModel;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SurveyPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;

public class CMUtils {

    private static final String ENTITIES_TABLE_NAME = "entities";
    private static final String ENTITIES_NAME = "name";
    private static final String ENTITIES_LABEL = "label";
    private static final String ENTITIES_ABBR = "abbr";
    private static final String ENTITIES_TYPE = "type";
    private static final String ENTITIES_GEOMETRY_TYPE = "geometrytype";
    private static final String ENTITIES_W9_ID_PROPERTY = "w9idpropertyname";
    private static final String ENTITIES_LABEL_PROPERTY = "labelpropertyname";
    private static final String ENTITIES_CATEGORY_PROPERTY_NAME = "categorypropertyname";
    private static final String ENTITIES_IS_LOCKED = "islocked";
    private static final String ENTITIES_SELECTED_RENDERER_NAME = "selectedRendererName";
    private static final String ENTITIES_ID_GEN_RULES = "idgenrules";
    private static final String ENTITIES_TEXT_STYLE = "textstyle";
    private static final String ENTITIES_SIMPLE_STYLE = "simplestyle";
    private static final String ENTITIES_UNIQUE_VALUE_STYLE = "uniquevaluestyle";
    private static final String ENTITIES_HEAT_MAP_STYLE = "heatmapstyle";
    private static final String ENTITIES_CLUSTER_STYLE = "clusterstyle";
    private static final String ENTITIES_PROPERTIES = "properties";
    private static final String ENTITIES_PROPERTY_GROUPS = "propertygroups";
    private static final String ENTITIES_DOMAIN_VALUES = "domainvalues";
    private static final String ENTITIES_DEPENDANT_PROPERTIES_GRAPH = "deppropsgraph";
    private static final String ENTITIES_PERSPECTIVES = "perspectives";
    private static final String ENTITIES_CATEGORIES = "categories";
    private static final String ENTITIES_HASSHADOWTABLE = "hasshadowtable";
    private static final String ENTITIES_FLOWS = "flows";
    public static final String ENTITIES_TRAVERSAL = "traversals";
    private static final String ENTITIES_ID_GENERATION_TYPE = "idgenerationtype";
    private static final String ENTITIES_IS_LOCK_LEAF = "islockleaf";
    /*------------------------- relationships table -------------------------*/
    private static final String RELATIONSHIPS_TABLE_NAME = "relationships";
    private static final String RELATIONSHIPS_NAME = "name";
    private static final String RELATIONSHIPS_FROM_COL = "fromcol";
    private static final String RELATIONSHIPS_TO_COL = "tocol";
    private static final String RELATIONSHIPS_FROM_ID_COL = "fromidcol";
    private static final String RELATIONSHIPS_TO_ID_COL = "toidcol";
    private static final String className = "CMUtils";
   // private static AttributesDao entititesDao;
   // private static AttributesDao relationsDao;
    private static List<CMEntity> CMEntitiesList = null;
    // private static Map<String,CMEntity> CMEntitiesMap = null;
    private static List<CMRelation> CMRelationsList = null;
    private static CMGraph CMGraph = null;
    private static ReveloConceptModel reveloConceptModel = null;

    public static void clearAttributeDao() {
        /*if (relationsDao != null) {
            relationsDao = null;
        }
        if (entititesDao != null) {
            entititesDao = null;
        }*/
    }

    public static void clearCMVariables() {
        CMGraph = null;
        CMEntitiesList = null;
        CMRelationsList = null;
        reveloConceptModel = null;
    }

    public static void updateCmEntitiesFeatureTables(Context context) {

        JSONObject graphResult = CMUtils.getCMGraph(context);
        CMGraph cmGraph = null;
        try {
            if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                cmGraph = (CMGraph) graphResult.get("result");
            }
            else {
                ReveloLogger.error("CMUTils", "updateCmEntitiesFeatureTables", "Could not create enitities list - could not fetch graph from memory. Reason - " + graphResult.getString("message"));
            }

            if (cmGraph == null) {
                return;
            }

            Set<CMEntity> cmEntitiesSet = cmGraph.getAllVertices();
            Iterator<CMEntity> cmEntityIterator = cmEntitiesSet.iterator();
            while (cmEntityIterator.hasNext()) {
                CMEntity cmEntity = cmEntityIterator.next();
                if (cmEntity != null) {
                    cmEntity.constructFeatureTable();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getCMGraph(Context context) {
        if (CMGraph == null) {
            try {
                ReveloLogger.debug("CMUtils", "getCMGraph", "Creating graph in memory..");
                if (reveloConceptModel == null) {
                    ReveloLogger.debug("CMUtils", "getCMGraph", "Creating conceptModel in memory..");
                    String gisUrl = OrgBoundaryPreferenceUtility.getGisServerUrl();
                    String surveyName = UserInfoPreferenceUtility.getSurveyName();
                    String conceptModelName = SurveyPreferenceUtility.getSurvey(surveyName).getConceptModelName();
                    JSONObject entitiesResult = getCMEntitiesList(context);
                    // List<CMEntity> entitiesResult = getCMEntitiesList(context);
                    if (entitiesResult.has("status") && entitiesResult.getString("status").equalsIgnoreCase("success")) {
                        // if (entitiesResult != null) {

                        List<CMEntity> cmEntitiesList = (List<CMEntity>) entitiesResult.get("result");


                        JSONObject relationsResult = getCMRelationsList(context);
                        //List<CMRelation> relationsResult = getCMRelationsList(context);
                        if (relationsResult.has("status") && relationsResult.getString("status").equalsIgnoreCase("success")) {
                            //if (relationsResult != null) {
                            List<CMRelation> cmRelationsList = (List<CMRelation>) relationsResult.get("result");


                            reveloConceptModel = new ReveloConceptModel(conceptModelName, gisUrl, cmEntitiesList, cmRelationsList);
                            CMGraph = GraphFactory.createCMGraph(reveloConceptModel, "");
                        }
                        else {
                            CMGraph = null;
                            ReveloLogger.error("CMUtils", "getCMGraph", "Error fetching relations list..could not create graph");
                            return SystemUtils.logAndReturnMessage("failure", "Error fetching relations list..could not create graph");
                            //return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,"Error fetching relations list..could not create graph",null);

                        }
                    }
                    else {
                        CMGraph = null;
                        ReveloLogger.error("CMUtils", "getCMGraph", "Error fetching entities list..could not create graph");
                        return SystemUtils.logAndReturnMessage("failure", "Error fetching entities list..could not create graph");
                        //  return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,"Error fetching entities list..could not create graph",null);
                    }

                }
            } catch (Exception e) {
                CMGraph = null;
                ReveloLogger.error("CMUtils", "getCMGraph", "Exception occurred while creating CM graph - " + e.getCause());
                return SystemUtils.logAndReturnErrorMessage("Exception occurred while creating CM graph -", e);
                //   return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,"Exception occurred while creating CM graph - "+e.getCause(),null);
            }
        }
        return SystemUtils.logAndReturnObject("success", "", CMGraph);
        //return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_SUCCESS,"",CMGraph);
    }

    public static JSONObject getCMEntitiesList(Context context) throws Exception {
        ReveloLogger.debug("CMUtils", "getCMEntitiesList", "Fetching cm entities list from memory..");
        // JSONObject resultJson = new JSONObject();
        AttributesDao entitiesAttributeDao=null;
        if (CMEntitiesList == null) {
            try {
                ReveloLogger.debug("CMUtils", "getCMEntitiesList", "creating CM entities list. This method executes only once after login. These variables are washed out on logout.");
                CMEntitiesList = new ArrayList<>();

                entitiesAttributeDao = getEntitiesDao(context);
                if (entitiesAttributeDao == null) {
                    CMEntitiesList = null;
                    ReveloLogger.debug("CMUtils", "getCMEntitiesList", "error creating CM entities list. DAO for table entities not found.. aborting..");
                    return SystemUtils.logAndReturnMessage("failure", "Error creating CM Entities list, could not read database...");
                        /*return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                                "Error creating CM Entities list, could not get DAO",null);*/
                }

                //GeoPackage dataGeoPackage = GeoPackageManagerAgent.getDataGeoPackage(context, DbRelatedConstants.getPropertiesJsonForDataGpkg(context));
                GeoPackage dataGeoPackage = null;
                try (AttributesCursor attributesCursor = entitiesAttributeDao.queryForAll()) {

                    while (attributesCursor.moveToNext()) {

                        try {

                            AttributesRow row = attributesCursor.getRow();

                            String name = "";
                            Object na = row.getValue(ENTITIES_NAME);
                            if (na != null) {
                                name = String.valueOf(na);
                            }

                            String label = "";
                            Object la = row.getValue(ENTITIES_LABEL);
                            if (la != null) {
                                label = String.valueOf(la);
                            }

                            String abbr = "";
                            Object ab = row.getValue(ENTITIES_ABBR);
                            if (ab != null) {
                                abbr = String.valueOf(ab);
                            }

                            String type = "";
                            Object ty = row.getValue(ENTITIES_TYPE);
                            if (ty != null) {
                                type = String.valueOf(ty);
                            }

                            String geomType = "";
                            Object gType = row.getValue(ENTITIES_GEOMETRY_TYPE);
                            if (gType != null) {
                                geomType = String.valueOf(gType);
                            }

                            String w9IdName = "";
                            Object w9Name = row.getValue(ENTITIES_W9_ID_PROPERTY);
                            if (w9Name != null) {
                                w9IdName = String.valueOf(w9Name);
                            }

                            String labelPropertyName = "";
                            Object labelPropertyNameObj = row.getValue(ENTITIES_LABEL_PROPERTY);
                            if (labelPropertyNameObj != null) {
                                labelPropertyName = String.valueOf(labelPropertyNameObj);
                            }

                            String categoryName = "";
                            Object catName = row.getValue(ENTITIES_CATEGORY_PROPERTY_NAME);
                            if (catName != null) {
                                categoryName = String.valueOf(catName);
                            }

                            String isLocked = "";
                            Object loc = row.getValue(ENTITIES_IS_LOCKED);
                            if (loc != null) {
                                isLocked = String.valueOf(loc);
                            }

                            String rendererName = "";
                            Object renName = row.getValue(ENTITIES_SELECTED_RENDERER_NAME);
                            if (renName != null) {
                                rendererName = String.valueOf(renName);
                            }

                            String idRule = "";
                          //  Object rule = row.getValue(ENTITIES_ID_GEN_RULES);
                          //  if (rule != null) {
                           //     idRule = String.valueOf(rule);
                           // }

                            String simpleStyle = "";
                            Object sStyle = row.getValue(ENTITIES_SIMPLE_STYLE);
                            if (sStyle != null) {
                                simpleStyle = String.valueOf(sStyle);
                            }

                            String uniqueStyle = "";
                            Object uStyle = row.getValue(ENTITIES_UNIQUE_VALUE_STYLE);
                            if (uStyle != null) {
                                uniqueStyle = String.valueOf(uStyle);
                            }

                            String properties = "";
                            Object pro = row.getValue(ENTITIES_PROPERTIES);
                            if (pro != null) {
                                properties = String.valueOf(pro);
                            }

                            String propertyGroups = "";
                            Object proGroup = row.getValue(ENTITIES_PROPERTY_GROUPS);
                            if (proGroup != null) {
                                propertyGroups = String.valueOf(proGroup);
                            }

                            String domainValues = "";
                            /*Object doValue = row.getValue(ENTITIES_DOMAIN_VALUES);
                            if (doValue != null) {
                                domainValues = String.valueOf(doValue);
                            }*/

                            String dependantPropertiesGraphStr = "";
                            Object dpPropGValue = row.getValue(ENTITIES_DEPENDANT_PROPERTIES_GRAPH);
                            if (dpPropGValue != null) {
                                dependantPropertiesGraphStr = String.valueOf(dpPropGValue);
                            }

                            String categories = "";
                            Object cat = row.getValue(ENTITIES_CATEGORIES);
                            if (cat != null) {
                                categories = String.valueOf(cat);
                            }

                            String hasShadowTable = "";
                            Object hasShadowt = row.getValue(ENTITIES_HASSHADOWTABLE);
                            if (hasShadowt != null) {
                                hasShadowTable = String.valueOf(hasShadowt);
                            }

                            String flows = "";
                            Object flowsObj = row.getValue(ENTITIES_FLOWS);
                            if (flowsObj != null) {
                                flows = String.valueOf(flowsObj);
                            }
                            String traversalGraphStr = "";
                            Object traversalGraphObj = row.getValue(ENTITIES_TRAVERSAL);
                            if (traversalGraphObj != null) {
                                traversalGraphStr = String.valueOf(traversalGraphObj);
                            }


/*
* public CMEntity(String name, String shortName, String label, String type, String geometryType,
             String w9IdPropertyName, String labelPropertyName, String abbreviation,
             boolean isLocked, boolean hasShadowTable, String datasetName,String conceptModelName,
             JSONArray propertyGroups, JSONArray propertiesArray, JSONObject domainValues,
             JSONObject visualizations, JSONObject infoTemplate, JSONObject layerStyles, JSONObject miscProperties,
             JSONObject categories, JSONObject depPropsGraph){*/


                            CMEntity cmEntity = new CMEntity(name, label, type, geomType, w9IdName, labelPropertyName, categoryName, abbr, isLocked, hasShadowTable, rendererName, idRule, simpleStyle, uniqueStyle, properties, propertyGroups, domainValues, dependantPropertiesGraphStr, categories, flows, traversalGraphStr, dataGeoPackage);

                            JSONObject domainsJson = getDomainRows(cmEntity.getProperties(), context);
                            cmEntity.setDomainValues(domainsJson.toString());
                            CMEntitiesList.add(cmEntity);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            ReveloLogger.debug("CMUtils", "getCMEntitiesList", "Exception adding CM entity to the list. exception - " + ex.getCause());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CMEntitiesList = null;
                    ReveloLogger.debug("CMUtils", "getCMEntitiesList", "Exception creating CM entities list. exception - " + e.getCause());
                    /*return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                            "Exception creating CM Entities list - " + e.getCause(), null);*/
                    return SystemUtils.logAndReturnErrorMessage("Exception creating CM Entities list ", e);
                }

            } catch (Exception e) {
                e.printStackTrace();
                CMEntitiesList = null;
                ReveloLogger.debug("CMUtils", "getCMEntitiesList", "error creating CM entities list. exception - " + e.getCause());
               /* return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                        "Exception creating CM Entities list - "+e.getCause(),null);*/
                return SystemUtils.logAndReturnErrorMessage("Exception creating CM Entities list ", e);
            }finally {
                if(entitiesAttributeDao!=null){
                    entitiesAttributeDao=null;
                }
            }
        }

        return SystemUtils.logAndReturnObject("success", "", CMEntitiesList);
        //return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_SUCCESS,"",CMEntitiesList);
    }

   /* public static JSONObject getTraversalJsonFromDb(Context context, CMEntity cmEntity){
        JSONObject graphJson = null;
        String taskName = "getTraversalJsonFromDb";
try{

    Map<String,String> conditionMap = new HashMap<>();
    conditionMap.put("name",cmEntity.getName());
    GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
    JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), getDatasetInfoMetaFP(), null, conditionMap, "", true, 1, false, false);

}catch (Exception e){e.printStackTrace();}

        return graphJson;
    }*/

    private static JSONObject getDatasetInfoMetaFP() {
        JSONObject datasetInfo = new JSONObject();
        try {

                datasetInfo.put("datasetName", "entities");
                datasetInfo.put("datasetType", "table");
                datasetInfo.put("geometryType", "");

            datasetInfo.put("idPropertyName", "name");
            datasetInfo.put("w9IdPropertyName", "name");
        } catch (JSONException e) {

            e.printStackTrace();
            return null;
        }
        return datasetInfo;
    }

    /* public static List<CMEntity> getCMEntitiesList(Context context) throws Exception {
         ReveloLogger.debug("CMUtils", "getCMEntitiesList", "Fetching cm entities list from memory..");
         // JSONObject resultJson = new JSONObject();
         if (CMEntitiesList == null) {
             try {
                 ReveloLogger.debug("CMUtils", "getCMEntitiesList", "creating CM entities list. This method executes only once after login. These variables are washed out on logout.");
                 CMEntitiesList = new ArrayList<>();
                 AttributesDao entitiesAttributeDao = getEntititesDao(context);
                 if (entitiesAttributeDao == null) {
                     CMEntitiesList = null;
                     ReveloLogger.debug("CMUtils", "getCMEntitiesList", "error creating CM entities list. DAO for table entities not found.. aborting..");
                     SystemUtils.logAndReturnMessage("failure", "Error creating CM Entities list, could not get DAO");
                         *//*return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                                "Error creating CM Entities list, could not get DAO",null);*//*
                }

                GeoPackage dataGeoPackage = GeoPackageManagerAgent.getDataGeoPackage(context, DbRelatedConstants.getPropertiesJsonForDataGpkg(context));
                try (AttributesCursor attributesCursor = entitiesAttributeDao.queryForAll()) {

                    while (attributesCursor.moveToNext()) {

                        try {

                            AttributesRow row = attributesCursor.getRow();

                            String name = "";
                            Object na = row.getValue(ENTITIES_NAME);
                            if (na != null) {
                                name = String.valueOf(na);
                            }

                            String label = "";
                            Object la = row.getValue(ENTITIES_LABEL);
                            if (la != null) {
                                label = String.valueOf(la);
                            }

                            String abbr = "";
                            Object ab = row.getValue(ENTITIES_ABBR);
                            if (ab != null) {
                                abbr = String.valueOf(ab);
                            }

                            String type = "";
                            Object ty = row.getValue(ENTITIES_TYPE);
                            if (ty != null) {
                                type = String.valueOf(ty);
                            }

                            String geomType = "";
                            Object gType = row.getValue(ENTITIES_GEOMETRY_TYPE);
                            if (gType != null) {
                                geomType = String.valueOf(gType);
                            }

                            String w9IdName = "";
                            Object w9Name = row.getValue(ENTITIES_W9_ID_PROPERTY);
                            if (w9Name != null) {
                                w9IdName = String.valueOf(w9Name);
                            }

                            String w9IdLabel = "";
                            Object w9Label = row.getValue(ENTITIES_LABEL_PROPERTY);
                            if (w9Label != null) {
                                w9IdLabel = String.valueOf(w9Label);
                            }

                            String categoryName = "";
                            Object catName = row.getValue(ENTITIES_CATEGORY_PROPERTY_NAME);
                            if (catName != null) {
                                categoryName = String.valueOf(catName);
                            }

                            String isLocked = "";
                            Object loc = row.getValue(ENTITIES_IS_LOCKED);
                            if (loc != null) {
                                isLocked = String.valueOf(loc);
                            }

                            String rendererName = "";
                            Object renName = row.getValue(ENTITIES_SELECTED_RENDERER_NAME);
                            if (renName != null) {
                                rendererName = String.valueOf(renName);
                            }

                            String idRule = "";
                            Object rule = row.getValue(ENTITIES_ID_GEN_RULES);
                            if (rule != null) {
                                idRule = String.valueOf(rule);
                            }

                            String simpleStyle = "";
                            Object sStyle = row.getValue(ENTITIES_SIMPLE_STYLE);
                            if (sStyle != null) {
                                simpleStyle = String.valueOf(sStyle);
                            }

                            String uniqueStyle = "";
                            Object uStyle = row.getValue(ENTITIES_UNIQUE_VALUE_STYLE);
                            if (uStyle != null) {
                                uniqueStyle = String.valueOf(uStyle);
                            }

                            String properties = "";
                            Object pro = row.getValue(ENTITIES_PROPERTIES);
                            if (pro != null) {
                                properties = String.valueOf(pro);
                            }

                            String propertyGroups = "";
                            Object proGroup = row.getValue(ENTITIES_PROPERTY_GROUPS);
                            if (proGroup != null) {
                                propertyGroups = String.valueOf(proGroup);
                            }

                            String domainValues = "";
                            Object doValue = row.getValue(ENTITIES_DOMAIN_VALUES);
                            if (doValue != null) {
                                domainValues = String.valueOf(doValue);
                            }

                            String categories = "";
                            Object cat = row.getValue(ENTITIES_CATEGORIES);
                            if (cat != null) {
                                categories = String.valueOf(cat);
                            }

                            String hasShadowTable = "";
                            Object hasShadowt = row.getValue(ENTITIES_HASSHADOWTABLE);
                            if (hasShadowt != null) {
                                hasShadowTable = String.valueOf(hasShadowt);
                            }


*//*
* public CMEntity(String name, String shortName, String label, String type, String geometryType,
             String w9IdPropertyName, String labelPropertyName, String abbreviation,
             boolean isLocked, boolean hasShadowTable, String datasetName,String conceptModelName,
             JSONArray propertyGroups, JSONArray propertiesArray, JSONObject domainValues,
             JSONObject visualizations, JSONObject infoTemplate, JSONObject layerStyles, JSONObject miscProperties,
             JSONObject categories, JSONObject depPropsGraph){*//*


                            CMEntity cmEntity = new CMEntity(name, label, type, geomType, w9IdName, w9IdLabel, categoryName,
                                    abbr, isLocked, hasShadowTable, rendererName, idRule, simpleStyle, uniqueStyle,
                                    properties, propertyGroups, domainValues, categories, dataGeoPackage);
                            CMEntitiesList.add(cmEntity);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            ReveloLogger.debug("CMUtils", "getCMEntitiesList", "Exception adding CM entity to the list. exception - " + ex.getCause());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CMEntitiesList = null;
                    ReveloLogger.debug("CMUtils", "getCMEntitiesList", "Exception creating CM entities list. exception - " + e.getCause());
                    *//*return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                            "Exception creating CM Entities list - " + e.getCause(), null);*//*
                    SystemUtils.logAndReturnErrorMessage("Exception creating CM Entities list ", e);
                }

            } catch (Exception e) {
                e.printStackTrace();
                CMEntitiesList = null;
                ReveloLogger.debug("CMUtils", "getCMEntitiesList", "error creating CM entities list. exception - " + e.getCause());
               *//* return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                        "Exception creating CM Entities list - "+e.getCause(),null);*//*
                SystemUtils.logAndReturnErrorMessage("Exception creating CM Entities list ", e);
            }
        }

        return CMEntitiesList;
        //return SystemUtils.logAndReturnObject("success","",CMEntitiesList);
        //return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_SUCCESS,"",CMEntitiesList);
    }
*/
    public static JSONObject getCMRelationsList(Context context) {
        AttributesDao relationShipDao=null;
        if (CMRelationsList == null) {
            ReveloLogger.debug("CMUtils", "getCMRelationsList", "Creating CM relations list");

            try {
                CMRelationsList = new ArrayList<>();

                relationShipDao = getRelationsDao(context);
                if (relationShipDao == null) {
                    CMRelationsList = null;
                    ReveloLogger.debug("CMUtils", "getCMRelationsList", "error creating CM relations list. DAO for table relations not found.. aborting..");
                    return SystemUtils.logAndReturnMessage("failure", "Error creating CM Relations list, could not get DAO");
                     /*return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                            "Error creating CM Relations list, could not get DAO", null);*/
                }
                try (AttributesCursor attributesCursor = relationShipDao.queryForAll()) {

                    while (attributesCursor.moveToNext()) {

                        try {

                            AttributesRow row = attributesCursor.getRow();

                            String name = "";
                            Object na = row.getValue(RELATIONSHIPS_NAME);
                            if (na != null) {
                                name = String.valueOf(na);
                            }

                            String fromCol = "";
                            Object frCol = row.getValue(RELATIONSHIPS_FROM_COL);
                            if (frCol != null) {
                                fromCol = String.valueOf(frCol);
                            }

                            String toCol = "";
                            Object tCol = row.getValue(RELATIONSHIPS_TO_COL);
                            if (tCol != null) {
                                toCol = String.valueOf(tCol);
                            }

                            String fromIdCol = "";
                            Object frIdCol = row.getValue(RELATIONSHIPS_FROM_ID_COL);
                            if (frIdCol != null) {
                                fromIdCol = String.valueOf(frIdCol);
                            }

                            String toIdCol = "";
                            Object tIdCol = row.getValue(RELATIONSHIPS_TO_ID_COL);
                            if (tIdCol != null) {
                                toIdCol = String.valueOf(tIdCol);
                            }

                            CMRelation cmRelation = new CMRelation(name, fromCol, toCol, fromIdCol, toIdCol);
                            CMRelationsList.add(cmRelation);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            ReveloLogger.debug("CMUtils", "getCMRelationsList", "Exception adding a CM relation to the list- " + ex.getCause());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    CMRelationsList = null;
                    ReveloLogger.debug("CMUtils", "getCMRelationsList", "Exception creating CM relations list- " + e.getCause());
                   /* return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                            "Exception creating CM relations list- " + e.getCause(), null);*/
                    return SystemUtils.logAndReturnErrorMessage("Exception creating CM relations list ", e);
                }

            } catch (Exception e) {
                e.printStackTrace();
                CMRelationsList = null;
                ReveloLogger.debug("CMUtils", "getCMRelationsList", "Exception creating CM relations list- " + e.getCause());
                /*return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                        "Exception creating CM relations list- "+e.getCause(),null);*/
                return SystemUtils.logAndReturnErrorMessage("Exception creating CM Relations list ", e);
            } finally {
                if (relationShipDao != null) {
                    relationShipDao = null;
                }
            }
        }
        return SystemUtils.logAndReturnObject("success", "", CMRelationsList);
        //return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_SUCCESS,"",CMRelationsList);
    }

    /*-----------------------------------------------------------------------*/
    private static AttributesDao getEntitiesDao(Context context) {
        AttributesDao entitiesDao=null;
        try {
            ReveloLogger.debug("CMUtils", "getEntititesDao", "Fetching data access object from metageopackage..");
           // if (entititesDao == null) {
                GeoPackage metaGeoPackage = GeoPackageManagerAgent.getMetaGeoPackage(context, DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context));
                if (metaGeoPackage != null) {
                    ReveloLogger.debug("CMUtils", "getEntititesDao", " Geopackage initalized..getting attributes type data access object for non spatial table entities");
                    entitiesDao = metaGeoPackage.getAttributesDao(ENTITIES_TABLE_NAME);
                }
                else {
                    ReveloLogger.error("CMUtils", "getEntititesDao", " Geopackage not initalized..failure getting attributes type data access object for non spatial table entities");
                }
            //}

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.debug("CMUtils", "getEntititesDao", "Exception fetching data access object from meta geopackage.." + e.getMessage());
        }
        return entitiesDao;
    }

    private static JSONObject getDomainRows(List<Attribute> properties, Context context) {

        Map<String, String> domainLabelashmap = new HashMap<>();
        JSONObject domainValuesJson = new JSONObject();

        try {

            JSONArray whereClauseArray = new JSONArray();
            for (Attribute attribute : properties) {
                if (attribute != null && attribute.getDomainName() != null) {
                    JSONObject conditionJobj = new JSONObject();
                    conditionJobj.put("conditionType", "attribute");
                    conditionJobj.put("columnName", "name");
                    conditionJobj.put("valueDataType", "text");
                    conditionJobj.put("value", attribute.getDomainName());
                    conditionJobj.put("operator", "=");
                    whereClauseArray.put(conditionJobj);
                }
            }


            GeoPackageRWAgent gpkgRWAgent = new GeoPackageRWAgent(DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context), new ReveloLogger(), context);
            JSONObject datasetInfo = getDomainsDatasetInfo();
            JSONObject respJObj = gpkgRWAgent.getDatasetContent(context, DbRelatedConstants.getDataSourceInfoForMetdataGpkg(context), datasetInfo, null, whereClauseArray, "OR", true, - 1, false, false);

            if (respJObj.has("status") && respJObj.getString("status").equalsIgnoreCase("success")) {
                if (respJObj.has("features")) {
                    JSONObject responseFeatures = respJObj.getJSONObject("features");
                    if (responseFeatures.has("features")) {
                        JSONArray featuresJArray = responseFeatures.getJSONArray("features");
                        if (featuresJArray.length() > 0) {
                            for (int i = 0; i < featuresJArray.length(); i++) {
                                JSONObject propertyJson = featuresJArray.getJSONObject(i).getJSONObject("properties");

                                domainLabelashmap.put(propertyJson.getString("name"), propertyJson.getString("typeInfo"));
                            }
                        }
                    }
                }
            }

            if (! domainLabelashmap.isEmpty()) {
                for (Attribute attribute : properties) {
                    String attrName = attribute.getName();
                    String attrDomainName = attribute.getDomainName();
                    if (domainLabelashmap.containsKey(attrDomainName)) {
                        try {
                            JSONObject domain = new JSONObject(StringEscapeUtils.unescapeJson(domainLabelashmap.get(attrDomainName)));
                            domainValuesJson.put(attrName, domain);
                            attribute.setDomainObject(domain);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return domainValuesJson;
    }

    private static AttributesDao getRelationsDao(Context context) {
        AttributesDao relationsDao=null;
        try {
            ReveloLogger.debug("CMUtils", "getRelationsDao", "getting RelationsDao");
           // if (relationsDao == null) {
                GeoPackage metaGeoPackage = GeoPackageManagerAgent.getMetaGeoPackage(context, DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context));
                if (metaGeoPackage != null) {
                    ReveloLogger.debug("CMUtils", "getRelationsDao", " Geopackage initalized..getting attributes type data access object for non spatial table relations");
                    relationsDao = metaGeoPackage.getAttributesDao(RELATIONSHIPS_TABLE_NAME);
                }
                else {
                    ReveloLogger.error("CMUtils", "getRelationsDao", " Geopackage not initalized..failure getting attributes type data access object for non spatial table relations");
                }
           // }

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.debug("CMUtils", "getRelationsDao", "Exception fetching data access object from meta geopackage.." + e.getMessage());
        }
        return relationsDao;
    }

    private static JSONObject getDomainsDatasetInfo() {
        JSONObject datasetInfo = new JSONObject();
        try {
            //ReveloLogger.info(className, "getEditMetadataDatasetInfo", "getting getEditMetadataDatasetInfo for " + entityName);
            datasetInfo.put("datasetName", "domains");
            datasetInfo.put("datasetType", "table");
            datasetInfo.put("geometryType", "");

            datasetInfo.put("idPropertyName", "name");
            datasetInfo.put("w9IdPropertyName", "name");
        } catch (JSONException e) {
            // ReveloLogger.error(className, "getDatasetInfo", "error initializing getEditMetadataDatasetInfo json for " + entityName + ". Exception - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return datasetInfo;
    }

    public static Interaction getNextInteraction(Context context, String entityName, String currentFlowName, String currentInteractionName) {
        try {
            JSONObject graphResult = CMUtils.getCMGraph(context);
            CMGraph cmGraph = null;
            if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                cmGraph = (CMGraph) graphResult.get("result");
            }
            else {
                ReveloLogger.error("HomePresenter", "createEntityListData", "Could not create enitities list - could not fetch graph from memory. Reason - " + graphResult.getString("message"));
            }

            if (cmGraph == null) {
                return null;
            }
            Map<String, Object> condtionmap = new HashMap<>();
            condtionmap.put("name", entityName);
            List<CMEntity> cmEntityList = cmGraph.getVertices(condtionmap);
            if (cmEntityList == null || cmEntityList.size() != 1) {
                return null;
            }
            CMEntity selectedEntity = cmEntityList.get(0);

            if (! selectedEntity.hasFlows()) {
                return null;
            }
            FlowGraph flowGraph = selectedEntity.getFlowGraph();
            if (flowGraph == null) {
                return null;
            }
            JSONObject currentInteractionJson = flowGraph.getVertex(Interaction.FieldNames.interactionName, currentInteractionName);

            if (currentInteractionJson == null || ! currentInteractionJson.has("status") || currentInteractionJson.getString("status").equalsIgnoreCase("failure")) {
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no interaction node found by name " + currentInteractionName + " in flowgraph for entity " + entityName);
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "flow applicable, but not returning any applicable interaction");
                return null;
            }

            Interaction currentInteraction = (Interaction) currentInteractionJson.get("result");
            List<Interaction> childInteractions = flowGraph.getChildren(currentInteraction);
            if (childInteractions == null || childInteractions.size() != 1) {
                //check for next phase. if exists, assign its first interaction
                Phase previousPhase = getPhase(false);
                if (previousPhase == null) {
                    return currentInteraction;
                }

                if (previousPhase.getEntityFlowNameMap() != null && previousPhase.getEntityFlowNameMap().containsKey(entityName)) {
                    String flowName = previousPhase.getEntityFlowNameMap().get(entityName);
                    if (flowName == null || flowName.isEmpty())
                        return currentInteraction;
                    FlowGraph newFlowGraph = selectedEntity.getFlowGraph(flowName);
                    if (newFlowGraph == null)
                        return currentInteraction;
                    Set<Interaction> newLeafInteractionsSet = newFlowGraph.getRootVertices();
                    if (newLeafInteractionsSet.isEmpty()) {
                        return currentInteraction;
                    }
                    Iterator<Interaction> interactionIterator = newLeafInteractionsSet.iterator();
                    while (interactionIterator.hasNext()) {
                        Interaction interaction = interactionIterator.next();
                        return interaction;
                    }

                }
            }
            return childInteractions.get(0);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Phase getPhase(boolean getPrevious) {
        Survey survey = SurveyPreferenceUtility.getSurvey(UserInfoPreferenceUtility.getSurveyName());
        HashMap<String, Phase> phaseHashMap = survey.getPhasesNameMapFromJson();
        String currentPhaseName = UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());

        Phase currentPhase = phaseHashMap.get(currentPhaseName);
        int currentPhaseIndex = currentPhase.getIndex();

        for (String phaseName : phaseHashMap.keySet()) {
            Phase p = phaseHashMap.get(phaseName);
            if (! phaseName.equalsIgnoreCase(currentPhaseName)) {
                if (getPrevious) {
                    assert p != null;
                    if (p.getIndex() < currentPhaseIndex) {
                        return p;
                    }
                }
                else {
                    assert p != null;
                    if (p.getIndex() > currentPhaseIndex) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    public static Interaction getPreviousInteraction(Context context, String entityName, String currentFlowName, String currentInteractionName) {
        try {
            JSONObject graphResult = CMUtils.getCMGraph(context);
            CMGraph cmGraph = null;
            if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                cmGraph = (CMGraph) graphResult.get("result");
            }
            else {
                ReveloLogger.error("HomePresenter", "createEntityListData", "Could not create enitities list - could not fetch graph from memory. Reason - " + graphResult.getString("message"));
            }

            if (cmGraph == null) {
                return null;
            }
            Map<String, Object> condtionmap = new HashMap<>();
            condtionmap.put("name", entityName);
            List<CMEntity> cmEntityList = cmGraph.getVertices(condtionmap);
            if (cmEntityList == null || cmEntityList.size() != 1) {
                return null;
            }
            CMEntity selectedEntity = cmEntityList.get(0);

            if (! selectedEntity.hasFlows()) {
                return null;
            }
            FlowGraph flowGraph = selectedEntity.getFlowGraph();
            if (flowGraph == null) {
                return null;
            }
            JSONObject currentInteractionJson = flowGraph.getVertex(Interaction.FieldNames.interactionName, currentInteractionName);

            if (currentInteractionJson == null || ! currentInteractionJson.has("status") || currentInteractionJson.getString("status").equalsIgnoreCase("failure")) {
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no interaction node found by name " + currentInteractionName + " in flowgraph for entity " + entityName);
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "flow applicable, but not returning any applicable interaction");
                return null;
            }

            Interaction currentInteraction = (Interaction) currentInteractionJson.get("result");
            Interaction parentInteraction = flowGraph.getParent(currentInteraction);
            if (parentInteraction == null) {
                //check for previous phase. if exists, assign its last interaction
                Survey survey = SurveyPreferenceUtility.getSurvey(UserInfoPreferenceUtility.getSurveyName());
                if (! survey.hasPhases()) {
                    return currentInteraction;
                }
                else {
                    Phase previousPhase = getPhase(true);
                    if (previousPhase == null) {
                        return currentInteraction;
                    }
                    if (previousPhase.getEntityFlowNameMap() != null && previousPhase.getEntityFlowNameMap().containsKey(entityName)) {
                        String flowName = previousPhase.getEntityFlowNameMap().get(entityName);
                        if (flowName == null || flowName.isEmpty())
                            return currentInteraction;
                        FlowGraph newFlowGraph = selectedEntity.getFlowGraph(flowName);
                        if (newFlowGraph == null)
                            return currentInteraction;
                        Set<Interaction> newLeafInteractionsSet = newFlowGraph.getLeafVertices();
                        if (newLeafInteractionsSet.isEmpty()) {
                            return currentInteraction;
                        }
                        Iterator<Interaction> interactionIterator = newLeafInteractionsSet.iterator();
                        while (interactionIterator.hasNext()) {
                            Interaction interaction = interactionIterator.next();
                            parentInteraction = interaction;
                        }

                    }
                }
            }
            return parentInteraction;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /*public static JSONObject checkOperationPermission(Activity activity, CMEntity currentEntity, Feature feature, String operationName, boolean isShadowFeature) {
        JSONObject resultJobj = new JSONObject();
        try {
            resultJobj.put("status", "failure");
            resultJobj.put("message", "You cannot perform this operation. Please contact admin for more details.");


            boolean isFlowsApplicable = false;
            boolean isGeomEditEnableInProfile = true;
            boolean isGeomEditEnableInFlows = true;
            boolean isEditorEnableInProfile = true;
            boolean isPropertiesEditEnabledInFlows = true;
            boolean isPropertiesEditEnabledInProfile = true;
            UserProfileModel userProfileModel = null;


            userProfileModel = UserInfoPreferenceUtility.getUserProfile(activity);
            if (userProfileModel == null) {
                resultJobj.put("message", "You cannot perform this operation. No details found for user profile. Please contact admin.");
                return resultJobj;
            }

            isEditorEnableInProfile = userProfileModel.isEditorEnable();

            if (operationName.equalsIgnoreCase("add") || operationName.equalsIgnoreCase("create")) {
                isPropertiesEditEnabledInProfile = userProfileModel.isAttributeEnable();
                isGeomEditEnableInProfile = userProfileModel.isGeoAdd();
            }
            else if (operationName.equalsIgnoreCase("delete")) {
                isPropertiesEditEnabledInProfile = true;
                isGeomEditEnableInProfile = userProfileModel.isGeoDelete();
            }else if(operationName.equalsIgnoreCase("edit") || operationName.equalsIgnoreCase("update")){
                isPropertiesEditEnabledInProfile = userProfileModel.isAttributeEnable();
            isGeomEditEnableInProfile = userProfileModel.isGeoUpdate();
            }else{
                isPropertiesEditEnabledInProfile = userProfileModel.isAttributeEnable();
                isGeomEditEnableInProfile = userProfileModel.isGeoUpdate();
            }

            if (currentEntity == null) {
                resultJobj.put("message", "You cannot perform this operation. No details found for layer. Please contact admin.");
                return resultJobj;
            }


            String surveyName = UserInfoPreferenceUtility.getSurveyName();
            Survey survey = SurveyPreferenceUtility.getSurvey(surveyName);
            String phaseName = UserInfoPreferenceUtility.getSurveyPhaseName(surveyName);
            if (! phaseName.isEmpty() && feature!=null) {
                HashMap<String, Phase> phaseHashMap = survey.getPhasesNameMapFromJson();
                if (phaseHashMap != null && ! phaseHashMap.isEmpty() && phaseHashMap.containsKey(phaseName)) {
                    Phase phase = phaseHashMap.get(phaseName);
                    HashMap<String,String> entityWhereclauseMap = phase.getEntityWhereClauseMap();

                    if(entityWhereclauseMap.containsKey(currentEntity.getName())) {
                        String whereClauseString = entityWhereclauseMap.get(currentEntity.getName());
                        if (whereClauseString != null && ! whereClauseString.isEmpty()) {
                            JSONObject whereClauseJson = new JSONObject(whereClauseString);
                            String andOR = "OR";
                            if (whereClauseJson.has("logicalOperator")) {
                                andOR = whereClauseJson.getString("logicalOperator");
                            }
                            if (whereClauseJson.has("clauses")) {
                                JSONArray clausesJArray = whereClauseJson.getJSONArray("clauses");

                                if (clausesJArray.length() > 0) {
                                    boolean conditionSatisfied;
                                    if (andOR.equalsIgnoreCase("AND")) {
                                        conditionSatisfied = true;
                                        for (int i = 0; i < clausesJArray.length(); i++) {
                                            JSONObject clauseJson = clausesJArray.getJSONObject(i);

                                            if (feature.getAttributes().containsKey(clauseJson.getString("fieldName"))) {
                                                Object actualValue = feature.getAttributes().get(clauseJson.getString("fieldName"));
                                                if (actualValue != null) {
                                                    String operStr = clauseJson.getString("operator");
                                                    if (operStr.equalsIgnoreCase("=")) {
                                                        if (actualValue != clauseJson.get("value")) {
                                                            conditionSatisfied = false;
                                                            break;
                                                        }
                                                    }
                                                    else if (operStr.equalsIgnoreCase("!=")) {
                                                        if (actualValue == clauseJson.get("value")) {
                                                            conditionSatisfied = false;
                                                            break;
                                                        }
                                                    }
                                                }
                                                else {
                                                    conditionSatisfied = false;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    else {
                                        conditionSatisfied = false;
                                        for (int i = 0; i < clausesJArray.length(); i++) {
                                            JSONObject clauseJson = clausesJArray.getJSONObject(i);

                                            if (feature.getAttributes().containsKey(clauseJson.getString("fieldName"))) {
                                                Object actualValue = feature.getAttributes().get(clauseJson.getString("fieldName"));
                                                if (actualValue != null) {
                                                    String operStr = clauseJson.getString("operator");
                                                    if (operStr.equalsIgnoreCase("=")) {
                                                        if (actualValue.equals(clauseJson.get("value"))) {
                                                            conditionSatisfied = true;
                                                            break;
                                                        }
                                                    }
                                                    else if (operStr.equalsIgnoreCase("!=")) {
                                                        if (! actualValue.equals(clauseJson.get("value"))) {
                                                            conditionSatisfied = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                                else {
                                                    conditionSatisfied = false;
                                                }
                                            }
                                        }
                                    }

                                    if (! conditionSatisfied) {
                                        resultJobj.put("message", "You cannot perform operations on " + currentEntity.getLabel() + "-" + feature.getFeatureLabel() + " , in " + phase.getLabel() + " mode.");
                                        return resultJobj;
                                    }
                                }
                            }
                        }
                    }
                }
            }


            JSONObject permissionJson = checkOperationPermissionFromFlowInteraction(activity, currentEntity, feature, operationName, isShadowFeature);
            String currentInteractionRoleName = "some other user";
            try {
                isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
                if (isFlowsApplicable) {
                    isPropertiesEditEnabledInFlows = permissionJson.getBoolean("arePropertiesEditable");
                    isGeomEditEnableInFlows = permissionJson.getBoolean("isGeometryEditable");
                    currentInteractionRoleName = permissionJson.getString(CMUtils.OperationPermissionVariables.currentInteractionRole);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isFlowsApplicable) {
                if (isPropertiesEditEnabledInProfile && isEditorEnableInProfile && isGeomEditEnableInProfile) {
                    if (isPropertiesEditEnabledInFlows || isGeomEditEnableInFlows) {
                        //return true;
                        resultJobj.put("status", "success");
                        resultJobj.put("message", permissionJson);

                    }
                    else {
                        String message = "You don't have permission to " + operationName + " feature. Please contact admin.";
                        if (operationName.equalsIgnoreCase("add") || operationName.equalsIgnoreCase("create")) {
                            message = "You don't have permission to add " + currentEntity.getLabel() + "\nThe user with role as a " + UserInfoPreferenceUtility.getRole() + " does not have properties or geometry adding privileges." + "\nPlease contact admin.";
                        }
                        else if (operationName.equalsIgnoreCase("delete")) {
                            message = "You cannot delete this " + currentEntity.getLabel() + " because it is ";
                            if (! currentInteractionRoleName.equalsIgnoreCase("some other user")) {
                                message += "still ";
                            }
                            message += "being updated by " + currentInteractionRoleName + ".\n" + "Try again after refreshing data.";
                        }
                        else {
                            message = "You cannot modify this " + currentEntity.getLabel() + " because it is ";
                            if (! currentInteractionRoleName.equalsIgnoreCase("some other user")) {
                                message += "still ";
                            }
                            message += "being updated by " + currentInteractionRoleName + ".\n" + "Try again after refreshing data.";
                        }

                        //InfoBottomSheet infoBottomSheet = InfoBottomSheet.geInstance(this, "Ok", "", "", message, 0, 0, "");
                        //infoBottomSheet.setCancelable(false);
                        //infoBottomSheet.show(getSupportFragmentManager(), BottomSheetTagConstants.okDialogInfo);
                        resultJobj.put("status", "failure");
                        resultJobj.put("message", message);
                    }
                }
                else {
                    String message = "You don't have permission to " + operationName + " feature.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    if(!isPropertiesEditEnabledInProfile){
                        message = "You don't have permission to make changes to values while you perform " + operationName + " operation. Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }else if(!isEditorEnableInProfile){
                        message = "Editor disabled.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }else if(!isGeomEditEnableInProfile) {
                        message = "You don't have permission to " + operationName + " feature.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    } resultJobj.put("status", "failure");
                    resultJobj.put("message", message);
                }
            }
            else {
                if (isPropertiesEditEnabledInProfile && isEditorEnableInProfile && isGeomEditEnableInProfile) {
                    //return true;
                    resultJobj.put("status", "success");
                    resultJobj.put("message", permissionJson);
                }
                else {
                    String message = "You don't have permission to " + operationName + " feature.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    if(!isPropertiesEditEnabledInProfile){
                        message = "You don't have permission to make changes to values while you perform " + operationName + " operation. Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }else if(!isEditorEnableInProfile){
                        message = "Editor disabled.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }else if(!isGeomEditEnableInProfile) {
                        message = "You don't have permission to " + operationName + " feature.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }

                    resultJobj.put("status", "failure");
                    resultJobj.put("message", message);
                }
            }

            return resultJobj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJobj;
    }
*/
    public static JSONObject checkOperationPermission(Activity activity, CMEntity currentEntity, Feature feature, String operationName, boolean isShadowFeature) {
        JSONObject resultJobj = new JSONObject();
      String taskname = "checkOperationPermission";
        try {

            ReveloLogger.info(className,taskname,"checking permission for operation "+operationName);

            resultJobj.put("status", "failure");
            resultJobj.put("message", "You cannot perform this operation. Please contact admin for more details.");


            boolean isFlowsApplicable = false;
            boolean isGeomEditEnableInProfile = true;
            boolean isGeomEditEnableInFlows = true;
            boolean isGeomEditEnableInPhase = true;
            boolean isEditorEnableInProfile = true;
            boolean isPropertiesEditEnabledInFlows = true;
            boolean isPropertiesEditEnabledInProfile = true;
            boolean isPropertiesEditEnabledInPhase = true;
            UserProfileModel userProfileModel = null;


            userProfileModel = UserInfoPreferenceUtility.getUserProfile(activity);
            if (userProfileModel == null) {
                ReveloLogger.error(className,taskname,"userprofile null. You cannot perform this operation. No details found for user profile. Please contact admin.");
                resultJobj.put("message", "You cannot perform this operation. No details found for user profile. Please contact admin.");
                return resultJobj;
            }

            ReveloLogger.info(className,taskname,"getting permissions acc to profile/role");
            isEditorEnableInProfile = userProfileModel.isEditorEnable();

            if (operationName.equalsIgnoreCase("add") || operationName.equalsIgnoreCase("create")) {
                isPropertiesEditEnabledInProfile = userProfileModel.isAttributeEnable();
                isGeomEditEnableInProfile = userProfileModel.isGeoAdd();
            }
            else if (operationName.equalsIgnoreCase("delete")) {
                isPropertiesEditEnabledInProfile = true;
                isGeomEditEnableInProfile = userProfileModel.isGeoDelete();
            }else if(operationName.equalsIgnoreCase("edit") || operationName.equalsIgnoreCase("update")){
                isPropertiesEditEnabledInProfile = userProfileModel.isAttributeEnable();
                isGeomEditEnableInProfile = userProfileModel.isGeoUpdate();
            }else{
                isPropertiesEditEnabledInProfile = userProfileModel.isAttributeEnable();
                isGeomEditEnableInProfile = userProfileModel.isGeoUpdate();
            }


            ReveloLogger.info(className,taskname, "isEditorEnableInProfile "+isEditorEnableInProfile+" "+
                    "isPropertiesEditEnabledInProfile "+isPropertiesEditEnabledInProfile+" "+
                    "isGeomEditEnableInProfile "+isGeomEditEnableInProfile+" ");


            if (currentEntity == null) {
                ReveloLogger.error(className,taskname,"current entity null. "+"You cannot perform this operation. No details found for layer. Please contact admin.");
                resultJobj.put("message", "You cannot perform this operation. No details found for layer. Please contact admin.");
                return resultJobj;
            }


            String surveyName = UserInfoPreferenceUtility.getSurveyName();
            Survey survey = SurveyPreferenceUtility.getSurvey(surveyName);
            String phaseName = UserInfoPreferenceUtility.getSurveyPhaseName(surveyName);
            ReveloLogger.info(className,taskname, "checking permission by phase named "+phaseName);
            if (! phaseName.isEmpty() ) {
                HashMap<String, Phase> phaseHashMap = survey.getPhasesNameMapFromJson();
                if (phaseHashMap != null && ! phaseHashMap.isEmpty() && phaseHashMap.containsKey(phaseName)) {
                    Phase phase = phaseHashMap.get(phaseName);
                    if(phase!=null) {
                       if( phase.isOperationAllowed(operationName)) {
                           ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "phase "+phaseName+" allows operation "+operationName);
                           isPropertiesEditEnabledInPhase=true;
                           isGeomEditEnableInPhase=true;
                           if(operationName.equalsIgnoreCase("update")||operationName.equalsIgnoreCase("edit")){
                               isGeomEditEnableInPhase = phase.isOperationAllowed("updategeometry");
                               isPropertiesEditEnabledInPhase = phase.isOperationAllowed("updateattributes");
                               ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "phase "+phaseName+" allows updating geometry? "+isGeomEditEnableInPhase);
                               ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "phase "+phaseName+" allows updating attributes? "+isPropertiesEditEnabledInPhase);

                           }
                       }else if(operationName.equalsIgnoreCase("update")||operationName.equalsIgnoreCase("edit")){
                                isGeomEditEnableInPhase = phase.isOperationAllowed("updategeometry");
                               isPropertiesEditEnabledInPhase = phase.isOperationAllowed("updateattributes");
                           ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "phase "+phaseName+" allows updating geometry? "+isGeomEditEnableInPhase);
                           ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "phase "+phaseName+" allows updating attributes? "+isPropertiesEditEnabledInPhase);

                       }
            else {
                           ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "phase "+phaseName+" does not allow operation "+operationName);
                           isPropertiesEditEnabledInPhase=false;
                           isGeomEditEnableInPhase=false;

                       }

                       if( feature!=null){
                           ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "phase and feature not null.. evaluating whereclause");
                            HashMap<String, String> entityWhereclauseMap = phase.getEntityWhereClauseMap();

                            if (entityWhereclauseMap.containsKey(currentEntity.getName())) {
                                String whereClauseString = entityWhereclauseMap.get(currentEntity.getName());
                                if (whereClauseString != null && ! whereClauseString.isEmpty()) {
                                    JSONObject whereClauseJson = new JSONObject(whereClauseString);
                                    String andOR = "OR";
                                    if (whereClauseJson.has("logicalOperator")) {
                                        andOR = whereClauseJson.getString("logicalOperator");
                                    }
                                    if (whereClauseJson.has("clauses")) {
                                        JSONArray clausesJArray = whereClauseJson.getJSONArray("clauses");

                                        if (clausesJArray.length() > 0) {
                                            boolean conditionSatisfied;
                                            if (andOR.equalsIgnoreCase("AND")) {
                                                conditionSatisfied = true;
                                                for (int i = 0; i < clausesJArray.length(); i++) {
                                                    JSONObject clauseJson = clausesJArray.getJSONObject(i);

                                                    if (feature.getAttributes().containsKey(clauseJson.getString("fieldName"))) {
                                                        Object actualValue = feature.getAttributes().get(clauseJson.getString("fieldName"));
                                                        if (actualValue != null) {
                                                            String operStr = clauseJson.getString("operator");
                                                            if (operStr.equalsIgnoreCase("=")) {
                                                                if (actualValue != clauseJson.get("value")) {
                                                                    conditionSatisfied = false;
                                                                    break;
                                                                }
                                                            }
                                                            else if (operStr.equalsIgnoreCase("!=")) {
                                                                if (actualValue == clauseJson.get("value")) {
                                                                    conditionSatisfied = false;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        else {
                                                            conditionSatisfied = false;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                            else {
                                                conditionSatisfied = false;
                                                for (int i = 0; i < clausesJArray.length(); i++) {
                                                    JSONObject clauseJson = clausesJArray.getJSONObject(i);

                                                    if (feature.getAttributes().containsKey(clauseJson.getString("fieldName"))) {
                                                        Object actualValue = feature.getAttributes().get(clauseJson.getString("fieldName"));
                                                        if (actualValue != null) {
                                                            String operStr = clauseJson.getString("operator");
                                                            if (operStr.equalsIgnoreCase("=")) {
                                                                if (actualValue.equals(clauseJson.get("value"))) {
                                                                    conditionSatisfied = true;
                                                                    break;
                                                                }
                                                            }
                                                            else if (operStr.equalsIgnoreCase("!=")) {
                                                                if (! actualValue.equals(clauseJson.get("value"))) {
                                                                    conditionSatisfied = true;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        else {
                                                            conditionSatisfied = false;
                                                        }
                                                    }
                                                }
                                            }

                                            if (! conditionSatisfied) {
                                                resultJobj.put("message", "You cannot perform operations on " + currentEntity.getLabel() + "-" + feature.getFeatureLabel() + " , in " + phase.getLabel() + " mode.");
                                                return resultJobj;
                                            }
                                        }
                                    }
                                }
                            }
                        }else {
                           ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "feature null.. not evaluating whereclause");
                       }
                    }else {
                        ReveloLogger.error(className, "checkOperationPermissionFromFlowInteraction", "phase "+phaseName+" not found..allowing operation "+operationName);
                    }
                }else {
                    ReveloLogger.error(className, "checkOperationPermissionFromFlowInteraction", "phase "+phaseName+" not found..allowing operation "+operationName);
                }
            }else {
                ReveloLogger.error(className, "checkOperationPermissionFromFlowInteraction", "phase "+phaseName+" not found..allowing operation "+operationName);
            }
            ReveloLogger.info(className,taskname, "isGeomEditEnableInPhase "+isGeomEditEnableInPhase+" isPropertiesEditEnabledInPhase "+isPropertiesEditEnabledInPhase);



            ReveloLogger.info(className,taskname, "checking permissions by flow");
            JSONObject permissionJson = checkOperationPermissionFromFlowInteraction(activity, currentEntity, feature, operationName, isShadowFeature);
            String currentInteractionRoleName = "some other user";
            try {
                isFlowsApplicable = permissionJson.getBoolean("isFlowApplicable");
                if (isFlowsApplicable) {
                    isPropertiesEditEnabledInFlows = permissionJson.getBoolean("arePropertiesEditable");
                    isGeomEditEnableInFlows = permissionJson.getBoolean("isGeometryEditable");
                    currentInteractionRoleName = permissionJson.getString(CMUtils.OperationPermissionVariables.currentInteractionRole);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ReveloLogger.info(className,taskname, "isFlowsApplicable "+isFlowsApplicable
                    +"; isPropertiesEditEnabledInFlows "+isPropertiesEditEnabledInFlows+"; "
                                      +"; isGeomEditEnableInFlows "+isGeomEditEnableInFlows+"; "
                                      +"; currentInteractionRoleName "+currentInteractionRoleName+"; "
                             );


            ReveloLogger.info(className,taskname, "generating complete permission json ..");
            if (isFlowsApplicable) {
                ReveloLogger.info(className,taskname, "flows applicable.. checking role permissions");
                if (isPropertiesEditEnabledInProfile && isEditorEnableInProfile && isGeomEditEnableInProfile) {
                    ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..checking for phase");
                    if(isPropertiesEditEnabledInPhase||isGeomEditEnableInPhase){
                        ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..phase allows , checking deeply for attr and geom edit permissions");
                        boolean proceed = false;
                        if(operationName.equalsIgnoreCase("add")
                        ||operationName.equalsIgnoreCase("create")
                        ||operationName.equalsIgnoreCase("delete")){

                            //you need both permissions here. any one true will not suffice.
                            if(isPropertiesEditEnabledInPhase && isGeomEditEnableInPhase){
                                proceed=true;
                            }
                        }else {//in case of update, we can have a split where we can either update attr or update geom
                            ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..phase allows . for edit we need any one of attr n geom permissions");
                            proceed=true;
                        }
                        if(proceed) {
                            ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..phase allows .. anding these val with flow values to get final permission json");
                            isPropertiesEditEnabledInFlows= isPropertiesEditEnabledInFlows && isPropertiesEditEnabledInPhase;
                            isGeomEditEnableInFlows= isGeomEditEnableInFlows && isGeomEditEnableInPhase;

                             permissionJson.put("arePropertiesEditable",isPropertiesEditEnabledInFlows);
                             permissionJson.put("isGeometryEditable",isGeomEditEnableInFlows);
                            ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..phase allows putting new val in permission json");

                            if (isPropertiesEditEnabledInFlows || isGeomEditEnableInFlows) {
                                ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..phase allows flows allow too..returning permission json "+permissionJson.toString());
                    //return true;
                    resultJobj.put("status", "success");
                    resultJobj.put("message", permissionJson);
                }
                else {
                                ReveloLogger.error(className,taskname, "flows applicable.. role allows operation..phase allows but flows dont");
                    String message = "You don't have permission to " + operationName + " feature. Please contact admin.";
                                if (operationName.equalsIgnoreCase("add") || operationName.equalsIgnoreCase("create")) {
                                    message = "You don't have permission to add " + currentEntity.getLabel() + "\nThe user with role as a " + UserInfoPreferenceUtility.getRole() + " does not have properties or geometry adding privileges." + "\nPlease contact admin.";
                                }
                                else if (operationName.equalsIgnoreCase("delete")) {
                                    message = "You cannot delete this " + currentEntity.getLabel() + " because it is ";
                                    if (! currentInteractionRoleName.equalsIgnoreCase("some other user")) {
                                        message += "still ";
                                    }
                                    message += "being updated by " + currentInteractionRoleName + ".\n" + "Try again after refreshing data.";
                                }
                                else {
                                    message = "You cannot modify this " + currentEntity.getLabel() + " because it is ";
                                    if (! currentInteractionRoleName.equalsIgnoreCase("some other user")) {
                                        message += "still ";
                                    }
                                    message += "being updated by " + currentInteractionRoleName + ".\n" + "Try again after refreshing data.";
                                }

                                resultJobj.put("status", "failure");
                                resultJobj.put("message", message);
                            }
                        }else {
                            ReveloLogger.error(className,taskname, "flows applicable.. role allows operation..phase allows but for add/delete we need both attr n geom permissions");
                            String message = "You don't have permission to "+operationName+" a " + currentEntity.getLabel() + " when operating in phase "+phaseName+". Please contact admin.";
                            resultJobj.put("status", "failure");
                            resultJobj.put("message", message);
                        }
                    }else {
                        ReveloLogger.error(className,taskname, "flows applicable.. role allows operation..but phase denies, none od attr or geom edit is allowed");
                        String message = "You don't have permission to "+operationName+" a " + currentEntity.getLabel() + " when operating in phase "+phaseName+". Please contact admin.";
                        resultJobj.put("status", "failure");
                        resultJobj.put("message", message);
                    }


                }
                else {
                    ReveloLogger.error(className,taskname, "flows applicable.. but role denies operation "+operationName);
                   String message = "You don't have permission to " + operationName + " feature.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    if(!isPropertiesEditEnabledInProfile){
                        message = "You don't have permission to make changes to values while you perform " + operationName + " operation. Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }else if(!isEditorEnableInProfile){
                        message = "Editor disabled.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }else if(!isGeomEditEnableInProfile) {
                        message = "You don't have permission to " + operationName + " feature.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    } resultJobj.put("status", "failure");
                    resultJobj.put("message", message);
                }
            }
            else {
                ReveloLogger.info(className,taskname, "no flows applicable.. checking for role permissions");
                if (isPropertiesEditEnabledInProfile && isEditorEnableInProfile && isGeomEditEnableInProfile) {
                    ReveloLogger.info(className,taskname, "role allows..checking phase permissions");
                    if(isPropertiesEditEnabledInPhase||isGeomEditEnableInPhase){
                        ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..phase allows , looking deeply");

                        boolean proceed=false;
                        if(operationName.equalsIgnoreCase("add")
                                ||operationName.equalsIgnoreCase("create")
                                ||operationName.equalsIgnoreCase("delete")){

                            //you need both permissions here. any one true will not suffice.
                            if(isPropertiesEditEnabledInPhase && isGeomEditEnableInPhase){
                                proceed=true;
                            }
                        }else {//in case of update, we can have a split where we can either update attr or update geom
                            ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..phase allows . for edit we need any one of attr n geom permissions");
                            proceed=true;
                        }

                        if(proceed) {
                            ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..phase allows putting new val in permission json");
                            permissionJson.put("arePropertiesEditable", isPropertiesEditEnabledInPhase);
                            permissionJson.put("isGeometryEditable", isGeomEditEnableInPhase);

                            resultJobj.put("status", "success");
                            resultJobj.put("message", permissionJson);
                            ReveloLogger.info(className,taskname, "flows applicable.. role allows operation..phase allows flows allow too..returning permission json "+permissionJson.toString());

                        }else {
                            ReveloLogger.error(className,taskname, "flows applicable.. role allows operation..phase allows but for add/delete we need both attr n geom permissions");
                            String message = "You don't have permission to "+operationName+" a " + currentEntity.getLabel() + " when operating in phase "+phaseName+". Please contact admin.";
                            resultJobj.put("status", "failure");
                            resultJobj.put("message", message);
                        }
                    }else {
                        ReveloLogger.error(className,taskname, "flows applicable.. role allows operation..phase denies operation "+operationName);
                        String message = "You don't have permission to "+operationName+" a " + currentEntity.getLabel() + " when operating in phase "+phaseName+". Please contact admin.";
                        resultJobj.put("status", "failure");
                        resultJobj.put("message", message);
                    }

                }
                else {
                    ReveloLogger.error(className,taskname, "no flows applicable.. but role denies operation "+operationName);
                    String message = "You don't have permission to " + operationName + " feature.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    if(!isPropertiesEditEnabledInProfile){
                         message = "You don't have permission to make changes to values while you perform " + operationName + " operation. Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }else if(!isEditorEnableInProfile){
                         message = "Editor disabled.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }else if(!isGeomEditEnableInProfile) {
                         message = "You don't have permission to " + operationName + " feature.  Please contact admin to revise overall permissions of role "+UserInfoPreferenceUtility.getRole();
                    }

                    resultJobj.put("status", "failure");
                    resultJobj.put("message", message);
                }
            }

            return resultJobj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultJobj;
    }

    public static JSONObject checkOperationPermissionFromFlowInteraction(Context context, CMEntity currentEntity, Feature feature, String operationName, boolean isShadowFeature) {
        JSONObject permissionJson = new JSONObject();
        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "checking if user has permission according to flow-interaction model");
        try {
            JSONArray propertiesArray = new JSONArray();
            permissionJson.put(OperationPermissionVariables.arePropertiesEditable, false);
            permissionJson.put(OperationPermissionVariables.isGeometryEditable, false);
            permissionJson.put(OperationPermissionVariables.isFlowApplicable, false);
            permissionJson.put(OperationPermissionVariables.propertiesArray, propertiesArray);
            permissionJson.put(OperationPermissionVariables.currentInteractionName, "");
            permissionJson.put(OperationPermissionVariables.currentInteractionRole, "");
            permissionJson.put(OperationPermissionVariables.currentFlowName, "");


            if (currentEntity == null || ! currentEntity.hasFlows()) {
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "currentEntity==null || currentEntity.getFlowsJArray() == null || currentEntity.getFlowsJArray().length() == 0");
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no flow applicable, giving editform and editgeom permissions");
                permissionJson.put(OperationPermissionVariables.arePropertiesEditable, true);
                permissionJson.put(OperationPermissionVariables.isGeometryEditable, true);
                permissionJson.put(OperationPermissionVariables.isFlowApplicable, false);
                permissionJson.put(OperationPermissionVariables.propertiesArray, propertiesArray);
                permissionJson.put(OperationPermissionVariables.currentInteractionName, "");
                permissionJson.put(OperationPermissionVariables.currentInteractionRole, "");
                permissionJson.put(OperationPermissionVariables.currentFlowName, "");
                return permissionJson;
            }
//check if interaction exists for this user role //TODO logs - Repeated variables
            String userRole = UserInfoPreferenceUtility.getRole();
            FlowGraph flowGraph = currentEntity.getFlowGraph();
            JSONObject interactionJson = flowGraph.getVertex(Interaction.FieldNames.roleName, userRole);
            if (interactionJson == null || ! interactionJson.has("status") || interactionJson.getString("status").equalsIgnoreCase("failure")) {
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no interaction node found for role " + userRole + " in flow " + flowGraph.getFlowGraphName() + " for entity " + currentEntity.getName());
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "flow applicable, but no interaction found. hence restricting editform and editgeom permissions");
                return permissionJson;
            }


            Interaction roleBasedInteraction = (Interaction) interactionJson.get("result");
            String interactionName = roleBasedInteraction.getInteractionName();
            ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", " interaction node found for role " + userRole + " in flow " + flowGraph.getFlowGraphName() + " for entity " + currentEntity.getName());
            ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "interaction name: " + interactionName);
            ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "interaction operations: " + roleBasedInteraction.getAllowedOperationsList());
            ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "interaction properties: " + roleBasedInteraction.getPropertiesList());

            if (isShadowFeature) {
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "Generating permission json for a shadow feature..");
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "since it is a shadow feature, we allow any operation by current user..in this case, the operation " + operationName);
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "hence, flow applicable and editform editgeom permissions granted");
                boolean arePropertiesEditable = true;
                boolean isGeometryEditable = true;
                if (operationName.equalsIgnoreCase("update")) {
//                    isGeometryEditable = roleBasedInteraction.getAllowedOperationsList().contains("updategeometry");
                    isGeometryEditable = roleBasedInteraction.isOperationAllowed("updategeometry");
//                            arePropertiesEditable = roleBasedInteraction.getAllowedOperationsList().contains("updateattributes");
                    arePropertiesEditable = roleBasedInteraction.isOperationAllowed("updateattributes");
                }

                permissionJson.put(OperationPermissionVariables.arePropertiesEditable, arePropertiesEditable);
                permissionJson.put(OperationPermissionVariables.isGeometryEditable, isGeometryEditable);
                permissionJson.put(OperationPermissionVariables.isFlowApplicable, true);
                permissionJson.put(OperationPermissionVariables.propertiesArray, roleBasedInteraction.getPropertiesJSONArray());
                permissionJson.put(OperationPermissionVariables.currentInteractionName, roleBasedInteraction.getInteractionName());
                permissionJson.put(OperationPermissionVariables.currentInteractionRole, roleBasedInteraction.getRoleName());
                permissionJson.put(OperationPermissionVariables.currentFlowName, roleBasedInteraction.getFlowName());
                return permissionJson;
            }
            else {

                if (feature != null) {
                    ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "checking interaction permission against feature : " + feature.getFeatureId());
                    ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "checking interaction permission against feature : " + feature.getFeatureLabel());

                    //feature.getAttributes().put("metadata", "interaction3_hoff_delete");


                    JSONObject metadataJson = currentEntity.getFeatureTable().getMetadataEntry(context, currentEntity.getName(), String.valueOf(feature.getFeatureId()));


                    if (metadataJson == null) {
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no metadata found for feature " + feature.getFeatureId() + " in metadata table ");
                        return permissionJson;
                    }
                    else {
                    try {
                        JSONObject metadatainteractionJson = flowGraph.getVertex(Interaction.FieldNames.interactionName, metadataJson.getString(OperationPermissionVariables.currentInteractionName));
                        Interaction metadataInteraction = (Interaction) metadatainteractionJson.get("result");
                        metadataJson.put(CMUtils.OperationPermissionVariables.currentInteractionRole, metadataInteraction.getRoleName());
                    }catch (Exception e){
                        e.printStackTrace();///herereee
                    }
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", " metadata found for feature " + feature.getFeatureId());
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "metadata = " + metadataJson.toString());
                    }
                }
                else {
                    ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "input feature = null ");
                }

                boolean isThisBeginningOfInteractions = false;
                if (feature == null) {
                    isThisBeginningOfInteractions = true;
                }
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "isThisBeginningOfInteractions = " + isThisBeginningOfInteractions);
                if (isThisBeginningOfInteractions) {
                    ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no feature metadata found and user's role-based interaction is at root of the flow graph");
                    if (roleBasedInteraction.isOperationAllowed(operationName)) {
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "user's rolebased interaction has operation " + operationName + " in allowed operations list.");
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "hence, flow applicable and editform editgeom permissions granted");
                        boolean arePropertiesEditable = true;
                        boolean isGeometryEditable = true;
                        if (operationName.equalsIgnoreCase("update")) {
//                            isGeometryEditable = roleBasedInteraction.getAllowedOperationsList().contains("updategeometry");
                            isGeometryEditable = roleBasedInteraction.isOperationAllowed("updategeometry");
//                            arePropertiesEditable = roleBasedInteraction.getAllowedOperationsList().contains("updateattributes");
                            arePropertiesEditable = roleBasedInteraction.isOperationAllowed("updateattributes");
                        }

                        permissionJson.put(OperationPermissionVariables.arePropertiesEditable, arePropertiesEditable);
                        permissionJson.put(OperationPermissionVariables.isGeometryEditable, isGeometryEditable);
                        permissionJson.put(OperationPermissionVariables.isFlowApplicable, true);
                        permissionJson.put(OperationPermissionVariables.propertiesArray, roleBasedInteraction.getPropertiesJSONArray());
                        permissionJson.put(OperationPermissionVariables.currentInteractionName, roleBasedInteraction.getInteractionName());
                        permissionJson.put(OperationPermissionVariables.currentFlowName, roleBasedInteraction.getFlowName());
                        return permissionJson;
                    }
                    else {
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "user's rolebased interaction does not have operation " + operationName + " in allowed operations list.");
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "hence, flow applicable but editform editgeom permissions denied");
                        permissionJson.put(OperationPermissionVariables.arePropertiesEditable, false);
                        permissionJson.put(OperationPermissionVariables.isGeometryEditable, false);
                        permissionJson.put(OperationPermissionVariables.isFlowApplicable, true);
                        permissionJson.put(OperationPermissionVariables.propertiesArray, propertiesArray);
                        permissionJson.put(OperationPermissionVariables.currentInteractionName, "");
                        permissionJson.put(OperationPermissionVariables.currentFlowName, "");
                        return permissionJson;
                    }
                }
                else {
                    ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "user role's interaction is not at root");
                    String featureStatusInteractionName = "";
                    String featureStatusFlowName = "";
                    String featureStatusRoleName = "";

                    if (feature != null) {
                        JSONObject metadataJson = currentEntity.getFeatureTable().getMetadataEntry(context, currentEntity.getName(), String.valueOf(feature.getFeatureId()));

                        if (metadataJson != null) {
                            if (metadataJson.has("w9Id") && metadataJson.getString("w9Id").equalsIgnoreCase(String.valueOf(feature.getFeatureId()))) {
                                if (metadataJson.has(OperationPermissionVariables.currentFlowName) && metadataJson.getString(OperationPermissionVariables.currentFlowName) != null && ! metadataJson.getString(OperationPermissionVariables.currentFlowName).isEmpty()) {
                                    featureStatusFlowName = metadataJson.getString(OperationPermissionVariables.currentFlowName);
                                    if (metadataJson.has(OperationPermissionVariables.currentInteractionName) && metadataJson.getString(OperationPermissionVariables.currentInteractionName) != null && ! metadataJson.getString(OperationPermissionVariables.currentInteractionName).isEmpty()) {
                                        featureStatusInteractionName = metadataJson.getString(OperationPermissionVariables.currentInteractionName);
                                    }
                                }
                            }
                        }
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", " feature metadata flow:" + featureStatusFlowName + " , interaction: " + featureStatusInteractionName);
                    }
                    else {
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", " feature = null or no valid metadata json found");
                    }
                    if (! featureStatusFlowName.isEmpty() && ! featureStatusInteractionName.isEmpty()) {
                        featureStatusRoleName = "some other user";
                        JSONObject interactionJObj = currentEntity.getFlowGraph().getVertex(Interaction.FieldNames.interactionName, featureStatusInteractionName);
                        if (interactionJObj == null || ! interactionJObj.has("status") || interactionJObj.getString("status").equalsIgnoreCase("failure")) {
                            ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no interaction node found for name " + interactionName + " in flow " + flowGraph.getFlowGraphName() + " for entity " + currentEntity.getName());
                        }
                        else {
                            Interaction nameBasedInteraction = (Interaction) interactionJObj.get("result");
                            featureStatusRoleName = nameBasedInteraction.getRoleName();
                            ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "Currently the feature is found to be in edit mode with role " + featureStatusRoleName);
                        }
                    }
                    if (! featureStatusFlowName.isEmpty() && featureStatusFlowName.equalsIgnoreCase(roleBasedInteraction.getFlowName()) && ! featureStatusInteractionName.isEmpty() && featureStatusInteractionName.equalsIgnoreCase(roleBasedInteraction.getInteractionName()) && roleBasedInteraction.isOperationAllowed(operationName)) {
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", " feature metadata matches with role's interaction name :" + roleBasedInteraction.getInteractionName());
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", " the operation :" + operationName + " exists in this interaction's allowed operations list");
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "hence, flow applicable and editform editgeom permissions granted");

                        boolean arePropertiesEditable = true;
                        boolean isGeometryEditable = true;
                        if (operationName.equalsIgnoreCase("update")) {
                            isGeometryEditable = roleBasedInteraction.isOperationAllowed("updategeometry");
                            arePropertiesEditable = roleBasedInteraction.isOperationAllowed("updateattributes");
                        }


                        permissionJson.put(OperationPermissionVariables.arePropertiesEditable, arePropertiesEditable);
                        permissionJson.put(OperationPermissionVariables.isGeometryEditable, isGeometryEditable);
                        permissionJson.put(OperationPermissionVariables.isFlowApplicable, true);
                        permissionJson.put(OperationPermissionVariables.propertiesArray, roleBasedInteraction.getPropertiesJSONArray());
                        permissionJson.put(OperationPermissionVariables.currentInteractionName, roleBasedInteraction.getInteractionName());
                        permissionJson.put(OperationPermissionVariables.currentInteractionRole, roleBasedInteraction.getRoleName());
                        permissionJson.put(OperationPermissionVariables.currentFlowName, roleBasedInteraction.getFlowName());
                        return permissionJson;
                    }
                    else {
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", " feature metadata flow:" + featureStatusFlowName + " , interaction: " + featureStatusInteractionName + " and role's flowname : " + roleBasedInteraction.getFlowName() + ", interaction name :" + roleBasedInteraction.getInteractionName());
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "OR the operation :" + operationName + " does not exist in this interaction's allowed operations list");
                        ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "hence, flow is applicable but editform editgeom permissions denied");
                        permissionJson.put(OperationPermissionVariables.arePropertiesEditable, false);
                        permissionJson.put(OperationPermissionVariables.isGeometryEditable, false);
                        permissionJson.put(OperationPermissionVariables.isFlowApplicable, true);
                        permissionJson.put(OperationPermissionVariables.propertiesArray, propertiesArray);
                        permissionJson.put(OperationPermissionVariables.currentInteractionName, featureStatusInteractionName);
                        permissionJson.put(OperationPermissionVariables.currentInteractionRole, featureStatusRoleName);
                        permissionJson.put(OperationPermissionVariables.currentFlowName, featureStatusFlowName);
                        return permissionJson;
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return permissionJson;
    }

    public static JSONObject getInteraction(Context context, CMEntity currentEntity, String flowName, String interactionName) {
        JSONObject interactionJson = new JSONObject();

        try {
            interactionJson.put("status", "failure");
            interactionJson.put("message", "Reason not available.");

            if (currentEntity == null || ! currentEntity.hasFlows()) {
                interactionJson.put("message", "Either entity not found or it does not have flows.");
            }

            interactionJson = currentEntity.getFlowGraph().getVertex(Interaction.FieldNames.interactionName, interactionName);
            if (interactionJson == null || ! interactionJson.has("status") || interactionJson.getString("status").equalsIgnoreCase("failure")) {
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no interaction node found with name " + interactionName + " in flow " + currentEntity.getFlowGraph().getFlowGraphName() + " for entity " + currentEntity.getName());
                return interactionJson;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return interactionJson;
    }

    public static boolean isInteractionAtBeginning(Context context, CMEntity currentEntity, String flowName, String interactionName) {
        JSONObject interactionJson = new JSONObject();

        try {
            interactionJson.put("status", "failure");
            interactionJson.put("message", "Reason not available.");

            if (currentEntity == null || ! currentEntity.hasFlows()) {
                interactionJson.put("message", "Either entity not found or it does not have flows.");
            }

            interactionJson = currentEntity.getFlowGraph().getVertex(Interaction.FieldNames.interactionName, interactionName);
            if (interactionJson == null || ! interactionJson.has("status") || interactionJson.getString("status").equalsIgnoreCase("failure")) {
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no interaction node found with name " + interactionName + " in flow " + currentEntity.getFlowGraph().getFlowGraphName() + " for entity " + currentEntity.getName());
                return false;
            }
            else {
                Interaction currentInteraction = (Interaction) interactionJson.get("result");
                Set<Interaction> rootInteractions = currentEntity.getFlowGraph().getRootVertices();
                Iterator<Interaction> iterator = rootInteractions.iterator();
                while (iterator.hasNext()) {
                    Interaction ri = iterator.next();
                    if (ri.equals(currentInteraction)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isInteractionAtEnd(Context context, CMEntity currentEntity, String flowName, String interactionName) {
        JSONObject interactionJson = new JSONObject();

        try {
            interactionJson.put("status", "failure");
            interactionJson.put("message", "Reason not available.");

            if (currentEntity == null || ! currentEntity.hasFlows()) {
                interactionJson.put("message", "Either entity not found or it does not have flows.");
            }

            interactionJson = currentEntity.getFlowGraph().getVertex(Interaction.FieldNames.interactionName, interactionName);
            if (interactionJson == null || ! interactionJson.has("status") || interactionJson.getString("status").equalsIgnoreCase("failure")) {
                ReveloLogger.debug(className, "checkOperationPermissionFromFlowInteraction", "no interaction node found with name " + interactionName + " in flow " + currentEntity.getFlowGraph().getFlowGraphName() + " for entity " + currentEntity.getName());
                return false;
            }
            else {
                Interaction currentInteraction = (Interaction) interactionJson.get("result");
                Set<Interaction> leafInteractions = currentEntity.getFlowGraph().getLeafVertices();
                Iterator<Interaction> iterator = leafInteractions.iterator();
                while (iterator.hasNext()) {
                    Interaction ri = iterator.next();
                    if (ri.equals(currentInteraction)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isLeafEntity(CMEntity cmEntity, Context context) {
        if (cmEntity == null || context == null)
            return false;
        JSONObject graphResult = CMUtils.getCMGraph(context);
        CMGraph cmGraph = null;
        try {
            if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                cmGraph = (CMGraph) graphResult.get("result");
            }
            else {
                ReveloLogger.error("HomePresenter", "createEntityListData", "Could not create enitities list - could not fetch graph from memory. Reason - " + graphResult.getString("message"));
            }

            if (cmGraph == null) {
                return false;
            }

            return cmGraph.isLeafVertex(cmEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static JSONObject getDbConfigJson(Context context) {
        JSONObject dbConfigJson = null;
        boolean abortAndReturnNull=false;
        try {
            dbConfigJson = new JSONObject();
            JSONObject databasesJson = new JSONObject();
            JSONObject secondaryDatabaseJson = new JSONObject();
            JSONObject secDbDataSourceJobj = new JSONObject();
            JSONArray secDbDatasetsJArray = new JSONArray();


            //datasource
           JSONObject dataSourceInfoForDataGpkg =  DbRelatedConstants.getDataSourceInfoForDataGpkg(context);
           if(dataSourceInfoForDataGpkg!=null && dataSourceInfoForDataGpkg.has("datasourceName")){
               secDbDataSourceJobj.put("name",dataSourceInfoForDataGpkg.getString("datasourceName"));
               secDbDataSourceJobj.put("type","geopackage");
               if(dataSourceInfoForDataGpkg.has("dbPath")){
                   secDbDataSourceJobj.put("filePath",dataSourceInfoForDataGpkg.getString("dbPath"));
               }else {
                   abortAndReturnNull=true;
               }

           }else {
               abortAndReturnNull=true;
           }

           //datasets
            if(!abortAndReturnNull){

                JSONObject cmGraphResult = CMUtils.getCMGraph(context);
                if (cmGraphResult.has("status") && cmGraphResult.getString("status").equalsIgnoreCase("success")) {
                    CMGraph cmGraph = (CMGraph) cmGraphResult.get("result");
                    TopologicalOrderIterator itr = cmGraph.getTopoSortIterator();
                    while (itr.hasNext()) {
                        CMEntity cmEntity = (CMEntity) itr.next();
                        if(!cmEntity.getName().equalsIgnoreCase(AppConstants.TRAIL_TABLE_NAME)
                        && cmEntity.getType().equalsIgnoreCase("spatial")){
                            JSONObject datasetJobj = new JSONObject();
                            datasetJobj.put("name",cmEntity.getName()+"_"+UserInfoPreferenceUtility.getSurveyName());
                            datasetJobj.put("type",cmEntity.getType());
                            datasetJobj.put("geometryType",cmEntity.getGeometryType());
                            datasetJobj.put("idPropertyName",cmEntity.getW9IdProperty());
                            datasetJobj.put("labelPropertyName",cmEntity.getLabelPropertyName());
                            JSONArray columnsArray = new JSONArray();
                            JSONArray propertiesArray = new JSONArray();//for display
                            for(Attribute attribute:cmEntity.getProperties()){
                                if(!attribute.getName().startsWith("w9")) {
                                    JSONObject columnJson = new JSONObject();
                                    columnJson.put("name", attribute.getName());
                                    columnJson.put("type", attribute.getType());
                                    columnsArray.put(columnJson);
                                    if(attribute.getName().equalsIgnoreCase(cmEntity.getW9IdProperty())
                                            ||attribute.getName().equalsIgnoreCase(cmEntity.getLabelPropertyName())){
                                        propertiesArray.put(attribute.getName());
                                    }
                                }
                            }
                            datasetJobj.put("columnNames",columnsArray);
                            JSONObject outputJboj = new JSONObject();
                            outputJboj.put("displayLabel",cmEntity.getLabelPropertyName());
                            outputJboj.put("properies",propertiesArray);
                            datasetJobj.put("outputAttributes",outputJboj);
                            datasetJobj.put("query",new JSONArray());
                            secDbDatasetsJArray.put(datasetJobj);
                        }
                    }
                }else {
                    abortAndReturnNull=true;
                }
                if(secDbDatasetsJArray.length()==0){
                    abortAndReturnNull=true;
                }
            }
            if(!abortAndReturnNull){
                secondaryDatabaseJson.put("dataSource",secDbDataSourceJobj);
                secondaryDatabaseJson.put("datasets",secDbDatasetsJArray);
                databasesJson.put("secondary",secondaryDatabaseJson);
                dbConfigJson.put("databases",databasesJson);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        if(abortAndReturnNull){
            return null;
        }else {
            return dbConfigJson;
        }
    }

    public interface OperationPermissionVariables {
        String arePropertiesEditable = "arePropertiesEditable";
        String isGeometryEditable = "isGeometryEditable";
        String isFlowApplicable = "isFlowApplicable";
        String propertiesArray = "propertiesArray";
        String currentInteractionName = "currentInteractionName";
        String currentInteractionRole = "currentInteractionRole";
        String currentFlowName = "currentFlowName";
    }

    /*public static JSONObject getCMEntitiesMap(Activity activity) {
        if(CMEntitiesMap==null) {
            try {
                CMEntitiesMap=new HashMap<>();
                JSONObject entitiesResult = getCMEntitiesList(activity);
                if(entitiesResult.has("status") && entitiesResult.getString("status").equalsIgnoreCase("success")){
                    List<CMEntity> cmEntitiesList = (List<CMEntity>) entitiesResult.get("result");
                    for (CMEntity cmEntity : cmEntitiesList) {
                        CMEntitiesMap.put(cmEntity.getName(), cmEntity);
                    }
                }else {
                    CMEntitiesMap=null;
                    ReveloLogger.error("CMUtils","getCMEntitiesMap","Error occurred while creating cm entities map in memory - could not fetch entities list");
                    return SystemUtils.logAndReturnMessage("failure","Error occurred while creating cm entities map in memory - could not fetch entities list");
                    *//*return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                            "Error occurred while creating cm entities map in memory - could not fetch entities list",null);*//*

                }
            }catch (Exception e){
                e.printStackTrace();
                CMEntitiesMap=null;
                ReveloLogger.error("CMUtils","getCMEntitiesMap","Exception occurred while creating CM entities map in memory - "+e.getCause());
               *//* return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_FAILURE,
                        "Exception occurred while creating CM entities map in memory - "+e.getCause(),null);*//*
                return SystemUtils.logAndReturnErrorMessage("Exception occurred while creating CM entities map in memory -",e);
            }
        }
        return SystemUtils.logAndReturnObject("success","",CMEntitiesMap);
        //return new ReveloOperationReturnType(ReveloOperationReturnType.RETURN_TYPE_OPERATION_STATUS_SUCCESS,"",CMEntitiesMap);
    }*/
}
