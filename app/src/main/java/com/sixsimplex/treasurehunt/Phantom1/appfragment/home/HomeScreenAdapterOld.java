package com.sixsimplex.treasurehunt.Phantom1.appfragment.home;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.Phantom1.ordersummery.OrderSummeryActivity;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;


import java.util.List;

public class HomeScreenAdapterOld extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<Feature> featureList;
    int listCount = 0;
    CMGraph cmGraph = null;
    Handler mHandler = new Handler(Looper.getMainLooper());
    Feature targetFeature = null;
//    private List<Feature> inRangeTargetList;
    IdeliveryActivityView ideliveryActivityView;


    public HomeScreenAdapterOld(Context context, List<Feature> featureList, IdeliveryActivityView ideliveryActivityView) {
        this.context = context;
        this.featureList = featureList;
        this.ideliveryActivityView=ideliveryActivityView;
    }

    public void setFeatureList(List<Feature> featureList) {
        this.featureList = featureList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View view = LayoutInflater.from(context).inflate(R.layout.delivery_list_item, parent, false);
                return new Viewholder1(view);

            case 1:
                View view2 = LayoutInflater.from(context).inflate(R.layout.delivery_list_item_config, parent, false);
                return new ViewHolder2(view2);
        }
        listCount = 0;

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Feature featureInDropOff = featureList.get(position);
        switch (getItemViewType(position)) {
            case 0:
                Viewholder1 viewHolder1 = (Viewholder1) holder;
                onBindFirst(viewHolder1, position, featureInDropOff);
                break;

            case 1:
                ViewHolder2 viewHolder2 = (ViewHolder2) holder;
                onBindSecond(viewHolder2, position, featureInDropOff);
                break;

            default:
                break;
        }
    }

    private void onBindFirst(Viewholder1 viewHolder1, int position, Feature featureInDropOff) {
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

                                String name=(String) feature.getAttributes().get("cusname");
                                viewHolder1.delivery_customer_name.setText(name);
                            } else {
                                viewHolder1.delivery_customer_name.setText("Your Customer");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();

        viewHolder1.delivery_list_count.setText(String.valueOf(position + 1));
        viewHolder1.delivery_customer_address.setText((String) featureInDropOff.getAttributes().get("address"));

//        boolean isVisited = (boolean) featureInDropOff.getAttributes().get("isvisited");
        boolean isDelivered = (boolean) featureInDropOff.getAttributes().get("isdelivered");
        boolean isSkipped = (boolean) featureInDropOff.getAttributes().get("skipped");


        int t = -1;
        if (targetFeature != null) {
            if (featureInDropOff.getFeatureId().equals(targetFeature.getFeatureId())) {
                t = 1;
            }
        }

        if (t == 1) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder1.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_t));
            } else {
                viewHolder1.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_t));
            }

        } else {
            if (!isSkipped && !isDelivered) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder1.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_p));
                } else {
                    viewHolder1.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_p));
                }
//            holder.status_tv.setText("Pending");
//            holder.status_tv.setTextColor(Color.parseColor("#fc0a0a"));
            } else if (isSkipped && !isDelivered) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder1.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_ic));
                } else {
                    viewHolder1.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_ic));
                }
//            holder.status_tv.setText("Incomplete");
//            holder.status_tv.setTextColor(Color.parseColor("#d9fa05"));
            } else if (isSkipped && isDelivered) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder1.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_c));
                } else {
                    viewHolder1.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_c));
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

        viewHolder1.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(context, OrderSummeryActivity.class);
//                intent.putExtra("featureInDropOffId", String.valueOf(featureInDropOff.getFeatureId()));
//                intent.putExtra("consumerId", String.valueOf(featureInDropOff.getAttributes().get("customerid")));
//                context.startActivity(intent);
            }
        });
    }

    private void onBindSecond(ViewHolder2 viewHolder2, int position, Feature featureInDropOff) {
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

                                String name=(String) feature.getAttributes().get("cusname");
                                viewHolder2.delivery_customer_name.setText(name);
                            } else {
                                viewHolder2.delivery_customer_name.setText("Your Customer");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();


        viewHolder2.delivery_list_count.setText(String.valueOf(position + 1));
//        viewHolder2.delivery_customer_address.setText((String) featureInDropOff.getAttributes().get("address"));

        boolean isDelivered = (boolean) featureInDropOff.getAttributes().get("isdelivered");
        boolean isSkipped = (boolean) featureInDropOff.getAttributes().get("skipped");
//        if(isDelivered){
//            viewHolder2.toggleButtonDeliveryStatus.setChecked(true);
//        }else{
//            viewHolder2.toggleButtonDeliveryStatus.setChecked(false);
//        }


        int t = -1;
        if (targetFeature != null) {
            if (featureInDropOff.getFeatureId().equals(targetFeature.getFeatureId())) {
                t = 1;
            }
        }

        if (t == 1) {
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder2.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_t));
            } else {
                viewHolder2.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_t));
            }

        } else {
            if (!isSkipped && !isDelivered) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder2.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_p));
                } else {
                    viewHolder2.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_p));
                }
