package com.unict.mobile.utils;

import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Classe di utility, utile a <b>gestire operazioni di caricamento dati in background</b>.<br>
 * Permette di eseguire un'attività complessa su un <b>thread separato</b> e di <b>aggiornare l'interfaccia
 * utente</b> in sicurezza una volta completata l'operazione o in caso di errore.
 */
public class AsyncDataLoadHelper {
    private static final String TAG = "AsyncDataLoadHelper"; // TAG usato per filtrare i messaggi nei log di Logcat

    /**
     * Interfaccia di <b>callback</b> per definire il comportamento dell'operazione asincrona.
     * Le implementazioni devono specificare la logica per il recupero dati, la gestione del risultato
     * e l'eventuale gestione degli errori.
     *
     * @param <T> Il tipo di dato che l'operazione asincrona restituirà.
     */
    public interface Callback<T> {
        /**
         * <b>Esegue la logica di recupero dati</b>. Questo metodo viene chiamato su un <b>thread in background</b>
         * e dovrebbe contenere tutte le operazioni che richiedono tempo (es. chiamate di rete, query database).
         *
         * @return Il risultato dell'operazione.
         * @throws Exception Se si verifica un problema durante il recupero.
         */
        T getDataRoutine() throws Exception;

        /**
         * <b>Gestisce il risultato dell'operazione riuscita</b>. Questo metodo viene chiamato sul <b>thread dell'UI</b>,
         * garantendo che sia sicuro aggiornare l'interfaccia utente.
         *
         * @param result I dati ottenuti da {@link #getDataRoutine()}.
         */
        void onResult(T result);

        /**
         * Metodo predefinito per la <b>gestione degli errori</b>. Registra l'eccezione nel Logcat.
         * Viene chiamato sul thread dell'UI se il fragment è ancora attivo, altrimenti sul thread di background.
         *
         * @param type Un descrittore dell'errore (es. "Errore di rete").
         * @param e L'eccezione che si è verificata.
         */
        default void onError(String type, Exception e) {
            Log.e(TAG, type, e);
        }
    }

    /**
     * <b>Avvia l'operazione di caricamento dati asincrona<b>.<br>
     * Crea un nuovo thread, esegue {@code getDataRoutine} e poi gestisce il ritorno al thread dell'UI
     * per gli aggiornamenti (es. disattivare {@code SwipeRefreshLayout}) e la notifica del risultato o dell'errore.
     *
     * @param <T> Il tipo di dato che il callback gestirà.
     * @param fragment Il {@code Fragment} che avvia l'operazione; serve a garantire aggiornamenti UI sicuri.
     * @param swipeRefreshLayout (Opzionale) Un componente {@code SwipeRefreshLayout} da disattivare al termine. Può essere {@code null}.
     * @param callback L'implementazione dell'interfaccia {@link Callback} che definisce l'operazione.
     */
    public static <T> void execute(Fragment fragment, SwipeRefreshLayout swipeRefreshLayout, Callback<T> callback) {
        new Thread(() -> {                                                                          // Crea e avvia un nuovo thread
            try {
                T result = callback.getDataRoutine();                                               // Esegue l'operazione in background

                if (fragment.isAdded()) {                                                           // Se il fragment è ancora attivo
                    fragment.requireActivity().runOnUiThread(() -> {                                // Torna sul thread dell'UI
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);    // Disattiva il refresh
                        callback.onResult(result);                                                  // Notifica il risultato
                    });
                }
            } catch (Exception e) {                                                                 // In caso di errore
                if (fragment.isAdded()) {                                                           // Se il fragment è ancora attivo
                    fragment.requireActivity().runOnUiThread(() -> {                                // Torna sul thread dell'UI
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);    // Disattiva il refresh
                        callback.onError("Errore generico", e);                                // Notifica l'errore
                    });
                } else {
                    callback.onError("Fragment non attaccato", e);                             // Notifica l'errore se il fragment non è più attivo
                }
            }
        }).start();                                                                                 // Avvia l'esecuzione del Thread
    }
}
