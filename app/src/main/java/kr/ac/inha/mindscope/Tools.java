package kr.ac.inha.mindscope;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RemoteViews;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.receivers.GeofenceReceiver;
import kr.ac.inha.mindscope.services.InterventionService;
import kr.ac.inha.mindscope.services.LocationService;
import kr.ac.inha.mindscope.services.MainService;

import static android.app.Notification.CATEGORY_ALARM;
import static android.content.Context.MODE_PRIVATE;
import static kr.ac.inha.mindscope.EMAActivity.EMA_NOTIF_HOURS;
import static kr.ac.inha.mindscope.StressReportActivity.REPORT_NOTIF_HOURS;
import static kr.ac.inha.mindscope.fragment.StressReportFragment1.REPORT_DURATION;
import static kr.ac.inha.mindscope.services.MainService.EMA_RESPONSE_EXPIRE_TIME;
import static kr.ac.inha.mindscope.services.MainService.REPORT_RESPONSE_EXPIRE_TIME;

public class Tools {

    private static final String TAG = "Tools";
    public static final String ACTION_OPEN_PAGE = "OPEN_PAGE";
    public static final String ACTION_CLICK_SAVE_BUTTON = "CLICK_SAVE_BUTTON";
    public static final String ACTION_CLICK_COMPLETE_BUTTON = "CLICK_COMPLETE_BUTTON";
    public static final String DATA_SOURCE_SEPARATOR = " ";
    public static final int CATEGORY_ACTIVITY_END_INDEX = 5;
    public static final int CATEGORY_SOCIAL_END_INDEX_EXCEPT_SNS_USAGE = 9;
    public static final int CATEGORY_LOCATION_END_INDEX = 15;
    public static final int CATEGORY_SNS_APP_USAGE = 10;
    public static final int CATEGORY_ENTERTAIN_APP_USAGE = 18;
    public static final int CATEGORY_FOOD_APP_USAGE = 27;
    public static final long STEP0_EXPIRE_TIMESTAMP_VALUE = 60 * 60 * 24 * 0 * 1000;  // TODO change 60 * 60 * 24 * 1 * 1000  for real test
    public static final long STEP1_EXPIRE_TIMESTAMP_VALUE = 60 * 60 * 24 * 4 * 1000;  // TODO change 60 * 60 * 24 * 14 * 1000  for real test

