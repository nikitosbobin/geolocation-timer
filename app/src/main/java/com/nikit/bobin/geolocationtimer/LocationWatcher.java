package com.nikit.bobin.geolocationtimer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.TimeUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocationWatcher extends Service
        implements GoogleApiClient.ConnectionCallbacks, LocationListener {
    public final static String BROADCAST_ACTION =
            "com.nikit.bobin.geolocationtimer.location_changed";
    public final static String BROADCAST_RES = "gfdgsdf";

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(5));
        //locationRequest.setInterval(TimeUnit.MINUTES.toMillis(10));
        //locationRequest.setFastestInterval(TimeUnit.MINUTES.toMillis(2));
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
            createNotification(this, "Йопта", "Succeed start location watch");
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
        Date now = DateHelper.now();
        boolean anyEdit = false;
        for (GeoInfo geoInfo : geoInfos) {
            if (!geoInfo.isEnabled() ||
                    DateHelper.millisecondsBetween(
                            DateHelper.now(),
                            geoInfo.getPeriodStart()) < 0L) {
                Log.d("LocationWatcher", String.format(
                        "Start time %s is earlier now",
                        DateUtils.formatDateTime(this, geoInfo.getPeriodStart().getTime(), 0)));
                geoInfo.setLastLocationUpdate(null);
                geoInfo.save();
                continue;
            }
            float distance = location.distanceTo(geoInfo.getLocation());
            Log.d("LocationWatcher", geoInfo.getTitle() + " " + distance);
            if (distance < 100F) {
                if (geoInfo.setLastLocationUpdate(now))
                    anyEdit = true;
            } else {
                geoInfo.setLastLocationUpdate(null);
            }
            geoInfo.save();
        }
        Intent locationWatcherNotify = new Intent(BROADCAST_ACTION);
        locationWatcherNotify.putExtra(BROADCAST_RES, anyEdit);
        if (anyEdit)
            logSpentTime();
        sendBroadcast(locationWatcherNotify);
        Log.d("LocationWatcher", "sendBroadcast");
    }

    private void logSpentTime() {
        List<GeoInfo> geoInfos = GeoInfo.listAll(GeoInfo.class);
        StringBuilder stringBuilder = new StringBuilder();
        for (GeoInfo geoInfo : geoInfos) {
            stringBuilder.append(geoInfo.getTitle());
            stringBuilder.append(":");
            stringBuilder.append(DateUtils.formatElapsedTime(geoInfo.getSpentTimeSeconds()));
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

    private void createNotification(Context context, String title, String text) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text);
        notificationManager.notify(11, builder.build());
    }
}
