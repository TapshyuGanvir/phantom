package com.sixsimplex.treasurehunt.revelocore.layer;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class UniqueStyleValueModel implements Serializable {

    public static String remainingValues ="remainingValues";
    HashMap<String, StyleModel> colorValuesMap = new HashMap<>();
    private String name;
    private String type;

    private String fieldName;
    private String fieldType;
    /*  private String fillColor;
    private String strokeColor;

    private int strokeWidth;
    private int points;
    private int radius;

    private String shape;

    private int otherNumPoints;

    private String path;
  */
    private int minZoom;
    private int maxZoom;
    private int colorRampIndex;

    private boolean enabled;

    public UniqueStyleValueModel() {
        colorValuesMap = new HashMap<>();
    }

    public static UniqueStyleValueModel parseUniqueValueObject(JSONObject uniqueStyleJsonObject ) {

        UniqueStyleValueModel uniqueStyleValueModel = new UniqueStyleValueModel();

        try {

            String name = uniqueStyleJsonObject.has("name") ? uniqueStyleJsonObject.getString("name") : "";

            String type = uniqueStyleJsonObject.has("type") ? uniqueStyleJsonObject.getString("type") : "";
            boolean enabled = uniqueStyleJsonObject.has("enabled") && uniqueStyleJsonObject.getBoolean("enabled");

            JSONObject styleObject = uniqueStyleJsonObject.has("style")?uniqueStyleJsonObject.getJSONObject("style"):null;

            if (styleObject != null){
                HashMap<String, StyleModel> colorValueMap = new HashMap<>();
                JSONObject fieldObject = styleObject.getJSONObject("field");
                String fieldName = fieldObject.has("name") ? fieldObject.getString("name") : "";
                String fieldType = fieldObject.has("type") ? fieldObject.getString("type") : "";

                JSONObject mappingObject =  styleObject.has("mapping")?styleObject.getJSONObject("mapping"):null;

                if (mappingObject != null){
                    Iterator<String> valuesStrItr = mappingObject.keys();
                    while (valuesStrItr.hasNext()) {
                        String valueStr = valuesStrItr.next();
                        JSONObject valueStyleJObj = mappingObject.getJSONObject(valueStr);
                        int points = valueStyleJObj.has("points") ? valueStyleJObj.getInt("points") : - 1;
                        int radius = valueStyleJObj.has("radius") ? valueStyleJObj.getInt("radius") : - 1;

                        JSONObject fillObject = valueStyleJObj.getJSONObject("fill");
                        String fillColor = fillObject.has("color") ? fillObject.getString("color") : "";

                        JSONObject strokeObject = valueStyleJObj.getJSONObject("stroke");
                        String strokeColor = strokeObject.has("color") ? strokeObject.getString("color") : "";
                        int strokeWidth = strokeObject.has("width") ? strokeObject.getInt("width") : - 1;

                        JSONObject pointInfoJsonObject = valueStyleJObj.getJSONObject("pointInfo");
                        String shape = pointInfoJsonObject.has("shape") ? pointInfoJsonObject.getString("shape") : "";

                        JSONObject otherJsonObject = pointInfoJsonObject.getJSONObject("other");
                        int numPoints = otherJsonObject.has("numPoints") ? otherJsonObject.getInt("numPoints") : - 1;
                        String path = otherJsonObject.has("path") ? otherJsonObject.getString("path") : "";

                        StyleModel styleModel = new StyleModel();
                        styleModel.setFillColor(fillColor);
                        styleModel.setStrokeColor(strokeColor);
                        styleModel.setStrokeWidth(strokeWidth);
                        styleModel.setPoints(points);
                        styleModel.setRadius(radius);
                        styleModel.setShape(shape);
                        styleModel.setOtherNumPoints(numPoints);
                        styleModel.setPath(path);
                        colorValueMap.put(valueStr, styleModel);
                    }

                 /*
                    JSONObject remainingObject =mappingObject.has(remainingValues)? mappingObject.getJSONObject(remainingValues):null;

                    if (remainingObject != null){

                        int points = remainingObject.has("points") ? remainingObject.getInt("points") : -1;
                        int radius = remainingObject.has("radius") ? remainingObject.getInt("radius") : -1;

                        JSONObject fillObject = remainingObject.getJSONObject("fill");
                        String fillColor = fillObject.has("color") ? fillObject.getString("color") : "";

                        JSONObject strokeObject = remainingObject.getJSONObject("stroke");
                        String strokeColor = strokeObject.has("color") ? strokeObject.getString("color") : "";
                        int strokeWidth = strokeObject.has("width") ? strokeObject.getInt("width") : -1;

                        JSONObject pointInfoJsonObject = remainingObject.getJSONObject("pointInfo");
                        String shape = pointInfoJsonObject.has("shape") ? pointInfoJsonObject.getString("shape") : "";

                        JSONObject otherJsonObject = pointInfoJsonObject.getJSONObject("other");
                        int numPoints = otherJsonObject.has("numPoints") ? otherJsonObject.getInt("numPoints") : -1;
                        String path = otherJsonObject.has("path") ? otherJsonObject.getString("path") : "";

                        StyleModel styleModel = new StyleModel();
                       styleModel.setFillColor(fillColor);
                       styleModel.setStrokeColor(strokeColor);
                       styleModel.setStrokeWidth(strokeWidth);
                       styleModel.setPoints(points);
                       styleModel.setRadius(radius);
                       styleModel.setShape(shape);
                       styleModel.setOtherNumPoints(numPoints);
                       styleModel.setPath(path);
                        colorValueMap.put(remainingValues,styleModel);
                    }*/

                    uniqueStyleValueModel.setColorValuesMap(colorValueMap);
                }

                int minZoom = styleObject.has("minZoom") ? styleObject.getInt("minZoom") : - 1;
                int maxZoom = styleObject.has("maxZoom") ? styleObject.getInt("maxZoom") : - 1;
                int colorRampIndex = styleObject.has("colorRampIndex") ? styleObject.getInt("colorRampIndex") : - 1;
                        uniqueStyleValueModel.setMinZoom(minZoom);
                        uniqueStyleValueModel.setMaxZoom(maxZoom);
                        uniqueStyleValueModel.setColorRampIndex(colorRampIndex);

                uniqueStyleValueModel.setFieldName(fieldName);
                uniqueStyleValueModel.setFieldType(fieldType);

            }

            uniqueStyleValueModel.setName(name);
            uniqueStyleValueModel.setType(type);
            uniqueStyleValueModel.setEnabled(enabled);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return uniqueStyleValueModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }

    public int getColorRampIndex() {
        return colorRampIndex;
    }

    public void setColorRampIndex(int colorRampIndex) {
        this.colorRampIndex = colorRampIndex;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public HashMap<String, StyleModel> getColorValuesMap() {
        return colorValuesMap;
    }

    public boolean containsValueInMap(String value){
        if (value != null
                && colorValuesMap != null
                && colorValuesMap.containsKey(value)) {
                return true;
        }
        return false;
    }
    public void setColorValuesMap(HashMap<String, StyleModel> colorValuesMap) {
        this.colorValuesMap = colorValuesMap;
    }

    public String getFillColor(String value) {
        String fillColor = "";
        if (colorValuesMap != null) {
            if ((value == null || value.isEmpty()) && colorValuesMap.containsKey(remainingValues)) {
                fillColor = Objects.requireNonNull(colorValuesMap.get(remainingValues)).getFillColor();
            }
            else if (value != null && colorValuesMap.containsKey(value)) {
                fillColor = Objects.requireNonNull(colorValuesMap.get(value)).getFillColor();
            }
        }
        return fillColor;
    }


    public String getStrokeColor(String value) {
        String strokeColor = "";
        if (colorValuesMap != null) {
            if ((value == null || value.isEmpty()) && colorValuesMap.containsKey(remainingValues)) {
                strokeColor = Objects.requireNonNull(colorValuesMap.get(remainingValues)).getStrokeColor();
            }
            else if (value != null && colorValuesMap.containsKey(value)) {
                strokeColor = Objects.requireNonNull(colorValuesMap.get(value)).getStrokeColor();
            }
        }
        return strokeColor;
    }


    public int getStrokeWidth(String value) {
        int strokeWidth = 2;
        if (colorValuesMap != null) {
            if ((value == null || value.isEmpty()) && colorValuesMap.containsKey(remainingValues)) {
                strokeWidth = Objects.requireNonNull(colorValuesMap.get(remainingValues)).getStrokeWidth();
            }
            else if (value != null && colorValuesMap.containsKey(value)) {
                strokeWidth = Objects.requireNonNull(colorValuesMap.get(value)).getStrokeWidth();
            }
        }
        return strokeWidth;
    }


    public int getPoints(String value) {
        int points = 1;
        if (colorValuesMap != null) {
            if ((value == null || value.isEmpty()) && colorValuesMap.containsKey(remainingValues)) {
                points = Objects.requireNonNull(colorValuesMap.get(remainingValues)).getPoints();
            }
            else if (value != null && colorValuesMap.containsKey(value)) {
                points = Objects.requireNonNull(colorValuesMap.get(value)).getPoints();
            }
        }
        return points;
    }


    public int getRadius(String value) {
        int radius = 1;
        if (colorValuesMap != null) {
            if ((value == null || value.isEmpty()) && colorValuesMap.containsKey(remainingValues)) {
                radius = Objects.requireNonNull(colorValuesMap.get(remainingValues)).getRadius();
            }
            else if (value != null && colorValuesMap.containsKey(value)) {
                radius = Objects.requireNonNull(colorValuesMap.get(value)).getRadius();
            }
        }
        return radius;
    }


    public String getShape(String value) {
        String shape = "";
        if (colorValuesMap != null) {
            if ((value == null || value.isEmpty()) && colorValuesMap.containsKey(remainingValues)) {
                shape = Objects.requireNonNull(colorValuesMap.get(remainingValues)).getShape();
            }
            else if (value != null && colorValuesMap.containsKey(value)) {
                shape = Objects.requireNonNull(colorValuesMap.get(value)).getShape();
            }
        }
        return shape;
    }


    public int getOtherNumPoints(String value) {
        int otherNumPoints = 1;
        if (colorValuesMap != null) {
            if ((value == null || value.isEmpty()) && colorValuesMap.containsKey(remainingValues)) {
                otherNumPoints = Objects.requireNonNull(colorValuesMap.get(remainingValues)).getOtherNumPoints();
            }
            else if (value != null && colorValuesMap.containsKey(value)) {
                otherNumPoints = Objects.requireNonNull(colorValuesMap.get(value)).getOtherNumPoints();
            }
        }
        return otherNumPoints;
    }


    public String getPath(String value) {
        String path = "";
        if (colorValuesMap != null) {
            if ((value == null || value.isEmpty()) && colorValuesMap.containsKey(remainingValues)) {
                path = Objects.requireNonNull(colorValuesMap.get(remainingValues)).getPath();
            }
            else if (value != null && colorValuesMap.containsKey(value)) {
                path = Objects.requireNonNull(colorValuesMap.get(value)).getPath();
            }
        }
        return path;
    }


    @Override
    public String toString() {
        return "{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", fieldName='" + fieldName + '\'' + ", fieldType='" + fieldType + '\'' + ", minZoom=" + minZoom + ", maxZoom=" + maxZoom + ", colorRampIndex=" + colorRampIndex + ", enabled=" + enabled + '}';
    }
}
