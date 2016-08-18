package com.wolffincdevelopment.hiit_it;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import util.SharedPreferencesUtil;

/**
 * Created by kylewolff on 6/2/2016.
 */
public class BaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_VIEW = 1;

    private TrackDBAdapter trackDBAdapter;
    private TrackData currentSong;
    private SharedPreferencesUtil sharedPreferencesUtil = SharedPreferencesUtil.getInstance();
    private MyViewHolder myViewHolder;
    private MessageHandler handler;
    private What whatInteger;
    private Message refreshMsg, playThisSong, pauseResumeSong, setCurrentSong;
    public List<TrackData> trackData;
    public ArrayList<TrackData> trackDataPositions;
    public boolean setSoundIconVisible = false;
    public boolean firstUpdateSound = false;
    public boolean activeIcon = false;
    public boolean menuItemClicked = false;
    public int currentTrackPlaying, previousTrackPlayed = -1, position;
    public Bundle data;

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

        public MyViewHolder(View view) {

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

                    if(currentSong == null) {

                        data.clear();
                        data.putSerializable("id", trackId);
                        playThisSong = handler.createMessage(playThisSong, whatInteger.getPlayThisSong(), data);
                        handler.sendMessage(playThisSong);

                    } else if(currentSong.getId().compareTo(trackId) != 0) {

                        data.clear();
                        data.putSerializable("id", trackId);
                        playThisSong = handler.createMessage(playThisSong, whatInteger.getPlayThisSong(), data);
                        handler.sendMessage(playThisSong);

                    } else {

                        Bundle data = new Bundle();
                        data.clear();
                        data.putSerializable("id", trackId);
                        pauseResumeSong = handler.createMessage(playThisSong, whatInteger.pauseResumeCurrentSong(), data);
                        handler.sendMessage(pauseResumeSong);
                    }
                }
            });

        }

    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.off_or_on)
        TextView oNorOffTextView;

        @BindView(R.id.replay_icon)
        ImageView replayIcon;

        public FooterViewHolder(View view) {

            super(view);
            ButterKnife.bind(this, view);

            if(sharedPreferencesUtil.getRepeat(view.getContext()) == false) {
                oNorOffTextView.setText("OFF");
            } else {
                oNorOffTextView.setText("ON");
                replayIcon.setImageResource(R.drawable.ic_repeat_deep_orange_48dp);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(sharedPreferencesUtil.getRepeat(v.getContext()) == false) {
                        sharedPreferencesUtil.setRepeat(v.getContext(), true);
                        oNorOffTextView.setText("ON");
                        replayIcon.setImageResource(R.drawable.ic_repeat_deep_orange_48dp);
                    } else {
                        sharedPreferencesUtil.setRepeat(v.getContext(), false);
                        oNorOffTextView.setText("OFF");
                        replayIcon.setImageResource(R.drawable.ic_repeat_black_48dp);
                    }
                }
            });


        }
    }

    public void setCurrentSong(TrackData currentSong) {
        this.currentSong = currentSong;
    }

    // The method to display the popUp menu
    private void showMenu(View v, final TrackData trackData1) {

        final IconizedMenu popup = new IconizedMenu(v.getContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch(item.getTitle().toString()) {

                    case "Move Up":

                        data.putSerializable("action", item.getTitle().toString());
                        data.putSerializable("TrackData", trackData1);

                        menuItemClicked = true;
                        trackDBAdapter.open();
                        trackDBAdapter.reorderItem(trackData1, "Move Up");
                        trackData = trackDBAdapter.getAllTracks();
                        trackDBAdapter.close();
                        refresh(trackData);

                        popup.dismiss();

                        break;

                    case "Move Down":

                        data.putSerializable("action", item.getTitle().toString());
                        data.putSerializable("TrackData", trackData1);

                        menuItemClicked = true;
                        trackDBAdapter.open();
                        trackDBAdapter.reorderItem(trackData1, "Move Down");
                        trackData = trackDBAdapter.getAllTracks();
                        trackDBAdapter.close();
                        refresh(trackData);

                        popup.dismiss();

                        break;

                    case "Delete":

                        data.putSerializable("action", item.getTitle().toString());
                        data.putSerializable("TrackData", trackData1);

                        menuItemClicked = true;
                        trackDBAdapter.open();
                        trackDBAdapter.deleteTrack(trackData1);
                        trackData = trackDBAdapter.getAllTracks();
                        trackDBAdapter.close();
                        refresh(trackData);

                        break;

                }

                return false;
            }
        });
    }

    // Constructor for this class
    public BaseAdapter(List<TrackData> trackData) {

        this.trackData = trackData;

        whatInteger = new What();
        data = new Bundle();
    }

    public void refresh(List<TrackData> trackData) {

        this.trackData = trackData;
        notifyDataSetChanged();

        if(menuItemClicked) {

            refreshMsg = handler.createMessage(refreshMsg, whatInteger.getRefreshSongList());
            refreshMsg.setData(data);
            handler.sendMessage(refreshMsg);
            menuItemClicked = false;
        }

    }

    public void updateSoundIcon(final String id, boolean active)
    {
        if(previousTrackPlayed != -1){
            notifyItemChanged(previousTrackPlayed);
        }


        for(TrackData td: trackData) {

            if(td.getId() == id) {

                position = trackData.indexOf(td);
                currentTrackPlaying = position;

                setSoundIconVisible = true;
                firstUpdateSound = true;
                activeIcon = active;

            }
        }

        notifyItemChanged(position);

    }

    public void setSoundIconInvisible(String id)
    {
        for(TrackData td: trackData) {

            if(td.getId() == id) {

                position = trackData.indexOf(td);

                setSoundIconVisible = false;
                firstUpdateSound = false;

            }
        }

        notifyItemChanged(position);
    }

    public void setHandler(MessageHandler handler)
    {
        this.handler = handler;
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
            trackDataPositions.add(trackData.get(position));

            final TrackData trackData = this.trackData.get(position);

            ((MyViewHolder) holder).trackSongTextView.setText(trackData.getSongAndArtist());
            ((MyViewHolder) holder).startTime.setText(trackData.getStartTime());
            ((MyViewHolder) holder).stopTime.setText(trackData.getStopTime());

            // Sets onClick for all option buttons
            ((MyViewHolder) holder).options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showMenu(v, trackData);
                }
            });

            ((MyViewHolder) holder).trackId = trackData.getId();

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

                    if(!activeIcon) {
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

        if (position == trackData.size()) {
            // This is where we'll add footer.
            return FOOTER_VIEW;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {

        int items = 0;

        if(!trackData.isEmpty()) {
            items = trackData.size() + 1;
        }

        return items;
    }

}
