package org.bubulescu.homebrain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 
public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "hbrain.db";
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createChangelogTable = "" +
                "CREATE TABLE changelog (\n" +
                "            timestamp DATETIME,\n" +
                "            statebefore varchar(30) NOT NULL,\n" +
                "            state varchar(50) NOT NULL,\n" +
                "            changedto int(1) NOT NULL\n" +
                "        );";

            db.execSQL(createChangelogTable);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables and trigger if existed
        db.execSQL("DROP TABLE IF EXISTS changelog");
 
        // Create tables again
        onCreate(db);
    }

    public void updateDb(String[] msgDataArray) {
        SQLiteDatabase db = this.getWritableDatabase();

        String timeStamp = msgDataArray[0];
        String stateBefore = msgDataArray[1];
        String state = msgDataArray[2];
        Integer changedTo = Integer.parseInt(msgDataArray[3]);

        db.execSQL("INSERT INTO changelog (timestamp, statebefore, state, changedto) " +
                    "VALUES ('"+ timeStamp +"', '"+ stateBefore +"', '"+ state +"', "+ changedTo +");"
        );

        db.execSQL("DELETE FROM changelog WHERE timestamp <= date('now', '-14 day');");
    }

    // Getting Count
    public int getCount() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM changelog", null);

        int retCount = cursor.getCount();
        cursor.close();

        return retCount;
    }
 
}