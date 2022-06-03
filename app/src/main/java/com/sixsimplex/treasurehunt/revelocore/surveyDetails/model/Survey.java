package com.sixsimplex.treasurehunt.revelocore.surveyDetails.model;

import com.sixsimplex.treasurehunt.revelocore.phaseDetails.model.Phase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Survey {

    HashMap<String, Phase> phasesInSurvey_Map = null;//for easy access
    List<Phase> phasesInSurvey_List = new ArrayList<>();//for easy access
    private int id;
    private String name;
    private String label;
    private String conceptModelName;
    private int bufferDistance;
    private boolean hasPhases;
    private String phasesJsonString;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getConceptModelName() {
        return conceptModelName;
    }

    public void setConceptModelName(String conceptModelName) {
        this.conceptModelName = conceptModelName;
    }

    public int getBufferDistance() {
        return bufferDistance;
    }

    public void setBufferDistance(int bufferDistance) {
        this.bufferDistance = bufferDistance;
    }

    public String getPhasesJsonString() {
        return phasesJsonString;
    }

    public void setPhasesJsonString(String phasesJsonString) {
        this.phasesJsonString = phasesJsonString;
    }

    public List<Phase> getPhasesFromJson() {

        try {
            if (phasesInSurvey_List == null) {
                phasesInSurvey_List = new ArrayList<>();

                JSONObject allPhasesJson = new JSONObject(getPhasesJsonString());
                Iterator<String> phaseNameItr = allPhasesJson.keys();
                int index = 0;
                while (phaseNameItr.hasNext()) {
                    index++;
                    String phaseName = phaseNameItr.next();
                    JSONObject phaseJson = allPhasesJson.getJSONObject(phaseName);

                    String label = phaseJson.has("label") ? phaseJson.getString("label") : phaseName;
                    int phaseindex = phaseJson.has("index") ? phaseJson.getInt("index") : index;

                    JSONArray roleNamesListJArray = phaseJson.has("roleNamesList") ? phaseJson.getJSONArray("roleNamesList") : new JSONArray();
                    List<String> roleNamesList = new ArrayList<>();
                    for (int i = 0; i < roleNamesListJArray.length(); i++) {
                        roleNamesList.add(roleNamesListJArray.getString(i));
                    }

                    JSONArray allowedOperationsListJArray = phaseJson.has("allowedOperationsList") ? phaseJson.getJSONArray("allowedOperationsList") : new JSONArray();
                    List<String> allowedOperationsList = new ArrayList<>();
                    for (int i = 0; i < allowedOperationsListJArray.length(); i++) {
                        allowedOperationsList.add(allowedOperationsListJArray.getString(i));
                    }
                    HashMap<String, String> entityWhereclauseMap = new HashMap<>();
                    HashMap<String, String> entityFlowNameMap = new HashMap<>();
                    if (allPhasesJson.has("entities")) {
                        JSONObject allEntitiesJObj = allPhasesJson.getJSONObject("entities");
                        Iterator<String> entityNameItr = allEntitiesJObj.keys();
                        while (entityNameItr.hasNext()) {
                            String entityName = entityNameItr.next();
                            JSONObject entityJson = allEntitiesJObj.getJSONObject(entityName);
                            if (entityJson.has("whereClause")) {
                                JSONObject whereclauseJson = entityJson.getJSONObject("whereClause");
                                entityWhereclauseMap.put(entityName, whereclauseJson.toString());
                            } else {
                                entityWhereclauseMap.put(entityName, "");
                            }
                            if (entityJson.has("flowName")) {
                                String flowName = entityJson.getString("flowName");
                                entityFlowNameMap.put(entityName, flowName);
                            } else {
                                entityFlowNameMap.put(entityName, "");
                            }
                        }
                    }
                    Phase phase = new Phase(phaseName, label, phaseindex, roleNamesList, allowedOperationsList, entityWhereclauseMap, entityFlowNameMap);
                    phasesInSurvey_List.add(phase);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phasesInSurvey_List;
    }

    public HashMap<String, Phase> getPhasesNameMapFromJson() {
        try {
            if (phasesInSurvey_Map == null) {
                phasesInSurvey_Map = new HashMap<>();
                JSONObject allPhasesJson = new JSONObject(getPhasesJsonString());
                Iterator<String> phaseNameItr = allPhasesJson.keys();
                int index = 0;
                while (phaseNameItr.hasNext()) {
                    index++;
                    String phaseName = phaseNameItr.next();
                    JSONObject phaseJson = allPhasesJson.getJSONObject(phaseName);

                    String label = phaseJson.has("label") ? phaseJson.getString("label") : phaseName;
                    int phaseindex = phaseJson.has("index") ? phaseJson.getInt("index") : index;

                    JSONArray roleNamesListJArray = phaseJson.has("roleNamesList") ? phaseJson.getJSONArray("roleNamesList") : new JSONArray();
                    List<String> roleNamesList = new ArrayList<>();
                    for (int i = 0; i < roleNamesListJArray.length(); i++) {
                        roleNamesList.add(roleNamesListJArray.getString(i));
                    }

                    JSONArray allowedOperationsListJArray = phaseJson.has("allowedOperationsList") ? phaseJson.getJSONArray("allowedOperationsList") : new JSONArray();
                    List<String> allowedOperationsList = new ArrayList<>();
                    for (int i = 0; i < allowedOperationsListJArray.length(); i++) {
                        allowedOperationsList.add(allowedOperationsListJArray.getString(i));
                    }
                    HashMap<String, String> entityWhereclauseMap = new HashMap<>();
                    HashMap<String, String> entityFlowNameMap = new HashMap<>();
                    if (phaseJson.has("entities")) {
                        JSONObject allEntitiesJObj = phaseJson.getJSONObject("entities");
                        Iterator<String> entityNameItr = allEntitiesJObj.keys();
                        while (entityNameItr.hasNext()) {
                            String entityName = entityNameItr.next();
                            JSONObject entityJson = allEntitiesJObj.getJSONObject(entityName);
                            if (entityJson.has("whereClause")) {
                                JSONObject whereclauseJson = entityJson.getJSONObject("whereClause");
                                entityWhereclauseMap.put(entityName, whereclauseJson.toString());
                            } else {
                                entityWhereclauseMap.put(entityName, "");
                            }
                            if (entityJson.has("flowName")) {
                                String flowName = entityJson.getString("flowName");
                                entityFlowNameMap.put(entityName, flowName);
                            } else {
                                entityFlowNameMap.put(entityName, "");
                            }
                        }
                    }
                    Phase phase = new Phase(phaseName, label, phaseindex, roleNamesList, allowedOperationsList, entityWhereclauseMap, entityFlowNameMap);
                    phasesInSurvey_Map.put(phaseName, phase);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phasesInSurvey_Map;
    }

    public boolean hasPhases() {
        return hasPhases;
    }

    public void setHasPhases(boolean hasPhases) {
        this.hasPhases = hasPhases;
    }
}


