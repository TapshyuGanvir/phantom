package com.sixsimplex.treasurehunt.revelocore.data;

import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONObject;
import java.util.Map;


public class Feature {

    String featureLabelExpression = "";
    String className = "6simplexFeature";
    private String geometryType;
    private Object featureId;
    private String featureLabel = "";
    private String entityName;
    private String entityLabel;
    private Map<String, Object> attributes;
    private JSONObject geoJsonGeometry;


    public String getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public JSONObject getGeoJsonGeometry() {
        return this.geoJsonGeometry;
    }

    public void setGeoJsonGeometry(JSONObject geoJsonGeometry) {
        this.geoJsonGeometry = geoJsonGeometry;
    }


    public String getFeatureLabel() {
        String taskName = "getFeatureLabel";
        if(featureLabel.isEmpty()) {
            try {
                if (getAttributes() != null) {
                    featureLabel="";
                    String columnNameLabelProperty = getFeatureLabelExpression();

                    if (columnNameLabelProperty != null && ! columnNameLabelProperty.isEmpty()) {
                        ReveloLogger.info(className, taskName, "setting featureLabel.. Entity.lableproperty = " + columnNameLabelProperty);
                        if (columnNameLabelProperty.contains("+")) {
                            String[] labelComponents = columnNameLabelProperty.split("\\+");

                            for (int i = 0; i < labelComponents.length; i++) {
                                String component = labelComponents[i];
                                if (component.contains("{")) {
                                    try {
                                        String columnName = component.replace("{", "").replace("}", "");
                                        if (getAttributes().containsKey(columnName)) {
                                            featureLabel += String.valueOf(getAttributes().get(columnName));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (component.contains("'")) {
                                    try {
                                        String staticString = component.replace("'", "");
                                        featureLabel += staticString;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        else if (columnNameLabelProperty.contains("{") || columnNameLabelProperty.contains("'")) {

                            if (columnNameLabelProperty.contains("{")) {
                                try {
                                    String columnName = columnNameLabelProperty.replace("{", "").replace("}", "");
                                    if (getAttributes().containsKey(columnName)) {
                                        featureLabel += String.valueOf(getAttributes().get(columnName));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (columnNameLabelProperty.contains("'")) {
                                try {
                                    String staticString = columnNameLabelProperty.replace("'", "");
                                    featureLabel += staticString;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                        else {
                            if (getAttributes().containsKey(columnNameLabelProperty)) {
                                featureLabel = String.valueOf(getAttributes().get(columnNameLabelProperty));
                            }
                        }
                        ReveloLogger.error(className, taskName, "setting featureLabel = " + featureLabel);
                    }
                    else {
                        ReveloLogger.error(className, taskName, "No label property found. setting featureLabel = empty ");
                    }


                    if (featureLabel == null || featureLabel.isEmpty()) {
                        featureLabel = String.valueOf(getFeatureId());
                        ReveloLogger.error(className, taskName, "featureLabel = empty, setting featureLabel =id i.e. " + featureLabel);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (featureLabel.isEmpty() && getFeatureId() != null) {
            return String.valueOf(getFeatureId());
        }
        return featureLabel;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getFeatureLabelExpression() {
        return featureLabelExpression;
    }

    public void setFeatureLabelExpression(String featureLabelExpression) {
        this.featureLabelExpression = featureLabelExpression;
    }

    public Object getFeatureId() {
        return featureId;
    }

    public void setFeatureId(Object featureId) {
        this.featureId = featureId;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setFeatureLabel(String newfeatureLabel) {//"" or null sets lable acc to expression or feature id
        /*this.featureLabel = newfeatureLabel;*/
        if(newfeatureLabel==null||newfeatureLabel.isEmpty()){
            String taskName = "getFeatureLabel";
            if(featureLabel.isEmpty()) {
                try {
                    if (getAttributes() != null) {
                        featureLabel="";
                        String columnNameLabelProperty = getFeatureLabelExpression();

                        if (columnNameLabelProperty != null && ! columnNameLabelProperty.isEmpty()) {
                            ReveloLogger.info(className, taskName, "setting featureLabel.. Entity.lableproperty = " + columnNameLabelProperty);
                            if (columnNameLabelProperty.contains("+")) {
                                String[] labelComponents = columnNameLabelProperty.split("\\+");

                                for (int i = 0; i < labelComponents.length; i++) {
                                    String component = labelComponents[i];
                                    if (component.contains("{")) {
                                        try {
                                            String columnName = component.replace("{", "").replace("}", "");
                                            if (getAttributes().containsKey(columnName)) {
                                                featureLabel += String.valueOf(getAttributes().get(columnName));
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (component.contains("'")) {
                                        try {
                                            String staticString = component.replace("'", "");
                                            featureLabel += staticString;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            else if (columnNameLabelProperty.contains("{") || columnNameLabelProperty.contains("'")) {

                                if (columnNameLabelProperty.contains("{")) {
                                    try {
                                        String columnName = columnNameLabelProperty.replace("{", "").replace("}", "");
                                        if (getAttributes().containsKey(columnName)) {
                                            featureLabel += String.valueOf(getAttributes().get(columnName));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (columnNameLabelProperty.contains("'")) {
                                    try {
                                        String staticString = columnNameLabelProperty.replace("'", "");
                                        featureLabel += staticString;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                            else {
                                if (getAttributes().containsKey(columnNameLabelProperty)) {
                                    featureLabel = String.valueOf(getAttributes().get(columnNameLabelProperty));
                                }
                            }
                            ReveloLogger.error(className, taskName, "setting featureLabel = " + featureLabel);
                        }
                        else {
                            ReveloLogger.error(className, taskName, "No label property found. setting featureLabel = empty ");
                        }


                        if (featureLabel == null || featureLabel.isEmpty()) {
                            featureLabel = String.valueOf(getFeatureId());
                            ReveloLogger.error(className, taskName, "featureLabel = empty, setting featureLabel =id i.e. " + featureLabel);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (featureLabel.isEmpty() && getFeatureId() != null) {
                        featureLabel= String.valueOf(getFeatureId());
                    }
                }
            }
        }else {
            this.featureLabel=newfeatureLabel;
        }

    }
}
