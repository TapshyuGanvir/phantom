package com.sixsimplex.treasurehunt.revelocore.insight;

import java.util.ArrayList;
import java.util.List;

public class Insight {
    public static String MAP="map";
    public static String HOME="home";
    public static String BASE="base";
    // map view Insight add edit delete permissions of feature
    public static boolean isFeaturesEditable=false;
    public static boolean isFeaturesDeletable =false;
    public static boolean isViewHasAdd=false;
    public static boolean showTrailsFeature=false;
    public static boolean applicationWhereAmIAccess=false;
    public static boolean showRevStatusPanel=false;
    public static boolean hideGoToJurisdictionInSetting=true;
    public static boolean showSurveyTab=false;
    public static boolean showPhaseTab=false;
    public static boolean showJurisdictionTab=false;

    //Application First activity
    //first page should be only between home or map according to current config
    public static String getApplicationFirstPage() {
        return MAP;
    }
    public static List<String> ApplicationActivities = new ArrayList<>();


    //first page should be compulsorily add in application activities
    public static List<String> getApplicationActivities() {
        ApplicationActivities.clear();
        ApplicationActivities.add(MAP);
        return ApplicationActivities;
    }

    public static boolean isIsFeaturesEditable() {
        return isFeaturesEditable;
    }
    public static boolean isIsFeaturesDeletable() {
        return isFeaturesDeletable;
    }
    public static boolean isViewHasAdd() {
        return isViewHasAdd;
    }

    //trail feature access
    public static boolean isShowTrailsFeature() {
        return showTrailsFeature;
    }

    //whereAmI access
    public static boolean isApplicationWhereAmIAccess() {
        return applicationWhereAmIAccess;
    }
    public static boolean showRevStatusPanel() {
        return showRevStatusPanel;
    }

    //hide Go To Jurisdiction settings
    public static boolean isHideGoToJurisdictionInSetting() {
        return hideGoToJurisdictionInSetting;
    }

    //header navigation panel feature visibility
    public static boolean getNavSurveyTabVis() {
        return showSurveyTab;
    }

    public static boolean getNavPhaseTabVis() {
        return showPhaseTab;
    }

    public static boolean getNavJurisdictionVis() {
        return showJurisdictionTab;
    }
}
