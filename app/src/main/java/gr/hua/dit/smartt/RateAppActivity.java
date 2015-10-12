package gr.hua.dit.smartt;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

/**
 * Created by nsouliotis on 8/10/2015.
 */


public class RateAppActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    float arrivalBarValue = 0;
    float comfortBarValue = 0;
    float routeDurationBarValue = 0;
    float driverRatingBarValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        final RatingBar arrivalBar = (RatingBar) findViewById(R.id.arrivalBar);
        arrivalBar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                arrivalBarValue = arrivalBar.getRating();
            }
        });

        final RatingBar comfortBar = (RatingBar) findViewById(R.id.comfortBar);
        comfortBar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                comfortBarValue = comfortBar.getRating();
            }
        });

        final RatingBar routeDurationBar = (RatingBar) findViewById(R.id.routeDurationBar);
        routeDurationBar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                routeDurationBarValue = routeDurationBar.getRating();
            }
        });

        final RatingBar driverRatingBar = (RatingBar) findViewById(R.id.driverRatingBar);
        driverRatingBar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                driverRatingBarValue = driverRatingBar.getRating();
            }
        });


        Button ratebtn = (Button) findViewById(R.id.ratebutton);
        Button backBtn = (Button) findViewById(R.id.backbutton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                //DO SOMETHING! {RUN SOME FUNCTION ... DO CHECKS... ETC}
                RateAppActivity.this.finish();
                Intent intent = new Intent(RateAppActivity.this, MapsActivity.class);
                startActivity(intent);
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
}
