package com.animalist.app;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("anime")
    Call<JikanResponse> searchAnime(
            @Query("q") String query,
            @Query("status") String status,
            @Query("genres") String genres,
            @Query("order_by") String orderBy,
            @Query("sort") String sort,
            @Query("limit") int limit,
            @Query("page") int page  // <-- KUNCI INFINITE SCROLL
    );
}