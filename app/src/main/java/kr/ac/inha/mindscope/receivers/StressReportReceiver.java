package kr.ac.inha.mindscope.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import kr.ac.inha.mindscope.services.StressReportDownloader;

public class StressReportReceiver extends BroadcastReceiver {

    private static final String TAG = "SReportReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(StressReportDownloader.class).build();
        WorkManager.getInstance(context).enqueue(oneTimeWorkRequest);
    }

}
