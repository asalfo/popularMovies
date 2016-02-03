package com.asalfo.model;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by asalfo on 01/02/16.
 */
public class MovieCollection {

    private int page;
    private ArrayList<Movie> results;
    private BigInteger totalResults;
    private int totalPages;

    public MovieCollection(int page, ArrayList<Movie> results, BigInteger totalResults, int totalPages) {
        this.page = page;
        this.results = results;
        this.totalResults = totalResults;
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public ArrayList<Movie> getResults() {
        return results;
    }

    public void setResults(ArrayList<Movie> results) {
        this.results = results;
    }

    public BigInteger getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(BigInteger totalResults) {
        this.totalResults = totalResults;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
