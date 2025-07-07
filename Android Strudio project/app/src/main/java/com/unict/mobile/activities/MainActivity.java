package com.unict.mobile.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.unict.mobile.R;
import com.unict.mobile.components.BottomBar;
import com.unict.mobile.components.PageTitle;
import com.unict.mobile.fragments.BaseFragment;
import com.unict.mobile.fragments.HomeFragment;
import com.unict.mobile.services.MicrophoneMonitorService;
import com.unict.mobile.utils.PermissionManager;

/**
 * Uso: Activity Principale avviata all'apertura dell'app
 * | Quando l'utente avvia l'app, la prima activity che verrà avviata sarà questa (definita a livello di Manifest), la quale gestira Permessi e Fragment da visualizzare
 * | come homepage
 *
 * Cosa sono questi implements?
 * - PermissionManager: Implemento metodi per la gestione dei permessi tramite la classe PermissionManager.
 * | Utilizzo una classe separata per poter riutilizzare la classe e relativi metodi in più parti del codice (riducendo le dimensioni
 * | del codice) e migliorando la leggibilità mediante l'uso di nomi per i metodi chiari e che facciano capire velocemente il loro scopo
 *
 * - BaseFragment: Implemento un metodo (univoco) che gestisce l'evento di cambio fragment e cambio titolo per tutte le fragment del tipo BaseFragment.
 * | Concettualmente l'applicazione funziona con una sola activity, così da ridurre il carico di android, la quale contiene una
 * | FragmentContainerView a cui eseguo l'injection del Fragment che voglio fare vedere.
 * | Utilizzo una classe BaseFragment padre per tutte le fragment che voglio fare vedere nel FragmentConteinerView in modo tale che impostando
 * | il codice eseguito al cambio di titolo o fragment per il padre, tutti i figli (se non specificato diversamente tramite @Override) seguiranno
 * | il comportamento impostato al padre, nonchè quello che definiamo in questa Activity
*/
public class MainActivity extends AppCompatActivity implements PermissionManager.PermissionCallback, BaseFragment.OnFragmentChangeEventListener, BaseFragment.TitleChangeListener {
    private PermissionManager permissionManager;    // Istanza dell'oggetto PermissionManager a cui andremo a impostare il comportamento da dare ai metodi di gestione dei permessi in base agli eventi di Android
    private PageTitle pageTitle;                    // Istanza dell'ogetto componente PageTitle che mostra il titolo della pagina corrente e possibili frecce direzionali per la navigazione
    private BottomBar bottomBar;                    // Istanzza dell'oggetto componente BottomBar che mostra una barra di navigazione inferiore per visualizzare velocemente fragment preimpostate o di utilizzo frequente


    @Override
    protected void onCreate(Bundle savedInstanceState) {                        // Metodo chiamato alla creazione dell'Activity
        super.onCreate(savedInstanceState);                                     // Richiamo onCreate della classe padre (Gestito da Android)
        setContentView(R.layout.layout_main);                                   // Effettuo l'injection del layout che l'activity deve renderizzare

        permissionManager = new PermissionManager(this, this);   // Creo un'istanza di PermissionManager passando l'activity e il context in cui sono contenute le callback che definiscono il comportamento che l'istanza deve avere in base agli eventi di Android
        permissionManager.requestAllNecessaryPermissions();                     // Avvio un check se tutti i permessi sono stati concessi, altrimenti li richiedo

        pageTitle = findViewById(R.id.layout_main_page_title);                  // Ottengo il componente PageTitle dal layout attraverso l'ID

        bottomBar = findViewById(R.id.layout_main_bottom_bar);                  // Ottengo il componente BottomBar dal layout attraverso l'ID
        bottomBar.setListener(this::setFragment);                               // Assegno il comportamento (sottoforma di callback) che verrà eseguito quando viene cliccato un pulsante

        if(savedInstanceState == null) {                                        // Controllo se l'activity non è stata già creata
            setFragment(HomeFragment.newInstance());                            // Imposto HomeFragment come fragment visualizzato all'inizio
        }
    }

    @Override
    public void onTitleChanged(String title, BaseFragment fragment) {          // Comportamento allo scatenarsi dell'evento di CambioTitolo
        if(pageTitle != null){                                                 // Verifico che il PageTitle sia istanziato correttamente
            pageTitle.setTitle(title);                                         // Imposto il titolo della pagina
            pageTitle.showBack(fragment!=null);                                // Specifico se mostrare o meno la freccia di navigazione
            pageTitle.setGoBackListener(l -> {                            // Specifico il fragment che deve essere aperto nel caso in cui l'utente clicca la freccia di navigazione
                if(fragment != null) setFragment(fragment);                    // Faccio in modo che l'azione si compia solo se viene specificato un fragment
            });
        }
    }

    @Override
    public void OnFragmentChangeRequest(BaseFragment fragment){                 // Comportamento allo scatenarsi dell'evento di CambioFragment
        setFragment(fragment);                                                  // Imposto la fragment da mostrare
    }

