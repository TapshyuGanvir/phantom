package com.sixsimplex.treasurehunt.Phantom1.chat.websocket;

import okhttp3.Response;
import okio.ByteString;

public interface WebSocketEventListener {
    void onSocketOpened(Response response);
    void onMessageReceived(String receivedMessage);
    void onMessageReceived(ByteString receivedBytes);

}
