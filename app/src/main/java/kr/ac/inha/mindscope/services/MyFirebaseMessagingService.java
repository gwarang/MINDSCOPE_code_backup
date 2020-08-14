package kr.ac.inha.mindscope.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("FCM Log", "Refreshed token: " + token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        SharedPreferences stepChangePrefs = getSharedPreferences("stepChange", MODE_PRIVATE);
//        int step = stepChangePrefs.getInt("stepCheck", 0);
        if(remoteMessage.getNotification() != null){
            Log.d(TAG, "FCM channelId" + remoteMessage.getNotification().getChannelId());
        }
//        if(step == 1 || step == 2)
//            sendNotification(step);
        Log.d(TAG, "FCM noti is arrived");
    }

}
