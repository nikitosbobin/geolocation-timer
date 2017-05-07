package com.nikit.bobin.geolocationtimer;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class LocationHelper {
    private Geocoder geocoder;

    public LocationHelper(Context context) {
        geocoder = new Geocoder(context);
    }

    public Address getAddress(LatLng location) {
        return getAddress(location.latitude, location.longitude);
    }

    public Address getAddress(Location location) {
        return getAddress(location.getLatitude(), location.getLongitude());
    }

    public Address getAddress(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1);
            if (addresses.size() == 0)
                return null;
            return addresses.get(0);
        } catch (IOException e) {
            return null;
        }
    }
}
