package com.asalfo.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.asalfo.movies.adapter.MovieAdapter;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Movie;
import com.asalfo.movies.service.ApiService;
import com.asalfo.movies.service.MovieSyncAdapter;
import com.asalfo.movies.service.ServiceGenerator;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);
    public static final String CURRENT_PAGE_KEY = "current_page";
    public static final String NEXT_PAGE_KEY = "next_page";
    private static final String SELECTED_KEY = "selected_position";
    private static final String MOVIE_LIST_KEY = "movie_list";
    public static final String FAVORITE = "favorite";
    RecyclerView mRecyclerView;
    ProgressBar mProgressBar;
    SwipeRefreshLayout mSwipeLayout;
    int mCurrentPage;
    int mNextPage;
    Boolean mUserScrolled = false;
    private MovieAdapter mMovieAdapter;

    private ArrayList<Movie> mMovieList;
    private int mPosition = GridView.INVALID_POSITION;

    static final String SELECTION = "selection";
    private int mChoiceMode;
    private boolean mAutoSelectView;
    private ArrayList<String> mFavoriteMovies = new ArrayList<>();


    private static final int MOVIE_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH
    };

    public static final int ID = 0;
    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_POSTER_PATH = 2;
    public static final int COLUMN_BACKDROP_PATH = 3;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailActivityFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri uri, Boolean favorite, MovieAdapter.MovieAdapterViewHolder vh);
    }

    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInflate(Context activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.MainActivityFragment,
                0, 0);
        mChoiceMode = a.getInt(R.styleable.MainActivityFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.MainActivityFragment_android_choiceMode, false);
        a.recycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_poster);

        View emptyView = rootView.findViewById(R.id.recyclerview_poster_empty);


        // Set the layout manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(getActivity(), new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(Long id, MovieAdapter.MovieAdapterViewHolder vh) {

                Boolean favorite = mFavoriteMovies.contains(id.toString());
                ((Callback) getActivity())
                        .onItemSelected(MovieContract.MovieEntry.buildMovieUri(id), favorite, vh
                        );
            }
        }, emptyView, mChoiceMode);

        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(mMovieAdapter);


        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            if (savedInstanceState != null) {
                mMovieAdapter.onRestoreInstanceState(savedInstanceState);
            }
        }


        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Called onActivityCreated!");
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        loadFavoriteMovies();
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        super.onResume();
    }


    // since we read the selection when we create the loader, all we need to do is restart things
    void onSelectionChanged(String selection) {
        Bundle args = new Bundle();
        args.putString(MainActivityFragment.SELECTION, selection);
        getLoaderManager().restartLoader(MOVIE_LOADER, args, this);
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void loadFavoriteMovies() {

        Cursor favoriteCursor = getActivity().getContentResolver().query(
                MovieContract.FavoriteEntry.CONTENT_URI,
                new String[]{MovieContract.FavoriteEntry._ID, MovieContract.FavoriteEntry.COLUMN_MOVIE_ID},
                null, null, null);
        mFavoriteMovies.clear();
        if (favoriteCursor.moveToNext()) {
            do {
                mFavoriteMovies.add(favoriteCursor.getString(1));
            } while (favoriteCursor.moveToNext());
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sort = Utility.getPreferredSelection(getActivity()).replace(".", " ");
        String selection = null;
        Uri movieUri;
        if (null != args) {
            sort = args.getString(SELECTION).replace(".", " ");
        }

        if (sort.equals(FAVORITE)) {
            movieUri = MovieContract.MovieEntry.buildFavoriteMoviesUri();
            sort = MovieSyncAdapter.DEFAULT_SELECTION.replace(".", " ");
        } else {
            movieUri = MovieContract.MovieEntry.CONTENT_URI;
        }

        CursorLoader cl = new CursorLoader(getActivity(),
                movieUri,
                MOVIE_COLUMNS,
                null,
                null,
                sort);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);

        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // Since we know we're going to get items, we keep the listener around until
                // we see Children.
                if (mRecyclerView.getChildCount() > 0) {
                    mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int position = mMovieAdapter.getSelectedItemPosition();

                    if (position == RecyclerView.NO_POSITION) position = 0;
                    // If we don't need to restart the loader, and there's a desired position to restore
                    // to, do so now.
                    mRecyclerView.smoothScrollToPosition(position);
                    RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(position);
                    if (null != vh && mAutoSelectView) {
                        mMovieAdapter.selectView(vh);
                    }

                    return true;
                }
                return false;
            }
        });

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);
        if (null != toolbarView) {
            toolbarView.setTitle(Utility.getSelectionName(getActivity()));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}


