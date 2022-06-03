package com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil;

import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.DbRelatedConstants;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageManagerAgent;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;

import java.util.LinkedHashMap;
import java.util.Map;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;

public class EntitiesTable {

    /*------------------------- entities table -----------------------------*/

    private static final String ENTITIES_TABLE_NAME = "entities";
    private static final String ENTITIES_NAME = "name";
    private static final String ENTITIES_LABEL = "label";
    private static final String ENTITIES_ABBR = "abbr";
    private static final String ENTITIES_TYPE = "type";
    private static final String ENTITIES_GEOMETRY_TYPE = "geometrytype";
    private static final String ENTITIES_W9_ID_PROPERTY = "w9idpropertyname";
    private static final String ENTITIES_LABEL_PROPERTY = "labelpropertyname";
    private static final String ENTITIES_CATEGORY_PROPERTY_NAME = "categoryPropertyName";
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
    private static final String ENTITIES_PERSPECTIVES = "perspectives";
    private static final String ENTITIES_CATEGORIES = "categories";
    private static final String ENTITIES_HASSHADOWTABLE = "hasshadowtable";

    private static final String ENTITIES_ID_GENERATION_TYPE = "idgenerationtype";
    private static final String ENTITIES_IS_LOCK_LEAF = "islockleaf";
//todo add hasshadowtabel
    /*----------------------------------------------------------------------*/

    private static AttributesDao dao;
    private static String className = "EntitiesTable";
    private static Map<String, FeatureLayer> featureLayerMap =null;

    private static AttributesDao getDao(Context context) {
        try {
            if (dao == null) {
                GeoPackage metaGeoPackage = GeoPackageManagerAgent.getMetaGeoPackage(context, DbRelatedConstants.getPropertiesJsonForMetdataGpkg(context));
                if (metaGeoPackage != null) {
                    dao = metaGeoPackage.getAttributesDao(ENTITIES_TABLE_NAME);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dao;
    }



    public static Map<String, FeatureLayer> getFeatureLayers(Context context) {

        if(featureLayerMap == null){
            try {
                featureLayerMap = new LinkedHashMap<>();
                AttributesDao entitiesAttributeDao = getDao(context);

                if (entitiesAttributeDao != null) {

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

                                FeatureLayer featureLayer = new FeatureLayer();
                                featureLayer.setName(name);
                                featureLayer.setLabel(label);
                                featureLayer.setType(type);
                                featureLayer.setGeometryType(geomType);
                                featureLayer.setW9IdProperty(w9IdName);
                                featureLayer.setLabelPropertyName(w9IdLabel);
                                featureLayer.setCategoryPropertyName(categoryName);
                                featureLayer.setAbbr(abbr);
                                featureLayer.setLocked(isLocked);
                                featureLayer.setSelectedRendererName(rendererName);
                                featureLayer.setIdGenRules(idRule);
                                featureLayer.setSimpleStyle(simpleStyle);
                                featureLayer.setUniqueValueStyle(uniqueStyle);
                                featureLayer.setProperties(properties);
                                featureLayer.setPropertyGroups(propertyGroups);
                                featureLayer.setDomainValues(domainValues);
                                featureLayer.setCategories(categories);
                                featureLayer.setHasShadowTable(hasShadowTable);

                                featureLayerMap.put(name, featureLayer);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return featureLayerMap;
    }

    public static void clearAttributeDao() {

        if (dao != null) {
            dao = null;
        }
        if(featureLayerMap!=null){
            featureLayerMap=null;
        }
    }
}