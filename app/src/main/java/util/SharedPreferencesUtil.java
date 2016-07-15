package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by mitchross on 6/30/16.
 */

public class SharedPreferencesUtil
{
    public static final String REPEAT_PREFERENCE_KEY = "RepeatOnOrOff";
    private SharedPreferences sharedPreferences;
    private static SharedPreferencesUtil sharedPreferencesUtil;

    private SharedPreferencesUtil(){}


    public static SharedPreferencesUtil getInstance()
    {
        if(sharedPreferencesUtil == null) {
            sharedPreferencesUtil = new SharedPreferencesUtil();
        }

        return sharedPreferencesUtil;
    }

    public void setRepeat(Context context, boolean onOroff)
    {
        sharedPreferences = context.getSharedPreferences(REPEAT_PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(REPEAT_PREFERENCE_KEY, onOroff);
        editor.apply();
    }

    public boolean getRepeat(Context context)
    {
        sharedPreferences = context.getSharedPreferences(REPEAT_PREFERENCE_KEY, Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean(REPEAT_PREFERENCE_KEY, false);
    }
}
