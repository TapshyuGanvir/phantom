package com.sixsimplex.treasurehunt.revelocore.geopackage.models;

public class RelationShipModel {

    private String name;
    private String fromCol;
    private String toCol;
    private String fromIdCol;
    private String toIdCol;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFromCol() {
        return fromCol;
    }

    public void setFromCol(String fromCol) {
        this.fromCol = fromCol;
    }

    public String getToCol() {
        return toCol;
    }

    public void setToCol(String toCol) {
        this.toCol = toCol;
    }

    public String getFromIdCol() {
        return fromIdCol;
    }

    public void setFromIdCol(String fromIdCol) {
        this.fromIdCol = fromIdCol;
    }

    public String getToIdCol() {
        return toIdCol;
    }

    public void setToIdCol(String toIdCol) {
        this.toIdCol = toIdCol;
    }
}
