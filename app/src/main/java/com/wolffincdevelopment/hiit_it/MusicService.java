package com.wolffincdevelopment.hiit_it;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.MediaController;

import java.util.ArrayList;

/**
 * Created by kylewolff on 6/5/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    private final IBinder musicBind = new MusicBinder();
    private BaseActivity baseActivity;
    private int startTime, stopTime;
    private boolean paused = false;

    //media player
    private MediaPlayer player;

    //song list
    private ArrayList<TrackData> songs;
    //current position
    private int songPosn;

    private TrackData playSong;

    public void onCreate() {

        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //create player
        player = new MediaPlayer();

        initMusicPlayer();

    }

    public void onDestroy() {

        player.release();
    }

    public void getBaseAcitivty(BaseActivity activity) {

        baseActivity = activity;
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnSeekCompleteListener(this);
    }

    public void setList(ArrayList<TrackData> songs){
        this.songs = songs;
    }

    public class MusicBinder extends Binder {

        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong(int startTime, int stopTime){

        if(!songs.isEmpty()) {

            this.startTime = startTime;
            this.stopTime = stopTime;

            //play a song
            player.reset();
            playSong = songs.get(songPosn);
            //get id
            long currSong = playSong.getMediaId();
            //set uri
            Uri trackUri = Uri.parse(playSong.getStream());

            try {
                player.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }

            player.prepareAsync();
        }
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }
    public boolean isPaused() {
        return paused;
    }


    public void pausePlayer(){
        player.pause();
        paused = true;
    }

    public void resume() {

        paused = false;
        player.seekTo(getPosn());
        player.start();
    }

    public void stop() {
        player.stop();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){

        player.start();
    }

    public void playPrev(int startTime, int stopTime){
        playSong(startTime, stopTime);
    }

    public void playNext(int startTime, int stopTime){
        playSong(startTime, stopTime);
    }

    public TrackData getCurrentSong() {

        if(!songs.isEmpty())
            playSong = songs.get(songPosn);

        return playSong;
    }

    public TrackData getPreviousSong() {

        if(!songs.isEmpty()) {

            songPosn--;

            if (songPosn == songs.size()) {
                songPosn = -1;
            } else if (songPosn > songs.size()) {
                songPosn = 0;
                playSong = songs.get(songPosn);
            } else if (songPosn <= 0) {
                songPosn = 0;
                playSong = songs.get(songPosn);
            } else {
                playSong = songs.get(songPosn);
            }
        }
        return playSong;
    }

    public TrackData getNextSong() {

        if(!songs.isEmpty()) {
            songPosn++;

            if (songPosn >= songs.size()) {
                songPosn = 0;
                playSong = songs.get(songPosn);
            } else {
                playSong = songs.get(songPosn);
            }
        }
        return playSong;
    }

    public void checkForNextSongDuringPlay() {

        if(!(songPosn >= songs.size()) && !(songPosn < 0)) {
            TrackData song = getNextSong();
            playNext(song.getStartTime2(), song.getStopTime3());
            baseActivity.updatePlayPauseButtons();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        player.seekTo(startTime);
        baseActivity.updatePlayPauseButtons();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int currentPosition = 0;
                while (player.isPlaying()) {
                    try {
                        Thread.sleep(1000);
                        currentPosition = getPosn();
                    } catch (InterruptedException e) {
                        return;
                    } catch (Exception e) {
                        return;
                    }
                    final int total = getDur();

                    if(currentPosition >= stopTime) {
                        player.stop();
                        baseActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkForNextSongDuringPlay();
                            }
                        });
                    }

                }
            }
        }).start();

    }

    public ArrayList<TrackData> getSongs() {
        return songs;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }
}