package com.wolffincdevelopment.hiit_it;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;

/**
 * Created by mitchross on 6/30/16.
 */

public class HIITITApplication extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		FirebaseDatabase.getInstance().setPersistenceEnabled(true);
	}
}
