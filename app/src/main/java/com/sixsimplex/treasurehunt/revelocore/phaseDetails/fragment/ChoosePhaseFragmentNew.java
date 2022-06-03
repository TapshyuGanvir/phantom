package com.sixsimplex.treasurehunt.revelocore.phaseDetails.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.phaseDetails.ISelectPhase;
import com.sixsimplex.treasurehunt.revelocore.phaseDetails.model.Phase;
import com.sixsimplex.treasurehunt.revelocore.phaseDetails.view.IPhaseSelection;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SurveyPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChoosePhaseFragmentNew extends BottomSheetDialogFragment implements ISelectPhase {

    private Activity parentActivity;
    private  String currentSurveyName="";
    private  String currentPhaseName="";
    private  String newlySelectedPhaseName="";
    private IPhaseSelection iOnPhaseSelection;
    private int requestType;
    private LineAdapter mAdapter;
    private final String className = "ChooseSurveyFragmentDialog";

    public static ChoosePhaseFragmentNew newInstance(String currentSurveyName,
                                                     Activity activity, int requestType,
                                                     IPhaseSelection iOnPhaseSelection) {

        ChoosePhaseFragmentNew fragmentDialog = new ChoosePhaseFragmentNew();
        fragmentDialog.setSurveyName(currentSurveyName);
        fragmentDialog.setParentActivity(activity);
        fragmentDialog.setRequestType(requestType);
        fragmentDialog.setiOnPhaseSelection(iOnPhaseSelection);
        fragmentDialog.setCurrentPhaseName(UserInfoPreferenceUtility.getSurveyPhaseName(currentSurveyName));
        return fragmentDialog;
    }

    private void setSurveyName(String currentSurveyName) {
      this.currentSurveyName = currentSurveyName;
    }

    private void setParentActivity(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    private void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public void setiOnPhaseSelection(IPhaseSelection iOnPhaseSelection) {
        this.iOnPhaseSelection = iOnPhaseSelection;
    }

    public void setCurrentPhaseName(String currentPhaseName) {
        this.currentPhaseName = currentPhaseName;
    }

    private static void deselect(TextView v) {
        // v.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        v.setSelected(false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @NonNull
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog bottomSheetDialog1 = (BottomSheetDialog) dialog;
            View bottomSheet = bottomSheetDialog1.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setPeekHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
                BottomSheetBehavior.from(bottomSheet).setFitToContents(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(false);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(false);
            }
        });

        return bottomSheetDialog;
/*
        Dialog dialog;
        if (requestType == AppConstants.CHANGE_SURVEY_PHASE_REQUEST) {
            dialog = super.onCreateDialog(savedInstanceState);
        } else {
            dialog = new Dialog(parentActivity, getTheme()) {
                @Override
                public void onBackPressed() {
                    if(UserInfoPreferenceUtility.getSurveyName().isEmpty()){
                        onCancelSelection("Selecting a phase, is a crucial step to use this app. " +
                                "Do you really want to cancel?");
                    }else {
                        ChoosePhaseFragmentDialog.this.dismiss();
                    }
                }
            };
        }

        AppMethods.closeKeyboard(dialog.getCurrentFocus(), parentActivity);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_choose_survey_new,
                container, false);
        return view;
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            super.onViewCreated(view, savedInstanceState);

            try {
                RecyclerView surveyRecyclerView = view.findViewById(R.id.surveyRecyclerView);
                setUp(surveyRecyclerView);

            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "onViewCreated", String.valueOf(e.getCause()));
            }
        }
    }

    private void setUp(RecyclerView surveyRecyclerView) {
        Survey survey = SurveyPreferenceUtility.getSurvey(UserInfoPreferenceUtility.getSurveyName());
        HashMap<String, Phase> phaseMap = survey.getPhasesNameMapFromJson();
        if (phaseMap != null && ! phaseMap.isEmpty()) {
            surveyRecyclerView.setVisibility(View.VISIBLE);
            GridLayoutManager mLayoutManager = new GridLayoutManager(parentActivity, 2);
            surveyRecyclerView.setLayoutManager(mLayoutManager);
            surveyRecyclerView.setItemAnimator(new DefaultItemAnimator());

            List<Phase> phaseList = new ArrayList<>(phaseMap.values());
            mAdapter = new LineAdapter(phaseList, parentActivity);
            surveyRecyclerView.setAdapter(mAdapter);
        }
        else {
            surveyRecyclerView.setVisibility(View.GONE);
            //go to download data
        }
    }

    public void onPhaseSelect(String newlySelectedPhaseName){

            //1. if no survey was selected now, check if previous survey exists.
            //--- a.if yes, show that selected(done already) and do nothing.on cancelling selection, go to home, show originally selected survey contents.
            //--- b.if no, don't let user proceed without selecting one survey. on cancelling selection, go to login.
            //2. if  survey was selected now, check if previous survey exists.
            //--- a.if yes, check if they are same. if yes, show that selected(done already) and do nothing.on cancelling selection, go to home, show originally selected survey contents.
            //--- b.if no, proceed with selected survey. on cancelling selection, go to login.
        try {
            if (newlySelectedPhaseName.isEmpty()) {
                if (currentPhaseName.isEmpty()) {
                    //do not let proceed
                    AppMethods.showAlertDialog(parentActivity, "Select a phase", "Ok", null,
                            DialogInterface::dismiss, null);
                } else {
                    //show same survey
                    iOnPhaseSelection.onPhaseSelected(currentSurveyName, currentPhaseName);
                    //ChoosePhaseFragmentNew.this.dismiss();
                            /*String msg = "You already have this phase selected. Please select a different phase or press back button to choose another project.";
                            AppMethods.showAlertDialog(parentActivity, msg, "Ok", null,
                                    DialogInterface::dismiss, null);*/
                }
            }
            else {
                if (currentPhaseName.isEmpty()) {
                    //proceed
                    iOnPhaseSelection.onPhaseSelected(currentSurveyName, newlySelectedPhaseName);
                    //ChoosePhaseFragmentNew.this.dismiss();
                } else if (currentPhaseName.equalsIgnoreCase(newlySelectedPhaseName)) {
                    //show same survey
                    iOnPhaseSelection.onPhaseSelected(currentSurveyName, newlySelectedPhaseName);
                    //ChoosePhaseFragmentNew.this.dismiss();
                            /*String msg = "You already have this phase selected. Please select a different phase or press back button to choose another project.";
                            AppMethods.showAlertDialog(parentActivity, msg, "Ok", null,
                                    DialogInterface::dismiss, null);*/
                } else {
                    //proceed
                    iOnPhaseSelection.onPhaseSelected(currentSurveyName, newlySelectedPhaseName);
                    //ChoosePhaseFragmentNew.this.dismiss();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancelPhaseSelection(String message) {
        AppMethods.showAlertDialog(parentActivity, message, "Yes", "No",
                new AppMethods.PositiveBtnCallBack() {
                    @Override
                    public void positiveCallBack(DialogInterface dialog) {
                        iOnPhaseSelection.onPhaseSelectionCancelled();
                        dialog.dismiss();
                        dismiss();
              //  ChoosePhaseFragmentNew.this.dismiss();
                try {
                    ChoosePhaseFragmentNew.this.dismissAllowingStateLoss();//.dismiss();
                }catch (Exception e){
                    ReveloLogger.error(className,"ChoosePhaseFragmentNew","Error closing onCancelPhaseSelection alert fragment "+e.getMessage());
                    e.printStackTrace();
                    }
                }
        }, new AppMethods.NegativeBtnCallBack() {
                    @Override
                    public void negativeCallBack(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
    }

    private class LineAdapter extends RecyclerView.Adapter<LineAdapter.LineHolder> implements View.OnClickListener {

        private final List<Phase> list;
        TextView selectedPhaseNextTv;
        private final Activity activity;
        private TextView selectedPhaseTv;
        private TextView mlastView;

        private LineAdapter(List<Phase> phaseList, Activity activity) {
            list = phaseList;
            this.activity = activity;
        }

        public String getSelectedPhaseTvText() {
            try {
                if (selectedPhaseTv != null && selectedPhaseTv.getText() != null)
                    return selectedPhaseTv.getText().toString();
            }catch (Exception e){
                e.printStackTrace();
            }
            return "";
        }

        @NonNull
        @Override
        public LineHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LineHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_survey_list, parent, false));
        }

        @Override
        public void onBindViewHolder(LineHolder holder, int position) {

            Phase phase = list.get(position);

            String phaseLabel = phase.getLabel();
            String name = phase.getName();

            TextView nameTv = holder.dialogPhaseNameTv;
            nameTv.setText(phaseLabel);
            nameTv.setSelected(false);
            nameTv.setOnClickListener(this);
            nameTv.setTag(name);

          /*  if ((newlySelectedPhaseName.isEmpty() && !currentPhaseName.isEmpty() && name.equalsIgnoreCase(currentPhaseName))
                    ||name.equalsIgnoreCase(newlySelectedPhaseName)
                    ||list.size()==1) {
                mlastView = nameTv;
               // nameTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                nameTv.setSelected(true);
            } else {
                //nameTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                nameTv.setSelected(false);
            }*/
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public void onClick(View v) {

            if (selectedPhaseTv != null) {
                //selectedPhaseTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                selectedPhaseTv.setSelected(false);
                newlySelectedPhaseName = "";
            }
            if (mlastView != null) {
                deselect(mlastView);
            }

            if (v instanceof TextView) {
                selectedPhaseTv = (TextView) v;
                //selectedPhaseTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                selectedPhaseTv.setSelected(true);
                newlySelectedPhaseName = getSelectedPhaseTvTag();
            }
            mlastView = selectedPhaseTv;
            onPhaseSelect(newlySelectedPhaseName);
        }

        public String getSelectedPhaseTvTag() {
            if (selectedPhaseTv != null && selectedPhaseTv.getTag() != null)
                return selectedPhaseTv.getTag().toString();
            return "";
        }

        private  class LineHolder extends RecyclerView.ViewHolder {

            private final TextView dialogPhaseNameTv;

            private LineHolder(View itemView) {
                super(itemView);
                dialogPhaseNameTv = itemView.findViewById(R.id.dialogSurveyNameTv);
            }
        }
    }
}