package org.bubulescu.homebrain;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class WebAppInterface {

    Context mContext;

    /**
     * Instantiate the interface and set the context
     */
    WebAppInterface(Context c) {
        mContext = c;
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void toast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public JSONObject getUpdate() throws JSONException {
        // TODO getUpdates from database
        DatabaseHandler dbh = new DatabaseHandler(mContext);

        JSONObject ret = new JSONObject();

        // Select All Query
        String selectQuery = "SELECT timestamp, statebefore, state, changedto FROM changelog";

        SQLiteDatabase db = dbh.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int rowNum = 0;

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                JSONObject row = new JSONObject();
                row.put("timestamp", cursor.getString(0));
                row.put("statebefore", cursor.getString(1));
                row.put("state", cursor.getString(2));
                row.put("changedto", cursor.getString(3));

                ret.put(Integer.toString(rowNum++), row);
            } while (cursor.moveToNext());
        }
        return ret;
    }
}
