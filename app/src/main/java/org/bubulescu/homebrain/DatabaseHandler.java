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
        String createStatesTable = "" +
                "CREATE TABLE states (\n" +
                "            name varchar(75) PRIMARY KEY,\n" +
                "            auto int(1) NOT NULL DEFAULT 1,\n" +
                "            active int(1) NOT NULL DEFAULT 0\n" +
                "        );";

        String createChangelogTable = "" +
                "CREATE TABLE changelog (\n" +
                "            timestamp DATETIME,\n" +
                "            statebefore varchar(30) NOT NULL,\n" +
                "            stateid int(11) NOT NULL,\n" +
                "            changedto int(1) NOT NULL,\n" +
                "            PRIMARY KEY(statebefore, stateid, changedto)\n" +
                "        );";

        String createChangelogTrigger = "" +
                "CREATE TRIGGER changelog_trigg\n" +
                "            BEFORE UPDATE ON states " +
                "            FOR EACH ROW\n" +
                "            WHEN OLD.active <> NEW.active\n" +
                "            BEGIN\n" +
                "                INSERT OR REPLACE INTO changelog (timestamp, statebefore, stateid, changedto)\n" +
                "                VALUES (\n" +
                "                            datetime('now','localtime'),\n" +
                "                            (SELECT group_concat(active, '') FROM states ORDER BY rowid ASC),\n" +
                "                            NEW.rowid,\n" +
                "                            NEW.active\n" +
                "                        );\n" +
                "                DELETE FROM changelog WHERE timestamp <= date('now', '-7 day');\n" +
                "            END;\n";

        try {
            db.execSQL(createStatesTable);
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        try {
            db.execSQL(createChangelogTable);
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        try {
            db.execSQL(createChangelogTrigger);
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables and trigger if existed
        db.execSQL("DROP TRIGGER IF EXISTS changelog_trigg");
        db.execSQL("DROP TABLE IF EXISTS changelog");
        db.execSQL("DROP TABLE IF EXISTS states");
 
        // Create tables again
        onCreate(db);
    }

    public void changeState(String state, int auto, int active) {
        SQLiteDatabase db = this.getWritableDatabase();

        int noActive = 0;
        if ( active == 0 ) noActive = 1;

        db.execSQL("INSERT OR REPLACE INTO states (name, auto, active) VALUES ('"+ state +"', "+ auto +", "+ noActive +")");
        db.execSQL("UPDATE states SET active = " + active + " WHERE name = '"+ state +"'");
    }

    // Getting Count
    public int getCount() {

        String countQuery = "SELECT * FROM changelog";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int retCount = cursor.getCount();
        cursor.close();

        return retCount;
    }
 
}