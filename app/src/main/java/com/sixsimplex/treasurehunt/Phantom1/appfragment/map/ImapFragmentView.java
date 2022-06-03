package com.sixsimplex.treasurehunt.Phantom1.appfragment.map;

import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;

import org.osmdroid.views.overlay.Overlay;

import java.util.LinkedHashMap;
import java.util.List;

public interface ImapFragmentView {
    void showFeaturesOnUI(LinkedHashMap<String, FeatureLayer> featureLayerLinkedHashMap, CMGraph cmGraph);

    void onGetSelectedFeatures(List<Overlay> features);
}
