package com.yahya.shadow.services;

import java.util.Calendar;
import java.util.Date;

public class TimestampUtils {

    public static long getTodayMidnightTimestamp(long timestamp) {
        // Create a Calendar instance and set the time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        // Set hours, minutes, seconds, and milliseconds to 0
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Return the timestamp in milliseconds for midnight of the given day
        return calendar.getTimeInMillis();
    }

    public static void main(String[] args) {
        long todayTimestamp = getTodayMidnightTimestamp(System.currentTimeMillis());
        System.out.println("Timestamp for today's midnight: " + todayTimestamp);
    }
}