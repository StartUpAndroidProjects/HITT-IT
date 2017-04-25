package com.wolffincdevelopment.hiit_it.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wolffincdevelopment.hiit_it.activity.home.viewmodel.HomeListItem;
import com.wolffincdevelopment.hiit_it.manager.MusicIndexManager;
import com.wolffincdevelopment.hiit_it.manager.UserManager;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;
import com.wolffincdevelopment.hiit_it.widget.MediaControllerView;
import com.wolffincdevelopment.hiit_it.widget.MusicPlayer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class HomeMusicService extends Service implements MusicPlayer.OnCompletionListener, MusicPlayer.OnPreparedListener,
        MusicPlayer.OnErrorListener, MusicPlayer.OnSeekCompleteListener, AudioManager.OnAudioFocusChangeListener {

    private UserManager userManager;

    private MusicPlayer musicPlayer;
    private TrackData trackToPlay;
    private List<HomeListItem> trackDataList;

    private Executor playerWatcher;
    private Handler handler;
    private MusicIndexManager indexManager;

    private int startTime;
    private int stopTime;

    private boolean stopThread;
    private boolean stopped;
    private boolean paused;

    private int loopedCount;

    private MusicPlayerListener listener;

    private final IBinder musicBind = new MusicBinder();

    public interface MusicPlayerListener {
        void onSongPlaying(HomeListItem listItem);

        void onSongPaused(HomeListItem listItem);

        void onStopMusic(HomeListItem listItem);

        void onCountDown(HomeListItem listItem, String time);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        playerWatcher = Executors.newSingleThreadExecutor();
        handler = new Handler(getMainLooper());

        indexManager = MusicIndexManager.getInstance();

        loopedCount = 0;

        initMusicPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (musicPlayer != null) {
            //stopMedia();
            musicPlayer.release();
        }

        stopThread = true;
        playerWatcher = null;

        //removeAudioFocus();
    }

    public void initMusicPlayer() {
        musicPlayer = new MusicPlayer();
        musicPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        musicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        musicPlayer.setOnPreparedListener(this);
        musicPlayer.setOnErrorListener(this);
        musicPlayer.setOnSeekCompleteListener(this);
        musicPlayer.setOnCompletionListener(this);
    }

    public void setMusicPlayerListener(MusicPlayerListener listener) {
        this.listener = listener;
    }

    public void setAudioList(List<HomeListItem> trackDataList) {
        this.trackDataList = trackDataList;
        indexManager.setTrackListLength(trackDataList.size());
    }

    public void setMediaControllerView(MediaControllerView v) {
        musicPlayer.setMediaControllerView(v);
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Play song pass the Track for the song you want to play
     * Should only be used for the Base Adapter since we allow the user to TAP to play
     * <p>
     * We need to set the index based on the Track
     *
     * @param trackData see {@link TrackData}
     */
    public void playSong(TrackData trackData) {

        for (HomeListItem listItem : trackDataList) {

            if (listItem.getTrackData().getKey().equals(trackData.getKey())) {
                indexManager.setIndex(trackDataList.indexOf(listItem));
                trackToPlay = listItem.getTrackData();
                playSong();
            }
        }
    }

    public void playSong() {

        if (!trackDataList.isEmpty()) {

            // If a song has not been initialized we must initialize
            if (trackToPlay == null) {
                setTrackToPlayWithCurrentIndex();
            }

            this.startTime = trackToPlay.getStartTimeInMilliseconds();
            this.stopTime = trackToPlay.getStopTimeInMilliseconds();

            musicPlayer.reset();

            // Thumser crashed the app by setting the songPosn == songs.size()
            if (indexManager.getIndex() == trackDataList.size()) {
                indexManager.setIndex(0);
            }

            //set uri
            Uri trackUri = Uri.parse(trackToPlay.getStream());

            try {
                musicPlayer.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }

            try {

                try {
                    musicPlayer.prepare();
                } catch (IllegalStateException e) {
                    // If this excpetion happens we will just play the song again
                    playSong();
                }

            } catch (IOException e) {
                Log.e("Logged Issue: ", e.getMessage());
            }
        }
    }

    public int getPosn() {
        return musicPlayer.getCurrentPosition();
    }

    public TrackData getCurrentSong() {

        if (trackToPlay != null) {
            return trackToPlay;
        } else if (trackDataList != null) {
            trackToPlay = trackDataList.get(indexManager.getIndex()).getTrackData();
        }

        return null;
    }

    public long getDuration() {
        return trackToPlay.getStopTimeInMilliseconds();
    }

    public boolean isPlaying() {

        if (stopped) {
            return false;
        } else {
            return musicPlayer.isPlaying();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    public void pausePlayer() {

        stopThread = true;

        if (listener != null) {
            listener.onSongPaused(trackDataList.get(indexManager.getIndex()));
        }

        musicPlayer.pause();
        paused = true;
        stopped = false;
    }

    public void resume() {

        stopThread = false;

        if (listener != null) {
            listener.onSongPlaying(trackDataList.get(indexManager.getIndex()));
        }

        // We need to check if the song was edited
        if (trackToPlay.getMediaId() != trackDataList.get(indexManager.getIndex()).getTrackData().getMediaId()) {
            trackToPlay = trackDataList.get(indexManager.getIndex()).getTrackData();
            playSong();
        } else {
            musicPlayer.start();
        }

        paused = false;
        stopped = false;

        startProgressTimer();
    }

    public void stop() {

        paused = false;
        stopped = true;
        stopThread = true;

        if (listener != null) {
            listener.onStopMusic(trackDataList.get(indexManager.getIndex()));
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                musicPlayer.stop();
            }
        });

        loopedCount = 0;
    }

    public void playPrev() {

        indexManager.prev();
        setTrackToPlayWithCurrentIndex();
        playSong();
    }

    public void playNext() {

        indexManager.next();
        setTrackToPlayWithCurrentIndex();
        playSong();
    }

    public void setTrackToPlayWithCurrentIndex() {

        if (!trackDataList.isEmpty()) {
            trackToPlay = trackDataList.get(indexManager.getIndex()).getTrackData();
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

        if (listener != null) {
            listener.onSongPlaying(trackDataList.get(indexManager.getIndex()));
        }

        stopThread = false;
        stopped = false;

        musicPlayer.start();
        musicPlayer.seekTo(startTime);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        startProgressTimer();
    }

    private void startProgressTimer() {

        playerWatcher.execute(new Runnable() {

            @Override
            public void run() {

                int currentPosition;
                int staticTime = 0;

                while (!stopThread) {

                    try {
                        Thread.sleep(500);
                        currentPosition = getPosn();
                    } catch (Exception e) {
                        return;
                    }

                    getCountDown(getDuration(), currentPosition);

                    if (staticTime != 0 && staticTime == currentPosition) {
                        currentPosition = stopTime;
                    }

                    if (currentPosition >= stopTime) {

                        // Loop Check
                        if (indexManager.getIndex() == trackDataList.size() - 1) {
                            loopedCount++;
                        }

                        checkForNextSongDuringPlay();
                    }

                    if (!paused) {
                        staticTime = currentPosition;
                    }
                }
            }
        });
    }

    private void getCountDown(long duration, long millisecond) {

        final String time = ConvertTimeUtils.convertMilliSecToString(duration - millisecond);

        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onCountDown(trackDataList.get(indexManager.getIndex()), time);
            }
        });
    }

    public void checkForNextSongDuringPlay() {

        if (loopedCount < userManager.getCurrenTrackCount() || userManager.getCurrentTrackContinuous()) {

            if (!(indexManager.getIndex() > trackDataList.size()) && !(indexManager.getIndex() < 0)) {
                playNext();
            }

        } else {
            Log.v("Stop", "Stopping Song");
            stop();
        }
    }

    public void resetSongs() {

        if (indexManager.getIndex() != 0 && !(indexManager.getIndex() < 0)) {
            if (!trackDataList.isEmpty())
                indexManager.setIndex(0);
            setTrackToPlayWithCurrentIndex();
        }

        stop();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    public class MusicBinder extends Binder {
        public HomeMusicService getService() {
            return HomeMusicService.this;
        }
    }
}
