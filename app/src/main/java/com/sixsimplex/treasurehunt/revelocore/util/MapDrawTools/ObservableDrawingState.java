package com.sixsimplex.treasurehunt.revelocore.util.MapDrawTools;

import java.io.Serializable;
import java.util.Observable;

public class ObservableDrawingState extends Observable implements Serializable {

    private static ObservableDrawingState INSTANCE = null;

    private boolean isEditingOngoing;//if ongoing, hide add part. if not ongoing, show addpart. vice versa with done button
    private boolean isEditingStarted;// to decide if show anything among tick(done btn) and/or add part button. if actual editing is started then only show done/addpart

    private ObservableDrawingState(){
        setCurrentDrawingState(false,false);
    }

    public static ObservableDrawingState getInstance(){
        if(INSTANCE==null){
            INSTANCE = new ObservableDrawingState();
        }
        return INSTANCE;
    }

    public void setCurrentDrawingState(boolean isEditingStarted,boolean isEditingOngoing){
        this.isEditingOngoing = isEditingOngoing;
        this.isEditingStarted=isEditingStarted;
        setChanged();
        notifyObservers();
    }

    public boolean isEditingOngoing() {
        return isEditingOngoing;
    }

    public boolean isEditingStarted() {
        return isEditingStarted;
    }
}
