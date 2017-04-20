package com.kingsman.hp.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;

import com.kingsman.hp.ProviderFragment;
import com.kingsman.hp.utils.SharePreferenceSettings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by T420_Note2 on 2017-04-09.
 */

public class DataCheckingService extends Service {
    public class DataServiceBinder extends Binder {
        public DataCheckingService getService(){
            return DataCheckingService.this;
        }
    }

    public interface ICallback{
        void updateCurUsage(long arg1);
        int getTermType();
        int getLimitValue();
        int getOldOfferedValue();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    public static final String INTENT_HPSHARE_DATA_USAGE = "com.kingsman.hp.service.DATA_USAGE";

    private final long BYTES = 1024L;
    private final int HANDLER_MESSAGE_CHECKING = 1;
    private final IBinder mBinder = new DataServiceBinder();

    private ICallback mCallback;
    private HashMap<String, Long> mAppDataUsages;
    private Handler mHandler;
    private SharePreferenceSettings mPref;

    private int mTermType = 0;
    private long mLimit_Value = 0L;
    private long mStartUsageValue = 0L;

    private long mTestAppDataUsageValue = 0L;
    private long mTestHotSpotDataUsageValue = 0L;
    private long mOldUsageValue = -1L;

    private long mCircular_time = 10000L;
    private long mDefault_limit_value = 5000000L;
    private long mCurValue;
    private long mCurTotalValue;
    private long mTotalOfferedValue;

    private boolean isRunning = false;

    public void registerCallback(ICallback cb){
        mCallback = cb;
    }

    public void setTermType(int value){
        mTermType = value;
    }
    public void setLimitValue(long value){
        mLimit_Value = value;
    }
    public long getCurTotalValue(){
        return mCurTotalValue;
    }
    public long getTotalOfferedValue(){
        return mTotalOfferedValue;
    }
    public void stopChcecking(){
        isRunning = false;
        if(mHandler != null) {
            mHandler.removeMessages(HANDLER_MESSAGE_CHECKING);
            mHandler = null;
        }
        if(this.mAppDataUsages != null) {
            this.mAppDataUsages.clear();
            this.mAppDataUsages = null;
        }
        saveInfo();
    }

    private HashMap<String, Long> initAppDataUsages(){
        HashMap<String, Long> hash = new HashMap<>();
        PackageManager pm  = this.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Iterator<ApplicationInfo> appIter = packages.iterator();
        while(appIter.hasNext()) {
            ApplicationInfo appInfo = appIter.next();
            long rxValue = TrafficStats.getUidRxBytes(appInfo.uid);
            long txValue = TrafficStats.getUidTxBytes(appInfo.uid);
            hash.put(appInfo.packageName, rxValue + txValue);
            //Log.d("aaa", appInfo.packageName + "(" + packageName + ") : rx=" + rxValue + ", tx=" + txValue);
        }
        mStartUsageValue = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
        return hash;
    }

    private long testDataUsageChecking(){
        PackageManager pm  = this.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Iterator<ApplicationInfo> appIter = packages.iterator();

        long appDataUsageValue = 0L;
        if(mOldUsageValue < 0L)
            mOldUsageValue = mStartUsageValue;

        while(appIter.hasNext()) {
            ApplicationInfo appInfo = appIter.next();
            long newValue = TrafficStats.getUidRxBytes(appInfo.uid) + TrafficStats.getUidTxBytes(appInfo.uid);
            long oldValue = this.mAppDataUsages.get(appInfo.packageName);

            if(newValue > oldValue){
                appDataUsageValue += (newValue - oldValue);
                this.mAppDataUsages.put(appInfo.packageName, newValue);
            }
        }
        long hotSpotDataUsageValue = 0L;
        long curUsageValue = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
        if(curUsageValue > mOldUsageValue && appDataUsageValue < curUsageValue - mOldUsageValue){
            hotSpotDataUsageValue = (curUsageValue - mOldUsageValue - appDataUsageValue);
        }
        mOldUsageValue = curUsageValue;
        mTestAppDataUsageValue += appDataUsageValue;
        mTestHotSpotDataUsageValue += hotSpotDataUsageValue;

        Log.d("aa", "Total AppDataUsageValue : " + Formatter.formatFileSize(this, mTestAppDataUsageValue));
        Log.d("aa", "Total UsageValue : " + Formatter.formatFileSize(this, curUsageValue - mStartUsageValue));
        Log.d("aa", "Total HotSpotDataUsageValue : " + Formatter.formatFileSize(this, mTestHotSpotDataUsageValue));

        return hotSpotDataUsageValue;
    }

