package com.asalfo.movies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.asalfo.movies.adapter.ReviewAdapter;
import com.asalfo.movies.adapter.TrailerSlideAdapter;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Review;
import com.asalfo.movies.model.Video;
import com.asalfo.movies.service.ApiService;
import com.asalfo.movies.service.FavoriteMovieTask;
import com.asalfo.movies.service.ReviewTask;
import com.asalfo.movies.service.ServiceGenerator;
import com.asalfo.movies.service.VideoTask;
import com.asalfo.movies.ui.CirclePageIndicator;
import com.asalfo.movies.ui.PageIndicator;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
            MovieContract.VideoEntry.TABLE_NAME + "." + MovieContract.VideoEntry._ID,
            MovieContract.VideoEntry.COLUMN_LANGUAGE,
            MovieContract.VideoEntry.COLUMN_KEY,
            MovieContract.VideoEntry.COLUMN_NAME,
            MovieContract.VideoEntry.COLUMN_SITE,
            MovieContract.VideoEntry.COLUMN_SIZE,
            MovieContract.VideoEntry.COLUMN_TYPE
    };

    public static final int COL_VIDEO_ID = 0;
    public static final int COL_VIDEO_LANGUAGE = 1;
    public static final int COL_VIDEO_KEY = 2;
    public static final int COL_VIDEO_NAME = 3;
    public static final int COL_VIDEO_SITE = 4;
    public static final int COL_VIDEO_SIZE = 5;
    public static final int COL_VIDEO_TYPE = 6;

    public static final String[] REVIEWS_COLUMNS = {
            MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry._ID,
            MovieContract.ReviewEntry.COLUMN_AUTHOR,
            MovieContract.ReviewEntry.COLUMN_CONTENT,
            MovieContract.ReviewEntry.COLUMN_URL
    };

    public static final int COL_REVIEW_ID = 0;
    public static final int COL_REVIEW_AUTHOR = 1;
    public static final int COL_REVIEW_CONTENT = 2;
    public static final int COL_REVIEW_URL = 3;

    public static final String REVIEWS_KEY = "reviews";
    public static final String VIDEO_KEY = "reviews";
    static final String DETAIL_URI = "URI";
    static final String TWO_PANE = "twopane";
    static final String FAVORITE = "favorite";

    private static final String MOVIE_SHARE_HASHTAG = " #PopularMovieApp";
    private static final long ANIM_VIEWPAGER_DELAY = 5000;
    private static final long ANIM_VIEWPAGER_DELAY_USER_VIEW = 10000;
    private static int DETAIL_LOADER = 0;
    PageIndicator mIndicator;
    TextView mImgNameTxt;
    boolean mStopSliding = false;
    boolean mFavoriteMovie;
    private String mTmdbMovieId;
    private ArrayList<Video> mVideos = new ArrayList<Video>();
    private ArrayList<Review> mReviews = new ArrayList<Review>();
    private Uri mUri;
    private ImageView mPoster;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverage;
    private TextView mVoteCount;
    private TextView mPopularity;
    private ExpandableTextView mSynopsis;
    private ImageView mFavIcon;

    private RecyclerView mRecyclerViewReview;
    private TextView mEmptyReviewView;
    private ReviewAdapter mReviewAdapter;
    private TrailerSlideAdapter mTrailerAdapter;
    private ViewPager mViewPager;
    private Runnable animateViewPager;
    private Handler handler;
    private String mShareTrailer;
    private boolean mTwoPane;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
            mFavoriteMovie = arguments.getBoolean(DetailActivityFragment.FAVORITE, false);
            mTwoPane = arguments.getBoolean(DetailActivityFragment.TWO_PANE, false);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mFavIcon = (ImageView) rootView.findViewById(R.id.fav_icon);

        mFavIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FavoriteMovieTask favTask = new FavoriteMovieTask(getActivity(), mFavIcon, mVideos, mReviews);
                String action = mFavoriteMovie ? FavoriteMovieTask.ACTION_REMOVE : FavoriteMovieTask.ACTION_ADD;
                favTask.execute(mTmdbMovieId, action);
            }
        });

        mPoster = (ImageView) rootView.findViewById(R.id.movie_poster);
        mTitle = (TextView) rootView.findViewById(R.id.movie_title);
        mReleaseDate = (TextView) rootView.findViewById(R.id.movie_release_date);
        mVoteAverage = (TextView) rootView.findViewById(R.id.movie_vote_average);
        mVoteCount = (TextView) rootView.findViewById(R.id.movie_vote_count);
        mPopularity = (TextView) rootView.findViewById(R.id.movie_popularity);
        mSynopsis = (ExpandableTextView) rootView.findViewById(R.id.movie_synopsis);
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mIndicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
        mImgNameTxt = (TextView) rootView.findViewById(R.id.img_name);

        mEmptyReviewView = (TextView) rootView.findViewById(R.id.recyclerview_review_empty);
        mRecyclerViewReview = (RecyclerView) rootView.findViewById(R.id.reviews_recycler_view);
        mRecyclerViewReview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerViewReview.setHasFixedSize(false);

        mReviewAdapter = new ReviewAdapter(mReviews, mEmptyReviewView, R.layout.list_item_review_short);
        mRecyclerViewReview.setAdapter(mReviewAdapter);


        mIndicator.setOnPageChangeListener(new PageChangeListener());
        mTrailerAdapter = new TrailerSlideAdapter(
                getActivity(), DetailActivityFragment.this);
        mViewPager.setAdapter(mTrailerAdapter);

        mIndicator.setViewPager(mViewPager);

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
                        if (handler != null && !mStopSliding) {
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


    void onSelectionChanged() {
        Uri uri = mUri;
        if (null != uri) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    private void finishCreatingMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() instanceof DetailActivity) {
            inflater.inflate(R.menu.menu_detail, menu);
            finishCreatingMenu(menu);
        }
    }


    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareTrailer + MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putParcelableArrayList(REVIEWS_KEY, mReviews);
        outState.putParcelableArrayList(VIDEO_KEY, mVideos);

        super.onSaveInstanceState(outState);
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
        Boolean mt = false;
        if (data != null && data.moveToFirst()) {
            ViewParent vp = getView().getParent();
            if (vp instanceof CardView) {
                ((View) vp).setVisibility(View.VISIBLE);
                mt = true;
            }
            mTmdbMovieId = data.getString(COLUMN_MOVIE_ID);


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

            if (mVideos.isEmpty()) {
                VideoTask videoTask = new VideoTask(this);
                videoTask.execute(movie_id, mFavoriteMovie ? "true" : "false");
            }

            if (mReviews.isEmpty()) {
                ReviewTask reviewTask = new ReviewTask(this);
                reviewTask.execute(movie_id, mFavoriteMovie ? "true" : "false");
            }

            AppCompatActivity activity = (AppCompatActivity) getActivity();
            Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

            if (!mTwoPane) {
                if (null != toolbarView) {
                    activity.setSupportActionBar(toolbarView);

                    activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                    activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            } else {
                if (null != toolbarView) {
                    Menu menu = toolbarView.getMenu();
                    if (null != menu) menu.clear();
                    toolbarView.inflateMenu(R.menu.menu_detail);
                    finishCreatingMenu(toolbarView.getMenu());
                }
            }


        }

    }

    public void updateVideos(ArrayList<Video> videos) {

        mVideos = videos;
        if (mVideos != null && mVideos.size() != 0) {
            mTrailerAdapter.swapData(mVideos);
            mImgNameTxt.setText(""
                    + mVideos.get(mViewPager
                    .getCurrentItem()).getName());
            runnable(mVideos.size());
            handler.postDelayed(animateViewPager,
                    ANIM_VIEWPAGER_DELAY);

            mShareTrailer = Utility.generateYoutubeVideoUrl(mVideos.get(0).getKey());

            Log.d(LOG_TAG,"SHARE "+mShareTrailer);
        } else {
            mImgNameTxt.setText("No trailer");
        }
    }

    private void refreshMenu() {
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);
        if (null != toolbarView) {
            Menu menu = toolbarView.getMenu();
            if (null != menu) menu.clear();
            toolbarView.inflateMenu(R.menu.menu_detail);
            finishCreatingMenu(toolbarView.getMenu());
        }
    }


    public void updateReviews(ArrayList<Review> reviews) {
        mReviews = reviews;
        if (mReviews != null && mReviews.size() > 0) {
            mReviewAdapter.swapData(mReviews);
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
                            + mVideos.get(mViewPager
                            .getCurrentItem()).getName());
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

