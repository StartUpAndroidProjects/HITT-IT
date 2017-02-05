package com.wolffincdevelopment.hiit_it;

import com.wolffincdevelopment.hiit_it.service.model.TrackData;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Kyle Wolff on 2/3/17.
 */

public class TrackDataList extends ArrayList<TrackData> {

    public static TrackDataList trackDataList;

    public static TrackDataList getInstance() {

        if (trackDataList == null) {
            trackDataList = new TrackDataList();
        }

        return trackDataList;
    }

    public TrackDataList moveItemUp(TrackData trackData) {

        if (trackData.getOrderId() != 0) {
           // Collections.rotate(this.subList(trackData.getOrderId(), size()), 1);
        }

        return this;
    }
}