    static int PERMISSION_ALL = 1;
    public static final int POINT_INCREASE_VALUE = 250;
    public static String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    };

    public static String[] FEATURE_IDS_WITH_NO_APPS = {
            "음악_및_영상",
            "클라우드_및_문서도구",
            "결제_및_쇼핑",
            "게임_및_웹툰",
            "비즈니스_도구(취업_및_화상미팅)",
            "건강_관리_도구",
            "SNS_및_메일",
            "교육_관련_앱",
            "교통_도구(지도)",
            "사진 ",
            "뉴스",
            "배달_및_음식_관련_앱"
    };

    /* Zaturi start */
    static String[] COMMUNICATION_APPS = {
            "com.kakao.talk",                       // KakaoTalk
            "com.facebook.orca",                    // Messenger
            "com.facebook.mlite",                   // Messenger Lite
            "jp.naver.line.android",                // Line
            "com.linecorp.linelite",                // Line Lite
            "com.Slack",                            // Slack
            "com.google.android.apps.messaging",    // Text message
            "kr.co.vcnc.android.couple",            // Between
            "org.telegram.messenger",               // Telegram
            "com.discord",                          // Discord
            "com.tencent.mm",                       // WeChat
            "com.samsung.android.messaging",        // Samsumg Messaging
            "kr.ac.inha.mindscope"                  // MindScope
    };
    public static final int ZATURI_NOTIFICATION_ID = 2222;
    public static final int STRESS_CONFIG = 0;
    public static final int STRESS_NEXT_TIME = 1;
    public static final int STRESS_MUTE_TODAY = 2;
    public static final int STRESS_UNMUTE_TODAY = 3;
    public static final int STRESS_DO_INTERVENTION = 4;
    public static final int STRESS_OTHER_RECOMMENDATION = 5;
    public static final int STRESS_PUSH_NOTI_SENT = 6;
    public static final int PATH_APP = 0;
    public static final int PATH_NOTIFICATION = 1;
    /* Zaturi end */

    //region static variable for Stress report
    public static final int PREDICTION_TIMESTAMP_INDEX = 0;
    public static final int PREDICTION_STRESSLV_INDEX = 1;
    public static final int PREDICTION_DAYNUM_INDEX = 2;
    public static final int PREDICTION_ORDER_INDEX = 3;
    public static final int PREDICTION_ACCURACY_INDEX = 4;
    public static final int PREDICTION_FEATUREIDS_INDEX = 5;
    public static final int PREDICTION_MODELTAG_INDEX = 6;
    public static final int SELF_REPORT_TIMESTAMP_INDEX = 0;
    public static final int SELF_REPORT_DAYNUM_INDEX = 1;
    public static final int SELF_REPORT_ORDER_INDEX = 2;
    public static final int SELF_REPORT_ANALYSIS_CORRECTNESS_INDEX = 3;
    public static final int SELF_REPORT_ANSWER_INDEX = 4;
    public static final int SELF_REPORT_FEATRUEIDS_INDEX = 5;
    //endregion

    public static boolean hasPermissions(Context con, String... permissions) {
        Context context = con.getApplicationContext();
        if (context != null && permissions != null)
            for (String permission : permissions)
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;

        assert context != null;
        if (!isAppUsageAccessGranted(context))
            return false;

        return isGPSLocationOn(context);
    }

    private static boolean isGPSLocationOn(Context con) {
        LocationManager lm = (LocationManager) con.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled;
        boolean network_enabled;
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps_enabled || network_enabled;
    }

    private static boolean isAppUsageAccessGranted(Context con) {
        try {
            PackageManager packageManager = con.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(con.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) con.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    static AlertDialog requestPermissions(final Activity activity) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.permissions))
                .setMessage(activity.getString(R.string.grant_permissions))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> Tools.grantPermissions(activity, PERMISSIONS))
                .setNegativeButton(android.R.string.cancel, null);


        return alertDialog.show();
    }

    static Dialog requestPermissionsWithCustomDialog(final Activity activity) {
        View view = activity.getLayoutInflater().inflate(R.layout.permission_dialog, null);
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        dialog.setContentView(view);
        dialog.setCancelable(false);
        ConstraintLayout permissionLayout = view.findViewById(R.id.permission_dialog_layout);
        Button permissionBtn = view.findViewById(R.id.permission_btn);
        permissionBtn.setOnClickListener(view1 -> {
            Tools.grantPermissions(activity, PERMISSIONS);
            dialog.dismiss();
        });
        return dialog;
    }

    private static void grantPermissions(Activity activity, String... permissions) {
        boolean simple_permissions_granted = true;
        for (String permission : permissions)
            if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                simple_permissions_granted = false;
                break;
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) activity.getApplicationContext().getSystemService(Context.POWER_SERVICE);
            Intent i = new Intent();
            if (powerManager.isIgnoringBatteryOptimizations(activity.getPackageName())) {
                i.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            } else {
                i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                i.setData(Uri.parse("package:" + activity.getPackageName()));
            }
        }
        if (!isAppUsageAccessGranted(activity.getApplicationContext()))
            activity.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        if (!isGPSLocationOn(activity.getApplicationContext()))
            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        if (!simple_permissions_granted)
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);

    }

    public static void checkAndSendUsageAccessStats(Context con) {
        // Init AppUseDb if it's null
        if (AppUseDb.getDB() == null)
            AppUseDb.init(con);

        SharedPreferences loginPrefs = con.getSharedPreferences("UserLogin", MODE_PRIVATE);
        long lastSavedTimestamp = loginPrefs.getLong("lastUsageSubmissionTime", -1);

        Calendar fromCal = Calendar.getInstance();
        if (lastSavedTimestamp == -1)
            fromCal.add(Calendar.DAY_OF_WEEK, -2);
        else
            fromCal.setTime(new Date(lastSavedTimestamp));

        final Calendar tillCal = Calendar.getInstance();
        tillCal.set(Calendar.MILLISECOND, 0);

        PackageManager localPackageManager = con.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        String launcher_packageName = Objects.requireNonNull(localPackageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)).activityInfo.packageName;

        UsageStatsManager usageStatsManager = (UsageStatsManager) con.getSystemService(Context.USAGE_STATS_SERVICE);

        /* Zaturi start */
        SharedPreferences interventionPrefs = con.getSharedPreferences("intervention", MODE_PRIVATE);
        SharedPreferences stressReportPrefs = con.getSharedPreferences("stressReport", MODE_PRIVATE);
        // Check if between 11am and 11pm
        if (isBetween11am11pm()) {
            // If not MUTE_TODAY
            if (!interventionPrefs.getBoolean("muteToday", false)) {
                // If user didn't perform stress intervention today
                if (!interventionPrefs.getBoolean("didIntervention", false)) {
                    // Check if the last self stress report was LITTLE_HIGH or HIGH
//                    int lastSelfStressReport = stressReportPrefs.getInt("reportAnswer", 0);
//                    if (lastSelfStressReport == 1 || lastSelfStressReport == 2) {

                        // Retrieve usage events in the last 3 seconds
                        UsageEvents.Event currentEvent;
                        UsageEvents usageEvents = usageStatsManager.queryEvents(
                                System.currentTimeMillis() - 3000,
                                System.currentTimeMillis());
                        // Get time of last phone usage (except communication apps)
                        //            long zaturiLastPhoneUsage = loginPrefs.getLong("zaturiLastPhoneUsage", -1);
                        long zaturiLastPhoneUsage = loginPrefs.getLong("zaturiLastPhoneUsage", System.currentTimeMillis());
                        while (usageEvents.hasNextEvent()) {
                            currentEvent = new UsageEvents.Event();
                            usageEvents.getNextEvent(currentEvent);
                            String packageName = currentEvent.getPackageName();
                            // For all apps except communication apps
                            Log.d("ZATURI", "packageName: " + packageName);
                            if (!Arrays.asList(COMMUNICATION_APPS).contains(packageName)
                                    && !packageName.contains(launcher_packageName)) {
                                // When an app is opened/resumed
                                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                                    Log.d("ZATURI", "ACTIVITY_RESUMED");
                                    // If the interval from last usage > 10 min
                                    if (System.currentTimeMillis() - zaturiLastPhoneUsage > 600000) {
                                        // Turn on flag for timer and set the timer
                                        // & remember the package name
                                        SharedPreferences.Editor editor = loginPrefs.edit();
                                        editor.putBoolean("zaturiTimerOn", true);
                                        editor.putLong("zaturiTimerStart", currentEvent.getTimeStamp());
                                        editor.putString("zaturiPackage", packageName);
                                        editor.apply();
                                    }
                                } else if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED
                                        || currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_STOPPED) {
                                    if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) Log.d("ZATURI", "ACTIVITY_PAUSED");
                                    if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_STOPPED) Log.d("ZATURI", "ACTIVITY_STOPPED");
                                    // When an app closes
                                    // Check if timer is on
                                    if (loginPrefs.getBoolean("zaturiTimerOn", false)) {
                                        // Check if the package name matches
                                        if (packageName.equals(loginPrefs.getString("zaturiPackage", ""))) {
                                            // Check if the usage duration < 30 sec
                                            if (currentEvent.getTimeStamp() -
                                                    loginPrefs.getLong("zaturiTimerStart", 0) < 30000) {
                                                // Then send a notification
                                                sendStressInterventionNoti(con);

                                                // Reset the variables
                                                SharedPreferences.Editor editor = loginPrefs.edit();
                                                editor.putBoolean("zaturiTimerOn", false);
                                                editor.putLong("zaturiTimerStart", 0);
                                                editor.putString("zaturiPackage", "");
                                                editor.apply();
                                            }
                                        }
                                    }

                                    // Save last phone usage time (except for communication app usage)
                                    SharedPreferences.Editor editor = loginPrefs.edit();
                                    editor.putLong("zaturiLastPhoneUsage", currentEvent.getTimeStamp());
                                    editor.apply();
                                }
                            }
                        }
