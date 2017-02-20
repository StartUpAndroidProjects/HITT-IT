package com.wolffincdevelopment.hiit_it.activity.home.listeners;

import android.databinding.ViewDataBinding;
import android.view.View;

import com.wolffincdevelopment.hiit_it.activity.home.viewmodel.HomeListItem;

/**
 * Created by Kyle Wolff on 1/29/17.
 */

public interface HomeListItemListener {
    void onItemClicked(HomeListItem listItem);
    void onOptionsClicked(View view, HomeListItem listItem, ViewDataBinding binding);
}
