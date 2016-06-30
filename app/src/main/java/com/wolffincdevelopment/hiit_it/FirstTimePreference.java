package com.wolffincdevelopment.hiit_it;

/**
 * Created by kylewolff on 6/4/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

/**
 * @author Gabriele Porcelli
 *
 *         Example.
 *         FirstTimePreference prefFirstTime = new FirstTimePreference(getApplicationContext());
 *         if (prefFirstTime.runTheFirstNTimes("myKey" , 3)) {
 *         Toast.makeText(this,"Test myKey & coutdown: "+ prefFirstTime.getCountDown("myKey"),Toast.LENGTH_LONG).show(); }
 */

public class FirstTimePreference {

    private static final int INT_ERROR = -1;
    public static final String FIRST_TIME_PREFERENCES_KEY = "FirstKeyPreferences";
    public static final String FIRST_TIME_COUNTDOWN_KEY = "FirstCountdownKey";
    private final SharedPreferences firstTimePreferences;
    private Context context;

    public FirstTimePreference(Context context) {

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
