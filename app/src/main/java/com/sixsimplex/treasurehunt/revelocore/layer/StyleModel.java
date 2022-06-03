package com.sixsimplex.treasurehunt.revelocore.layer;

public class StyleModel {
    private String fillColor;
    private String strokeColor;
    private int strokeWidth;
    private int points;
    private int radius;
    private String shape;
    private int otherNumPoints;
    private String path;

    public StyleModel() {
    }

    public StyleModel(String fillColor, String strokeColor, int strokeWidth, int points, int radius, String shape, int otherNumPoints, String path) {
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.points = points;
        this.radius = radius;
        this.shape = shape;
        this.otherNumPoints = otherNumPoints;
        this.path = path;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public int getOtherNumPoints() {
        return otherNumPoints;
    }

    public void setOtherNumPoints(int otherNumPoints) {
        this.otherNumPoints = otherNumPoints;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
