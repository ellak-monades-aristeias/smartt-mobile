package gr.hua.dit.smartt;


import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class NetworkTracker extends Service {

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location = new Location("");// location
    Location locationGPS;
    Location locationWIFI;
    double latitude; // latitude
    double longitude; // longitude
    float appAccuracy = 999;
    String appProvider = "none";

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 20 * 1; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public NetworkTracker(Context context) {
        this.mContext = context;
        location.setLatitude(0);
        location.setLongitude(0);
        getLocation();
    }

    int intTest = 0;
    // Define a listener that responds to location updates
    String strProvider = "";
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location locationCurrent) {
            // Called when a new location is found by the network location provider.
            //makeUseOfNewLocation(location);
            if (isGPSEnabled || isNetworkEnabled) {
                intTest = intTest + 1;
                location = locationCurrent;
                appAccuracy = location.getAccuracy();
                appProvider = location.getProvider();
                if ((!isGPSEnabled) && appAccuracy <= 50) {
                    stopUsingGPS();
                }
                if (isGPSEnabled && appAccuracy <= 20) {
                    stopUsingGPS();
                }
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("NETTR", String.valueOf(status));
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public Location getLocation() {
        try {

            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled || isGPSEnabled) {
                this.canGetLocation = true;
            }


            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                if (isNetworkEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                    //Log.d("Network", "Network");
                    if (locationManager != null) {
                        locationWIFI = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        locationGPS = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }

                if (locationWIFI != null) {
                    location = locationWIFI;
                }
                if (locationGPS != null) {
                    location = locationGPS;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return location;

    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            return location.getLatitude();
        } else {
            return 0;
        }
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            return location.getLongitude();
        } else {
            return 0;
        }
    }


    public String getAccuracy() {
        return Float.toString(appAccuracy);
    }

    public String getProvider() {
        return appProvider;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("ÈÝëåôå íá åíåñãïðïéÞóåôå ôï GPS óáò;");

        // Setting Dialog Message
        alertDialog.setMessage("Ãéá ìåãáëýôåñç áêñßâåéá ôçò ôïðïèåóßáò óáò åíåñãïðïéÞóôå ôï GPS");

        // On pressing Settings button
        alertDialog.setPositiveButton("Íáé", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("¼÷é", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    public void showBadAccuracyAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("Ðñïóï÷Þ!");

        // Setting Dialog Message
        alertDialog.setMessage("Ç ôïðïèåóßá óáò äåí Ý÷åé êáëÞ áêñßâåéá êáé ðïëý ðéèáíü íá åßíáé ëáíèáóìÝíç!\nÃéá ìåãáëýôåñç áêñßâåéá ôçò ôïðïèåóßáò óáò åíåñãïðïéÞóôå ôï GPS!");

        // On pressing Settings button
        alertDialog.setPositiveButton("Íáé", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("¼÷é", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}