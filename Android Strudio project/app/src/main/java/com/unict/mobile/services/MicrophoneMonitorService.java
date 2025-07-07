package com.unict.mobile.services;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioRecordingConfiguration;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.unict.mobile.models.ApplicationAccessLog;
import com.unict.mobile.models.ApplicationItem;
import com.unict.mobile.models.AudioTypeLog;
import com.unict.mobile.models.MicrophoneAccessLog;
import com.unict.mobile.tflite.AudioClassification;
import com.unict.mobile.utils.MicrophoneAccessLogManager;
import com.unict.mobile.utils.PackageResolverUtil;

import org.tensorflow.lite.support.label.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Dichiarazione della classe principale del servizio, che estende AccessibilityService.
// Questo servizio monitora l'uso del microfono e rileva potenziali anomalie.
public class MicrophoneMonitorService extends AccessibilityService {

    // Costanti per i tag di log, gli ID delle notifiche e i canali.
    private static final String TAG = "MicrophoneMonitorService";
    private static final String DEFAULT_CHANNEL_ID = "mic_monitor_channel"; // Canale per notifiche standard.
    private static final String ANOMALY_CHANNEL_ID = "mic_anomaly_channel"; // Canale per notifiche di anomalia.
    private static final int NOTIFICATION_ID = 1; // ID per la notifica di monitoraggio.
    private static final int ANOMALY_NOTIFICATION_ID = 2; // ID per la notifica di anomalia.
    private static final long ANOMALY_ACCESS_TIMEOUT_MS = 10 * 1000; // Durata (ms) per considerare un accesso anomalo prolungato.

    // Variabili di stato per tracciare l'accesso corrente al microfono e il tipo di audio.
    private MicrophoneAccessLog currentMicrophoneAccessLog = new MicrophoneAccessLog();
    private AudioTypeLog currentAudioTypeLog = new AudioTypeLog();
    private boolean isClassificationActive = false; // Indica se la classificazione audio è in corso.

    // Oggetti per la gestione degli eventi, la classificazione e i log.
    private Handler anomalyNotificationHandler; // Gestore per schedulare avvisi di anomalia.
    private AudioClassification audioClassification; // Oggetto per classificare l'audio.
    private MicrophoneAccessLogManager microphoneMicrophoneAccessLogManager; // Gestore per salvare i dati di accesso al microfono.

    private String latestGesturePackage = null; // Il pacchetto dell'app dove è avvenuta l'ultima interazione utente.

    // Runnable che verifica e notifica se un accesso anomalo al microfono è prolungato.
    private final Runnable anomalyNotificationRunnable = () -> {
        if(currentMicrophoneAccessLog.isAnomaly() && (System.currentTimeMillis() - currentMicrophoneAccessLog.getStartDateMillis()) >= ANOMALY_ACCESS_TIMEOUT_MS) {
            notifyAnomalyInProgress(); // Mostra una notifica di anomalia in corso.
        }
    };

