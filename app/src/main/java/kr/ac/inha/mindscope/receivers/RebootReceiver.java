package kr.ac.inha.mindscope.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

import kr.ac.inha.mindscope.AuthenticationActivity;

public class RebootReceiver extends BroadcastReceiver {
    public static final String TAG = "RebootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)){
            Intent intentService = new Intent(context, AuthenticationActivity.class);
            intentService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentService.putExtra("fromReboot", true);
            context.startActivity(intentService);
        }
    }
}
