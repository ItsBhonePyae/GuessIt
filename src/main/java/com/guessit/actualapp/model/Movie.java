package com.guessit.actualapp.model;

public class Movie {
    private final int contentId;
    private final String title;
    private final String clipPath;
    private final String genre;
    private final int year;

    public Movie(int contentId, String title, String clipPath, String genre, int year) {
        this.contentId = contentId;
        this.title = title;
        this.clipPath = clipPath;
        this.genre = genre;
        this.year = year;
    }

    public int getContentId() {
        return contentId;
    }

    public String getTitle() {
        return title;
    }

    public String getClipPath() {
        return clipPath;
    }

    public String getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }
}
