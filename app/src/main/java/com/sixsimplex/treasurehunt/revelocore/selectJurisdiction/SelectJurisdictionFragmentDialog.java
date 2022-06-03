package com.sixsimplex.treasurehunt.revelocore.selectJurisdiction;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.TinkerGraphUtil;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.JurisdictionInfoPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectJurisdictionFragmentDialog extends BottomSheetDialogFragment implements ISelectJurisdictionValues {

    private Activity parentActivity;
    private int requestType;
    private String assignedJurisdictionName, assignedJurisdictionType;
    private LinearLayout selectJurisdictionLl;
    private TextView selectedSurveyName;
    private Map<String, Object> userUpperHierarchyMap;
    private Graph reCmGraph;
    private JSONObject selectionFilter;
    private CheckBox downloadAttachmentCb;
    private ProgressBar progressBar;
    private LinearLayout progressBarLl;
    private ISelectedJurisdictionsData iSelectedJurisdictionsData;

    private String className = "SelectJurisdictionFragmentDialog";

    public static SelectJurisdictionFragmentDialog newInstance(Activity activity, int requestType,
                                                               Graph reCmGraph, String jurisdictionName,
                                                               String jurisdictionType) {

        SelectJurisdictionFragmentDialog fragmentDialog = new SelectJurisdictionFragmentDialog();
        fragmentDialog.setParentActivity(activity);
        fragmentDialog.setRequestType(requestType);
        fragmentDialog.setJurisdictionName(jurisdictionName);
        fragmentDialog.setJurisdictionType(jurisdictionType);
        fragmentDialog.setReCmGraph(reCmGraph);
        return fragmentDialog;
    }

    private void setParentActivity(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    private void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    private void setJurisdictionName(String jurisdictionName) {
        assignedJurisdictionName = jurisdictionName;
    }

    private void setJurisdictionType(String jurisdictionType) {
        assignedJurisdictionType = jurisdictionType;
    }

    private void setReCmGraph(Graph reCmGraph) {
        this.reCmGraph = reCmGraph;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

      /*  Dialog dialog;
        if (requestType == AppConstants.CHANGE_JURISDICTION_REQUEST) {
            dialog = super.onCreateDialog(savedInstanceState);
        } else {
            dialog = new Dialog(parentActivity, getTheme()) {
                @Override
                public void onBackPressed() {
                }
            };
        }

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;*/
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
        //return inflater.inflate(R.layout.dialog_fragment_select_jurisdiction, container);
        View view = inflater.inflate(R.layout.dialog_fragment_select_jurisdiction,
                container, false);
        return view;
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        if (view != null) {

            super.onViewCreated(view, savedInstanceState);

            ReveloLogger.debug(className, "onViewCreated", "Initializing views");
            selectedSurveyName = view.findViewById(R.id.selectedSurveyName);
            String surveyName = UserInfoPreferenceUtility.getSurveyName();
            String surveyNameLabel = UserInfoPreferenceUtility.getSurveyNameLabel();
            String surveyPhaseNameLabel = UserInfoPreferenceUtility.getSurveyPhaseLabel(surveyName);
            String selectedTVText = "";
            if(surveyNameLabel!=null && !surveyNameLabel.isEmpty()){
                selectedTVText = "Project : "+surveyNameLabel;
            }
            if(surveyPhaseNameLabel!=null && !surveyPhaseNameLabel.isEmpty()){
                selectedTVText += "\nPhase   : "+surveyPhaseNameLabel;
            }
            if(selectedTVText.isEmpty()){
                selectedSurveyName.setVisibility(View.GONE);
            }else {
                selectedSurveyName.setVisibility(View.VISIBLE);
                selectedSurveyName.setText(selectedTVText);
            }
            iSelectedJurisdictionsData = (ISelectedJurisdictionsData) getActivity();
            selectJurisdictionLl = view.findViewById(R.id.selectJurisdictionLl);
            downloadAttachmentCb = view.findViewById(R.id.downloadAttachmentCb);
            progressBar = view.findViewById(R.id.progressBar);
            progressBarLl = view.findViewById(R.id.progressBarLl);

            ImageView backIV = view.findViewById(R.id.backIV);

           /* if (requestType == AppConstants.CHANGE_JURISDICTION_REQUEST) {
                backIV.setVisibility(View.VISIBLE);
            }*/
            backIV.setVisibility(View.VISIBLE);
            backIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (requestType == AppConstants.CHANGE_JURISDICTION_REQUEST) {
                        JurisdictionInfoPreferenceUtility.storeJurisdictions(JurisdictionInfoPreferenceUtility.getPreviousJurisdictions());
                        //SelectJurisdictionFragmentDialog.this.dismiss();
                        try {
                            SelectJurisdictionFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                        }catch (Exception e){
                            ReveloLogger.error(className,"SelectJurisdictionFragmentDialog","on back - Error closing fragment "+e.getMessage());
                            e.printStackTrace();
                        }
                    }else {
                        j_onCancelSelectingJurisdiction("Selecting jurisdiction is a crucial step to download databases. Do you really want to cancel?");
                    }
                    //JurisdictionInfoPreferenceUtility.storeJurisdictions(JurisdictionInfoPreferenceUtility.getPreviousJurisdictions());
                }
            });

            // FloatingActionButton selectJurisdictionFabBt = view.findViewById(R.id.selectJurisdictionFabBt);
            // selectJurisdictionFabBt.setOnClickListener(v -> clickOnSelectFabBt());

            TextView nextTv = view.findViewById(R.id.nextbtn);
            nextTv.setOnClickListener(v -> clickOnSelectFabBt());

            createDataBaseFilterView(parentActivity);
        }
    }

    private void clickOnSelectFabBt() {

        ReveloLogger.debug(className, "clickOnSelectFabBt", "click on select jurisdictions event");

        StringBuilder jurisdictionText = new StringBuilder();
        StringBuilder downloadAttachmentText = new StringBuilder();

        String jurisdictionName = "";
        String jurisdictionValue = "";

        JSONArray selectedJurisdictionArray = new JSONArray();
        try {
            boolean isAttachmentDownload = downloadAttachmentCb.isChecked();
            selectionFilter = new JSONObject();
            selectionFilter.put("downloadAttachments", isAttachmentDownload);
            String label = "Download Attachments";
            String value;

            if (isAttachmentDownload) {
                value = "Yes";
            } else {
                value = "No";
            }

            downloadAttachmentText.append(label);
            downloadAttachmentText.append(" : ");
            downloadAttachmentText.append(value);
            jurisdictionText.append(System.getProperty("line.separator")); // Add new Line


            for (int i = 0; i < selectJurisdictionLl.getChildCount(); i++) {

                View filterView = selectJurisdictionLl.getChildAt(i);

                if (filterView != null) {
                    if (filterView instanceof LinearLayout) {

                        View v = ((LinearLayout) filterView).getChildAt(1);
                        if (v instanceof Spinner) {

                            Spinner jurisdictionSpinner = (Spinner) v;
                            String type = String.valueOf(jurisdictionSpinner.getTag());
                            String selectItem = String.valueOf(jurisdictionSpinner.getSelectedItem());

                            selectionFilter.put(type, selectItem);

                            jurisdictionName = AppMethods.capitaliseFirstLatter(type);
                            jurisdictionValue = AppMethods.capitaliseFirstLatter(selectItem);

                            jurisdictionText.append(jurisdictionName);
                            jurisdictionText.append(" : ");
                            jurisdictionText.append(jurisdictionValue);
                            jurisdictionText.append(System.getProperty("line.separator")); // Add new Line

                            try {
                                JSONObject jurisdictionObject = new JSONObject();
                                jurisdictionObject.put("name", jurisdictionName);
                                jurisdictionObject.put("value", jurisdictionValue);
                                jurisdictionObject.put("index", i);
                                selectedJurisdictionArray.put(jurisdictionObject);

                            } catch (Exception e) {
                                e.printStackTrace();
                                ReveloLogger.error(className, "clickOnSelectFabBt", String.valueOf(e.getCause()));
                            }
                        }
                    }
                }
            }

            Log.e("jurisdictionName", jurisdictionName);
            Log.e("jurisdictionValue", jurisdictionValue);

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "clickOnSelectFabBt", String.valueOf(e.getCause()));
        }

        final String filterString = selectionFilter.toString();

        String filterJson = JurisdictionInfoPreferenceUtility.getJurisdictions();

        if (filterString != null && !filterString.isEmpty()) {

            if (filterString.equalsIgnoreCase(filterJson)) {
                String msg = "You already have this jurisdiction selected. Please select a different jurisdiction or press back button to cancel operation.";
                AppMethods.showAlertDialog(parentActivity, msg, "Ok", null,
                        DialogInterface::dismiss, null);


            } else {

                String confirmationText = "Are you sure you want to download data for following jurisdiction?"
                        + System.getProperty("line.separator") + jurisdictionText + downloadAttachmentText;


                AppMethods.showAlertDialog(parentActivity, confirmationText, "Yes", "No",
                        dialog -> {
                            Log.e("jurisdictionFilter", filterString);

                            if (selectedJurisdictionArray.length() > 0) {
                                try {
                                    JSONObject jsonObject = selectedJurisdictionArray.getJSONObject(selectedJurisdictionArray.length() - 2);
                                    String name = jsonObject.getString("name");
                                    String value = jsonObject.getString("value");
                                    JurisdictionInfoPreferenceUtility.storeSelectedJurisdictionName(name);
                                    JurisdictionInfoPreferenceUtility.storeSelectedJurisdictionType(value);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ReveloLogger.error(className, "clickOnSelectFabBt", String.valueOf(e.getCause()));
                                }
                            }

                            iSelectedJurisdictionsData.getSelectedJurisdictionsData(filterString, AppConstants.CREATE_DATA_GP_FILE);
                            dialog.dismiss();
                            dismiss();
                           // SelectJurisdictionFragmentDialog.this.dismiss();
                            try {
                                SelectJurisdictionFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                            }catch (Exception e){
                                ReveloLogger.error(className,"SelectJurisdictionFragmentDialog","on confirm - Error closing fragment "+e.getMessage());
                                e.printStackTrace();
                            }
                        }, DialogInterface::dismiss);

            }
        } else {
            String msg = "Select correct jurisdiction.";
            AppMethods.showAlertDialog(parentActivity, msg, "Ok", null,
                    DialogInterface::dismiss, null);
        }
    }

    private void createDataBaseFilterView(Activity activity) {

        try {

            ReveloLogger.debug(className, "createDataBaseFilterView", "Creating jurisdictions hierarchy view.");

            Vertex rootVertex = TinkerGraphUtil.findRootVertex(reCmGraph);

            if (rootVertex != null) {
                Vertex assignedVertex = reCmGraph.getVertex(assignedJurisdictionType);

                if (assignedVertex != null) {
                    String idProperty = assignedVertex.getProperty(GraphConstants.ID_PROPERTY);
                    /*HashMap<String, Attribute> propertiesHashmap = assignedVertex.getProperty(GraphConstants.PROPERTIES_LIST);
                    String idPropertyDataType = propertiesHashmap.get(idProperty).getType();*/
                    String idPropertyDataType = "string";
                    progressBarLl.setVisibility(View.VISIBLE);
                    progressBarLl.setClickable(true);
                    new SelectJurisdictionDialogAsync(rootVertex, idProperty, idPropertyDataType,assignedJurisdictionName, assignedJurisdictionType, activity,
                            this).execute(true);

                    /*userUpperHierarchyMap = ReDbTable.upperHierarchyMap(rootVertex, idProperty,
                            assignedJurisdictionName, assignedJurisdictionType, activity);

                    if (!userUpperHierarchyMap.isEmpty()) {

                        String rootVertexName = rootVertex.getProperty(GraphConstants.NAME);
                        if (userUpperHierarchyMap.containsKey(rootVertexName)) {
                            Object value = userUpperHierarchyMap.get(rootVertexName);

                            if (value != null) {
                                List<String> valuesList = new ArrayList<>();
                                valuesList.add(String.valueOf(value));
                                createSpinner(activity, rootVertexName, false, valuesList);
                            }
                        }
                    }*/
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "createDataBaseFilterView", String.valueOf(e.getCause()));
        }
    }

    private void createSpinner(final Activity activity, final String spinnerName, boolean isEnabled, List<String> spinnerValues) {

        try {

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 2);

            LinearLayout spinnerLinerLayout = new LinearLayout(activity);
            spinnerLinerLayout.setTag(spinnerName + "_LinearLayout");
            spinnerLinerLayout.setOrientation(LinearLayout.VERTICAL);
            spinnerLinerLayout.setLayoutParams(layoutParams);
            spinnerLinerLayout.setBackground(getResources().getDrawable(R.drawable.border));

            TextView spinnerTv = new TextView(activity);
            spinnerTv.setTextSize(18);
            spinnerTv.setText(" Select " + spinnerName);

            final Spinner spinner = new Spinner(activity);
            spinner.setEnabled(isEnabled);

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, spinnerValues);
            spinner.setAdapter(spinnerArrayAdapter);
            spinner.setSelection(0);

            spinner.setTag(spinnerName);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> view, View arg1, int pos, long arg3) {
                    try {
                        String value = String.valueOf(view.getItemAtPosition(pos));
                        clickOnSpinnerOption(spinnerName, value, activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ReveloLogger.error(className, "createSpinner", "Error occured while on item selected listener " + e.getCause());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

            spinnerLinerLayout.addView(spinnerTv);
            spinnerLinerLayout.addView(spinner);
            selectJurisdictionLl.addView(spinnerLinerLayout);

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "createSpinner", String.valueOf(e.getCause()));
        }
    }

    private void clickOnSpinnerOption(String spinnerName, String value, Activity activity) {
        try {
            removeLayout(spinnerName);

            if (!value.equalsIgnoreCase("All")) {
                Vertex spinnerVertex = reCmGraph.getVertex(spinnerName);

                if (spinnerVertex != null) {
                    boolean isReference = spinnerVertex.getProperty(GraphConstants.IS_REFERENCE);
                    String spinnerColumnName = spinnerVertex.getProperty(GraphConstants.ID_PROPERTY);
                    if (!isReference) {

                        Iterable<Vertex> vertexIterable = spinnerVertex.getVertices(Direction.OUT);
                        for (Vertex vertex : vertexIterable) {
                            if (vertex != null) {

                                String requiredColumnName = vertex.getProperty(GraphConstants.ID_PROPERTY);

                                String nextSpinnerName = vertex.getProperty(GraphConstants.NAME);
                                if (userUpperHierarchyMap.containsKey(nextSpinnerName)) {
                                    Object nextSpinnerValue = userUpperHierarchyMap.get(nextSpinnerName);

                                    if (nextSpinnerValue != null) {
                                        List<String> valuesList = new ArrayList<>();
                                        valuesList.add(String.valueOf(nextSpinnerValue));
                                        createSpinner(activity, nextSpinnerName, false, valuesList);
                                    }
                                } else {

                                    progressBarLl.setVisibility(View.VISIBLE);
                                    progressBarLl.setClickable(true);

                                    new SelectJurisdictionDialogAsync(nextSpinnerName, spinnerColumnName, value, requiredColumnName, activity,
                                            this).execute(false);

                                    // List<String> valueList = ReDbTable.getEntityValues(spinnerColumnName, value, requiredColumnName, activity);
                                    // createSpinner(activity, nextSpinnerName, true, valueList);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "clickOnSpinnerOption", String.valueOf(e.getCause()));
        }
    }

    private void removeLayout(String parentName) {
        Vertex parentVertex = reCmGraph.getVertex(parentName);
        List<String> childList = getAllChild(parentVertex, null);
        if (childList != null) {
            for (int i = 0; i < childList.size(); i++) {
                LinearLayout linearLayout = selectJurisdictionLl.findViewWithTag(childList.get(i) + "_LinearLayout");
                if (linearLayout != null) {
                    selectJurisdictionLl.removeView(linearLayout);
                }
            }
        }
    }

    private static List<String> getAllChild(Vertex rootVertex, List<String> childList) {

        Iterable<Vertex> vertexIterable = rootVertex.getVertices(Direction.OUT);
        for (Vertex internalVertex : vertexIterable) {
            String name = internalVertex.getProperty(GraphConstants.NAME);
            if (childList == null) {
                childList = new ArrayList<>();
            }
            childList.add(name);

            getAllChild(internalVertex, childList);
        }
        return childList;
    }

    @Override
    public void j_spinnerUpperHierarchy(Map<String, Object> upperHierarchy, Vertex rootVertex) {

        progressBarLl.setVisibility(View.GONE);
        progressBarLl.setClickable(false);

        userUpperHierarchyMap = upperHierarchy;

        if (!userUpperHierarchyMap.isEmpty()) {

            String rootVertexName = rootVertex.getProperty(GraphConstants.NAME);
            if (userUpperHierarchyMap.containsKey(rootVertexName)) {
                Object value = userUpperHierarchyMap.get(rootVertexName);

                if (value != null) {
                    List<String> valuesList = new ArrayList<>();
                    valuesList.add(String.valueOf(value));
                    createSpinner(parentActivity, rootVertexName, false, valuesList);
                }
            }
        }
    }

    @Override
    public void j_spinnerLowerHierarchy(List<String> valueList, String nextSpinnerName) {

        progressBarLl.setVisibility(View.GONE);
        progressBarLl.setClickable(false);

        createSpinner(parentActivity, nextSpinnerName, true, valueList);
    }

    @Override
    public void j_onErrorSelectingJurisdiction(String message) {
        progressBarLl.setVisibility(View.GONE);
        progressBarLl.setClickable(false);

        AppMethods.showAlertDialog(parentActivity, message, "Ok", null, new AppMethods.PositiveBtnCallBack() {
                    @Override
                    public void positiveCallBack(DialogInterface dialog) {
                        iSelectedJurisdictionsData.errorGettingJurisdictionData(message);
                        dialog.dismiss();
                        dismiss();
                      //  SelectJurisdictionFragmentDialog.this.dismiss();
                        try {
                            SelectJurisdictionFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                        }catch (Exception e){
                            ReveloLogger.error(className,"SelectJurisdictionFragmentDialog","on error - Error closing fragment "+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
               , null);

        /*iSelectedJurisdictionsData.errorGettingJurisdictionData("Could not get data from org boundaries while creating database. Please contact admin.");
        dismiss();*/
    }

    @Override
    public void j_onCancelSelectingJurisdiction(String message) {
        AppMethods.showAlertDialog(parentActivity, message, "Yes", "No",
                new AppMethods.PositiveBtnCallBack() {
                    @Override
                    public void positiveCallBack(DialogInterface dialog) {
                        iSelectedJurisdictionsData.cancelGettingJurisdictionData(message,requestType);
                        dialog.dismiss();
                        dismiss();
                      //  SelectJurisdictionFragmentDialog.this.dismiss();
                        try {
                            SelectJurisdictionFragmentDialog.this.dismissAllowingStateLoss();//.dismiss();
                        }catch (Exception e){
                            ReveloLogger.error(className,"SelectJurisdictionFragmentDialog","on cancel - Error closing fragment "+e.getMessage());
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

}
