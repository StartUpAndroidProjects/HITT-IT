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
    public void pause() throws IllegalStateException {
        super.pause();
        mediaControllerView.updatePlayButton(true);
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        super.seekTo(msec);
        mediaControllerView.updatePlayButton(false);
    }

    public void setMediaControllerView(MediaControllerView mediaControllerView) {
        this.mediaControllerView = mediaControllerView;
    }
}
