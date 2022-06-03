package com.sixsimplex.treasurehunt.revelocore.conceptModel;

import org.json.JSONObject;

public class CMRelation {
    /*
    * {
            "relationshipId": 27,
            "name": "hasboreholes",
            "label": "Grids has Boreholes",
            "type": "1-M",
            "from": "grids",
            "to": "boreholes",
            "fromId": "name",
            "toId": "gridno",
            "properties": {
                "childSpatialRelation": "contains"
            },
            "constraints": [],
            "conceptModel": "surveymeclcm"
        }*/

     //int relationshipId;
     String name;
    // String label;
    // String type;
     String from;
     String to;
     String fromId;
     String toId;
    // JSONObject  properties;
    // JSONArray constraints;
    // String conceptModel;
    public CMRelation(/*int relationshipId,*/ String name, /*String label, String type,*/ String from, String to,
                      String fromId, String toId/*, JSONObject properties, JSONArray constraints, String conceptModel*/){
      //  this.relationshipId=relationshipId;
        this.name=name;
       // this.label=label;
       // this.type=type;
        this.from=from;
        this.to=to;
        this.fromId=fromId;
        this.toId=toId;
       //this.properties=properties;
       //this.constraints=constraints;
       //this.conceptModel=conceptModel;
    }
    public CMRelation(JSONObject relationJSON){
        try {
           // this.relationshipId = relationJSON.getInt("relationshipId");
            this.name = relationJSON.getString("name");
          //  this.label = relationJSON.getString("label");
          //  this.type = relationJSON.getString("type");
            this.from = relationJSON.getString("from");
            this.to = relationJSON.getString("to");
            this.fromId = relationJSON.getString("fromId");
            this.toId = relationJSON.getString("toId");
           // this.properties = relationJSON.getJSONObject("properties");
           // this.constraints = relationJSON.optJSONArray("constraints");
           // this.conceptModel = relationJSON.getString("conceptModel");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "CMRelation{" +
              //  "relationshipId=" + relationshipId +
                " name='" + name + '\'' +
               // ", label='" + label + '\'' +
               // ", type='" + type + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", fromId='" + fromId + '\'' +
                ", toId='" + toId + '\'' +
               // ", properties=" + properties +
               // ", constraints=" + constraints +
               // ", conceptModel='" + conceptModel + '\'' +
                '}';
    }

    public JSONObject toJson(){
        JSONObject relations = new JSONObject();
        try {
           // relations.put("relationshipId",relationshipId);
            relations.put("name",name);
           //relations.put("label",label);
           //relations.put("type",type);
            relations.put("from",from);
            relations.put("to",to);
            relations.put("fromId",fromId);
            relations.put("toId",toId);
           // relations.put("properties",properties);
           // relations.put("constraints",constraints);
           // relations.put("conceptModel",conceptModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  relations;
    }

   /* public int getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(int relationshipId) {
        this.relationshipId = relationshipId;
    }
*/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

  /*  public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
*/
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

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    /*public JSONObject getProperties() {
        return properties;
    }

    public void setProperties(JSONObject properties) {
        this.properties = properties;
    }

    public JSONArray getConstraints() {
        return constraints;
    }

    public void setConstraints(JSONArray constraints) {
        this.constraints = constraints;
    }

    public String getConceptModel() {
        return conceptModel;
    }

    public void setConceptModel(String conceptModel) {
        this.conceptModel = conceptModel;
    }*/
}
