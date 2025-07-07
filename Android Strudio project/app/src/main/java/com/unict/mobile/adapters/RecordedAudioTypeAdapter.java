package com.unict.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unict.mobile.R;
import com.unict.mobile.adapters.models.ItemRecordedAudioTypeLog;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.models.ApplicationItem;
import com.unict.mobile.utils.decorators.HorizontalItemDecorator;

import java.util.List;

public class RecordedAudioTypeAdapter extends BaseRecyclerViewAdapter<ItemRecordedAudioTypeLog, RecordedAudioTypeAdapter.ViewHolder>{
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView type, recTime;
        private final AppIconListAdapter appIconListAdapter = new AppIconListAdapter();

        public ViewHolder(View view){
            super(view);
            type = view.findViewById(R.id.item_recording_type_log_type);
            recTime = view.findViewById(R.id.item_recording_type_log_rec_time);
            RecyclerView apps = view.findViewById(R.id.item_recording_type_log_apps);
            apps.addItemDecoration(new HorizontalItemDecorator(4));
            apps.setAdapter(appIconListAdapter);
        }

        public void setType(String type){
            this.type.setText(type);
        }
        public void setRecTime(String recTime){
            this.recTime.setText(recTime);
        }
        public void setApps(List<ApplicationItem> apps){
            this.appIconListAdapter.setData(apps);
        }
    }

    public RecordedAudioTypeAdapter(BaseRecyclerViewContainer container) {
        super(container, R.string.recorded_audio_type_empty_message,R.string.recorded_audio_type_loading_message);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recording_type_log, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemRecordedAudioTypeLog record = data.get(position);
        holder.setType(record.getType());
        holder.setRecTime(record.getRecTimeStr());
        holder.setApps(record.getApps());
    }
}
