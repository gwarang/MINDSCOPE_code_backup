package kr.ac.inha.mindscope.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

import kr.ac.inha.mindscope.services.MainService;

public class DateChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "DateChangeReceiver";
    private int[] restartHours = new int[]{6, 12, 18};
    private int[] retrieveStressReportHours = new int[]{10,14,18,22};


    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(Intent.ACTION_TIME_TICK)) {
            Log.e(TAG, "minute is changed");
            Calendar curCal = Calendar.getInstance();
            int curDate = curCal.get(Calendar.DATE);
            SharedPreferences datePrefs = context.getSharedPreferences("DatePrefs", Context.MODE_PRIVATE);
            int oldDate = datePrefs.getInt("date", 0);
            if (oldDate != curDate) {
                SharedPreferences.Editor editor = datePrefs.edit();
                editor.putInt("date", curDate);
                editor.apply();
                Log.e(TAG, "DATE IS CHANGED");

                Intent intentService = new Intent(context, MainService.class);
                context.stopService(intentService);
                SharedPreferences configPrefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);

                if (configPrefs.getLong("startTimestamp", 0) <= System.currentTimeMillis()) {
                    Log.e(TAG, "RESTART SERVICE");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intentService);
                    } else {
                        context.startService(intentService);
                    }
                }

            }
            for(int hour : restartHours){
                if(curCal.get(Calendar.HOUR_OF_DAY) == hour && curCal.get(Calendar.MINUTE) == 0){
                    Log.e(TAG, "restart at " + hour + " O'clock");
                    Intent intentService = new Intent(context, MainService.class);
                    context.stopService(intentService);
                    SharedPreferences configPrefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);

                    if (configPrefs.getLong("startTimestamp", 0) <= System.currentTimeMillis()) {
                        Log.e(TAG, "RESTART SERVICE");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intentService);
                        } else {
                            context.startService(intentService);
                        }
                    }
                }
            }
            for(int hour : retrieveStressReportHours){
                if(curCal.get(Calendar.HOUR_OF_DAY) == hour && curCal.get(Calendar.MINUTE) == 55){
                    Log.e(TAG, "time to take stress report");
                    Intent intentService = new Intent(); // TODO new service

                }
            }
        }
    }
}
