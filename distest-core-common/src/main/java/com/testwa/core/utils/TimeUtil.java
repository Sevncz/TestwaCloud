package com.testwa.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Contains time related utility methods.
 */
public class TimeUtil {

    private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static SimpleDateFormat TIME_FORMAT_MSEC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    private final static SimpleDateFormat FILE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    // only static methods, don't allow construction
    private TimeUtil() {
    }

    /**
     * Return a prettified version of the given elapsed time in milliseconds.
     */
    public static synchronized String formatElapsedTime(long elapsedTimeMs) {
        if (elapsedTimeMs < 1000) {
            return String.format("%d ms", elapsedTimeMs);
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTimeMs) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMs) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMs);
        StringBuilder time = new StringBuilder();
        if (hours > 0) {
            time.append(hours);
            time.append("h ");
        }
        if (minutes > 0) {
            time.append(minutes);
            time.append("m ");
        }
        time.append(seconds);
        time.append("s");

        return time.toString();
    }

    /**
     * Return a readable formatted version of the given epoch time.
     *
     * @param epochTime the epoch time in milliseconds
     * @return a auth readable string
     */
    public static synchronized String formatTimeStamp(long epochTime) {
        return TIME_FORMAT.format(new Date(epochTime));
    }

    public static String getTimestamp() {
        return formatTimeStamp(System.currentTimeMillis());
    }

    public static Long getTimestampLong() {
        return System.currentTimeMillis();
    }
    
    public static synchronized String formatTimeStampMsec(long epochTime) {
        return TIME_FORMAT_MSEC.format(new Date(epochTime));
    }
    
    public static String getTimestampMsec() {
        return formatTimeStampMsec(System.currentTimeMillis());
    }
    
    public static synchronized String formatTimeForFile(long epochTime) {
        return FILE_TIME_FORMAT.format(new Date(epochTime));
    }
    
    public static String getTimestampForFile() {
        return formatTimeForFile(System.currentTimeMillis());
    }

    public static Date getMongoDate(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.HOUR_OF_DAY, 8);
        return sdf.parse(sdf.format(ca.getTime()));
    }

    public static String toCron(Date date){
        String dateFormat="ss mm HH dd MM ? yyyy";
        return formatDateByPattern(date, dateFormat);
    }

    public static String formatDateByPattern(Date date, String dateFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String formatTimeStr = null;
        if (date != null) {
            formatTimeStr = sdf.format(date);
        }
        return formatTimeStr;
    }

    public static Date now(){
        return new Date();
    }

}