package com.sixsimplex.treasurehunt.revelocore.util;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class ToastUtility {

    public static void snackBarMessage(View v, String message, boolean isLong) {
        Snackbar snackbar;
        if (isLong) {
            snackbar = Snackbar.make(v, message, Snackbar.LENGTH_LONG);
        } else {
            snackbar = Snackbar.make(v, message, Snackbar.LENGTH_SHORT);
        }
        snackbar.show();
    }

    public static void toast(String message, Context context, boolean isLong) {

        int duration;
        if (isLong) {
            duration = Toast.LENGTH_LONG;
        } else {
            duration = Toast.LENGTH_SHORT;
        }

        Toast.makeText(context, message, duration).show();
    }
}
