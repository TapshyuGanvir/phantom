package com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.graph.ReveloAbstractGraph;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class CMGraph extends ReveloAbstractGraph<CMEntity, CMEdge> {

	private String conceptModelName = null;
	private static String className = "CMGraph";

	public CMGraph(String conceptModelName) {
		super(CMEdge.class);
		this.conceptModelName = conceptModelName;
	}	

	/**
	 * @return the conceptModelName
	 */
	public String getConceptModelName() {
		return this.conceptModelName;
	}
	
	@Override
	public void addEdge(CMEntity sourceVertex, CMEntity targetVertex, JSONObject properties) {
		try {	
			String fromPropertyName = properties.getString("fromParameterName");
			String toPropertyName = properties.getString("toParameterName");
			CMEdge edge = new CMEdge(fromPropertyName, sourceVertex.getName(), toPropertyName, targetVertex.getName());
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
			Set<CMEdge> edgesSet = this.nativeGraph.edgeSet();
			Iterator<CMEdge> edgesIt = edgesSet.iterator();
			while (edgesIt.hasNext()) {
				CMEdge userEdge = edgesIt.next();
				edgesArray.put(userEdge.toJson());				
			}
			graphJSON.put("edges", edgesArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return graphJSON;
	}

	@Override
	public List<CMEntity> getVertices(Map<String, Object> propertiesMap) {
		ReveloLogger.trace(className,"getVertices", "TimeLogs "+ "getVertices started - "+ SystemUtils.getCurrentDateTimeMiliSec());
		List<CMEntity> verticesList = new ArrayList<CMEntity>();
		try {
			Set<CMEntity> allVertices = this.nativeGraph.vertexSet();
			Iterator<CMEntity> itVertices = allVertices.iterator();
			while(itVertices.hasNext()) {
				CMEntity cmEntity = itVertices.next();
				JSONObject cmEntityJSON = cmEntity.toJson();
				if(!propertiesMap.isEmpty()) {
					boolean isMatching = true;
					for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
						String propertyName = entry.getKey();
						Object propertyValue = entry.getValue();
						if(cmEntityJSON.has(propertyName)) {
							isMatching &= propertyValue.equals(cmEntityJSON.get(propertyName));
						}
						else if(propertyName.equalsIgnoreCase("isReferenceEntity") && cmEntityJSON.has("miscProperties")) {
							//for cm entities, we should also look at miscProperties.isReferenceEntity
							JSONObject miscPropertiesJSON = cmEntityJSON.getJSONObject("miscProperties");
							isMatching &= (propertyValue.equals(miscPropertiesJSON.getBoolean("isReferenceEntity")));
						}
						else {
							//if the property does not exist in the entity, then its not a match
							isMatching = false;
						}
					}				
				
					if(isMatching) {
						verticesList.add(cmEntity);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ReveloLogger.trace(className,"getVertices", "TimeLogs "+ "getVertices ended - "+ SystemUtils.getCurrentDateTimeMiliSec());
		return verticesList;
	}

	public JSONObject getVertex(String propertyName, Object propertyValue){
		JSONObject resultJson = new JSONObject();
		ReveloLogger.trace(className,"getVertex", "TimeLogs "+ "getVertex propertyname = "+propertyName+", propertyvalue = "+propertyValue+" started - "+ SystemUtils.getCurrentDateTimeMiliSec());
		try {
			CMEntity requiredCmEntity = null;
			Set<CMEntity> allVertices = this.nativeGraph.vertexSet();
			Iterator<CMEntity> itVertices = allVertices.iterator();
			while(itVertices.hasNext()) {
				CMEntity cmEntity = itVertices.next();
				JSONObject cmEntityJSON = cmEntity.toJson();
				if(propertyName != null && !propertyName.isEmpty()) {
					boolean isMatching = true;
						if(cmEntityJSON.has(propertyName)) {
							isMatching &= propertyValue.equals(cmEntityJSON.get(propertyName));
						}
						else if(propertyName.equalsIgnoreCase("isReferenceEntity") && cmEntityJSON.has("miscProperties")) {
							//for cm entities, we should also look at miscProperties.isReferenceEntity
							JSONObject miscPropertiesJSON = cmEntityJSON.getJSONObject("miscProperties");
							isMatching &= (propertyValue.equals(miscPropertiesJSON.getBoolean("isReferenceEntity")));
						}
						else {
							//if the property does not exist in the entity, then its not a match
							isMatching = false;
						}


					if(isMatching) {
						requiredCmEntity=cmEntity;
						break;
					}
				}
			}

			if(requiredCmEntity==null){
				resultJson.put("status","failure");
				resultJson.put("message","Could not find entity.");
			}else {
				resultJson.put("status","success");
				resultJson.put("result",requiredCmEntity);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ReveloLogger.trace(className,"getVertex", "TimeLogs "+ "getVertex ended - "+ SystemUtils.getCurrentDateTimeMiliSec());
		return resultJson;
	}

	public JSONObject getVertex(String propertyName, Object propertyValue,String callingMethodName){
		JSONObject resultJson = new JSONObject();
		ReveloLogger.trace(className,"getVertex", "TimeLogs -"+ callingMethodName+" propertyname = "+propertyName+", propertyvalue = "+propertyValue+" started - "+ SystemUtils.getCurrentDateTimeMiliSec());
		try {
			CMEntity requiredCmEntity = null;
			Set<CMEntity> allVertices = this.nativeGraph.vertexSet();
			Iterator<CMEntity> itVertices = allVertices.iterator();
			while(itVertices.hasNext()) {
				CMEntity cmEntity = itVertices.next();
				JSONObject cmEntityJSON = cmEntity.toJson();
				if(propertyName != null && !propertyName.isEmpty()) {
					boolean isMatching = true;
						if(cmEntityJSON.has(propertyName)) {
							isMatching &= propertyValue.equals(cmEntityJSON.get(propertyName));
						}
						else if(propertyName.equalsIgnoreCase("isReferenceEntity") && cmEntityJSON.has("miscProperties")) {
							//for cm entities, we should also look at miscProperties.isReferenceEntity
							JSONObject miscPropertiesJSON = cmEntityJSON.getJSONObject("miscProperties");
							isMatching &= (propertyValue.equals(miscPropertiesJSON.getBoolean("isReferenceEntity")));
						}
						else {
							//if the property does not exist in the entity, then its not a match
							isMatching = false;
						}


					if(isMatching) {
						requiredCmEntity=cmEntity;
						break;
					}
				}
			}

			if(requiredCmEntity==null){
				resultJson.put("status","failure");
				resultJson.put("message","Could not find entity.");
			}else {
				resultJson.put("status","success");
				resultJson.put("result",requiredCmEntity);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ReveloLogger.trace(className,"getVertex", "TimeLogs "+ "getVertex ended - "+ SystemUtils.getCurrentDateTimeMiliSec());
		return resultJson;
	}

	@Override
	public Map<String, CMEntity> getAncestorNodesMap(CMEntity childVertex, String idFieldName) {
		Map<String, CMEntity> nodesMap = new HashMap<>();
		Set<CMEntity> verticesSet = this.nativeGraph.getAncestors(childVertex);
		Iterator<CMEntity> verticesIt = verticesSet.iterator();
		while (verticesIt.hasNext()) {
			CMEntity vertex = verticesIt.next();
			nodesMap.put(vertex.getName(), vertex);
		}		
		return nodesMap;
	}
	
	@Override
	public Map<String, CMEntity> getDescendantsNodesMap(CMEntity parentVertex, String idFieldName) {
		Map<String, CMEntity> nodesMap = new HashMap<>();
		Set<CMEntity> verticesSet = this.nativeGraph.getDescendants(parentVertex);
		Iterator<CMEntity> verticesIt = verticesSet.iterator();
		while (verticesIt.hasNext()) {
			CMEntity vertex = verticesIt.next();
			nodesMap.put(vertex.getName(), vertex);
		}		
		return nodesMap;
	}


}
