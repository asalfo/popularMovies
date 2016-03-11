package com.asalfo.movies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.asalfo.movies.adapter.TrailerSlideAdapter;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Movie;
import com.asalfo.movies.model.Review;
import com.asalfo.movies.model.TmdbCollection;
import com.asalfo.movies.model.Video;
import com.asalfo.movies.service.ApiService;
import com.asalfo.movies.service.FavoriteMovieTask;
import com.asalfo.movies.service.ServiceGenerator;
import com.asalfo.movies.ui.CirclePageIndicator;
import com.asalfo.movies.ui.PageIndicator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);
    public static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POPULARITY,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_VOTE_COUNT,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_HOMEPAGE,
            MovieContract.MovieEntry.COLUMN_TAGLINE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
            MovieContract.MovieEntry.COLUMN_BUDGET,
            MovieContract.MovieEntry.COLUMN_REVENUE,
            MovieContract.MovieEntry.COLUMN_FAVORITE
    };
    public static final int _ID = 0;
    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_MOVIE_TITLE = 2;
    public static final int COLUMN_ORIGINAL_TITLE = 3;
    public static final int COLUMN_ORIGINAL_LANGUAGE = 4;
    public static final int COLUMN_POSTER_PATH = 5;
    public static final int COLUMN_BACKDROP_PATH = 6;
    public static final int COLUMN_OVERVIEW = 7;
    public static final int COLUMN_POPULARITY = 8;
    public static final int COLUMN_VOTE_AVERAGE = 9;
    public static final int COLUMN_VOTE_COUNT = 10;
    public static final int COLUMN_RELEASE_DATE = 11;
    public static final int COLUMN_HOMEPAGE = 12;
    public static final int COLUMN_TAGLINE = 13;
    public static final int COLUMN_RUNTIME = 14;
    public static final int COLUMN_BUDGET = 15;
    public static final int COLUMN_REVENUE = 16;
    public static final int COLUMN_FAVORITE = 17;

    public static final String[] VIDEO_COLUMNS = {
            MovieContract.VideoEntry._ID,
            MovieContract.VideoEntry.COLUMN_MOVIE_ID,
            MovieContract.VideoEntry.COLUMN_LANGUAGE,
            MovieContract.VideoEntry.COLUMN_KEY,
            MovieContract.VideoEntry.COLUMN_NAME,
            MovieContract.VideoEntry.COLUMN_SITE,
            MovieContract.VideoEntry.COLUMN_SIZE,
            MovieContract.VideoEntry.COLUMN_TYPE
    };
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_VIDEO_LANGUAGE = 2;
    public static final int COL_VIDEO_KEY = 3;
    public static final int COL_VIDEO_NAME = 4;
    public static final int COL_VIDEO_SITE = 5;
    public static final int COL_VIDEO_SIZE = 6;
    public static final int COL_VIDEO_TYPE = 7;

    public static final String[] REVIEWS_COLUMNS = {
            MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.COLUMN_MOVIE_ID,
            MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT,
            MovieContract.ReviewEntry.COLUMN_URL
    };

    public static final int COL_REVIEW_MOVIE_ID = 1;
    public static final int COL_REVIEW_AUTHOR = 2;
    public static final int COL_REVIEW_CONTENT = 3;
    public static final int COL_REVIEW_URL = 4;
    public static final String REVIEWS_KEY = "reviews";
    static final String DETAIL_URI = "URI";
    static final String DETAIL_TRANSITION_ANIMATION = "DTA";
    private static final String FORECAST_SHARE_HASHTAG = " #PopularMovieApp";
    private static final long ANIM_VIEWPAGER_DELAY = 5000;
    private static final long ANIM_VIEWPAGER_DELAY_USER_VIEW = 10000;
    private static int DETAIL_LOADER = 0;
    PageIndicator mIndicator;
    TextView mImgNameTxt;
    boolean mStopSliding = false;
    boolean mFavoriteMovie;
    private String mTmdbMovieId, mLocalMovieId;
    private ArrayList<Video> mVideos = new ArrayList<Video>();
    private ArrayList<Review> mReviews = new ArrayList<Review>();
    private Uri mUri;
    private ImageView mPoster;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverage;
    private TextView mVoteCount;
    private TextView mPopularity;
    private TextView mSynopsis;
    private ImageView mFavIcon;
    private TextView mReviewCount;
    private View mReviewView;
    private TrailerSlideAdapter mTrailerAdapter;
    private ViewPager mViewPager;
    private int mCurrentPage;
    private Runnable animateViewPager;
    private Handler handler;

    public DetailActivityFragment() {
    }


