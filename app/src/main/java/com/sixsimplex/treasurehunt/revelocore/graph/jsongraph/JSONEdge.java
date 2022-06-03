package com.sixsimplex.treasurehunt.revelocore.graph.jsongraph;

import org.jgrapht.graph.DefaultEdge;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONEdge extends DefaultEdge {

	private static final long serialVersionUID = 1L;
	private String idPropertyName = null,from="",to="";
	private JSONObject properties;
	private JSONObject fromNode, toNode;

	public JSONEdge(JSONObject fromNode, JSONObject toNode, String idPropertyName, JSONObject properties,String from,String to) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.idPropertyName = idPropertyName;
		this.properties = properties;
		this.from = from;
		this.to=to;
	}

	public JSONEdge(JSONObject fromNode, JSONObject toNode, String idPropertyName, JSONObject properties) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.idPropertyName = idPropertyName;
		this.properties = properties;
		this.from = "";
		this.to="=";
	}

	public JSONObject toJson() {
		JSONObject edgeJSON = new JSONObject();
		try {
			edgeJSON.put("fromNodeName", this.fromNode.getString(this.idPropertyName));
			edgeJSON.put("toNodeName", this.toNode.getString(this.idPropertyName));
			edgeJSON.put("properties", this.properties);
		}
		catch(JSONException e) {
			e.printStackTrace();
		}		
		return edgeJSON;
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

	@Override
	public String toString() {
		return this.toJson().toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fromNode == null) ? 0 : fromNode.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((toNode == null) ? 0 : toNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof JSONEdge))
			return false;
		JSONEdge other = (JSONEdge) obj;
		if (fromNode == null) {
			if (other.fromNode != null)
				return false;
		} else if (!fromNode.equals(other.fromNode))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (toNode == null) {
			if (other.toNode != null)
				return false;
		} else if (!toNode.equals(other.toNode))
			return false;
		return true;
	}

	/**
	 * @return the properties
	 */
	public JSONObject getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(JSONObject properties) {
		this.properties = properties;
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

	/**
	 * @return the fromNode
	 */
	public JSONObject getFromNode() {
		return fromNode;
	}

	/**
	 * @param fromNode the fromNode to set
	 */
	public void setFromNode(JSONObject fromNode) {
		this.fromNode = fromNode;
	}

	/**
	 * @return the toNode
	 */
	public JSONObject getToNode() {
		return this.toNode;
	}

	/**
	 * @param toNode the toNode to set
	 */
	public void setToNode(JSONObject toNode) {
		this.toNode = toNode;
	}
}
