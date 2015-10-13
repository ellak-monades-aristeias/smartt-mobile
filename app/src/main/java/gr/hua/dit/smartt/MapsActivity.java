package gr.hua.dit.smartt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static java.lang.Double.parseDouble;


public class MapsActivity extends AppCompatActivity implements LocationProvider.LocationCallback {
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    public ArrayList<GetLinesNearStop> stopLines = new ArrayList<GetLinesNearStop>();
    public ArrayList<GetLinesNearStop> nearlines = new ArrayList<GetLinesNearStop>();
    boolean isFromRoutes = false;
    boolean istracked = false;
    ArrayList<GetStopsNearMe> routeStopsList = new ArrayList<GetStopsNearMe>();
    MarkerOptions options;
    ArrayList<LatLng> nearstopslatlng = new ArrayList<LatLng>();
    boolean isStartGiveLocation = false;
    double currentLatitude;
    double currentLongitude;
    boolean stopLoop;
   // final Handler handler = new Handler();
    static Timer timer;
    static TimerTask timerTask;
    static Handler handler = new Handler();
    final ArrayList<String> drawerlist = new ArrayList<String>();


    public static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play ser ices APK is not available.

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

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            //from Routes
            isFromRoutes = extra.getBoolean("isFromRouteValue");
            isStartGiveLocation = extra.getBoolean("isStartGiveLocation");
            if(extra.getBoolean("isFromRouteActivity")) {
                new LoadBus(extra.getString("routeid"),extra.getString("routedir")).execute();
            }else {
                istracked = extra.getBoolean("tracked");
                if (istracked) {
                    new LoadBus(extra.getString("routeid"), extra.getString("routedir")).execute();
                }
            }
            routeStopsList = (ArrayList<GetStopsNearMe>) extra.getSerializable("routeStops");
        }
        mActivityTitle = String.valueOf(ut.getEmailAddress());
        if(ut.getEmailAddress().equals("none")) {
            getSupportActionBar().setTitle("Your Position");
        }
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);


        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mLocationProvider = new LocationProvider(this, this);

        nettracker = new NetworkTracker(MapsActivity.this);
        gps = new GPSTracker(MapsActivity.this);





        try {
            // Loading map
            Log.i("RG", "Loading the map");
            initilizeMap();
            Log.i("RG", "After Loading the map");



        } catch (Exception e) {
            e.printStackTrace();
        }


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                LoadLinesfromStop loadlinesfromstop = new LoadLinesfromStop();
                try {
                loadlinesfromstop.execute(arg0.getSnippet()).get();

                AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
                View customView = LayoutInflater.from(MapsActivity.this).inflate(
                        R.layout.markerinfowindowlayout, null, false);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(arg0.getPosition().latitude, arg0.getPosition().longitude)));
                TextView tv = (TextView) customView.findViewById(R.id.StopName);
                tv.setText(arg0.getTitle());
                final ListView listView = (ListView) customView.findViewById(R.id.ListItems);
                Log.i("epistrefei", stopLines.size() + " !");
                final AlertListAdapter mAdapter = new AlertListAdapter(stopLines, getBaseContext());
                listView.setAdapter(mAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {

                        String value = (String) stopLines.get(position).getStopName();
                        String[] parts = value.split(" ", 2);
                        final String getIdFromValue = parts[0].trim();


                        routeStopsList.clear();
                        if(stopLines.get(position).getdirectionid().equals("1")) {
                            if(stopLines.get(position).getTracked()==0){
                                new GetRouteStops("","no",getIdFromValue, "1").execute();
                            }else {
                                new GetRouteStops("","yes",getIdFromValue, "1").execute();
                            }
                        }else {
                            if(stopLines.get(position).getTracked()==0){
                                new GetRouteStops("","no",getIdFromValue, "0").execute();
                            }else {
                                new GetRouteStops("","yes",getIdFromValue, "0").execute();
                            }

                        }
                    }
                });
                //listView.setOnItemClickListener(mOnItemClick);
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                dialog.setView(customView);
                dialog.setPositiveButton("Back", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.i("RG", "After Loading the map when marker click sto catch " + e.getMessage());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Log.i("RG", "After Loading the map when marker click sto catch1 " + e.getMessage());
                }
                return true;
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
            isStartGiveLocation = extra.getBoolean("isStartGiveLocation");
            if(extra.getBoolean("isFromRouteActivity")) {
                new LoadBus(extra.getString("routeid"),extra.getString("routedir")).execute();
            }else {
                istracked = extra.getBoolean("tracked");
                if (istracked) {
                    new LoadBus(extra.getString("routeid"), extra.getString("routedir")).execute();
                }
            }
            routeStopsList = (ArrayList<GetStopsNearMe>) extra.getSerializable("routeStops");
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

        final String[] osArray = { "Login", "Routes", "Give my location", "Rate SMARTT", "Exit SMARTT" };
        final String[] osArrayLogin = { "Logout", "Routes", "Give my location", "Rate SMARTT", "Exit SMARTT" };
        final String[] osArrayLoginGiveLocation = { "Logout", "Routes", "Stop give my location", "Rate SMARTT", "Exit SMARTT" };

        if(ut.getEmailAddress() != "none") {
            if(isStartGiveLocation) {
                drawerlist.addAll(Arrays.asList(osArrayLoginGiveLocation));
            }else {
                drawerlist.addAll(Arrays.asList(osArrayLogin));
            }
            //mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArrayLogin);
        }else {
            drawerlist.addAll(Arrays.asList(osArray));
            //mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        }

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerlist);

        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MapsActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();

                //final double latitude = 37.98392;
                //final double longitude = 23.72936;
                Log.i("RG", String.valueOf(id) + ' ' + String.valueOf(position) + ' ' + String.valueOf(osArray[position]));
                //mMap.clear();

                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);

                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                //Login - Logout

                if (id == 0) {
                    if (ut.getEmailAddress() != "none") {
                        MapsActivity.stoptimertask();
                        ut.clearPreferences();
                        addDrawerItems();
                        setupDrawer();
                        Toast.makeText(MapsActivity.this, "You just logged out!", Toast.LENGTH_SHORT).show();
                    } else {
//                        if (ut.getEmailAddress() != "none") {
//                            Log.i("SV-email", String.valueOf(ut.getEmailAddress()));
//                            Toast.makeText(MapsActivity.this, "Logged in as " + String.valueOf(ut.getEmailAddress()), Toast.LENGTH_SHORT).show();
//                        } else {
                        MapsActivity.this.finish();
                        Intent intent = new Intent("gr.hua.dit.smartt.LOGIN");
                        startActivity(intent);
//                        }
                    }
                }
                //Routes
                if (id == 1) {
                    //Toast.makeText(MapsActivity.this, "Routes!", Toast.LENGTH_SHORT).show();
                    MapsActivity.this.finish();
                    Intent intent = new Intent("gr.hua.dit.smartt.ROUTES");
                    startActivity(intent);
                }
                //Give my location
                if (id == 2) {
                    if (ut.getEmailAddress() != "none") {
                        if(isStartGiveLocation) {
                            MapsActivity.stoptimertask();
                            //stopRepeatingTask();
                            //loop("","","","");
                            isStartGiveLocation = false;
                            drawerlist.set(2, "Give my location");
                            mAdapter.notifyDataSetChanged();

                            Log.i("parametroi", "isStartGiveLocation " + isStartGiveLocation);
                        }else {
                            LoadNearLines loadnearlines = new LoadNearLines();
                            try {
                                loadnearlines.execute(String.valueOf(options.getPosition().latitude),
                                        String.valueOf(options.getPosition().longitude)).get();

                                AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
                                View customView = LayoutInflater.from(MapsActivity.this).inflate(
                                        R.layout.markerinfowindowlayout, null, false);
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(options.getPosition().latitude, options.getPosition().longitude)));
                                TextView tv = (TextView) customView.findViewById(R.id.StopName);
                                tv.setText("Κοντινές διαδρομές");
                                final ListView listView = (ListView) customView.findViewById(R.id.ListItems);
                                final AlertListAdapter mAdapter = new AlertListAdapter(nearlines, getBaseContext());
                                listView.setAdapter(mAdapter);
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                                            final long id) {
                                        String value = (String) nearlines.get(position).getStopName();
                                        String[] parts = value.split(" ", 2);
                                        final String getIdFromValue = parts[0].trim();

                                        routeStopsList.clear();
                                        if(nearlines.get(position).getdirectionid().equals("1")) {
                                            if(nearlines.get(position).getTracked()==0) {
                                                new GetRouteStops("givelocation","no",getIdFromValue, "1").execute();
                                            }else {
                                                new GetRouteStops("givelocation","yes",getIdFromValue, "1").execute();
                                            }
                                            MapsActivity.this.startTimer(getIdFromValue, "1", String.valueOf(options.getPosition().latitude), String.valueOf(options.getPosition().longitude));
                                        }else {
                                            if(nearlines.get(position).getTracked()==0) {
                                                new GetRouteStops("givelocation","no",getIdFromValue, "0").execute();
                                            }else {
                                                new GetRouteStops("givelocation","yes",getIdFromValue, "0").execute();
                                            }
                                            MapsActivity.this.startTimer(getIdFromValue, "1", String.valueOf(options.getPosition().latitude), String.valueOf(options.getPosition().longitude));
                                        }
                                    }
                                });
                                dialog.setView(customView);
                                dialog.setPositiveButton("Back", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub

                                    }
                                });
                                dialog.show();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }else {
                        Toast.makeText(MapsActivity.this, "You have to Login first!", Toast.LENGTH_SHORT).show();
                    }
                }
                //Rate SMARTT
                if (id == 3) {
                    if (ut.getEmailAddress() != "none") {
                        MapsActivity.this.finish();
                        Intent intent = new Intent("gr.hua.dit.smartt.RATE");
                        startActivity(intent);
                    } else {
                        Toast.makeText(MapsActivity.this, "You have to Login first if you want to Rate SMARTT!", Toast.LENGTH_SHORT).show();
                    }
                }
                //Exit SMARTT
                if (id == 4) {
                    MapsActivity.stoptimertask();
                    finish();
                    System.exit(0);
                }
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
                if(ut.getEmailAddress().equals("none")) {
                    getSupportActionBar().setTitle("Your Position");
                }else {
                    getSupportActionBar().setTitle(mActivityTitle);
                }
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

            for(int i=0; i< routeStopsList.size(); i++) {
                //marker = new MarkerOptions().position(new LatLng(latitude, longitude));
                markersList.add(new MarkerOptions().position(new LatLng(routeStopsList.get(i).getStopLat(), routeStopsList.get(i).getStopLng())));
                Log.i("emfanisi grammwn34", "teleiwnei to views " + String.valueOf(routeStopsList.get(i).getId()));
                mMap.addMarker(new MarkerOptions().position(new LatLng(routeStopsList.get(i).getStopLat(), routeStopsList.get(i).getStopLng()))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.busstop))
                        .title(routeStopsList.get(i).getStopName())
                        .snippet(routeStopsList.get(i).getId()+""));
            }
        }
        Log.d(TAG, location.toString());

        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
        options = new MarkerOptions()
                .position(latLng)
                .title("Η τοποθεσία μου!");
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
                //Intent intent = new Intent("gr.hua.dit.smartt.LOGIN");
                //startActivity(intent);

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
                        .snippet(mLatLng.get(i).getId() + ""));
            }

        }


    }



    public class LoadLinesfromStop extends AsyncTask<String, Void, List<GetLinesNearStop>> {

        @Override
        protected List<GetLinesNearStop> doInBackground(String... Params) {
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
                            String directionFlag = actor.getString("direction_flag");
                            int tracked = actor.getInt("tracked");

                            String[] partsName = lineName.split("-");
                            String firstName = partsName[0].trim();
                            String lastName = partsName[partsName.length - 1].trim();
                            if(directionFlag.equals("1")) {
                                stopLines.add(new GetLinesNearStop(lineId + " " + lineName + " ( " + lastName + " )" ,directionFlag,tracked));
                            }else if (directionFlag.equals("2")) {
                                stopLines.add(new GetLinesNearStop(lineId + " " + lineName + " ( " + firstName + " )" ,directionFlag,tracked));
                            }
                        }

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            } catch (IOException ex) {
                ex.printStackTrace();
                //Intent intent = new Intent("gr.hua.dit.smartt.LOGIN");
                //startActivity(intent);

            }
            HttpUtility.disconnect();
            return stopLines;
        }

        @Override
        protected void onPostExecute(List<GetLinesNearStop> myLatLng) {
            Log.i("emfanisi grammwn", stopLines.toString());
            //planetList.addAll(stopLines);



        }

    }

    //get all the routestops of the selected route
    public class GetRouteStops extends AsyncTask<String, Void, List<GetStopsNearMe>> {
        //private final ArrayAdapter<String> mAdapter;
        private final String mgivelocation;
        private final String mtracked;
        private final String mrouteId;
        private final String mdir;

        GetRouteStops(String givelocation,String tracked,String routeid, String dir) {
            mgivelocation = givelocation;
            mtracked = tracked;
            mrouteId = routeid;
            mdir = dir;
        }

        @Override
        protected List<GetStopsNearMe> doInBackground(String... params) {
            // test sending POST request

            String requestURL = "http://83.212.116.159/smartt/backend/api/routes/routestops?route="+mrouteId+"&dir="+mdir;
            Log.i("RG-res-requestURL", requestURL);
            try {
                HttpUtility.sendGetRequest(requestURL);
                String[] response = HttpUtility.readMultipleLinesRespone();

                for (String line : response) {
                    System.out.println(line);
                    Log.i("RG-res", String.valueOf(line));

                    try {
                        JSONObject jObject = new JSONObject(line);
                        String success = jObject.getString("stops");
                        JSONArray jsonroutes = new JSONArray(success);
                        for (int i=0; i<jsonroutes.length(); i++) {
                            JSONObject actor = jsonroutes.getJSONObject(i);
                            String id = actor.getString("s_id");
                            String name = actor.getString("name_el");
                            String lat = actor.getString("lat");
                            String lon = actor.getString("lon");
                            routeStopsList.add(new GetStopsNearMe(name, id, Double.parseDouble(lat), Double.parseDouble(lon)));
                            Log.i("RG-res-name", String.valueOf(name));
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            HttpUtility.disconnect();

            return routeStopsList;
        }

        @Override
        protected void onPostExecute(final List<GetStopsNearMe> result) {

            super.onPostExecute(result);
            MapsActivity.this.finish();
            Intent openStartingPoint = new Intent(MapsActivity.this, MapsActivity.class);
            openStartingPoint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            openStartingPoint.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            openStartingPoint.putExtra("isFromRouteValue", true);
            openStartingPoint.putExtra("isFromRouteActivity", false);
            if(mtracked.equals("yes")) {
                openStartingPoint.putExtra("routeid", mrouteId);
                openStartingPoint.putExtra("routedir", mdir);
                openStartingPoint.putExtra("tracked", true);
            }else {
                openStartingPoint.putExtra("tracked", false);
            }
            if(mgivelocation.equals("givelocation")) {
                openStartingPoint.putExtra("isStartGiveLocation", true);
            }else {
                openStartingPoint.putExtra("isStartGiveLocation", false);
            }
            openStartingPoint.putExtra("routeStops", (ArrayList<GetStopsNearMe>) routeStopsList);
            startActivity(openStartingPoint);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onCancelled() {

        }
    }


    public class LoadNearLines extends AsyncTask<String, Void, List<GetLinesNearStop>> {

        @Override
        protected List<GetLinesNearStop> doInBackground(String... Params) {
            nearlines.clear();
            String lat = Params[0];
            String lng = Params[1];

            String requestURL = "http://83.212.116.159/smartt/backend/api/routes/nearlines?lat=" + lat + "&lon=" + lng;

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
                            String directionFlag = actor.getString("direction_flag");
                            int tracked = actor.getInt("tracked");


                            String[] partsName = lineName.split("-");
                            String firstName = partsName[0].trim();
                            String lastName = partsName[partsName.length - 1].trim();
                            if(directionFlag.equals("1")) {
                                nearlines.add(new GetLinesNearStop(lineId + " " + lineName + " ( " + lastName + " )" ,directionFlag,tracked));
                            }else if (directionFlag.equals("2")) {
                                nearlines.add(new GetLinesNearStop(lineId + " " + lineName + " ( " + firstName + " )" ,directionFlag,tracked));
                            }
                        }

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            } catch (IOException ex) {
                ex.printStackTrace();
                //Intent intent = new Intent("gr.hua.dit.smartt.LOGIN");
                //startActivity(intent);

            }
            HttpUtility.disconnect();

            return nearlines;
        }

        @Override
        protected void onPostExecute(List<GetLinesNearStop> myLatLng) {

        }

    }


    public void startTimer(final String routeId, final String routeDirection, final String lat, final String lon) {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask(routeId,routeDirection, lat,lon);

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, 10000); //
        Log.i("parametroi ", "start timer");
    }

    public static void stoptimertask() {
        //stop the timer, if it's not already null
        Log.i("parametroi ", "in stop timer");

        if (timer != null) {
            Log.i("parametroi ", "if in stop timer");
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask(final String routeId, final String routeDirection, final String lat, final String lon) {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
                        Log.i("parametroi", "mesa sto loop ");
                        new UpdateLocationTask(routeId,routeDirection, lat,lon).execute();
                    }
                });
            }
        };
    }

    public class UpdateLocationTask extends AsyncTask<Void, Void, Boolean> {

        private final String mrouteId;
        private final String mrouteDirection;
        private final String mlatit;
        private final String mlongit;


        UpdateLocationTask(String routeId, String routeDirection, String latit, String longit) {
            mrouteId = routeId;
            mrouteDirection = routeDirection;
            mlatit = latit;
            mlongit = longit;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String mac=ut.getMacAddress();
            boolean result=false;

            // test sending POST request
            Map<String, String> params1 = new HashMap<String, String>();
            String requestURL = getString(R.string.url_updateLocation);
            params1.put("lat", mlatit);
            params1.put("lon", mlongit);
            params1.put("email",String.valueOf(ut.loadStoredValue("app_email_address", "none")));
            params1.put("route", mrouteId);
            params1.put("dir", mrouteDirection);
            Log.i("parametroi", mlatit + " - " + mlongit +
            " / " + String.valueOf(ut.loadStoredValue("app_email_address", "none")) + " - " + mrouteId + " - " + mrouteDirection);

            try {
                HttpUtility.sendPostRequest(requestURL, params1);

                String[] response = HttpUtility.readMultipleLinesRespone();

                for (String line : response) {
                    System.out.println(line);
                    Log.i("RG-res", String.valueOf(line));

                    try {
                        JSONObject jObject = new JSONObject(line);
                        String success = jObject.getString("success");
                        String message = jObject.getString("message");

                        Log.i("RG-giveLocation success", String.valueOf(success));
                        Log.i("RG-giveLocation message", String.valueOf(message));
                        if(success.equals("true")) {
                            result=true;
                        }else {
                            result=false;
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            } catch (IOException ex) {
                ex.printStackTrace();
                //Intent intent = new Intent("gr.hua.dit.smartt.LOGIN");
                //startActivity(intent);

            }
            HttpUtility.disconnect();


            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                new LoadBus(mrouteId,mrouteDirection).execute();
            }else {
                Toast.makeText(MapsActivity.this, "Η τοποθεσία που δίνετε δεν αντιστοιχεί σε διαδρομή του ΟΑΣΑ!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    public class LoadBus extends AsyncTask<String, Void, List<LatLng>> {

        ArrayList<LatLng> buslist = new ArrayList<LatLng>();
        String mroute;
        String mdir;

        LoadBus(String route, String dir) {
            mroute = route;
            mdir = dir;
        }

        @Override
        protected List<LatLng> doInBackground(String... Params) {


            String requestURL = "http://83.212.116.159/smartt/backend/api/routes/buslocation?route=" + mroute + "&dir=" + mdir;

            try {
                HttpUtility.sendGetRequest(requestURL);

                String[] response = HttpUtility.readMultipleLinesRespone();

                for (String line : response) {

                    try {
                        JSONObject jObject = new JSONObject(line);
                        String success = jObject.getString("");
                        Log.i("RG-login success", String.valueOf(success));


                        JSONArray jsonroutes = new JSONArray(success);
                        for (int i=0; i<jsonroutes.length(); i++) {
                            JSONObject actor = jsonroutes.getJSONObject(i);
                            String lat = actor.getString("lat");
                            String lon = actor.getString("lon");
                            //String time = actor.getString("time");

                            buslist.add(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)));
                        }

                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            } catch (IOException ex) {
                ex.printStackTrace();
                //Intent intent = new Intent("gr.hua.dit.smartt.LOGIN");
                //startActivity(intent);

            }
            HttpUtility.disconnect();

            return buslist;
        }

        @Override
        protected void onPostExecute(List<LatLng> myLatLng) {
            for(int i=0; i< myLatLng.size(); i++) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(myLatLng.get(i).latitude, myLatLng.get(i).longitude))
                        .title("Διαδρομή " + mroute).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
            }
        }

    }

}