    public void setFragment(BaseFragment fragment){                             // Metodo per impostare una fragment (di famiglia BaseFragment) nella FragmentContentView
        /*
         * Utilizzo FragmentTransaction per avere il supporto della history delle fragment che
         * vengono rimpiazzate ogni volta affinche l'utente quando esegue lo swipe indietro venga
         * recuperata automaticamente la vecchia fragment
        */
        FragmentTransaction transaction = getSupportFragmentManager()           // Instanzio un oggetto di tipo FragmentTransaction per generare una transizione di Fragment
        .beginTransaction()                                                     // Configuro la Transazione
        .replace(R.id.layout_main_content, fragment);                           // Specifico il FragmentContentView a cui applicare la transazione e il fragment che voglio impostare
        onTitleChanged(fragment.getTitle(),fragment.getGoBackTarget());         // Imposto titolo e freccia di navigazione in base al fragment impostato
        transaction.commit();                                                   // Eseguo la transazione
    }

    @Override
    public void onResume(){                                                     // Modifico il comportamento dell'activity quando viene ripristinanta (passaggio da background a foreground)
        super.onResume();                                                       // Richiamo l'onResume da parte della classe padre
        this.bottomBar.updateHomeIcon();                                        // Aggiungo un refresh del bottone home della bottombar (EASTER EGG - Il bottone della home cambia espressione in base al livello di rischio rilevato nella giornata)
    }

    private void startMicrophoneMonitorService(){                                                   // Metodo per avviare il Service di monitoraggio dell'utilizzo del microfono
        Intent serviceIntent = new Intent(this, MicrophoneMonitorService.class);      // Istanzio l'Intent del servizio specificando il contesto e la classe del Service da eseguire
        startForegroundService(serviceIntent);                                                      // utilizzo il metodo startForegroundService, metodo simile a startService ma con un utilizzo specifico se si usa Foreground Notification
    }

    // -- PERMISSION REQUESTS HANDLER --
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { // Comportamento all'evento in cui ricevo il risultato di consenso un una richiesta di permesso
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);                   // Richiamo l'evento dalla classe padre
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);       // Comunico l'evento al permission manager
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){                  // Comportamento all'evento in cui un'Activity viene chiusa (usata per gestire l'evento di chiusura popup di richiesta permesso)
        super.onActivityResult(requestCode, resultCode, data);                                      // Richiamo l'evento della classe padre
        permissionManager.onActivityResult(requestCode, resultCode, data);                          // Comunico l'evento al permission manager
    }
    @Override
    public void onAllPermissionsGranted(){                                                          // Comportamento all'evento in cui il check dei permessi porti un successo
       startMicrophoneMonitorService();                                                             // Avvio il Servizio di monitoraggio del microfono
    }
    @Override
    public void onPermissionsDenied(){                                                                                          // Comportamento all'evento in cui uno o più permessi vengono negati
        Log.d("MainActivity", getString(R.string.main_activity_on_permission_denied));                                // Stampo su Logcat un log di debug
        Toast.makeText(this, R.string.main_activity_on_permission_denied, Toast.LENGTH_LONG).show();    // Invio un Toast message all'utente per comunicare il problema
    }
    @Override
    public void onUsageStatsPermissionGranted(){                                                                        // Comportamento all'evento in cui l'utente (manualmente) garantisce l'accesso all'uso delle Statistiche di utilizzo del dispositivo
        Log.d("MainActivity", getString(R.string.main_activity_on_usage_stats_permission_granted));                                 // Stampo su Logcat un log di debugging
        Toast.makeText(this, R.string.main_activity_on_usage_stats_permission_granted, Toast.LENGTH_SHORT).show();    // Invio un Toast message all'utente per comunicare la conferma
    }
    @Override
    public void onUsageStatsPermissionDenied(){                                                                                  // Comportamento all'evento in cui l'utente (manualmente) non garantisce l'accesso all'uso delle Statistiche di utilizzo del dispositivo
        Log.d("MainActivity", getString(R.string.main_activity_on_usage_stats_permission_denied));                                // Stampo su Logcat un log di debugging
        Toast.makeText(this, R.string.main_activity_on_usage_stats_permission_denied, Toast.LENGTH_LONG).show();    // Invio un Toast message all'utente per comunicare il problema
    }
    @Override
    public void onAccessibilityPermissionGranted(){                                                          // Comportamento all'evento in cui l'utente (manualmente) garantisce il permesso di Accessibilità del dispositivo
        Log.d("MainActivity", getString(R.string.main_activity_on_accessibility_permission_granted));                                   // Stampo su Logcat un log di debugging
        Toast.makeText(this, R.string.main_activity_on_accessibility_permission_granted, Toast.LENGTH_SHORT).show();      // Invio un Toast message all'utente per comunicare la conferma
    }
    @Override
    public void onAccessibilityPermissionDenied(){                                                                              // Comportamento all'evento in cui l'utente (manualmente) non garantisce il permesso di Accessibilità del dispositivo
        Log.d("MainActivity", getString(R.string.main_activity_on_accessibility_permission_denied));                                // Stampo su Logcat un log di debugging
        Toast.makeText(this, R.string.main_activity_on_accessibility_permission_denied, Toast.LENGTH_LONG).show();    // Invio un Toast message all'utente per comunicare il problema
    }
}