package com.unict.mobile.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unict.mobile.R;
import com.unict.mobile.models.ApplicationItem;

import java.util.ArrayList;
import java.util.List;

public class AppIconListAdapter extends RecyclerView.Adapter<AppIconListAdapter.ViewHolder> {
    private List<ApplicationItem> data = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView icon;
        public ViewHolder(View view){
            super(view);
            icon = view.findViewById(R.id.item_application_icon_list_icon);
        }
        public void setIcon(Drawable icon){
            this.icon.setImageDrawable(icon);
        }
    }

    public AppIconListAdapter(){}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_icon_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationItem item = data.get(position);
        holder.setIcon(item.getIcon());
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<ApplicationItem> data){
        this.data = data;
        notifyDataSetChanged();
    }
}


