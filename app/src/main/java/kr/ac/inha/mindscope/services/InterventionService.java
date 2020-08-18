package kr.ac.inha.mindscope.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import java.util.Calendar;

import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;

import static android.app.Notification.CATEGORY_ALARM;
import static android.app.Notification.PRIORITY_LOW;
import static kr.ac.inha.mindscope.Tools.PATH_NOTIFICATION;
import static kr.ac.inha.mindscope.Tools.STRESS_DO_DIFF_INTERVENTION;
import static kr.ac.inha.mindscope.Tools.STRESS_DO_INTERVENTION;
import static kr.ac.inha.mindscope.Tools.STRESS_MUTE_TODAY;
import static kr.ac.inha.mindscope.Tools.STRESS_NEXT_TIME;
import static kr.ac.inha.mindscope.Tools.ZATURI_DIFF_INT_NOTIFICATION_ID;
import static kr.ac.inha.mindscope.Tools.ZATURI_NOTIFICATION_ID;
import static kr.ac.inha.mindscope.Tools.saveStressIntervention;
import static kr.ac.inha.mindscope.services.MainService.ZATURI_RESPONSE_EXPIRE_TIME;

public class InterventionService extends Service {

    private static final String TAG = "InterventionService";

    public InterventionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Calendar cal = Calendar.getInstance();

        boolean action_next_time = intent.getBooleanExtra("stress_next_time", false);
        boolean action_mute_today = intent.getBooleanExtra("stress_mute_today", false);
        boolean action_do_intervention = intent.getBooleanExtra("stress_do_intervention", false);
        boolean action_diff_int = intent.getBooleanExtra("stress_diff_int", false);
        boolean action_diff_int_done = intent.getBooleanExtra("stress_diff_int_done", false);
        int path = intent.getIntExtra("path", -1);

        SharedPreferences prefs = getSharedPreferences("intervention", MODE_PRIVATE);
        String curIntervention = prefs.getString("curIntervention", "");

        if(action_next_time){
            saveStressIntervention(this, System.currentTimeMillis(), curIntervention, STRESS_NEXT_TIME, PATH_NOTIFICATION);
        }
        else if(action_mute_today){
//            saveStressIntervention(this, System.currentTimeMillis(), curIntervention, STRESS_NEXT_TIME, path);
            saveStressIntervention(this, System.currentTimeMillis(), curIntervention, STRESS_MUTE_TODAY, PATH_NOTIFICATION);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("muteToday", true);
            Log.d(TAG, "mute today is ture");
            editor.putInt("muteDate", cal.get(Calendar.DATE));
            editor.apply();
        }
        else if(action_do_intervention){
            saveStressIntervention(this, System.currentTimeMillis(), curIntervention, STRESS_DO_INTERVENTION, PATH_NOTIFICATION);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("didIntervention", true);
            editor.apply();
            Toast.makeText(getApplicationContext(),
                    getApplicationContext().getString(R.string.string_do_intervention_toast),
                    Toast.LENGTH_LONG).show();
        } else {
            Context con =  getApplicationContext();
            final NotificationManager notificationManager = (NotificationManager)
                    con.getSystemService(Context.NOTIFICATION_SERVICE);
            if (action_diff_int) {
                Log.d("ZATURI", "ACTION_DIFF_INT");
                Intent diffIntIntent = new Intent(con, InterventionService.class);
                diffIntIntent.putExtra("stress_diff_int_done", true);
                diffIntIntent.putExtra("path", 1);

                RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply")
                        .setLabel(con.getResources().getString(R.string.enter_diff_int))
                        .build();
                PendingIntent diffIntPI = PendingIntent.getService(con,
                        4, diffIntIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_edit_24px,
                        "다른 해소 방안 입력", diffIntPI)
                        .addRemoteInput(remoteInput)
                        .build();

                RemoteViews notificationLayout = new RemoteViews(con.getPackageName(), R.layout.notification_different_intervention);
                notificationLayout.setTextViewText(R.id.textIntervention, curIntervention);

                String channelId = con.getString(R.string.notif_channel_id);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        con.getApplicationContext(), channelId);
                builder.setContentTitle(con.getString(R.string.app_name))
                        .setTimeoutAfter(1000 * ZATURI_RESPONSE_EXPIRE_TIME)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setContent(notificationLayout)
                        .setCustomContentView(notificationLayout)
                        .setSmallIcon(R.mipmap.ic_launcher_low_foreground)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(true)
                        .setOngoing(false)
                        .addAction(action);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId,
                            con.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(channel);
                    }
                }

                final Notification notification = builder.build();
                Log.d("ZATURI", "CHECKPOINT 1");
                if (notificationManager != null) {
                    Log.d("ZATURI", "CHECKPOINT 2");
                    notificationManager.notify(ZATURI_NOTIFICATION_ID, notification);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("zaturiLastIntervention", System.currentTimeMillis());
                    editor.apply();
//                saveStressIntervention(con, System.currentTimeMillis(), curIntervention,
//                        STRESS_PUSH_NOTI_SENT, PATH_NOTIFICATION);
                }
            } else if (action_diff_int_done) {
                Log.d("ZATURI", "Different intervention");
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                if (remoteInput != null) {
                    String diffIntervention = remoteInput.getCharSequence("key_text_reply").toString();

                    RemoteViews notificationLayout = new RemoteViews(con.getPackageName(),
                            R.layout.notification_different_intervention);
                    notificationLayout.setTextViewText(R.id.textIntervention,
                            curIntervention);
                    String channelId = con.getString(R.string.notif_channel_id);
                    Notification repliedNotification = new NotificationCompat.Builder(con, channelId)
                            .setTimeoutAfter(1000)
                            .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                            .setContent(notificationLayout)
                            .setCustomContentView(notificationLayout)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setSmallIcon(R.mipmap.ic_launcher_low_foreground)
                            .setNotificationSilent()
                            .build();

                    notificationManager.notify(ZATURI_NOTIFICATION_ID, repliedNotification);
                    Toast.makeText(getApplicationContext(),
                            getApplicationContext().getString(R.string.string_do_intervention_toast),
                            Toast.LENGTH_LONG).show();

                    if (diffIntervention.charAt(0) != '#') diffIntervention = String.format("#%s", diffIntervention);
                    diffIntervention = diffIntervention.replace(" ", "_");
                    saveStressIntervention(this, System.currentTimeMillis(), diffIntervention,
                            STRESS_DO_DIFF_INTERVENTION, PATH_NOTIFICATION);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("didIntervention", true);
                    editor.apply();
                    Intent openMindscope = new Intent(con, MainActivity.class);
                    openMindscope.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    openMindscope.putExtra("change_intervention", true);
                    startActivity(openMindscope);
                }
            }
        }

        intent.removeExtra("stress_next_time");
        intent.removeExtra("stress_mute_today");
        intent.removeExtra("stress_do_intervention");
        intent.removeExtra("stress_diff_int");
        intent.removeExtra("stress_diff_int_done");
        intent.removeExtra("path");

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationManagerCompat notificationManager = getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            if (!action_diff_int && !action_diff_int_done) notificationManager.cancel(ZATURI_NOTIFICATION_ID);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.d("ZATURI", "DESTROY SERVICE");
    }
}
