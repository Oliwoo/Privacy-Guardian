package com.unict.mobile.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.unict.mobile.R;
import com.unict.mobile.adapters.DateStoredLogAdapter;
import com.unict.mobile.adapters.models.ItemDateStoredLog;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.utils.AsyncDataLoadHelper;
import com.unict.mobile.utils.MicrophoneAccessLogManager;
import com.unict.mobile.utils.ResourcesUtils;
import com.unict.mobile.utils.decorators.VerticalItemDecorator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class DateStoredAccessLogsFragment extends BaseFragment {

    private static final String DATE_STORED_LOGS = "dateStoredLogs";
    private MicrophoneAccessLogManager microphoneMicrophoneAccessLogManager;
    private SwipeRefreshLayout refresh;
    private DateStoredLogAdapter dateStoredLogAdapter;

    public static DateStoredAccessLogsFragment newInstance(){
        DateStoredAccessLogsFragment fragment = new DateStoredAccessLogsFragment();
        fragment.setGoBackTarget(HomeFragment.newInstance());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_mic_usage_logs_list, container, false);
        setTitle(ResourcesUtils.getString(requireContext(),R.string.date_stored_access_logs_title));

        refresh = v.findViewById(R.id.fragment_mic_usage_logs_list_refresh);

        BaseRecyclerViewContainer list = v.findViewById(R.id.fragment_mic_usage_logs_list_data);
        dateStoredLogAdapter = new DateStoredLogAdapter(list);
        list.addItemDecoration(new VerticalItemDecorator(8));
        list.setAdapter(dateStoredLogAdapter);

        microphoneMicrophoneAccessLogManager = MicrophoneAccessLogManager.getInstance(getContext());
        refresh.setOnRefreshListener(this::refreshLogData);

        ImageView selectDate = v.findViewById(R.id.fragment_mic_usage_logs_list_select_btn);
        selectDate.setOnClickListener(l -> showWeekPicker());

        loadData();
        return v;
    }

    public void showWeekPicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(new ContextThemeWrapper(getContext(), R.style.DarkDatePickerDialog), (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);

            requestFragmentChange(MicAccessDateLogsFragment.newInstance(selected.getTime()));
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void refreshLogData(){
        dateStoredLogAdapter.showLoading();
        loadData();
    }
    private void loadData(){
        AsyncDataLoadHelper.execute(this, refresh, new AsyncDataLoadHelper.Callback<Map<String,Object>>() {
            @Override
            public Map<String, Object> getDataRoutine(){
                final List<ItemDateStoredLog> logs = new ArrayList<>();
                try {
                    logs.addAll(microphoneMicrophoneAccessLogManager.getAllLogsList());
                    logs.forEach(l -> l.setOnClickListener(v -> requestFragmentChange(MicAccessDateLogsFragment.newInstanceFromList(l.getDate()))));
                }catch(Exception e){
                    dateStoredLogAdapter.setEmptyMessage("Error on getting logs");
                }
                return Map.of(DATE_STORED_LOGS, logs);
            }

            @Override
            public void onResult(Map<String, Object> result) {
                dateStoredLogAdapter.setData((List<ItemDateStoredLog>) result.getOrDefault(DATE_STORED_LOGS,new ArrayList<>()));
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        loadData();
    }
}