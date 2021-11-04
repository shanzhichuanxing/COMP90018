package com.example.homepage.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.List;

public class addressToLocation {
    public static LatLng getLocationFromAddress( Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List address = null;
        LatLng p1 = (LatLng)null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = (Address)address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException var7) {
            var7.printStackTrace();
        }

        return p1;
    }
}
