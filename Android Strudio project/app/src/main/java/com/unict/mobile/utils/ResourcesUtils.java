package com.unict.mobile.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

/**
 * Uso: Classe di utility per ottenere velocemente risorse (String, Color, Drawable)
 *
 * Perchè ci sono metodi statici richiamati dai metodi pubblici?
 * | La scelta di avere dei metodi statici mi permette di utilizzarli in qualsiasi punto del codice senza dover necessariamente istanziare
 * | un oggetto di tipo ResourceUtils ma con l'obbligo di specificare il context su cui deve lavorare.
 * | L'uso nei metodi pubblici invece è legato all'uso mediante un'istanza di ResourceUtils che contiene al suo interno un riferimento
 * | al context cosicchè non debba specificarlo ogni volta
 */
public class ResourcesUtils {

    public static Drawable getIcon(Context context, @DrawableRes int icon){     // Metodo per ottenere un Drawable a partire da un Context e ID della risorsa
        return AppCompatResources.getDrawable(context, icon);                   // Ottengo la risorsa a partire dal Context e Resource ID
    }
    public static String getString(Context context, @StringRes int string){     // Metodo per ottenere una String a partire da un Context e ID della risorsa
        return context.getString(string);                                       // Ottengo la risorsa a partire dal Context e Resourse ID
    }
    public static String getFormattedString(Context context, @StringRes int string, Object... args){ // Metodo per ottenere una String con formattazione da un Context e ID della risorsa specificandone i valori dei parametri da formattare
        return String.format(getString(context,string),args);                                        // Ottengo la stringa a partire dal Context e Resourse ID e la formatto con i valori dei parametri
    }
    public static int getColor(Context context, @ColorRes int color){           // Metodo per ottenenere un Color (in Android sono intesi come int) a partire da un Context e ID della risorsa
        return ContextCompat.getColor(context,color);                           // Ottengo il colore a partire dal Context e Resource ID
    }

    private final Context ctx;                                                  // Istanza del context
    public ResourcesUtils(Context ctx){                                         // Costruttore oggetto ResourceUtils
        this.ctx = ctx;                                                         // Assegno il context all'instanza
    }

    public Drawable getIcon(@DrawableRes int icon){                             // Metodo per ottenere un Drawable a partire dall'ID Risorsa
        return getIcon(ctx,icon);                                               // Richiamo il metodo per ottenere il Drawable specificando Context e ID Risorsa
    }
    public String getString(@StringRes int string){                             // Metodo per ottenere un String a partire dall'ID Risorsa
        return getString(ctx,string);                                           // Richiamo il metodo per ottenere la stringa specificando Context e ID Risorsa
    }

    public String getFormattedString(@StringRes int string, Object... args){    // Metodo per ottenere una String fromattata a partire dall'ID Risorsa e i valori dei parametri da formattare
        return getFormattedString(ctx,string,args);                             // Richiamo il metodo per ottenere una stringa formattata specificando Context, ID Risorsa e valori dei parametri
    }
    public int getColor(@ColorRes int color){                                   // Metodo per ottenere un Colore a partire dall'ID Risorsa
        return getColor(ctx, color);                                            // Richiamo il metodo per ottenere il colore specificando Context e ID Risorsa
    }
}
