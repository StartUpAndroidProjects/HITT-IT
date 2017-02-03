package com.wolffincdevelopment.hiit_it.manager;

import android.content.SharedPreferences;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class UserManager {

    private static final String PREF_USER_NAME = "user_name";
    private static final String PREF_HAS_SPOTIFY_ACCOUNT = "has_spotify_account";
    private static final String PREF_HAS_SEEN_ADD_TRACK_IMAGE = "has_seen_ad_track_image";

    private static UserManager userManager;

    private final PreferenceManager prefManager;

    private UserManager(PreferenceManager prefManager) {
        this.prefManager = prefManager;
    }

    public static UserManager getInstance(SharedPreferences sharedPreferences) {

        if (userManager == null) {
            userManager = new UserManager(new PreferenceManager(sharedPreferences));
        }

        return userManager;
    }

    public void setSpotifyUserName(String userName) {
        prefManager.apply(PREF_USER_NAME, userName);
    }

    public void setHasSpotifyAccount(boolean hasSpotifyAccount) {
        prefManager.apply(PREF_HAS_SPOTIFY_ACCOUNT, hasSpotifyAccount);
    }

    public void setSeenAddTrackImage(boolean hasSeenTrackImage) {
        prefManager.apply(PREF_HAS_SEEN_ADD_TRACK_IMAGE, hasSeenTrackImage);
    }

    public boolean getPrefHasSeenAddTrackImage() {
        return prefManager.getBoolean(PREF_HAS_SEEN_ADD_TRACK_IMAGE, false);
    }
}
