package com.wolffincdevelopment.hiit_it.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Kyle Wolff on 11/22/16.
 */

public class LayoutDimensionUtil {

    public LayoutDimensionUtil() {

    }

    public static int dpToPx(float dp, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }
    public static int pxToDp(float px, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                px, context.getResources().getDisplayMetrics());
    }
}
