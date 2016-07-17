package com.wolffincdevelopment.hiit_it;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BaseActivity extends AppCompatActivity {

	public static final int ADD_ACTIVITY_RESULT_CODE = 232;

    private Intent playIntent;

    private RecyclerView.LayoutManager mLayoutManager;
    private BaseAdapter baseAdapter;
    private Handler handler;
    private What whatIntegers;
    private MessageHandler messageHandler;


    private TrackDBAdapter trackDBAdapter;

    private ArrayList<TrackData> songList;
    private List<TrackData> trackDataList;

    private FirstTimePreference prefFirstTime;

    private MusicService musicService;
    private ServiceConnection musicConnection;

    private MusicService.MusicBinder binder;

    private ProgressDialog progress;

    private boolean musicBound;
    private boolean musicConnected = false;

    @BindView( R.id.recycler_view )
    RecyclerView recyclerView;

    @BindView( R.id.first_time_user_add_image )
    ImageView firstTimeUserImageSwitcher;

    @BindView( R.id.play )
    ImageButton playButton;

    @BindView( R.id.next )
    ImageButton nextButton;

    @BindView( R.id.prev )
    ImageButton prevButton;

    @BindView( R.id.fab )
    FloatingActionButton fab;

    ///Click Handlers

    @OnClick(R.id.fab)
    protected void onFabPressed()
    {
        prefFirstTime.runCheckFirstTime( getString(R.string.firstTimeFabPressed) );

        Intent addTrackIntent = new Intent(BaseActivity.this, AddTrackActivity.class);
        startActivityForResult(addTrackIntent, ADD_ACTIVITY_RESULT_CODE);
    }

    @OnClick(R.id.play)
    protected void onPlayPressed()
    {
        if (musicService != null) {

            if (musicService.getSongs().isEmpty()) {

            } else {

                TrackData currentSong = musicService.getCurrentSong();

                if(!musicService.isPlaying() && !musicService.isPaused()) {
                    musicService.playSong(currentSong.getStartTime2(), currentSong.getStopTime3());
                } else {
                    pauseResume();
                }
            }
        }
    }

    @OnClick(R.id.next)
    protected void onNextPressed()
    {
        if(musicService != null) {
            TrackData song = musicService.getNextSong();
            musicService.playNext(song.getStartTime2(), song.getStopTime3());
        }
    }

    @OnClick(R.id.prev)
    protected void onPrevPressed()
    {
        if(musicService != null) {
            TrackData song = musicService.getPreviousSong();
            musicService.playPrev(song.getStartTime2(), song.getStopTime3());
        }
    }


	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );

		if (requestCode == ADD_ACTIVITY_RESULT_CODE )
		{
			refreshSongList();
		}
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_activity);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hides the default title for the actity so we can use our custom one
        getSupportActionBar().setDisplayShowTitleEnabled(false);

		prefFirstTime = new FirstTimePreference(this);
		trackDBAdapter = new TrackDBAdapter(this);
		mLayoutManager = new LinearLayoutManager(this);
		trackDataList = new ArrayList<>();
        whatIntegers = new What();

        setSongList();

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                if(msg.what == whatIntegers.getRefreshSongList()) {
                    refreshSongList();
                } else if(msg.what == whatIntegers.getUpdatePlayControls()) {
                    updatePlayPauseButtons();
                } else if(msg.what == whatIntegers.getSetSoundIconVisible()) {
                    baseAdapter.updateSoundIcon((long) msg.getData().get("id"));
                } else if(msg.what == whatIntegers.getSetSoundIconNonVisible()) {
                    baseAdapter.setSoundIconInvisible((long) msg.getData().get("id"));
                }

                return false;
            }
        });

        messageHandler = new MessageHandler(handler);

    }

	@Override
	protected void onResume()
    {
		super.onResume();

		init();

		if(musicService != null && musicConnected == true) {
			progress.dismiss();
		}
	}

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

    }

    private void init()
    {
		progress = new ProgressDialog(this);
		progress.setMessage("Loading...");
		progress.show();

		checkFirstTimePreference();

		initRecyclerView();

		initMusicService();

        setSongList();

    }


	private void initRecyclerView()
	{
        // Recycler View Adapter, passing the arrayList
        baseAdapter = new BaseAdapter(trackDataList);
        baseAdapter.setHandler(messageHandler);

		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(baseAdapter);

		// Adding the divider lines to the recycler view
		recyclerView.addItemDecoration(new DividerItemDecoration(this));
	}

	private void initMusicService()
    {

		musicConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected( ComponentName name, IBinder service) {

				binder = (MusicService.MusicBinder)service;
				//get service
				musicService = binder.getService();
				//pass list
                musicService.setList(songList);
				musicBound = true;

                musicService.setHandler(messageHandler);

				progress.dismiss();
				musicConnected = true;

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				musicBound = false;
				musicConnected = false;
			}
		};

		if(playIntent == null){

			playIntent = new Intent(this, MusicService.class);
//			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
		}
	}

    public void refreshSongList()
    {

        trackDBAdapter.open();
        trackDataList = trackDBAdapter.getAllTracks();
        songList = trackDBAdapter.getAllStreams();
        trackDBAdapter.close();

        baseAdapter.refresh(trackDataList);

        if(musicService != null) {
            musicService.setList(songList);
        }
    }

    public void setSongList()
    {
        trackDBAdapter.open();
        trackDataList = trackDBAdapter.getAllTracks();
        songList = trackDBAdapter.getAllStreams();
        trackDBAdapter.close();

        if(musicService != null) {
            musicService.setList(songList);
        }
    }

    private void checkFirstTimePreference() {

        SharedPreferences sharedPreferences = getSharedPreferences("FirstKeyPreferences", Context.MODE_PRIVATE);

        if(sharedPreferences.contains(getBaseContext().getString(R.string.firstTimeFabPressed))) {
            firstTimeUserImageSwitcher.setVisibility(View.INVISIBLE);
        }else {
            firstTimeUserImageSwitcher.setVisibility(View.VISIBLE);
        }
    }

    public void updatePlayPauseButtons() {

        if (musicService.isPlaying()) {
            playButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        } else {
            playButton.setImageResource(R.drawable.ic_play_circle_outline_white);
        }
    }

    public void pauseResume() {

            if (musicService.isPaused() && !musicService.isPlaying()) {
                musicService.resume();
                updatePlayPauseButtons();
            } else {
                musicService.pausePlayer();
                updatePlayPauseButtons();
            }
    }





}

