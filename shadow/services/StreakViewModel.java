package com.yahya.shadow.services;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;

public class StreakViewModel extends AndroidViewModel {

    private SharedPreferences streakPrefs;
    private MutableLiveData<Integer> streakCountLiveData = new MutableLiveData<>();
    private static final String PREF_NAME = "streak_prefs";
    private static final String STREAK_KEY = "streak_count";
    private static final String LAST_CHECKUP_DATE_KEY = "last_checkup_date";


    public StreakViewModel(@NonNull Application application) {
        super(application);
        streakPrefs = application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int initialStreak = streakPrefs.getInt(STREAK_KEY, 0);
        streakCountLiveData.setValue(initialStreak);
    }

    public LiveData<Integer> getStreakCountLiveData() {
        return streakCountLiveData;
    }

    public void checkDailyCheckup() {
        Calendar today = Calendar.getInstance();
        long lastCheckupDateMillis = streakPrefs.getLong(LAST_CHECKUP_DATE_KEY, 0);

        if (isSameDay(today, lastCheckupDateMillis)) {
            // Already checked today
            return;
        }

        // Update streak count if it's a consecutive checkup
        int currentStreak = streakCountLiveData.getValue() != null ? streakCountLiveData.getValue() : 0;
        int newStreakCount = isNextDay(today, lastCheckupDateMillis) ? currentStreak + 1 : 1;

        streakCountLiveData.setValue(newStreakCount);
        saveStreak(newStreakCount);
        saveLastCheckupDate(today.getTimeInMillis());
    }

    private boolean isSameDay(Calendar cal, long timeInMillis) {
        Calendar checkupCal = Calendar.getInstance();
        checkupCal.setTimeInMillis(timeInMillis);
        return cal.get(Calendar.YEAR) == checkupCal.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == checkupCal.get(Calendar.MONTH) &&
                cal.get(Calendar.DAY_OF_MONTH) == checkupCal.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isNextDay(Calendar cal, long timeInMillis) {
        Calendar checkupCal = Calendar.getInstance();
        checkupCal.setTimeInMillis(timeInMillis);
        checkupCal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.after(checkupCal);
    }

    private void saveStreak(int streak) {
        streakPrefs.edit().putInt(STREAK_KEY, streak).apply();
    }

    private void saveLastCheckupDate(long dateMillis) {
        streakPrefs.edit().putLong(LAST_CHECKUP_DATE_KEY, dateMillis).apply();
    }
}