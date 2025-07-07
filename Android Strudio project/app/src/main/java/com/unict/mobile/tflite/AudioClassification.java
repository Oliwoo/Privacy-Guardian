package com.unict.mobile.tflite;

import android.content.Context;
import android.media.AudioRecord;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier.AudioClassifierOptions;
import org.tensorflow.lite.task.core.BaseOptions;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Classe per la classificazione dei suoni mediante modello TFLite "yamnet-classification"
 * Questa classe gestisce l'intero processo di classificazione audio, dall'inizializzazione del modello
 * alla registrazione e analisi dei suoni.
 */
public class AudioClassification {
    /**
     * <b>Interfaccia di gestione eventi di classificazione</b>
     * <p>
     * Questa interfaccia permette di implementare dei comportamenti in base al risultato della
     * classificazione, gestendo possibili errori (evento <code>onError</code>) o risultati (evento <code>onResult</code>).
     * </p>
     */
    public interface AudioClassificationListener {
        /**
         * Evento chiamato quando il classificatore incontra un errore durante la classificazione.
         * @param s Il messaggio di errore.
         */
        void onError(String s);

        /**
         * Evento chiamato quando il classificatore ottiene dei risultati.
         * @param categories Una lista delle categorie di suoni rilevati.
         * @param inferenceTime Il tempo impiegato per l'inferenza (classificazione).
         */
        void onResult(List<Category> categories, long inferenceTime);
    }

    // Costanti per la configurazione del classificatore
    public static final float DISPLAY_THRESHOLD = 0.3f;                     // Soglia minima per visualizzare un risultato di classificazione
    public static final int DEFAULT_NUM_OF_RESULTS = 10;                    // Numero massimo di risultati da ottenere
    public static final float DEFAULT_OVERLAP_VALUE = 0.5f;                 // Valore di sovrapposizione tra segmenti audio analizzati
    private static final int NUM_OF_THREAD = 2;                             // Numero di thread da usare per il classificatore
    public static final String MODEL = "yamnet-classification.tflite";      // Nome del file del modello TFLite

    // Variabili della classe
    private final Context context;                                          // Contesto dell'applicazione Android
    private final AudioClassificationListener listener;                     // Listener per gestire gli eventi di classificazione

    private AudioClassifier classifier;                                     // L'oggetto classificatore TFLite
    private TensorAudio tensorAudio;                                        // Oggetto per gestire i dati audio in formato Tensor
    private AudioRecord recorder;                                           // Oggetto per registrare l'audio dal microfono
    private ScheduledThreadPoolExecutor executor;                           // Esecutore per pianificare le operazioni di classificazione

    /**
     * Costruttore della classe <code>AudioClassification</code>.
     * @param context Il contesto dell'applicazione.
     * @param listener L'implementazione dell'interfaccia `AudioClassificationListener`.
     */
    public AudioClassification(Context context, AudioClassificationListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Inizializza il classificatore audio.
     * Carica il modello TFLite e configura le opzioni per la classificazione.
     */
    public void initClassifier() {
        // Configura le opzioni base per il classificatore, come il numero di thread
        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder().setNumThreads(NUM_OF_THREAD);

        // Configura le opzioni specifiche del classificatore audio
        AudioClassifierOptions options = AudioClassifierOptions.builder()
                .setScoreThreshold(DISPLAY_THRESHOLD) // Imposta la soglia di confidenza per i risultati
                .setMaxResults(DEFAULT_NUM_OF_RESULTS) // Imposta il numero massimo di risultati
                .setBaseOptions(baseOptionsBuilder.build()) // Aggiunge le opzioni base
                .build();

        try {
            // Crea il classificatore dal file del modello e le opzioni
            classifier = AudioClassifier.createFromFileAndOptions(context, MODEL, options);
            // Crea un TensorAudio per l'input, basato sul formato richiesto dal classificatore
            tensorAudio = classifier.createInputTensorAudio();
            // Crea un oggetto AudioRecord per la registrazione audio
            recorder = classifier.createAudioRecord();
        } catch (IllegalStateException | IOException e) {
            // In caso di errore nell'inizializzazione, notifica il listener e logga l'errore
            listener.onError("Audio Classifier failed to initialize. See error logs for details.");
            Log.e("MicrophoneAudioClassification", "TFLite failed to load with error: " + e.getMessage());
        }
    }

    /**
     * Avvia la classificazione audio.
     * Inizia la registrazione dal microfono e pianifica l'esecuzione periodica della classificazione.
     */
    public void startAudioClassification() {
        // Se il classificatore o il recorder non sono inizializzati, li inizializza
        if (classifier == null || recorder == null) {
            initClassifier();
        }
        Log.d("MicrophoneAudioClassification", "Started Audio recognition");
        // Se il recorder è nullo o sta già registrando, esce dalla funzione
        if (recorder == null || recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) return;

        recorder.startRecording(); // Avvia la registrazione audio
        Log.d("MicrophoneAudioClassification", "RecordingState: " + recorder.getRecordingState());

        // Crea un esecutore per pianificare le attività di classificazione
        executor = new ScheduledThreadPoolExecutor(1);
        // Calcola la durata di un segmento audio in millisecondi
        float lengthInMilliSeconds = (classifier.getRequiredInputBufferSize() * 1.0f /
                classifier.getRequiredTensorAudioFormat().getSampleRate()) * 1000;
        // Calcola l'intervallo tra le classificazioni, considerando la sovrapposizione
        long interval = (long) (lengthInMilliSeconds * (1 - DEFAULT_OVERLAP_VALUE));

        // Pianifica l'esecuzione del metodo classifyAudio a intervalli regolari
        executor.scheduleWithFixedDelay(this::classifyAudio, 0, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * Classifica l'audio corrente.
     * Legge i dati audio dal recorder, li passa al classificatore e notifica il listener con i risultati.
     */
    private void classifyAudio() {
        tensorAudio.load(recorder); // Carica i dati audio dal recorder nel TensorAudio
        long inferenceTime = SystemClock.uptimeMillis(); // Cattura il tempo di inizio dell'inferenza
        // Esegue la classificazione audio
        List<org.tensorflow.lite.task.audio.classifier.Classifications> output = classifier.classify(tensorAudio);
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime; // Calcola il tempo impiegato per l'inferenza
        // Notifica il listener con le categorie di suoni rilevati e il tempo di inferenza
        listener.onResult(output.get(0).getCategories(), inferenceTime);
    }

    /**
     * Ferma la classificazione audio.
     * Ferma la registrazione, chiude il classificatore e ferma l'esecutore.
     */
    public void stopAudioClassification() {
        Log.d("MicrophoneAudioClassification", "Stopped Audio recognition!");
        // Ferma e rilascia il recorder se è attivo
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        // Spegne l'esecutore se è attivo
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        // Chiude il classificatore se è attivo
        if (classifier != null) {
            classifier.close();
            classifier = null;
        }
    }
}