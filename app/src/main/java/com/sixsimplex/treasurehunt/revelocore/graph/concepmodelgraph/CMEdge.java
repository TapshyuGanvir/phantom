package com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph;


import org.jgrapht.graph.DefaultEdge;
import org.json.JSONException;
import org.json.JSONObject;

public class CMEdge extends DefaultEdge {

	private static final long serialVersionUID = 1L;
	private String fromParameterName, toParameterName;
	private String fromEntityName, toEntityName;

	public CMEdge(String fromParamName, String fromEntityName, String toParamName, String toEntityName) {
		this.fromParameterName = fromParamName;
		this.fromEntityName = fromEntityName;
		this.toParameterName = toParamName;
		this.toEntityName = toEntityName;
	}

	public JSONObject toJson() {
		JSONObject edgeJSON = new JSONObject();
		try {
			edgeJSON.put("fromParameterName", this.fromParameterName);
			edgeJSON.put("fromEntityName", this.fromEntityName);
			edgeJSON.put("toParameterName", this.toParameterName);
			edgeJSON.put("toEntityName", this.toEntityName);
		}
		catch(JSONException e) {
			e.printStackTrace();
		}		
		return edgeJSON;
	}	

	@Override
	public String toString() {
		return this.toJson().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.fromEntityName == null) ? 0 : this.fromEntityName.hashCode());
		result = prime * result + ((this.fromParameterName == null) ? 0 : this.fromParameterName.hashCode());
		result = prime * result + ((this.toEntityName == null) ? 0 : this.toEntityName.hashCode());
		result = prime * result + ((this.toParameterName == null) ? 0 : this.toParameterName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CMEdge))
			return false;
		CMEdge other = (CMEdge) obj;
		if (this.fromEntityName == null) {
			if (other.fromEntityName != null)
				return false;
		} else if (!this.fromEntityName.equals(other.fromEntityName))
			return false;
		if (this.fromParameterName == null) {
			if (other.fromParameterName != null)
				return false;
		} else if (!this.fromParameterName.equals(other.fromParameterName))
			return false;
		if (this.toEntityName == null) {
			if (other.toEntityName != null)
				return false;
		} else if (!this.toEntityName.equals(other.toEntityName))
			return false;
		if (this.toParameterName == null) {
			if (other.toParameterName != null)
				return false;
		} else if (!this.toParameterName.equals(other.toParameterName))
			return false;
		return true;
	}

	/**
	 * @return the fromParameterName
	 */
	public String getFromParameterName() {
		return this.fromParameterName;
	}

	/**
	 * @return the toParameterName
	 */
	public String getToParameterName() {
		return this.toParameterName;
	}

	/**
	 * @param //fromParameterName the fromParameterName to set
	 */
	public void setFromParameterName(String fromParameter) {
		this.fromParameterName = fromParameter;
	}

	/**
	 * @param //toParameterName the toParameterName to set
	 */
	public void setToParameterName(String toParameter) {
		this.toParameterName = toParameter;
	}

	/**
	 * @return the fromNodeName
	 */
	public String getFromEntityName() {
		return this.fromEntityName;
	}

	/**
	 * @return the toNodeName
	 */
	public String getToEntityName() {
		return this.toEntityName;
	}

	/**
	 * @param //fromNodeName the fromNodeName to set
	 */
	public void setFromEntityName(String fromEntityName) {
		this.fromEntityName = fromEntityName;
	}

	/**
	 * @param //toNodeName the toNodeName to set
	 */
	public void setToEntityName(String toEntityName) {
		this.toEntityName = toEntityName;
	}
}
