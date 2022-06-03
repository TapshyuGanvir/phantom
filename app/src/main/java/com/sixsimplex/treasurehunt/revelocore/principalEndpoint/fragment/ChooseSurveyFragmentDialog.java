package com.sixsimplex.treasurehunt.revelocore.principalEndpoint.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.sixsimplex.treasurehunt.revelocore.principalEndpoint.view.ISelectedSurveyName;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.ISelectSurvey;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import java.util.List;

public class ChooseSurveyFragmentDialog extends BottomSheetDialogFragment implements ISelectSurvey {

    private final String className = "ChooseSurveyFragmentDialog";
    private Activity parentActivity;
    private List<Survey> surveyNameList;
    private String currentSurveyName = "";
    private String newlySelectedSurvey = "";
    private ISelectedSurveyName iOnSurveyNameSelected;
    private int requestType;
    private LineAdapter mAdapter;

    public static ChooseSurveyFragmentDialog newInstance(String currentSurveyName, List<Survey> surveyNameList,
                                                         Activity activity, int requestType,
                                                         ISelectedSurveyName iOnSurveyNameSelected) {

        ChooseSurveyFragmentDialog fragmentDialog = new ChooseSurveyFragmentDialog();
        fragmentDialog.setSurveyName(currentSurveyName);
        fragmentDialog.setParentActivity(activity);
        fragmentDialog.setSurveyNameList(surveyNameList);
        fragmentDialog.setRequestType(requestType);
        fragmentDialog.setISelectedSurveyName(iOnSurveyNameSelected);
        return fragmentDialog;
    }



