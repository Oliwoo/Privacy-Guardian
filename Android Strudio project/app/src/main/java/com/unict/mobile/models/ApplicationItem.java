package com.unict.mobile.models;

import android.graphics.drawable.Drawable;

public class ApplicationItem {
    private Drawable icon;
    private String appName;
    private String appPackage;

    public ApplicationItem(Drawable icon, String appName, String appPackage) {
        this.icon = icon;
        this.appName = appName;
        this.appPackage = appPackage;
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
}
