package com.kingsman.hp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by HaeDam on 2017-03-05.
 */

public class SharePreferenceSettings {

    private static final String SETTINGS_NAME = "default_settings";
    private static SharePreferenceSettings sSharedPrefs;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;

    public static class Key {
        /* Recommended naming convention:
         * ints, floats, doubles, longs:
         * SAMPLE_NUM or SAMPLE_COUNT or SAMPLE_INT, SAMPLE_LONG etc.
         *
         * boolean: IS_SAMPLE, HAS_SAMPLE, CONTAINS_SAMPLE
         *
         * String: SAMPLE_KEY, SAMPLE_STR or just SAMPLE
         */
        public static final String NAVIGATION_TYPE = "navigation_type_key";

    }

    private SharePreferenceSettings(Context context) {
        mPref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }

    public static SharePreferenceSettings getInstance(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new SharePreferenceSettings(context.getApplicationContext());
        }
        return sSharedPrefs;
    }

    public static SharePreferenceSettings getInstance() {
        if (sSharedPrefs != null) {
            return sSharedPrefs;
        }

        throw new IllegalArgumentException("Should use getInstance(Context) at least once before using this method.");
    }

    public void put(String key, String val) {
        doEdit();
        mEditor.putString(key, val);
        doCommit();
    }

    public void put(String key, int val) {
        doEdit();
        mEditor.putInt(key, val);
        doCommit();
    }

    public void put(String key, long val) {
        doEdit();
        mEditor.putLong(key, val);
        doCommit();
    }


    public String getString(String key, String defaultValue) {
        return mPref.getString(key, defaultValue);
    }

    public String getString(String key) {
        return mPref.getString(key, null);
    }

    public int getInt(String key) {
        return mPref.getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return mPref.getInt(key, defaultValue);
    }

    public long getLong(String key) {
        return mPref.getLong(key, 0L);
    }

    public long getLong(String key, long defaultValue) {
        return mPref.getLong(key, defaultValue);
    }

    private void doEdit() {
        if (mEditor == null) {
            mEditor = mPref.edit();
        }
    }

    private void doCommit() {
        if (mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }
}