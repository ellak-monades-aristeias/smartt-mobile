package gr.hua.dit.smartt;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class MapsActivity extends AppCompatActivity implements LocationProvider.LocationCallback {
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    public ArrayList<String> stopLines = new ArrayList<String>();
    public View v;
    boolean isFromRoutes = false;
    ArrayList<LatLng> waypointsRouteList = new  ArrayList<LatLng>();

    ArrayList<LatLng> nearstopslatlng = new ArrayList<LatLng>();


    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    GPSTracker gps;
    NetworkTracker nettracker;

    CameraUpdate cameraUpdate;
    MarkerOptions marker = new MarkerOptions();
    ArrayList<MarkerOptions> markersList = new ArrayList<MarkerOptions>();
    Marker positionmarker;
    Utilities ut = new Utilities(this);
    //public View CustomMarker = getLayoutInflater().inflate(R.layout.markerinfowindowlayout, null);

    private LocationProvider mLocationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //setUpMapIfNeeded();

        if(ut.loadStoredValue("app_mac_address", "none").equals("none")) {
            ut.savePreferences("app_mac_address", ut.getMacAddress());
            Log.i("MAC",String.valueOf(ut.getMacAddress()));
        }



        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mLocationProvider = new LocationProvider(this, this);

        nettracker = new NetworkTracker(MapsActivity.this);
        gps = new GPSTracker(MapsActivity.this);


        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            isFromRoutes = extra.getBoolean("isFromRouteValue");
            Log.i("nikosa", "nikos " + isFromRoutes);

            waypointsRouteList = extra.getParcelableArrayList("waypoints");
        }else {
            Log.i("nikosq","nikos " +isFromRoutes);
        }

        if(isFromRoutes) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            polylineOptions.width(5);
            polylineOptions.addAll(waypointsRouteList);

            mMap.addPolyline(polylineOptions);
//            mMap.addPolyline(new PolylineOptions()
//                    .addAll(waypointsRouteList)
//                    .width(5)
//                    .color(Color.GRAY));
        }
        try {
            // Loading map
            Log.i("RG", "Loading the map");
            initilizeMap();
            Log.i("RG", "After Loading the map");



        } catch (Exception e) {
            e.printStackTrace();
        }


