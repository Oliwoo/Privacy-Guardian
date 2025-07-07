package com.unict.mobile.adapters.models;

import android.graphics.drawable.Drawable;

public class ItemAppDetail {
    private String appName;
    private String appPackage;
    private Drawable icon;

    public ItemAppDetail(String appName, String appPackage, Drawable icon){
        this.appName = appName;
        this.icon = icon;
        this.appPackage = appPackage;
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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}