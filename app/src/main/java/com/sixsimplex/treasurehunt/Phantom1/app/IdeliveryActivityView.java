package com.sixsimplex.treasurehunt.Phantom1.app;

import com.sixsimplex.treasurehunt.Phantom1.trip.ITripCallback;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;

public interface IdeliveryActivityView {
    void onTraversalDataFetchComplete();
    void performDeliveryActionForFeature(Feature targetFeature,String mode);
    void onTargetFeatureUpdated(String mode, int position);
    void updateHomeAndMapUI(int position);
    void showTripItemSelectionDialog(ITripCallback iTripCallback);
    void hideProgressDialog();
    void showProgressDialog(String progressText);

    void showProductsUpdateDialogForTargetFeature(Feature targetFeature);
}