//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//
//            @Override
//            public boolean onMarkerClick(Marker arg0) {
//
//                try {
//
//                    v = getLayoutInflater().inflate(R.layout.markerinfowindowlayout, null);
//                    Log.i("RG", "After Loading the map when marker click");
//                    v.isClickable();
//                    int selectedMarkerIndex = -1; // On-select from list-view this needs to be highlighted in marker as selected
//                    LinearLayout mapinnerLayout = (LinearLayout)v;
//                    mapinnerLayout.removeView(v);
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude)));
//
//                    LoadLinesfromStop loadlinesfromstop = new LoadLinesfromStop();
//                    loadlinesfromstop.execute(arg0.getSnippet()).get();
//
//                    //v.setVisibility(View.VISIBLE);
//                    TextView tv = (TextView) v.findViewById(R.id.StopName);
//                    tv.setText(arg0.getTitle());
//                    final ListView list = (ListView) v.findViewById(R.id.listView);
//                    MarkerInfoAdapter listAdapter = new MarkerInfoAdapter(getApplicationContext(), stopLines, list);
//                    //ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simplerow, stopLines);
//                    list.setAdapter(listAdapter);
////                    if (selectedMarkerIndex >= 0) {
////                        adapter.setSelected(selectedMarkerIndex);
////                        list.setSelection(selectedMarkerIndex);
////                    }
////                    int height = getMarkerInfoHeight(markerClass.size(), list);
//
//                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(450,300);
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                    //layoutParams.setMargins(0, getHeight() / 2 - (height+30), 0, 0);
//                    mapinnerLayout.addView(v, layoutParams);
//                    return true;
//                } catch (Exception e) {
//                    //e.printStackTrace();
//                    Log.i("RG", "After Loading the map when marker click sto catch " + e.getMessage());
//                    return false;
//                }
//
//            }
//        });
        // Setting a custom info window adapter for the google map
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {

                // Getting view from the layout file info_window_layout
                v = getLayoutInflater().inflate(R.layout.markerinfowindowlayout, null);
                v.isClickable();
                LoadLinesfromStop loadlinesfromstop = new LoadLinesfromStop();
                try {

                    Log.i("emfanisi grammwn3", "teleiwnei to views " + arg0.getSnippet());
                    loadlinesfromstop.execute(arg0.getSnippet()).get();

                    String stopName = arg0.getTitle();

                    // Getting reference to the TextView to set latitude
                    TextView tvName = (TextView) v.findViewById(R.id.StopName);
                    tvName.setText(stopName);
                    //String[] planets = new String[] { "Route1", "Route2", "Route3", "Route4"};
                    //ArrayList<String> planetList = new ArrayList<String>();
                    ListView list = (ListView) v.findViewById(R.id.ListItems);
                    //ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simplerow, stopLines);
                    MarkerInfoAdapter listAdapter = new MarkerInfoAdapter(getApplicationContext(), stopLines, list);
                    list.setAdapter(listAdapter);

//                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> adapter, View v, int position,
//                                                long arg3) {
//                            String value = (String) adapter.getItemAtPosition(position);
//                            Log.i("LIST", value);
//                            Toast.makeText(MapsActivity.this, value, Toast.LENGTH_SHORT).show();
//                            //Intent intent = new Intent("gr.hua.dit.smartt.MAIN");
//                            //startActivity(intent);
//
////                            Intent openStartingPoint = new Intent(RoutesActivity.this, MapsActivity.class);
////                            startActivity(openStartingPoint);
//                            // assuming string and if you want to get the value on click of list item
//                            // do what you intend to do on click of listview row
//                        }
//                    });

                    // Returning the view containing InfoWindow contents

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.i("RG", "After Loading the map when marker click sto catch " + e.getMessage());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Log.i("RG", "After Loading the map when marker click sto catch1 " + e.getMessage());
                }
                // Getting the position from the marker
                return v;
            }
        });






        //end of onCreate
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mMap.clear();
        Bundle extra = intent.getExtras();
        if (extra != null) {
            isFromRoutes = extra.getBoolean("isFromRouteValue");
            Log.i("nikosa2", "nikos " + isFromRoutes);

            waypointsRouteList = extra.getParcelableArrayList("waypoints");
            Log.i("nikos way ", "nikos " + waypointsRouteList.size());
        }

        if(isFromRoutes) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            polylineOptions.width(5);
            polylineOptions.addAll(waypointsRouteList);

            mMap.addPolyline(polylineOptions);

