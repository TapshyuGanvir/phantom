package com.sixsimplex.treasurehunt.revelocore.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMRelation;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.ReveloConceptModel;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel.Flow;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel.Interaction;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel.InteractionLink;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMEdge;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.graph.flowinteractiongraph.FlowGraph;
import com.sixsimplex.treasurehunt.revelocore.graph.flowinteractiongraph.FlowInteractionEdge;
import com.sixsimplex.treasurehunt.revelocore.graph.jsongraph.JSONGraph;
import com.sixsimplex.treasurehunt.Phantom1.traversalgraph.TraversalGraph;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GraphFactory {
	/**
	 * Creates a Graph out of concept model
	 * @param conceptModel
	 * @param entityNameSuffix
	 * @return
	 * @throws JSONException 
	 */
	public static CMGraph createCMGraph(ReveloConceptModel conceptModel, String entityNameSuffix) throws JSONException {
		
		String conceptModelName = conceptModel.getName();
		CMGraph newGraph = new CMGraph(conceptModelName);
		Map<String, CMEntity> verticesMap = new HashMap<>();

		Set<CMEntity> allEntities = conceptModel.getEntities();
		Iterator<CMEntity> itEntities = allEntities.iterator();
		while (itEntities.hasNext()) {
			CMEntity entity = itEntities.next();
			newGraph.addVertex(entity);
			verticesMap.put(entity.getName(), entity);
		}
		
		//create edges
		Set<CMRelation> allRelations = conceptModel.getRelations();
		Iterator<CMRelation> itRelations = allRelations.iterator();
		while (itRelations.hasNext()) {
			CMRelation relation = itRelations.next();
		
			String fromEntityName = relation.getFrom() + entityNameSuffix;
			if(verticesMap.containsKey(fromEntityName)) {
				CMEntity fromEntity = verticesMap.get(fromEntityName);
				
				String toEntityName = relation.getTo() + entityNameSuffix;
				if(verticesMap.containsKey(toEntityName)) {
					CMEntity toEntity = verticesMap.get(toEntityName);
					CMEdge edge = new CMEdge(relation.getFromId(), fromEntityName, relation.getToId(), toEntityName);
					newGraph.addEdge(fromEntity, toEntity, edge.toJson());
				}
			}
		}
		return newGraph;	
	}

	/**
	 * Creates a Graph out of flows and interactions model
	 * @param flow
	 * @param entityNameSuffixf
	 * @return
	 * @throws JSONException
	 */
	public static FlowGraph createFlowGraph(Flow flow, String entityNameSuffix) throws JSONException {

		String flowName = flow.getFlowName();
		FlowGraph newGraph = new FlowGraph(flowName);
		Map<String, Interaction> verticesMap = new HashMap<>();

		Set<Interaction> allInteractions = flow.getInteractions();
		Iterator<Interaction> itInteraction = allInteractions.iterator();
		while (itInteraction.hasNext()) {
			Interaction interaction = itInteraction.next();
			newGraph.addVertex(interaction);
			verticesMap.put(interaction.getInteractionName(), interaction);
		}

		//create edges
		Set<InteractionLink> allInteractionLinks = flow.getInteractionLinks();
		Iterator<InteractionLink> itInteractionLinks = allInteractionLinks.iterator();
		while (itInteractionLinks.hasNext()) {
			InteractionLink interactionLink = itInteractionLinks.next();

			String fromInteractionName = interactionLink.getFrom() ;
			if(verticesMap.containsKey(fromInteractionName)) {
				Interaction fromInteraction = verticesMap.get(fromInteractionName);

				String toInteractionName = interactionLink.getTo();
				if(verticesMap.containsKey(toInteractionName)) {
					Interaction toInteraction = verticesMap.get(toInteractionName);
					FlowInteractionEdge edge = new FlowInteractionEdge(fromInteractionName, toInteractionName, interactionLink.getProperties());
					newGraph.addEdge(fromInteraction, toInteraction, edge.toJson());
				}
			}
		}
		return newGraph;
	}
/**
	 * Creates a Graph out of flows and interactions model
	 * @param //flow
	 * @param //entityNameSuffix
	 * @return
	 * @throws JSONException
	 */
	/*public static WhereClauseGraph createWhereClauseGraph(WhereClassModel whereClassModel) throws JSONException {

		String flowName = flow.getFlowName();
		FlowGraph newGraph = new FlowGraph(flowName);
		Map<String, Interaction> verticesMap = new HashMap<>();

		Set<Interaction> allInteractions = flow.getInteractions();
		Iterator<Interaction> itInteraction = allInteractions.iterator();
		while (itInteraction.hasNext()) {
			Interaction interaction = itInteraction.next();
			newGraph.addVertex(interaction);
			verticesMap.put(interaction.getInteractionName(), interaction);
		}

		//create edges
		Set<InteractionLink> allInteractionLinks = flow.getInteractionLinks();
		Iterator<InteractionLink> itInteractionLinks = allInteractionLinks.iterator();
		while (itInteractionLinks.hasNext()) {
			InteractionLink interactionLink = itInteractionLinks.next();

			String fromInteractionName = interactionLink.getFrom() ;
			if(verticesMap.containsKey(fromInteractionName)) {
				Interaction fromInteraction = verticesMap.get(fromInteractionName);

				String toInteractionName = interactionLink.getTo();
				if(verticesMap.containsKey(toInteractionName)) {
					Interaction toInteraction = verticesMap.get(toInteractionName);
					FlowInteractionEdge edge = new FlowInteractionEdge(fromInteractionName, toInteractionName, interactionLink.getProperties());
					newGraph.addEdge(fromInteraction, toInteraction, edge.toJson());
				}
			}
		}
		return newGraph;
	}
*/

	/**
	 * Creates AM graph
	 * @param analysisModel
	 * @return
	 * @throws JSONException
	 */
/*	public static AMGraph createAMGraph(ReveloAnalysisModel analysisModel) throws JSONException {
		
		String analysisModelName = analysisModel.getName();
		AMGraph newGraph = new AMGraph(analysisModelName);
		Map<String, AMTool> verticesMap = new HashMap<>();

		Set<AMTool> allTools = analysisModel.getTools();
		Iterator<AMTool> itTools = allTools.iterator();
		while (itTools.hasNext()) {
			AMTool tool = itTools.next();
			newGraph.addVertex(tool);
			verticesMap.put(Long.toString(tool.getAmToolId()), tool);
		}
		
		//create edges
		Set<AMLink> allLinks = analysisModel.getLinks();
		Iterator<AMLink> itLinks = allLinks.iterator();
		while (itLinks.hasNext()) {
			AMLink link = itLinks.next();		
			
			long fromToolId = link.getFromToolId();
			String fromToolIdStr = Long.toString(fromToolId);
			if(verticesMap.containsKey(fromToolIdStr)) {
				AMTool fromTool = verticesMap.get(fromToolIdStr);
			
				long toToolId = link.getToToolId();
				String toToolIdStr = Long.toString(toToolId);
				if(verticesMap.containsKey(toToolIdStr)) {
					AMTool toTool = verticesMap.get(toToolIdStr);						
					AMEdge edge = new AMEdge(fromToolId, toToolId);
					newGraph.addEdge(fromTool, toTool, edge.toJson());
				}
			}
		}
		return newGraph;	
	}*/
	
	/**
	 * 
	 * @param graphJSON
	 * @param //entityNameSuffix
	 * @return
	 * @throws JSONException
	 */
	public static JSONGraph createJSONGraph(JSONObject graphJSON, String idPropertyName) throws JSONException {
		if(idPropertyName == null || idPropertyName.trim().isEmpty()) {
			return null;	
		}
		
		JSONArray edgesArray = new JSONArray();
		if(!graphJSON.has("edges") && !graphJSON.has("relationships")) {
			return null;	
		}
		else {
			if(graphJSON.has("edges")) {
				edgesArray =  graphJSON.getJSONArray("edges");	
			}
			else if(graphJSON.has("relationships")) {
				edgesArray =  graphJSON.getJSONArray("relationships");	
			}
		}
		
		JSONObject verticesJSON = new JSONObject();
		if(!graphJSON.has("vertices") && !graphJSON.has("entities")) {
			return null;	
		}
		else {
			if(graphJSON.has("vertices")) {
				verticesJSON =  graphJSON.getJSONObject("vertices");	
			}
			else if(graphJSON.has("entities")) {
				verticesJSON =  graphJSON.getJSONObject("entities");	
			}
		}
		
		JSONGraph newGraph = new JSONGraph(idPropertyName);
		try {
			if(edgesArray.length() > 0) {
				for(int e = 0; e < edgesArray.length(); e++) {
					JSONObject edgeJSON = edgesArray.getJSONObject(e);				
					String fromNodeName = edgeJSON.getString("fromNodeName");
					if(verticesJSON.has(fromNodeName)) {
						String toNodeName = edgeJSON.getString("toNodeName");
						if(verticesJSON.has(toNodeName)) {
							JSONObject fromNode = verticesJSON.getJSONObject(fromNodeName);
							JSONObject toNode = verticesJSON.getJSONObject(toNodeName);
							newGraph.addVertex(fromNode);
							newGraph.addVertex(toNode);						
							
							JSONObject edgePropertiesJSON = new JSONObject();
							if(edgeJSON.has("properties")) {
								edgePropertiesJSON = edgeJSON.getJSONObject("properties");
							}
							newGraph.addEdge(fromNode, toNode, edgePropertiesJSON);
						}
					}				
				}
			}
			else {
				Iterator<?> itVerticeIds = verticesJSON.keys();
				while(itVerticeIds.hasNext()) {
					String vertexId = (String) itVerticeIds.next();
					JSONObject vertexNode = verticesJSON.getJSONObject(vertexId);
					newGraph.addVertex(vertexNode);
				}
			}				
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return newGraph;
	}



	public static TraversalGraph createTraversalGraph(JSONObject traversalJSON, String idPropertyName) throws JSONException {
		if(idPropertyName == null || idPropertyName.trim().isEmpty()) {
			return null;
		}
		if(!traversalJSON.has("graph") || !traversalJSON.has("properties") ){
			return null;
		}

		JSONObject graphJSON = null;
		try{
			graphJSON=traversalJSON.getJSONObject("graph");
		}catch (Exception e){
			e.printStackTrace();
		}

		if(graphJSON==null)
			return null;

		JSONArray edgesArray = new JSONArray();
		if(!graphJSON.has("edges") && !graphJSON.has("relationships")) {
			return null;
		}
		else {
			if(graphJSON.has("edges")) {
				edgesArray =  graphJSON.getJSONArray("edges");
			}
			else if(graphJSON.has("relationships")) {
				edgesArray =  graphJSON.getJSONArray("relationships");
			}
		}

		JSONObject nodeJSON = new JSONObject();
		if(!graphJSON.has("nodes")) {
			return null;
		}
		else {
			if(graphJSON.has("nodes")) {
				nodeJSON =  graphJSON.getJSONObject("nodes");
			}
		}

		JSONObject traversalPropertiesJson=null;
		try{
			traversalPropertiesJson=traversalJSON.getJSONObject("properties");
		}catch (Exception e){
			e.printStackTrace();
		}

		TraversalGraph newGraph = new TraversalGraph(idPropertyName,traversalPropertiesJson);
		try {
			if(edgesArray.length() > 0) {
				for(int e = 0; e < edgesArray.length(); e++) {
					JSONObject edgeJSON = edgesArray.getJSONObject(e);
					String fromNodeName = edgeJSON.getString("from");
					if(nodeJSON.has(fromNodeName)) {
						String toNodeName = edgeJSON.getString("to");
						if(nodeJSON.has(toNodeName)) {
							JSONObject fromNode = nodeJSON.getJSONObject(fromNodeName);
							String fromVertexId = fromNodeName;
							fromNode.put(idPropertyName,fromVertexId);
							newGraph.addVertex(fromNode);

							JSONObject toNode = nodeJSON.getJSONObject(toNodeName);
							String toVertexId = toNodeName;
							toNode.put(idPropertyName,toVertexId);
							newGraph.addVertex(toNode);

							JSONObject edgePropertiesJSON = new JSONObject();
							if(edgeJSON.has("properties")) {
								edgePropertiesJSON = edgeJSON.getJSONObject("properties");
							}
							newGraph.addEdge(fromNode, toNode, edgePropertiesJSON,fromNodeName,toNodeName);
						}
					}
				}
			}
			else {
				Iterator<?> itVerticeIds = nodeJSON.keys();
				while(itVerticeIds.hasNext()) {
					String vertexId = (String) itVerticeIds.next();
					JSONObject vertexNode = nodeJSON.getJSONObject(vertexId);
					vertexNode.put(idPropertyName,vertexId);
					newGraph.addVertex(vertexNode);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return newGraph;
	}
}
