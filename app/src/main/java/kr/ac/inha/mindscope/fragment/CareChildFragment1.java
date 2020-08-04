package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.SharedPreferences;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;

import static kr.ac.inha.mindscope.Tools.PREDICTION_FEATUREIDS_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_MODELTAG_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_ORDER_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_STRESSLV_INDEX;
import static kr.ac.inha.mindscope.Tools.SELF_REPORT_DAYNUM_INDEX;
import static kr.ac.inha.mindscope.Tools.SELF_REPORT_ORDER_INDEX;
import static kr.ac.inha.mindscope.fragment.MeFragmentStep2.TIMESTAMP_ONE_DAY;
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
    List<String> stressReports;
    List<String> selfStressReports;
    ConstraintLayout defaultContainer;
    ConstraintLayout hiddenContainer;
    RelativeLayout beforeStartStep2Container;
    TextView beforeStartStep2TextView;
    ImageView hiddenStressImg;
    TextView hiddenDateView;
    TextView hiddenTimeView;
    TextView hiddenStressLevelView;
    ListView phoneListView;
    ListView activityListView;
    ListView socialListView;
    ListView locationListView;
    ListView sleepListView;
    LinearLayout phoneContainer;
    LinearLayout activityContainer;
    LinearLayout socialContainer;
    LinearLayout locationContainer;
    LinearLayout sleepContainer;
    ScrollView reasonContainer;
    int order1StressLevel;
    int order2StressLevel;
    int order3StressLevel;
    int order4StressLevel;


    ArrayList<JSONObject[]> stressReportsJsonArray;
    int[] selfStressReportWithOrderIndex;
    //endregion

    public CareChildFragment1() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

//        if(Tools.isNetworkAvailable()){
//            getStressReportDataFromGRPC();
//            getSelfStressReportDataFromGRPC();
//        }else{
//            Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.when_network_unable), Toast.LENGTH_SHORT).show();
//        }

//        stressReportsJsonArray = new ArrayList<>();
//
//        if(stressReports != null){
//            for(String reportStr : stressReports){
//                JSONObject[] jsonObjects = Tools.parsingStressReport(reportStr);
//                stressReportsJsonArray.add(jsonObjects);
//            }
//        }

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

