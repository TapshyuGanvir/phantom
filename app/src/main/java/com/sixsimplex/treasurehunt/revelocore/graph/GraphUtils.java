package com.sixsimplex.treasurehunt.revelocore.graph;


import com.sixsimplex.treasurehunt.revelocore.graph.jsongraph.JSONGraph;

import org.json.JSONException;
import org.json.JSONObject;

public class GraphUtils {
	/**
	 * Generates hierarchy info used by Web client
	 * @param userNode
	 * @param usersGraph
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject generateHierarchyInfo(JSONObject userNode, JSONGraph usersGraph) throws JSONException {
		JSONObject currentVertexJSON = new JSONObject();
		currentVertexJSON.put("name", userNode.getString("userName"));
		currentVertexJSON.put("role", userNode.getString("role"));

		JSONObject jurisdictionJSON = new JSONObject();
		jurisdictionJSON.put("name", userNode.getString("jurisdictionName"));
		jurisdictionJSON.put("type", userNode.getString("jurisdictionType"));

		currentVertexJSON.put("jurisdiction", jurisdictionJSON);
		
		JSONObject parentUserNode = usersGraph.getParent(userNode);
		if(parentUserNode != null) {
			JSONObject parentJSON = generateHierarchyInfo(parentUserNode, usersGraph);
			currentVertexJSON.put("parent", parentJSON);
		}

		return currentVertexJSON;
	}

	/**
	 * Get Reference Entity Vertex
	 * @param obcmGraph
	 * @param logger
	 * @return
	 */
	/*public static CMEntity_server getREVertex(CMGraph obcmGraph, ReveloLogger logger) {

		try {
			//find out Reference Entity			
			Set<CMEntity_server> allVerticesSet = obcmGraph.getAllVertices();
			Iterator<CMEntity_server> itEntities = allVerticesSet.iterator();
			while (itEntities.hasNext()) {
				CMEntity_server reVertex = itEntities.next();
				JSONObject miscPropertiesJSON = reVertex.getMiscProperties();
				//JSONObject miscPropertiesJSON = new JSONObject(new String(reVertex.getMiscProperties()));
				if (miscPropertiesJSON.getBoolean("isReferenceEntity")) {
					return reVertex;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}*/
}
