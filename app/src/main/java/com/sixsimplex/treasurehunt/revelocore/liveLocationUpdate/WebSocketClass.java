package com.sixsimplex.treasurehunt.revelocore.liveLocationUpdate;

import android.util.Log;

import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.SecurityPreferenceUtility;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClass {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    String inputMessage = "hi";
    private Request request;
    private OkHttpClient client;
    WebSocket ws;
    Listener listener;
    public  boolean isCoonectionOpen = false;
    public WebSocketClass() {

    }
    public WebSocketClass (String urlString,String inputMessage) {
        request = new Request.Builder()
                .url(urlString)
                .addHeader("Authorization", "Bearer "+ SecurityPreferenceUtility.getAccessToken())
                .build();
        listener = new Listener();
        client = new OkHttpClient();
        this.inputMessage=inputMessage;
        ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
        ReveloLogger.debug("WEBSOCKET","initWebsocketClass","connection init url "+urlString);
    }

    public String getInputMessage() {
        return inputMessage;
    }

    public void setInputMessage(String inputMessage) {
        this.inputMessage = inputMessage;
    }

    private class Listener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send(getInputMessage());
            isCoonectionOpen=true;
            Log.i("WEBSOCKET","connection opened");
        }
        @Override
        public void onMessage(WebSocket webSocket, String str) {
            // output("Receiving : " + str);
            Log.i("WEBSOCKET","connection on message "+str);
        }
        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            // output("Receiving bytes : " + bytes.hex());
            Log.i("WEBSOCKET","connection on message bytes");
        }
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            isCoonectionOpen=false;
            //output("Closing : " + code + " / " + reason);
            Log.i("WEBSOCKET","connection on closing, reason- "+reason);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            //output("Error : " + t.getMessage());
            isCoonectionOpen=false;
            Log.i("WEBSOCKET","connection on failure "+t.getMessage());
        }

    }

    public void close() {
        client.dispatcher().executorService().shutdown();
        Log.i("WEBSOCKET","connection closed");
    }
}
