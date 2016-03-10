package com.asalfo.movies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MovieProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper mDBHelper;

    static final int MOVIE = 100;
    static final int VIDEO_WITH_ID =101;
    static final int VIDEO = 200;
    static final int VIDEO_WITH_MOVIE = 201;
    static final int REVIEW = 300;
    static final int REVIEW_WITH_MOVIE = 301;
    static final int FAVORITE = 400;


    private static final SQLiteQueryBuilder sVideoQueryBuilder;
    private static final SQLiteQueryBuilder sReviewQueryBuilder;
    private static final SQLiteQueryBuilder sMovieQueryBuilder;
    private static final SQLiteQueryBuilder sFavoitreQueryBuilder;

    static{
        sVideoQueryBuilder = new SQLiteQueryBuilder();
        sVideoQueryBuilder.setTables(
                MovieContract.VideoEntry.TABLE_NAME);
    }

    static{
        sReviewQueryBuilder = new SQLiteQueryBuilder();
        sReviewQueryBuilder.setTables(MovieContract.ReviewEntry.TABLE_NAME);
    }

    static{
        sMovieQueryBuilder = new SQLiteQueryBuilder();
        sMovieQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
    }

    static{
        sFavoitreQueryBuilder = new SQLiteQueryBuilder();
        sFavoitreQueryBuilder.setTables(MovieContract.FavoriteEntry.TABLE_NAME);
    }



    //video.movie_id = ?
    private static final String sMovieSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    //video.movie_id = ?
    private static final String sVideoMovieSelection =
            MovieContract.VideoEntry.TABLE_NAME+
                    "." + MovieContract.VideoEntry.COLUMN_MOVIE_ID + " = ? ";
    //review.movie_id = ?
    private static final String sReviewMovieSelection =
            MovieContract.ReviewEntry.TABLE_NAME+
                    "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";


    //favorite.movie_id = ?
    private static final String sFavoriteMovieSelection =
            MovieContract.FavoriteEntry.TABLE_NAME+
                    "." + MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + " = ? ";




    private Cursor getFavorite(Uri uri, String[] projection, String sortOrder) {
        String movie_id = MovieContract.FavoriteEntry.getFavoriteMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sFavoriteMovieSelection;
        selectionArgs = new String[]{movie_id};

        return sFavoitreQueryBuilder.query(mDBHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    private Cursor getMovie(Uri uri, String[] projection, String sortOrder) {
        String movie_id = MovieContract.MovieEntry.getMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sMovieSelection;
        selectionArgs = new String[]{movie_id};

        return sMovieQueryBuilder.query(mDBHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getVideosByMovie(Uri uri, String[] projection, String sortOrder) {
        String movie_id = MovieContract.VideoEntry.getVideoMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection;

            selection = sVideoMovieSelection;
            selectionArgs = new String[]{movie_id};

        return sVideoQueryBuilder.query(mDBHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    private Cursor getReviewsByMovie(Uri uri, String[] projection, String sortOrder) {
        String movie_id = MovieContract.ReviewEntry.getReviewMovieIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sReviewMovieSelection;
        selectionArgs = new String[]{movie_id};

        return sReviewQueryBuilder.query(mDBHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }


    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*", VIDEO_WITH_ID);

        matcher.addURI(authority, MovieContract.PATH_FAVORITE, FAVORITE);

        matcher.addURI(authority, MovieContract.PATH_VIDEO, VIDEO);
        matcher.addURI(authority, MovieContract.PATH_VIDEO + "/*", VIDEO_WITH_MOVIE);

        matcher.addURI(authority, MovieContract.PATH_REVIEW, REVIEW);
        matcher.addURI(authority, MovieContract.PATH_REVIEW + "/*", REVIEW_WITH_MOVIE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new MovieDBHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "video/*"
            case VIDEO_WITH_MOVIE:
            {
                retCursor = getVideosByMovie(uri, projection, sortOrder);
                break;
            }
            // "video"
            case VIDEO: {
                retCursor = mDBHelper.getReadableDatabase().query(
                        MovieContract.VideoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "video/*"
            case REVIEW_WITH_MOVIE:
            {
                retCursor = getReviewsByMovie(uri, projection, sortOrder);
                break;
            }
            // "video"
            case REVIEW: {
                retCursor = mDBHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "movie/*"
            case VIDEO_WITH_ID:
            {
                retCursor = getMovie(uri, projection, sortOrder);
                break;
            }
            // "movie"
            case MOVIE: {
                retCursor = mDBHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            // "movie"
            case FAVORITE: {
                retCursor = mDBHelper.getReadableDatabase().query(
                        MovieContract.FavoriteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
            }
            break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case VIDEO:
                return MovieContract.VideoEntry.CONTENT_TYPE;
            case VIDEO_WITH_MOVIE:
                return MovieContract.VideoEntry.CONTENT_ITEM_TYPE;
            case REVIEW:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW_WITH_MOVIE:
                return MovieContract.ReviewEntry.CONTENT_ITEM_TYPE;
            case FAVORITE:
                return MovieContract.FavoriteEntry.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case VIDEO: {
                long _id = db.insert(MovieContract.VideoEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.VideoEntry.buildVideoUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEW: {
                long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.ReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case FAVORITE: {
                long _id = db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MovieContract.FavoriteEntry.buildFavoritewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(
                        MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case VIDEO:
                rowsDeleted = db.delete(
                        MovieContract.VideoEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case FAVORITE:
                rowsDeleted = db.delete(
                        MovieContract.FavoriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEW:
                rowsUpdated = db.update(MovieContract.ReviewEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case VIDEO:
                rowsUpdated = db.update(MovieContract.VideoEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case FAVORITE:
                rowsUpdated = db.update(MovieContract.FavoriteEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


}
