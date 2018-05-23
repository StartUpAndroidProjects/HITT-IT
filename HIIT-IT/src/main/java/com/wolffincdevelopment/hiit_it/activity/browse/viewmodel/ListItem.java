package com.wolffincdevelopment.hiit_it.activity.browse.viewmodel;

import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ConvertTimeUtils;

/**
 * Created by Kyle Wolff on 2/4/17.
 */

public class ListItem extends Item {

    private TrackData trackData;

    public ListItem(TrackData trackData) {
        super(R.layout.view_browse_list_item);

        this.trackData = trackData;
    }

    public String getArtistAndAlbum() {
        return String.format("%s \u2022 %s", trackData.getArtist(), trackData.getAlbum());
    }

    public String getSong() {
        return trackData.getSong();
    }

    public String getDuration() {
        return ConvertTimeUtils.convertMilliSecToStringWithColon(trackData.getDuration());
    }

    public TrackData getTrackData() {
        return trackData;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.TRACK_ITEM;
    }
}
