package com.wolffincdevelopment.hiit_it.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kylewolff on 6/2/2016.
 */
public class TrackData implements Parcelable {

    public String song, artist, startTime, stopTime, id, stream;
    public long mediaId, duration;
    public int orderId;

    public TrackData(String artist, String song, String stream, String startTime, String stopTime, long mediaId, String id, int orderId) {
        this.song = song;
        this.artist = artist;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.stream = stream;
        this.mediaId = mediaId;
        this.id = id;
        this.orderId = orderId;
    }

    public TrackData(String artist, String song, String stream, long duration, long mediaId) {
        this.song = song;
        this.artist = artist;
        this.stream = stream;
        this.mediaId = mediaId;
        this.duration = duration;
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
        dest.writeString(this.id);
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
        this.id = in.readString();
        this.stream = in.readString();
        this.mediaId = in.readLong();
        this.duration = in.readLong();
        this.orderId = in.readInt();
    }

    public static final Parcelable.Creator<TrackData> CREATOR = new Parcelable.Creator<TrackData>() {
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


