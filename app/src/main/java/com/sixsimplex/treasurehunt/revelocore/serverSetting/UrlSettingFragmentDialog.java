package com.sixsimplex.treasurehunt.revelocore.serverSetting;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.serverSetting.presenter.IServerSettingPresenter;
import com.sixsimplex.treasurehunt.revelocore.serverSetting.presenter.ServerSettingPresenter;
import com.sixsimplex.treasurehunt.revelocore.serverSetting.view.IServerSettingView;
import com.sixsimplex.treasurehunt.revelocore.util.AppMethods;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.BottomSheetTagConstants;
import com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet.InfoBottomSheet;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UrlPreferenceUtility;


import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UrlSettingFragmentDialog extends DialogFragment implements IServerSettingView {

    private Activity parentActivity;

    private IServerSettingPresenter iServerSettingPresenter;

    @BindViews({R.id.app_url_et, R.id.security_url_et,R.id.security_realmname_et})
    List<TextInputEditText> textInputEditTexts;

    public static UrlSettingFragmentDialog newInstance(Activity activity) {
        UrlSettingFragmentDialog urlSettingFragmentDialog = new UrlSettingFragmentDialog();
        Bundle bundle = new Bundle();
        urlSettingFragmentDialog.setParentActivity(activity);
        urlSettingFragmentDialog.setArguments(bundle);
        return urlSettingFragmentDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(STYLE_NO_FRAME, R.style.NoActionBarTheme);

    }

    public void setParentActivity(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @OnClick({R.id.saveServerBtn, R.id.resetServerBtn, R.id.cancelIV})
    public void onClickEvent(View view) {

        switch (view.getId()) {
            case R.id.saveServerBtn:
                AppMethods.closeKeyboard(view, getActivity());
                iServerSettingPresenter.onServerSetting(getActivity(), view,
                        textInputEditTexts.get(0).getText().toString().trim(),
                        textInputEditTexts.get(1).getText().toString().trim(),
                        textInputEditTexts.get(2).getText().toString().trim());

                break;

            case R.id.resetServerBtn:
                AppMethods.closeKeyboard(view, getActivity());
                clearField();
                break;

            case R.id.cancelIV:
                dismiss();
                break;

            default:
                break;
        }
    }

    private void clearField() {
        textInputEditTexts.get(0).setText("");
        textInputEditTexts.get(1).setText("");
        textInputEditTexts.get(2).setText("");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_url_setting_dialog, container, false);
        ButterKnife.bind(this, view);

        iServerSettingPresenter = new ServerSettingPresenter(this);

        String mSecurityServer = UrlPreferenceUtility.getSecurityServerIP();
        String mAppServer = UrlPreferenceUtility.getAppServerIp();
        String mRealmName = UrlPreferenceUtility.getSecurityRealmName();

        if (!TextUtils.isEmpty(mAppServer)) {
            textInputEditTexts.get(0).setText(mAppServer);
        }
        if (!TextUtils.isEmpty(mSecurityServer)) {
            textInputEditTexts.get(1).setText(mSecurityServer);
        }
        if (!TextUtils.isEmpty(mRealmName)) {
            textInputEditTexts.get(2).setText(mRealmName);
        }

        return view;
    }

    @Override
    public void onServerSetting(String message, View view, boolean error, String appServer, String securityServer,String realmName) {
        dismiss();
        UrlPreferenceUtility.saveSecurityServerIP(securityServer);
        UrlPreferenceUtility.saveAppServerIp(appServer);
        UrlPreferenceUtility.saveSecurityRealmName(realmName);
    }

    @Override
    public void onErrorServerSetting(String message, View view) {
        InfoBottomSheet infoBottomSheet = InfoBottomSheet.geInstance(getActivity(), "Ok", "", "", message, 0,0,"");
        infoBottomSheet.setCancelable(false);
        assert getFragmentManager() != null;
        infoBottomSheet.show(getFragmentManager(), BottomSheetTagConstants.loginInfo);
    }
}