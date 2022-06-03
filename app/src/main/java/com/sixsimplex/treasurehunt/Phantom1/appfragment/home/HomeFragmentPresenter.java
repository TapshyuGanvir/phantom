package com.sixsimplex.treasurehunt.Phantom1.appfragment.home;

import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;

import java.util.List;

public class HomeFragmentPresenter implements IhomeFragmentPresenter {
    private IhomeFragmentView ihomeFragmentView;


    public HomeFragmentPresenter(IhomeFragmentView ihomeFragmentView) {
        this.ihomeFragmentView = ihomeFragmentView;
    }


    public int getFirstTargetFeaturePosition(List<Feature> inRangeTargetList) {
        if(!inRangeTargetList.isEmpty()){
            for(Feature feature:inRangeTargetList){
                try {
                    boolean isDelivered = (boolean) feature.getAttributes().get("isdelivered");
//                    boolean isVisited = (boolean) feature.getAttributes().get("isvisited");
                    boolean isSkipped = (boolean) feature.getAttributes().get("skipped");
                    if(!isDelivered && !isSkipped){
                        return DeliveryDataModel.getFeatureList().indexOf(feature);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return 0;
                }
            }
        }
        return 0;
    }
}
