package com.sixsimplex.treasurehunt.revelocore.serverSetting.presenter;

import android.content.Context;
import android.view.View;

public interface IServerSettingPresenter {
    void onServerSetting(Context context, View view, String appServer, String securityServer, String realmName);
}