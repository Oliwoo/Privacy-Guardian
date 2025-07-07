package com.unict.mobile.adapters.models;

import android.graphics.drawable.Drawable;

public class ItemMicrophoneAccessLogRecord {
    private String name;
    private String desc;
    private Drawable icon;

    public ItemMicrophoneAccessLogRecord(String name, String desc, Drawable icon){
        this.name = name;
        this.desc = desc;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}

