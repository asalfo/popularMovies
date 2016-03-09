package com.asalfo.movies;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asalfo.movies.adapter.TrailerSlideAdapter;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Movie;
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
public class DetailActivityFragment extends Fragment {
    public static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);
    private FavoriteMovieTask mFavoriteMovieTask;
    private static final long ANIM_VIEWPAGER_DELAY = 5000;
    private static final long ANIM_VIEWPAGER_DELAY_USER_VIEW = 10000;
    public static final String MOVIE_KEY = "movie";
    public static final String VIDEO_KEY = "videos";
    public static final String PAGE_NUM = "page";
    private Movie mMovie;
    private ImageView mPoster;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverage;
    private TextView mVoteCount;
    private TextView mPopularity;
    private TextView mSynopsis;
    private ImageView mFavIcon;
    private TrailerSlideAdapter mTrailerAdapter;
    private ArrayList<Video> mVideos = new ArrayList<Video>();


    private ViewPager mViewPager;
    private int mCurrentPage;
    PageIndicator mIndicator;
    TextView imgNameTxt;
    boolean stopSliding = false;

    private Runnable animateViewPager;
    private Handler handler;

    public DetailActivityFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_KEY)) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra("movie")) {
                mMovie = intent.getParcelableExtra("movie");
                retreiveVideos(mMovie.getId());
            }
        } else {
            Log.d(LOG_TAG, "savedInstanceState");
            mMovie = savedInstanceState.getParcelable(MOVIE_KEY);
            mVideos = savedInstanceState.getParcelableArrayList(VIDEO_KEY);
            mCurrentPage = savedInstanceState.getInt(PAGE_NUM);
        }

        this.setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("movie")) {

            mMovie = intent.getParcelableExtra("movie");

            RelativeLayout reviewView = (RelativeLayout) rootView.findViewById(R.id.reviews);
            reviewView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), ReviewsActivity.class)
                            .putExtra("movie_id", mMovie.getId());
                    startActivity(intent);
                }
            });


            mFavIcon = (ImageView) rootView.findViewById(R.id.fav_icon);

            if (isFavorite(mMovie)) {
                mFavIcon.setImageResource(R.drawable.ic_favorite);
            }
            mFavIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFavoriteMovieTask = new FavoriteMovieTask(getActivity(), mFavIcon);
                    String action = isFavorite(mMovie) ? FavoriteMovieTask.ACTION_REMOVE : FavoriteMovieTask.ACTION_ADD;
                    mFavoriteMovieTask.execute(mMovie.getId(),action);
                }
            });
            mPoster = (ImageView) rootView.findViewById(R.id.movie_poster);
            String posterUrl = Utility.generatePosterUrl(mMovie.getPosterPath(), "w185");
            Picasso.with(getActivity()).load(posterUrl).into(mPoster);
            Picasso.with(this.getContext()).load(posterUrl).into(mPoster);
            mTitle = (TextView) rootView.findViewById(R.id.movie_title);
            mTitle.setText(mMovie.getOriginalTitle());
            mReleaseDate = (TextView) rootView.findViewById(R.id.movie_release_date);
            mReleaseDate.setText("Released: " + mMovie.getReleaseDate());
            mVoteAverage = (TextView) rootView.findViewById(R.id.movie_vote_average);
            mVoteAverage.setText(mMovie.getVoteAverage() + "");
            mVoteCount = (TextView) rootView.findViewById(R.id.movie_vote_count);
            mVoteCount.setText(mMovie.getVoteCount() + "");
            mPopularity = (TextView) rootView.findViewById(R.id.movie_popularity);
            mPopularity.setText(String.format("%1$.2f", mMovie.getPopularity()));
            mSynopsis = (TextView) rootView.findViewById(R.id.movie_synopsis);
            mSynopsis.setText(mMovie.getOverview());


            mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
            mIndicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);
            imgNameTxt = (TextView) rootView.findViewById(R.id.img_name);


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
                                stopSliding = false;
                                runnable(mVideos.size());
                                handler.postDelayed(animateViewPager,
                                        ANIM_VIEWPAGER_DELAY_USER_VIEW);
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:
                            // calls when ViewPager touch
                            if (handler != null && stopSliding == false) {
                                stopSliding = true;
                                handler.removeCallbacks(animateViewPager);
                            }
                            break;
                    }
                    return false;
                }
            });
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mMovie != null) {
            outState.putParcelable(MOVIE_KEY, mMovie);
            outState.putParcelableArrayList(VIDEO_KEY, mVideos);
            outState.putInt(PAGE_NUM, mViewPager.getCurrentItem());
        }
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


    private void retreiveVideos(String video_id) {

        Call<TmdbCollection<Video>> call = apiService.getVideos(video_id, BuildConfig.THE_MOVIE_DB_API_KEY);
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
                        imgNameTxt.setText(""
                                + ((Video) mVideos.get(mViewPager
                                .getCurrentItem())).getName());
                        runnable(mVideos.size());
                        handler.postDelayed(animateViewPager,
                                ANIM_VIEWPAGER_DELAY);
                    } else {
                        imgNameTxt.setText("No trailer");
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


    public void runnable(final int size) {
        handler = new Handler();
        animateViewPager = new Runnable() {
            public void run() {
                if (!stopSliding) {
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

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                if (mVideos != null) {
                    imgNameTxt.setText(""
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
