package com.nikit.bobin.geolocationtimer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity
        extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";

    private GoogleMap mMap;
    private Marker currentMarker;
    private LatLng currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        ButterKnife.bind(this);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        enableMyLocationWithPermissionGrant();
        Intent intent = getIntent();
        if (intent.hasExtra(LATITUDE) && intent.hasExtra(LONGITUDE)) {
            LatLng latLng = new LatLng(
                    intent.getDoubleExtra(LATITUDE, 0D),
                    intent.getDoubleExtra(LONGITUDE, 0D));
            if (currentMarker != null)
                currentMarker.remove();
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Pointer"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    private void enableMyLocationWithPermissionGrant() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
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
        if (currentMarker != null)
            currentMarker.remove();
        currentLocation = latLng;
        currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Pointer"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
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
                mMap.setMyLocationEnabled(true);
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
            result.putExtra(LATITUDE, currentLocation.latitude);
            result.putExtra(LONGITUDE, currentLocation.longitude);
            setResult(RESULT_OK, result);
            finish();
        }
    }
}
