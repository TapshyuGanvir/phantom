package com.sixsimplex.treasurehunt.revelocore.login.model;

import android.content.Context;
import android.text.TextUtils;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;

public class User implements IUser {

    private String username;
    private String password;
    private Context context;

    public User(String userName, String password, Context context) {
        this.username = userName;
        this.password = password;
        this.context = context;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String validateUserData() {
        if (TextUtils.isEmpty(getUsername()) && (TextUtils.isEmpty(getPassword()))) {
            return context.getResources().getString(R.string.error_username_password);
        } else if (TextUtils.isEmpty(getUsername())) {
            return context.getResources().getString(R.string.error_username);
        } else if (TextUtils.isEmpty(getPassword())) {
            return context.getApplicationContext().getResources().getString(R.string.error_password);
        } else {
            return AppConstants.SUCCESS;
        }
    }
}