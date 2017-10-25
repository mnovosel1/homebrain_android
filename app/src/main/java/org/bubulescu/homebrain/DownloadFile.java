package org.bubulescu.homebrain;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;


public class DownloadFile {

    private static final String TAG = "DownloadFile";


    public DownloadFile (Context context) {
        this(context, "https://hbr.bubulescu.org:9343/upd/", "index.html");
    }

    public DownloadFile(Context context, String downloadURL, String dlFilename) {


        //downloadURL = "https://hbr.bubulescu.org:9343/upd";

        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(downloadURL);
        DownloadManager.Request request = new DownloadManager.Request(uri);


        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.parse("file://hbrain/" + dlFilename ));

        downloadmanager.enqueue(request);

    }
}