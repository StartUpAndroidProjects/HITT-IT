package com.wolffincdevelopment.hiit_it.activity.browse.viewmodel;

import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;

/**
 * Created by Kyle Wolff on 2/4/17.
 */

public class ListItem extends Item {

    private TrackData trackData;

    public ListItem(TrackData trackData) {
        super(R.layout.view_browse_list_item);

        this.trackData = trackData;
    }

    public String getArtist() {
        return trackData.getArtist();
    }

    public String getSong() {
        return trackData.getSong();
    }

    public TrackData getTrackData() {
        return trackData;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.TRACK_ITEM;
    }
}
