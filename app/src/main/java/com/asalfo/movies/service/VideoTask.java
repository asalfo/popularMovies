package com.asalfo.movies.service;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import com.asalfo.movies.BuildConfig;
import com.asalfo.movies.DetailActivityFragment;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.TmdbCollection;
import com.asalfo.movies.model.Video;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit2.Call;

public class VideoTask  extends AsyncTask<String, Void, ArrayList<Video>>{

    public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);

    private WeakReference<DetailActivityFragment> mFragment;


    public static final String[] VIDEO_COLUMNS = {
            MovieContract.VideoEntry.TABLE_NAME + "." + MovieContract.VideoEntry._ID,
            MovieContract.VideoEntry.COLUMN_LANGUAGE,
            MovieContract.VideoEntry.COLUMN_KEY,
            MovieContract.VideoEntry.COLUMN_NAME,
            MovieContract.VideoEntry.COLUMN_SITE,
            MovieContract.VideoEntry.COLUMN_SIZE,
            MovieContract.VideoEntry.COLUMN_TYPE
    };

    public static final int COL_VIDEO_ID = 0;
    public static final int COL_VIDEO_LANGUAGE = 1;
    public static final int COL_VIDEO_KEY = 2;
    public static final int COL_VIDEO_NAME = 3;
    public static final int COL_VIDEO_SITE = 4;
    public static final int COL_VIDEO_SIZE = 5;
    public static final int COL_VIDEO_TYPE = 6;


    public VideoTask(DetailActivityFragment fragment) {
        mFragment = new WeakReference<>(fragment);
    }

    @Override
    protected ArrayList<Video> doInBackground(String... params) {

        ArrayList<Video> videos = new ArrayList<Video>();

        String movie_id =params[0];
        boolean isFavorite = Boolean.valueOf(params[1]);

        if (isFavorite) {
            Uri favoriteUri = MovieContract.VideoEntry.buildVideoMovie(movie_id);
            if(null != mFragment.get()) {
                Cursor videoCursor = mFragment.get().getActivity().getContentResolver().query(
                        favoriteUri,
                        VIDEO_COLUMNS,
                        MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{movie_id},
                        null);

                if (videoCursor.moveToFirst()) {
                    do {
                        Video video = new Video(videoCursor.getString(COL_VIDEO_ID),
                                videoCursor.getString(COL_VIDEO_LANGUAGE),
                                videoCursor.getString(COL_VIDEO_KEY),
                                videoCursor.getString(COL_VIDEO_NAME),
                                videoCursor.getString(COL_VIDEO_SITE),
                                videoCursor.getString(COL_VIDEO_SIZE),
                                videoCursor.getString(COL_VIDEO_TYPE)
                        );
                        videos.add(video);

                    } while (videoCursor.moveToNext());
                }
                videoCursor.close();
            }
        }else{
            Call<TmdbCollection<Video>> call = apiService.getVideos(movie_id, BuildConfig.THE_MOVIE_DB_API_KEY);
            try {
                videos = call.execute().body().getResults();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return videos;
    }

    @Override
    protected void onPostExecute(ArrayList<Video> videos) {
        if(null != mFragment.get()) {
            mFragment.get().updateVideos(videos);
        }
    }
}