    private void setParentActivity(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    private void setSurveyName(String currentSurveyName) {
        this.currentSurveyName = currentSurveyName;
        //newlySelectedSurvey = currentSurveyName;
    }

    private void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    private void setSurveyNameList(List<Survey> surveyNameList) {
        this.surveyNameList = surveyNameList;
    }

    private void setISelectedSurveyName(ISelectedSurveyName iSelectedSurveyName) {
        this.iOnSurveyNameSelected = iSelectedSurveyName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /* @NonNull
     @Override
     public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

         Dialog dialog;
         if (requestType == AppConstants.CHANGE_SURVEY_REQUEST) {
             dialog = super.onCreateDialog(savedInstanceState);
         } else {
             dialog = new Dialog(parentActivity, getTheme()) {
                 @Override
                 public void onBackPressed() {
                     if (!newlySelectedSurvey.isEmpty()) {
                         // UserInfoPreferenceUtility.resetSelectedSurveyName();
                     }
                     if (UserInfoPreferenceUtility.getSurveyName().isEmpty()) {
                         onCancelSelection("Selecting a project, is a crucial step to use this app. " +
                                 "Do you really want to cancel and logout?");
                     } else {
                         ChooseSurveyFragmentDialog.this.dismiss();
                     }
                 }
             };
         }

         AppMethods.closeKeyboard(dialog.getCurrentFocus(), parentActivity);

         dialog.setCancelable(false);
         dialog.setCanceledOnTouchOutside(false);
         return dialog;
     }*/
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_choose_survey,
                container, false);
        return view;
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            super.onViewCreated(view, savedInstanceState);

            try {
                TextView tv_headertext = view.findViewById(R.id.tv_headertext);
                tv_headertext.setText("Select Project");
                TextView noSurveysAvailable = view.findViewById(R.id.noSurveyTv);
                noSurveysAvailable.setVisibility(View.GONE);
                TextView selectedSurveyName = view.findViewById(R.id.selectedSurveyName);
                selectedSurveyName.setVisibility(View.GONE);
                RecyclerView surveyRecyclerView = view.findViewById(R.id.surveyRecyclerView);
                TextView selectSurveyFabBt = view.findViewById(R.id.selectSurveyFabBt);
                ImageView chooseSurveyBackIv = view.findViewById(R.id.chooseSurveyBackIv);

                //  if (requestType == AppConstants.CHANGE_SURVEY_REQUEST) {
                chooseSurveyBackIv.setVisibility(View.VISIBLE);
                // }

                chooseSurveyBackIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!newlySelectedSurvey.isEmpty()) {
                            // UserInfoPreferenceUtility.resetSelectedSurveyName();
                        }
                        if (UserInfoPreferenceUtility.getSurveyName().isEmpty()) {
                            onCancelSurveySelection("Selecting a project, is a crucial step to use this app. " +
                                    "Do you really want to cancel and logout?");
                        } else {
                            //ChooseSurveyFragmentDialog.this.dismiss();
                            try {
                                ChooseSurveyFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                            }catch (Exception e){
                                ReveloLogger.error(className,"ChooseSurveyFragmentDialog","Error closing onCancelPhaseSelection alert fragment "+e.getMessage());
                                e.printStackTrace();
                            }
                        }

                    }
                });

                selectSurveyFabBt.setOnClickListener(v -> {
                    if (mAdapter != null) {
                        newlySelectedSurvey = mAdapter.getSelectedSurveyTvTag();
                    }


                    //1. if no survey was selected now, check if previous survey exists.
                    //--- a.if yes, show that selected(done already) and do nothing.on cancelling selection, go to home, show originally selected survey contents.
                    //--- b.if no, don't let user proceed without selecting one survey. on cancelling selection, go to login.
                    //2. if  survey was selected now, check if previous survey exists.
                    //--- a.if yes, check if they are same. if yes, show that selected(done already) and do nothing.on cancelling selection, go to home, show originally selected survey contents.
                    //--- b.if no, proceed with selected survey. on cancelling selection, go to login.

                    if (newlySelectedSurvey.isEmpty()) {
                        if (currentSurveyName.isEmpty()) {
                            //do not let proceed
                            AppMethods.showAlertDialog(parentActivity, "Select survey", "Ok", null,
                                    DialogInterface::dismiss, null);
                        } else {
                            //show same survey
                           /* String msg = "You already have this survey selected. Please select a different survey or press back button to cancel operation";
                            AppMethods.showAlertDialog(parentActivity, msg, "Ok", null,
                                    DialogInterface::dismiss, null);*/
                            iOnSurveyNameSelected.s_selectedSurveyName(currentSurveyName);
                           // ChooseSurveyFragmentDialog.this.dismiss();
                            try {
                                ChooseSurveyFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                            }catch (Exception e){
                                ReveloLogger.error(className,"ChooseSurveyFragmentDialog","Error closing select survey alert fragment "+e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (currentSurveyName.isEmpty()) {
                            //proceed
                            iOnSurveyNameSelected.s_selectedSurveyName(newlySelectedSurvey);
                          //  ChooseSurveyFragmentDialog.this.dismiss();
                            try {
                                ChooseSurveyFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                            }catch (Exception e){
                                ReveloLogger.error(className,"ChooseSurveyFragmentDialog","Error closing onCancelPhaseSelection alert fragment "+e.getMessage());
                                e.printStackTrace();
                            }
                        } else if (currentSurveyName.equalsIgnoreCase(newlySelectedSurvey)) {
                            //show same survey
                            String msg = "You already have this survey selected. Please select a different survey or press back button to cancel operation";
                            AppMethods.showAlertDialog(parentActivity, msg, "Ok", null,
                                    DialogInterface::dismiss, null);
                        } else {
                            //proceed
                            iOnSurveyNameSelected.s_selectedSurveyName(newlySelectedSurvey);
                          //  ChooseSurveyFragmentDialog.this.dismiss();
                            try {
                                ChooseSurveyFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                            }catch (Exception e){
                                ReveloLogger.error(className,"ChooseSurveyFragmentDialog","Error closing onCancelPhaseSelection alert fragment "+e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }


                    /*if(newlySelectedSurvey.isEmpty() && !currentSurveyName.isEmpty()){//if no survey was selected, make selection=original survey
                        newlySelectedSurvey = currentSurveyName;
                    }
                    if (TextUtils.isEmpty(newlySelectedSurvey) && currentSurveyName.isEmpty()) {//if no survey was selected now and this is first time i.e. no currentsurveyname=empty, then dont let user proceed without selecting one
                        AppMethods.showAlertDialog(parentActivity, "Select survey", "Ok", null,
                                DialogInterface::dismiss, null);

                    } else if (currentSurveyName.equalsIgnoreCase(newlySelectedSurvey)) {//if no survey was selected now and this is first time i.e. no currentsurveyname=empty, then dont let user proceed without selecting one
                        String msg = "You already have this survey selected. Please select a different survey or press back button to cancel operation";
                        AppMethods.showAlertDialog(parentActivity, msg, "Ok", null,
                                DialogInterface::dismiss, null);

                    } else {
                        iOnSurveyNameSelected.selectedSurveyName(newlySelectedSurvey);
                    }*/
                });

                setUp(surveyRecyclerView, noSurveysAvailable);

            } catch (Exception e) {
                e.printStackTrace();
                ReveloLogger.error(className, "onViewCreated", String.valueOf(e.getCause()));
            }
        }
    }

    private void setUp(RecyclerView surveyRecyclerView, TextView noSurveysAvailable) {
        if (surveyNameList != null && !surveyNameList.isEmpty()) {
            surveyRecyclerView.setVisibility(View.VISIBLE);
            noSurveysAvailable.setVisibility(View.GONE);
           /* LinearLayoutManager mLayoutManager = new LinearLayoutManager(parentActivity);
            mLayoutManager.setOrientation(RecyclerView.VERTICAL);
            surveyRecyclerView.setLayoutManager(mLayoutManager);
            surveyRecyclerView.setItemAnimator(new DefaultItemAnimator());*/

            GridLayoutManager gridLayoutManager = new GridLayoutManager(parentActivity, 2);
            surveyRecyclerView.setLayoutManager(gridLayoutManager);
            surveyRecyclerView.setItemAnimator(new DefaultItemAnimator());


            mAdapter = new LineAdapter(surveyNameList, parentActivity);
            surveyRecyclerView.setAdapter(mAdapter);
        } else {
            surveyRecyclerView.setVisibility(View.GONE);
            noSurveysAvailable.setVisibility(View.VISIBLE);
        }
    }
    private static void deselect(TextView v) {
        // v.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        v.setSelected(false);
    }
    @Override
    public void onCancelSurveySelection(String message) {
        AppMethods.showAlertDialog(parentActivity, message, "Yes", "No",
                new AppMethods.PositiveBtnCallBack() {
                    @Override
                    public void positiveCallBack(DialogInterface dialog) {
                        iOnSurveyNameSelected.s_onCancellingSelectingSurvey(message);
                        dialog.dismiss();
                        dismiss();
                       // ChooseSurveyFragmentDialog.this.dismiss();
                        try {
                            ChooseSurveyFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                        }catch (Exception e){
                            ReveloLogger.error(className,"ChooseSurveyFragmentDialog","Error closing onCancelPhaseSelection alert fragment "+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                , new AppMethods.NegativeBtnCallBack() {
                    @Override
                    public void negativeCallBack(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
    }

    private class LineAdapter extends RecyclerView.Adapter<LineAdapter.LineHolder> implements View.OnClickListener {

        private final List<Survey> list;
        private final Activity activity;
        private TextView selectedSurveyTv;
        private TextView mlastView;

        private LineAdapter(List<Survey> surveyList, Activity activity) {
            list = surveyList;
            this.activity = activity;
        }

        public String getSelectedSurveyTvTag() {
            if (selectedSurveyTv != null && selectedSurveyTv.getTag() != null)
                return selectedSurveyTv.getTag().toString();
            return "";
        }

        @NonNull
        @Override
        public LineHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LineHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_survey_list, parent, false));
        }

        @Override
        public void onBindViewHolder(LineHolder holder, int position) {

            Survey survey = list.get(position);

            String surveyLabel = survey.getLabel();
            String name = survey.getName();

            TextView nameTv = holder.dialogSurveyNameTv;
            nameTv.setText(surveyLabel);
            nameTv.setOnClickListener(this);
            nameTv.setTag(name);

            if ((newlySelectedSurvey.isEmpty()
                    && !currentSurveyName.isEmpty()
                    && name.equalsIgnoreCase(currentSurveyName))
                    || name.equalsIgnoreCase(newlySelectedSurvey)
                    || list.size() == 1) {
                mlastView = nameTv;
                //nameTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                nameTv.setSelected(true);
            } else {
                //nameTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                nameTv.setSelected(false);
            }
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public void onClick(View v) {

            if (selectedSurveyTv != null) {
                //selectedSurveyTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                selectedSurveyTv.setSelected(false);
                newlySelectedSurvey = "";
            }
            if (mlastView != null) {
                deselect(mlastView);
            }

            if (v instanceof TextView) {
                selectedSurveyTv = (TextView) v;
                //selectedSurveyTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0);
                selectedSurveyTv.setSelected(true);
                newlySelectedSurvey = getSelectedSurveyTvTag();
            }
            mlastView = selectedSurveyTv;
        }

        private class LineHolder extends RecyclerView.ViewHolder {

            private final TextView dialogSurveyNameTv;

            private LineHolder(View itemView) {
                super(itemView);
                dialogSurveyNameTv = itemView.findViewById(R.id.dialogSurveyNameTv);
            }
        }
    }
}