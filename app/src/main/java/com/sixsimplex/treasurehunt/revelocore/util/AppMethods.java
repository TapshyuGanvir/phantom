package com.sixsimplex.treasurehunt.revelocore.util;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AlertDialog;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage.GeoPackageManagerAgent;
import com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil.JurisdictionTable;
import com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil.ReDbTable;
import com.sixsimplex.treasurehunt.revelocore.obConceptModel.OrgBoundaryConceptModel;
import com.sixsimplex.treasurehunt.revelocore.service.LocationReceiverService;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//import static androidx.constraintlayout.motion.widget.MotionScene.TAG;

public class AppMethods {

    private static final String className = "AppMethods";

    public static void closeKeyboard(View v, Activity activity) {
        try {
            if (activity != null) {
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    if (v != null) {
                        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "closeKeyboard", String.valueOf(e.getCause()));
        }
    }

    public static String capitaliseFirstLatter(String text) {
        try {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return text;
    }
    }

    public static String lowercaseFirstLatter(String text) {
        try {
        return text.substring(0, 1).toLowerCase() + text.substring(1).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return text;
        }
    }

    public static AlertDialog showAlertDialog(Activity activity, String message, String positiveBtnText, String negativeBtnText, PositiveBtnCallBack dialogPositiveBtnCallBack, NegativeBtnCallBack dialogNegativeBtnCallBack) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setMessage(message);

        if (! TextUtils.isEmpty(positiveBtnText)) {

            builder.setPositiveButton(positiveBtnText, (dialog, which) -> {
                if (dialogPositiveBtnCallBack != null) {
                    dialogPositiveBtnCallBack.positiveCallBack(dialog);
                }
            });
        }

        if (! TextUtils.isEmpty(negativeBtnText)) {

            builder.setNegativeButton(negativeBtnText, (dialog, which) -> {
                if (dialogNegativeBtnCallBack != null) {
                    dialogNegativeBtnCallBack.negativeCallBack(dialog);
                }
            });
        }

        return builder.show();
    }

