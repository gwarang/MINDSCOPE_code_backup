package kr.ac.inha.mindscope.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.MapsActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.StressReportActivity;
import kr.ac.inha.mindscope.Tools;

import static kr.ac.inha.mindscope.StressReportActivity.REPORT_NOTIF_HOURS;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV1;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV2;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV3;
import static kr.ac.inha.mindscope.fragment.StressReportFragment1.REPORT_DURATION;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.setListViewHeightBasedOnChildren;

public class MeFragmentStep2 extends Fragment {

    public static final long TIMESTAMP_ONE_DAY = 60 * 60 * 24 * 1000;

    private static final String TAG = "MeFragmentStep2";
    private static final String LAST_NAV_FRG1 = "me";
    private static final String LAST_NAV_FRG2 = "care";
    private static final String LAST_NAV_FRG3 = "report";
    private static final int DAYS_UNITL_STEP_STARTS = 4; // TODO change 15 for study
    public static JSONObject[] jsonObjects;
    public static View view;
    static int lastReportHours;
    public int stressLevel;
    int day_num;
    int order;
    Double accuracy;
    ScrollView reasonContainer;
    ImageView stressImg;
    ConstraintLayout allContainer;
    ConstraintLayout firstStartBefore11hoursContainer;
    TextView before11hoursTextView;
    long reportTimestamp;
    List<String> selfStressReports;
    boolean notSubmit = false;
    private String stressReportStr;
    private ImageButton btnMap;
    private Button stepTestBtn;
    private TextView dateView;
    private TextView timeView;
    private TextView stressLvView;
    private TextView waitNextReportTextView;
    private TextView versionNameTextView;
    private Button reportBtn;
    private String feature_ids;
    SharedPreferences lastPagePrefs;
    final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            applyUi(view);
        }
    };

    public MeFragmentStep2() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastPagePrefs = requireActivity().getSharedPreferences("LastPage", Context.MODE_PRIVATE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_me_step2, container, false);
        init(view);
        gettingStressReportFromGRPC(view); // get Stress Report Result from gRPC server;

        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
        SharedPreferences stressReportPrefs = requireActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);

        NavController navController = Navigation.findNavController(view);
        Log.e(TAG, "navController : " + navController.getCurrentDestination().getId());

        if(stressReportPrefs.getBoolean("today_last_report", false)){
//            SharedPreferences.Editor editor = stressReportPrefs.edit();
//            editor.putBoolean("today_last_report", false);
//            editor.apply();
            navController.navigate(R.id.action_me_to_care_step2);
        }
        else {
            String last_frg = lastPagePrefs.getString("last_open_nav_frg", "");
            switch (last_frg){
                case LAST_NAV_FRG1:
                    // nothing
                    break;
                case LAST_NAV_FRG2:
                    Navigation.findNavController(view).navigate(R.id.action_me_to_care_step2);
                    break;
                case LAST_NAV_FRG3:
                    Navigation.findNavController(view).navigate(R.id.action_me_to_report_step2);
                    break;
                default:
                    // nothing
                    break;
            }
        }

    }


    public void init(View view){
        Context context = MainActivity.getInstance();
        allContainer = view.findViewById(R.id.frg_me_step2_container);
        stressLvView = view.findViewById(R.id.txt_stress_level);
        waitNextReportTextView = view.findViewById(R.id.txt_wait_next_report);
        waitNextReportTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tools.sendStressInterventionNoti(context);
            }
        });
        before11hoursTextView = view.findViewById(R.id.frg_me_step2_before_time);
        firstStartBefore11hoursContainer = view.findViewById(R.id.frg_me_step2_before_11hours_container);
        reasonContainer = view.findViewById(R.id.stress_reason_container);
        stressImg = view.findViewById(R.id.frg_me_step2_img1);
        btnMap = view.findViewById(R.id.fragment_me_step2_btn_map);
        btnMap.setOnClickListener(view13 -> {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
        });
        dateView = view.findViewById(R.id.frg_me_step2_date1);
        timeView = (TextView) view.findViewById(R.id.frg_me_step2_time1);
        versionNameTextView = view.findViewById(R.id.version_name_step2);
        versionNameTextView.setText(getVersionInfo(requireContext()));
    }

    public void applyUi(View view) {
//        SharedPreferences stressReportPrefs = requireActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
        Context context = MainActivity.getInstance();

        SharedPreferences stepChangePrefs = context.getSharedPreferences("stepChange", Context.MODE_PRIVATE);
        SharedPreferences firstPref = requireActivity().getSharedPreferences("firstStart", Context.MODE_PRIVATE);
        Calendar cal = Calendar.getInstance();
        boolean firstStartStep2Check = stepChangePrefs.getBoolean("first_start_step2_check", false);
        boolean isFirstStartStep2DialogShowing = firstPref.getBoolean("firstStartStep2", false);
        if(!firstStartStep2Check && cal.get(Calendar.HOUR_OF_DAY) < 11){

            Calendar step2Cal = Calendar.getInstance();
            step2Cal.setTimeInMillis(stepChangePrefs.getLong("join_timestamp", 0));
            step2Cal.add(Calendar.DATE, DAYS_UNITL_STEP_STARTS);
            String stepDateStr = new SimpleDateFormat("yyyy년 MM월 dd일 (EE) ", Locale.getDefault()).format(step2Cal.getTimeInMillis());
            before11hoursTextView.setText(stepDateStr + context.getResources().getString(R.string.string_frg_me_stpe2_before_txt1));
            firstStartBefore11hoursContainer.setVisibility(View.VISIBLE);
            allContainer.setVisibility(View.INVISIBLE);
        }
        else{
            if(!firstStartStep2Check){
                SharedPreferences.Editor editor = stepChangePrefs.edit();
                editor.putBoolean("first_start_step2_check", true);
                editor.apply();
            }

            if (feature_ids != null)
                featureViewUpdate(feature_ids, view);
            else
                Log.e(TAG, "feature_ids string is null");

            switch (stressLevel) {
                case STRESS_LV1:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        stressLvView.setText(Html.fromHtml(context.getResources().getString(R.string.string_stress_level_low)));
                    } else {
                        stressLvView.setText(Html.fromHtml(context.getResources().getString(R.string.string_stress_level_low), Html.FROM_HTML_MODE_LEGACY));
                    }
                    reasonContainer.setBackgroundColor(context.getResources().getColor(R.color.color_low_bg, context.getTheme()));
                    stressImg.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_low, context.getTheme()));
                    break;
                case STRESS_LV2:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        stressLvView.setText(Html.fromHtml(context.getResources().getString(R.string.string_stress_level_littlehigh)));
                    } else {
                        stressLvView.setText(Html.fromHtml(context.getResources().getString(R.string.string_stress_level_littlehigh), Html.FROM_HTML_MODE_LEGACY));
                    }
                    reasonContainer.setBackgroundColor(context.getResources().getColor(R.color.color_littlehigh_bg, context.getTheme()));
                    stressImg.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_littlehigh, context.getTheme()));
                    break;
                case STRESS_LV3:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        stressLvView.setText(Html.fromHtml(context.getResources().getString(R.string.string_stress_level_high)));
                    } else {
                        stressLvView.setText(Html.fromHtml(context.getResources().getString(R.string.string_stress_level_high), Html.FROM_HTML_MODE_LEGACY));
                    }
                    reasonContainer.setBackgroundColor(context.getResources().getColor(R.color.color_high_bg, context.getTheme()));
                    stressImg.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_high, context.getTheme()));
                    break;
            }

            cal.setTimeInMillis(reportTimestamp);
            Date currentTime = new Date();
            currentTime.setTime(reportTimestamp);
            String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
            dateView.setText(date_text);

            lastReportHours = cal.get(Calendar.HOUR_OF_DAY);
            Log.i(TAG, "current hours: " + lastReportHours);

            if (lastReportHours >= 22) {
                timeView.setText(context.getResources().getString(R.string.time_report_duration4));
            } else if (lastReportHours >= 18) {
                timeView.setText(context.getResources().getString(R.string.time_report_duration3));
            } else if (lastReportHours >= 14) {
                timeView.setText(context.getResources().getString(R.string.time_report_duration2));
            } else if (lastReportHours >= 10) {
                timeView.setText(context.getResources().getString(R.string.time_report_duration1));
            } else {
                timeView.setText(context.getResources().getString(R.string.time_report_duration4));
            }


