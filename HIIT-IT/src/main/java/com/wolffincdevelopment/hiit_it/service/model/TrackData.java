package com.wolffincdevelopment.hiit_it.service.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;

import java.util.HashMap;
import java.util.Map;

import static com.wolffincdevelopment.hiit_it.FireBaseManager.ALBUM;
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

    protected String key;
    protected String song;
    protected String artist;
    protected String startTime;
    protected String stopTime;
    protected String stream;
    protected String album;
    protected long mediaId;
    protected long duration;
    protected int orderId;

    public TrackData() {
    }

    public TrackData(String song, String artist, String startTime, String stopTime, String stream, String album, long mediaId, long duration, int orderId) {
        this.song = song;
        this.artist = artist;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.stream = stream;
        this.album = album;
        this.mediaId = mediaId;
        this.duration = duration;
        this.orderId = orderId;
    }

    public TrackData(String artist, String title, String stream, String album, long duration, long mediaId) {
        this(title, artist, null, null, stream, album, mediaId, duration, 0);
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

    public String getAlbum() {
        return album;
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

    public void setStream(String stream) {
        this.stream = stream;
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
        return ConvertTimeUtils.convertTimeToMilliseconds(startTime);
    }

    public int getStopTimeInMilliseconds() {
        return ConvertTimeUtils.convertTimeToMilliseconds(stopTime);
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
        result.put(ALBUM, album);
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
        dest.writeString(this.album);
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
        this.album = in.readString();
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
