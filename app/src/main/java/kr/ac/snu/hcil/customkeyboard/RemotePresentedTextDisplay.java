package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by min90 on 10/07/2017.
 */

public class RemotePresentedTextDisplay implements PresentedTextDisplay {
    private static final String TAG = RemotePresentedTextDisplay.class.getName();
    private NetworkManager mNetworkManager = null;
    private WebSocketClient mWebSocketClient = null;
    private String mPresentedText = "";

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://min90.koreasouth.cloudapp.azure.com:3000/wss");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i(TAG, "Opened");
                if (0 == mPresentedText.length()) {
                    mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                } else {
                    mWebSocketClient.send(mPresentedText);
                }
            }

            @Override
            public void onMessage(String message) {
                Log.i(TAG, "Message from server: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i(TAG, "Closed" + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.i(TAG, "Error " + ex.getMessage());
            }
        };
        try {
            mWebSocketClient.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    RemotePresentedTextDisplay(Context context) {
        mNetworkManager = NetworkManager.getInstance(context);
        if(mNetworkManager.isNetworkHighBandwidth()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    Log.i(TAG, "connectWebSocket():1");
                    connectWebSocket();
                    return null;
                }
            }.execute();
        } else {
            mNetworkManager.sendWhenReady(new NetworkManager.SendCallback() {
                @Override
                public void send() {
                    Log.i(TAG, "connectWebSocket():2");
                    connectWebSocket();
                }
            });
        }
    }

    @Override
    public void setText(final String presentedText) {
        mPresentedText = presentedText;
        if (mWebSocketClient != null && mWebSocketClient.isOpen()) {
            if(mNetworkManager.isNetworkHighBandwidth()) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        Log.i(TAG, "setText1: " + mPresentedText);
                        mWebSocketClient.send(mPresentedText);
                        return null;
                    }
                }.execute();
            } else {
                mNetworkManager.sendWhenReady(new NetworkManager.SendCallback() {
                    @Override
                    public void send() {
                        Log.i(TAG, "setText2: " + mPresentedText);
                        mWebSocketClient.send(mPresentedText);
                    }
                });
            }
        }
    }
}