    @Override
    public void onCreate() {
        //initInfo();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
//                if(!isRunning){
//                    //stopSelf();
//                    return;
//                }
                if(msg.what == HANDLER_MESSAGE_CHECKING){
                    mCurValue = testDataUsageChecking();
                    if(mCurValue <= 0L)
                        return;
                    mCurTotalValue += mCurValue;
                    mTotalOfferedValue += mCurValue;
                    //mCallback.updateCurUsage(mCurValue); change to send broadcast or do it on activity.
                    sendBroadcast(new Intent(INTENT_HPSHARE_DATA_USAGE));

                    if(mLimit_Value == mDefault_limit_value){
                        //mLimitMB_Value = mCallback.getLimitValue() * BYTES * BYTES;
                        mLimit_Value = mPref.getLong(ProviderFragment.SP_AOMUNT_limitData, mDefault_limit_value);
                    }
                }
                super.handleMessage(msg);
            }
        };
        initInfo();
        //Log.d("aaa", "DataService onCreate");
        super.onCreate();
    }

    private void initInfo(){
        mPref = SharePreferenceSettings.getInstance();
        mTestAppDataUsageValue = 0L;
        mTestHotSpotDataUsageValue = 0L;
        mOldUsageValue = -1L;
        this.mAppDataUsages = initAppDataUsages();

        // need to change - get info from server
        mCurValue = 0;
        //mCurTotalValue = mCallback.getOldOfferedValue();
        mCurTotalValue = mPref.getLong(ProviderFragment.SP_AOMUNT_offeredData, 0L);
        mTotalOfferedValue = mPref.getLong(ProviderFragment.SP_AOMUNT_offeredTotalData, 0L);
        Log.d("service", "mCurTotalValue : " + mCurTotalValue);
        Log.d("service", "mTotalOfferedValue : " + mTotalOfferedValue);

        //mLimitMB_Value = mCallback.getLimitValue() * BYTES * BYTES;
        //mLimitMB_Value = mDefault_limit_value;
        mLimit_Value = mPref.getLong(ProviderFragment.SP_AOMUNT_limitData, mDefault_limit_value);
        Log.d("service", "mLimit_Value : " + mLimit_Value);
    }

    private void startService(){
        isRunning = true;
        checkingDataUsage();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1,new Notification());
        startService();
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkingDataUsage(){
        if(this.mAppDataUsages == null){
            initInfo();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("aaa", "Running start");
                while (isRunning && mCurTotalValue < mLimit_Value){//mDefault_limit_value){
                    Message msg = new Message();
                    msg.what = HANDLER_MESSAGE_CHECKING;
                    mHandler.sendMessage(msg);
                    try {
                        Thread.sleep(mCircular_time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //stopSelf();
                Log.d("aaa", "Running over");
            }
        }).start();
    }

    private void saveInfo(){
        Log.d("aa", "saveInfo +");

        Log.d("aa", "mCurTotalValue : " + mCurTotalValue + ", mTotalOfferedValue : " + mTotalOfferedValue + ", mLimit_Value : " + mLimit_Value);
        // if something to need to be saved..
        if(mPref == null)
            mPref = SharePreferenceSettings.getInstance();
        mPref.put(ProviderFragment.SP_AOMUNT_offeredData, mCurTotalValue);
        mPref.put(ProviderFragment.SP_AOMUNT_offeredTotalData, mTotalOfferedValue);
        mPref.put(ProviderFragment.SP_AOMUNT_limitData, mLimit_Value);

        Log.d("aa", "saveInfo -");
    }

    @Override
    public void onDestroy() {
        saveInfo();
        super.onDestroy();
    }
}
