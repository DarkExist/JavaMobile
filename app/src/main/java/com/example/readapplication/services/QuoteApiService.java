package com.example.readapplication.services;

import com.example.readapplication.Contracts.QuoteResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QuoteApiService {
    @GET(".")
    Call<QuoteResponse> getRandomQuote(
        @Query("method") String method,
        @Query("format") String format,
        @Query("lang") String lang
    );
}