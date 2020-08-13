package kr.ac.inha.mindscope;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.dialog.PointCustomDialog;
import kr.ac.inha.mindscope.receivers.AppUseNotifierReceiver;
import kr.ac.inha.mindscope.receivers.ConnectionMonitor;
import kr.ac.inha.mindscope.receivers.StressReportReceiver;
import kr.ac.inha.mindscope.services.MainService;
import kr.ac.inha.mindscope.services.StressReportDownloader;

import static kr.ac.inha.mindscope.services.MainService.HEARTBEAT_PERIOD;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static Context mContext;
    ConnectionMonitor connectionMonitor;
    Dialog permissionDialog;

    private static final String LAST_NAV_FRG1 = "me";
    private static final String LAST_NAV_FRG2 = "care";
    private static final String LAST_NAV_FRG3 = "report";

    private SharedPreferences loginPrefs;
    SharedPreferences configPrefs;
    Dialog firstStartStepDialog;
    Button firstStartStepDialogBtn;
    TextView firstStartStepDialogTitle;
    TextView firstStartStepDialogTxt;
    RelativeLayout firstStartStepLayout;
    AppBarConfiguration appBarConfiguration;
    private Intent customSensorsService;

    private AlertDialog dialog;
    private PointCustomDialog pointCustomDialog;

    SharedPreferences lastPagePrefs;
    SharedPreferences stepChangePrefs;
    NavController navController;

    private Handler heartBeatHandler = new Handler();
    private Runnable heartBeatSendRunnable = new Runnable() {
        public void run() {
            if (Tools.heartbeatNotSent(MainActivity.this)) {
                Log.e(TAG, "Heartbeat not sent");
                /*Tools.perform_logout(CustomSensorsService.this);
                stopSelf();*/
            }
            heartBeatHandler.postDelayed(this, HEARTBEAT_PERIOD * 1000);
        }
    };
    private View.OnClickListener pointDialogListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pointCustomDialog.dismiss();
        }
    };

    private View.OnClickListener firstStartStep1DialogListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences stepChange = getSharedPreferences("stepChange", MODE_PRIVATE);
            SharedPreferences.Editor editor = stepChange.edit();
            SharedPreferences prefs = getSharedPreferences("firstStart", MODE_PRIVATE);
            SharedPreferences.Editor firstStartStepEditor = prefs.edit();
            firstStartStepEditor.putBoolean("firstStartStep1", true);
            firstStartStepEditor.apply();
            editor.putBoolean("step1Done", true);
            editor.apply();
            firstStartStepDialog.dismiss();
        }
    };
    private View.OnClickListener firstStartStep2DialogListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences prefs = getSharedPreferences("firstStart", MODE_PRIVATE);
            SharedPreferences.Editor firstStartStepEditor = prefs.edit();
            firstStartStepEditor.putBoolean("firstStartStep2", true);
            firstStartStepEditor.apply();
            SharedPreferences stepChange = getSharedPreferences("stepChange", MODE_PRIVATE);
            SharedPreferences.Editor editor = stepChange.edit();
            editor.putBoolean("step2Done", true);
            editor.apply();
            firstStartStepDialog.dismiss();
        }
    };

    public static synchronized Context getInstance() {
        return mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mContext = this;
        setContentView(R.layout.activity_main);

        getFirebaseToken();

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            if (intent.getBooleanExtra("get_point", false)) {
                updatePointAndShowDialog(intent);
            }
        }

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

        lastPagePrefs = getSharedPreferences("LastPage", MODE_PRIVATE);
        stepChangePrefs = getSharedPreferences("stepChange", MODE_PRIVATE);

        DbMgr.init(getApplicationContext());
        AppUseDb.init(getApplicationContext());

        //region Registering BroadcastReciever for connectivity changed
        // only for LOLLIPOP and newer versions
        connectionMonitor = new ConnectionMonitor(this);
        connectionMonitor.enable();
        //endregion


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

        heartBeatHandler.post(heartBeatSendRunnable);

        cancelPreviousAppUseNotification();
        setUpDownloadStressReport();


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        changeNav();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        String last_frg = lastPagePrefs.getString("last_open_nav_frg", "");
        switch (last_frg) {
            case LAST_NAV_FRG1:
                // nothing
                break;
            case LAST_NAV_FRG2:
                navController.navigate(R.id.action_me_to_care_step2);
                break;
            case LAST_NAV_FRG3:
                navController.navigate(R.id.action_me_to_report_step2);
                break;
            default:
                // nothing
                break;
        }

        if (Tools.heartbeatNotSent(getApplicationContext())) {
            Log.e(TAG, "Heartbeat not sent");
            /*Tools.perform_logout(MainActivity.this);
            stopService(customSensorsService);
            finish();*/
        }

        loginPrefs = getSharedPreferences("UserLogin", MODE_PRIVATE);
        configPrefs = getSharedPreferences("Configurations", Context.MODE_PRIVATE);

        customSensorsService = new Intent(this, MainService.class);

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
//            Toast.makeText(this, "Please connect to the Internet for the first launch!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SharedPreferences stressReportPrefs = getSharedPreferences("stressReport", Context.MODE_PRIVATE);
        SharedPreferences.Editor stressReportPrefsEditor = stressReportPrefs.edit();
        stressReportPrefsEditor.putBoolean("fromMain", true);
        stressReportPrefsEditor.apply();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(StressReportDownloader.class).build();
        WorkManager.getInstance(getApplicationContext()).enqueue(oneTimeWorkRequest);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        SharedPreferences.Editor editor = loginPrefs.edit();
        editor.putLong("lastUsageTimestamp", Calendar.getInstance().getTimeInMillis());
        editor.apply();

        int curFrgId = navController.getCurrentDestination().getId();
        if (curFrgId == R.id.navigation_me_step2)
            Log.d(TAG, "navController test");

        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        if (permissionDialog != null) {
            permissionDialog.dismiss();
            permissionDialog = null;
        }

        heartBeatHandler.removeCallbacks(heartBeatSendRunnable);

        SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
        lastPagePrefsEditor.putString("last_open_nav_frg", "me");
        lastPagePrefsEditor.putInt("last_open_tab_position", 0);
        lastPagePrefsEditor.apply();

        Log.d(TAG, String.format("onDestroy - frg : %s, tab : %d", lastPagePrefs.getString("last_open_nav_frg", ""), lastPagePrefs.getInt("last_open_tab_position", 55)));

        setUpNewAppUseNotification();
    }

    private void cancelPreviousAppUseNotification() {
        Intent intent = new Intent(this, AppUseNotifierReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 109, intent, PendingIntent.FLAG_NO_CREATE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (pendingIntent != null)
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

    private void setUpDownloadStressReport() {
        Intent intent = new Intent(this, StressReportReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 506, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.MINUTE) > 55) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        calendar.set(Calendar.MINUTE, 55);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Log.d(TAG, "알람 등록 함수" + dateFormat.format(calendar.getTimeInMillis()));


        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pendingIntent);

    }

    public void restartServiceClick(MenuItem item) {
        customSensorsService = new Intent(this, MainService.class);
        if (item != null) {
            stopService(customSensorsService);
            if (!Tools.hasPermissions(this, Tools.PERMISSIONS)) {
                runOnUiThread(() -> {
                    permissionDialog = Tools.requestPermissionsWithCustomDialog(MainActivity.this);
                    permissionDialog.show();
                });
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
//                    runOnUiThread(() -> dialog = Tools.requestPermissions(MainActivity.this));
                    runOnUiThread(() -> {
                        permissionDialog = Tools.requestPermissionsWithCustomDialog(MainActivity.this);
                        permissionDialog.show();
                    });
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

    private void loadCampaign() {
        new Thread(() -> {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
            try {
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                EtService.RetrieveCampaign.Request retrieveCampaignRequestMessage = EtService.RetrieveCampaign.Request.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                        .build();

                EtService.RetrieveCampaign.Response retrieveCampaignResponseMessage = stub.retrieveCampaign(retrieveCampaignRequestMessage);
                if (retrieveCampaignResponseMessage.getSuccess()) {
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
        // if (configJson.equals(oldConfigJson))
        //    return;

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

    private void createSharedPrefPoints() {
        SharedPreferences points = getSharedPreferences("points", MODE_PRIVATE);
        SharedPreferences.Editor pointsEditor = points.edit();
        pointsEditor.putInt("todayPoints", 0);
        pointsEditor.putInt("sumPoints", 0);
        pointsEditor.putLong("daynum", 0);
        pointsEditor.apply();
    }

    private void changeNav() {
        stepChangePrefs = getSharedPreferences("stepChange", MODE_PRIVATE);
        int step = stepChangePrefs.getInt("stepCheck", 0);

        if (step == 1 && !stepChangePrefs.getBoolean("step1Done", false)) {
            View view = getLayoutInflater().inflate(R.layout.first_start_step_dialog, null);
            // step1 첫 시작시 dialog
            firstStartStepDialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
            firstStartStepDialog.setCancelable(false);
            firstStartStepDialog.setContentView(view);
            firstStartStepLayout = view.findViewById(R.id.first_start_step_layout);
            firstStartStepDialogBtn = view.findViewById(R.id.first_start_step_btn);
            firstStartStepDialogTitle = view.findViewById(R.id.first_start_step_title);
            firstStartStepDialogTitle.setText(getResources().getString(R.string.string_first_start_step1_title));
            firstStartStepDialogTxt = view.findViewById(R.id.first_start_step_txt);
            firstStartStepDialogTxt.setText(Html.fromHtml(getResources().getString(R.string.string_first_start_step1_txt)));
            firstStartStepDialogBtn.setOnClickListener(firstStartStep1DialogListener);
            firstStartStepDialog.show();
        }

        // step2
        if (step == 2 && !stepChangePrefs.getBoolean("step2Done", false)) {
            // step2 첫 시작시 dialog
            View view = getLayoutInflater().inflate(R.layout.first_start_step_dialog, null);
            firstStartStepDialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
            firstStartStepDialog.setContentView(view);
            firstStartStepDialog.setCancelable(false);
            firstStartStepLayout = view.findViewById(R.id.first_start_step_layout);
            firstStartStepDialogBtn = view.findViewById(R.id.first_start_step_btn);
            firstStartStepDialogTitle = view.findViewById(R.id.first_start_step_title);
            firstStartStepDialogTitle.setText(getResources().getString(R.string.string_first_start_step2_title));
            firstStartStepDialogTxt = view.findViewById(R.id.first_start_step_txt);
            firstStartStepDialogTxt.setText(Html.fromHtml(getResources().getString(R.string.string_first_start_step2_txt)));
            firstStartStepDialogBtn.setOnClickListener(firstStartStep2DialogListener);
            firstStartStepDialog.show();

            SharedPreferences prefs = getSharedPreferences("firstStart", MODE_PRIVATE);
            SharedPreferences.Editor firstStartStepEditor = prefs.edit();
            firstStartStepEditor.putBoolean("firstStartStep2", true);
            firstStartStepEditor.apply();

            SharedPreferences.Editor editor = stepChangePrefs.edit();
            editor.putBoolean("step2Done", true);
            editor.apply();

            Log.d(TAG, "step2 test " + stepChangePrefs.getInt("stepchange", 9));
        }

        // Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);

        getSupportActionBar().hide();
        if (stepChangePrefs.getInt("stepCheck", 0) == 2) {
            // STEP 2
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_navigation_menu_step2);
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_me_step2, R.id.navigation_care, R.id.navigation_report).build();
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.setGraph(R.navigation.mobile_navigation_stpe2);

        } else {
            // STEP 1 -- stepCheck == 0 or 1
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_navigation_menu);
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_me, R.id.navigation_care, R.id.navigation_report).build();
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            Log.d(TAG, "nav1");
        }
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
//        if(getIntent().getBooleanExtra("today_last_report", false)){
//            navController.navigate(R.id.action_me_to_care_step2);
//            getIntent().removeExtra("today_last_report");
//        }
    }

    private void updatePointAndShowDialog(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras().getBoolean("get_point", false)) {
            Log.d(TAG, "point dialog test");
            pointCustomDialog = new PointCustomDialog(this, pointDialogListener);
            pointCustomDialog.setCancelable(false);
            pointCustomDialog.show();
            intent.putExtra("get_point", false);
        }
    }

    private void getFirebaseToken() {

        SharedPreferences stepChangePrefs = getSharedPreferences("stepChange", MODE_PRIVATE);
        int stepCheck = stepChangePrefs.getInt("stepCheck", 0);
        if (stepCheck != 0) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
//                        String ms = getString(R.string.msg_)
                        Log.d("FCM Log", "FCM token: " + token);
                    });
        }
    }


}