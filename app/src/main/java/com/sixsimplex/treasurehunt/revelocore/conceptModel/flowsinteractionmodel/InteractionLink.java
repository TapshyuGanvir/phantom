package com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InteractionLink {
    /*
    * {
    "name": "flowlink1",
    "label": "Flow Interaction Link",
    "description": "Flow interaction link1",
    "from": "int1",
    "to": "int2",
    "properties": {}
}*/
    private String flowName;
    private String name;
    private String label;
    private String description;
    private String from;
    private String to;
    private List<String> properties;

    public InteractionLink(String name, String label, String description, String from, String to, List<String> properties) {
        this.name = name;
        this.label = label;
        this.description = description;
        this.from = from;
        this.to = to;
        this.properties = properties;
    }

    public InteractionLink() {
    }

    public InteractionLink(JSONObject interactionLinkJobj, String flowName) throws Exception{
        try {
            this.flowName = flowName;
            this.name = interactionLinkJobj.getString("name");
            this.label = interactionLinkJobj.getString("label");
            this.description = interactionLinkJobj.getString("description");
            this.from = interactionLinkJobj.getString("from");
            this.to = interactionLinkJobj.getString("to");
            JSONObject propertiesJobj = interactionLinkJobj.getJSONObject("properties");
            this.properties = new ArrayList<>();
            Iterator<String> iterator = propertiesJobj.keys();
            while (iterator.hasNext()){
                properties.add(iterator.next());
            }
        }catch (Exception e){
            e.printStackTrace();
            throw  e;
        }
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }
}
