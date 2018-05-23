
package com.wolffincdevelopment.hiit_it.activity.addtrack;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.StringRes;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.activity.HiitItActivity;
import com.wolffincdevelopment.hiit_it.activity.addtrack.viewmodel.AddTrackItem;
import com.wolffincdevelopment.hiit_it.databinding.ActivityAddTrackBinding;
import com.wolffincdevelopment.hiit_it.service.PreviewMusicService;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ActionBarUtils;
import com.wolffincdevelopment.hiit_it.util.ActivityTransitionUtil;
import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;
import com.wolffincdevelopment.hiit_it.util.InputManagerUtil;
import com.wolffincdevelopment.hiit_it.util.StringUtils;

import static com.wolffincdevelopment.hiit_it.activity.HiitItIntent.BROWSE_ACTIVITY_REQUEST_CODE;
import static com.wolffincdevelopment.hiit_it.activity.HiitItIntent.EXTRA_EDIT_TRACK;
import static com.wolffincdevelopment.hiit_it.activity.HiitItIntent.EXTRA_TRACK_DATA;

 /*
  * Add Track Activity Created by Kyle Wolff
  *
  * This is the activity for adding music
  */

public class AddTrackActivity extends HiitItActivity implements AddTrackItem.AddTrackItemCallback,
    View.OnFocusChangeListener {

    private InputMethodManager inputManager;
    private PreviewMusicService previewMusicService;

    private ActivityAddTrackBinding binding;
    private AddTrackItem addTrackItem;

    private TrackData trackData;
    private boolean inEditMode;
    private boolean serviceBound;

    private boolean finishing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_track);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        inEditMode = getIntent().getBooleanExtra(EXTRA_EDIT_TRACK, false);

        trackData = getIntent().getParcelableExtra(EXTRA_TRACK_DATA);
        addTrackItem = new AddTrackItem(this, inputManager, trackData, inEditMode);

        binding.setItem(addTrackItem);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ActionBarUtils.showUpButton(this);

        if (ActivityTransitionUtil.supportsTransitions()) {
            handleTransitions();
        }

        finishing = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopMusicPlayer();
        finishing = true;
        setResult(RESULT_CANCELED);
        supportFinishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            stopMusicPlayer();
            finishing = true;
            setResult(RESULT_CANCELED);
            supportFinishAfterTransition();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        addTrackItem.onViewAttached(this);

        Intent playIntent = new Intent(this, PreviewMusicService.class);
        bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addTrackItem.onViewResumed();

        addTrackItem.setBinding(binding);

        binding.startTime.setOnFocusChangeListener(this);
        binding.stopTime.setOnFocusChangeListener(this);

        binding.startTime.addTextChangedListener(addTrackItem);
        binding.stopTime.addTextChangedListener(addTrackItem);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finishing = false;
    }

    @Override
    protected void onDestroy() {
        addTrackItem.onViewDetached();
        super.onDestroy();

        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            previewMusicService.stopSelf();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BROWSE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            TrackData trackData1 = data.getParcelableExtra(EXTRA_TRACK_DATA);

            if (getIntent().getBooleanExtra(EXTRA_EDIT_TRACK, false)) {
                trackData1.setKey(trackData.getKey());
                trackData1.setOrderId(trackData.getOrderId());
            }

            addTrackItem = new AddTrackItem(this, inputManager, trackData1, getIntent().getBooleanExtra(EXTRA_EDIT_TRACK, false));
            binding.setItem(addTrackItem);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        addTrackItem.onTimeFieldsFocusChanged((EditText) v, hasFocus);
    }

    @Override
    public void onShowProgressView(boolean isVisible, @StringRes int textResourceId) {

    }

    @Override
    public void onPreviewClicked(boolean play) {

        if (previewMusicService != null) {

            if (play) {

                final int startTime;
                final int stopTime;

                if (StringUtils.isEmptyOrNull(binding.startTime.getText().toString())) {
                    startTime = trackData.getStartTimeInMilliseconds();
                } else {
                    startTime = ConvertTimeUtils.convertTimeToMilliseconds(binding.startTime.getText().toString());
                }

                if (StringUtils.isEmptyOrNull(binding.stopTime.getText().toString())) {
                    stopTime = trackData.getStopTimeInMilliseconds();
                } else {
                    stopTime = ConvertTimeUtils.convertTimeToMilliseconds(binding.stopTime.getText().toString());
                }

                previewMusicService.playSong(startTime, stopTime);

            } else {
                previewMusicService.stop();
            }
        }
    }

    @Override
    public void onAddTrack(TrackData trackData) {

        stopMusicPlayer();

        View view = getCurrentFocus();

        if (inputManager.isActive()) {
            InputManagerUtil.dismissKeyboard(view, inputManager);
        }

        fireBaseManager.pushTrackData(trackData);

        // Tell the browse activity we are finished so we can navigate back to home
        setResult(RESULT_OK);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleTransitions() {

        getWindow().getSharedElementEnterTransition().addListener(new ActivityTransitionUtil.SimpleTransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                showNonAnimatedViews(false);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                showNonAnimatedViews(true);
            }
        });
    }

    private void showNonAnimatedViews(boolean isVisible) {

        int duration = 200;

        if (isVisible) {

            if (finishing) {
                binding.previewClockContainer.setBackgroundColor(Color.TRANSPARENT);
                binding.previewButton.setVisibility(View.INVISIBLE);
                binding.startTimeContainer.setVisibility(View.INVISIBLE);
                binding.stopTimeContainer.setVisibility(View.INVISIBLE);
                binding.albumImage.setVisibility(View.INVISIBLE);
            } else {
                binding.previewClockContainer.setBackgroundColor(Color.BLACK);
                binding.previewButton.setVisibility(View.VISIBLE);
                binding.startTimeContainer.setVisibility(View.VISIBLE);
                binding.stopTimeContainer.setVisibility(View.VISIBLE);
                binding.albumImage.setVisibility(View.VISIBLE);
            }

            binding.albumImage.animate().alpha(1).setDuration(duration);
            binding.previewButton.animate().alpha(1).setDuration(duration);
            binding.addTrackButton.animate().alpha(1).setDuration(duration);
            binding.addTrackButtonView.animate().alpha(1).setDuration(duration);
            binding.previewClockContainer.animate().alpha(1).setDuration(duration);
            binding.startTimeContainer.animate().alpha(1).setDuration(duration);
            binding.stopTimeContainer.animate().alpha(1).setDuration(duration);

        } else {

            binding.startTimeContainer.setAlpha(0f);
            binding.stopTimeContainer.setAlpha(0f);
            binding.previewButton.setAlpha(0f);
            binding.albumImage.setAlpha(0f);
            binding.previewClockContainer.setAlpha(0f);
            binding.addTrackButton.setAlpha(0f);
            binding.addTrackButtonView.setAlpha(0f);
        }
    }

    private void stopMusicPlayer() {
        if (previewMusicService != null) {
            previewMusicService.stop();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            PreviewMusicService.MusicBinder binder = (PreviewMusicService.MusicBinder) service;
            previewMusicService = binder.getService();
            serviceBound = true;

            previewMusicService.setListener(addTrackItem);
            previewMusicService.setSongToPreview(trackData);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };
}