//        // checkbox UI update
//        for(short i = 0; i < selfStressReportWithOrderIndex.length; i++){
//            if(selfStressReportWithOrderIndex[i] != NON_SELF_STRESS_LV){
//                switch (i+1){
//                    case ORDER1:
//                        checkBox1.setChecked(true);
//                        break;
//                    case ORDER2:
//                        checkBox2.setChecked(true);
//                        break;
//                    case ORDER3:
//                        checkBox3.setChecked(true);
//                        break;
//                    case ORDER4:
//                        checkBox4.setChecked(true);
//                        break;
//                }
//            }else{
//                switch (i+1){
//                    case ORDER1:
//                        checkBox1.setChecked(false);
//                        break;
//                    case ORDER2:
//                        checkBox2.setChecked(false);
//                        break;
//                    case ORDER3:
//                        checkBox3.setChecked(false);
//                        break;
//                    case ORDER4:
//                        checkBox4.setChecked(false);
//                        break;
//                }
//            }
//        }

        updateUI();

        backArrow.setOnClickListener(view1 -> {
            defaultContainer.setVisibility(View.VISIBLE);
            hiddenContainer.setVisibility(View.INVISIBLE);
        });


        arrowBtn1.setOnClickListener(view12 -> {
            defaultContainer.setVisibility(View.INVISIBLE);
            hiddenContainer.setVisibility(View.VISIBLE);
            hiddenViewUpdate(feature_ids1, order1StressLevel);
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration1));
            Tools.saveApplicationLog(getContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 1);
        });
        arrowBtn2.setOnClickListener(view13 -> {
            defaultContainer.setVisibility(View.INVISIBLE);
            hiddenContainer.setVisibility(View.VISIBLE);
            hiddenViewUpdate(feature_ids2, order2StressLevel);
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration2));
            Tools.saveApplicationLog(getContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 2);
        });
        arrowBtn3.setOnClickListener(view14 -> {
            defaultContainer.setVisibility(View.INVISIBLE);
            hiddenContainer.setVisibility(View.VISIBLE);
            hiddenViewUpdate(feature_ids3, order3StressLevel);
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration3));
            Tools.saveApplicationLog(getContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 3);
        });
        arrowBtn4.setOnClickListener(view15 -> {
            defaultContainer.setVisibility(View.INVISIBLE);
            hiddenContainer.setVisibility(View.VISIBLE);
            hiddenViewUpdate(feature_ids4, order4StressLevel);
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration4));
            Tools.saveApplicationLog(getContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 4);
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
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
        phoneListView = view.findViewById(R.id.child1_listview_phone);
        activityListView = view.findViewById(R.id.child1_listview_activity);
        socialListView = view.findViewById(R.id.child1_listview_social);
        locationListView = view.findViewById(R.id.child1_listview_location);
        sleepListView = view.findViewById(R.id.child1_listview_sleep);
        phoneContainer = view.findViewById(R.id.child1_listview_phone_container);
        activityContainer = view.findViewById(R.id.child1_listview_activity_container);
        socialContainer = view.findViewById(R.id.child1_listview_social_container);
        locationContainer = view.findViewById(R.id.child1_listview_location_container);
        sleepContainer = view.findViewById(R.id.child1_listview_sleep_container);

        reasonContainer = view.findViewById(R.id.child1_stress_reason_container);
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);

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
        // Set the date to today if it is between 11 and 24, and set the date to yesterday if it is between 0 and 11
        if (fromCalendar.get(Calendar.HOUR_OF_DAY) >= 11) {
            // today
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            tillCalendar.set(Calendar.HOUR_OF_DAY, 23);
            tillCalendar.set(Calendar.MINUTE, 59);
            tillCalendar.set(Calendar.SECOND, 59);
        } else {
            // yesterday
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            tillCalendar.set(Calendar.HOUR_OF_DAY, 23);
            tillCalendar.set(Calendar.MINUTE, 59);
            tillCalendar.set(Calendar.SECOND, 59);
            long fromTimestampYesterday = fromCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
            long tillTimestampYesterday = tillCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
            fromCalendar.setTimeInMillis(fromTimestampYesterday);
            tillCalendar.setTimeInMillis(tillTimestampYesterday);
        }
        timestamp = fromCalendar.getTimeInMillis();

        //region stress prediction
        while ((line = bufferedReader.readLine()) != null) {
            Log.i(TAG, "readStressReport test: " + line);
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
                Log.i(TAG, "readStressReport test: " + line2);
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
            String date_text2 = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(calendar.getTimeInMillis());
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
                                        arrowBtn1.setVisibility(View.VISIBLE);
                                        feature_ids1 = featre_ids_result;
                                        order1StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV1 ORDER1 feature_ids1: " + feature_ids1);
                                        break;
                                    case ORDER2:
                                        stressImg2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                                        stressTextview2.setText(getResources().getString(R.string.string_low));
                                        stressImg2.setVisibility(View.VISIBLE);
                                        stressTextview2.setVisibility(View.VISIBLE);
                                        arrowBtn2.setVisibility(View.VISIBLE);
                                        feature_ids2 = featre_ids_result;
                                        order2StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV1 ORDER2 feature_ids2: " + feature_ids2);
                                        break;
                                    case ORDER3:
                                        stressImg3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                                        stressTextview3.setText(getResources().getString(R.string.string_low));
                                        stressImg3.setVisibility(View.VISIBLE);
                                        stressTextview3.setVisibility(View.VISIBLE);
                                        arrowBtn3.setVisibility(View.VISIBLE);
                                        feature_ids3 = featre_ids_result;
                                        order3StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV1 ORDER3 feature_ids3: " + feature_ids3);
                                        break;
                                    case ORDER4:
                                        stressImg4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                                        stressTextview4.setText(getResources().getString(R.string.string_low));
                                        stressImg4.setVisibility(View.VISIBLE);
                                        stressTextview4.setVisibility(View.VISIBLE);
                                        arrowBtn4.setVisibility(View.VISIBLE);
                                        feature_ids4 = featre_ids_result;
                                        order4StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV1 ORDER4 feature_ids4: " + feature_ids4);
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
                                        Log.i(TAG, "STRESS_LV2 ORDER1 feature_ids1: " + feature_ids1);
                                        break;
                                    case ORDER2:
                                        stressImg2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                                        stressTextview2.setText(getResources().getString(R.string.string_littlehigh));
                                        stressImg2.setVisibility(View.VISIBLE);
                                        stressTextview2.setVisibility(View.VISIBLE);
                                        arrowBtn2.setVisibility(View.VISIBLE);
                                        feature_ids2 = featre_ids_result;
                                        order2StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV2 ORDER2 feature_ids2: " + feature_ids2);
                                        break;
                                    case ORDER3:
                                        stressImg3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                                        stressTextview3.setText(getResources().getString(R.string.string_littlehigh));
                                        stressImg3.setVisibility(View.VISIBLE);
                                        stressTextview3.setVisibility(View.VISIBLE);
                                        arrowBtn3.setVisibility(View.VISIBLE);
                                        feature_ids3 = featre_ids_result;
                                        order3StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV2 ORDER3 feature_ids3: " + feature_ids3);
                                        break;
                                    case ORDER4:
                                        stressImg4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                                        stressTextview4.setText(getResources().getString(R.string.string_littlehigh));
                                        stressImg4.setVisibility(View.VISIBLE);
                                        stressTextview4.setVisibility(View.VISIBLE);
                                        arrowBtn4.setVisibility(View.VISIBLE);
                                        feature_ids4 = featre_ids_result;
                                        order4StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV2 ORDER4 feature_ids4: " + feature_ids4);
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
                                        Log.i(TAG, "STRESS_LV3 ORDER1 feature_ids1: " + feature_ids1);
                                        break;
                                    case ORDER2:
                                        stressImg2.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                                        stressTextview2.setText(getResources().getString(R.string.string_high));
                                        stressImg2.setVisibility(View.VISIBLE);
                                        stressTextview2.setVisibility(View.VISIBLE);
                                        arrowBtn2.setVisibility(View.VISIBLE);
                                        feature_ids2 = featre_ids_result;
                                        order2StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV3 ORDER2 feature_ids2: " + feature_ids2);
                                        break;
                                    case ORDER3:
                                        stressImg3.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                                        stressTextview3.setText(getResources().getString(R.string.string_high));
                                        stressImg3.setVisibility(View.VISIBLE);
                                        stressTextview3.setVisibility(View.VISIBLE);
                                        arrowBtn3.setVisibility(View.VISIBLE);
                                        feature_ids3 = featre_ids_result;
                                        order3StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV3 ORDER3 feature_ids3: " + feature_ids3);
                                        break;
                                    case ORDER4:
                                        stressImg4.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                                        stressTextview4.setText(getResources().getString(R.string.string_high));
                                        stressImg4.setVisibility(View.VISIBLE);
                                        stressTextview4.setVisibility(View.VISIBLE);
                                        arrowBtn4.setVisibility(View.VISIBLE);
                                        feature_ids4 = featre_ids_result;
                                        order4StressLevel = stressLevel;
                                        Log.i(TAG, "STRESS_LV3 ORDER4 feature_ids4: " + feature_ids4);
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
            if (predictionModelTag && predictionOrder != 0) {
                if (selfStressReportWithOrderIndex[predictionOrder - 1] != NON_SELF_STRESS_LV
                        && selfStressReportWithOrderIndex[predictionOrder - 1] != predictionStressLv) {
                    stressLvArray[predictionOrder - 1] = selfStressReportWithOrderIndex[predictionOrder - 1];
                } else {
                    stressLvArray[predictionOrder - 1] = predictionStressLv;
                }
            }
        }

