package com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlowInteractionsModel {
    String flowInteractionModelName, entityName;
    List<Flow> flowList;


    public FlowInteractionsModel() {
    }

    public FlowInteractionsModel(String flowInteractionModelName, String entityName, List<Flow> flowList) {
        this.flowInteractionModelName = flowInteractionModelName;
        this.entityName = entityName;
        this.flowList = flowList;
    }

    public String getFlowInteractionModelName() {
        return flowInteractionModelName;
    }

    public void setFlowInteractionModelName(String flowInteractionModelName) {
        this.flowInteractionModelName = flowInteractionModelName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<Flow> getFlowList() {
        return flowList;
    }

    public Set<Flow> getFlows() {
        Set<Flow> set = new HashSet<Flow>(flowList);
        return set;
    }

    public void setFlowList(List<Flow> flowList) {
        this.flowList = flowList;
    }

}
