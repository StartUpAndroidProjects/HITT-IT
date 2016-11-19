package com.wolffincdevelopment.hiit_it;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;

import com.wolffincdevelopment.hiit_it.model.TrackData;
import com.wolffincdevelopment.hiit_it.viewmodel.TrackItem;

import java.util.ArrayList;

/**
 * Created by kylewolff on 6/6/2016.
 */
public class TrackDBAdapter
{
    private static int order_id_counter = 0;
    private int permissionGranted;
    private long media_id;

    private static final String DATABASE_NAME = "music_tracks.db";
    private static final String DATABASE_VERSION = "1";
    private Cursor createCursor;
    private ContentResolver cr;

    public static final String TRACK_TABLE = "track";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ARTIST_NAME = "artist";
    public static final String COLUMN_SONG_NAME = "song";
    public static final String COLUMN_STREAM_PATH = "stream";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_STOP_TIME = "stop_time";
    public static final String COLUMN_MEDIA_ID = "mediaId";
    public static final String COLUMN_ORDER_ID = "order_id";

    private String[] item_columns = {COLUMN_ID, COLUMN_ARTIST_NAME, COLUMN_SONG_NAME, COLUMN_STREAM_PATH,
            COLUMN_START_TIME, COLUMN_STOP_TIME, COLUMN_MEDIA_ID, COLUMN_ORDER_ID};

    private static final String DATABASE_CREATE = "CREATE TABLE " + TRACK_TABLE + " ( " +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ARTIST_NAME + " TEXT NOT NULL, " +
            COLUMN_SONG_NAME + " TEXT NOT NULL, " +
            COLUMN_STREAM_PATH + " TEXT NOT NULL, " +
            COLUMN_START_TIME + " TEXT NOT NULL, " +
            COLUMN_STOP_TIME + " TEXT NOT NULL, " +
            COLUMN_MEDIA_ID + " INTEGER NOT NULL, " +
            COLUMN_ORDER_ID + " INTEGER NOT NULL" +
            ");";

    private static ArrayList<String> media_ids = new ArrayList<>();
    private static String mediaIds;

    private SQLiteDatabase sqLiteDatabase;
    private Context context;

    private TrackDBHelper trackDBHelper;

    public TrackDBAdapter(Context context) {
        this.context = context;
    }

    public TrackDBAdapter open() throws android.database.SQLException
    {
        trackDBHelper = new TrackDBHelper(context);
        sqLiteDatabase = trackDBHelper.getWritableDatabase();

        return this;
    }

    public void close()
    {
        trackDBHelper.close();
        sqLiteDatabase.close();
    }

    /**
     * When the user deletes a media file on their phone that is on their list we need to update accordingly
     */
    public void checkForStorageDeletion()
    {
        permissionGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE );

