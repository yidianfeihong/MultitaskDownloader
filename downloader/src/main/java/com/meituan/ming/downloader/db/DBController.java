package com.meituan.ming.downloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.meituan.ming.downloader.DownloadEntry;

import java.util.ArrayList;

public class DBController {

    private DBHelper dbHelper;
    private static DBController mInstance;

    private static SQLiteDatabase db;
    private static final String TABLE_NAME = "DownloadEntry";

    public DBController(Context context) {
        dbHelper = new DBHelper(context, TABLE_NAME);
    }

    public static DBController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBController(context);
        }
        return mInstance;
    }


    public void insert(DownloadEntry entry) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", entry.id);
        values.put("name", entry.name);
        values.put("url", entry.url);
        values.put("currentLength", entry.currentLength);
        values.put("totalLength", entry.totalLength);
        values.put("status", entry.status.ordinal());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void delete(DownloadEntry entry) {
        db = dbHelper.getWritableDatabase();
        db = dbHelper.getWritableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[]{entry.id});
        db.close();
    }

    public void newOrUpdate(DownloadEntry entry) {
        if (queryById(entry.id) == null) {
            insert(entry);
        } else {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("id", entry.id);
            values.put("name", entry.name);
            values.put("url", entry.url);
            values.put("currentLength", entry.currentLength);
            values.put("totalLength", entry.totalLength);
            values.put("status", entry.status.ordinal());
            db.update(TABLE_NAME, values, "id = ?", new String[]{entry.id});
            db.close();
        }
    }


    public DownloadEntry queryById(String id) {
        db = dbHelper.getWritableDatabase();
        DownloadEntry entry = null;
        Cursor cursor = db.query(TABLE_NAME, null, "id = ?", new String[]{id}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                entry = new DownloadEntry();
                entry.id = cursor.getString(cursor.getColumnIndex("id"));
                entry.name = cursor.getString(cursor.getColumnIndex("name"));
                entry.url = cursor.getString(cursor.getColumnIndex("url"));

                entry.currentLength = cursor.getInt(cursor.getColumnIndex("currentLength"));
                entry.totalLength = cursor.getInt(cursor.getColumnIndex("totalLength"));
                entry.status = DownloadEntry.DownloadStatus.values()[cursor.getInt(cursor.getColumnIndex("status"))];
            }
            cursor.close();
        }
        db.close();
        return entry;
    }

    public ArrayList<DownloadEntry> queryAll() {
        db = dbHelper.getWritableDatabase();
        ArrayList<DownloadEntry> entries = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                DownloadEntry entry = new DownloadEntry();
                entry.id = cursor.getString(cursor.getColumnIndex("id"));
                entry.name = cursor.getString(cursor.getColumnIndex("name"));
                entry.url = cursor.getString(cursor.getColumnIndex("url"));
                entry.currentLength = cursor.getInt(cursor.getColumnIndex("currentLength"));
                entry.totalLength = cursor.getInt(cursor.getColumnIndex("totalLength"));
                entry.status = DownloadEntry.DownloadStatus.values()[cursor.getInt(cursor.getColumnIndex("status"))];
                entries.add(entry);
            }
            cursor.close();
        }
        db.close();
        return entries;
    }

}
