package com.kingsman.hp.ap;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by Haedam on 2017-03-10.
 */

public class WifiApManager {
    private static final String TAG = WifiApManager.class.getName();

    public static final int WIAD =1;

    public static final int	WIFI_AP_STATE_DISABLED	= 1;
    public static final int	WIFI_AP_STATE_DISABLING	= 0;
    public static final int	WIFI_AP_STATE_ENABLED	= 3;
    public static final int	WIFI_AP_STATE_ENABLING	= 2;
    public static final int	WIFI_AP_STATE_FAILED	= 4;

    private final WifiManager mWifiManager;

    public WifiApManager(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean setWifiApEnabled(WifiConfiguration config, boolean enabled) {
        try {
            if (enabled) {
                mWifiManager.setWifiEnabled(false);
            }
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            return (Boolean) method.invoke(mWifiManager, config, enabled);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }

    public int getWifiApState() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");
            return (Integer) method.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return WIFI_AP_STATE_FAILED;
        }
    }

    public boolean setWifiApConfiguration(WifiConfiguration config) {
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            return (Boolean) method.invoke(mWifiManager, config);
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return false;
        }
    }
}
