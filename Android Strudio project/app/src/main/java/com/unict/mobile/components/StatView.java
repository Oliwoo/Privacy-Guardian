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

public class StatView extends LinearLayout {

    private final ImageView icon;
    private final TextView value, desc;
    private final ResourcesUtils resourcesUtils;

    public StatView(Context context) {
        this(context, null, 0,0);
    }

    public StatView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public StatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public StatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        resourcesUtils = new ResourcesUtils(context);

        LayoutInflater.from(context).inflate(R.layout.component_stat_view,this);
        icon = findViewById(R.id.component_stat_view_icon);
        value = findViewById(R.id.component_stat_view_value);
        desc = findViewById(R.id.component_stat_view_desc);
    }

    public void init(@DrawableRes int icon, String val, String desc){
        setIcon(icon);
        setValue(val);
        setDesc(desc);
    }
    public void init(@DrawableRes int icon, @StringRes int val, @StringRes int desc){
        setIcon(icon);
        setValue(val);
        setDesc(desc);
    }

    public void setIcon(@DrawableRes int icon){
        this.icon.setImageDrawable(resourcesUtils.getIcon(icon));
    }
    public void setValue(String value){
        this.value.setText(value);
    }
    public void setValue(@StringRes int value){
        this.value.setText(value);
    }
    public void setDesc(String desc){
        this.desc.setText(desc);
    }
    public void setDesc(@StringRes int desc){
        this.desc.setText(desc);
    }
}
