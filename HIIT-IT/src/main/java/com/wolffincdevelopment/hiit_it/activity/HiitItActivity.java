package com.wolffincdevelopment.hiit_it.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wolffincdevelopment.hiit_it.FireBaseManager;
import com.wolffincdevelopment.hiit_it.RxJavaBus;
import com.wolffincdevelopment.hiit_it.TrackDataList;
import com.wolffincdevelopment.hiit_it.manager.UserManager;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.FileStorageUtil;
import com.wolffincdevelopment.hiit_it.util.PermissionUtil;
import com.wolffincdevelopment.hiit_it.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class HiitItActivity extends AppCompatActivity {

	protected HiitItActivityCallBack callBack;
	private final String USER_MANAGER_PREF = "user_manager_pref";

	private SharedPreferences sharedPreferences;
	private TrackDataList trackDataList;
	protected PermissionUtil permissionUtil;

	public UserManager userManager;
	public FireBaseManager fireBaseManager;

	private Context context;

	public interface HiitItActivityCallBack {
		void onDataChanged();
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;

		sharedPreferences = this.getSharedPreferences(USER_MANAGER_PREF, MODE_PRIVATE);

		userManager = UserManager.getInstance(sharedPreferences);
		fireBaseManager = FireBaseManager.getInstance(userManager);

		permissionUtil = new PermissionUtil();

		if (StringUtils.isEmptyOrNull(userManager.getPrefUserKey())) {
			userManager.setUserKey(fireBaseManager.getRandomKey());
		}

		setLaunchTimeStamp();

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

							if (permissionUtil.isReadStoragePermissionGranted(context)) {

								FileStorageUtil.TrackDataChanged dataChanged = FileStorageUtil.checkForNewPathData(trackData, context);

								if (dataChanged.updated) {
									fireBaseManager.deleteTrack(trackData.getKey());
									trackData = dataChanged.trackData;
									fireBaseManager.pushTrackData(dataChanged.trackData);
								}
							}

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

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	private void setLaunchTimeStamp() {

		final Calendar lastLoginCalendar = Calendar.getInstance();
		final Calendar currentCalendar = Calendar.getInstance();

		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		final String formattedDate = df.format(currentCalendar.getTime());

		if (StringUtils.isEmptyOrNull(userManager.getPrefLastLogin())) {
			userManager.setPrefLastLogin(formattedDate);
			fireBaseManager.setLastLogin();
		} else {

			String y = userManager.getPrefLastLogin().substring(0, 4);
			String m = userManager.getPrefLastLogin().substring(5, 7);
			String d = userManager.getPrefLastLogin().substring(8, 10);

			int year = Integer.valueOf(y);
			int month = Integer.valueOf(m);
			int day = Integer.valueOf(d);

			lastLoginCalendar.set(year, month, day);

			if (lastLoginCalendar.after(currentCalendar)) {
				userManager.setPrefLastLogin(formattedDate);
				fireBaseManager.setLastLogin();
			}
		}
	}

	public void setCallBack(HiitItActivityCallBack callBack) {
		this.callBack = callBack;
	}

	public RxJavaBus getRxJavaBus() {
		return RxJavaBus.getInstance();
	}
}
