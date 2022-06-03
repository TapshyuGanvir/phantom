package com.sixsimplex.treasurehunt.revelocore.upload;

import android.app.ProgressDialog;

import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;

import org.json.JSONObject;

public interface IUploadHelper {

    void onPayLoad(JSONObject dataJson, CMGraph cmGraph,  ProgressDialog progressDialog);
    void onError(String errorMessage, ProgressDialog progressDialog);


}
