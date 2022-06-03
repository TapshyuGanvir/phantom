package com.sixsimplex.treasurehunt.revelocore.principalEndpoint;

import android.app.Activity;
import android.os.AsyncTask;

import com.sixsimplex.treasurehunt.revelocore.upload.UploadFile;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.io.File;

public class DownloadLogoAsyncTask extends AsyncTask<String,String, JSONObject> {
    Activity activity;

    public DownloadLogoAsyncTask(Activity activity) {
        this.activity=activity;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {

        File orgNameFolder = new File(AppFolderStructure.orgLogoFolderPath(activity));
        orgNameFolder.mkdir();
        ReveloLogger.debug("organization logo downloading","download logo","downloading...");
        String fileName= UserInfoPreferenceUtility.getOrgName()+"AppLogo.png";
        UploadFile.downloadFile(UrlStore.getOrgLogoUrl(),fileName,false,orgNameFolder,activity,null);
        return null;
    }
}