//            stepTestBtn = view.findViewById(R.id.step_test_btn_step2);
//            stepTestBtn.setOnClickListener(view1 -> {
//                SharedPreferences stepChange = context.getSharedPreferences("stepChange", Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = stepChange.edit();
//
//                if (stepChange.getInt("stepCheck", 0) == 1) {
//                    Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
//                    stepTestBtn.setText("STEP 2");
//                    editor.putInt("stepCheck", 2);
//                    editor.apply();
//                } else {
//                    Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
//                    stepTestBtn.setText("STEP 1");
//                    editor.putInt("stepCheck", 1);
//                    editor.apply();
//                }
//            });

//            reportBtn = view.findViewById(R.id.report_test_btn);
//            reportBtn.setOnClickListener(view12 -> {
//                Intent intent = new Intent(getActivity(), StressReportActivity.class);
//                startActivity(intent);
//            });
        }
        if(isFirstStartStep2DialogShowing)
            startStressReportActivityWhenNotSubmitted();
    }

    public void featureViewUpdate(String feature_ids, View view) {
        Context context = MainActivity.getInstance();
        ArrayList<String> phoneReason = new ArrayList<>();
        ArrayList<String> activityReason = new ArrayList<>();
        ArrayList<String> socialReason = new ArrayList<>();
        ArrayList<String> locationReason = new ArrayList<>();
        ArrayList<String> sleepReason = new ArrayList<>();

        if (feature_ids.equals("")) {
            Log.i(TAG, "feature_ids is empty");
        } else {
            String[] featureArray = feature_ids.split(" ");

            for (int i = 0; i < featureArray.length; i++) {
                String[] splitArray = featureArray[i].split("-");
                int category = Integer.parseInt(splitArray[0]);
                String strID = "@string/feature_" + splitArray[0] + splitArray[1];
                String packName = MainActivity.getInstance().getPackageName();
                int resId = context.getResources().getIdentifier(strID, "string", packName);

                if (category <= 5) {
                    activityReason.add(context.getResources().getString(resId));
                } else if (category <= 11) {
                    socialReason.add(context.getResources().getString(resId));
                } else if (category <= 16) {
                    locationReason.add(context.getResources().getString(resId));
                } else if (category <= 28) {
                    phoneReason.add(context.getResources().getString(resId));
                } else {
                    sleepReason.add(context.getResources().getString(resId));
                }

                if (i == 4) // maximun number of showing feature is five
                    break;
            }
        }

        Log.d(TAG, "phoneReason" + phoneReason.toString());
        Log.d(TAG, "activityReason" + activityReason.toString());

        ListView phoneListView = view.findViewById(R.id.me_listview_phone);
        ListView activityListView = view.findViewById(R.id.me_listview_activity);
        ListView socialListView = view.findViewById(R.id.me_listview_social);
        ListView locationListView = view.findViewById(R.id.me_listview_location);
        ListView sleepListView = view.findViewById(R.id.me_listview_sleep);
        LinearLayout phoneContainer = view.findViewById(R.id.me_listview_phone_container);
        LinearLayout activityContainer = view.findViewById(R.id.me_listview_activity_container);
        LinearLayout socialContainer = view.findViewById(R.id.me_listview_social_container);
        LinearLayout locationContainer = view.findViewById(R.id.me_listview_location_container);
        LinearLayout sleepContainer = view.findViewById(R.id.me_listview_sleep_container);


        ArrayAdapter<String> phoneAdapter = new ArrayAdapter<>(
                context, R.layout.item_feature_ids, phoneReason
        );
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(
                context, R.layout.item_feature_ids, activityReason
        );
        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(
                context, R.layout.item_feature_ids, socialReason
        );
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                context, R.layout.item_feature_ids, locationReason
        );
        ArrayAdapter<String> sleepAdapter = new ArrayAdapter<>(
                context, R.layout.item_feature_ids, sleepReason
        );

        phoneListView.setAdapter(phoneAdapter);
        activityListView.setAdapter(activityAdapter);
        socialListView.setAdapter(socialAdapter);
        locationListView.setAdapter(locationAdapter);
        sleepListView.setAdapter(sleepAdapter);


        if (phoneReason.isEmpty())
            phoneContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(phoneListView);
            phoneContainer.setVisibility(View.VISIBLE);
        }

        if (activityReason.isEmpty())
            activityContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(activityListView);
            activityContainer.setVisibility(View.VISIBLE);
        }

        if (socialReason.isEmpty())
            socialContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(socialListView);
            socialContainer.setVisibility(View.VISIBLE);
        }

        if (locationReason.isEmpty())
            locationContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(locationListView);
            locationContainer.setVisibility(View.VISIBLE);
        }

        if (sleepReason.isEmpty())
            sleepContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(sleepListView);
            sleepContainer.setVisibility(View.VISIBLE);
        }

    }

    public void gettingStressReportFromGRPC(View view) {
        Context context = requireContext();
        stressReportStr = null;
        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        SharedPreferences configPrefs = requireActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

        Calendar fromCalendar = Calendar.getInstance();
        Calendar tillCalendar = Calendar.getInstance();

        int reportOrder = getReportPreviousOrder(fromCalendar);
        // initialize calendar time
        if (reportOrder < 4) {
            fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - REPORT_DURATION);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
            tillCalendar.set(Calendar.MINUTE, 59);
            tillCalendar.set(Calendar.SECOND, 59);
        } else {
            if (fromCalendar.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[0] - REPORT_DURATION) {
                fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - REPORT_DURATION);
                fromCalendar.set(Calendar.MINUTE, 0);
                fromCalendar.set(Calendar.SECOND, 0);
                tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
                tillCalendar.set(Calendar.MINUTE, 59);
                tillCalendar.set(Calendar.SECOND, 59);
                long fromTimestampYesterday = fromCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
                long tillTimestampYesterday = tillCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
                fromCalendar.setTimeInMillis(fromTimestampYesterday);
                tillCalendar.setTimeInMillis(tillTimestampYesterday);
            } else {
                fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - REPORT_DURATION);
                fromCalendar.set(Calendar.MINUTE, 0);
                fromCalendar.set(Calendar.SECOND, 0);
                tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
                tillCalendar.set(Calendar.MINUTE, 59);
                tillCalendar.set(Calendar.SECOND, 59);
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG, "initialize fromCalendar: " + dateFormat.format(fromCalendar.getTime()));
        Log.i(TAG, "initialize tillCalendar: " + dateFormat.format(tillCalendar.getTime()));

        // test
