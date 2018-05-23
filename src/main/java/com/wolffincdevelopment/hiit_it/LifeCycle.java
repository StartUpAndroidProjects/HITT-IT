package com.wolffincdevelopment.hiit_it;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public interface LifeCycle {

    interface View {

    }

    interface LoadingView extends View {
        void onShowProgressView(boolean isVisible, @StringRes int textResourceId);
    }

    interface ViewModel {

        void onViewResumed();

        void onViewAttached(@NonNull LifeCycle.View viewCallback);

        void onViewDetached();
    }
}