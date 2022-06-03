package com.sixsimplex.treasurehunt.revelocore.util.log;

import android.os.AsyncTask;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class InsertLogInFile extends AsyncTask<JSONObject, Void, Void> {

    @Override
    protected Void doInBackground(JSONObject... jsonObjects) {

        try {

            JSONObject object = jsonObjects[0];
            File logFile = new File(object.getString("logFilePath"));

            JSONArray logJsonArray;

            String oldString = FileUtils.readFileToString(logFile);
            if (oldString.equalsIgnoreCase("")) {
                logJsonArray = new JSONArray();
            } else {
                try {
                    logJsonArray = new JSONArray(oldString);
                } catch (JSONException e) {
                    e.printStackTrace();
                    logJsonArray = new JSONArray();
                }
            }

            JSONObject logJsonObject = object.getJSONObject("logJsonObject");
            logJsonArray.put(logJsonObject);

            //insert logJsonArray to logFile
            FileUtils.writeStringToFile(logFile, logJsonArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
