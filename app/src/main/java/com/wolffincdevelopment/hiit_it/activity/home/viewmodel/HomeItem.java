package com.wolffincdevelopment.hiit_it.activity.home.viewmodel;

import android.content.Context;
import android.databinding.Bindable;
import android.view.MenuItem;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wolffincdevelopment.hiit_it.BaseViewModel;
import com.wolffincdevelopment.hiit_it.FireBaseHelper;
import com.wolffincdevelopment.hiit_it.LifeCycle;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.RxJavaBus;
import com.wolffincdevelopment.hiit_it.TrackDataList;
import com.wolffincdevelopment.hiit_it.activity.home.listeners.HomeListItemListener;
import com.wolffincdevelopment.hiit_it.manager.UserManager;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;

import java.util.List;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class HomeItem extends BaseViewModel implements HomeListItemListener, ValueEventListener {

    private UserManager userManager;
    private FireBaseHelper fireBaseHelper;

    private Context context;
    private RxJavaBus rxJavaBus;
    private TrackDataList trackDataList;
    private String key;
    private String privateUserKey;

    public HomeItem(Context context, UserManager userManager, RxJavaBus rxJavaBus, FireBaseHelper fireBaseHelper) {
        super();

        this.userManager = userManager;
        this.context = context;
        this.rxJavaBus = rxJavaBus;
        this.fireBaseHelper = fireBaseHelper;

        trackDataList = TrackDataList.getInstance();

        // The value event listener is always listening.
        if (fireBaseHelper.getTracksAndUserKeyChild() != null) {
            fireBaseHelper.getTracksAndUserKeyChild().addValueEventListener(this);
        }
    }

    public interface HomeItemCallback extends LifeCycle.LoadingView {
        void onDataReady(List<TrackData> trackDataList);

        void onFabMenuClicked();

        void onBrowseClicked();

        void onOptionsClicked(View view, HomeListItem homeListItem);

        void onItemClicked();
    }

    @Override
    protected HomeItemCallback getViewCallback() {
        return (HomeItemCallback) super.getViewCallback();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        // Empty list for the new items
        trackDataList.clear();

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

            if (snapshot != null) {

                TrackData trackData = snapshot.getValue(TrackData.class);
                trackData.setKey(snapshot.getKey());

                trackDataList.add(trackData);
            }
        }

        refreshData();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    protected void refreshData() {
        if (hasViewCallback() && !trackDataList.isEmpty()) {
            getViewCallback().onDataReady(trackDataList);
        }
    }

    public void optionsItemSelected(MenuItem menuItem, final HomeListItem homeListItem) {

        if (menuItem.getItemId() == R.id.move_Up) {
            trackDataList = trackDataList.moveItemUp(homeListItem.getTrackData());
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
    public void onItemClicked(HomeListItem listItem) {
        if (hasViewCallback()) {
            getViewCallback().onItemClicked();
        }
    }

    @Override
    public void onOptionsClicked(View view, HomeListItem listItem) {
        if (hasViewCallback()) {
            getViewCallback().onOptionsClicked(view, listItem);
        }
    }

    public void onBrowseClicked() {

        TrackData trackData = new TrackData(1001, "Song", "Artist", "Stop Time", "Stop Time", "Stream", 1111, 4400, 1);

//        fireBaseHelper.setValue(fireBaseHelper.getRandomKey(), trackData);

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
}
