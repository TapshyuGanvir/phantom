package com.sixsimplex.treasurehunt.revelocore.login.presenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sixsimplex.treasurehunt.revelocore.login.model.User;
import com.sixsimplex.treasurehunt.revelocore.login.view.ILoginView;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;
import com.sixsimplex.treasurehunt.revelocore.util.NetworkUtility;
import com.sixsimplex.treasurehunt.revelocore.util.ProgressUtility;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginPresenter implements ILoginPresenter {

    private ILoginView iLoginView;
    private Context context;

    public LoginPresenter(ILoginView iLoginView) {
        this.iLoginView = iLoginView;
    }

    @Override
    public void proceedForLogin(Context context, View view, String username, String password, String clientId, String grantType) {
        this.context = context;
        User user = new User(username, password, context);
        String resultMessage = user.validateUserData();

        if (resultMessage.equalsIgnoreCase(AppConstants.SUCCESS)) {
            if (NetworkUtility.checkNetworkConnectivity(context)) {
                loginNetworkCall(view, username, password, clientId, grantType);
            } else {
                String errorMsg = context.getResources().getString(R.string.permission_internet_connection);
                iLoginView.onErrorLogin(errorMsg, view, null);
            }
        } else {
            iLoginView.onErrorLogin(resultMessage, view, null);
        }
    }

    private void loginNetworkCall(final View view, final String username, final String password, String clientId, String grantType) {

        String url = UrlStore.securityTokenUrl();
        if (url.trim().length() == 0) {
            iLoginView.onErrorLogin(context.getResources().getString(R.string.error_security_server), view, null);
        } else {

            ProgressDialog progressDialog = ProgressUtility.showProgressDialog(context, context.getResources().getString(R.string.progress_title_login), context.getResources().getString(R.string.progress_message_login));

            try {

                RequestQueue requestQueue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {

                    try {

                        if (!TextUtils.isEmpty(response)) {

                            JSONObject responseJson = new JSONObject(response);

                            if (responseJson.has(AppConstants.ERROR)) {
                                String errorDescription = responseJson.has(AppConstants.ERROR_DESCRIPTION) ?
                                        responseJson.getString(AppConstants.ERROR_DESCRIPTION) : "";
                                iLoginView.onErrorLogin(errorDescription, view, progressDialog);
                            } else {

                                String accessToken = responseJson.has(SecurityPreferenceUtility.ACCESS_TOKEN) ?
                                        responseJson.getString(SecurityPreferenceUtility.ACCESS_TOKEN) : "";

                                String refreshToken = responseJson.has(SecurityPreferenceUtility.REFRESH_TOKEN) ?
                                        responseJson.getString(SecurityPreferenceUtility.REFRESH_TOKEN) : "";

                                if (!TextUtils.isEmpty(accessToken)) {
                                    iLoginView.onResultView("", view, username, false, accessToken, refreshToken, progressDialog);
                                } else {
                                    String errorDescription = "No Access Token Found";
                                    iLoginView.onErrorLogin(errorDescription, view, progressDialog);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        iLoginView.onErrorLogin(e.getMessage(), view, progressDialog);
                    }
                    requestQueue.cancelAll("revelo3login");
                }, volleyError -> { //This code is executed if there is an error.

                    requestQueue.cancelAll("revelo3login");

                    String errorDescription = NetworkUtility.getErrorFromVolleyError(volleyError);
                    iLoginView.onErrorLogin(errorDescription, view, progressDialog);

                }) {
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("username", username.trim());
                        params.put("password", password.trim());
                        params.put("client_id", clientId.trim());
                        params.put("grant_type", grantType.trim());
                        return params;
                    }
                };

                stringRequest.setTag("revelo3login");
                requestQueue.add(stringRequest);

            } catch (Exception e) {
                e.printStackTrace();
                iLoginView.onErrorLogin(e.toString(), view, progressDialog);
            }
        }
    }
}