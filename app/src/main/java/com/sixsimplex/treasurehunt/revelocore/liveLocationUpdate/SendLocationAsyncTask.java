package com.sixsimplex.treasurehunt.revelocore.liveLocationUpdate;

import android.content.Context;
import android.os.AsyncTask;

import com.sixsimplex.treasurehunt.revelocore.upload.UploadFile;
import com.sixsimplex.treasurehunt.revelocore.util.UrlStore;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONObject;

public class SendLocationAsyncTask extends AsyncTask<String,Void,Void> {
    private Context context;
    public SendLocationAsyncTask(Context context){
        this.context=context;
    }
    @Override
    protected Void doInBackground(String... strings) {
        try {
            String dataToUpload = strings[0];
            String url = UrlStore.getUpdateProfileUrl();
            JSONObject resultJson = UploadFile.doPostToSendJson(url,dataToUpload,context);
            String resultJsoneResult = resultJson.getString("status");
            String resultJsonMessage = resultJson.getString("message");
            /*createDataResponseResult = {"status":"Success","message":"{\"dbFileName\":\"revelodatagdb_2021_01_20_12_32_37.sqlite\",\"dbFileSize\":\"5697536\"}\n"}*/
            if (resultJsoneResult.equalsIgnoreCase("success")) {
                ReveloLogger.debug("WEBSOCKET","update user profile","successful");
            }else {
                if(resultJsonMessage!=null && !resultJsonMessage.isEmpty()) {
                    ReveloLogger.debug("WEBSOCKET", "update user profile", "failed -"+resultJsonMessage);
                }else {
                    ReveloLogger.debug("WEBSOCKET", "update user profile", "failed - reason unavailable");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);

    }
}
