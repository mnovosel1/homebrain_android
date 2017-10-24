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

    private Context mContext;
    private SharedPreferences configs;
    private HttpReqHelper httpReq;

    public User(Context context) {

        mContext = context;
        configs = mContext.getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE);
        httpReq = new HttpReqHelper(mContext);
    }

    public boolean isRegistered() {
        return (configs.getString("token", null) != null);
    }

    public boolean isVerified() {
        return (configs.getString("pages", null) != null);
    }

    public void register(String email) {

        String token = FirebaseInstanceId.getInstance().getToken();
        String regData = "{\"email\": \"" + email + "\", \"token\": \"" + token + "\"}";

        httpReq.sendReq("fcm", "reg", regData);

        MainActivity.saveConfigs(regData);
    }

    public void verify(String code) {

        httpReq.sendReq("fcm", "verify", new String("{\"code\": \"" + code + "\", \"email\": \""+ email() +"\"}"));

        Toast.makeText(mContext, "Please wait for verification...", Toast.LENGTH_LONG).show();
    }

    public String email() {
        return configs.getString("email", null);
    }

    public String pages() {
        return MainActivity.getConfig("pages");
    }
}
