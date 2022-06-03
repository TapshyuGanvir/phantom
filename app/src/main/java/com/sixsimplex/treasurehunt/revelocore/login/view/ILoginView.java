package com.sixsimplex.treasurehunt.revelocore.login.view;

import android.app.ProgressDialog;
import android.view.View;

public interface ILoginView {
    void onResultView(String message, View view, String username,
                      boolean error, String accessToken, String refreshToken, ProgressDialog progressDialog);

    void onErrorLogin(String message, View view, ProgressDialog progressDialog);
}