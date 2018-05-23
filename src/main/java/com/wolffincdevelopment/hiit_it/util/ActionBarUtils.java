package com.wolffincdevelopment.hiit_it.util;


import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Kyle Wolff on 12/11/16.
 */

public class ActionBarUtils {

    public static void showUpButton(AppCompatActivity activity) {

        final ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeAsUpIndicator(0);
        }
    }
}
