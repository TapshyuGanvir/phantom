package com.sixsimplex.treasurehunt.revelocore.geopackage.models;

import java.util.List;

public class DataSetInfo {

    private String name;
    private String type;
    private String geomType;
    private String idProperty;
    private List<ColumnInfo> columnList;

    public String getName() {
        return name;
    }

    public void setName(String dataSetName) {
        name = dataSetName;
    }

    public String getType() {
        return type;
    }

    public void setType(String dataSetType) {
        type = dataSetType;
    }

    public String getGeomType() {
        return geomType;
    }

    public void setGeomType(String dataSetGeomType) {
        geomType = dataSetGeomType;
    }

    public String getIdProperty() {
        return idProperty;
    }

    public void setIdProperty(String dataSetIdProperty) {
        idProperty = dataSetIdProperty;
    }

    public List<ColumnInfo> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ColumnInfo> columnList) {
        this.columnList = columnList;
    }
}
