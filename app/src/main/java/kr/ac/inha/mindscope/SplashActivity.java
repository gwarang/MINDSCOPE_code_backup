package kr.ac.inha.mindscope;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.protobuf.ByteString;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import static kr.ac.inha.mindscope.Tools.PREDICTION_ORDER_INDEX;
import static kr.ac.inha.mindscope.services.StressReportDownloader.STRESS_PREDICTION_RESULT;

public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";
    private final int SPLASH_DISPLAY_TIME = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Context context = getApplicationContext();

        Log.d("SplashActivity", "onCreate");

        SharedPreferences lastPagePrefs = getSharedPreferences("LastPage", MODE_PRIVATE);
        SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
        lastPagePrefsEditor.putString("last_open_nav_frg", "me");
        lastPagePrefsEditor.putInt("last_open_tab_position", 0);
        lastPagePrefsEditor.apply();


        Handler handler = new Handler();
        handler.postDelayed(() -> {
            SharedPreferences stepChangePrefs = getSharedPreferences("stepChange", MODE_PRIVATE);
            if (stepChangePrefs.getInt("stepCheck", 0) == 2) {
                Log.d(TAG, "stress prediction synchronized");
                Calendar cal = Calendar.getInstance();
                SharedPreferences stressReportPrefs = getSharedPreferences("stressReport", MODE_PRIVATE);

                SharedPreferences.Editor stressReportPrefsEditor = stressReportPrefs.edit();

                long fromTimestamp = stressReportPrefs.getLong("lastDownloadTime", 0);
                long tillTimestamp = cal.getTimeInMillis();


                if (Tools.isNetworkAvailable()) {
                    String stressReportStr;
                    SharedPreferences loginPrefs = getSharedPreferences("UserLogin", MODE_PRIVATE);
                    SharedPreferences configPrefs = getSharedPreferences("Configurations", MODE_PRIVATE);

                    ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

                    ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

                    EtService.RetrieveFilteredDataRecords.Request retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                            .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                            .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                            .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                            .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                            .setTargetDataSourceId(configPrefs.getInt("STRESS_PREDICTION", -1))
                            .setFromTimestamp(fromTimestamp) //  fromTimestamp
                            .setTillTimestamp(tillTimestamp)
                            .build();
                    try {
                        final EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
                        if (responseMessage.getSuccess()) {
                            // checkByteString
                            List<ByteString> values = responseMessage.getValueList();
                            List<Long> valuesTimestamp = responseMessage.getTimestampList();
                            if (!values.isEmpty()) {
                                for (int i = 0; i < values.size(); i++) {
                                    stressReportStr = values.get(i).substring(1,values.get(i).size()-1).toString("UTF-8");
                                    long timestamp = valuesTimestamp.get(i);
                                    try {
                                        JSONObject stressReportJSON = new JSONObject(stressReportStr);
                                        for (short stressLv = 0; stressLv < 3; stressLv++) {
                                            JSONObject eachLevelJSON = new JSONObject(stressReportJSON.getString(String.valueOf(stressLv)));
                                            String oneReportWithTimestamp = String.format(Locale.getDefault(), "%d,%d,%d,%d,%.2f,%s,%b\n",
                                                    timestamp,
                                                    stressLv,
                                                    eachLevelJSON.getInt("day_num"),
                                                    eachLevelJSON.getInt("ema_order"),
                                                    eachLevelJSON.getDouble("accuracy"),
                                                    eachLevelJSON.getString("feature_ids"),
                                                    eachLevelJSON.getBoolean("model_tag"));
//                                            timestamp + "#" + stressLv + "#" + stressReportJSON.getString(String.valueOf(stressLv));
                                            String[] split = oneReportWithTimestamp.split(",");
                                            if (Integer.parseInt(split[PREDICTION_ORDER_INDEX]) > 0) {
                                                FileOutputStream fileOutputStream = openFileOutput(STRESS_PREDICTION_RESULT, MODE_APPEND);
                                                fileOutputStream.write(oneReportWithTimestamp.getBytes());
                                                fileOutputStream.close();
                                            }
                                            Log.d(TAG, oneReportWithTimestamp);
                                            if (eachLevelJSON.getBoolean("model_tag")) {
                                                stressReportPrefsEditor.putInt("reportAnswer", stressLv);
                                                stressReportPrefsEditor.apply();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                stressReportPrefsEditor.putLong("lastDownloadTime", valuesTimestamp.get(valuesTimestamp.size() - 1));
                                stressReportPrefsEditor.apply();
                            } else {
                                Log.d(TAG, "values empty");
                            }
                        }
                    } catch (IOException | StatusRuntimeException e) {
                        e.printStackTrace();
                    }
                    channel.shutdown();
                }

            }

            Intent intent = new Intent(this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            SplashActivity.this.finish();
        }, SPLASH_DISPLAY_TIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        // remove back pressed
    }
}