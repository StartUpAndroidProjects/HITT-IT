package com.wolffincdevelopment.hiit_it;

/**
 * Created by Kyle Wolff on 1/29/17.
 */

public class TrackPlayEvent {

    public boolean isPlaying;
    public String id;

    public TrackPlayEvent(boolean isPlaying, String id) {
        this.isPlaying = isPlaying;
        this.id = id;
    }
}
