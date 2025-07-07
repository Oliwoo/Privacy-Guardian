package com.unict.mobile.adapters.models;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.unict.mobile.models.ApplicationItem;

import java.util.List;

public class ItemMicrophoneAccessLog {
    private String appName;
    private Drawable icon;
    private List<ApplicationItem> apps;
    private List<ItemMicrophoneAccessLogRecord> infoRecords;
    private View.OnClickListener onClickListener;
    private boolean minimized = true;

    public ItemMicrophoneAccessLog(String appName, List<ApplicationItem> apps, Drawable icon, List<ItemMicrophoneAccessLogRecord> data, View.OnClickListener l){
        this.appName = appName;
        this.icon = icon;
        this.apps = apps;
        this.infoRecords = data;
        this.onClickListener = l;
    }

    public String getAppName() {
        return appName!=null?appName:"(Unknown)";
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<ApplicationItem> getApps() {
        return apps;
    }

    public void setApps(List<ApplicationItem> apps) {
        this.apps = apps;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isMinimized() {
        return minimized;
    }

    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    public List<ItemMicrophoneAccessLogRecord> getInfoRecords() {
        return infoRecords;
    }

    public void setInfoRecords(List<ItemMicrophoneAccessLogRecord> infoRecords) {
        this.infoRecords = infoRecords;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}