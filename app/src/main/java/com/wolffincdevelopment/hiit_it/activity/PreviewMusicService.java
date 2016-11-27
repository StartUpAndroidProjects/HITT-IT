package com.wolffincdevelopment.hiit_it.activity;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wolffincdevelopment.hiit_it.viewmodel.TrackItem;

import java.io.IOException;

/**
 * Created by Kyle Wolff on 11/26/16.
 */

public class PreviewMusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {

    private MediaPlayer player;
    private MusicService musicService;
    private TrackItem trackToPlay;

    private int startTime;
    private int stopTime;

    private boolean paused;

    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();

        initPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initPlayer() {

        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnCompletionListener(this);
    }

    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
    }

    public void playSong(TrackItem trackToPlay) {

        this.trackToPlay = trackToPlay;

        if (trackToPlay != null) {

            if(musicService != null) {
                musicService.stop();
            }

            this.startTime = trackToPlay.getStartTimeInMilliseconds();
            this.stopTime = trackToPlay.getStopTimeInMilliseconds();

            player.reset();

            Uri trackUri = Uri.parse(trackToPlay.getStream());

            try {
                player.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }

            try {

                try {
                    player.prepare();
                } catch (IllegalStateException e) {
                    playSong(trackToPlay);
                }

            } catch (IOException e) {
                Log.e("Logged Issue: ", e.getMessage());
            }

        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        player.pause();
        paused = true;
    }

    public void resume() {
        player.start();
        paused = false;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void stopPlayer() {
        player.stop();
        paused = false;
    }

    public void releasePlayer() {
        player.release();
    }

    /**
     *
     * @return position of song in milliseconds
     */
    public int getCurrentPosition() {

        try {
            return player.getCurrentPosition();
        }catch (IllegalStateException e) {
            return trackToPlay.getStopTimeInMilliseconds();
        }
    }

    public class MusicBinder extends Binder {
        PreviewMusicService getService() {
            return PreviewMusicService.this;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        player.start();
        player.seekTo(startTime);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
