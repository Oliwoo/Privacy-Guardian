package com.unict.mobile.utils;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Classe di utility per gestire le date in vari formati
*/
public class DateTimeUtils {

    // -- CONSTANTS --
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final Locale locale = Locale.ENGLISH;
    private static final SimpleDateFormat SDF = new SimpleDateFormat(DATE_PATTERN, locale);
    private static final SimpleDateFormat DAY_MONTH_FORMAT = new SimpleDateFormat("dd MMMM", locale);
    private static final SimpleDateFormat WEEKDAY_FORMAT = new SimpleDateFormat("EEEE", locale);

    /** Rappresenta un intervallo settimanale con data di inizio e fine. */
    public static class WeekRange {
        private Date start; // Ora di tipo Date
        private Date end;   // Ora di tipo Date

        /**
         * Costruttore per creare un nuovo oggetto WeekRange.
         * @param start La data di inizio della settimana come oggetto Date.
         * @param end La data di fine della settimana come oggetto Date.
         */
        public WeekRange(Date start, Date end) {
            this.start = start;
            this.end = end;
        }

        /** Restituisce la data di inizio della settimana come oggetto Date. */
        public Date getStart() {
            return start;
        }

        /** Restituisce la data di fine della settimana come oggetto Date. */
        public Date getEnd() {
            return end;
        }

        /** Imposta la data di inizio della settimana. */
        public void setStart(Date start) {
            this.start = start;
        }

        /** Imposta la data di fine della settimana. */
        public void setEnd(Date end) {
            this.end = end;
        }

        /**
         * Restituisce la data di inizio formattata come stringa.
         * Utile per la visualizzazione o il logging.
         */
        public String getStartStr() {
            return SDF.format(start);
        }

        /**
         * Restituisce la data di fine formattata come stringa.
         * Utile per la visualizzazione o il logging.
         */
        public String getEndStr() {
            return SDF.format(end);
        }
    }

    /** Restituisce la data odierna come stringa "yyyy-MM-dd". */
    public static String getTodayStr() {
        return SDF.format(new Date());
    }

    /**
     * Formatta una data in una stringa "yyyy-MM-dd".
     * @param date Data da formattare.
     * @return Stringa della data formattata.
     */
    public static String parseDate(@NonNull Date date) {
        return SDF.format(date);
    }

    /**
     * Formatta una durata in millisecondi in una stringa hh:mm:ss.ms.
     * @param totalMillis Durata totale in millisecondi.
     * @return Stringa formattata.
     */
    public static String getFormattedTimeMillisStr(long totalMillis) {
        long seconds = totalMillis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        long millis = totalMillis % 1000;

        return String.format(locale, "%02d:%02d:%02d.%03d", hours, minutes, secs, millis);
    }

    /**
     * Formatta un timestamp in millisecondi in una stringa "yyyy-MM-dd".
     * @param millis Timestamp da formattare.
     * @return Stringa della data formattata.
     */
    public static String getFormattedDayMillisStr(long millis) {
        return SDF.format(new Date(millis));
    }

    /**
     * Restituisce il nome completo del giorno della settimana (es. "Thursday") per una data.
     * @param date Data.
     * @return Nome del giorno della settimana.
     */
    public static String getWeekdayDay(@NonNull Date date) {
        return WEEKDAY_FORMAT.format(date);
    }

    /**
     * Calcola l'intervallo (inizio/fine) della settimana corrente. Inizio: lunedì.
     * @return Oggetto WeekRange con inizio e fine settimana come oggetti Date.
     */
    public static WeekRange getCurrentWeekDates() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 6);
        Date endDate = calendar.getTime();

        return new WeekRange(startDate, endDate);
    }

    /**
     * Estrae e formatta giorno e mese (es. "26 October") da una data.
     * @param date Data.
     * @return Stringa "dd MMMM".
     */
    public static String getDayAndMonthLabel(@NonNull Date date) {
        return DAY_MONTH_FORMAT.format(date);
    }

    /**
     * Genera una lista di date tra due date specificate (incluse).
     * @param startDate Data di inizio.
     * @param endDate Data di fine.
     * @return Lista di date.
     */
    public static List<Date> getDatesBetween(@NonNull Date startDate, @NonNull Date endDate){

        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        while (!calendar.getTime().after(endDate)) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dates;
    }

    /**
     * Calcola l'intervallo della settimana precedente.
     * @param currentRange Intervallo settimana corrente.
     * @return WeekRange della settimana precedente.
     * @throws Exception Se l'intervallo corrente è null o le sue date sono null.
     */
    public static WeekRange getPreviousWeekRange(WeekRange currentRange) throws Exception {
        return shiftWeek(currentRange, -7);
    }

    /**
     * Calcola l'intervallo della settimana successiva.
     * @param currentRange Intervallo settimana corrente.
     * @return WeekRange della settimana successiva.
     * @throws Exception Se l'intervallo corrente è null o le sue date sono null.
     */
    public static WeekRange getNextWeekRange(WeekRange currentRange) throws Exception {
        return shiftWeek(currentRange, 7);
    }

    /**
     * Converte una stringa in un oggetto Date usando il pattern predefinito.
     * @param dateStr Stringa data in formato "yyyy-MM-dd".
     * @return Oggetto Date.
     * @throws ParseException Se il formato della stringa non è valido.
     * @throws Exception Se la data è null dopo il parsing.
     */
    public static Date parseDate(String dateStr) throws Exception {
        Date date = SDF.parse(dateStr);
        if(date == null) throw new Exception("La stringa di data '" + dateStr + "' non ha prodotto una data valida.");
        return date;
    }

    /**
     * Formatta un oggetto Date in una stringa "yyyy-MM-dd".
     * @param date Oggetto Date.
     * @return Stringa formattata.
     */
    public static String formatDate(@NonNull Date date){
        return SDF.format(date);
    }

    // -- INTERNAL UTILS --

    /**
     * Sposta un intervallo di date (settimana) di un numero specificato di giorni.
     * @param currentRange L'intervallo di riferimento.
     * @param offsetDays Numero di giorni da spostare (positivo avanti, negativo indietro).
     * @return WeekRange dell'intervallo spostato.
     */
    private static WeekRange shiftWeek(@NonNull WeekRange currentRange, int offsetDays){
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.setTime(currentRange.getStart());
        end.setTime(currentRange.getEnd());

        start.add(Calendar.DAY_OF_MONTH, offsetDays);
        end.add(Calendar.DAY_OF_MONTH, offsetDays);

        return new WeekRange(start.getTime(), end.getTime());
    }
}