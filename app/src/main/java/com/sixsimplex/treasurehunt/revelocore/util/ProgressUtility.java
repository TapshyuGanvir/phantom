package com.sixsimplex.treasurehunt.revelocore.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import java.util.List;

public class ProgressUtility {
    private static ProgressDialog appLevelProgressDialog = null;
    public static ProgressDialog showProgressDialog(Context context, String title, String message) {

        ProgressDialog progressDialog = new ProgressDialog(context);

        if (!TextUtils.isEmpty(title)) {
            progressDialog.setTitle(title);
        }

        if (!TextUtils.isEmpty(message)) {
            progressDialog.setMessage(message);
        }

        progressDialog.setCancelable(false);

        try {
            progressDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return progressDialog;
    }

    public static void changeProgressDialogMessage(ProgressDialog progressDialog, String title, String message) {

        if (progressDialog != null) {

            if (progressDialog.isShowing()) {

                if (!TextUtils.isEmpty(title)) {
                    progressDialog.setTitle(title);
                }

                if (!TextUtils.isEmpty(message)) {
                    progressDialog.setMessage(message);
                }
            }
        }
    }

    public static void dismissProgressDialog(ProgressDialog progressDialog) {
        try {
            if (progressDialog != null) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public static ProgressDialog showAppLevelProgressDialog(Context context, boolean isIndeterminate, String title, String message) {


        if(appLevelProgressDialog==null) {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(isIndeterminate);

            try {

                if (! isIndeterminate) {
                    if (! TextUtils.isEmpty(title)) {
                        progressDialog.setTitle(title);
                    }

                    if (! TextUtils.isEmpty(message)) {
                        progressDialog.setMessage(message);
                    }
                }

                progressDialog.show();
                appLevelProgressDialog = progressDialog;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            if(!appLevelProgressDialog.isShowing()){
                appLevelProgressDialog.show();
            }
            changeAppLevelProgressDialogMessage(isIndeterminate,title,message);
        }
        return appLevelProgressDialog;
    }
    public static void changeAppLevelProgressDialogMessage(boolean isIndeterminate,String title, String message) {

        if (appLevelProgressDialog != null) {

            if (appLevelProgressDialog.isShowing()) {

                if(isIndeterminate){
                    appLevelProgressDialog.setIndeterminate(true);
                }else {
                    if (! TextUtils.isEmpty(title)) {
                        appLevelProgressDialog.setTitle(title);
                    }

                    if (! TextUtils.isEmpty(message)) {
                        appLevelProgressDialog.setMessage(message);
                    }
                }
            }
        }
    }
    public static void dismissAppLevelProgressDialog() {
        try {
            if (appLevelProgressDialog != null) {
                if (appLevelProgressDialog.isShowing()) {
                    appLevelProgressDialog.dismiss();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void dismissBottomSheetDialogFragment(BottomSheetDialogFragment bottomSheetDialogFragment){
        if(bottomSheetDialogFragment!=null){
            if(bottomSheetDialogFragment.isVisible()){
                try {
                    bottomSheetDialogFragment.dismissAllowingStateLoss();//.dismiss();
                }catch (Exception e){
                    ReveloLogger.error("ProgressUtility", "dismissBottomSheetDialogFragment", "exception dismissing bottom sheet "+e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    public static void dismissBottomSheetDialogFragmentList(List<BottomSheetDialogFragment> bottomSheetDialogFragmentList){
        if(bottomSheetDialogFragmentList!=null && bottomSheetDialogFragmentList.size()>0){
            for(BottomSheetDialogFragment bottomSheetDialogFragment:bottomSheetDialogFragmentList){
                dismissBottomSheetDialogFragment(bottomSheetDialogFragment);
            }
        }
    }
}
