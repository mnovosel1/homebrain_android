package org.bubulescu.homebrain;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static org.bubulescu.homebrain.R.id.url;

public class HttpReqHelper {

    private static final String TAG = "HttpRH_LOG_";

    private static String protocolAway, urlAway, connAway;
    private static String protocolHome, urlHome, connHome;
    private static String connNone;
    private static Integer portAway, portHome;
    private String baseUrl;
    private Context context;

    private Handler handler;

    public HttpReqHelper (Context ctx) {

        context = ctx;
        setPrefs(ctx);
        handler = new Handler();
    }

    public static void setPrefs(Context ctx) {

        protocolAway = ctx.getString(R.string.protocolAway);
        urlAway = ctx.getString(R.string.urlAway);
        portAway = Integer.parseInt(ctx.getString(R.string.portAway));
        connAway = ctx.getString(R.string.connAway);

        protocolHome = ctx.getString(R.string.protocolHome);
        urlHome = ctx.getString(R.string.urlHome);
        portHome = Integer.parseInt(ctx.getString(R.string.portHome));
        connHome = ctx.getString(R.string.connHome);

        connNone = ctx.getString(R.string.connNone);
    }

    public static String checkConn(final Context ctx) {

        String connIs;
        setPrefs(ctx);

        if ( isLive(urlAway, portAway, 128) ) {
            connIs = connAway;
        } else if ( isLive(urlHome, portHome, 128) ) {
            connIs = connHome;
        } else connIs = connNone;

        MainActivity.sendBcastMsg(new String("{\"runOnWebView\": \"notice('" + connIs + "')\"}"));

        Log.d(TAG + "checkConn", "{\"runOnWebView\": \"notice('" + connIs + "')\"}");

        return connIs;
    };

    public void sendReq(final String name, final String verb, final String arguments) {
        sendReq(name, verb, arguments, null);
    }

    public void sendReq(final String name, final String verb, final String arguments, final String bcMessage) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String connIs = checkConn(HbApp.getAppContext());

                if ( connIs == connHome ) baseUrl = protocolHome + "://" + urlHome + ":" + portHome;
                else if ( connIs == connAway ) baseUrl = protocolAway + "://" + urlAway + ":" + portAway;

                if ( connIs == connHome || connIs == connAway ) {
                    HttpURLConnection httpCon = null;
                    try {

                        URL url = new URL(baseUrl + "/api/" + name + "/" + verb);
                        httpCon = (HttpURLConnection) url.openConnection();

                        try {
                            //httpCon.setReadTimeout(10000);
                            //httpCon.setConnectTimeout(15000);
                            httpCon.setRequestMethod("POST");
                            httpCon.setDoInput(true);
                            httpCon.setDoOutput(true);

                            Uri.Builder builder = new Uri.Builder().appendQueryParameter("secToken", getToken());

                            JSONObject args = new JSONObject(arguments);
                            for (int i = 0; i<args.names().length(); i++) {
                                builder.appendQueryParameter(args.names().getString(i), args.get(args.names().getString(i)).toString());
                            }

                            String query = builder.build().getEncodedQuery();

                            OutputStream os = httpCon.getOutputStream();

                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(query);
                            writer.flush();
                            writer.close();
                            os.close();

                            httpCon.getInputStream();

                            int response = httpCon.getResponseCode();
                            Log.d(TAG + "sendReq", "HttpResponse: " + response + " - " + baseUrl + arguments);

                            if ( bcMessage != null ) {
                                MainActivity.sendBcastMsg(new String("{'" + bcMessage + "': '"+ response +"'}"));
                            }

                        } catch (MalformedURLException e) {
                            Log.d(TAG + "sendReq", Log.getStackTraceString(e));
                        } catch (JSONException e) {
                            Log.d(TAG + "sendReq", Log.getStackTraceString(e));
                        } finally {
                            httpCon.disconnect();
                        }

                    } catch (IOException e) {
                        Log.d(TAG + "sendReq", "TimeOut? " + baseUrl + arguments);
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
