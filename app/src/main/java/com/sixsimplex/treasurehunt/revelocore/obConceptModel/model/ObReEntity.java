package com.sixsimplex.treasurehunt.revelocore.obConceptModel.model;

import com.sixsimplex.treasurehunt.revelocore.layer.Attribute;

import java.util.HashMap;

public class ObReEntity {

    private String name;
    private String label;
    private String type;
    private String geometryType;
    private String w9IdPropertyName;
    private HashMap<String, Attribute> propertiesHashMap;
    private boolean isReferenceEntity = false;

    public HashMap<String, Attribute> getPropertiesHashMap() {
        if (propertiesHashMap == null) {
            propertiesHashMap = new HashMap<>();
        }
        return propertiesHashMap;
    }

    public void setPropertiesHashMap(HashMap<String, Attribute> propertiesHashMap) {
        this.propertiesHashMap = propertiesHashMap;
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

    public String getW9IdPropertyName() {
        return w9IdPropertyName;
    }

    public void setW9IdPropertyName(String w9IdPropertyName) {
        this.w9IdPropertyName = w9IdPropertyName;
    }

    public boolean isReferenceEntity() {
        return isReferenceEntity;
    }

    public void setReferenceEntity(boolean referenceEntity) {
        isReferenceEntity = referenceEntity;
    }
}
