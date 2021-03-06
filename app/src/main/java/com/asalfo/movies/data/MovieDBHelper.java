package com.asalfo.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.asalfo.movies.data.MovieContract.MovieEntry;
import com.asalfo.movies.data.MovieContract.ReviewEntry;
import com.asalfo.movies.data.MovieContract.VideoEntry;
import com.asalfo.movies.data.MovieContract.FavoriteEntry;

/**
 * Manages a local database for movie data.
 */
public class MovieDBHelper extends SQLiteOpenHelper {


    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movie.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a table to hold movies.
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY," +
                MovieEntry.COLUMN_MOVIE_ID +" INTEGER NOT NULL, " +
                MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_BACKDROP_PATH + " TEXT , " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MovieEntry.COLUMN_VOTE_COUNT + " REAL NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_HOMEPAGE + " TEXT, " +
                MovieEntry.COLUMN_TAGLINE + " TEXT, " +
                MovieEntry.COLUMN_RUNTIME + " INTEGER, " +
                MovieEntry.COLUMN_BUDGET + " DOUBLE, " +
                MovieEntry.COLUMN_REVENUE + " DOUBLE, " +
                MovieEntry.COLUMN_FAVORITE + " BOOL ," +
                " UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ", " +
                MovieEntry.COLUMN_MOVIE_TITLE + ") ON CONFLICT REPLACE);";


        // Create a table to hold trailers.
        final String SQL_CREATE_VIDEO_TABLE = "CREATE TABLE " + VideoEntry.TABLE_NAME + " (" +
                VideoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                VideoEntry.COLUMN_VIDEO_ID +" INTEGER NOT NULL, " +
                VideoEntry.COLUMN_FAVORITE_ID +" INTEGER NOT NULL, " +
                VideoEntry.COLUMN_LANGUAGE + " TEXT , " +
                VideoEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_SITE + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_SIZE + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                // Set up the movie column as a foreign key to movie table.
                " FOREIGN KEY (" + VideoEntry.COLUMN_FAVORITE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + FavoriteEntry._ID + ") ON DELETE CASCADE, " +
                " UNIQUE (" + VideoEntry.COLUMN_VIDEO_ID + ") ON CONFLICT REPLACE);";


        // Create a table to hold reviews.
        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY," +
                ReviewEntry.COLUMN_REVIEW_ID +" INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_FAVORITE_ID +" INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_URL + " TEXT NOT NULL, " +
                " FOREIGN KEY (" + ReviewEntry.COLUMN_FAVORITE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + FavoriteEntry._ID + ")  ON DELETE CASCADE, " +
                " UNIQUE (" + ReviewEntry.COLUMN_REVIEW_ID + ") ON CONFLICT REPLACE);";

        // Create a table to hold favorites.
        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY," +
                FavoriteEntry.COLUMN_MOVIE_ID +" INTEGER NOT NULL, " +
                " FOREIGN KEY (" + FavoriteEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ")  ON DELETE CASCADE" +
                " );";


        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);
        db.execSQL(SQL_CREATE_VIDEO_TABLE);
        db.execSQL(SQL_CREATE_FAVORITE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VideoEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        onCreate(db);

    }
}
