package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;

import static kr.ac.inha.mindscope.Tools.CATEGORY_ACTIVITY_END_INDEX;
import static kr.ac.inha.mindscope.Tools.CATEGORY_ENTERTAIN_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_FOOD_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_LOCATION_END_INDEX;
import static kr.ac.inha.mindscope.Tools.CATEGORY_SNS_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_SOCIAL_END_INDEX_EXCEPT_SNS_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_UNLOCK_DURATION_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.PREDICTION_FEATUREIDS_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_MODELTAG_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_ORDER_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_STRESSLV_INDEX;
import static kr.ac.inha.mindscope.Tools.SELF_REPORT_DAYNUM_INDEX;
import static kr.ac.inha.mindscope.Tools.SELF_REPORT_ORDER_INDEX;
import static kr.ac.inha.mindscope.Tools.timeTheDayNumIsChanged;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.CONDITION1;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.CONDITION2;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.CONDITION3;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.setListViewHeightBasedOnChildren;
import static kr.ac.inha.mindscope.services.StressReportDownloader.SELF_STRESS_REPORT_RESULT;
import static kr.ac.inha.mindscope.services.StressReportDownloader.STRESS_PREDICTION_RESULT;

public class CareChildFragment1 extends Fragment {

    private static final String TAG = "CareChildFragment1";
    private static final String ACTION_CLICK_DETAIL_REPORT = "CLICK_DETAIL_REPORT";
    private static final int STRESS_LV1 = 0;
    private static final int STRESS_LV2 = 1;
    private static final int STRESS_LV3 = 2;
    private static final int NON_SELF_STRESS_LV = 3;
    private static final int ORDER1 = 1;
    private static final int ORDER2 = 2;
    private static final int ORDER3 = 3;
    private static final int ORDER4 = 4;
    static String feature_ids1 = "";
    static String feature_ids2 = "";
    static String feature_ids3 = "";
    static String feature_ids4 = "";
    ArrayList<String> predictionArray;
    ArrayList<String> selfReportArray;
    int[] stressLvArray;
    long timestamp;
    //region variable
    ImageView stressAvgImg;
    TextView dateTextView;
    TextView stressAvgTextview;
    CheckBox checkBox1;
    CheckBox checkBox2;
    CheckBox checkBox3;
    CheckBox checkBox4;
    ImageView stressImg1;
    ImageView stressImg2;
    ImageView stressImg3;
    ImageView stressImg4;
    TextView stressTextview1;
    TextView stressTextview2;
    TextView stressTextview3;
    TextView stressTextview4;
    ImageButton arrowBtn1;
    ImageButton arrowBtn2;
    ImageButton arrowBtn3;
    ImageButton arrowBtn4;
    ImageButton backArrow;
    ConstraintLayout defaultContainer;
    ConstraintLayout hiddenContainer;
    RelativeLayout beforeStartStep2Container;
    TextView beforeStartStep2TextView;
    ImageView hiddenStressImg;
    TextView hiddenDateView;
    TextView hiddenTimeView;
    TextView hiddenStressLevelView;
    ListView integrateListView;
//    ListView phoneListView;
//    ListView activityListView;
//    ListView socialListView;
//    ListView locationListView;
//    ListView sleepListView;
    LinearLayout integrateContainer;
//    LinearLayout phoneContainer;
//    LinearLayout activityContainer;
//    LinearLayout socialContainer;
//    LinearLayout locationContainer;
//    LinearLayout sleepContainer;
    ScrollView reasonContainer;
    TextView noFeatureTextview;
    int order1StressLevel;
    int order2StressLevel;
    int order3StressLevel;
    int order4StressLevel;

    RelativeLayout categoryImgContainer1;
    RelativeLayout categoryImgContainer2;
    RelativeLayout categoryImgContainer3;
    RelativeLayout categoryImgContainer4;
    RelativeLayout categoryImgContainer5;
    ImageView condition2Img1;
    ImageView condition2Img2;
    ImageView condition2Img3;
    ImageView condition2Img4;
    ImageView condition2Img5;
    TextView stressLevelView;
    TextView condition2txt1;
    TextView condition2txt2;
    TextView condition2txt3;
    TextView condition2txt4;
    TextView condition2txt5;

