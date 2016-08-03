package com.wolffincdevelopment.hiit_it;

import java.io.Serializable;

/**
 * Created by kylewolff on 6/2/2016.
 */
public class TrackData implements Serializable{

    public String song, artist, startTime, stopTime, id, stream;
    public long mediaId, duration;
    public String orderId;

    public TrackData(String orderId) {
        this.orderId = orderId;
    }

    public TrackData(String stream, long mediaId, String startTime, String stopTime, String orderId) {

        this.stream = stream;
        this.mediaId = mediaId;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.orderId = orderId;
    }

    public TrackData(String artist, String song, String stream, String startTime, String stopTime, long mediaId, String id, String orderId)
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

    public String getOrderId() { return  orderId; }

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

        String convertToCorrectString = time.replace(":", "");
        int correctIntForString = Integer.parseInt(convertToCorrectString);

        return (correctIntForString * 1000);
    }
}


