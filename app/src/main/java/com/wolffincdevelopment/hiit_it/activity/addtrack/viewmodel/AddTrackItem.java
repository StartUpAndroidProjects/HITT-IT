package com.wolffincdevelopment.hiit_it.activity.addtrack.viewmodel;

import android.content.Context;
import android.databinding.Bindable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.wolffincdevelopment.hiit_it.BR;
import com.wolffincdevelopment.hiit_it.BaseViewModel;
import com.wolffincdevelopment.hiit_it.LifeCycle;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.databinding.ActivityAddTrackBinding;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;
import com.wolffincdevelopment.hiit_it.util.InputManagerUtil;
import com.wolffincdevelopment.hiit_it.util.StringUtils;

/**
 * Created by Kyle Wolff on 2/5/17.
 */

public class AddTrackItem extends BaseViewModel implements TextWatcher {

    private ActivityAddTrackBinding binding;
    private InputMethodManager inputManager;
    private Context context;
    private TrackData trackData;

    private String STOP_TIME_MAX;
    private String startTime;
    private String stopTime;

    private boolean inEditMode;
    private boolean verified;
    private boolean startTimeSucceeded;

    private int delete;

    private long startTimeStaticSec = 0;
    private long startTimeStaticMin = 0;

    public AddTrackItem(Context context, InputMethodManager inputManager, TrackData trackData, boolean inEditMode) {
        super();

        this.context = context;
        this.inputManager = inputManager;
        this.trackData = trackData;
        this.inEditMode = inEditMode;

        STOP_TIME_MAX = ConvertTimeUtils.convertMilliSecToStringWithColon(trackData.getDuration());

        if (inEditMode && !StringUtils.isEmptyOrNull(trackData.getStopTime())) {
            setStopTime(trackData.getStopTime());
            setStartTime(trackData.getStartTime());
        } else {
            setStopTime(ConvertTimeUtils.convertMilliSecToStringWithColon(trackData.getDuration()));
            setStartTime(context.getString(R.string.zero_time));
        }
    }

    public interface AddTrackItemCallback extends LifeCycle.LoadingView {
        void onPreviewClicked();

        void onPauseClicked();

        void onAddTrack(TrackData trackData);

        void onBrowseClicked();
    }

    @Override
    protected AddTrackItemCallback getViewCallback() {
        return (AddTrackItemCallback) super.getViewCallback();
    }

    @Override
    protected void refreshData() {
        // There is no data call here needed.
    }

    public void setBinding(ActivityAddTrackBinding binding) {
        this.binding = binding;
    }

    @Bindable
    public String getName() {
        return trackData.getSong();
    }

    @Bindable
    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
        trackData.setStopTime(stopTime);
        notifyPropertyChanged(BR.stopTime);
    }

    @Bindable
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
        trackData.setStartTime(startTime);
        notifyPropertyChanged(BR.startTime);
    }

    @Bindable
    public Drawable getAddTrackButtonDrawable() {
        return verified ? ContextCompat.getDrawable(context, R.drawable.square_button_coloraccent_background) : ContextCompat.getDrawable(context, R.drawable.square_button_gray_background);
    }

    @Bindable
    public boolean getAddTrackButtonVerified() {
        return verified;
    }

    private void setAddTrackButtonVerified(boolean verified) {
        this.verified = verified;
        notifyPropertyChanged(BR.addTrackButtonDrawable);
        notifyPropertyChanged(BR.addTrackButtonVerified);
    }

    public void onBrowseFieldFocusChanged(View v, boolean hasFocus) {

        if (hasFocus) {
            if (hasViewCallback()) {
                InputManagerUtil.dismissKeyboard(v, inputManager);
                getViewCallback().onBrowseClicked();
            }
        }
    }

    public void onTimeFieldsFocusChanged(EditText editText, boolean hasFocus) {

        if (hasFocus) {
            editText.getText().clear();
        } else {
            checkTimeField(editText);
        }
    }

    public void onAddClicked() {

        checkStartAndStopTimeDurations();

        if (hasViewCallback()) {
            getViewCallback().onAddTrack(trackData);
        }
    }

    private void checkTimeField(EditText textField) {

        if (textField.getText().toString().matches("^:([0-9]{2})$")) {
            CharSequence add = textField.getText().subSequence(0, 3);
            textField.getText().clear();
            textField.setText("00" + add);
        } else if (textField.getText().toString().matches("^([0-9]{2}):$")) {
            CharSequence add = textField.getText().subSequence(0, 3);
            textField.getText().clear();
            textField.setText(add + "00");
        } else if (textField.getText().toString().matches("^([0-9]{2}):([0-9]{1})$")) {
            CharSequence add = textField.getText().subSequence(0, 4);
            textField.getText().clear();
            textField.setText(add + "0");
        } else if (textField.getText().toString().matches("^([0-9]{1}):([0-9]{2})$")) {
            CharSequence add = textField.getText().subSequence(0, 4);
            textField.getText().clear();
            textField.setText("0" + add);
        } else if (textField.getText().toString().matches("^([0-9]{1})$")) {
            CharSequence add = textField.getText().subSequence(0, 1);
            textField.getText().clear();
            textField.setText("00:0" + add);
        } else if (textField.getText().toString().isEmpty()) {
            textField.setText(context.getString(R.string.zero_time));
        }
    }

    private void checkStartAndStopTimeDurations() {

        if (STOP_TIME_MAX != null) {

            if (!checkTimeFields(binding.startTime)) {
                setStartTime("00:00");
            } else {
                setStartTime(binding.startTime.getText().toString());
            }

            if (!checkTimeFields(binding.stopTime)) {

                setStopTime(STOP_TIME_MAX);

                if (!startTimeSucceeded) {
                    setStartTime("00:00");
                }

                STOP_TIME_MAX = null;

            } else {
                setStopTime(binding.stopTime.getText().toString());
            }
        }
    }

    private void checkFields() {

        if (binding.browseTextField.getText().toString().isEmpty() || binding.startTime.getText().toString().isEmpty()
                || binding.stopTime.getText().toString().isEmpty() || !binding.startTime.getText().toString().matches("^([0-9]{2}):([0-9]{2})$")
                || !binding.stopTime.getText().toString().matches("^([0-9]{2}):([0-9]{2})$")) {
            setAddTrackButtonVerified(false);
        } else {
            setAddTrackButtonVerified(true);
        }
    }

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

    private boolean checkTimeFields(EditText timeField) {

        String minutes;
        String maxMin;
        String seconds;
        String maxSec;

        long maxMilliSec;
        long maxMillMin;
        long secMilli;
        long minMilli;

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


            if (timeField == binding.startTime) {

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

                if (timeField == binding.stopTime && (startTimeStaticMin + startTimeStaticSec) >= (secMilli + minMilli)) {
                    startTimeSucceeded = false;
                } else {
                    startTimeSucceeded = true;
                }

                if (startTimeSucceeded) {

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

                } else {

                    if (minMilli == maxMillMin) {

                        if (secMilli <= maxMilliSec) {
                            checked = true;
                        } else {
                            checked = false;
                        }

                    } else if (minMilli < maxMillMin) {
                        checked = true;
                    }
                }
            }

        } else {
            checked = false;
        }

        return checked;
    }
}
