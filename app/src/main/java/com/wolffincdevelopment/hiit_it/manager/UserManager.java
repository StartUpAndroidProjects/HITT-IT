package com.wolffincdevelopment.hiit_it.manager;

import android.content.SharedPreferences;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class UserManager {

    private static final String PREF_USER_NAME = "user_name";
    private static final String PREF_USER_KEY = "user_key";
    private static final String PREF_LAST_LOGIN = "last_login";
    private static final String PREF_DATE = "date";
    private static final String PREF_HAS_SPOTIFY_ACCOUNT = "has_spotify_account";
    private static final String PREF_HAS_SEEN_ADD_TRACK_IMAGE = "has_seen_ad_track_image";
    private static final String PREF_TRACK_SET_COUNT = "track_set_count";
    private static final String PREF_TRACK_SET_CONTINUOUS = "track_set_continuous";


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

    /**
     * Set FireBase User Key
     * @param userKey
     */
    public void setUserKey(String userKey) {
        prefManager.apply(PREF_USER_KEY, userKey);
    }

    public void setPrefLastLogin(String lastLogin) {
        prefManager.apply(PREF_LAST_LOGIN, lastLogin);
    }

    public void setDate(long date) {
        prefManager.apply(PREF_DATE, date);
    }

    public void setCurrentTrackCount(int trackCount) {
        prefManager.apply(PREF_TRACK_SET_COUNT, trackCount);
    }

    public void setCurrentTrackContinuous(boolean continuous) {
        prefManager.apply(PREF_TRACK_SET_CONTINUOUS, continuous);
    }

    /**
     * Get the FireBase User Key
     * @return the user key {@link String}
     */
    public String getPrefUserKey() {
        return prefManager.getString(PREF_USER_KEY, null);
    }

    public String getPrefLastLogin() {
        return prefManager.getString(PREF_LAST_LOGIN, null);
    }

    public long getDate() {
        return prefManager.getLong(PREF_DATE, 0);
    }

    public int getCurrenTrackCount() {
        return prefManager.getInt(PREF_TRACK_SET_COUNT, 1);
    }

    public boolean getCurrentTrackContinuous() {
        return prefManager.getBoolean(PREF_TRACK_SET_CONTINUOUS, false);
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
