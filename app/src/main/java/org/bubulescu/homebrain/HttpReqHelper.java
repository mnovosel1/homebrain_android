package org.bubulescu.homebrain;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class HttpReqHelper {

    private static final String TAG = "HttpRH_LOG_";

    private static String protocolAway, urlAway, connAway;
    private static String protocolHome, urlHome, connHome;
    private static String connNone;
    private static Integer portAway, portHome;
    private static final int BUFFER_SIZE = 4096;
    private static String baseUrl;

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

        return connIs;
    };

    public static void downloadFile(String fileUrl, String dirDst) {

        sendReq("app", null, null, "FILE_DOWNLOADED", fileUrl, dirDst);
    }

    public static void sendReq(final String name, final String verb, final String arguments, final String bcMessage) {

        sendReq("api/" + name, verb, arguments, bcMessage, null, null);
    }

    public static void sendReq(final String name, final String verb, final String arguments, final String bcMessage, final String fileUpd, final String dirDst) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String connIs = checkConn(HbApp.getAppContext());

                if ( connIs == connHome ) baseUrl = protocolHome + "://" + urlHome + ":" + portHome;
                else if ( connIs == connAway ) baseUrl = protocolAway + "://" + urlAway + ":" + portAway;

                if ( connIs == connHome || connIs == connAway ) {
                    HttpURLConnection httpCon = null;
                    try {

                        URL url;
                        if ( name != null && verb == null )  url = new URL(baseUrl + "/" + name + "/" + fileUpd);
                        else url = new URL(baseUrl + "/" + name + "/" + verb);

                        httpCon = (HttpURLConnection) url.openConnection();

                        try {
                            //httpCon.setReadTimeout(10000);
                            //httpCon.setConnectTimeout(15000);
                            httpCon.setRequestMethod("POST");
                            httpCon.setDoInput(true);
                            httpCon.setDoOutput(true);

                            Uri.Builder builder = new Uri.Builder().appendQueryParameter("secToken", getToken());

                            if ( arguments != null ) {

                                JSONObject args = new JSONObject(arguments);
                                for (int i = 0; i<args.names().length(); i++) {
                                    builder.appendQueryParameter(args.names().getString(i), args.get(args.names().getString(i)).toString());
                                }
                            }

                            String query = builder.build().getEncodedQuery();

                            OutputStream os = httpCon.getOutputStream();

                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(query);
                            writer.flush();
                            writer.close();
                            os.close();

                            InputStream is = httpCon.getInputStream();
                            int response = httpCon.getResponseCode();

                            // file download
                            if ( fileUpd != null & dirDst != null ) {

                                String saveFilePath = dirDst + fileUpd;

                                // opens an output stream to save into file
                                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                                int bytesRead = -1;
                                byte[] buffer = new byte[BUFFER_SIZE];
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }

                                Log.d(TAG + "sendReq", "HttpResponse: " + response + " - " + url);
                                Log.d(TAG + "sendReq", "saved to: " + saveFilePath);
                            }

                            else {
                                Log.d(TAG + "sendReq", "HttpResponse: " + response + " - " + url + "/" + name + "/" + verb);
                            }


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

                    } catch (IOException ex) {
                        Log.d(TAG + "sendReq", "TimeOut? " + baseUrl + Log.getStackTraceString(ex));
                    }
                }
            }
        }).start();
    }

    private static String getToken() {
        int tstamp = (int) ((System.currentTimeMillis() / 1000) / 20);
        String s = "HomeBrain";
        Random random = new Random();
        String letter = String.valueOf(s.charAt(random.nextInt(s.length())));

        return letter + tstamp + ": " + md5(letter + String.valueOf(tstamp));
    }

    @Nullable
    private static String md5(String in) {
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
        } catch (NoSuchAlgorithmException ex) {
            Log.d(TAG + "_md5", Log.getStackTraceString(ex));
        }
        return null;
    }

    public static boolean isLive(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (IOException ex) {
            Log.d(TAG + "_isLive: ", host + " - timeout");
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    public static String copyDirorfileFromAssetManager(String arg_assetDir, String arg_destinationDir) throws IOException {

        String dest_dir_path = addLeadingSlash(arg_destinationDir);
        File dest_dir = new File(arg_destinationDir);

        createDir(dest_dir);

        AssetManager asset_manager = HbApp.getAppContext().getAssets();
        String[] files = asset_manager.list(arg_assetDir);

        for (int i = 0; i < files.length; i++) {

            String abs_asset_file_path = addTrailingSlash(arg_assetDir) + files[i];
            String sub_files[] = asset_manager.list(abs_asset_file_path);

            if (sub_files.length == 0)
            {
                // It is a file
                String dest_file_path = addTrailingSlash(dest_dir_path) + files[i];
                copyAssetFile(abs_asset_file_path, dest_file_path);
            } else
            {
                // It is a sub directory
                copyDirorfileFromAssetManager(abs_asset_file_path, addTrailingSlash(arg_destinationDir) + files[i]);
            }
        }


        Log.d(TAG + "_cpAssets:", " copied..");

        return dest_dir_path;
    }

    public static void copyAssetFile(String assetFilePath, String destinationFilePath) throws IOException {

        InputStream in = HbApp.getAppContext().getAssets().open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }

    public static String addTrailingSlash(String path) {
        if (path.charAt(path.length() - 1) != '/') {

            path += "/";
        }
        return path;
    }

    public static String addLeadingSlash(String path) {
        if (path.charAt(0) != '/') {

            path = "/" + path;
        }
        return path;
    }

    public static void createDir(File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {

                throw new IOException("Can't create directory, a file is in the way");
            }
        } else {

            dir.mkdirs();
            if (!dir.isDirectory()) {

                throw new IOException("Unable to create directory");
            }
        }
    }
}
