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

    private Context context;
    private SharedPreferences configs;
    private HttpReqHelper httpReq;

    public User(Context ctx) {

        context = ctx;
        configs = context.getSharedPreferences(MainActivity.CONFIGS, Context.MODE_PRIVATE);
        httpReq = new HttpReqHelper(context);
    }

    public boolean isRegistered() {
        return (configs.getString("token", null) != null);
    }

    public boolean isVerified() {
        return (configs.getString("pages", null) != null);
    }

    public void verify(String code) {

        httpReq.sendReq("fcm", "verify", ("{\"code\": \"" + code + "\", \"email\": \""+ email() +"\"}"), ("verification"));
        Toast.makeText(context, context.getString(R.string.waitForVerification), Toast.LENGTH_LONG).show();
    }

    public String email() {
        return configs.getString("email", null);
    }
    public void setEmail(String email) { MainActivity.saveConfigs("{'email': '"+email+"'}"); }

    public String pages() {
        return MainActivity.getConfig(context, "pages");
    }
}
