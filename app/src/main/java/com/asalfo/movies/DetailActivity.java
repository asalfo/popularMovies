package com.asalfo.movies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.asalfo.movies.model.Movie;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            Boolean favorite = getIntent().getBooleanExtra(DetailActivityFragment.FAVORITE, false);
            Boolean twopane = getIntent().getBooleanExtra(DetailActivityFragment.TWO_PANE, false);
            arguments.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());
            arguments.putBoolean(DetailActivityFragment.FAVORITE, favorite);
            arguments.putBoolean(DetailActivityFragment.TWO_PANE,twopane);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();

            // Being here means we are in animation mode
            supportPostponeEnterTransition();
        }

    }

}
