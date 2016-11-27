package com.wolffincdevelopment.hiit_it.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.ColorRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wolffincdevelopment.hiit_it.HiitItIntents;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackDBAdapter;
import com.wolffincdevelopment.hiit_it.model.TrackData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTouch;

import com.wolffincdevelopment.hiit_it.util.BuildSupportUtil;
import com.wolffincdevelopment.hiit_it.util.InputManagerUtil;
import com.wolffincdevelopment.hiit_it.viewmodel.TrackItem;
import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.wolffincdevelopment.hiit_it.activity.HomeActivity.ADDED_TRACK;

/*
 * Add Track Activity Created by Kyle Wolff
 *
 * This is the activity for adding music
 */
public class AddTrackActivity extends BaseMusicActivity {

    public static final int BROWSE_ACTIVITY_RESULT_CODE = 232;

    private InputMethodManager inputManager;
    private TrackDBAdapter trackDBAdapter;
    private TrackData item;
    private TrackItem trackItem;
    public TextWatcher textWatcher;

    private String minutes, maxMin;
    private String seconds, maxSec;
    private long maxMilliSec, maxMillMin, secMilli, minMilli;

    private long startTimeStaticSec, startTimeStaticMin;

    private String STOP_TIME_MAX;

    private boolean startTimeSucceeded;
    private boolean inPreviewMode;
    private boolean retrievedXY;

    private int previewButtonX;
    private int previewButtonY;

    protected PreviewMusicService previewMusicService;
    private PreviewMusicService.MusicBinder binder;
    private boolean musicBound;

    private CountDownTimer countDownTimer;

    private Intent playIntent;

    @BindView(R.id.add_track_button_view)
    View buttonShadow;

    @BindView(R.id.previewButton)
    ImageButton previewButton;

    @BindView(R.id.add_track_container)
    RelativeLayout addTrackContainer;

    @BindView(R.id.pausePlay)
    ImageButton playActionButton;

    @BindView(R.id.timer)
    TextView timer;

    @BindView(R.id.song_artist)
    TextView songAndArtist;

    @BindView(R.id.card_view)
    CardView cardView;

    @BindView(R.id.browse_text_field)
    EditText browseTextField;

    @BindView(R.id.start_time)
    EditText startTime;

    @BindView(R.id.stop_time)
    EditText stopTime;

    @BindView(R.id.add_track_button)
    Button addTrackButton;

    @BindView(R.id.radio_song_button)
    RadioButton radioButton;

    @OnTouch(R.id.previewButton)
    protected boolean onTouch(View view, MotionEvent motionEvent) {

        // Only retrieve the coordinates one time so the animation looks correct
        if(!retrievedXY) {

            retrievedXY = true;

            previewButtonX = (int) motionEvent.getRawX();
            previewButtonY = (int) motionEvent.getRawY();
        }

        // On touch gets called three times setting this check fires playSong once
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            playSong(previewButtonX, previewButtonY);
        }

