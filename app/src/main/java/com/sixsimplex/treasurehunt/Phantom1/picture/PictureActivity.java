package com.sixsimplex.treasurehunt.Phantom1.picture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sixsimplex.treasurehunt.Phantom1.CURD.upload.UploadInterface;
import com.sixsimplex.treasurehunt.Phantom1.app.view.DeliveryMainActivity;
import com.sixsimplex.treasurehunt.Phantom1.picture.camera.GetAttachmentInfoInterface;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.Phantom1.picture.camera.CameraFragment;
import com.sixsimplex.treasurehunt.revelocore.editing.model.Attachment;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.DatePickerMethods;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.locationModule.GetUserLocation;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PictureActivity extends AppCompatActivity implements GetAttachmentInfoInterface,IPictureCallback {

    String featureId, entityName, path, type;
    RecyclerView capturedImageView;
    Button updatePicture,cancelPicture;
    LinearLayoutManager linearLayoutManager;
    PictureViewAdapter pictureViewAdapter;
    private List<Attachment> attachmentFileList;
    PictureActivityPresenter pictureActivityPresenter;
    GetUserLocation getUserLocation;
    Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        Objects.requireNonNull(getSupportActionBar()).hide();
        pictureActivityPresenter=new PictureActivityPresenter(this,this);
        handleIntent();
        init();
        openCamera(savedInstanceState);
        setUpCapturedPictureView();
        getUserLocation=new GetUserLocation(PictureActivity.this,null,null,null,null,this::onLocationChange);
    }

    private void onLocationChange(Location location) {
    }


    @SuppressLint("SetTextI18n")
    public void showProgressDialog(String progressText) {
        PictureActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    progressDialog = new Dialog(PictureActivity.this);
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    progressDialog.setTitle(null);
                    progressDialog.setCancelable(false);
                    LayoutInflater inflater = PictureActivity.this.getLayoutInflater();
                    @SuppressLint("InflateParams") View content = inflater.inflate(R.layout.progress_dialog, null);
                    progressDialog.setContentView(content);
                    TextView progressTextView = (TextView) content.findViewById(R.id.progressText);
                    progressTextView.setText(progressText);
                    progressDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void hideProgressDialog() {
        PictureActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (progressDialog != null) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setUpCapturedPictureView() {
        attachmentFileList=new ArrayList<>();
        linearLayoutManager=new LinearLayoutManager(PictureActivity.this,LinearLayoutManager.HORIZONTAL,false);
        capturedImageView.setLayoutManager(linearLayoutManager);
        pictureViewAdapter=new PictureViewAdapter(PictureActivity.this,attachmentFileList,PictureActivity.this);
        capturedImageView.setAdapter(pictureViewAdapter);
    }

    private void handleIntent() {
        featureId = getIntent().getStringExtra("featureId");
        entityName = getIntent().getStringExtra("entityName");
        type = getIntent().getStringExtra(AppConstants.attachmentType);
    }
    private void init(){
        capturedImageView=findViewById(R.id.clickedImageView);
        updatePicture=findViewById(R.id.update_picture);
        cancelPicture=findViewById(R.id.cancel_picture);

        updatePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!attachmentFileList.isEmpty()){
                    showProgressDialog("Saving Pictures");
                    pictureActivityPresenter.saveAndUploadAttachment(attachmentFileList, featureId, entityName, getUserLocation.getUserCurrentLocation(), new UploadInterface() {
                        @Override
                        public void OnUploadStarted() {

                        }

                        @Override
                        public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {
                            try {
                                hideProgressDialog();
                                if(isSuccessfull){
                                    Toast.makeText(PictureActivity.this, "Pictures Update Successfully", Toast.LENGTH_SHORT).show();
                                    AppFolderStructure.deleteAttachmentFolder(PictureActivity.this);
                                    finish();
                                }else{
                                    Toast.makeText(PictureActivity.this, "Pictures Update Failed", Toast.LENGTH_SHORT).show();
                                }
                                hideProgressDialog();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    Toast.makeText(PictureActivity.this, "Click picture first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppFolderStructure.deleteAttachmentFolder(PictureActivity.this);
                finish();
            }
        });
    }

    private void openCamera(Bundle savedInstanceState) {
        try {
            if (null == savedInstanceState) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, CameraFragment.newInstance(path, type,PictureActivity.this)).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getCaptureImageInfo(File file, String fileType) {
        try{
            if(file != null){
                Attachment attachment = new Attachment();
                String currentDate = DatePickerMethods.getCurrentDateString();
                attachment.setFile(file);
                attachment.setDateTimeStamp(currentDate);
                attachment.setContentType(AppConstants.imageType);
                attachment.setAttachmentName(file.getName());
                attachment.setLabel("");
                attachmentFileList.add(attachment);
                pictureViewAdapter.notifyItemInserted(attachmentFileList.size());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        
    }

    @Override
    public void deletePicture(int position) {
        try {
            if(attachmentFileList != null){
                if(!attachmentFileList.isEmpty()){
                    attachmentFileList.remove(position);
                    if(pictureViewAdapter != null){
                        pictureViewAdapter.notifyItemRemoved(position);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onTaskCompleted(int attchmentUpoad, String message) {

    }
}