package com.wolffincdevelopment.hiit_it;

import com.wolffincdevelopment.hiit_it.service.model.TrackData;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kyle Wolff on 2/3/17.
 */

public class TrackDataList extends ArrayList<TrackData> {

    private int MAX_ORDERID;

    private TrackData firstObject;
    private TrackData secondObject;

    private int firstOrderId;
    private int secondOrderId;

    private static TrackDataList trackDataList;

    public static TrackDataList getInstance() {

        if (trackDataList == null) {
            trackDataList = new TrackDataList();
        }

        return trackDataList;
    }

    private TrackDataList() {
        super();
    }

    private void setHashMap(HashMap<String, Object> hashMap) {
        hashMap.put(firstObject.getKey(), firstObject.toMap());
        hashMap.put(secondObject.getKey(), secondObject.toMap());
    }

    /**
     * Reorder items upward
     *
     * @param trackData is the object containing music data {@link TrackData}
     * @return {@link HashMap} a map of the json data
     */
    public HashMap<String, Object> moveItemUp(TrackData trackData) {

        MAX_ORDERID = size() - 1;

        HashMap<String, Object> hashMap = new HashMap<>();

        // Set First Object and Id
        firstObject = get(trackData.getOrderId());
        firstOrderId = firstObject.getOrderId();

        // If Id is 0 second object going up will be the last in the list
        // Else
        if (trackData.getOrderId() == 0) {
            secondObject = get(MAX_ORDERID);

        } else {
            secondObject = get(firstOrderId - 1);
        }

        secondOrderId = secondObject.getOrderId();

        firstObject.setOrderId(secondOrderId);
        secondObject.setOrderId(firstOrderId);

        setHashMap(hashMap);

        return hashMap;
    }

    /**
     * Reorder items downward
     *
     * @param trackData is the object containing music data {@link TrackData}
     * @return {@link HashMap} a map of the json data
     */
    public HashMap<String, Object> moveItemDown(TrackData trackData) {

        MAX_ORDERID = size() - 1;

        HashMap<String, Object> hashMap = new HashMap<>();

        // Set First Object and Id
        firstObject = get(trackData.getOrderId());
        firstOrderId = firstObject.getOrderId();

        if (trackData.getOrderId() == MAX_ORDERID) {
            secondObject = get(0);

        } else {
            secondObject = get(firstOrderId + 1);
        }

        secondOrderId = secondObject.getOrderId();

        firstObject.setOrderId(secondOrderId);
        secondObject.setOrderId(firstOrderId);

        setHashMap(hashMap);

        return hashMap;
    }

    /**
     * Reorder items and set the order id.
     * This method should only be used after the delete track call.
     *
     * @param deletedItem item that was deleted {@link TrackData}
     * @return {@link HashMap} a map of the json data
     */
    public HashMap<String, Object> reorderItems(TrackData deletedItem) {

        MAX_ORDERID = size() - 1;

        HashMap<String, Object> hashMap = new HashMap<>();

        if (deletedItem.getOrderId() != MAX_ORDERID) {

            for (TrackData data : this) {

                if (data.getOrderId() > deletedItem.getOrderId()) {
                    data.setOrderId(data.getOrderId() - 1);
                    hashMap.put(data.getKey(), data.toMap());
                }
            }
        }

        return hashMap;
    }
}
