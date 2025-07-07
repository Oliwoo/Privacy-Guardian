package com.unict.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unict.mobile.R;
import com.unict.mobile.adapters.models.ItemDateStoredLog;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.utils.DateTimeUtils;

public class DateStoredLogAdapter extends BaseRecyclerViewAdapter<ItemDateStoredLog, DateStoredLogAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView date, count;

        public ViewHolder(View view){
            super(view);
            date = view.findViewById(R.id.item_microphone_access_logs_item_date);
            count = view.findViewById(R.id.item_microphone_access_logs_item_count);
        }
        public void setDate(String date){
            this.date.setText(date);
        }
        public void setCount(String count){
            this.count.setText(count);
        }
        public void setOnClickListener(View.OnClickListener l){
            this.itemView.setOnClickListener(l);
        }
    }

    public DateStoredLogAdapter(BaseRecyclerViewContainer container) {
        super(container, R.string.date_stored_log_empty_message, R.string.date_stored_log_loading_message);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_microphone_access_logs_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemDateStoredLog item = data.get(position);
        holder.setCount(getResourcesUtils().getFormattedString(R.string.date_stored_log_count_formatter,item.getCount()));
        holder.setOnClickListener(item.getOnClickListener());
        holder.setDate(DateTimeUtils.parseDate(item.getDate()));
    }
}


