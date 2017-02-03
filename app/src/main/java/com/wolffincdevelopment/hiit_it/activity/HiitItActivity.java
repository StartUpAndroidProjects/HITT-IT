package com.wolffincdevelopment.hiit_it.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wolffincdevelopment.hiit_it.RxJavaBus;
import com.wolffincdevelopment.hiit_it.manager.UserManager;
import com.wolffincdevelopment.hiit_it.service.HomeMusicService;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class HiitItActivity extends AppCompatActivity {

    private final String USER_MANAGER_PREF = "user_manager_pref";

    private SharedPreferences sharedPreferences;
    private HomeMusicService homeMusicService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getSharedPreferences(USER_MANAGER_PREF, MODE_PRIVATE);
    }

    public UserManager getUserManager() {
        return UserManager.getInstance(sharedPreferences);
    }

    public RxJavaBus getRxJavaBus() {
        return RxJavaBus.getInstance();
    }
}
