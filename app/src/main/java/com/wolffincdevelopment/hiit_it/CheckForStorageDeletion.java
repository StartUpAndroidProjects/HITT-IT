package com.wolffincdevelopment.hiit_it;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;

import com.karumi.dexter.Dexter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by kylewolff on 8/23/2016.
 */
public class CheckForStorageDeletion {

    private TrackDBAdapter trackDBAdapter;

    private Cursor cur;
    private ContentResolver cr;
    public String artist, title, stream;
    public long mediaId, duration;
    int permissionGranted;

    private ArrayList<String> items;
    private ArrayList<TrackData> usersList;

    public CheckForStorageDeletion(Context context)
    {
        trackDBAdapter = new TrackDBAdapter(context);
        permissionGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE );

        cr = context.getContentResolver();
    }

    public void getStorageAndDeletePlaylistItems()
    {
        if(permissionGranted == 0)
        {

            items = new ArrayList<>();

            trackDBAdapter.open();
            usersList = trackDBAdapter.getAllTracks();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

            cur = cr.query(uri, null, selection, null, sortOrder);

            int count = 0;

            if(cur != null)
            {
                count = cur.getCount();

                if(count > 0)
                {
                    while(cur.moveToNext())
                    {
                        stream = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));

                        for(TrackData trackData: usersList)
                        {
                            if(trackData.getStream().compareTo(stream) != 0)
                            {
                                items.add(stream);
                            }
                        }
                    }
                }
            }

            cur.close();

            if(items.size() == 0) {

                for (TrackData trackData : usersList) {
                    trackDBAdapter.deleteTrack(trackData);
                }

            }

            trackDBAdapter.close();
        }
    }

}
