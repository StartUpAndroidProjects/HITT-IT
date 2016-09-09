package com.wolffincdevelopment.hiit_it.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.wolffincdevelopment.hiit_it.adapter.BrowseListAdapter;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackData;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * Created by kylewolff on 6/4/2016.
 */
public class BrowseActivity extends AppCompatActivity
{
    private ContentResolver cr;
    private Cursor cur;
    private BrowseListAdapter adapter;
    private TrackData item;

    public ArrayList<TrackData> items;

    public String artist, title, stream;
    public long mediaId, duration;
    public int permissionGranted;

    private boolean permissonAlreadyChecked = false;

    Intent applicationSettingsIntent;

    AlertDialog.Builder dialogBuilder;

    @BindView(R.id.title_no_media)
    TextView titleNoMedia;

    @BindView(R.id.desc_no_permissions)
    TextView descNoPermissions;

    @BindView(R.id.browse_list_view)
    ListView listView;

    @OnClick(R.id.desc_no_permissions)
    protected void onDescPressed()
    {
        applicationSettingsIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        applicationSettingsIntent.setData(uri);
        applicationSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(applicationSettingsIntent);
    }

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

        dialogBuilder = new AlertDialog.Builder(this);

        getPermission();

        applicationSettingsIntent = new Intent();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        permissionGranted = ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE );

        if(permissionGranted == 0 && items != null && items.isEmpty())
        {
            permissonAlreadyChecked = true;
            findMusicFiles();
        }
    }

    private void getPermission()
    {
        Dexter.checkPermission( new PermissionListener()
        {
            @Override
            public void onPermissionGranted( PermissionGrantedResponse response )
            {
                if(!permissonAlreadyChecked && items != null && items.isEmpty())
                {
                    findMusicFiles();
                }
            }

            @Override
            public void onPermissionDenied( PermissionDeniedResponse response )
            {
                titleNoMedia.setVisibility(View.VISIBLE);
                descNoPermissions.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPermissionRationaleShouldBeShown( PermissionRequest permission, PermissionToken token )
            {
                token.continuePermissionRequest();
            }
        }, android.Manifest.permission.READ_EXTERNAL_STORAGE );
    }

    public void findMusicFiles()
    {

        titleNoMedia.setVisibility(View.INVISIBLE);
        descNoPermissions.setVisibility(View.INVISIBLE);

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

                    if(duration <= 3540000)
                    {
                        items.add(new TrackData(artist, title, stream, duration, mediaId));
                    }
                }
            }
        }

        cur.close();

        if(items.isEmpty()) {
            titleNoMedia.setVisibility(View.VISIBLE);
        }else {
            titleNoMedia.setVisibility(View.INVISIBLE);
        }

        adapter = new BrowseListAdapter(this, items);
        listView.setAdapter(adapter);
    }
}
