package com.sixsimplex.treasurehunt.revelocore.editprofile;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.upload.UploadFile;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.io.File;

public class GetProfilePicAsyncTask extends AsyncTask<String,String, JSONObject> {
    Activity activity;
    String userName;
//    ProgressDialog pDialog;
    ImageView imageView;
    String fileName="";
    public GetProfilePicAsyncTask(Activity activity, String userName, ImageView imageView){
        this.activity=activity;
        this.userName=userName;
        this.imageView=imageView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        pDialog = new ProgressDialog(activity);
//        pDialog.setMessage("Loading user profile...");
//        pDialog.setCancelable(false);
//
//        if (!activity.isFinishing()) {
//            pDialog.show();
//        }
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        File usernameFolder = new File(AppFolderStructure.userProfilePictureFolderPath(activity));
        usernameFolder.mkdir();
        ReveloLogger.debug("GetProfilePicAsyncTask","download profilepic","downloading...");

        fileName = UserInfoPreferenceUtility.getUserName()+"ProfilePic.png";
        UploadFile.downloadFile(UrlStore.getProfilePicUrl(),fileName,false,usernameFolder,activity,null);
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject responseJson) {
        super.onPostExecute(responseJson);
//        if (!activity.isFinishing() && pDialog.isShowing()) {
//            pDialog.dismiss();
//        }
        try {
            File profilepic = new File(AppFolderStructure.userProfilePictureFolderPath(activity) + File.separator + fileName);
            if (profilepic.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(profilepic.getAbsolutePath());
                if(myBitmap != null){
                imageView.setImageBitmap(myBitmap);
                }else{
                    Bitmap UserIconBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.user_icon);
                    imageView.setImageBitmap(UserIconBitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
