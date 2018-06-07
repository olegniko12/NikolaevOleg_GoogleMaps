package olegnikolaev.mymapapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean isSatellite = false;

    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private boolean getMyLocation = false;
    private boolean gotMyLocationOneTime;
    private double latitude, longitude;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 5;

    private EditText trackingButton;
    private boolean isTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Add marker on birthplace

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        LatLng moscow = new LatLng(55.7558, 37.6173);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.addMarker(new MarkerOptions().position(moscow).title("Born in Moscow"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(moscow));

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("MyMapsApp", "Failed Permission Check - FINE LOCATION");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d("MyMapsApp", "Failed Permission Check - COARSE LOCATION");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            mMap.setMyLocationEnabled(true);
        }

        locationSearch = (EditText) findViewById(R.id.addressText);
        //trackingButton = (EditText) findViewById(R.id.trackingButton);

        gotMyLocationOneTime = false;
        getLocation();

    }

    public void changeView(View view){
        isSatellite = !isSatellite;
        if (isSatellite){
            mMap.setMapType(mMap.MAP_TYPE_NORMAL);

        } else {
            mMap.setMapType(mMap.MAP_TYPE_SATELLITE);
        }
    }

    public void onSearch(View v){
        String location = locationSearch.getText().toString();

        List<Address> addressList = null;
        List<Address> addressListZip = null;

        //Use location manager for user location
        //Implement the LocationListener interface to setup location services
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMapsApp", "onSearch:  location = " + location);
        Log.d("MyMapsApp", "onSearch: provider " + provider);

        //Check last known location, need to list provider
        LatLng userLocation = null;

        try {

            if(locationManager != null){
                Log.d("MyMapsApp", "onSearch: locationManager is not null");

                if((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is :" + myLocation.getLatitude() + " " +myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is :" + myLocation.getLatitude() + " " +myLocation.getLongitude());

                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                } else {
                    Log.d("MyMapsApp", "onSearch: myLocation is null from getLastKnownLocation");
                }
            }

        } catch (SecurityException | IllegalArgumentException e){
            Log.d("MyMapsApp", "onSearch: got Exception getLastKnownLocation");
            Toast.makeText(this, "onSearch: Exception getLastKnownLocation", Toast.LENGTH_SHORT).show();
        }

        if (!location.matches("")){
            Log.d("MyMapsApp", "onSearch: location field is populated");
            Toast.makeText(this, "onSearch: location field is populated", Toast.LENGTH_SHORT).show();

            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {
                addressList = geocoder.getFromLocationName(location, 100, userLocation.latitude - (5.0/60), userLocation.longitude-(5.0/60),
                        userLocation.latitude+(5.0/60), userLocation.longitude+(5.0/60));
                Log.d("MyMapsApp", "onSearch: addressList is created");
            } catch (IOException e){
                e.printStackTrace();
            }

            if(!addressList.isEmpty()){
                Log.d("MyMapsApp", "onSearch: addressList has " + addressList.size() + "elements");
                Toast.makeText(this,"onSearch: addressList has " + addressList.size() + "elements", Toast.LENGTH_SHORT).show();

                for(int i = 0; i <addressList.size(); i++){
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    mMap.addMarker(new MarkerOptions().position(latLng).title(i+": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                }

            }

        }

    }

    public void getLocation(){

        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //Get GPS status

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled)Log.d("MyMapsApp", "getLocation: GPS is enabled");

            if(!isGPSEnabled && !isNetworkEnabled){
                Log.d("MyMapsApp", "getLocation: no provider enabled!");
            } else {
                if (isNetworkEnabled){
                    //Request location updates
                    if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if (isGPSEnabled){
                    //locationManager request for GPS_PROVIDER
                    //CODE HERE.....
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                }

            }

        } catch (Exception e){
            Log.d("MyMapsApp", "getLocation: Exception in getLocation");
            e.printStackTrace();
        }


    }

    //LocationListener to setup callbacks for requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            dropAmarker(LocationManager.NETWORK_PROVIDER);

            if(gotMyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                gotMyLocationOneTime = true;
            } else {
                if((ActivityCompat.checkSelfPermission( MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status has changed");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            dropAmarker(LocationManager.GPS_PROVIDER);

            if(gotMyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                gotMyLocationOneTime = true;
            } else {
                if((ActivityCompat.checkSelfPermission( MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status has changed");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    void dropAmarker(String provider){
        if (locationManager != null){
            if((ActivityCompat.checkSelfPermission( MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
                return;
            }

            myLocation = locationManager.getLastKnownLocation(provider);

            if (myLocation != null){
                latitude = myLocation.getLatitude();
                longitude= myLocation.getLongitude();
            }
        }

        LatLng userLocation = null;

        if(myLocation == null){
            Toast.makeText(this, "dropAmarker: myLocation is null", Toast.LENGTH_SHORT).show();
        } else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
            Toast.makeText(this, "dropAmarker: myLocation is " + userLocation, Toast.LENGTH_SHORT).show();
            if (provider == LocationManager.GPS_PROVIDER){
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.RED));

                Circle circleOuter = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(3)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));

                Circle circleOuter2 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(5)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));
            } else {
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.BLUE));

                Circle circleOuter = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(3)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));

                Circle circleOuter2 = mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(5)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));
            }
            mMap.animateCamera(update);
        }

    }

    public void TrackMyLocation (View view){
        isTracking = !isTracking;
        Toast.makeText(this,"trackMyLocation: tracking is now" + isTracking, Toast.LENGTH_SHORT).show();
        if (isTracking){
            if(isGPSEnabled){
                dropAmarker(LocationManager.GPS_PROVIDER);
            } else if (isNetworkEnabled) {
                dropAmarker(LocationManager.NETWORK_PROVIDER);
            }
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
        } else {
            getLocation();
        }



    }

    public void clearMarkers(View view){
        mMap.clear();
    }


}
