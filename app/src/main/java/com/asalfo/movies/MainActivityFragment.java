package com.asalfo.movies;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.asalfo.movies.adapter.MovieAdapter;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.service.MovieSyncAdapter;
import com.asalfo.movies.ui.CustomRecyclerView;
import com.asalfo.movies.ui.MarginDecoration;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();


    public static final String FAVORITE = "favorite";
    public static final int ID = 0;
    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_POSTER_PATH = 2;
    public static final int COLUMN_BACKDROP_PATH = 3;
    static final String SELECTION = "selection";
    private static final int MOVIE_LOADER = 0;
    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH
    };
    private CustomRecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private ArrayList<String> mFavoriteMovies = new ArrayList<>();
    private boolean mAutoSelectView;


    public MainActivityFragment() {

    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MainActivityFragment,
                0, 0);
        mAutoSelectView = a.getBoolean(R.styleable.MainActivityFragment_autoSelectView, false);
        a.recycle();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        View emptyView = rootView.findViewById(R.id.recyclerview_poster_empty);


        mRecyclerView = (CustomRecyclerView) rootView.findViewById(R.id.recyclerview_poster);
        mRecyclerView.addItemDecoration(new MarginDecoration(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(getActivity(), new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(Long id, MovieAdapter.MovieAdapterViewHolder vh) {

                Boolean favorite = mFavoriteMovies.contains(id.toString());
                ((Callback) getActivity())
                        .onItemSelected(MovieContract.MovieEntry.buildMovieUri(id), favorite, vh
                        );
            }
        }, emptyView);

        mRecyclerView.setAdapter(mMovieAdapter);


        final AppBarLayout appbarView = (AppBarLayout)rootView.findViewById(R.id.appbar);
        if (null != appbarView) {
            ViewCompat.setElevation(appbarView, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (0 == mRecyclerView.computeVerticalScrollOffset()) {
                            appbarView.setElevation(0);
                        } else {
                            appbarView.setElevation(appbarView.getTargetElevation());
                        }
                    }
                });
            }
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        loadFavoriteMovies();
        super.onResume();
    }


    void onSelectionChanged(String selection) {
        Bundle args = new Bundle();
        args.putString(MainActivityFragment.SELECTION, selection);
        getLoaderManager().restartLoader(MOVIE_LOADER, args, this);

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

        favoriteCursor.close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sort_selection = Utility.getPreferredSelection(getActivity()).replace(".", " ");
        Uri movieUri;
        if (null != args) {
            sort_selection = args.getString(SELECTION).replace(".", " ");
        }

        if (sort_selection.equals(FAVORITE) || !Utility.isNetworkAvailable(getActivity())) {
            movieUri = MovieContract.MovieEntry.buildFavoriteMoviesUri();
            sort_selection = MovieSyncAdapter.DEFAULT_SELECTION.replace(".", " ");
        } else {
            movieUri = MovieContract.MovieEntry.CONTENT_URI;
        }

        return  new CursorLoader(getActivity(),
                movieUri,
                MOVIE_COLUMNS,
                null,
                null,
                sort_selection);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        updateToolbar();
        if ( data.getCount() == 0 ) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (mRecyclerView.getChildCount() > 0) {
                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int position = mMovieAdapter.getSelectedItemPosition();

                        if (position == RecyclerView.NO_POSITION) position = 0;
                        RecyclerView.ViewHolder vh = mRecyclerView.findViewHolderForAdapterPosition(position);
                        if (null != vh && mAutoSelectView) {
                            mMovieAdapter.selectView(vh);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void updateToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        if (null != actionBar) {
            actionBar.setTitle(Utility.getSelectionName(getActivity()));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

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

}


