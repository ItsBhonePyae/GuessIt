package com.guessit.actualapp.model;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.List;

public final class GameHistory {

    private static final int MAX_ENTRIES = 30;
    private static final ArrayDeque<Entry> ENTRIES = new ArrayDeque<>();

    static {
        LocalDateTime now = LocalDateTime.now();
        ENTRIES.add(new Entry("Whiplash", 0, now.minusDays(1), true));
        ENTRIES.addFirst(new Entry("Dune: Part Two", 0, now.minusHours(2), true));
    }

    private GameHistory() {
    }

    public static synchronized void record(String movieTitle, int score) {
        ENTRIES.addFirst(new Entry(movieTitle, score, LocalDateTime.now(), false));
        while (ENTRIES.size() > MAX_ENTRIES) {
            ENTRIES.removeLast();
        }
    }

    public static synchronized List<Entry> recent() {
        return List.copyOf(ENTRIES);
    }

    public record Entry(String movieTitle, int score, LocalDateTime completedAt, boolean sample) {
    }
}
