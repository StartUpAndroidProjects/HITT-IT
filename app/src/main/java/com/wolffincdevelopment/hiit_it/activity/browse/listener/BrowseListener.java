package com.wolffincdevelopment.hiit_it.activity.browse.listener;

import android.databinding.ViewDataBinding;

import com.wolffincdevelopment.hiit_it.activity.browse.viewmodel.ListItem;

/**
 * Created by Kyle Wolff on 2/4/17.
 */

public interface BrowseListener {
    void onItemClicked(ListItem listItem, ViewDataBinding viewDataBinding);
}
