package com.wolffincdevelopment.hiit_it.manager;

import com.wolffincdevelopment.hiit_it.activity.MusicService;

/**
 * Created by Kyle Wolff on 11/2/16.
 */

public class MusicIndexManager implements MusicService.MusicServiceListener{

    private static MusicIndexManager manager = null;

    private int index;
    private int previousIndex;
    private int trackListLength;

    private MusicIndexManager(){};

    public static MusicIndexManager getInstance() {

        if(manager == null) {
            manager = new MusicIndexManager();
        }

        return manager;
    }

    public void setTrackListLength(int trackListLength) {
        this.trackListLength = trackListLength;
    }

    public void setIndex(int index) {
        previousIndex = this.index;
        this.index = index;
    }

    public int getIndex() {

        if(!(index <= trackListLength) && !(index > trackListLength) && trackListLength != 0) {
            return index;
        } else {
            return index = 0;
        }
    }

    public int getPreviousIndex() {
        return previousIndex;
    }

    @Override
    public void onNext() {

        previousIndex = index;

        if(trackListLength != 0) {

            index++;

            if(index >= trackListLength) {
                index = 0;
            }
        }
    }

    @Override
    public void onPrev() {

        previousIndex = index;

        if (trackListLength != 0) {

            index--;

            if (index == trackListLength) {
                index = 0;
            } else if (index > trackListLength - 1) {
                index = 0;
            } else if (index == -1) {
                index = trackListLength - 1;
            }
        }
    }
}
