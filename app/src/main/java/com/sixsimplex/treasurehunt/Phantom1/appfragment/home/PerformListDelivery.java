package com.sixsimplex.treasurehunt.Phantom1.appfragment.home;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.sixsimplex.treasurehunt.Phantom1.CURD.EditAndUpload;
import com.sixsimplex.treasurehunt.Phantom1.CURD.upload.UploadInterface;
import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.Phantom1.mode.ModeUtility;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.Phantom1.picture.PictureActivity;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PerformListDelivery {

    public static final String DELIVER="deliver";
    public static final String SKIP="skip";

    Context context;
    private HomeScreenAdapter.ViewHolder viewHolder;
    private int position;
    private Feature targetFeature;
    IdeliveryActivityView ideliveryActivityView;

    private Animation rotate_down;

    public boolean isCheckedStatus() {
        return isCheckedStatus;
    }

    public void setCheckedStatus(boolean checkedStatus) {
        isCheckedStatus = checkedStatus;
    }

    boolean isCheckedStatus;

    public PerformListDelivery(Context context, HomeScreenAdapter.ViewHolder viewHolder, int position, Feature targetFeature, IdeliveryActivityView ideliveryActivityView) {
        this.context=context;
        this.viewHolder=viewHolder;
        this.position=position;
        this.targetFeature=targetFeature;
        this.ideliveryActivityView=ideliveryActivityView;
    }

    public void perform() {

        viewHolder.pictureButtonLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        rotate_down = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        viewHolder.dropDownLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewHolder.downArrowImageView.getVisibility() == View.VISIBLE) {
                    viewHolder.downArrowImageView.setVisibility(View.GONE);
                    viewHolder.upArrowImageView.setVisibility(View.VISIBLE);
                    viewHolder.drop_data_ll.startAnimation(rotate_down);
                    viewHolder.drop_data_ll.setVisibility(View.VISIBLE);
                }
                else {
                    viewHolder.downArrowImageView.setVisibility(View.VISIBLE);
                    viewHolder.upArrowImageView.setVisibility(View.GONE);
                    viewHolder.drop_data_ll.setVisibility(View.GONE);
                }
            }
        });

        viewHolder.delivery_toggle_btn_lv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ideliveryActivityView.showProgressDialog("Updating");
                updateDelivery(context,targetFeature,DELIVER,viewHolder.getAdapterPosition());
            }
        });
        viewHolder.skip_lv_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ideliveryActivityView.showProgressDialog("Updating");
                updateDelivery(context,targetFeature,SKIP,viewHolder.getAdapterPosition());
            }
        });

        viewHolder.product_lv_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ideliveryActivityView.showProductsUpdateDialogForTargetFeature(targetFeature);
            }
        });


    }

    private void takePicture() {
        try{

            Intent intent=new Intent(context, PictureActivity.class);
            intent.putExtra("featureId",String.valueOf(targetFeature.getFeatureId()));
            intent.putExtra("entityName",String.valueOf(targetFeature.getEntityName()));
            intent.putExtra(AppConstants.attachmentType, AppConstants.photo);
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateDelivery(Context context, Feature targetFeature, String status, int position) {
        try{
            Map<String, Object> attributeValueMap = new HashMap<>();
            Date dateCurrent = new Date();
            String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dateCurrent);
            if(status.equals(DELIVER)){
                attributeValueMap.put("isdelivered", 1);
                attributeValueMap.put("skipped", 0);
            }else{
                attributeValueMap.put("isdelivered", 0);
                attributeValueMap.put("skipped", 1);
            }
            attributeValueMap.put("dropdate", currentDate);
            attributeValueMap.put("isvisited", 1);
            attributeValueMap.put("delvboyid", UserInfoPreferenceUtility.getUserName());
            attributeValueMap.put("w9entityclassname", DeliveryDataModel.traversalEntityName);
            saveAndUpdateDataToServer(context,attributeValueMap,targetFeature,position);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void saveAndUpdateDataToServer(Context context, Map<String, Object> attributeValueMap, Feature targetFeature, int position) {
        try{
            JSONObject jsonObject = DeliveryDataModel.getInstance().getTraversalEntity()
                    .updateFeatureTraversal(String.valueOf(targetFeature.getFeatureId()), true, context);
            JSONObject resulJson = EditAndUpload.perform(context,
                    AppConstants.EDIT,
                    DeliveryDataModel.getInstance().getTraversalEntity(),
                    attributeValueMap,
                    null,
                    DeliveryDataModel.getInstance().getTraversalEntity().getFeatureTable(),
                    null,
                    String.valueOf(targetFeature.getFeatureId()),
                    String.valueOf(targetFeature.getFeatureLabel()),
                    null,
                    null,
                    "", new UploadInterface() {
                        @Override
                        public void OnUploadStarted() {

                        }

                        @Override
                        public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                        }
                    },true);

            if (resulJson != null) {
                if (resulJson.has("status")) {
                    if (resulJson.getString("status").equalsIgnoreCase("success")) {
                        Toast.makeText(context, "Delivery Successful", Toast.LENGTH_SHORT).show();
                        ideliveryActivityView.onTargetFeatureUpdated(ModeUtility.SINGLE,position);
                    } else {
                        Toast.makeText(context, "Delivery Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
