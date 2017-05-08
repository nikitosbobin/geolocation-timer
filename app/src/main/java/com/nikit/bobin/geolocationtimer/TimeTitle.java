package com.nikit.bobin.geolocationtimer;

public enum TimeTitle {
    Second(0, "second"),
    Minute(1, "minute"),
    Hour(2, "hour"),
    Day(3, "day");
    private final int index;
    private final String title;

    TimeTitle(int index, String title) {
        this.index = index;
        this.title = title;
    }

    public int getIndex() {
        return index;
    }

    public String getTitle() {
        return title;
    }
}
