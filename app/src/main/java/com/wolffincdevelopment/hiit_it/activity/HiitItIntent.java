package com.wolffincdevelopment.hiit_it.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.wolffincdevelopment.hiit_it.activity.browse.BrowseActivity;

/**
 * Created by Kyle Wolff on 2/4/17.
 */

public class HiitItIntent {

    public static Intent createSettingsPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        return intent;
    }

    public static Intent createBrowse(Context context) {
        return new Intent(context, BrowseActivity.class);
    }
}
