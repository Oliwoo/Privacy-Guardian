package com.unict.mobile.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.unict.mobile.services.MicrophoneMonitorService;

import java.util.ArrayList;
import java.util.List;

/**
 * Uso: Classe di utility per per gestire i permessi necessari per l'applicazione
 */
public class PermissionManager {

    private static final String TAG = "PermissionManager";          // Definisco un TAG per semplificare la ricerca di possibili log su Logcat
    public static final int REQUEST_RUNTIME_PERMISSIONS = 100;      // ID richiesta permesso runtime (Notifiche, Accesso al Microfono, etc..) [Permesso runtime = permesso che può essere richiesto a runtime tramite popup di consenso]
    public static final int REQUEST_USAGE_STATS_PERMISSION = 101;   // ID richiesta permesso utilizzo delle statitiche d'uso
    public static final int REQUEST_ACCESSIBILITY_PERMISSION = 102; // ID richiesta permesso accessibilità del dispositivo

    private final Activity activity;                                // Istanza dell'activity chiamante
    private final PermissionCallback callback;                      // Istanza per definire i comportamenti in base agli eventi gestiti

    /**
     * Interfaccia di callback per notificare lo stato dei permessi.
     *
     * onAllPermissionGranted() -> Evento in cui tutti i permessi sono consentiti
     * onPermissionDenied() -> Evento in cui uno o più permessi runtime vengano rifiutati
     * onUsageStatsPermissionGranted -> Evento in cui il permesso di utilizzo dell'API di Statistiche d'uso venga consentito
     * onUsageStatsPermissionDenied() -> Evento in cui il permesso di utilizzo dell'API di Statistiche d'uso venga rifiutato
     * onAccessibilityPermissionGranted() -> Evento in cui il permesso di utilizzo dell'API Accessibilità venga consentito
     * onAccessibilityPermissionGranted() -> Evento in cui il permesso di utilizzo dell'API Accessibilità venga rifiutato
     */
    public interface PermissionCallback {
        void onAllPermissionsGranted();
        void onPermissionsDenied();
        void onUsageStatsPermissionGranted();
        void onUsageStatsPermissionDenied();
        void onAccessibilityPermissionGranted();
        void onAccessibilityPermissionDenied();
    }

