package org.bubulescu.homebrain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BroadcastReceiver_LOG_";
    private static MyBroadcastReceiver instance = null;
    Context mContext;

    MyBroadcastReceiver(Context c) {
        mContext = c;
    }

    private MyBroadcastReceiver() {
        if (instance != null) {
            throw new RuntimeException("Singleton CLASS: use MyBroadcastReceiver.getInstance();");
        }
    }

    ;

    public synchronized static MyBroadcastReceiver getInstance() {
        if (instance == null) return new MyBroadcastReceiver();
        else return instance;
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        // just a message
        if (arg1.hasExtra("message")) {
            Log.d(TAG, "FCMessage:: " + arg1.getStringExtra("message"));
        }
    }

}
