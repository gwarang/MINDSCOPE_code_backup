package kr.ac.inha.mindscope.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.SilenceDetector;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import kr.ac.inha.mindscope.DbMgr;
import kr.ac.inha.mindscope.Utils;

class AudioFeatureRecorder {
    // region Constants
    public static final String TAG = "AudioFeatureRecorder";
    private final int SAMPLING_RATE = 11025;
    private final int AUDIO_BUFFER_SIZE = 1024;
    private final double SILENCE_THRESHOLD = -65.0D;
    // endregion

    // region Variables
    private boolean started;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private AudioDispatcher dispatcher;
    // endregion

    AudioFeatureRecorder(final Context con) {
        Utils.logThreadSignature(TAG + " AudioFeatureRecorder constructor");
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLING_RATE, AUDIO_BUFFER_SIZE, 512);
        final SilenceDetector silenceDetector = new SilenceDetector(SILENCE_THRESHOLD, false);

        AudioProcessor mainAudioProcessor = new AudioProcessor() {

            @Override
            public void processingFinished() {
                Utils.logThreadSignature(TAG + " AudioFeatureRecorder processingFinished()");
            }

            @Override
            public boolean process(AudioEvent audioEvent) {
//                Utils.logThreadSignature(TAG + " AudioFeatureRecorder process");
                if (silenceDetector.currentSPL() >= -110.0D) {
                    SharedPreferences prefs = con.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
                    if (DbMgr.getDB() == null)
                        DbMgr.init(con);
                    int dataSourceId = prefs.getInt("AUDIO_LOUDNESS", -1);
                    assert dataSourceId != -1;
                    long curTimestamp = System.currentTimeMillis();
                    DbMgr.saveMixedData(dataSourceId, curTimestamp, 1.0f, curTimestamp, silenceDetector.currentSPL());
//                    Log.d(TAG, curTimestamp + " " + silenceDetector.currentSPL() + "");
                }
                return true;
            }
        };


        if (dispatcher == null)
            Log.d(TAG, "Dispatcher is NULL: ");
        dispatcher.addAudioProcessor(silenceDetector);
        dispatcher.addAudioProcessor(mainAudioProcessor);
    }

    void start() {
        Log.d(TAG, "Started: AudioFeatureRecorder");
        executor.execute(dispatcher);
        started = true;
    }

    void stop() {
        Log.d(TAG, "Stopped: AudioFeatureRecorder, started: " + started);
        if (started) {
            Log.d(TAG, "call dispatcher.stop()");
            dispatcher.stop();
            started = false;
        }
    }
}
