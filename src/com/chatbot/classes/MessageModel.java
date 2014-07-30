package com.chatbot.classes;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Tung GIGA on 7/10/2014.
 */
public class MessageModel {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.MESSAGE_COLUMN_ID,
            MySQLiteHelper.MESSAGE_COLUMN_CONTENT,
            MySQLiteHelper.MESSAGE_COLUMN_OWNER,
            MySQLiteHelper.MESSAGE_COLUMN_TIMESTAMP,
            MySQLiteHelper.MESSAGE_COLUMN_BOT_ID
    };

    public MessageModel(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createMessage(Message message) {
        Util.log("MessageModel.createMessage: " + message.content);

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.MESSAGE_COLUMN_CONTENT, message.content);
        values.put(MySQLiteHelper.MESSAGE_COLUMN_OWNER, message.owner.getValue());
        values.put(MySQLiteHelper.MESSAGE_COLUMN_TIMESTAMP,message.timestamp);
        values.put(MySQLiteHelper.MESSAGE_COLUMN_BOT_ID,message.bot_id);

        long insertId = database.insert(MySQLiteHelper.TABLE_MESSAGE, null, values);
        message.id = insertId;
    }

    public void deleteMessage(Message message) {
        long id = message.id;
        if(id==0)
            return;
        System.out.println("Comment deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_MESSAGE, MySQLiteHelper.MESSAGE_COLUMN_ID
                + " = " + id, null);
    }

    public boolean deleteAllMessages(String bot_id)
    {
        database.delete(MySQLiteHelper.TABLE_MESSAGE, MySQLiteHelper.MESSAGE_COLUMN_BOT_ID + " = ? ", new String[] { bot_id });
        return true;
    }

    public List<Message> getAllMessages() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGE,
                allColumns, null, null, null, null, null);
        return cursorToMessages(cursor);
    }

    public List<Message> getAllMessagesBeforeId(long id, String bot_id, int count)
    {
        if(id==0)
        {
            Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGE,
                    allColumns, MySQLiteHelper.MESSAGE_COLUMN_BOT_ID + " = ? ", new String[]{ bot_id }, null, null, MySQLiteHelper.MESSAGE_COLUMN_ID + " desc" , "" + count);
            return cursorToMessages(cursor);
        }
        Cursor cursor = database.query(MySQLiteHelper.TABLE_MESSAGE,
                allColumns, MySQLiteHelper.MESSAGE_COLUMN_BOT_ID + " = ? and " + MySQLiteHelper.MESSAGE_COLUMN_ID + " < ?", new String[]{ bot_id , "" + id }, null, null, MySQLiteHelper.MESSAGE_COLUMN_ID + " desc" , "" + count);
        return cursorToMessages(cursor);
    }

    public List<Message> cursorToMessages(Cursor cursor)
    {
        List<Message> messages = new ArrayList<Message>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message message = cursorToMessage(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return messages;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.id = cursor.getLong(0);
        message.content = cursor.getString(1);
        message.owner = Message.MessageOwner.getOwner(cursor.getInt(2));
        message.timestamp = cursor.getLong(3);
        message.bot_id = cursor.getString(4);
        return message;
    }
}
