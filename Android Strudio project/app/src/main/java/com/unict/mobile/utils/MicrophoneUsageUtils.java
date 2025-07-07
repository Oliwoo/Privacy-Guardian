package com.unict.mobile.utils;

import static android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility per ottenere informazioni sull'uso del microfono da parte delle app
 * e per aprire le impostazioni delle app.
 */
public class MicrophoneUsageUtils {

    /**
     * Rappresenta i dettagli di un'applicazione: nome, package e icona.
     */
    public static class AppDetails {
        public final String appName;
        public final String packageName;
        public final Drawable appIcon;

        public AppDetails(String appName, String packageName, Drawable appIcon) {
            this.appName = appName;
            this.packageName = packageName;
            this.appIcon = appIcon;
        }
    }

    /**
     * Recupera le applicazioni non di sistema che hanno il permesso di accedere al microfono.
     * @param context Contesto dell'applicazione.
     * @return Lista di AppDetails per le app con accesso al microfono.
     */
    public static List<AppDetails> getAppsWithMicrophoneAccess(Context context) {
        List<AppDetails> appsWithMicrophoneInfo = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedApps = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo pkg : installedApps) {
            try {
                String[] perms = packageManager.getPackageInfo(pkg.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
                if(perms == null) continue;

                int permPivot = Arrays.asList(perms).indexOf(Manifest.permission.RECORD_AUDIO);
                boolean granted = permPivot != -1 && pkg.requestedPermissionsFlags != null &&
                        (pkg.requestedPermissionsFlags[permPivot] & REQUESTED_PERMISSION_GRANTED) != 0;

                ApplicationInfo appInfo = packageManager.getApplicationInfo(pkg.packageName, PackageManager.GET_PERMISSIONS);
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && granted) {
                    String appName = packageManager.getApplicationLabel(appInfo).toString();
                    String packageName = appInfo.packageName;
                    Drawable appIcon = packageManager.getApplicationIcon(appInfo);
                    appsWithMicrophoneInfo.add(new AppDetails(appName, packageName, appIcon));
                }
            } catch (Exception ignored) {
                // Ignora le eccezioni (es. PackageManager.NameNotFoundException)
                // che possono verificarsi per pacchetti specifici.
            }
        }
        return appsWithMicrophoneInfo;
    }

    /**
     * Apre la schermata delle impostazioni di un'applicazione specifica.
     * @param context Contesto dell'applicazione.
     * @param packageName Nome del package dell'app.
     */
    public static void openAppSettings(Context context, String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", packageName, null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Intent fallbackIntent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(fallbackIntent);
        }
    }
}