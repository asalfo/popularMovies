package com.asalfo.movies.model;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by asalfo on 01/02/16.
 */
public class TmdbCollection <E> {

    private int page;
    private ArrayList<E> results;
    private BigInteger totalResults;
    private int totalPages;

    public TmdbCollection(int page, ArrayList<E> results, BigInteger totalResults, int totalPages) {
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

    public ArrayList<E> getResults() {
        return results;
    }

    public void setResults(ArrayList<E> results) {
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
