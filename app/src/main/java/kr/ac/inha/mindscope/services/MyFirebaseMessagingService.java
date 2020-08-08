package kr.ac.inha.mindscope.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;

import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV1;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV2;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV3;
import static kr.ac.inha.mindscope.services.MainService.EMA_NOTI_ID;
import static kr.ac.inha.mindscope.services.MainService.EMA_RESPONSE_EXPIRE_TIME;

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
