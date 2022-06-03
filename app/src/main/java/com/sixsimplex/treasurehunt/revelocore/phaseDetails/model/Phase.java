package com.sixsimplex.treasurehunt.revelocore.phaseDetails.model;

import java.util.HashMap;
import java.util.List;

public class Phase {
    private int index = 0;
   private String name, label;
   private List<String> roleNamesList;
   private List<String> allowedOperationsList;
   private HashMap<String,String> entityWhereClauseMap;
   private HashMap<String,String> entityFlowNameMap;

    public Phase(String name, String label, int index,List<String> roleNamesList,
                 List<String> allowedOperationsList,
                 HashMap<String,String> entityWhereClauseMap,HashMap<String,String> entityFlowNameMap) {
        this.name = name;
        this.label = label;
        this.index=index;
        this.roleNamesList = roleNamesList;
        this.allowedOperationsList = allowedOperationsList;
        this.entityWhereClauseMap = entityWhereClauseMap;
        this.entityFlowNameMap=entityFlowNameMap;
    }

    public int getIndex() {
        return index;
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

    public List<String> getRoleNamesList() {
        return roleNamesList;
    }

    public void setRoleNamesList(List<String> roleNamesList) {
        this.roleNamesList = roleNamesList;
    }

    public List<String> getAllowedOperationsList() {
        return allowedOperationsList;
    }
    public boolean isOperationAllowed(String soughtFor) {
        for (String current : allowedOperationsList) {
            if (current.equalsIgnoreCase(soughtFor)) {
                return true;
            } else if (soughtFor.equalsIgnoreCase("update")) {
                if (current.equalsIgnoreCase("updategeometry")
                        || current.equalsIgnoreCase("updateattributes")) {
                    return true;
                }
            }
        }
        return false;
    }
    public void setAllowedOperationsList(List<String> allowedOperationsList) {
        this.allowedOperationsList = allowedOperationsList;
    }

    public HashMap<String, String> getEntityFlowNameMap() {
        return entityFlowNameMap;
    }

    public HashMap<String, String> getEntityWhereClauseMap() {
        return entityWhereClauseMap;
    }

    public void setEntityWhereClauseMap(HashMap<String, String> entityWhereClauseMap) {
        this.entityWhereClauseMap = entityWhereClauseMap;
    }
}
