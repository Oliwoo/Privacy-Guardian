package com.unict.mobile.utils;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.PowerManager;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.unict.mobile.R;
import com.unict.mobile.models.ApplicationItem;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Utility per ottenere informazioni sulle applicazioni e la loro interazione con il microfono e il sistema.
 */
public class PackageResolverUtil {

    /**
     * Controlla se un dato package è l'app Home (launcher) predefinita.
     * @param context Contesto dell'applicazione.
     * @param packageName Nome del package da controllare.
     * @return true se è l'app Home, false altrimenti.
     */
    public static boolean isHomeApp(Context context, @NonNull String packageName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, 0);
        if (resolveInfo == null) return false;
        return packageName.equals(resolveInfo.activityInfo.packageName);
    }

    /**
     * Controlla se lo schermo del dispositivo è spento o non interattivo.
     * @param context Contesto dell'applicazione.
     * @return true se lo schermo è spento, false altrimenti.
     */
    public static boolean isScreenOff(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(pm == null) return false;
        return !pm.isInteractive();
    }

    /**
     * Controlla se un'applicazione ha il permesso di usare il microfono (RECORD_AUDIO).
     * @param ctx Contesto.
     * @param packageName Nome del package.
     * @return true se il permesso è concesso, false altrimenti.
     */
    public static boolean hasMicrophonePermission(Context ctx, @NonNull String packageName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            return pm.checkPermission(android.Manifest.permission.RECORD_AUDIO, packageName) == PackageManager.PERMISSION_GRANTED;
        }catch(Exception e){
            return false;
        }
    }

    /**
     * Controlla se un'applicazione ha effettivamente accesso al microfono tramite AppOpsManager (API 29+).
     * @param ctx Contesto.
     * @param packageName Nome del package.
     * @return true se l'accesso è consentito, false altrimenti.
     */
    public static boolean hasMicrophoneAccess(Context ctx, @NonNull String packageName){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppOpsManager appOps = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);
            if(appOps == null) return false;
            try{
                ApplicationInfo appInfo = ctx.getPackageManager().getApplicationInfo(packageName, 0);
                return appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_RECORD_AUDIO, appInfo.uid, packageName) == AppOpsManager.MODE_ALLOWED;
            }catch(Exception e){
                return false;
            }
        }
        return true;
    }

    /**
     * Ottiene il nome del package dell'app in foreground usando UsageStatsManager.
     * Richiede il permesso USAGE_STATS.
     * @param ctx Contesto.
     * @return Nome del package dell'app in foreground, o null se non trovata o permesso negato.
     */
    public static String getForegroundAppPackageByUsageStats(Context ctx) {
        if(!hasUsageStatsPermission(ctx)) return null;

        UsageStatsManager usageStatsManager = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        if(usageStatsManager == null) return null;

        long currentTime = System.currentTimeMillis();
        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - (5 * 1000), currentTime);

        if (stats != null && !stats.isEmpty()) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : stats){
                if (usageStats.getTotalTimeInForeground() > 0 && usageStats.getLastTimeUsed() > currentTime - 5000 && !usageStats.getPackageName().equals(ctx.getPackageName())) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
            }
            if (!mySortedMap.isEmpty()){
                UsageStats latestUsageStats = mySortedMap.get(mySortedMap.lastKey());
                if(latestUsageStats != null) return latestUsageStats.getPackageName();
            }
        }
        return null;
    }

    /**
     * Controlla se l'applicazione ha il permesso USAGE_STATS.
     * @param ctx Contesto.
     * @return true se il permesso è concesso, false altrimenti.
     */
    public static boolean hasUsageStatsPermission(Context ctx){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppOpsManager appOps = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);
            if (appOps == null) return false;
            int mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), ctx.getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        }else{
            return true;
        }
    }

    /**
     * Ottiene il nome dell'applicazione dato il nome del package.
     * @param context Contesto.
     * @param packageName Nome del package.
     * @return Nome dell'app o null.
     */
    public static String getAppNameFromPackage(Context context, @NonNull String packageName) {
        try{
            PackageManager pm = context.getApplicationContext().getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return (String) pm.getApplicationLabel(ai);
        }catch(Exception ignored){
            return null;
        }
    }

    /**
     * Ottiene l'icona Drawable di un'applicazione dato il nome del package.
     * @param context Contesto.
     * @param packageName Nome del package.
     * @return Icona dell'app o un'icona di default se non trovata.
     */
    public static Drawable getAppDrawableIconFromPackage(Context context, @NonNull String packageName){
        Drawable icon = AppCompatResources.getDrawable(context, R.drawable.unknown_package_icon);
        try{
            PackageManager pm = context.getApplicationContext().getPackageManager();
            icon = pm.getApplicationIcon(packageName);
        }catch(Exception ignored){}
        return icon;
    }

    /**
     * Crea un oggetto ApplicationItem con icona, nome e package dell'app.
     * @param context Contesto.
     * @param packageName Nome del package.
     * @return Oggetto ApplicationItem.
     */
    public static ApplicationItem getApplicationItem(Context context, @NonNull String packageName){
        return new ApplicationItem(
                getAppDrawableIconFromPackage(context, packageName),
                getAppNameFromPackage(context, packageName),
                packageName
        );
    }
}