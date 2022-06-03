package com.sixsimplex.treasurehunt.revelocore.initialsetup;

import static com.sixsimplex.treasurehunt.revelocore.util.AppMethods.stopLocationService;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.sixsimplex.treasurehunt.R;

import com.sixsimplex.treasurehunt.revelocore.createAndDownloadFile.DownloadFileForegroundService;
import com.sixsimplex.treasurehunt.revelocore.editprofile.EditProfile;
import com.sixsimplex.treasurehunt.revelocore.editprofile.GetProfilePicAsyncTask;
import com.sixsimplex.treasurehunt.revelocore.geopackage.models.Jurisdiction;
import com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil.EditMetaDataTable;
import com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil.ReDbTable;

import com.sixsimplex.treasurehunt.revelocore.insight.Insight;
import com.sixsimplex.treasurehunt.revelocore.login.view.LoginActivity;

import com.sixsimplex.treasurehunt.revelocore.obConceptModel.OrgBoundaryConceptModel;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.model.OBDataModel;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.sharedPreference.OrgBoundaryPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.view.IOrgBoundaryConceptModel;
import com.sixsimplex.treasurehunt.revelocore.phaseDetails.fragment.ChoosePhaseFragmentNew;
import com.sixsimplex.treasurehunt.revelocore.phaseDetails.model.Phase;
import com.sixsimplex.treasurehunt.revelocore.phaseDetails.view.IPhaseSelection;
import com.sixsimplex.treasurehunt.revelocore.principalEndpoint.PrincipalEndpoint;
import com.sixsimplex.treasurehunt.revelocore.principalEndpoint.fragment.ChooseSurveyFragmentNew;
import com.sixsimplex.treasurehunt.revelocore.principalEndpoint.view.IPrincipleEndpointView;
import com.sixsimplex.treasurehunt.revelocore.principalEndpoint.view.ISelectedSurveyName;
import com.sixsimplex.treasurehunt.revelocore.service.LocationReceiverService;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.SurveyDetails;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.model.Survey;
import com.sixsimplex.treasurehunt.revelocore.surveyDetails.view.ISurveyDetails;
import com.sixsimplex.treasurehunt.Phantom1.app.view.DeliveryMainActivity;
import com.sixsimplex.treasurehunt.revelocore.upload.IUpload;
import com.sixsimplex.treasurehunt.revelocore.upload.Upload;