//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_KEY)) {
////            Intent intent = getActivity().getIntent();
////            if (intent != null && intent.hasExtra("movie")) {
////                mMovie = intent.getParcelableExtra("movie");
////                retrieveVideos(mMovie.getId());
////            }
////        } else {
////            Log.d(LOG_TAG, "savedInstanceState");
////            mMovie = savedInstanceState.getParcelable(MOVIE_KEY);
////            mVideos = savedInstanceState.getParcelableArrayList(VIDEO_KEY);
////            mCurrentPage = savedInstanceState.getInt(PAGE_NUM);
////        }
//
//        Bundle arguments = getArguments();
//
//        if (arguments != null) {
//            Log.d(LOG_TAG,"NOT NULL");
//            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
//            // mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
//        }
//
//        this.setRetainInstance(true);
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Bundle arguments = getArguments();

        if (arguments != null) {
            Log.d(LOG_TAG, "NOT NULL");
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
            // mTransitionAnimation = arguments.getBoolean(DetailFragment.DETAIL_TRANSITION_ANIMATION, false);
        } else {
            Log.d(LOG_TAG, "NULL NULL");
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mReviewView = rootView.findViewById(R.id.reviews);
        mReviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ReviewsActivity.class)
                        .putParcelableArrayListExtra(REVIEWS_KEY, mReviews);
                startActivity(intent);
            }
        });


        mFavIcon = (ImageView) rootView.findViewById(R.id.fav_icon);

        mFavIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoriteMovieTask favTask = new FavoriteMovieTask(getActivity(), mFavIcon, mVideos, mReviews);
                String action = mFavoriteMovie ? FavoriteMovieTask.ACTION_REMOVE : FavoriteMovieTask.ACTION_ADD;
                favTask.execute(mTmdbMovieId, mLocalMovieId, action);
            }
        });
        mReviewCount = (TextView) rootView.findViewById(R.id.review_count);
        mPoster = (ImageView) rootView.findViewById(R.id.movie_poster);
        mTitle = (TextView) rootView.findViewById(R.id.movie_title);
        mReleaseDate = (TextView) rootView.findViewById(R.id.movie_release_date);
        mVoteAverage = (TextView) rootView.findViewById(R.id.movie_vote_average);
        mVoteCount = (TextView) rootView.findViewById(R.id.movie_vote_count);
        mPopularity = (TextView) rootView.findViewById(R.id.movie_popularity);
        mSynopsis = (TextView) rootView.findViewById(R.id.movie_synopsis);
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mIndicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
        mImgNameTxt = (TextView) rootView.findViewById(R.id.img_name);


        mIndicator.setOnPageChangeListener(new PageChangeListener());
        mViewPager.addOnPageChangeListener(new PageChangeListener());
        mViewPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction()) {

                    case MotionEvent.ACTION_CANCEL:
                        break;

                    case MotionEvent.ACTION_UP:
                        // calls when touch release on ViewPager
                        if (mVideos != null && mVideos.size() != 0) {
                            mStopSliding = false;
                            runnable(mVideos.size());
                            handler.postDelayed(animateViewPager,
                                    ANIM_VIEWPAGER_DELAY_USER_VIEW);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // calls when ViewPager touch
                        if (handler != null && mStopSliding == false) {
                            mStopSliding = true;
                            handler.removeCallbacks(animateViewPager);
                        }
                        break;
                }
                return false;
            }
        });

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mVideos != null && mVideos.size() != 0) {
            mTrailerAdapter = new TrailerSlideAdapter(
                    getActivity(), mVideos, DetailActivityFragment.this);
            mViewPager.setAdapter(mTrailerAdapter);
            mIndicator.setViewPager(mViewPager);
            mViewPager.setCurrentItem(mCurrentPage);
        }

    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "MOVIE" + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