//            mMap.addPolyline(new PolylineOptions()
//                    .addAll(waypointsRouteList)
//                    .width(5)
//                    .color(Color.GRAY));


        }
        try {
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


        if (!(ut.loadStoredValue("app_email_address", "none").equals("none"))) {
            getSupportActionBar().setTitle(String.valueOf(ut.loadStoredValue("app_email_address", "none")));
            Log.i("LOADED-app_email_add",String.valueOf(ut.loadStoredValue("app_email_address", "none")));
        }

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

    private void addDrawerItems() {
        final String[] osArray = { "Login", "Routes", "Windows", "OS X", "Logout" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MapsActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();

                //final double latitude = 37.98392;
                //final double longitude = 23.72936;
                Log.i("RG", String.valueOf(id) + ' ' + String.valueOf(position) + ' ' + String.valueOf(osArray[position]));
                mMap.clear();

                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);

                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                if (id == 0) {

                    if (ut.getEmailAddress() != "none") {
                        Log.i("SV-email", String.valueOf(ut.getEmailAddress()));
                        Toast.makeText(MapsActivity.this, "Logged in as " + String.valueOf(ut.getEmailAddress()), Toast.LENGTH_SHORT).show();
                    } else {

                        Intent intent = new Intent("gr.hua.dit.smartt.LOGIN");
                        startActivity(intent);
                    }
                }

                if (id == 1) {
                    Toast.makeText(MapsActivity.this, "Routes!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("gr.hua.dit.smartt.ROUTES");
                    startActivity(intent);
                }

                if (id == 4) {
                    ut.clearPreferences();
                    Toast.makeText(MapsActivity.this, "Cleared Cache", Toast.LENGTH_SHORT).show();
                }

                //marker = new MarkerOptions().position(new LatLng(latitude, longitude));
                // adding marker
                positionmarker = mMap.addMarker(marker);


            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
           // mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
                    R.id.map)).getMap();


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
        mMap.setMyLocationEnabled(true);

        if ((!nettracker.isGPSEnabled) && isOnline()) {
            nettracker.showSettingsAlert();
        }
        positioncheck();

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Log.i("LOCATION", "in button click");
                if (mMap.getMyLocation() != null) {
                    Toast.makeText(
                            getApplicationContext(),
                            "Accuracy " + String.valueOf(mMap.getMyLocation().getAccuracy()) + "\n" +
                                    "Lat " + String.valueOf(mMap.getMyLocation().getLatitude()) + "\n" +
                                    "Lon " + String.valueOf(mMap.getMyLocation().getLongitude()),
                            Toast.LENGTH_SHORT).show();


                    LatLng latlon = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlon));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                    }
                    return true;
                }
            });



        if(ut.loadStoredValue("map_type", "normal").equals("normal")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }else if(ut.loadStoredValue("map_type", "normal").equals("hybrid")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }else if(ut.loadStoredValue("map_type", "normal").equals("terrain")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }

    }

    public void positioncheck() {

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        // check if GPS enabled
        if(nettracker.canGetLocation()){

            final double latitude = nettracker.getLatitude();
            final double longitude = nettracker.getLongitude();



            marker = new MarkerOptions().position(new LatLng(latitude, longitude));
            // adding marker
            //positionmarker = mMap.addMarker(marker);



            if(isOnline()) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 14);
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

            Location loc = new Location(String.valueOf(new LatLng(latitude, longitude)));

            handleNewLocation(loc);
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
        mMap.clear();
        if(isFromRoutes) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.GRAY);
            polylineOptions.width(5);
            polylineOptions.addAll(waypointsRouteList);

            mMap.addPolyline(polylineOptions);

