package com.wolffincdevelopment.hiit_it;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;

/*
 * Add Track Activity Created by Kyle Wolff
 *
 * This is the activity for adding music
 */
public class AddTrackActivity extends AppCompatActivity {

    private InputMethodManager inputManager;

    public EditText browseTextField, startTime, stopTime;
    public RadioButton radioButton;
    public TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_track);

        // Instantiating the objects for the Activity UI
        browseTextField = (EditText) findViewById(R.id.browsTextField);
        startTime = (EditText) findViewById(R.id.start_time);
        stopTime = (EditText) findViewById(R.id.stop_time);
        radioButton = (RadioButton) findViewById(R.id.radioSongButton);

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

                if(s.length() == 2 && delete != 0 && s.subSequence(1,2).toString().compareTo(":") != 0)
                {
                    s.append(":");
                }

                if(delete == 0 && s.length() > 2 && s.subSequence(1,2).toString().compareTo(":") == 0)
                {
                    s.delete(1,2);
                }

                if(s.toString().startsWith(":"))
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

        // Added a focusChangeListener so when this field as focus the keyboard does not display
        browseTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(hasFocus)
                {
                    // Hides the keyboard
                    inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(browseTextField.getWindowToken(), 0);
                }

            }
        });
    }
}
