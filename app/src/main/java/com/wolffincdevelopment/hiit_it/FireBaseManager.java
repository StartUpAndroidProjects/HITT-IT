package com.wolffincdevelopment.hiit_it;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.wolffincdevelopment.hiit_it.manager.UserManager;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.StringUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kyle Wolff on 2/2/17.
 */

public class FireBaseManager {

    private String KEY_TRACKS = "tracks";
    private String KEY_USERS = "users";
    private String KEY_LAST_LOGIN = "lastlogin";

    public static String KEY = "key";
    public static String SONG = "song";
    public static String ARTIST = "artist";
    public static String STARTTIME = "startTime";
    public static String STOPTIME = "stopTime";
    public static String STREAM = "stream";
    public static String MEDIAID = "mediaId";
    public static String DURATION = "duration";
    public static String ORDERID = "orderId";

    private int dayOfLogin = 100;

    private boolean peristenceEnabled;
    private static FireBaseManager fireBaseManager;

    private UserManager userManager;

    public static FireBaseManager getInstance(UserManager userManager) {

        if (fireBaseManager == null) {
            fireBaseManager = new FireBaseManager().setUserManager(userManager);
        }

        return fireBaseManager;
    }

    private FireBaseManager setUserManager(UserManager userManager) {
        this.userManager = userManager;
        return this;
    }

    private DatabaseReference getDatabaseReference() {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        if (!peristenceEnabled) {
            firebaseDatabase.setPersistenceEnabled(true);
            peristenceEnabled = true;
        }

        return firebaseDatabase.getReference();
    }

    /**
     * Set value of a specific child. Hier is tracks -> userKey -> {child}
     *
     * @param child
     * @param o     the object you want to save
     */
    public void setValue(String child, Object o) {
        if (getUserKeyAndTracksDB() != null) {
            setValue(getUserKeyAndTracksDB().child(child), o);
        }
    }

    private void setValue(DatabaseReference databaseReference, Object o) {
        databaseReference.setValue(o);
    }

    public void updateChildren(DatabaseReference databaseReference, Map<String, Object> map) {
        databaseReference.updateChildren(map);
    }

    public void deleteTrack(String trackKey) {
        getUserKeyAndTracksDB().child(trackKey).removeValue();
    }

    public int getLastLogin() {
        return dayOfLogin;
    }

    public void setLastLogin() {

        Map<String, Object> value;

        if (getUserKey() != null) {

            value = new HashMap<>();
            value.put(KEY_LAST_LOGIN, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
            setValue(getUserKey(), value);

        }

        if (getUserKey() != null) {

           getUserKey().child(KEY_LAST_LOGIN).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {



                    //setLastLogin(Integer.valueOf(dataSnapshot.getValue().toString()));

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    setLastLogin(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
                }
            });
        }
    }

    private void setLastLogin(int loginInMilliSec) {
        this.dayOfLogin = loginInMilliSec;
    }

    private DatabaseReference getUserKey() {
        if (!StringUtils.isEmptyOrNull(userManager.getPrefUserKey())) {
            return getDatabaseReference().child(KEY_USERS).child(userManager.getPrefUserKey());
        } else {
            return null;
        }
    }

    public DatabaseReference getUserKeyAndTracksDB() {
        if (getUserKey() != null) {
            return getUserKey().child(KEY_TRACKS);
        } else {
           return null;
        }
    }

    public String getRandomKey() {
        return getDatabaseReference().push().getKey();
    }

    /**
     * Set our TrackData in FireBase
     *
     * @param trackData {@link TrackData}
     */
    public void pushTrackData(TrackData trackData) {

        String key = getRandomKey();

        TrackDataList trackDataList = TrackDataList.getInstance();

        for (TrackData data : trackDataList) {

            if (data.getKey().equals(trackData.getKey())) {
                updateChildren(getUserKeyAndTracksDB().child(trackData.getKey()), trackData.toMap());
                return;
            }
        }

        trackData.setKey(key);

        if (trackDataList.isEmpty()) {
            setValue(key, trackData);
        } else {
            setValue(key, getNextTrackDataWithOrderId(trackData));
        }
    }

    private TrackData getNextTrackDataWithOrderId(TrackData trackData) {

        TrackDataList trackDataList = TrackDataList.getInstance();

        int nextId = 0;

        for (TrackData trackData1 : trackDataList) {
            nextId = trackData1.getOrderId() + 1;
        }

        trackData.setOrderId(nextId);

        return trackData;
    }
}
