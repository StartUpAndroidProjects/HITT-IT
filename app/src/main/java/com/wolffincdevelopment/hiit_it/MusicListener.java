package com.wolffincdevelopment.hiit_it;

/**
 * Created by Kyle Wolff on 11/2/16.
 */

public class MusicListener {

    public boolean paused;
    public TrackData trackData;

    public MusicListener(boolean paused, TrackData trackData) {
        this.paused = paused;
        this.trackData = trackData;
    }
}
