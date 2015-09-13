package gr.hua.dit.smartt;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;



public class MapsActivity extends FragmentActivity implements LocationProvider.LocationCallback {

    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    GPSTracker gps;
    NetworkTracker nettracker;

    CameraUpdate cameraUpdate;
    MarkerOptions marker = new MarkerOptions();
    Marker positionmarker;
    Utilities ut = new Utilities(this);

    private LocationProvider mLocationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //setUpMapIfNeeded();

        mLocationProvider = new LocationProvider(this, this);

        nettracker = new NetworkTracker(MapsActivity.this);
        gps = new GPSTracker(MapsActivity.this);

        try {
            // Loading map
            initilizeMap();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    @Override
    protected void onResume() {
        super.onResume();

        nettracker.getLocation();
        gps = new GPSTracker(MapsActivity.this);
        setUpMapIfNeeded();
        mLocationProvider.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect();
        initilizeMap();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nettracker.stopUsingGPS();
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */

    private void initilizeMap() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();



            // check if map is created successfully or not
            if (mMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
            if((!nettracker.isGPSEnabled)&&isOnline()){
                nettracker.showSettingsAlert();
            }
            positioncheck();
        }
        if(ut.loadStoredValue("map_type", "normal").equals("normal")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }else if(ut.loadStoredValue("map_type", "normal").equals("hybrid")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }else if(ut.loadStoredValue("map_type", "normal").equals("terrain")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
    }

    public void positioncheck() {

        // check if GPS enabled
        if(nettracker.canGetLocation()){

            final double latitude = nettracker.getLatitude();
            final double longitude = nettracker.getLongitude();



            marker = new MarkerOptions().position(new LatLng(latitude, longitude));
            // adding marker
            positionmarker = mMap.addMarker(marker);

            if(isOnline()) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 11);
            }else {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13);
            }

            mMap.animateCamera(cameraUpdate);

            nettracker.getLocation();
            if(nettracker.appAccuracy > 100 && nettracker.appAccuracy!=999) {
                if(!nettracker.isGPSEnabled) {
                    nettracker.showBadAccuracyAlert();
                }else {
                    Toast.makeText(getApplicationContext(), "message!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    public void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("I am here!");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }
}
