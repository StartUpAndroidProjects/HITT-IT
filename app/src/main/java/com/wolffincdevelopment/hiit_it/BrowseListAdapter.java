package com.wolffincdevelopment.hiit_it;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kylewolff on 6/4/2016.
 */
public class BrowseListAdapter extends ArrayAdapter<TrackData> {

    private Context context;
    private ArrayList<TrackData> items;

    @BindView(R.id.song_textview)
    TextView songText;

    @BindView(R.id.artist_textview)
    TextView artistText;

    public BrowseListAdapter(Context context, ArrayList<TrackData> items) {

        super(context, 0, items);

        this.context = context;
        this.items = items;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TrackData browseItem = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.browse_listview_row_normal, parent, false);
        }

        ButterKnife.bind(this, convertView);

        songText.setText(browseItem.getSongName());
        artistText.setText(browseItem.getArtistName());

        return convertView;
    }
}

