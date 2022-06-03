package com.sixsimplex.treasurehunt.revelocore.selectJurisdiction;

import com.tinkerpop.blueprints.Vertex;

import java.util.List;
import java.util.Map;

public interface ISelectJurisdictionValues {

    void j_spinnerUpperHierarchy(Map<String, Object> upperHierarchy, Vertex rootVertex);

    void j_spinnerLowerHierarchy(List<String> valueList, String nextSpinnerName);

    void j_onErrorSelectingJurisdiction(String message);

    void j_onCancelSelectingJurisdiction(String message);

}
