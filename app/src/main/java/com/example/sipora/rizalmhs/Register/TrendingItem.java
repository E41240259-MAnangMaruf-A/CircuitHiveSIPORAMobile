package com.example.sipora.rizalmhs.Register;

public class TrendingItem {
    private String title;
    private int count;

    public TrendingItem(String title, int count) {
        this.title = title;
        this.count = count;
    }

    public String getTitle() { return title; }
    public int getCount() { return count; }
}
