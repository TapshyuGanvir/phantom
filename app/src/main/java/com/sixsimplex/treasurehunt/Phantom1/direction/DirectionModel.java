package com.sixsimplex.treasurehunt.Phantom1.direction;

import java.io.Serializable;

public class DirectionModel implements Serializable {
    String instruction;
    int index;
    Double distance;
    int type;

    public String getInstruction() {
        return instruction;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public Double getDistance() {
        return distance;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}

