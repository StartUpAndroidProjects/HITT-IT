package com.wolffincdevelopment.hiit_it.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.wolffincdevelopment.hiit_it.service.model.TrackData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Kyle Wolff on 4/6/17.
 */

public class FileStorageUtil {

	public static Uri EXTERNAL_CONTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	public static Uri INTERNAL_CONTENT_URI = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

	public static List<TrackData> getMusicFilesViaMediaStore(ContentResolver cr, Uri uri) {

		List<TrackData> trackDataList = new ArrayList<>();

		String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
		String sortOrder = MediaStore.Audio.Media.ARTIST + " ASC";

		Cursor cur = cr.query(uri, null, selection, null, sortOrder);

		int count;

		if (cur != null) {

			count = cur.getCount();

			if (count > 0) {

				while (cur.moveToNext()) {

					String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
					String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
					String album = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM));
					long duration = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));
					String stream = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
					long mediaId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media._ID));

					if (StringUtils.isEmptyOrNull(album) || album.equalsIgnoreCase("null")) {
						album = "Unknown";
					}

					if (artist.contains("unknown") || StringUtils.isEmptyOrNull(artist) || artist.equalsIgnoreCase("null")) {
						artist = "Unknown";
					}

					if (duration <= 3540000 && duration > 2000) {
						trackDataList.add(new TrackData(artist, title, stream, album, duration, mediaId));
					}
				}
			}

			cur.close();
		}

		// Reorder the items because the query returns unknown tracks first
		Collections.sort(trackDataList, new Comparator<TrackData>() {
			@Override
			public int compare(TrackData trackData, TrackData trackData1) {
				return trackData.getArtist().compareToIgnoreCase(trackData1.getArtist());
			}
		});

		/*
			This chunk of code removes duplicate files that are the same song.
			There could be 3 files with different paths but same song so we only need to show one.
		 */

		List<Long> integerList = new ArrayList<>();
		String title = null;

		for (TrackData trackData : trackDataList) {

			if (title != null && title.equalsIgnoreCase(trackData.getSong())) {
				integerList.add(trackData.getMediaId());
			}

			title = trackData.getSong();
		}

		for (int i = 0; i < trackDataList.size(); i++) {

			for (Long mediaId : integerList) {

				if (trackDataList.get(i).getMediaId() == mediaId) {
					trackDataList.remove(i);
				}
			}
		}

		return trackDataList;
	}

	public static FileStorageUtil.TrackDataChanged checkForNewPathData(TrackData trackData, Context context) {

		boolean updated = false;

		List<TrackData> trackDataList = new ArrayList<>();
		trackDataList.addAll(getMusicFilesViaMediaStore(context.getContentResolver(), FileStorageUtil.EXTERNAL_CONTENT_URI));
		trackDataList.addAll(getMusicFilesViaMediaStore(context.getContentResolver(), FileStorageUtil.INTERNAL_CONTENT_URI));

		for (TrackData data : trackDataList) {

			if (data.getSong().equalsIgnoreCase(trackData.getSong()) && !data.getStream().equals(trackData.getStream())) {
				trackData.setStream(data.getStream());
				updated = true;
			}
		}

		return new FileStorageUtil.TrackDataChanged(trackData, updated);
	}

	public static class TrackDataChanged {

		public TrackData trackData;
		public boolean updated;

		public TrackDataChanged(TrackData trackData, boolean updated) {
			this.trackData = trackData;
			this.updated = updated;
		}
	}
}
