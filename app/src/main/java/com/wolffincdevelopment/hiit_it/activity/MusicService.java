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
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.wolffincdevelopment.hiit_it.Constant;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackData;
import com.wolffincdevelopment.hiit_it.What;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.wolffincdevelopment.hiit_it.handler.MessageHandler;
import com.wolffincdevelopment.hiit_it.util.SharedPreferencesUtil;

/**
 * Created by kylewolff on 6/5/2016.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener
{
    private final IBinder musicBind = new MusicBinder();

    private Intent playPauseIntent, prevIntent, nextIntent, notificationIntent;
    private PendingIntent playPausePenIntent, prevPenIntent, nextPenIntent, pendingNotificationIntent, pendingSwitchIntent;
    private RemoteViews contentView, bigView;

    private NotificationManager notificationManager;
    private Notification notification;

    private Message updateControlsMsg, sendSoundIconVisible, sendSoundIconNonVisible, setCurrentSong, pauseResumeMessage;
    private MessageHandler handler;
    private What whatInteger;
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

    public void onCreate()
    {
        //create the service
        super.onCreate();
        //initialize position
        songPosn = 0;
        //create player
        player = new MediaPlayer();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        whatInteger = new What();
        data = new Bundle();
        nonVisibleIconData = new Bundle();
        playerWatcher = Executors.newSingleThreadExecutor();

        initMusicPlayer();
        initNotification();
    }

    public void onDestroy()
    {
        super.onDestroy();

        player.release();

        if (notification != null)
        {
            notificationManager.cancelAll();
        }
    }

    private void initNotification()
    {

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

    public void setNotification()
    {

        contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());

        if (isPlaying())
        {
            contentView.setImageViewResource(R.id.notPlayPause, R.drawable.ic_pause_circle_outline_white_48dp);
        }

        notification = new Notification(R.mipmap.ic_launcher, null, System.currentTimeMillis());

        notification.contentView = contentView;
        notification.bigContentView = contentView;
        notification.contentIntent = pendingNotificationIntent;
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void initMusicPlayer()
    {
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
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null && intent.getAction() != null && intent.getAction().compareTo(Constant.ACTION_PLAY_PAUSE) == 0)
        {
            contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());

            pauseResumeMessage = handler.createMessage(pauseResumeMessage, whatInteger.pauseResumeCurrentSong());
            handler.sendMessage(pauseResumeMessage);

        } else if (intent != null && intent.getAction() != null && intent.getAction().compareTo(Constant.ACTION_NEXT) == 0)
        {
            TrackData song = getNextSong();

            if (song != null)
            {
                playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
                contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());
            }

        } else if (intent != null && intent.getAction() != null && intent.getAction().compareTo(Constant.ACTION_PREVIOUS) == 0)
        {
            TrackData song = getPreviousSong();

            if (song != null)
            {
                playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
                contentView.setTextViewText(R.id.track_static, getCurrentSong().getSongAndArtist());
            }

        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void updateNotificationView()
    {
        if (isPlaying())
        {
            contentView.setImageViewResource(R.id.notPlayPause, R.drawable.ic_pause_circle_outline_white_48dp);
        } else
        {
            contentView.setImageViewResource(R.id.notPlayPause, R.drawable.ic_play_circle_outline_white);
        }

        if (notification != null)
        {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    public void setList(ArrayList<TrackData> songs, String action, TrackData trackData)
    {
        boolean currentSong = false;
        boolean previousSong = false;
        boolean nextSong = false;

        if (trackData != null)
        {
            if (trackData.getId().compareTo(getCurrentSong().getId()) == 0)
            {
                currentSong = true;
            } else if (trackData.getOrderId() < getCurrentSong().getOrderId() && !(trackData.getOrderId() < getCurrentSong().getOrderId() - 1))
            {
                previousSong = true;
            } else if (trackData.getOrderId() > getCurrentSong().getOrderId() && !(trackData.getOrderId() > getCurrentSong().getOrderId() + 1))
            {
                nextSong = true;
            }
        }

        this.songs = songs;

        switch (action)
        {
            case "Move Up":

                if (!songs.isEmpty())
                {
                    if (songPosn != 0 && currentSong && songPosn != songs.size())
                    {
                        songPosn--;

                        if (playSong != null && isPlaying())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        } else if (playSong != null && isPaused())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        }

                        previousSongPosn = songPosn;

                    } else if (nextSong && songPosn != songs.size())
                    {
                        songPosn++;

                        if (playSong != null && isPlaying())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        } else if (playSong != null && isPaused())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        }

                        previousSongPosn = songPosn;
                    }
                }

                break;

            case "Move Down":

                if (!songs.isEmpty())
                {
                    if (currentSong && songPosn != songs.size())
                    {
                        songPosn++;

                        if (songPosn == songs.size())
                        {
                            songPosn--;
                        }

                        if (playSong != null && isPlaying())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        } else if (playSong != null && isPaused())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        }

                        previousSongPosn = songPosn;

                    } else if (songPosn != 0 && previousSong && songPosn != songs.size())
                    {
                        songPosn--;

                        if (playSong != null && isPlaying())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        } else if (playSong != null && isPaused())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        }

                        previousSongPosn = songPosn;
                    }
                }

                break;

            case "Delete":

                if (!songs.isEmpty())
                {

                    if (currentSong)
                    {
                        if (songPosn == songs.size() && !songs.isEmpty())
                        {
                            songPosn--;
                            previousSongPosn = songPosn;
                        }

                        if (playSong != null && isPlaying())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                            playSong(getCurrentSong().getStartTime2(), getCurrentSong().getStopTime3(), getCurrentSong().getId());

                        } else if (playSong != null && isPaused())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);

                        }

                    } else if (previousSong && songPosn != 0)
                    {
                        songPosn--;

                        if (playSong != null && isPlaying())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", true);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        } else if (playSong != null && isPaused())
                        {
                            data.clear();
                            data.putSerializable("id", getCurrentSong().getId());
                            data.putSerializable("boolean", false);
                            sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                            handler.sendMessage(sendSoundIconVisible);
                        }

                        previousSongPosn = songPosn;
                    }
                } else if (isPlaying() || isPaused())
                {
                    stopPlayer();

                    updateControlsMsg = handler.createMessage(updateControlsMsg, whatInteger.getUpdatePlayControls());
                    handler.sendMessage(updateControlsMsg);
                }

                break;
        }

        currentSong = false;
        previousSong = false;
        nextSong = false;
    }

    public class MusicBinder extends Binder
    {
        MusicService getService()
        {
            return MusicService.this;
        }
    }

    public void playSong(int startTime, int stopTime, String id)
    {
        if (!songs.isEmpty())
        {
            if (songPosn != songs.size() && songs.get(songPosn).getId().compareTo(id) != 0)
            {
                for (TrackData trackData : songs)
                {
                    if (trackData.getId().compareTo(id) == 0)
                    {
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
            if (songPosn == songs.size())
            {
                songPosn = 0;
            } else
            {
                playSong = songs.get(songPosn);
            }

            //get id
            long currSong = playSong.getMediaId();
            //set uri
            Uri trackUri = Uri.parse(playSong.getStream());

            try
            {
                player.setDataSource(getApplicationContext(), trackUri);
                data.clear();
                data.putSerializable("id", playSong.getId());
                data.putSerializable("boolean", true);
                sendSoundIconVisible = handler.createMessage(sendSoundIconVisible, whatInteger.getSetSoundIconVisible(), data);
                handler.sendMessage(sendSoundIconVisible);


                setCurrentSong = handler.createMessage(setCurrentSong, whatInteger.getCurrentSong());
                handler.sendMessage(setCurrentSong);

            } catch (Exception e)
            {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }

            try
            {
                try
                {
                    player.prepare();
                } catch (IllegalStateException e)
                {
                    playSong(startTime, stopTime, id);
                }
            } catch (IOException e)
            {
                Log.e("Logged Issue: ", e.getMessage());
            }

        }
    }

    public int getPosn()
    {
        return player.getCurrentPosition();
    }

    public int getDur()
    {
        return player.getDuration();
    }

    public boolean isPlaying()
    {
        return player.isPlaying();
    }

    public boolean isReleased()
    {
        return mPlayerReleased;
    }

    public boolean isPaused()
    {
        return paused;
    }

    public void pausePlayer()
    {
        player.pause();
        paused = true;
    }

    public void resume()
    {
        paused = false;
        player.seekTo(getPosn());
        player.start();
    }

    public void stopPlayer()
    {
        if (isPlaying())
        {
            player.stop();
        }

        paused = false;
    }

    public void stop(String id)
    {
        paused = false;

        nonVisibleIconData.clear();
        nonVisibleIconData.putSerializable("id", id);
        player.stop();
        sendSoundIconNonVisible = handler.createMessage(sendSoundIconNonVisible, whatInteger.getSetSoundIconNonVisible(), nonVisibleIconData);
        handler.sendMessage(sendSoundIconNonVisible);

        data.clear();
        data.putString("NULL", "NULL");
        setCurrentSong = handler.createMessage(setCurrentSong, whatInteger.getCurrentSong(), data);
        handler.sendMessage(setCurrentSong);

        stopThread = true;

    }

    public void playPrev(int startTime, int stopTime, String id)
    {
        playSong(startTime, stopTime, id);
    }

    public void playNext(int startTime, int stopTime, String id)
    {
        playSong(startTime, stopTime, id);
    }

    public TrackData getCurrentSong()
    {

        if (songs != null && !songs.isEmpty())
        {
            if (songPosn != songs.size())
                playSong = songs.get(songPosn);
            else
            {
                playSong = songs.get(0);
            }
        }

        return playSong;
    }

    public TrackData getPreviousSong()
    {
        if (!songs.isEmpty())
        {
            songPosn--;

            if (songPosn == songs.size())
            {
                songPosn = 0;
            } else if (songPosn > songs.size() - 1)
            {
                songPosn = 0;
                playSong = songs.get(songPosn);
            } else if (songPosn == 0)
            {
                playSong = songs.get(songPosn);
            } else if (songPosn == -1)
            {
                songPosn = songs.size() - 1;
                playSong = songs.get(songPosn);
            } else
            {
                playSong = songs.get(songPosn);
            }
        }

        return playSong;
    }

    public TrackData getNextSong()
    {

        if (!songs.isEmpty())
        {

            songPosn++;

            if (songPosn >= songs.size())
            {
                songPosn = 0;
                playSong = songs.get(songPosn);
            } else
            {
                playSong = songs.get(songPosn);
            }
        }

        return playSong;
    }

    public void checkForNextSongDuringPlay()
    {
        if (previousSongPosn != songs.size() - 1)
        {
            if (!(songPosn > songs.size()) && !(songPosn < 0))
            {
                TrackData song = getNextSong();

                if (songPosn != 0)
                {
                    playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
                } else
                {
                    playSong(song.getStartTime2(), song.getStopTime3(), song.getId());
                }
            }
        } else
        {
            if (SharedPreferencesUtil.getInstance().getRepeat(this))
            {
                TrackData song = getNextSong();

                playNext(song.getStartTime2(), song.getStopTime3(), song.getId());
            } else
            {
                resetSongs();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        player.start();
        player.seekTo(startTime);
        stopThread = false;

    }

    @Override
    public void onSeekComplete(MediaPlayer mp)
    {
        updateMediaPlayerUIControls();

        playerWatcher.execute(new Runnable()
        {
            @Override
            public void run()
            {
                int currentPosition = 0;
                int staticTime = 0;

                while (!stopThread)
                {
                    try
                    {
                        Thread.sleep(1000);
                        currentPosition = getPosn();
                    } catch (InterruptedException e)
                    {
                        return;
                    } catch (Exception e)
                    {
                        return;
                    }

                    final int total = getDur();

                    if (staticTime != 0 && staticTime == currentPosition)
                    {
                        currentPosition = stopTime;
                    }

                    if (currentPosition >= stopTime)
                    {
                        player.seekTo(total);

                        if (songPosn <= songs.size() - 1)
                        {
                            checkForNextSongDuringPlay();
                        }
                    }

                    staticTime = currentPosition;
                }
            }

        });
    }

    public void resetSongs()
    {
        stop(playSong.getId());

        if (songPosn != 0 && !(songPosn < 0))
        {
            if (!songs.isEmpty())
                songPosn = 0;
        }
    }

    public void updateMediaPlayerUIControls()
    {
        updateControlsMsg = handler.createMessage(updateControlsMsg, whatInteger.getUpdatePlayControls());
        handler.sendMessage(updateControlsMsg);
    }

    public void setHandler(MessageHandler handler)
    {
        this.handler = handler;
    }

    public ArrayList<TrackData> getSongs()
    {
        return songs;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        stopPlayer();
        player.release();
        return false;
    }
}
