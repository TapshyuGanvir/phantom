package com.sixsimplex.treasurehunt.revelocore.serverSetting.presenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.serverSetting.model.ServerSetting;
import com.sixsimplex.treasurehunt.revelocore.serverSetting.view.IServerSettingView;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.ProgressUtility;
import com.sixsimplex.treasurehunt.revelocore.util.ToastUtility;

public class ServerSettingPresenter implements IServerSettingPresenter {

    private IServerSettingView iServerSettingView;
    private Context context;

    public ServerSettingPresenter(IServerSettingView iServerSettingView) {
        this.iServerSettingView = iServerSettingView;
    }

    @Override
    public void onServerSetting(Context context, View view, String appServer, String securityServer, String realmName) {
        this.context = context;

        ServerSetting serverSettingModel = new ServerSetting(appServer, securityServer,realmName);
        String success = serverSettingModel.validateSecurityServer();

        if (success.equalsIgnoreCase(AppConstants.SUCCESS)) {

            if (NetworkUtility.checkNetworkConnectivity(context)) {
                checkSecurityServerNetworkCall(view, appServer, securityServer,realmName);
            } else {
                iServerSettingView.onErrorServerSetting(context.getResources().getString(R.string.permission_internet_connection), view);
            }
        } else {
            iServerSettingView.onErrorServerSetting(success, view);
        }
    }

    private void checkSecurityServerNetworkCall(final View view, final String appServer, final String securityServer,String realmName) {

        ProgressDialog progressDialog = ProgressUtility.showProgressDialog(context, "", context.getResources().getString(R.string.progress_message_login));

        try {

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, securityServer,
                    response -> {

                        ProgressUtility.dismissProgressDialog(progressDialog);
                        String msg = "Security Server is online!";

                        ToastUtility.toast(msg, context, false);
                        checkAppServerNetworkCall(view, appServer, securityServer,realmName);

                    },
                    error -> {
                        ProgressUtility.dismissProgressDialog(progressDialog);
                        String errorDescription = context.getResources().getString(R.string.error_security_server_offline);
                        iServerSettingView.onErrorServerSetting(errorDescription, view);
                    });

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(AppConstants.NETWORK_TIME_OUT_MS, 1, 1));
            queue.add(stringRequest);

        } catch (Exception e) {
            e.printStackTrace();
            ProgressUtility.dismissProgressDialog(progressDialog);
            iServerSettingView.onErrorServerSetting(context.getResources().getString(R.string.error_security_server_invalid), view);
        }
    }

    private void checkAppServerNetworkCall(final View view, final String appServer, final String securityServer, String realmName) {

        ProgressDialog progressDialog = ProgressUtility.showProgressDialog(context, "", context.getResources().getString(R.string.progress_message_login));

        try {

            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, appServer + "/reveloadmin/revelo",
                    response -> {
                        // Display the first 500 characters of the response string.
                        ProgressUtility.dismissProgressDialog(progressDialog);

                        iServerSettingView.onServerSetting(context.getResources().getString(R.string.success_security_server_online), view, false, appServer, securityServer,realmName);
                    },
                    error -> {

                        ProgressUtility.dismissProgressDialog(progressDialog);
                        String errorDescription = context.getResources().getString(R.string.error_app_server_offline);
                        iServerSettingView.onErrorServerSetting(errorDescription, view);
                    });
            queue.add(stringRequest);

        } catch (Exception e) {
            e.printStackTrace();
            ProgressUtility.dismissProgressDialog(progressDialog);
            iServerSettingView.onErrorServerSetting(context.getResources().getString(R.string.error_app_server_invalid), view);
        }
    }
}