package com.yahya.shadow.chartRecyclerView;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

public class ChartData {
    private String title;
    private ArrayList<Entry> entries;

    public ChartData(String title, ArrayList<Entry> entries) {
        this.title = title;
        this.entries = entries;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
