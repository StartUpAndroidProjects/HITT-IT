package com.wolffincdevelopment.hiit_it;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import util.ConvertTime;

/*
 * Add Track Activity Created by Kyle Wolff
 *
 * This is the activity for adding music
 */
public class AddTrackActivity extends AppCompatActivity {

    public static final int BROWSE_ACTIVITY_RESULT_CODE = 232;

    private InputMethodManager inputManager;
    private TrackDBAdapter trackDBAdapter;
    private Intent i;
    private TrackData item;
    private ConvertTime convertTime;
    public  TextWatcher textWatcher;

    private String STOP_TIME_MAX;

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
    protected void onStartTimeFocuseChange(boolean focused)
    {
        if(focused)
            startTime.getText().clear();
        else
            checkTimeField(startTime);
    }

    @OnFocusChange(R.id.stop_time)
    protected void onStopTimeFocuseChange(boolean focused)
    {
        if(focused)
            stopTime.getText().clear();
        else
            checkTimeField(stopTime);
    }

    // Added a focusChangeListener so when this field as focus the keyboard does not display
    @OnFocusChange(R.id.browse_text_field)
    protected void onFocusChanged(boolean focused)
    {
        if(focused)
        {
            // Hides the keyboard
            inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(browseTextField.getWindowToken(), 0);
            startNextActivity();
        }
    }

    @OnClick(R.id.add_track_button)
    protected void onClick()
    {
        if(STOP_TIME_MAX != null) {

            if (!checkTimeFields(startTime)) {
                startTime.setText("00:00");
            }

            if (!checkTimeFields(stopTime)) {
                stopTime.setText(STOP_TIME_MAX);

                STOP_TIME_MAX = null;

            }
        }

        trackDBAdapter.open();

        trackDBAdapter.createTrackData(item.getArtistName(), item.getSongName(),
                stopTime.getText().toString(), startTime.getText().toString(),
                item.getStream(), item.getMediaId(), trackDBAdapter.getNextOrderId());

        trackDBAdapter.close();

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_track);
        ButterKnife.bind(this);

        convertTime = new ConvertTime();

        trackDBAdapter = new TrackDBAdapter(getBaseContext());

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        browseTextField.clearFocus();

        init();

        if(!addTrackButton.isEnabled()) {
            addTrackButton.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BROWSE_ACTIVITY_RESULT_CODE) {

            if(resultCode == RESULT_OK){

                boolean checked = data.getBooleanExtra("enabled", true);
                item = (TrackData) data.getSerializableExtra("listItem");

                browseTextField.setText(item.getSongName() + " - " + item.getArtistName());
                stopTime.setText(convertTime.convertMilliSecToStringWithColon(item.getDuration()));
                startTime.setText("00:00");

                STOP_TIME_MAX = convertTime.convertMilliSecToStringWithColon(item.getDuration());

                checkFields();
            }
        }
    }

    private void startNextActivity() {

        Intent browseActivity = new Intent(AddTrackActivity.this,BrowseActivity.class);
        AddTrackActivity.this.startActivityForResult(browseActivity,BROWSE_ACTIVITY_RESULT_CODE);
    }

    public void init() {

        /*
         * This textWatcher is what handles the logic for the colon
         * We do not want the user to be able to select the colon so we will auto populate for them
         */
        textWatcher= new TextWatcher() {

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

                if(s.length() == 2 && delete != 0 && s.subSequence(1,2).toString().compareTo(":") != 0)
                {
                    s.append(":");
                }

                if(s.toString().matches("^([0-9]{3})$"))
                {
                    CharSequence added = s.subSequence(0,2);
                    CharSequence added2 = s.subSequence(2,3);

                    s.clear();

                    s.append(added + ":" + added2);



                }

                if(s.toString().startsWith(":") && s.toString().endsWith(":"))
                {
                    s.clear();
                }

                if(s.toString().matches("^([0-9]{1}):$"))
                {
                    s.delete(1,2);
                }

                if(s.toString().matches("^([0-9]{2})::$"))
                {
                    s.delete(2,3);
                }

                if(s.toString().matches("^([0-9]{2}):([0-9]{1}):$"))
                {
                    s.delete(4,5);
                }
            }
        };

        // Adding the textWatcher to the object
        startTime.addTextChangedListener(textWatcher);
        stopTime.addTextChangedListener(textWatcher);

    }

    private void checkTimeField(EditText textField)
    {
        if(textField.getText().toString().matches("^:([0-9]{2})$"))
        {
            CharSequence add =  textField.getText().subSequence(0,3);
            textField.getText().clear();
            textField.getText().append("00" + add);
        } else if(textField.getText().toString().matches("^([0-9]{2}):$"))
        {
            CharSequence add =  textField.getText().subSequence(0,3);
            textField.getText().clear();
            textField.getText().append(add + "00");
        } else if(textField.getText().toString().matches("^([0-9]{2}):([0-9]{1})$"))
        {
            CharSequence add =  textField.getText().subSequence(0,4);
            textField.getText().clear();
            textField.getText().append(add + "0");
        } else if(textField.getText().toString().matches("^([0-9]{1}):([0-9]{2})$"))
        {
            CharSequence add =  textField.getText().subSequence(0,4);
            textField.getText().clear();
            textField.getText().append("0" + add);
        }
    }

    public void checkFields()
    {
        boolean verified;

        if(browseTextField.getText().toString().isEmpty() || startTime.getText().toString().isEmpty()
                || stopTime.getText().toString().isEmpty() || !startTime.getText().toString().matches("^([0-9]{2}):([0-9]{2})$")
                || !stopTime.getText().toString().matches("^([0-9]{2}):([0-9]{2})$") ) {

            verified = false;
            addTrackButton.setEnabled(verified);
            addTrackButton.setBackgroundColor(Color.GRAY);

        } else {

            verified = true;
            addTrackButton.setEnabled(verified);
            addTrackButton.setBackground(getResources().getDrawable(R.color.colorAccent));
        }
    }

    public boolean checkTimeFields(EditText timeField)
    {
        if(Integer.valueOf(timeField.getText().toString().substring(0, 2)) > Integer.valueOf(STOP_TIME_MAX.substring(0, 2)) ||
                (Integer.valueOf(timeField.getText().toString().substring(0, 2)).compareTo(Integer.valueOf(STOP_TIME_MAX.substring(0, 2))) == 0 && Integer.valueOf(timeField.getText().toString().substring(3, 5)) > Integer.valueOf(STOP_TIME_MAX.substring(3, 5)))
                            || (Integer.valueOf(timeField.getText().toString().substring(0, 2)) < Integer.valueOf(STOP_TIME_MAX.substring(0, 2)) &&
                        Integer.valueOf(timeField.getText().toString().substring(3, 5)) > Integer.valueOf(STOP_TIME_MAX.substring(3, 5)))
                ||  Integer.valueOf(timeField.getText().toString().substring(3, 5)) >= 60
        ){
            return false;
        }else {
            return  true;
        }
    }
}