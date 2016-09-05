package com.wolffincdevelopment.hiit_it;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by kylewolff on 6/6/2016.
 */
public class TrackDBAdapter {

    private static int order_id_counter = 0;

    private static final String DATABASE_NAME = "tracks.db";
    private static final String DATABASE_VERSION = "2";
    private Cursor createCursor;

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

    private SQLiteDatabase sqLiteDatabase;
    private Context context;

    private TrackDBHelper trackDBHelper;

    public TrackDBAdapter(Context context) {
        this.context = context;
    }

    public TrackDBAdapter open() throws android.database.SQLException {

        trackDBHelper = new TrackDBHelper(context);
        sqLiteDatabase = trackDBHelper.getWritableDatabase();

        return this;
    }

    public void close() {
        trackDBHelper.close();
        sqLiteDatabase.close();
    }

    public int getNextOrderId() {

        int orderId;

        String[] orderIdStringArray = {COLUMN_ORDER_ID};

        Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, orderIdStringArray, null, null, null, null, COLUMN_ORDER_ID + " ASC");

        cursor.moveToLast();

        if(cursor.getPosition() == -1) {
            orderId = 1;
        } else {
            orderId = cursor.getInt(0) + 1;
        }

        cursor.close();

        return orderId;
    }

    public TrackData createTrackData(String aristName, String songName, String stopTime, String startTime, String stream, long mediaId, int orderId) {

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

        TrackData trackData = cursorToTrack(createCursor);

        return trackData;
    }

    public void deleteTrack(TrackData trackData) {

        ContentValues updateOrderId = new ContentValues();

        Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, item_columns, COLUMN_ORDER_ID + " >= " + String.valueOf(trackData.getOrderId()),
                null, null, null, COLUMN_ORDER_ID + " ASC ");

        cursor.moveToFirst();

        if(cursor.getColumnCount() != 0) {

            if (trackData.getOrderId() == cursor.getInt(7)) {
                sqLiteDatabase.delete(TRACK_TABLE, COLUMN_ID + " = " + trackData.getId(), null);
            }

            cursor.moveToLast();

            if (trackData.getOrderId() == cursor.getInt(7)) {
                sqLiteDatabase.delete(TRACK_TABLE, COLUMN_ID + " = " + trackData.getId(), null);
            } else {
                int lastOrderId = cursor.getInt(7);

                for (int count = trackData.getOrderId(); count < lastOrderId; count++) {
                    updateOrderId.put(COLUMN_ORDER_ID, count);

                    if (count == trackData.getOrderId()) {
                        sqLiteDatabase.update(TRACK_TABLE, updateOrderId, COLUMN_ORDER_ID + " = " + String.valueOf(trackData.getOrderId() + 1), null);
                    } else {
                        sqLiteDatabase.update(TRACK_TABLE, updateOrderId, COLUMN_ORDER_ID + " = " + String.valueOf(count + 1), null);
                    }
                    updateOrderId.clear();
                }
            }

            sqLiteDatabase.delete(TRACK_TABLE, COLUMN_ID + " = " + trackData.getId(), null);
        }
        cursor.close();
    }

    public void reorderItem(TrackData trackData, String upOrdown) {

        ContentValues contentValues = new ContentValues();

        int previousId;

        switch(upOrdown) {

            case "Move Up":

                if(trackData.getOrderId() != 1) {

                    String[] columnIds = {COLUMN_ORDER_ID, COLUMN_ID};

                    Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, columnIds, COLUMN_ORDER_ID + " = " +
                        String.valueOf(trackData.getOrderId() - 1), null, null, null, null);

                    cursor.moveToLast();

                        previousId = cursor.getInt(1);
                        contentValues.put(COLUMN_ORDER_ID, cursor.getInt(0));
                        sqLiteDatabase.update(TRACK_TABLE, contentValues, COLUMN_ID + " = " + trackData.getId(), null);

                    cursor.close();

                    contentValues.clear();

                    contentValues.put(COLUMN_ORDER_ID, trackData.getOrderId());

                    sqLiteDatabase.update(TRACK_TABLE, contentValues, COLUMN_ID + " = " + String.valueOf(previousId) , null);

                }

                break;

            case "Move Down":

                   String[] columnIds = {COLUMN_ORDER_ID, COLUMN_ID};

                    Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, columnIds, COLUMN_ORDER_ID + " = " +
                        String.valueOf(trackData.getOrderId() + 1), null, null, null, null);

                    cursor.moveToLast();

                        if(cursor.getCount() != 0) {

                            previousId = cursor.getInt(1);
                            contentValues.put(COLUMN_ORDER_ID, cursor.getInt(0));
                            sqLiteDatabase.update(TRACK_TABLE, contentValues, COLUMN_ID + " = " + trackData.getId(), null);

                            cursor.close();

                            contentValues.clear();

                            contentValues.put(COLUMN_ORDER_ID, trackData.getOrderId());

                            sqLiteDatabase.update(TRACK_TABLE, contentValues, COLUMN_ID + " = " + String.valueOf(previousId), null);
                        } else {
                            cursor.close();
                        }

                break;
        }
    }

    public ArrayList<TrackData> getAllTracks() {

        ArrayList<TrackData> userTracks = new ArrayList<>();

        Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, item_columns, null, null, null, null, COLUMN_ORDER_ID + " ASC");

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {

            TrackData trackData = cursorToTrack(cursor);
            userTracks.add(trackData);
            cursor.moveToNext();
        }

        cursor.close();

        return userTracks;
    }

    public ArrayList<TrackData> getAllStreams() {

        ArrayList<TrackData> streams = new ArrayList<>();
        String[] streamStringArray = {COLUMN_STREAM_PATH, COLUMN_MEDIA_ID, COLUMN_START_TIME, COLUMN_STOP_TIME, COLUMN_ORDER_ID};

        Cursor cursor = sqLiteDatabase.query(TRACK_TABLE, streamStringArray, null, null, null, null, COLUMN_ORDER_ID + " ASC");

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {

            TrackData trackData = streamToTrack(cursor);
            streams.add(trackData);
            cursor.moveToNext();
        }

        cursor.close();

        return streams;
    }

    public TrackData cursorToTrack(Cursor cursor){

        TrackData trackData = null;

        order_id_counter++;

        trackData = new TrackData(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),
                    cursor.getString(5), cursor.getLong(6), cursor.getString(0), cursor.getInt(7));

        return trackData;
    }

    public TrackData streamToTrack(Cursor cursor){

        TrackData song = null;

        song = new TrackData(cursor.getString(0), cursor.getLong(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));

        return song;
    }

    private static class TrackDBHelper extends SQLiteOpenHelper {

        TrackDBHelper(Context context) {

            super(context, DATABASE_NAME, null, Integer.parseInt(DATABASE_VERSION));
        }

        @Override
        public  void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + TRACK_TABLE);
            onCreate(db);
        }
    }
}
