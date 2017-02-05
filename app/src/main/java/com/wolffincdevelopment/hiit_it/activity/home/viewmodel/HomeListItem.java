package com.wolffincdevelopment.hiit_it.activity.home.viewmodel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.RxJavaBus;
import com.wolffincdevelopment.hiit_it.TrackPlayEvent;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;

/**
 * Created by Kyle Wolff on 1/29/17.
 */

public class HomeListItem {

    private TrackData trackData;
    private Context context;
    private boolean isPlaying;
    private boolean wasPlaying;

    public HomeListItem(Context context, TrackData trackData, RxJavaBus rxJavaBus) {
        this.trackData = trackData;
        this.context = context;

        //rxJavaBus.subscribe(TrackPlayEvent.class, this::trackPlayEvent);
    }

    public TrackData getTrackData() {
        return trackData;
    }

    private void trackPlayEvent(TrackPlayEvent trackPlayEvent) {

        if (trackPlayEvent.isPlaying) {
            isPlaying = false;
            wasPlaying = true;
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean wasPlaying() {
        return wasPlaying;
    }

    public long getId() {
        return trackData.getId();
    }

    public String getName() {
        return String.format("%s - %s", trackData.getArtist(), trackData.getSong());
    }

    public String getStartTime() {
        return trackData.getStartTime();
    }

    public String getStopTime() {
        return trackData.getStopTime();
    }

    public Drawable getSoundIcon() {
        return isPlaying ? ContextCompat.getDrawable(context, R.drawable.ic_volume_up_deep_orange_48dp)
                : ContextCompat.getDrawable(context, R.drawable.ic_volume_up_black_48dp);
    }
}
