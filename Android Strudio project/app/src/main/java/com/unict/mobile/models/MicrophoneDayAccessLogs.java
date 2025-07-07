package com.unict.mobile.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.unict.mobile.R;
import com.unict.mobile.adapters.models.ItemApplicationStatInfo;
import com.unict.mobile.adapters.models.ItemRecordingTypeStatInfo;
import com.unict.mobile.utils.MicrophoneAccessLogManager;
import com.unict.mobile.utils.PackageResolverUtil;
import com.unict.mobile.utils.ResourcesUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MicrophoneDayAccessLogs{
    final Date date;
    final List<MicrophoneAccessLog> data;
    final MicrophoneAccessLogManager microphoneAccessLogManager;

    public MicrophoneDayAccessLogs(Date date, Context context){
        this.date = date;
        microphoneAccessLogManager = MicrophoneAccessLogManager.getInstance(context);
        data = microphoneAccessLogManager.getLogsForDate(date);
    }

    public List<MicrophoneAccessLog> getLogs(){
        return data;
    }

    public long getAnomaliesCount(){
        return data.stream().filter(MicrophoneAccessLog::isAnomaly).count();
    }

    public AudioTypeLog.RiskLevel getRiskLevel(){
        AtomicReference<AudioTypeLog.RiskLevel> riskLevel = new AtomicReference<>(AudioTypeLog.RiskLevel.LOW);
        data.forEach(l -> riskLevel.set(AudioTypeLog.RiskLevel.max(riskLevel.get(), l.getRiskLevel())));
        return riskLevel.get();
    }

    public Date getDate(){
        return this.date;
    }

    public List<ItemApplicationStatInfo> getAppsStats(Context context){
        Map<String, Long> durationMap = new HashMap<>();

        for (MicrophoneAccessLog micLog : this.data) {
            for (ApplicationAccessLog appLog : micLog.getAppsHistory(context)) {
                String packageName = appLog.getAppPackage();
                if(packageName != null) packageName = packageName.trim().toLowerCase();
                long duration = appLog.getEndDateMillis() - appLog.getStartDateMillis();
                durationMap.compute(packageName, (k, i) -> (i != null ? i : 0) + duration);
            }
        }

        List<ItemApplicationStatInfo> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : durationMap.entrySet()) {
            result.add(new ItemApplicationStatInfo(
                PackageResolverUtil.getAppDrawableIconFromPackage(context, entry.getKey()),
                PackageResolverUtil.getAppNameFromPackage(context,entry.getKey()),
                entry.getKey(),
                entry.getValue()
            ));
        }
        return result;
    }

    public List<ItemRecordingTypeStatInfo> getRecTypeStats(Context context){
        Map<String, Long> durationMap = getStringLongMap();

        List<ItemRecordingTypeStatInfo> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : durationMap.entrySet()) {
            result.add(new ItemRecordingTypeStatInfo(
                    ResourcesUtils.getIcon(context, R.drawable.soundwave),
                    entry.getKey(),
                    entry.getValue()
            ));
        }
        return result;
    }

    @NonNull
    private Map<String, Long> getStringLongMap() {
        Map<String, Long> durationMap = new HashMap<>();

        for (MicrophoneAccessLog micLog : this.data) {
            for (AudioTypeLog recTypeLog : micLog.getAudioTypeHistory()) {
                String type = recTypeLog.getType();
                if(type!=null) type = type.trim().toLowerCase();
                long duration = recTypeLog.getEndDateMillis() - recTypeLog.getStartDateMillis();
                durationMap.compute(type, (k, i) -> (i != null ? i : 0) + duration);
            }
        }
        return durationMap;
    }
}
