package com.wolffincdevelopment.hiit_it.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.wolffincdevelopment.hiit_it.Constant;
import com.wolffincdevelopment.hiit_it.HiitBus;
import com.wolffincdevelopment.hiit_it.MusicListener;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.wolffincdevelopment.hiit_it.manager.MusicIndexManager;
import com.wolffincdevelopment.hiit_it.util.SharedPreferencesUtil;

/**
 * Created by kylewolff on 6/5/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {
    private final IBinder musicBind = new MusicBinder();

    private Intent playPauseIntent, prevIntent, nextIntent, notificationIntent;
    private PendingIntent playPausePenIntent, prevPenIntent, nextPenIntent, pendingNotificationIntent, pendingSwitchIntent;
    private RemoteViews contentView, bigView;

    private NotificationManager notificationManager;
    private Notification notification;

    private Bundle data, nonVisibleIconData, updateMediaControlsData;

    private TelephonyManager telephonyManager;

    private int startTime, stopTime;

    private boolean paused = false;
    private boolean stopThread = false;
    private boolean mPlayerReleased = true;

    //media player
    private MediaPlayer player;
    private final int NOTIFICATION_ID = 1;

    //song list
    private ArrayList<TrackData> songs;
    //current position
    private int songPosn, previousSongPosn;

    private TrackData playSong;

    private Executor playerWatcher;

    private MusicIndexManager indexManager;

    private HiitBus bus;

    public void onCreate() {
        //create the service
        super.onCreate();

        //create player
        player = new MediaPlayer();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        data = new Bundle();
        nonVisibleIconData = new Bundle();
        playerWatcher = Executors.newSingleThreadExecutor();

        indexManager = MusicIndexManager.getInstance();

        initMusicPlayer();
        initNotification();

        bus = new HiitBus();
    }

    public HiitBus getHiitBus() {
        return bus;
    }

    public void onDestroy() {
        super.onDestroy();

        player.release();

        if (notification != null) {
            notificationManager.cancelAll();
        }
    }

    private void initNotification() {

        playPauseIntent = new Intent(this, MusicService.class);
        playPauseIntent.setAction(Constant.ACTION_PLAY_PAUSE);

        prevIntent = new Intent(this, MusicService.class);
        prevIntent.setAction(Constant.ACTION_PREVIOUS);

        nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(Constant.ACTION_NEXT);

        playPausePenIntent = PendingIntent.getService(this, 99, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        prevPenIntent = PendingIntent.getService(this, 99, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        nextPenIntent = PendingIntent.getService(this, 99, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationIntent = new Intent(this, BaseActivity.class);

        contentView = new RemoteViews(getPackageName(), R.layout.mediaplayer_notiification);

        pendingNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        contentView.setOnClickPendingIntent(R.id.notPlayPause, playPausePenIntent);
        contentView.setOnClickPendingIntent(R.id.notPrev, prevPenIntent);
        contentView.setOnClickPendingIntent(R.id.notNext, nextPenIntent);

    }

    public void setNotification() {

        contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());

        if (isPlaying()) {
            contentView.setImageViewResource(R.id.notPlayPause, R.drawable.ic_pause_circle_outline_white_48dp);
        }

        notification = new Notification(R.mipmap.ic_launcher, null, System.currentTimeMillis());

        notification.contentView = contentView;
        notification.bigContentView = contentView;
        notification.contentIntent = pendingNotificationIntent;
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void initMusicPlayer() {
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null && intent.getAction().compareTo(Constant.ACTION_PLAY_PAUSE) == 0) {
            contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());
        } else if (intent != null && intent.getAction() != null && intent.getAction().compareTo(Constant.ACTION_NEXT) == 0) {
            nextSong();

            if (getCurrentSong() != null) {
                //playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
                contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());
            }

        } else if (intent != null && intent.getAction() != null && intent.getAction().compareTo(Constant.ACTION_PREVIOUS) == 0) {
            prevSong();

            if (getCurrentSong() != null) {
                //playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
                contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());
            }

        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void updateNotificationView() {
        if (isPlaying()) {
            contentView.setImageViewResource(R.id.notPlayPause, R.drawable.ic_pause_circle_outline_white_48dp);
        } else {
            contentView.setImageViewResource(R.id.notPlayPause, R.drawable.ic_play_circle_outline_white);
        }

        if (notification != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    public void setList(ArrayList<TrackData> songs, String action, TrackData trackData) {
        boolean currentSong = false;
        boolean previousSong = false;
        boolean nextSong = false;

        if (trackData != null) {
            if (trackData.getId().compareTo(getCurrentSong().getId()) == 0) {
                currentSong = true;
            } else if (trackData.getOrderId() < getCurrentSong().getOrderId() && !(trackData.getOrderId() < getCurrentSong().getOrderId() - 1)) {
                previousSong = true;
            } else if (trackData.getOrderId() > getCurrentSong().getOrderId() && !(trackData.getOrderId() > getCurrentSong().getOrderId() + 1)) {
                nextSong = true;
            }
        }

        this.songs = songs;
        indexManager.setTrackListLength(songs.size());

        switch (action) {
            case "Move Up":

                if (!songs.isEmpty()) {

                    if (indexManager.getIndex() != 0 && currentSong && indexManager.getIndex() != songs.size()) {

                        indexManager.getPrevIndex();
                        callOnStateChanged();

                    } else if (nextSong && indexManager.getIndex() != songs.size()) {

                        indexManager.getNextIndex();
                        callOnStateChanged();
                    }
                }

                previousSongPosn = indexManager.getIndex();

                break;

            case "Move Down":

                if (!songs.isEmpty()) {

                    if (currentSong && indexManager.getIndex() != songs.size()) {

                        indexManager.getNextIndex();

                        if (indexManager.getIndex() == songs.size()) {
                            indexManager.getPrevIndex();
                        }

                        callOnStateChanged();

                    } else if (songPosn != 0 && previousSong && songPosn != songs.size()) {

                        indexManager.getPrevIndex();

                        callOnStateChanged();
                    }
                }

                previousSongPosn = indexManager.getIndex();

                break;

            case "Delete":

                if (!songs.isEmpty()) {

                    if (currentSong) {

                        if (indexManager.getIndex() == songs.size() && !songs.isEmpty()) {
                            indexManager.getPrevIndex();
                        }

                        callOnStateChanged();

                    } else if (previousSong && songPosn != 0) {

                        indexManager.getPrevIndex();

                        callOnStateChanged();
                    }
                } else if (isPlaying() || isPaused()) {
                    stopPlayer();
                }

                previousSongPosn = songPosn;

                break;
        }
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong() {

        int startTime = getCurrentSong().getStartTime2();
        int stopTime = getCurrentSong().getStopTime3();
        String id = getCurrentSong().getId();

        if (!songs.isEmpty()) {
            if (songPosn != songs.size() && songs.get(songPosn).getId().compareTo(id) != 0) {
                for (TrackData trackData : songs) {
                    if (trackData.getId().compareTo(id) == 0) {
                        int index = songs.indexOf(trackData);
                        songPosn = index;
                    }
                }
            }

            previousSongPosn = songPosn;

            this.startTime = startTime;
            this.stopTime = stopTime;

            player.reset();

            // Thumser crashed the app by setting the songPosn == songs.size()
            if (songPosn == songs.size()) {
                songPosn = 0;
            } else {
                playSong = songs.get(songPosn);
            }

            //get id
            long currSong = playSong.getMediaId();
            //set uri
            Uri trackUri = Uri.parse(playSong.getStream());

            try {
                player.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }

            try {
                try {
                    player.prepare();
                } catch (IllegalStateException e) {
                    //playSong(startTime, stopTime, id);
                }
            } catch (IOException e) {
                Log.e("Logged Issue: ", e.getMessage());
            }

        }
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public boolean isReleased() {
        return mPlayerReleased;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pausePlayer() {
        player.pause();
        paused = true;
        callOnStateChanged();
    }

    public void resume() {
        paused = false;
        player.seekTo(getPosn());
        player.start();
        callOnStateChanged();
    }

    public void callOnStateChanged() {
        bus.post(new MusicListener(paused, getCurrentSong()));
    }

    public void stopPlayer() {
        if (isPlaying()) {
            player.stop();
        }

        paused = false;
    }

    public void stop(String id) {
        paused = false;

        nonVisibleIconData.clear();
        nonVisibleIconData.putSerializable("id", id);
        player.stop();

        data.clear();
        data.putString("NULL", "NULL");

        notificationManager.cancelAll();

        stopThread = true;
    }

    public void playPrev() {
        prevSong();
    }

    public void playNext() {
        nextSong();
    }

    public TrackData getCurrentSong() {

        if (songs != null && !songs.isEmpty()) {
            playSong = songs.get(indexManager.getIndex());
        }

        return playSong;
    }

    public void prevSong() {
        if (songs != null && !songs.isEmpty()) {
            playSong = songs.get(indexManager.getPrevIndex());
            playSong();
        }
    }

    public void nextSong() {
        if (songs != null && !songs.isEmpty()) {
            playSong = songs.get(indexManager.getNextIndex());
            playSong();
        }
    }

    public void checkForNextSongDuringPlay() {
        if (previousSongPosn != songs.size() - 1) {
            if (!(songPosn > songs.size()) && !(songPosn < 0)) {
                TrackData song = songs.get(indexManager.getNextIndex());

                if (songPosn != 0) {
                    //playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
                } else {
                    //playSong(song.getStartTime2(), song.getStopTime3(), song.getId());
                }
            }
        } else {
            if (SharedPreferencesUtil.getInstance().getRepeat(this)) {
                //TrackData song = getNextSong();

                //playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
            } else {
                resetSongs();
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
        stopThread = false;

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

        callOnStateChanged();

        playerWatcher.execute(new Runnable() {
            @Override
            public void run() {
                int currentPosition = 0;
                int staticTime = 0;

                while (!stopThread) {
                    try {
                        Thread.sleep(1000);
                        currentPosition = getPosn();
                    } catch (InterruptedException e) {
                        return;
                    } catch (Exception e) {
                        return;
                    }

                    final int total = getDur();

                    if (staticTime != 0 && staticTime == currentPosition) {
                        currentPosition = stopTime;
                    }

                    if (currentPosition >= stopTime) {
                        player.seekTo(total);

                        if (songPosn <= songs.size() - 1) {
                            checkForNextSongDuringPlay();
                        }
                    }

                    staticTime = currentPosition;
                }
            }

        });
    }

    public void resetSongs() {
        stop(playSong.getId());

        if (songPosn != 0 && !(songPosn < 0)) {
            if (!songs.isEmpty())
                songPosn = 0;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopPlayer();
        player.release();
        return false;
    }
}
