package com.sixsimplex.treasurehunt.revelocore.layer;

import org.json.JSONObject;

public class TextStyleModel {

    private String name;
    private String type;
    private String font;
    private String textAttributeName;
    private String textAlign;
    private String textBaseline;
    private int offsetX;
    private int offsetY;
    private int rotation;
    private String color;
    private boolean enable;

    public static TextStyleModel parseTextStyleJson(JSONObject textStyleJson) {

        TextStyleModel textStyleModel = new TextStyleModel();

        try {

            String name = textStyleJson.has("name") ? textStyleJson.getString("name") : "";
            String type = textStyleJson.has("type") ? textStyleJson.getString("type") : "";
            JSONObject styleJsonObject =textStyleJson.has("style")? textStyleJson.getJSONObject("style"): null;

            if (styleJsonObject != null){
                String font = styleJsonObject.has("font") ? styleJsonObject.getString("font") : "";
                String textAttributeName = styleJsonObject.has("textAttributeName") ? styleJsonObject.getString("textAttributeName") : "";
                String textAlign = styleJsonObject.has("textAlign") ? styleJsonObject.getString("textAlign") : "";
                String textBaseline = styleJsonObject.has("textBaseline") ? styleJsonObject.getString("textBaseline") : "";

                int offsetX = styleJsonObject.has("offsetX") ? styleJsonObject.getInt("offsetX") : -1;
                int offsetY = styleJsonObject.has("offsetY") ? styleJsonObject.getInt("offsetY") : -1;
                int rotation = styleJsonObject.has("rotation") ? styleJsonObject.getInt("rotation") : -1;

                JSONObject fillJsonObject = styleJsonObject.getJSONObject("fill");
                String color = fillJsonObject.has("color") ? fillJsonObject.getString("color") : "";
                boolean enabled = textStyleJson.has("enabled") && textStyleJson.getBoolean("enabled");

                textStyleModel.setFont(font);
                textStyleModel.setTextAttributeName(textAttributeName);
                textStyleModel.setTextAlign(textAlign);
                textStyleModel.setTextBaseline(textBaseline);
                textStyleModel.setOffsetX(offsetX);
                textStyleModel.setOffsetY(offsetY);
                textStyleModel.setRotation(rotation);
                textStyleModel.setColor(color);
                textStyleModel.setEnable(enabled);
            }


            textStyleModel.setName(name);
            textStyleModel.setType(type);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return textStyleModel;
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

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public String getTextAttributeName() {
        return textAttributeName;
    }

    public void setTextAttributeName(String textAttributeName) {
        this.textAttributeName = textAttributeName;
    }

    public String getTextAlign() {
        return textAlign;
    }

    public void setTextAlign(String textAlign) {
        this.textAlign = textAlign;
    }

    public String getTextBaseline() {
        return textBaseline;
    }

    public void setTextBaseline(String textBaseline) {
        this.textBaseline = textBaseline;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

}
