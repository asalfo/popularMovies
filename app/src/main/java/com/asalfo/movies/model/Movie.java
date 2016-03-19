package com.asalfo.movies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by asalfo on 12/01/16.
 */
public class Movie implements Parcelable {
    private String id;
    private String title;
    private String originalTitle;
    private String originalLanguage;
    private String homepage;
    private String posterPath;
    private String backdropPath;
    private String overview;
    private String tagLine;
    private float popularity;
    private Float voteAverage;
    private int voteCount;
    private String releaseDate;
    private int runtine;
    private double budget;
    private double revenue;
    private Boolean favorite;


    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public Movie() {
    }

    public Movie(String id, String title, String originalTitle, String originalLanguage,
                 String posterPath, String backdropPath, String overview,
                 float popularity, Float voteAverage, String releaseDate,
                 int voteCount) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.originalLanguage = originalLanguage;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.voteCount = voteCount;
    }

    public Movie(String id, String title, String posterPath, String overview, String releaseDate, Float voteAverage) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
    }

    private Movie(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.originalTitle = in.readString();
        this.posterPath = in.readString();
        this.overview = in.readString();
        this.releaseDate = in.readString();
        this.voteAverage = in.readFloat();
        this.voteCount = in.readInt();
        this.popularity = in.readFloat();
        this.homepage = in.readString();
        this.backdropPath = in.readString();
        this.tagLine = in.readString();
        this.runtine = in.readInt();
        this.budget = in.readDouble();
        this.revenue = in.readDouble();
        this.favorite = in.readInt() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeFloat(voteAverage);
        dest.writeInt(voteCount);
        dest.writeFloat(popularity);
        dest.writeString(homepage);
        dest.writeString(backdropPath);
        dest.writeString(tagLine);
        dest.writeInt(runtine);
        dest.writeDouble(budget);
        dest.writeDouble(revenue);
        dest.writeInt(favorite ?1:0);

    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", overview='" + overview + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagTine) {
        this.tagLine = tagTine;
    }

    public float getPopularity() {
        return popularity;
    }

    public void setPopularity(float popularity) {
        this.popularity = popularity;
    }

    public Float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }


    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public int getRuntine() {
        return runtine;
    }

    public void setRuntine(int runtine) {
        this.runtine = runtine;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public Boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public String getYear(){
        String[] parts = this.releaseDate.split("-");
        return parts[0];
    }

}
