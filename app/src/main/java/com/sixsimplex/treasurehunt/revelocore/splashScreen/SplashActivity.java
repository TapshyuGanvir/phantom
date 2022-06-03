package com.sixsimplex.treasurehunt.revelocore.splashScreen;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.sixsimplex.treasurehunt.BuildConfig;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.revelocore.initialsetup.InitializationActivity;
import com.sixsimplex.treasurehunt.revelocore.login.view.LoginActivity;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.Phantom1.app.view.DeliveryMainActivity;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.ToastUtility;
import com.sixsimplex.treasurehunt.revelocore.util.Utilities;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {
    @BindView(R.id.apkdetails)
    TextView apkdetails;
    @BindView(R.id.reveloLogoId)
    ImageView revelo3Logo;
    @BindView(R.id.reveloAppName)
    TextView reveloAppName;
    @BindView(R.id.reveloTagline)
    TextView reveloTagLine;
    @BindView(R.id.reveloOrgName)
    TextView reveloOrgName;
    @BindView(R.id.updateAvailableTv)
    TextView updateAvailableTv;
    String className = "SplashActivity";
    // Declare the UpdateManager

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        ReveloLogger.initialize(this, UserInfoPreferenceUtility.getUserName());

        String apk = "";
        String fileName="";
        try{
            apk= Utilities.getApkName(SplashActivity.this);
        }catch (Exception e){
            e.printStackTrace();
        }
        ReveloLogger.info(className, "apkname", apk);

        apkdetails.setText(apk);
       // setAppUi();
        showSplash();



        updateAvailableTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                callFlexibleUpdate();
            }
        });



    }
    public void callFlexibleUpdate() {
        // Start a Flexible Update
        ReveloLogger.debug(className,"checkUpdates","Starting flexible updates");
    }

    public void callImmediateUpdate() {
        // Start a Immediate Update
        ReveloLogger.debug(className,"checkUpdates","Starting immediate updates");
    }

    private void setAppUi() {
        String fileName=UserInfoPreferenceUtility.getOrgName()+"AppLogo.png";
        try {
            File logo=new File(AppFolderStructure.orgLogoFolderPath(SplashActivity.this)+File.separator+fileName);
            if(logo.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(logo.getAbsolutePath());
                revelo3Logo.setImageBitmap(myBitmap);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!UserInfoPreferenceUtility.getAppName().equals("")){
            reveloAppName.setText(UserInfoPreferenceUtility.getAppName());

        } else {
            Typeface face = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                face = getResources().getFont(R.font.lobster_family);
            }
            if(face != null){
            reveloAppName.setTypeface(face);
            }
            reveloAppName.setText(getString(R.string.app_name));
        }

        reveloTagLine.setText(UserInfoPreferenceUtility.getTagLine());
        reveloOrgName.setText(UserInfoPreferenceUtility.getOrgLabel());

        //hardcoding

//        reveloOrgName.setText("Forest Department, Haryana");
//        reveloAppName.setText("GEO FOREST");
//        reveloTagLine.setText("Geo-tagging Forest Resources, Assets and Interventions.");

    }

    private void showSplash() {
        try {
            Thread background = new Thread() {
                public void run() {
                    try {

//                        JSONObject graphResult= CMUtils.getCMGraph(SplashActivity.this);
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                launchHomeScreen();
//                            }
//                        });
                        sleep(2000);
                        launchHomeScreen();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            background.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchHomeScreen() {
        ReveloLogger.debug(className, "launchHomeScreen", "Checking login state..");
        boolean isLoginUser = SecurityPreferenceUtility.getIsLoginUser();

        if (isLoginUser) {
            ReveloLogger.debug(className, "launchHomeScreen", "User has logged in before..Checking if he has dbs downloaded");
            boolean metaDbPresent = AppFolderStructure.isMetaDataGpPresent(SplashActivity.this);
            boolean reDbPresent = AppFolderStructure.isReGpPresent(SplashActivity.this);
            boolean dataDbPresent = AppFolderStructure.isDataGpPresent(SplashActivity.this);
            if (metaDbPresent && reDbPresent && dataDbPresent ) {
                ReveloLogger.debug(className, "launchHomeScreen", "User has logged in before..and has downloaded data..taking him to home view");
                callHomeUi();
            }
            else {
                ReveloLogger.debug(className, "launchHomeScreen", "User has logged in before..taking him to init view");
            Intent homeActivityIntent = new Intent(SplashActivity.this, InitializationActivity.class);
            homeActivityIntent.putExtra("callingActivity", "login");
            startActivity(homeActivityIntent);
            finish();
            }
        }
        else {
            ReveloLogger.debug(className, "launchHomeScreen", "User has not logged in before,this seems to be new user..taking him to login");
            Intent loginActivityIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(loginActivityIntent);
            finish();
        }
    }

    private void callHomeUi() {

       Intent homeActivityIntent = new Intent(SplashActivity.this, DeliveryMainActivity.class);

//        ReveloLogger.info(className, "callHomeUi", "setting reload flag true because going from splash");
//        homeActivityIntent.putExtra("reload", true);
//
//        homeActivityIntent.putExtra("callingActivity", "splash");
//        homeActivityIntent.putExtra("requestType", AppConstants.LOGIN_DATA_REQUEST);
//
//        homeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeActivityIntent);
        finish();
    }
}