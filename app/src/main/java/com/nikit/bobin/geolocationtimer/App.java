package com.nikit.bobin.geolocationtimer;

import android.content.Intent;

import com.orm.SugarApp;

public class App extends SugarApp {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!ServiceHelper.isServiceRunning(this, LocationWatcher.class)) {
            startService(new Intent(this, LocationWatcher.class));
        }
    }
}
