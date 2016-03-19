package com.asalfo.movies.service;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.asalfo.movies.R;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Review;
import com.asalfo.movies.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;


public class FavoriteMovieTask  extends AsyncTask<String, Void, Integer> {
    public static final String LOG_TAG = FavoriteMovieTask.class.getSimpleName();
    public static final String ACTION_ADD = "add";
    public static final String ACTION_REMOVE = "remove";
    private final Context mContext;
    private ImageView mFavIcon;
    private  ArrayList<Review> mReviews;
    private  ArrayList<Video> mVideos;

     public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);

     public FavoriteMovieTask(Context context, ImageView favIcon,ArrayList<Video> videos,ArrayList<Review> reviews) {
         mContext = context;
         mFavIcon = favIcon;
         mReviews = reviews;
         mVideos  = videos;
     }


    public void addToFavorite(String tmdbId) throws IOException {

        ContentValues favoriteValues = new ContentValues();

        favoriteValues.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID, tmdbId);

        Uri uri = mContext.getContentResolver().insert(
                MovieContract.FavoriteEntry.CONTENT_URI, favoriteValues);
        String favId = MovieContract.FavoriteEntry.getFavoriteMovieIdFromUri(uri);
        if (favId != null) {
            addReviews(favId, mReviews);
            addVideo(favId, mVideos);
        }
    }

     public  int removeFromFavorite( String movie_id){
         ContentResolver cr = mContext.getContentResolver();
         return cr.delete(
                  MovieContract.FavoriteEntry.CONTENT_URI,
                  MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                  new String[]{movie_id});

     }
     public void addVideo (String favId,ArrayList<Video> videos) throws IOException {

         // Insert the new video information into the database
         Vector<ContentValues> cVVector = new Vector<>(videos.size());

         for( Video video : videos ) {

             ContentValues videoValues = new ContentValues();

             videoValues.put(MovieContract.VideoEntry.COLUMN_VIDEO_ID, video.getId());
             videoValues.put(MovieContract.VideoEntry.COLUMN_FAVORITE_ID, favId);
             videoValues.put(MovieContract.VideoEntry.COLUMN_LANGUAGE, video.getLanguague());
             videoValues.put(MovieContract.VideoEntry.COLUMN_KEY, video.getKey());
             videoValues.put(MovieContract.VideoEntry.COLUMN_NAME, video.getName());
             videoValues.put(MovieContract.VideoEntry.COLUMN_SITE, video.getSite());
             videoValues.put(MovieContract.VideoEntry.COLUMN_SIZE, video.getSize());
             videoValues.put(MovieContract.VideoEntry.COLUMN_TYPE, video.getType());


             cVVector.add(videoValues);
         }
         // add to database
         if ( cVVector.size() > 0 ) {
             ContentValues[] cvArray = new ContentValues[cVVector.size()];
             cVVector.toArray(cvArray);
             mContext.getContentResolver().bulkInsert(MovieContract.VideoEntry.CONTENT_URI, cvArray);
         }
     }


     public void addReviews (String favId,ArrayList<Review> reviews) throws IOException {

         // Insert the new reviews information into the database
         Vector<ContentValues> cVVector = new Vector<>(reviews.size());

         for( Review review : reviews ) {

             ContentValues weatherValues = new ContentValues();

             weatherValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, review.getId());
             weatherValues.put(MovieContract.ReviewEntry.COLUMN_FAVORITE_ID, favId);
             weatherValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
             weatherValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.getContent());
             weatherValues.put(MovieContract.ReviewEntry.COLUMN_URL, review.getUrl());


             cVVector.add(weatherValues);
         }
         // add to database
         if ( cVVector.size() > 0 ) {
             ContentValues[] cvArray = new ContentValues[cVVector.size()];
             cVVector.toArray(cvArray);
             mContext.getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, cvArray);
         }

     }




    @Override
    protected Integer doInBackground(String... params) {
        int result =-1;

        String tmdbId = params[0];
        String action = params[1];

        try {
            switch (action) {
                case ACTION_ADD :
                    addToFavorite(tmdbId);
                    result = 1;
                    break;
                case ACTION_REMOVE:
                    removeFromFavorite(tmdbId);
                    result = 2;
                    break;
                default:
                    result = -1;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return result;
    }


    @Override
    protected void onPostExecute(Integer result) {
        if (result == 1) {
          mFavIcon.setImageResource(R.drawable.ic_favorite);
            Toast.makeText(mContext, "Movie added to your favorite collection ", Toast.LENGTH_LONG).show();
        }else if(result == 2) {
            mFavIcon.setImageResource(R.drawable.ic_favorite_border);
            Toast.makeText(mContext, "Movie removed from your favorite collection ", Toast.LENGTH_LONG).show();
        }
    }
}
