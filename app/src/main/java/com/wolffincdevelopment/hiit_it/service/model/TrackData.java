package com.wolffincdevelopment.hiit_it.service.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kyle Wolff on 1/29/17.
 */

public class TrackData implements Parcelable {

    private long id;
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

    public TrackData(long id, String song, String artist, String startTime, String stopTime, String stream, long mediaId, long duration, int orderId) {
        this.id = id;
        this.song = song;
        this.artist = artist;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.stream = stream;
        this.mediaId = mediaId;
        this.duration = duration;
        this.orderId = orderId;
    }

    public long getId() {
        return id;
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
        dest.writeLong(this.id);
        dest.writeString(this.stream);
        dest.writeLong(this.mediaId);
        dest.writeLong(this.duration);
        dest.writeInt(this.orderId);
    }

    protected TrackData(Parcel in) {
        this.song = in.readString();
        this.artist = in.readString();
        this.startTime = in.readString();
        this.stopTime = in.readString();
        this.id = in.readLong();
        this.stream = in.readString();
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
