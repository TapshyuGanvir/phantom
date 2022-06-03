package com.sixsimplex.treasurehunt.revelocore.principalEndpoint.view;

public interface IJurisdictionDownload {

    void onSuccess(String message);

    void onError(String message);

    void dismissDialog();
}
