package com.unict.mobile.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.unict.mobile.R;
import com.unict.mobile.utils.ResourcesUtils;

public class HomeCategoryBtn extends LinearLayout {

    private final ImageView icon;
    private final TextView title, desc;
    private final ResourcesUtils resourcesUtils;

    public HomeCategoryBtn(Context context) {
        this(context, null, 0,0);
    }

    public HomeCategoryBtn(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public HomeCategoryBtn(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public HomeCategoryBtn(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        resourcesUtils = new ResourcesUtils(context);

        LayoutInflater.from(context).inflate(R.layout.component_home_category_btn,this);
        icon = findViewById(R.id.component_home_category_btn_icon);
        title = findViewById(R.id.component_home_category_btn_title);
        desc = findViewById(R.id.component_home_category_btn_desc);
    }

    public void init(@DrawableRes int icon, String title, String desc, OnClickListener l){
        setIcon(icon);
        setTitle(title);
        setDesc(desc);
        setOnClickListener(l);
    }
    public void init(@DrawableRes int icon, @StringRes int title, @StringRes int desc, OnClickListener l){
        setIcon(icon);
        setTitle(title);
        setDesc(desc);
        setOnClickListener(l);
    }

    public void setIcon(@DrawableRes int icon){
        this.icon.setImageDrawable(resourcesUtils.getIcon(icon));
    }
    public void setTitle(String title){
        this.title.setText(title);
    }
    public void setTitle(@StringRes int title){
        this.title.setText(title);
    }
    public void setDesc(String desc){
        this.desc.setText(desc);
    }
    public void setDesc(@StringRes int desc){
        this.desc.setText(desc);
    }
}
