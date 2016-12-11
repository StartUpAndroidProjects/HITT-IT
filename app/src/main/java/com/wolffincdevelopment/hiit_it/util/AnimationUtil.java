package com.wolffincdevelopment.hiit_it.util;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by Kyle Wolff on 12/11/16.
 */

public class AnimationUtil {

    @TargetApi(21)
    public static void animateCircularReveal(final ViewGroup viewRoot, @ColorRes int color, Context context) {

        viewRoot.setVisibility(View.VISIBLE);

        int x = viewRoot.getWidth() / 2;
        int y = viewRoot.getHeight() / 2;

        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, 0, finalRadius);
        viewRoot.setBackgroundColor(ContextCompat.getColor(context, color));
        anim.setDuration(1000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }

    @TargetApi(21)
    public static void reverseAnimateCircularReveal(final ViewGroup viewRoot, @ColorRes int color, Context context) {

        viewRoot.setVisibility(View.VISIBLE);

        int x = viewRoot.getWidth() / 2;
        int y = viewRoot.getHeight() / 2;

        float finalRadius = (float) Math.hypot(viewRoot.getWidth(), viewRoot.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(viewRoot, x, y, finalRadius, 0);
        viewRoot.setBackgroundColor(ContextCompat.getColor(context, color));
        anim.setDuration(1000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }
}
