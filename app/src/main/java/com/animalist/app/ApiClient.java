package com.animalist.app;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Alamat utama server Jikan API v4
    private static final String BASE_URL = "https://api.jikan.moe/v4/";
    private static Retrofit retrofit = null;

    public static ApiService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}