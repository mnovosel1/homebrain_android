package org.bubulescu.homebrain;

import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyAndroidFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyAndroidFCMIIDService";

    @Override
    public void onTokenRefresh() {
        //Get hold of the registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log the token
        //Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }
    private void sendRegistrationToServer(String token) {
        try {
            URL url = new URL("http://homebrain.bubulescu.org/api/fcm/reg/" + token);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setRequestProperty("User-Agent", "homebrainapp/0.1");
            httpCon.setRequestMethod("PUT");
            httpCon.setDoOutput(true);
            httpCon.getInputStream();
            Log.d(TAG, "Registered: http://homebrain.bubulescu.org/api/fcm/reg/" + token);
        }
        catch (MalformedURLException ex) {
            Log.d(TAG, Log.getStackTraceString(ex));
        }
        catch (IOException ex) {
            Log.d(TAG, Log.getStackTraceString(ex));
        }
    }
}
