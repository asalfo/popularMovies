package com.asalfo.movies.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.asalfo.movies.model.Review;
import com.asalfo.movies.model.Video;
import com.asalfo.movies.DetailActivityFragment;
import com.asalfo.movies.R;
import com.asalfo.movies.Utility;
import com.asalfo.movies.ui.IconFontTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TrailerSlideAdapter extends PagerAdapter {

	FragmentActivity activity;
	List<Video> mVideos;
	DetailActivityFragment homeFragment;

	public TrailerSlideAdapter(FragmentActivity activity, DetailActivityFragment homeFragment) {
		this.activity = activity;
		this.homeFragment = homeFragment;
	}

	@Override
	public int getCount() {
		if(null != mVideos) {
			return mVideos.size();
		}
		return 0;
	}

	@Override
	public View instantiateItem(ViewGroup container, final int position) {
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.vp_image, container, false);

		ImageView mImageView = (ImageView) view
				.findViewById(R.id.image_display);


		final Video video = mVideos.get(position);
		IconFontTextView play = (IconFontTextView) view.findViewById(R.id.play_button);
		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + video.getKey()));
				 activity.startActivity(intent);
			}
		});

		if(!Utility.isNetworkAvailable(activity)){
			play.setVisibility(View.GONE);
		}

		String trailerUrl = Utility.generateYoutubeVideoThumbnailUrl(video.getKey());

		Picasso.with(activity).load(trailerUrl).into(mImageView);

		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	public void swapData( ArrayList<Video> data){
		mVideos = data;
		notifyDataSetChanged();
	}

}