package com.unict.mobile.adapters.models;

import android.view.View;

import java.util.Date;

public class ItemDateStoredLog {
    private Date date;
    private long count;
    private View.OnClickListener onClickListener = null;

    public ItemDateStoredLog(Date date, long count){
        this.date = date;
        this.count = count;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}