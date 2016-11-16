package com.wolffincdevelopment.hiit_it;

/**
 * Created by Kyle Wolff on 11/8/16.
 */

public abstract class Item {

    public int resourceId;

    public Item(int resourceId) {
        this.resourceId = resourceId;
    }

    public enum ItemType {

        TRACK_ITEM,
        HEADER,
        FOOTER
    }

    public abstract ItemType getItemType();
}
