package com.sixsimplex.treasurehunt.revelocore.graph.jsongraph;

import com.sixsimplex.treasurehunt.revelocore.graph.ReveloAbstractGraph;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class JSONGraph extends ReveloAbstractGraph<JSONObject, JSONEdge> {

	private String idPropertyName = null;

	public JSONGraph(String idPropertyName) {
		super(JSONEdge.class);
		this.idPropertyName = idPropertyName;
	}

	/**
	 * @return the idPropertyName
	 */
	public String getIdPropertyName() {
		return idPropertyName;
	}

	/**
	 * @param idPropertyName the idPropertyName to set
	 */
	public void setIdPropertyName(String idPropertyName) {
		this.idPropertyName = idPropertyName;
	}

	@Override
	public void addEdge(JSONObject sourceVertex, JSONObject targetVertex, JSONObject properties) {
		JSONEdge edge = new JSONEdge(sourceVertex, targetVertex, this.idPropertyName, properties);
		this.nativeGraph.addEdge(sourceVertex, targetVertex, edge);
	}

	@Override
	public void addEdge(JSONObject sourceVertex, JSONObject targetVertex, JSONObject properties,String from ,String to) {
		JSONEdge edge = new JSONEdge(sourceVertex, targetVertex, this.idPropertyName, properties,from,to);
		this.nativeGraph.addEdge(sourceVertex, targetVertex, edge);
	}

	@Override
	public JSONObject toJson() {

		JSONObject graphJSON = new JSONObject();
		try {			
			JSONObject verticesJSON = new JSONObject();
			Set<JSONObject> verticesSet = this.nativeGraph.vertexSet();
			Iterator<JSONObject> verticesIt = verticesSet.iterator();
			while (verticesIt.hasNext()) {
				JSONObject nodeJSON = verticesIt.next();
				verticesJSON.put(nodeJSON.getString(this.idPropertyName), nodeJSON);
			}			
			graphJSON.put("vertices", verticesJSON);
			
			JSONArray edgesArray = new JSONArray();
			Set<JSONEdge> edgesSet = this.nativeGraph.edgeSet();
			Iterator<JSONEdge> edgesIt = edgesSet.iterator();
			while (edgesIt.hasNext()) {
				JSONEdge userEdge = edgesIt.next();
				edgesArray.put(userEdge.toJson());				
			}
			graphJSON.put("edges", edgesArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return graphJSON;
	}

	@Override
	public List<JSONObject> getVertices(Map<String, Object> propertiesMap) {		
		List<JSONObject> verticesList = new ArrayList<JSONObject>();		
		try {
			Set<JSONObject> allVertices = this.nativeGraph.vertexSet();
			Iterator<JSONObject> itVertices = allVertices.iterator();
			while(itVertices.hasNext()) {
				JSONObject nodeJSON = itVertices.next();								
				if(!propertiesMap.isEmpty()) {
					boolean isMatching = true;
					for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
						String propertyName = entry.getKey();
						if(nodeJSON.has(propertyName)) {							
							Object propertyValue = entry.getValue();
							isMatching &= propertyValue.equals(nodeJSON.get(propertyName));
						}
					}				
				
					if(isMatching) {
						verticesList.add(nodeJSON);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return verticesList;
	}

	@Override
	public Map<String, JSONObject> getAncestorNodesMap(JSONObject childVertex, String idFieldName) {		
		Map<String, JSONObject> nodesMap = new HashMap<>();
		Set<JSONObject> verticesSet = this.nativeGraph.getAncestors(childVertex);
		Iterator<JSONObject> verticesIt = verticesSet.iterator();
		while (verticesIt.hasNext()) {
			JSONObject vertex = verticesIt.next();
			try {
				nodesMap.put(vertex.getString(idFieldName), vertex);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}		
		return nodesMap;
	}
	
	@Override
	public Map<String, JSONObject> getDescendantsNodesMap(JSONObject parentVertex, String idFieldName) {		
		Map<String, JSONObject> nodesMap = new HashMap<>();
		Set<JSONObject> verticesSet = this.nativeGraph.getDescendants(parentVertex);
		Iterator<JSONObject> verticesIt = verticesSet.iterator();
		while (verticesIt.hasNext()) {
			JSONObject vertex = verticesIt.next();
			try {
				nodesMap.put(vertex.getString(idFieldName), vertex);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}		
		return nodesMap;
	}
}
