package com.sixsimplex.treasurehunt.revelocore.serverSetting.model;

import android.text.TextUtils;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.AppController;

public class ServerSetting implements IServerSetting {

    private String appServer;
    private String securityServer;
    private String securityRealmName;

    public ServerSetting(String appServer, String securityServer, String securityRealmName) {
        this.appServer = appServer;
        this.securityServer = securityServer;
        this.securityRealmName  =securityRealmName;
    }

    @Override
    public String getAppServer() {
        return appServer;
    }

    @Override
    public String getSecurityServer() {
        return securityServer;
    }

    public String getSecurityRealmName() {
        return securityRealmName;
    }

    public void setSecurityRealmName(String securityRealmName) {
        this.securityRealmName = securityRealmName;
    }

    @Override
    public String validateSecurityServer() {

        if (TextUtils.isEmpty(getAppServer()) && TextUtils.isEmpty(getSecurityServer()) && TextUtils.isEmpty(getSecurityRealmName())) {
            return AppController.getInstance().getApplicationContext().getResources().getString(R.string.error_server_setting);

        } else if (TextUtils.isEmpty(getAppServer())) {
            return AppController.getInstance().getApplicationContext().getResources().getString(R.string.error_app_server_setting);

        } else if (TextUtils.isEmpty(getSecurityServer())) {
            return AppController.getInstance().getApplicationContext().getResources().getString(R.string.error_app_security_server_setting);

        } else if (TextUtils.isEmpty(getSecurityRealmName())) {
            return AppController.getInstance().getApplicationContext().getResources().getString(R.string.error_app_security_realmname_setting);

        } else {
            return AppConstants.SUCCESS;
        }
    }
}
