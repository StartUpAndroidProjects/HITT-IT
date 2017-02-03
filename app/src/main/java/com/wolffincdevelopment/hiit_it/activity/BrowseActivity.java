/*
package com.wolffincdevelopment.hiit_it.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wolffincdevelopment.hiit_it.HiitItIntents;
import com.wolffincdevelopment.hiit_it.listeners.BrowseTrackListener;
import com.wolffincdevelopment.hiit_it.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ActionBarUtils;
import com.wolffincdevelopment.hiit_it.util.AnimationUtil;
import com.wolffincdevelopment.hiit_it.util.BuildSupportUtil;
import com.wolffincdevelopment.hiit_it.viewmodel.TrackItem;
import com.wolffincdevelopment.hiit_it.adapter.BrowseListAdapter;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.util.DialogBuilder;
import com.wolffincdevelopment.hiit_it.util.PermissionUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

*/
/**
 * Created by kylewolff on 6/4/2016.
 *//*

public class BrowseActivity extends AppCompatActivity implements BrowseTrackListener {
    private ContentResolver cr;
    private Cursor cur;
    private BrowseListAdapter adapter;
    private TrackData item;

    public ArrayList<TrackItem> items;

    public String artist, title, stream;
    public long mediaId, duration;

    public Snackbar snackbar;

    public Intent applicationSettingsIntent;

    public PermissionUtil permissionUtil;

    @BindView(R.id.browse_layout)
    RelativeLayout browseLayout;

    @BindView(R.id.title_no_media)
    TextView titleNoMedia;

    @BindView(R.id.desc_no_permissions)
    TextView descNoPermissions;

    @BindView(R.id.browse_list_view)
    RecyclerView listView;

    @OnClick(R.id.desc_no_permissions)
    protected void onDescPressed() {
        applicationSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        applicationSettingsIntent.setData(uri);
        startActivity(applicationSettingsIntent);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);

        setContentView(R.layout.browse_layout);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        ActionBarUtils.showUpButton(this);

        cr = getContentResolver();
        items = new ArrayList<>();

        permissionUtil = new PermissionUtil();

        if (permissionUtil.checkReadStoragePermission(this)) {
            findMusicFiles();
        }

        listView.setLayoutManager(new LinearLayoutManager(this));

        browseLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {

                if(BuildSupportUtil.isLollipopAndUp()) {
                    AnimationUtil.animateCircularReveal(browseLayout, android.R.color.background_light, browseLayout.getContext());
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        View v = findViewById(R.id.browse_layout);
        snackbar = Snackbar.make(v, "Cannot access media content without permission", Snackbar.LENGTH_LONG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            AnimationUtil.reverseAnimateCircularReveal(browseLayout, android.R.color.background_light, browseLayout.getContext());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        final Activity activity = this;

        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findMusicFiles();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialogOK("Read Storage Permission is required for this app. Try again?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            permissionUtil.checkReadStoragePermission(activity);

                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            showSettingsPrompt();

                                            break;
                                    }
                                }
                            });
                } else {
                    showSettingsPrompt();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showSettingsPrompt() {
        snackbar.show();
        titleNoMedia.setVisibility(View.VISIBLE);
        descNoPermissions.setVisibility(View.VISIBLE);
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        DialogBuilder dialogBuilder = new DialogBuilder(message, this);
        dialogBuilder.setButtons("Ok", "Cancel", okListener);
        dialogBuilder.create();
        dialogBuilder.show();
    }

    public void findMusicFiles() {

        ArrayList<TrackItem> unknownItems = new ArrayList<>();

        titleNoMedia.setVisibility(View.INVISIBLE);
        descNoPermissions.setVisibility(View.INVISIBLE);

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.ARTIST + " ASC";

        cur = cr.query(uri, null, selection, null, sortOrder);

        int count = 0;

        if (cur != null) {
            count = cur.getCount();

            if (count > 0) {
                while (cur.moveToNext()) {
                    artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    duration = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    stream = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    mediaId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID));

                    if (artist.contains("unknown")) {
                        artist = "Unknown";
                    }

                    if (duration <= 3540000) {
                        items.add(new TrackItem(new TrackData(artist, title, stream, duration, mediaId)));
                    }
                }
            }
        }

        cur.close();

        // Reorder the items because the query returns unknown tracks first
        Collections.sort(items, new Comparator<TrackItem>() {
            @Override
            public int compare(TrackItem item, TrackItem t1) {
                return item.getArtistName().compareToIgnoreCase(t1.getArtistName());
            }
        });

        if (items.isEmpty()) {
            titleNoMedia.setVisibility(View.VISIBLE);
        } else {
            titleNoMedia.setVisibility(View.INVISIBLE);
        }

        adapter = new BrowseListAdapter(items, this);
        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClicked(TrackItem item) {

        TrackData data = item.getTrackData();

        if (data != null) {

            if(getIntent().getBooleanExtra(HiitItIntents.EXTRA_FINISH_ACTIVITY, false)) {
                setResult(RESULT_OK, new Intent().putExtra(HiitItIntents.EXTRA_ITEM, data));
                finish();
            } else {
                //startActivity(HiitItIntents.createAddTrackIntent(this, data));
            }
        }
    }
}
*/
