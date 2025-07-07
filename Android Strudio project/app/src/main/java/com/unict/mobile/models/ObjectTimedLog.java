package com.unict.mobile.models;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ObjectTimedLog {
    private long startDateMillis;
    private long endDateMillis;

    public ObjectTimedLog(long startDateMillis, long endDateMillis) {
        this.startDateMillis = startDateMillis;
        this.endDateMillis = endDateMillis;
    }

    public long getStartDateMillis() {
        return startDateMillis;
    }

    public void setStartDateMillis(long startDateMillis) {
        this.startDateMillis = startDateMillis;
    }

    public long getEndDateMillis() {
        return endDateMillis;
    }

    public void setEndDateMillis(long endDateMillis) {
        this.endDateMillis = endDateMillis;
    }

    public long getDurationMillis(){
        return endDateMillis - startDateMillis;
    }
    public double getDuration(){
        return getDurationMillis()/1000.0;
    }
    public String getDateStr(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new java.util.Date(endDateMillis));
    }
    public String getTimeStr(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new java.util.Date(endDateMillis));
    }
}
