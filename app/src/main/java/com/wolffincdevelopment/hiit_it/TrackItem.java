package com.wolffincdevelopment.hiit_it;

import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by Kyle Wolff on 11/11/16.
 */

public class TrackItem extends Item {

    private TrackData trackData;
    private boolean show;

    public TrackItem(TrackData trackData) {
        super(R.layout.base_activity_row);

        this.trackData = trackData;
    }

    public String getSongAndArtist() {
        return trackData != null ? trackData.artist + " - " + trackData.song : "";
    }

    public String getSongName() {
        return trackData != null ? trackData.song : "";
    }

    public String getArtistName() {
        return trackData != null ? trackData.artist : "Unknown";
    }

    public String getStartTime() {
        return trackData != null ? trackData.startTime : "00:00";
    }

    public String getStopTimeWithColon() {
        return ConvertTimeUtils.convertMilliSecToStringWithColon(getDuration());
    }

    public String getStopTime() {
        return trackData != null ? trackData.stopTime : "00:00";
    }

    public String getId() {
        return trackData != null ? trackData.id : "";
    }

    public String getStream() {
        return trackData != null ? trackData.stream : "";
    }

    public long getMediaId() {
        return trackData != null ? trackData.mediaId : 0;
    }

    public int getOrderId() {
        return trackData != null ? trackData.orderId : 0;
    }

    public long getDuration() {
        return trackData != null ? trackData.duration : 0;
    }

    public int getStartTime2() {
        return trackData != null ? convertTime(trackData.startTime) : 0;
    }

    public int getStopTime3() {
        return trackData != null ? convertTime(trackData.stopTime) : 0;
    }

    private int convertTime(String time) {

        String minutes;
        String seconds;
        long timeLong, secMilli, minMilli, minutesLong, secondsLong = 0;

        seconds = time.substring(3,5);
        secondsLong = Long.parseLong(seconds);
        secMilli = TimeUnit.SECONDS.toMillis(secondsLong);

        minutes = time.substring(0,2);
        minutesLong = Long.parseLong(minutes);
        minMilli = TimeUnit.MINUTES.toMillis(minutesLong);

        timeLong = minMilli + secMilli;

        if(timeLong < Integer.MIN_VALUE || timeLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(timeLong + "cannot cast Long value as int");
        }

        return (int) timeLong;
    }

    public TrackData getTrackData() {
        return trackData;
    }

    public boolean show() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.TRACK_ITEM;
    }
}
