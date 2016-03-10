package com.asalfo.movies;

import android.app.Activity;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> ,SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);
    public static final String CURRENT_PAGE_KEY = "current_page";
    public static final String NEXT_PAGE_KEY = "next_page";
    private static final String SELECTED_KEY = "selected_position";
    private static final String MOVIE_LIST_KEY = "movie_list";
    RecyclerView mRecyclerView;
    ProgressBar mProgressBar;
    SwipeRefreshLayout mSwipeLayout;
    int mCurrentPage;
    int mNextPage;
    Boolean mUserScrolled = false;
    private MovieAdapter mMovieAdapter;

    private ArrayList<Movie> mMovieList;
    private int mPosition = GridView.INVALID_POSITION;
    private String mSortValue;
    private int mChoiceMode;
    private boolean mAutoSelectView;
    private boolean mHoldForTransition;
    private long mInitialSelectedDate = -1;


    private static final int MOVIE_LOADER = 0;

    private static final String[] FAVORITE_MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH
    };

    public static final int ID = 0;
    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_POSTER_PATH = 2 ;
    public static final int COLUMN_BACKDROP_PATH  = 3;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailActivityFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri uri, MovieAdapter.MovieAdapterViewHolder vh);
    }
    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_LIST_KEY)) {
            mMovieList = new ArrayList<Movie>();
            mCurrentPage = 0;
            mNextPage = 1;
        } else {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
            mCurrentPage = savedInstanceState.getInt(CURRENT_PAGE_KEY);
            mNextPage = savedInstanceState.getInt(NEXT_PAGE_KEY);
        }
        mSortValue = Utility.getPreferredSelection(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        if (!mMovieList.isEmpty()) {
            outState.putParcelableArrayList(MOVIE_LIST_KEY, mMovieList);
            outState.putInt(CURRENT_PAGE_KEY, mCurrentPage);
            outState.putInt(NEXT_PAGE_KEY, mNextPage);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.MainActivityFragment,
                0, 0);
        mChoiceMode = a.getInt(R.styleable.MainActivityFragment_android_choiceMode, AbsListView.CHOICE_MODE_NONE);
        mAutoSelectView = a.getBoolean(R.styleable.MainActivityFragment_android_choiceMode, false);
        mHoldForTransition = a.getBoolean(R.styleable.MainActivityFragment_android_choiceMode, false);
        a.recycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_poster);

        View emptyView = rootView.findViewById(R.id.recyclerview_poster_empty);




        // Set the layout manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(getActivity(), new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(Long id, MovieAdapter.MovieAdapterViewHolder vh) {
           ((Callback) getActivity())
                        .onItemSelected(MovieContract.MovieEntry.buildMovieUri(id),
                                vh
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
        super.onResume();
    }


    // since we read the location when we create the loader, all we need to do is restart things
    void onSelectionChanged() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }



    private void updateMovie() {
//        String sort_value = Utility.getPreferredSelection(getActivity());
//        if (sort_value != null && !sort_value.equals(mSortValue)) {
//            mMovieAdapter.clear();
//            mNextPage = 1;
//            mCurrentPage = 0;
//            mSortValue = sort_value;
//        }
//        if (mCurrentPage != mNextPage) {
//            mProgressBar.setVisibility(View.VISIBLE);
////            FetchMovieTask mMovieTask = new FetchMovieTask();
////            mMovieTask.execute(sort_value);
//            //fetchMovies(sort_value, mNextPage);
//        }
//        Log.d(LOG_TAG, "Called updateMovie!" + sort_value);
//        Log.d(LOG_TAG, "Called updateMovie!" + mSortValue);
        MovieSyncAdapter.syncImmediately(getActivity());

    }

    @Override
    public void onStart() {
        super.onStart();
       // updateMovie();
//        if (mPosition != GridView.INVALID_POSITION) {
//            mGridView.smoothScrollToPosition(mPosition);
//            Log.d(LOG_TAG, "Movie Current position!" + mPosition);
//        }
    }


//    public void fetchMovies(String sort, int page) {
//        Call<TmdbCollection<Movie>> call = apiService.getShortedMovies(sort, page, BuildConfig.THE_MOVIE_DB_API_KEY);
//        call.enqueue(new Callback<TmdbCollection<Movie>>() {
//            @Override
//            public void onResponse(Response<TmdbCollection<Movie>> response) {
//                if (response.isSuccess()) {
//                    TmdbCollection<Movie> collection = response.body();
//                    mMovieAdapter.addAll(collection.getResults());
//                    mCurrentPage = mNextPage;
//                } else {
//                    Log.d(LOG_TAG, "Faill");
//                }
//
//                mProgressBar.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                Log.d(LOG_TAG, t.getMessage());
//            }
//        });
//
//    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;

       CursorLoader cl =  new CursorLoader(getActivity(),
                movieUri,
                FAVORITE_MOVIE_COLUMNS,
                null,
                null,
                sortOrder);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
       mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}


