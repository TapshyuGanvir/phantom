package com.sixsimplex.treasurehunt.Phantom1.appfragment.home;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.Phantom1.utils.Utils;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.Phantom1.ordersummery.OrderSummeryActivity;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;


import java.util.List;

public class HomeScreenAdapter extends RecyclerView.Adapter<HomeScreenAdapter.ViewHolder> {
    Context context;
    List<Feature> featureList;
    int listCount = 0;
    CMGraph cmGraph = null;
    Handler mHandler = new Handler(Looper.getMainLooper());
    Feature targetFeature = null;
//    private List<Feature> inRangeTargetList;
    IdeliveryActivityView ideliveryActivityView;


    public HomeScreenAdapter(Context context, List<Feature> featureList, IdeliveryActivityView ideliveryActivityView) {
        this.context = context;
        this.featureList = featureList;
        this.ideliveryActivityView=ideliveryActivityView;
    }

    public void setFeatureList(List<Feature> featureList) {
        this.featureList = featureList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.delivery_list_item_config, parent, false);
        listCount = 0;
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Feature featureInDropOff = featureList.get(position);
        listCount++;
        if (position == 0) {

        }

        final int sdk = android.os.Build.VERSION.SDK_INT;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Feature feature = getConsumerFeatureForTargetDelivery(context, featureInDropOff);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (feature != null) {
//                                String firstname = (String) feature.getAttributes().get("firstname");
//                                String lastname = (String) feature.getAttributes().get("lastname");

                                String name =(String) feature.getAttributes().get("cusname");
                                viewHolder.delivery_customer_name.setText(name);
                                String mobileNumber = String.valueOf(feature.getAttributes().get("mobno"));
                                viewHolder.mobile_number_tv_lv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobileNumber, null));
                                        context.startActivity(intent);
                                    }
                                });
                            } else {
                                viewHolder.delivery_customer_name.setText("Your Customer");
                                viewHolder.mobile_number_tv_lv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(context, "Mobile number not available.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();


        viewHolder.delivery_list_count.setText(String.valueOf(position + 1));
        viewHolder.delivery_customer_address.setText((String) featureInDropOff.getAttributes().get("address"));


//        boolean isVisited = (boolean) featureInDropOff.getAttributes().get("isvisited");
        boolean isSkipped = (boolean) featureInDropOff.getAttributes().get("skipped");
        boolean isDelivered = (boolean) featureInDropOff.getAttributes().get("isdelivered");
        if(isDelivered){
            viewHolder.delivery_toggle_btn_lv.setChecked(true);
        }else{
            viewHolder.delivery_toggle_btn_lv.setChecked(false);
        }

        if(isFeatureEditable(featureInDropOff)){
            viewHolder.pictureButtonLl.setEnabled(true);
            viewHolder.product_lv_ll.setEnabled(true);

            viewHolder.skip_lv_ll.setVisibility(View.VISIBLE);
            viewHolder.delivery_toggle_btn_lv.setVisibility(View.VISIBLE);

            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.inner_view.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.delivery_list_item_bg_selected));
            }else{
                viewHolder.inner_view.setBackground(ContextCompat.getDrawable(context,R.drawable.delivery_list_item_bg_selected));
            }

        }else{
            viewHolder.pictureButtonLl.setEnabled(false);
            viewHolder.product_lv_ll.setEnabled(false);

            viewHolder.skip_lv_ll.setVisibility(View.GONE);
            viewHolder.delivery_toggle_btn_lv.setVisibility(View.GONE);

            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.inner_view.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.delivery_list_item_bg));
            }else{
                viewHolder.inner_view.setBackground(ContextCompat.getDrawable(context,R.drawable.delivery_list_item_bg));
            }
        }


        int t = -1;
        if (targetFeature != null) {
            if (featureInDropOff.getFeatureId().equals(targetFeature.getFeatureId())) {
                t = 1;
            }
        }

        if (t == 1) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_t));
            } else {
                viewHolder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_t));
            }

        } else {
            if (!isSkipped && !isDelivered) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_p));
                } else {
                    viewHolder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_p));
                }
//            holder.status_tv.setText("Pending");
//            holder.status_tv.setTextColor(Color.parseColor("#fc0a0a"));
            } else if (isSkipped && !isDelivered) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_ic));
                } else {
                    viewHolder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_ic));
                }
//            holder.status_tv.setText("Incomplete");
//            holder.status_tv.setTextColor(Color.parseColor("#d9fa05"));
            } else if (!isSkipped && isDelivered) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_c));
                } else {
                    viewHolder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_c));
                }
//            holder.status_tv.setText("Complete");
//            holder.status_tv.setTextColor(Color.parseColor("#007a02"));
            }
        }


