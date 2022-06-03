package com.sixsimplex.treasurehunt.Phantom1.traversalgraph;

import com.sixsimplex.treasurehunt.revelocore.graph.jsongraph.JSONEdge;
import com.sixsimplex.treasurehunt.revelocore.graph.jsongraph.JSONGraph;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.jgrapht.traverse.TopologicalOrderIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TraversalGraph extends JSONGraph {
    private JSONObject propertiesJson = null;
    // private LinkedHashMap<String, TraversalFeature> idTraversalMap = new LinkedHashMap<>();
    private boolean isVisibleByDefault = true, isApprovalRequired = false;
    private String visibilityPropertyName = "", approvalPropertyName = "";
    private Object visibilityPropertyValue = "", approvalPropertyValue = "";
    private TraversalStatus isTraversalStarted = TraversalStatus.NOTSTARTED;


    public TraversalGraph(String idPropertyName, JSONObject propertiesJson) {
        super(idPropertyName);
        this.propertiesJson = propertiesJson;
        isTraversalStarted = TraversalStatus.NOTSTARTED;
        setProperties(propertiesJson);
        // generatew9idTraversalFeatureMap();
    }

    private void setProperties(JSONObject propertiesJson) {
        this.propertiesJson = propertiesJson;
        /*
        * "properties": {
			"visibility": {
				"comment": "if visibleByDefault = true, then show all the features on the map initially, else use visibility property name and value to decide to show the feature. This applies to all features in this entity.",
				"visibleByDefault": false,
				"visibilityPropertyName": "for ex: isVisible",
				"visibilityPropertyValue": "for ex: true or false"
			},
			"approval": {
				"comment": "if requiresApproval = true, then field user will communicate with Admin and Admin will update the approvalPropertyName property with value = 'approvalPropertyValue'. Server will communicate this edit to mobile using web sockets and mobile app UI will take cognizance of that and update itself. This applies to all features in this entity.",
				"required": true,
				"approvalPropertyName": "for ex: isApproved",
				"approvalPropertyValue": "for ex: true or false"
			}
		}*/
        try {
            if (propertiesJson == null) {
                isVisibleByDefault = true;
                isApprovalRequired = false;
            }
            else {
                if (propertiesJson.has("visibility")) {
                    JSONObject visibilityJson = propertiesJson.getJSONObject("visibility");
                    if (visibilityJson.has("visibleByDefault") && visibilityJson.getBoolean("visibleByDefault")) {
                        isVisibleByDefault = true;
                    }
                    else {
                        isVisibleByDefault = false;
                        if (visibilityJson.has("visibilityPropertyName")) {
                            visibilityPropertyName = visibilityJson.getString("visibilityPropertyName");
                        }
                        if (visibilityJson.has("visibilityPropertyValue")) {
                            visibilityPropertyValue = visibilityJson.getString("visibilityPropertyValue");
                        }
                    }
                }
                else {
                    isVisibleByDefault = true;
                }
                if (propertiesJson.has("approval")) {
                    JSONObject approvalJson = propertiesJson.getJSONObject("approval");
                    if (approvalJson.has("required") && approvalJson.getBoolean("required")) {
                        isApprovalRequired = true;
                        if (approvalJson.has("approvalPropertyName")) {
                            approvalPropertyName = approvalJson.getString("approvalPropertyName");
                        }
                        if (approvalJson.has("approvalPropertyValue")) {
                            approvalPropertyValue = approvalJson.getString("approvalPropertyValue");
                        }
                    }
                    else {
                        isApprovalRequired = false;
                    }
                }
                else {
                    isApprovalRequired = false;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject getCurrentlyTraversingFeature() {
        String w9id = "";
        JSONObject currentTreversingVertex = null;
       /* try {
            Map<String, Object> propertiesMap = new HashMap<>();
            propertiesMap.put("isVisited", true);
            LinkedList<JSONObject> verticesList  = getOrderedVerticesList(propertiesMap);
            if(verticesList!=null && verticesList.size()>0){
              JSONObject lastTraversedVertex =   verticesList.get(verticesList.size()-1);
              //this was last visited spot. now we find and return its child
              List<JSONObject> childrenList = getChildren(lastTraversedVertex);//ideally this list should be of size 1(1child) or zero(no child).
              if(childrenList==null||childrenList.size()!=1){
                  return "";
              }else {
                  JSONObject childVertx = childrenList.get(0);
                  w9id = childVertx.getString("w9id");
              }
            }else {
                //none of them were visited, give back the root.
                Set<JSONObject> rootVertices = getRootVertices();
                if(rootVertices==null||rootVertices.size()!=1){
                    return "";
                }else {
                    JSONObject rootVertex = rootVertices.iterator().next();
                    w9id = rootVertex.getString("w9id");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }*/
        try {
            Map<String, Object> propertiesMap = new HashMap<>();
            propertiesMap.put("isVisited", false);
            LinkedList<JSONObject> verticesList = getOrderedVerticesList(propertiesMap);
            if (verticesList != null && verticesList.size() > 0) {
                JSONObject lastTraversedVertex = verticesList.get(0);
                //this was first vertex in line after last visited spot or if no vertices were traversed, this is the root vertex.
                w9id = lastTraversedVertex.getString("w9id");
                currentTreversingVertex = lastTraversedVertex;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentTreversingVertex;
    }

    /**
     * Returns an ordered list of vertices that match properties listed in the input param
     *
     * @param propertiesMap
     * @return
     */
    public LinkedList<JSONObject> getOrderedVerticesList(Map<String, Object> propertiesMap) {
        LinkedList<JSONObject> verticesList = new LinkedList<>();
        try {
            /*if(propertiesMap!=null || propertiesMap.isEmpty()) {
                return verticesList;
            }*/

            TopologicalOrderIterator<JSONObject, JSONEdge> topoIterator = this.getTopoSortIterator();
            while (topoIterator.hasNext()) {
                JSONObject vertexJSON = topoIterator.next();
                if (propertiesMap == null || propertiesMap.isEmpty()) {
                    verticesList.add(vertexJSON);
                }
                else {
                    for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                        String propertyName = entry.getKey();
                        if (vertexJSON.has(propertyName)) {
                            Object propertyValue = entry.getValue();
                            if (propertyValue.equals(vertexJSON.get(propertyName))) {
                                verticesList.add(vertexJSON);
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return verticesList;
    }

    public TraversalStatus isTraversalStarted() {
        return isTraversalStarted;
    }
/*

    private void generatew9idTraversalFeatureMap() {
        idTraversalMap.clear();
        */
/*try {
            Set<JSONObject> rootVertices = getRootVertices();
            Iterator<JSONObject> rootItr = rootVertices.iterator();
            while (rootItr.hasNext()) {
                JSONObject rootVertex = rootItr.next();
                String id = rootVertex.getString(getIdPropertyName());
                String sequenceLabel = "1";
                TraversalFeature traversalFeature = new TraversalFeature(1,sequenceLabel,
                        TraversalFeature.VisitingStatus.NOTVISITED,id);
                idTraversalMap.put(id, traversalFeature);
                List<JSONObject> childrenVertices = getChildren(rootVertex);
                for (JSONObject childVertex : childrenVertices) {
                    generateTraversalFeature(childVertex, 2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*//*

        try{
            idTraversalMap = new LinkedHashMap<>();
            LinkedList<JSONObject> verticesList = new LinkedList<>();
            if(isVisibleByDefault) {
                verticesList = getOrderedVerticesList(null);
            }else {
                Map<String, Object> propertiesMap =new HashMap<>();
                propertiesMap.put("isVisited",true);
                verticesList = getOrderedVerticesList(propertiesMap);
            }
            if(verticesList!=null && verticesList.size()>0){
                int i=1;
                for(JSONObject vertexJson:verticesList){
                    TraversalFeature traversalFeature = new TraversalFeature(i,String.valueOf(i),
                            TraversalFeature.VisitingStatus.NOTVISITED,vertexJson.getString("w9id"));
                    idTraversalMap.put(vertexJson.getString("w9id"),traversalFeature);
                    i++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void generateTraversalFeature(JSONObject parentVertex, int parentSequenceNo) {
        if (idTraversalMap == null) {
            idTraversalMap = new LinkedHashMap<>();
        }
        try {
            String id = parentVertex.getString(getIdPropertyName());
            String sequenceLabel = String.valueOf(parentSequenceNo);
            TraversalFeature traversalFeature = new TraversalFeature(parentSequenceNo,sequenceLabel,
                    TraversalFeature.VisitingStatus.NOTVISITED,id);
            idTraversalMap.put(id, traversalFeature);
            List<JSONObject> childrenVertices = getChildren(parentVertex);
            for (JSONObject childVertex : childrenVertices) {
                generateTraversalFeature(childVertex, parentSequenceNo + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

    public void setTraversalStarted(TraversalStatus traversalStarted) {
        isTraversalStarted = traversalStarted;
    }

    public boolean isVisibleByDefault() {
        return isVisibleByDefault;
    }

    public void setVisibleByDefault(boolean visibleByDefault) {
        isVisibleByDefault = visibleByDefault;
    }

    public boolean isApprovalRequired() {
        return isApprovalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        isApprovalRequired = approvalRequired;
    }

    public String getVisibilityPropertyName() {
        return visibilityPropertyName;
    }

    public void setVisibilityPropertyName(String visibilityPropertyName) {
        this.visibilityPropertyName = visibilityPropertyName;
    }

    public String getApprovalPropertyName() {
        return approvalPropertyName;
    }

    public void setApprovalPropertyName(String approvalPropertyName) {
        this.approvalPropertyName = approvalPropertyName;
    }

    public Object getVisibilityPropertyValue() {
        return visibilityPropertyValue;
    }

    public void setVisibilityPropertyValue(Object visibilityPropertyValue) {
        this.visibilityPropertyValue = visibilityPropertyValue;
    }

    public Object getApprovalPropertyValue() {
        return approvalPropertyValue;
    }

    public void setApprovalPropertyValue(Object approvalPropertyValue) {
        this.approvalPropertyValue = approvalPropertyValue;
    }

    public HashMap<String, JSONObject> getCurrentlyVisibleVertices() {
        HashMap<String, JSONObject> currentlyVisibleVertices = new HashMap<>();
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("isVisible", true);
        try {
            List<JSONObject> verticesObj = getVertices(conditionMap);
            if (verticesObj != null && ! verticesObj.isEmpty()) {
                for (JSONObject vertex : verticesObj) {
                    String id = vertex.getString(getIdPropertyName());
                    currentlyVisibleVertices.put(id, vertex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return currentlyVisibleVertices;
    }

    @Override
    public String toString() {

        try {
            JSONObject traversalJobj = new JSONObject();
            JSONObject graphJobj = new JSONObject();
            traversalJobj.put("properties", getPropertiesJson());

            JSONObject nodesJobj = new JSONObject();
            Iterator<JSONObject> nodesItr = getAllVertices().iterator();
            while (nodesItr.hasNext()) {
                JSONObject node = nodesItr.next();
                nodesJobj.put(node.getString("w9id"), node);
            }

            JSONArray edgesJArray = new JSONArray();
            Iterator<JSONEdge> edgesItr = getAllEdges().iterator();
            while (edgesItr.hasNext()) {
                JSONEdge edge = edgesItr.next();
                JSONObject edgeJson = new JSONObject();
                edgeJson.put("from", edge.getFromNode().getString("w9id"));
                edgeJson.put("to", edge.getToNode().getString("w9id"));
                edgesJArray.put(edgeJson);
            }
            graphJobj.put("nodes", nodesJobj);
            graphJobj.put("edges", edgesJArray);
            traversalJobj.put("graph", graphJobj);
            return traversalJobj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return "TraversalGraph{" + "propertiesJson=" + propertiesJson + ", isVisibleByDefault=" + isVisibleByDefault + ", isApprovalRequired=" + isApprovalRequired + ", visibilityPropertyName='" + visibilityPropertyName + '\'' + ", approvalPropertyName='" + approvalPropertyName + '\'' + ", visibilityPropertyValue=" + visibilityPropertyValue + ", approvalPropertyValue=" + approvalPropertyValue + ", isTraversalStarted=" + isTraversalStarted + '}';
    }

    public JSONObject getPropertiesJson() {
        return propertiesJson;
    }
/*
    public String getSequenceLabel(String w9Id) {
        if (idTraversalMap == null || ! idTraversalMap.containsKey(w9Id)) {
            generatew9idTraversalFeatureMap();
        }
        if (idTraversalMap != null && ! idTraversalMap.isEmpty() && idTraversalMap.containsKey(w9Id)) {
            return idTraversalMap.get(w9Id).getSequenceLabel();
        }
        else {
            return "";
        }
    }

    public LinkedHashMap<String, TraversalFeature> getw9IdTraversalFeatureMap() {
        if (idTraversalMap == null) {
            generatew9idTraversalFeatureMap();
        }
        return idTraversalMap;
    }

    public void updatew9IdTraversalFeatureMap(LinkedHashMap<String, TraversalFeature> idTraversalMap) {
        if (idTraversalMap != null) {
            this.idTraversalMap = idTraversalMap;
        }
    }

    public boolean containsFeature(String w9Id) {
        if (idTraversalMap == null || ! idTraversalMap.containsKey(w9Id)) {
            generatew9idTraversalFeatureMap();
        }
       if(idTraversalMap!=null && idTraversalMap.containsKey(w9Id))
           return true;
       return false;
    }*/

    public void setPropertiesJson(JSONObject propertiesJson) {
        this.propertiesJson = propertiesJson;
    }

    @Override
    public boolean updateVertex(JSONObject vertex, JSONObject targetVertex) {
        try {
            if (nativeGraph.containsVertex(vertex)) {
                Map<String, Object> conditionMap = new HashMap<>();
                conditionMap.put("w9id", vertex.getString("w9id"));
                getVertices(conditionMap).get(0).put("isVisited", targetVertex.getBoolean("isVisited"));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONObject getuploadJson() {
        JSONObject traversalJson = null;
        try{
            traversalJson = new JSONObject();
            traversalJson.put("properties",getPropertiesJson());

            Set<JSONObject> vertices = getAllVertices();
            Set<JSONEdge> edges = getAllEdges();

            JSONObject nodesJson = new JSONObject();
            JSONArray edgesArray = new JSONArray();
            Iterator<JSONObject> vertexItr = vertices.iterator();
            Iterator<JSONEdge> edgesItr = edges.iterator();
            while (vertexItr.hasNext()){
                JSONObject nodeObj = new JSONObject();
                JSONObject vertexObj = vertexItr.next();
                nodeObj.put("isSkipable",vertexObj.getBoolean("isSkipable"));
                nodeObj.put("isVisited",vertexObj.getBoolean("isVisited"));
                nodesJson.put(vertexObj.getString("w9id"),nodeObj);
            }


            while (edgesItr.hasNext()){
                JSONObject edgeObj = new JSONObject();
                JSONEdge jsonEdge = edgesItr.next();
                edgeObj.put("from",jsonEdge.getFrom());
                edgeObj.put("to",jsonEdge.getTo());

                edgesArray.put(edgeObj);
            }

            JSONObject graphJobj = new JSONObject();
            graphJobj.put("nodes",nodesJson);
            graphJobj.put("edges",edgesArray);
            traversalJson.put("graph",graphJobj);


            ReveloLogger.info("TraversalGraph","jsonforupload","graph - "+traversalJson);
        }catch (Exception e){
            e.printStackTrace();
        }
        return traversalJson;
    }

    public enum TraversalStatus {STARTED, NOTSTARTED, PAUSED}
}
