package com.animalist.app;

import java.util.List;

public class Anime {
    private String userStatus; // Buat nyimpen stempel "Watching", "Plan", dll
    private String title;
    private String thumbnailUrl;
    private double rating;
    private List<String> genres;
    private String synopsis;
    private String episodes;
    private String year;
    private String status;

    // Constructor buat masukin data
    public Anime(String title, String thumbnailUrl, double rating, List<String> genres, String synopsis, String episodes, String year, String status) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.rating = rating;
        this.genres = genres;
        this.synopsis = synopsis;
        this.episodes = episodes;
        this.year = year;
        this.status = status;
    }

    // Getters buat ngambil data
    public String getTitle() { return title; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public double getRating() { return rating; }
    public List<String> getGenres() { return genres; }
    public String getSynopsis() { return synopsis; }
    public String getEpisodes() { return episodes; }
    public String getYear() { return year; }
    public String getStatus() { return status; }
    public String getUserStatus() {return userStatus;}
    public void setUserStatus(String userStatus) {this.userStatus = userStatus;}
}