//        if(getItemViewType(position) == getItemCount()-1){
//            holder.strip_bottom.setVisibility(View.INVISIBLE);
//        }

        if (position == featureList.size() - 1) {

        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(context, OrderSummeryActivity.class);
//                intent.putExtra("featureInDropOffId", String.valueOf(featureInDropOff.getFeatureId()));
//                intent.putExtra("consumerId", String.valueOf(featureInDropOff.getAttributes().get("customerid")));
//                context.startActivity(intent);
            }
        });

        new PerformListDelivery(context,viewHolder,position,featureInDropOff,ideliveryActivityView).perform();

    }



//    @Override
//    public int getItemViewType(int position) {
//        try {
//            Feature featureInDropOff = featureList.get(position);
//            if (DeliveryDataModel.getInstance().getInRangeFeature() != null) {
//                if (DeliveryDataModel.getInstance().getInRangeFeature().contains(featureInDropOff)) {
//                    try {
//                        boolean isDelivered = (boolean) featureInDropOff.getAttributes().get("isdelivered");
//                        boolean isVisited = (boolean) featureInDropOff.getAttributes().get("isvisited");
//                        if(!isDelivered && !isVisited){
//                            return 1;
//                        }else{
//                            return 0;
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//                        return 0;
//                    }
//                } else {
//                    return 0;
//                }
//            } else {
//                return 0;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            return 0;
//        }
//    }

     private boolean isFeatureEditable(Feature featureInDropOff){
         try {
             if (DeliveryDataModel.getInstance().getInRangeFeature() != null) {
//                 DeliveryDataModel.getInstance().getInRangeFeature().contains(featureInDropOff)
                 if (Utils.isFeaturePresentInList(DeliveryDataModel.getInstance().getInRangeFeature(),featureInDropOff)) {
                     try {
                         boolean isDelivered = (boolean) featureInDropOff.getAttributes().get("isdelivered");
                         boolean isSkipped = (boolean) featureInDropOff.getAttributes().get("skipped");
//                         boolean isVisited = (boolean) featureInDropOff.getAttributes().get("isvisited");
                         if(!isDelivered && !isSkipped){
                             return true;
                         }else{
                             return false;
                         }
                     }catch (Exception e){
                         e.printStackTrace();
                         return false;
                     }
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         }catch (Exception e){
             e.printStackTrace();
             return false;
         }
     }

    @Override
    public int getItemCount() {
        return featureList.size();
    }

    public Feature getConsumerFeatureForTargetDelivery(Context activity, Feature targetFeature) {

        try {
            if(DeliveryDataModel.getInstance().getConsumersList() != null){
                if(!DeliveryDataModel.getInstance().getConsumersList().isEmpty()){
                    for(Feature feature:DeliveryDataModel.getInstance().getConsumersList()){
                        if(feature.getFeatureId().equals(targetFeature.getAttributes().get("customerid"))){
                            return feature;
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;


    }

    public void setTargetFeature(Feature targetFeature) {
        this.targetFeature = targetFeature;
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sequenceNum, delivery_customer_address, delivery_list_count, delivery_customer_name;
        LinearLayout inner_view, featureIndexLayout,dropDownLl,drop_data_ll,pictureButtonLl,product_lv_ll,skip_lv_ll;
        ImageView downArrowImageView,upArrowImageView;
        TextView mobile_number_tv_lv;
        ToggleButton delivery_toggle_btn_lv;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            delivery_customer_address = itemView.findViewById(R.id.address_in_list_tv);
            delivery_customer_name = itemView.findViewById(R.id.name_in_list_tv);
            mobile_number_tv_lv=itemView.findViewById(R.id.mobile_number_tv_lv);
            delivery_list_count = itemView.findViewById(R.id.count_tv);
            featureIndexLayout = itemView.findViewById(R.id.featureIndexLayout);
            delivery_toggle_btn_lv=itemView.findViewById(R.id.delivery_toggle_btn_lv);
            inner_view = itemView.findViewById(R.id.inner_view);
            product_lv_ll=itemView.findViewById(R.id.product_lv_ll);
            pictureButtonLl=itemView.findViewById(R.id.picture_button_ll);
            skip_lv_ll=itemView.findViewById(R.id.skip_lv_ll);
            dropDownLl=itemView.findViewById(R.id.drop_down_ll);
            downArrowImageView=itemView.findViewById(R.id.down_arrow_iv);
            upArrowImageView=itemView.findViewById(R.id.up_arrow_iv);
            drop_data_ll=itemView.findViewById(R.id.drop_data_ll);
        }
    }


}