//        while((predictionLine = bufferedReader.readLine()) != null){
//            String[] predictionTokens = predictionLine.split(",");
//            boolean predictionModelTag = Boolean.parseBoolean(predictionTokens[PREDICTION_MODELTAG_INDEX]);
//            int predictionOrder = Integer.parseInt(predictionTokens[PREDICTION_ORDER_INDEX]);
//            int predictionStressLv = Integer.parseInt(predictionTokens[PREDICTION_STRESSLV_INDEX]);
//            if(predictionModelTag){
//                if(selfStressReportWithOrderIndex[predictionOrder - 1] != NON_SELF_STRESS_LV
//                        && selfStressReportWithOrderIndex[predictionOrder - 1] != predictionStressLv){
//                    stressLvArray[predictionOrder - 1] = selfStressReportWithOrderIndex[predictionOrder - 1];
//                }else{
//                    stressLvArray[predictionOrder - 1] = predictionStressLv;
//                }
//            }
//        }

        int count = 0;
        for (int i : stressLvArray) {
            if (i != NON_SELF_STRESS_LV) {
                sumStress += (i + 1);
                count++;
            }
        }

//        for(int order = 1; order <= selfStressReportWithOrderIndex.length; order++){
//            if(selfStressReportWithOrderIndex[order -1] != NON_SELF_STRESS_LV){
//                sumStress += selfStressReportWithOrderIndex[order - 1] + 1;
//            }
//            else{
//                for(JSONObject[] resultSet : stressReportsJsonArray){
//                    if(resultSet != null){
//                        try {
//                            if(resultSet[0].getInt("ema_order") == order){
//                                for(short stressLevel = 0; stressLevel < resultSet.length; stressLevel++){
//                                    if(resultSet[stressLevel].getBoolean("model_tag")){
//                                        sumStress += stressLevel + 1;
//                                    }
//                                }
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
        float avg = (float) sumStress / count;
        return avg;
