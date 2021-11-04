package com.example.homepage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,LocationListener {

    boolean isPermissionGranted;
    boolean isProviderEnabled;
    GoogleMap mMap;
    FloatingActionButton fab;
    private FusedLocationProviderClient mLocationClient;
    EditText locSearch;
    ImageButton searchIcon;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    LocationManager locationManager;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String userID;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fab = findViewById(R.id.fab);
//        locSearch = findViewById(R.id.et_search);
//        searchIcon = findViewById(R.id.search_icon);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("Trace");

        checkMyPermission();
        isGPSEnabled();

        if(isPermissionGranted){
            if(isProviderEnabled){
                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                supportMapFragment.getMapAsync(this);
            }
        }
        /**
        mLocationClient = new FusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //if(locationResult==null){
                  //  return;
                //}
                Location location = locationResult.getLastLocation();
                mDatabase.child("time").child("Latitude").setValue(location.getLatitude());
                //Toast.makeText(MainActivity.this, location.getLatitude()+"/n"+location.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        };*/
        searchIcon.setOnClickListener(this::geoLocate);
    }

    private void checkMyPermission() {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermissionGranted = true;
                getLocationUpdates();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(),"");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    /**
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        mLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Location location = task.getResult();
                gotoLocation(location.getLatitude(),location.getLongitude());
            }
        });
    }*/

    /**
    private void showTrace(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds:snapshot.getChildren()){
                        for(DataSnapshot ds1:ds.getChildren()){
                            String key = ds1.getKey(); //date
                            Double lat = Double.valueOf(Objects.requireNonNull(ds1.child("Lat").getValue()).toString());
                            Double lng = Double.valueOf(Objects.requireNonNull(ds1.child("Lng").getValue()).toString());
                            //gotoLocation(lat,lng);
                            //mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)));
                        }
                    }
                } else {
                    Log.d("Tag", "No snapshot");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    private boolean isGPSEnabled() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(providerEnabled){
            Toast.makeText(this, "GPS is enabled", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

    private void gotoLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        mMap.moveCamera(cameraUpdate);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }


    @SuppressLint("MissingPermission")
    private void getLocationUpdates(){
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 1, MainActivity.this);

    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            String[] array = getGPSLocalTime(location.getTime());
            String date = array[0];
            String time = array[1];
            Double lat = location.getLatitude();
            Double lng = location.getLongitude();

            Toast.makeText(this, "Location: "+location.getLatitude()+", "+location.getLongitude() +", "+time, Toast.LENGTH_SHORT).show();

            mDatabase.child(userID).child(date).child(time).child("Lat").setValue(lat);
            mDatabase.child(userID).child(date).child(time).child("Lng").setValue(lng);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String[] getGPSLocalTime(long gpsTime) throws ParseException {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(gpsTime);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timef = new SimpleDateFormat("HH:mm:ss");
        Date calendarTime = calendar.getTime();
        String date = String.valueOf(datef.format(calendarTime));
        String time = timef.format(calendarTime);
        String array[] = {date,time};
        return array;
    }

    //Geocoder
    private void geoLocate(View view) {
        String locationName = locSearch.getText().toString();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if(addresses.size()>0){
                Address address = addresses.get(0);
                gotoLocation(address.getLatitude(), address.getLongitude());
                addMarker(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //add marker
    private void addMarker(Address address){
        mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(), address.getLongitude())));
        Toast.makeText(this, address.getLocality(), Toast.LENGTH_SHORT).show();

    }
}

