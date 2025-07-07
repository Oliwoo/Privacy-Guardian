package com.unict.mobile.adapters.models;

import android.graphics.drawable.Drawable;

public class ItemRecordingTypeStatInfo {
    private transient Drawable icon;
    private String type;
    private long duration;

    public ItemRecordingTypeStatInfo(Drawable icon, String type, long duration) {
        this.icon = icon;
        this.type = type;
        this.duration = duration;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