//        return (float)sumStress / stressReportsJsonArray.size();
    }

    public void hiddenViewUpdate(String feature_ids, int stressLevl) {
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
                String packName = requireContext().getPackageName();
                int resId = getResources().getIdentifier(strID, "string", packName);

                if (category <= 5) {
                    activityReason.add(getResources().getString(resId));
                } else if (category <= 11) {
                    socialReason.add(getResources().getString(resId));
                } else if (category <= 16) {
                    locationReason.add(getResources().getString(resId));
                } else if (category <= 28) {
                    phoneReason.add(getResources().getString(resId));
                } else {
                    sleepReason.add(getResources().getString(resId));
                }

                if (i == 4) // maximun number of showing feature is five
                    break;
            }
        }

        Log.d(TAG, "phoneReason" + phoneReason.toString());
        Log.d(TAG, "activityReason" + activityReason.toString());

        ArrayAdapter<String> phoneAdapter = new ArrayAdapter<>(
                requireActivity(), R.layout.item_feature_ids, phoneReason
        );
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(
                requireActivity(), R.layout.item_feature_ids, activityReason
        );
        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(
                requireActivity(), R.layout.item_feature_ids, socialReason
        );
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                requireActivity(), R.layout.item_feature_ids, locationReason
        );
        ArrayAdapter<String> sleepAdapter = new ArrayAdapter<>(
                requireActivity(), R.layout.item_feature_ids, sleepReason
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

    //region old fuction
    public void getStressReportDataFromGRPC() {
        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        SharedPreferences configPrefs = requireActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

        // initialize timestamp from today 00:00:00 to 23:59:59
        Calendar fromCalendar = Calendar.getInstance();
        Calendar tillCalendar = Calendar.getInstance();
        // Set the date to today if it is between 11 and 24, and set the date to yesterday if it is between 0 and 11
        if (fromCalendar.get(Calendar.HOUR_OF_DAY) >= 11) {
            // today
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            tillCalendar.set(Calendar.HOUR_OF_DAY, 23);
            tillCalendar.set(Calendar.MINUTE, 59);
            tillCalendar.set(Calendar.SECOND, 59);
        } else {
            // yesterday
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            tillCalendar.set(Calendar.HOUR_OF_DAY, 23);
            tillCalendar.set(Calendar.MINUTE, 59);
            tillCalendar.set(Calendar.SECOND, 59);
            long fromTimestampYesterday = fromCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
            long tillTimestampYesterday = tillCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
            fromCalendar.setTimeInMillis(fromTimestampYesterday);
            tillCalendar.setTimeInMillis(tillTimestampYesterday);
        }
        timestamp = fromCalendar.getTimeInMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG, "initialize fromCalendar: " + dateFormat.format(fromCalendar.getTime()));
        Log.i(TAG, "initialize tillCalendar: " + dateFormat.format(tillCalendar.getTime()));

        // for test 2020/07/01 00:00:00 ~ 23:59:59
        long fromtimestamp = 1593561600000l;
        long tilltimestamp = 1593647999000l;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

        ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

        EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                .setTargetDataSourceId(configPrefs.getInt("STRESS_PREDICTION", -1))
                .setFromTimestamp(fromCalendar.getTimeInMillis()) // TODO change fromCalendar.getTimeInMillis()
                .setTillTimestamp(tillCalendar.getTimeInMillis()) // TODO change tillCalendar.getTimeInMillis()
                .build();


        final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
        if (responseMessage.getDoneSuccessfully()) {
            List<String> values = responseMessage.getValueList();
            List<Long> timestampValues = responseMessage.getTimestampList();
            if (!values.isEmpty()) {
                for (String report : values) {
                    stressReports = values;
                    Log.i(TAG, report);
                }
            } else {
                Log.d(TAG, "values empty");
            }

        }
        channel.shutdown();
    }

    public void getSelfStressReportDataFromGRPC() {
        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        SharedPreferences configPrefs = requireActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

        // initialize timestamp from today 00:00:00 to 23:59:59
        Calendar fromCalendar = Calendar.getInstance();
        Calendar tillCalendar = Calendar.getInstance();

        // Set the date to today if it is between 11 and 24, and set the date to yesterday if it is between 0 and 11
        if (fromCalendar.get(Calendar.HOUR_OF_DAY) >= 11) {
            // today
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            tillCalendar.set(Calendar.HOUR_OF_DAY, 23);
            tillCalendar.set(Calendar.MINUTE, 59);
            tillCalendar.set(Calendar.SECOND, 59);
        } else {
            // yesterday
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            tillCalendar.set(Calendar.HOUR_OF_DAY, 23);
            tillCalendar.set(Calendar.MINUTE, 59);
            tillCalendar.set(Calendar.SECOND, 59);
            long fromTimestampYesterday = fromCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
            long tillTimestampYesterday = tillCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
            fromCalendar.setTimeInMillis(fromTimestampYesterday);
            tillCalendar.setTimeInMillis(tillTimestampYesterday);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG, "initialize fromCalendar: " + dateFormat.format(fromCalendar.getTime()));
        Log.i(TAG, "initialize tillCalendar: " + dateFormat.format(tillCalendar.getTime()));

        // for test 2020/07/018 04:19:00 ~ 04:28:00
        long fromtimestamp = 1595013540000l;
        long tilltimestamp = 1595014080000l;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

        ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

        EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                .setTargetDataSourceId(configPrefs.getInt("SELF_STRESS_REPORT", -1))
                .setFromTimestamp(fromCalendar.getTimeInMillis()) //  fromtimestamp
                .setTillTimestamp(tillCalendar.getTimeInMillis()) // tilltimestamp
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
    }
    //endregion
}