        if(permissionGranted == 0)
        {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

            cr = context.getContentResolver();

            Cursor cursor = cr.query(uri, null, selection, null, sortOrder);

            int count = 0;

            if (cursor != null && cursor.getColumnCount() != 0)
            {
                count = cursor.getCount();

                if (count > 0)
                {
                    while (cursor.moveToNext())
                    {
                        media_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                        media_ids.add(String.valueOf(media_id));
                    }
                }
            }

            if(cursor != null)
            {
                cursor.close();
            }

            cursor = sqLiteDatabase.query(TRACK_TABLE, item_columns, COLUMN_MEDIA_ID + " NOT IN" + media_ids.toString().replace('[', '(').replace(']', ')'), null, null, null, null);

            if (cursor != null && cursor.getColumnCount() != 0)
            {
                count = cursor.getCount();

                if (count > 0)
                {
                    while (cursor.moveToNext())
                    {
                        TrackItem trackItem = cursorToTrack(cursor);
                        deleteTrack(trackItem);
                    }
                }
            }

            if(cursor != null)
            {
                cursor.close();
            }
        }
    }

    /**
     * In SQLITE we have a KEY ID but we needed an orderId for the list order.
     * This method will generate the next orderId when creating tracks
     *
     * @return orderId
     */
    public int getNextOrderId()
    {
        int orderId;

        String[] orderIdStringArray = {COLUMN_ORDER_ID};

        Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, orderIdStringArray, null, null, null, null, COLUMN_ORDER_ID + " ASC");

        cursor.moveToLast();

        if(cursor.getPosition() == -1)
        {
            orderId = 1;
        }
        else
        {
            orderId = cursor.getInt(0) + 1;
        }

        cursor.close();

        return orderId;
    }

    /**
     * Creating the TrackData objects
     *
     * @param aristName
     * @param songName
     * @param stopTime
     * @param startTime
     * @param stream
     * @param mediaId
     * @param orderId
     * @return TrackData
     */
    public TrackItem createTrackData(String aristName, String songName, String stopTime, String startTime, String stream, long mediaId, int orderId)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ARTIST_NAME, aristName);
        contentValues.put(COLUMN_SONG_NAME, songName);
        contentValues.put(COLUMN_STREAM_PATH, stream);
        contentValues.put(COLUMN_START_TIME, startTime);
        contentValues.put(COLUMN_STOP_TIME, stopTime);
        contentValues.put(COLUMN_MEDIA_ID, mediaId);
        contentValues.put(COLUMN_ORDER_ID, orderId);

        long insertId = sqLiteDatabase.insert(TRACK_TABLE, null, contentValues);

         createCursor = sqLiteDatabase.query(TRACK_TABLE, item_columns,
                COLUMN_ID + " = " + insertId, null, null, null, null);

        createCursor.moveToFirst();

        TrackItem trackItem = cursorToTrack(createCursor);

        return trackItem;
    }

    /**
     * Update the DB correctly when a track is deleted from the list
     *
     * @param trackItem
     */
    public void deleteTrack(TrackItem trackItem) {

        ContentValues updateOrderId = new ContentValues();

        Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, item_columns, COLUMN_ORDER_ID + " >= " + String.valueOf(trackItem.getOrderId()),
                null, null, null, COLUMN_ORDER_ID + " ASC ");

        cursor.moveToFirst();

        if(cursor.getColumnCount() != 0)
        {
            if (trackItem.getOrderId() == cursor.getInt(7))
            {
                sqLiteDatabase.delete(TRACK_TABLE, COLUMN_ID + " = " + trackItem.getId(), null);
            }

            cursor.moveToLast();

            if (trackItem.getOrderId() == cursor.getInt(7))
            {
                sqLiteDatabase.delete(TRACK_TABLE, COLUMN_ID + " = " + trackItem.getId(), null);
            }
            else
            {
                int lastOrderId = cursor.getInt(7);

                for (int count = trackItem.getOrderId(); count < lastOrderId; count++)
                {
                    updateOrderId.put(COLUMN_ORDER_ID, count);

                    if (count == trackItem.getOrderId())
                    {
                        sqLiteDatabase.update(TRACK_TABLE, updateOrderId, COLUMN_ORDER_ID + " = " + String.valueOf(trackItem.getOrderId() + 1), null);
                    }
                    else
                    {
                        sqLiteDatabase.update(TRACK_TABLE, updateOrderId, COLUMN_ORDER_ID + " = " + String.valueOf(count + 1), null);
                    }

                    updateOrderId.clear();
                }
            }

            sqLiteDatabase.delete(TRACK_TABLE, COLUMN_ID + " = " + trackItem.getId(), null);
        }

        cursor.close();
    }

    /**
     * When the user taps the options menu and selects Move Up or Down we need to reorder the DB correctly
     *
     * @param trackItem
     * @param upOrdown
     */
    public void reorderItem(TrackItem trackItem, String upOrdown) {

        ContentValues contentValues = new ContentValues();

        int previousId;

        switch(upOrdown) {

            case "Move Up":

                if(trackItem.getOrderId() != 1)
                {

                    String[] columnIds = {COLUMN_ORDER_ID, COLUMN_ID};

                    Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, columnIds, COLUMN_ORDER_ID + " = " +
                        String.valueOf(trackItem.getOrderId() - 1), null, null, null, null);

                    cursor.moveToLast();

                        previousId = cursor.getInt(1);
                        contentValues.put(COLUMN_ORDER_ID, cursor.getInt(0));
                        sqLiteDatabase.update(TRACK_TABLE, contentValues, COLUMN_ID + " = " + trackItem.getId(), null);

                    cursor.close();

                    contentValues.clear();

                    contentValues.put(COLUMN_ORDER_ID, trackItem.getOrderId());

                    sqLiteDatabase.update(TRACK_TABLE, contentValues, COLUMN_ID + " = " + String.valueOf(previousId) , null);

                }

                break;

            case "Move Down":

                   String[] columnIds = {COLUMN_ORDER_ID, COLUMN_ID};

                    Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, columnIds, COLUMN_ORDER_ID + " = " +
                        String.valueOf(trackItem.getOrderId() + 1), null, null, null, null);

                    cursor.moveToLast();

                    if(cursor.getCount() != 0)
                    {
                        previousId = cursor.getInt(1);
                        contentValues.put(COLUMN_ORDER_ID, cursor.getInt(0));
                        sqLiteDatabase.update(TRACK_TABLE, contentValues, COLUMN_ID + " = " + trackItem.getId(), null);

                        cursor.close();

                        contentValues.clear();

                        contentValues.put(COLUMN_ORDER_ID, trackItem.getOrderId());

                        sqLiteDatabase.update(TRACK_TABLE, contentValues, COLUMN_ID + " = " + String.valueOf(previousId), null);
                    }
                    else
                    {
                        cursor.close();
                    }

                break;
        }
    }

    /**
     *
     * @return ArrayList<TrackData></TrackData>
     */
    public ArrayList<TrackItem> getAllTracks()
    {
        ArrayList<TrackItem> userTracks = new ArrayList<>();

        Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, item_columns, null, null, null, null, COLUMN_ORDER_ID + " ASC");

        cursor.moveToFirst();

        while(!cursor.isAfterLast())
        {
            userTracks.add(cursorToTrack(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return userTracks;
    }

    /**
     * Take the cursor and create a new TrackData object
     *
     * @param cursor
     * @return TrackData
     */
    public TrackItem cursorToTrack(Cursor cursor)
    {
        TrackData trackData;

        order_id_counter++;

        trackData = new TrackData(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),
                    cursor.getString(5), cursor.getLong(6), cursor.getString(0), cursor.getInt(7));

        return new TrackItem(trackData);
    }

    private static class TrackDBHelper extends SQLiteOpenHelper
    {
        TrackDBHelper(Context context)
        {
            super(context, DATABASE_NAME, null, Integer.parseInt(DATABASE_VERSION));
        }

        @Override
        public  void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS " + TRACK_TABLE);
            onCreate(db);
        }
    }
}
