package com.wolffincdevelopment.hiit_it.activity.home.viewmodel;

import android.content.Context;
import android.databinding.Bindable;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.wolffincdevelopment.hiit_it.BR;
import com.wolffincdevelopment.hiit_it.BaseViewModel;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;

/**
 * Created by Kyle Wolff on 1/29/17.
 */

public class HomeListItem extends BaseViewModel {

    private TrackData trackData;
    private Context context;
    private String countDown;
    private boolean isPlaying;
    private boolean showIcon;

    public HomeListItem(Context context, TrackData trackData) {
        this.trackData = trackData;
        this.context = context;


        countDown = ConvertTimeUtils.convertMilliSecToStringWithColonWithoutLeadingMinZero(trackData.getStopTimeInMilliseconds()
                - trackData.getStartTimeInMilliseconds());

        isPlaying = false;
    }

    @Override
    protected void refreshData() {
        // Do not need to do anything
    }

    @Bindable
    public String getCountDown() {

        if (!showIcon || countDown.length() == 2 && countDown.contains("00")) {
            countDown = ConvertTimeUtils.convertMilliSecToStringWithColonWithoutLeadingMinZero(trackData.getStopTimeInMilliseconds()
                    - trackData.getStartTimeInMilliseconds());
        }

        return countDown.concat("s");
    }

    public void setCountDown(String countDown) {
        this.countDown = countDown;
        notifyPropertyChanged(BR.countDown);
    }

    public TrackData getTrackData() {
        return trackData;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
        notifyPropertyChanged(BR.timeTint);
        notifyPropertyChanged(BR.soundIcon);
    }

    @Bindable
    public int getTimeTint() {
        return showIcon && isPlaying ? ContextCompat.getColor(context, R.color.colorPrimaryDark) : Color.BLACK;
    }

    @Bindable
    public int getShowIcon() {
        return showIcon ? View.VISIBLE : View.INVISIBLE;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
        notifyPropertyChanged(BR.showIcon);
        notifyPropertyChanged(BR.timeTint);
        notifyPropertyChanged(BR.countDown);
    }

    public String getArtist() {
        return trackData.getArtist();
    }

    public String getSong() {
        return trackData.getSong();
    }

    public String getStartTime() {
        return trackData.getStartTime();
    }

    public String getStopTime() {
        return trackData.getStopTime();
    }

    @Bindable
    public Drawable getSoundIcon() {

        if (isPlaying && showIcon) {
            return ContextCompat.getDrawable(context, R.drawable.ic_volume_up_deep_orange_48dp);
        } else if (!isPlaying && showIcon) {
            return ContextCompat.getDrawable(context, R.drawable.ic_volume_up_black_48dp);
        } else {
            return null;
        }
    }
}
