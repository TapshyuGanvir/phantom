package com.sixsimplex.treasurehunt.Phantom1.chat.websocket;

import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClass {

    private static final int NORMAL_CLOSURE_STATUS = 1000;
    public boolean isCoonectionOpen = false;
    String inputMessage = "hi";
    WebSocket ws;
    Listener listener;
    WebSocketEventListener webSocketEventListener;
    String classname = "WebSocketClass";
    private Request request;
    private OkHttpClient client;

    public WebSocketClass() {

    }

    public WebSocketClass(String urlString, String inputMessage) {
        request = new Request.Builder().url(urlString)
                /*.addHeader("Authorization", "Bearer "+ Constants.getAccessToken())*/.build();
        listener = new Listener();
        client = new OkHttpClient();
        this.inputMessage = inputMessage;
        ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
        ReveloLogger.info(classname, "init", "initWebsocketClass connection init url " + urlString);
    }

    public WebSocketClass(String urlString, String inputMessage, WebSocketEventListener webSocketEventListener) {
        request = new Request.Builder().url(urlString)
                /*.addHeader("Authorization", "Bearer "+ Constants.getAccessToken())*/.build();
        this.listener = new Listener();
        this.webSocketEventListener = webSocketEventListener;
        client = new OkHttpClient();
        this.inputMessage = inputMessage;
        ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
        ReveloLogger.info(classname, "init", "initWebsocketClass connection init url " + urlString);
    }

    public String getInputMessage() {
        ReveloLogger.info(classname, "getInputMessage", "retruning inputmessage = " + inputMessage);
        return inputMessage;
    }

    public void sendMessage(String inputMessage, Context context) {
        this.inputMessage = inputMessage;
        try {
            ReveloLogger.info(classname, "getInputMessage", "sending inputmessage = " + inputMessage);
            ws.send(inputMessage);
            // JSONObject response = UploadFile.doPostToSendJson(UrlStore.getCreateMessageUrl(), inputMessage, context);
            //ReveloLogger.info(classname, "sendmessage", "response = " + response.toString());
           /* try {

                RequestQueue requestQueue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, UrlStore.getCreateMessageUrl(), response -> {

                    try {
                        ReveloLogger.info(classname, "sendmessage", "response = " + response);
                        if (! TextUtils.isEmpty(response)) {
                            JSONObject responseJson = new JSONObject(response);
                            if (responseJson.has(AppConstants.ERROR)) {
                                String errorDescription = responseJson.has(AppConstants.ERROR_DESCRIPTION) ? responseJson.getString(AppConstants.ERROR_DESCRIPTION) : "";
                                ReveloLogger.error(classname, "sendmessage", "error response = " + errorDescription);
                            }
                        }
                        else {
                            ReveloLogger.error(classname, "sendmessage", "error response = EMPTY");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    requestQueue.cancelAll("revelo3message");
                }, volleyError -> { //This code is executed if there is an error.

                    requestQueue.cancelAll("revelo3message");

                    String errorDescription = NetworkUtility.getErrorFromVolleyError(volleyError);
                    ReveloLogger.error(classname, "sendmessage", "error response = " + errorDescription);
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        String accessToken = SecurityPreferenceUtility.getAccessToken();
                    //   String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpVEZmLTl5Q0dDU0Z5bzB3Wk9ISXFMQ2FOQnQta0t4LUxBcG9nU2hLWEFzIn0.eyJleHAiOjE2NDgwMzY5ODEsImlhdCI6MTY0NTQ0NDk4MSwianRpIjoiYTIxZTk3ZTgtZjdlNC00MjExLThkMjgtMjJjYmM2MDBiMzgyIiwiaXNzIjoiaHR0cDovLzEzNy41OS41My42ODo5MDkwL2F1dGgvcmVhbG1zL3JldmVsbzMiLCJhdWQiOlsicmV2ZWxvIiwiYWNjb3VudCJdLCJzdWIiOiI3N2U5OTYzYS00MmQzLTRiOTktOWI5NS1hZDU1MTYyYzYxNzciLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJyZXZlbG9hZG1pbiIsInNlc3Npb25fc3RhdGUiOiJmMDc0OTFlMS1hY2NjLTQwYzMtYWJjYy04NGE0Y2QyNGE0NTQiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHA6Ly8xMzcuNTkuNTMuNjg6ODA3MCIsImh0dHA6Ly8xMzcuNTkuNTMuNjg6ODA4NSIsImh0dHA6Ly8xMzcuNTkuNTMuNjg6ODA2NSIsImh0dHA6Ly8xMzcuNTkuNTMuNjg6ODA4MCJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJvcmdhZG1pbiIsInVtYV9hdXRob3JpemF0aW9uIiwiZGVmYXVsdC1yb2xlcy1yZXZlbG8zIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicmV2ZWxvIjp7InJvbGVzIjpbIm9yZ2FkbWluIl19LCJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50Iiwidmlldy1hcHBsaWNhdGlvbnMiLCJ2aWV3LWNvbnNlbnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsImRlbGV0ZS1hY2NvdW50IiwibWFuYWdlLWNvbnNlbnQiLCJ2aWV3LXByb2ZpbGUiXX0sInJldmVsb2FkbWluIjp7InJvbGVzIjpbIm9yZ2FkbWluIl19fSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJPcmdBZG1pbiBVc2VyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoib3JnYWRtaW4iLCJnaXZlbl9uYW1lIjoiT3JnQWRtaW4iLCJmYW1pbHlfbmFtZSI6IlVzZXIiLCJlbWFpbCI6Im9yZ2FkbWluQGdtYWlsLmNvbSJ9.oRiuFEkH7NNXYJdx-ZuuGcD4QXysk84q_erXqN9jA3BjfnBz5f2d7bD6aqfZ4tCkTGOiJUhRvGyDbGfJ8FETuzMPM-WzmCVhvh7R-TLq3y2B9zUxnVzUQ4e8P7FjERHG19s6qkFHsqVNl6o4PXr77-aK3SiSYqpF1IkfuA_qf3J9oeyue7j1rg3u7ctjNFRfQxfKx85ph28hYHRPKKjQroHtYkF-fAL95sEa3EpXaV9Z86ZbwGNY9Gar7w8Bv9yeaqC93vi7Y8foTcSEJ3p0IVCJPEW6E1aHnsC3OtsiX5mUExhwrl07sWYU6TZO6wIV0zCTlkP-Nxx3S1qu8Ne24g";
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "Bearer " + accessToken);
                        return params;
                    }
                    @Override
                    public byte[] getBody() throws AuthFailureError {

                        try {
                            return inputMessage == null ? null : inputMessage.getBytes(StandardCharsets.UTF_8);
                        } catch (Exception uee) {
                            ReveloLogger.error(classname, "sendmessage", "error creating body = " + uee.toString());
                            return super.getBody();
                        }
                    }
                };

                stringRequest.setTag("revelo3message");
                requestQueue.add(stringRequest);

            } catch (Exception e) {
                e.printStackTrace();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        client.dispatcher().executorService().shutdown();
        ReveloLogger.info(classname, "close", "connection closed");
    }

    private class Listener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send(getInputMessage());
            isCoonectionOpen = true;
            webSocketEventListener.onSocketOpened(response);
            ReveloLogger.info(classname, "onopen - websocketlistner", "connection opened");
        }

        @Override
        public void onMessage(WebSocket webSocket, String str) {
            ReveloLogger.info(classname, "onMessage -string- websocketlistner", "connection on message " + str);
            webSocketEventListener.onMessageReceived(str);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            ReveloLogger.info(classname, "onMessage - bytes - websocketlistner", "connection on message bytes");
            webSocketEventListener.onMessageReceived(bytes);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            isCoonectionOpen = false;
            ReveloLogger.info(classname, "onClosing - bytes - websocketlistner", "connection on closing, reason- " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            isCoonectionOpen = false;
            ReveloLogger.error(classname, "onFailure - bytes - websocketlistner", "connection on failure " + t.getMessage());
        }
    }
}
