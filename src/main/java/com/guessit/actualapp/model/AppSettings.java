package com.guessit.actualapp.model;

public final class AppSettings {

    private static volatile double videoVolume = 0.7;

    private AppSettings() {
    }

    public static double getVideoVolume() {
        return videoVolume;
    }

    public static void setVideoVolume(double value) {
        videoVolume = Math.max(0, Math.min(1, value));
    }
}
