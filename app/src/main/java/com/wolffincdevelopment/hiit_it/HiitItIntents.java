package com.wolffincdevelopment.hiit_it;

import android.content.Context;
import android.content.Intent;

import com.wolffincdevelopment.hiit_it.activity.AddTrackActivity;

/**
 * Created by Kyle Wolff on 11/22/16.
 */

public class HiitItIntents {

    public static Intent createAddTrackIntent(Context context) {
        return new Intent(context, AddTrackActivity.class);
    }
}
