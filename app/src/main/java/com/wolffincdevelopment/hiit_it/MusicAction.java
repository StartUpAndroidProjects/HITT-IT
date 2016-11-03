package com.wolffincdevelopment.hiit_it;

/**
 * Created by Kyle Wolff on 11/3/16.
 */

public class MusicAction {

    public enum Action{
        PLAY,
        PAUSE,
        RESUME
    }

    public Action action;
    public String id;

    public MusicAction(String id, Action action) {
        this.id = id;
        this.action = action;
    }
}
