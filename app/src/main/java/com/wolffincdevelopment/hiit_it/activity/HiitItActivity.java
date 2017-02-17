package com.wolffincdevelopment.hiit_it.activity;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.wolffincdevelopment.hiit_it.FireBaseManager;
import com.wolffincdevelopment.hiit_it.RxJavaBus;
import com.wolffincdevelopment.hiit_it.TrackDataList;
import com.wolffincdevelopment.hiit_it.manager.UserManager;
import com.wolffincdevelopment.hiit_it.service.HomeMusicService;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.StringUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class HiitItActivity extends AppCompatActivity {

    private final String USER_MANAGER_PREF = "user_manager_pref";

    private SharedPreferences sharedPreferences;
    private HomeMusicService homeMusicService;
    private TrackDataList trackDataList;
    private HiitItActivityCallBack callBack;

    public UserManager userManager;
    public FireBaseManager fireBaseManager;

    public interface HiitItActivityCallBack {
        void onDataChanged();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = this.getSharedPreferences(USER_MANAGER_PREF, MODE_PRIVATE);

        userManager = UserManager.getInstance(sharedPreferences);
        fireBaseManager = FireBaseManager.getInstance(userManager);

        if (StringUtils.isEmptyOrNull(userManager.getPrefUserKey())) {
            userManager.setUserKey(fireBaseManager.getRandomKey());
        }

        // ToDo Add in date logic to keep track of app usage

        trackDataList = TrackDataList.getInstance();

        if (fireBaseManager.getUserKeyAndTracksDB() != null) {

            fireBaseManager.getUserKeyAndTracksDB().orderByChild(FireBaseManager.ORDERID).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Empty list for the new items
                    trackDataList.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        if (snapshot != null) {

                            TrackData trackData = snapshot.getValue(TrackData.class);

                            trackDataList.add(trackData);

                            if (callBack != null) {
                                callBack.onDataChanged();
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        }
    }

    public void setCallBack(HiitItActivityCallBack callBack) {
        this.callBack = callBack;
    }

    public RxJavaBus getRxJavaBus() {
        return RxJavaBus.getInstance();
    }
}
