package com.asalfo.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment  {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    public static final String CURRENT_PAGE_KEY = "current_page";
    public static final String NEXT_PAGE_KEY = "next_page";
    private MovieAdapter mMovieAdapter;
    private ArrayList<Movie> mMovieList;
    GridView mGridView;
    ProgressBar mProgressBar;
    SwipeRefreshLayout mSwipeLayout;
    int mCurrentPage;
    int mNextPage;
    Boolean mUserScrolled = false;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final String MOVIE_LIST_KEY = "movie_list";
    private String mSortValue;



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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);

        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mGridView.setAdapter(mMovieAdapter);

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

    private void updateMovie() {
        String sort_value = Utility.getPreferredSortBy(getActivity());
        if (sort_value != null && !sort_value.equals(mSortValue)) {
            mMovieAdapter.clear();
            mNextPage =1;
            mCurrentPage = 0;
            mSortValue = sort_value;
        }
        if (mCurrentPage != mNextPage) {
            mProgressBar.setVisibility(View.VISIBLE);
            FetchMovieTask mMovieTask = new FetchMovieTask();
            mMovieTask.execute(sort_value);
        }
        Log.d(LOG_TAG, "Called updateMovie!" + sort_value);
        Log.d(LOG_TAG, "Called updateMovie!" + mSortValue);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
        if(mPosition != GridView.INVALID_POSITION) {
            mGridView.smoothScrollToPosition(mPosition);
            Log.d(LOG_TAG, "Movie Current position!" + mPosition);
        }
    }



    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        private Context context;

        public FetchMovieTask() {
        }

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULT = "results";
            final String TMDB_MOVIE_ID = "id";
            final String TMDB_MOVIE_POSTER_PATH = "poster_path";
            final String TMDB_MOVIE_OVERVIEW = "overview";
            final String TMDB_MOVIE_RELEASE_DATE = "release_date";
            final String TMDB_MOVIE_TITLE = "original_title";
            final String TMDB_MOVIE_VOTE_AVERAGE = "vote_average";
            final String TMDB_TOTAL_RESULTS = "total_results";
            final String TMDB_PAGE = "page";
            final String TMDB_TOTAL_PAGES = "total_pages";

            final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/#";
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULT);
            SharedPreferences sharedPrefs =
                    PreferenceManager.getDefaultSharedPreferences(getActivity());

            ArrayList<Movie> movies = new ArrayList();

            for (int i = 0; i < movieArray.length(); i++) {
                String id;
                String title;
                String thumbnailUrl;
                String synopsis;
                Float rating;
                String releaseDate;

                // Get the JSON object representing the movie
                JSONObject movieObject = movieArray.getJSONObject(i);
                id = movieObject.getString(TMDB_MOVIE_ID);
                title = movieObject.getString(TMDB_MOVIE_TITLE);
                thumbnailUrl = MOVIE_POSTER_BASE_URL.concat(movieObject.getString(TMDB_MOVIE_POSTER_PATH));
                synopsis = movieObject.getString(TMDB_MOVIE_OVERVIEW);
                rating = (float) movieObject.getDouble(TMDB_MOVIE_VOTE_AVERAGE);
                releaseDate = movieObject.getString(TMDB_MOVIE_RELEASE_DATE);
                Movie movie = new Movie(id, title, thumbnailUrl, synopsis, rating, releaseDate);

                movies.add(movie);
            }
            return movies;

        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String MOVIE_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY_PARAM = "sort_by";
                final String APIKEY_PARAM = "api_key";
                final String PAGE_PARAM = "page";
                Log.d(LOG_TAG, "Next page to be loaded!" + mNextPage);
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, params[0])
                        .appendQueryParameter(PAGE_PARAM, String.valueOf(mNextPage))
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());
                Log.d(LOG_TAG, "Called url!" + url);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> result) {
            if (result != null) {
                mMovieAdapter.addAll(result);
                mCurrentPage = mNextPage;
            }
            Log.d(LOG_TAG, "NextPage =" + mNextPage);
            Log.d(LOG_TAG, "CurrentPage " + mCurrentPage);

            mProgressBar.setVisibility(View.GONE);
        }
    }
}


