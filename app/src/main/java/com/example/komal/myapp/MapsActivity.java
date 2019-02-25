package com.example.komal.myapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code=99;
    private int ProximityRadius=100000;
    private double lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            checkUserLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = ( SupportMapFragment ) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    public void onClick(View view){
        String hospital="hospital";
        Object transferData[]=new Object[2];
        GetNearbyPlaces getNearbyPlaces=new GetNearbyPlaces();



        switch (view.getId()){
            case R.id.search_address:
                EditText addressField=(EditText)findViewById(R.id.location_search);
                String address=addressField.getText().toString();
                List<Address> addressList=null;
                MarkerOptions userMarkerOptions=new MarkerOptions();
                if(!TextUtils.isEmpty(address)){
                    Geocoder geocoder=new Geocoder(this);
                    try {
                        addressList=geocoder.getFromLocationName(address,6);
                        if(addressList!=null){
                        for(int i=0;i<addressList.size();i++){
                            Address userAddress=addressList.get(i);
                            LatLng latLng=new LatLng(userAddress.getLatitude(),userAddress.getLongitude());
                            userMarkerOptions.position(latLng);
                            userMarkerOptions.title(address);
                            userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            mMap.addMarker(userMarkerOptions);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(3));
                        }
                    }
                    else{
                            Toast.makeText(this,"location not found...",Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    Toast.makeText(this,"please type address",Toast.LENGTH_SHORT).show();
                }
                break;



            case R.id.hospitals_nearby:
                mMap.clear();
                String url=getUrl(lat,lng,hospital);
                transferData[0]=mMap;
                transferData[1]=url;
                getNearbyPlaces.execute(transferData);
                Toast.makeText(this,"Searching Nearby hospitals..",Toast.LENGTH_SHORT).show();
                Toast.makeText(this,"Showing Nearby hospitals..",Toast.LENGTH_SHORT).show();

                break;
        }

    }

    private String getUrl(double lat,double  lng,String  nearbyPlace) {
        StringBuilder googleURL=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location="+lat+","+lng);
        googleURL.append("&radius="+ProximityRadius);
        googleURL.append("&type="+nearbyPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key="+"AIzaSyAf16qBrn1GJ_gMxDLo8ppX1AQXsfuoG7g");
        Log.d("MapsActivity","url="+googleURL.toString());
        return googleURL.toString();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }



    }

    public boolean checkUserLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code);
            }
            else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code);
            }
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Request_User_Location_Code:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        if(googleApiClient==null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else {
                    Toast.makeText(this,"Permission Denied...",Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }




    protected synchronized void buildGoogleApiClient(){
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
     }

    @Override
    public void onLocationChanged(Location location) {
        lat=location.getLatitude();
        lng=location.getLongitude();
        lastLocation=location;
        if(currentUserLocationMarker!=null){
            currentUserLocationMarker.remove();
        }
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
//        MarkerOptions markerOptions=new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("user current location");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//        currentUserLocationMarker=mMap.addMarker(markerOptions);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomBy(12));
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(100000)
                .strokeColor(Color.RED)
                .fillColor(Color.RED));
          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,5.02f));


        if(googleApiClient!=null)
        {
          LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
       locationRequest=new LocationRequest();
       locationRequest.setInterval(1100);
       locationRequest.setFastestInterval(1100);
       locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
       if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
           LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
       }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