//            holder.status_tv.setText("Pending");
//            holder.status_tv.setTextColor(Color.parseColor("#fc0a0a"));
            } else if (isSkipped && !isDelivered) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder2.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_ic));
                } else {
                    viewHolder2.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_ic));
                }
//            holder.status_tv.setText("Incomplete");
//            holder.status_tv.setTextColor(Color.parseColor("#d9fa05"));
            } else if (isSkipped && isDelivered) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder2.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_c));
                } else {
                    viewHolder2.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_c));
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

        viewHolder2.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(context, OrderSummeryActivity.class);
//                intent.putExtra("featureInDropOffId", String.valueOf(featureInDropOff.getFeatureId()));
//                intent.putExtra("consumerId", String.valueOf(featureInDropOff.getAttributes().get("customerid")));
//                context.startActivity(intent);
            }
        });

//        new PerformListDelivery(context,viewHolder2,position,featureInDropOff,ideliveryActivityView).perform();
    }

//    @SuppressLint({"SetTextI18n"})
//    @Override
//    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
//        listCount++;
//        if (getItemViewType(position) == 0) {
//
//        }
//
//        final int sdk = android.os.Build.VERSION.SDK_INT;
//        Feature featureInDropOff = featureList.get(getItemViewType(position));
//        if (inRangeTargetList != null) {
//            if (inRangeTargetList.contains(featureInDropOff)) {
//                holder.update_form_widget.setVisibility(View.VISIBLE);
//                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    holder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.delivery_list_item_bg_selected));
//                } else {
//                    holder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.delivery_list_item_bg_selected));
//                }
//            } else {
//                holder.update_form_widget.setVisibility(View.GONE);
//                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    holder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.delivery_list_item_bg));
//                } else {
//                    holder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.delivery_list_item_bg));
//                }
//
//            }
//        } else {
//            holder.update_form_widget.setVisibility(View.GONE);
//            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                holder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.delivery_list_item_bg));
//            } else {
//                holder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.delivery_list_item_bg));
//            }
//        }
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Feature feature = getConsumerFeatureForTargetDelivery(context, featureInDropOff);
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            if (feature != null) {
//                                String firstname = (String) feature.getAttributes().get("firstname");
//                                String lastname = (String) feature.getAttributes().get("lastname");
//                                holder.delivery_customer_name.setText(firstname + " " + lastname);
//                            } else {
//                                holder.delivery_customer_name.setText("Your Customer");
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        }).start();
//
//
//        holder.delivery_list_count.setText(String.valueOf(position + 1));
//        holder.delivery_customer_address.setText((String) featureInDropOff.getAttributes().get("address"));
//
//        boolean isVisited = (boolean) featureInDropOff.getAttributes().get("isvisited");
//        boolean isDelivered = (boolean) featureInDropOff.getAttributes().get("isdelivered");
//
//
//        int t = -1;
//        if (targetFeature != null) {
//            if (featureInDropOff.getFeatureId().equals(targetFeature.getFeatureId())) {
//                t = 1;
//            }
//        }
//
//        if (t == 1) {
//            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                holder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_t));
//            } else {
//                holder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_t));
//            }
//
//        } else {
//            if (!isVisited && !isDelivered) {
//                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    holder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_p));
//                } else {
//                    holder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_p));
//                }
////            holder.status_tv.setText("Pending");
////            holder.status_tv.setTextColor(Color.parseColor("#fc0a0a"));
//            } else if (isVisited && !isDelivered) {
//                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    holder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_ic));
//                } else {
//                    holder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_ic));
//                }
////            holder.status_tv.setText("Incomplete");
////            holder.status_tv.setTextColor(Color.parseColor("#d9fa05"));
//            } else if (isVisited && isDelivered) {
//                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    holder.featureIndexLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_c));
//                } else {
//                    holder.featureIndexLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_c));
//                }
////            holder.status_tv.setText("Complete");
////            holder.status_tv.setTextColor(Color.parseColor("#007a02"));
//            }
//        }
//
//
////        if(getItemViewType(position) == getItemCount()-1){
////            holder.strip_bottom.setVisibility(View.INVISIBLE);
////        }
//
//        if (getItemViewType(position) == featureList.size() - 1) {
//
//        }
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(context, OrderSummeryActivity.class);
//                intent.putExtra("featureInDropOffId", String.valueOf(featureInDropOff.getFeatureId()));
//                intent.putExtra("consumerId", String.valueOf(featureInDropOff.getAttributes().get("customerid")));
//                context.startActivity(intent);
//            }
//        });
//    }

    @Override
    public int getItemViewType(int position) {
        try {
            Feature featureInDropOff = featureList.get(position);
            if (DeliveryDataModel.getInstance().getInRangeFeature() != null) {
                if (DeliveryDataModel.getInstance().getInRangeFeature().contains(featureInDropOff)) {
                    try {
                        boolean isDelivered = (boolean) featureInDropOff.getAttributes().get("isdelivered");
                        boolean isVisited = (boolean) featureInDropOff.getAttributes().get("isvisited");
                        if(!isDelivered && !isVisited){
                            return 1;
                        }else{
                            return 0;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        return 0;
                    }
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }catch (Exception e){
            e.printStackTrace();
            return 0;
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


//        List<Feature> featureList = new ArrayList<>();
//        try {
//            String customerid = (String) targetFeature.getAttributes().get("customerid");
//            if (customerid == null) {
//                return null;
//            }
//            JSONObject graphResult = CMUtils.getCMGraph(activity);
//            if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
//                cmGraph = (CMGraph) graphResult.get("result");
//            } else {
//
//            }
//            Set<CMEntity> rootEntities = cmGraph.getRootVertices();
//            for (CMEntity cmEntity : rootEntities) {
//                if (cmEntity.getName().equalsIgnoreCase(DeliveryDataModel.consumerEntityName)) {
//                    List<String> requiredColumnList = new ArrayList<>();
//                    requiredColumnList.add("customerid");
//                    requiredColumnList.add("firstname");
//                    requiredColumnList.add("lastname");
//
//                    JSONArray conditionClause = new JSONArray();
//                    JSONObject conditionJobj = new JSONObject();
//                    conditionJobj.put("conditionType", "attribute");
//                    conditionJobj.put("columnName", "customerid");
//                    conditionJobj.put("valueDataType", "String");
//                    conditionJobj.put("value", customerid);
//                    conditionJobj.put("operator", "=");
//                    conditionClause.put(conditionJobj);
//
//                    featureList = cmEntity.getFeatureTable().getFeaturesByQuery(activity,
//                            requiredColumnList,
//                            null, conditionClause,
//                            "OR",
//                            true,
//                            false,
//                            true,
//                            0,
//                            -1,
//                            false,
//                            false);
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Feature feature = null;
//        if (!featureList.isEmpty()) {
//            feature = featureList.get(0);
//        }
//        return feature;
    }

    public void setTargetFeature(Feature targetFeature) {
        this.targetFeature = targetFeature;
    }

//    @SuppressLint("NotifyDataSetChanged")
//    public void setFeaturesEnable(List<Feature> inRangeTargetList) {
//        this.inRangeTargetList = inRangeTargetList;
//    }

    class Viewholder1 extends RecyclerView.ViewHolder {
        TextView sequenceNum, delivery_customer_address, delivery_list_count, delivery_customer_name;
        LinearLayout inner_view, featureIndexLayout, update_form_widget;


        public Viewholder1(@NonNull View itemView) {
            super(itemView);
            delivery_customer_address = itemView.findViewById(R.id.address_in_list_tv);
            delivery_customer_name = itemView.findViewById(R.id.name_in_list_tv);
            delivery_list_count = itemView.findViewById(R.id.count_tv);
            featureIndexLayout = itemView.findViewById(R.id.featureIndexLayout);
            inner_view = itemView.findViewById(R.id.inner_view);
        }
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        TextView sequenceNum, delivery_customer_address, delivery_list_count, delivery_customer_name;
        LinearLayout inner_view, featureIndexLayout,dropDownLl,drop_data_ll;
        ImageView downArrowImageView,upArrowImageView;


//
//        //config buttons declaration
//        ToggleButton toggleButtonDeliveryStatus;
//        LinearLayout pictureBtnLl,productBtnLl,updateBtnLl;



        public ViewHolder2(@NonNull View itemView) {
            super(itemView);
            delivery_customer_address = itemView.findViewById(R.id.address_in_list_tv);
            delivery_customer_name = itemView.findViewById(R.id.name_in_list_tv);
            delivery_list_count = itemView.findViewById(R.id.count_tv);
            featureIndexLayout = itemView.findViewById(R.id.featureIndexLayout);
            inner_view = itemView.findViewById(R.id.inner_view);

            dropDownLl=itemView.findViewById(R.id.drop_down_ll);
            downArrowImageView=itemView.findViewById(R.id.down_arrow_iv);
            upArrowImageView=itemView.findViewById(R.id.up_arrow_iv);
            drop_data_ll=itemView.findViewById(R.id.drop_data_ll);

//            update_form_widget = itemView.findViewById(R.id.update_form_widget);
//            toggleButtonDeliveryStatus =itemView.findViewById(R.id.delivery_status_toggle_button);
//            pictureBtnLl=itemView.findViewById(R.id.picture_button_ll);
//            productBtnLl=itemView.findViewById(R.id.product_button_ll);
//            updateBtnLl=itemView.findViewById(R.id.update_button_ll);
        }
    }

}
