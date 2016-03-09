package com.asalfo.movies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.asalfo.movies.MainActivityFragment;
import com.asalfo.movies.model.Movie;
import com.asalfo.movies.R;
import com.asalfo.movies.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by asalfo on 12/01/16.
 */
public class MovieAdapter extends  CursorAdapter {

    public static final String LOG_TAG = FavoriteMovieAdapter.class.getSimpleName();
    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);


        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String posterPath = cursor.getString(MainActivityFragment.COLUMN_POSTER_PATH);
        String thumbnailUrl = Utility.generatePosterUrl(posterPath, "w92");
        ImageView posterView = (ImageView) view.findViewById(R.id.grid_item_poster);
        Picasso.with(context).load(thumbnailUrl).fit().into(posterView);
        Log.d(LOG_TAG, thumbnailUrl);

    }
}