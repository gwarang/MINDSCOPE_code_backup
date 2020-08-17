package kr.ac.inha.mindscope.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Objects;

import kr.ac.inha.mindscope.DbMgr;

import static kr.ac.inha.mindscope.Tools.sendStressInterventionNoti;


public class ScreenAndUnlockReceiver extends BroadcastReceiver {
    public static final String TAG = "ScreenAndUnlockReceiver";

    private long phoneUnlockedDurationStart = System.currentTimeMillis();
    private long screenONStartTime = System.currentTimeMillis();
    private boolean unlocked = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_USER_PRESENT)) {
            Log.d(TAG, "Phone unlocked");
            unlocked = true;
            phoneUnlockedDurationStart = System.currentTimeMillis();
        } else if (Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
            Log.d(TAG, "Phone locked / Screen OFF");
            //region Handling phone locked state
            if (unlocked) {
                unlocked = false;
                long phoneUnlockedDurationEnd = System.currentTimeMillis();
                long phoneUnlockedDuration = (phoneUnlockedDurationEnd - phoneUnlockedDurationStart) / 1000; // in seconds
                SharedPreferences prefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
                if (DbMgr.getDB() == null)
                    DbMgr.init(context);

                int dataSourceId = prefs.getInt("UNLOCK_DURATION", -1);
                assert dataSourceId != -1;
                Log.d(TAG, "STORING DATA :" + dataSourceId + " " + phoneUnlockedDuration);
                DbMgr.saveMixedData(dataSourceId, phoneUnlockedDurationStart, 1.0f, phoneUnlockedDurationStart, phoneUnlockedDurationEnd, phoneUnlockedDuration);

                /* Zaturi starts */
                SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", Context.MODE_PRIVATE);

                if (loginPrefs.getBoolean("zaturiTimerOn", false)) {
                    Log.d("ZATURI", "Different package name");
                    // Check if the usage duration < 30 sec
                    if (System.currentTimeMillis() -
                            loginPrefs.getLong("zaturiTimerStart", 0) < 30000) {
                        Log.d("ZATURI", "SEND NOTI");
                        // Then send a notification
                        sendStressInterventionNoti(context);
                    }
                    // Reset the variables
                    SharedPreferences.Editor editor = loginPrefs.edit();
                    editor.putBoolean("zaturiTimerOn", false);
                    editor.putLong("zaturiTimerStart", 0);
                    editor.putString("zaturiPackage", "");

                    // Save last phone usage time (except for communication app usage)
                    editor.putLong("zaturiLastPhoneUsage", System.currentTimeMillis());
                    editor.apply();
                }

                /* Zaturi ends */
            }
            //endregion

            //region Handling screen OFF state
            long screenONEndTime = System.currentTimeMillis();
            long screenOnDuration = (screenONEndTime - screenONStartTime) / 1000; //seconds
            SharedPreferences prefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
            int dataSourceId = prefs.getInt("SCREEN_ON_OFF", -1);
            assert dataSourceId != -1;
            DbMgr.saveMixedData(dataSourceId, screenONStartTime, 1.0f, screenONStartTime, screenONEndTime, screenOnDuration);
            //endregion

        } else if (Objects.equals(intent.getAction(), Intent.ACTION_SCREEN_ON)) {
            Log.d(TAG, "Screen ON");
            screenONStartTime = System.currentTimeMillis();
        }
    }
}
