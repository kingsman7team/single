package com.kingsman.hp.utils;

/**
 * Created by Haedam on 2017-03-05.
 */

import android.widget.ImageView;

import com.kingsman.hp.ap.AccessPoint;

public class Item {

    private AccessPoint accessPoint;
    private int type;
    private int level;
    private String ssid;
    private int rssid;
    private ImageView icon;

    public AccessPoint getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(AccessPoint accessPoint) {
        this.accessPoint = accessPoint;
        this.ssid = accessPoint.ssid;
        this.rssid = accessPoint.mRssi;
        this.level = accessPoint.getLevel();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getRssid() {
        return rssid;
    }

    public void setRssid(int mssid) {
        this.rssid = mssid;
    }

    public ImageView getIcon() {
        return icon;
    }

    public void setIcon(ImageView icon) {
        this.icon = icon;
    }
}