//            mMap.addPolyline(new PolylineOptions()
//                    .addAll(waypointsRouteList)
//                    .width(5)
//                    .color(Color.GRAY));


        }
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
        if(!isFromRoutes) {
            LoadnearstopsTask loadnearstopsTask = new LoadnearstopsTask();
            loadnearstopsTask.execute(latLng);
        }

        Toast.makeText(MapsActivity.this, "Location changed", Toast.LENGTH_SHORT).show();

    }


    public class LoadnearstopsTask extends AsyncTask<LatLng, Void, List<GetStopsNearMe>> {

        List<GetStopsNearMe> mLatLng = new ArrayList<GetStopsNearMe>();

        @Override
        protected List<GetStopsNearMe> doInBackground(LatLng... Params) {

            LatLng latlngTemp = Params[0];
            String latTemp = String.valueOf(latlngTemp.latitude);
            String lngTemp = String.valueOf(latlngTemp.longitude);
            Map<String, String> params1 = new HashMap<String, String>();
            //String requestURL = getString(R.string.url_nearstops);
            //String requestURL = "http://83.212.116.159/smartt/backend/api/routes/nearstops?lat=38.0088432&lon=23.6591570";
            String requestURL = "http://83.212.116.159/smartt/backend/api/routes/nearstops?lat=" + latTemp + "&lon=" + lngTemp;

            try {
                HttpUtility.sendGetRequest(requestURL);

                String[] response = HttpUtility.readMultipleLinesRespone();

                for (String line : response) {
                    System.out.println(line);
                    Log.i("RG-res", String.valueOf(line));

                    try {
                        JSONObject jObject = new JSONObject(line);
                        String success = jObject.getString("stops");
                        Log.i("RG-login success", String.valueOf(success));


                        JSONArray jsonroutes = new JSONArray(success);
                        for (int i=0; i<jsonroutes.length(); i++) {
                            JSONObject actor = jsonroutes.getJSONObject(i);
                            String name = actor.getString("name_el");
                            String id = actor.getString("s_id");
                            String lat = actor.getString("lat");
                            String lon = actor.getString("lon");

                            mLatLng.add(new GetStopsNearMe(name, id, Double.parseDouble(lat), Double.parseDouble(lon)));
                            Log.i("RG-res-name", String.valueOf(lat) +","+ String.valueOf(lon));
                        }

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Intent intent = new Intent("gr.hua.dit.smartt.LOGIN");
                startActivity(intent);

            }
            HttpUtility.disconnect();

            Log.i("mlist", mLatLng.size()+"");
            return mLatLng;
        }

        @Override
        protected void onPostExecute(List<GetStopsNearMe> myLatLng) {

            markersList.clear();
            for(int i=0; i< mLatLng.size(); i++) {
                //marker = new MarkerOptions().position(new LatLng(latitude, longitude));
                markersList.add(new MarkerOptions().position(new LatLng(mLatLng.get(i).getStopLat(), mLatLng.get(i).getStopLng())));
                Log.i("emfanisi grammwn34", "teleiwnei to views " + String.valueOf(mLatLng.get(i).getId()));
                mMap.addMarker(new MarkerOptions().position(new LatLng(mLatLng.get(i).getStopLat(), mLatLng.get(i).getStopLng()))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop))
                        .title(mLatLng.get(i).getStopName())
                        .snippet(mLatLng.get(i).getId()+""));
            }

        }


    }



    public class LoadLinesfromStop extends AsyncTask<String, Void, List<String>> {



        @Override
        protected List<String> doInBackground(String... Params) {
            stopLines.clear();
            String stopId = Params[0];

            String requestURL = "http://83.212.116.159/smartt/backend/api/routes/linesfromstop?stop=" + stopId;

            try {
                HttpUtility.sendGetRequest(requestURL);

                String[] response = HttpUtility.readMultipleLinesRespone();

                for (String line : response) {
                    System.out.println(line);
                    Log.i("RG-res", String.valueOf(line));

                    try {
                        JSONObject jObject = new JSONObject(line);
                        String success = jObject.getString("lines");
                        Log.i("RG-login success", String.valueOf(success));


                        JSONArray jsonroutes = new JSONArray(success);
                        for (int i=0; i<jsonroutes.length(); i++) {
                            JSONObject actor = jsonroutes.getJSONObject(i);
                            String lineId = actor.getString("line_id");
                            String lineName = actor.getString("line_name_el");

                            stopLines.add(lineId + " " + lineName);
                            Log.i("emfanisi grammwn2", stopId + " " + lineName);
                        }

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            } catch (IOException ex) {
                ex.printStackTrace();
                Intent intent = new Intent("gr.hua.dit.smartt.LOGIN");
                startActivity(intent);

            }
            HttpUtility.disconnect();

            Log.i("stopLines", stopLines.size()+"");
            return stopLines;
        }

        @Override
        protected void onPostExecute(List<String> myLatLng) {
            Log.i("emfanisi grammwn", stopLines.toString());
            //planetList.addAll(stopLines);



        }

    }



    public class GetStopsNearMe {
        String stopName;
        String id;
        double stopLat;
        double stopLng;

        public GetStopsNearMe(String stopName, String id, double stopLat, double stopLng) {
            this.stopName = stopName;
            this.id = id;
            this.stopLat = stopLat;
            this.stopLng = stopLng;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStopName() {
            return stopName;
        }

        public double getStopLat() {
            return stopLat;
        }

        public double getStopLng() {
            return stopLng;
        }

        public void setStopName(String stopName) {
            this.stopName = stopName;
        }

        public void setStopLat(double stopLat) {
            this.stopLat = stopLat;
        }

        public void setStopLng(double stopLng) {
            this.stopLng = stopLng;
        }
    }

}


