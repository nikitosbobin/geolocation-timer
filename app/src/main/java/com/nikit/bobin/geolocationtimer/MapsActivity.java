package com.nikit.bobin.geolocationtimer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity
        extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    public static final String DYNAMIC_LATITUDE = "latitude";
    public static final String DYNAMIC_LONGITUDE = "longitude";

    public static final String STATIC_LATITUDES = "latitudes";
    public static final String STATIC_LONGITUDES = "longitudes";
    public static final String STATIC_TITLES = "titles";
    public static final String STATIC_COLORS = "colors";

    private GoogleMap googleMap;

    private LatLng currentLocation;
    private Marker currentMarker;
    private Circle currentCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        ButterKnife.bind(this);
        mapFragment.getMapAsync(this);
    }

    private void loadStaticMarkers(Intent intent) {
        if (!intent.hasExtra(STATIC_LATITUDES)
                || !intent.hasExtra(STATIC_LONGITUDES)
                || !intent.hasExtra(STATIC_TITLES)
                || !intent.hasExtra(STATIC_COLORS))
            return;
        double[] latitudes = intent.getDoubleArrayExtra(STATIC_LATITUDES);
        double[] longitudes = intent.getDoubleArrayExtra(STATIC_LONGITUDES);
        String[] titles = intent.getStringArrayExtra(STATIC_TITLES);
        float[] colors = intent.getFloatArrayExtra(STATIC_COLORS);
        int length = min(latitudes.length, longitudes.length, titles.length, colors.length);
        for (int i = 0; i < length; ++i) {
            LatLng latLng = new LatLng(latitudes[i], longitudes[i]);
            putMarker(latLng, titles[i], colors[i], false);
        }
    }

    private int min(int... ints) {
        if (ints == null || ints.length == 0)
            return Integer.MIN_VALUE;
        int minItem = ints[0];
        for (int i = 0; i < ints.length; ++i) {
            if (ints[i] < minItem)
                minItem = ints[i];
        }
        return minItem;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMapClickListener(this);
        enableMyLocationWithPermissionGrant();
        Intent intent = getIntent();
        loadStaticMarkers(intent);
        loadDynamicMarker(intent);
    }

    private void loadDynamicMarker(Intent intent) {
        if (intent.hasExtra(DYNAMIC_LATITUDE) && intent.hasExtra(DYNAMIC_LONGITUDE)) {
            LatLng latLng = new LatLng(
                    intent.getDoubleExtra(DYNAMIC_LATITUDE, 0D),
                    intent.getDoubleExtra(DYNAMIC_LONGITUDE, 0D));
            currentLocation = latLng;
            putMarker(latLng, "Current", BitmapDescriptorFactory.HUE_RED, true);
        }
    }

    private void putMarker(LatLng latLng, String title, float color, boolean needReplace) {
        Marker currentMarker = this.googleMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(color)));
        Circle currentCircle = this.googleMap.addCircle(
                new CircleOptions()
                        .center(latLng)
                        .radius(100D));
        if (needReplace) {
            if (this.currentMarker != null)
                this.currentMarker.remove();
            if (this.currentCircle != null)
                this.currentCircle.remove();
            this.currentMarker = currentMarker;
            this.currentCircle = currentCircle;
        }
    }

    private void enableMyLocationWithPermissionGrant() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        currentLocation = latLng;
        putMarker(latLng, "Current", BitmapDescriptorFactory.HUE_RED, true);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && permissions.length == 2
                && grantResults.length == 2
                && Objects.equals(permissions[0], Manifest.permission.ACCESS_FINE_LOCATION)
                && Objects.equals(permissions[1], Manifest.permission.ACCESS_COARSE_LOCATION)
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            try {
                googleMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Toast
                        .makeText(this, "You should grant location permission", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @OnClick(R.id.submit_location_button)
    public void submitLocation() {
        if (currentLocation == null) {
            Toast.makeText(this, "You should select location", Toast.LENGTH_SHORT).show();
        } else {
            Intent result = new Intent();
            result.putExtra(DYNAMIC_LATITUDE, currentLocation.latitude);
            result.putExtra(DYNAMIC_LONGITUDE, currentLocation.longitude);
            setResult(RESULT_OK, result);
            finish();
        }
    }
}
