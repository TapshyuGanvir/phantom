package com.sixsimplex.treasurehunt.revelocore.login.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sixsimplex.treasurehunt.revelocore.initialsetup.InitializationActivity;
import com.sixsimplex.treasurehunt.revelocore.login.presenter.ILoginPresenter;
import com.sixsimplex.treasurehunt.revelocore.login.presenter.LoginPresenter;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.serverSetting.UrlSettingFragmentDialog;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.ProgressUtility;
import com.sixsimplex.treasurehunt.revelocore.util.RuntimePermission;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.BottomSheetTagConstants;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.InfoBottomSheet;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.InfoBottomSheetInterface;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import java.io.File;
import java.util.List;
import java.util.Objects;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements ILoginView, InfoBottomSheetInterface {

    @BindViews({R.id.usernameET, R.id.passwordET})
    List<EditText> textInputEditTexts;


    TextView reveloAppName;
    TextView reveloTagLine;
    ImageView reveloLogoinlogin;

    private ILoginPresenter iLoginPresenter;
    private int tapCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        reveloAppName=findViewById(R.id.reveloTitleTV);
        reveloTagLine=findViewById(R.id.tagLineTV);
        reveloLogoinlogin=findViewById(R.id.reveloLogoinlogin);
        setAppUi();

        ///AppMethods.clearAllDatabaseRelatedStaticVariables();//on login page created
        AppMethods.clearDatabaseRelatedStaticVariables();

        iLoginPresenter = new LoginPresenter(this);
        RuntimePermission.checkPermissions(this);
    }

    private void setAppUi() {
        String fileName=UserInfoPreferenceUtility.getOrgName()+"AppLogo.png";
        try {
            File logo=new File(AppFolderStructure.orgLogoFolderPath(LoginActivity.this)+File.separator+fileName);
            if(logo.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(logo.getAbsolutePath());
                reveloLogoinlogin.setImageBitmap(myBitmap);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!UserInfoPreferenceUtility.getAppName().equals("")){
            reveloAppName.setText(UserInfoPreferenceUtility.getAppName());

        }else{
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
    }

    @OnClick({R.id.signInBtn, R.id.resetBtn, R.id.reveloTitleTV})
    public void onClickEvents(View view) {
        switch (view.getId()) {
            case R.id.signInBtn:
                if (RuntimePermission.storagePermission(this, true)
                        && RuntimePermission.locationPermission(this, true)) {

                    String userName = Objects.requireNonNull(textInputEditTexts.get(0).getText()).toString().trim();
                    String password = Objects.requireNonNull(textInputEditTexts.get(1).getText()).toString().trim();

                    String clientId = AppConstants.CLIENT_ID;
                    String grantType = AppConstants.GRANT_TYPE;

                    iLoginPresenter.proceedForLogin(this, view, userName, password, clientId, grantType);
                }

                break;

            case R.id.resetBtn:
                AppMethods.closeKeyboard(view, this);
                clearFields();
                break;

            case R.id.reveloTitleTV:
                tapCount++;
                if (tapCount == 5) {
                    openServerSettingFragmentDialog();
                    tapCount = 0;
                }
                break;

            default:
                break;
        }
    }

    private void openServerSettingFragmentDialog() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        String fragmentTag = "ServerSettingDialogFragment";
        Fragment previousIncarnation = fragmentManager.findFragmentByTag(fragmentTag);

        if (previousIncarnation != null) {
            fragmentTransaction.remove(previousIncarnation);
        }

        UrlSettingFragmentDialog urlSettingFragmentDialog = UrlSettingFragmentDialog.newInstance(LoginActivity.this);
        urlSettingFragmentDialog.show(fragmentTransaction, "SecurityDialog");
    }

    @Override
    public void onResultView(String message, View view, String username, boolean error, String accessToken, String refreshToken, ProgressDialog progressDialog) {

        ProgressUtility.dismissProgressDialog(progressDialog);

        SecurityPreferenceUtility.setAccessToken(accessToken);
        SecurityPreferenceUtility.setRefreshToken(refreshToken);
        SecurityPreferenceUtility.isLoginUser(true);

        if(UserInfoPreferenceUtility.getUserName().equalsIgnoreCase("")
                || UserInfoPreferenceUtility.getUserName().equalsIgnoreCase(username)) {
            UserInfoPreferenceUtility.storeUserName(username);
        }
        else if(!UserInfoPreferenceUtility.getUserName().equalsIgnoreCase(username)){
            UserInfoPreferenceUtility.resetAllVariables(true,false);
            AppFolderStructure.resetAllVariables();
            UserInfoPreferenceUtility.storeUserName(username);
        }

        /*Intent homeActivityIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeActivityIntent.putExtra("callingActivity","login");
        homeActivityIntent.putExtra("requestType", AppConstants.LOGIN_DATA_REQUEST);
        startActivity(homeActivityIntent);
        finish();*/

        //Setup
        Intent homeActivityIntent = new Intent(LoginActivity.this, InitializationActivity.class);
        homeActivityIntent.putExtra("callingActivity","login");
        homeActivityIntent.putExtra("requestType", AppConstants.LOGIN_DATA_REQUEST);
        homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeActivityIntent);
        finish();
    }

    @Override
    public void onErrorLogin(String message, View view, ProgressDialog progressDialog) {

        ProgressUtility.dismissProgressDialog(progressDialog);

        InfoBottomSheet infoBottomSheet = InfoBottomSheet.geInstance(this, "Ok", "", "", message, 0, 0,"");
        infoBottomSheet.setCancelable(false);
        infoBottomSheet.show(getSupportFragmentManager(), BottomSheetTagConstants.loginInfo);
    }

    private void clearFields() {
        textInputEditTexts.get(0).setText("");
        textInputEditTexts.get(1).setText("");
    }

    @Override
    public void onOkInfoBottomSheetResult(int requestCode, int errorCode, String jurisdiction) {
    }

    @Override
    public void onCancelOkBottomSheetResult(int requestCode, int errorCode) {
    }
}