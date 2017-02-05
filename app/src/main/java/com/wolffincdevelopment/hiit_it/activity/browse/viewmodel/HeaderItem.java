package com.wolffincdevelopment.hiit_it.activity.browse.viewmodel;

import com.wolffincdevelopment.hiit_it.R;

/**
 * Created by Kyle Wolff on 11/15/16.
 */

public class HeaderItem extends Item {

    private String character;

    public HeaderItem(String character) {
        super(R.layout.view_browse_list_header);
        this.character = character;
    }

    public String getChar() {
        return character;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.HEADER;
    }
}
