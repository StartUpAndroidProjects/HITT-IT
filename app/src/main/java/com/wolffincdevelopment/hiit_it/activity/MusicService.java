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
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.wolffincdevelopment.hiit_it.Constant;
import com.wolffincdevelopment.hiit_it.HiitBus;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.wolffincdevelopment.hiit_it.TrackItem;
import com.wolffincdevelopment.hiit_it.manager.MusicIndexManager;
import com.wolffincdevelopment.hiit_it.util.SharedPreferencesUtil;
import com.wolffincdevelopment.hiit_it.widget.MusicPlayer;

/**
 * Created by kylewolff on 6/5/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {

    private final IBinder musicBind = new MusicBinder();

    private Intent playPauseIntent, prevIntent, nextIntent, notificationIntent;
    private PendingIntent playPausePenIntent, prevPenIntent, nextPenIntent, pendingNotificationIntent;
    private RemoteViews contentView;

    private NotificationManager notificationManager;
    private Notification notification;

    private TelephonyManager telephonyManager;

    private int startTime, stopTime;

    private boolean paused;
    private boolean stopThread;

    //media player
    private MusicPlayer player;
    private final int NOTIFICATION_ID = 1;

    //song list
    private ArrayList<TrackItem> songs;

    private TrackItem trackToPlay;

    private Executor playerWatcher;

    private Handler mainThread;

    private MusicIndexManager indexManager;

    private HiitBus bus;

    private Context context;

    private MusicServiceListener listener;

    public interface MusicServiceListener {

        void onNext();
        void onPrev();
    }

    public void onCreate() {
        //create the service
        super.onCreate();

        //create player
        player = new MusicPlayer();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        playerWatcher = Executors.newSingleThreadExecutor();

        indexManager = MusicIndexManager.getInstance();
        listener = indexManager;

        initMusicPlayer();
        initNotification();
        initHandler();

        bus = HiitBus.getInstance();
        bus.register(this);
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

        notificationIntent = new Intent(this, HomeActivity.class);

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
    }

    public void initHandler() {
        mainThread = new Handler(getApplicationContext().getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null && intent.getAction().compareTo(Constant.ACTION_PLAY_PAUSE) == 0) {
            contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());
        } else if (intent != null && intent.getAction() != null && intent.getAction().compareTo(Constant.ACTION_NEXT) == 0) {
            //nextSong();

            if (getCurrentSong() != null) {
                //playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
                contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());
            }

        } else if (intent != null && intent.getAction() != null && intent.getAction().compareTo(Constant.ACTION_PREVIOUS) == 0) {
            //prevSong();

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

    public void setList(ArrayList<TrackItem> songs, String action, TrackItem trackItem) {
        boolean currentSong = false;
        boolean previousSong = false;
        boolean nextSong = false;

        if (trackItem != null) {
            if (trackItem.getId().compareTo(getCurrentSong().getId()) == 0) {
                currentSong = true;
            } else if (trackItem.getOrderId() < getCurrentSong().getOrderId() && !(trackItem.getOrderId() < getCurrentSong().getOrderId() - 1)) {
                previousSong = true;
            } else if (trackItem.getOrderId() > getCurrentSong().getOrderId() && !(trackItem.getOrderId() > getCurrentSong().getOrderId() + 1)) {
                nextSong = true;
            }
        }

        this.songs = songs;
        indexManager.setTrackListLength(songs.size());

        if(action != null) {

            switch (action) {
                case "Move Up":

                    if (!songs.isEmpty()) {

                        if (indexManager.getIndex() != 0 && currentSong && indexManager.getIndex() != songs.size()) {

                            //indexManager.getPrevIndex();
                        } else if (nextSong && indexManager.getIndex() != songs.size()) {

                            //indexManager.getNextIndex();
                        }
                    }

                    break;

                case "Move Down":

                    if (!songs.isEmpty()) {

                        if (currentSong && indexManager.getIndex() != songs.size()) {

                            //indexManager.getNextIndex();

                            if (indexManager.getIndex() == songs.size()) {
                                //indexManager.getPrevIndex();
                            }

                        } else if (indexManager.getIndex() != 0 && previousSong && indexManager.getIndex() != songs.size()) {

                            //indexManager.getPrevIndex();
                        }
                    }

                    break;

                case "Delete":

                    if (!songs.isEmpty()) {

                        if (currentSong) {

                            if (indexManager.getIndex() == songs.size() && !songs.isEmpty()) {
                                //indexManager.getPrevIndex();
                            }

                        } else if (previousSong && indexManager.getIndex() != 0) {

                            //indexManager.getPrevIndex();
                        }
                    } else if (isPlaying() || isPaused()) {
                        stopPlayer();
                    }

                    break;
            }
        }
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    /**
     * Play song pass the Track for the song you want to play
     * Should only be used for the Base Adapter since we allow the user to TAP to play
     *
     * We need to set the index based on the Track
     * @param trackItem see {@link TrackData}
     */
    public void playSong(TrackItem trackItem) {

        for(TrackItem item : songs) {

            if(item.getId().equals(trackItem.getId())) {
                indexManager.setIndex(songs.indexOf(item));
                trackToPlay = item;
                playSong();
            }
        }
    }

    /**
     * Play song knows what song your on and should just work
     */
    public void playSong() {

        if (!songs.isEmpty()) {

            // If a song has not been initialized we must initialize
            if(trackToPlay == null) {
                //If trackToPlay is
                trackToPlay = songs.get(indexManager.getIndex());
            }

            if (songs.get(indexManager.getIndex()).getId().compareTo(trackToPlay.getId()) != 0) {

                for (TrackItem trackData : songs) {

                    if (trackData.getId().compareTo(trackToPlay.getId()) == 0) {
                        indexManager.setIndex(songs.indexOf(trackData));
                        trackToPlay = songs.get(indexManager.getIndex());
                    }
                }
            }

            this.startTime = trackToPlay.getStartTime2();
            this.stopTime = trackToPlay.getStopTime3();

            player.reset();

            // Thumser crashed the app by setting the songPosn == songs.size()
            if (indexManager.getIndex() == songs.size()) {
                indexManager.setIndex(0);
            }

            //set uri
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
                    // If this excpetion happens we will just play the song again
                    playSong();
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

    public boolean isPaused() {
        return paused;
    }

    public void pausePlayer() {
        player.pause();
        paused = true;
        //((HomeActivity) context).getAdapter().updateSoundIcon(trackToPlay.getId());
    }

    public void resume() {

        paused = false;
        player.seekTo(getPosn());
        player.start();
    }

    public void stopPlayer() {

        if (isPlaying()) {
            player.stop();
        }

        paused = false;
    }

    public void stop() {

        paused = false;

        player.stop();

        notificationManager.cancelAll();

        stopThread = true;

        //((HomeActivity) context).getAdapter().setSoundIconInvisible(trackToPlay.getId());
    }

    public void playPrev() {

        if(listener != null) {
            listener.onPrev();
        }
    }

    public void playNext() {

        if(listener != null) {
            listener.onNext();
        }
    }

    public TrackItem getCurrentSong() {

        if (songs != null && !songs.isEmpty()) {
            trackToPlay = songs.get(indexManager.getIndex());
        }

        return trackToPlay;
    }

    public void checkForNextSongDuringPlay() {

        Runnable mainRunnable = new Runnable() {

            @Override
            public void run() {

                if (indexManager.getPreviousIndex() != songs.size() - 1) {

                    if (!(indexManager.getIndex() > songs.size()) && !(indexManager.getIndex() < 0)) {

                        // Call onNext to tell the index manager that we need to increment
                        listener.onNext();

                        if (indexManager.getIndex() != 0) {
                            // Play song
                        }
                    }

                } else {

                    if (SharedPreferencesUtil.getInstance().getRepeat(getBaseContext())) {
                        //TrackData song = getNextSong();

                        //playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
                    } else {
                        resetSongs();
                    }
                }
            }
        };

        mainThread.post(mainRunnable);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {}

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        //((HomeActivity) context).getAdapter().updateSoundIcon(trackToPlay.getId());
        player.start();
        player.seekTo(startTime);
        stopThread = false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

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
                    }

                    final int total = getDur();

                    if (staticTime != 0 && staticTime == currentPosition) {
                        currentPosition = stopTime;
                    }

                    if (currentPosition >= stopTime) {
                        player.seekTo(total);

                        if (indexManager.getIndex() <= songs.size() - 1) {
                            checkForNextSongDuringPlay();
                        }
                    }

                    staticTime = currentPosition;
                }
            }
        });
    }

    public void setActivityContext(Context context) {
        this.context = context;
    }

    public void resetSongs() {
        stop();

        if (indexManager.getIndex() != 0 && !(indexManager.getIndex() < 0)) {
            if (!songs.isEmpty())
                indexManager.setIndex(0);
        }
    }

    public MusicPlayer getMusicPlayer() {
        return player;
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
