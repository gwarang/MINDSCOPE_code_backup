package kr.ac.inha.mindscope;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.receivers.AppUseNotifierReceiver;
import kr.ac.inha.mindscope.receivers.ConnectionMonitor;
import kr.ac.inha.mindscope.services.MainService;

import static kr.ac.inha.mindscope.services.MainService.HEARTBEAT_PERIOD;
import static kr.ac.inha.mindscope.services.MainService.PERMISSION_REQUEST_NOTIFICATION_ID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    //region UI variables
//    private Button btnEMA;
//    private TextView tvServiceStatus;
//    private TextView tvInternetStatus;
//    public TextView tvFileCount;
//    public TextView tvDayNum;
//    public TextView tvEmaNum;
//    public TextView tvHBPhone;
//    public TextView tvDataLoadedPhone;
//    private RelativeLayout loadingPanel;
//    private TextView ema_tv_1;
//    private TextView ema_tv_2;
//    private TextView ema_tv_3;
//    private TextView ema_tv_4;
    //endregion

    private Intent customSensorsService;
    ConnectionMonitor connectionMonitor;


    private SharedPreferences loginPrefs;
    SharedPreferences configPrefs;
    SharedPreferences firstPref;
    SharedPreferences prefPermission;

    AppBarConfiguration appBarConfiguration;


    private Intent checkIntent;

    private AlertDialog dialog;
    private PointCustomDialog pointCustomDialog;

    private Handler heartBeatHandler = new Handler();
    private Runnable heartBeatSendRunnable = new Runnable() {
        public void run() {
            try {
                if (Tools.heartbeatNotSent(MainActivity.this)) {
                    Log.e(TAG, "Heartbeat not sent");
                    /*Tools.perform_logout(CustomSensorsService.this);
                    stopSelf();*/
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            heartBeatHandler.postDelayed(this, HEARTBEAT_PERIOD * 1000);
        }
    };

    private void sendNotificationForPermissionSetting() {
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "StressSensor_permission_notif";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getApplicationContext(), channelId);
        builder.setContentTitle(this.getString(R.string.app_name))
                .setContentText(this.getString(R.string.grant_permissions))
                .setTicker("New Message Alert!")
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_no_bg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, this.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        final Notification notification = builder.build();
        if (notificationManager != null) {
            notificationManager.notify(PERMISSION_REQUEST_NOTIFICATION_ID, notification);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent.getExtras() != null)
            updatePointAndShowDialog(intent);


        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            // only for gingerbread and newer versions
            Tools.PERMISSIONS = new String[]{
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }


        DbMgr.init(getApplicationContext());
        AppUseDb.init(getApplicationContext());
//        setActionBar((Toolbar) findViewById(R.id.my_toolbar));

        //region Init UI variables
//        btnEMA = findViewById(R.id.btn_late_ema);
//        tvServiceStatus = findViewById(R.id.tvStatus);
//        tvInternetStatus = findViewById(R.id.connectivityStatus);
//        tvFileCount = findViewById(R.id.filesCountTextView);
//        loadingPanel = findViewById(R.id.loadingPanel);
//        tvDayNum = findViewById(R.id.txt_day_num);
//        tvEmaNum = findViewById(R.id.ema_responses_phone);
//        tvHBPhone = findViewById(R.id.heartbeat_phone);
//        tvDataLoadedPhone = findViewById(R.id.data_loaded_phone);
//        ema_tv_1 = findViewById(R.id.ema_tv_1);
//        ema_tv_2 = findViewById(R.id.ema_tv_2);
//        ema_tv_3 = findViewById(R.id.ema_tv_3);
//        ema_tv_4 = findViewById(R.id.ema_tv_4);
        //endregion

//        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
//        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                updateStats();
//                try {
//                    if (Tools.heartbeatNotSent(getApplicationContext())) {
//                        Log.e(TAG, "Heartbeat not sent");
//                        /*Tools.perform_logout(MainActivity.this);
//                        stopService(customSensorsService);
//                        finish();*/
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                pullToRefresh.setRefreshing(false);
//            }
//        });

        //region Registering BroadcastReciever for connectivity changed
        // only for LOLLIPOP and newer versions
        connectionMonitor = new ConnectionMonitor(this);
        connectionMonitor.enable();
        //endregion


        //test
        //첫번째 실행의 경우, INTRO 및 장소설정
        SharedPreferences firstPref = getSharedPreferences("firstStart", MODE_PRIVATE);
        int firstviewshow = firstPref.getInt("First", 0);

        if (firstviewshow != 1) {
            SharedPreferences firstDate = getSharedPreferences("firstDate", MODE_PRIVATE);
            Date firstTime = Calendar.getInstance().getTime();
            String firstDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(firstTime);

            SharedPreferences.Editor fDateEdit = firstDate.edit();
            fDateEdit.putString("firstDateMillis", firstDateStr);
            fDateEdit.apply();

            createSharedPrefPoints();

            Intent intentFirst = new Intent(MainActivity.this, FirstStartActivity.class);
            startActivity(intentFirst);
        }

        changeNav();

        heartBeatHandler.post(heartBeatSendRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();

        changeNav();

        if (!Tools.hasPermissions(this, Tools.PERMISSIONS)) {
            dialog = Tools.requestPermissions(MainActivity.this);
        }

        try {
            if (Tools.heartbeatNotSent(getApplicationContext())) {
                Log.e(TAG, "Heartbeat not sent");
                /*Tools.perform_logout(MainActivity.this);
                stopService(customSensorsService);
                finish();*/
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        loginPrefs = getSharedPreferences("UserLogin", MODE_PRIVATE);
        configPrefs = getSharedPreferences("Configurations", Context.MODE_PRIVATE);

//        int ema_order = Tools.getEMAOrderFromRangeAfterEMA(Calendar.getInstance());
//        if (ema_order == 0) {
//            btnEMA.setVisibility(View.GONE);
//        } else {
//            boolean ema_btn_visible = loginPrefs.getBoolean("ema_btn_make_visible", true);
//            if (!ema_btn_visible) {
//                btnEMA.setVisibility(View.GONE);
//            } else {
//                btnEMA.setVisibility(View.VISIBLE);
//            }
//        }


        customSensorsService = new Intent(this, MainService.class);
        initUserStats(true, 0, 0, null);

        if (Tools.isNetworkAvailable()) {
            loadCampaign();
        } else if (configPrefs.getBoolean("campaignLoaded", false)) {
            try {
                setUpCampaignConfigurations(
                        configPrefs.getString("name", null),
                        configPrefs.getString("notes", null),
                        configPrefs.getString("creatorEmail", null),
                        Objects.requireNonNull(configPrefs.getString("configJson", null)),
                        configPrefs.getLong("startTimestamp", -1),
                        configPrefs.getLong("endTimestamp", -1),
                        configPrefs.getInt("participantCount", -1)
                );
                restartServiceClick(null);
            } catch (JSONException e) {
                e.printStackTrace();
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "Please connect to the Internet for the first launch!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        updateStats();
    }

    private String formatMinutes(int minutes) {
        if (minutes > 60) {
            if (minutes > 1440) {
                return minutes / 60 / 24 + "days";
            } else {
                int h = minutes / 60;
                float dif = (float) minutes / 60 - h;
                //Toast.makeText(MainActivity.this, dif + "", Toast.LENGTH_SHORT).show();
                int m = (int) (dif * 60);
                return h + "h " + m + "m";
            }
        } else
            return minutes + "m";
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = loginPrefs.edit();
        editor.putLong("lastUsageTimestamp", Calendar.getInstance().getTimeInMillis());
        editor.apply();

        super.onStop();
//        loadingPanel.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        // loadingPanel.setVisibility(View.GONE);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

        heartBeatHandler.removeCallbacks(heartBeatSendRunnable);

        // cancelPreviousAppUseNotification();
        setUpNewAppUseNotification();

        super.onDestroy();
    }

    public void initUserStats(boolean error, long joinedTimesamp, long hbPhone, String dataLoadedPhone) {
//        if (Tools.isMainServiceRunning(MainActivity.this)) {
//            tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
//            tvServiceStatus.setText(getString(R.string.service_runnig));
//        } else {
//            tvServiceStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
//            tvServiceStatus.setText(getString(R.string.service_stopped));
//        }
        if (!error) {
//            tvDayNum.setVisibility(View.VISIBLE);
//            tvEmaNum.setVisibility(View.VISIBLE);
//            tvDataLoadedPhone.setVisibility(View.VISIBLE);
//            tvHBPhone.setVisibility(View.VISIBLE);
//
//            tvInternetStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
//
//            tvInternetStatus.setText(getString(R.string.internet_on));

            Calendar now = Calendar.getInstance();

            float joinTimeDif = now.getTimeInMillis() - joinedTimesamp;
            int dayNum = (int) Math.ceil(joinTimeDif / 1000 / 3600 / 24); // in days

            float hbTimeDif = now.getTimeInMillis() - hbPhone;
            int heart_beat = (int) Math.ceil(hbTimeDif / 1000 / 60); // in minutes

//            if (heart_beat > 30)
//                tvHBPhone.setTextColor(ContextCompat.getColor(this, R.color.red));
//            else
//                tvHBPhone.setTextColor(ContextCompat.getColor(this, R.color.green));
//
//
//            if (dayNum > 1) {
//                tvDayNum.setText(getString(R.string.day_num, dayNum) + "s");
//            } else {
//                tvDayNum.setText(getString(R.string.day_num, dayNum));
//            }
//
//
//            tvDataLoadedPhone.setText(getString(R.string.data_loaded, dataLoadedPhone));
//            String last_active_text = hbPhone == 0 ? "just now" : formatMinutes(heart_beat) + " ago";
//            tvHBPhone.setText(getString(R.string.last_active, last_active_text));
        } else {
//            tvInternetStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
//            tvInternetStatus.setText(getString(R.string.internet_off));
//            tvDayNum.setVisibility(View.GONE);
//            tvEmaNum.setVisibility(View.GONE);
//            tvDataLoadedPhone.setVisibility(View.GONE);
//            tvHBPhone.setVisibility(View.GONE);
//            ema_tv_1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unchecked_box, 0, 0);
//            ema_tv_2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unchecked_box, 0, 0);
//            ema_tv_3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unchecked_box, 0, 0);
//            ema_tv_4.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unchecked_box, 0, 0);
        }
    }

    private void cancelPreviousAppUseNotification() {
        Intent intent = new Intent(this, AppUseNotifierReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 109, intent, PendingIntent.FLAG_NO_CREATE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void setUpNewAppUseNotification() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 3);

        Intent intent = new Intent(this, AppUseNotifierReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 109, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void updateEmaResponseView(List<String> values) {
//        ema_tv_1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unchecked_box, 0, 0);
//        ema_tv_2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unchecked_box, 0, 0);
//        ema_tv_3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unchecked_box, 0, 0);
//        ema_tv_4.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.unchecked_box, 0, 0);
        if (values != null) {
//            tvEmaNum.setText(getString(R.string.ema_responses_rate, values.size()));
            for (String val : values) {
                switch (Integer.parseInt(val.split(Tools.DATA_SOURCE_SEPARATOR)[1])) {
//                    case 1:
//                        ema_tv_1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_box, 0, 0);
//                        break;
//                    case 2:
//                        ema_tv_2.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_box, 0, 0);
//                        break;
//                    case 3:
//                        ema_tv_3.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_box, 0, 0);
//                        break;
//                    case 4:
//                        ema_tv_4.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.checked_box, 0, 0);
//                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void updateStats() {
        if (Tools.isNetworkAvailable())
            new Thread(() -> {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                EtService.RetrieveParticipantStatisticsRequestMessage retrieveParticipantStatisticsRequestMessage = EtService.RetrieveParticipantStatisticsRequestMessage.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                        .build();
                try {
                    EtService.RetrieveParticipantStatisticsResponseMessage responseMessage = stub.retrieveParticipantStatistics(retrieveParticipantStatisticsRequestMessage);
                    if (responseMessage.getDoneSuccessfully()) {
                        final long join_timestamp = responseMessage.getCampaignJoinTimestamp();
                        final long hb_phone = responseMessage.getLastHeartbeatTimestamp();
                        final int samples_amount = responseMessage.getAmountOfSubmittedDataSamples();
                        runOnUiThread(() -> initUserStats(false, join_timestamp, hb_phone, String.valueOf(samples_amount)));
                    } else {
                        runOnUiThread(() -> initUserStats(true, 0, 0, null));
                    }
                } catch (StatusRuntimeException e) {
                    Log.e("Tools", "DataCollectorService.setUpHeartbeatSubmissionThread() exception: " + e.getMessage());
                    e.printStackTrace();
                    runOnUiThread(() -> initUserStats(true, 0, 0, null));

                }

                Calendar fromCal = Calendar.getInstance();
                fromCal.set(Calendar.HOUR_OF_DAY, 0);
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                Calendar tillCal = (Calendar) fromCal.clone();
                tillCal.set(Calendar.HOUR_OF_DAY, 23);
                tillCal.set(Calendar.MINUTE, 59);
                tillCal.set(Calendar.SECOND, 59);
                EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredDataRecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                        .setTargetDataSourceId(configPrefs.getInt("SURVEY_EMA", -1))
                        .setFromTimestamp(fromCal.getTimeInMillis())
                        .setTillTimestamp(tillCal.getTimeInMillis())
                        .build();
                try {
                    final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredDataRecordsRequestMessage);
                    if (responseMessage.getDoneSuccessfully()) {
                        runOnUiThread(() -> updateEmaResponseView(responseMessage.getValueList()));
                    } else {
                        runOnUiThread(() -> initUserStats(true, 0, 0, null));
                    }
                } catch (StatusRuntimeException e) {
                    Log.e("Tools", "DataCollectorService.setUpHeartbeatSubmissionThread() exception: " + e.getMessage());
                    e.printStackTrace();
                    runOnUiThread(() -> updateEmaResponseView(null));

                } finally {
                    channel.shutdown();
                }
            }).start();
        else {
            Toast.makeText(MainActivity.this, "Please connect to Internet!", Toast.LENGTH_SHORT).show();
            runOnUiThread(() -> initUserStats(true, 0, 0, null));
        }
    }

    // TODO 추후 사용 가능성 있음
    public void lateEMAClick(View view) {
        int ema_order = Tools.getEMAOrderFromRangeAfterEMA(Calendar.getInstance());
        if (ema_order != 0) {
            Intent intent = new Intent(this, EMAActivity.class);
            intent.putExtra("ema_order", ema_order);
            startActivity(intent);
        }
    }

    public void restartServiceClick(MenuItem item) {
        customSensorsService = new Intent(this, MainService.class);
        if (item != null) {
            stopService(customSensorsService);
            if (!Tools.hasPermissions(this, Tools.PERMISSIONS)) {
                runOnUiThread(() -> dialog = Tools.requestPermissions(MainActivity.this));
            } else {
                Log.e(TAG, "restartServiceClick: 3");
                if (configPrefs.getLong("startTimestamp", 0) <= System.currentTimeMillis()) {
                    Log.e(TAG, "RESTART SERVICE");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(customSensorsService);
                    } else {
                        startService(customSensorsService);
                    }
                }
            }
        } else {
            if (!Tools.isMainServiceRunning(getApplicationContext())) {
                customSensorsService = new Intent(this, MainService.class);
                stopService(customSensorsService);
                if (!Tools.hasPermissions(this, Tools.PERMISSIONS)) {
                    runOnUiThread(() -> dialog = Tools.requestPermissions(MainActivity.this));
                } else {
                    if (configPrefs.getLong("startTimestamp", 0) <= System.currentTimeMillis()) {
                        Log.e(TAG, "RESTART SERVICE");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(customSensorsService);
                        } else {
                            startService(customSensorsService);
                        }
                    }
                }
            }
        }

    }

    // TODO 추후 삭제 예정
    public void setLocationsClick(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, LocationsSettingActivity.class);
        startActivity(intent);
    }

    public void logoutClick(MenuItem item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(getString(R.string.log_out_confirmation));
        alertDialog.setPositiveButton(
                getString(R.string.yes), (dialog, which) -> {
                    Tools.perform_logout(getApplicationContext());
                    stopService(customSensorsService);
                    finish();
                });

        alertDialog.setNegativeButton(
                getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    private void loadCampaign() {
        new Thread(() -> {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

            try {
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                EtService.RetrieveCampaignRequestMessage retrieveCampaignRequestMessage = EtService.RetrieveCampaignRequestMessage.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                        .build();

                EtService.RetrieveCampaignResponseMessage retrieveCampaignResponseMessage = stub.retrieveCampaign(retrieveCampaignRequestMessage);
                if (retrieveCampaignResponseMessage.getDoneSuccessfully()) {
                    setUpCampaignConfigurations(
                            retrieveCampaignResponseMessage.getName(),
                            retrieveCampaignResponseMessage.getNotes(),
                            retrieveCampaignResponseMessage.getCreatorEmail(),
                            retrieveCampaignResponseMessage.getConfigJson(),
                            retrieveCampaignResponseMessage.getStartTimestamp(),
                            retrieveCampaignResponseMessage.getEndTimestamp(),
                            retrieveCampaignResponseMessage.getParticipantCount()
                    );
                    SharedPreferences.Editor editor = configPrefs.edit();
                    editor.putString("name", retrieveCampaignResponseMessage.getName());
                    editor.putString("notes", retrieveCampaignResponseMessage.getNotes());
                    editor.putString("creatorEmail", retrieveCampaignResponseMessage.getCreatorEmail());
                    editor.putString("configJson", retrieveCampaignResponseMessage.getConfigJson());
                    editor.putLong("startTimestamp", retrieveCampaignResponseMessage.getStartTimestamp());
                    editor.putLong("endTimestamp", retrieveCampaignResponseMessage.getEndTimestamp());
                    editor.putInt("participantCount", retrieveCampaignResponseMessage.getParticipantCount());
                    editor.putBoolean("campaignLoaded", true);
                    editor.apply();
                    restartServiceClick(null);
                }
            } catch (StatusRuntimeException | JSONException e) {
                e.printStackTrace();
            } finally {
                channel.shutdown();
            }
        }).start();
    }

    private void setUpCampaignConfigurations(String name, String notes, String creatorEmail, String configJson, long startTimestamp, long endTimestamp, int participantCount) throws JSONException {
        String oldConfigJson = configPrefs.getString(String.format(Locale.getDefault(), "%s_configJson", name), null);
        if (configJson.equals(oldConfigJson))
            return;

        SharedPreferences.Editor editor = configPrefs.edit();
        editor.putString(String.format(Locale.getDefault(), "%s_configJson", name), configJson);

        JSONArray dataSourceConfigurations = new JSONArray(configJson);
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < dataSourceConfigurations.length(); n++) {
            JSONObject dataSourceConfig = dataSourceConfigurations.getJSONObject(n);
            String _name = dataSourceConfig.getString("name");
            int _dataSourceId = dataSourceConfig.getInt("data_source_id");
            editor.putInt(_name, _dataSourceId);
            String _json = dataSourceConfig.getString("config_json");
            editor.putString(String.format(Locale.getDefault(), "config_json_%s", _name), _json);
            sb.append(_name).append(',');
        }
        if (sb.length() > 0)
            sb.replace(sb.length() - 1, sb.length(), "");
        editor.putString("dataSourceNames", sb.toString());
        editor.apply();
    }

    private View.OnClickListener pointDialogListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pointCustomDialog.dismiss();
        }
    };

    private void createSharedPrefPoints() {
        SharedPreferences points = getSharedPreferences("points", MODE_PRIVATE);
        SharedPreferences.Editor pointsEditor = points.edit();
        pointsEditor.putInt("todayPoints", 0);
        pointsEditor.putInt("sumPoints", 0);
        pointsEditor.putLong("daynum", 0);
        pointsEditor.apply();
    }

    private void changeNav() {

        // TODO 추후에는 시작날로부터 2주후부터 stepChange 하도록 구현할것
        SharedPreferences stepChange = getSharedPreferences("stepChange", MODE_PRIVATE);
        int step = stepChange.getInt("stepCheck", 0);
        if (step == 0) {
            SharedPreferences.Editor stepEditor = stepChange.edit();
            stepEditor.putInt("stepCheck", 1);
            stepEditor.apply();
        }

        NavController navController;
        // Bottom Navigation Bar
        BottomNavigationView navView = (BottomNavigationView) findViewById(R.id.nav_view);

        getSupportActionBar().hide();
        if (stepChange.getInt("stepCheck", 0) == 2) {
            // STEP 2
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_navigation_menu_step2);
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_me_step2, R.id.navigation_care, R.id.navigation_report).build();
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.setGraph(R.navigation.mobile_navigation_stpe2);
            Log.i(TAG, "nav2");
        } else {
            // STEP 1
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_navigation_menu);
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_me, R.id.navigation_care, R.id.navigation_report).build();
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            Log.i(TAG, "nav1");
        }
//        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void updatePointAndShowDialog(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras().getInt("tagcode") == TagActivity.DIALOG_ENALBE) {
            // TODO point 얻는거 / alert dialog
            Log.i(TAG, "point dialog test");
            Tools.updatePoint(getApplicationContext()); // poitn update
            pointCustomDialog = new PointCustomDialog(this, pointDialogListener);
            pointCustomDialog.show();
            intent.removeExtra("tagcode");
        } else if (intent.getExtras() != null) {
            Tools.updatePoint(getApplicationContext());
            pointCustomDialog = new PointCustomDialog(this, pointDialogListener);
            pointCustomDialog.show();
            int reportAnswer = intent.getExtras().getInt("reportAnswer");
            Log.i(TAG, "reportAnswer in main: " + reportAnswer);
            SharedPreferences prefs = getSharedPreferences("stressReport", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("result", reportAnswer);
            editor.apply();
        }
    }

}