package com.unict.mobile.models;

import android.content.Context;

import com.unict.mobile.utils.PackageResolverUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MicrophoneAccessLog extends ObjectTimedLog{
    private boolean anomaly;
    private List<ApplicationAccessLog> appsHistory;
    private List<AudioTypeLog> audioTypeHistory;

    public MicrophoneAccessLog(){
        super(0, 0);
        this.anomaly = false;
        this.appsHistory = new ArrayList<>();
        this.audioTypeHistory = new ArrayList<>();
    }

    public MicrophoneAccessLog(long startDateMillis, long endDateMillis, boolean anomaly, List<ApplicationAccessLog> appsHistory, List<AudioTypeLog> audioTypeHistory) {
        super(startDateMillis, endDateMillis);
        this.anomaly = anomaly;
        this.appsHistory = appsHistory;
        this.audioTypeHistory = audioTypeHistory;
    }

    // -- ANOMALY MANAGER --
    public void setAnomaly(boolean anomaly) {
        this.anomaly = anomaly;
    }
    public boolean isAnomaly() {
        return anomaly;
    }


    // -- APPLICATION ACCESS HISTORY MANAGER --
    public void setAppsHistory(List<ApplicationAccessLog> appsHistory) {
        this.appsHistory = appsHistory;
    }
    public List<ApplicationAccessLog> getAppsHistory(Context ctx) {
        this.appsHistory.forEach(i -> i.setIcon(PackageResolverUtil.getAppDrawableIconFromPackage(ctx,i.getAppPackage())));
        return this.appsHistory;
    }
    public List<String> getDetectedApps(){
        return this.appsHistory.stream().map(ApplicationAccessLog::getAppName).distinct().collect(Collectors.toList());
    }
    public ApplicationAccessLog getLastDetectedApp(){
        if(this.appsHistory.isEmpty()) return null;
        return this.appsHistory.get(this.appsHistory.size()-1);
    }
    public void addDetectedApp(ApplicationAccessLog log){
        if(this.getLastDetectedApp() != null){
            if(Objects.equals(this.getLastDetectedApp().getAppPackage(), log.getAppPackage())){
                this.getLastDetectedApp().setEndDateMillis(log.getStartDateMillis());
            }else{
                this.getLastDetectedApp().setEndDateMillis(log.getStartDateMillis());
                this.appsHistory.add(log);
            }
        }else{
            this.appsHistory.add(log);
        }
    }
    public List<ApplicationItem> getDetectedApplicationItems(Context context){
        return this.appsHistory.stream().map(ApplicationAccessLog::getAppPackage).distinct().map(i -> PackageResolverUtil.getApplicationItem(context,i)).collect(Collectors.toList());
    }


    // -- AUDIO TYPE HISTORY MANAGER --
    public void setAudioTypeHistory(List<AudioTypeLog> audioTypeHistory) {
        this.audioTypeHistory = audioTypeHistory;
    }
    public List<AudioTypeLog> getAudioTypeHistory() {
        return audioTypeHistory;
    }
    public List<String> getDetectedAudioTypes(){
        return this.audioTypeHistory.stream().map(AudioTypeLog::getType).distinct().collect(Collectors.toList());
    }
    private AudioTypeLog getLastDetectedAudioType(){
        if(this.audioTypeHistory.isEmpty()) return null;
        return this.audioTypeHistory.get(this.audioTypeHistory.size()-1);
    }
    public void addDetectedAudioType(AudioTypeLog log){
        if(this.getLastDetectedAudioType() != null && Objects.equals(this.getLastDetectedAudioType().getType(),log.getType())){
            log.getDetectedApps().forEach(a -> this.getLastDetectedAudioType().addDetectedApp(a));
        }else{
            if(!this.audioTypeHistory.isEmpty()){this.getLastDetectedAudioType().setEndDateMillis(log.getEndDateMillis());}
            this.audioTypeHistory.add(log);
        }
    }

    // -- RISK MANAGER --
    public AudioTypeLog.RiskLevel getRiskLevel(){
        AtomicReference<AudioTypeLog.RiskLevel> level = new AtomicReference<>(AudioTypeLog.RiskLevel.LOW);
        if(this.audioTypeHistory.isEmpty()) return AudioTypeLog.RiskLevel.LOW;
        this.getAudioTypeHistory().forEach(l -> level.set(AudioTypeLog.RiskLevel.max(level.get(),l.getPrivacyRiskLevel())));
        return level.get();
    }
}