    /**
     * Costruttore per il gestore dei permessi.
     * @param activity L'Activity chiamante.
     * @param callback Il callback per la gestione degli eventi sui permessi.
     */
    public PermissionManager(Activity activity, PermissionCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    private final String[] RUNTIME_PERMISSIONS = {                                                              // Definisco un Array di permessi richiesti
        Manifest.permission.RECORD_AUDIO,                                                                       // Permesso di accesso al microfono
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.POST_NOTIFICATIONS : null   // Se versione di android >= Tiramisù, permesso di invio notifiche
    };

    /**
     * Avvia il processo di richiesta di tutti i permessi necessari.
     * Questo metodo controlla e richiede i permessi di runtime, poi procede con
     * i permessi speciali (Usage Stats e Accessibilità).
     */
    public void requestAllNecessaryPermissions() {
        Log.d(TAG, "Avvio del processo di richiesta permessi.");                                                                    // Invio su Logcat un log per informare l'avvio del processo di richiesta dei permessi
        List<String> permissionsToRequest = new ArrayList<>();                                                                           // Creo una Lista di permessi (richiesti) ma che non sono stati consentiti

        for (String permission : RUNTIME_PERMISSIONS) {                                                                                  // Creo un ciclo che per ogni permesso runtime richiesto (RUNTIME_PERMISIONS) ne ottiene il nome
            if (permission != null && ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {    // A partire dal nome e activity chiamante verifico se il permesso non è stato già consentito
                permissionsToRequest.add(permission);                                                                                    // Se non consentito lo aggiungo alla lista dei permessi da richiedere
            }
        }

        if (!permissionsToRequest.isEmpty()) {                                                      // Verifico se ci sono permessi da richiedere
            ActivityCompat.requestPermissions(activity,                                             // In tal caso richiedo di consentire la lista dei permessi (non ancora consentiti)
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_RUNTIME_PERMISSIONS);
        } else {
            Log.d(TAG, "Tutti i permessi di runtime già concessi. Controllo Usage Stats.");    // Stampo su Logcat un log per notificare che tutti i permessi sono stati consentiti
            checkUsageStatsPermission();                                                            // Avvio il controllo sul permesso API Statistiche di utilizzo
        }
    }

    /**
     * Controlla se l'app ha il permesso "Accesso ai dati di utilizzo" e, in caso contrario, lo richiede.
     */
    public void checkUsageStatsPermission() {
        if (hasUsageStatsPermission()) {                                                                                                                        // Controllo se il permesso è stato già consentito
            Log.d(TAG, "Permesso Usage Stats già concesso. Controllo Accessibilità.");                                                                     // Stampo su Logcat un log per segnalare che il permesso è stato consentito
            callback.onUsageStatsPermissionGranted();                                                                                                           // Richieamo la colback per segnalare che il permesso è stato garantito
            checkAccessibilityPermission();
        } else {
            Log.d(TAG, "Permesso Usage Stats non concesso. Richiesta in corso...");                                                                        // Stampo su Logcat un log per segnalare che il permesso non è stato garantito
            Toast.makeText(activity, "Per favore, abilita 'Accesso ai dati di utilizzo' per questa app nelle Impostazioni.", Toast.LENGTH_LONG).show();    // Invio un Toast message all'utente per segnalare che il permesso non è stato garantito
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);                                                                                  // Creo l'intent per rimandare l'utente alla pagina delle impostazioni per garantire il permesso
            activity.startActivityForResult(intent, REQUEST_USAGE_STATS_PERMISSION);                                                                            // Avvio l'intent specificando la pagina delle impostazioni a cui rimandare
        }
    }

    /**
     * Verifica se l'app ha il permesso "Accesso ai dati di utilizzo".
     * @return true se il permesso è concesso, false altrimenti.
     */
    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);  // Ottengo il manager per gestire il servizio di AppOpsManager (Gestore dei permessi)
        if (appOps == null) return false;                                                           // Se tale manager è nullo vuol dire che l'app non ha accesso al manager => non può usare l'API

        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,                       // Tramite AppOpsManager richiedo lo status del permesso per l'uso dell'API di Statistiche d'utilizzo
                android.os.Process.myUid(), activity.getPackageName());                             // Specifico il processo e il package name su cui effettuare il controllo
        return mode == AppOpsManager.MODE_ALLOWED;                                                  // Ritorno un booleano per specificare se il permesso è stato garantito o no
    }

    /**
     * Controlla se il servizio di accessibilità dell'app è abilitato e, in caso contrario, lo richiede.
     */
    public void checkAccessibilityPermission() {
        if (isAccessibilityServiceEnabled()) {                                                      // Verifico se il permesso per l'uso dell'API di accessibilità sia stato garantito
            Log.d(TAG, "Servizio di accessibilità già abilitato.");                            // Stampo su Logcat un log per segnalare che il permesso è garantito
            callback.onAccessibilityPermissionGranted();                                            // Richiamo la callback per segnalare che il permesso è stato garantito
            callback.onAllPermissionsGranted(); // Tutti i permessi necessari sono ora concessi.    // Richiamo la callback per segnalare che tutti i permessi sono stati garantiti
        } else {
            Log.d(TAG, "Servizio di accessibilità non abilitato. Richiesta in corso...");      // Stampo su Logcat un log per segnalare che il permesso non è stato garantito
            Toast.makeText(activity, "Per favore, abilita il servizio di accessibilità per questa app nelle Impostazioni.", Toast.LENGTH_LONG).show(); // Invio un Toast message all'utente per segnalare che il permesso non è stato garantito
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);                     // Creo un intent per portare l'utente alla pagina delle impostazioni per consentire il permesso
            activity.startActivityForResult(intent, REQUEST_ACCESSIBILITY_PERMISSION);              // Avvio l'intent specificando la pagina delle impostazioni a cui rimandare
        }
    }

    /**
     * Verifica se il servizio di accessibilità dell'app è abilitato.
     * @return true se il servizio è abilitato, false altrimenti.
     */
    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) activity.getSystemService(Context.ACCESSIBILITY_SERVICE);                          // Ottengo l'AccessibilityManager per gestire i permessi di accessibilità
        if(am == null)  return false;                                                                                                       // Se nullo l'app non ha accesso all'uso del manager => non può utilizzare i permessi di accessibilità

        String expectedServiceId = activity.getPackageName() + "/.services." + MicrophoneMonitorService.class.getSimpleName();              // Ottengo l'id della classe di cui l'app necessita il permesso di accessibilità, nel nostro caso la classe del servizio di monitoraggio del microfono

        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);  // Ottengo la lista di tutti gli id delle classi per ogni packagename che hanno il permesso accessibilità
        for(AccessibilityServiceInfo service : enabledServices){                                                                            // Scorro ogni elemento della lista
            if(expectedServiceId.equals(service.getId())) {                                                                                 // Se nella lista c'è l'id della nostra classe allora il permesso è garantito
                Log.d(TAG, "Servizio di accessibilità trovato e abilitato: " + service.getId());                                       // Stampo su Logcat un log per segnalare che il permesso è garantito
                return true;                                                                                                                // Ritorno true per indicare al chiamante che è stato garantito il permesso
            }
        }
        Log.d(TAG, "Servizio di accessibilità non trovato o non abilitato.");                                                          // Stampo su Logcat un log per segnalare che il permesso non è stato garantito
        return false;                                                                                                                       // Ritorno false per indicare al chiamante che non è stato garantito il permesso
    }

    /**
     * Gestisce il risultato della richiesta di permessi di runtime.
     * Questo metodo deve essere chiamato da onRequestPermissionsResult() dell'Activity.
     * @param requestCode Il codice della richiesta.
     * @param permissions I permessi richiesti.
     * @param grantResults I risultati dei permessi.
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) { // Metodo per catturare l'evento di risposta del popup dei permessi
        if(requestCode == REQUEST_RUNTIME_PERMISSIONS){                                             // Verifica che il codice della risposta è inerente ad una richiesta di permessi runtime
            boolean allGranted = true;                                                              // Creo una variabile di flag per verificare che tutti i permessi richiesti (Array di permessi definito a riga 66) sono stati consentiti
            for(int result : grantResults){                                                         // Scorro tutti i risulati
                if(result != PackageManager.PERMISSION_GRANTED){                                    // Verifico se uno specifico risultato non è stato garantito
                    allGranted = false;                                                             // Modifico il flag per segnalare che almeno un permesso non è stato consentito
                    break;                                                                          // Mi fermo di scorrere visto che già sappiamo che uno non è stato consetito
                }
            }

            if(allGranted){                                                                         // Verifico il flag per capire se sono stati tutti consentiti
                Log.d(TAG, "Tutti i permessi di runtime concessi. Controllo Usage Stats.");    // Stampo su Logcat un log per segnalare che tutti i permessi runtime sono stati consentiti
                checkUsageStatsPermission();                                                        // Avvio il processo di controllo del permesso all'uso dell'API di Statistiche di utilizzo
            }else{
                Log.d(TAG, "Alcuni permessi di runtime negati.");                              // Stampo su Logcat un log per segnalare che almeno un permesso non è stato consentito
                callback.onPermissionsDenied();                                                     // Richiamo la callback per segnalare che almeno un permesso non è stato consentito
                Toast.makeText(activity, "Permessi essenziali non concessi. Impossibile avviare il monitoraggio.", Toast.LENGTH_SHORT).show(); // Invio un Toast message all'utente per notificare che almeno un permesso non è stato consentito
            }
        }
    }

    /**
     * Gestisce il risultato delle Activity avviate per i permessi speciali.
     * Questo metodo deve essere chiamato da onActivityResult() dell'Activity.
     * @param requestCode Il codice della richiesta.
     * @param resultCode Il codice del risultato.
     * @param data L'Intent con i dati del risultato.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {                            // Metodo per catturare l'evento di ritorno dalle impostazioni (es dopo aver garantito un permesso (manuale) verifico se il permesso è stato consentito o meno)
        if (requestCode == REQUEST_USAGE_STATS_PERMISSION) {                                                // Verifico che il codice della richiesta riguarda il permesso per l'uso dell'API di Statistiche d'utilizzo
            if (hasUsageStatsPermission()) {                                                                // Verifico se il permesso è stato consentito
                Log.d(TAG, "Permesso Usage Stats concesso dalle impostazioni.");                       // Stampo su Logcat un log per segnalare che il permesso è stato consentito
                callback.onUsageStatsPermissionGranted();                                                   // Richiamo la callback per segnalare che il permesso è stato consentito
                checkAccessibilityPermission();                                                             // Avvio il processo di controllo del permesso di accessibilità
            } else {
                Log.d(TAG, "Permesso Usage Stats ancora negato dopo il ritorno dalle impostazioni.");  // Stampo su Logcat un log per segnalare che il permesso non è stato consentito
                callback.onUsageStatsPermissionDenied();                                                    // Richiamo la callback per segnalare che il permesso è stato negato
                callback.onPermissionsDenied();                                                             // Richiamo la callback per segnalare che tutti i permessi sono stati negati
                Toast.makeText(activity, "Permesso 'Accesso ai dati di utilizzo' non concesso. Impossibile monitorare le app in primo piano.", Toast.LENGTH_SHORT).show(); // Invio un Toast message all'utente per segnalare che il permesso non è stato consentito
            }
        } else if (requestCode == REQUEST_ACCESSIBILITY_PERMISSION) {                                           // Verifico che il codice della richiesta riguarda il permesso di accessibilità
            if (isAccessibilityServiceEnabled()) {                                                              // Verifico se il permesso è stato consentito
                Log.d(TAG, "Permesso Accessibilità concesso dalle impostazioni.");                         // Stampo su Logcat un log per segnalare che il permesso è stato consentit
                callback.onAccessibilityPermissionGranted();                                                    // Richiamo la callback per segnalare che il permesso è stato consentito
                callback.onAllPermissionsGranted();                                                             // Richiamo la callback per segnalare che tutti i permessi sono stati concessi
            } else {
                Log.d(TAG, "Permesso Accessibilità ancora negato dopo il ritorno dalle impostazioni.");    // Stampo su Logcat un log per segnalare che il permesso non è stato consentito
                callback.onAccessibilityPermissionDenied();                                                     // Richiamo la callback per segnalare che il permesso è stato negato
                callback.onPermissionsDenied();                                                                 // Richiamo la callback per segnalare che tutti i permessi sono stati negati
                Toast.makeText(activity, "Servizio di accessibilità non abilitato. Funzionalità limitata.", Toast.LENGTH_SHORT).show(); // Invio un Toast message all'utente per segnalare che il permesso non è stato consentito
            }
        }
    }
}