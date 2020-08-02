package kr.ac.inha.mindscope.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import kr.ac.inha.mindscope.services.MainService;

public class UndeadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            Intent in = new Intent(context, MainService.class);
            context.startForegroundService(in);
        }else {
            Intent in = new Intent(context, MainService.class);
            context.startService(in);
        }
    }
}
