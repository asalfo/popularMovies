package com.asalfo.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by asalfo on 21/01/16.
 */
public class Utility {

    public static String getPreferredSortBy(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_most_popular));

    }
}
