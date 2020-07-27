package kr.ac.inha.mindscope.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
import kr.ac.inha.mindscope.BuildConfig;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;

import static android.content.Context.MODE_PRIVATE;

public class AppUseNotifierReceiver extends BroadcastReceiver {
    private final static String CHANNEL_ID = "kr.ac.inha.nsl.ET_Notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);

        Intent notificationIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(100, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", MODE_PRIVATE);
        long lastTimestamp = loginPrefs.getLong("lastUsageTimestamp", -1);
        if (BuildConfig.DEBUG && lastTimestamp == -1)
            throw new AssertionError("Assertion failed : sharedprefs doesn't contain lastUsageTimestamp");

        Calendar lastCalendar = Calendar.getInstance();
        lastCalendar.setTimeInMillis(lastTimestamp);
        Date lastDate = lastCalendar.getTime();
        Date nowDate = Calendar.getInstance().getTime();
        long diffInMillis = lastDate.getTime() - nowDate.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        Notification notification = builder.setContentTitle("MindScope-App Reminder")
                .setContentText(String.format(Locale.getDefault(), "You didn't use the app for %d days =) Please check it out!", diffInDays))
                .setTicker("New MindScope Notification!")
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher_low_foreground)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ET_Notification";
            String description = "EasyTrack platform notification channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

