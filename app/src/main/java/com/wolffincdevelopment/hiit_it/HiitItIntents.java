package com.wolffincdevelopment.hiit_it;

import android.content.Context;
import android.content.Intent;


/**
 * Created by Kyle Wolff on 11/22/16.
 */

public class HiitItIntents {

    public static String EXTRA_ITEM = "com.wolffincdevelopment.hiit_it.ITEM";
    public static String EXTRA_FINISH_ACTIVITY = "com.wolffincdevelopment.hiit_it.FINISH_ACTIVITY";

    public static Intent addSingleTopFlag(Intent intent) {
        return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    /*public static Intent createAddTrackIntent(Context context) {
        return new Intent(context, AddTrackActivity.class);
    }

    public static Intent createAddTrackIntent(Context context, TrackData trackData) {
        Intent intent = new Intent(context, AddTrackActivity.class);
        intent.putExtra(EXTRA_ITEM, trackData);
        return intent;
    }
*/
    /*public static Intent createBrowseIntent(Context context, boolean finish) {
        Intent intent = new Intent(context, BrowseActivity.class);
        intent.putExtra(EXTRA_FINISH_ACTIVITY, finish);
        return intent;
    }*/
}
