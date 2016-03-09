package com.asalfo.movies;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asalfo.movies.adapter.ReviewAdapter;
import com.asalfo.movies.model.Review;
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
public class ReviewsActivityFragment extends Fragment {
    public static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    public static final ApiService apiService = ServiceGenerator.createService(ApiService.class);
    private RecyclerView mRecyclerViewReview;
    private ReviewAdapter mReviewAdapter;
    private String mMovieTitle;
    private String mMovieId;
    private ArrayList<Review> mReviews = new ArrayList<Review>();


    public ReviewsActivityFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("movie_id")) {
            mMovieId = intent.getStringExtra("movie_id");
            retreiveReview(mMovieId);

        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);
        TextView emptyView = (TextView) rootView.findViewById(R.id.recyclerview_review_empty);
        mRecyclerViewReview = (RecyclerView) rootView.findViewById(R.id.reviews_recycler_view);
        mRecyclerViewReview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerViewReview.setHasFixedSize(false);

        mReviewAdapter = new ReviewAdapter(mReviews,emptyView);
        mRecyclerViewReview.setAdapter(mReviewAdapter);

        return rootView;

    }



    private void retreiveReview(String video_id){

        Call<TmdbCollection<Review>> call = apiService.getReviews(video_id, BuildConfig.THE_MOVIE_DB_API_KEY);
        call.enqueue(new Callback<TmdbCollection<Review>>() {
            @Override
            public void onResponse(Response<TmdbCollection<Review>> response) {
                if (response.isSuccess()) {
                    TmdbCollection<Review> collection = response.body();
                    mReviews.addAll(collection.getResults());
                    if (mReviews != null && mReviews.size() != 0) {
                        mReviewAdapter.swapData(mReviews);

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
