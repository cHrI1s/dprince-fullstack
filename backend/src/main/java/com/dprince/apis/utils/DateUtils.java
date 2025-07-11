package com.dprince.apis.utils;

import com.dprince.apis.dashboard.utils.enums.DashboardDuration;
import com.dprince.apis.utils.vos.DateGap;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.text.ParseException;
import java.util.Locale;


/**
 * * @author Chris Ndayishimiye
 * * @created 11/16/23
 */

public class DateUtils {
	
	
	 // >>>>> YOU HAVE THESE TWO METHODS ALREADY HERE <<<<<

    /**
     * Formats a Date object into a "yyyy-MM" string. This format is chronologically sortable as a String.
     * Used as a key in TreeMap for monthly statistics.
     * @param date The date to format.
     * @return A string in "yyyy-MM" format (e.g., "2024-07").
     */
    public static String getYearMonthKey(@NotNull Date date) { // Using @NotNull as per your existing code style
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH); // Locale.ENGLISH ensures consistent format
        return sdf.format(date);
    }

    /**
     * Converts a "yyyy-MM" string key back to a display format (e.g., "Jan", "Feb").
     * This is used when retrieving data from the TreeMap to prepare it for the UI.
     * @param yearMonthKey The "yyyy-MM" string to parse.
     * @return A displayable month string (e.g., "Jan") or the original key if parsing fails.
     */
    public static String formatMonthKeyForDisplay(@NotNull String yearMonthKey) { // Using @NotNull
        try {
            SimpleDateFormat keySdf = new SimpleDateFormat("yyyy-MM", Locale.ENGLISH);
            Date date = keySdf.parse(yearMonthKey); // Parse the "yyyy-MM" string into a Date object
            SimpleDateFormat displaySdf = new SimpleDateFormat("MMM", Locale.ENGLISH); // Format that Date object to "Jan", "Feb", etc.
            return displaySdf.format(date);
        } catch (ParseException e) {
            System.err.println("Error parsing yearMonthKey for display: " + yearMonthKey + " - " + e.getMessage());
            return yearMonthKey; // As a fallback, return the original key if parsing fails
        }
    }
	

    // =========================================================
    // >>>>> PASTE THE getYYYYMMDD METHOD HERE <<<<<
    // =========================================================
    /**
     * Formats a Date object into a "yyyy-MM-DD" string.
     * This format is suitable for Chart.js time scale labels for daily data.
     * @param date The Date object to format.
     * @return A String representing the date in "yyyy-MM-DD" format, or null if the input date is null.
     */
    public static String getYYYYMMDD(@NotNull Date date) { // Assuming @NotNull is preferred as per your style
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return sdf.format(date);
    }
    // =========================================================
    // >>>>> END OF getYYYYMMDD METHOD <<<<<
    // =========================================================


    public static String getDateString(Date date){
        LocalDateTime localDate = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return localDate.format(dateTimeFormatter);
    }

    public static Date getTomorrowEndDateTime(){
        Date today = getNowDateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }


    public static Date addDays(int days){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.getNowDateTime());
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    public static Date getCurrentWeekDate(int calendarDay){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendarDay);
        return calendar.getTime();
    }

    public static String getDayName(Date date){
        // Format the date to get the day name
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        return formatter.format(date);
    }

    public static String getMonthDayName(Date date){
        // Format the date to get the day name
        SimpleDateFormat formatter = new SimpleDateFormat("d MMM");
        return formatter.format(date);
    }

    public static String getMonthName(Date date){
        // Format the date to get the day name
        SimpleDateFormat formatter = new SimpleDateFormat("MMM");
        return formatter.format(date);
    }

    public static Date atTheEndOfTheDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date atTheBeginningOfTheDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    public static String getTodayDate(){
        LocalDateTime nowTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return nowTime.format(dateTimeFormat);
    }

    public static Date atEndOfWeek(Date date){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    public static Date atStartOfWeek(Date date){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getFirstDayOfMonth(Date date){
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate firstDay = localDate.withDayOfMonth(1);
        return Date.from(firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date getLastDayOfMonth(Date date){
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        YearMonth yearMonth = YearMonth.from(localDate);
        return Date.from(yearMonth.atEndOfMonth().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date getFirstDateOfPreviousMonth(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.getNowDateTime());
        return calendar.getTime();
    }

    public static Date getFirstDateOfMonth(@Nullable Date date){
        Calendar calendar = new GregorianCalendar();
        if(date!=null) calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static Date getFirstDateOfYear(@Nullable Date date){
        Calendar calendar = Calendar.getInstance();
        if(date!=null) calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    public static Date getLastDateOfYear(@Nullable Date date){
        if(date==null) date = DateUtils.getNowDateTime();
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        YearMonth yearMonth = YearMonth.of(localDate.getYear(), 12);
        LocalDate endOfTheMonth = yearMonth.atEndOfMonth();
        return Date.from(endOfTheMonth.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date convertToGMT(Date date){
        Instant instant = date.toInstant();
        ZonedDateTime zonedDateTimeFrom = instant.atZone(ZoneId.of("GMT"));
        return Date.from(zonedDateTimeFrom.toInstant());
    }

    public static Date convertToTimezone(Date date, String timezone){
        return date;
    }

    @NotNull
    public static Date getNowDateTime(){
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Calcutta")));
        return new Date(System.currentTimeMillis());
    }

    public static Date addOneDay(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        return calendar.getTime();
    }

    public static String getBirthdaySearchFormat(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH)+1;
        String monthString = "0"+month,
                dayString = "0"+calendar.get(Calendar.DAY_OF_MONTH);
        return monthString.substring(monthString.length()-2)
                + "-"
                + dayString.substring(dayString.length()-2);
    }

    public static LinkedList<DateGap> getMonthsGapsIn(Date start, Date end,
                                                      DashboardDuration duration){
        List<DashboardDuration> noLastMonth = Arrays.asList(DashboardDuration.THREE_MONTH, DashboardDuration.SEMESTER);
        LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long months = ChronoUnit.MONTHS.between(startDate, endDate);
        LinkedList<DateGap> list = new LinkedList<>();

        long monthsCount = noLastMonth.contains(duration) ? months : months+1;
        for(long i=0; i<monthsCount; i++){
            Date dateCurrentMonth = Date.from(startDate.plusMonths(i)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date monthStart = DateUtils.getFirstDateOfMonth(dateCurrentMonth);
            Date monthEnd = DateUtils.getLastDayOfMonth(monthStart);
            DateGap dateGap = DateGap.builder()
                    .end(monthEnd)
                    .from(monthStart)
                    .build();
            list.add(dateGap);
        }
        return list;
    }


    public static List<Date> getDatesBetween(Date start, Date end){
        LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        List<Date> dates = new ArrayList<>();
        for (long i = 0; i <= daysBetween; i++) {
            Date dateCurrentDate = Date.from(startDate.plusDays(i).atStartOfDay(ZoneId.systemDefault()).toInstant());
            dates.add(dateCurrentDate);
        }
        return dates;
    }


    public static DateGap getDateGapsBetweenYears(int start, int end){
        Calendar calendarOne = Calendar.getInstance(),
                calendarTwo = Calendar.getInstance();
        Date today = getNowDateTime();
        calendarOne.setTime(today);
        calendarTwo.setTime(today);

        calendarOne.add(Calendar.YEAR, -end);
        Date dateStart = calendarOne.getTime();

        calendarTwo.add(Calendar.YEAR, -start);
        Date dateENd = calendarTwo.getTime();

        return DateGap.builder()
                .from(dateStart)
                .end(dateENd)
                .build();
    }
}