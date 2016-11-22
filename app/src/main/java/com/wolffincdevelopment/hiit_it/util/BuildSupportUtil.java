package com.wolffincdevelopment.hiit_it.util;

import android.os.Build;

/**
 * Created by Kyle Wolff on 11/22/16.
 */

public class BuildSupportUtil {

    public static boolean isLollipopAndUp() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
