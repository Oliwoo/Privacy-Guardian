package com.unict.mobile.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.unict.mobile.R;

public class PageTitle extends LinearLayout {

    private final ImageView goBack;
    private final TextView title;
    private String titleTxt = "";
    private boolean goBackShow = false;

    public PageTitle(Context context) {
        this(context, null, 0,0);
    }

    public PageTitle(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public PageTitle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public PageTitle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.component_page_title,this);
        goBack = findViewById(R.id.component_page_title_go_back);
        title = findViewById(R.id.component_page_title_name);
    }

    public void setTitle(String title){
        this.titleTxt = title;
        this.title.setText(title);
    }
    public String getTitle(){
        return this.titleTxt;
    }

    public void showBack(boolean show){
        this.goBackShow = show;
        this.goBack.setVisibility(show?VISIBLE:INVISIBLE);
    }
    public boolean getGoBackShow(){
        return this.goBackShow;
    }
    public void setGoBackListener(OnClickListener l){
        this.goBack.setOnClickListener(l);
    }
}
