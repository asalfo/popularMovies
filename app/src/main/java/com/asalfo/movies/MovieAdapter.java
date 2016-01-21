package com.asalfo.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asalfo on 12/01/16.
 */
public class MovieAdapter  extends ArrayAdapter<Movie> {
    public MovieAdapter(Context context, int resource, ArrayList<Movie> movies) {
        super(context, resource, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }
        String thumbnailUrl = movie.mThumbnailUrl.replace("#","w92");
        ImageView posterView = (ImageView) convertView.findViewById(R.id.grid_item_poster);
        Picasso.with(this.getContext()).load(thumbnailUrl).fit().into(posterView);
        return convertView;
    }
}