//        long fillMillis = 1593554400000l;
//        long tillTime = 1593568801000l;

//        long fillMillis = 1593568801000l;
//        long tillTime = 1593583201000l;

//        long fillMillis = 1593583201000l;
//        long tillTime = 1593597601000l;
        if(Tools.isNetworkAvailable()){
            new Thread(() -> {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

                EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                        .setTargetDataSourceId(configPrefs.getInt("STRESS_PREDICTION", -1))
                        .setFromTimestamp(fromCalendar.getTimeInMillis()) //  change fromCalendar.getTimeInMillis()
                        .setTillTimestamp(tillCalendar.getTimeInMillis()) //  change tillCalendar.getTimeInMillis()
                        .build();


                final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
                if (responseMessage.getDoneSuccessfully()) {
                    List<String> values = responseMessage.getValueList();
                    List<Long> valuesTimestamp = responseMessage.getTimestampList();
                    if (!values.isEmpty()) {
                        stressReportStr = values.get(0);
                        Log.d(TAG, "stressReportStr: " + stressReportStr);
                    } else {
                        Log.d(TAG, "values empty");
                    }
                    if (!valuesTimestamp.isEmpty()) {
                        reportTimestamp = valuesTimestamp.get(0);
                        Log.d(TAG, "report timestamp from gRPC is " + reportTimestamp);
                    } else {
                        Log.d(TAG, "report timestamp from gRPC is empty");
                    }
                }

                // initialize timestamp from today 00:00:00 to 23:59:59
                Calendar fromCalendar2 = Calendar.getInstance();
                Calendar tillCalendar2 = Calendar.getInstance();
                fromCalendar2.set(Calendar.HOUR_OF_DAY, 0);
                fromCalendar2.set(Calendar.MINUTE, 0);
                fromCalendar2.set(Calendar.SECOND, 0);
                tillCalendar2.set(Calendar.HOUR_OF_DAY, 23);
                tillCalendar2.set(Calendar.MINUTE, 59);
                tillCalendar2.set(Calendar.SECOND, 59);
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Log.i(TAG, "initialize fromCalendar: " + dateFormat2.format(fromCalendar2.getTime()));
                Log.i(TAG, "initialize tillCalendar: " + dateFormat2.format(tillCalendar2.getTime()));

                // for test 2020/07/018 04:19:00 ~ 04:28:00
//        long fromtimestamp = 1595013540000l;
//        long tilltimestamp = 1595014080000l;


                retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetCampaignId(Integer.parseInt(context.getString(R.string.stress_campaign_id)))
                        .setTargetDataSourceId(configPrefs.getInt("SELF_STRESS_REPORT", -1))
                        .setFromTimestamp(fromCalendar.getTimeInMillis()) // change fromCalendar.getTimeInMillis()
                        .setTillTimestamp(tillCalendar.getTimeInMillis()) // change tillCalendar.getTimeInMillis()
                        .build();


                final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage2 = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
                if (responseMessage2.getDoneSuccessfully()) {
                    List<String> values = responseMessage2.getValueList();
                    if (!values.isEmpty()) {
                        for (String report : values) {
                            selfStressReports = values;
                            Log.i(TAG, "selfreport " + report);
                        }
                    } else {
                        Log.d(TAG, "values empty");
                    }

                }
                channel.shutdown();

                if (stressReportStr != null) {
                    jsonObjects = Tools.parsingStressReport(stressReportStr);
                    if (jsonObjects != null) {
                        for (short i = 0; i < jsonObjects.length; i++) {
                            try {
                                if (jsonObjects[0] != null) {
                                    if (jsonObjects[i].getBoolean("model_tag")) {
                                        stressLevel = i;
                                        day_num = jsonObjects[i].getInt("day_num");
                                        order = jsonObjects[i].getInt("ema_order");
                                        accuracy = jsonObjects[i].getDouble("accuracy");
                                        feature_ids = jsonObjects[i].getString("feature_ids");
//                                    SharedPreferences reportPrefs = requireActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
//                                    SharedPreferences.Editor editor = reportPrefs.edit();
//                                    editor.putInt("result", stressLevel);
//                                    editor.apply();
                                    }
                                } else {
                                    Log.e(TAG, "report is not in jsonObjects");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (jsonObjects != null && selfStressReports != null) {
                    for (String selfResults : selfStressReports) {
                        String[] result = selfResults.split(" ");
                        if (day_num == Integer.parseInt(result[1]) && order == Integer.parseInt(result[2])) {
                            stressLevel = Integer.parseInt(result[4]);
                            if (jsonObjects[stressLevel] != null) {
                                try {
                                    feature_ids = jsonObjects[stressLevel].getString("feature_ids");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                notSubmit = false;
                            }
                        } else {
                            notSubmit = true;
                        }
                    }
                }

                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);
            }).start();
        } else{
            Toast.makeText(context, context.getResources().getString(R.string.when_network_unable), Toast.LENGTH_SHORT).show();
        }


        //end getting data from gRPC
    }

    public int getReportPreviousOrder(Calendar cal) {
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

    public void getSelfStressReportDataFromGRPC() {
        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        SharedPreferences configPrefs = requireActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

        // initialize timestamp from today 00:00:00 to 23:59:59
        Calendar fromCalendar = Calendar.getInstance();
        Calendar tillCalendar = Calendar.getInstance();
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        tillCalendar.set(Calendar.HOUR_OF_DAY, 23);
        tillCalendar.set(Calendar.MINUTE, 59);
        tillCalendar.set(Calendar.SECOND, 59);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG, "initialize fromCalendar: " + dateFormat.format(fromCalendar.getTime()));
        Log.i(TAG, "initialize tillCalendar: " + dateFormat.format(tillCalendar.getTime()));

        // for test 2020/07/018 04:19:00 ~ 04:28:00
//        long fromtimestamp = 1595013540000l;
//        long tilltimestamp = 1595014080000l;

        new Thread(() -> {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

            ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

            EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                    .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                    .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                    .setTargetDataSourceId(configPrefs.getInt("SELF_STRESS_REPORT", -1))
                    .setFromTimestamp(fromCalendar.getTimeInMillis()) // change fromCalendar.getTimeInMillis()
                    .setTillTimestamp(tillCalendar.getTimeInMillis()) // change tillCalendar.getTimeInMillis()
                    .build();


            final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
            if (responseMessage.getDoneSuccessfully()) {
                List<String> values = responseMessage.getValueList();
                if (!values.isEmpty()) {
                    for (String report : values) {
                        selfStressReports = values;
                        Log.i(TAG, "selfreport " + report);
                    }
                } else {
                    Log.d(TAG, "values empty");
                }

            }
            channel.shutdown();
        }).start();

    }

    public void startStressReportActivityWhenNotSubmitted() {
        SharedPreferences selfReportSubmitCheckPrefs = requireActivity().getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
        SharedPreferences.Editor reportSubmitEditor = selfReportSubmitCheckPrefs.edit();
        boolean[] submits = {
                selfReportSubmitCheckPrefs.getBoolean("self_report_submit_check_1", false),
                selfReportSubmitCheckPrefs.getBoolean("self_report_submit_check_2", false),
                selfReportSubmitCheckPrefs.getBoolean("self_report_submit_check_3", false),
                selfReportSubmitCheckPrefs.getBoolean("self_report_submit_check_4", false),
        };
        Calendar cal = Calendar.getInstance();
        int curHour = cal.get(Calendar.HOUR_OF_DAY);
        int todayDate = cal.get(Calendar.DATE);
        if (todayDate != selfReportSubmitCheckPrefs.getInt("reportSubmitDate", -1)) {
            for (short i = 0; i < 4; i++) {
                reportSubmitEditor.putBoolean("self_report_submit_check_" + (i + 1), false);
                reportSubmitEditor.apply();
            }
        }
        for (short i = 0; i < 4; i++) {
            if (curHour == REPORT_NOTIF_HOURS[i] && !submits[i]) {
                int ema_order = Tools.getReportOrderFromRangeAfterReport(cal);
                if (ema_order != 0) {
                    Intent intent = new Intent(getActivity(), StressReportActivity.class);
//                    intent.putExtra("ema_order", ema_order);
                    startActivity(intent);
                }
            }
        }
    }

    public String getVersionInfo(Context context){
        String version = "Unknown";
        PackageInfo packageInfo;

        if(context == null){
            return version;
        }
        try {
            packageInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

}