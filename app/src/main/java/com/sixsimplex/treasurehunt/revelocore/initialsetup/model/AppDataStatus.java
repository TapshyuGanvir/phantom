package com.sixsimplex.treasurehunt.revelocore.initialsetup.model;

import android.content.Context;

public class AppDataStatus {
    private static AppDataStatus INSTANCE = null;
    boolean surveySelected = false;
    boolean surveySelectionNeeded = false;
    boolean surveySelectionOnGoing = false;
    String surveyName = "";
    boolean singleSurvey = false;
    boolean surveyHasPhases = false;
    boolean singlePhase = false;
    boolean phaseSelected = false;
    boolean phaseSelectionNeeded = false;
    boolean phaseSelectionOnGoing = false;
    String phaseName = "";
    boolean redbDonloadRequired = false;
    boolean redbDonloadOnGoing = false;
    boolean redbDonloaded = false;
    boolean jurisdictionAssigned = false;
    boolean selectedJurisdictionEqualsAssigned = false;
    boolean jurisdictionDialogNeeded = false;
    boolean jurisdictionSelectionOnGoing = false;
    boolean jurisdictionSelectionDone = false;
    boolean metadataDbDonloadRequired = false;
    boolean metadataDbDonloadOnGoing = false;
    boolean metadataDbDonloaded = false;
    boolean dataDbDonloadRequired = false;
    boolean dataDbDonloadOnGoing = false;
    boolean dataDbDonloaded = false;

    AppDataStatus(Context context) {

    }

    public static AppDataStatus getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppDataStatus(context);
        }
        return INSTANCE;
    }

    public void setSurveyVariables(boolean surveySelected,
                                   boolean surveySelectionNeeded,
                                   boolean surveySelectionOnGoing,
                                   String surveyName,
                                   boolean singleSurvey,
                                   boolean surveyHasPhases) {
        this.surveySelected = surveySelected;
        this.surveySelectionNeeded = surveySelectionNeeded;
        this.surveySelectionOnGoing = surveySelectionOnGoing;
        this.surveyName = surveyName;
        this.singleSurvey = singleSurvey;
        this.surveyHasPhases = surveyHasPhases;
    }

    boolean isSurveySelected(String surveyName) {
        if (this.surveyName == null || this.surveyName.isEmpty())
            return false;
        return this.surveyName.equalsIgnoreCase(surveyName);
    }

    boolean isSurveySelectionOnGoing(String surveyName){
        if (this.surveyName == null || this.surveyName.isEmpty())
            return false;
        if (this.surveyName.equalsIgnoreCase(surveyName)){
            return surveySelectionOnGoing;
        }else {
            return false;
        }
    }
    boolean isSurveySelectionNeeded(){
        if (this.surveyName == null || this.surveyName.isEmpty())
            return true;
       else if(surveySelectionOnGoing){
           return false;
        }else  {
            return surveySelectionNeeded;
        }
    }

    public void resetSurveyVariables() {
        this. surveySelected = false;
        this. surveySelectionNeeded = false;
        this. surveySelectionOnGoing = false;
        this.surveyName = "";
        this. singleSurvey = false;
        this.surveyHasPhases=false;
    }

}
