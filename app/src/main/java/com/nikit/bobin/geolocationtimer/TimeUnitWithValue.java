package com.nikit.bobin.geolocationtimer;

public class TimeUnitWithValue {
    private final TimeTitle timeUnit;
    private final long value;

    public TimeUnitWithValue(TimeTitle timeUnit, long value) {
        this.timeUnit = timeUnit;
        this.value = value;
    }

    public TimeTitle getTimeUnit() {
        return timeUnit;
    }

    public long getValue() {
        return value;
    }
}
