package com.unict.mobile.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unict.mobile.adapters.models.ItemAppDetail;
import com.unict.mobile.R;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.utils.MicrophoneUsageUtils;

public class AppWithMicrophoneAccessAdapter extends BaseRecyclerViewAdapter<ItemAppDetail,AppWithMicrophoneAccessAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView name, pkg;
        private final ImageView icon;

        public ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.item_app_microphone_access_name);
            pkg = view.findViewById(R.id.item_app_microphone_access_package);
            icon = view.findViewById(R.id.item_app_microphone_access_icon);
        }

        public void setName(String name){
            this.name.setText(name);
        }
        public void setIcon(Drawable icon){
            this.icon.setImageDrawable(icon);
        }
        public void setPkg(String pkg){
            this.pkg.setText(pkg);
            this.itemView.setOnClickListener(l -> MicrophoneUsageUtils.openAppSettings(itemView.getContext(), pkg));
        }
    }

    public AppWithMicrophoneAccessAdapter(BaseRecyclerViewContainer container) {
        super(container,R.string.app_with_microphone_access_empty_message,R.string.app_with_microphone_access_loading_message);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_with_microphone_access, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemAppDetail record = data.get(position);
        holder.setName(record.getAppName());
        holder.setPkg(record.getAppPackage());
        holder.setIcon(record.getIcon());
    }
}


