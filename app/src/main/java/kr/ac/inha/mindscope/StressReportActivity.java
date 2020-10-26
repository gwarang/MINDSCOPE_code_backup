package kr.ac.inha.mindscope;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.protobuf.ByteString;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.fragment.StressReportFragment1;

import static kr.ac.inha.mindscope.Tools.PREDICTION_ORDER_INDEX;
import static kr.ac.inha.mindscope.services.StressReportDownloader.STRESS_PREDICTION_RESULT;


public class StressReportActivity extends AppCompatActivity {

    private static final String TAG = "StressReportActivity";
    private static final String SAVE = "저장";
    private static final String DONE = "완료";
    public static final int STRESS_LV1 = 0;
    public static final int STRESS_LV2 = 1;
    public static final int STRESS_LV3 = 2;
    public static final int REPORTNUM1 = 1;
    public static final int REPORTNUM2 = 2;
    public static final int REPORTNUM3 = 3;
    public static final int REPORTNUM4 = 4;
    public static final Short[] REPORT_NOTIF_HOURS = {11, 15, 19, 23};  //in hours of day

    private static final int LAST_SYNC_TIME_THRESHOLD = 60 * 5; // sec

    public static int stressLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stres_report);

        forceSyncIfLastDownloadIsOld();


        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.frameLayout, StressReportFragment1.newInstance(stressLevel)).commit();

    }

    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment).commit();
    }


    private void forceSyncIfLastDownloadIsOld(){
        Log.d(TAG, "stress prediction synchronized");
        Calendar cal = Calendar.getInstance();
        SharedPreferences stressReportPrefs = getSharedPreferences("stressReport", MODE_PRIVATE);

        SharedPreferences.Editor stressReportPrefsEditor = stressReportPrefs.edit();

        long fromTimestamp = stressReportPrefs.getLong("lastDownloadTime", 0);
        long tillTimestamp = cal.getTimeInMillis();


        if (tillTimestamp - fromTimestamp > LAST_SYNC_TIME_THRESHOLD  && Tools.isNetworkAvailable()) {
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
                            stressReportStr = values.get(i).toString();
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



}