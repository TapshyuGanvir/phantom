package com.sixsimplex.treasurehunt.Phantom1.appfragment.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;

import java.util.List;



public class HomeFragment extends Fragment {

    Context context;

    public HomeFragmentPresenter getHomeFragmentPresenter() {
        return homeFragmentPresenter;
    }

    HomeFragmentPresenter homeFragmentPresenter;
    RecyclerView delivery_item_list_view;
    HomeScreenAdapter homeScreenAdapter;
    LinearLayoutManager linearLayoutManager;
    IdeliveryActivityView ideliveryActivityView;
//    TextView empty_view;



    public HomeFragment(Context context, HomeFragmentPresenter homeFragmentPresenter, IdeliveryActivityView ideliveryActivityView) {
        // Required empty public constructor
        this.context = context;
        this.homeFragmentPresenter = homeFragmentPresenter;
        this.ideliveryActivityView=ideliveryActivityView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        delivery_item_list_view = view.findViewById(R.id.delivery_item_list_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linearLayoutManager=new LinearLayoutManager(context);
        delivery_item_list_view.setLayoutManager(linearLayoutManager);
        homeScreenAdapter = new HomeScreenAdapter(context, DeliveryDataModel.getFeatureList(),ideliveryActivityView);
        delivery_item_list_view.setAdapter(homeScreenAdapter);
        notifyListData(-1);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyListData(int position) {
        homeScreenAdapter.setFeatureList(DeliveryDataModel.getFeatureList());
        if(position != -1){
            homeScreenAdapter.notifyItemChanged(position);
        }else{
            homeScreenAdapter.notifyDataSetChanged();
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
//            highlightTargetFeature(DeliveryDataModel.getInstance().getTargetFeature());
//            if(DeliveryDataModel.getInstance().getInRangeFeature() != null){
//                performAutoSelectionFunctionalityInListView(DeliveryDataModel.getInstance().getInRangeFeature());
//            }
        }
        super.onHiddenChanged(hidden);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void highlightTargetFeature(Feature targetFeatureData) {
        if(homeScreenAdapter != null){
            homeScreenAdapter.setTargetFeature(targetFeatureData);
            homeScreenAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void performAutoSelectionFunctionalityInListView(List<Feature> inRangeTargetList){
        try {
//            homeScreenAdapter.setFeaturesEnable(inRangeTargetList);
            scrollTopToThePosition(homeFragmentPresenter.getFirstTargetFeaturePosition(inRangeTargetList));
            homeScreenAdapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void scrollTopToThePosition(int position) {
        delivery_item_list_view.post(new Runnable() {
            @Override
            public void run() {
                linearLayoutManager.scrollToPositionWithOffset(position,0);
            }
        });
    }

}