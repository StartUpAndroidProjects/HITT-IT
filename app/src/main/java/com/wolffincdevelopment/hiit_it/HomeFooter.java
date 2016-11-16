package com.wolffincdevelopment.hiit_it;

import android.content.Context;

import com.wolffincdevelopment.hiit_it.util.SharedPreferencesUtil;

/**
 * Created by Kyle Wolff on 11/11/16.
 */

public class HomeFooter extends Item {

    private SharedPreferencesUtil sharedPreferencesUtil;

    public HomeFooter() {
        super(R.layout.base_activity_foot_row);
        sharedPreferencesUtil = SharedPreferencesUtil.getInstance();
    }

    public boolean getRepeat(Context context) {
        return sharedPreferencesUtil.getRepeat(context);
    }

    @Override
    public ItemType getItemType() {
        return ItemType.FOOTER;
    }
}
