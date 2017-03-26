package com.wolffincdevelopment.hiit_it;

import android.databinding.BaseObservable;
import android.support.annotation.NonNull;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public abstract class BaseViewModel extends BaseObservable implements LifeCycle.ViewModel {

    protected NetworkState state;

    private LifeCycle.View viewCallback;

    protected enum NetworkState {
        SHOULD_REFRESH,
        IDLE,
        REFRESHING_DATA,
        SAVING_DATA,
        FINISH_ACTIVITY;

        public boolean shouldRefresh() {
            return this == SHOULD_REFRESH;
        }
    }

    public BaseViewModel() {
        state = NetworkState.SHOULD_REFRESH;
    }

    /**
     * Called during {@link android.app.Activity#onResume()}. This is where
     * any refresh logic should reside.
     */
    @Override
    public void onViewResumed() {
        if (shouldRefresh()) {
            refreshData();
        }
    }

    /**
     * Called during {@link android.app.Activity#onStart()} when the View
     * gets attached to the ViewModel
     *
     * @param viewCallback the callback methods the view needs in order for the view model to update
     *                     it
     */
    @Override
    public void onViewAttached(@NonNull LifeCycle.View viewCallback) {
        this.viewCallback = viewCallback;
    }

    /**
     * Called during {@link android.app.Activity#onDestroy()} when the View has been
     * destroyed.
     */
    @Override
    public void onViewDetached() {
        viewCallback = null;
    }

    /**
     * @return {@code true} if there is a view callback, {@code false} otherwise
     */
    protected boolean hasViewCallback() {
        return viewCallback != null;
    }

    /**
     * @return the view callback
     */
    protected LifeCycle.View getViewCallback() {
        return viewCallback;
    }

    /**
     * @return {@code true} if the ViewModel should refresh its data
     */
    protected boolean shouldRefresh() {
        return state.shouldRefresh();
    }

    /**
     * Method to refresh the data. Gets called from {@link #onViewResumed()}
     */
    protected abstract void refreshData();
}