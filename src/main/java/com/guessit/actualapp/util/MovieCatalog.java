package com.guessit.actualapp.util;

import com.guessit.actualapp.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieCatalog {
    private static final List<Movie> MOVIES = new ArrayList<>();

    static {
        MOVIES.add(new Movie(1, "The Lord of the Rings: The Fellowship of the Ring",
            "/assets/clips/LordOfTheRings/scene1.mp4", "Fantasy", 2001));
        MOVIES.add(new Movie(2, "The Lord of the Rings: The Two Towers",
            "/assets/clips/LordOfTheRings/scene2.mp4", "Fantasy", 2002));
        MOVIES.add(new Movie(3, "The Lord of the Rings: The Return of the King",
            "/assets/clips/LordOfTheRings/scene3.mp4", "Fantasy", 2003));
    }

    public static List<Movie> getAllMovies() {
        return new ArrayList<>(MOVIES);
    }

    public static Movie getRandomMovie() {
        int idx = (int) (Math.random() * MOVIES.size());
        return MOVIES.get(idx);
    }
}
