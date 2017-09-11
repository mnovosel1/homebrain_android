package org.bubulescu.homebrain;

import android.net.Uri;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
            httpCon.setReadTimeout(10000);
            httpCon.setConnectTimeout(15000);
            httpCon.setRequestMethod("POST");
            httpCon.setDoInput(true);
            httpCon.setDoOutput(true);

            int tstamp = (int) ((System.currentTimeMillis()/1000)/30);
            Uri.Builder builder = new Uri.Builder().appendQueryParameter("token", md5("HomeBrain" + String.valueOf(tstamp)));
            //Uri.Builder builder = new Uri.Builder().appendQueryParameter("token", String.valueOf(tstamp));
            String query = builder.build().getEncodedQuery();

            OutputStream os = httpCon.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

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

    private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return null;
    }
}
