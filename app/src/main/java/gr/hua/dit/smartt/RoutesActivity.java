package gr.hua.dit.smartt;

import android.app.Dialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.lang.Double.*;

public class RoutesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private RoutesFetch mRoutesFetchTask = null;
    private List<String> routes = new ArrayList<String>();
    private ArrayList<LatLng> waypointList = new ArrayList<LatLng>();
    private ArrayList<GetStopsNearMe> routeStopsList = new ArrayList<GetStopsNearMe>();
    private ListView lv;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);


        Log.i("list", String.valueOf(routes.size()));
        ListView lv = (ListView) findViewById(R.id.listView);


        Log.i("list after", String.valueOf(routes.size()));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                routes );


        lv.setAdapter(arrayAdapter);
        mRoutesFetchTask = new RoutesFetch(arrayAdapter);
        mRoutesFetchTask.execute();

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                String value = (String) adapter.getItemAtPosition(position);
                Log.i("LIST", value);

                String[] parts = value.split(" ", 2);
                final String getIdFromValue = parts[0].trim();
                //Log.i("nikos", parts[1]);
                String[] partsName = parts[1].split("-");
                String firstName = partsName[0].trim();
                String lastName = partsName[partsName.length - 1].trim();
                //Log.i("nikos2", firstName + " - " + lastName);
                //Toast.makeText(RoutesActivity.this, firstName + " - " + lastName, Toast.LENGTH_SHORT).show();


                final Dialog dialog = new Dialog(RoutesActivity.this);
                dialog.setContentView(R.layout.routepopup);
                dialog.setTitle("ΚΑΤΕΥΘΥΝΣΗ");
                Button btn = (Button) dialog.findViewById(R.id.route1);
                Button btn2 = (Button) dialog.findViewById(R.id.route2);
                btn.setText("Προς: " + firstName);
                btn2.setText("Προς: " + lastName);
                dialog.show();
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            new GetRouteStops().execute(getIdFromValue, "0").get();
                            new GetWaypoints().execute(getIdFromValue, "0");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                    }
                });

                btn2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            new GetRouteStops().execute(getIdFromValue, "1").get();
                            new GetWaypoints().execute(getIdFromValue, "1");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_routes, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
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

        return super.onOptionsItemSelected(item);
    }

    public class RoutesFetch extends AsyncTask<Void, Void, List<String>> {
        private final ArrayAdapter<String> mAdapter;

        RoutesFetch(ArrayAdapter<String> arrayAdapter) {
        mAdapter=arrayAdapter;
        }

        @Override
        protected List<String> doInBackground(Void... params) {

            // test sending POST request
            Map<String, String> params1 = new HashMap<String, String>();
            String requestURL = getString(R.string.url_routes);

            try {
                HttpUtility.sendGetRequest(requestURL);
                String[] response = HttpUtility.readMultipleLinesRespone();

                for (String line : response) {
                    System.out.println(line);
                    Log.i("RG-res", String.valueOf(line));

                    try {
                        JSONArray jsonroutes = new JSONArray(line);
                        for (int i=0; i<jsonroutes.length(); i++) {
                            JSONObject actor = jsonroutes.getJSONObject(i);
                            String id = actor.getString("_id");
                            String name = actor.getString("line_name_en");
                            routes.add(id +" "+name);
                            //Log.i("RG-res-name", String.valueOf(name));
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
            return routes;
        }

        @Override
        protected void onPostExecute(final List<String> result) {
            super.onPostExecute(result);
            mAdapter.addAll(routes);

            Log.i("list after", String.valueOf(routes.size()));
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onCancelled() {

        }
    }



    //get all the waypoints of the selected route
    public class GetWaypoints extends AsyncTask<String, Void, List<LatLng>> {
        //private final ArrayAdapter<String> mAdapter;
        String route;
        String dir;

        @Override
        protected List<LatLng> doInBackground(String... params) {
            route = params[0];
            dir = params[1];
            // test sending POST request
            Map<String, String> params1 = new HashMap<String, String>();
            String requestURL = "http://83.212.116.159/smartt/backend/api/routes/waypoints?route="+route+"&dir="+dir;

            try {
                HttpUtility.sendGetRequest(requestURL);
                String[] response = HttpUtility.readMultipleLinesRespone();

                for (String line : response) {
                    System.out.println(line);
                    Log.i("RG-res", String.valueOf(line));

                    try {
                        JSONObject jObject = new JSONObject(line);
                        String success = jObject.getString("points");
                        JSONArray jsonroutes = new JSONArray(success);
                        for (int i=0; i<jsonroutes.length(); i++) {
                            JSONObject actor = jsonroutes.getJSONObject(i);
                            String lat = actor.getString("lat");
                            String lon = actor.getString("lon");
                            waypointList.add(new LatLng(parseDouble(lat), parseDouble(lon)));
                            //Log.i("RG-res-name", String.valueOf(name));
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

            return waypointList;
        }

        @Override
        protected void onPostExecute(final List<LatLng> result) {

            super.onPostExecute(result);


                RoutesActivity.this.finish();
                Intent openStartingPoint = new Intent(RoutesActivity.this, MapsActivity.class);
//            openStartingPoint.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//            openStartingPoint.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                openStartingPoint.addFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
                openStartingPoint.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                openStartingPoint.putExtra("isFromRouteValue", true);
                openStartingPoint.putParcelableArrayListExtra("waypoints", (ArrayList<LatLng>) result);
                openStartingPoint.putExtra("routeStops", (ArrayList<GetStopsNearMe>) routeStopsList);
                startActivity(openStartingPoint);

                Log.i("list after", String.valueOf(routes.size()));

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onCancelled() {

        }
    }


    //get all the waypoints of the selected route
    public class GetRouteStops extends AsyncTask<String, Void, List<GetStopsNearMe>> {
        //private final ArrayAdapter<String> mAdapter;


        @Override
        protected List<GetStopsNearMe> doInBackground(String... params) {
            // test sending POST request

            String requestURL = "http://83.212.116.159/smartt/backend/api/routes/routestops?route="+params[0]+"&dir="+params[1];

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
                            //Log.i("RG-res-name", String.valueOf(name));
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

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onCancelled() {

        }
    }

}
