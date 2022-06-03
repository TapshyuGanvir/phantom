package com.sixsimplex.treasurehunt.revelocore.graph.flowinteractiongraph;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel.Interaction;
import com.sixsimplex.treasurehunt.revelocore.graph.ReveloAbstractGraph;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
//graph of one flow. nodes- interactions. edges- interactionLinks
public class FlowGraph extends ReveloAbstractGraph<Interaction, FlowInteractionEdge> implements Serializable {
    String flowGraphName;
    public FlowGraph(String flowGraphName) {
        super(FlowInteractionEdge.class);
        this.flowGraphName=flowGraphName;
    }

    public String getFlowGraphName() {
        return flowGraphName;
    }

    @Override
    public void addEdge(Interaction sourceVertex, Interaction targetVertex, JSONObject properties) {
        try {
            String fromInteractionName = properties.getString("fromInteractionName");
            String toInteractionName = properties.getString("toInteractionName");
            List<String> propertiesList = new ArrayList<>();
            JSONArray propertiesJSONArray = properties.getJSONArray("properties");
            if(propertiesJSONArray!=null && propertiesJSONArray.length()>0) {
                for (int i = 0; i < propertiesJSONArray.length(); i++) {
                    propertiesList.add(propertiesJSONArray.getString(i));
                }
            }
            FlowInteractionEdge edge = new FlowInteractionEdge(fromInteractionName,  toInteractionName, propertiesList);
            this.nativeGraph.addEdge(sourceVertex, targetVertex, edge);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject graphJSON = new JSONObject();
        try {
            JSONArray edgesArray = new JSONArray();
            Set<FlowInteractionEdge> edgesSet = this.nativeGraph.edgeSet();
            Iterator<FlowInteractionEdge> edgesIt = edgesSet.iterator();
            while (edgesIt.hasNext()) {
                FlowInteractionEdge userEdge = edgesIt.next();
                edgesArray.put(userEdge.toJson());
            }
            graphJSON.put("edges", edgesArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return graphJSON;
    }

    @Override
    public List<Interaction> getVertices(Map<String, Object> propertiesMap) {
        List<Interaction> verticesList = new ArrayList<Interaction>();
        try {
            Set<Interaction> allVertices = this.nativeGraph.vertexSet();
            Iterator<Interaction> itVertices = allVertices.iterator();
            while(itVertices.hasNext()) {
                Interaction interaction = itVertices.next();
                JSONObject interactionJson = interaction.toJson();
                if(!propertiesMap.isEmpty()) {
                    boolean isMatching = true;
                    for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                        String propertyName = entry.getKey();
                        Object propertyValue = entry.getValue();
                        if(interactionJson.has(propertyName)) {
                            isMatching &= propertyValue.equals(interactionJson.get(propertyName));
                        }
                        else {
                            //if the property does not exist in the entity, then its not a match
                            isMatching = false;
                        }
                    }

                    if(isMatching) {
                        verticesList.add(interaction);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return verticesList;
    }

    public JSONObject getVertex(String propertyName, Object propertyValue){
        JSONObject resultJson = new JSONObject();
        try {
            Interaction requiredInteraction = null;
            Set<Interaction> allVertices = this.nativeGraph.vertexSet();
            Iterator<Interaction> itVertices = allVertices.iterator();
            while(itVertices.hasNext()) {
                Interaction interaction = itVertices.next();
                JSONObject interactionJson = interaction.toJson();
                if(propertyName != null && !propertyName.isEmpty()) {
                    boolean isMatching = true;
                    if(interactionJson.has(propertyName)) {
                        isMatching &= propertyValue.equals(interactionJson.get(propertyName));
                    } else {
                        //if the property does not exist in the entity, then its not a match
                        isMatching = false;
                    }


                    if(isMatching) {
                        requiredInteraction=interaction;
                        break;
                    }
                }
            }

            if(requiredInteraction==null){
                resultJson.put("status","failure");
                resultJson.put("message","Could not find interaction with "+propertyName+" as "+propertyValue);
            }else {
                resultJson.put("status","success");
                resultJson.put("result",requiredInteraction);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson;
    }

    @Override
    public Map<String, Interaction> getAncestorNodesMap(Interaction childInteraction, String idFieldName) {
        Map<String, Interaction> nodesMap = new HashMap<>();
        Set<Interaction> verticesSet = this.nativeGraph.getAncestors(childInteraction);
        Iterator<Interaction> verticesIt = verticesSet.iterator();
        while (verticesIt.hasNext()) {
            Interaction vertex = verticesIt.next();
            nodesMap.put(vertex.getInteractionName(), vertex);
        }
        return nodesMap;
    }

    @Override
    public Map<String, Interaction> getDescendantsNodesMap(Interaction parentVertex, String idFieldName) {
        Map<String, Interaction> nodesMap = new HashMap<>();
        Set<Interaction> verticesSet = this.nativeGraph.getDescendants(parentVertex);
        Iterator<Interaction> verticesIt = verticesSet.iterator();
        while (verticesIt.hasNext()) {
            Interaction vertex = verticesIt.next();
            nodesMap.put(vertex.getInteractionName(), vertex);
        }
        return nodesMap;
    }


}
