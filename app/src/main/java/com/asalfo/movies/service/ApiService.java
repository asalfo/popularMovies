package com.asalfo.movies.service;

import com.asalfo.movies.model.Movie;
import com.asalfo.movies.model.Review;
import com.asalfo.movies.model.TmdbCollection;
import com.asalfo.movies.model.Video;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by asalfo on 01/02/16.
 */
public interface ApiService {
    @GET("discover/movie")
    Call<TmdbCollection<Movie>> getDiscoverMovies(@Query("vote_count.gte") int vote_count,@Query("sort_by") String sort,@Query("page") int page, @Query("api_key") String api_key);

    @GET("movie/{id}")
    Call<Movie> getMovie(@Path("id") String id,@Query("api_key") String api_key);

    @GET("movie/{id}/videos")
    Call<TmdbCollection<Video>> getVideos(@Path("id") String id,@Query("api_key") String api_key);

    @GET("movie/{id}/reviews")
    Call<TmdbCollection<Review>> getReviews(@Path("id") String id,@Query("api_key") String api_key);
}
