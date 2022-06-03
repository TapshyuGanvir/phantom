package com.sixsimplex.treasurehunt.Phantom1.trip;

import com.sixsimplex.treasurehunt.revelocore.data.Feature;

public class TripDataModel {

    private static TripDataModel instance =instance =new TripDataModel();

    public static TripDataModel getInstance() {
        return instance;
    }

    private Boolean isTodayTripAdded=null;
    private Boolean isInventoryItemAddedForCurrentTrip=null;
    private Feature todayTripFeature=null;


    public Boolean getTodayTripAdded() {
        return isTodayTripAdded;
    }

    public void setTodayTripAdded(Boolean todayTripAdded) {
        isTodayTripAdded = todayTripAdded;
    }

    public Boolean getInventoryItemAddedForCurrentTrip() {
        return isInventoryItemAddedForCurrentTrip;
    }

    public void setInventoryItemAddedForCurrentTrip(Boolean inventoryItemAddedForCurrentTrip) {
        isInventoryItemAddedForCurrentTrip = inventoryItemAddedForCurrentTrip;
    }

    public Feature getTodayTripFeature() {
        return todayTripFeature;
    }

    public void setTodayTripFeature(Feature todayTripFeature) {
        this.todayTripFeature = todayTripFeature;
    }
}
