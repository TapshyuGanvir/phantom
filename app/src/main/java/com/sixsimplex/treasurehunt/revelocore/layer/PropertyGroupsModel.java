package com.sixsimplex.treasurehunt.revelocore.layer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PropertyGroupsModel implements Serializable {

    private String name;
    private String label;
    private int index;
    private List<String> propertyNames;

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    public static PropertyGroupsModel parsePropertyGroupJson(JSONObject propertyGroupJsonObject) {

        PropertyGroupsModel propertyGroupsModel = new PropertyGroupsModel();

        try {

            String name = propertyGroupJsonObject.has("name") ? propertyGroupJsonObject.getString("name") : "";
            String label = propertyGroupJsonObject.has("label") ? propertyGroupJsonObject.getString("label") : "";
            String index = propertyGroupJsonObject.has("index") ? propertyGroupJsonObject.getString("index") : "";

            JSONArray propertyNameArray = propertyGroupJsonObject.getJSONArray("propertyNames");

            List<String> propertyNameList = new ArrayList<>();
            for (int i = 0; i < propertyNameArray.length(); i++) {
                propertyNameList.add(propertyNameArray.getString(i));
            }

            propertyGroupsModel.setName(name);
            propertyGroupsModel.setLabel(label);
            try {
                propertyGroupsModel.setIndex(Integer.parseInt(index));
            } catch (Exception e) {
                e.printStackTrace();
            }
            propertyGroupsModel.setPropertyNames(propertyNameList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return propertyGroupsModel;
    }


}
