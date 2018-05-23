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

import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;
import com.wolffincdevelopment.hiit_it.widget.MusicPlayer;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Kyle Wolff on 4/23/17.
 */

public class PreviewMusicService extends Service implements MusicPlayer.OnCompletionListener, MusicPlayer.OnPreparedListener,
		MusicPlayer.OnErrorListener, MusicPlayer.OnSeekCompleteListener {

	private Executor playerWatcher;
	private Handler handler;
	private TrackData trackData;
	private MusicPlayer musicPlayer;
	private boolean stopThread;
	private boolean playNonDefaultTime;

	private int startTime;
	private int stopTime;

	private PreviewListener listener;

	private final IBinder musicBind = new PreviewMusicService.MusicBinder();

	public interface PreviewListener {
		void onCountDown(String time);
		void onStop();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		playerWatcher = Executors.newSingleThreadExecutor();
		handler = new Handler(getMainLooper());

		musicPlayer = new MusicPlayer();
		musicPlayer.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		musicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		musicPlayer.setOnPreparedListener(this);
		musicPlayer.setOnErrorListener(this);
		musicPlayer.setOnSeekCompleteListener(this);
		musicPlayer.setOnCompletionListener(this);

		stopThread = false;
		playNonDefaultTime = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (musicPlayer != null) {
			//stopMedia();
			musicPlayer.release();
		}
	}

	public void setListener(PreviewListener listener) {
		this.listener = listener;
	}

	public void playSong(int startTime, int stopTime) {

		this.startTime = startTime;
		this.stopTime = stopTime;

		stopThread = false;
		musicPlayer.reset();

		Uri trackUri = Uri.parse(trackData.getStream());

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
				playSong(startTime, stopTime);
			}

		} catch (IOException e) {
			Log.e("Logged Issue: ", e.getMessage());
		}

	}

	public void stop() {

		if (musicPlayer.isPlaying()) {

			stopThread = true;
			musicPlayer.stop();

			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onStop();
				}
			});
		}
	}

	public void setSongToPreview(TrackData trackData) {
		this.trackData = trackData;
	}

	private void startTimer() {

		playerWatcher.execute(new Runnable() {

			@Override
			public void run() {

				while (!stopThread) {

					try {
						Thread.sleep(500);
					} catch (Exception e) {
						return;
					}

					if (!stopThread) {

						getCountDown(musicPlayer.getCurrentPosition());

						if (playNonDefaultTime) {

							if (musicPlayer.getCurrentPosition() >= stopTime) {
								stop();
							}
						}
					}
				}
			}
		});
	}

	private void getCountDown(long millisecond) {

		final String time = ConvertTimeUtils.convertMilliSecToStringWithColon(millisecond);

		if (!stopThread) {

			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onCountDown(time);
				}
			});
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stop();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {

		if (stopTime == musicPlayer.getDuration() || stopTime < startTime) {
			playNonDefaultTime = false;
		} else {
			playNonDefaultTime = true;
		}

		musicPlayer.start();

		if (playNonDefaultTime) {
			musicPlayer.seekTo(startTime);
		} else {
			startTimer();
		}

	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		startTimer();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	public class MusicBinder extends Binder {
		public PreviewMusicService getService() {
			return PreviewMusicService.this;
		}
	}
}
