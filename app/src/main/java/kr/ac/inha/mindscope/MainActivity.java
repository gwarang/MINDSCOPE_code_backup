package kr.ac.inha.mindscope;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.protobuf.ByteString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
    private View.OnClickListener pointDialogListener2 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences stressReportPrefs = getSharedPreferences("stressReport", MODE_PRIVATE);
            SharedPreferences.Editor stressReportPrefsEdiotr = stressReportPrefs.edit();
            stressReportPrefsEdiotr.putBoolean("click_go_to_care", true);
            stressReportPrefsEdiotr.apply();

//            SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
//            lastPagePrefsEditor.putString("last_open_nav_frg", LAST_NAV_FRG2);
//            lastPagePrefsEditor.apply();
            navController.navigate(R.id.action_me_to_care_step2);


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
        SharedPreferences PointPrefs = getApplicationContext().getSharedPreferences("points", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorPoint = PointPrefs.edit();
        int pointcheck = PointPrefs.getInt("firstCheck",0);
        checkVersionInfo();
        getFirebaseToken();
        if(pointcheck == 0) {
            loadAllPoints();
        }
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            // "get_point" 라는 intent가 있으면 포인트 얻었다는 팝업창 보여주기
            if (intent.getBooleanExtra("get_point", false)) {
                updatePointAndShowDialog(intent);
            } else if (intent.getBooleanExtra("change_intervention", false)) {
                // TODO: Navigate to 마음 케어 -> 스트레스 해소 탭
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
        SharedPreferences stepChangePrefs = getSharedPreferences("stepChange", MODE_PRIVATE);
        String last_frg = lastPagePrefs.getString("last_open_nav_frg", "");
        try{
            switch (last_frg) {
                case LAST_NAV_FRG1:
                    // nothing
                    break;
                case LAST_NAV_FRG2:
                    if(stepChangePrefs.getInt("stepCheck", 1) == 2)
                        navController.navigate(R.id.action_me_to_care_step2);
                    else
                        navController.navigate(R.id.action_navigation_me_to_navigation_care);
                    break;
                case LAST_NAV_FRG3:
                    if(stepChangePrefs.getInt("stepCheck", 1) == 2)
                        navController.navigate(R.id.action_me_to_report_step2);
                    else
                        navController.navigate(R.id.action_navigation_me_to_navigation_report);
                    break;
                default:
                    // nothing
                    break;
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
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

        // 신경안써도됨
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
        if (calendar.get(Calendar.MINUTE) > 50) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }
        calendar.set(Calendar.MINUTE, 50);
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
        pointsEditor.putInt("firstCheck",0);
        pointsEditor.apply();
    }

    private void changeNav() {
        stepChangePrefs = getSharedPreferences("stepChange", MODE_PRIVATE);
        int step = stepChangePrefs.getInt("stepCheck", 0);

        // For step 2 testing
//        step = 2;
//        SharedPreferences.Editor editor1 = stepChangePrefs.edit();
//        editor1.putBoolean("step1Done", true);
//        editor1.putInt("stepCheck", 2);
//        editor1.apply();

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

//            Log.d(TAG, "step2 test " + stepChangePrefs.getInt("stepchange", 9));
        }

        // Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);

        getSupportActionBar().hide();
        if (stepChangePrefs.getInt("stepCheck", 0) == 2) {
            // STEP 2
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_navigation_menu_step2);
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_me_step2, R.id.navigation_care_step2, R.id.navigation_report_step2).build();
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.setGraph(R.navigation.mobile_navigation_step2);

        } else {
            // STEP 1 -- stepCheck == 0 or 1
            navView.getMenu().clear();
            navView.inflateMenu(R.menu.bottom_navigation_menu);
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_me, R.id.navigation_care, R.id.navigation_report).build();
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.setGraph(R.navigation.mobile_navigation_step1);
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
            pointCustomDialog = new PointCustomDialog(this, pointDialogListener, pointDialogListener2);
            pointCustomDialog.setCancelable(false);
            pointCustomDialog.show();
            intent.putExtra("get_point", false);
        }
    }

    private void getFirebaseToken() {
        // Firebase Cloud Msg.
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
                        Log.d("FCM Log mainactivity", "FCM token: " + token);
                        SharedPreferences.Editor stepChangePrefsEditor = stepChangePrefs.edit();
                        stepChangePrefsEditor.putBoolean("haveToken", true);
                        stepChangePrefsEditor.putString("FCM_token", token);
                        stepChangePrefsEditor.apply();
                    });
        }
    }

    public void checkVersionInfo() {
        if (DbMgr.getDB() == null)
            DbMgr.init(getApplicationContext());

        SharedPreferences configPrefs = getSharedPreferences("Configurations", MODE_PRIVATE);
        SharedPreferences.Editor configPrefsEditor = configPrefs.edit();
        String oldVersion = configPrefs.getString("versionName", "Unknown");
        Log.d(TAG, "oldVersion: " + oldVersion);
//        String oldVersion = "test";
        String version = "Unknown";
        PackageInfo packageInfo;

        try {
            packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(!version.equals("Unknown") && !version.equals(oldVersion)){
            int dataSourceId = configPrefs.getInt("APP_VERSION", -1);
            if(dataSourceId != -1){
                long timestamp = System.currentTimeMillis();
                Log.d(TAG, "APP_VERSION dataSourceId, version: " + dataSourceId + ", " + version);
                DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f, timestamp, version);
                configPrefsEditor.putString("versionName", version);
                configPrefsEditor.apply();
            }

        }


    }
    public void loadAllPoints() {
        //서버와 포인트 동기화 과정
        SharedPreferences PointPrefs = getApplicationContext().getSharedPreferences("points", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorPoint = PointPrefs.edit();
        if (Tools.isNetworkAvailable()) {
            new Thread(() -> {
                SharedPreferences loginPrefs = getApplicationContext().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(getApplicationContext().getString(R.string.stress_campaign_id));
                final int REWARD_POINTS = 26;

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                Calendar c = Calendar.getInstance();
                EtService.RetrieveFilteredDataRecords.Request requestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(REWARD_POINTS)
                        .setFromTimestamp(0)
                        .setTillTimestamp(c.getTimeInMillis())
                        .build();
                int points = 0;
                HashMap<Long, Integer> allPointsMaps = new HashMap<>();
                try {
                    EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                    if (responseMessage.getSuccess()) {
                        // checkByteString
                        for (ByteString value : responseMessage.getValueList()) {
                            String valueStr = value.toString("UTF-8");
                            String[] cells = valueStr.split(" ");
                            if (cells.length != 3)
                                continue;
                            allPointsMaps.put(Long.parseLong(cells[0]), Integer.parseInt(cells[2]));
                            Log.d("test", allPointsMaps.toString());
//                            points += Integer.parseInt(cells[2]);
                        }
                    }
                } catch (StatusRuntimeException | IOException e) {
                    e.printStackTrace();
                }
                channel.shutdown();


                for (Map.Entry<Long, Integer> elem : allPointsMaps.entrySet()) {
                    points += elem.getValue();
                }
                final int finalPoints = points;
                Log.d("포인트",String.valueOf(finalPoints));
                editorPoint.putInt("sumPoints",finalPoints);
                editorPoint.putInt("firstCheck",1);
                editorPoint.apply();
            }).start();
        }
    }
//    public void loadAllPoints() {
//
//        SharedPreferences pointsPrefs = getSharedPreferences("points", Context.MODE_PRIVATE);
//        int localSumPoints = pointsPrefs.getInt("sumPoints", 0);
//        long daynum = pointsPrefs.getLong("daynum", 0);
//        int localTodayPoints = 0;
//        for (int i = 1; i<=4; i++){
//            localTodayPoints += pointsPrefs.getInt("day_" + daynum + "_order_" + i, 0);
//        }

//        sumPointsView.setText(String.format(Locale.getDefault(), "%d", localSumPoints));
//        todayPointsView.setText(String.format(Locale.getDefault(), "%d", localTodayPoints));



//        Log.d(TAG, "start loadAllPoints");
//        Context context = requireContext();
//        allPointsMaps.clear();
//        if (Tools.isNetworkAvailable()) {
//
//            if (loadPointsThread != null && loadPointsThread.isAlive())
//                loadPointsThread.interrupt();
//
//            loadPointsThread = new Thread() {
//                @Override
//                public void run() {
//                    while (!loadPointsThread.isInterrupted()) {
//                        int allPoints = 0;
//                        int dailyPoints = 0;
//                        SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
//                        int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
//                        String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
//                        int campaignId = Integer.parseInt(context.getString(R.string.stress_campaign_id));
//                        final int REWARD_POINTS = 58;
//
//                        ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
//                        ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
//                        Calendar c = Calendar.getInstance();
//                        EtService.RetrieveFilteredDataRecords.Request requestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
//                                .setUserId(userId)
//                                .setEmail(email)
//                                .setTargetEmail(email)
//                                .setTargetCampaignId(campaignId)
//                                .setTargetDataSourceId(REWARD_POINTS)
//                                .setFromTimestamp(0)
//                                .setTillTimestamp(c.getTimeInMillis())
//                                .build();
//                        try {
//                            EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
//                            if (responseMessage.getSuccess()) {
//                                // checkByteString
//                                for (ByteString value : responseMessage.getValueList()) {
//                                    String[] cells = value.toString().split(" ");
//                                    if (cells.length != 3)
//                                        continue;
//                                    allPointsMaps.put(Long.parseLong(cells[0]), Integer.parseInt(cells[2]));
//                                }
//                            }
//                        } catch (IllegalStateException | StatusRuntimeException e) {
//                            e.printStackTrace();
//                        }
//                        channel.shutdown();
//
//                        Calendar todayStartCal = Calendar.getInstance();
//                        Calendar todayEndCal = Calendar.getInstance();
//                        if(todayStartCal.get(Calendar.HOUR_OF_DAY) < timeTheDayNumIsChanged){
//                            todayStartCal.add(Calendar.DATE, -1);
//                        }else{
//                            todayEndCal.add(Calendar.DATE, 1);
//                        }
//                        todayStartCal.set(Calendar.HOUR_OF_DAY, timeTheDayNumIsChanged);
//                        todayStartCal.set(Calendar.MINUTE, 0);
//                        todayStartCal.set(Calendar.SECOND, 0);
//                        todayStartCal.set(Calendar.MILLISECOND, 0);
//                        todayEndCal.set(Calendar.HOUR_OF_DAY, timeTheDayNumIsChanged - 1);
//                        todayEndCal.set(Calendar.MINUTE, 59);
//                        todayEndCal.set(Calendar.SECOND, 59);
//                        todayEndCal.set(Calendar.MILLISECOND, 0);
//                        Calendar pointCal = Calendar.getInstance();
//
//                        long startTimestamp = todayStartCal.getTimeInMillis();
//                        long endTimestamp = todayEndCal.getTimeInMillis();
//                        long pointTimestamp = 0;
//
//                        for (Map.Entry<Long, Integer> elem : allPointsMaps.entrySet()) {
//                            allPoints += elem.getValue();
//                            pointCal.setTimeInMillis(elem.getKey());
//
//                            pointTimestamp = pointCal.getTimeInMillis();
//                            if(startTimestamp <= pointTimestamp && pointTimestamp <= endTimestamp){
//                                dailyPoints += elem.getValue();
//                            }
//
//                        }
//
//                        if (isAdded()) {
//                            int finalAllPoints = allPoints;
//                            int finalDailyPoints = dailyPoints;
//                            requireActivity().runOnUiThread(() -> {
//                                sumPointsView.setText(String.format(Locale.getDefault(), "%d", finalAllPoints));
//                                todayPointsView.setText(String.format(Locale.getDefault(), "%d", finalDailyPoints));
//                            });
//                        }
//                    }
//                }
//            };
//            loadPointsThread.setDaemon(true);
//            loadPointsThread.start();
//        }
//    }
}