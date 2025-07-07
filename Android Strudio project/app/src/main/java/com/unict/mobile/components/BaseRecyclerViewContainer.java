package com.unict.mobile.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.unict.mobile.R;

public class BaseRecyclerViewContainer extends FrameLayout {

    private RecyclerView list;
    private ProgressBar loading;
    private ImageView icon;
    private TextView msg;

    public BaseRecyclerViewContainer(Context context) {
        super(context);
        init(context);
    }

    public BaseRecyclerViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseRecyclerViewContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.component_base_recycle_view, this, true);
        list = findViewById(R.id.component_base_recycle_view_list);
        loading = findViewById(R.id.component_base_recycle_view_loading);
        icon = findViewById(R.id.component_base_recycle_view_icon);
        msg = findViewById(R.id.component_base_recycle_view_msg);
    }

    public RecyclerView getRecyclerView() {
        return list;
    }

    public void setAdapter(Adapter<? extends RecyclerView.ViewHolder> adapter){
        this.list.setAdapter(adapter);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration decor){
        this.list.addItemDecoration(decor);
    }

    public void showLoading(String msg) {
        loading.setVisibility(VISIBLE);
        list.setVisibility(GONE);
        icon.setVisibility(GONE);
        this.msg.setVisibility(VISIBLE);
        this.msg.setText(msg);
    }

    public void showMessage(String msg, Drawable icon){
        loading.setVisibility(GONE);
        list.setVisibility(GONE);
        this.icon.setImageDrawable(icon);
        this.icon.setVisibility(VISIBLE);
        this.msg.setText(msg);
        this.msg.setVisibility(VISIBLE);
    }

    public void showData() {
        loading.setVisibility(GONE);
        icon.setVisibility(GONE);
        msg.setVisibility(GONE);
        list.setVisibility(VISIBLE);
    }
}

