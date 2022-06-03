package com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Interaction {
    private String flowName, interactionName, label, description, roleName;
    private boolean isExecuted; //tells if this interaction is ready to go to next level. this must be set by some action of user
    private List<String> propertiesList, allowedOperationsList;

    public Interaction() {
    }

    public Interaction(String flowName, String interactionName, String label, String description, String roleName,
                       boolean isExecuted, List<String> propertiesList, List<String> allowedOperationsList) {
        this.flowName = flowName;
        this.interactionName = interactionName;
        this.label = label;
        this.description = description;
        this.roleName = roleName;
        this.isExecuted = isExecuted;
        this.propertiesList = propertiesList;
        this.allowedOperationsList = allowedOperationsList;
    }

    public Interaction(JSONObject interactionJObj, String flowName) throws Exception {

        try {
            this.flowName = flowName;
            this.interactionName = interactionJObj.getString("name");
            this.label = interactionJObj.getString("label");
            this.description = interactionJObj.getString("description");
            this.roleName = interactionJObj.getString("roleName");
            this.isExecuted = interactionJObj.getBoolean("isExecuted");
            JSONArray propJArray = interactionJObj.getJSONArray("propertiesList");
            propertiesList = new ArrayList<>();
            for (int i = 0; i < propJArray.length(); i++) {
                this.propertiesList.add(propJArray.getString(i));
            }
            JSONArray operJArray = interactionJObj.getJSONArray("allowedOperationsList");
            allowedOperationsList = new ArrayList<>();
            for (int i = 0; i < operJArray.length(); i++) {
                this.allowedOperationsList.add(operJArray.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getInteractionName() {
        return interactionName;
    }

    public void setInteractionName(String interactionName) {
        this.interactionName = interactionName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean isExecuted() {
        return isExecuted;
    }

    public void setExecuted(boolean executed) {
        isExecuted = executed;
    }

    public List<String> getPropertiesList() {
        return propertiesList;
    }

    public void setPropertiesList(List<String> propertiesList) {
        this.propertiesList = propertiesList;
    }

    public JSONArray getPropertiesJSONArray() {
        JSONArray propertiesJArray = new JSONArray();
        try {
            if (propertiesList != null) {
                for (String property : propertiesList) {
                    propertiesJArray.put(property);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return propertiesJArray;
    }

    public List<String> getAllowedOperationsList() {
        return allowedOperationsList;
    }

    public void setAllowedOperationsList(List<String> allowedOperationsList) {
        this.allowedOperationsList = allowedOperationsList;
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

    public JSONArray getAllowedOperationsJSONArray() {
        JSONArray operationsJArray = new JSONArray();
        try {
            if (allowedOperationsList != null) {
                for (String operation : allowedOperationsList) {
                    operationsJArray.put(operation);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return operationsJArray;
    }

    @Override
    public String toString() {
        return "Interaction{" +
                "flowName='" + flowName + '\'' +
                ", interactionName='" + interactionName + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", roleName='" + roleName + '\'' +
                ", isExecuted=" + isExecuted +
                ", propertiesList=" + propertiesList +
                ", allowedOperationsList=" + allowedOperationsList +
                '}';
    }

    public JSONObject toJson() {
        JSONObject entityJson = new JSONObject();
        try {
            entityJson.put("name", interactionName);
            entityJson.put("flowName", flowName);
            entityJson.put("label", label);
            entityJson.put("description", description);
            entityJson.put("roleName", roleName);
            entityJson.put("isExecuted", isExecuted);
            JSONArray propertiesJArray = new JSONArray();
            if (propertiesList != null) {
                for (String property : propertiesList) {
                    propertiesJArray.put(property);
                }
            }
            entityJson.put("propertiesList", propertiesJArray);

            JSONArray operationsJArray = new JSONArray();
            if (allowedOperationsList != null) {
                for (String operation : allowedOperationsList) {
                    operationsJArray.put(operation);
                }
            }
            entityJson.put("allowedOperationsList", operationsJArray);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return entityJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interaction)) return false;
        Interaction that = (Interaction) o;
        return isExecuted() == that.isExecuted() && getFlowName().equals(that.getFlowName()) && getInteractionName().equals(that.getInteractionName()) && getLabel().equals(that.getLabel()) && getDescription().equals(that.getDescription()) && getRoleName().equals(that.getRoleName()) && getPropertiesList().equals(that.getPropertiesList()) && getAllowedOperationsList().equals(that.getAllowedOperationsList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFlowName(), getInteractionName(), getLabel(), getDescription(), getRoleName(), isExecuted(), getPropertiesList(), getAllowedOperationsList());
    }

    /*
    * {
    "name": "interaction1",
    "label": "Default Flow Interaction",
    "description": "Default entity flow interaction",
    "roleName": "fg",
    "isExecuted": false,
    "propertiesList": ["p1","p2"],
    "allowedOperationsList": ["create","update","delete"]
}*/
    public interface FieldNames {
        String flowName = "flowName";
        String interactionName = "name";
        String label = "label";
        String description = "description";
        String roleName = "roleName";
        String isExecuted = "isExecuted";
        String propertiesList = "propertiesList";
        String allowedOperationsList = "allowedOperationsList";
    }
}
