package com.unict.mobile.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unict.mobile.adapters.models.ItemMicrophoneAccessLogRecord;
import com.unict.mobile.R;

import java.util.ArrayList;
import java.util.List;

public class MicrophoneAccessLogRecordAdapter extends RecyclerView.Adapter<MicrophoneAccessLogRecordAdapter.ViewHolder> {
    private List<ItemMicrophoneAccessLogRecord> data = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView name, desc;
        private final ImageView icon;

        public ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.item_microphone_app_usage_record_name);
            desc = view.findViewById(R.id.item_microphone_app_usage_record_desc);
            icon = view.findViewById(R.id.item_microphone_app_usage_record_icon);
        }

        public void setName(String name){
            this.name.setText(name);
        }
        public void setIcon(Drawable icon){
            this.icon.setImageDrawable(icon);
        }
        public void setDesc(String desc){
            this.desc.setText(desc);
        }
    }

    public MicrophoneAccessLogRecordAdapter(){}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_microphone_app_usage_record, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemMicrophoneAccessLogRecord record = data.get(position);
        holder.setName(record.getName());
        holder.setDesc(record.getDesc());
        holder.setIcon(record.getIcon());
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<ItemMicrophoneAccessLogRecord> data){
        this.data = data;
        notifyDataSetChanged();
    }
}


