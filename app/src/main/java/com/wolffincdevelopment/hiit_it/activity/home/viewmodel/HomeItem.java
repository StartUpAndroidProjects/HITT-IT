package com.wolffincdevelopment.hiit_it.activity.home.viewmodel;

import android.content.Context;
import android.databinding.Bindable;
import android.util.Log;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wolffincdevelopment.hiit_it.BaseViewModel;
import com.wolffincdevelopment.hiit_it.LifeCycle;
import com.wolffincdevelopment.hiit_it.RxJavaBus;
import com.wolffincdevelopment.hiit_it.TrackPlayEvent;
import com.wolffincdevelopment.hiit_it.activity.home.listeners.HomeListItemListener;
import com.wolffincdevelopment.hiit_it.manager.UserManager;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class HomeItem extends BaseViewModel implements HomeListItemListener {

    private UserManager userManager;
    private Context context;
    private RxJavaBus rxJavaBus;
    private List<TrackData> trackDataList;
    private String key;
    private String privateUserKey;

    public HomeItem(Context context, UserManager userManager, RxJavaBus rxJavaBus) {
        super();

        this.userManager = userManager;
        this.context = context;
        this.rxJavaBus = rxJavaBus;

        trackDataList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        TrackData trackData = new TrackData(1001, "Song", "Artist", "Stop Time", "Stop Time", "Stream", 1111, 4400, 1);

        key = databaseReference.push().getKey();

        databaseReference.child("tracks").child(key).setValue(trackData);
    }

    public interface HomeItemCallback extends LifeCycle.LoadingView {
        void onDataReady(List<TrackData> trackDataList);
        void onFabMenuClicked();
        void onBrowseClicked();
    }

    @Override
    protected HomeItemCallback getViewCallback() {
        return (HomeItemCallback) super.getViewCallback();
    }

    @Override
    protected void refreshData() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("tracks").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TrackData trackData;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    if (snapshot != null) {
                        trackDataList.add(snapshot.getValue(TrackData.class));
                    }
                }

                if (hasViewCallback() && !trackDataList.isEmpty()) {
                    getViewCallback().onDataReady(trackDataList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void hideTrackImage() {

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
        //rxJavaBus.send(new TrackPlayEvent(listItem.isPlaying(), listItem.getId()));
    }

    @Override
    public void onOptionsClicked(HomeListItem listItem) {

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
}
