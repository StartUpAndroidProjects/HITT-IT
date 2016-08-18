package com.wolffincdevelopment.hiit_it;

/**
 * Created by kylewolff on 7/11/2016.
 */
public class What {

    private int refreshSongList = 300;
    private int updatePlayControls = 301;
    private int setSoundIconVisible = 302;
    private int setSoundIconNonVisible = 303;
    private int sendTrackData = 304;
    private int showDialog = 305;
    private int playThisSong = 306;
    private int updateMediaControls = 307;
    private int pauseResumeCurrentSong = 308;
    private int cuurentSong = 309;
    private int nextOrPrev = 310;

    public What() {

    }

    public int getRefreshSongList() {
        return refreshSongList;
    }

    public int getUpdatePlayControls() {
        return updatePlayControls;
    }

    public int getSetSoundIconVisible() {
        return setSoundIconVisible;
    }

    public int getSetSoundIconNonVisible() {
        return setSoundIconNonVisible;
    }

    public int getSendTrackData() { return sendTrackData; }

    public int getShowDialog() { return showDialog; }

    public int getPlayThisSong() { return playThisSong; }

    public int getUpdateMediaControls() { return  updateMediaControls;}

    public int pauseResumeCurrentSong() { return pauseResumeCurrentSong; }

    public int getCurrentSong() { return cuurentSong; }

    public int getNextOrPrev() { return nextOrPrev;}

}
