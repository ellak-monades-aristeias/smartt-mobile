package gr.hua.dit.smartt;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nsouliotis on 8/10/2015.
 */


public class RateAppActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    float arrivalBarValue;
    float comfortBarValue;
    float routeDurationBarValue;
    float driverRatingBarValue;
    String routeID;
    String routeDIR;
    RatingBar arrivalBar;
    RatingBar comfortBar;
    RatingBar routeDurationBar;
    RatingBar driverRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        final Utilities ut = new Utilities(this);
        final Bundle extra = getIntent().getExtras();
        routeID = extra.getString("routeid");
        routeDIR = extra.getString("routedir");

        arrivalBar = (RatingBar) findViewById(R.id.arrivalBar);
        arrivalBar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                arrivalBarValue = arrivalBar.getRating();
                Log.i("parametroi11", arrivalBar.getRating() + " - ");
            }
        });

        comfortBar = (RatingBar) findViewById(R.id.comfortBar);
        comfortBar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                comfortBarValue = comfortBar.getRating();
                Log.i("parametroi22", comfortBar.getRating() + " - ");
            }
        });

        routeDurationBar = (RatingBar) findViewById(R.id.routeDurationBar);
        routeDurationBar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                routeDurationBarValue = routeDurationBar.getRating();
                Log.i("parametroi33", routeDurationBarValue + " - ");
            }
        });

        driverRatingBar = (RatingBar) findViewById(R.id.driverRatingBar);
        driverRatingBar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                driverRatingBarValue = driverRatingBar.getRating();
                Log.i("parametroi44", driverRatingBarValue + " - ");
            }
        });


        Button ratebtn = (Button) findViewById(R.id.ratebutton);
        Button backBtn = (Button) findViewById(R.id.backbutton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //DO SOMETHING! {RUN SOME FUNCTION ... DO CHECKS... ETC}
                RateAppActivity.this.finish();
                //Intent intent = new Intent(RateAppActivity.this, MapsActivity.class);
                //startActivity(intent);
            }
        });

        ratebtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //DO SOMETHING! {RUN SOME FUNCTION ... DO CHECKS... ETC}
                new RateTask(ut.getEmailAddress(),routeID,routeDIR,arrivalBar.getRating()+"",comfortBar.getRating()+"",routeDurationBar.getRating()+"",driverRatingBar.getRating()+"").execute();
            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    public class RateTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mRoute;
        private final String mDir;
        private final String marrivalTime;
        private final String mComfort;
        private final String mRouteDuration;
        private final String mDriverRating;

        RateTask(String email, String route, String dir, String arrivalTime, String comfort, String routeDuration, String driverRating) {
            mEmail = email;
            mRoute = route;
            mDir = dir;
            marrivalTime = arrivalTime;
            mComfort = comfort;
            mRouteDuration = routeDuration;
            mDriverRating = driverRating;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // test sending POST request
            Map<String, String> params1 = new HashMap<String, String>();
            String requestURL = "http://83.212.116.159/smartt/backend/api/user/rating";
            params1.put("email", mEmail);
            params1.put("route", mRoute);
            params1.put("dir", mDir);
            params1.put("arrival_time", marrivalTime);
            params1.put("comfort", mComfort);
            params1.put("route_duration", mRouteDuration);
            params1.put("driver_rating", mDriverRating);

            Log.i("parametroi", mEmail + " - " + mRoute +
                    " / " + mDir + " - " + marrivalTime + " - " + mComfort+ " - " + mRouteDuration + " - " + mDriverRating);
            try {
                HttpUtility.sendPostRequest(requestURL, params1);


                String[] response = HttpUtility.readMultipleLinesRespone();

                for (String line : response) {
                    System.out.println(line);
                    Log.i("RG-res", String.valueOf(line));

                    JSONObject jObject = new JSONObject(line);
                    String success = jObject.getString("success");
                    String message = jObject.getString("message");

                    Log.i("RG-resrate", String.valueOf(success));
                }


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            HttpUtility.disconnect();

            return true;
        }
            @Override
        protected void onPostExecute(final Boolean success) {
                Toast.makeText(RateAppActivity.this, "Μόλις Βαθμολογήσατε το SMARTT! Σας ευχαριστούμε!", Toast.LENGTH_SHORT).show();
                RateAppActivity.this.finish();
                Intent intent = new Intent(RateAppActivity.this, MapsActivity.class);
                startActivity(intent);
        }
    }
}
