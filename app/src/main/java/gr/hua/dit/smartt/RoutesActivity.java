package gr.hua.dit.smartt;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private RoutesFetch mRoutesFetchTask = null;
    private List<String> routes = new ArrayList<String>();
    private ListView lv;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        Log.i("list",String.valueOf(routes.size()));
       ListView lv = (ListView) findViewById(R.id.listView);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                routes );

        lv.setAdapter(arrayAdapter);
        getroutes();
        Log.i("list after", String.valueOf(routes.size()));


    }


    public void getroutes(){
        mRoutesFetchTask = new RoutesFetch();
        mRoutesFetchTask.execute((Void) null);

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

    public class RoutesFetch extends AsyncTask<Void, Void, Boolean> {


        RoutesFetch() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {



            // test sending POST request
            Map<String, String> params1 = new HashMap<String, String>();
            String requestURL = "http://83.212.116.159/smartt/backend/api/routes/routes";


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
                            String name = actor.getString("line_name_en");
                            routes.add(name);
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


            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

        }

        @Override
        protected void onCancelled() {

        }
    }
}
