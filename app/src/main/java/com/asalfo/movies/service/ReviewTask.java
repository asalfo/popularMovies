package com.asalfo.movies.service;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.asalfo.movies.BuildConfig;
import com.asalfo.movies.DetailActivityFragment;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Review;
import com.asalfo.movies.model.TmdbCollection;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit2.Call;

public class ReviewTask extends AsyncTask<String, Void, ArrayList<Review>>{

    public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);
    public static final String[] REVIEWS_COLUMNS = {
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT,
            MovieContract.ReviewEntry.COLUMN_URL
    };
    public static final int COL_REVIEW_ID = 0;
    public static final int COL_REVIEW_AUTHOR = 1;
    public static final int COL_REVIEW_CONTENT = 2;
    public static final int COL_REVIEW_URL = 3;
    WeakReference<DetailActivityFragment> mFragment;


    public ReviewTask(DetailActivityFragment fragment) {
        mFragment = new WeakReference<>(fragment);
    }

    @Override
    protected ArrayList<Review> doInBackground(String... params) {

        ArrayList<Review> reviews = new ArrayList<Review>();

        String movie_id =params[0];
        boolean isFavorite = Boolean.valueOf(params[1]);

        if (isFavorite) {
            Uri favoriteUri = MovieContract.ReviewEntry.buildReviewMovie(movie_id);
            if(null != mFragment.get()) {
                Cursor reviewCursor = mFragment.get().getActivity().getContentResolver().query(
                        favoriteUri,
                        REVIEWS_COLUMNS,
                        MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{movie_id},
                        null);

                if (reviewCursor.moveToFirst()) {
                    do {
                        Review review = new Review(reviewCursor.getString(COL_REVIEW_ID),
                                reviewCursor.getString(COL_REVIEW_AUTHOR),
                                reviewCursor.getString(COL_REVIEW_CONTENT),
                                reviewCursor.getString(COL_REVIEW_URL)
                        );
                        reviews.add(review);


                    } while (reviewCursor.moveToNext());
                }
                reviewCursor.close();
            }

        }else{
            Call<TmdbCollection<Review>> call = apiService.getReviews(movie_id, BuildConfig.THE_MOVIE_DB_API_KEY);
            try {
                reviews = call.execute().body().getResults();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return reviews;
    }

    @Override
    protected void onPostExecute(ArrayList<Review> reviews) {
        if(null != mFragment.get()) {
            mFragment.get().updateReviews(reviews);
        }
    }
}
