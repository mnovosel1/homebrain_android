package org.bubulescu.homebrain;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyAndroidFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "FCMIID_LOG";

    @Override
    public void onTokenRefresh() {
        //Get hold of the registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, "TokenRefreshed: " + refreshedToken);
        registerToken(refreshedToken);
    }

    private void registerToken(String token){
        Intent intent = new Intent();
        intent.setAction(MainActivity.SENDMESAGGE);
        intent.putExtra("token", token);
        sendBroadcast(intent);
    }
}
