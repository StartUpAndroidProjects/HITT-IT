package com.wolffincdevelopment.hiit_it;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kylewolff on 6/2/2016.
 */
public class BaseAdapter extends RecyclerView.Adapter<BaseAdapter.MyViewHolder> {

    private TrackDBAdapter trackDBAdapter;
    private MusicService musicService;
    private BaseActivity baseActivity;
    private MyViewHolder myViewHolder;
    public List<TrackData> trackData;
    public ArrayList<TrackData> trackDataPositions;

    /*
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * The custom row layout components are here and this where we can add Listeners etc...
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView song_Artist, startTime, stopTime;
        public ImageButton options;
        public ImageView sound;
        public String trackId;

        public MyViewHolder(View view) {

            super(view);
            song_Artist = (TextView) view.findViewById(R.id.trackSongTextView);
            startTime = (TextView) view.findViewById(R.id.startTimeTextView);
            stopTime = (TextView) view.findViewById(R.id.stopTimeTextView);
            options = (ImageButton) view.findViewById(R.id.optionsIcon);
            sound = (ImageView) view.findViewById(R.id.soundIconImageView);

            trackDBAdapter = new TrackDBAdapter(view.getContext());

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Snackbar snackbar = Snackbar.make(v, song_Artist.getText().toString(), Snackbar.LENGTH_LONG);
                    snackbar.show();

                    return false;
                }
            });
        }
    }

    // The method to display the popUp menu
    private void showMenu(View v, String id) {

        final long itemId = Long.parseLong(id);

        IconizedMenu popup = new IconizedMenu(v.getContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch(item.getTitle().toString()) {

                    case "Move Up":
                        trackDBAdapter.open();
                        trackDBAdapter.reorderItem(itemId, "Move Up");
                        trackData = trackDBAdapter.getAllTracks();
                        trackDBAdapter.close();
                        refresh(trackData);
                        break;

                    case "Delete":
                        trackDBAdapter.open();
                        trackDBAdapter.deleteTrack(itemId);
                        trackData = trackDBAdapter.getAllTracks();
                        trackDBAdapter.close();
                        refresh(trackData);
                        musicService.stop();
                        break;
                }

                return false;
            }
        });
    }

    // Constructor for this class
    public BaseAdapter(List<TrackData> trackData) {

        this.trackData = trackData;
    }

    public void refresh(List<TrackData> trackData) {
        this.trackData = trackData;
        notifyDataSetChanged();
    }

    public void setBaseActivity(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.base_activity_row, parent, false);

        // The class above that has our components
        return new MyViewHolder(itemView);
    }

    // Updates the Recycler View with the data we pass it.
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        trackDataPositions = new ArrayList<>();
        trackDataPositions.add(trackData.get(position));

        final TrackData trackDataList = trackData.get(position);
        holder.song_Artist.setText(trackDataList.getSongAndArtist());
        holder.startTime.setText(trackDataList.getStartTime());
        holder.stopTime.setText(trackDataList.getStopTime());

        // Sets onClick for all option buttons
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMenu(v, trackDataList.getId());
            }
        });
    }

    @Override
    public int getItemCount() {

        return trackData.size();
    }
}
