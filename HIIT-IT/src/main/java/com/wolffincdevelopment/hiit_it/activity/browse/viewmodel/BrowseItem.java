package com.wolffincdevelopment.hiit_it.activity.browse.viewmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.databinding.Bindable;
import android.databinding.ViewDataBinding;
import android.view.View;

import com.wolffincdevelopment.hiit_it.BR;
import com.wolffincdevelopment.hiit_it.BaseViewModel;
import com.wolffincdevelopment.hiit_it.LifeCycle;
import com.wolffincdevelopment.hiit_it.activity.browse.listener.BrowseListener;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.FileStorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle Wolff on 2/4/17.
 */

public class BrowseItem extends BaseViewModel implements BrowseListener {

    private final List<TrackData> trackDataList;
    private Context context;
    private ContentResolver cr;

    private boolean showError;
    private boolean isPermitted;

    public BrowseItem(Context context) {
        super();

        cr = context.getContentResolver();

        this.context = context;
        trackDataList = new ArrayList<>();
        showError(false);
    }

    public interface BrowseItemCallback extends LifeCycle.LoadingView {
        void onItemClicked(ListItem listItem, ViewDataBinding viewDataBinding);

        void onVerifyPermissions();

        void onDataReady(List<TrackData> trackDataList);
    }

    @Override
    protected BrowseItemCallback getViewCallback() {
        return (BrowseItemCallback) super.getViewCallback();
    }

    public void setPermitted(boolean isPermitted) {
        this.isPermitted = isPermitted;
    }


    @Override
    protected void refreshData() {

        if (isPermitted) {

            showError(false);

            trackDataList.clear();
            trackDataList.addAll(FileStorageUtil.getMusicFilesViaMediaStore(cr, FileStorageUtil.EXTERNAL_CONTENT_URI));
            trackDataList.addAll(FileStorageUtil.getMusicFilesViaMediaStore(cr, FileStorageUtil.INTERNAL_CONTENT_URI));

            if (trackDataList.isEmpty()) {
                state = NetworkState.SHOULD_REFRESH;
                showError(true);
            } else {

                state = NetworkState.IDLE;

                if (hasViewCallback()) {
                    getViewCallback().onDataReady(trackDataList);
                }

                showError(false);
            }
        } else {
            showError(true);
            state = NetworkState.SHOULD_REFRESH;
        }

    }

    public void onErrorClicked() {
        if (hasViewCallback()) {
            getViewCallback().onVerifyPermissions();
        }
    }

    private void showError(boolean showError) {
        this.showError = showError;
        notifyPropertyChanged(BR.showError);
    }

    @Bindable
    public int getShowError() {
        return showError ? View.VISIBLE : View.GONE;
    }

    @Override
    public void onItemClicked(ListItem listItem, ViewDataBinding viewDataBinding) {
        if (hasViewCallback()) {
            getViewCallback().onItemClicked(listItem, viewDataBinding);
        }
    }
}