//                    }
                }
            }
        }
        /* Zaturi end */

        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, fromCal.getTimeInMillis(), System.currentTimeMillis());
        for (UsageStats usageStats : stats) {
            //do not include launcher's package name
            if (!AppUseDb.initialized) {
                AppUseDb.init(con);
            }
            if (usageStats.getTotalTimeInForeground() > 0 && !usageStats.getPackageName().contains(launcher_packageName)) {
                AppUseDb.saveAppUsageStat(usageStats.getPackageName(), usageStats.getLastTimeUsed(), usageStats.getTotalTimeInForeground());
            }
        }
        SharedPreferences.Editor editor = loginPrefs.edit();
        editor.putLong("lastUsageSubmissionTime", tillCal.getTimeInMillis());
        editor.apply();
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void enable_touch(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    static void disable_touch(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private static boolean isReachable;

    public static boolean isNetworkAvailable() {
        try {
            Thread thread = new Thread(() -> {
                try {
                    InetAddress address = InetAddress.getByName("www.google.com");
                    isReachable = !address.toString().equals("");
                } catch (Exception e) {
                    e.printStackTrace();
                    isReachable = false;
                }
            });
            thread.start();
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
            isReachable = false;
        }

        return isReachable;
    }

    public static boolean isMainServiceRunning(Context con) {
        ActivityManager manager = (ActivityManager) con.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLocationServiceRunning(Context con) {
        ActivityManager manager = (ActivityManager) con.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static synchronized boolean heartbeatNotSent(final Context con) {
        final SharedPreferences loginPrefs = con.getSharedPreferences("UserLogin", MODE_PRIVATE);

        if (Tools.isNetworkAvailable()) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    ManagedChannel channel = ManagedChannelBuilder.forAddress(
                            con.getString(R.string.grpc_host),
                            Integer.parseInt(con.getString(R.string.grpc_port))
                    ).usePlaintext().build();
                    ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                    EtService.SubmitHeartbeat.Request submitHeartbeatRequestMessage = EtService.SubmitHeartbeat.Request.newBuilder()
                            .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                            .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                            .setCampaignId(Integer.parseInt(con.getString(R.string.stress_campaign_id)))
                            .build();
                    try {
                        @SuppressWarnings("unused")
                        EtService.SubmitHeartbeat.Response responseMessage = stub.submitHeartbeat(submitHeartbeatRequestMessage);
                    } catch (StatusRuntimeException e) {
                        Log.e("Tools", "DataCollectorService.setUpHeartbeatSubmissionThread() exception: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        channel.shutdown();
                    }
                }
            }.start();
        }

        return false;
    }

    public static int getEMAOrderAtExactTime(Calendar cal) {
        for (short i = 0; i < EMA_NOTIF_HOURS.length; i++) {
            if (cal.get(Calendar.HOUR_OF_DAY) == EMA_NOTIF_HOURS[i] && cal.get(Calendar.MINUTE) == 0) {
                return i + 1;
            }
        }
        return 0;
    }

    public static int getReportOrderAtExactTime(Calendar cal) {
        for (short i = 0; i < REPORT_NOTIF_HOURS.length; i++) {
            if (cal.get(Calendar.HOUR_OF_DAY) == REPORT_NOTIF_HOURS[i] && cal.get(Calendar.MINUTE) == 0) {
                return i + 1;
            }
        }
        return 0;
    }


    public static int getEMAOrderFromRangeAfterEMA(Calendar cal) {
        long t = (cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)) * 1000;
        for (int i = 0; i < EMA_NOTIF_HOURS.length; i++) {
            if ((EMA_NOTIF_HOURS[i] * 3600 * 1000) <= t && t <= (EMA_NOTIF_HOURS[i] * 3600 * 1000) + EMA_RESPONSE_EXPIRE_TIME * 1000)
                return i + 1;

        }
        return 0;
    }

    public static int getReportOrderFromRangeAfterReport(Calendar cal) {
        long t = (cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)) * 1000;
        for (int i = 0; i < REPORT_NOTIF_HOURS.length; i++) {
            if ((REPORT_NOTIF_HOURS[i] * 3600 * 1000) <= t && t <= (REPORT_NOTIF_HOURS[i] * 3600 * 1000) + REPORT_RESPONSE_EXPIRE_TIME * 1000)
                return i + 1;
        }
        return 0;
    }

    static void perform_logout(Context con) {

        SharedPreferences loginPrefs = con.getSharedPreferences("UserLogin", MODE_PRIVATE);
        SharedPreferences locationPrefs = con.getSharedPreferences("UserLocations", MODE_PRIVATE);

        SharedPreferences.Editor editorLocation = locationPrefs.edit();
        editorLocation.clear();
        editorLocation.apply();

        SharedPreferences.Editor editorLogin = loginPrefs.edit();
        editorLogin.remove("username");
        editorLogin.remove("password");
        editorLogin.putBoolean("logged_in", false);
        editorLogin.remove("ema_btn_make_visible");
        editorLogin.clear();
        editorLogin.apply();

        GeofenceHelper.removeAllGeofences(con);
    }

    public static boolean inRange(long value, long start, long end) {
        return start < value && value < end;
    }

    public static void updatePoint(Context context) {
        long calDateDays;
        SharedPreferences stepChangePrefs = context.getSharedPreferences("stepChange", MODE_PRIVATE);
        long joinTimestamp = stepChangePrefs.getLong("join_timestamp", 0);
        long timestamp = System.currentTimeMillis();

        long diffTimestamp = timestamp - joinTimestamp;
        calDateDays = Math.abs(diffTimestamp / (24 * 60 * 60 * 1000));


        SharedPreferences prefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
        SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", MODE_PRIVATE);
        int dataSourceId = prefs.getInt("REWARD_POINTS", -1);
        assert dataSourceId != -1;
        Log.d(TAG, "REWARD_POINTS dataSourceId: " + dataSourceId);
        DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f, timestamp, calDateDays, Tools.POINT_INCREASE_VALUE);

        fastUploadToServer(context);
    }

    /* Zaturi start */
    private static boolean isBetween11am11pm() {
        // Check if the current time is between 11AM and 11PM
        int start = 11;
        int end = 23;
        int hours = (end - start) % 24;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, start);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long startHourMilli = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, hours);
        long endHourMilli = cal.getTimeInMillis();
        return System.currentTimeMillis() >= startHourMilli
                && System.currentTimeMillis() <= endHourMilli;
    }

    public static void sendStressInterventionNoti(Context con) {
        // Get current intervention
        SharedPreferences prefs = con.getSharedPreferences("intervention", MODE_PRIVATE);
        String curIntervention = prefs.getString("curIntervention", "");
        Long lastInterventionTime = prefs.getLong("zaturiLastIntervention", 0);

        if (System.currentTimeMillis() - lastInterventionTime > 10000) {
            if (!curIntervention.equals("")) {
                // Create and send stress intervention notification

                final NotificationManager notificationManager = (NotificationManager)
                        con.getSystemService(Context.NOTIFICATION_SERVICE);

//        Intent notificationIntent = new Intent(MainService.this, EMAActivity.class);
                Intent nextTimeIntent = new Intent(con, InterventionService.class);
                // TODO: Save to server : 다음에 하기 (STRESS_NEXT_TIME)
                nextTimeIntent.putExtra("stress_next_time", true); // STRESS_NEXT_TIME 다음에 하기 1
                nextTimeIntent.putExtra("path", 1); // path is 1 (notification)
//        saveStressIntervention(con, System.currentTimeMillis(), curIntervention,
//                STRESS_NEXT_TIME, PATH_NOTIFICATION);

                Intent muteTodayIntent = new Intent(con, InterventionService.class);
                muteTodayIntent.putExtra("stress_mute_today", true); // STRESS_MUTE_TODAY 오늘의 알림 끄기 2
                muteTodayIntent.putExtra("path", 1); // path is 1 (notification)
                // TODO: Save to server : 오늘의 알림 끄기 (STRESS_MUTE_TODAY)
                //  Change muteToday to true
//        saveStressIntervention(con, System.currentTimeMillis(), curIntervention,
//                STRESS_MUTE_TODAY, PATH_NOTIFICATION);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putBoolean("muteToday", true);
//        editor.apply();

                Intent stressRelIntent = new Intent(con, InterventionService.class);
                stressRelIntent.putExtra("stress_do_intervention", true); // STRESS_DO_INTERVENTION 스트레스 해소하기 4
                stressRelIntent.putExtra("path", 1); // path is 1 (notification)
                // TODO: Save to server : 스트레스 해소하기 (STRESS_DO_INTERVENTION)
                //  Go to 스트레스 해소하기 화면 (to be determined)
                //  Change didIntervention to true
//        saveStressIntervention(con, System.currentTimeMillis(), curIntervention,
//                STRESS_DO_INTERVENTION, PATH_NOTIFICATION);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putBoolean("didIntervention", true);
//        editor.apply();

                PendingIntent nextTimePI = PendingIntent.getService(con,
                        1, nextTimeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent muteTodayPI = PendingIntent.getService(con,
                        2, muteTodayIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent stressRelPI = PendingIntent.getService(con,
                        3, stressRelIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                RemoteViews notificationLayout = new RemoteViews(con.getPackageName(), R.layout.notification_intervention);
                notificationLayout.setTextViewText(R.id.textIntervention, curIntervention);
                notificationLayout.setOnClickPendingIntent(R.id.btnNextTime, nextTimePI);
                notificationLayout.setOnClickPendingIntent(R.id.btnMuteToday, muteTodayPI);
                notificationLayout.setOnClickPendingIntent(R.id.btnStressRel, stressRelPI);

                String channelId = con.getString(R.string.notif_channel_id);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        con.getApplicationContext(), channelId);
                builder.setContentTitle(con.getString(R.string.app_name))
                        .setTimeoutAfter(1000 * EMA_RESPONSE_EXPIRE_TIME)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setContent(notificationLayout)
                        .setCustomContentView(notificationLayout)
                        .setAutoCancel(true)
                        .setCategory(CATEGORY_ALARM)
                        .setSmallIcon(R.mipmap.ic_launcher_low_foreground)
                        .setPriority(NotificationCompat.PRIORITY_MAX);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId,
                            con.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                    if (notificationManager != null) {
                        notificationManager.createNotificationChannel(channel);
                    }
                }

                final Notification notification = builder.build();
                if (notificationManager != null) {
                    notificationManager.notify(ZATURI_NOTIFICATION_ID, notification);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("zaturiLastIntervention", System.currentTimeMillis());
                    editor.apply();
                    saveStressIntervention(con, System.currentTimeMillis(), curIntervention,
                            STRESS_PUSH_NOTI_SENT, PATH_NOTIFICATION);
                }
            }
        }
    }

    public static void saveStressIntervention(Context con, long timestamp, String curIntervention,
                                              int action, int path) {
        SharedPreferences prefs = con.getSharedPreferences("Configurations", MODE_PRIVATE);

        int dataSourceId = prefs.getInt("STRESS_INTERVENTION", -1);
        assert dataSourceId != -1;
        if (curIntervention.equals(""))
            curIntervention = "NOT_SELECT_INTERVENTION";
        DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f,
                timestamp, curIntervention, action, path);

        fastUploadToServer(con);
    }
    /* Zaturi end */

    private static void fastUploadToServer(Context context) {
        SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", MODE_PRIVATE);

        // upload to server
        Thread fastUploadThread = new Thread() {
            @Override
            public void run() {
                if (Tools.isNetworkAvailable()) {
                    Cursor cursor = DbMgr.getSensorData();
                    if (cursor.moveToFirst()) {
                        ManagedChannel channel = ManagedChannelBuilder.forAddress(
                                context.getString(R.string.grpc_host),
                                Integer.parseInt(context.getString(R.string.grpc_port))
                        ).usePlaintext().build();
                        ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

                        int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                        String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);

                        try {
                            do {
                                EtService.SubmitDataRecord.Request submitDataRecordRequestMessage = EtService.SubmitDataRecord.Request.newBuilder()
                                        .setUserId(userId)
                                        .setEmail(email)
                                        .setDataSource(cursor.getInt(1))
                                        .setTimestamp(cursor.getLong(2))
                                        .setValues(cursor.getString(4))
                                        .setCampaignId(Integer.parseInt(context.getString(R.string.stress_campaign_id)))
                                        .build();
                                EtService.SubmitDataRecord.Response responseMessage = stub.submitDataRecord(submitDataRecordRequestMessage);

                                if (responseMessage.getSuccess()) {
                                    DbMgr.deleteRecord(cursor.getInt(0));
                                }

                            } while (cursor.moveToNext());
                        } catch (StatusRuntimeException e) {
                            Log.e(TAG, "DataCollectorService.setUpDataSubmissionThread() exception: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            channel.shutdown();
                        }
                    }
                    cursor.close();
                }
            }
        };
        fastUploadThread.start();
    }

    public static JSONObject[] parsingStressReport(String originStressReportStr) {
        // REPORT Parsing
        try {
            JSONObject jsonObject = new JSONObject(originStressReportStr);
            JSONObject[] jsonObjects = new JSONObject[jsonObject.length()];

            for (short i = 0; i < jsonObject.length(); i++) {
                jsonObjects[i] = jsonObject.getJSONObject(String.valueOf(i));
            }
//            jsonObjects[0] = jsonObject.getJSONObject("1"); // jsonObjects[key - 1] has a report for prediction stress level is key
//            jsonObjects[1] = jsonObject.getJSONObject("2");
//            jsonObjects[2] = jsonObject.getJSONObject("3");
            return jsonObjects;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveApplicationLog(Context con, String uniqueTagForEachActivityOrEvent, String action, Object... params) {
        // Save MindScope working log
        StringBuilder sb = new StringBuilder();
        for (Object value : params)
            sb.append(value).append(Tools.DATA_SOURCE_SEPARATOR);
        if (sb.length() > 0)
            sb.replace(sb.length() - 1, sb.length(), "");
        new Thread(() -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MILLISECOND, 0);
            long timestamp = cal.getTimeInMillis();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String logDate = simpleDateFormat.format(cal.getTimeInMillis());
            SharedPreferences prefs = con.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
            int dataSourceId = prefs.getInt("APPLICATION_LOG", -1);
            assert dataSourceId != -1;
            if (uniqueTagForEachActivityOrEvent != null && action != null)
                DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f, timestamp, logDate, uniqueTagForEachActivityOrEvent, action, sb.toString());
        }).start();
    }

    public static int getReportPreviousOrder(Calendar cal) {
        int curHour = cal.get(Calendar.HOUR_OF_DAY);
//        if ((REPORT_NOTIF_HOURS[0] - REPORT_DURATION) <= curHour &&
//                cal.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[0]) {
//            return 0;
//        }
        if ((REPORT_NOTIF_HOURS[1] - REPORT_DURATION) <= curHour &&
                cal.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[1]) {
            return 1;
        } else if ((REPORT_NOTIF_HOURS[2] - REPORT_DURATION) <= curHour &&
                cal.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[2]) {
            return 2;
        } else if ((REPORT_NOTIF_HOURS[3] - REPORT_DURATION) <= curHour &&
                cal.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[3]) {
            return 3;
        } else /*if(REPORT_NOTIF_HOURS[3] == curHour)*/ {
            return 4;
        }
    }

    public static void stepCheck(Context context) {
        //region step check
        Calendar curCal = Calendar.getInstance();
        long curTimestamp = curCal.getTimeInMillis();

        SharedPreferences stepChangePrefs = context.getSharedPreferences("stepChange", MODE_PRIVATE);

        long joinTimestamp = stepChangePrefs.getLong("join_timestamp", 0);
        if (joinTimestamp == 0) {
            joinTimestamp = getJoinTime(context);
        }
        int stepCheck = stepChangePrefs.getInt("stepCheck", 0);

        long diff = curTimestamp - joinTimestamp;
        if (diff >= STEP0_EXPIRE_TIMESTAMP_VALUE && diff < STEP1_EXPIRE_TIMESTAMP_VALUE) {
            // step1
            SharedPreferences.Editor stepEditor = stepChangePrefs.edit();
            stepEditor.putInt("stepCheck", 1);
            stepEditor.apply();
        } else if (diff >= STEP1_EXPIRE_TIMESTAMP_VALUE) {
            // step2
            SharedPreferences.Editor stepEditor = stepChangePrefs.edit();
            stepEditor.putInt("stepCheck", 2);
            if (diff >= STEP1_EXPIRE_TIMESTAMP_VALUE + 60 * 60 * 11 * 1000) {
                stepEditor.putBoolean("first_start_care_step2_check", true);
                stepEditor.putBoolean("first_start_step2_check", true);
            }
            stepEditor.apply();
        }
        //endregion

    }

    public static long getJoinTime(Context context) {
        long firstDayTimestamp = 0;
        SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        SharedPreferences stepChangePrefs = context.getSharedPreferences("stepChange", MODE_PRIVATE);
        SharedPreferences.Editor editor = stepChangePrefs.edit();

        if (Tools.isNetworkAvailable()) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(context.getString(R.string.grpc_host), Integer.parseInt(context.getString(R.string.grpc_port))).usePlaintext().build();
            ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
            EtService.RetrieveParticipantStats.Request retrieveParticipantStatisticsRequestMessage = EtService.RetrieveParticipantStats.Request.newBuilder()
                    .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                    .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetCampaignId(Integer.parseInt(context.getString(R.string.stress_campaign_id)))
                    .build();
            try {
                EtService.RetrieveParticipantStats.Response responseMessage = stub.retrieveParticipantStats(retrieveParticipantStatisticsRequestMessage);
                if (responseMessage.getSuccess()) {
                    long joinTimestamp = responseMessage.getCampaignJoinTimestamp();
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(joinTimestamp);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    editor.putLong("join_timestamp", cal.getTimeInMillis());
                    editor.apply();
                    firstDayTimestamp = cal.getTimeInMillis();
                }
            } catch (StatusRuntimeException e) {
                e.printStackTrace();
            }
            channel.shutdown();
        }
        return firstDayTimestamp;
    }

}


