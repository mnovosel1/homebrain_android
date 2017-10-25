package org.bubulescu.homebrain;

import android.app.Application;
import android.content.Context;

public class HbApp extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        HbApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return HbApp.context;
    }
}
