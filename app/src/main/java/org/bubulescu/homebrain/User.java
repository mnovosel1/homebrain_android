package org.bubulescu.homebrain;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

public class User {

    private static final String TAG = "User_LOG_";

    private String configs;
    private boolean registered = false;
    private boolean verified = false;
    private Context mContext;

    public User(Context context)
    {
        mContext = context;

        SharedPreferences configs = mContext.getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE);
        if ( configs.getString("token", null) != null ) registered = true;
        if ( configs.getBoolean("verified", false) != false ) verified = true;
    }

    public boolean isRegistered()
    {
        return registered;
    }

    public boolean isVerified() { return verified; }

    public void register(String email)
    {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE).edit();
        String token = FirebaseInstanceId.getInstance().getToken();

        editor.putString("email", email);
        editor.putString("token", token);
        editor.commit();

        HttpReqHelper httpReq = new HttpReqHelper();
        httpReq.sendReq("fcm/reg/" + email + MainActivity.DELIMITER + token);

        registered = true;
        Toast.makeText(mContext,"CODE je mailan na: " + email, Toast.LENGTH_LONG).show();
    }

    public void verify(String code)
    {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE).edit();

        // TODO: verify entered CODE

        editor.putBoolean("verified", true);
        editor.commit();

        HttpReqHelper httpReq = new HttpReqHelper();

        verified = true;
        Toast.makeText(mContext,"CODE OK", Toast.LENGTH_LONG).show();
    }
}
