package com.wolffincdevelopment.hiit_it;

import com.wolffincdevelopment.hiit_it.viewmodel.TrackItem;

/**
 * Created by Kyle Wolff on 11/3/16.
 */

public class SoundIcon {

    public enum SoundIconActions {

        VISIBLE,
        PAUSE,
        RESUME
    }

    public TrackItem trackItem;
    public SoundIconActions iconActions;

    public SoundIcon(SoundIconActions iconActions, TrackItem trackItem) {
        this.trackItem = trackItem;
        this.iconActions = iconActions;
    }
}
