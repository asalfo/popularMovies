package com.asalfo.movies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.asalfo.movies.ItemChoiceManager;
import com.asalfo.movies.MainActivityFragment;
import com.asalfo.movies.data.MovieContract;
import com.asalfo.movies.model.Movie;
import com.asalfo.movies.R;
import com.asalfo.movies.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by asalfo on 12/01/16.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    public static final String LOG_TAG = FavoriteMovieAdapter.class.getSimpleName();
    private Cursor mCursor;
    final private Context mContext;
    final private MovieAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;
    final private ItemChoiceManager mICM;


    public MovieAdapter(Context context, MovieAdapterOnClickHandler dh, View emptyView, int choiceMode) {
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
        mICM = new ItemChoiceManager(this);
        mICM.setChoiceMode(choiceMode);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mPosterView;


        public MovieAdapterViewHolder(View view) {
            super(view);
            mPosterView = (ImageView) view.findViewById(R.id.grid_item_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int idColumnIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            mClickHandler.onClick(mCursor.getLong(idColumnIndex), this);
            mICM.onClick(this);
        }
    }

    public static interface MovieAdapterOnClickHandler {
        void onClick(Long date, MovieAdapterViewHolder vh);
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_movie, parent, false);
            view.setFocusable(true);
            return new MovieAdapterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int movieId = mCursor.getInt(MainActivityFragment.COLUMN_MOVIE_ID);
        String posterPath = mCursor.getString(MainActivityFragment.COLUMN_POSTER_PATH);
        String thumbnailUrl = Utility.generatePosterUrl(posterPath, "w92");

        Picasso.with(mContext).load(thumbnailUrl).fit().into(holder.mPosterView);
        mICM.onBindViewHolder(holder, position);
    }


    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
        mICM.onSaveInstanceState(outState);
    }


    public int getSelectedItemPosition() {
        return mICM.getSelectedItemPosition();
    }


    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof MovieAdapterViewHolder ) {
            MovieAdapterViewHolder mah = (MovieAdapterViewHolder)viewHolder;
            mah.onClick(mah.itemView);
        }
    }
}