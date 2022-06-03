package com.sixsimplex.treasurehunt.revelocore.upload;

import org.json.JSONObject;

public interface IUpload {

    void showUploadResultDialog(JSONObject uploadResponseJsonObject,int requestCode);
    void onUploadResultDialogDismissed(int requestCode);

}
