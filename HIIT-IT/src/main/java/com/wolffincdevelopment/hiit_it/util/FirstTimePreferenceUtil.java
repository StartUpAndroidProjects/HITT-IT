package com.wolffincdevelopment.hiit_it.util;

/**
 * Created by kylewolff on 6/4/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;

import com.wolffincdevelopment.hiit_it.R;

/**
 * @author Gabriele Porcelli
 *
 *         Example.
 *         FirstTimePreferenceUtil prefFirstTime = new FirstTimePreferenceUtil(getApplicationContext());
 *         if (prefFirstTime.runTheFirstNTimes("myKey" , 3)) {
 *         Toast.makeText(this,"Test myKey & coutdown: "+ prefFirstTime.getCountDown("myKey"),Toast.LENGTH_LONG).show(); }
 */

public class FirstTimePreferenceUtil {

    private static final int INT_ERROR = -1;
    public static final String FIRST_TIME_PREFERENCES_KEY = "FirstKeyPreferences";
    private final SharedPreferences firstTimePreferences;
    private Context context;

    public FirstTimePreferenceUtil(Context context) {

        this.context = context;

        firstTimePreferences = context.getSharedPreferences(
                FIRST_TIME_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public void runCheckFirstTime(String preference) {

        if(!firstTimePreferences.contains(context.getString(R.string.firstTimeFabPressed))) {
            SharedPreferences.Editor editor = firstTimePreferences.edit();
            editor.putBoolean(preference, true);
            editor.commit();
        }
    }
}
