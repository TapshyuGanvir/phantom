package com.sixsimplex.treasurehunt.revelocore.conceptModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class           ReveloConceptModel {

    /*
    *
    "conceptModelId": 3,
    "name": "surveymeclcm",
    "label": "surveymeclcm",
    "ownerName": "meclproject",
    "ownerType": "survey",
    "datasourceName": "surveymeclds",
    "importMode": "single",
    "sourceCMNames": "meclorgcm",
    "domains": 2,
    "org": "mecl",
    "gisServerUrl": "http://103.248.60.18:9095/geoserver"
    "entities":[],
    "relations":[]
    * */


    private String name;
    private String gisServerUrl;
    private List<CMEntity> entities;
    private List<CMRelation> relations;


    public ReveloConceptModel(
            String name,
            String gisServerUrl,
            List<CMEntity> entities,
            List<CMRelation> relations) {
        this.name = name;
        this.gisServerUrl = gisServerUrl;
        this.entities = entities;
        this.relations = relations;
    }

    public ReveloConceptModel(
            String name,
            String gisServerUrl,
            JSONArray entitiesJArray,
            JSONArray relationsJArray) {
        this.name = name;
        this.gisServerUrl = gisServerUrl;
        try {
            for (int i = 0; i < entitiesJArray.length(); i++) {
                JSONObject entityJson = entitiesJArray.getJSONObject(i);
                CMEntity cmEntityServer = new CMEntity(entityJson);
                entities.add(cmEntityServer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < relationsJArray.length(); i++) {
                JSONObject relationJson = relationsJArray.getJSONObject(i);
                CMRelation cmRelation = new CMRelation(relationJson);
                relations.add(cmRelation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String getName() {
        return name;
    }

    public Set<CMEntity> getEntities() {
        Set<CMEntity> set = new HashSet<CMEntity>(entities);
        return set;
    }

    public Set<CMRelation> getRelations() {
        Set<CMRelation> set = new HashSet<CMRelation>(relations);
        return set;
    }


}
