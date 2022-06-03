package com.sixsimplex.treasurehunt.revelocore.graph.flowinteractiongraph;

import org.jgrapht.graph.DefaultEdge;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FlowInteractionEdge extends DefaultEdge {

    private static final long serialVersionUID = 1L;
    private String fromInteractionName, toInteractionName;
    private List<String> properties;

    public FlowInteractionEdge(String fromInteractionName, String toInteractionName, List<String> properties) {
        this.fromInteractionName = fromInteractionName;
        this.toInteractionName = toInteractionName;
        this.properties=properties;

    }

    public JSONObject toJson() {
        JSONObject edgeJSON = new JSONObject();
        try {
            edgeJSON.put("fromInteractionName", this.fromInteractionName);
            edgeJSON.put("toInteractionName", this.toInteractionName);
            JSONArray propertiesJArray = new JSONArray();
            if(properties!=null) {
                for (int i = 0; i < properties.size(); i++) {
                    propertiesJArray.put(properties.get(i));
                }
            }
            edgeJSON.put("properties", propertiesJArray);
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
        return edgeJSON;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.fromInteractionName == null) ? 0 : this.fromInteractionName.hashCode());
        result = prime * result + ((this.toInteractionName == null) ? 0 : this.toInteractionName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof FlowInteractionEdge))
            return false;
        FlowInteractionEdge other = (FlowInteractionEdge) obj;
        if (this.fromInteractionName == null) {
            if (other.fromInteractionName != null)
                return false;
        } else if (!this.fromInteractionName.equals(other.fromInteractionName))
            return false;
        /*if (this.fromParameterName == null) {
            if (other.fromParameterName != null)
                return false;
        } else if (!this.fromParameterName.equals(other.fromParameterName))
            return false;*/
        if (this.toInteractionName == null) {
            if (other.toInteractionName != null)
                return false;
        } else if (!this.toInteractionName.equals(other.toInteractionName))
            return false;
    /*    if (this.toParameterName == null) {
            if (other.toParameterName != null)
                return false;
        } else if (!this.toParameterName.equals(other.toParameterName))
            return false;*/
        return true;
    }

    public String getFromInteractionName() {
        return fromInteractionName;
    }

    public void setFromInteractionName(String fromInteractionName) {
        this.fromInteractionName = fromInteractionName;
    }

    public String getToInteractionName() {
        return toInteractionName;
    }

    public void setToInteractionName(String toInteractionName) {
        this.toInteractionName = toInteractionName;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }
}
