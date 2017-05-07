package com.nikit.bobin.geolocationtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemMessagesListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SystemMessagesListener", intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (!ServiceHelper.isServiceRunning(context, LocationWatcher.class)) {
                context.startService(new Intent(context, LocationWatcher.class));
            }
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d("SystemMessagesListener", "screen on");
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d("SystemMessagesListener", "screen off");
        }
    }
}
