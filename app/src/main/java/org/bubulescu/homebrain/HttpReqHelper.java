package org.bubulescu.homebrain;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.net.Socket;
import java.net.InetSocketAddress;

public class HttpReqHelper {

    private static final String TAG = "HttpReqHelper_LOG";

    private String baseUrlAway = "homebrain.bubulescu.org";
    private String baseUrlHome = "10.10.10.128";
    private String baseUrl;

    public void sendReq(final String arguments)
    {
        new Thread(new Runnable() { @Override public void run() {

            baseUrl = "http://" + baseUrlHome + "/api/";
            if ( !isLive(baseUrlHome, 80, 64) ) baseUrl = "http://" + baseUrlAway + "/api/";

            try {
                URL url = new URL(baseUrl + arguments);
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setReadTimeout(10000);
                httpCon.setConnectTimeout(15000);
                httpCon.setRequestMethod("POST");
                httpCon.setDoInput(true);
                httpCon.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder().appendQueryParameter("token", getToken());
                String query = builder.build().getEncodedQuery();

                OutputStream os = httpCon.getOutputStream();

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                httpCon.getInputStream();

                Log.d(TAG, "HttpResponse: " + httpCon.getResponseMessage()+ " HttpRequested: " + baseUrl + arguments);
            }
            catch (MalformedURLException ex) {
                Log.d(TAG, Log.getStackTraceString(ex));
            }
            catch (IOException ex) {
                Log.d(TAG, Log.getStackTraceString(ex));
            }
        } }).start();
        /*
        */
    }

    private String getToken()
    {
        int tstamp = (int) ((System.currentTimeMillis()/1000)/20);
        String s = "HomeBrain";
        Random random = new Random();
        String letter = String.valueOf(s.charAt(random.nextInt(s.length())));

        return letter + tstamp + ": " + md5(letter + String.valueOf(tstamp));
    }

    @Nullable
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

    public boolean isLive(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }
}
