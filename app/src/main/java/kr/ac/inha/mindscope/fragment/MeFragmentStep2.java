package kr.ac.inha.mindscope.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.MapsActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.StressReportActivity;
import kr.ac.inha.mindscope.Tools;

import static kr.ac.inha.mindscope.StressReportActivity.REPORT_NOTIF_HOURS;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV1;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV2;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV3;
import static kr.ac.inha.mindscope.Tools.CATEGORY_ACTIVITY_END_INDEX;
import static kr.ac.inha.mindscope.Tools.CATEGORY_ENTERTAIN_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_FOOD_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_LOCATION_END_INDEX;
import static kr.ac.inha.mindscope.Tools.CATEGORY_SNS_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_SOCIAL_END_INDEX_EXCEPT_SNS_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_UNLOCK_DURATION_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.SELF_REPORT_ANSWER_INDEX;
import static kr.ac.inha.mindscope.Tools.SELF_REPORT_DAYNUM_INDEX;
import static kr.ac.inha.mindscope.Tools.SELF_REPORT_ORDER_INDEX;
import static kr.ac.inha.mindscope.Tools.SELF_REPORT_TIMESTAMP_INDEX;
import static kr.ac.inha.mindscope.Tools.timeTheDayNumIsChanged;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.setListViewHeightBasedOnChildren;
import static kr.ac.inha.mindscope.services.StressReportDownloader.SELF_STRESS_REPORT_RESULT;
import static kr.ac.inha.mindscope.services.StressReportDownloader.STRESS_PREDICTION_RESULT;

public class MeFragmentStep2 extends Fragment {

    public static final long TIMESTAMP_ONE_DAY = 60 * 60 * 24 * 1000;

    private static final String TAG = "MeFragmentStep2";
    private static final int DAYS_UNITL_STEP_STARTS = 4; // TODO change 15 for study
    static int lastReportHours;
    public View view;
    public int stressLevel;
    ScrollView reasonContainer;
    ImageView stressImg;
    ConstraintLayout allContainer;
    ConstraintLayout firstStartBefore11hoursContainer;
    TextView before11hoursTextView;
    long reportTimestamp;
    String stressResult;
    SharedPreferences lastPagePrefs;
    private ImageButton btnMap;
    private TextView dateView;
    private TextView timeView;
    private TextView stressLvView;
    private TextView waitNextReportTextView;
    private TextView versionNameTextView;


