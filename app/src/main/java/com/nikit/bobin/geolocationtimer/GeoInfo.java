package com.nikit.bobin.geolocationtimer;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;
import com.orm.dsl.Table;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Table
public class GeoInfo extends SugarRecord {
    private Long id;

    private String title;
    private double latitude;
    private double longitude;

    private long spentTime;
    private Date periodStart;
    private Date lastLocationUpdate;
    private int periodDays;
    private boolean enabled;
    private boolean notifyPeriodEnds;
    private boolean clearTimerEachPeriod;

    public GeoInfo(){}

    public GeoInfo(String title, double latitude, double longitude) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public Location getLocation() {
        Location location = new Location(title);
        location.setLongitude(longitude);
        location.setLatitude(latitude);
        return location;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public int getPeriodDays() {
        return periodDays;
    }

    public void setPeriodDays(int periodDays) {
        this.periodDays = periodDays;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isNotifyPeriodEnds() {
        return notifyPeriodEnds;
    }

    public void setNotifyPeriodEnds(boolean notifyPeriodEnds) {
        this.notifyPeriodEnds = notifyPeriodEnds;
    }

    public boolean isClearTimerEachPeriod() {
        return clearTimerEachPeriod;
    }

    public void setClearTimerEachPeriod(boolean clearTimerEachPeriod) {
        this.clearTimerEachPeriod = clearTimerEachPeriod;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getSpentTimeMilliseconds() {
        return spentTime;
    }

    public long getSpentTimeSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(spentTime);
    }

    public Date getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void resetSpentTime() {
        spentTime = 0L;
    }

    public boolean setLastLocationUpdate(Date lastLocationUpdate) {
        boolean result = false;
        if (this.lastLocationUpdate != null && lastLocationUpdate != null) {
            long delta = DateHelper.millisecondsBetween(
                    lastLocationUpdate,
                    this.lastLocationUpdate);
            spentTime += delta;
            result = true;
        }
        this.lastLocationUpdate = lastLocationUpdate;
        return result;
    }
}
