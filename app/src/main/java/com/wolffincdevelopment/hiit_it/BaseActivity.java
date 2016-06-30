package com.wolffincdevelopment.hiit_it;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    private Intent addTrackIntent;
    private Intent playIntent;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    private BaseAdapter baseAdapter;

    private TrackDBAdapter trackDBAdapter;

    private ArrayList<TrackData> songList;
    private List<TrackData> trackDataList;

    private FirstTimePreference prefFirstTime;

    private MusicService musicService;
    private ServiceConnection musicConnection;

    private MusicService.MusicBinder binder;

    private FloatingActionButton fab;
    private ImageView firstTimeUserImageSwitcher;
    private ImageButton playButton, nextButton, prevButton;
    private ProgressDialog progress;

    private boolean musicBound;
    private boolean musicConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progress = new ProgressDialog(this);
        progress.setMessage("Loading...");

        setContentView(R.layout.base_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hides the default title for the actity so we can use our custom one
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        firstTimeUserImageSwitcher = (ImageView) findViewById(R.id.first_time_user_add_image);
        playButton = (ImageButton) findViewById(R.id.play);
        nextButton = (ImageButton) findViewById(R.id.next);
        prevButton = (ImageButton) findViewById(R.id.prev);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        addTrackIntent = new Intent(BaseActivity.this,AddTrackActivity.class);
        prefFirstTime = new FirstTimePreference(getApplicationContext());


        trackDBAdapter = new TrackDBAdapter(getBaseContext());

        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        checkFirstTimePreference();
    }

    public void checkForAddedTracks() {

        trackDBAdapter.open();
        trackDataList = trackDBAdapter.getAllTracks();
        songList = trackDBAdapter.getAllStreams();
        trackDBAdapter.close();
        baseAdapter.refresh(trackDataList);
    }

    public void refreshSongList(){
        trackDBAdapter.open();
        songList = trackDBAdapter.getAllStreams();
        trackDBAdapter.close();

        if(musicService != null) {
            musicService.setList(songList);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        progress.show();

        checkFirstTimePreference();

        // Recycler View Adapter, passing the arrayList
        baseAdapter = new BaseAdapter(trackDataList);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(baseAdapter);

        // Adding the divider lines to the recycler view
        recyclerView.addItemDecoration(new DividerItemDecoration(this));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prefFirstTime.runCheckFirstTime(getBaseContext().getString(R.string.firstTimeFabPressed));
                BaseActivity.this.startActivity(addTrackIntent);
            }
        });

        checkForAddedTracks();

        musicConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                binder = (MusicService.MusicBinder)service;
                //get service
                musicService = binder.getService();
                //pass list
                musicService.setList(songList);
                musicBound = true;

                baseAdapter.setMusicService(musicService);

                musicService.getBaseAcitivty(BaseActivity.this);

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
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (musicService != null) {

                    if (musicService.getSongs().isEmpty()) {

                    } else {

                        TrackData currentSong = musicService.getCurrentSong();

                        if(!musicService.isPlaying()) {
                            musicService.playSong(currentSong.getStartTime2(), currentSong.getStopTime3());
                        } else {
                            pauseResume();
                        }

                    }
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TrackData song = musicService.getNextSong();
                musicService.playNext(song.getStartTime2(), song.getStopTime3());

            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TrackData song = musicService.getPreviousSong();
                musicService.playPrev(song.getStartTime2(), song.getStopTime3());
            }
        });


        refreshSongList();

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

        if(musicService.isPlaying()) {
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

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(musicService != null && musicConnected == true) {
            progress.dismiss();
        }
    }

    @Override
    protected  void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

