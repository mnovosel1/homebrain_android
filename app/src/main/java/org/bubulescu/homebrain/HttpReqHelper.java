package org.bubulescu.homebrain;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HttpReqHelper {

    private static final String TAG = "HttpReqHelper_LOG_";

    private static String protocolAway, urlAway, connAway;
    private static String protocolHome, urlHome, connHome;
    private static String connNone;
    private static Integer portAway, portHome;
    private String baseUrl;

    Context mContext;

    HttpReqHelper (Context c) {

        mContext = c;
        setPrefs(mContext);
    }

    public static void setPrefs(Context c) {

        protocolAway = c.getString(R.string.protocolAway);
        urlAway = c.getString(R.string.urlAway);
        portAway = Integer.parseInt(c.getString(R.string.portAway));
        connAway = c.getString(R.string.connAway);

        protocolHome = c.getString(R.string.protocolHome);
        urlHome = c.getString(R.string.urlHome);
        portHome = Integer.parseInt(c.getString(R.string.portHome));
        connHome = c.getString(R.string.connHome);

        connNone = c.getString(R.string.connNone);
    }

    public static String checkConn(Context c) {

        String connIs;
        setPrefs(c);

        if ( isLive(urlAway, portAway, 128) ) {
            connIs = connAway;
        } else if ( isLive(urlHome, portHome, 128) ) {
            connIs = connHome;
        } else connIs = connNone;

        MainActivity.sendBcastMsg(c, new String("{\"runOnWebView\": \"notice('" + connIs + "')\"}"));

        Log.d(TAG, "{\"runOnWebView\": \"notice('" + connIs + "')\"}");

        return connIs;
    };

    public void sendReq(final String name, final String verb, final Map<String, String> arguments) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String connIs = checkConn(mContext);

                if ( connIs == connHome ) baseUrl = protocolHome + "://" + urlHome + ":" + portHome;
                else if ( connIs == connAway ) baseUrl = protocolAway + "://" + urlAway + ":" + portAway;

                if ( connIs == connHome || connIs == connAway ) {
                    try {
                        URL url = new URL(baseUrl + "/api/" + name + "/" + verb);
                        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                        httpCon.setReadTimeout(10000);
                        httpCon.setConnectTimeout(15000);
                        httpCon.setRequestMethod("POST");
                        httpCon.setDoInput(true);
                        httpCon.setDoOutput(true);

                        Uri.Builder builder = new Uri.Builder().appendQueryParameter("secToken", getToken());

                        for ( Map.Entry<String, String> arg : arguments.entrySet() ) {
                            builder.appendQueryParameter(arg.getKey(), arg.getValue());
                        }

                        String query = builder.build().getEncodedQuery();

                        OutputStream os = httpCon.getOutputStream();

                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        writer.write(query);
                        writer.flush();
                        writer.close();
                        os.close();

                        httpCon.getInputStream();

                        Log.d(TAG, "HttpResponse: " + httpCon.getResponseMessage() + " HttpRequested: " + baseUrl + arguments);
                    } catch (MalformedURLException ex) {
                        Log.d(TAG, Log.getStackTraceString(ex));
                    } catch (IOException ex) {
                        Log.d(TAG, Log.getStackTraceString(ex));
                    }
                }
            }
        }).start();
    }

    private String getToken() {
        int tstamp = (int) ((System.currentTimeMillis() / 1000) / 20);
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isLive(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }
}
