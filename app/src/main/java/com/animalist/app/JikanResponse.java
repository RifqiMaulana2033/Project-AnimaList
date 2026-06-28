package com.animalist.app;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class JikanResponse {
    @SerializedName("data")
    public List<JikanAnime> data;

    public static class JikanAnime {
        @SerializedName("title")
        public String title;

        @SerializedName("images")
        public JikanImages images;

        @SerializedName("score")
        public Double score;

        @SerializedName("synopsis")
        public String synopsis;

        @SerializedName("episodes")
        public Integer episodes;

        @SerializedName("year")
        public Integer year;

        @SerializedName("status")
        public String status;

        @SerializedName("genres")
        public List<JikanGenre> genres;

        @SerializedName("trailer")
        public JikanTrailer trailer;
    }

    public static class JikanImages {
        @SerializedName("jpg")
        public JikanJpg jpg;
    }

    public static class JikanJpg {
        @SerializedName("image_url")
        public String imageUrl;
    }

    public static class JikanGenre {
        @SerializedName("name")
        public String name;
    }

    public static class JikanTrailer {
        @SerializedName("url")
        public String url; // Ini link resmi video YouTube-nya!
    }
}