package com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Flow {

    private String flowName, flowLabel, flowDescription, entityName;
    private List<Interaction> interactions;
    private List<InteractionLink> interactionLinks;

    public Flow(){}

    public Flow(String flowName, String flowLabel, String flowDescription, String entityName,
                List<Interaction> interactions, List<InteractionLink> interactionLinks) {
        this.flowName = flowName;
        this.flowLabel = flowLabel;
        this.flowDescription = flowDescription;
        this.entityName = entityName;
        this.interactions = interactions;
        this.interactionLinks = interactionLinks;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowLabel() {
        return flowLabel;
    }

    public void setFlowLabel(String flowLabel) {
        this.flowLabel = flowLabel;
    }

    public String getFlowDescription() {
        return flowDescription;
    }

    public void setFlowDescription(String flowDescription) {
        this.flowDescription = flowDescription;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

   /* public List<Interaction> getInteractions() {
        return interactions;
    }*/
    public Set<Interaction> getInteractions() {
        Set<Interaction> set = new HashSet<Interaction>(interactions);
        return set;
    }
    public void setInteractions(List<Interaction> interactions) {
        this.interactions = interactions;
    }

    /*public List<InteractionLink> getInteractionLinks() {
        return interactionLinks;
    }*/

    public Set<InteractionLink> getInteractionLinks() {
        Set<InteractionLink> set = new HashSet<InteractionLink>(interactionLinks);
        return set;
    }

    public void setInteractionLinks(List<InteractionLink> interactionLinks) {
        this.interactionLinks = interactionLinks;
    }
}
