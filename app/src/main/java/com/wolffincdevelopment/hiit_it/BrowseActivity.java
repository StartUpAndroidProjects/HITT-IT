package com.wolffincdevelopment.hiit_it;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by kylewolff on 6/4/2016.
 */
public class BrowseActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ContentResolver cr;
    private Cursor cur;
    private BrowseListAdapter adapter;
    private TrackData item;

    public ArrayList<TrackData> items;
    public ListView listView;

    public String artist, title, stream;
    public long mediaId, duration;

    @Override
    protected void onCreate(Bundle savedInstances)
    {
        super.onCreate(savedInstances);
        setContentView(R.layout.browse_layout);

        listView = (ListView) findViewById(R.id.browse_list_view);
        listView.setOnItemClickListener(this);
        //listView.setDivider(null);

        cr = getContentResolver();

        items = new ArrayList<>();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        findMusicFiles();
        adapter = new BrowseListAdapter(this, items);
        listView.setAdapter(adapter);
    }


    @Override
    public void onItemClick(AdapterView arg0, View arg1, int position, long arg3)
    {
        item = (TrackData) items.get(position);

        Intent intent = new Intent();

        if(!item.getSongName().isEmpty() && !item.getArtistName().isEmpty())
        {
            intent.putExtra("listItem", item);
            intent.putExtra("enabled", true);
            setResult(RESULT_OK, intent);
        }

        finish();
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
    }
}
