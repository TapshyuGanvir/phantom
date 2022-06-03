package com.sixsimplex.treasurehunt.revelocore.conceptModel.flowsinteractionmodel;

public class FlowInteractionRelation {
    private String name;
    private String from;
    private String to;
    private String fromId;
    private String toId;

    public FlowInteractionRelation(){}

    public FlowInteractionRelation(String name,
                                   String from,
                                   String to,
                                   String fromId,
                                   String toId) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.fromId = fromId;
        this.toId = toId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }
}
