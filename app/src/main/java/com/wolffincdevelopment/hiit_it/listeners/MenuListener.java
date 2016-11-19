package com.wolffincdevelopment.hiit_it.listeners;

import android.view.MenuItem;

import com.wolffincdevelopment.hiit_it.viewmodel.TrackItem;

/**
 * Created by Kyle Wolff on 11/16/16.
 */

public interface MenuListener {
    void onMenuItemSelected(TrackItem trackItem, MenuItem menuItem);
}
