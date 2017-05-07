package com.nikit.bobin.geolocationtimer;

import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class DateHelper {
    public static Date now() {
        return new Date();
    }

    public static int dayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static Date createDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    public static Date createDate(DatePicker datePicker) {
        return createDate(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth());
    }

    public static long millisecondsBetween(Date first, Date second) {
        if (first == null || second == null)
            return 0L;
        return first.getTime() - second.getTime();
    }

    public static long daysBetween(Date first, Date second) {
        return TimeUnit.MILLISECONDS.toDays(millisecondsBetween(first, second));
    }

    public static long daysForToday(Date date) {
        return daysBetween(now(), date);
    }

    public static int getDay(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return instance.get(Calendar.DAY_OF_MONTH);
    }

    public static int getMonth(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return instance.get(Calendar.MONTH);
    }

    public static int getYear(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        return instance.get(Calendar.YEAR);
    }

    public static void updatePicker(Date date, DatePicker datePicker) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);

        datePicker.updateDate(
                instance.get(Calendar.YEAR),
                instance.get(Calendar.MONTH),
                instance.get(Calendar.DAY_OF_MONTH));
    }
}
