package com.unict.mobile.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unict.mobile.R;
import com.unict.mobile.adapters.models.ItemHistoryObject;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.utils.ResourcesUtils;

import java.util.ArrayList;
import java.util.List;

public class HistoryObjectAdapter extends BaseRecyclerViewAdapter<ItemHistoryObject, HistoryObjectAdapter.ViewHolder> {

    public HistoryObjectAdapter(BaseRecyclerViewContainer container) {
        super(container, R.string.default_no_data, R.string.default_loading_message);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_object, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemHistoryObject item = data.get(position);
        holder.setLabel(item.getLabel()==null?getResourcesUtils().getString(R.string.unknown_app_name):item.getLabel());
        holder.setInfoList(item.getInfoList());
        holder.setIcon(item.getIcon());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final TextViewAdapter adapter = new TextViewAdapter();
        private final ImageView icon;

        public ViewHolder(@NonNull View view) {
            super(view);
            icon = view.findViewById(R.id.item_history_object_icon);
            label = view.findViewById(R.id.item_history_object_label);
            RecyclerView infoList = view.findViewById(R.id.item_history_object_records);
            infoList.setLayoutManager(new LinearLayoutManager(view.getContext()));
            infoList.setAdapter(adapter);
        }

        public void setIcon(Drawable icon) {
            this.icon.setImageDrawable(icon);
        }

        public void setLabel(String labelText) {
            label.setText(labelText);
        }

        public void setInfoList(List<String> info) {
            adapter.setData(info);
        }
    }

    private static class TextViewAdapter extends RecyclerView.Adapter<TextViewAdapter.TextViewHolder> {

        private List<String> items = new ArrayList<>();

        public void setData(List<String> newItems) {
            this.items = newItems != null ? newItems : new ArrayList<>();
            notifyItemRangeChanged(0, this.items.size()-1);
        }

        @NonNull
        @Override
        public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ResourcesUtils resourcesUtils = new ResourcesUtils(parent.getContext());
            TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            textView.setTextSize(14);
            textView.setTextColor(resourcesUtils.getColor(R.color.white_50));
            return new TextViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull TextViewHolder holder, int position) {
            holder.textView.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class TextViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;
            public TextViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }

            public void setText(TextView textView) {
                this.textView = textView;
            }
        }
    }
}
