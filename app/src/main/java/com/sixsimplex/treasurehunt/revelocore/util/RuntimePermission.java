package com.sixsimplex.treasurehunt.revelocore.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class RuntimePermission {

    private static String[] storagePermissionArray = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static String[] locationPermissionArray = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private static String[] cameraPermissionArray = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    private static String[] fingerprintPermissionArray = new String[]{};

    public static void checkPermissions(Activity activity) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {

            String[] allPermissionArray = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.INTERNET
                    , Manifest.permission.ACCESS_WIFI_STATE
                    , Manifest.permission.CHANGE_WIFI_STATE
                    , Manifest.permission.ACCESS_NETWORK_STATE
                    , Manifest.permission.CHANGE_NETWORK_STATE
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.CAMERA
                    , Manifest.permission.RECORD_AUDIO};

            requestPermissions(activity, allPermissionArray, true);
        }
    }

    public static boolean storagePermission(Activity activity, boolean askPermission) {
        boolean permission = requestPermissions(activity, storagePermissionArray, askPermission);
        if (askPermission) {
            if (!permission) {
//                AppMethods.toast(activity.getResources().getString(R.string.storage_permission), activity,false);
            }
        }
        return permission;
    }

    public static boolean locationPermission(Activity activity, boolean askPermission) {
        boolean permission = requestPermissions(activity, locationPermissionArray, askPermission);
        if (askPermission) {
            if (!permission) {
//                AppMethods.toast(activity.getResources().getString(R.string.location_permission), activity,false);
            }
        }
        return permission;
    }

    public static boolean cameraPermission(Activity activity, boolean askPermission) {
        boolean permission = requestPermissions(activity, cameraPermissionArray, askPermission);
        if (askPermission) {
            if (!permission) {
//                AppMethods.toast(activity.getResources().getString(R.string.camera_permission), activity,false);
            }
        }
        return permission;
    }

    private static boolean requestPermissions(Activity activity, String[] permissionArray, boolean askPermission) {

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {

            for (String permission : permissionArray) {
                int result = ContextCompat.checkSelfPermission(activity, permission);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }

            if (askPermission) {
                if (!listPermissionsNeeded.isEmpty()) {
                    ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[0]), 1);
                }
            }
        }

        return listPermissionsNeeded.isEmpty();//if empty permission granted else false
    }
}
