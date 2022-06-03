package com.sixsimplex.treasurehunt.revelocore.principalEndpoint.view;

public interface IPrincipleEndpointView {

    //void onPrincipalEndPointSuccess(int request, ProgressDialog progressDialog);

    void onPrincipalEndPointSuccess(int request,boolean downloadRedb);

    void onPrincipalEndPointError(int request,String errorMessage);
}