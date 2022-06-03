package com.sixsimplex.treasurehunt.revelocore.login.presenter;

import android.content.Context;
import android.view.View;

public interface ILoginPresenter {
    void proceedForLogin(Context context, View view, String username, String password, String clientId, String grantType);
}