        return false;
    }

    @OnClick(R.id.pausePlay)
    protected void onPausePlayClicked(View view) {

        if (previewMusicService != null && previewMusicService.isPlaying()) {
           cancelCounter();
        }
    }


    @OnFocusChange(R.id.start_time)
    protected void onStartTimeFocuseChange(boolean focused) {

        if (focused)
            startTime.getText().clear();
        else
            checkTimeField(startTime);
    }

    @OnFocusChange(R.id.stop_time)
    protected void onStopTimeFocuseChange(boolean focused) {

        if (focused)
            stopTime.getText().clear();
        else
            checkTimeField(stopTime);
    }

    // Added a focusChangeListener so when this field as focus the keyboard does not display
    @OnFocusChange(R.id.browse_text_field)
    protected void onFocusChanged(boolean focused) {

        if (focused) {
            // Hides the keyboard
            InputManagerUtil.dismissKeyboard(browseTextField, inputManager);
            startNextActivity();
        }
    }

    @OnClick(R.id.add_track_button)
    protected void onClick() {

        // Verify the Start and Stop times do not have any validation errors
        checkStartAndStopTimeDurations();

        trackDBAdapter.open();

        trackDBAdapter.createTrackData(trackItem.getArtistName(), trackItem.getSongName(),
                stopTime.getText().toString(), startTime.getText().toString(),
                trackItem.getStream(), trackItem.getMediaId(), trackDBAdapter.getNextOrderId());

        trackDBAdapter.close();

        View view = getCurrentFocus();

        if (view != null && inputManager.isActive()) {
            InputManagerUtil.dismissKeyboard(view, inputManager);
        }

        setResultAddTrackFinish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_track);
        ButterKnife.bind(this);

        trackDBAdapter = new TrackDBAdapter(getBaseContext());
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    @Override
    protected void onStart() {
        super.onStart();

        playIntent = new Intent(this, PreviewMusicService.class);
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        browseTextField.clearFocus();

        init();

        if (!addTrackButton.isEnabled()) {
            addTrackButton.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public void onBackPressed() {

        if (inPreviewMode) {
            cancelCounter();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            supportFinishAfterTransition();
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BROWSE_ACTIVITY_RESULT_CODE) {

            if (resultCode == RESULT_OK) {
                item = data.getParcelableExtra("item");
                trackItem = new TrackItem(item);

                /*
                 * Greater than 1 hour in milliseconds
                 */
                if (trackItem.getDuration() >= 3600000) {

                } else {

                    browseTextField.setText(trackItem.getSongName() + " - " + trackItem.getArtistName());
                    stopTime.setText(trackItem.getStopTimeWithColon());
                    startTime.setText("00:00");

                    STOP_TIME_MAX = ConvertTimeUtils.convertMilliSecToStringWithColon(trackItem.getDuration());

                    checkFields();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (playIntent != null) {

            stopService(playIntent);

            if (musicBound) {
                unbindService(musicConnection);
            }
        }

        if (previewMusicService != null) {
            previewMusicService.stopPlayer();
            previewMusicService.releasePlayer();
        }
    }

    private void setResultAddTrackFinish() {
        setResult(ADDED_TRACK);
        supportFinishAfterTransition();
    }

    private void startNextActivity() {
        startActivityForResult(HiitItIntents.createBrowseIntent(this), BROWSE_ACTIVITY_RESULT_CODE);
    }

    public void init() {

        if (musicService != null) {
            previewMusicService.setMusicService(musicService);
        }

        /*
         * This textWatcher is what handles the logic for the colon
         * We do not want the user to be able to select the colon so we will auto populate for them
         */
        textWatcher = new TextWatcher() {
            private int delete;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                delete = count;
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkFields();

                if (s.length() == 2 && delete != 0 && s.subSequence(1, 2).toString().compareTo(":") != 0) {
                    s.append(":");
                }

                if (s.toString().matches("^([0-9]{3})$")) {
                    CharSequence added = s.subSequence(0, 2);
                    CharSequence added2 = s.subSequence(2, 3);

                    s.clear();

                    s.append(added + ":" + added2);

                }

                if (s.toString().startsWith(":") && s.toString().endsWith(":")) {
                    s.clear();
                }

                if (s.toString().matches("^([0-9]{1}):$")) {
                    s.delete(1, 2);
                }

                if (s.toString().matches("^([0-9]{2})::$")) {
                    s.delete(2, 3);
                }

                if (s.toString().matches("^([0-9]{2}):([0-9]{1}):$")) {
                    s.delete(4, 5);
                }
            }
        };

        // Adding the textWatcher to the object
        startTime.addTextChangedListener(textWatcher);
        stopTime.addTextChangedListener(textWatcher);

    }

    private void checkStartAndStopTimeDurations() {

        if (STOP_TIME_MAX != null) {

            if (!checkTimeFields(startTime)) {
                startTime.setText("00:00");
            }

            if (!checkTimeFields(stopTime)) {

                stopTime.setText(STOP_TIME_MAX);

                if (!startTimeSucceeded) {
                    startTime.setText("00:00");
                }

                STOP_TIME_MAX = null;

            }
        }
    }

    private void checkTimeField(EditText textField) {

        if (textField.getText().toString().matches("^:([0-9]{2})$")) {
            CharSequence add = textField.getText().subSequence(0, 3);
            textField.getText().clear();
            textField.getText().append("00" + add);
        } else if (textField.getText().toString().matches("^([0-9]{2}):$")) {
            CharSequence add = textField.getText().subSequence(0, 3);
            textField.getText().clear();
            textField.getText().append(add + "00");
        } else if (textField.getText().toString().matches("^([0-9]{2}):([0-9]{1})$")) {
            CharSequence add = textField.getText().subSequence(0, 4);
            textField.getText().clear();
            textField.getText().append(add + "0");
        } else if (textField.getText().toString().matches("^([0-9]{1}):([0-9]{2})$")) {
            CharSequence add = textField.getText().subSequence(0, 4);
            textField.getText().clear();
            textField.getText().append("0" + add);
        }
    }

    public void checkFields() {

        boolean verified;

        if (browseTextField.getText().toString().isEmpty() || startTime.getText().toString().isEmpty()
                || stopTime.getText().toString().isEmpty() || !startTime.getText().toString().matches("^([0-9]{2}):([0-9]{2})$")
                || !stopTime.getText().toString().matches("^([0-9]{2}):([0-9]{2})$")) {

            verified = false;
            addTrackButton.setEnabled(verified);
            addTrackButton.setBackgroundColor(Color.GRAY);

        } else {

            verified = true;
            addTrackButton.setEnabled(verified);
            addTrackButton.setBackground(getResources().getDrawable(R.color.colorAccent));
        }
    }

    public boolean checkTimeFields(EditText timeField) {

        boolean checked = false;

        if (timeField.getText().toString().length() >= 5) {

            maxSec = STOP_TIME_MAX.substring(3, 5);
            maxMilliSec = ConvertTimeUtils.getMilliSeconds(maxSec, "sec");

            maxMin = STOP_TIME_MAX.substring(0, 2);
            maxMillMin = ConvertTimeUtils.getMilliSeconds(maxMin, "min");

            seconds = timeField.getText().toString().substring(3, 5);
            secMilli = ConvertTimeUtils.getMilliSeconds(seconds, "sec");

            minutes = timeField.getText().toString().substring(0, 2);
            minMilli = ConvertTimeUtils.getMilliSeconds(minutes, "min");


            if (timeField == startTime) {
                if (minMilli <= maxMillMin) {
                    if (minMilli == maxMillMin) {
                        if (secMilli <= maxMilliSec) {
                            checked = true;
                        } else {
                            checked = false;
                        }
                    } else {
                        if (secMilli <= maxMilliSec) {
                            checked = true;
                        } else if (secMilli >= maxMilliSec && secMilli < 59000) {
                            checked = true;
                        }
                    }

                } else {
                    checked = false;
                }

                if (checked) {
                    startTimeStaticSec = secMilli;
                    startTimeStaticMin = minMilli;
                }

            } else {
                if (minMilli >= startTimeStaticMin && minMilli <= maxMillMin) {
                    if (minMilli == maxMillMin) {
                        if (secMilli <= maxMilliSec) {
                            checked = true;
                        } else {
                            checked = false;
                        }
                    } else if (minMilli == startTimeStaticMin) {
                        if (secMilli >= startTimeStaticSec) {
                            checked = true;
                        } else {
                            checked = false;
                        }
                    } else if (minMilli < maxMillMin && minMilli > startTimeStaticMin) {
                        if (secMilli < 59000) {
                            checked = true;
                        } else {
                            checked = false;
                        }
                    }
                } else {
                    checked = false;
                }
            }

            if (timeField == stopTime && (startTimeStaticMin + startTimeStaticSec) == (secMilli + minMilli)) {
                checked = false;
                startTimeSucceeded = false;
            } else {
                startTimeSucceeded = true;
            }
        } else {
            checked = false;
        }

        return checked;

    }

    @TargetApi(21)
    private void animateCircularReveal(final ViewGroup viewRoot, @ColorRes int color, int x, int y) {

        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);
        viewRoot.setBackgroundColor(ContextCompat.getColor(this, color));
        anim.setDuration(1000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();

    }

    @TargetApi(21)
    private void animateReverseCircularReveal(final ViewGroup viewRoot, @ColorRes int color, int x, int y) {

        final Context context = this;

        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, finalRadius / 2, 0);
        viewRoot.setBackgroundColor(ContextCompat.getColor(this, color));
        anim.setDuration(1000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                @ColorRes int color = android.R.color.background_light;

                viewRoot.setBackgroundColor(ContextCompat.getColor(context, color));
                showAddTrack();
            }
        });

    }

    private void hideAddTrack() {

        cardView.setVisibility(View.GONE);
        addTrackButton.setVisibility(View.GONE);
        buttonShadow.setVisibility(View.GONE);

        playActionButton.setVisibility(View.VISIBLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void showAddTrack() {

        cardView.setVisibility(View.VISIBLE);
        addTrackButton.setVisibility(View.VISIBLE);
        buttonShadow.setVisibility(View.VISIBLE);

        playActionButton.setVisibility(View.GONE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            binder = (PreviewMusicService.MusicBinder) service;

            previewMusicService = binder.getService();

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void playSong(int x, int y) {

        // Default to zeros every time we start a new song
        timer.setText("00:00:00");

        if (item != null) {

            checkStartAndStopTimeDurations();

            item.startTime = startTime.getText().toString();
            item.stopTime = stopTime.getText().toString();

            // Show start time asap
            timer.setText("00:"  + item.startTime);

            if (musicBound) {

                inPreviewMode = true;
                hideAddTrack();
                animateCircularReveal(addTrackContainer, R.color.colorAccent, x, y);

                songAndArtist.setText(trackItem.getSongName() + " - " + trackItem.getArtistName());
                previewMusicService.playSong(trackItem);

                startTimer();
            }

        } else {
            Snackbar.make(addTrackContainer, "An issue occurred try to replay song...", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void startTimer() {

        View view = getCurrentFocus();

        if (view != null && inputManager.isActive()) {
            InputManagerUtil.dismissKeyboard(view, inputManager);
        }

        countDownTimer = new CountDownTimer(trackItem.getStopTimeInMilliseconds(), 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                long millis = (trackItem.getStopTimeInMilliseconds() - millisUntilFinished) + trackItem.getStartTimeInMilliseconds();

                timer.setText("00:" + ConvertTimeUtils.convertMilliSecToStringWithColon(millis));

                if(millis >= trackItem.getStopTimeInMilliseconds()) {
                    countDownTimer.cancel();
                    returnToAddTrack(true);
                }
            }

            @Override
            public void onFinish() {
                // We are reversing the timer so this does not get called
            }

        }.start();
    }

    private void returnToAddTrack(boolean addOne) {

        inPreviewMode = false;

        if (BuildSupportUtil.isLollipopAndUp()) {
            animateReverseCircularReveal(addTrackContainer, R.color.colorAccent, previewButtonX, previewButtonY);
        } else {
            showAddTrack();
        }

        if(addOne) {
            stopTime.setText(ConvertTimeUtils.convertMilliSecToStringWithColon(previewMusicService.getCurrentPosition() + 1));
        } else {
            stopTime.setText(ConvertTimeUtils.convertMilliSecToStringWithColon(previewMusicService.getCurrentPosition()));
        }

        previewMusicService.stopPlayer();
    }

    private void cancelCounter() {

        countDownTimer.cancel();
        countDownTimer = null;

        returnToAddTrack(true);

        if (previewMusicService != null) {
            previewMusicService.stopPlayer();
        }
    }
}