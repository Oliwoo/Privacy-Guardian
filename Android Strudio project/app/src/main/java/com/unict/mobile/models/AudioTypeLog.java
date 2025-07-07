package com.unict.mobile.models;

import android.content.Context;

import com.unict.mobile.utils.PackageResolverUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AudioTypeLog extends ObjectTimedLog{

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH;

        public static RiskLevel max(RiskLevel level1, RiskLevel level2) {
            return (level1.ordinal() > level2.ordinal()) ? level1 : level2;
        }
    }
    private String type;
    private List<String> detectedApps;

    public AudioTypeLog(){
        super(0L,0L);
        this.detectedApps = new ArrayList<>();
    }

    public AudioTypeLog(String type, long startDateMillis, long endDateMillis, List<String> detectedApps) {
        super(startDateMillis,endDateMillis);
        this.type = type;
        this.detectedApps = detectedApps;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getDetectedApps() {
        return detectedApps;
    }

    public void setDetectedApps(List<String> detectedApps) {
        this.detectedApps = detectedApps;
    }
    public void addDetectedApp(String packageName){
        this.detectedApps.add(packageName);
    }

    public List<ApplicationItem> getDetectedApplicationItems(Context context){
        return this.detectedApps.stream().distinct().map(i -> PackageResolverUtil.getApplicationItem(context,i)).collect(Collectors.toList());
    }

    public RiskLevel getPrivacyRiskLevel(){
        return getRiskLevel(this.type);
    }

    public static RiskLevel getRiskLevel(String type){
        if(type == null) return RiskLevel.LOW;

        String label = type.toLowerCase();

        // Rischio alto: voce umana, musica, contenuti sensibili
        if (label.contains("speech") || label.contains("conversation") || label.contains("singing") || label.contains("music") ||
                label.contains("whispering") || label.contains("radio") || label.contains("telephone")) {
            return RiskLevel.HIGH;
        }

        // Rischio medio: rumori ambientali riconoscibili
        if (label.contains("engine") || label.contains("vehicle") || label.contains("keyboard") || label.contains("door") ||
                label.contains("typing") || label.contains("alarm") || label.contains("tools")) {
            return RiskLevel.MEDIUM;
        }

        // Rischio basso: silenzio, suoni naturali, animali non parlanti
        if (label.contains("silence") || label.contains("ambient") || label.contains("wind") || label.contains("animal") ||
                label.contains("bird") || label.contains("footsteps")) {
            return RiskLevel.LOW;
        }

        // Default
        return RiskLevel.MEDIUM;
    }
}
