package com.wolffincdevelopment.hiit_it;

import android.content.Context;
import android.content.Intent;

import com.wolffincdevelopment.hiit_it.activity.AddTrackActivity;
import com.wolffincdevelopment.hiit_it.activity.BrowseActivity;

/**
 * Created by Kyle Wolff on 11/22/16.
 */

public class HiitItIntents {

    public static Intent createAddTrackIntent(Context context) {
        return new Intent(context, AddTrackActivity.class);
    }

    public static Intent createBrowseIntent(Context context) {
        return new Intent(context, BrowseActivity.class);
    }
}
