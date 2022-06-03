package com.sixsimplex.treasurehunt.revelocore.principalEndpoint.model;

public class OrgBoundariesDataModel {

    private String name;
    private String label;
    private String abbreviation;
    private String w9IdFieldName;
    private String secondaryW9IdFieldName;
    private String parentW9IdFieldName;
    private String dataSetName;
    private String updateDate;
    private boolean isReference;
    private int layerIndex;

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

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getW9IdFieldName() {
        return w9IdFieldName;
    }

    public void setW9IdFieldName(String w9IdFieldName) {
        this.w9IdFieldName = w9IdFieldName;
    }

    public String getSecondaryW9IdFieldName() {
        return secondaryW9IdFieldName;
    }

    public void setSecondaryW9IdFieldName(String secondaryW9IdFieldName) {
        this.secondaryW9IdFieldName = secondaryW9IdFieldName;
    }

    public String getParentW9IdFieldName() {
        return parentW9IdFieldName;
    }

    public void setParentW9IdFieldName(String parentW9IdFieldName) {
        this.parentW9IdFieldName = parentW9IdFieldName;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public boolean isReference() {
        return isReference;
    }

    public void setReference(boolean reference) {
        isReference = reference;
    }

    public int getLayerIndex() {
        return layerIndex;
    }

    public void setLayerIndex(int layerIndex) {
        this.layerIndex = layerIndex;
    }
}
