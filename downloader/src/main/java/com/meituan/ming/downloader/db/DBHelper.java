package com.meituan.ming.downloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download_data.db";
    private static final int DB_VERSION = 1;
    private String tableName;

    public DBHelper(Context context, String tableName) {

        super(context, DB_NAME, null, DB_VERSION);
        this.tableName = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + tableName + "(_id integer primary key autoincrement,id text,name text,url text,currentLength integer,totalLength integer,status integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + tableName);
        onCreate(db);
    }
}
