package com.wolffincdevelopment.hiit_it.util;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Kyle Wolff on 11/27/16.
 */

/**
 * Support methods for the InputMethodManger
 *
 * @see InputMethodManager
 */
public class InputManagerUtil {

    public static void dismissKeyboard(View view, @NonNull InputMethodManager inputMethodManager) {

        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
