package org.bubulescu.homebrain;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.key;
import static android.R.attr.value;
import static android.R.id.edit;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String TAG = "MyDbaHandler_LOG_";

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

    public void updateDb(JSONObject data) {

        SQLiteDatabase db = this.getWritableDatabase();
        String keys = "", vals = "", table, sql;

        try {

            JSONObject values = (JSONObject) data.get("values");
            table = (String) data.get("table");

            for (int i = 0; i<values.names().length(); i++) {
                keys += values.names().getString(i) + ", ";
                vals += "'" + values.get(values.names().getString(i)).toString() + "', ";
            }

            keys = keys.substring(0, keys.length() - 2);
            vals = vals.substring(0, vals.length() - 2);

            sql = "INSERT INTO " + table + " (" + keys +  ") VALUES (" + vals + ");";

            db.execSQL(sql);
            Log.d(TAG, sql);


        } catch (JSONException e) {
                e.printStackTrace();
            Log.d(TAG, "JSON exc.: " + e.toString());
        } catch (Exception e){
            Log.d(TAG, e.toString());
        }

        db.execSQL("DELETE FROM changelog WHERE timestamp <= date('now', '-14 day');");

        db.close();
    }

    // Getting Count
    public int getCount() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM changelog", null);

        int retCount = cursor.getCount();
        cursor.close();
        db.close();

        return retCount;
    }

}