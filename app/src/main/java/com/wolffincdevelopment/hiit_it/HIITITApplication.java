package com.wolffincdevelopment.hiit_it;

import android.app.Application;

import com.karumi.dexter.Dexter;

/**
 * Created by mitchross on 6/30/16.
 */

public class HIITITApplication extends Application
{
	private static HIITITApplication singleton;


	@Override
	public void onCreate()
	{
		super.onCreate();
		Dexter.initialize(getApplicationContext());
	}
}
