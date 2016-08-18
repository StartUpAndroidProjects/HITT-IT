package com.wolffincdevelopment.hiit_it;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.MediaController;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;

import util.SharedPreferencesUtil;

/**
 * Created by kylewolff on 6/5/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {

    private final IBinder musicBind = new MusicBinder();

    private Message updateControlsMsg, sendSoundIconVisible, sendSoundIconNonVisible, setCurrentSong;
    private MessageHandler handler;
    private What whatInteger;
    private Bundle data, nonVisibleIconData, updateMediaControlsData;

    private int startTime, stopTime;

    private boolean paused = false;
    private boolean mPlayerReleased = true;

    //media player
    private MediaPlayer player;
    private NotificationManager notificationManager;
    private Notification notification = null;
    private final int NOTIFICATION_ID = 1;
    private MediaSessionManager mediaSessionManager;
    private MediaSession mediaSession;
    private MediaController mediaController;

    //song list
    private ArrayList<TrackData> songs;
    //current position
    private int songPosn;

    private TrackData playSong;

    public void onCreate()
    {
        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //create player
        player = new MediaPlayer();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        whatInteger = new What();
        data = new Bundle();
        nonVisibleIconData = new Bundle();

        initMusicPlayer();
    }

    public void onDestroy() {
        super.onDestroy();
        player.release();
        stopForeground(true);
        stopForeground(true);
    }

    public void initMusicPlayer(){

        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnCompletionListener(this);

        updateMediaControlsData = new Bundle();

    }


    public void setList(ArrayList<TrackData> songs, String action, TrackData trackData){

        boolean currentSong = false;
        boolean previousSong = false;
        boolean nextSong = false;

        if(trackData != null) {

            if(trackData.getId().compareTo(getCurrentSong().getId()) == 0){
                currentSong = true;
            }else if (trackData.getOrderId() < getCurrentSong().getOrderId() && !(trackData.getOrderId() < getCurrentSong().getOrderId() - 1)) {
                previousSong = true;
            } else if (trackData.getOrderId() > getCurrentSong().getOrderId() && !(trackData.getOrderId() > getCurrentSong().getOrderId() + 1)) {
                nextSong = true;
            }
        }

        this.songs = songs;

        switch (action) {

            case "Move Up":

                if(!songs.isEmpty()) {

                    if(songPosn != 0 && currentSong && songPosn != songs.size()) {

                        songPosn--;

                        if (playSong != null && isPlaying()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        } else if (playSong != null && isPaused()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        }

                    } else if(nextSong && songPosn != songs.size()) {

                        songPosn++;

                        if (playSong != null && isPlaying()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        } else if (playSong != null && isPaused()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        }
                    }
                }

                break;

            case "Move Down":

                if(!songs.isEmpty()) {

                    if(currentSong && songPosn != songs.size()) {

                        songPosn++;

                        if(songPosn == songs.size()) {
                            songPosn--;
                        }

                        if (playSong != null && isPlaying()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        } else if (playSong != null && isPaused()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        }

                    }else if(songPosn != 0 && previousSong && songPosn != songs.size() ) {

                        songPosn--;

                        if (playSong != null && isPlaying()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        } else if (playSong != null && isPaused()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        }
                    }
                }

                break;

            case "Delete":

                if(!songs.isEmpty()) {

                    if(currentSong) {

                        if (playSong != null && isPlaying()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                            playSong(getCurrentSong().getStartTime2(), getCurrentSong().getStopTime3(), getCurrentSong().getId());

                        } else if (playSong != null && isPaused()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        }

                    }else if(previousSong && songPosn != 0) {

                        songPosn--;

                        if (playSong != null && isPlaying()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        } else if (playSong != null && isPaused()) {

                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        }

                    }

                }else if(songs.isEmpty() && isPlaying() || isPaused()) {

                    stopPlayer();

                    updateControlsMsg = handler.createMessage(updateControlsMsg, whatInteger.getUpdatePlayControls());
                    handler.sendMessage(updateControlsMsg);
                }

                break;
        }

        currentSong = false;
        previousSong = false;
        nextSong= false;
    }

    public class MusicBinder extends Binder {

        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong(int startTime, int stopTime, String id) {

        if(!songs.isEmpty()) {

            if(songs.get(songPosn).getId().compareTo(id) != 0) {

                for(TrackData trackData : songs) {

                    if(trackData.getId().compareTo(id) == 0) {

                        int index = songs.indexOf(trackData);
                        songPosn = index;
                    }
                }

            }

            this.startTime = startTime;
            this.stopTime = stopTime;

           player.reset();

            playSong = songs.get(songPosn);
            //get id
            long currSong = playSong.getMediaId();
            //set uri
            Uri trackUri = Uri.parse(playSong.getStream());

            try {
                player.setDataSource(getApplicationContext(), trackUri);
                data.clear();
                data.putSerializable("id", playSong.getId());
                data.putSerializable("boolean", true);
                sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                handler.sendMessage(sendSoundIconVisible);


                setCurrentSong = handler.createMessage(setCurrentSong, whatInteger.getCurrentSong());
                handler.sendMessage(setCurrentSong);

            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }

            try {

                try {
                    player.prepare();
                }catch (IllegalStateException e) {
                    playSong(startTime,stopTime,id);
                }
            }catch (IOException e) {
                Log.e("Logged Issue: ", e.getMessage());
            }

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

    public boolean isReleased()
    {
        return mPlayerReleased;
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

    public void stopPlayer()
    {
        player.stop();
    }

    public void stop(String id) {
        nonVisibleIconData.clear();
        nonVisibleIconData.putSerializable("id", id);
        player.stop();
        sendSoundIconNonVisible = handler.createMessage(sendSoundIconNonVisible,whatInteger.getSetSoundIconNonVisible(), nonVisibleIconData);
        handler.sendMessage(sendSoundIconNonVisible);
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){

        player.start();
    }

    public void playPrev(int startTime, int stopTime, String id){
        playSong(startTime, stopTime, id);
    }

    public void playNext(int startTime, int stopTime, String id){
         playSong(startTime, stopTime, id);
    }

    public TrackData getCurrentSong() {

        if(!songs.isEmpty() && songPosn != songs.size())
            playSong = songs.get(songPosn);
        else {
            playSong = songs.get(0);
        }

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

        if(!(songPosn > songs.size()) && !(songPosn < 0)) {

            TrackData song = getNextSong();

            if(songPosn != 0) {
                playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
            } else {
                playSong(song.getStartTime2(), song.getStopTime3(), song.getId());
            }

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

        player.start();
        player.seekTo(startTime);

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

        updateControlsMsg = handler.createMessage(updateControlsMsg, whatInteger.getUpdatePlayControls());
        handler.sendMessage(updateControlsMsg);

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

                        if (currentPosition >= stopTime) {
                            player.seekTo(total);

                            if(songPosn <= songs.size() - 1 && SharedPreferencesUtil.getInstance().getRepeat(getBaseContext())) {
                                checkForNextSongDuringPlay();
                            } else if(!SharedPreferencesUtil.getInstance().getRepeat(getBaseContext())) {

                                if(songPosn == songs.size() - 1 ) {
                                    stop(playSong.getId());
                                } else {
                                    checkForNextSongDuringPlay();
                                }

                            } else {
                                stop(playSong.getId());
                            }
                        }
                    }
                }

        }).start();

    }

    public void setHandler(MessageHandler handler)
    {
        this.handler = handler;
    }

    public void completeAudio()
    {
        player.release();
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

    public void updateNotification(String text)
    {

    }
}
