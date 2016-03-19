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
    private ArrayList<Review> mReviews ;


    public ReviewsActivityFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(DetailActivityFragment.REVIEWS_KEY)) {
            mReviews = intent.getParcelableArrayListExtra(DetailActivityFragment.REVIEWS_KEY);


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

        mReviewAdapter = new ReviewAdapter(mReviews,emptyView,R.layout.list_item_review);
        mRecyclerViewReview.setAdapter(mReviewAdapter);

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mReviews != null && mReviews.size() != 0) {
            mReviewAdapter.swapData(mReviews);

        }
    }


}
