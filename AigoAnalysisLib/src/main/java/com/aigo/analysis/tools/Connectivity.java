package com.aigo.analysis.tools;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/5/18 11:04
 */
public class Connectivity {
    private final ConnectivityManager mConnectivityManager;

    public Connectivity(Context context) {
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @SuppressWarnings("deprecation")
    public boolean isConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(mConnectivityManager.getActiveNetwork());
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            NetworkInfo network = mConnectivityManager.getActiveNetworkInfo();
            return network != null && network.isConnected();
        }
    }

    public enum Type {
        /**
         * 无网络，蜂窝网，WIFI
         */
        NONE,
        MOBILE,
        WIFI
    }

    @SuppressWarnings("deprecation")
    public Type getType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(mConnectivityManager.getActiveNetwork());
            if (networkCapabilities == null) {
                return Type.NONE;
            }
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return Type.WIFI;
            } else {
                return Type.MOBILE;
            }
        } else {
            NetworkInfo network = mConnectivityManager.getActiveNetworkInfo();
            if (network == null || !network.isConnected()) {
                return Type.NONE;
            }
            if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                return Type.WIFI;
            } else {
                return Type.MOBILE;
            }
        }
    }
}