    public static void showAlertDialog(Activity activity, String message, String positiveBtnText, String negativeBtnText, PositiveBtnCallBack dialogPositiveBtnCallBack, NegativeBtnCallBack dialogNegativeBtnCallBack, boolean cancelable, DismissDialogCallBack dismissDialogCallBack) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(cancelable);
        builder.setMessage(message);
        if (cancelable) {
            builder.setNeutralButtonIcon(activity.getResources().getDrawable(R.drawable.ic_cancel));
            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (dialogNegativeBtnCallBack != null) {
                    dismissDialogCallBack.dismissDialogCallBack(dialogInterface);
                }
                else {
                    dialogInterface.dismiss();
                }
            }
        });
        if (! TextUtils.isEmpty(positiveBtnText)) {

            builder.setPositiveButton(positiveBtnText, (dialog, which) -> {
                if (dialogPositiveBtnCallBack != null) {
                    dialogPositiveBtnCallBack.positiveCallBack(dialog);
                }
            });
        }

        if (! TextUtils.isEmpty(negativeBtnText)) {

            builder.setNegativeButton(negativeBtnText, (dialog, which) -> {
                if (dialogNegativeBtnCallBack != null) {
                    dialogNegativeBtnCallBack.negativeCallBack(dialog);
                }
            });
        }

        builder.show();
    }

    public static void vibrate(Context context, long milliseconds) {
        try {
        Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null) {// Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //    vib.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else {
                //  vib.vibrate(milliseconds);//deprecated in API 26
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getColor(String color) {

        int colorObj = - 1;
        try {
            if (color.startsWith("rgba")) {

                String[] colorArray = color.split("\\(");
                colorArray = colorArray[1].split(",");
                String redColor = colorArray[0];
                if (redColor != null) {
                    redColor = redColor.trim();
                }

                String greenColor = colorArray[1];
                if (greenColor != null) {
                    greenColor = greenColor.trim();
                }

                String blueColor = colorArray[2];
                if (blueColor != null) {
                    blueColor = blueColor.trim();
                }

                String alphaColor = colorArray[3].split("\\)")[0];
                if (alphaColor != null) {
                    alphaColor = alphaColor.trim();
                }

                int red = Integer.parseInt(redColor);
                int green = Integer.parseInt(greenColor);
                int blue = Integer.parseInt(blueColor);
                int alpha = (int) (Float.parseFloat(alphaColor) * 255);

                colorObj = Color.argb(alpha, red, green, blue);

            }
            else {
                colorObj = Color.parseColor(color);
            }

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "getColor", String.valueOf(e.getCause()));
            return Color.BLACK;
        }

        return colorObj;
    }

    public static String checkInvalidCharacter(String string, String alias) {
        String errorString = "";
        try {
        if (! TextUtils.isEmpty(string)) {
            if (string.matches("[_a-zA-Z0-9]+")) {
                if ((string.matches(".*[a-zA-Z0-9]+.*"))) {
                    errorString = "";
                }
                else {
                    errorString = "You can enter _ along with letter or number for " + alias + ".";
                }
            }
            else {
                errorString = "You can enter only letters, _ and numbers for " + alias + ".";
            }
        }
        else {
            if (TextUtils.isEmpty(alias)) {
                errorString = "Enter value.";
            }
            else {
                errorString = "Enter value for " + alias + ".";
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorString;
    }

    public static byte[] getByteFromFile(File file) {
        try {
        //you bitmap image first get
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, blob);
        return blob.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readFileToByteArray(File file) {
        FileInputStream fis = null;
        byte[] bArray = null;
        try {
            bArray = new byte[(int) file.length()];
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }
        return bArray;
    }

    public static File createFileFromByte(byte[] bytes, File file) {

        try {

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (Exception e) {
            Log.e(className, "createFileFromByte - " + e.getMessage());
            e.printStackTrace();
        }
        return file;
    }

    public static byte[] compressImage(File file) {

        FileInputStream fis = null;
        byte[] bArray = null;

        try {
            bArray = new byte[(int) file.length()];
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();
        } catch (IOException ioExp) {
            ioExp.printStackTrace();
        }

        try {
            if (bArray != null) {
        Bitmap originalImage = BitmapFactory.decodeByteArray(bArray, 0, bArray.length);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        originalImage.compress(Bitmap.CompressFormat.PNG, 50, stream);
        return stream.toByteArray();
    }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clearAllStaticData() {
        SecurityPreferenceUtility.isLoginUser(false);
        SecurityPreferenceUtility.setAccessToken("");
        SecurityPreferenceUtility.setRefreshToken("");

        UserInfoPreferenceUtility.storeUserName("");
        clearDatabaseRelatedStaticVariables();
        ///clearAllDatabaseRelatedStaticVariables();//clear all static data
    }

    public static void clearDatabaseRelatedStaticVariables(){
        OrgBoundaryConceptModel.clearObReGraph();
        GeoPackageManagerAgent.clearAllGeopackage();
        CMUtils.clearAttributeDao();
        CMUtils.clearCMVariables();
        JurisdictionTable.clearJurisdictions();
        ReDbTable.clearReGpFeatureDao();

    }

    public static void clearAllDatabaseRelatedStaticVariables() {
        //GeoPackageManagerAgent.clearAllGeopackage();
        clearDataDbRelatedStaticVariables();
        clearMetadataDbRelatedStaticVariables();
        clearRedbRelatedStaticVariables();
    }

    public static void clearDataDbRelatedStaticVariables(){
        GeoPackageManagerAgent.clearDataGeoPackage();
    }

    public static void clearMetadataDbRelatedStaticVariables() {
        GeoPackageManagerAgent.clearMetaGeoPackage();
        CMUtils.clearAttributeDao();
        CMUtils.clearCMVariables();
    }

    public static void clearRedbRelatedStaticVariables() {
        GeoPackageManagerAgent.clearRedbGeopackage();
        OrgBoundaryConceptModel.clearObReGraph();
        JurisdictionTable.clearJurisdictions();
        ReDbTable.clearReGpFeatureDao();
    }

    public static String getTrailId() {
        String userName = UserInfoPreferenceUtility.getUserName();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss", Locale.getDefault());
        String uniqueTimeStamp = sdf.format(new Date());
        return userName + "_" + uniqueTimeStamp;
    }

    public static void stopLocationService(String operationName,Activity activity) {
        try {
            if (! LocationReceiverService.isServiceRunning.equalsIgnoreCase(LocationReceiverService.STATE.LOCATION_SERIVCE_STATE_STOPPED)) {
                Intent intent = new Intent(activity, LocationReceiverService.class);
                intent.putExtra("operationName", operationName);
                intent.setAction(LocationReceiverService.ACTION.UPDATE_LOCATION_STOPFOREGROUND_ACTION);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    activity.startForegroundService(intent);
                }
                else {
                    activity.startService(intent);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
//    public static void stopWhereAmIService(Activity activity) {
//        try {
////            if(WUtils.isWhereIAmServiceRunning((ActivityManager) activity.getApplicationContext().getSystemService(ACTIVITY_SERVICE) )){
////                activity.stopService(new Intent(activity.getApplicationContext(), WhereAmIService.class));
////            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    public interface PositiveBtnCallBack {
        void positiveCallBack(DialogInterface dialog);
    }

    public interface NegativeBtnCallBack {
        void negativeCallBack(DialogInterface dialog);
    }

    public interface DismissDialogCallBack {
        void dismissDialogCallBack(DialogInterface dialog);
    }
}