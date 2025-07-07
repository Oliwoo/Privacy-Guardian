package com.unict.mobile.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.unict.mobile.adapters.models.ItemDateStoredLog;
import com.unict.mobile.models.AudioTypeLog;
import com.unict.mobile.models.MicrophoneAccessLog;

/**
 * Gestisce il salvataggio, il recupero e la gestione dei log di accesso al microfono
 * usando SharedPreferences.
 */
public class MicrophoneAccessLogManager {
    private static MicrophoneAccessLogManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private static final String PREF_NAME = "MicrophoneLogPrefs";
    private static final String LOGS_KEY_PREFIX = "logs_";
    private static final String TOTAL_LOGS_KEY = "total_logs";
    private static final long kilo = 1024;
    private static final long mega = kilo * kilo;
    private static final long giga = mega * kilo;
    private static final long tera = giga * kilo;

    /**
     * Costruttore privato per il pattern Singleton.
     * @param context Contesto dell'applicazione.
     */
    private MicrophoneAccessLogManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Restituisce l'istanza Singleton di MicrophoneAccessLogManager.
     * @param context Contesto dell'applicazione.
     * @return L'istanza del gestore.
     */
    public static synchronized MicrophoneAccessLogManager getInstance(Context context) {
        if (instance == null) instance = new MicrophoneAccessLogManager(context);
        return instance;
    }

    /**
     * Genera la chiave di SharedPreferences per una data specifica.
     * @param date La data.
     * @return Stringa della chiave.
     */
    private String getPrefixFromDate(@NonNull Date date){
        return LOGS_KEY_PREFIX + DateTimeUtils.formatDate(date);
    }

    /**
     * Recupera la lista di log di accesso al microfono per una data specifica.
     * @param date La data per cui recuperare i log.
     * @return Lista di MicrophoneAccessLog.
     */
    public List<MicrophoneAccessLog> getLogsForDate(@NonNull Date date) {
        String logsJson = sharedPreferences.getString(getPrefixFromDate(date), "[]");
        Type listType = new TypeToken<List<MicrophoneAccessLog>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(logsJson, listType);
    }

    /**
     * Aggiunge un nuovo log di accesso al microfono.
     * @param log Il log da aggiungere.
     */
    public void addLog(MicrophoneAccessLog log) {
        Date date = new Date(log.getStartDateMillis());
        List<MicrophoneAccessLog> logsForDate = getLogsForDate(date);

        logsForDate.add(0, log);

        Gson gson = new Gson();
        String logsJson = gson.toJson(logsForDate);
        editor.putString(getPrefixFromDate(date), logsJson);

        int totalLogs = getTotalLogs();
        editor.putInt(TOTAL_LOGS_KEY, totalLogs + 1);

        editor.apply();
    }

    /**
     * Restituisce il numero totale di log memorizzati.
     * @return Numero totale di log.
     */
    public int getTotalLogs() {
        return sharedPreferences.getInt(TOTAL_LOGS_KEY, 0);
    }

    /**
     * Restituisce i log di accesso al microfono per la data odierna.
     * @return Lista di MicrophoneAccessLog per oggi.
     */
    public List<MicrophoneAccessLog> getLogsForToday() {
        return getLogsForDate(new Date());
    }

