package com.unict.mobile.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.unict.mobile.R;
import com.unict.mobile.adapters.HistoryObjectAdapter;
import com.unict.mobile.adapters.models.ItemHistoryObject;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.components.StatView;
import com.unict.mobile.models.AudioTypeLog;
import com.unict.mobile.models.MicrophoneAccessLog;
import com.unict.mobile.utils.AsyncDataLoadHelper;
import com.unict.mobile.utils.DateTimeUtils;
import com.unict.mobile.utils.MicrophoneAccessLogManager;
import com.unict.mobile.utils.decorators.VerticalItemDecorator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MicAccessLogFragment extends BaseFragment {

    private enum INFO_ID{ID,FROM_LIST,RECORDING_TIME,DETECTED_APPS,DETECTED_AUDIO_TYPES,RISK_LEVEL,APP_HISTORY,AUDIO_TYPE_HISTORY}

    private MicrophoneAccessLogManager microphoneMicrophoneAccessLogManager;
    private SwipeRefreshLayout refresh;
    private HistoryObjectAdapter appAccessLogAdapter, recordedAudioTypeLogAdapter;

    private StatView data1, data2, data3, data4;
    private View v;
    private Long id = -1L;
    private boolean fromList = false;
    public static MicAccessLogFragment newInstance(long id){
        MicAccessLogFragment fragment = new MicAccessLogFragment();
        Bundle args = new Bundle();
        args.putLong(INFO_ID.ID.toString(), id);
        fragment.setArguments(args);
        return fragment;
    }

    public static MicAccessLogFragment newInstanceFromList(long id){
        MicAccessLogFragment fragment = new MicAccessLogFragment();
        Bundle args = new Bundle();
        args.putLong(INFO_ID.ID.toString(), id);
        args.putBoolean(INFO_ID.FROM_LIST.toString(),true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        microphoneMicrophoneAccessLogManager = MicrophoneAccessLogManager.getInstance(getContext());

        if(getArguments() != null){
            id = getArguments().getLong(INFO_ID.ID.toString(), -1);
            fromList = getArguments().getBoolean(INFO_ID.FROM_LIST.toString(), false);
        }

        if(fromList){
            setGoBackTarget(MicAccessDateLogsFragment.newInstanceFromList(new Date(id)));
        }else{
            setGoBackTarget(MicAccessDateLogsFragment.newInstance(new Date(id)));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        if(id == -1 || microphoneMicrophoneAccessLogManager.getLogByID(id) == null){
            v = inflater.inflate(R.layout.fragment_mic_usage_log_error, container, false);
            setGoBackTarget(HomeFragment.newInstance());
            setTitle(resourcesUtils.getString(R.string.mic_access_log_not_found));
            return v;
        }

        setTitle(resourcesUtils.getFormattedString(R.string.mic_access_log_title_formatter,microphoneMicrophoneAccessLogManager.getLogByID(id).getDateStr()));
        v = inflater.inflate(R.layout.fragment_mic_usage_log, container, false);

        refresh = v.findViewById(R.id.fragment_mic_usage_log_refresh);
        refresh.setOnRefreshListener(this::refreshLogData);

        BaseRecyclerViewContainer appsHistoryList = v.findViewById(R.id.fragment_mic_usage_log_apps_history);
        appAccessLogAdapter = new HistoryObjectAdapter(appsHistoryList);
        appsHistoryList.addItemDecoration(new VerticalItemDecorator(8));
        appsHistoryList.setAdapter(appAccessLogAdapter);

        BaseRecyclerViewContainer audioTypesHistoryList = v.findViewById(R.id.fragment_mic_usage_log_audio_types_history);
        recordedAudioTypeLogAdapter = new HistoryObjectAdapter(audioTypesHistoryList);
        audioTypesHistoryList.addItemDecoration(new VerticalItemDecorator(16));
        audioTypesHistoryList.setAdapter(recordedAudioTypeLogAdapter);

        data1 = v.findViewById(R.id.fragment_mic_usage_log_data_1);
        data1.init(R.drawable.microphone,R.string.loading, R.string.mic_access_log_stat_rec_time);
        data2 = v.findViewById(R.id.fragment_mic_usage_log_data_2);
        data2.init(R.drawable.shield_warning,R.string.loading, R.string.mic_access_log_stat_risk_level);
        data3 = v.findViewById(R.id.fragment_mic_usage_log_data_3);
        data3.init(R.drawable.data_stored,R.string.loading, R.string.mic_access_log_stat_detected_apps);
        data4 = v.findViewById(R.id.fragment_mic_usage_log_data_4);
        data4.init(R.drawable.soundwave,R.string.loading, R.string.mic_access_log_stat_detected_audio_type);

        loadData();

        return v;
    }

    private void refreshLogData(){
        setTitle(resourcesUtils.getString(R.string.loading));
        data1.setValue(R.string.loading);
        data2.setValue(R.string.loading);
        data3.setValue(R.string.loading);
        data4.setValue(R.string.loading);
        v.setBackground(resourcesUtils.getIcon(R.drawable.bg));
        appAccessLogAdapter.showLoading();
        recordedAudioTypeLogAdapter.showLoading();
        loadData();
    }
    private void loadData(){
        AsyncDataLoadHelper.execute(this, refresh, new AsyncDataLoadHelper.Callback<Map<INFO_ID,Object>>() {
            @Override
            public Map<INFO_ID, Object> getDataRoutine(){
                final MicrophoneAccessLog log = microphoneMicrophoneAccessLogManager.getLogByID(id);
                final List<ItemHistoryObject> appsHistory = new ArrayList<>();
                final List<ItemHistoryObject> recordingTypeHistory = new ArrayList<>();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    log.getAppsHistory(requireContext()).forEach(a -> appsHistory.add(new ItemHistoryObject(
                        a.getIcon(),
                        a.getAppName(),
                        List.of(
                            resourcesUtils.getFormattedString(R.string.mic_access_log_app_history_package_formatter,a.getAppPackage()),
                            resourcesUtils.getFormattedString(R.string.mic_access_log_app_history_duration_formatter,DateTimeUtils.getFormattedTimeMillisStr(a.getDurationMillis()))
                        )
                    )));

                    log.getAudioTypeHistory().forEach(a -> recordingTypeHistory.add(new ItemHistoryObject(
                        resourcesUtils.getIcon(R.drawable.soundwave),
                        a.getType(),
                        List.of(
                            resourcesUtils.getFormattedString(R.string.mic_access_log_rec_type_history_duration_formatter,DateTimeUtils.getFormattedTimeMillisStr(a.getDurationMillis())),
                            resourcesUtils.getFormattedString(R.string.mic_access_log_rec_type_history_risk_level_formatter,AudioTypeLog.getRiskLevel(a.getType()).toString())
                        )
                    )));
                }

                return Map.of(
                    INFO_ID.RECORDING_TIME, log.getDurationMillis(),
                    INFO_ID.DETECTED_APPS, log.getDetectedApps().stream().distinct().count(),
                    INFO_ID.DETECTED_AUDIO_TYPES, log.getDetectedAudioTypes().size(),
                    INFO_ID.RISK_LEVEL, log.getRiskLevel(),
                    INFO_ID.APP_HISTORY, appsHistory,
                    INFO_ID.AUDIO_TYPE_HISTORY, recordingTypeHistory
                );
            }

            @Override
            public void onResult(Map<INFO_ID, Object> result) {
                Long recTime = (Long) result.get(INFO_ID.RECORDING_TIME);
                AudioTypeLog.RiskLevel riskLevel = (AudioTypeLog.RiskLevel) result.get(INFO_ID.RISK_LEVEL);

                data1.setValue(DateTimeUtils.getFormattedTimeMillisStr(recTime!=null?recTime:0L));
                data2.setValue(riskLevel!=null?riskLevel.toString():"-");
                data3.setValue(resourcesUtils.getFormattedString(R.string.mic_access_log_stat_detected_apps_formatter,result.getOrDefault(INFO_ID.DETECTED_APPS,0)));
                data4.setValue(resourcesUtils.getFormattedString(R.string.mic_access_log_stat_detected_audio_types_formatter,result.getOrDefault(INFO_ID.DETECTED_AUDIO_TYPES,0)));
                appAccessLogAdapter.setData((List<ItemHistoryObject>) result.get(INFO_ID.APP_HISTORY));
                recordedAudioTypeLogAdapter.setData((List<ItemHistoryObject>) result.get(INFO_ID.AUDIO_TYPE_HISTORY));

                updateRiskUI(riskLevel);
            }
        });
    }
    private void updateRiskUI(AudioTypeLog.RiskLevel riskLevel){
        if(riskLevel == null) return;
        switch(riskLevel){
            case LOW: v.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.bg_green)); break;
            case MEDIUM: v.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.bg_yellow)); break;
            case HIGH: v.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.bg_red)); break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(id != -1) loadData();
    }
}