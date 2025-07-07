package com.unict.mobile.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.unict.mobile.R;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.utils.ResourcesUtils;

import java.util.ArrayList;
import java.util.List;

abstract class BaseRecyclerViewAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private final ResourcesUtils resourcesUtils;
    private final BaseRecyclerViewContainer container;
    private String emptyMessage;
    private String loadingMessage;
    private Drawable emptyIcon;
    protected List<T> data = new ArrayList<>();


    public BaseRecyclerViewAdapter(BaseRecyclerViewContainer container){
        this.container = container;
        this.resourcesUtils = new ResourcesUtils(container.getContext());
        this.setEmptyMessage(resourcesUtils.getString(R.string.default_base_recycle_view_empty_message));
        this.setLoadingMessage(resourcesUtils.getString(R.string.default_base_recycle_view_loading_message));
        this.setEmptyIcon(resourcesUtils.getIcon(R.drawable.question));
        this.showLoading();
    }

    public BaseRecyclerViewAdapter(BaseRecyclerViewContainer container, @StringRes int emptyMessage, @StringRes int loadingMessage) {
        this(container);
        this.emptyMessage = resourcesUtils.getString(emptyMessage);
        this.loadingMessage = resourcesUtils.getString(loadingMessage);
        this.showLoading();
    }

    public BaseRecyclerViewAdapter(BaseRecyclerViewContainer container, @StringRes int emptyMessage, @StringRes int loadingMessage, @DrawableRes int emptyIcon) {
        this(container, loadingMessage, emptyMessage);
        this.emptyIcon = resourcesUtils.getIcon(emptyIcon);
        this.showLoading();
    }

    public BaseRecyclerViewAdapter(BaseRecyclerViewContainer container, String emptyMessage, String loadingMessage, Drawable emptyIcon) {
        this(container);
        this.emptyMessage = emptyMessage;
        this.loadingMessage = loadingMessage;
        this.emptyIcon = emptyIcon;
        this.showLoading();
    }

    public void setEmptyMessage(String message) {
        this.emptyMessage = message;
    }
    public void setLoadingMessage(String message){
        this.loadingMessage = message;
    }

    public void setEmptyIcon(Drawable icon) {
        this.emptyIcon = icon;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<T> newData) {
        if(newData == null) return;
        this.data = newData;
        notifyDataSetChanged();
        updateUIState();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addItem(T item) {
        if(item == null) return;
        this.data.add(item);
        notifyDataSetChanged();
        updateUIState();
    }

    public void showLoading(){
        this.container.showLoading(loadingMessage);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    private void updateUIState() {
        if(container == null) return;

        if(data == null || data.isEmpty()) {
            container.showMessage(emptyMessage, emptyIcon);
        }else{
            container.showData();
        }
    }

    public ResourcesUtils getResourcesUtils() {
        return resourcesUtils;
    }
}
