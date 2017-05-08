package com.nikit.bobin.geolocationtimer;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateGeoInfoActivity extends AppCompatActivity {
    private static final int GET_LOCATION_REQUEST_CODE = 12;
    public static final String GEO_INFO_DATABASE_ID = "id";

    private LocationHelper locationHelper;
    private GeoInfo loadedGeoInfo;

    private double longitude;
    private double latitude;

    @BindView(R.id.geo_info_title)
    EditText geoInfoTitle;
    @BindView(R.id.location_label)
    EditText locationLabel;
    @BindView(R.id.datePicker)
    DatePicker datePicker;
    @BindView(R.id.period_selector)
    EditText periodSelector;
    @BindView(R.id.notify_period_end_switch)
    Switch notifyPeriodEndSwitch;
    @BindView(R.id.clear_timer_each_period_switch)
    Switch clearTimerEachPeriodSwitch;
    @BindView(R.id.geo_info_enabled_switch)
    Switch geoInfoEnabledSwitch;
    @BindView(R.id.delete_button)
    Button deleteGeoInfoButton;
    @BindView(R.id.reset_timer_button)
    Button resetTimerButton;
    @BindView(R.id.time_unit_spinner)
    Spinner timeUnitSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_geo_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationHelper = new LocationHelper(this);

        ButterKnife.bind(this);
        loadFromIntent();
    }

    @OnClick(R.id.location_label)
    public void getLocation() {
        Intent intent = new Intent(this, MapsActivity.class);
        if (latitude != 0D && longitude != 0D) {
            intent.putExtra(MapsActivity.DYNAMIC_LATITUDE, latitude);
            intent.putExtra(MapsActivity.DYNAMIC_LONGITUDE, longitude);
        }

        List<GeoInfo> geoInfos;
        if (loadedGeoInfo == null || loadedGeoInfo.getId() < 1)
            geoInfos = GeoInfo.listAll(GeoInfo.class);
        else
            geoInfos = GeoInfo.find(GeoInfo.class, "id!=?", loadedGeoInfo.getId().toString());
        double[] latitudes = new double[geoInfos.size()];
        double[] longitudes = new double[latitudes.length];
        String[] titles = new String[latitudes.length];
        float[] colors = new float[latitudes.length];
        for (int i = 0; i < latitudes.length; ++i) {
            GeoInfo currentGeoInfo = geoInfos.get(i);
            latitudes[i] = currentGeoInfo.getLatitude();
            longitudes[i] = currentGeoInfo.getLongitude();
            titles[i] = currentGeoInfo.getTitle();
            colors[i] = BitmapDescriptorFactory.HUE_CYAN;
        }

        intent.putExtra(MapsActivity.STATIC_LATITUDES, latitudes);
        intent.putExtra(MapsActivity.STATIC_LONGITUDES, longitudes);
        intent.putExtra(MapsActivity.STATIC_TITLES, titles);
        intent.putExtra(MapsActivity.STATIC_COLORS, colors);

        startActivityForResult(intent, GET_LOCATION_REQUEST_CODE);
    }

    @OnClick(R.id.reset_timer_button)
    public void resetTimer() {
        loadedGeoInfo.resetSpentTime();
        loadedGeoInfo.save();
        Toast.makeText(this, "Timer was reset", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_LOCATION_REQUEST_CODE && resultCode == RESULT_OK) {
            latitude = data.getDoubleExtra(MapsActivity.DYNAMIC_LATITUDE, 0D);
            longitude = data.getDoubleExtra(MapsActivity.DYNAMIC_LONGITUDE, 0D);
            Address address = locationHelper.getAddress(latitude, longitude);
            if (address != null)
                locationLabel.setText(address.getAddressLine(0));
        }
    }

    private void loadFromIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(GEO_INFO_DATABASE_ID)) {
            long id = intent.getLongExtra(GEO_INFO_DATABASE_ID, -1L);
            GeoInfo geoInfo = GeoInfo.findById(GeoInfo.class, id);
            if (geoInfo != null) {
                loadedGeoInfo = geoInfo;
                latitude = geoInfo.getLatitude();
                longitude = geoInfo.getLongitude();
                Address address = locationHelper.getAddress(latitude, longitude);
                if (address != null)
                    locationLabel.setText(address.getAddressLine(0));

                String title = geoInfo.getTitle();
                geoInfoTitle.setText(title);

                geoInfoEnabledSwitch.setChecked(geoInfo.isEnabled());
                clearTimerEachPeriodSwitch.setChecked(geoInfo.isClearTimerEachPeriod());
                notifyPeriodEndSwitch.setChecked(geoInfo.isNotifyPeriodEnds());

                periodSelector.setText(String.format(Locale.getDefault(), "%d", geoInfo.getPeriodDays()));

                Date periodStart = geoInfo.getPeriodStart();
                DateHelper.updatePicker(periodStart, datePicker);

                deleteGeoInfoButton.setVisibility(View.VISIBLE);
                resetTimerButton.setVisibility(View.VISIBLE);
                return;
            }
        }
        resetTimerButton.setVisibility(View.GONE);
        deleteGeoInfoButton.setVisibility(View.GONE);
    }

    private Integer getPeriod() {
        Editable text = periodSelector.getText();
        if (text == null || text.length() == 0)
            return null;
        try {
            return Integer.parseInt(text.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @OnClick(R.id.delete_button)
    public void delete() {
        if (loadedGeoInfo == null)
            return;
        if (loadedGeoInfo.delete()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(GEO_INFO_DATABASE_ID, -1L);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(
                    this,
                    String.format(
                            Locale.getDefault(),
                            "Can not delete item with id: %d",
                            loadedGeoInfo.getId()), Toast.LENGTH_SHORT
            ).show();
        }
    }

    @OnClick(R.id.save_geo_info_button)
    public void save() {
        Editable locationText = locationLabel.getText();
        Editable title = geoInfoTitle.getText();
        if (title == null || title.length() == 0) {
            Toast.makeText(this, "You should set title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude == 0L && longitude == 0L && (locationText == null || locationText.length() == 0)) {
            Toast.makeText(this, "You should set location", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean enabled = geoInfoEnabledSwitch.isChecked();
        GeoInfo geoInfo;
        if (loadedGeoInfo == null)
            geoInfo = new GeoInfo(title.toString(), latitude, longitude);
        else {
            geoInfo = loadedGeoInfo;
            geoInfo.setTitle(title.toString());
            geoInfo.setLatitude(latitude);
            geoInfo.setLongitude(longitude);
        }
        geoInfo.setEnabled(enabled);

        boolean needNotify = notifyPeriodEndSwitch.isChecked();
        boolean clearEachPeriod = clearTimerEachPeriodSwitch.isChecked();
        if (needNotify || clearEachPeriod) {
            Integer period = getPeriod();
            if (period == null) {
                Toast.makeText(this, "You should set period", Toast.LENGTH_SHORT).show();
                return;
            }
            if (period == 0) {
                Toast.makeText(this, "You should set non-zero period", Toast.LENGTH_SHORT).show();
                return;
            }
            geoInfo.setPeriodDays(period);
        }
        geoInfo.setNotifyPeriodEnds(needNotify);
        geoInfo.setClearTimerEachPeriod(clearEachPeriod);

        Date startPeriod = DateHelper.createDate(datePicker);
        geoInfo.setPeriodStart(startPeriod);

        long id = geoInfo.save();
        if (id < 1) {
            Toast.makeText(this, "Saving error", Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(GEO_INFO_DATABASE_ID, id);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}
