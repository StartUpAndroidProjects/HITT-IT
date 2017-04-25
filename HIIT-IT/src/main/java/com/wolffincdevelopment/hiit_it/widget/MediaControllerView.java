package com.wolffincdevelopment.hiit_it.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.wolffincdevelopment.hiit_it.R;

/**
 * Created by Kyle Wolff on 11/1/16.
 */

public class MediaControllerView extends LinearLayout implements View.OnClickListener {

    private MediaControllerListener listener;

    private ImageButton playButton;
    private ImageButton nextButton;
    private ImageButton prevButton;

    public interface MediaControllerListener {

        void onPlay();

        void onNext();

        void onPrev();
    }

    public MediaControllerView(Context context) {
        this(context, null);
    }

    public MediaControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_media_controller, this);

        playButton = (ImageButton) findViewById(R.id.play);
        nextButton = (ImageButton) findViewById(R.id.next);
        prevButton = (ImageButton) findViewById(R.id.prev);

        initListener();
    }

    @Override
    public void onClick(View v) {

        if (v.equals(playButton)) {
            if (listener != null) {
                listener.onPlay();
            }
        } else if (v.equals(nextButton)) {
            if (listener != null) {
                listener.onNext();
            }
        } else if (v.equals(prevButton)) {
            if (listener != null) {
                listener.onPrev();
            }
        }

    }

    public void setListener(MediaControllerListener listener) {
        this.listener = listener;
    }

    public void initListener() {
        playButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
    }

    public void updatePlayButton(boolean paused, boolean stopped) {
        if (paused || stopped) {
            playButton.setImageResource(R.drawable.ic_play_circle_outline_white);
        } else {
            playButton.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
        }

    }
}