//        if (mMovie != null) {
//            outState.putParcelable(MOVIE_KEY, mMovie);
//            outState.putParcelableArrayList(VIDEO_KEY, mVideos);
//            outState.putInt(PAGE_NUM, mViewPager.getCurrentItem());
//        }
        super.onSaveInstanceState(outState);
    }

    private boolean isFavorite(Movie movie) {
        Cursor movieCursor = getActivity().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID},
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movie.getId()},
                null);

        if (movieCursor.moveToFirst()) {
            return true;
        }
        return false;
    }


    private void retrieveVideos(String movie_id) {

        if (mFavoriteMovie) {
            Cursor videoCursor = getActivity().getContentResolver().query(
                    MovieContract.VideoEntry.CONTENT_URI,
                    VIDEO_COLUMNS,
                    MovieContract.VideoEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{ movie_id},
                    null);

            if (videoCursor.moveToFirst()) {
                do {
                    Video video = new Video(videoCursor.getString(COL_MOVIE_ID),
                                            videoCursor.getString(COL_VIDEO_LANGUAGE),
                                            videoCursor.getString(COL_VIDEO_KEY),
                                            videoCursor.getString(COL_VIDEO_NAME),
                                            videoCursor.getString(COL_VIDEO_SITE),
                                            videoCursor.getString(COL_VIDEO_SIZE),
                                            videoCursor.getString(COL_VIDEO_TYPE)
                                         );
                    mVideos.add(video);


                }while (videoCursor.moveToNext());
            }

            if (mVideos != null && mVideos.size() != 0) {
                mTrailerAdapter = new TrailerSlideAdapter(
                        getActivity(), mVideos, DetailActivityFragment.this);
                mViewPager.setAdapter(mTrailerAdapter);

                mIndicator.setViewPager(mViewPager);
                mImgNameTxt.setText(""
                        + ((Video) mVideos.get(mViewPager
                        .getCurrentItem())).getName());
                runnable(mVideos.size());
                handler.postDelayed(animateViewPager,
                        ANIM_VIEWPAGER_DELAY);
            } else {
                mImgNameTxt.setText("No trailer");
            }
            videoCursor.close();


        } else {
            Call<TmdbCollection<Video>> call = apiService.getVideos(movie_id, BuildConfig.THE_MOVIE_DB_API_KEY);
            call.enqueue(new Callback<TmdbCollection<Video>>() {
                @Override
                public void onResponse(Response<TmdbCollection<Video>> response) {
                    if (response.isSuccess()) {
                        TmdbCollection<Video> collection = response.body();
                        mVideos.addAll(collection.getResults());
                        if (mVideos != null && mVideos.size() != 0) {
                            mTrailerAdapter = new TrailerSlideAdapter(
                                    getActivity(), mVideos, DetailActivityFragment.this);
                            mViewPager.setAdapter(mTrailerAdapter);

                            mIndicator.setViewPager(mViewPager);
                            mImgNameTxt.setText(""
                                    + ((Video) mVideos.get(mViewPager
                                    .getCurrentItem())).getName());
                            runnable(mVideos.size());
                            handler.postDelayed(animateViewPager,
                                    ANIM_VIEWPAGER_DELAY);
                        } else {
                            mImgNameTxt.setText("No trailer");
                        }

                    } else {
                        Log.d(LOG_TAG, "Faill");
                    }

                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d(LOG_TAG, t.getMessage());
                }
            });
        }


    }


    public void runnable(final int size) {
        handler = new Handler();
        animateViewPager = new Runnable() {
            public void run() {
                if (!mStopSliding) {
                    if (mViewPager.getCurrentItem() == size - 1) {
                        mViewPager.setCurrentItem(0);
                    } else {
                        mViewPager.setCurrentItem(
                                mViewPager.getCurrentItem() + 1, true);
                    }
                    handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
                }
            }
        };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader sss " + mUri);

        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );

        }
        ViewParent vp = getView().getParent();
        if (vp instanceof CardView) {
            ((View) vp).setVisibility(View.INVISIBLE);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            ViewParent vp = getView().getParent();
            if (vp instanceof CardView) {
                ((View) vp).setVisibility(View.VISIBLE);
            }
            mTmdbMovieId = data.getString(COLUMN_MOVIE_ID);
            mLocalMovieId = data.getString(_ID);
            mFavoriteMovie = (data.getInt(COLUMN_FAVORITE) == 1) ? true : false;
            if (mFavoriteMovie) {
                mFavIcon.setImageResource(R.drawable.ic_favorite);
            }
            String movie_id = data.getString(COLUMN_MOVIE_ID);

            String posterUrl = Utility.generatePosterUrl(data.getString(COLUMN_POSTER_PATH), "w185");
            Picasso.with(getActivity()).load(posterUrl).into(mPoster);
            Picasso.with(getActivity()).load(posterUrl).into(mPoster);
            mTitle.setText(data.getString(COLUMN_ORIGINAL_TITLE));
            mReleaseDate.setText("Released: " + data.getString(COLUMN_RELEASE_DATE));
            mVoteAverage.setText(data.getString(COLUMN_VOTE_AVERAGE));
            mVoteCount.setText(data.getString(COLUMN_VOTE_COUNT));
            mPopularity.setText(String.format("%1$.2f", data.getFloat(COLUMN_POPULARITY)));
            mSynopsis.setText(data.getString(COLUMN_OVERVIEW));
            retrieveVideos(movie_id);
            retreiveReviews(movie_id);


        }

    }

    private void retreiveReviews(String movie_id) {

        if (mFavoriteMovie) {
            Log.d(LOG_TAG,movie_id);
            Cursor reviewCursor = getActivity().getContentResolver().query(
                    MovieContract.ReviewEntry.CONTENT_URI,
                    REVIEWS_COLUMNS,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{ movie_id},
                    null);

            if (reviewCursor.moveToFirst()) {
                do {
                    Review review = new Review(reviewCursor.getString(COL_REVIEW_MOVIE_ID),
                            reviewCursor.getString(COL_REVIEW_AUTHOR),
                            reviewCursor.getString(COL_REVIEW_CONTENT),
                            reviewCursor.getString(COL_REVIEW_URL)
                    );
                    mReviews.add(review);


                }while (reviewCursor.moveToNext());
            }
            if (mReviews != null && mReviews.size() > 0) {
                mReviewCount.setText(Integer.toString(mReviews.size()));
                mReviewView.setVisibility(View.VISIBLE);
            }
            reviewCursor.close();


        } else {

            Call<TmdbCollection<Review>> call = apiService.getReviews(movie_id, BuildConfig.THE_MOVIE_DB_API_KEY);
            call.enqueue(new Callback<TmdbCollection<Review>>() {
                @Override
                public void onResponse(Response<TmdbCollection<Review>> response) {
                    if (response.isSuccess()) {
                        TmdbCollection<Review> collection = response.body();
                        mReviews.addAll(collection.getResults());
                        if (mReviews != null && mReviews.size() > 0) {
                            mReviewCount.setText(Integer.toString(mReviews.size()));
                            mReviewView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.d(LOG_TAG, "Faill");
                    }

                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d(LOG_TAG, t.getMessage());
                }
            });
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (mVideos != null) {
                    mImgNameTxt.setText(""
                            + ((Video) mVideos.get(mViewPager
                            .getCurrentItem())).getName());
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
        }
    }
}

