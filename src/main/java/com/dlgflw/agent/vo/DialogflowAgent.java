/**
 * 
 */
package com.dlgflw.agent.vo;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;

/**
 * @author SII068
 *
 */
@Getter
@Setter
public class DialogflowAgent {

	private JsonNode agentDetails;

	/**
	 * @return the agentDetails
	 */
	public JsonNode getAgentDetails() {
		return agentDetails;
	}

	/**
	 * @param agentDetails the agentDetails to set
	 */
	public void setAgentDetails(JsonNode agentDetails) {
		this.agentDetails = agentDetails;
	}
	
	

}
