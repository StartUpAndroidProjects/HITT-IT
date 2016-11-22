package com.wolffincdevelopment.hiit_it.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackDBAdapter;
import com.wolffincdevelopment.hiit_it.model.TrackData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

import com.wolffincdevelopment.hiit_it.viewmodel.TrackItem;
import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;

import static com.wolffincdevelopment.hiit_it.activity.HomeActivity.ADDED_TRACK;

/*
 * Add Track Activity Created by Kyle Wolff
 *
 * This is the activity for adding music
 */
public class AddTrackActivity extends AppCompatActivity {

    public static final int BROWSE_ACTIVITY_RESULT_CODE = 232;

    private InputMethodManager inputManager;
    private TrackDBAdapter trackDBAdapter;
    private TrackData item;
    public TextWatcher textWatcher;

    private String minutes, maxMin;
    private String seconds, maxSec;
    private long maxMilliSec, maxMillMin, secMilli, minMilli;

    private long startTimeStaticSec, startTimeStaticMin;

    private String STOP_TIME_MAX;

    private boolean startTimeSucceeded;

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
            inputManager.hideSoftInputFromWindow(browseTextField.getWindowToken(), 0);
            startNextActivity();
        }
    }

    @OnClick(R.id.add_track_button)
    protected void onClick() {

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

        trackDBAdapter.open();

        TrackItem trackItem = new TrackItem(item);

        trackDBAdapter.createTrackData(trackItem.getArtistName(), trackItem.getSongName(),
                stopTime.getText().toString(), startTime.getText().toString(),
                trackItem.getStream(), trackItem.getMediaId(), trackDBAdapter.getNextOrderId());

        trackDBAdapter.close();

        if (inputManager.isActive()) {
            if (startTime.hasFocus()) {
                inputManager.hideSoftInputFromWindow(startTime.getWindowToken(), 0);
            } else if (stopTime.hasFocus()) {
                inputManager.hideSoftInputFromWindow(stopTime.getWindowToken(), 0);
            }
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
    protected void onResume() {
        super.onResume();
        browseTextField.clearFocus();

        init();

        if (!addTrackButton.isEnabled()) {
            addTrackButton.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BROWSE_ACTIVITY_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                item = data.getParcelableExtra("item");
                TrackItem trackItem = new TrackItem(item);

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

    private void setResultAddTrackFinish() {
        setResult(ADDED_TRACK);
        finish();
    }

    private void startNextActivity() {
        Intent browseActivity = new Intent(AddTrackActivity.this, BrowseActivity.class);
        startActivityForResult(browseActivity, BROWSE_ACTIVITY_RESULT_CODE);
    }

    public void init() {
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

        return checked;

    }
}