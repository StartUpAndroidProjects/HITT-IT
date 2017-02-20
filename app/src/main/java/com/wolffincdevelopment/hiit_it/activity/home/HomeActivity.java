package com.wolffincdevelopment.hiit_it.activity.home;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.wolffincdevelopment.hiit_it.IconizedMenu;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.activity.HiitItActivity;
import com.wolffincdevelopment.hiit_it.activity.HiitItIntent;
import com.wolffincdevelopment.hiit_it.activity.home.adapters.HomeAdapter;
import com.wolffincdevelopment.hiit_it.activity.home.viewmodel.HomeItem;
import com.wolffincdevelopment.hiit_it.activity.home.viewmodel.HomeListItem;
import com.wolffincdevelopment.hiit_it.databinding.ActivityHomeBinding;
import com.wolffincdevelopment.hiit_it.databinding.ViewHomeListItemBinding;
import com.wolffincdevelopment.hiit_it.service.HomeMusicService;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ActivityTransitionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class HomeActivity extends HiitItActivity implements HomeItem.HomeItemCallback, HomeMusicService.MusicPlayerListener {

    private ActivityHomeBinding binding;
    private HomeItem homeItem;
    private HomeAdapter homeAdapter;
    private IconizedMenu firstMenuSelected;
    private ValueAnimator animator;

    private Animation rotateFabForward;
    private Animation rotateFabBackward;

    private boolean fabMenuIsShowing;
    private int initalHeight;

    private ViewHomeListItemBinding layoutBinding;
    private List<HomeListItem> homeListItems;

    private HomeMusicService musicService;
    private Intent playIntent;
    private boolean serviceBound;

    private HomeActivity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeItem = new HomeItem(this, userManager, getRxJavaBus(), fireBaseManager);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.setItem(homeItem);
        binding.controllerView.setListener(homeItem);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.replayFooter.setOnLongClickListener(homeItem);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        homeAdapter = new HomeAdapter(this, homeItem);
        binding.recyclerView.setAdapter(homeAdapter);

        rotateFabForward = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate_forward);
        rotateFabBackward = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate_backward);

        setCallBack(homeItem);

        if (ActivityTransitionUtil.supportsTransitions()) {
            handleTransitions();
        }

        homeListItems = new ArrayList<>();
        activity = this;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onStart() {
        super.onStart();
        homeItem.onViewAttached(this);

        playIntent = new Intent(this, HomeMusicService.class);
        bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeItem.onViewResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fabMenuIsShowing) {
            closeFABMenu();
        }
    }

    @Override
    protected void onDestroy() {
        homeItem.onViewDetached();
        super.onDestroy();

        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            musicService.stopSelf();
        }
    }

    @Override
    public void onShowProgressView(boolean isVisible, @StringRes int textResourceId) {

    }

    @Override
    public void onDataReady(List<TrackData> trackDataList) {

        homeListItems.clear();

        for (TrackData trackData : trackDataList) {
            homeListItems.add(new HomeListItem(this, trackData));
        }

        homeAdapter.updateData(homeListItems);

        if (serviceBound) {
            musicService.setAudioList(homeListItems);
        }
    }

    @Override
    public void onEditItem(TrackData trackData, ViewDataBinding binding) {

        layoutBinding = (ViewHomeListItemBinding) binding;
        Intent intent = HiitItIntent.createAddTrackEdit(this, trackData);

        ActivityTransitionUtil.startActivity(this, intent, gatherTransitionViews(layoutBinding));
    }

    @NonNull
    private List<View> gatherTransitionViews(ViewHomeListItemBinding layoutBinding) {

        List<View> transitionViews = new ArrayList<>();

        if (ActivityTransitionUtil.includeContainer()) {
            transitionViews.add(layoutBinding.trackItem);
        }

        transitionViews.add(layoutBinding.startTimePlaceholder);
        transitionViews.add(layoutBinding.startTimeTextview);
        transitionViews.add(layoutBinding.stopTimePlaceholder);
        transitionViews.add(layoutBinding.stopTimeTextView);
        transitionViews.add(layoutBinding.trackSongTextview);


        return transitionViews;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleTransitions() {

        getWindow().getSharedElementEnterTransition().addListener(new ActivityTransitionUtil.SimpleTransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                showNonAnimatedViews(true);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                showNonAnimatedViews(false);
            }
        });
    }

    private void showNonAnimatedViews(boolean isVisible) {
        int duration = 200;

        if (isVisible) {
            layoutBinding.soundIconImageview.animate().alpha(1).setDuration(duration);
            layoutBinding.optionsIcon.animate().alpha(1).setDuration(duration);
            layoutBinding.trackTextView.animate().alpha(1).setDuration(duration);
        } else {
            layoutBinding.soundIconImageview.setAlpha(0f);
            layoutBinding.optionsIcon.setAlpha(0f);
            layoutBinding.trackTextView.setAlpha(0f);
        }
    }

    @Override
    public void onOptionsClicked(View view, final HomeListItem homeListItem, final ViewDataBinding binding) {

        final IconizedMenu popup = new IconizedMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
        popup.show();

        if (firstMenuSelected != null && firstMenuSelected.isShowing() && popup.isShowing()) {
            firstMenuSelected.dismiss();
        }

        firstMenuSelected = popup;

        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                homeItem.optionsItemSelected(item, homeListItem, binding);
                return true;
            }
        });
    }

    @Override
    public void onBrowseClicked() {
        startActivity(HiitItIntent.createBrowse(this, false));
    }

    @Override
    public void onFabMenuClicked() {

        if (fabMenuIsShowing) {
            closeFABMenu();
        } else {
            showFABMenu();
        }
    }

    @Override
    public void onFooterArrowClicked(boolean footerOpen) {

        if (initalHeight == 0) {
            initalHeight = binding.replayFooter.getMeasuredHeight();
        }

        if (!footerOpen) {
            openFooter();
        } else {
            closeFooter();
        }
    }

    @Override
    public void onFooterClicked() {

    }

    @Override
    public void onFooterLongPress() {

    }

    private void openFooter() {

        animator = ValueAnimator.ofInt(initalHeight - 94, initalHeight);
        animator.setDuration(300);
        addListener();
        animator.start();
    }

    private void closeFooter() {

        animator = ValueAnimator.ofInt(initalHeight, initalHeight - 94);
        animator.setDuration(300);
        addListener();
        animator.start();
    }

    private void addListener() {

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                binding.replayFooter.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                binding.replayFooter.requestLayout();
            }
        });
    }

    private void showFABMenu() {

        fabMenuIsShowing = true;

        binding.fab.startAnimation(rotateFabForward);

        binding.fabBrowse.animate().translationY(-getResources().getDimension(R.dimen.fab_marginBottom_Browse_animation))
                .alpha(1f).setDuration(300);

        binding.fabSpotify.animate().translationY(-getResources().getDimension(R.dimen.fab_marginBottom_Spotify_animation))
                .alpha(1f).setDuration(300);
    }

    private void closeFABMenu() {

        fabMenuIsShowing = false;

        binding.fab.startAnimation(rotateFabBackward);

        binding.fabBrowse.animate().translationY(0).alpha(0).setDuration(300);
        binding.fabSpotify.animate().translationY(0).alpha(0).setDuration(300);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            HomeMusicService.MusicBinder binder = (HomeMusicService.MusicBinder) service;
            musicService = binder.getService();

            serviceBound = true;

            // Set up the service listeners and call backs
            musicService.setMusicPlayerListener(activity);
            musicService.setAudioList(homeListItems);
            musicService.setMediaControllerView(binding.controllerView);
            musicService.setUserManager(userManager);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    public void onItemClicked(HomeListItem listItem) {

        if (serviceBound) {
            play(listItem);
        }
    }

    @Override
    public void onPlay() {
        play();
    }

    @Override
    public void onNext() {
        if (serviceBound) {
            musicService.playNext();
        }
    }

    @Override
    public void onPrev() {
        if (serviceBound) {
            musicService.playPrev();
        }
    }

    private void play() {
        play(null);
    }

    private void play(HomeListItem homeListItem) {

        if (serviceBound) {

            if (homeListItem != null && musicService.getCurrentSong() != null) {

                if (homeListItem.getTrackData().getKey().equals(musicService.getCurrentSong().getKey())) {

                    if (musicService.isPlaying()) {
                        musicService.pausePlayer();
                        homeItem.setPlaying(false);
                    } else if (musicService.isPaused()) {
                        musicService.resume();
                        homeItem.setPlaying(true);
                    } else {
                        musicService.playSong(homeListItem.getTrackData());
                        homeItem.setPlaying(true);
                    }

                } else {
                    musicService.playSong(homeListItem.getTrackData());
                    homeItem.setPlaying(true);
                }

            } else if (musicService.isPlaying()) {
                musicService.pausePlayer();
                homeItem.setPlaying(false);
            } else if (musicService.isPaused()) {
                musicService.resume();
                homeItem.setPlaying(true);
            } else {
                musicService.playSong();
                homeItem.setPlaying(true);
            }
        }
    }

    // Updates UI for the listItems
    @Override
    public void onSongPlaying(HomeListItem listItem) {

        for (HomeListItem homeListItem : homeListItems) {

            if (listItem.getTrackData().getKey().equals(homeListItem.getTrackData().getKey())) {
                homeListItem.setIsPlaying(true);
                homeListItem.setShowIcon(true);
            } else {
                homeListItem.setShowIcon(false);
                homeListItem.setIsPlaying(false);
            }
        }
    }

    @Override
    public void onSongPaused(HomeListItem listItem) {

        for (HomeListItem homeListItem : homeListItems) {

            if (listItem.getTrackData().getKey().equals(homeListItem.getTrackData().getKey())) {
                homeListItem.setIsPlaying(false);
                homeListItem.setShowIcon(true);
            } else {
                homeListItem.setShowIcon(false);
                homeListItem.setIsPlaying(false);
            }
        }
    }

    @Override
    public void onStopMusic() {
        for (HomeListItem homeListItem : homeListItems) {
            homeListItem.setShowIcon(false);
            homeListItem.setIsPlaying(false);
        }
    }
}
