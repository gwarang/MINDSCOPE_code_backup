package kr.ac.inha.mindscope.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import be.tarsos.dsp.SilenceDetector;
import kr.ac.inha.mindscope.DbMgr;

import static android.media.AudioManager.MODE_IN_CALL;
import static kr.ac.inha.mindscope.receivers.CallReceiver.AudioRunningForCall;

public class CallVoiceFeatureService extends AccessibilityService {
    private static  final String TAG = "AccessibilityService";
    private static SilenceDetector silenceDetector;
    AudioManager am;
    private static boolean duringCall = false;

    int audioMode;
    private final double SILENCE_THRESHOLD = -65.0D;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
//
//        Log.e(TAG, "Catch Event Package Name : " + accessibilityEvent.getPackageName());
//        Log.e(TAG, "Catch Event TEXT : " + accessibilityEvent.getText());
//        Log.e(TAG, "Catch Event ContentDescription : " + accessibilityEvent.getContentDescription());
//        Log.e(TAG, "Catch Event getSource : " + accessibilityEvent.getSource());
//        Log.e(TAG, "=========================================================================");

        if(audioMode == MODE_IN_CALL || audioMode == AudioManager.MODE_IN_COMMUNICATION) {
            Log.e(TAG, "통화중입니당");
        }
//        if(am != null){
//            audioMode = am.getMode();
//            if(audioMode == MODE_IN_CALL || audioMode == AudioManager.MODE_IN_COMMUNICATION){
//                Log.e(TAG, "통화중입니당");
//                if(!duringCall){
//                    silenceDetector = new SilenceDetector(SILENCE_THRESHOLD, false);
//                }
//                saveVoiceFeatureDuringCall();
//                duringCall = true;
//            }else{
//                duringCall = false;
//            }
//        }

    }

    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK; // take all events
        info.feedbackType = AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FEEDBACK_HAPTIC;
        info.notificationTimeout = 100;

        setServiceInfo(info);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    public void saveVoiceFeatureDuringCall(){
        Context con = getApplicationContext();
        if (silenceDetector.currentSPL() >= -110.0D) {
            SharedPreferences prefs = con.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
            if (DbMgr.getDB() == null)
                DbMgr.init(con);
            int dataSourceId = prefs.getInt("AUDIO_LOUDNESS", -1);
            assert dataSourceId != -1;
            long curTimestamp = System.currentTimeMillis();
            DbMgr.saveMixedData(dataSourceId, curTimestamp, 1.0f, curTimestamp, silenceDetector.currentSPL());
            Log.d(TAG, curTimestamp + " " + silenceDetector.currentSPL());
        }
    }

    @Override
    public void onInterrupt() {
        Log.e("TEST", "OnInterrupt");
    }
}
