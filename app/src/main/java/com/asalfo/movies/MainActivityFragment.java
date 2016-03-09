package com.asalfo.movies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.asalfo.movies.adapter.FavoriteMovieAdapter;
import com.asalfo.movies.adapter.MovieAdapter;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Movie;
import com.asalfo.movies.model.TmdbCollection;
import com.asalfo.movies.service.ApiService;
import com.asalfo.movies.service.ServiceGenerator;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);
    public static final String CURRENT_PAGE_KEY = "current_page";
    public static final String NEXT_PAGE_KEY = "next_page";
    private static final String SELECTED_KEY = "selected_position";
    private static final String MOVIE_LIST_KEY = "movie_list";
    GridView mGridView;
    ProgressBar mProgressBar;
    SwipeRefreshLayout mSwipeLayout;
    int mCurrentPage;
    int mNextPage;
    Boolean mUserScrolled = false;
    private MovieAdapter mMovieAdapter;
    private FavoriteMovieAdapter mFavoriteMovieAdapter;
    private ArrayList<Movie> mMovieList;
    private int mPosition = GridView.INVALID_POSITION;
    private String mSortValue;


    private static final int FAVORITE_MOVIE_LOADER = 0;

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


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_LIST_KEY)) {
            mMovieList = new ArrayList<Movie>();
            mCurrentPage = 0;
            mNextPage = 1;
        } else {
            mMovieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
            mCurrentPage = savedInstanceState.getInt(CURRENT_PAGE_KEY);
            mNextPage = savedInstanceState.getInt(NEXT_PAGE_KEY);
        }
        mSortValue = Utility.getPreferredSortBy(getActivity());
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMovieAdapter = new MovieAdapter(getActivity(), 0, mMovieList);
         mFavoriteMovieAdapter = new FavoriteMovieAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);

        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mGridView.setAdapter(mFavoriteMovieAdapter);
        //mGridView.setAdapter(mMovieAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mMovieAdapter.getItem(position);
                mPosition = position;
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("movie", movie);
                startActivity(intent);

            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    mUserScrolled = true;

                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mUserScrolled
                        && firstVisibleItem + visibleItemCount == totalItemCount) {
                    mUserScrolled = false;
                    mNextPage++;
                    Log.d(LOG_TAG, "onScroll Movie Page to be loaded !" + mNextPage);
                    updateMovie();

                }

            }
        });

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Called onActivityCreated!");
        getLoaderManager().initLoader(FAVORITE_MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateMovie() {
        String sort_value = Utility.getPreferredSortBy(getActivity());
        if (sort_value != null && !sort_value.equals(mSortValue)) {
            mMovieAdapter.clear();
            mNextPage = 1;
            mCurrentPage = 0;
            mSortValue = sort_value;
        }
        if (mCurrentPage != mNextPage) {
            mProgressBar.setVisibility(View.VISIBLE);
//            FetchMovieTask mMovieTask = new FetchMovieTask();
//            mMovieTask.execute(sort_value);
            //fetchMovies(sort_value, mNextPage);
        }
        Log.d(LOG_TAG, "Called updateMovie!" + sort_value);
        Log.d(LOG_TAG, "Called updateMovie!" + mSortValue);
    }

    @Override
    public void onStart() {
        super.onStart();
       // updateMovie();
        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
            Log.d(LOG_TAG, "Movie Current position!" + mPosition);
        }
    }


    public void fetchMovies(String sort, int page) {
        Call<TmdbCollection<Movie>> call = apiService.getMovies(sort, page, BuildConfig.THE_MOVIE_DB_API_KEY);
        call.enqueue(new Callback<TmdbCollection<Movie>>() {
            @Override
            public void onResponse(Response<TmdbCollection<Movie>> response) {
                if (response.isSuccess()) {
                    TmdbCollection<Movie> collection = response.body();
                    mMovieAdapter.addAll(collection.getResults());
                    mCurrentPage = mNextPage;
                } else {
                    Log.d(LOG_TAG, "Faill");
                }

                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(LOG_TAG, t.getMessage());
            }
        });

    }


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
       mFavoriteMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoriteMovieAdapter.swapCursor(null);

    }
}