import com.sixsimplex.treasurehunt.revelocore.userProfile.FetchUserProfile;
import com.sixsimplex.treasurehunt.revelocore.userProfile.FetchUserProfileListener;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.TinkerGraphUtil;
import com.sixsimplex.treasurehunt.revelocore.util.ToastUtility;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.Utilities;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.BottomSheetTagConstants;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.InfoBottomSheet;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.InfoBottomSheetInterface;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.progressdialog.PercentageProgressBar;
import com.sixsimplex.treasurehunt.revelocore.util.progressdialog.PercentageProgressBarView;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.JurisdictionInfoPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SurveyPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InitializationActivity extends AppCompatActivity implements IPrincipleEndpointView, IInitializationView, InfoBottomSheetInterface, NavigationView.OnNavigationItemSelectedListener, FetchUserProfileListener, IUpload {
    private final static String FRAGMENT_SURVEYS = "FRAGMENT_SURVEYS";
    private final static String FRAGMENT_PHASES = "FRAGMENT_PHASES";
    private final static boolean SHOW = true;
    private final static boolean HIDE = false;
    private final String className = "InitializationActivity";
    @BindView(R.id.progressDescriptionLayout)
    LinearLayout progressDescriptionLayout;
    @BindView(R.id.tvSelectionInstruction)
    TextView tvSelectionInstruction;

    @BindView(R.id.parentOrgBoundariesLayout)
    LinearLayout parentOrgBoundariesLayout;
    @BindView(R.id.parentMetadataLayout)
    LinearLayout parentMeatadataLayout;


    @BindView(R.id.redbCheckIV)
    ImageView redbCheckIV;
    @BindView(R.id.metadataCheckIV)
    ImageView metadataCheckIV;
    @BindView(R.id.metadataProgressTV)
    TextView metadataProgressTV;
    @BindView(R.id.redbProgressTV)
    TextView redbProgressTV;
    @BindView(R.id.loadingPhaseTv)
    TextView loadingPhaseTv;
    /*@BindView(R.id.titleTextTv)
    TextView titleTextTv;*/
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.initialLoading)
    RelativeLayout initialLoading;
    boolean phaseORSurveyChanged = false;
    LinearLayout parentforProgressLayout = null;
    Intent onCreateIntent = null;
    boolean wasAppConfiguredBefore = false;

    boolean mServiceConnected = false;
    boolean mBound = false;
    private Animation rotate_down;
    private boolean firstTimeDrawerOpen = true;
    private ChooseSurveyFragmentNew chooseSurveyFragmentDialog;
    private ChoosePhaseFragmentNew choosePhaseFragmentDialog;
    private PercentageProgressBar percentageProgressBar;
    private PercentageProgressBarView percentageProgressBarView;
    //    private ProgressDialog progressUtility;
    private InfoBottomSheet errorMessageBottomSheet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_setup);
        ButterKnife.bind(this);
        ReveloLogger.debug(className, "oncreate", "init activity created");
        ReveloLogger.timeLog(className, "oncreate", "init activity created");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                View header = navigationView.getHeaderView(0);
                TextView mmFirstNameTextView = header.findViewById(R.id.firstNameId);
                TextView mmLastNameTextView = header.findViewById(R.id.lastNameId);
                TextView mmPhoneNumberTextView = header.findViewById(R.id.phoneNumberId);
                ImageView mmUserIcon = header.findViewById(R.id.userIconIV);

                if (! mmFirstNameTextView.getText().equals(UserInfoPreferenceUtility.getFirstName())) {
                    if (UserInfoPreferenceUtility.getFirstName().length() >= 15) {
                        mmFirstNameTextView.setText(UserInfoPreferenceUtility.getFirstName().substring(0, 15) + "...");
                    }
                    else {
                        mmFirstNameTextView.setText(UserInfoPreferenceUtility.getFirstName());
                    }
                }
                if (! mmLastNameTextView.getText().equals(UserInfoPreferenceUtility.getLastName())) {
                    if (UserInfoPreferenceUtility.getFirstName().length() >= 15) {
                        mmLastNameTextView.setText(UserInfoPreferenceUtility.getLastName().substring(0, 15) + "...");
                    }
                    else {
                        mmLastNameTextView.setText(UserInfoPreferenceUtility.getLastName());
                    }
                }
                if (! mmPhoneNumberTextView.getText().equals(UserInfoPreferenceUtility.getPhoneNumber())) {
                    mmPhoneNumberTextView.setText("Phone Number : " + UserInfoPreferenceUtility.getPhoneNumber());
                }
                String mmFileName = UserInfoPreferenceUtility.getUserName() + "ProfilePic.png";
                File mmProfilePic = new File(AppFolderStructure.userProfilePictureFolderPath(InitializationActivity.this) + File.separator + mmFileName);
                if (mmProfilePic.exists()) {
                    if (firstTimeDrawerOpen) {
                        new GetProfilePicAsyncTask(InitializationActivity.this, UserInfoPreferenceUtility.getUserName(), mmUserIcon).execute();
                        firstTimeDrawerOpen = false;
                    }
                    Bitmap myBitmap = BitmapFactory.decodeFile(mmProfilePic.getAbsolutePath());
                    ((ImageView) header.findViewById(R.id.userIconIV)).setImageBitmap(myBitmap);
                }
                else {
                    if (NetworkUtility.checkNetworkConnectivity(InitializationActivity.this)) {
                        new GetProfilePicAsyncTask(InitializationActivity.this, UserInfoPreferenceUtility.getUserName(), mmUserIcon).execute();
                    }
                }

            }
        };
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        ReveloLogger.info(className, "onCreate", "init activity created");
        // titleTextTv.setText("");
        progressDescriptionLayout.setVisibility(View.GONE);

        //hit principal endpoint
        //hit userprofile endpoint
        //download redb
        //get list of surveys
        //download metadatadb
        //get phases
        //show homepage
        /* handleIntent("OnCreateIntent", getIntent());*/
        onCreateIntent = getIntent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ReveloLogger.debug(className, "onpause", "init activity paused");
        try {
            //progressDescriptionLayout.setVisibility(View.GONE);
//            ProgressUtility.dismissProgressDialog(progressUtility);
            if (percentageProgressBar != null) {
                percentageProgressBar.dismiss();
            }
            if (parentforProgressLayout != null) {
                parentforProgressLayout.removeAllViews();
            }
            if (percentageProgressBarView != null) {
                percentageProgressBarView.dismiss();
            }
            try {
                if (errorMessageBottomSheet != null && errorMessageBottomSheet.isVisible()) {
                    errorMessageBottomSheet.dismissAllowingStateLoss();//.dismiss();
                }
            } catch (Exception e) {
                ReveloLogger.error(className, "onPause", "exception closing error dialog " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent("newIntent", intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        updateUi(SHOW);
        ReveloLogger.debug(className, "onresume", "init activity resumed");
        try {
            progressDescriptionLayout.setVisibility(View.GONE);
//            ProgressUtility.dismissProgressDialog(progressUtility);
            /*if (percentageProgressBar != null) {
                percentageProgressBar.dismiss();
            }*/
            if (percentageProgressBarView != null) {
                percentageProgressBarView.dismiss();
            }

            try {
                if (errorMessageBottomSheet != null && errorMessageBottomSheet.isVisible()) {
                    errorMessageBottomSheet.dismissAllowingStateLoss();//.dismiss();
                }
            } catch (Exception e) {
                ReveloLogger.error(className, "onResume", "exception closing error dialog " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleIntent(String caller, Intent intent) {
        if (intent != null) {
            ReveloLogger.info(className, "handleIntent", "intent received from " + caller);
            if (intent.hasExtra("callingActivity")) {
                if (! intent.getStringExtra("callingActivity").equalsIgnoreCase("home") & ! intent.getStringExtra("callingActivity").equalsIgnoreCase("map")) {
                    ReveloLogger.info(className, caller, "callingactivity is NOT home.. calling principal endpoint");
                    callPrincipalEndPoint(AppConstants.LOGIN_DATA_REQUEST);
                }
                else {
                    if (caller.equalsIgnoreCase("newintent")) {
                        ReveloLogger.info(className, caller, "newintent received callingactivity as home.. doing nothing");
                        initNavigationComponent();
                    }
                    else {
                        ReveloLogger.info(className, caller, "oncrreateintent received callingactivity as home..showing ui..");
//                        ProgressDialog progressDialog = ProgressUtility.showProgressDialog(InitializationActivity.this, "", "Retrieving project's data... Please wait.");
                        initNavigationComponent();
                        showUI();
                    }

                }
            }
            else {
                ReveloLogger.info(className, caller, "no value for calling activity found");
//                ProgressDialog progressDialog = ProgressUtility.showProgressDialog(InitializationActivity.this, "", "Retrieving project's data... Please wait.");
                initNavigationComponent();
                showUI();
            }
        }
        else {
            ReveloLogger.info(className, "handleIntent", "null intent received from " + caller + ", returning..");
//            ProgressDialog progressDialog = ProgressUtility.showProgressDialog(InitializationActivity.this, "", "Retrieving project's data... Please wait.");
            initNavigationComponent();
            showUI();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ReveloLogger.debug(className, "onstart", "init activity started");
        if (onCreateIntent != null) {
            ReveloLogger.debug(className, "onstart", "oncreate received intent..calling handleintent..");
            updateFullScreenProgressBar(SHOW);
            handleIntent("OnCreateIntent", onCreateIntent);
            onCreateIntent = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ReveloLogger.debug(className, "onstop", "init activity stopped");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReveloLogger.debug(className, "ondestroy", "init activity destroyed");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ReveloLogger.debug(className, "onrestart", "init activity restarted");
    }

    //calling principal endpoint--------
    public void callPrincipalEndPoint(int request) {
        progressDescriptionLayout.setVisibility(View.GONE);
        ReveloLogger.info(className, "callPrincipalEndPoint", "getting access token to hit principal url");
//        if (progressUtility != null && progressUtility.isShowing()) {
//            ProgressUtility.changeProgressDialogMessage(progressUtility, "", "Initializing... Please wait.");
//        } else {
//            progressUtility = ProgressUtility.showProgressDialog(InitializationActivity.this, "", "Initializing... Please wait.");
//        }


        boolean metaDbPresent = AppFolderStructure.isMetaDataGpPresent(InitializationActivity.this);
        boolean reDbPresent = AppFolderStructure.isReGpPresent(InitializationActivity.this);
        boolean DbPresent = AppFolderStructure.isDataGpPresent(InitializationActivity.this);

        if (metaDbPresent && reDbPresent && DbPresent) {
            wasAppConfiguredBefore = true;
        }
        if (wasAppConfiguredBefore && ! NetworkUtility.checkNetworkConnectivity(InitializationActivity.this)) {
            initNavigationComponent();
//            showUI(progressUtility);
        }
        else {
            String accessToken = SecurityPreferenceUtility.getAccessToken();
            new PrincipalEndpoint(InitializationActivity.this, request, "all", accessToken, this);
               /* String errorMessage = "You are offline. You need an active internet connection to configure app for first time use.\n" +
                        "Please turn on your internet and try again.";

                ReveloLogger.error(className, "call principal endpoint", "No internet and app not configured.. using request code login_data_request to be able to retry");
                ReveloLogger.error(className, "call principal endpoint", errorMessage + " " + AppConstants.LOGIN_DATA_REQUEST);
                errorMessage(errorMessage, AppConstants.LOGIN_DATA_REQUEST, "");*/
        }


    }

    public void editProfile(View view) {
        drawerLayout.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(InitializationActivity.this, EditProfile.class);
        startActivity(intent);
    }


    @Override
    public void onPrincipalEndPointSuccess(int request, boolean downloadRedb) {
        ReveloLogger.debug(className, "onPrincipalEndPointSuccess", "Principal endpoint hit successfully for request " + request);
        if (request == AppConstants.LOGIN_DATA_REQUEST) {
            ReveloLogger.debug(className, "onPrincipalEndPointSuccess", "Request is for login_data_request.. fetching userprofile..");
            fetchUserProfile();
            showUI();

        }
        else {
//            ProgressUtility.dismissProgressDialog(progressUtility);
        }
    }

    @Override
    public void onPrincipalEndPointError(int request, String errorMessage) {
        ReveloLogger.error(className, "onPrincipalEndPointError", "principal endpoint failed for request " + request + "..showing error message");
//        ProgressUtility.dismissProgressDialog(progressDialog);
        errorMessage(errorMessage, request, - 1, null);
    }

    //when activity receives success/error message-----------------------
    public void errorMessage(String message, int requestCode, int errorCode, String jurisdiction) {
//        ProgressUtility.dismissProgressDialog(progressUtility);
        updateFullScreenProgressBar(HIDE);
        String okBtn = "";
        String cancelBtn = "Cancel";
        String title = "";
        if (requestCode == AppConstants.CHANGE_JURISDICTION_REQUEST) {
            cancelBtn = "Cancel";
            title = "Sorry, unable to change jurisdiction.";
            JurisdictionInfoPreferenceUtility.storeJurisdictions(JurisdictionInfoPreferenceUtility.getPreviousJurisdictions());
        }
        else if (requestCode == AppConstants.CHANGE_SURVEY_REQUEST) {
            cancelBtn = "Cancel";
            title = "Sorry, unable to change project.";
            //resetsurveyname to previousone
            UserInfoPreferenceUtility.resetSelectedSurveyName();
        }
        else if (requestCode == AppConstants.CHANGE_SURVEY_PHASE_REQUEST) {
            cancelBtn = "Cancel";
            title = "Sorry, unable to change project's phase.";
            //resetsurveyphasename to previousone
            UserInfoPreferenceUtility.resetSelectedSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());
        }
        else if (requestCode == AppConstants.LOGOUT_RETRY) {
            cancelBtn = "Cancel";
            title = "Unable to logout. Please check your internet connection.";
        }
        else if (requestCode == AppConstants.OK_REQUEST) {
            cancelBtn = "";
            title = "Unable to logout. Please check your internet connection.";
        }
        else if (requestCode == AppConstants.LOGIN_DATA_REQUEST) {
            cancelBtn = "Exit";
            okBtn = "Logout";
            title = "Unable to configure app";
        }
        else if (requestCode == AppConstants.LOGOUT_REQUEST) {
            cancelBtn = "Cancel";
            title = "Unable to logout";
            if (errorCode == AppConstants.ERROR_TRAIL_ON) {
                okBtn = "Stop trail and Logout";
                cancelBtn = "Cancel Logout";
            }
            else if (errorCode == AppConstants.ERROR_WHEREAMI_ON) {
                okBtn = "Stop Where Am I and Logout";
                cancelBtn = "Cancel Logout";
            }
            else if (errorCode == AppConstants.ERROR_UPLOAD_AVAILABLE) {
                okBtn = "Upload data and Logout";
                cancelBtn = "Logout";
            }
            else if (errorCode == AppConstants.ERROR_NO_NETWORK) {
                okBtn = "Retry";
                cancelBtn = "Cancel Logout";
            }
        }

        if (errorCode == DownloadFileForegroundService.ERROR_DOWNLOADING_REGP) {
            okBtn = "Retry";
            cancelBtn = "Exit";
        }
        else if (errorCode == DownloadFileForegroundService.ERROR_CREATING_DATA_DB || errorCode == DownloadFileForegroundService.ERROR_DOWNLOADING_DATAGP) {
            okBtn = "Retry";
            cancelBtn = "Exit";
        }
        else if (errorCode == DownloadFileForegroundService.ERROR_CREATING_META_DATA_DB || errorCode == DownloadFileForegroundService.ERROR_DOWNLOADING_METADATAGP) {
            okBtn = "Retry";
            cancelBtn = "Exit";
        }
        ReveloLogger.error(className, "errorMessage", message + " " + requestCode);

        if (errorMessageBottomSheet != null && errorMessageBottomSheet.isVisible()) {
            errorMessageBottomSheet.dismiss();
        }
        errorMessageBottomSheet = InfoBottomSheet.geInstance(this, okBtn, cancelBtn, title, message, requestCode, errorCode, jurisdiction);
        errorMessageBottomSheet.setCancelable(false);
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            // Do fragment's transaction commit
            errorMessageBottomSheet.show(getSupportFragmentManager(), BottomSheetTagConstants.loginInfo);
        }
        else {
            ToastUtility.toast(message, InitializationActivity.this, true);
        }
    }

    private void updateFullScreenProgressBar(boolean showProgressScreen) {
        if (showProgressScreen) {
            initialLoading.setVisibility(View.VISIBLE);
            Objects.requireNonNull(getSupportActionBar()).hide();
        }
        else {
            initialLoading.setVisibility(View.GONE);
            Objects.requireNonNull(getSupportActionBar()).show();
        }
    }

    private void showUI() {
//single survey, single phase - let user pass
        //single survey, multiple phases - show choice of phase
        //multiple survey   -- 1.survey already chosen, see if it has multiple phases. if yes, show choice of phase or show choice of survey
        //                  -- 2.survey not chosen - show choice of phases
        ReveloLogger.debug(className, "onPrincipalEndPointSuccess", "Checking if we have a survey name saved/selected..");
        ReveloLogger.timeLog(className, "onPrincipalEndPointSuccess", "Checking if we have a survey name saved/selected..");
        String surveyName = UserInfoPreferenceUtility.getSurveyName();
        if (TextUtils.isEmpty(surveyName)) {//survey not chosen, showing choice of survey
            ReveloLogger.debug(className, "onPrincipalEndPointSuccess", "No survey name found as selected survey. hence moving to show survey list and let user choose..");
//            ProgressUtility.dismissProgressDialog(progressDialog);
//            ProgressUtility.dismissProgressDialog(progressUtility);
            showSurveyList("", AppConstants.LOGIN_DATA_REQUEST);
        }
        else {
//survey already chosen, checking if it has multiple phases
            boolean selectedSurveyHasMultiplePhases = false;
            Survey currentSurvey = SurveyPreferenceUtility.getSurvey(surveyName);
            if (currentSurvey != null) {
                if (currentSurvey.hasPhases()) {
                    HashMap<String, Phase> phasesNameMap = currentSurvey.getPhasesNameMapFromJson();
                    if (phasesNameMap != null && phasesNameMap.size() > 1) {
                        selectedSurveyHasMultiplePhases = true;//has multiple phases..show choice of phases
                    }
                }
            }
            if (selectedSurveyHasMultiplePhases) {
                ReveloLogger.debug(className, "onPrincipalEndPointSuccess", " survey " + surveyName + " found as selected survey. hence moving to getting its details..");
                getSurveyDetails(InitializationActivity.this, surveyName, AppConstants.LOGIN_DATA_REQUEST);
            }
            else {
                ReveloLogger.debug(className, "onPrincipalEndPointSuccess", "No survey name found as selected survey. hence moving to show survey list and let user choose..");
//                ProgressUtility.dismissProgressDialog(progressDialog);
//                ProgressUtility.dismissProgressDialog(progressUtility);
                showSurveyList("", AppConstants.LOGIN_DATA_REQUEST);
            }
            /*if(!selectedSurveyHasMultiplePhases || UserInfoPreferenceUtility.getSurveyPhaseName(surveyName).isEmpty()){//if survey doesnt have phases or not selected yet, show surveys view
                ReveloLogger.debug(className, "onPrincipalEndPointSuccess", "No survey name found as selected survey. hence moving to show survey list and let user choose..");
                ProgressUtility.dismissProgressDialog(progressDialog);
                ProgressUtility.dismissProgressDialog(progressUtility);
                showSurveyList("", progressDialog, AppConstants.LOGIN_DATA_REQUEST);
            }else {
                ReveloLogger.debug(className, "onPrincipalEndPointSuccess", " survey " + surveyName + " found as selected survey. hence moving to getting its details..");
                getSurveyDetails(InitializationActivity.this, surveyName, progressDialog, AppConstants.LOGIN_DATA_REQUEST);
            }*/
        }
    }

    //saving user profile--------------
    private void fetchUserProfile() {
        if (NetworkUtility.checkNetworkConnectivity(InitializationActivity.this)) {
            ReveloLogger.error(className, "fetchUserProfile", " internet available...fetching user profile..");
            new FetchUserProfile(InitializationActivity.this, InitializationActivity.this);
        }
        else {
            ReveloLogger.error(className, "fetchUserProfile", "No internet available...skipping user profile fetch");
        }
    }

    private void fileDownloadSuccess(String downloadedFile, String destinationFolder, String fileName) {
        ReveloLogger.info(className, "fileDownloadSuccess", "downloadedFile " + downloadedFile + ";  destination folder - " + destinationFolder + ";  filename - " + fileName);
        boolean isUnzipSuccessful = false;

        if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
            ReveloLogger.info(className, "fileDownloadSuccess", "file is regpkg");
            String reDbFilePath = AppFolderStructure.getReGpFilePath(InitializationActivity.this);
            File reDbFile = new File(reDbFilePath);
            if (reDbFile.exists()) {
                ReveloLogger.info(className, "fileDownloadSuccess", "deleting existing regp file");
                reDbFile.delete();
            }

            File file = new File(downloadedFile);
            try {
                isUnzipSuccessful = SystemUtils.unzip(InitializationActivity.this, file, destinationFolder, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ReveloLogger.info(className, "fileDownloadSuccess", "deleting downloaded zip regp file, after unzipping regp file");
                FileUtils.forceDelete(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isUnzipSuccessful) {
                ReveloLogger.info(className, "fileDownloadSuccess", fileName + " File download successfully");
            }
            else {
//            fileDownloadInterface.errorFileDownload("cannot unzip the downloaded zip file.");
                if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
                    errorMessage("Could not unzip " + fileName + ".", AppConstants.DOWNLOAD_DATA_REQUEST, DownloadFileForegroundService.ERROR_DOWNLOADING_REGP, "");
                }
                else if (fileName.equalsIgnoreCase(AppConstants.DATA_GP_FILE)) {
                    errorMessage("Could not unzip " + fileName + ".", AppConstants.DOWNLOAD_DATA_REQUEST, DownloadFileForegroundService.ERROR_CREATING_DATA_DB, "");
                }
                else if (fileName.equalsIgnoreCase(AppConstants.METADATA_FILE)) {
                    errorMessage("Could not unzip " + fileName + ".", AppConstants.DOWNLOAD_DATA_REQUEST, DownloadFileForegroundService.ERROR_CREATING_META_DATA_DB, "");
                }
                ReveloLogger.error(className, "fileDownloadSuccess", "cannot unzip the downloaded zip file.");
            }
        }

        ReveloLogger.info(className, "fileDownloadSuccess", "dismissing progress bar");
        //percentageProgressBar.dismiss();


    }

    //survey related methods-------------
    private void showSurveyList(String message, int requestType) {

        List<Survey> surveyNameList = UserInfoPreferenceUtility.getSurveyNameList();
        String currentSurveyName = UserInfoPreferenceUtility.getSurveyName();
        ReveloLogger.debug(className, "showSurveyList", "current survey name - " + currentSurveyName);

        if (surveyNameList == null || surveyNameList.isEmpty()) {
            ReveloLogger.debug(className, "showSurveyList", "survey list empty for user..throwing error message");
            //no survey assigned. inform user
            ToastUtility.toast("No project assigned, Please contact the administrator.", InitializationActivity.this, true);
        }
        else {
            ReveloLogger.debug(className, "showSurveyList", surveyNameList.size() + " surveys assigned to this user..Creating choose survey fragment");
//            ProgressUtility.dismissProgressDialog(progressUtility);
            tvSelectionInstruction.setVisibility(View.VISIBLE);
            tvSelectionInstruction.setText("Projects : ");
            // titleTextTv.setText("Survey");
            chooseSurveyFragmentDialog = (ChooseSurveyFragmentNew) getSupportFragmentManager().findFragmentByTag(FRAGMENT_SURVEYS);
            if (chooseSurveyFragmentDialog == null) {
                ISelectedSurveyName onSurveySelectedInterface = new ISelectedSurveyName() {
                    @Override
                    public void s_selectedSurveyName(String surveyName) {

                        if (requestType == AppConstants.CHANGE_SURVEY_REQUEST) {

                            ReveloLogger.debug(className, "showSurveyList", "Survey " + surveyName + " selected by user..confirming if he really wants to change project");

                            String title = getResources().getString(R.string.title_change_survey);
                            String changeSurveyMessage = getResources().getString(R.string.message_change_survey);

                            InfoBottomSheet infoBottomSheet = InfoBottomSheet.geInstance(InitializationActivity.this, "Yes", "No", title, changeSurveyMessage, requestType, 0, surveyName);
                            infoBottomSheet.setCancelable(false);
                            infoBottomSheet.show(getSupportFragmentManager(), BottomSheetTagConstants.changeSurveyInfo);

                        }
                        else {
                            ReveloLogger.debug(className, "showSurveyList", "Survey " + surveyName + " selected by user..getting its details..");
                            getSurveyDetails(InitializationActivity.this, surveyName, requestType);
                            updateFullScreenProgressBar(SHOW);
                        }
                    }

                    @Override
                    public void s_onCancellingSelectingSurvey(String message) {
                        ReveloLogger.error(className, "showSurveyList", "Survey selection cancelled by user..if no other survey is saved earlier, logout will be initiated..");
                        if (UserInfoPreferenceUtility.getSurveyName().isEmpty()) {
                            ReveloLogger.error(className, "showSurveyList", "No other survey found.. logout initiated..");
                            logoutRequest(AppConstants.LOGOUT_REQUEST);
                            ToastUtility.toast("logout request", InitializationActivity.this, true);
                        }
                    }
                };
                chooseSurveyFragmentDialog = ChooseSurveyFragmentNew.newInstance(currentSurveyName, surveyNameList, InitializationActivity.this, requestType, onSurveySelectedInterface);
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, chooseSurveyFragmentDialog, FRAGMENT_SURVEYS).commit();
            }
            if (surveyNameList.size() == 1) {

                ReveloLogger.debug(className, "showSurveyList", "Single survey assigned to this user..selecting it automatically..moving to check if survey has phases");
                //select the survey and move to show phases
                Survey singleProject = surveyNameList.get(0);
                //ToastUtility.toast("Project assigned: " + singleProject.getLabel(), HomeActivity.this, true);
                if (requestType == AppConstants.CHANGE_SURVEY_REQUEST) {
                    //only one survey assigned. inform user
                }
                else {
                    updateFullScreenProgressBar(SHOW);
                    ReveloLogger.debug(className, "showSurveyList", "Checking survey details..for survey " + singleProject.getName());
                    getSurveyDetails(InitializationActivity.this, singleProject.getName(), requestType);
                    ReveloLogger.debug(className, "chooseSurveyFragmentDialog", "Get survey details from server.");
                }
            }
            else {
                updateFullScreenProgressBar(HIDE);
            }
        }
    }


    //download redb

    private void getSurveyDetails(Activity activity, String selectedSurveyName, int requestType) {
        ReveloLogger.debug(className, "getSurveyDetails", "call survey details operation for survey " + selectedSurveyName + " request code " + requestType);
        ReveloLogger.timeLog(className, "getSurveyDetails", "call survey details operation for survey " + selectedSurveyName + " request code " + requestType);
        phaseORSurveyChanged = ! UserInfoPreferenceUtility.getSurveyName().equalsIgnoreCase(selectedSurveyName);
        if (phaseORSurveyChanged) {
//            switchOffWhereAmI();
        }
        ReveloLogger.debug(className, "getSurveyDetails", "Survey changed? " + phaseORSurveyChanged);

        if (requestType == AppConstants.CHANGE_SURVEY_REQUEST || requestType == AppConstants.CHANGE_SURVEY_PHASE_REQUEST) {

            ReveloLogger.debug(className, "getSurveyDetails", "proceed for change survey request...looking if this survey is pre-saved");

            Survey survey = SurveyPreferenceUtility.getSurvey(selectedSurveyName);

            if (survey == null) {
                ReveloLogger.debug(className, "showSurveyList", "no pre-saved survey found, moving to fetch it freshly from server..");
                if (NetworkUtility.checkNetworkConnectivity(activity)) {

                    new SurveyDetails(activity, requestType, selectedSurveyName, new ISurveyDetails() {
                        @Override
                        public void onError(String message) {
                            errorMessage(message, requestType, - 1, null);
                            ReveloLogger.error(className, "SurveyDetails", message + " " + requestType);
                        }

                        @Override
                        public void onSuccess() {
                            ReveloLogger.info(className, "SurveyDetails", "Fetched survey details from server.");
                            Survey selectedSurvey = SurveyPreferenceUtility.getSurvey(selectedSurveyName);
                            setTitle(selectedSurvey.getLabel());
                            if (selectedSurvey.hasPhases()) {
//                                ProgressUtility.dismissProgressDialog(progressUtility);
                                showSurveyPhasesList(selectedSurvey, requestType);//get survey details,survey has phases
                            }
                            else {
                                showProgressUi("surveySelected");
                                downloadOrgBoundaryConceptModel(activity, requestType);
                            }
                        }
                    });
                }
                else {

                    String errorMessage = activity.getResources().getString(R.string.error_network_download);

                    errorMessage(errorMessage, requestType, - 1, "");
                    ReveloLogger.error(className, "getSurveyDetails", errorMessage + " " + requestType);

                }
            }
            else {
                ReveloLogger.debug(className, "showSurveyList", "pre-saved survey found, shifting previous survey name to " + UserInfoPreferenceUtility.getSurveyName() + " and current survey name to " + survey.getName());

                UserInfoPreferenceUtility.storePreviousSurveyName(UserInfoPreferenceUtility.getSurveyName());
                UserInfoPreferenceUtility.storePreviousSurveyNameLabel(UserInfoPreferenceUtility.getSurveyNameLabel());
                UserInfoPreferenceUtility.storeSurveyName(survey.getName());
                UserInfoPreferenceUtility.storeSurveyNameLabel(survey.getLabel());

                ReveloLogger.debug(className, "showSurveyList", "getting survey " + selectedSurveyName + " from shared pref ...checking if it has phases..");
                Survey selectedSurvey = SurveyPreferenceUtility.getSurvey(selectedSurveyName);
                setTitle(selectedSurvey.getLabel());
                if (selectedSurvey.hasPhases()) {
                    ReveloLogger.debug(className, "showSurveyList", "survey " + selectedSurveyName + " has phases..moving to show list of phases for selection..");
                    showSurveyPhasesList(selectedSurvey, requestType);//change survey, survey has phases
                }
                else {
                    ReveloLogger.debug(className, "showSurveyList", "survey " + selectedSurveyName + " does not have phases..checking db status..");
                    checkDataDownloadScenario(activity, requestType);
                    ToastUtility.toast("checkDataDownloadScenario", InitializationActivity.this, true);
                }

            }
        }
        else {

            if (wasAppConfiguredBefore && ! NetworkUtility.checkNetworkConnectivity(InitializationActivity.this)) {
                ReveloLogger.debug(className, "showSurveyList", "app was previously configured, and we dont have internet..using old survey state" + selectedSurveyName + " from local storage..");
                if (! UserInfoPreferenceUtility.getSurveyName().equalsIgnoreCase(selectedSurveyName)) {
                    phaseORSurveyChanged = true;
//                    switchOffWhereAmI();
                }
                ReveloLogger.debug(className, "showSurveyList", "survey " + selectedSurveyName + " fetched from server..saving survey name and label in pref");
                Survey selectedSurvey = SurveyPreferenceUtility.getSurvey(selectedSurveyName);
                UserInfoPreferenceUtility.storeSurveyName(selectedSurvey.getName());
                UserInfoPreferenceUtility.storeSurveyNameLabel(selectedSurvey.getLabel());
                setTitle(selectedSurvey.getLabel());
                try {
                    ReveloLogger.debug(className, "showSurveyList", "creating survey folder..");
                    AppFolderStructure.createSurveyFolder(activity);
                } catch (Exception e) {
                    ReveloLogger.error(className, "showSurveyList", "error creating survey folder..");
                    e.printStackTrace();
                }

                ReveloLogger.debug(className, "showSurveyList", "Checking selected phase name for survey " + selectedSurveyName);
                String selectedPhaseName = UserInfoPreferenceUtility.getSurveyPhaseName(selectedSurveyName);

                if (selectedSurvey.hasPhases()) {
//                    ProgressUtility.dismissProgressDialog(progressDialog);
                    showSurveyPhasesList(selectedSurvey, requestType);//notchangesurveyrequest,getsurveydetails

                }
                else {
                    boolean metaDbPresent = AppFolderStructure.isMetaDataGpPresent(InitializationActivity.this);
                    boolean reDbPresent = AppFolderStructure.isReGpPresent(InitializationActivity.this);

                    //if (phaseORSurveyChanged) { //added additional checks because if selecction is done and db download fails, next time user directly reaches home activity without any db. making app further unstable
                    if (phaseORSurveyChanged || ! metaDbPresent || ! reDbPresent) {
                        ReveloLogger.debug(className, "showSurveyList", "survey " + selectedSurveyName + " does not have phases..checking existing data state");
                        checkDataDownloadScenario(activity, requestType);
                    }
                    else {
                        ReveloLogger.info(className, "successMessage", "success - database file exists");
                        ReveloLogger.timeLog(className, "successMessage", "success - database file exists");
                        /* AppMethods.clearAllDatabaseRelatedStaticVariables();//all db files exist*/
                        ReveloLogger.info(className, "successMessage", "database ralated static var cleared, mapactivity.maploaded set to false");
                        ReveloLogger.info(className, "successMessage", "calling homeui() to refresh ui");

                        callHomeUi("DATABASE_FILE_EXIST");//db files exist? = true,survey selected

                    }
                }
            }
            else {
                ReveloLogger.debug(className, "showSurveyList", "fetching freshly survey " + selectedSurveyName + " from server..");
                new SurveyDetails(activity, requestType, selectedSurveyName, new ISurveyDetails() {
                    @Override
                    public void onError(String message) {
//                        ProgressUtility.dismissProgressDialog(progressDialog);
                        errorMessage(message, requestType, - 1, selectedSurveyName);
                        ReveloLogger.error(className, "SurveyDetails", "Could not fetch survey details for survey " + selectedSurveyName + ".Details - " + message + " " + requestType);
                    }

                    @Override
                    public void onSuccess() {

                        if (! UserInfoPreferenceUtility.getSurveyName().equalsIgnoreCase(selectedSurveyName)) {
                            phaseORSurveyChanged = true;
//                            switchOffWhereAmI();
                        }
                        ReveloLogger.debug(className, "showSurveyList", "survey " + selectedSurveyName + " fetched from server..saving survey name and label in pref");
                        Survey selectedSurvey = SurveyPreferenceUtility.getSurvey(selectedSurveyName);
                        UserInfoPreferenceUtility.storeSurveyName(selectedSurvey.getName());
                        UserInfoPreferenceUtility.storeSurveyNameLabel(selectedSurvey.getLabel());
                        setTitle(selectedSurvey.getLabel());
                        try {
                            ReveloLogger.debug(className, "showSurveyList", "creating survey folder..");
                            AppFolderStructure.createSurveyFolder(activity);
                        } catch (Exception e) {
                            ReveloLogger.error(className, "showSurveyList", "error creating survey folder..");
                            e.printStackTrace();
                        }

                        ReveloLogger.debug(className, "showSurveyList", "Checking selected phase name for survey " + selectedSurveyName);
                        String selectedPhaseName = UserInfoPreferenceUtility.getSurveyPhaseName(selectedSurveyName);

                        if (selectedSurvey.hasPhases()) {
//                            ProgressUtility.dismissProgressDialog(progressDialog);
                            showSurveyPhasesList(selectedSurvey, requestType);//notchangesurveyrequest,getsurveydetails

                        }
                        else {
                            ReveloLogger.debug(className, "showSurveyList", "survey " + selectedSurveyName + " does not have phases..creating data nd log folder..moving to downloading org boundary..");
                            try {
                                AppFolderStructure.createDataGpFolder(activity);
                                AppFolderStructure.createLogFolder(activity);
                            } catch (Exception e) {
                                ReveloLogger.error(className, "showSurveyList", "error creating data nd log folder.." + e.getMessage());
                                e.printStackTrace();
                            }
                            downloadOrgBoundaryConceptModel(InitializationActivity.this, requestType);
                        }

                    }
                });
            }
        }
    }

    //phases related methods----------------
    private void showSurveyPhasesList(Survey selectedSurvey, int requestType) {
        tvSelectionInstruction.setVisibility(View.GONE);
        ReveloLogger.debug(className, "showSurveyPhasesList", "Showing phases list for survey " + selectedSurvey.getName() + " - request - " + requestType);
        ReveloLogger.timeLog(className, "showSurveyPhasesList", "Showing phases list for survey " + selectedSurvey.getName() + " - request - " + requestType);
        if (selectedSurvey.hasPhases()) {
            ReveloLogger.debug(className, "showSurveyPhasesList", "Survey " + selectedSurvey.getName() + " has phases. Checking count..");
            HashMap<String, Phase> phasesNameMap = selectedSurvey.getPhasesNameMapFromJson();
            if (phasesNameMap == null || phasesNameMap.isEmpty()) {
                ReveloLogger.debug(className, "showSurveyPhasesList", "Survey " + selectedSurvey.getName() + " has phases but no phase map found. hence moving to shor jurisdiction dialog");
                //show jurisdictions dialog
                downloadOrgBoundaryConceptModel(InitializationActivity.this, requestType);
            }
            else {


                tvSelectionInstruction.setVisibility(View.VISIBLE);
                tvSelectionInstruction.setText("Phases : ");
                ReveloLogger.debug(className, "showSurveyPhasesList", "Survey " + selectedSurvey.getName() + " has " + phasesNameMap.size() + " phases. Hence moving to show choose phase option dialog");
                //show select phase dialog
//                ProgressUtility.dismissProgressDialog(progressUtility);
                choosePhaseFragmentDialog = (ChoosePhaseFragmentNew) getSupportFragmentManager().findFragmentByTag(FRAGMENT_PHASES);
                if (choosePhaseFragmentDialog == null) {
                    IPhaseSelection iOnPhaseSelection = new IPhaseSelection() {

                        @Override
                        public void onPhaseSelected(String surveyName, String phaseName) {
                            updateFullScreenProgressBar(SHOW);
                            ReveloLogger.debug(className, "showSurveyPhasesList", "Phase " + phaseName + " selected of Survey " + selectedSurvey.getName());
                            ReveloLogger.timeLog(className, "onReceiveResult", "Phase " + phaseName + " selected of Survey " + selectedSurvey.getName());
//                            if (progressUtility != null && progressUtility.isShowing()) {
//                                ProgressUtility.changeProgressDialogMessage(progressUtility, "", "Loading " + phaseName + " information... Please wait.");
//                            } else {
//                                progressUtility = ProgressUtility.showProgressDialog(choosePhaseFragmentDialog.getActivity(), "", "Loading " + phaseName + " information... Please wait.");
//                            }
                            try {

                                if (UserInfoPreferenceUtility.getSurveyName().equalsIgnoreCase(surveyName)) {
                                    phaseORSurveyChanged = ! UserInfoPreferenceUtility.getSurveyPhaseName(surveyName).equalsIgnoreCase(phaseName);
                                }
                                else {
                                    phaseORSurveyChanged = true;
//                                    switchOffWhereAmI();
                                }

                                ReveloLogger.debug(className, "showSurveyPhasesList", "Phase changed? " + phaseORSurveyChanged);

                                ReveloLogger.debug(className, "showSurveyPhasesList", "Saving phase name label to shared pref, creating data log and phase folders");
                                UserInfoPreferenceUtility.storeSurveyPhaseName(surveyName, phaseName);
                                Survey survey = SurveyPreferenceUtility.getSurvey(surveyName);
                                HashMap<String, Phase> phaseList = survey.getPhasesNameMapFromJson();
                                String phaseLabel = phaseName;
                                if (phaseList.containsKey(phaseName)) {
                                    phaseLabel = phaseList.get(phaseName).getLabel();
                                }
                                UserInfoPreferenceUtility.storeSurveyPhaseLabel(surveyName, phaseLabel);
                                setTitle(phaseLabel);
                                if (phaseORSurveyChanged) {
//                                    switchOffWhereAmI();
                                }
                            } catch (Exception e) {
                                ReveloLogger.error(className, "showSurveyPhasesList", "Error Saving phase name label to shared pref, creating data log and phase folders. message = " + e.getMessage());
                                e.printStackTrace();
                            }

                            ReveloLogger.debug(className, "showSurveyPhasesList", "Phase name saved..Checking db state..");
                            ReveloLogger.timeLog(className, "showSurveyPhasesList", "Phase name saved..Checking db state..");
//                            if (progressUtility != null && progressUtility.isShowing()) {
//                                ProgressUtility.changeProgressDialogMessage(progressUtility, "Checking app's data state..", "");
//                            } else {
//                                progressUtility = ProgressUtility.showProgressDialog(InitializationActivity.this, "Checking app's data state..", "Please wait..");
//                            }
                            boolean metaDbPresent = AppFolderStructure.isMetaDataGpPresent(InitializationActivity.this);
                            boolean reDbPresent = AppFolderStructure.isReGpPresent(InitializationActivity.this);
//if (phaseORSurveyChanged) { //added additional checks because if selecction is done and db download fails, next time user directly reaches home activity without any db. making app further unstable
                            if (phaseORSurveyChanged || ! metaDbPresent || ! reDbPresent) {
                                checkDataDownloadScenario(InitializationActivity.this, requestType);
                            }
                            else {
                                ReveloLogger.info(className, "successMessage", "success - database file exists");
                                ReveloLogger.timeLog(className, "successMessage", "success - database file exists");
                                /* AppMethods.clearAllDatabaseRelatedStaticVariables();//all db files exist*/
                                ReveloLogger.info(className, "successMessage", "database ralated static var cleared, mapactivity.maploaded set to false");
                                ReveloLogger.info(className, "successMessage", "calling homeui() to refresh ui");

                                callHomeUi("DATABASE_FILE_EXIST");//db files exist? = true,survey selected
                            }

                        }

                        @Override
                        public void onPhaseSelectionCancelled() {
                            ReveloLogger.error(className, "showSurveyPhasesList", "Phase selection cancelled..moving to survey list..");
                            if (! UserInfoPreferenceUtility.getPreviousSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty() && requestType == AppConstants.CHANGE_SURVEY_PHASE_REQUEST) {
                                ReveloLogger.error(className, "showSurveyPhasesList", "change phase request cancelled, resetting current phase name to previous one.." + UserInfoPreferenceUtility.getPreviousSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()));
                                UserInfoPreferenceUtility.resetSelectedSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());
                            }
                            else if (! UserInfoPreferenceUtility.getPreviousSurveyName().isEmpty() && requestType == AppConstants.CHANGE_SURVEY_REQUEST) {
                                ReveloLogger.error(className, "showSurveyPhasesList", "change survey request cancelled, " + "resetting current phase name to previous one.." + UserInfoPreferenceUtility.getPreviousSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()) + " and current survey name to previous one.." + UserInfoPreferenceUtility.getSurveyName());
                                UserInfoPreferenceUtility.resetSelectedSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());
                                UserInfoPreferenceUtility.resetSelectedSurveyName();//returning without selecting jurisdictions for second time
                            }
                            else if (JurisdictionInfoPreferenceUtility.getJurisdictions().isEmpty()) {
                                ReveloLogger.error(className, "showSurveyPhasesList", "No jurisdiction value set..resetting phase and survey name to previous ones");
                                UserInfoPreferenceUtility.resetSelectedSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());
                                UserInfoPreferenceUtility.resetSelectedSurveyName();//returning without selecting jurisdictions for first time
                            }
                            else {
                                Survey selectedSurvey = SurveyPreferenceUtility.getSurvey(UserInfoPreferenceUtility.getSurveyName());
                                if (selectedSurvey.hasPhases() && UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()) {
                                    ReveloLogger.error(className, "showSurveyPhasesList", "Survey " + selectedSurvey.getName() + " was selected, it has phases but none was selected. resetting selected surveyname to previous one");
                                    UserInfoPreferenceUtility.resetSelectedSurveyName();
                                }
                            }
                            List<Survey> surveyNameList = UserInfoPreferenceUtility.getSurveyNameList();
                            if ((surveyNameList == null || surveyNameList.isEmpty() || surveyNameList.size() == 1)) {
                                //back from all dialogs
                                if (UserInfoPreferenceUtility.getSurveyName().isEmpty()) {
                                    ReveloLogger.error(className, "showSurveyPhasesList", "No or only onw survey was assigned to user, and even that was not applied. hence logging out on back press");
                                    logoutRequest(AppConstants.LOGOUT_REQUEST);
                                    ToastUtility.toast("logoutRequest", InitializationActivity.this, true);
                                }
                            }
                            else {
                                ReveloLogger.error(className, "showSurveyPhasesList", "more than one surveys was assigned to user, showing surveylist..");
                                showSurveyList("Select Survey", requestType);
                            }
                        }
                    };
                    choosePhaseFragmentDialog = ChoosePhaseFragmentNew.newInstance(selectedSurvey.getName(), InitializationActivity.this, requestType, iOnPhaseSelection);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, choosePhaseFragmentDialog, FRAGMENT_PHASES).commit();
                }
                if (phasesNameMap.size() == 1) {
                    if (requestType != AppConstants.CHANGE_SURVEY_PHASE_REQUEST) {
                        ReveloLogger.error(className, "showSurveyPhasesList", "Survey " + selectedSurvey.getName() + " has only 1 phase. Selecting it by default and moving to shor jurisdiction dialog");
                        //select that phase and move to show jusrisdiction dialog
                        String surveyName = selectedSurvey.getName();
                        String phaseName = "", phaseLabel = "";
                        Phase phase = null;
                        for (String pName : phasesNameMap.keySet()) {
                            phaseName = pName;
                            phase = phasesNameMap.get(pName);
                            phaseLabel = phase.getLabel();
                        }
                        if (phase == null || phaseName.isEmpty()) {
                            ReveloLogger.error(className, "showSurveyPhasesList", "phase name / phase not found. Showing jurisdiction");
                            //dont select that phase and move to show jusrisdiction dialog
                            ReveloLogger.debug(className, "showSurveyPhasesList", "Survey " + selectedSurvey.getName() + " has no phase. moving to show choose phase option dialog");
                            downloadOrgBoundaryConceptModel(InitializationActivity.this, requestType);
                            setTitle(selectedSurvey.getLabel());
                        }
                        else {
                            try {
                                UserInfoPreferenceUtility.storeSurveyPhaseName(surveyName, phaseName);
                                UserInfoPreferenceUtility.storeSurveyPhaseLabel(surveyName, phaseLabel);
                                setTitle(phaseLabel);
                                AppFolderStructure.createPhaseFolder(InitializationActivity.this);
                                AppFolderStructure.createDataGpFolder(InitializationActivity.this);
                                AppFolderStructure.createLogFolder(InitializationActivity.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ReveloLogger.debug(className, "showSurveyPhasesList", "Survey " + selectedSurvey.getName() + " has one phase.Selecting it automatically and moving to show choose phase option dialog");
                            downloadOrgBoundaryConceptModel(InitializationActivity.this, requestType);
                        }
                    }
                }
                else {
                    updateFullScreenProgressBar(HIDE);
                }
            }
        }
        else {
            ReveloLogger.error(className, "showSurveyPhasesList", "selected survey " + selectedSurvey.getName() + "does not have phases Moving to download redb");
            //show jurisdictions dialog
            downloadOrgBoundaryConceptModel(InitializationActivity.this, requestType);
        }
    }

//    private void switchOffWhereAmI() {
//        if (WUtils.isWhereIAmServiceRunning((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))) {
//            stopService(new Intent(getApplicationContext(), WhereAmIService.class));
//        }
//    }

    public void successMessage(String message, int requestType) {
        if (requestType != AppConstants.REFRESH_DATA_REQUEST) {
//            ProgressUtility.dismissProgressDialog(progressUtility);
        }
        ReveloLogger.info(className, "successMessage", "Success message received for request " + requestType);
        if (requestType == AppConstants.DATABASE_FILE_EXIST) {
            ReveloLogger.info(className, "successMessage", "success - database file exists");
            ReveloLogger.timeLog(className, "successMessage", "success - database file exists");
            /* AppMethods.clearAllDatabaseRelatedStaticVariables();//all db files exist*/
            AppMethods.clearDatabaseRelatedStaticVariables();
            ReveloLogger.info(className, "successMessage", "database ralated static var cleared, mapactivity.maploaded set to false");
            ReveloLogger.info(className, "successMessage", "calling homeui() to refresh ui");

            callHomeUi(message);//db files exist? = true
        }
        else if (message.equalsIgnoreCase("" + AppConstants.CREATE_META_GP_FILE_REQUEST)) {
            ReveloLogger.info(className, "successMessage", "success request - show jurisdiction dialog");
            ReveloLogger.timeLog(className, "successMessage", "success request - show jurisdiction dialog");
            createMetaGPRequestReceived(requestType);//request - show jurisdiction dialog
        }
        else if (requestType == AppConstants.LOGOUT_REQUEST) {
            ReveloLogger.info(className, "successMessage", "success request - logout");
            //logoutRequest();
            logout();
        }

        // ReveloLogger.info(className, "successMessage", "Operation perform success for " + requestType);
    }

    private void createGPRequestReceived(int requestType) {
        ReveloLogger.debug(className, "createGPRequestReceived", "Calling to create gp file");
        showProgressUi("downloadingdatadb");
        getSelectedJurisdictionsData(AppConstants.CREATE_DATA_GP_FILE, requestType);
    }

    private void showProgressUi(String message) {
        String orgLabel = UserInfoPreferenceUtility.getOrgLabel();
        if (orgLabel.isEmpty()) {
            orgLabel = "your Organization";
        }
        ReveloLogger.debug(className, "showProgressUi", "Showing progress ui for " + message);
        progressDescriptionLayout.setVisibility(View.VISIBLE);
        String project_title = UserInfoPreferenceUtility.getSurveyNameLabel();
        if (! UserInfoPreferenceUtility.getSurveyPhaseLabel(UserInfoPreferenceUtility.getSurveyName()).isEmpty()) {
            project_title = UserInfoPreferenceUtility.getSurveyPhaseLabel(UserInfoPreferenceUtility.getSurveyName());
        }
        loadingPhaseTv.setText("Getting ready for " + project_title + "...");
        if (message.equalsIgnoreCase("surveySelected")) {

            redbCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            redbProgressTV.setText("Download " + orgLabel + "'s Administrative boundaries.");
            redbProgressTV.setTextSize(16);
            redbProgressTV.setTextColor(getResources().getColor(R.color.colorBlack));

            metadataProgressTV.setTextColor(getResources().getColor(R.color.colorBlack));
            metadataProgressTV.setTextSize(16);
            metadataCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            metadataProgressTV.setText("Download " + project_title + " configuration.");
        }
        if (message.equalsIgnoreCase("downloadingRedb")) {

            redbCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            redbProgressTV.setText("Downloading " + orgLabel + "'s Administrative boundaries... Please wait.");
            redbProgressTV.setTextSize(18);
            redbProgressTV.setTextColor(getResources().getColor(R.color.colorPrimary));

            metadataProgressTV.setTextColor(getResources().getColor(R.color.colorBlack));
            metadataProgressTV.setTextSize(16);
            metadataCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            metadataProgressTV.setText("Download the configuration for " + project_title + ".");
        }
        if (message.equalsIgnoreCase("downloadedRedb")) {


            redbCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            redbCheckIV.setColorFilter(getResources().getColor(R.color.colorPrimary));
            redbProgressTV.setText(AppMethods.capitaliseFirstLatter(orgLabel) + "'s Administrative boundaries downloaded.");
            redbProgressTV.setTextSize(18);
            redbProgressTV.setTextColor(getResources().getColor(R.color.colorPrimary));

            metadataProgressTV.setTextColor(getResources().getColor(R.color.colorBlack));
            metadataProgressTV.setTextSize(16);
            metadataCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            metadataProgressTV.setText("Download the configuration for " + project_title + ".");
        }

        if (message.equalsIgnoreCase("downloadingmetadatadb")) {


            redbCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            redbCheckIV.setColorFilter(getResources().getColor(R.color.colorPrimary));
            redbProgressTV.setText(AppMethods.capitaliseFirstLatter(orgLabel) + "'s Administrative boundaries downloaded.");
            redbProgressTV.setTextSize(16);
            redbProgressTV.setTextColor(getResources().getColor(R.color.colorBlack));

            metadataProgressTV.setTextColor(getResources().getColor(R.color.colorPrimary));
            metadataProgressTV.setTextSize(18);
            metadataCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            metadataProgressTV.setText("Downloading the configuration for " + project_title + "... Please wait.");

        }
        if (message.equalsIgnoreCase("downloadedMetadatadb")) {


            redbCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            redbCheckIV.setColorFilter(getResources().getColor(R.color.colorPrimary));
            redbProgressTV.setText(AppMethods.capitaliseFirstLatter(orgLabel) + "'s Administrative boundaries downloaded.");
            redbProgressTV.setTextSize(16);
            redbProgressTV.setTextColor(getResources().getColor(R.color.colorBlack));

            metadataProgressTV.setTextColor(getResources().getColor(R.color.colorPrimary));
            metadataProgressTV.setTextSize(18);
            metadataCheckIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_grey));
            metadataCheckIV.setColorFilter(getResources().getColor(R.color.colorPrimary));
            metadataProgressTV.setText("Configuration for " + project_title + " downloaded... Please wait.");

        }
    }

    private void getSelectedJurisdictionsData(String fileType, int requestType) {
        ReveloLogger.debug(className, "getSelectedJurisdictionsData", "moving to create geopackage for file type " + fileType);
        createGeoPackage(fileType, requestType);
    }

    private void createGeoPackage(String fileType, int requestType) {
        try {
            String assignedJurisdictionName = UserInfoPreferenceUtility.getJurisdictionName();
            String assignedJurisdictionType = UserInfoPreferenceUtility.getJurisdictionType();
            ReveloLogger.debug(className, "createGeoPackage", "getting assigned jurisdiction name and type.. " + assignedJurisdictionName + " - " + assignedJurisdictionType);

            ReveloLogger.debug(className, "createGeoPackage", "Clearing and recreating obre graph to create selection filter..");
            OrgBoundaryConceptModel.clearObReGraph();
            Graph reCmGraph = OrgBoundaryConceptModel.getObReGraph();
            Vertex rootVertex = TinkerGraphUtil.findRootVertex(reCmGraph);
            Vertex assignedVertex = reCmGraph.getVertex(assignedJurisdictionType);
            String idProperty = assignedVertex.getProperty(GraphConstants.ID_PROPERTY);
            String idPropertyDataType = "string";
            Map<String, Object> upperMap = ReDbTable.upperHierarchyMap(rootVertex, idProperty, idPropertyDataType, assignedJurisdictionName, assignedJurisdictionType, InitializationActivity.this);

            JSONObject selectionFilter = new JSONObject();
            selectionFilter.put("downloadAttachments", true);
            for (String key : upperMap.keySet()) {
                selectionFilter.put(key, upperMap.get(key));
            }


            ReveloLogger.debug(className, "createGeoPackage", "Selection filter created as.. " + selectionFilter.toString());

            ReveloLogger.debug(className, "createGeoPackage", "Start progress dialog..send intent to DownloadFileForegroundService");
//            if (progressUtility != null && progressUtility.isShowing()) {
//                ProgressUtility.changeProgressDialogMessage(progressUtility, "", "Retrieving project's data... Please wait..");
//            } else {
//                progressUtility = ProgressUtility.showProgressDialog(this, "", "Retrieving project's data... Please wait..");
//            }
            String accessToken = SecurityPreferenceUtility.getAccessToken();
            Intent intent = new Intent(InitializationActivity.this, DownloadFileForegroundService.class);
            intent.putExtra("url", "");
            intent.putExtra("accessToken", accessToken);
            intent.putExtra("dbFolder", "");
            intent.putExtra("fileName", "createFile");
            intent.putExtra("fileType", fileType);
            intent.putExtra("operationType", requestType);
            intent.putExtra(DownloadFileForegroundService.JURISDICTION, selectionFilter.toString());
            intent.putExtra("receiver", new DownloadReceiver(new Handler()));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }
            else {
                startService(intent);
            }

        } catch (Exception e) {
            ReveloLogger.error(className, "createGeoPackage", "Exception while preparing to create gropackage for file type " + fileType + ". Exception - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createMetaGPRequestReceived(int requestType) {
        ReveloLogger.debug(className, "createMetaGPRequestReceived", "Calling to create meta gp file");
        showProgressUi("downloadingmetadatadb");
        getSelectedJurisdictionsData(AppConstants.CREATE_META_GP_FILE, requestType);
    }

    private void callHomeUi(String message) {

       /* if (Insight.getApplicationFirstPage().equals(Insight.HOME)) {
            ReveloLogger.info(className, "callHomeUi", "message received for loading home ui - " + message);
            ReveloLogger.timeLog(className, "callHomeUi", "message received for loading home ui - " + message);
            Intent homeActivityIntent = new Intent(InitializationActivity.this, HomeEntityGridActivity.class);
            if (message.equalsIgnoreCase("DATABASE_FILE_EXIST")) {
                ReveloLogger.info(className, "callHomeUi", "setting reload flag false because of message - " + message);
                homeActivityIntent.putExtra("reload", false);
            }
            else {
                ReveloLogger.info(className, "callHomeUi", "setting reload flag true because of message - " + message);
                homeActivityIntent.putExtra("reload", true);
            }
            homeActivityIntent.putExtra("callingActivity", "initialize");
            homeActivityIntent.putExtra("requestType", AppConstants.LOGIN_DATA_REQUEST);
            ReveloLogger.timeLog(className, "onReceiveResult", "intentsent");
            if (phaseORSurveyChanged) {
                homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(homeActivityIntent);
            }
            else {
                homeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(homeActivityIntent);
            }
        }
        else {
            Intent intent = new Intent(InitializationActivity.this, MapActivity.class);
            intent.putExtra("callingActivity", "home");
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        }*/

        Intent homeActivityIntent = new Intent(InitializationActivity.this, DeliveryMainActivity.class);
        startActivity(homeActivityIntent);
        finish();


    }

    public void downloadOrgBoundaryConceptModel(Activity activity, int requestType) {
        ReveloLogger.debug(className, "callOrgBoundaryConceptModel", "Call org boundary concept model.");
        ReveloLogger.timeLog(className, "callOrgBoundaryConceptModel", "Call org boundary concept model.");
        showProgressUi("downloadingRedb");
        new OrgBoundaryConceptModel(activity, new IOrgBoundaryConceptModel() {
            @Override
            public void onSuccess(OBDataModel OBDataModel) {

                if (OBDataModel != null) {

                    if (! TextUtils.isEmpty(OBDataModel.getDataSourceName())) {
                        OrgBoundaryPreferenceUtility.storeDataSourceName(OBDataModel.getDataSourceName());
                    }

                    if (! TextUtils.isEmpty(OBDataModel.getGisServerUrl())) {
                        OrgBoundaryPreferenceUtility.storeGisServerUrl(OBDataModel.getGisServerUrl());
                    }

                }

                ReveloLogger.info(className, "OrgBoundaryConceptModel", "Successfully get call model from server.");

                OrgBoundaryPreferenceUtility.storeObRe(OBDataModel);
                if (requestType != AppConstants.REFRESH_DATA_REQUEST) {
//                    ProgressUtility.dismissProgressDialog(progressDialog);
                }
                boolean reDbPresent = AppFolderStructure.isReGpPresent(activity);

                if (reDbPresent) {
                    //temporary boolean reDbRequiredToDownload = UserInfoPreferenceUtility.isReBbRequired();
                    boolean reDbRequiredToDownload = false;
                    if (reDbRequiredToDownload) {
                        ReveloLogger.debug(className, "OrgBoundaryConceptModel", "proceed to download REGP file.");
                        downloadREGPFile("", requestType, null);
                    }
                    else {
                        checkDataDownloadScenario(activity, requestType);
                    }
                }
                else {
                    ReveloLogger.debug(className, "OrgBoundaryConceptModel", "proceed to download REGP file.");

                    downloadREGPFile("", requestType, null);
                }
            }

            @Override
            public void onError(String errorMsg) {
//                ProgressUtility.dismissProgressDialog(progressDialog);
                errorMessage(errorMsg, requestType, AppConstants.ERROR_GETTING_OBCM, null);
                ReveloLogger.error(className, "OrgBoundaryConceptModel", errorMsg);
            }
        });
    }

    public void downloadREGPFile(String message, int requestType, String jurisdiction) {
        downloadFile(this, AppConstants.REGP_FILE, requestType, jurisdiction);
    }

    private void downloadFile(Context context, String fileName, int requestType, String jurisdictions) {
        ReveloLogger.info(className, "downloadFile", "preparing to start download file service for request type " + requestType + " jurisdictions =" + jurisdictions + " filename=" + fileName);

        String progressMessage = "";
        String fileDbUrl = null;
        File fileDbFolder = null;
        String surveyName = UserInfoPreferenceUtility.getSurveyName();
        ReveloLogger.info(className, "downloadFile", "validating filename and jurisdictions..");
        try {
            if (fileName == null || fileName.isEmpty() || jurisdictions == null || jurisdictions.isEmpty()) {
                ReveloLogger.error(className, "downloadFile", "parameters missing..aborting download");
                String errorMessage = "Could not start downloading... Please try again";
                if (fileName == null || fileName.isEmpty()) {
                    ReveloLogger.error(className, "downloadFile", "missing filename..requesting to start download from redb");
                    errorMessage(errorMessage, requestType, DownloadFileForegroundService.ERROR_DOWNLOADING_REGP, jurisdictions);
                    return;
                }
                else if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
                    ReveloLogger.error(className, "downloadFile", "missing jurisdictions..but not needed while downloading redb. hence moving further");
                    //errorMessage(errorMessage, requestType, DownloadFileForegroundService.ERROR_DOWNLOADING_REGP, jurisdictions);
                }
                else if (fileName.contains(AppConstants.DATA_GP_FILE)) {
                    ReveloLogger.error(className, "downloadFile", "missing jurisdictions..requesting to download data db again");
                    errorMessage(errorMessage, requestType, DownloadFileForegroundService.ERROR_DOWNLOADING_DATAGP, jurisdictions);
                    return;
                }
                else if (fileName.contains(AppConstants.METADATA_FILE)) {
                    ReveloLogger.error(className, "downloadFile", "missing jurisdictions..requesting to download metadata again");
                    errorMessage(errorMessage, requestType, DownloadFileForegroundService.ERROR_DOWNLOADING_METADATAGP, jurisdictions);
                    return;
                }

            }
            else {
                ReveloLogger.info(className, "downloadFile", "basic validation of filename and jurisdictions done");
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "downloadFile", "Exception validating filename and jurisdictions - " + e.getMessage());
            e.printStackTrace();
        }

        try {
            ReveloLogger.info(className, "downloadFile", "getting download url and creating respective folders");
            if (fileName.contains(AppConstants.METADATA_FILE)) {

                progressMessage = "Downloading configuration... Please wait.";
                fileDbUrl = UrlStore.downloadMetaDatabaseUrl(surveyName);
                fileDbFolder = AppFolderStructure.createMetadataFolder(context);
                if (parentforProgressLayout != null) {
                    parentforProgressLayout.removeAllViews();
                }
                parentforProgressLayout = parentMeatadataLayout;
            }
            else if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
                String orgLabel = UserInfoPreferenceUtility.getOrgLabel();
                if (orgLabel.isEmpty()) {
                    orgLabel = "your Organization";
                }
                progressMessage = "Downloading " + orgLabel + "'s Administrative boundaries... Please wait.";
                fileDbUrl = UrlStore.getREDatabaseUrl(surveyName);
                fileDbFolder = AppFolderStructure.createReDbFolder(context);
                if (parentforProgressLayout != null) {
                    parentforProgressLayout.removeAllViews();
                }
                parentforProgressLayout = parentOrgBoundariesLayout;
            }
            else if (fileName.contains(AppConstants.DATA_GP_FILE)) {

                progressMessage = "Downloading " + UserInfoPreferenceUtility.getPhaseORSurveyName() + " data... Please wait.";
                fileDbUrl = UrlStore.downloadDataGpUrl(surveyName);
                fileDbFolder = AppFolderStructure.createDataGpFolder(context);
                if (parentforProgressLayout != null) {
                    parentforProgressLayout.removeAllViews();
                }
                parentforProgressLayout = null;
            }
//        ProgressUtility.dismissProgressDialog(progressUtility);
            if (parentforProgressLayout != null) {
                percentageProgressBarView = new PercentageProgressBarView(InitializationActivity.this, progressMessage, PercentageProgressBar.HORIZONTAL);
                parentforProgressLayout.addView(percentageProgressBarView.getView(), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            else {
                percentageProgressBar = new PercentageProgressBar(InitializationActivity.this, progressMessage, PercentageProgressBar.HORIZONTAL);
                if (! this.isFinishing()) {
                    percentageProgressBar.show();
                }
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "downloadFile", "Exception getting download url and creating respective folders - " + e.getMessage());
            e.printStackTrace();
        }

        try {
            ReveloLogger.info(className, "downloadFile", "Validating before starting download service");
            String accessToken = SecurityPreferenceUtility.getAccessToken();

            boolean allInputValid = true;
            if (accessToken == null || accessToken.isEmpty() || fileDbUrl == null || fileDbUrl.isEmpty() || jurisdictions == null || jurisdictions.isEmpty() || fileDbFolder == null || fileName == null || fileName.isEmpty()) {
                allInputValid = false;
                ReveloLogger.error(className, "downloadFile", "Error...missing params for download service..aborting file download for " + fileName);
                ReveloLogger.error(className, "downloadFile", "accessToken == null || accessToken.isEmpty()" + (accessToken == null || accessToken.isEmpty()));
                ReveloLogger.error(className, "downloadFile", "fileDbUrl == null || fileDbUrl.isEmpty() " + (fileDbUrl == null || fileDbUrl.isEmpty()));
                ReveloLogger.error(className, "downloadFile", "jurisdictions == null || jurisdictions.isEmpty() " + (jurisdictions == null || jurisdictions.isEmpty()));
                ReveloLogger.error(className, "downloadFile", "fileDbFolder == null " + (fileDbFolder == null));
                ReveloLogger.error(className, "downloadFile", "fileName == null || fileName.isEmpty()" + (fileName == null || fileName.isEmpty()));

                String errorMessage = "Could not start downloading... Please try again";
                if (fileName == null || fileName.isEmpty()) {
                    ReveloLogger.error(className, "downloadFile", "missing filename..requesting to start download from redb");
                    errorMessage(errorMessage, requestType, DownloadFileForegroundService.ERROR_DOWNLOADING_REGP, jurisdictions);
                    return;
                }
                else if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {

                    if (jurisdictions == null || jurisdictions.isEmpty() && ! (accessToken == null || accessToken.isEmpty() || fileDbUrl == null || fileDbUrl.isEmpty() || fileDbFolder == null || fileName == null || fileName.isEmpty())) {
                        allInputValid = true;
                        ReveloLogger.error(className, "downloadFile", "missing jurisdictions..but we dnt need them for redb..skipping again..");
                    }
                    else {
                        ReveloLogger.error(className, "downloadFile", "something other than jurisdictions is missing..aborting download");
                        errorMessage(errorMessage, requestType, DownloadFileForegroundService.ERROR_DOWNLOADING_REGP, jurisdictions);
                        return;
                    }
                }
                else if (fileName.contains(AppConstants.DATA_GP_FILE)) {
                    ReveloLogger.error(className, "downloadFile", "missing jurisdictions..requesting to download data db again");
                    errorMessage(errorMessage, requestType, DownloadFileForegroundService.ERROR_DOWNLOADING_DATAGP, jurisdictions);
                    return;
                }
                else if (fileName.contains(AppConstants.METADATA_FILE)) {
                    ReveloLogger.error(className, "downloadFile", "missing jurisdictions..requesting to download metadata again");
                    errorMessage(errorMessage, requestType, DownloadFileForegroundService.ERROR_DOWNLOADING_METADATAGP, jurisdictions);
                    return;
                }
            }

            if (allInputValid) {
                ReveloLogger.debug(className, "downloadFile", "all variables valid..starting Downloading file progress " + fileName);
                Intent intent = new Intent(InitializationActivity.this, DownloadFileForegroundService.class);
                intent.putExtra("url", fileDbUrl);
                intent.putExtra("accessToken", accessToken);
                intent.putExtra(DownloadFileForegroundService.JURISDICTION, jurisdictions);
                intent.putExtra("dbFolder", fileDbFolder.getAbsolutePath());
                intent.putExtra("fileName", fileName);
                intent.putExtra("operationType", requestType);
                intent.putExtra("receiver", new DownloadReceiver(new Handler()));

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(this, intent);
                }
                else {
                    startService(intent);
                }
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "downloadFile", "Exception validating before starting download service -" + e.getMessage());
            e.printStackTrace();
        }

    }

    private void checkDataDownloadScenario(Activity activity, int request) {

        boolean metaDbPresent = AppFolderStructure.isMetaDataGpPresent(activity);
        boolean reDbPresent = AppFolderStructure.isReGpPresent(activity);
        boolean DataGpPresent = AppFolderStructure.isDataGpPresent(activity);

        if (metaDbPresent && reDbPresent && DataGpPresent) { /*state_5 mode*/
            ReveloLogger.debug(className, "checkDataDownloadScenario", "All database present call home page");
            showProgressUi("downloadedMetadatadb");
            successMessage("DATABASE_FILE_EXIST", AppConstants.DATABASE_FILE_EXIST);
        }
        else if (! reDbPresent) {
            downloadOrgBoundaryConceptModel(activity, request);
        }
        else {
            ReveloLogger.debug(className, "checkDataDownloadScenario", "Proceed to show jurisdictions dialog, in this case, download metadata db");
            ReveloLogger.timeLog(className, "checkDataDownloadScenario", "Proceed to show jurisdictions dialog, in this case, download metadata db");
            successMessage("" + AppConstants.CREATE_META_GP_FILE_REQUEST, request);
        }

    }

    @Override
    public void onBackPressed() {

        if (choosePhaseFragmentDialog != null && choosePhaseFragmentDialog.isVisible()) {
            //check if user has mutiple surveys, if yes, show surveylist. else exit warning

            List<Survey> surveyNameList = UserInfoPreferenceUtility.getSurveyNameList();
            ReveloLogger.debug(className, "onBackPressed", "currently visible - phases.. checking if user has multiple surveys assigned");

            if (surveyNameList != null && surveyNameList.size() > 1) {
                ReveloLogger.debug(className, "onBackPressed", "Multiple surveys assigned to this user..Showing survey blocks");
//                if (progressUtility != null && progressUtility.isShowing()) {
//                    ProgressUtility.changeProgressDialogMessage(progressUtility, "Retrieving project details",
//                            getResources().getString(R.string.progress_message_login));
//                } else {
//                    progressUtility = ProgressUtility.showProgressDialog(InitializationActivity.this, "Retrieving project details",
//                            getResources().getString(R.string.progress_message_login));
//                }
                try {
                    choosePhaseFragmentDialog.dismissAllowingStateLoss();//.dismiss();
                } catch (Exception e) {
                    ReveloLogger.error(className, "onBackPressed", "Error closing error message " + e.getMessage());
                    e.printStackTrace();
                }
                showSurveyList("", AppConstants.LOGIN_DATA_REQUEST);
            }
            else {
                ReveloLogger.debug(className, "showSurveyList", "Single or no survey assigned to this user..Showing exit warning");
                showExitAppWarning();
            }

        }
        else if (chooseSurveyFragmentDialog != null && chooseSurveyFragmentDialog.isVisible()) {
            ReveloLogger.debug(className, "onBackPressed", "currently visible - survey.. Showing exit warning");
            showExitAppWarning();
        }
        else {
            ReveloLogger.debug(className, "onBackPressed", "currently visible - nothing.. Showing exit warning");
            showExitAppWarning();
        }
    }

    public void showExitAppWarning() {
        InfoBottomSheet infoBottomSheet = InfoBottomSheet.geInstance(this, getResources().getString(R.string.dialog_yes), getResources().getString(R.string.dialog_no), getResources().getString(R.string.title_exit_app), getResources().getString(R.string.message_exit_app), AppConstants.EXIT_REQUEST, 0, "");

        infoBottomSheet.setCancelable(false);
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            infoBottomSheet.show(getSupportFragmentManager(), BottomSheetTagConstants.appExitInfo);
        }
        ReveloLogger.debug(className, "onBackPressed", "Request for exist applications.");
    }

    @Override
    public void onOkInfoBottomSheetResult(int requestCode, int errorCode, String jurisdictions) {
        updateFullScreenProgressBar(SHOW);
        ReveloLogger.debug(className, "onOkInfoBottomSheetResult", "ok clicked after request " + requestCode + " , errorcode " + errorCode + " for data/jurisdictions " + jurisdictions);

        if (requestCode == AppConstants.EXIT_REQUEST) {
            ReveloLogger.debug(className, "onOkInfoBottomSheetResult", "OK clicked. request - EXIT_REQUEST request");
            ReveloLogger.debug(className, "onOkInfoBottomSheetResult", "finishing affinity and exiting");
            try {
                if (choosePhaseFragmentDialog != null && choosePhaseFragmentDialog.isVisible()) {
                    choosePhaseFragmentDialog.dismissAllowingStateLoss();//.dismiss();
                }
            } catch (Exception e) {
                ReveloLogger.error(className, "onOkInfoBottomSheetResult", "Error closing phase fragment " + e.getMessage());
                e.printStackTrace();
            }

            try {
                if (chooseSurveyFragmentDialog != null && chooseSurveyFragmentDialog.isVisible()) {
                    chooseSurveyFragmentDialog.dismissAllowingStateLoss();//.dismiss();
                }
            } catch (Exception e) {
                ReveloLogger.error(className, "onOkInfoBottomSheetResult", "Error closing survey fragment " + e.getMessage());
                e.printStackTrace();
            }


            ActivityCompat.finishAffinity(this);
        }
        else if (requestCode == AppConstants.LOGIN_DATA_REQUEST) {
            ReveloLogger.debug(className, "onCancelOkBottomSheetResult", "ok clicked after error message for login_data_rerquest... Checking errorcode..");
            if (errorCode == DownloadFileForegroundService.ERROR_CREATING_DATA_DB || errorCode == DownloadFileForegroundService.ERROR_DOWNLOADING_DATAGP || errorCode == DownloadFileForegroundService.ERROR_CREATING_META_DATA_DB || errorCode == DownloadFileForegroundService.ERROR_DOWNLOADING_METADATAGP) {
                ReveloLogger.debug(className, "onOkInfoBottomSheetResult", "Retry clicked after db download failed. failure error code - " + requestCode + "..");
                ReveloLogger.debug(className, "onOkInfoBottomSheetResult", "retrying download..");
//                ProgressUtility.dismissProgressDialog(progressUtility);
                checkDataDownloadScenario(InitializationActivity.this, requestCode);
            }
            else if (errorCode == DownloadFileForegroundService.ERROR_DOWNLOADING_REGP) {
                ReveloLogger.debug(className, "onOkInfoBottomSheetResult", "retry clicked after download regp failed message. retrying regp download");
                showProgressUi("downloadingRedb");
                downloadREGPFile("", requestCode, null);
            }
            else if (errorCode == AppConstants.ERROR_GETTING_OBCM) {
                ReveloLogger.debug(className, "onOkInfoBottomSheetResult", "retry clicked after failed to fetch obcm message. retrying fetching obcm");
                showProgressUi("downloadingRedb");
                downloadOrgBoundaryConceptModel(InitializationActivity.this, requestCode);
            }
            else {
                ReveloLogger.debug(className, "onOkInfoBottomSheetResult", "Error code found = " + errorCode + ". Currently, we have nothing for it. calling principal endpoint");
                logout();
            }
        }
        else if (requestCode == AppConstants.LOGOUT_REQUEST) {
            ReveloLogger.info(className, "onOkInfoBottomSheetResult", "OK clicked. request - LOGOUT_REQUEST request " + requestCode);
            ReveloLogger.info(className, "onOkInfoBottomSheetResult", "checking for error codes..");
            if (errorCode == AppConstants.ERROR_TRAIL_ON) {
                ReveloLogger.info(className, "onOkInfoBottomSheetResult", "ok click means stop trail and logout request..stopping trail and retrying logout");
//                progressUtility = ProgressUtility.showProgressDialog(InitializationActivity.this, "", "Stopping trail... Please wait.");
                if (! LocationReceiverService.isServiceRunning.equalsIgnoreCase(LocationReceiverService.STATE.LOCATION_SERIVCE_STATE_STOPPED)) {
                    stopLocationService("drawTrail", this);
                }
//                ProgressUtility.dismissProgressDialog(progressUtility);
                ReveloLogger.info(className, "onOkInfoBottomSheetResult", "calling presenter to logout with (same) request code " + requestCode);
                logoutRequest(AppConstants.LOGOUT_REQUEST);

            }
            else if (errorCode == AppConstants.ERROR_WHEREAMI_ON) {
                ReveloLogger.info(className, "onOkInfoBottomSheetResult", "ok click means stop where am i and logout request..stopping trail and retrying logout");
//                progressUtility = ProgressUtility.showProgressDialog(InitializationActivity.this, "", "Stopping Where Am I... Please wait.");
//                stopWhereAmIService(this);
//                ProgressUtility.dismissProgressDialog(progressUtility);
                ReveloLogger.info(className, "onOkInfoBottomSheetResult", "calling presenter to logout with (same) request code " + requestCode);
                logoutRequest(AppConstants.LOGOUT_REQUEST);
            }
            else if (errorCode == AppConstants.ERROR_UPLOAD_AVAILABLE) {
                ReveloLogger.info(className, "onOkInfoBottomSheetResult", "ok click means stop upload and logout request..uploading data and retrying logout");
                //progressUtility = ProgressUtility.showProgressbar(HomeEntityGridActivity.this,"","Uploading data... Please wait.");
                try {
                    ReveloLogger.info(className, "onOkInfoBottomSheetResult", "calling presenter to upload data with (same) request code " + requestCode);
                    new Upload(this, this, requestCode,false);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            else if (errorCode == AppConstants.ERROR_NO_NETWORK) {
                ReveloLogger.info(className, "onOkInfoBottomSheetResult", "ok click means no network, retry logout request.. retrying logout");
                ReveloLogger.info(className, "onOkInfoBottomSheetResult", "calling presenter to logout with (same) request code " + requestCode);
                logoutRequest(AppConstants.LOGOUT_REQUEST);
            }
            else {
                logout();
            }
        }
        else if (requestCode == AppConstants.LOGOUT_RETRY) {
            ReveloLogger.info(className, "onOkInfoBottomSheetResult", "OK clicked. request - LOGOUT_RETRY request");
            ReveloLogger.info(className, "onOkInfoBottomSheetResult", "calling presenter to logout with (same) request code " + requestCode);
            // iHomePresenter.onLogout(this, InitializationActivity.this, requestCode);
            logoutRequest(AppConstants.LOGOUT_REQUEST);
        }
        else if (requestCode == AppConstants.LOGOUT_UPLOAD_DATA_AVAILABLE_RETRY) {
            ReveloLogger.info(className, "onOkInfoBottomSheetResult", "OK clicked. request - LOGOUT_UPLOAD_DATA_AVAILABLE_RETRY request");
            logout();
        }

    }

    @Override
    public void onCancelOkBottomSheetResult(int requestCode, int errorCode) {
        if (requestCode == AppConstants.EXIT_REQUEST) {
            //do nothing
        }
        else if (requestCode == AppConstants.LOGIN_DATA_REQUEST) {
            ReveloLogger.error(className, "onOkInfoBottomSheetResult", "cancel clicked after no internet message");
            ReveloLogger.debug(className, "onOkInfoBottomSheetResult", "finishing affinity and exiting");
            logout();
        }
        else if (requestCode == AppConstants.LOGOUT_REQUEST) {
            ReveloLogger.info(className, "onCancelInfoBottomSheetResult", "Cancel clicked. request - LOGOUT_REQUEST request " + requestCode);
            ReveloLogger.info(className, "onCancelInfoBottomSheetResult", "checking for error codes..");
            if (errorCode == AppConstants.ERROR_TRAIL_ON || errorCode == AppConstants.ERROR_WHEREAMI_ON || errorCode == AppConstants.ERROR_NO_NETWORK) {
                //do nothing
            }
            else if (errorCode == AppConstants.ERROR_UPLOAD_AVAILABLE) {
                ReveloLogger.info(className, "onCancelInfoBottomSheetResult", "cancel click means do not upload and logout request..Warning user");
                String warningMessage = "Are you sure you want to logout without uploading data?";
                errorMessage(warningMessage, AppConstants.LOGOUT_UPLOAD_DATA_AVAILABLE_RETRY, AppConstants.ERROR_UPLOAD_AVAILABLE, "");
            }
        }
        else if (requestCode == AppConstants.LOGOUT_UPLOAD_DATA_AVAILABLE_RETRY) {
            //do nothing
        }
        else if (requestCode == AppConstants.LOGOUT_RETRY) {
            //do nothing
        }
        else if (requestCode == AppConstants.UPLOAD_REQUEST) {
            //do nothing
        }
        else {
            logoutRequest(requestCode);
        }
        if (errorCode == DownloadFileForegroundService.ERROR_DOWNLOADING_REGP || errorCode == DownloadFileForegroundService.ERROR_CREATING_DATA_DB || errorCode == DownloadFileForegroundService.ERROR_DOWNLOADING_DATAGP || errorCode == DownloadFileForegroundService.ERROR_CREATING_META_DATA_DB || errorCode == DownloadFileForegroundService.ERROR_DOWNLOADING_METADATAGP) {
            ReveloLogger.debug(className, "onCancelOkBottomSheetResult", "Exit clicked after db download failed. failure error code - " + requestCode + "..");
            ReveloLogger.debug(className, "onCancelOkBottomSheetResult", "finishing affinity and exiting");
//            ProgressUtility.dismissProgressDialog(progressUtility);
            try {
                if (choosePhaseFragmentDialog != null && choosePhaseFragmentDialog.isVisible()) {
                    choosePhaseFragmentDialog.dismissAllowingStateLoss();//.dismiss();
                }
            } catch (Exception e) {
                ReveloLogger.error(className, "onCancelOkBottomSheetResult", "Error closing phase fragment " + e.getMessage());
                e.printStackTrace();
            }

            try {
                if (chooseSurveyFragmentDialog != null && chooseSurveyFragmentDialog.isVisible()) {
                    chooseSurveyFragmentDialog.dismissAllowingStateLoss();//.dismiss();
                }
            } catch (Exception e) {
                ReveloLogger.error(className, "onCancelOkBottomSheetResult", "Error closing survey fragment " + e.getMessage());
                e.printStackTrace();
            }

            ActivityCompat.finishAffinity(this);
        }
    }

    private void logout() {
        ReveloLogger.info(className, "logoutRequest", "executing logout");
        ReveloLogger.info(className, "logoutRequest", "logout - clearing all static data and mapactivity.maploaded set to false");
        AppMethods.clearAllStaticData();
        ReveloLogger.info(className, "logoutRequest", "redirecting to login activity");
        Intent homeActivityIntent = new Intent(InitializationActivity.this, LoginActivity.class);
        startActivity(homeActivityIntent);
        finish();
    }

    private void logoutRequest(int requestcode) {
//        ProgressUtility.dismissProgressDialog(progressUtility);
        boolean proceedWithLogout = serviceAndDataStateAllowsOperation(InitializationActivity.this, requestcode, true, true);
        if (proceedWithLogout) {
            logout();
        }
    }

    public boolean serviceAndDataStateAllowsOperation(Activity activity, int request, boolean checkForUpload, boolean checkForWhereAmI) {
        if (NetworkUtility.checkNetworkConnectivity(activity)) {
            boolean isTrailStopped = LocationReceiverService.isServiceRunning.equalsIgnoreCase(LocationReceiverService.STATE.LOCATION_SERIVCE_STATE_STOPPED) && LocationReceiverService.trailState.equalsIgnoreCase("stopped");
//            boolean isWhereAmIRunning = (WUtils.isWhereIAmServiceRunning((ActivityManager) activity.getSystemService(ACTIVITY_SERVICE)));
            if (! isTrailStopped) {
                //show dialog to stop trail
                String errorMessage = activity.getResources().getString(R.string.error_trail_running);
                errorMessage(errorMessage, request, AppConstants.ERROR_TRAIL_ON, null);

                ReveloLogger.debug(className, "serviceAndDataStateAllowsOperation", "Trail is running..");
            }
//            else if (isWhereAmIRunning && checkForWhereAmI) {
//                ReveloLogger.debug(className, "serviceAndDataStateAllowsOperation", "whereami is running..");
//                if (checkForWhereAmI) {
//                    //show dialog to stop whereami
//                    String errorMessage = activity.getResources().getString(R.string.error_whereami_running);
//                    errorMessage(errorMessage, request, AppConstants.ERROR_WHEREAMI_ON, null);
//
//                    ReveloLogger.debug(className, "serviceAndDataStateAllowsOperation", "whereami is running..not allowing operation");
//                }
//                else {
//                    ReveloLogger.debug(className, "serviceAndDataStateAllowsOperation", "whereami is running..but no checking was ordered..hence allowing operation");
//                }
//            }
            else if (checkForUpload) {
                boolean isDataForUpload = EditMetaDataTable.isDataForUpload("", activity);
                if (isDataForUpload) {
                    //show dialog to upload data
                    String errorMessage = activity.getResources().getString(R.string.error_data_have_upload);
                    errorMessage(errorMessage, request, AppConstants.ERROR_UPLOAD_AVAILABLE, null);

                    ReveloLogger.debug(className, "serviceAndDataStateAllowsOperation", "Data available for upload...");
                }
                else {
                    if (NetworkUtility.checkNetworkConnectivity(activity)) {
                        return true;
                    }
                    else {
                        String errorMessage = activity.getResources().getString(R.string.error_network_download);
                        errorMessage(errorMessage, request, AppConstants.ERROR_NO_NETWORK, null);
                        ReveloLogger.error(className, "serviceAndDataStateAllowsOperation", errorMessage);
                    }
                }
            }
            else {
                return true;
            }
        }
        else {
            String errorMessage = activity.getResources().getString(R.string.error_network_download);
            errorMessage(errorMessage, request, AppConstants.ERROR_NO_NETWORK, null);
            ReveloLogger.error(className, "serviceAndDataStateAllowsOperation", errorMessage);
        }
        return false;
    }

    @Override
    public void onUserProfileFetchingSuccessfull() {
        ReveloLogger.debug(className, "onUserProfileFetchingSuccessfull", "user profile fetched successfully..initializing navigation component");
        initNavigationComponent();
    }

    private void initNavigationComponent() {
        ReveloLogger.info(className, "initNavigationComponent", "initializing side navigation bar");
        View header = navigationView.getHeaderView(0);
        applyInsightOnHeaderView(header);
        TextView firstName = header.findViewById(R.id.firstNameId);
        TextView lastName = header.findViewById(R.id.lastNameId);
        TextView phoneNumber = header.findViewById(R.id.phoneNumberId);
        TextView username = header.findViewById(R.id.userNameId);
        TextView surveyName = header.findViewById(R.id.surveynameIdTV);
        TextView changeSurveyId = header.findViewById(R.id.changeSurveyId);
        TextView surveyPhaseName = header.findViewById(R.id.surveyPhaseNameIdTV);
        TextView changeSurveyPhaseId = header.findViewById(R.id.changeSurveyPhaseId);
        TextView changeJurisdictionId = header.findViewById(R.id.changeJurisdictionId);
        TextView projectPhaseTitle = header.findViewById(R.id.projectPhaseTitle);
        LinearLayout surveyPhaseLayout = header.findViewById(R.id.surveyPhaseLayout);
        ImageView userIcon = header.findViewById(R.id.userIconIV);

        String mFirstName = UserInfoPreferenceUtility.getFirstName();
        String mLastName = UserInfoPreferenceUtility.getLastName();
        String mPhoneNumber = UserInfoPreferenceUtility.getPhoneNumber();
        String mUsername = UserInfoPreferenceUtility.getUserName();
        String mSurveyName = UserInfoPreferenceUtility.getSurveyName();
        String mSurveyLabel = UserInfoPreferenceUtility.getSurveyNameLabel();
        String mSurveyPhaseLabel = UserInfoPreferenceUtility.getSurveyPhaseLabel(mSurveyName);

        Survey mSurvey = SurveyPreferenceUtility.getSurvey(mSurveyName);

        String fileName = UserInfoPreferenceUtility.getUserName() + "ProfilePic.png";
        File profilepic = new File(AppFolderStructure.userProfilePictureFolderPath(InitializationActivity.this) + File.separator + fileName);
        if (profilepic.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(profilepic.getAbsolutePath());
            ((ImageView) header.findViewById(R.id.userIconIV)).setImageBitmap(myBitmap);
        }
        else {
            if (NetworkUtility.checkNetworkConnectivity(InitializationActivity.this)) {
                new GetProfilePicAsyncTask(InitializationActivity.this, UserInfoPreferenceUtility.getUserName(), userIcon).execute();
            }
        }

        rotate_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        ReveloLogger.info(className, "initNavigationComponent", "firstname lastname set - " + mFirstName + " " + mLastName);

        if (mFirstName.length() >= 15) {
            firstName.setText(mFirstName.substring(0, 15) + "...");
        }
        else {
            firstName.setText(mFirstName);
        }

        if (mLastName.length() >= 15) {
            lastName.setText(mLastName.substring(0, 15) + "...");
        }
        else {
            lastName.setText(mLastName);
        }
        phoneNumber.setText("Phone Number : " + mPhoneNumber);
        if (mSurveyLabel.isEmpty() || mSurvey == null) {
            surveyName.setText("No Project assigned");
        }
        else {
            surveyName.setText(mSurveyLabel);
            if (UserInfoPreferenceUtility.getSurveyNameList() == null || UserInfoPreferenceUtility.getSurveyNameList().size() <= 1) {
                changeSurveyId.setVisibility(View.GONE);
            }
            else {
                changeSurveyId.setVisibility(View.VISIBLE);
            }
        }
        changeSurveyId.setVisibility(View.GONE);
        if (mSurveyLabel.isEmpty() || mSurvey == null || mSurveyPhaseLabel == null || mSurveyPhaseLabel.isEmpty()) {
            projectPhaseTitle.setVisibility(View.GONE);
            surveyPhaseLayout.setVisibility(View.GONE);
        }
        else {
            projectPhaseTitle.setVisibility(View.VISIBLE);
            surveyPhaseLayout.setVisibility(View.VISIBLE);
            surveyPhaseName.setText(mSurveyPhaseLabel);

            HashMap<String, Phase> phaseNameMap = mSurvey.getPhasesNameMapFromJson();
            if (phaseNameMap == null || phaseNameMap.isEmpty() || phaseNameMap.size() <= 1) {
                changeSurveyPhaseId.setVisibility(View.GONE);
            }
            else {
                changeSurveyPhaseId.setVisibility(View.VISIBLE);
            }
        }
        changeSurveyPhaseId.setVisibility(View.GONE);
        username.setText(mUsername);
        ReveloLogger.info(className, "initNavigationComponent", "username set - " + mUsername);

        showJurisdictionDetails(header);
        ReveloLogger.info(className, "initNavigationComponent", "jurisdiction data set");

        setTitle(mSurveyLabel);
        ReveloLogger.info(className, "initNavigationComponent", "title suveyname  set -" + mSurveyLabel);

        Menu menu = navigationView.getMenu();
        MenuItem changePhase = menu.findItem(R.id.nav_change_phase);
        Survey selectedSurvey = SurveyPreferenceUtility.getSurvey(UserInfoPreferenceUtility.getSurveyName());
        if (selectedSurvey != null && selectedSurvey.hasPhases()) {
            ReveloLogger.info(className, "initNavigationComponent", "Survey has phases? -true");

            changePhase.setVisible(selectedSurvey.getPhasesNameMapFromJson() != null && selectedSurvey.getPhasesNameMapFromJson().size() > 1);
            changePhase.setVisible(false);//temporary//remove from nav menu, put into edi profile section
        }
        changeJurisdictionId.setVisibility(View.GONE);

        MenuItem settings = menu.findItem(R.id.settings).setVisible(Insight.isApplicationWhereAmIAccess());
        ReveloLogger.info(className, "initNavigationComponent", "Display Navigation component information");
        setNavigationPanelWidth();
    }

    private void applyInsightOnHeaderView(View header) {
        if (header != null) {
            RelativeLayout survey_tab, phase_tab, jurisdiction_tab;
            survey_tab = header.findViewById(R.id.survey_tab);
            phase_tab = header.findViewById(R.id.phase_tab);
            jurisdiction_tab = header.findViewById(R.id.jurisdiction_tab);

            if (Insight.getNavSurveyTabVis()) {
                survey_tab.setVisibility(View.VISIBLE);
            }
            else {
                survey_tab.setVisibility(View.GONE);
            }

            if (Insight.getNavPhaseTabVis()) {
                phase_tab.setVisibility(View.VISIBLE);
            }
            else {
                phase_tab.setVisibility(View.GONE);
            }

            if (Insight.getNavJurisdictionVis()) {
                jurisdiction_tab.setVisibility(View.VISIBLE);
            }
            else {
                jurisdiction_tab.setVisibility(View.GONE);
            }


        }
    }

    @SuppressLint("SetTextI18n")
    private void showJurisdictionDetails(View header) {

        //TextView surveyNameTV = header.findViewById(R.id.surveynameIdTV);
        /*String surveyName = "No Project selected";
        if(!UserInfoPreferenceUtility.getSurveyName().isEmpty()){
            surveyName = "Selected : " + UserInfoPreferenceUtility.getSurveyName();
            if(!UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()){
                surveyName+= " (" + AppMethods.capitaliseFirstLatter(UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName())) + ")";
            }
        }
        surveyNameTV.setText(surveyName);*/

        TextView assignJurisdictionName = header.findViewById(R.id.assignJurisdictionNameTV);
        LinearLayout selectedJurisdictionRL = header.findViewById(R.id.selectedJurisdictionRL);
        TextView selectedJurisdictionName = header.findViewById(R.id.selectedJurisdictionNameTv);

        LinearLayout selectedJurisdictionHierachyLV = header.findViewById(R.id.selectedJurisdictionHierachyLV);
        RecyclerView selectedJurisdictionRV = header.findViewById(R.id.selectedJurisdictionRV);

        final ImageView downArrowIV = header.findViewById(R.id.downArrowIV);
        final ImageView upArrowIV = header.findViewById(R.id.upArrowIV);

        String nameOfAssignJurisdiction = AppMethods.capitaliseFirstLatter(UserInfoPreferenceUtility.getJurisdictionName());
        String typeOfAssignJurisdiction = AppMethods.capitaliseFirstLatter(UserInfoPreferenceUtility.getJurisdictionType());
        String jurisdictionName = "Assigned : " + nameOfAssignJurisdiction + " (" + typeOfAssignJurisdiction + ")";
        assignJurisdictionName.setText(jurisdictionName);
        ReveloLogger.info(className, "showJurisdictionDetails", "assigned jurisdiction data set - " + jurisdictionName);
        try {
            String jurisdictions = JurisdictionInfoPreferenceUtility.getJurisdictions();
            if (! TextUtils.isEmpty(jurisdictions)) {
                JSONObject jurisdiction = new JSONObject(jurisdictions);

                List<Jurisdiction> userJurisdictionModelList = new ArrayList<>();
                Iterator<String> keys = jurisdiction.keys();
                String mSelectedJurisdictionValue = "";
                String mSelectedJurisdictionType = "";

                while (keys.hasNext()) {

                    String key = keys.next();

                    if (! key.equalsIgnoreCase("downloadAttachments")) {
                        String type = AppMethods.capitaliseFirstLatter(key);
                        String name = AppMethods.capitaliseFirstLatter(jurisdiction.getString(key));

                        Jurisdiction userJusrisdictionModel = new Jurisdiction();
                        userJusrisdictionModel.setName(name);
                        userJusrisdictionModel.setType(type);

                        mSelectedJurisdictionValue = name;
                        mSelectedJurisdictionType = type;

                        userJurisdictionModelList.add(userJusrisdictionModel);
                    }
                }

                String name = "Selected : " + mSelectedJurisdictionValue + " (" + mSelectedJurisdictionType + ")";
                selectedJurisdictionName.setText(name);
                ReveloLogger.info(className, "showJurisdictionDetails", "selected jurisdictions set - " + name);
                ReveloLogger.info(className, "showJurisdictionDetails", "setting adapter for jurisdictions details");
                UserJurisdictionsAdapter userJurisdictionsAdapter = new UserJurisdictionsAdapter(userJurisdictionModelList);
                selectedJurisdictionRV.setHasFixedSize(true);
                selectedJurisdictionRV.setLayoutManager(new LinearLayoutManager(InitializationActivity.this));
                selectedJurisdictionRV.setAdapter(userJurisdictionsAdapter);

                selectedJurisdictionRL.setOnClickListener(v -> {
                    if (downArrowIV.getVisibility() == View.VISIBLE) {
                        downArrowIV.setVisibility(View.GONE);
                        upArrowIV.setVisibility(View.VISIBLE);
                        selectedJurisdictionHierachyLV.startAnimation(rotate_down);
                        selectedJurisdictionHierachyLV.setVisibility(View.VISIBLE);
                    }
                    else {
                        downArrowIV.setVisibility(View.VISIBLE);
                        upArrowIV.setVisibility(View.GONE);
                        selectedJurisdictionHierachyLV.setVisibility(View.GONE);
                    }
                });
            }
            else {
                ReveloLogger.error(className, "showJurisdictionDetails", "selected jurisdictions empty!!");
            }

            ReveloLogger.info(className, "showJurisdictionDetails", "Display jurisdictions information.");
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "showJurisdictionDetails", "error occurred while " + e.getMessage());
        }
    }

    private void setNavigationPanelWidth() {
        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        float widthOfPanel = (float) ((Utilities.Screen.screenWidth) * 0.68);
        params.width = Math.round(widthOfPanel);
        navigationView.setLayoutParams(params);
    }

    @Override
    public void onUserProfileFetchingFailed() {
        ReveloLogger.error(className, "onUserProfileFetchingFailed", "user profile fetching failed..doing nothing");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_refresh) {

            String title = getResources().getString(R.string.title_refresh_data);
            String message = getResources().getString(R.string.message_refresh_data);

            InfoBottomSheet infoBottomSheet = InfoBottomSheet.geInstance(this, "Yes", "No", title, message, AppConstants.REFRESH_DATA_REQUEST, 0, "");
            infoBottomSheet.setCancelable(false);
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                infoBottomSheet.show(getSupportFragmentManager(), BottomSheetTagConstants.refreshInfo);
            }

        }
        else if (id == R.id.settings) {

        }
        else if (id == R.id.nav_help) {

        }
        else if (id == R.id.nav_about_us) {

        }
        else if (id == R.id.nav_logout) {
            logoutRequest(AppConstants.LOGOUT_REQUEST);
        }

        ReveloLogger.debug(className, "onNavigationItemSelected", "call method for " + id);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void showUploadResultDialog(JSONObject uploadResponseJsonObject, int requestType) {
        ReveloLogger.info(className, "showUploadResultDialog", "showing upload result - " + uploadResponseJsonObject);
    }

    @Override
    public void onUploadResultDialogDismissed(int requestType) {
        if (requestType == AppConstants.LOGOUT_REQUEST) {
//            ProgressUtility.dismissProgressDialog(progressUtility);
            ReveloLogger.info(className, "onOkInfoBottomSheetResult", "calling presenter to logout with (same) request code " + requestType);
            logoutRequest(requestType);
        }
    }

    private class DownloadReceiver extends ResultReceiver {

        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            super.onReceiveResult(resultCode, resultData);
            if (resultData == null) {
                return;
            }
            String fileName = "";
            try {
                fileName = resultData.getString("fileName");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (fileName == null || fileName.isEmpty()) {
                return;
            }


            if (fileName.equalsIgnoreCase(DownloadFileForegroundService.CREATE_FILE)) { // used for to create metadata and data db.
                if (resultCode == DownloadFileForegroundService.CREATE_DATA_DB_CHANGE_PROGRESS_MESSAGE) {

//                    progressUtility.setMessage("Retrieving project information... Please wait.");

                }
                else if (resultCode == DownloadFileForegroundService.CREATED_META_DATA_DB_SUCCESS) {

//                    ProgressUtility.dismissProgressDialog(progressUtility);

                    String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);
                    downloadFile(InitializationActivity.this, UserInfoPreferenceUtility.getMetatdataDbName(), AppConstants.LOGIN_DATA_REQUEST, jurisdictions);
                }
                else if (resultCode == DownloadFileForegroundService.CREATED_DATA_DB_SUCCESS) {

//                    ProgressUtility.dismissProgressDialog(progressUtility);

                    String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);
                    downloadFile(InitializationActivity.this, UserInfoPreferenceUtility.getDataDbName(), AppConstants.LOGIN_DATA_REQUEST, jurisdictions);
                }
                else if (resultCode == DownloadFileForegroundService.ERROR_CREATING_META_DATA_DB) {

//                    ProgressUtility.dismissProgressDialog(progressUtility);
                    String errorMessage = resultData.getString("errorMessage");
                    int originalRequestCode = resultData.getInt("requestCode");
                    String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);


                    errorMessage(errorMessage, originalRequestCode, DownloadFileForegroundService.ERROR_CREATING_META_DATA_DB, jurisdictions);

                }
                else if (resultCode == DownloadFileForegroundService.ERROR_CREATING_DATA_DB) {

//                    ProgressUtility.dismissProgressDialog(progressUtility);
                    String errorMessage = resultData.getString("errorMessage");
                    int originalRequestCode = resultData.getInt("requestCode");
                    String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);
                    errorMessage(errorMessage, originalRequestCode, DownloadFileForegroundService.ERROR_CREATING_DATA_DB, jurisdictions);
                }

                ReveloLogger.debug(className, "onReceiveResult", "File creating start for " + resultCode);

            }
            else {  // else part used for to downlaod file.
                if (resultCode == DownloadFileForegroundService.UPDATE_PROGRESS) {
                    int progress = resultData.getInt("progress"); //get the progress

                    if (percentageProgressBarView != null) {
                        percentageProgressBarView.setDownloadProgress(progress);
                    }
                    else if (percentageProgressBar != null) {
                        percentageProgressBar.setDownloadProgress(progress);
                    }
                }
                else if (resultCode == DownloadFileForegroundService.DOWNLOAD_SPEED_SIZE) {

                    int downloadSpeed = resultData.getInt("speedData");
                    int downlaodSizeData = resultData.getInt("downlaodSizeData");
                    String finalFileSizeString = resultData.getString("finalFileSizeString");

                    if (percentageProgressBarView != null) {
                        percentageProgressBarView.setDownloadSize(downlaodSizeData, finalFileSizeString);
                        percentageProgressBarView.setDownloadSpeed(downloadSpeed);
                    }
                    else if (percentageProgressBar != null) {
                        percentageProgressBar.setDownloadSize(downlaodSizeData, finalFileSizeString);
                        percentageProgressBar.setDownloadSpeed(downloadSpeed);
                    }
                }
                else if (resultCode == DownloadFileForegroundService.FINAL_DOWNLOAD) {  //successfully file dowlaod

                    String downloadedFile = resultData.getString("downloadedFile");
                    String destinationFolder = resultData.getString("destinationFolder");
                    int requestCode = resultData.getInt("requestCode");

                    String jurisdiction = resultData.getString(DownloadFileForegroundService.JURISDICTION);
                    fileDownloadSuccess(downloadedFile, destinationFolder, fileName);

                    if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
                        if (parentforProgressLayout != null) {
                            parentforProgressLayout.removeAllViews();
                        }
                        if (percentageProgressBarView != null) {
                            percentageProgressBarView.dismiss();
                        }
                        else if (percentageProgressBar != null) {
                            percentageProgressBar.dismiss();
                        }
                        showProgressUi("downloadedReDb");
                        UserInfoPreferenceUtility.setReBbRequired(false);
                        /// AppMethods.clearRedbRelatedStaticVariables();
                        createMetaGPRequestReceived(requestCode);//redb downloaded.
                        ReveloLogger.info(className, "onReceiveResult", "Show jurisdictions dialog.");
                    }
                    else if (fileName.contains(AppConstants.METADATA_FILE)) {
                        if (parentforProgressLayout != null) {
                            parentforProgressLayout.removeAllViews();
                        }
                        if (percentageProgressBarView != null) {
                            percentageProgressBarView.dismiss();
                        }
                        else if (percentageProgressBar != null) {
                            percentageProgressBar.dismiss();
                        }
                        showProgressUi("downloadedMetadataDb");
                        JurisdictionInfoPreferenceUtility.storeJurisdictions(jurisdiction);
                        createGPRequestReceived(requestCode);
                        //AppMethods.clearAllDatabaseRelatedStaticVariables();
                        ///AppMethods.clearMetadataDbRelatedStaticVariables();
                        ///AppMethods.clearDataDbRelatedStaticVariables();
                    }
                    else if (fileName.contains(AppConstants.DATA_GP_FILE)) {
                        if (parentforProgressLayout != null) {
                            parentforProgressLayout.removeAllViews();
                        }
                        if (percentageProgressBarView != null) {
                            percentageProgressBarView.dismiss();
                        }
                        else if (percentageProgressBar != null) {
                            percentageProgressBar.dismiss();
                        }
                        String message = "Data downloaded successfully";
                        ReveloLogger.info(className, "onReceiveResult", message);
                        ReveloLogger.timeLog(className, "onReceiveResult", message);
                        showProgressUi("downloadedMetadataDb");
                        JurisdictionInfoPreferenceUtility.storeJurisdictions(jurisdiction);
                        AppMethods.clearAllDatabaseRelatedStaticVariables();
                        ///AppMethods.clearMetadataDbRelatedStaticVariables();
                        ///AppMethods.clearDataDbRelatedStaticVariables();
                        AppMethods.clearDatabaseRelatedStaticVariables();
                        callHomeUi(message);//after all db files downloaded successfully

                    }
                    else {
                        downloadFile(InitializationActivity.this, UserInfoPreferenceUtility.getMetatdataDbName(), AppConstants.LOGIN_DATA_REQUEST, jurisdiction); // proceed for download metadata.
                    }

                }
                else if (resultCode == DownloadFileForegroundService.FAIL_DOWNLOAD) {
                    if (parentforProgressLayout != null) {
                        parentforProgressLayout.removeAllViews();
                    }
                    if (percentageProgressBarView != null) {
                        percentageProgressBarView.dismiss();
                    }
                    else if (percentageProgressBar != null) {
                        percentageProgressBar.dismiss();
                    }
                    String errorMessage = resultData.getString("errorMessage");
                    int originalRequestCode = resultData.getInt("requestCode");
                    String jurisdictions = resultData.getString(DownloadFileForegroundService.JURISDICTION);

                    if (fileName.equalsIgnoreCase(AppConstants.REGP_FILE)) {
                        errorMessage(errorMessage, originalRequestCode, DownloadFileForegroundService.ERROR_DOWNLOADING_REGP, jurisdictions);
                    }
                    else if (fileName.contains(AppConstants.DATA_GP_FILE)) {
                        errorMessage(errorMessage, originalRequestCode, DownloadFileForegroundService.ERROR_DOWNLOADING_DATAGP, jurisdictions);
                    }
                    else if (fileName.contains(AppConstants.METADATA_FILE)) {
                        errorMessage(errorMessage, originalRequestCode, DownloadFileForegroundService.ERROR_DOWNLOADING_METADATAGP, jurisdictions);
                    }

                    ReveloLogger.error(className, "onReceiveResult", errorMessage);
                }
            }


        }
    }

}
