package com.sixsimplex.treasurehunt.Phantom1.app.model;

public class TraversalStateModel {
    String entityName;
    public enum TraversalState {NOT_STARTED, STARTED, FINISHED}
    public TraversalState traversalState;
    String lastVisitedFeatureId;

    public TraversalStateModel(String entityName, TraversalState traversalState, String featureId) {
        this.entityName = entityName;
        this.traversalState = traversalState;
        this.lastVisitedFeatureId = featureId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public TraversalState getTraversalState() {
        return traversalState;
    }

    public void setTraversalState(TraversalState traversalState) {
        this.traversalState = traversalState;
        if(traversalState==TraversalState.NOT_STARTED){
            setLastVisitedFeatureId("");
        }
    }

    public String getLastVisitedFeatureId() {
        if(traversalState==TraversalState.NOT_STARTED) {
            return "";
        }else {
            return lastVisitedFeatureId;
        }
    }

    public void setLastVisitedFeatureId(String lastVisitedFeatureId) {
        this.lastVisitedFeatureId = lastVisitedFeatureId;
        if(traversalState==TraversalState.NOT_STARTED){
            setTraversalState(TraversalState.STARTED);
        }
    }
}
