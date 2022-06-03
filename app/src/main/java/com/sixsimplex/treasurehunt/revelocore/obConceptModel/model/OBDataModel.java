package com.sixsimplex.treasurehunt.revelocore.obConceptModel.model;

import java.util.List;

public class OBDataModel {

    private List<ObReEntity> obReEntityList;
    private List<ObReRelation> obReRelations;
    private String dataSourceName;
    private String gisServerUrl;

    public OBDataModel(){

    }

    public List<ObReEntity> getObReEntityList() {
        return obReEntityList;
    }

    public void setObReEntityList(List<ObReEntity> obReEntityList) {
        this.obReEntityList = obReEntityList;
    }

    public List<ObReRelation> getObReRelations() {
        return obReRelations;
    }

    public void setObReRelations(List<ObReRelation> obReRelations) {
        this.obReRelations = obReRelations;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getGisServerUrl() {
        return gisServerUrl;
    }

    public void setGisServerUrl(String gisServerUrl) {
        this.gisServerUrl = gisServerUrl;
    }

}
