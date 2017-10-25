package org.bubulescu.homebrain;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;


public class DownloadFile {

    private static final String TAG = "DownloadFile";


    public DownloadFile (Context context) {
        this(context, "https://hbr.bubulescu.org:9343/upd/", "index.html");
    }

    public DownloadFile(Context context, String downloadURL, String dlFilename) {

        File dir = new File(Environment.getExternalStorageDirectory() + "/hbrain");
        if ( !dir.exists() ) dir.mkdirs();

        //downloadURL = "https://hbr.bubulescu.org:9343/upd";

        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downloadURL);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
               .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
               .setDestinationInExternalFilesDir(context, "hbrain", dlFilename);

        downloadmanager.enqueue(request);
    }
}