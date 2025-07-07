package com.unict.mobile.adapters.models;

import com.unict.mobile.models.ApplicationItem;
import com.unict.mobile.utils.DateTimeUtils;

import java.util.List;

public class ItemRecordedAudioTypeLog {
    private String type;
    private long recTime;
    private List<ApplicationItem> apps;

    public ItemRecordedAudioTypeLog(String type, long recTime, List<ApplicationItem> apps) {
        this.type = type;
        this.recTime = recTime;
        this.apps = apps;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRecTime(long recTime){
        this.recTime = recTime;
    }
    public long getRecTime() {
        return recTime;
    }

    public List<ApplicationItem> getApps() {
        return apps;
    }
    public void setApps(List<ApplicationItem> apps) {
        this.apps = apps;
    }

    public String getRecTimeStr(){
        return DateTimeUtils.getFormattedTimeMillisStr(recTime);
    }
}
