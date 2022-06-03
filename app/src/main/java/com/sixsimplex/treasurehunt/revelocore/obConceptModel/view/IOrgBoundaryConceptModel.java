package com.sixsimplex.treasurehunt.revelocore.obConceptModel.view;

import com.sixsimplex.treasurehunt.revelocore.obConceptModel.model.OBDataModel;

public interface IOrgBoundaryConceptModel {

    void onSuccess(OBDataModel OBDataModel);

    void onError(String errorMsg);

}
