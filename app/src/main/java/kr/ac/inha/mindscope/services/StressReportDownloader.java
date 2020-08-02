package kr.ac.inha.mindscope.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;

import static kr.ac.inha.mindscope.Tools.PREDICTION_ORDER_INDEX;

public class StressReportDownloader extends Worker {

    private static final String TAG = "SRDownloader";
    public static final String STRESS_PREDICTION_RESULT = "stressReportResult.txt";
    public static final String SELF_STRESS_REPORT_RESULT = "selfStressReportResult.txt";


    public StressReportDownloader(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        ArrayList<Integer> validHours = new ArrayList<>();
        validHours.add(10);
        validHours.add(14);
        validHours.add(18);
        validHours.add(22);

        Context context = getApplicationContext();

        Calendar cal = Calendar.getInstance();
        SharedPreferences stressReportPrefs = context.getSharedPreferences("stressReport", Context.MODE_PRIVATE);
        if (stressReportPrefs.getBoolean("fromMain", false) || validHours.contains(cal.get(Calendar.HOUR_OF_DAY))) {
            SharedPreferences.Editor stressReportPrefsEditor = stressReportPrefs.edit();
            stressReportPrefsEditor.putBoolean("fromMain", false);
            long fromTimestamp = stressReportPrefs.getLong("lastDownloadTime", 0);
            long tillTimestamp = cal.getTimeInMillis();

            long fromTest = 1596289202000l;
            long tillTest = 1596290282000l;

            if (Tools.isNetworkAvailable()) {
                String stressReportStr;
                long reportTimestamp;
                SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                SharedPreferences configPrefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);

                ManagedChannel channel = ManagedChannelBuilder.forAddress(context.getString(R.string.grpc_host), Integer.parseInt(context.getString(R.string.grpc_port))).usePlaintext().build();

                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

                EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetCampaignId(Integer.parseInt(context.getString(R.string.stress_campaign_id)))
                        .setTargetDataSourceId(configPrefs.getInt("STRESS_PREDICTION", -1))
                        .setFromTimestamp(fromTimestamp) //  fromTimestamp
                        .setTillTimestamp(tillTimestamp)
                        .build();

                final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
                if (responseMessage.getDoneSuccessfully()) {
                    List<String> values = responseMessage.getValueList();
                    List<Long> valuesTimestamp = responseMessage.getTimestampList();
                    if (!values.isEmpty()) {
                        for (int i = 0; i < values.size(); i++) {
                            stressReportStr = values.get(i);
                            long timestamp = valuesTimestamp.get(i);
                            try {
                                JSONObject stressReportJSON = new JSONObject(stressReportStr);
                                for (short stressLv = 0; stressLv < 3; stressLv++) {
                                    JSONObject eachLevelJSON = new JSONObject(stressReportJSON.getString(String.valueOf(stressLv)));
                                    String oneReportWithTimestamp = String.format("%d,%d,%d,%d,%.2f,%s,%b\n",
                                            timestamp,
                                            stressLv,
                                            eachLevelJSON.getInt("day_num"),
                                            eachLevelJSON.getInt("ema_order"),
                                            eachLevelJSON.getDouble("accuracy"),
                                            eachLevelJSON.getString("feature_ids"),
                                            eachLevelJSON.getBoolean("model_tag"));
//                                            timestamp + "#" + stressLv + "#" + stressReportJSON.getString(String.valueOf(stressLv));
                                    String[] split = oneReportWithTimestamp.split(",");
                                    if(Integer.parseInt(split[PREDICTION_ORDER_INDEX]) != 0){
                                        FileOutputStream fileOutputStream = context.openFileOutput(STRESS_PREDICTION_RESULT, Context.MODE_APPEND);
                                        fileOutputStream.write(oneReportWithTimestamp.getBytes());
                                        fileOutputStream.close();
                                    }
                                    Log.e(TAG, oneReportWithTimestamp);
                                    if(eachLevelJSON.getBoolean("model_tag")){
                                        stressReportPrefsEditor.putInt("reportAnswer", stressLv);
                                        stressReportPrefsEditor.apply();
                                    }

                                }
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                        stressReportPrefsEditor.putLong("lastDownloadTime", tillTimestamp);
                        stressReportPrefsEditor.apply();
                    } else {
                        Log.e(TAG, "values empty");
                    }
                }
                if(fromTimestamp == 0){
                    EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredEMARecordsRequestMessage2 = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                            .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                            .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                            .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                            .setTargetCampaignId(Integer.parseInt(context.getString(R.string.stress_campaign_id)))
                            .setTargetDataSourceId(configPrefs.getInt("SELF_STRESS_REPORT", -1))
                            .setFromTimestamp(fromTimestamp) //  fromTimestamp
                            .setTillTimestamp(tillTimestamp)
                            .build();

                    final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage2 = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage2);
                    if (responseMessage2.getDoneSuccessfully()) {
                        List<String> values = responseMessage2.getValueList();
                        if(!values.isEmpty()){
                            FileOutputStream fileOutputStream = null;
                            try {
                                fileOutputStream = context.openFileOutput(SELF_STRESS_REPORT_RESULT, Context.MODE_APPEND);
                                for(String value : values){
                                    String selfReport = value.replace(" ", ",");
                                    selfReport += '\n';
                                    fileOutputStream.write(selfReport.getBytes());
                                }
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                channel.shutdown();
            }
        }
        return Result.success();

        /*
         * 성공 시 Result.success() return
         * 실패 시 Result.failure() return
         * 다시 시도시 Result.retry() return
         * */
    }
}
