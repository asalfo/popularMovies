package com.asalfo.movies.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.asalfo.movies.BuildConfig;
import com.asalfo.movies.R;
import com.asalfo.movies.Utility;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Movie;
import com.asalfo.movies.model.Review;
import com.asalfo.movies.model.TmdbCollection;
import com.asalfo.movies.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import retrofit2.Call;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);
    public static final String ACTION_DATA_UPDATED =
            "com.asalfo.movies.ACTION_DATA_UPDATED";
    public static final int MAX_PAGES = 20;
    private static final int DEFAULT_VOTE_COUNT = 500;
    public static final String DEFAULT_SELECTION = "popularity.desc";
    // Interval at which to sync with the the movie api, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private static final String[] FAVORITE_MOVIE_PROJECTION = new String[]{
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID
    };
    // these indices must match the projection
    private static final int INDEX_ID = 0;
    private static final int INDEX_MOVIE_ID = 1;
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    private ArrayList<String> mFavoriteIds = new ArrayList<String>();


    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        Context context = getContext();
        String selection = Utility.getPreferredSelection(context);
        if(selection == "favorites")
            selection = DEFAULT_SELECTION;

        Log.d(LOG_TAG, "Starting sync "+selection);
        // query
        for (int page = 1; page <= MAX_PAGES; page++) {

            Call<TmdbCollection<Movie>> call = apiService.getDiscoverMovies(DEFAULT_VOTE_COUNT,selection,page, BuildConfig.THE_MOVIE_DB_API_KEY);
            TmdbCollection<Movie> collection;
            try {
                collection = call.execute().body();
                if(collection != null) {
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(collection.getResults().size());
                    for (Movie movie : collection.getResults()) {


                        Boolean favorite = mFavoriteIds.contains(movie.getId());
                        ContentValues movieValues = new ContentValues();

                        // Then add the data, along with the corresponding name of the data type,
                        // so the content provider knows what kind of value is being inserted.
                        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, movie.getOriginalLanguage());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
                        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, favorite);

                        cVVector.add(movieValues);
                    }

                    // add to database
                    if (cVVector.size() > 0) {

                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);

                    }
                    Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

        }

        return;
    }

    private void fetchFavoriteMovieReviewsAndVideos() throws IOException {

        if (!mFavoriteIds.isEmpty()) {
            Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;

            String selection = MovieContract.MovieEntry._ID + " IN (" + TextUtils.join(",", mFavoriteIds) + ")";
            Cursor cursor = getContext().getContentResolver().query(movieUri, FAVORITE_MOVIE_PROJECTION, selection, null, null);

            if (cursor.moveToFirst()) {
                do {
                    int movieId = cursor.getInt(INDEX_ID);
                    String tmdbMovieId = cursor.getString(INDEX_MOVIE_ID);
                    addReviews(tmdbMovieId, movieId);
                    addVideo(tmdbMovieId, movieId);
                } while (cursor.moveToNext());
            }

            cursor.close();
        }
    }

    public void addVideo(String movie_id, int dbMovieId) throws IOException {
        Call<TmdbCollection<Video>> call = apiService.getVideos(movie_id, BuildConfig.THE_MOVIE_DB_API_KEY);
        TmdbCollection<Video> videos = call.execute().body();

        // Insert the new video information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(videos.getResults().size());

        for (Video video : videos.getResults()) {

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(MovieContract.VideoEntry.COLUMN_FAVORITE_ID, dbMovieId);
            weatherValues.put(MovieContract.VideoEntry.COLUMN_LANGUAGE, video.getLanguague());
            weatherValues.put(MovieContract.VideoEntry.COLUMN_KEY, video.getKey());
            weatherValues.put(MovieContract.VideoEntry.COLUMN_NAME, video.getName());
            weatherValues.put(MovieContract.VideoEntry.COLUMN_SITE, video.getSite());
            weatherValues.put(MovieContract.VideoEntry.COLUMN_SIZE, video.getSize());
            weatherValues.put(MovieContract.VideoEntry.COLUMN_TYPE, video.getType());


            cVVector.add(weatherValues);
        }
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(MovieContract.VideoEntry.CONTENT_URI, cvArray);
        }
    }

    public void addReviews(String movie_id, long dbMovieId) throws IOException {
        Call<TmdbCollection<Review>> call = apiService.getReviews(movie_id, BuildConfig.THE_MOVIE_DB_API_KEY);
        TmdbCollection<Review> reviews = call.execute().body();

        // Insert the new reviews information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(reviews.getResults().size());

        for (Review review : reviews.getResults()) {

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(MovieContract.ReviewEntry.COLUMN_FAVORITE_ID, dbMovieId);
            weatherValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
            weatherValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.getContent());
            weatherValues.put(MovieContract.ReviewEntry.COLUMN_URL, review.getUrl());


            cVVector.add(weatherValues);
        }
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().bulkInsert(MovieContract.ReviewEntry.CONTENT_URI, cvArray);
        }

    }

}
