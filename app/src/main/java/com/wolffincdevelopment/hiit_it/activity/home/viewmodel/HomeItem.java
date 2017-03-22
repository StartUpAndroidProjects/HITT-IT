package com.wolffincdevelopment.hiit_it.activity.home.viewmodel;

import android.content.Context;
import android.databinding.Bindable;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.wolffincdevelopment.hiit_it.BR;
import com.wolffincdevelopment.hiit_it.BaseViewModel;
import com.wolffincdevelopment.hiit_it.FireBaseManager;
import com.wolffincdevelopment.hiit_it.LifeCycle;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.RxJavaBus;
import com.wolffincdevelopment.hiit_it.TrackDataList;
import com.wolffincdevelopment.hiit_it.activity.HiitItActivity;
import com.wolffincdevelopment.hiit_it.activity.home.listeners.HomeListItemListener;
import com.wolffincdevelopment.hiit_it.manager.UserManager;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.widget.MediaControllerView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class HomeItem extends BaseViewModel implements HomeListItemListener, HiitItActivity.HiitItActivityCallBack,
        View.OnLongClickListener, MediaControllerView.MediaControllerListener {

    private UserManager userManager;
    private FireBaseManager fireBaseManager;

    private Context context;
    private RxJavaBus rxJavaBus;
    private TrackDataList trackDataList;

    private int currentTrackSetCount;
    private boolean footerOpen;
    private boolean continuousPlay;

    private boolean isPlaying;

    public HomeItem(Context context, UserManager userManager, RxJavaBus rxJavaBus, FireBaseManager fireBaseManager) {
        super();

        this.userManager = userManager;
        this.context = context;
        this.rxJavaBus = rxJavaBus;
        this.fireBaseManager = fireBaseManager;

        trackDataList = TrackDataList.getInstance();

        footerOpen = true;

        currentTrackSetCount = 1;
    }

    public interface HomeItemCallback extends LifeCycle.LoadingView {
        void onDataReady(List<TrackData> trackDataList);

        void onFabMenuClicked();

        void onBrowseClicked();

        void onOptionsClicked(View view, HomeListItem homeListItem, ViewDataBinding binding);

        void onFooterArrowClicked(boolean footerOpen);

        void onItemClicked(HomeListItem listItem);

        void onFooterClicked();

        void onFooterLongPress();

        void onEditItem(TrackData trackData, ViewDataBinding binding);

        void onPlay();

        void onNext();

        void onPrev();
    }

    @Override
    protected HomeItemCallback getViewCallback() {
        return (HomeItemCallback) super.getViewCallback();
    }

    @Override
    protected void refreshData() {

        state = NetworkState.IDLE;

        if (hasViewCallback()) {
            getViewCallback().onDataReady(trackDataList);
        }
    }

    @Override
    public void onDataChanged() {
        refreshData();
    }

    @Bindable
    public String getCurrentTrackCount() {

        String sets = currentTrackSetCount == 1 ? " Set" : " Sets";

        if (userManager.getCurrentTrackContinuous()) {
            return " Continuous";
        } else {
            return " " + String.valueOf(currentTrackSetCount) + sets;
        }
    }

    @Bindable
    public Drawable getFooterArrow() {

        if (footerOpen) {
            return ContextCompat.getDrawable(context, R.drawable.arrow_down_white_48dp);
        } else {
            return ContextCompat.getDrawable(context, R.drawable.arrow_up_white_48dp);
        }
    }

    private void setFooterOpen(boolean footerOpen) {
        this.footerOpen = footerOpen;
        notifyPropertyChanged(BR.footerArrow);
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void optionsItemSelected(MenuItem menuItem, final HomeListItem homeListItem, ViewDataBinding binding) {

        HashMap<String, Object> hashMap = null;

        if (menuItem.getItemId() == R.id.move_Up) {
            hashMap = trackDataList.moveItemUp(homeListItem.getTrackData());
        } else if (menuItem.getItemId() == R.id.move_Down) {
            hashMap = trackDataList.moveItemDown(homeListItem.getTrackData());
        } else if (menuItem.getItemId() == R.id.edit_item) {

            if (hasViewCallback()) {
                getViewCallback().onEditItem(homeListItem.getTrackData(), binding);
            }

        } else {

            fireBaseManager.deleteTrack(homeListItem.getTrackData().getKey());

            if (trackDataList.size() == 1) {
                trackDataList.clear();
            }

            hashMap = trackDataList.reorderItems(homeListItem.getTrackData());
        }

        if (hashMap != null) {
            fireBaseManager.updateChildren(fireBaseManager.getUserKeyAndTracksDB(), hashMap);
        }

        refreshData();
    }

    private void hideTrackImage() {

        if (!userManager.getPrefHasSeenAddTrackImage()) {
            userManager.setSeenAddTrackImage(true);
        }

        notifyPropertyChanged(BR.hideAddToTrackImage);
    }

    @Bindable
    public int getHideAddToTrackImage() {
        return userManager.getPrefHasSeenAddTrackImage() ? View.GONE : View.VISIBLE;
    }

    @Override
    public void onOptionsClicked(View view, HomeListItem listItem, ViewDataBinding binding) {
        if (hasViewCallback() && !isPlaying) {
            getViewCallback().onOptionsClicked(view, listItem, binding);
        }

        if (isPlaying) {
            Toast toast = Toast.makeText(context, "Please pause music to edit", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void onBrowseClicked() {
        if (hasViewCallback()) {
            getViewCallback().onBrowseClicked();
        }
    }

    public void onFabMenuClicked() {
        if (hasViewCallback()) {
            hideTrackImage();
            getViewCallback().onFabMenuClicked();
        }
    }

    public void onFooterArrowClicked() {

        if (hasViewCallback()) {
            getViewCallback().onFooterArrowClicked(footerOpen);
        }

        if (footerOpen) {
            setFooterOpen(false);
        } else {
            setFooterOpen(true);
        }
    }

    public void onFooterClicked() {

        getCurrentTrackSetCount(false);

        if (hasViewCallback()) {
            getViewCallback().onFooterClicked();
        }
    }

    private void getCurrentTrackSetCount(boolean continuous) {

        if (!continuous) {

            userManager.setCurrentTrackContinuous(false);

            if (!continuousPlay) {

                userManager.setCurrentTrackContinuous(false);

                currentTrackSetCount++;

                if (currentTrackSetCount > 10) {
                    currentTrackSetCount = 1;
                }

                // Store as preference so the Music Service can loop the correct amount
                userManager.setCurrentTrackCount(currentTrackSetCount);
            }

        } else {

            userManager.setCurrentTrackContinuous(true);
        }

        continuousPlay = continuous;

        notifyPropertyChanged(BR.currentTrackCount);
    }

    @Override
    public boolean onLongClick(View v) {

        if (continuousPlay) {
            getCurrentTrackSetCount(false);
        } else {
            getCurrentTrackSetCount(true);
        }

        if (hasViewCallback()) {
            getViewCallback().onFooterLongPress();
            return true;
        }

        return false;
    }

    @Override
    public void onItemClicked(HomeListItem listItem) {
        if (hasViewCallback()) {
            getViewCallback().onItemClicked(listItem);
        }
    }

    @Override
    public void onPlay() {
        if (hasViewCallback()) {
            getViewCallback().onPlay();
        }
    }

    @Override
    public void onNext() {
        if (hasViewCallback()) {
            getViewCallback().onNext();
        }
    }

    @Override
    public void onPrev() {
        if (hasViewCallback()) {
            getViewCallback().onPrev();
        }
    }
}