    /** Rimuove tutti i log di accesso al microfono memorizzati. */
    public void clearAllLogs() {
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            if (entry.getKey().startsWith(LOGS_KEY_PREFIX)) editor.remove(entry.getKey());
        }
        editor.putInt(TOTAL_LOGS_KEY, 0);
        editor.apply();
    }

    /**
     * Rimuove i log di accesso al microfono per una data specifica.
     * @param date La data per cui cancellare i log.
     */
    public void clearLogForDate(@NonNull Date date) {
        int totalLogs = getTotalLogs();
        int removedLogs = getLogsForDate(date).size();
        editor.remove(getPrefixFromDate(date));
        editor.putInt(TOTAL_LOGS_KEY, totalLogs - removedLogs);
        editor.apply();
    }

    /** Rimuove i log di accesso al microfono per la data odierna. */
    public void clearLogsForToday() {
        clearLogForDate(new Date());
    }

    /**
     * Calcola il livello di rischio massimo registrato per una data specifica.
     * @param date La data per cui calcolare il livello di rischio.
     * @return Il livello di rischio più alto.
     */
    public AudioTypeLog.RiskLevel getRiskLevelForDate(@NonNull Date date) {
        AtomicReference<AudioTypeLog.RiskLevel> riskLevel = new AtomicReference<>(AudioTypeLog.RiskLevel.LOW);
        getLogsForDate(date).forEach(log ->
                log.getAudioTypeHistory().forEach(audioType ->
                        riskLevel.set(AudioTypeLog.RiskLevel.max(riskLevel.get(), audioType.getPrivacyRiskLevel()))
                )
        );
        return riskLevel.get();
    }

    /**
     * Calcola il livello di rischio massimo registrato per la data odierna.
     * @return Il livello di rischio più alto per oggi.
     */
    public AudioTypeLog.RiskLevel getRiskLevelForToday() {
        return getRiskLevelForDate(new Date());
    }

    /**
     * Stima la dimensione totale in byte dei log di accesso al microfono memorizzati.
     * @return Dimensione in byte.
     */
    public double getStoredDataSize() {
        long size = 0L;
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            if (entry.getKey().startsWith(LOGS_KEY_PREFIX)) {
                size += sizeOfValue(entry.getValue());
            }
        }
        return size;
    }

    /**
     * Restituisce la dimensione stimata dei dati memorizzati come stringa formattata (es. "1.23 MB").
     * @return Stringa della dimensione dei dati.
     */
    @SuppressLint("DefaultLocale")
    public String getStoredDataSizeString() {
        double size = getStoredDataSize();

        double kb = size / kilo;
        double mb = size / mega;
        double gb = size / giga;
        double tb = size / tera;

        if (size < kilo) {
            return size + " Bytes";
        } else if (size < mega) {
            return String.format("%.2f", kb) + " KB";
        } else if (size < giga) {
            return String.format("%.2f", mb) + " MB";
        } else if (size < tera) {
            return String.format("%.2f", gb) + " GB";
        } else {
            return String.format("%.2f", tb) + " TB";
        }
    }

    /**
     * Calcola la dimensione in byte di un oggetto convertito in stringa UTF-8.
     * @param value L'oggetto di cui calcolare la dimensione.
     * @return Dimensione in byte.
     */
    public long sizeOfValue(Object value) {
        if (value == null) return 0;
        String data = String.valueOf(value);
        return data.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * Restituisce la data dell'ultimo log memorizzato per oggi, o un messaggio se non ci sono log.
     * @return Data dell'ultimo log come stringa o messaggio.
     */
    public String getLastStoredLogDateStr() {
        List<MicrophoneAccessLog> todayLogs = getLogsForToday();
        if (!todayLogs.isEmpty()) {
            MicrophoneAccessLog log = todayLogs.get(0);
            if (log != null) return log.getDateStr();
        }
        return "No log generated today";
    }

    /**
     * Recupera un log di accesso al microfono tramite il suo ID (startDateMillis).
     * @param id L'ID (startDateMillis) del log.
     * @return Il MicrophoneAccessLog corrispondente, o null se non trovato.
     */
    public MicrophoneAccessLog getLogByID(long id) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<MicrophoneAccessLog>>() {}.getType();

        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            if (entry.getKey().startsWith(LOGS_KEY_PREFIX)) {
                String logsJson = (String) entry.getValue();
                List<MicrophoneAccessLog> logs = gson.fromJson(logsJson, listType);
                for (MicrophoneAccessLog log : logs) {
                    if (log.getStartDateMillis() == id) {
                        return log;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Restituisce una lista di riepiloghi dei log, raggruppati per data.
     * @return Lista di ItemDateStoredLog con data e conteggio dei log.
     * @throws Exception Se si verifica un errore durante il parsing della data dalla chiave.
     */
    public List<ItemDateStoredLog> getAllLogsList() throws Exception {
        List<ItemDateStoredLog> list = new ArrayList<>();
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            if (entry.getKey().startsWith(LOGS_KEY_PREFIX)) {
                String dateStr = entry.getKey().replace(LOGS_KEY_PREFIX, "");
                List<MicrophoneAccessLog> logs = getLogsForDate(DateTimeUtils.parseDate(dateStr));
                list.add(new ItemDateStoredLog(DateTimeUtils.parseDate(dateStr), logs.size()));
            }
        }
        return list.stream().sorted(Comparator.comparing(ItemDateStoredLog::getDate).reversed()).collect(Collectors.toList());
    }
}