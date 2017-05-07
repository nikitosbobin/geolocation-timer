package com.nikit.bobin.geolocationtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class GeoInfoListActivity extends AppCompatActivity {
    private static final int GET_GEO_INFO_REQUEST_CODE = 12;

    private LocationWatcherReceiver watcherReceiver;
    private IntentFilter watcherReceiverFilter;

    @BindView(R.id.geo_info_list)
    ListView geoInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_info_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        geoInfoList.setAdapter(new GeoInfoListViewAdapter(this));
        watcherReceiver = new LocationWatcherReceiver();
        watcherReceiverFilter = new IntentFilter(LocationWatcher.BROADCAST_ACTION);
        watcherReceiver.register();
    }

    @OnClick(R.id.fab)
    public void clickFab(View view) {
        startActivityForResult(
                new Intent(this, CreateGeoInfoActivity.class),
                GET_GEO_INFO_REQUEST_CODE);
    }

    @OnItemClick(R.id.geo_info_list)
    public void geoInfoListClick(long id) {
        GeoInfo geoInfo = GeoInfo.findById(GeoInfo.class, id);
        if (geoInfo != null) {
            Intent intent = new Intent(this, CreateGeoInfoActivity.class);
            intent.putExtra(CreateGeoInfoActivity.GEO_INFO_DATABASE_ID, id);
            startActivityForResult(intent, GET_GEO_INFO_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_GEO_INFO_REQUEST_CODE
                && resultCode == RESULT_OK
                && data.hasExtra(CreateGeoInfoActivity.GEO_INFO_DATABASE_ID)) {
            geoInfoList.invalidateViews();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        watcherReceiver.unregister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        watcherReceiver.unregister();
    }

    @Override
    protected void onResume() {
        super.onResume();
        watcherReceiver.register();
        geoInfoList.invalidateViews();
    }

    private class LocationWatcherReceiver extends BroadcastReceiver {
        private boolean registered;

        public void register() {
            if (!registered) {
                registerReceiver(watcherReceiver, watcherReceiverFilter);
                registered = true;
            }
        }

        public void unregister() {
            if (registered) {
                unregisterReceiver(watcherReceiver);
                registered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(LocationWatcher.BROADCAST_RES)) {
                boolean needUpdate = intent.getBooleanExtra(
                        LocationWatcher.BROADCAST_RES,
                        false);
                if (needUpdate)
                    geoInfoList.invalidateViews();
                Log.d("LocationWatcherReceiver", "onReceive: " + needUpdate);
            } else {
                Log.d("LocationWatcherReceiver", "onReceive: without data");
            }
        }
    }
}
