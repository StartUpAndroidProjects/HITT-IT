package com.wolffincdevelopment.hiit_it.widget;

import android.media.MediaPlayer;

/**
 * Created by Kyle Wolff on 11/3/16.
 */

public class MusicPlayer extends MediaPlayer {

    private MediaControllerView mediaControllerView;

    public MusicPlayer() {
        super();
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        mediaControllerView.updatePlayButton(false);
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        mediaControllerView.updatePlayButton(true);
    }

    public void setMediaControllerView(MediaControllerView mediaControllerView) {
        this.mediaControllerView = mediaControllerView;
    }
}
