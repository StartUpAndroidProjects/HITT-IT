package com.wolffincdevelopment.hiit_it.activity.browse.viewmodel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.databinding.Bindable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.wolffincdevelopment.hiit_it.BR;
import com.wolffincdevelopment.hiit_it.BaseViewModel;
import com.wolffincdevelopment.hiit_it.LifeCycle;
import com.wolffincdevelopment.hiit_it.activity.browse.listener.BrowseListener;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Kyle Wolff on 2/4/17.
 */

public class BrowseItem extends BaseViewModel implements BrowseListener {

    private List<TrackData> trackDataList;
    private Context context;
    private Activity activity;
    private ContentResolver cr;
    private Cursor cur;

    private boolean showError;
    private boolean isPermitted;

    public BrowseItem(Context context, Activity activity) {
        super();

        cr = context.getContentResolver();

        this.context = context;
        this.activity = activity;
        trackDataList = new ArrayList<>();
        showError(false);
    }

    public interface BrowseItemCallback extends LifeCycle.LoadingView {
        void onItemClicked(ListItem listItem);

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

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.ARTIST + " ASC";

            cur = cr.query(uri, null, selection, null, sortOrder);

            int count = 0;

            if (cur != null) {
                count = cur.getCount();

                if (count > 0) {

                    while (cur.moveToNext()) {

                        String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        long duration = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        String stream = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                        long mediaId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID));

                        if (artist.contains("unknown")) {
                            artist = "Unknown";
                        }

                        if (duration <= 3540000) {
                            trackDataList.add(new TrackData(artist, title, stream, duration, mediaId));
                        }
                    }
                }

                cur.close();
            }

            // Reorder the items because the query returns unknown tracks first
            Collections.sort(trackDataList, new Comparator<TrackData>() {
                @Override
                public int compare(TrackData trackData, TrackData trackData1) {
                    return trackData.getArtist().compareToIgnoreCase(trackData1.getArtist());
                }
            });

            if (trackDataList.isEmpty()) {
                state = NetworkState.NO_DATA;
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
            state = NetworkState.NO_DATA;
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
    public void onItemClicked(ListItem listItem) {
        if (hasViewCallback()) {
            getViewCallback().onItemClicked(listItem);
        }
    }
}
