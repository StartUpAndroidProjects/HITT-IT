package com.wolffincdevelopment.hiit_it.service.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.wolffincdevelopment.hiit_it.FireBaseManager.ARTIST;
import static com.wolffincdevelopment.hiit_it.FireBaseManager.DURATION;
import static com.wolffincdevelopment.hiit_it.FireBaseManager.KEY;
import static com.wolffincdevelopment.hiit_it.FireBaseManager.MEDIAID;
import static com.wolffincdevelopment.hiit_it.FireBaseManager.ORDERID;
import static com.wolffincdevelopment.hiit_it.FireBaseManager.SONG;
import static com.wolffincdevelopment.hiit_it.FireBaseManager.STARTTIME;
import static com.wolffincdevelopment.hiit_it.FireBaseManager.STOPTIME;
import static com.wolffincdevelopment.hiit_it.FireBaseManager.STREAM;

/**
 * Created by Kyle Wolff on 1/29/17.
 */

public class TrackData implements Parcelable {

    private String key;
    private String song;
    private String artist;
    private String startTime;
    private String stopTime;
    private String stream;
    private long mediaId;
    private long duration;
    private int orderId;

    public TrackData() {
    }

    public TrackData(String song, String artist, String startTime, String stopTime, String stream, long mediaId, long duration, int orderId) {
        this.song = song;
        this.artist = artist;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.stream = stream;
        this.mediaId = mediaId;
        this.duration = duration;
        this.orderId = orderId;
    }

    public TrackData(String artist, String title, String stream, long duration, long mediaId) {
        this(title, artist, null, null, stream, mediaId, duration, 0);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSong() {
        return song;
    }

    public String getArtist() {
        return artist;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public String getStream() {
        return stream;
    }

    public long getMediaId() {
        return mediaId;
    }

    public long getDuration() {
        return duration;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getName() {
        return String.format("%s - %s", getArtist(), getSong());
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getStartTimeInMilliseconds() {
        return convertTime(startTime);
    }

    public int getStopTimeInMilliseconds() {
        return convertTime(stopTime);
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

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();

        result.put(KEY, key);
        result.put(SONG, song);
        result.put(ARTIST, artist);
        result.put(STARTTIME, startTime);
        result.put(STOPTIME, stopTime);
        result.put(STREAM, stream);
        result.put(MEDIAID, mediaId);
        result.put(DURATION, duration);
        result.put(ORDERID, orderId);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.song);
        dest.writeString(this.artist);
        dest.writeString(this.startTime);
        dest.writeString(this.stopTime);
        dest.writeString(this.stream);
        dest.writeString(this.key);
        dest.writeLong(this.mediaId);
        dest.writeLong(this.duration);
        dest.writeInt(this.orderId);
    }

    protected TrackData(Parcel in) {
        this.song = in.readString();
        this.artist = in.readString();
        this.startTime = in.readString();
        this.stopTime = in.readString();
        this.stream = in.readString();
        this.key = in.readString();
        this.mediaId = in.readLong();
        this.duration = in.readLong();
        this.orderId = in.readInt();
    }

    public static final Creator<TrackData> CREATOR = new Creator<TrackData>() {
        @Override
        public TrackData createFromParcel(Parcel source) {
            return new TrackData(source);
        }

        @Override
        public TrackData[] newArray(int size) {
            return new TrackData[size];
        }
    };
}
