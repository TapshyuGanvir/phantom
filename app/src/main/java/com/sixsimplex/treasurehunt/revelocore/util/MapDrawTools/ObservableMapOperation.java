package com.sixsimplex.treasurehunt.revelocore.util.MapDrawTools;

import java.io.Serializable;
import java.util.Observable;

public class ObservableMapOperation extends Observable implements Serializable {
    private static ObservableMapOperation Operation_INSTANCE = null;
    private String mainOperationName = "none";//add, edit
    private String geometryType = "";
    private boolean isPartSelected = false;
    private boolean isAddPartClicked = false;
    private boolean isDrawingOngoing = false;
    private boolean isGeometryValid = false;
    private boolean isUndoPossible = false;
    private boolean isRedoPossible = false;
    private boolean isBufferEnabled = false;


    private ObservableMapOperation() {
        setCurrentOperationState("none","", false,
                false, false,false, false,
                false, false);
    }

    public static ObservableMapOperation getInstance() {
        if (Operation_INSTANCE == null) {
            Operation_INSTANCE = new ObservableMapOperation();
        }
        return Operation_INSTANCE;
    }

    private void setCurrentOperationState(String mainOperationName,String geometryType,
                                          boolean isPartSelected,
                                          boolean isDrawingOngoing,boolean isAddPartClicked,
                                          boolean isGeometryValid, boolean isUndoPossible,
                                          boolean isRedoPossible,
                                          boolean isBufferEnabled) {
        this.mainOperationName = mainOperationName;
        this.geometryType=geometryType;
        this.isPartSelected = isPartSelected;
        this.isDrawingOngoing = isDrawingOngoing;
        this.isAddPartClicked = isAddPartClicked;
        this.isGeometryValid = isGeometryValid;
        this.isUndoPossible = isUndoPossible;
        this.isRedoPossible = isRedoPossible;
        this.isBufferEnabled = isBufferEnabled;
        setChanged();
        notifyObservers();
    }

    public String getMainOperationName() {
        return mainOperationName;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public void setMainOperationName(String mainOperationName,String geometryType) {
        this.mainOperationName = mainOperationName;
        this.geometryType = geometryType;
        setChanged();
        notifyObservers();
        if(mainOperationName!=null && mainOperationName.equalsIgnoreCase("none")){
            setCurrentOperationState("none",geometryType, false,
                    false, false,false, false,
                    false, false);
        }
    }

    public boolean isAddPartClicked() {
        return isAddPartClicked;
    }

    public void setAddPartClicked(boolean addPartClicked) {
        isAddPartClicked = addPartClicked;
        setChanged();
        notifyObservers();
    }

    public boolean isPartSelected() {
        return isPartSelected;
    }

    public void setPartSelected(boolean partSelected) {
        isPartSelected = partSelected;
        setChanged();
        notifyObservers();
    }

    public boolean isDrawingOngoing() {
        return isDrawingOngoing;
    }

    public void setDrawingOngoing(boolean drawingOngoing) {
        isDrawingOngoing = drawingOngoing;
        setChanged();
        notifyObservers();
    }

    public boolean isGeometryValid() {
        return isGeometryValid;
    }

    public void setGeometryValid(boolean geometryValid) {
        isGeometryValid = geometryValid;
        setChanged();
        notifyObservers();
    }

    public boolean isUndoPossible() {
        return isUndoPossible;
    }

    public void setUndoPossible(boolean undoPossible) {
        isUndoPossible = undoPossible;
        setChanged();
        notifyObservers();
    }

    public boolean isRedoPossible() {
        return isRedoPossible;
    }

    public void setRedoPossible(boolean redoPossible) {
        isRedoPossible = redoPossible;
        setChanged();
        notifyObservers();
    }

    public boolean isBufferEnabled() {
        return isBufferEnabled;
    }

    public void setBufferEnabled(boolean bufferEnabled) {
        isBufferEnabled = bufferEnabled;
        setChanged();
        notifyObservers();
    }
}
