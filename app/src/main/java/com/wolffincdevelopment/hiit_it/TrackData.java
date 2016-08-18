package com.wolffincdevelopment.hiit_it;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.lang.Math.*;

/**
 * Created by kylewolff on 6/2/2016.
 */
public class TrackData implements Serializable{

    public String song, artist, startTime, stopTime, id, stream;
    public long mediaId, duration;
    public int orderId;

    public TrackData(int orderId) {
        this.orderId = orderId;
    }

    public TrackData(String stream, long mediaId, String startTime, String stopTime, int orderId) {

        this.stream = stream;
        this.mediaId = mediaId;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.orderId = orderId;
    }

    public TrackData(String artist, String song, String stream, String startTime, String stopTime, long mediaId, String id, int orderId)
    {
        this.song = song;
        this.artist = artist;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.stream = stream;
        this.mediaId = mediaId;
        this.id = id;
        this.orderId = orderId;
    }

    public TrackData(String artist, String song, String stream, long duration, long mediaId)
    {
        this.song = song;
        this.artist = artist;
        this.duration = duration;
        this.stream = stream;
        this.mediaId = mediaId;
    }

    public String getSongAndArtist()
    {
        String track;

        track = artist + " - " + song;

        return track;
    }

    public String getSongName() {
        return song;
    }

    public String getArtistName() {
        return artist;
    }

    public String getStartTime()
    {
        return startTime;
    }

    public String getStopTime()
    {
        return stopTime;
    }

    public String getId() { return id; }

    public String getStream() { return stream;}

    public long getMediaId() { return  mediaId; }

    public int getOrderId() { return  orderId; }

    public long getDuration() {
        return duration;
    }

    public int getStartTime2() {

        return convertTime(startTime);
    }

    public int getStopTime3() {

        return convertTime(stopTime);
    }

    public int convertTime(String time) {

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
}


