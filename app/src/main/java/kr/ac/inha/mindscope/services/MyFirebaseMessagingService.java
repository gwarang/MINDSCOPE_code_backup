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
//
//        if(step == 1 || step == 2)
//            sendNotification(step);
        Log.d(TAG, "FCM noti is arrived");
    }

    private void sendNotification(int step) {
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        Intent intent = new Intent(this, MainActivity.class); // 애초에 Main으로 보내버리면 step에 관계없이 노티 도착 시간에 main에서 ema와 stressreport 실행이 가능
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        final String channelId = this.getString(R.string.notif_channel_id);
        builder = new NotificationCompat.Builder(this.getApplicationContext(), channelId);
        builder.setContentTitle(this.getString(R.string.app_name))
                .setTimeoutAfter(1000 * EMA_RESPONSE_EXPIRE_TIME)
                .setTicker("New Message Alert!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_low_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);
        if(step == 1){
            builder.setContentText(this.getString(R.string.daily_notif_text));
        }else{
            SharedPreferences prefs = getSharedPreferences("stressReport", MODE_PRIVATE);
            int stressLv = prefs.getInt("reportAnswer", 3);
            switch (stressLv){
                case STRESS_LV1:
                    builder.setContentText(this.getString(R.string.daily_notif_report_low_text));
                    break;
                case STRESS_LV2:
                    builder.setContentText(this.getString(R.string.daily_notif_report_littlehigh_text));
                    break;
                case STRESS_LV3:
                    builder.setContentText(this.getString(R.string.daily_notif_report_high_text));
                    break;
                default:
                    builder.setContentText(this.getString(R.string.daily_notif_report_text));
                    break;
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, this.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        final Notification notification = builder.build();
        if (notificationManager != null) {
            notificationManager.notify(EMA_NOTI_ID, notification);
        }
    }
}