class GeofenceHelper {
    private static GeofencingClient geofencingClient;
    private static PendingIntent geofencePendingIntent;
    private static final String TAG = "GeofenceHelper";

    static void startGeofence(Context context, String location_id, LatLng position, int radius) {
        if (geofencingClient == null)
            geofencingClient = LocationServices.getGeofencingClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        GeofencingRequest geofencingRequest = getGeofenceRequest(createGeofence(location_id, position, radius));
        Log.e(TAG, "Setting location with ID: " + geofencingRequest.getGeofences().get(0).getRequestId());
        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent(context))
                .addOnSuccessListener(aVoid -> {
                    // Geofences added
                    Log.e(TAG, "Geofence added");
                })
                .addOnFailureListener(e -> {
                    // Failed to add geofences
                    Log.e(TAG, "Geofence add failed: " + e.toString());
                });

    }

    private static GeofencingRequest getGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private static Geofence createGeofence(String location_id, LatLng position, int radius) {
        return new Geofence.Builder()
                .setRequestId(location_id)
                .setCircularRegion(position.latitude, position.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)  // geofence should never expire
                .setNotificationResponsiveness(60 * 1000)          //notifying after 60sec. Can save power
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private static PendingIntent getGeofencePendingIntent(Context context) {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    static void removeGeofence(Context context, String reqID) {
        if (geofencingClient == null)
            geofencingClient = LocationServices.getGeofencingClient(context);

        ArrayList<String> reqIDs = new ArrayList<>();
        reqIDs.add(reqID);
        geofencingClient.removeGeofences(reqIDs)
                .addOnSuccessListener(aVoid -> {
                    // Geofences removed
                    Log.e(TAG, "Geofence removed");
                })
                .addOnFailureListener(e -> {
                    // Failed to remove geofences
                    Log.e(TAG, "Geofence not removed: " + e.toString());
                });
    }

    static void removeAllGeofences(Context context) {
        if (geofencingClient == null)
            geofencingClient = LocationServices.getGeofencingClient(context);
        geofencingClient.removeGeofences(getGeofencePendingIntent(context));
    }

}

