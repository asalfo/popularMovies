package com.asalfo.movies.service;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.asalfo.movies.BuildConfig;
import com.asalfo.movies.R;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Movie;
import com.asalfo.movies.model.Review;
import com.asalfo.movies.model.TmdbCollection;
import com.asalfo.movies.model.Video;

import java.io.IOException;
import java.util.Vector;

import retrofit2.Call;


public class FavoriteMovieTask  extends AsyncTask<String, Void, Integer> {
    public static final String LOG_TAG = FavoriteMovieTask.class.getSimpleName();
    public static final String ACTION_ADD = "add";
    public static final String ACTION_REMOVE = "remove";
     private final Context mContext;
     private ImageView mFavIcon;

     public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);

     public FavoriteMovieTask(Context context, ImageView favIcon) {
         mContext = context;
         mFavIcon = favIcon;
     }




     public long addMovie (String movie_id) throws IOException {
         Log.d(LOG_TAG,movie_id);
         long movieId;
         Call<Movie> call = apiService.getMovie(movie_id, BuildConfig.THE_MOVIE_DB_API_KEY);
         Movie movie = call.execute().body();
         // First, check if the location with this city name exists in the db
         Cursor movieCursor = mContext.getContentResolver().query(
                 MovieContract.MovieEntry.CONTENT_URI,
                 new String[]{MovieContract.MovieEntry._ID},
                 MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                 new String[]{movie_id},
                 null);

         if (movieCursor.moveToFirst()) {
             int movieIdIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry._ID);
             movieId = movieCursor.getLong(movieIdIndex);
         } else {

             ContentValues movieValues = new ContentValues();

             // Then add the data, along with the corresponding name of the data type,
             // so the content provider knows what kind of value is being inserted.
             movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
             movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
             movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
             movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, movie.getOriginalLanguage());
             movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
             movieValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,movie.getBackdropPath());
             movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,movie.getOverview());
             movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY,movie.getPopularity());
             movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,movie.getVoteAverage());
             movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT,movie.getVoteCount());
             movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,movie.getReleaseDate());
             movieValues.put(MovieContract.MovieEntry.COLUMN_VIDEO,movie.getVideo());
             movieValues.put(MovieContract.MovieEntry.COLUMN_ADULT,movie.getAdult());

             // Finally, insert location data into the database.
             Uri insertedUri = mContext.getContentResolver().insert(
                     MovieContract.MovieEntry.CONTENT_URI,
                     movieValues
             );

             // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
             movieId = ContentUris.parseId(insertedUri);
        }

         movieCursor.close();

         addReviews(movie_id, movieId);
         addVideo(movie_id, movieId);
         return movieId;

     }

     public  int removeMovie( String movie_id){
         ContentResolver cr = mContext.getContentResolver();
          int deleteRows = cr.delete(
                 MovieContract.MovieEntry.CONTENT_URI,
                 MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                 new String[]{movie_id});

         return deleteRows;
     }
     public void addVideo (String movie_id,long dbMovieId) throws IOException {
         Call<TmdbCollection<Video>> call = apiService.getVideos(movie_id, BuildConfig.THE_MOVIE_DB_API_KEY);
         TmdbCollection<Video> videos = call.execute().body();

         // Insert the new video information into the database
         Vector<ContentValues> cVVector = new Vector<ContentValues>(videos.getResults().size());

         for( Video video : videos.getResults() ) {

             ContentValues weatherValues = new ContentValues();

             weatherValues.put(MovieContract.VideoEntry.COLUMN_MOVIE_ID, dbMovieId);
             weatherValues.put(MovieContract.VideoEntry.COLUMN_LANGUAGE, video.getLanguague());
             weatherValues.put(MovieContract.VideoEntry.COLUMN_KEY, video.getKey());
             weatherValues.put(MovieContract.VideoEntry.COLUMN_NAME, video.getName());
             weatherValues.put(MovieContract.VideoEntry.COLUMN_SITE, video.getSite());
             weatherValues.put(MovieContract.VideoEntry.COLUMN_SIZE, video.getSize());
             weatherValues.put(MovieContract.VideoEntry.COLUMN_TYPE, video.getType());


             cVVector.add(weatherValues);
         }
         // add to database
         if ( cVVector.size() > 0 ) {
             ContentValues[] cvArray = new ContentValues[cVVector.size()];
             cVVector.toArray(cvArray);
             mContext.getContentResolver().bulkInsert(MovieContract.VideoEntry.CONTENT_URI, cvArray);
         }
     }

     public  void removeVideo(int movie_id){

     }

     public void addReviews (String movie_id,long dbMovieId) throws IOException {
         Call<TmdbCollection<Review>> call = apiService.getReviews(movie_id, BuildConfig.THE_MOVIE_DB_API_KEY);
         TmdbCollection<Review> reviews = call.execute().body();

         // Insert the new reviews information into the database
         Vector<ContentValues> cVVector = new Vector<ContentValues>(reviews.getResults().size());

         for( Review review : reviews.getResults() ) {

             ContentValues weatherValues = new ContentValues();

             weatherValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, dbMovieId);
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

     public  void removeReview(int movie_id){

     }


    @Override
    protected Integer doInBackground(String... params) {
        int result =-1;

        String movieId = params[0];
        String action = params[1];

        try {
            switch (action) {
                case ACTION_ADD :
                    addMovie(movieId);
                    result = 1;
                    break;
                case ACTION_REMOVE:
                    removeMovie(movieId);
                    result = 2;
                    break;
                default:
                    result = -1;;
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
