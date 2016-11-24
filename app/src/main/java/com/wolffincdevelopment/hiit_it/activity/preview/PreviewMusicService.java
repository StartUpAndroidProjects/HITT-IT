package com.wolffincdevelopment.hiit_it.activity.preview;

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
 * Created by Kyle Wolff on 11/23/16.
 */

public class PreviewMusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {

    private MediaPlayer mediaPlayer;
    private TrackItem trackToPlay;
    private boolean paused;

    private int startTime, stopTime;

    private final IBinder musicBind = new PreviewMusicService.MusicBinder();

    public class MusicBinder extends Binder {
        PreviewMusicService getService() {
            return PreviewMusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initMusicPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaPlayer.release();
    }

    private void initMusicPlayer() {

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public void playSong() {

        mediaPlayer.reset();

        this.startTime = trackToPlay.getStartTime2();
        this.stopTime = trackToPlay.getStopTime3();

        Uri trackUri = Uri.parse(trackToPlay.getStream());

        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e("MUSIC PREPARED", "Error ", e);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public boolean isPaused() {
        return paused;
    }

    public void pausePlayer() {
        mediaPlayer.pause();
        paused = true;
    }

    public void resume() {
        mediaPlayer.start();
        paused = false;
    }

    public void stopPlayer() {

        if (isPlaying()) {
            mediaPlayer.stop();
        }

        paused = false;
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
        mediaPlayer.start();

        if(startTime > 0) {
            mediaPlayer.seekTo(startTime);
        }
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
        stopPlayer();
        mediaPlayer.release();
        return false;
    }
}
