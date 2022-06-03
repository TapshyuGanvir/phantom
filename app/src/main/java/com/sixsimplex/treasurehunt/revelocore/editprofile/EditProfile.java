package com.sixsimplex.treasurehunt.revelocore.editprofile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.sixsimplex.treasurehunt.R;

import com.sixsimplex.treasurehunt.revelocore.upload.UploadFile;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditProfile extends AppCompatActivity {
    private String className = "EditProfile";
    private final int IMAGE_REQUEST = 123;
    String firstName, lastName, phoneNumber;
    EditText firstNameEt, lastNameEt, phoneNumberEt,userNameEt;
    ImageView profilePicIv,editIv;
    Button btn_update;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit profile");
        init();
        setOriginalValues();
    }

    private void init(){
        firstNameEt = findViewById(R.id.firstNameEt);
        lastNameEt = findViewById(R.id.lastNameEt);
        phoneNumberEt= findViewById(R.id.phoneNumberEt);
        userNameEt=findViewById(R.id.userNameEt);
        userNameEt.setEnabled(false);


        profilePicIv = findViewById(R.id.profilePic);
        editIv = findViewById(R.id.editIv);
        editIv.setClickable(true);
        editIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfilePic();
            }
        });
        btn_update = findViewById(R.id.btn_update);

        liveValidation();
    }

    private void liveValidation() {
        firstNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.toString().trim().length()>25){
                        firstNameEt.setError("Name character length should be less than 25");
                    }
                    if(!s.toString().trim().matches("^[A-Za-z ]+$")){
                        firstNameEt.setError("Please enter valid name");
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lastNameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()>25){
                    lastNameEt.setError("Name character length should be less than 25");
                }
                if(!s.toString().trim().matches("^[A-Za-z ]+$")){
                    lastNameEt.setError("Please enter valid name");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void editProfilePic() {
        ImagePicker.Companion.with(this)
                .crop()//Crop image(Optional), Check Customization for more option
                .compress(200)   //Final image size will be less than 1 MB(Optional)
                .maxResultSize(200, 200) //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }
    private void setOriginalValues() {
        firstNameEt.setText(UserInfoPreferenceUtility.getFirstName());
        lastNameEt.setText(UserInfoPreferenceUtility.getLastName());
        phoneNumberEt.setText(UserInfoPreferenceUtility.getPhoneNumber());
        userNameEt.setText(UserInfoPreferenceUtility.getUserName());
        String fileName = UserInfoPreferenceUtility.getUserName()+"ProfilePic.png";
        File profilepic = new File(AppFolderStructure.userProfilePictureFolderPath(this) + File.separator + fileName);
        if(profilepic.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(profilepic.getAbsolutePath());
            if(myBitmap != null){
            profilePicIv.setImageBitmap(myBitmap);
            }else{
                Bitmap UserIconBitmap = BitmapFactory.decodeResource(EditProfile.this.getResources(), R.drawable.user_icon);
                profilePicIv.setImageBitmap(UserIconBitmap);
            }
        }
    }
    public void updateProfile(View view) {


        if(firstNameEt.getText() == null || firstNameEt.getText().toString().trim().isEmpty()){
            firstNameEt.setError("Please enter first name");
            return;
        }else if(firstNameEt.getText().toString().trim().length()>25){
            firstNameEt.setError("Name character length should be less than 25");
            return;
        }else if(!firstNameEt.getText().toString().trim().matches("^[A-Za-z ]+$")){
            firstNameEt.setError("Please enter valid name");
            return;
        }

        if(lastNameEt.getText() == null || lastNameEt.getText().toString().trim().isEmpty()){
            lastNameEt.setError("Please enter last name");
            return;
        }else if(lastNameEt.getText().toString().trim().length()>25){
            lastNameEt.setError("Name character length should be less than 25");
            return;
        }else if(!lastNameEt.getText().toString().trim().matches("^[A-Za-z ]+$")){
            lastNameEt.setError("Please enter valid name");
            return;
        }

        if(phoneNumberEt.getText() == null || phoneNumberEt.getText().toString().trim().isEmpty()){
            phoneNumberEt.setError("Please enter phone number");
            return;
        }else if(!(phoneNumberEt.getText().toString().trim().length() == 10)){
            phoneNumberEt.setError("Please enter valid phone number");
            return;
        }

        firstName = firstNameEt.getText().toString().trim();
        lastName = lastNameEt.getText().toString().trim();
        phoneNumber = phoneNumberEt.getText().toString().trim();

        new UpdateProfileTask().execute();



//        boolean isDataValid = validateValues();
//        if(!isDataValid){
//            editIv.setClickable(true);
//            SystemUtils.showOkDialogBox("Invalid data! Please add data again.",this);
//        }else {
//            editIv.setClickable(false);
//            new UpdateProfileTask().execute();
//        }
    }
    private boolean validateValues() {
        boolean isDataValid = false;
        if(firstNameEt.getText()!=null && !firstNameEt.getText().toString().isEmpty()){
            firstName = firstNameEt.getText().toString();
        }else {
            firstName = UserInfoPreferenceUtility.getFirstName();
        }
        isDataValid=true;

        if(lastNameEt.getText()!=null && !lastNameEt.getText().toString().isEmpty()){
            lastName = lastNameEt.getText().toString();
        }else {
            lastName = UserInfoPreferenceUtility.getLastName();
        }
        isDataValid=true;

        if(phoneNumberEt.getText()!=null && !phoneNumberEt.getText().toString().isEmpty()){
            phoneNumber = phoneNumberEt.getText().toString();
        }else {
            phoneNumber = UserInfoPreferenceUtility.getPhoneNumber();
        }
        isDataValid=true;
        try{
            //to get the image from the ImageView (say iv)
            BitmapDrawable draw = (BitmapDrawable) profilePicIv.getDrawable();
           /* Bitmap bitmap = draw.getBitmap();

            FileOutputStream outStream = null;
            File dir = new File(SystemUtils.userNameFolderPath());
            dir.mkdirs();
            String fileName = ReveloStore.getUsername()+"ProfilePic.png";
            File outFile = new File(dir, fileName);
            outStream = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            isDataValid=true;*/

            if(draw!=null) isDataValid=true;
        }catch (Exception e){
            e.printStackTrace();
            isDataValid=false;
        }
        return isDataValid;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            if(data.getData()!=null){
                Uri picUri = getPickImageResultUri(data);

                try {
                    Bitmap myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
                    profilePicIv.setImageBitmap(myBitmap);
               /* String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/PhysicsSketchpad";
                File dir = new File(file_path);
                if(!dir.exists())
                    dir.mkdirs();
                File file = new File(dir, ReveloStore.g);
                FileOutputStream fOut = new FileOutputStream(file);

                bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                fOut.flush();
                fOut.close();*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, "Could not get image", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "Error occurred. Please retry", Toast.LENGTH_SHORT).show();
        }
    }
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }


        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
        }
        return outputFileUri;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private class UpdateProfileTask extends AsyncTask<String, Integer, JSONObject> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(EditProfile.this);
            pDialog.setMessage("Updating user profile..");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (pDialog != null) {
                if (pDialog.isShowing()) {
                    pDialog.setProgress(values[0]);
                    //pg.setMax(values[1]);
                }
            }
        }
        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject response1 = new JSONObject();
            try {
                try {
                    JSONObject jsonObjectNames = new JSONObject();
                    jsonObjectNames.put("firstName", firstName);
                    jsonObjectNames.put("lastName", lastName);
                    jsonObjectNames.put("phoneNumber", phoneNumber);
                    response1 = UploadFile.doPostToSendJson(UrlStore.getUpdateProfileUrl(), jsonObjectNames.toString(), EditProfile.this);
                    String response1Result = response1.getString("status");
                    String response1Message = response1.getString("message");
                    if(response1Result.equalsIgnoreCase("success")){

//                        Log.d("editprofilecheck", "doInBackground: "+"success");
                        UserInfoPreferenceUtility.storeFirstName(firstName);
                        UserInfoPreferenceUtility.storeLastName(lastName);
                        UserInfoPreferenceUtility.storePhoneNumber(phoneNumber);
                        
                        BitmapDrawable draw = (BitmapDrawable) profilePicIv.getDrawable();
                        Bitmap bitmap = draw.getBitmap();

                        FileOutputStream outStream = null;
                        File dir = new File(AppFolderStructure.userProfilePictureFolderPath(EditProfile.this));
                        dir.mkdirs();
                        String fileName = UserInfoPreferenceUtility.getUserName()+"ProfilePic.png";
                        File outFile = new File(dir, fileName);
                        outStream = new FileOutputStream(outFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.flush();
                        outStream.close();

                        File profilepic = new File(AppFolderStructure.userProfilePictureFolderPath(EditProfile.this) + File.separator + fileName);
                        String pictureFileUploadResult;
                        try {

                            pictureFileUploadResult = UploadFile.uploadFile(fileName, SecurityPreferenceUtility.getAccessToken(), UrlStore.getProfilePicUrl(), profilepic,EditProfile.this, new UploadFile.ShowProgress() {
                                        @Override
                                        public void progress(long bytesUploaded, long fileLength) {
                                            showProgressToUser(bytesUploaded, fileLength);
                                        }
                                    });
                        } catch (Exception e) {
                            pictureFileUploadResult = e.toString();
                            ReveloLogger.error("EditProfile", "upload User picture", pictureFileUploadResult);
                        }

                        Log.d("pictureFileUploadResult", "doInBackground: "+pictureFileUploadResult);

                        if(pictureFileUploadResult.equalsIgnoreCase("success")){
                            response1.put("status","success");
                            response1.put("message","User's First name, Last name, Phone number and profile picture updated successfully.");
                        }else {
                            response1.put("status","failure");
                            response1.put("message","profile picture update failed. ");
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            return response1;
        }
        private void showProgressToUser(long bytesUploaded, long totalFileSizeInBytes) {
            int megabytesUploaded = (int) bytesUploaded;
            int totalFileSizeInMegabytes = (int) totalFileSizeInBytes;
            onProgressUpdate(megabytesUploaded, totalFileSizeInMegabytes);
        }
        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(JSONObject responseJson) {
            super.onPostExecute(responseJson);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            try {
                editIv.setClickable(true);
                if (responseJson.has("status")) {
                    if (responseJson.getString("status").equalsIgnoreCase("success")) {
                        UserInfoPreferenceUtility.storeFirstName(firstName);
                        UserInfoPreferenceUtility.storeLastName(lastName);
                        UserInfoPreferenceUtility.storePhoneNumber(phoneNumber);
                        BitmapDrawable draw = (BitmapDrawable) profilePicIv.getDrawable();
                        Bitmap bitmap = draw.getBitmap();

                        FileOutputStream outStream = null;
                        File dir = new File(AppFolderStructure.userProfilePictureFolderPath(EditProfile.this));
                        dir.mkdirs();
                        String fileName = UserInfoPreferenceUtility.getUserName()+"ProfilePic.png";
                        File outFile = new File(dir, fileName);
                        outStream = new FileOutputStream(outFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.flush();
                        outStream.close();

                        SystemUtils.showOkDialogBoxWithCallback("Profile updated...", EditProfile.this, new SystemUtils.OkDialogBox() {
                            @Override
                            public void onOkClicked(DialogInterface alertDialog) {
                                alertDialog.dismiss();
                                finish();
                            }
                        });
                    }else {
                        if(responseJson.has("message") && !responseJson.getString("message").isEmpty()) {
                            String message = responseJson.getString("message");
                            SystemUtils.showOkDialogBox(message,EditProfile.this);
                        }else {
                            SystemUtils.showOkDialogBox("Updating user profile failed. Please retry after some time.",EditProfile.this);
                        }
                    }
                }else {
                    finish();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