    // Callback per intercettare i cambiamenti nella configurazione di registrazione audio (Android Q+).
    // Questo è il modo principale per sapere quando il microfono viene attivato o disattivato.
    private final AudioManager.AudioRecordingCallback audioRecordingCallback = new AudioManager.AudioRecordingCallback(){
        @Override
        public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs){
            // Se la classificazione è attiva e c'è solo una configurazione (indicando forse un cambio o stop implicito), la ferma.
            if(isClassificationActive && configs.size() == 1){stopClassification(); configs.clear();}
            // Se il microfono è stato attivato e non c'era un log in corso.
            if(!configs.isEmpty() && currentMicrophoneAccessLog.getStartDateMillis() == 0){
                handleMicrophoneActivation(configs); // Gestisce l'inizio dell'uso del microfono.
            }
            // Se il microfono è stato disattivato e c'era un log in corso.
            else if(configs.isEmpty() && currentMicrophoneAccessLog.getStartDateMillis() > 0){
                handleMicrophoneDeactivation(); // Gestisce la fine dell'uso del microfono.
            }
        }
    };

    /**
     * Chiamato quando il servizio viene creato. Inizializza tutte le funzionalità di monitoraggio.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Avvia il servizio in foreground per garantirne l'esecuzione continua e mostra una notifica.
        startForeground(NOTIFICATION_ID, buildDefaultNotification("Monitoraggio in avvio..."));

        initAudioConfigCallback();      // Inizializza il listener per l'uso del microfono.
        initAnomalyEventHandler();      // Inizializza il gestore per gli avvisi di anomalia.
        initClassification();           // Inizializza il modello di classificazione audio.
        createNotificationChannels();   // Crea i canali di notifica (necessari da Android 8.0 Oreo).
    }

    /**
     * Chiamato quando il servizio di accessibilità è connesso. Configura gli eventi UI da monitorare.
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // Specifica gli eventi di UI che il servizio deve intercettare.
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED // Cambi di app in primo piano.
                | AccessibilityEvent.TYPE_TOUCH_INTERACTION_START // Inizio di interazioni touch.
                | AccessibilityEvent.TYPE_TOUCH_INTERACTION_END // Fine di interazioni touch.
                | AccessibilityEvent.TYPE_VIEW_CLICKED; // Click su elementi.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        info.notificationTimeout = 100; // Ridotto timeout tra eventi simili.
        setServiceInfo(info); // Applica le impostazioni al servizio.
    }

    /**
     * Chiamato ogni volta che viene rilevato un evento di accessibilità.
     * Utilizzato per identificare l'app in primo piano e rilevare anomalie basate sulle interazioni utente.
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        // Se l'evento indica un cambio di finestra (app in primo piano).
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.getPackageName() != null) {
            String foregroundPackage = event.getPackageName().toString();
            latestGesturePackage = foregroundPackage; // Aggiorna l'ultima app vista dall'utente.

            // Se il microfono è attivo, associa l'app corrente al log di accesso.
            if (currentMicrophoneAccessLog.getStartDateMillis() > 0) {
                ApplicationItem app = PackageResolverUtil.getApplicationItem(getBaseContext(), foregroundPackage);
                currentMicrophoneAccessLog.addDetectedApp(new ApplicationAccessLog(app.getIcon(), app.getAppName(), app.getAppPackage(), System.currentTimeMillis(), 0L));
                currentAudioTypeLog.addDetectedApp(foregroundPackage);
            }
            Log.d(TAG, "App in foreground: " + foregroundPackage);

            // Se l'evento è un'interazione touch o un click.
        } else if ((event.getEventType() == AccessibilityEvent.TYPE_TOUCH_INTERACTION_START
                || event.getEventType() == AccessibilityEvent.TYPE_TOUCH_INTERACTION_END
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) && event.getPackageName() != null) {
            String packageName = event.getPackageName().toString();
            latestGesturePackage = packageName; // Aggiorna l'app dove è avvenuta la gesture.

            String foregroundPackage = PackageResolverUtil.getForegroundAppPackageByUsageStats(getBaseContext());
            // Rileva un'anomalia se la gesture non avviene nell'app in primo piano mentre il microfono è attivo.
            if (!Objects.equals(packageName, foregroundPackage) && currentMicrophoneAccessLog.getStartDateMillis() > 0) {
                currentMicrophoneAccessLog.setAnomaly(true); // Segna l'accesso come anomalo.
                Log.w(TAG, "Anomalia: gesture fuori dall'app foreground");
            }
        }
    }

    /**
     * Inizializza la callback per il monitoraggio dell'audio.
     */
    private void initAudioConfigCallback(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        microphoneMicrophoneAccessLogManager = MicrophoneAccessLogManager.getInstance(this);

        // Registra la callback solo su Android Q (API 29) e successivi.
        if(audioManager != null)
            audioManager.registerAudioRecordingCallback(audioRecordingCallback, null);
    }

    /**
     * Inizializza l'handler per schedulare gli avvisi di anomalia.
     */
    private void initAnomalyEventHandler(){
        anomalyNotificationHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Gestisce l'attivazione del microfono: avvia la classificazione, aggiorna i log e le notifiche.
     * @param configs Le configurazioni di registrazione audio attive.
     */
    private void handleMicrophoneActivation(List<AudioRecordingConfiguration> configs){
        String foregroundPackage = PackageResolverUtil.getForegroundAppPackageByUsageStats(getBaseContext());
        ApplicationItem app = new ApplicationItem(null,"(Unknown)","(Unknown)");
        if(foregroundPackage!=null) app = PackageResolverUtil.getApplicationItem(getBaseContext(),foregroundPackage);
        long now = System.currentTimeMillis();

        startClassification(); // Avvia la classificazione audio.

        currentMicrophoneAccessLog.setStartDateMillis(now); // Imposta l'inizio dell'accesso.
        currentMicrophoneAccessLog.addDetectedApp(new ApplicationAccessLog(app.getIcon(),app.getAppName(),app.getAppPackage(),now,0L));
        // Se non è già un'anomalia, verifica se l'evento attuale è anomalo.
        if(!currentMicrophoneAccessLog.isAnomaly()) currentMicrophoneAccessLog.setAnomaly(isAnomalyEvent(foregroundPackage));

        String appName = app.getAppName();
        String status = "Microfono ATTIVO! App: " + (appName != null ? appName : foregroundPackage);

        // Se è un'anomalia, schedula un avviso per l'accesso prolungato.
        if(currentMicrophoneAccessLog.isAnomaly()){
            status += " (Potenziale Anomalia)";
            anomalyNotificationHandler.removeCallbacks(anomalyNotificationRunnable); // Rimuove eventuali callback precedenti.
            anomalyNotificationHandler.postDelayed(anomalyNotificationRunnable, ANOMALY_ACCESS_TIMEOUT_MS); // Schedula il nuovo avviso.
        }else{
            anomalyNotificationHandler.removeCallbacks(anomalyNotificationRunnable); // Rimuove l'avviso se non è un'anomalia.
        }

        updateDefaultNotification(status); // Aggiorna la notifica di monitoraggio.
        Log.d(TAG, status);
    }

    /**
     * Determina se un evento di accesso al microfono è da considerarsi anomalo.
     * @param packageName Il nome del pacchetto dell'app che usa il microfono.
     * @return true se è un'anomalia, false altrimenti.
     */
    private boolean isAnomalyEvent(String packageName) {
        Context ctx = getBaseContext();
        // Criteri di anomalia:
        return !PackageResolverUtil.hasMicrophonePermission(ctx, packageName) // L'app non ha il permesso microfono.
                || !PackageResolverUtil.hasMicrophoneAccess(ctx, packageName) // L'app non ha accesso attivo al microfono (potrebbe essere ridondante).
                || PackageResolverUtil.isHomeApp(ctx, packageName) // L'app è la home screen.
                || PackageResolverUtil.isScreenOff(ctx) // Lo schermo è spento.
                || Objects.equals(latestGesturePackage,packageName); // L'ultima gesture è avvenuta in questa app (potenziale falso positivo se l'app è in background ma legittima).
    }

    /**
     * Gestisce la disattivazione del microfono: aggiorna la notifica e salva il log.
     */
    private void handleMicrophoneDeactivation() {
        Log.d(TAG,"Microfono NON attivo");
        updateDefaultNotification("Microfono NON attivo."); // Aggiorna la notifica.
        addMicrophoneAccessLog(); // Salva il log dell'accesso al microfono.
    }

    /**
     * Completa e salva il log dell'accesso corrente al microfono.
     */
    private void addMicrophoneAccessLog(){
        long now = System.currentTimeMillis();
        currentMicrophoneAccessLog.setEndDateMillis(now); // Imposta l'ora di fine dell'accesso.
        if(currentMicrophoneAccessLog.getLastDetectedApp()!=null){
            currentMicrophoneAccessLog.getLastDetectedApp().setEndDateMillis(now); // Imposta l'ora di fine per l'ultima app rilevata.
        }
        microphoneMicrophoneAccessLogManager.addLog(currentMicrophoneAccessLog); // Aggiunge il log al gestore.
        currentMicrophoneAccessLog = new MicrophoneAccessLog(); // Resetta il log corrente per il prossimo accesso.
        currentAudioTypeLog = new AudioTypeLog(); // Resetta il log del tipo audio.
        anomalyNotificationHandler.removeCallbacksAndMessages(null); // Rimuove callback di anomalia pendenti.
        Log.d(TAG, "Microphone log saved.");
    }

    /**
     * Mostra una notifica e un Toast quando viene rilevata un'anomalia prolungata.
     */
    private void notifyAnomalyInProgress() {
        String msg = "Accesso anomalo al microfono da app in background da più di " + (ANOMALY_ACCESS_TIMEOUT_MS / 1000) + "s";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); // Mostra un messaggio Toast.
        buildAndShowAnomalyNotification(msg); // Crea e mostra la notifica di anomalia.
    }

    /**
     * Inizializza il classificatore audio.
     */
    private void initClassification() {
        Log.d(TAG, "Inizializzazione classificatore audio nel servizio...");
        // Crea un'istanza di AudioClassification con un listener per i risultati.
        audioClassification = new AudioClassification(getApplicationContext(), new AudioClassification.AudioClassificationListener() {
            @Override
            public void onError(String s) {
                Log.e(TAG, "Errore nella classificazione: " + s); // Logga errori di classificazione.
            }

            @Override
            public void onResult(List<Category> categories, long inferenceTime) {
                Log.d(TAG, "Rilevamento risultati dalla classificazione...");
                // Elabora i risultati della classificazione.
                for (Category category : categories) {
                    if(category.getScore() < 0.7) continue; // Ignora risultati con bassa confidenza.

                    Log.d(TAG,"BEFORE AudioTypes: " + currentMicrophoneAccessLog.getAudioTypeHistory().size());

                    currentAudioTypeLog.setType(category.getLabel()); // Imposta il tipo di audio rilevato.
                    currentAudioTypeLog.setEndDateMillis(System.currentTimeMillis()); // Imposta la fine del rilevamento.

                    AudioTypeLog logToAdd = getAudioTypeLog(category); // Ottiene il log del tipo di audio.

                    currentMicrophoneAccessLog.addDetectedAudioType(logToAdd); // Aggiunge il tipo di audio al log principale.
                    currentAudioTypeLog.setStartDateMillis(currentAudioTypeLog.getEndDateMillis()); // Prepara il log per il prossimo intervallo.

                    Log.d(TAG,"AFTER AudioTypes: " + currentMicrophoneAccessLog.getAudioTypeHistory().size());
                    Log.d(TAG, "Categoria: " + category.getLabel() + " con confidenza " + category.getScore());
                }
            }

            /**
             * Crea un oggetto AudioTypeLog basandosi sul risultato della classificazione.
             * @param category La categoria classificata.
             * @return Un oggetto AudioTypeLog.
             */
            @NonNull
            private AudioTypeLog getAudioTypeLog(Category category) {
                AudioTypeLog logToAdd;
                // Se la confidenza è alta, usa l'etichetta classificata, altrimenti "(Unknown)".
                if(category.getScore() > 0.75){
                    logToAdd = new AudioTypeLog(
                            currentAudioTypeLog.getType(),
                            currentAudioTypeLog.getStartDateMillis(),
                            currentAudioTypeLog.getEndDateMillis(),
                            new ArrayList<>(currentAudioTypeLog.getDetectedApps())
                    );
                }else{
                    logToAdd = new AudioTypeLog(
                            "(Unknown)",
                            currentAudioTypeLog.getStartDateMillis(),
                            currentAudioTypeLog.getEndDateMillis(),
                            new ArrayList<>(currentAudioTypeLog.getDetectedApps())
                    );
                }
                return logToAdd;
            }
        });
    }

    /**
     * Avvia il processo di classificazione audio.
     */
    private void startClassification(){
        try{
            if(audioClassification != null && !isClassificationActive){
                audioClassification.startAudioClassification(); // Avvia la classificazione.
                currentAudioTypeLog.setStartDateMillis(System.currentTimeMillis()); // Imposta l'inizio per il log del tipo audio.
                isClassificationActive = true;
            }
        }catch (Exception e) {
            Log.e(TAG, "Errore nella classificazione periodica: " + e.getMessage());
        }
    }

    /**
     * Ferma il processo di classificazione audio.
     */
    private void stopClassification() {
        if(audioClassification != null) audioClassification.stopAudioClassification(); // Ferma la classificazione.
        isClassificationActive = false;
    }

    /**
     * Aggiorna la notifica di monitoraggio predefinita.
     * @param status Il testo di stato da visualizzare.
     */
    private void updateDefaultNotification(String status) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildDefaultNotification(status)); // Aggiorna la notifica esistente.
        }
    }

    /**
     * Costruisce la notifica di monitoraggio predefinita.
     * @param status Il testo di stato.
     * @return L'oggetto Notification.
     */
    private Notification buildDefaultNotification(String status) {
        return new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                .setContentTitle("Monitoraggio Microfono")
                .setContentText(status)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now) // Icona piccola.
                .setPriority(NotificationCompat.PRIORITY_LOW) // Priorità bassa per notifiche persistenti.
                .setOngoing(true) // Rende la notifica persistente (per foreground service).
                .build();
    }

    /**
     * Costruisce e mostra la notifica di anomalia.
     * @param message Il messaggio di anomalia.
     */
    private void buildAndShowAnomalyNotification(String message) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            Notification notification = new NotificationCompat.Builder(this, ANOMALY_CHANNEL_ID)
                    .setContentTitle("Allarme Microfono Anomalo!")
                    .setContentText(message)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert) // Icona di avviso.
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Priorità alta per avvisi urgenti.
                    .setCategory(NotificationCompat.CATEGORY_ALARM) // Categoria di allarme.
                    .setAutoCancel(true) // La notifica si chiude automaticamente al tap.
                    .build();
            manager.notify(ANOMALY_NOTIFICATION_ID, notification); // Mostra la notifica di anomalia.
        }
    }

    /**
     * Crea i canali di notifica.
     */
    private void createNotificationChannels() {
        NotificationChannel defaultChannel = new NotificationChannel(DEFAULT_CHANNEL_ID, "Monitoraggio Microfono", NotificationManager.IMPORTANCE_LOW);
        defaultChannel.setDescription("Notifiche per uso del microfono");

        NotificationChannel anomalyChannel = new NotificationChannel(ANOMALY_CHANNEL_ID, "Anomalie Microfono", NotificationManager.IMPORTANCE_HIGH);
        anomalyChannel.setDescription("Allarmi per accesso anomalo al microfono");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(defaultChannel); // Crea il canale predefinito.
            manager.createNotificationChannel(anomalyChannel); // Crea il canale di anomalia.
        }
    }

    /**
     * Metodo chiamato quando un Intent viene inviato al servizio.
     * Restituisce START_STICKY per indicare che il sistema dovrebbe riavviare il servizio se viene terminato.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY; // Il servizio verrà ricreato se ucciso dal sistema.
    }

    /**
     * Chiamato quando il servizio sta per essere distrutto. Pulisce le risorse.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopClassification(); // Ferma la classificazione audio.

        // Rimuove tutte le notifiche del servizio.
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
            manager.cancel(ANOMALY_NOTIFICATION_ID);
        }
    }

    /**
     * Metodo astratto di AccessibilityService, deve essere implementato.
     * Chiamato quando il servizio viene interrotto dal sistema.
     */
    @Override
    public void onInterrupt() {}

}