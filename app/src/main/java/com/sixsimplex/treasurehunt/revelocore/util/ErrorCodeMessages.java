package com.sixsimplex.treasurehunt.revelocore.util;

import com.sixsimplex.treasurehunt.R;

public class ErrorCodeMessages {

    public static String getErrorMessage(int code) {
        String message;
        switch (code) {
            case 400:
                message = AppController.getInstance().getResources().getString(R.string.error_401_message);
            case 401:
                message = AppController.getInstance().getResources().getString(R.string.error_401_message);
                break;
            case 404:
                message =  AppController.getInstance().getResources().getString(R.string.error_404_message);
                break;
            case 403:
                message =  AppController.getInstance().getResources().getString(R.string.error_403_message);
                break;
            case 500:
                message =  AppController.getInstance().getResources().getString(R.string.error_500_message);
                break;
            default:
                message =  AppController.getInstance().getResources().getString(R.string.error_default_server_message);
                break;
        }
        return message;
    }


}
