package com.wolffincdevelopment.hiit_it.util;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.transition.Transition;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.wolffincdevelopment.hiit_it.activity.HiitItIntent;

/**
 * Created by Kyle Wolff on 2/9/17.
 */
public class ActivityTransitionUtil {

    /**
     * @return whether shared element transitions are supported
     */
    public static boolean supportsTransitions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * The Lollipop 5.0 shared element transition framework is still very green and has some quirks.
     * On Lollipop 5.0, if you have transitions with a container and views in those container, you should only animate
     * just the container or just the individual views, not both. The framework will handle transitioning all
     * the views in the container by itself, but it might not always look the best. Lollipop 5.1 and above seem
     * to handle doing the animation with the container and individual views.
     *
     * @return
     */
    public static boolean includeContainer() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static void startActivity(Activity activity, Intent intent, View transitionView) {
        if (supportsTransitions()) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(activity, transitionView, transitionView.getTransitionName());
            intent.putExtra(HiitItIntent.EXTRA_SHARED_ELEMENT_TRANSITION, true);
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public static void startActivity( Activity activity, Intent intent, List<View> transitionViews) {
        if (supportsTransitions()) {
            ActivityOptionsCompat options = getActivityOptionsCompat(activity, transitionViews);
            intent.putExtra(HiitItIntent.EXTRA_SHARED_ELEMENT_TRANSITION, true);
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public static void startActivity(Fragment fragment, Intent intent, List<View> transitionViews) {
        if (supportsTransitions() && fragment.isAdded()) {
            ActivityOptionsCompat options = getActivityOptionsCompat(fragment.getActivity(), transitionViews);
            intent.putExtra(HiitItIntent.EXTRA_SHARED_ELEMENT_TRANSITION, true);
            fragment.startActivity(intent, options.toBundle());
        } else {
            fragment.startActivity(intent);
        }
    }

    public static void startActivityForResult(Activity activity, Intent intent, int requestCode, List<View> transitionViews) {
        if (supportsTransitions()) {
            ActivityOptionsCompat options = getActivityOptionsCompat(activity, transitionViews);
            intent.putExtra(HiitItIntent.EXTRA_SHARED_ELEMENT_TRANSITION, true);
            activity.startActivityForResult(intent, requestCode, options.toBundle());
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static void startActivityForResult(Fragment fragment, Intent intent, int requestCode, List<View> transitionViews) {
        if (supportsTransitions() && fragment.isAdded()) {
            ActivityOptionsCompat options = getActivityOptionsCompat(fragment.getActivity(), transitionViews);
            intent.putExtra(HiitItIntent.EXTRA_SHARED_ELEMENT_TRANSITION, true);
            fragment.startActivityForResult(intent, requestCode, options.toBundle());
        } else {
            fragment.startActivityForResult(intent, requestCode);
        }
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static ActivityOptionsCompat getActivityOptionsCompat(Activity activity, List<View> transitionViews) {
        List<Pair<View, String>> transitionViewPairs = new ArrayList<>(transitionViews.size());
        for (View view : transitionViews) {
            transitionViewPairs.add(Pair.create(view, view.getTransitionName()));
        }
        Pair[] pairsArray = transitionViewPairs.toArray(new Pair[transitionViewPairs.size()]);
        //noinspection unchecked
        return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pairsArray);
    }


    /**
     * Basic implementation of Transition Listener so that subclasses only have to override the
     * methods they need.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static class SimpleTransitionListener implements Transition.TransitionListener {

        @Override
        public void onTransitionStart(Transition transition) {
            //no-op
        }

        @Override
        public void onTransitionEnd(Transition transition) {
            //no-op
        }

        @Override
        public void onTransitionCancel(Transition transition) {
            //no-op
        }

        @Override
        public void onTransitionPause(Transition transition) {
            //no-op
        }

        @Override
        public void onTransitionResume(Transition transition) {
            //no-op
        }
    }
}
