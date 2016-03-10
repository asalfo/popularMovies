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
    public static final String FAVORITE = "favorite";


    public static String getPreferredSelection(Context context) {
//        if(isConnectedToInternet(context)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(context.getString(R.string.pref_sort_key),
                    context.getString(R.string.pref_sort_most_popular));
//        }else{
//            return  FAVORITE;
//        }
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

    public static Boolean isConnectedToInternet(Context context ){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.isConnectedOrConnecting();
    }
}
