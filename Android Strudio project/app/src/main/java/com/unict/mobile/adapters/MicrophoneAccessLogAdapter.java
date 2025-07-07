package com.unict.mobile.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.unict.mobile.adapters.models.ItemMicrophoneAccessLog;
import com.unict.mobile.adapters.models.ItemMicrophoneAccessLogRecord;
import com.unict.mobile.R;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.models.ApplicationItem;
import com.unict.mobile.utils.decorators.HorizontalItemDecorator;
import com.unict.mobile.utils.decorators.VerticalItemDecorator;

import java.util.List;

public class MicrophoneAccessLogAdapter extends BaseRecyclerViewAdapter<ItemMicrophoneAccessLog, MicrophoneAccessLogAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView name;
        private final ImageView icon, collapse;
        private final LinearLayout infoBox;
        private final Button showMore;
        private final AppIconListAdapter appIconListAdapter = new AppIconListAdapter();
        private final MicrophoneAccessLogRecordAdapter microphoneAppUsageRecordAdapter = new MicrophoneAccessLogRecordAdapter();

        public ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.item_microphone_access_log_date);
            icon = view.findViewById(R.id.item_microphone_access_log_icon);
            collapse = view.findViewById(R.id.item_microphone_access_log_collapse);
            infoBox = view.findViewById(R.id.item_microphone_access_log_info);
            showMore = view.findViewById(R.id.item_microphone_access_log_show_more);

            RecyclerView apps = view.findViewById(R.id.item_microphone_access_log_apps);
            apps.addItemDecoration(new HorizontalItemDecorator(4));
            apps.setAdapter(appIconListAdapter);

            RecyclerView list = view.findViewById(R.id.item_microphone_access_log_list);
            list.addItemDecoration(new VerticalItemDecorator(8));
            list.setAdapter(microphoneAppUsageRecordAdapter);
        }

        public void setName(String name){
            this.name.setText(name);
        }
        public void setIcon(Drawable icon){
            this.icon.setImageDrawable(icon);
        }
        public void setApps(List<ApplicationItem> apps){
            this.appIconListAdapter.setData(apps);
        }
        public void setOnClickListener(View.OnClickListener l){
            this.itemView.setOnClickListener(l);
        }
        public void setData(List<ItemMicrophoneAccessLogRecord> list){
            this.microphoneAppUsageRecordAdapter.setData(list);
        }
        public void setMinimized(boolean minimized){
            this.infoBox.setVisibility(minimized?GONE:VISIBLE);
            this.collapse.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), minimized?R.drawable.arrow_bottom:R.drawable.arrow_top));
        }
        public void setShowMoreOnClickListener(View.OnClickListener l){
            this.showMore.setOnClickListener(l);
        }
    }

    public MicrophoneAccessLogAdapter(BaseRecyclerViewContainer container) {
        super(container,R.string.microphone_access_log_empty_message,R.string.microphone_access_log_loading_message);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_microphone_access_log, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemMicrophoneAccessLog item = data.get(position);
        holder.setName(item.getAppName());
        holder.setApps(item.getApps());
        holder.setIcon(item.getIcon());
        holder.setData(item.getInfoRecords());
        holder.setMinimized(item.isMinimized());
        holder.setShowMoreOnClickListener(item.getOnClickListener());

        holder.setOnClickListener(v -> {
            item.setMinimized(!item.isMinimized());
            holder.setMinimized(item.isMinimized());
        });
    }
}


