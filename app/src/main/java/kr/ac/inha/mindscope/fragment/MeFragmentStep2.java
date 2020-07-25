package kr.ac.inha.mindscope.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private final long TIMESTAMP_ONE_DAY = 60 * 60 * 24 * 1000;

    private static final String TAG = "MeFragmentStep2";
    public static JSONObject[] jsonObjects;
    static int lastReportHours;
    public int stressLevel;
    int day_num;
    int order;
    Double accuracy;
    ScrollView reasonContainer;
    ImageView stressImg;
    ConstraintLayout allContainer;
    ConstraintLayout timeContainer;
    TextView beforeTextView;
    long reportTimestamp;
    List<String> selfStressReports;
    boolean notSubmit = false;
    private String stressReportStr;
    private ImageButton btnMap;
    private Button stepTestBtn;
    private TextView dateView;
    private TextView timeView;
    private TextView stressLvView;
    private Button reportBtn;
    private String feature_ids;

    public static View view;

    final Handler handler = new Handler(Looper.getMainLooper()){
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

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_me_step2, container, false);

        gettingStressReportFromGRPC(view); // get Stress Report Result from gRPC server;
//        getSelfStressReportDataFromGRPC();

//        if (stressReportStr != null) {
//            jsonObjects = Tools.parsingStressReport(stressReportStr);
//            if (jsonObjects != null) {
//                for (short i = 0; i < jsonObjects.length; i++) {
//                    try {
//                        if (jsonObjects[0] != null) {
//                            if (jsonObjects[i].getBoolean("model_tag")) {
//                                stressLevel = i;
//                                day_num = jsonObjects[i].getInt("day_num");
//                                order = jsonObjects[i].getInt("ema_order");
//                                accuracy = jsonObjects[i].getDouble("accuracy");
//                                feature_ids = jsonObjects[i].getString("feature_ids");
//                                SharedPreferences reportPrefs = requireActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
//                                SharedPreferences.Editor editor = reportPrefs.edit();
//                                editor.putInt("result", stressLevel);
//                                editor.apply();
//                            }
//                        } else {
//                            Log.e(TAG, "report is not in jsonObjects");
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        if (jsonObjects != null && selfStressReports != null) {
//            for (String selfResults : selfStressReports) {
//                String[] result = selfResults.split(" ");
//                if (day_num == Integer.parseInt(result[1]) && order == Integer.parseInt(result[2])) {
//                    stressLevel = Integer.parseInt(result[4]);
//                    if (jsonObjects[stressLevel] != null) {
//                        try {
//                            feature_ids = jsonObjects[stressLevel].getString("feature_ids");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        notSubmit = false;
//                    }
//                } else {
//                    notSubmit = true;
//                }
//            }
//        }


//        SharedPreferences stressReportPrefs = requireActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
//
//        allContainer = view.findViewById(R.id.frg_me_step2_container);
//        beforeTextView = view.findViewById(R.id.frg_me_step2_before_time);
//        timeContainer = view.findViewById(R.id.frg_me_step2_before_11hours_container);
//        stressLvView = view.findViewById(R.id.txt_stress_level);
//
//
//        if (feature_ids != null)
//            featureViewUpdate(feature_ids, view);
//        else
//            Log.e(TAG, "feature_ids string is null");
//
//        reasonContainer = view.findViewById(R.id.stress_reason_container);
//        stressImg = view.findViewById(R.id.frg_me_step2_img1);
//
//        switch (stressLevel) {
//            case STRESS_LV1:
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low)));
//                } else {
//                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low), Html.FROM_HTML_MODE_LEGACY));
//                }
//                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_low_bg, requireActivity().getTheme()));
//                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_low, requireActivity().getTheme()));
//                break;
//            case STRESS_LV2:
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_littlehigh)));
//                } else {
//                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_littlehigh), Html.FROM_HTML_MODE_LEGACY));
//                }
//                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_littlehigh_bg, requireActivity().getTheme()));
//                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_littlehigh, requireActivity().getTheme()));
//                break;
//            case STRESS_LV3:
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_high)));
//                } else {
//                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_high), Html.FROM_HTML_MODE_LEGACY));
//                }
//                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_high_bg, requireActivity().getTheme()));
//                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_high, requireActivity().getTheme()));
//                break;
//        }
//
//        btnMap = (ImageButton) view.findViewById(R.id.fragment_me_step2_btn_map);
//        btnMap.setOnClickListener(view13 -> {
//            Intent intent = new Intent(getActivity(), MapsActivity.class);
//            startActivity(intent);
//        });
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(reportTimestamp);
//
//        dateView = (TextView) view.findViewById(R.id.frg_me_step2_date1);
//        Date currentTime = cal.getTime();
//        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
//        dateView.setText(date_text);
//
//
//        lastReportHours = cal.get(Calendar.HOUR_OF_DAY);
//        Log.i(TAG, "current hours: " + lastReportHours);
//        timeView = (TextView) view.findViewById(R.id.frg_me_step2_time1);
//        if (lastReportHours >= 22) {
//            timeView.setText(getResources().getString(R.string.time_report_duration4));
//        } else if (lastReportHours >= 18) {
//            timeView.setText(getResources().getString(R.string.time_report_duration3));
//        } else if (lastReportHours >= 14) {
//            timeView.setText(getResources().getString(R.string.time_report_duration2));
//        } else if (lastReportHours >= 10) {
//            timeView.setText(getResources().getString(R.string.time_report_duration1));
//        } else {
//            timeView.setText(getResources().getString(R.string.time_report_duration4));
//        }
//
//
//        // TODO 추후 step 시간으로 확인할때는 삭제할 부분
//        stepTestBtn = (Button) view.findViewById(R.id.step_test_btn_step2);
//        stepTestBtn.setOnClickListener(view1 -> {
//            SharedPreferences stepChange = requireActivity().getSharedPreferences("stepChange", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = stepChange.edit();
//
//            if (stepChange.getInt("stepCheck", 0) == 1) {
//                Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
//                stepTestBtn.setText("STEP 2");
//                editor.putInt("stepCheck", 2);
//                editor.apply();
//            } else {
//                Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
//                stepTestBtn.setText("STEP 1");
//                editor.putInt("stepCheck", 1);
//                editor.apply();
//            }
//        });
//
//        reportBtn = (Button) view.findViewById(R.id.report_test_btn);
//        reportBtn.setOnClickListener(view12 -> {
//            Intent intent = new Intent(getActivity(), StressReportActivity.class);
//            startActivity(intent);
//        });
//
//
//        Calendar curCal = Calendar.getInstance();
//        int curHour = curCal.get(Calendar.HOUR_OF_DAY);
//        int curMin = curCal.get(Calendar.MINUTE);
//        // TODO Start StressReportActivity when not submitted during the 1 hour
//        if (notSubmit && ((curHour == 11) || (curHour == 15) || (curHour == 19) || (curHour == 23))) {
////            Intent intent = new Intent(getContext(), StressReportActivity.class);
////            intent.putExtra("report_order", order);
////            startActivity(intent);
////            notSubmit = false;
//        }


        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
    }

    public void applyUi(View view){
//        SharedPreferences stressReportPrefs = requireActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
        Context context = MainActivity.getInstance();

        allContainer = view.findViewById(R.id.frg_me_step2_container);
        beforeTextView = view.findViewById(R.id.frg_me_step2_before_time);
        timeContainer = view.findViewById(R.id.frg_me_step2_before_11hours_container);
        stressLvView = view.findViewById(R.id.txt_stress_level);


        if (feature_ids != null)
            featureViewUpdate(feature_ids, view);
        else
            Log.e(TAG, "feature_ids string is null");

        reasonContainer = view.findViewById(R.id.stress_reason_container);
        stressImg = view.findViewById(R.id.frg_me_step2_img1);

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

        btnMap = view.findViewById(R.id.fragment_me_step2_btn_map);
        btnMap.setOnClickListener(view13 -> {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
        });

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(reportTimestamp);

        dateView = view.findViewById(R.id.frg_me_step2_date1);
        Date currentTime = new Date();
        currentTime.setTime(reportTimestamp);
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);


        lastReportHours = cal.get(Calendar.HOUR_OF_DAY);
        Log.i(TAG, "current hours: " + lastReportHours);
        timeView = (TextView) view.findViewById(R.id.frg_me_step2_time1);
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


        // TODO 추후 step 시간으로 확인할때는 삭제할 부분
        stepTestBtn = (Button) view.findViewById(R.id.step_test_btn_step2);
        stepTestBtn.setOnClickListener(view1 -> {
            SharedPreferences stepChange = context.getSharedPreferences("stepChange", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = stepChange.edit();

            if (stepChange.getInt("stepCheck", 0) == 1) {
                Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
                stepTestBtn.setText("STEP 2");
                editor.putInt("stepCheck", 2);
                editor.apply();
            } else {
                Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
                stepTestBtn.setText("STEP 1");
                editor.putInt("stepCheck", 1);
                editor.apply();
            }
        });

        reportBtn = (Button) view.findViewById(R.id.report_test_btn);
        reportBtn.setOnClickListener(view12 -> {
            Intent intent = new Intent(getActivity(), StressReportActivity.class);
            startActivity(intent);
        });
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
        }else{
            if(fromCalendar.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[0] - REPORT_DURATION){
                fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - REPORT_DURATION);
                fromCalendar.set(Calendar.MINUTE, 0);
                fromCalendar.set(Calendar.SECOND, 0);
                tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
                tillCalendar.set(Calendar.MINUTE, 59);
                tillCalendar.set(Calendar.SECOND, 59);
                long fromTimestampYesterday = fromCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
                long tillTImestampYesterday = tillCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
                fromCalendar.setTimeInMillis(fromTimestampYesterday);
                tillCalendar.setTimeInMillis(tillTImestampYesterday);
            }else{
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
        } else /*if(REPORT_NOTIF_HOURS[3] == curHour)*/{
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

}