package com.sixsimplex.treasurehunt.Phantom1.trip;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.products.ProductSpinnerAdapter;
import com.sixsimplex.treasurehunt.Phantom1.CURD.upload.UploadInterface;

import org.json.JSONObject;

import java.util.List;

public class TripItemsForm extends BottomSheetDialogFragment {
    Context context;
    LinearLayout product_container_view;
    Button add_product_btn;
    Button cancelBtn,addBtn;
    TripItemFormPresenter tripItemFormPresenter;
    Location location;
    ITripCallback iTripCallback;
    IdeliveryActivityView ideliveryActivityView;
    RelativeLayout tripProgressBar;
    Activity parentActivity;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public TripItemsForm(Activity context, Location userCurrentLocation, ITripCallback iTripCallback, IdeliveryActivityView ideliveryActivityView) {
        this.parentActivity=context;
        this.context = context;
        this.location=userCurrentLocation;
        this.iTripCallback=iTripCallback;
        this.ideliveryActivityView=ideliveryActivityView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.trip_item_layout, null);
        dialog.setContentView(contentView);
        tripItemFormPresenter =new TripItemFormPresenter();
        List<Feature> productList=tripItemFormPresenter.getProducts(context);
        product_container_view=contentView.findViewById(R.id.container_view);
        add_product_btn=contentView.findViewById(R.id.add_product_btn);
        add_product_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adddailyscheduleview(productList);
            }
        });
        tripProgressBar=contentView.findViewById(R.id.progress_dialog_layout_trip);
        cancelBtn=contentView.findViewById(R.id.form_cancel_trip_items);
        addBtn=contentView.findViewById(R.id.form_update_trip_items);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ideliveryActivityView.showProgressDialog("Updating...");

                showProgress();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        addProductsInTripItems(dialog,productList);
                    }
                }).start();
            }
        });
        adddailyscheduleview(productList);

        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void adddailyscheduleview(List<Feature> productList) {
        @SuppressLint("InflateParams") View productRowView=getLayoutInflater().inflate(R.layout.prodcut_selection_layout_row,null,false);
        Spinner productItems=productRowView.findViewById(R.id.productItemsSpinner);
        ProductSpinnerAdapter productSpinnerAdapter=new ProductSpinnerAdapter(context,productList);
        productItems.setAdapter(productSpinnerAdapter);
        ImageView removeProduct=productRowView.findViewById(R.id.remove_item);
        removeProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeProductFromForm(productRowView);
            }
        });
        product_container_view.addView(productRowView);
    }

    private void removeProductFromForm(View view){
        product_container_view.removeView(view);
    }

    private void showProgress(){
        parentActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        tripProgressBar.setVisibility(View.VISIBLE);
        ((View) tripProgressBar.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void hideProgress(){
        parentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        tripProgressBar.setVisibility(View.GONE);
    }


    private void addProductsInTripItems(Dialog dialog, List<Feature> productList) {
        try {
            UploadInterface uploadInterface=new UploadInterface() {
                @Override
                public void OnUploadStarted() {

                }

                @Override
                public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                }
            };
            Feature feature = TripDataModel.getInstance().getTodayTripFeature();
            if(feature != null){
                boolean allProductAddSuccessfully=true;
                for(int i=0;i<product_container_view.getChildCount();i++){
                    View productView=product_container_view.getChildAt(i);
                    EditText productQntEt=(EditText)productView.findViewById(R.id.product_count_et);
                    Spinner spinner_of_i=productView.findViewById(R.id.productItemsSpinner);
                    int productQntVal=0;
                    if(productQntEt.getText() != null && !productQntEt.getText().equals("")){
                        productQntVal=Integer.valueOf(String.valueOf(productQntEt.getText()));
                    }
                    String productId= (String) productList.get(spinner_of_i.getSelectedItemPosition()).getFeatureId();


                    JSONObject resultJson=tripItemFormPresenter.addTripItemsInDataBase(context, productId, productQntVal, String.valueOf(feature.getFeatureId()), location, new UploadInterface() {
                        @Override
                        public void OnUploadStarted() {

                        }

                        @Override
                        public void OnUploadFinished(boolean isSuccessfull, JSONObject uploadResult) {

                        }
                    });
                    if (resultJson != null) {
                        if (resultJson.has("status")) {
                            if (resultJson.getString("status").equalsIgnoreCase("success")) {
                                Trip.initiateTripData(context);
                            }
                        }
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(iTripCallback != null){
                                iTripCallback.onSuccessTripAddUpdate();
                            }
                            hideProgress();
                            dialog.dismiss();
                        }catch (Exception e){
                            hideProgress();
                            e.printStackTrace();
                        }

                    }
                });
            }
        }catch (Exception e){
            hideProgress();
            e.printStackTrace();
        }

    }


}
