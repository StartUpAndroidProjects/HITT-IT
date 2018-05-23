package com.wolffincdevelopment.hiit_it.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.wolffincdevelopment.hiit_it.activity.addtrack.AddTrackActivity;
import com.wolffincdevelopment.hiit_it.activity.browse.BrowseActivity;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;

/**
 * Created by Kyle Wolff on 2/4/17.
 */

public class HiitItIntent {

    public static String EXTRA_FINISH_ACTIVITY = "com.wolffincdevelopment.hiit_it.Finish_Activity";
    public static String EXTRA_TRACK_DATA = "com.wolffincdevelopment.hiit_it.Track_Data";
    public static String EXTRA_EDIT_TRACK = "com.wolffincdevelopment.hiit_it.Edit_Track";
    public static String EXTRA_SHARED_ELEMENT_TRANSITION = "com.wolffincdevelopment.hiit_it.extra.SHARED_ELEMENT_TRANSITION";

    public static int ADD_TRACK_ACTIVITY_REQUEST_CODE = 100;
    public static int BROWSE_ACTIVITY_REQUEST_CODE = 101;


    public static Intent createSettingsPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        return intent;
    }

    public static Intent createAddTrack(Context context, TrackData trackData) {
        Intent intent = new Intent(context, AddTrackActivity.class);
        intent.putExtra(EXTRA_TRACK_DATA, trackData);
        return  intent;
    }

    public static Intent createAddTrackEdit(Context context, TrackData trackData) {
        Intent intent = createAddTrack(context, trackData);
        intent.putExtra(EXTRA_EDIT_TRACK, true);
        return  intent;
    }

    public static Intent createBrowse(Context context, boolean finish) {
        Intent intent = new Intent(context, BrowseActivity.class);
        intent.putExtra(EXTRA_FINISH_ACTIVITY, finish);
        return intent;
    }
}
