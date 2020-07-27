package kr.ac.inha.mindscope.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

import static kr.ac.inha.mindscope.Tools.PATH_NOTIFICATION;
import static kr.ac.inha.mindscope.Tools.STRESS_DO_INTERVENTION;
import static kr.ac.inha.mindscope.Tools.STRESS_MUTE_TODAY;
import static kr.ac.inha.mindscope.Tools.STRESS_NEXT_TIME;
import static kr.ac.inha.mindscope.Tools.ZATURI_NOTIFICATION_ID;
import static kr.ac.inha.mindscope.Tools.saveStressIntervention;

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
        int path = intent.getIntExtra("path", -1);

        Log.e(TAG, "next time: " + action_next_time);
        Log.e(TAG, "mute today: " + action_mute_today);
        Log.e(TAG, "do intervention: " + action_do_intervention);
        Log.e(TAG, "path: " + path);

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
            Log.e(TAG, "mute today is ture");
            editor.putInt("muteDate", cal.get(Calendar.DATE));
            editor.apply();
        }
        else if(action_do_intervention){
            saveStressIntervention(this, System.currentTimeMillis(), curIntervention, STRESS_DO_INTERVENTION, PATH_NOTIFICATION);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("didIntervention", true);
            editor.apply();
        }

        intent.removeExtra("stress_next_time");
        intent.removeExtra("stress_mute_today");
        intent.removeExtra("stress_do_intervention");
        intent.removeExtra("path");

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(ZATURI_NOTIFICATION_ID);
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
        Log.e(TAG, "intervention service destory");
    }
}
