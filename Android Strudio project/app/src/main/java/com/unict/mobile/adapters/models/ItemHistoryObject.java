package com.unict.mobile.adapters.models;

import android.graphics.drawable.Drawable;

import java.util.List;

public class ItemHistoryObject {
    private Drawable icon;
    private String label;
    private List<String> infoList;

    public ItemHistoryObject(Drawable icon, String label, List<String> infoList) {
        this.icon = icon;
        this.label = label;
        this.infoList = infoList;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getInfoList() {
        return infoList;
    }

    public void setInfoList(List<String> infoList) {
        this.infoList = infoList;
    }
}
