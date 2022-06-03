package com.sixsimplex.treasurehunt.revelocore.upload;

public class UploadItemModel {
    String entityName;
    String entityLabel;
    String operationName;
    String status;
    String featureId;
    String featureLabel;
/*

    public UploadItemModel() {
        this.entityName = "";
        this.operationName = "";
        this.status = "";
        this.featureId = "";
        this.featureLabel = "";
    }
*/

    public UploadItemModel(String entityName,String entityLabel, String operationName, String status, String featureId, String featureLabel) {
        this.entityName = entityName;
        this.entityLabel = entityLabel;
        this.operationName = operationName;
        this.status = status;
        this.featureId = featureId;
        this.featureLabel = featureLabel;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getFeatureLabel() {
        return featureLabel;
    }

    public void setFeatureLabel(String featureLabel) {
        this.featureLabel = featureLabel;
    }
}
