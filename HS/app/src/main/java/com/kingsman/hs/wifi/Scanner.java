package com.kingsman.hs.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.kingsman.hs.R;

/**
 * Created by HaeDam on 2017-03-05.
 */

public class Scanner extends Handler {

    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
    private int mRetry = 0;
    private final WifiManager mWifiManager;
    private Context mContext = null;

    public Scanner(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public void resume() {
        if (!hasMessages(0)) {
            sendEmptyMessage(0);
        }
    }

    public void forceScan() {
        removeMessages(0);
        sendEmptyMessage(0);
    }

    public void pause() {
        mRetry = 0;
        removeMessages(0);
    }


    @Override
    public void handleMessage(Message message) {
        if (mWifiManager.startScan()) {
            mRetry = 0;
        } else if (++mRetry >= 3) {
            mRetry = 0;
            Toast.makeText(mContext, R.string.wifi_fail_to_scan, Toast.LENGTH_LONG).show();
            return;
        }
        sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
    }
}
