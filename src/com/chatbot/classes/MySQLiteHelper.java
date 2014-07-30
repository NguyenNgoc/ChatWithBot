package com.chatbot.classes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Tung GIGA on 7/10/2014.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_MESSAGE = "messages";
    public static final String MESSAGE_COLUMN_ID = "_id";
    public static final String MESSAGE_COLUMN_CONTENT = "comment";
    public static final String MESSAGE_COLUMN_OWNER = "owner"; // 1 = BOT, 2 = USER
    public static final String MESSAGE_COLUMN_TIMESTAMP = "timestamp";
    public static final String MESSAGE_COLUMN_BOT_ID = "bot_id";

    private static final String DATABASE_NAME = "messages.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_MESSAGE + "("
            + MESSAGE_COLUMN_ID + " integer primary key autoincrement, "
            + MESSAGE_COLUMN_CONTENT + " text not null, "
            + MESSAGE_COLUMN_OWNER + " integer not null, "
            + MESSAGE_COLUMN_TIMESTAMP + " integer not null, "
            + MESSAGE_COLUMN_BOT_ID + " text not null"
            + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
        onCreate(db);
    }
}
