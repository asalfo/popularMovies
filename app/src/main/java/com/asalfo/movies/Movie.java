package com.asalfo.movies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by asalfo on 12/01/16.
 */
public class Movie implements Parcelable{
    String id;
    String mTitle;
    String mThumbnailUrl;
    String mSynopsis;
    Float mRating;
    String mReleaseDate;

    public Movie(String id,String mTitle, String mThumbnailUrl, String mSynopsis, Float mRating, String mReleaseDate) {
        this.id = id;
        this.mTitle = mTitle;
        this.mThumbnailUrl = mThumbnailUrl;
        this.mSynopsis = mSynopsis;
        this.mRating = mRating;
        this.mReleaseDate = mReleaseDate;
    }

    private Movie(Parcel in){
        this.id = in.readString();
        this.mTitle = in.readString();
        this.mThumbnailUrl = in.readString();
        this.mSynopsis = in.readString();
        this.mRating = in.readFloat();
        this.mReleaseDate = in.readString();
    }
    @Override
    public String toString() {
        return "Movie{" +
                "mTitle='" + mTitle + '\'' +
                ", mThumbnailUrl='" + mThumbnailUrl + '\'' +
                ", mSynopsis='" + mSynopsis + '\'' +
                ", mRating=" + mRating +
                ", mReleaseDate='" + mReleaseDate + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(mTitle);
        dest.writeString(mThumbnailUrl);
        dest.writeString(mSynopsis);
        dest.writeFloat(mRating);
        dest.writeString(mReleaseDate);
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