    ConstraintLayout condition2Container;

    int condition;

    int[] selfStressReportWithOrderIndex;
    //endregion

    public CareChildFragment1() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences stepChangePrefs = requireContext().getSharedPreferences("stepChagne", Context.MODE_PRIVATE);
        condition = stepChangePrefs.getInt("condition", 0);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_care_child1, container, false);
        Context context = getContext();
        init(view);

        try {
            assert context != null;
            getTodayPredictionAndSelfReport(context);
        } catch (IOException e) {
            e.printStackTrace();
        }

        selfStressReportWithOrderIndex = new int[]{NON_SELF_STRESS_LV, NON_SELF_STRESS_LV, NON_SELF_STRESS_LV, NON_SELF_STRESS_LV};
        if (selfReportArray != null && !selfReportArray.isEmpty()) {
            for (String selfReportStr : selfReportArray) {
                String[] result = selfReportStr.split(",");
                // result[0] : timestamp, result[1] : day num, result[2] : report order(ema_order), result[3] : Analysis correct, result[4] self stress report answer
                if (Integer.parseInt(result[SELF_REPORT_ORDER_INDEX]) > 0 && Integer.parseInt(result[SELF_REPORT_DAYNUM_INDEX]) != 0)
                    selfStressReportWithOrderIndex[Integer.parseInt(result[2]) - 1] = Integer.parseInt(result[4]);
            }
        }

        float avgStress = 0;
        try {
            avgStress = getAvgStress(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "avg stress: " + avgStress);

        if (avgStress < 1.5) {
            stressAvgImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
            stressAvgTextview.setText(Html.fromHtml(getString(R.string.string_stress_level_low)));
        } else if (avgStress < 2.5) {
            stressAvgImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
            stressAvgTextview.setText(Html.fromHtml(getString(R.string.string_stress_level_littlehigh)));
        } else {
            stressAvgImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
            stressAvgTextview.setText(Html.fromHtml(getString(R.string.string_stress_level_high)));
        }

        updateUI();

        backArrow.setOnClickListener(view1 -> {
            defaultContainer.setVisibility(View.VISIBLE);
            hiddenContainer.setVisibility(View.INVISIBLE);
        });

        // todo condition 1일때는 arrowBtn들 안보이게 또는 비활성화
        arrowBtn1.setOnClickListener(view12 -> {
            defaultContainer.setVisibility(View.INVISIBLE);
            hiddenContainer.setVisibility(View.VISIBLE);
            hiddenViewUpdate(feature_ids1, order1StressLevel);
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration1));
            selectHiddenViewContentsByCondition();
            Tools.saveApplicationLog(getContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 1);
        });
        arrowBtn2.setOnClickListener(view13 -> {
            defaultContainer.setVisibility(View.INVISIBLE);
            hiddenContainer.setVisibility(View.VISIBLE);
            hiddenViewUpdate(feature_ids2, order2StressLevel);
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration2));
            selectHiddenViewContentsByCondition();
            Tools.saveApplicationLog(getContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 2);
        });
        arrowBtn3.setOnClickListener(view14 -> {
            defaultContainer.setVisibility(View.INVISIBLE);
            hiddenContainer.setVisibility(View.VISIBLE);
            hiddenViewUpdate(feature_ids3, order3StressLevel);
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration3));
            selectHiddenViewContentsByCondition();
            Tools.saveApplicationLog(getContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 3);
        });
        arrowBtn4.setOnClickListener(view15 -> {
            defaultContainer.setVisibility(View.INVISIBLE);
            hiddenContainer.setVisibility(View.VISIBLE);
            hiddenViewUpdate(feature_ids4, order4StressLevel);
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration4));
            selectHiddenViewContentsByCondition();
            Tools.saveApplicationLog(getContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 4);
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
        SharedPreferences lastPagePrefs = requireActivity().getSharedPreferences("LastPage", Context.MODE_PRIVATE);
        SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
        lastPagePrefsEditor.putInt("last_open_tab_position", 0);
        lastPagePrefsEditor.apply();
    }

    public void init(View view) {
        Context context = MainActivity.getInstance();
        SharedPreferences stepChangePrefs = context.getSharedPreferences("stepChange", Context.MODE_PRIVATE);
        Calendar cal = Calendar.getInstance();
        boolean firstStartCareStep2Check = stepChangePrefs.getBoolean("first_start_care_step2_check", false);

        defaultContainer = view.findViewById(R.id.child1_container1);
        beforeStartStep2Container = view.findViewById(R.id.before_start_step2_container);
        beforeStartStep2TextView = view.findViewById(R.id.before_start_step2_text);

        if (!firstStartCareStep2Check && cal.get(Calendar.HOUR_OF_DAY) < 11) {
            defaultContainer.setVisibility(View.INVISIBLE);
            beforeStartStep2Container.setVisibility(View.VISIBLE);
        } else {
            if (!firstStartCareStep2Check) {
                SharedPreferences.Editor editor = stepChangePrefs.edit();
                editor.putBoolean("first_start_care_step2_check", true);
                editor.apply();
            }
        }
        stressAvgImg = view.findViewById(R.id.child1_img);
        dateTextView = view.findViewById(R.id.child1_date);
        stressAvgTextview = view.findViewById(R.id.child1_stress_level);

        checkBox1 = view.findViewById(R.id.child1_ch_report1);
        checkBox2 = view.findViewById(R.id.child1_ch_report2);
        checkBox3 = view.findViewById(R.id.child1_ch_report3);
        checkBox4 = view.findViewById(R.id.child1_ch_report4);

        stressImg1 = view.findViewById(R.id.child1_img_report_result1);
        stressImg2 = view.findViewById(R.id.child1_img_report_result2);
        stressImg3 = view.findViewById(R.id.child1_img_report_result3);
        stressImg4 = view.findViewById(R.id.child1_img_report_result4);


        stressTextview1 = view.findViewById(R.id.child1_txt_report_result1);
        stressTextview2 = view.findViewById(R.id.child1_txt_report_result2);
        stressTextview3 = view.findViewById(R.id.child1_txt_report_result3);
        stressTextview4 = view.findViewById(R.id.child1_txt_report_result4);

        arrowBtn1 = view.findViewById(R.id.child1_arrow_result1);
        arrowBtn2 = view.findViewById(R.id.child1_arrow_result2);
        arrowBtn3 = view.findViewById(R.id.child1_arrow_result3);
        arrowBtn4 = view.findViewById(R.id.child1_arrow_result4);

        // hidden views
        hiddenContainer = view.findViewById(R.id.child1_container2);
        hiddenStressImg = view.findViewById(R.id.child1_step2_img1);
        hiddenDateView = view.findViewById(R.id.child1_step2_date1);
        hiddenTimeView = view.findViewById(R.id.child1_step2_time1);
        hiddenStressLevelView = view.findViewById(R.id.child1_txt_stress_level);
        backArrow = view.findViewById(R.id.child1_back_arrow);

        integrateListView = view.findViewById(R.id.child1_listview_integrate);
        integrateContainer = view.findViewById(R.id.child1_listview_integrate_container);
//        phoneListView = view.findViewById(R.id.child1_listview_phone);
//        activityListView = view.findViewById(R.id.child1_listview_activity);
//        socialListView = view.findViewById(R.id.child1_listview_social);
//        locationListView = view.findViewById(R.id.child1_listview_location);
//        sleepListView = view.findViewById(R.id.child1_listview_sleep);
//        phoneContainer = view.findViewById(R.id.child1_listview_phone_container);
//        activityContainer = view.findViewById(R.id.child1_listview_activity_container);
//        socialContainer = view.findViewById(R.id.child1_listview_social_container);
//        locationContainer = view.findViewById(R.id.child1_listview_location_container);
//        sleepContainer = view.findViewById(R.id.child1_listview_sleep_container);

        condition2Img1 = view.findViewById(R.id.child1_stress_report_img1);
        condition2Img2 = view.findViewById(R.id.child1_stress_report_img2);
        condition2Img3 = view.findViewById(R.id.child1_stress_report_img3);
        condition2Img4 = view.findViewById(R.id.child1_stress_report_img4);
        condition2Img5 = view.findViewById(R.id.child1_stress_report_img5);
        condition2txt1 = view.findViewById(R.id.child1_stress_report_txt1);
        condition2txt2 = view.findViewById(R.id.child1_stress_report_txt2);
        condition2txt3 = view.findViewById(R.id.child1_stress_report_txt3);
        condition2txt4 = view.findViewById(R.id.child1_stress_report_txt4);
        condition2txt5 = view.findViewById(R.id.child1_stress_report_txt5);
        categoryImgContainer1 = view.findViewById(R.id.child1_stress_report_img_container1);
        categoryImgContainer2 = view.findViewById(R.id.child1_stress_report_img_container2);
        categoryImgContainer3 = view.findViewById(R.id.child1_stress_report_img_container3);
        categoryImgContainer4 = view.findViewById(R.id.child1_stress_report_img_container4);
        categoryImgContainer5 = view.findViewById(R.id.child1_stress_report_img_container5);

        reasonContainer = view.findViewById(R.id.child1_stress_reason_container);
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);

        noFeatureTextview = view.findViewById(R.id.child1_no_features);

    }

    public void getTodayPredictionAndSelfReport(Context context) throws IOException {
        FileInputStream fis = context.openFileInput(STRESS_PREDICTION_RESULT);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line;
        predictionArray = new ArrayList<>();

        // initialize timestamp from today 00:00:00 to 23:59:59
        Calendar fromCalendar = Calendar.getInstance();
        Calendar tillCalendar = Calendar.getInstance();
        tillCalendar.add(Calendar.DATE, 1);
        // Set the date to today(07:00:00~06:59:59), and set the date to yesterday if it is between 0 and 11
        int curHour = fromCalendar.get(Calendar.HOUR_OF_DAY);
        if (curHour < timeTheDayNumIsChanged) {
            // yesterday
            fromCalendar.add(Calendar.DATE, -1);
            tillCalendar.add(Calendar.DATE, -1);
        }

        fromCalendar.set(Calendar.HOUR_OF_DAY, 7);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);
        tillCalendar.add(Calendar.DATE, 1);
        tillCalendar.set(Calendar.HOUR_OF_DAY, 6);
        tillCalendar.set(Calendar.MINUTE, 59);
        tillCalendar.set(Calendar.SECOND, 59);
        tillCalendar.set(Calendar.MILLISECOND, 0);

        timestamp = fromCalendar.getTimeInMillis();

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
        selfReportArray = new ArrayList<>();
        int selfStressLv = 5;
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
                    selfReportArray.add(line2);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "load serf report error");
        }
        //endregion

    }

    public void updateUI() {

        if (predictionArray == null) {
            Calendar calendar = Calendar.getInstance();
//            String date_text2 = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(calendar.getTimeInMillis());
            defaultContainer.setVisibility(View.INVISIBLE);
            beforeStartStep2Container.setVisibility(View.VISIBLE);
            beforeStartStep2TextView.setText(getResources().getString(R.string.string_when_no_prediction));

        }

        Date currentTime = new Date(timestamp);
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateTextView.setText(date_text + "의");
        hiddenDateView.setText(date_text);

        // each stress level view update
        if (predictionArray != null) {
            for (short order = 0; order < 4; order++) {
                for (String prediction : predictionArray) {
                    String[] predictionTokens = prediction.split(",");
                    if (order + 1 == Integer.parseInt(predictionTokens[PREDICTION_ORDER_INDEX])
                            && stressLvArray[order] == Integer.parseInt(predictionTokens[PREDICTION_STRESSLV_INDEX])) {
                        int stressLevel = stressLvArray[order];
                        String featre_ids_result = predictionTokens[PREDICTION_FEATUREIDS_INDEX];
                        switch (stressLevel) {
                            case STRESS_LV1:
                                switch (order + 1) {
                                    case ORDER1:
                                        stressImg1.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                                        stressTextview1.setText(getResources().getString(R.string.string_low));
                                        stressImg1.setVisibility(View.VISIBLE);
                                        stressTextview1.setVisibility(View.VISIBLE);
                                        arrowBtn1.setVisibility(View.VISIBLE); // "condition" 1일때는 안보이게
                                        feature_ids1 = featre_ids_result;
                                        order1StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV1 ORDER1 feature_ids1: " + feature_ids1);
                                        break;
                                    case ORDER2:
                                        stressImg2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                                        stressTextview2.setText(getResources().getString(R.string.string_low));
                                        stressImg2.setVisibility(View.VISIBLE);
                                        stressTextview2.setVisibility(View.VISIBLE);
                                        arrowBtn2.setVisibility(View.VISIBLE);
                                        feature_ids2 = featre_ids_result;
                                        order2StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV1 ORDER2 feature_ids2: " + feature_ids2);
                                        break;
                                    case ORDER3:
                                        stressImg3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                                        stressTextview3.setText(getResources().getString(R.string.string_low));
                                        stressImg3.setVisibility(View.VISIBLE);
                                        stressTextview3.setVisibility(View.VISIBLE);
                                        arrowBtn3.setVisibility(View.VISIBLE);
                                        feature_ids3 = featre_ids_result;
                                        order3StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV1 ORDER3 feature_ids3: " + feature_ids3);
                                        break;
                                    case ORDER4:
                                        stressImg4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                                        stressTextview4.setText(getResources().getString(R.string.string_low));
                                        stressImg4.setVisibility(View.VISIBLE);
                                        stressTextview4.setVisibility(View.VISIBLE);
                                        arrowBtn4.setVisibility(View.VISIBLE);
                                        feature_ids4 = featre_ids_result;
                                        order4StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV1 ORDER4 feature_ids4: " + feature_ids4);
                                        break;
                                }
                                break;
                            case STRESS_LV2:
                                switch (order + 1) {
                                    case ORDER1:
                                        stressImg1.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                                        stressTextview1.setText(getResources().getString(R.string.string_littlehigh));
                                        stressImg1.setVisibility(View.VISIBLE);
                                        stressTextview1.setVisibility(View.VISIBLE);
                                        arrowBtn1.setVisibility(View.VISIBLE);
                                        feature_ids1 = featre_ids_result;
                                        order1StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV2 ORDER1 feature_ids1: " + feature_ids1);
                                        break;
                                    case ORDER2:
                                        stressImg2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                                        stressTextview2.setText(getResources().getString(R.string.string_littlehigh));
                                        stressImg2.setVisibility(View.VISIBLE);
                                        stressTextview2.setVisibility(View.VISIBLE);
                                        arrowBtn2.setVisibility(View.VISIBLE);
                                        feature_ids2 = featre_ids_result;
                                        order2StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV2 ORDER2 feature_ids2: " + feature_ids2);
                                        break;
                                    case ORDER3:
                                        stressImg3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                                        stressTextview3.setText(getResources().getString(R.string.string_littlehigh));
                                        stressImg3.setVisibility(View.VISIBLE);
                                        stressTextview3.setVisibility(View.VISIBLE);
                                        arrowBtn3.setVisibility(View.VISIBLE);
                                        feature_ids3 = featre_ids_result;
                                        order3StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV2 ORDER3 feature_ids3: " + feature_ids3);
                                        break;
                                    case ORDER4:
                                        stressImg4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                                        stressTextview4.setText(getResources().getString(R.string.string_littlehigh));
                                        stressImg4.setVisibility(View.VISIBLE);
                                        stressTextview4.setVisibility(View.VISIBLE);
                                        arrowBtn4.setVisibility(View.VISIBLE);
                                        feature_ids4 = featre_ids_result;
                                        order4StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV2 ORDER4 feature_ids4: " + feature_ids4);
                                        break;
                                }
                                break;
                            case STRESS_LV3:
                                switch (order + 1) {
                                    case ORDER1:
                                        stressImg1.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                                        stressTextview1.setText(getResources().getString(R.string.string_high));
                                        stressImg1.setVisibility(View.VISIBLE);
                                        stressTextview1.setVisibility(View.VISIBLE);
                                        arrowBtn1.setVisibility(View.VISIBLE);
                                        feature_ids1 = featre_ids_result;
                                        order1StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV3 ORDER1 feature_ids1: " + feature_ids1);
                                        break;
                                    case ORDER2:
                                        stressImg2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                                        stressTextview2.setText(getResources().getString(R.string.string_high));
                                        stressImg2.setVisibility(View.VISIBLE);
                                        stressTextview2.setVisibility(View.VISIBLE);
                                        arrowBtn2.setVisibility(View.VISIBLE);
                                        feature_ids2 = featre_ids_result;
                                        order2StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV3 ORDER2 feature_ids2: " + feature_ids2);
                                        break;
                                    case ORDER3:
                                        stressImg3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                                        stressTextview3.setText(getResources().getString(R.string.string_high));
                                        stressImg3.setVisibility(View.VISIBLE);
                                        stressTextview3.setVisibility(View.VISIBLE);
                                        arrowBtn3.setVisibility(View.VISIBLE);
                                        feature_ids3 = featre_ids_result;
                                        order3StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV3 ORDER3 feature_ids3: " + feature_ids3);
                                        break;
                                    case ORDER4:
                                        stressImg4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                                        stressTextview4.setText(getResources().getString(R.string.string_high));
                                        stressImg4.setVisibility(View.VISIBLE);
                                        stressTextview4.setVisibility(View.VISIBLE);
                                        arrowBtn4.setVisibility(View.VISIBLE);
                                        feature_ids4 = featre_ids_result;
                                        order4StressLevel = stressLevel;
                                        Log.d(TAG, "STRESS_LV3 ORDER4 feature_ids4: " + feature_ids4);
                                        break;
                                }
                                break;
                        }
                        // checkbox UI update

                        if (selfStressReportWithOrderIndex[order] != NON_SELF_STRESS_LV) {
                            switch (order + 1) {
                                case ORDER1:
                                    checkBox1.setChecked(true);
                                    break;
                                case ORDER2:
                                    checkBox2.setChecked(true);
                                    break;
                                case ORDER3:
                                    checkBox3.setChecked(true);
                                    break;
                                case ORDER4:
                                    checkBox4.setChecked(true);
                                    break;
                            }
                        } else {
                            switch (order + 1) {
                                case ORDER1:
                                    checkBox1.setChecked(false);
                                    break;
                                case ORDER2:
                                    checkBox2.setChecked(false);
                                    break;
                                case ORDER3:
                                    checkBox3.setChecked(false);
                                    break;
                                case ORDER4:
                                    checkBox4.setChecked(false);
                                    break;
                            }
                        }
                    }
                }
            }
        }
        if(condition <= 1){
            arrowBtn1.setVisibility(View.INVISIBLE);
            arrowBtn2.setVisibility(View.INVISIBLE);
            arrowBtn3.setVisibility(View.INVISIBLE);
            arrowBtn4.setVisibility(View.INVISIBLE);
            arrowBtn1.setEnabled(false);
            arrowBtn2.setEnabled(false);
            arrowBtn3.setEnabled(false);
            arrowBtn4.setEnabled(false);
        }
    }

    public float getAvgStress(Context context) throws IOException {
        int sumStress = 0;

        FileInputStream fis = context.openFileInput(STRESS_PREDICTION_RESULT);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String predictionLine;

        stressLvArray = new int[]{NON_SELF_STRESS_LV, NON_SELF_STRESS_LV, NON_SELF_STRESS_LV, NON_SELF_STRESS_LV};

        for (String prediction : predictionArray) {
            String[] predictionTokens = prediction.split(",");
            boolean predictionModelTag = Boolean.parseBoolean(predictionTokens[PREDICTION_MODELTAG_INDEX]);
            int predictionOrder = Integer.parseInt(predictionTokens[PREDICTION_ORDER_INDEX]);
            int predictionStressLv = Integer.parseInt(predictionTokens[PREDICTION_STRESSLV_INDEX]);
            if (predictionModelTag && predictionOrder > 0) {
                if (selfStressReportWithOrderIndex[predictionOrder - 1] != NON_SELF_STRESS_LV
                        && selfStressReportWithOrderIndex[predictionOrder - 1] != predictionStressLv) {
                    stressLvArray[predictionOrder - 1] = selfStressReportWithOrderIndex[predictionOrder - 1];
                } else {
                    stressLvArray[predictionOrder - 1] = predictionStressLv;
                }
            }
        }

        int count = 0;
        for (int i : stressLvArray) {
            if (i != NON_SELF_STRESS_LV) {
                sumStress += (i + 1);
                count++;
            }
        }

        return (float) sumStress / count;
    }

    public void hiddenViewUpdate(String feature_ids, int stressLevl) {
        Context context = requireContext();

        ArrayList<String> integrateReason = new ArrayList<>();

//        ArrayList<String> phoneReason = new ArrayList<>();
//        ArrayList<String> activityReason = new ArrayList<>();
//        ArrayList<String> socialReason = new ArrayList<>();
//        ArrayList<String> locationReason = new ArrayList<>();
//        ArrayList<String> sleepReason = new ArrayList<>();



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
                    if (packageSplit.length > 1) {
                        String packageName = packageSplit[1];
                        final PackageManager pm = requireActivity().getApplicationContext().getPackageManager();
                        ApplicationInfo ai;
                        try {
                            ai = pm.getApplicationInfo(packageName, 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            ai = null;
                            e.printStackTrace();
                        }
                        applicationName = (String) (ai != null ? "(" + pm.getApplicationLabel(ai) + ")" : "");
                    }
                }

                String strID = "@string/feature_" + splitArray[0] + splitArray[splitArray.length - 1];
                String packName = MainActivity.getInstance().getPackageName();
                int resId = context.getResources().getIdentifier(strID, "string", packName);


                if (category <= CATEGORY_ACTIVITY_END_INDEX) {
//                    activityReason.add(context.getResources().getString(resId));
                    condition2Img4.setAlpha(1.0f);
                    condition2txt4.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (stressLevl){
                        case STRESS_LV1:
                            categoryImgContainer4.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer4.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer4.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category <= CATEGORY_SOCIAL_END_INDEX_EXCEPT_SNS_USAGE) {
//                    socialReason.add(context.getResources().getString(resId));
                    condition2Img2.setAlpha(1.0f);
                    condition2txt2.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (stressLevl){
                        case STRESS_LV1:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category == CATEGORY_SNS_APP_USAGE) {
//                    String text = String.format(context.getResources().getString(resId), applicationName);
//                    socialReason.add(text);
                    condition2Img2.setAlpha(1.0f);
                    condition2txt2.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (stressLevl){
                        case STRESS_LV1:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category <= CATEGORY_LOCATION_END_INDEX) {
                    condition2Img3.setAlpha(1.0f);
                    condition2txt3.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (stressLevl){
                        case STRESS_LV1:
                            categoryImgContainer3.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer3.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer3.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category <= CATEGORY_UNLOCK_DURATION_APP_USAGE) {
                    condition2Img1.setAlpha(1.0f);
                    condition2txt1.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (stressLevl){
                        case STRESS_LV1:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category <= CATEGORY_FOOD_APP_USAGE) {
//                    String text = String.format(context.getResources().getString(resId), applicationName);
//                    phoneReason.add(text);
                    condition2Img1.setAlpha(1.0f);
                    condition2txt1.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (stressLevl){
                        case STRESS_LV1:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else {
//                    sleepReason.add(context.getResources().getString(resId));
                    condition2Img5.setAlpha(1.0f);
                    condition2txt5.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (stressLevl){
                        case STRESS_LV1:
                            categoryImgContainer5.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer5.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer5.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                }

                if( category == CATEGORY_SNS_APP_USAGE || (category <= CATEGORY_FOOD_APP_USAGE && category > CATEGORY_UNLOCK_DURATION_APP_USAGE)){
                    String text = String.format(context.getResources().getString(resId), applicationName);
                    integrateReason.add(text);
                }else{
                    integrateReason.add(context.getResources().getString(resId));
                }


                if (i == 4) // maximun number of showing feature is five
                    break;
            }
        }

        ArrayAdapter<String> integrateAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, integrateReason
        );

//        ArrayAdapter<String> phoneAdapter = new ArrayAdapter<>(
//                requireActivity(), R.layout.item_feature_ids, phoneReason
//        );
//        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(
//                requireActivity(), R.layout.item_feature_ids, activityReason
//        );
//        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(
//                requireActivity(), R.layout.item_feature_ids, socialReason
//        );
//        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
//                requireActivity(), R.layout.item_feature_ids, locationReason
//        );
//        ArrayAdapter<String> sleepAdapter = new ArrayAdapter<>(
//                requireActivity(), R.layout.item_feature_ids, sleepReason
//        );

        if (noFeatures) {
            integrateContainer.setVisibility(View.GONE);
//            phoneContainer.setVisibility(View.GONE);
//            activityContainer.setVisibility(View.GONE);
//            socialContainer.setVisibility(View.GONE);
//            locationContainer.setVisibility(View.GONE);
//            sleepContainer.setVisibility(View.GONE);
            noFeatureTextview.setVisibility(View.VISIBLE);
        } else {
            integrateListView.setAdapter(integrateAdapter);
//            phoneListView.setAdapter(phoneAdapter);
//            activityListView.setAdapter(activityAdapter);
//            socialListView.setAdapter(socialAdapter);
//            locationListView.setAdapter(locationAdapter);
//            sleepListView.setAdapter(sleepAdapter);
            noFeatureTextview.setVisibility(View.GONE);

            if(integrateReason.isEmpty())
                integrateContainer.setVisibility(View.GONE);
            else{
                setListViewHeightBasedOnChildren(integrateListView);
                integrateContainer.setVisibility(View.VISIBLE);
            }

//            if (phoneReason.isEmpty())
//                phoneContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(phoneListView);
//                phoneContainer.setVisibility(View.VISIBLE);
//            }
//
//            if (activityReason.isEmpty())
//                activityContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(activityListView);
//                activityContainer.setVisibility(View.VISIBLE);
//            }
//
//            if (socialReason.isEmpty())
//                socialContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(socialListView);
//                socialContainer.setVisibility(View.VISIBLE);
//            }
//
//            if (locationReason.isEmpty())
//                locationContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(locationListView);
//                locationContainer.setVisibility(View.VISIBLE);
//            }
//
//            if (sleepReason.isEmpty())
//                sleepContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(sleepListView);
//                sleepContainer.setVisibility(View.VISIBLE);
//            }

        }
        switch (stressLevl) {
            case STRESS_LV1:
                hiddenStressImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                hiddenStressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low)));
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_low_bg, requireActivity().getTheme()));
                break;
            case STRESS_LV2:
                hiddenStressImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                hiddenStressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_littlehigh)));
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_littlehigh_bg, requireActivity().getTheme()));
                break;
            case STRESS_LV3:
                hiddenStressImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                hiddenStressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_high)));
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_high_bg, requireActivity().getTheme()));
                break;
        }

    }

    public void selectHiddenViewContentsByCondition(){
        switch (condition){
            case CONDITION1:
                // nothing
                break;
            case CONDITION2:
                condition2Container.setVisibility(View.VISIBLE);
                reasonContainer.setVisibility(View.INVISIBLE);
                Log.d(TAG, "condition2");
                break;
            case CONDITION3:
            default:
                condition2Container.setVisibility(View.GONE);
                reasonContainer.setVisibility(View.VISIBLE);
                Log.d(TAG, "condition3");
                break;
        }
    }
}