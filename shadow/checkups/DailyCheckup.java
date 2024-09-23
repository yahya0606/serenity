package com.yahya.shadow.checkups;

public class DailyCheckup {
    private String day,rating,sleepDuration,notes,sleepRate,moodRate,productivity;

    public DailyCheckup(String day, String rating,String sleepRate,String moodRate,String productivity, String sleepDuration, String notes) {
        this.day = day;
        this.rating = rating;
        this.sleepDuration = sleepDuration;
        this.notes = notes;
        this.sleepRate = sleepRate;
        this.moodRate = moodRate;
        this.productivity = productivity;
    }
    public String getSleepRate() {
        return sleepRate;
    }
    public String getMoodRate() {
        return moodRate;
    }
    public String getProductivity() {
        return productivity;
    }

    public String getDay() {
        return day;
    }

    public String getRating() {
        return rating;
    }

    public String getSleepDuration() {
        return sleepDuration;
    }

    public String getNotes() {
        return notes;
    }
}
