package com.unict.mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.unict.mobile.adapters.MicrophoneAccessLogAdapter;
import com.unict.mobile.adapters.models.ItemMicrophoneAccessLog;
import com.unict.mobile.adapters.models.ItemMicrophoneAccessLogRecord;
import com.unict.mobile.R;
import com.unict.mobile.components.StatView;
import com.unict.mobile.models.AudioTypeLog;
import com.unict.mobile.utils.AsyncDataLoadHelper;
import com.unict.mobile.utils.DateTimeUtils;
import com.unict.mobile.utils.MicrophoneAccessLogManager;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.models.MicrophoneAccessLog;
import com.unict.mobile.utils.decorators.VerticalItemDecorator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MicAccessDateLogsFragment extends BaseFragment {

    private enum INFO_ID{DATE,FROM_LIST,ANOMALIES_COUNT,RECORDING_TIME,DETECTED_APPS,LAST_REPORT,RISK_LEVEL,DATA}
    private MicrophoneAccessLogManager microphoneMicrophoneAccessLogManager;
    private SwipeRefreshLayout refresh;
    private MicrophoneAccessLogAdapter adapter;

    private StatView data1, data2, data3, data4;
    private View v;
    private Date date;
    private boolean fromList;

    public static MicAccessDateLogsFragment newInstance(Date date) {
        MicAccessDateLogsFragment fragment = new MicAccessDateLogsFragment();
        Bundle args = new Bundle();
        args.putString(INFO_ID.DATE.toString(),DateTimeUtils.parseDate(date));
        fragment.setArguments(args);
        return fragment;
    }

    public static MicAccessDateLogsFragment newInstanceFromList(Date date) {
        MicAccessDateLogsFragment fragment = new MicAccessDateLogsFragment();
        Bundle args = new Bundle();
        args.putString(INFO_ID.DATE.toString(),DateTimeUtils.parseDate(date));
        args.putBoolean(INFO_ID.FROM_LIST.toString(),true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        microphoneMicrophoneAccessLogManager = MicrophoneAccessLogManager.getInstance(getContext());

        if(getArguments() != null){
            try {
                date = DateTimeUtils.parseDate(getArguments().getString(INFO_ID.DATE.toString(), null));
            } catch (Exception e) {
                date = null;
            }

            if(getArguments().getBoolean(INFO_ID.FROM_LIST.toString(), false)) {
                setGoBackTarget(DateStoredAccessLogsFragment.newInstance());
                fromList = true;
            }else{
                setGoBackTarget(HomeFragment.newInstance());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        v = inflater.inflate(R.layout.fragment_mic_usage_logs, container, false);

        if(date == null){
            v = inflater.inflate(R.layout.fragment_mic_usage_log_error, container, false);
            setTitle(resourcesUtils.getString(R.string.mic_access_date_logs_title));
            return v;
        }else{
            if(microphoneMicrophoneAccessLogManager.getLogsForDate(date)==null){
                v = inflater.inflate(R.layout.fragment_mic_usage_log_error, container, false);
                setTitle(resourcesUtils.getString(R.string.mic_access_log_not_found));
                return v;
            }else{
                setTitle(resourcesUtils.getFormattedString(R.string.mic_access_date_logs_title_formatter, DateTimeUtils.formatDate(date)));
            }
        }

        refresh = v.findViewById(R.id.fragment_mic_usage_logs_refresh);
        refresh.setOnRefreshListener(this::refreshLogData);

        BaseRecyclerViewContainer list = v.findViewById(R.id.fragment_mic_usage_logs_list);
        adapter = new MicrophoneAccessLogAdapter(list);
        list.addItemDecoration(new VerticalItemDecorator(8));
        list.setAdapter(adapter);

        data1 = v.findViewById(R.id.fragment_mic_usage_logs_data_1);
        data1.init(R.drawable.shield_warning,R.string.loading, R.string.mic_access_date_logs_stat_detected_anomalies);
        data2 = v.findViewById(R.id.fragment_mic_usage_logs_data_2);
        data2.init(R.drawable.microphone,R.string.loading, R.string.mic_access_date_logs_stat_rec_time);
        data3 = v.findViewById(R.id.fragment_mic_usage_logs_data_3);
        data3.init(R.drawable.data_stored,R.string.loading, R.string.mic_access_date_logs_stat_detected_apps);
        data4 = v.findViewById(R.id.fragment_mic_usage_logs_data_4);
        data4.init(R.drawable.calendar,R.string.loading, R.string.mic_access_date_logs_stat_last_log_time);

        loadData();
        return v;
    }

    private void refreshLogData(){
        data1.setValue(R.string.loading);
        data2.setValue(R.string.loading);
        data3.setValue(R.string.loading);
        data4.setValue(R.string.loading);
        v.setBackground(AppCompatResources.getDrawable(requireContext(),R.drawable.bg));
        adapter.showLoading();
        loadData();
    }
    private void loadData(){
        AsyncDataLoadHelper.execute(this, refresh, new AsyncDataLoadHelper.Callback<Map<INFO_ID,Object>>() {
            @Override
            public Map<INFO_ID, Object> getDataRoutine(){
                final List<MicrophoneAccessLog> logs = microphoneMicrophoneAccessLogManager.getLogsForDate(date);
                AtomicReference<AudioTypeLog.RiskLevel> riskLevel = new AtomicReference<>(AudioTypeLog.RiskLevel.LOW);
                AtomicInteger todayAnomalies = new AtomicInteger();
                AtomicLong recordingTime = new AtomicLong(0L);
                final List<String> detectedApps = new ArrayList<>();
                final List<ItemMicrophoneAccessLog> data = new ArrayList<>();

                logs.forEach(l -> {
                    if(l.isAnomaly()) todayAnomalies.getAndIncrement(); // Count detected anomalies
                    recordingTime.addAndGet(l.getDurationMillis()); // Count detected recording time
                    detectedApps.addAll(l.getDetectedApps());
                    riskLevel.set(AudioTypeLog.RiskLevel.max(riskLevel.get(), l.getRiskLevel()));

                    List<ItemMicrophoneAccessLogRecord> records = new ArrayList<>();
                    records.add(new ItemMicrophoneAccessLogRecord(resourcesUtils.getString(R.string.item_microphone_access_log_record_access_date), l.getDateStr(), AppCompatResources.getDrawable(requireContext(), R.drawable.calendar)));
                    records.add(new ItemMicrophoneAccessLogRecord(resourcesUtils.getString(R.string.item_microphone_access_log_record_mic_usage), DateTimeUtils.getFormattedTimeMillisStr(l.getDurationMillis()), AppCompatResources.getDrawable(requireContext(), R.drawable.microphone)));
                    records.add(new ItemMicrophoneAccessLogRecord(resourcesUtils.getString(R.string.item_microphone_access_log_record_anomaly_usage), resourcesUtils.getString(l.isAnomaly()?R.string.is_anomaly_bool_true:R.string.is_anomaly_bool_false), AppCompatResources.getDrawable(requireContext(), R.drawable.shield_warning)));
                    records.add(new ItemMicrophoneAccessLogRecord(resourcesUtils.getString(R.string.item_microphone_access_log_record_detected_apps), l.getDetectedApps().stream().map(a -> a!=null?a:resourcesUtils.getString(R.string.unknown_app_package)).collect(Collectors.joining(", ")), AppCompatResources.getDrawable(requireContext(),R.drawable.data_stored)));
                    records.add(new ItemMicrophoneAccessLogRecord(resourcesUtils.getString(R.string.item_microphone_access_log_record_detected_audio_types), l.getDetectedAudioTypes().isEmpty()?resourcesUtils.getString(R.string.default_no_data):String.join(", ",l.getDetectedAudioTypes()), AppCompatResources.getDrawable(requireContext(),R.drawable.soundwave)));

                    data.add(new ItemMicrophoneAccessLog(
                        resourcesUtils.getString(l.isAnomaly()?R.string.is_anomaly_str_true:R.string.is_anomaly_str_false),
                        l.getDetectedApplicationItems(requireContext()),
                        resourcesUtils.getIcon(l.isAnomaly()?R.drawable.shield_warning:R.drawable.microphone),
                        records,
                        v -> requestFragmentChange(fromList?MicAccessLogFragment.newInstanceFromList(l.getStartDateMillis()):MicAccessLogFragment.newInstance(l.getStartDateMillis()))
                    ));
                });

                return Map.of(
                    INFO_ID.ANOMALIES_COUNT, todayAnomalies.get(),
                    INFO_ID.RECORDING_TIME, recordingTime.get(),
                    INFO_ID.DETECTED_APPS, detectedApps.stream().distinct().count(),
                    INFO_ID.LAST_REPORT, logs.isEmpty()?"-":logs.get(0).getTimeStr(),
                    INFO_ID.RISK_LEVEL, riskLevel.get(),
                    INFO_ID.DATA, data
                );
            }

            @Override
            public void onResult(Map<INFO_ID, Object> result) {
                Long millis = (Long) result.get(INFO_ID.RECORDING_TIME);
                AudioTypeLog.RiskLevel riskLevel = (AudioTypeLog.RiskLevel) result.get(INFO_ID.RISK_LEVEL);

                data1.setValue(resourcesUtils.getFormattedString(R.string.mic_access_date_logs_anomalies_count_formatter,result.getOrDefault(INFO_ID.ANOMALIES_COUNT,0)));
                data2.setValue(DateTimeUtils.getFormattedTimeMillisStr(millis!=null?millis:0L));
                data3.setValue(resourcesUtils.getFormattedString(R.string.mic_access_date_logs_detected_apps_formatter,result.getOrDefault(INFO_ID.DETECTED_APPS,0)));
                data4.setValue((String) result.getOrDefault(INFO_ID.LAST_REPORT,"-"));
                adapter.setData((List<ItemMicrophoneAccessLog>) result.get(INFO_ID.DATA));
                updateRiskUI(riskLevel);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if(date != null) loadData();
    }

    private void updateRiskUI(AudioTypeLog.RiskLevel riskLevel){
        if(riskLevel == null) return;
        switch(riskLevel){
            case LOW: v.setBackground(resourcesUtils.getIcon(R.drawable.bg_green)); break;
            case MEDIUM: v.setBackground(resourcesUtils.getIcon(R.drawable.bg_yellow)); break;
            case HIGH: v.setBackground(resourcesUtils.getIcon(R.drawable.bg_red)); break;
        }
    }
}