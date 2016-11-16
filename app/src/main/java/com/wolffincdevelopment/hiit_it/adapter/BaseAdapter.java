package com.wolffincdevelopment.hiit_it.adapter;

import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.wolffincdevelopment.hiit_it.HiitBus;
import com.wolffincdevelopment.hiit_it.IconizedMenu;
import com.wolffincdevelopment.hiit_it.MusicListener;
import com.wolffincdevelopment.hiit_it.TrackData;
import com.wolffincdevelopment.hiit_it.TrackItem;
import com.wolffincdevelopment.hiit_it.handler.MessageHandler;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackDBAdapter;
import com.wolffincdevelopment.hiit_it.What;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.wolffincdevelopment.hiit_it.util.SharedPreferencesUtil;

/**
 * Created by kylewolff on 6/2/2016.
 */
public class BaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_VIEW = 1;

    private TrackDBAdapter trackDBAdapter;
    private TrackItem currentSong;
    private SharedPreferencesUtil sharedPreferencesUtil = SharedPreferencesUtil.getInstance();
    private MessageHandler handler;
    private What whatInteger;
    private Message refreshMsg, playThisSong, pauseResumeSong;
    private List<TrackItem> trackItems;
    private ArrayList<TrackItem> trackDataPositions;
    private boolean setSoundIconVisible = false;
    private boolean firstUpdateSound = false;
    private boolean paused = false;
    private boolean menuItemClicked = false;
    private int currentTrackPlaying, previousTrackPlayed = -1, position;
    public Bundle data;
    private MenuInflater inflater;
    private IconizedMenu firstMenuSelected;
    private HiitBus bus;

    /*
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * The custom row layout components are here and this where we can add Listeners etc...
     */
    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.track_song_textview)
        TextView trackSongTextView;

        @BindView(R.id.start_time_textview)
        TextView startTime;

        @BindView(R.id.stop_time_text_view)
        TextView stopTime;

        @BindView(R.id.options_icon)
        ImageButton options;

        @BindView(R.id.sound_icon_imageview)
        ImageView sound;

        public String trackId;

        public MyViewHolder(View view)
        {
            super(view);
            ButterKnife.bind(this, view);

            trackDBAdapter = new TrackDBAdapter(view.getContext());

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Snackbar snackbar = Snackbar.make(v, trackSongTextView.getText().toString(), Snackbar.LENGTH_LONG);
                    snackbar.show();

                    return false;
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });

            bus = HiitBus.getInstance();
            bus.register(this);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.off_or_on)
        TextView oNorOffTextView;

        @BindView(R.id.replay_icon)
        ImageView replayIcon;

        public FooterViewHolder(View view)
        {
            super(view);
            ButterKnife.bind(this, view);

            if(!sharedPreferencesUtil.getRepeat(view.getContext()))
            {
                oNorOffTextView.setText("OFF");
            }
            else
            {
                oNorOffTextView.setText("ON");
                replayIcon.setImageResource(R.drawable.ic_repeat_deep_orange_48dp);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(!sharedPreferencesUtil.getRepeat(v.getContext()))
                    {
                        sharedPreferencesUtil.setRepeat(v.getContext(), true);
                        oNorOffTextView.setText("ON");
                        replayIcon.setImageResource(R.drawable.ic_repeat_deep_orange_48dp);
                    }
                    else
                    {
                        sharedPreferencesUtil.setRepeat(v.getContext(), false);
                        oNorOffTextView.setText("OFF");
                        replayIcon.setImageResource(R.drawable.ic_repeat_black_48dp);
                    }
                }
            });


        }
    }

    // The method to display the popUp menu
    private void showMenu(View v, final TrackItem trackItem)
    {
        final IconizedMenu popup = new IconizedMenu(v.getContext(), v);
        inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
        popup.show();

        if(firstMenuSelected != null && firstMenuSelected.isShowing() && popup.isShowing())
        {
            firstMenuSelected.dismiss();
        }

        firstMenuSelected = popup;

        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch(item.getTitle().toString())
                {
                    case "Move Up":

                        menuItemClicked = true;
                        trackDBAdapter.open();
                        trackDBAdapter.reorderItem(trackItem, "Move Up");
                        trackItems = trackDBAdapter.getAllTracks();
                        trackDBAdapter.close();
                        refresh(trackItems);

                        popup.dismiss();

                        break;

                    case "Move Down":

                        menuItemClicked = true;
                        trackDBAdapter.open();
                        trackDBAdapter.reorderItem(trackItem, "Move Down");
                        trackItems = trackDBAdapter.getAllTracks();
                        trackDBAdapter.close();
                        refresh(trackItems);

                        popup.dismiss();

                        break;

                    case "Delete":

                        menuItemClicked = true;
                        trackDBAdapter.open();
                        trackDBAdapter.deleteTrack(trackItem);
                        trackItems = trackDBAdapter.getAllTracks();
                        trackDBAdapter.close();
                        refresh(trackItems);

                        break;

                }

                return false;
            }
        });
    }

    // Constructor for this class
    public BaseAdapter(List<TrackItem> trackItems) {

        this.trackItems = trackItems;

        whatInteger = new What();
        data = new Bundle();
    }

    public void refresh(List<TrackItem> trackItems) {

        this.trackItems = trackItems;
        notifyDataSetChanged();

        if(menuItemClicked) {

            menuItemClicked = false;
        }

    }

    public void updateSoundIcon(final String id)
    {
        if(previousTrackPlayed != -1){
            notifyItemChanged(previousTrackPlayed);
        }


        for(TrackItem td: trackItems) {

            if(td.getId() == id) {

                position = trackItems.indexOf(td);
                currentTrackPlaying = position;

                setSoundIconVisible = true;
                firstUpdateSound = true;

            }
        }

        notifyItemChanged(position);

    }

    public void setSoundIconInvisible(String id)
    {
        for(TrackItem td: trackItems) {

            if(td.getId() == id) {

                position = trackItems.indexOf(td);

                setSoundIconVisible = false;
                firstUpdateSound = false;

            }
        }

        notifyItemChanged(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        FooterViewHolder footerViewHolder;
        MyViewHolder myViewHolder;

        if (viewType == FOOTER_VIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.base_activity_foot_row, parent, false);

            footerViewHolder = new FooterViewHolder(view);

            return footerViewHolder;

        } else {

            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.base_activity_row, parent, false);

            myViewHolder = new MyViewHolder(view);

            return myViewHolder;

        }
    }

    // Updates the Recycler View with the data we pass it.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if(holder instanceof MyViewHolder) {

            trackDataPositions = new ArrayList<>();
            trackDataPositions.add(this.trackItems.get(position));

            final TrackItem trackItem = this.trackItems.get(position);

            ((MyViewHolder) holder).trackSongTextView.setText(trackItem.getSongAndArtist());
            ((MyViewHolder) holder).startTime.setText(trackItem.getStartTime());
            ((MyViewHolder) holder).stopTime.setText(trackItem.getStopTime());

            // Sets onClick for all option buttons
            ((MyViewHolder) holder).options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showMenu(v, trackItem);
                }
            });

            ((MyViewHolder) holder).trackId = trackItem.getId();

            if(firstUpdateSound) {

                if (position == currentTrackPlaying) {
                    setSoundIconVisible = true;
                } else {
                    setSoundIconVisible = false;
                }

            }

                if(setSoundIconVisible) {

                    ((MyViewHolder) holder).sound.setVisibility(View.VISIBLE);
                    previousTrackPlayed = position;

                    if(paused) {
                        ((MyViewHolder) holder).sound.setImageResource(R.drawable.ic_volume_up_black_48dp);
                    } else {
                        ((MyViewHolder) holder).sound.setImageResource(R.drawable.ic_volume_up_deep_orange_48dp);
                    }

                } else {
                    ((MyViewHolder) holder).sound.setVisibility(View.INVISIBLE);
                }
            }
        }

    @Override
    public int getItemViewType(int position) {

        if (position == trackItems.size()) {
            // This is where we'll add footer.
            return FOOTER_VIEW;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {

        int items = 0;

        if (!trackItems.isEmpty()) {
            items = trackItems.size() + 1;
        }

        return items;
    }


    @Subscribe
    public void musicListener(MusicListener event) {
        currentSong = event.trackItem;
        updateSoundIcon(event.trackItem.getId());
        this.paused = event.paused;
    }

}
