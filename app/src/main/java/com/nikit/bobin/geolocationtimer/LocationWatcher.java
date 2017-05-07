package com.nikit.bobin.geolocationtimer;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocationWatcher extends Service
        implements GoogleApiClient.ConnectionCallbacks, LocationListener {
    public final static String BROADCAST_ACTION =
            "com.nikit.bobin.geolocationtimer.location_changed";
    public final static String BROADCAST_RES = "gfdgsdf";

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationHelper locationHelper;
    private HashSet<Long> lastNearGeoInfoIds;
    private Date lastLocationUpdate;

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        locationHelper = new LocationHelper(this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(5));
        //locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(5));
        Log.d("LocationWatcher", "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        googleApiClient.connect();
        Log.d("LocationWatcher", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("LocationWatcher", "onConnected");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    locationRequest,
                    this);
            Toast.makeText(this, "Succeed start location watch", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Log.d("LocationWatcher", "Could not start location watch");
            Toast.makeText(this, "Could not start location watch", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        List<GeoInfo> geoInfos = GeoInfo.listAll(GeoInfo.class);
        HashSet<Long> currentNearGeoInfoIds = new HashSet<>();
        HashMap<Long, GeoInfo> supportHashMap = new HashMap<>();
        for (GeoInfo geoInfo : geoInfos) {
            float distance = location.distanceTo(geoInfo.getLocation());
            Log.d("LocationWatcher", geoInfo.getTitle() + " " + distance);
            if (distance < 100F) {
                currentNearGeoInfoIds.add(geoInfo.getId());
            }
            supportHashMap.put(geoInfo.getId(), geoInfo);
        }

        Intent locationWatcherNotify = new Intent(BROADCAST_ACTION);
        if (lastNearGeoInfoIds == null) {
            if (currentNearGeoInfoIds.size() > 0)
                lastNearGeoInfoIds = currentNearGeoInfoIds;
            locationWatcherNotify.putExtra(BROADCAST_RES, false);
        } else if (currentNearGeoInfoIds.size() == 0) {
            lastNearGeoInfoIds = null;
            locationWatcherNotify.putExtra(BROADCAST_RES, false);
        } else {
            Date now = DateHelper.now();
            long delta = DateHelper.millisecondsBetween(now, lastLocationUpdate);
            lastLocationUpdate = now;
            boolean anyEdit = false;
            for (Long geoInfoId : currentNearGeoInfoIds) {
                if (lastNearGeoInfoIds.contains(geoInfoId)) {
                    GeoInfo geoInfo = supportHashMap.get(geoInfoId);
                    geoInfo.addSpendMilliseconds(delta);
                    geoInfo.save();
                    anyEdit = true;
                }
            }
            locationWatcherNotify.putExtra(BROADCAST_RES, anyEdit);
            if (anyEdit)
                logSpentTime();
        }

        sendBroadcast(locationWatcherNotify);
        Log.d("LocationWatcher", "sendBroadcast");
    }

    private void logSpentTime() {
        List<GeoInfo> geoInfos = GeoInfo.listAll(GeoInfo.class);
        StringBuilder stringBuilder = new StringBuilder();
        for (GeoInfo geoInfo : geoInfos) {
            stringBuilder.append(geoInfo.getTitle());
            stringBuilder.append(":");
            stringBuilder.append(TimeUnit.MILLISECONDS.toMinutes(geoInfo.getSpentTimeMilliseconds()));
            stringBuilder.append(" minutes (");
            stringBuilder.append(geoInfo.getSpentTimeMilliseconds());
            stringBuilder.append(" milliseconds) \n");
        }
        Log.d("LocationWatcher", stringBuilder.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LocationWatcher", "onDestroy");
    }
}