    public MeFragmentStep2() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "test onCreate");

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_me_step2, container, false);
        Context context = requireContext();
        init(view);
        try {
            stressResult = getStressResult(context);
            if (stressResult != null)
                updateUi(view, stressResult);
            else {
                Calendar calendar = Calendar.getInstance();
                firstStartBefore11hoursContainer.setVisibility(View.VISIBLE);
                allContainer.setVisibility(View.INVISIBLE);
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 11) {
                    before11hoursTextView.setText(getResources().getString(R.string.when_no_report_and_after_11_hour));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            firstStartBefore11hoursContainer.setVisibility(View.VISIBLE);
            allContainer.setVisibility(View.INVISIBLE);
        }


        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
        SharedPreferences stressReportPrefs = requireActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);

        NavController navController = Navigation.findNavController(view);
        Log.d(TAG, "navController : " + Objects.requireNonNull(navController.getCurrentDestination()).getId());
        SharedPreferences firstPref = requireActivity().getSharedPreferences("firstStart", Context.MODE_PRIVATE);
        boolean isFirstStartStep2DialogShowing = firstPref.getBoolean("firstStartStep2", false);
        if (isFirstStartStep2DialogShowing)
            startStressReportActivityWhenNotSubmitted();

        if (stressReportPrefs.getBoolean("today_last_report", false) || stressReportPrefs.getBoolean("click_go_to_care", false)) {
            navController.navigate(R.id.action_me_to_care_step2);
            // todo : ref 스트레스 해소하기 이동
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        lastPagePrefs = requireActivity().getSharedPreferences("LastPage", Context.MODE_PRIVATE);
        SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
        lastPagePrefsEditor.putString("last_open_nav_frg", "me");
        lastPagePrefsEditor.apply();
    }


    public void init(View view) {
        Context context = MainActivity.getInstance();
        allContainer = view.findViewById(R.id.frg_me_step2_container);
        stressLvView = view.findViewById(R.id.txt_stress_level);
        waitNextReportTextView = view.findViewById(R.id.txt_wait_next_report);
//        waitNextReportTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Tools.sendStressInterventionNoti(context);
//            }
//        });
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
//        versionNameTextView = view.findViewById(R.id.version_name_step2);
//        versionNameTextView.setText(getVersionInfo(requireContext()));
    }

    @SuppressLint("SetTextI18n")
    public void updateUi(View view, String stressResult) {
        Context context = MainActivity.getInstance();

        String[] splitResult = stressResult.split(",");
        long reportTimestamp = Long.parseLong(splitResult[0]);
        int stressLevel = Integer.parseInt(splitResult[1]);
        int day_num = Integer.parseInt(splitResult[2]);
        int ema_order = Integer.parseInt(splitResult[3]);
        float accuracy = Float.parseFloat(splitResult[4]);
        String feature_ids = splitResult[5];
        boolean model_tag = Boolean.parseBoolean(splitResult[6]);

        SharedPreferences stepChangePrefs = context.getSharedPreferences("stepChange", Context.MODE_PRIVATE);

        Calendar cal = Calendar.getInstance();
        boolean firstStartStep2Check = stepChangePrefs.getBoolean("first_start_step2_check", false);

        if (!firstStartStep2Check && cal.get(Calendar.HOUR_OF_DAY) < 11) {
            Calendar step2Cal = Calendar.getInstance();
            step2Cal.setTimeInMillis(stepChangePrefs.getLong("join_timestamp", 0));
            step2Cal.add(Calendar.DATE, DAYS_UNITL_STEP_STARTS);
            String stepDateStr = new SimpleDateFormat("yyyy년 MM월 dd일 (EE) ", Locale.getDefault()).format(step2Cal.getTimeInMillis());
            before11hoursTextView.setText(stepDateStr + context.getResources().getString(R.string.string_frg_me_stpe2_before_txt1));
            firstStartBefore11hoursContainer.setVisibility(View.VISIBLE);
            allContainer.setVisibility(View.INVISIBLE);
        } else {
            if (!firstStartStep2Check) {
                SharedPreferences.Editor editor = stepChangePrefs.edit();
                editor.putBoolean("first_start_step2_check", true);
                editor.apply();
            }

            if (feature_ids != null)
                featureViewUpdate(feature_ids, view);
            else
                Log.d(TAG, "feature_ids string is null");

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
        }

    }

    public void featureViewUpdate(String feature_ids, View view) {
        Context context = MainActivity.getInstance();
        ArrayList<String> phoneReason = new ArrayList<>();
        ArrayList<String> activityReason = new ArrayList<>();
        ArrayList<String> socialReason = new ArrayList<>();
        ArrayList<String> locationReason = new ArrayList<>();
        ArrayList<String> sleepReason = new ArrayList<>();


//        feature_ids = "18-high&com.joara.mobile 14-low 13-low 6-low 10-low"; // "18-high&com.joara.mobile 24-low 17-high 28-high 8-low";
//        feature_ids = "18-low 14-low 5-low";
        Log.e(TAG, "feature_ids: " + feature_ids);


        boolean noFeatures = false;

        if (feature_ids.equals("") || feature_ids.equals("NO_FEATURES")) {
            Log.d(TAG, "feature_ids is empty");
            noFeatures = true;
        } else {
            String[] featureArray = feature_ids.split(" ");

            for (int i = 0; i < featureArray.length; i++) {
                String[] splitArray = featureArray[i].split("-");
                int category = Integer.parseInt(splitArray[0]);
                if((category == 12 || category == 18 || category == 29) && (splitArray[1].equals("general_1") || splitArray[1].equals("general_0"))){
                    category--;
                }
                String applicationName = "";


                if (splitArray[1].contains("&") && (category == CATEGORY_SNS_APP_USAGE
                        || (category >= CATEGORY_ENTERTAIN_APP_USAGE && category <= CATEGORY_FOOD_APP_USAGE))) {
                    String[] packageSplit = splitArray[1].split("&");
                    splitArray[1] = packageSplit[0];
                    List<String> listNoApps = Arrays.asList(Tools.FEATURE_IDS_WITH_NO_APPS);
                    if (!listNoApps.contains(packageSplit[1])) {
                        String packageName = packageSplit[1];
                        final PackageManager pm = requireActivity().getApplicationContext().getPackageManager();
                        ApplicationInfo ai;
                        try {
                            ai = pm.getApplicationInfo(packageName, 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            ai = null;
                            e.printStackTrace();
                        }
                        applicationName = (String) (ai != null ? String.format("(%s)", pm.getApplicationLabel(ai)) : "");
                    } else {
                        applicationName = String.format("(%s)", packageSplit[1].replace("_", " "));
                    }
                }

                String strID = "@string/feature_" + splitArray[0] + splitArray[splitArray.length - 1];
                String packName = MainActivity.getInstance().getPackageName();
                int resId = context.getResources().getIdentifier(strID, "string", packName);

                if (category <= CATEGORY_ACTIVITY_END_INDEX) {
                    activityReason.add(context.getResources().getString(resId));
                } else if (category <= CATEGORY_SOCIAL_END_INDEX_EXCEPT_SNS_USAGE) {
                    socialReason.add(context.getResources().getString(resId));
                } else if (category == CATEGORY_SNS_APP_USAGE) {
                    String text = String.format(context.getResources().getString(resId), applicationName);
                    socialReason.add(text);
                } else if (category <= CATEGORY_LOCATION_END_INDEX) {
                    locationReason.add(context.getResources().getString(resId));
                } else if (category <= CATEGORY_UNLOCK_DURATION_APP_USAGE) {
                    phoneReason.add(context.getResources().getString(resId));
                } else if (category <= CATEGORY_FOOD_APP_USAGE) {
                    String text = String.format(context.getResources().getString(resId), applicationName);
                    phoneReason.add(text);
                } else {
                    if (Integer.parseInt(splitArray[0]) == 28)
                        sleepReason.add(context.getResources().getString(resId));
                }


                if (i == 4) // maximun number of showing feature is five
                    break;
            }
        }

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

        TextView noFeatureTextview = view.findViewById(R.id.frg_me_step2_no_features);

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

        if (noFeatures) {
            phoneContainer.setVisibility(View.GONE);
            activityContainer.setVisibility(View.GONE);
            socialContainer.setVisibility(View.GONE);
            locationContainer.setVisibility(View.GONE);
            sleepContainer.setVisibility(View.GONE);
            noFeatureTextview.setVisibility(View.VISIBLE);
        } else {
            phoneListView.setAdapter(phoneAdapter);
            activityListView.setAdapter(activityAdapter);
            socialListView.setAdapter(socialAdapter);
            locationListView.setAdapter(locationAdapter);
            sleepListView.setAdapter(sleepAdapter);
            noFeatureTextview.setVisibility(View.GONE);

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

    }

    public String getStressResult(Context context) throws IOException {
        String stressResult = null;
        FileInputStream fis = context.openFileInput(STRESS_PREDICTION_RESULT);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line;
        ArrayList<String> predictionArray = new ArrayList<>();

        //region set calendar
        Calendar fromCalendar = Calendar.getInstance();
        int reportOrder = Tools.getReportPreviousOrder(fromCalendar);
        Calendar tillCalendar = Calendar.getInstance();

        // initialize calendar time
        // 10:00:00~11:59:59, 14:00:00~15:59:59, 18:00:00~19:59:59, 22:00:00~06:59:59
        if (reportOrder < 4) {
            fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
            tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1]);
        } else {
            if (fromCalendar.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[0]) {
                fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
                fromCalendar.add(Calendar.DATE, -1);
                tillCalendar.set(Calendar.HOUR_OF_DAY, timeTheDayNumIsChanged - 1);
            } else {
                fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
                tillCalendar.set(Calendar.HOUR_OF_DAY, timeTheDayNumIsChanged - 1);
                tillCalendar.add(Calendar.DATE, 1);
            }
        }
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        tillCalendar.set(Calendar.MINUTE, 59);
        tillCalendar.set(Calendar.SECOND, 59);
        //end region

        //region stress prediction
        while ((line = bufferedReader.readLine()) != null) {
            String[] tokens = line.split(",");
            long timestamp = Long.parseLong(tokens[0]);

            if (fromCalendar.getTimeInMillis() <= timestamp && timestamp <= tillCalendar.getTimeInMillis()) {
                predictionArray.add(line);
            }
        }
        //endregion

        //region self stress
        int selfStressLv = 5;
        String lastSelfReport = null;
        try {
            FileInputStream fis2 = context.openFileInput(SELF_STRESS_REPORT_RESULT);
            InputStreamReader isr2 = new InputStreamReader(fis2);
            BufferedReader bufferedReader2 = new BufferedReader(isr2);
            String line2;
            while ((line2 = bufferedReader2.readLine()) != null) {
                String[] tokens = line2.split(",");
                long timestamp = Long.parseLong(tokens[0]);

                tillCalendar.add(Calendar.HOUR_OF_DAY, 1);

                if (fromCalendar.getTimeInMillis() <= timestamp && timestamp <= tillCalendar.getTimeInMillis()) {
                    selfStressLv = Integer.parseInt(tokens[4]);
                    lastSelfReport = line2;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion

        if (selfStressLv != 5) {
            for (String result : predictionArray) {
                String[] splitResult = result.split(",");
                if (Integer.parseInt(splitResult[1]) == selfStressLv) {
                    stressResult = result;
                    break;
                }
            }
        } else {
            for (String result : predictionArray) {
                String[] splitResult = result.split(",");
                if (Boolean.parseBoolean(splitResult[6])) {
                    stressResult = result;
                    break;
                }
            }
        }

        if(lastSelfReport != null && stressResult == null){
            // when no prediction, making stressResult using only self stress
            String[] splitSelfReport = lastSelfReport.split(",");
            stressResult = String.format(Locale.getDefault(),"%d,%d,%d,%d,%.2f,%s,%b",
                    Long.parseLong(splitSelfReport[SELF_REPORT_TIMESTAMP_INDEX]),
                    Integer.parseInt(splitSelfReport[SELF_REPORT_ANSWER_INDEX]),
                    Integer.parseInt(splitSelfReport[SELF_REPORT_DAYNUM_INDEX]),
                    Integer.parseInt(splitSelfReport[SELF_REPORT_ORDER_INDEX]),
                    0f,
                    "",
                    true);
        }

        return stressResult;
    }

    public void startStressReportActivityWhenNotSubmitted() {
        updateStressReportSubmit();
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
        if(cal.get(Calendar.HOUR_OF_DAY) < timeTheDayNumIsChanged){
            cal.add(Calendar.DATE, -1);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 0);
        }
        int todayDate = cal.get(Calendar.DATE);
        if (todayDate != selfReportSubmitCheckPrefs.getInt("reportSubmitDate", -1) && curHour >= timeTheDayNumIsChanged) {
            for (short i = 0; i < 4; i++) {
                reportSubmitEditor.putBoolean("self_report_submit_check_" + (i + 1), false);
                reportSubmitEditor.apply();
            }
        }
        int report_order = Tools.getReportOrderFromRangeAfterReport(cal);
        for (short i = 0; i < 4; i++) {
            if ((curHour == REPORT_NOTIF_HOURS[i] || curHour == REPORT_NOTIF_HOURS[i]+1 || curHour < timeTheDayNumIsChanged) && report_order > 0 && !submits[report_order-1]) {
                Intent intent = new Intent(getActivity(), StressReportActivity.class);
                startActivity(intent);
            }
        }
    }

    private void updateStressReportSubmit() {
        SharedPreferences selfReportSubmitCheckPrefs = requireActivity().getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
        SharedPreferences.Editor reportSubmitEditor = selfReportSubmitCheckPrefs.edit();
        Context context = requireContext();
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(SELF_STRESS_REPORT_RESULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line;

        Calendar startToday = Calendar.getInstance();
        Calendar endToday = Calendar.getInstance();
        endToday.add(Calendar.DATE, 1);

        startToday.set(Calendar.HOUR_OF_DAY, 1);
        startToday.set(Calendar.MINUTE, 0);
        startToday.set(Calendar.SECOND, 0);
        endToday.set(Calendar.HOUR_OF_DAY, 0);
        endToday.set(Calendar.MINUTE, 59);
        endToday.set(Calendar.SECOND, 59);

        long startTimestamp = startToday.getTimeInMillis();
        long endTimestamp = endToday.getTimeInMillis();

        try {
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = line.split(",");
                long timestamp = Long.parseLong(tokens[0]);
                if (startTimestamp <= timestamp && timestamp <= endTimestamp) {
                    reportSubmitEditor.putBoolean("self_report_submit_check_" + tokens[2], true);
                    reportSubmitEditor.apply();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public String getVersionInfo(Context context) {
//        String version = "Unknown";
//        PackageInfo packageInfo;
//
//        if (context == null) {
//            return version;
//        }
//        try {
//            packageInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0);
//            version = packageInfo.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return version;
//    }
}