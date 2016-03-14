package com.asalfo.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Created by asalfo on 21/01/16.
 */
public class Utility {

    public static final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/%width%%path%";
    public static final String YOUTUBE_VIDEO_THUMBS_URL = "http://img.youtube.com/vi/%video_id%/hqdefault.jpg";
    public static final String YOUTUBE_VIDEO__URL = "https://www.youtube.com/watch?v=%video_id%";
    public static final String FAVORITE = "favorite";



    public static String getPreferredSelection(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(context.getString(R.string.pref_sort_key),
                    context.getString(R.string.pref_sort_most_popular));
    }

    public static String getSelectionName(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String selection = prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_most_popular));
        String[] options =  context.getResources().getStringArray(R.array.pref_sort_options);
        if(selection == context.getString(R.string.pref_sort_most_popular)){
            return options[0];
        }else if(selection == context.getString(R.string.pref_sort_highest_rated)){
            return options[1];
        }else {
            return options[2];
        }
    }

    public static String generatePosterUrl(String poster_path, String imageWidth) {
        if (poster_path != null) {
            return MOVIE_POSTER_BASE_URL.replace("%width%", imageWidth).replace("%path%", poster_path);
        }
        return null;
    }


    public static String generateYoutubeVideoThumbnailUrl(String video_id) {
        if (video_id != null) {
            return YOUTUBE_VIDEO_THUMBS_URL.replace("%video_id%", video_id);
        }
        return null;
    }

    public static String generateYoutubeVideoUrl(String video_id) {
        if (video_id != null) {
            return YOUTUBE_VIDEO__URL.replace("%video_id%", video_id);
        }
        return null;
    }

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

}
