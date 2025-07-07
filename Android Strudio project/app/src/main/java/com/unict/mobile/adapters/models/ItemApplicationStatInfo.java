package com.unict.mobile.adapters.models;

import android.graphics.drawable.Drawable;

public class ItemApplicationStatInfo {
    private transient Drawable icon;
    private String appName;
    private String appPackage;
    private long duration;

    public ItemApplicationStatInfo(Drawable icon, String appName, String appPackage, long duration) {
        this.icon = icon;
        this.appName = appName;
        this.appPackage = appPackage;
        this.duration = duration;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
