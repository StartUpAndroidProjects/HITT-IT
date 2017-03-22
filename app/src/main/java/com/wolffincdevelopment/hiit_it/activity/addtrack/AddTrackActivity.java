
package com.wolffincdevelopment.hiit_it.activity.addtrack;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.activity.HiitItActivity;
import com.wolffincdevelopment.hiit_it.activity.HiitItIntent;
import com.wolffincdevelopment.hiit_it.activity.addtrack.viewmodel.AddTrackItem;
import com.wolffincdevelopment.hiit_it.databinding.ActivityAddTrackBinding;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ActionBarUtils;
import com.wolffincdevelopment.hiit_it.util.ActivityTransitionUtil;
import com.wolffincdevelopment.hiit_it.util.InputManagerUtil;

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

    private ActivityAddTrackBinding binding;
    private AddTrackItem addTrackItem;

    private TrackData trackData;
    private boolean inEditMode;

    private Drawable defaultBackground;
    private Drawable emptyBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_track);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        inEditMode =  getIntent().getBooleanExtra(EXTRA_EDIT_TRACK, false);

        trackData = getIntent().getParcelableExtra(EXTRA_TRACK_DATA);
        addTrackItem = new AddTrackItem(this, inputManager, trackData, inEditMode);

        binding.setItem(addTrackItem);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ActionBarUtils.showUpButton(this);

        if (ActivityTransitionUtil.supportsTransitions()) {
            handleTransitions();
        }

        defaultBackground = ContextCompat.getDrawable(this, R.drawable.edittext_underline);
        emptyBackground = ContextCompat.getDrawable(this, R.drawable.edittext_remove_underline);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        supportFinishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            supportFinishAfterTransition();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        addTrackItem.onViewAttached(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addTrackItem.onViewResumed();

        binding.browseTextField.clearFocus();

        addTrackItem.setBinding(binding);

        binding.startTime.setOnFocusChangeListener(this);
        binding.stopTime.setOnFocusChangeListener(this);
        binding.browseTextField.setOnFocusChangeListener(this);

        binding.startTime.addTextChangedListener(addTrackItem);
        binding.stopTime.addTextChangedListener(addTrackItem);
    }

    @Override
    protected void onDestroy() {
        addTrackItem.onViewDetached();
        super.onDestroy();
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

        if (binding.browseTextField.getId() == v.getId()) {
            addTrackItem.onBrowseFieldFocusChanged(v, hasFocus);
        } else {
            addTrackItem.onTimeFieldsFocusChanged((EditText) v, hasFocus);
        }
    }

    @Override
    public void onShowProgressView(boolean isVisible, @StringRes int textResourceId) {

    }

    @Override
    public void onPreviewClicked() {

    }

    @Override
    public void onPauseClicked() {

    }

    @Override
    public void onAddTrack(TrackData trackData) {

        View view = getCurrentFocus();

        if (inputManager.isActive()) {
            InputManagerUtil.dismissKeyboard(view, inputManager);
        }

        fireBaseManager.pushTrackData(trackData);

        // Tell the browse activity we are finished so we can navigate back to home
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBrowseClicked() {
        startActivityForResult(HiitItIntent.createBrowse(this, true), BROWSE_ACTIVITY_REQUEST_CODE);
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

        if(isVisible) {
            binding.browseTextField.setBackground(defaultBackground);
            binding.radioSongButton.animate().alpha(1).setDuration(duration);
            binding.previewButton.animate().alpha(1).setDuration(duration);
            binding.addTrackButton.animate().alpha(1).setDuration(duration);
            binding.addTrackButtonView.animate().alpha(1).setDuration(duration);
        } else {
            binding.browseTextField.setBackground(emptyBackground);
            binding.radioSongButton.setAlpha(0f);
            binding.previewButton.setAlpha(0f);
            binding.addTrackButton.setAlpha(0f);
            binding.addTrackButtonView.setAlpha(0f);
        }
    }
}
