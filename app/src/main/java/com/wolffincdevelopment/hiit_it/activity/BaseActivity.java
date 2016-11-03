package com.wolffincdevelopment.hiit_it.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.wolffincdevelopment.hiit_it.adapter.BaseAdapter;
import com.wolffincdevelopment.hiit_it.DividerItemDecoration;
import com.wolffincdevelopment.hiit_it.FirstTimePreference;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackDBAdapter;
import com.wolffincdevelopment.hiit_it.TrackData;
import com.wolffincdevelopment.hiit_it.util.DialogBuilder;
import com.wolffincdevelopment.hiit_it.util.PermissionUtil;
import com.wolffincdevelopment.hiit_it.widget.MediaControllerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Create by Kyle Wolff
 */
public class BaseActivity extends AppCompatActivity implements MediaControllerView.MediaControllerListener {

	public static final int ADD_ACTIVITY_RESULT_CODE = 232;

    private Intent playIntent;

    private RecyclerView.LayoutManager mLayoutManager;
    private BaseAdapter baseAdapter;
    private Bundle data;

    private TrackDBAdapter trackDBAdapter;

    private ArrayList<TrackData> songList;
    private ArrayList<TrackData> trackDataList;

    private FirstTimePreference prefFirstTime;

    private MusicService musicService;

    private MusicService.MusicBinder binder;

    private ProgressDialog progress;

    private PermissionUtil permissionUtil;

    private boolean musicBound = false;

    @BindView( R.id.recycler_view )
    RecyclerView recyclerView;

    @BindView( R.id.first_time_user_add_image )
    ImageView firstTimeUserImageSwitcher;

    @BindView(R.id.controller_view)
    MediaControllerView mediaControllerView;

    @BindView( R.id.fab )
    FloatingActionButton fab;

    @OnClick(R.id.fab)
    protected void onFabPressed()
    {
        prefFirstTime.runCheckFirstTime( getString(R.string.firstTimeFabPressed) );

        Intent addTrackIntent = new Intent(BaseActivity.this, AddTrackActivity.class);
        startActivityForResult(addTrackIntent, ADD_ACTIVITY_RESULT_CODE);
    }

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );

		if (requestCode == ADD_ACTIVITY_RESULT_CODE )
		{
			setSongList("none", null);
		}
	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_activity);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mediaControllerView.setListener(this);

        // Hides the default title for the activity so we can use our custom one
        getSupportActionBar().setDisplayShowTitleEnabled(false);

		prefFirstTime = new FirstTimePreference(this);
		trackDBAdapter = new TrackDBAdapter(this);
		mLayoutManager = new LinearLayoutManager(this);
		trackDataList = new ArrayList<>();
        data = new Bundle();
        permissionUtil = new PermissionUtil();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        trackDBAdapter.open();
        trackDBAdapter.checkForStorageDeletion();
        trackDBAdapter.close();

    }

    @Override
	protected void onResume()
    {
		super.onResume();

		init();

		if(musicBound)
        {
			dismissDialog();
		}
	}

    @Override
    protected void onStart() {
        super.onStart();

        playIntent = new Intent(this, MusicService.class);
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if(musicBound && musicService.isPlaying())
        {
            musicService.setNotification();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(playIntent != null)
        {
            musicService.stopPlayer();
            stopService(playIntent);

            if(musicBound)
            {
                unbindService(musicConnection);
            }
        }
    }

    private void init()
    {
		showDialog();

		checkFirstTimePreference();

		initRecyclerView();

        setSongList("none", null);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        final Activity activity = this;

        if(requestCode == 0)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {

            }
            else
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE))
                {
                    showDialogOK("The HIIT IT! app needs access to check if you are making or receiving calls. Try again?",
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    switch (which)
                                    {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            permissionUtil.checkPhoneStatePermission(activity);

                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            finish();

                                            break;
                                    }
                                }
                            });
                }
                else
                {
                    showDialogToSettings("Please allo the HIIT IT! app to have access to the Phone State. Please change permissions in settings.",
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    switch (which)
                                    {
                                        case DialogInterface.BUTTON_POSITIVE:

                                            Intent applicationSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            applicationSettingsIntent.setData(uri);
                                            startActivity(applicationSettingsIntent);

                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:

                                            finish();
                                            break;
                                    }
                                }
                            });
                }
            }
        }
        else
        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener)
    {
        DialogBuilder dialogBuilder = new DialogBuilder(message,this);
        dialogBuilder.setButtons("Ok", "Cancel", okListener);
        dialogBuilder.create();
        dialogBuilder.show();
    }

    private void showDialogToSettings(String message, DialogInterface.OnClickListener okListener)
    {
        DialogBuilder dialogBuilder = new DialogBuilder(message,this);
        dialogBuilder.setButtons("Change Permissions", "Dismiss", okListener);
        dialogBuilder.create();
        dialogBuilder.show();
    }

    private void showDialog()
    {
        progress = new ProgressDialog(this);
        progress.setMessage("Loading...");
        progress.show();
    }

    private void dismissDialog() {
        progress.dismiss();
    }


	private void initRecyclerView()
	{
        if(baseAdapter == null) {
            // Recycler View Adapter, passing the arrayList
            baseAdapter = new BaseAdapter(trackDataList);
        }

        if(recyclerView.getLayoutManager() == null) {

            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(baseAdapter);
            // Adding the divider lines to the recycler view
            recyclerView.addItemDecoration(new DividerItemDecoration(this));
        }

	}

    public void setSongList(String action, TrackData trackData)
    {
        trackDBAdapter.open();
        trackDataList = trackDBAdapter.getAllTracks();
        trackDBAdapter.close();

        if(musicBound) {
            musicService.setList(trackDataList, action, trackData);
        }

        if(trackDataList != null && !trackDataList.isEmpty())
        {
            baseAdapter.refresh(trackDataList);
        }
    }

    private void checkFirstTimePreference()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("FirstKeyPreferences", Context.MODE_PRIVATE);

        if(sharedPreferences.contains(getBaseContext().getString(R.string.firstTimeFabPressed))) {
            firstTimeUserImageSwitcher.setVisibility(View.INVISIBLE);
        }else {
            firstTimeUserImageSwitcher.setVisibility(View.VISIBLE);
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            binder = (MusicService.MusicBinder) service;

            musicService = binder.getService();

            musicService.setList(trackDataList, "none", null);

            progress.dismiss();
            musicBound = true;

            musicService.getHiitBus().register(mediaControllerView);
            musicService.getHiitBus().register(baseAdapter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onPlay() {

        if(musicBound) {

            if(musicService.isPlaying()) {
                musicService.pausePlayer();
            } else if(musicService.isPaused()){
                musicService.resume();
            } else {
                musicService.playSong();
            }
        }
    }

    @Override
    public void onNext() {
        if(musicBound) {
            musicService.playNext();
        }
    }

    @Override
    public void onPrev() {
        if(musicBound) {
            musicService.playPrev();
        }
    }
}

