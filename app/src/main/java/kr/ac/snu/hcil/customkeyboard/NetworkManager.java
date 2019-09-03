package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.TimeUnit;

/**
 * Created by min90 on 01/08/2017.
 */

public class NetworkManager {
    private static final String TAG = NetworkManager.class.getName();
    private static final int MIN_NETWORK_BANDWIDTH_KBPS = 300;
    // Message to notify the network request timout handler that too much time has passed.
    private static final int MESSAGE_CONNECTIVITY_TIMEOUT = 1;
    // How long the app should wait trying to connect to a sufficient high-bandwidth network before
    // asking the user to add a new Wi-Fi network.
    private static final long NETWORK_CONNECTIVITY_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(30);

    public interface SendCallback {
        public void send();
    }

    private Context mContext = null;
    private static NetworkManager mInstance = null;
    private ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private Handler mHandler;

    private NetworkManager(Context context) {
        mContext = context;
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case  MESSAGE_CONNECTIVITY_TIMEOUT:
                        Log.d(TAG, "Network connection timeout");
                        unregisterNetworkCallback();
                        break;
                }
            }
        };
    }

    public static synchronized NetworkManager getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new NetworkManager(context);
        }

        return mInstance;
    }

    public boolean isNetworkHighBandwidth() {
        Network network = mConnectivityManager.getBoundNetworkForProcess();
        network = null == network ? mConnectivityManager.getActiveNetwork() : network;
        if (null == network) {
            Log.d(TAG, "there is no active now.");
            return false;
        }

        // requires android.permission.ACCESS_NETWORK_STATE
        int bandwidth = mConnectivityManager.getNetworkCapabilities(network).getLinkDownstreamBandwidthKbps();

        if (bandwidth > MIN_NETWORK_BANDWIDTH_KBPS) {
            return true;
        }

        Log.d(TAG, "Active network has low bandwidth: " + bandwidth);
        return false;
    }

    private void unregisterNetworkCallback() {
        if (mNetworkCallback != null) {
            Log.d(TAG, "Unregistering network callback");
            mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
            mNetworkCallback = null;
        }
    }

    public void sendWhenReady(final SendCallback sendCallback) {
        unregisterNetworkCallback();

        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                mHandler.removeMessages(MESSAGE_CONNECTIVITY_TIMEOUT);

                // requires android.permission.INTERNET
                if (!mConnectivityManager.bindProcessToNetwork(network)) {
                    Log.e(TAG, "ConnectivityManager.bindProcessToNetwork()"
                            + " requires android.permission.INTERNET");
                } else {
                    Log.d(TAG, "Network available");
                }
                sendCallback.send();
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                Log.d(TAG, "Network capabilities changed");
            }

            @Override
            public void onLost(Network network) {
                Log.d(TAG, "Network lost");
            }
        };

        requestHighBandwidthNetwork();
    }

    private void requestHighBandwidthNetwork() {

        Log.d(TAG, "Requesting high-bandwidth network");

        // Requesting an unmetered network may prevent you from connecting to the cellular
        // network on the user's watch or phone; however, unless you explicitly ask for permission
        // to a access the user's cellular network, you should request an unmetered network.
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
                .build();

        // requires android.permission.CHANGE_NETWORK_STATE
        mConnectivityManager.requestNetwork(request, mNetworkCallback);

        mHandler.sendMessageDelayed(
                mHandler.obtainMessage(MESSAGE_CONNECTIVITY_TIMEOUT),
                NETWORK_CONNECTIVITY_TIMEOUT_MS);
    }

    private void releaseHighBandwidthNetwork() {
        mConnectivityManager.bindProcessToNetwork(null);
        unregisterNetworkCallback();
    }

}
