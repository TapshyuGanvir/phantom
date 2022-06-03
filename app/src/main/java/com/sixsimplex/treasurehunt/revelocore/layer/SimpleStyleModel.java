package com.sixsimplex.treasurehunt.revelocore.layer;

import org.json.JSONObject;

import java.io.Serializable;

public class SimpleStyleModel implements Serializable {

    private String name;
    private String type;
    private StyleModel styleModel;
    private int minZoom;
    private int maxZoom;
    private boolean enabled;

    public SimpleStyleModel() {
        styleModel = new StyleModel();
    }

    public static SimpleStyleModel parseSimpleStyleObject(JSONObject simpleStyleJsonObject) {

        SimpleStyleModel simpleStyleModel = new SimpleStyleModel();

        try {

            String name = simpleStyleJsonObject.has("name") ? simpleStyleJsonObject.getString("name") : "";
            String type = simpleStyleJsonObject.has("type") ? simpleStyleJsonObject.getString("type") : "";

            boolean enabled = simpleStyleJsonObject.has("enabled") && simpleStyleJsonObject.getBoolean("enabled");

            JSONObject styleObject = simpleStyleJsonObject.has("style") ? simpleStyleJsonObject.getJSONObject("style") : null;
            if (styleObject != null) {
                int points = styleObject.has("points") ? styleObject.getInt("points") : -1;
                int radius = styleObject.has("radius") ? styleObject.getInt("radius") : -1;

                JSONObject fillObject = styleObject.getJSONObject("fill");

                String fillColor = fillObject.has("color") ? fillObject.getString("color") : "";
                String strokeColor = "";
                int strokeWidth =2;
                if(styleObject.has("stroke")){
                    JSONObject strokeObject = styleObject.getJSONObject("stroke");
                    strokeColor = strokeObject.has("color") ? strokeObject.getString("color") : "";
                    strokeWidth = strokeObject.has("width") ? strokeObject.getInt("width") : -1;
                }
                String shape = "" ; int numPoints = -1 ; String path = "";
                if(styleObject.has("pointInfo")) {
                    JSONObject pointInfoJsonObject = styleObject.getJSONObject("pointInfo");

                    shape = pointInfoJsonObject.has("shape") ? pointInfoJsonObject.getString("shape") : "";

                    if (pointInfoJsonObject.has("other")) {
                        JSONObject otherJsonObject = pointInfoJsonObject.getJSONObject("other");
                        numPoints = otherJsonObject.has("numPoints") ? otherJsonObject.getInt("numPoints") : -1;
                        path = otherJsonObject.has("path") ? otherJsonObject.getString("path") : "";
                    }
                }
                int minZoom = styleObject.has("minZoom") ? styleObject.getInt("minZoom") : -1;
                int maxZoom = styleObject.has("maxZoom") ? styleObject.getInt("maxZoom") : -1;

                simpleStyleModel.setFillColor(fillColor);
                simpleStyleModel.setStrokeColor(strokeColor);
                simpleStyleModel.setStrokeWidth(strokeWidth);
                simpleStyleModel.setPoints(points);
                simpleStyleModel.setRadius(radius);
                simpleStyleModel.setShape(shape);
                simpleStyleModel.setOtherNumPoints(numPoints);
                simpleStyleModel.setPath(path);
                simpleStyleModel.setMinZoom(minZoom);
                simpleStyleModel.setMaxZoom(maxZoom);
            }

            simpleStyleModel.setName(name);
            simpleStyleModel.setType(type);

            simpleStyleModel.setEnabled(enabled);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return simpleStyleModel;
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

    public String getFillColor() {
        return styleModel.getFillColor();
    }

    public void setFillColor(String fillColor) {
        this.styleModel.setFillColor(fillColor);
    }

    public String getStrokeColor() {
        return styleModel.getStrokeColor();
    }

    public void setStrokeColor(String strokeColor) {
        this.styleModel.setStrokeColor(strokeColor);
    }

    public int getStrokeWidth() {
        return styleModel.getStrokeWidth();
    }

    public void setStrokeWidth(int strokeWidth) {
        this.styleModel.setStrokeWidth(strokeWidth);
    }

    public int getPoints() {
        return styleModel.getPoints();
    }

    public void setPoints(int points) {
        this.styleModel.setPoints(points);
    }

    public int getRadius() {
        return styleModel.getRadius();
    }

    public void setRadius(int radius) {
        this.styleModel.setRadius(radius);
    }

    public String getShape() {
        return styleModel.getShape();
    }

    public void setShape(String shape) {
        this.styleModel.setShape(shape);
    }

    public int getOtherNumPoints() {
        return styleModel.getOtherNumPoints();
    }

    public void setOtherNumPoints(int otherNumPoints) {
        this.styleModel.setOtherNumPoints(otherNumPoints);
    }

    public String getPath() {
        return styleModel.getPath();
    }

    public void setPath(String path) {
        this.styleModel.setPath(path);
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}