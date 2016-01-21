package com.asalfo.movies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asalfo.image.CropTransformation;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("movie")) {

            DisplayMetrics displaymetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = displaymetrics.heightPixels;
            int width = displaymetrics.widthPixels;

            Movie movie = intent.getParcelableExtra("movie");
            ImageView moviePoster =   (ImageView) rootView.findViewById(R.id.movie_poster);
            String posterUrl = movie.mThumbnailUrl.replace("#","w780");
            Picasso.with(this.getContext()).load(posterUrl).transform(new CropTransformation(0,500, CropTransformation.CropType.TOP)).into(moviePoster);
            TextView movieTitle =   (TextView) rootView.findViewById(R.id.movie_title);
            movieTitle.setText(movie.mTitle);
            TextView movieReleaseDate =   (TextView) rootView.findViewById(R.id.movie_date);
            movieReleaseDate.setText(movie.mReleaseDate);
//            TextView movieRateAvg =   (TextView) rootView.findViewById(R.id.movie_rate_average);
//            movieRateAvg.setText(movie.mRating.toString());
            TextView movieSynopsis =   (TextView) rootView.findViewById(R.id.movie_synopsis);
            movieSynopsis.setText(movie.mSynopsis);

       }
        return rootView;
    }
}
