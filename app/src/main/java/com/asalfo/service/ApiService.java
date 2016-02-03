package com.asalfo.service;

import com.asalfo.model.Movie;
import com.asalfo.model.MovieCollection;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by asalfo on 01/02/16.
 */
public interface ApiService {
 @GET("discover/movie")
 Call<MovieCollection> getMovies(@Query("sort_by") String sort,@Query("page") int page,@Query("api_key") String api_key);
}
