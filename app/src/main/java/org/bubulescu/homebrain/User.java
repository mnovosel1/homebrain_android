package org.bubulescu.homebrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class User {

    private static final String TAG = "User_LOG_";

    private boolean registered = false;
    private boolean verified = false;
    private boolean configured = true;
    private Context mContext;
    private SharedPreferences configs;

    public User(Context context) {
        mContext = context;

        configs = mContext.getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE);
        if (configs.getString("token", null) != null) registered = true;
        if (configs.getBoolean("verified", false) != false) verified = true;
    }

    public boolean isRegistered() {
        return registered;
    }

    public boolean isConfigured() {
        return configured;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean OK() { return registered && configured && verified; }

    public void register(String email) {

        String token = FirebaseInstanceId.getInstance().getToken();

        SharedPreferences.Editor edit = configs.edit();
        edit.putString("email", email);
        edit.putString("token", token);
        edit.commit();

        HttpReqHelper httpReq = new HttpReqHelper(mContext);
        HashMap<String, String> arguments = new HashMap<String, String>();
        arguments.put("email", email);
        arguments.put("token", token);
        httpReq.sendReq("fcm", "reg", arguments);

        registered = true;
    }

    public void configure(JSONObject config) {

        SharedPreferences.Editor edit = configs.edit();
        String key, value;

        try {
                for (int i = 0; i<config.names().length(); i++) {
                    key = config.names().getString(i);
                    value = config.get(config.names().getString(i)).toString();
                    edit.putString(key, value);
                }
                edit.commit();
                configured = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

    public void verify(String code) {

        // TODO: verify entered CODE

        SharedPreferences.Editor edit = configs.edit();
        edit.putBoolean("verified", true);
        edit.commit();

        HttpReqHelper httpReq = new HttpReqHelper(mContext);

        verified = true;
        Toast.makeText(mContext, "CODE OK", Toast.LENGTH_LONG).show();
    }

    public String email() {
        SharedPreferences configs = mContext.getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE);
        return configs.getString("email", null);
    }
}
