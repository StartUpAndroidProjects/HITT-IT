package com.wolffincdevelopment.hiit_it;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Created by kylewolff on 6/4/2016.
 */
public class BrowseActivity extends AppCompatActivity {

    private ContentResolver cr;
    private Cursor cur;
    private BrowseListAdapter adapter;
    private TrackData item;

    public ArrayList<TrackData> items;

    public String artist, title, stream;
    public long mediaId, duration;

    @BindView(R.id.browse_list_view)
    ListView listView;

    @OnItemClick(R.id.browse_list_view)
    protected void onItemClick(int position)
    {
        item = (TrackData) items.get(position);

        Intent intent = new Intent();

        if(!item.getSongName().isEmpty() && !item.getArtistName().isEmpty())
        {
            intent.putExtra("listItem", item);
            intent.putExtra("enabled", true);
            setResult(RESULT_OK, intent);
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstances)
    {
        super.onCreate(savedInstances);

        setContentView(R.layout.browse_layout);
        ButterKnife.bind(this);

        cr = getContentResolver();
        items = new ArrayList<>();

        getPermission();
    }

    private void getPermission()
    {
        Dexter.checkPermission( new PermissionListener()
        {
            @Override
            public void onPermissionGranted( PermissionGrantedResponse response )
            {
                findMusicFiles();
            }

            @Override
            public void onPermissionDenied( PermissionDeniedResponse response )
            {

            }

            @Override
            public void onPermissionRationaleShouldBeShown( PermissionRequest permission, PermissionToken token )
            {

            }
        }, android.Manifest.permission.READ_EXTERNAL_STORAGE );
    }

    public void findMusicFiles()
    {

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
                    artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    duration = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    stream = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    mediaId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID));

                    if(artist.contains("unknown"))
                    {
                        artist = "Unknown";
                    }

                    items.add(new TrackData(artist, title, stream, duration, mediaId));
                }

            }
        }

        cur.close();

        adapter = new BrowseListAdapter(this, items);
        listView.setAdapter(adapter);
    }
}
