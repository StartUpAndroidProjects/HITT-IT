package com.wolffincdevelopment.hiit_it;

/**
 * Created by Kyle Wolff on 11/2/16.
 */

public class MusicListener {

    public boolean paused;
    public TrackItem trackItem;

    public MusicListener(boolean paused, TrackItem trackItem) {
        this.paused = paused;
        this.trackItem = trackItem;
    }
}
