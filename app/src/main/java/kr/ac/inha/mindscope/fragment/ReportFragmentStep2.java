package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.ByteString;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.MapsActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;

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
import static kr.ac.inha.mindscope.Tools.timeTheDayNumIsChanged;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.CONDITION1;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.CONDITION2;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.CONDITION3;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.setListViewHeightBasedOnChildren;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportFragmentStep2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportFragmentStep2 extends Fragment implements OnDateSelectedListener {
    // region variables

    private static final String TAG = "ReportFragmentStep2";
    private static final String ACTION_CLICK_HELP = "CLICK_HELP";
    private static final String ACTION_CLICK_DAY = "CLICK_DAY";
    private static final String ACTION_CLICK_DETAIL_REPORT = "CLICK_DETAIL_REPORT";

    private static final int tileWidth = 45;
    private static final int tileHeight = 45;
    int curCondition;
    static HashMap<Long, Pair<Integer, Integer>> stressLevels = new HashMap<>();
    static HashMap<CalendarDay, Integer> dailyAverageStressLevels = new HashMap<>();
    static HashMap<CalendarDay, ArrayList<Triple<Long, Integer, Integer>>> dailyStressLevelClusters = new HashMap<>();
    static HashMap<Long, JSONObject> timestampStressFeaturesMap = new HashMap<>();
    static HashMap<Long, String> selectedDailyTags = new HashMap<>();
    private static MaterialCalendarView materialCalendarView;
    Button frg_report_toolbar_btn;
    ImageView fragmentMeStep2BtnHelp;
    TextView fragment_report_app_title;
    TextView dateView;
    TextView txtStressLevel;
    TextView sumPointsView;
    TextView dailyPointsView;
    ImageView dailyAverageStressLevelView;
    CheckBox checkBoxEmaOrder1;
    CheckBox checkBoxEmaOrder2;
    CheckBox checkBoxEmaOrder3;
    CheckBox checkBoxEmaOrder4;
    ImageView imageViewSlot1;
    ImageView imageViewSlot2;
    ImageView imageViewSlot3;
    ImageView imageViewSlot4;
    TextView textViewSlot1;
    TextView textViewSlot2;
    TextView textViewSlot3;
    TextView textViewSlot4;
    ImageButton arrowResult1;
    ImageButton arrowResult2;
    ImageButton arrowResult3;
    ImageButton arrowResult4;
    TextView selectedDayComment;
    ScrollView defaultContainer;
    ConstraintLayout hiddenContainer;
    ConstraintLayout dayDetailContainer;
    ConstraintLayout comment_container;
    ConstraintLayout condition3_container;
    ImageView hiddenStressImg;
    TextView hiddenDateView;
    TextView hiddenTimeView;
    TextView hiddenStressLevelView;
    ConstraintLayout condition2Container;
    LinearLayout condition2Layout;

    private TextView txtReason;
    private ImageView condition2Img1;
    private ImageView condition2Img2;
    private ImageView condition2Img3;
    private ImageView condition2Img4;
    private ImageView condition2Img5;
    private TextView condition2txt1;
    private TextView condition2txt2;
    private TextView condition2txt3;
    private TextView condition2txt4;
    private TextView condition2txt5;
    public int condition;
    RelativeLayout categoryImgContainer1;
    RelativeLayout categoryImgContainer2;
    RelativeLayout categoryImgContainer3;
    RelativeLayout categoryImgContainer4;
    RelativeLayout categoryImgContainer5;

    ScrollView reasonContainer;
    //ImageButton backArrow;
    ArrayList<String> predictionArray;
    ArrayList<String> selfReportArray;
    private long joinTimestamp;
    private CalendarDay selectedDay;
    private CalendarDay chooseDay;
    SharedPreferences lastPagePrefs;
    TextView noFeatureTextview;
    TextView tagsTextview;

    ListView integrateListView;
    LinearLayout integrateContainer;

    
    //@jeongin: 화살표 클릭했을때 리스너
    class ArrowBtnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            switch(v.getTag().toString()){
                case "1": //7:00 ~ 11:00
                    c.set(chooseDay.getYear(), chooseDay.getMonth() - 1, chooseDay.getDay(), 11, 0, 0);
                    hiddenTimeView.setText(getResources().getString(R.string.time_report_duration1));
                    break;
                case "2"://11:00 ~ 15:00
                    c.set(selectedDay.getYear(), selectedDay.getMonth() - 1, selectedDay.getDay(), 15, 0, 0);
                    hiddenTimeView.setText(getResources().getString(R.string.time_report_duration2));
                    break;
                case "3"://15:00 ~ 19:00
                    c.set(selectedDay.getYear(), selectedDay.getMonth() - 1, selectedDay.getDay(), 19, 0, 0);
                    hiddenTimeView.setText(getResources().getString(R.string.time_report_duration3));
                    break;
                case "4"://19:00 ~ 23:00
                    c.set(selectedDay.getYear(), selectedDay.getMonth() - 1, selectedDay.getDay(), 23, 0, 0);
                    hiddenTimeView.setText(getResources().getString(R.string.time_report_duration4));
                    break;

            }
            c.set(Calendar.MILLISECOND, 0);
            
            //@jeongin: 선택한 날짜에서 컨디션 계산하기
            long timestamp = c.getTimeInMillis();
            long diff = timestamp - joinTimestamp;
            if(diff < Tools.CONDITION_EXPIRE_DURATION1){
                curCondition = 0;
            }
            else if(diff < Tools.CONDITION_EXPIRE_DURATION2){
                curCondition = 3;
            }
            else if(diff < Tools.CONDITION_EXPIRE_DURATION3){
                curCondition = 2;
            }
            else{
                curCondition = 1;
            }

            String hiddenDateStr = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(timestamp);
            hiddenDateView.setText(hiddenDateStr + "의");
            JSONObject object = timestampStressFeaturesMap.get(timestamp);

            int stressLv = stressLevels.get(timestamp).second;

            Log.d(TAG, String.valueOf(timestamp));
            Log.d(TAG,timestampStressFeaturesMap.toString());

            if(curCondition<1){
                //step1일때 태그 가져와서 보여주기
                defaultContainer.setVisibility(View.INVISIBLE);
                hiddenContainer.setVisibility(View.VISIBLE);
                hiddenViewUpdateStep1(stressLv,Integer.parseInt(v.getTag().toString()));
                fragment_report_app_title.setText("나의 태그");

            }else{
                //step2일때 상세화면
                String feature_ids = null;
                if (object != null) {
                    try {
                        feature_ids = object.getJSONObject(String.valueOf(stressLv)).getString("feature_ids");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //feature_ids ="17-low 12-low";
                if (feature_ids != null && !feature_ids.equals("NO_FEATURES")) {
                    Log.d(TAG, "feature_ids : " + feature_ids);
                    defaultContainer.setVisibility(View.INVISIBLE);
                    hiddenContainer.setVisibility(View.VISIBLE);
                    hiddenViewUpdateStep2(feature_ids, stressLv);
                    fragment_report_app_title.setText("스트레스 리포트");
                } else {
                    Toast.makeText(requireContext(), getResources().getString(R.string.string_no_detail_report), Toast.LENGTH_LONG).show();
                }
                Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 1);

            }

            fragmentMeStep2BtnHelp.setVisibility(View.GONE);
            frg_report_toolbar_btn.setVisibility(View.VISIBLE);
        }
    }


    // endregion

    public ReportFragmentStep2() {
        // Required empty public constructor
    }

    // region utility functions
    public static ReportFragmentStep2 newInstance() {
        return new ReportFragmentStep2();
    }

    // region override
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SharedPreferences stepChangePrefs = requireContext().getSharedPreferences("stepChange", Context.MODE_PRIVATE);
        condition = stepChangePrefs.getInt("condition", 0);
        Log.d(TAG,"condition"+condition);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_report_step2, container, false);
        try {
            joinTimestamp = getJoinTime();
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "join timestamp " + joinTimestamp);

        defaultContainer = root.findViewById(R.id.frg_report_step2_container1);
        dayDetailContainer = root.findViewById(R.id.frg_report_step2_date_container);

        Calendar firstDayCal = Calendar.getInstance(Locale.KOREA);
        firstDayCal.setTimeInMillis(joinTimestamp);
        String date = DateFormat.format("yyyy-MM-dd", firstDayCal).toString();

        Log.d(TAG, "firstdate calendar " + date);
        frg_report_toolbar_btn = root.findViewById(R.id.frg_report_toolbar_btn);
        fragment_report_app_title = root.findViewById(R.id.fragment_report_app_title);
        sumPointsView = root.findViewById(R.id.summary_point_my);
        dailyPointsView = root.findViewById(R.id.point_day);
        checkBoxEmaOrder1 = root.findViewById(R.id.ch_report1);
        checkBoxEmaOrder2 = root.findViewById(R.id.ch_report2);
        checkBoxEmaOrder3 = root.findViewById(R.id.ch_report3);
        checkBoxEmaOrder4 = root.findViewById(R.id.ch_report4);
        imageViewSlot1 = root.findViewById(R.id.img_report_result1);
        imageViewSlot2 = root.findViewById(R.id.img_report_result2);
        imageViewSlot3 = root.findViewById(R.id.img_report_result3);
        imageViewSlot4 = root.findViewById(R.id.img_report_result4);
        textViewSlot1 = root.findViewById(R.id.txt_report_result1);
        textViewSlot2 = root.findViewById(R.id.txt_report_result2);
        textViewSlot3 = root.findViewById(R.id.txt_report_result3);
        textViewSlot4 = root.findViewById(R.id.txt_report_result4);
        arrowResult1 = root.findViewById(R.id.arrow_result1);
        arrowResult2 = root.findViewById(R.id.arrow_result2);
        arrowResult3 = root.findViewById(R.id.arrow_result3);
        arrowResult4 = root.findViewById(R.id.arrow_result4);
        comment_container = root.findViewById(R.id.comment_container);
        selectedDayComment = root.findViewById(R.id.selected_day_comment);
        dailyAverageStressLevelView = root.findViewById(R.id.frg_report_step2_img1);
        SharedPreferences prefs = requireContext().getSharedPreferences("points", Context.MODE_PRIVATE);
        sumPointsView.setText(String.valueOf(prefs.getInt("sumPoints", 0)));

        // hidden view
        hiddenContainer = root.findViewById(R.id.frg_report_step2_container2);
        hiddenStressImg = root.findViewById(R.id.frg_report_step2_hidden_img1);
        hiddenDateView = root.findViewById(R.id.frg_report_step2_date1);
        hiddenTimeView = root.findViewById(R.id.frg_report_step2_time1);
        hiddenStressLevelView = root.findViewById(R.id.frg_report_step2_txt_stress_level);
       // backArrow = root.findViewById(R.id.frg_report_step2_back_arrow);
        condition2Container = root.findViewById(R.id.frg_report_step2_stress_condition2_container);
        condition2Layout = root.findViewById(R.id.frg_report_step2_condition2_layout);
        condition3_container = root.findViewById(R.id.frg_report_condition3_container);

        condition2Img1 = root.findViewById(R.id.frg_report_step2_img1_);
        condition2Img2 = root.findViewById(R.id.frg_report_step2_img2);
        condition2Img3 = root.findViewById(R.id.frg_report_step2_img3);
        condition2Img4 = root.findViewById(R.id.frg_report_step2_img4);
        condition2Img5 = root.findViewById(R.id.frg_report_step2_img5);
        condition2txt1 = root.findViewById(R.id.frg_report_step2_txt1);
        condition2txt2 = root.findViewById(R.id.frg_report_step2_txt2);
        condition2txt3 = root.findViewById(R.id.frg_report_step2_txt3);
        condition2txt4 = root.findViewById(R.id.frg_report_step2_txt4);
        condition2txt5 = root.findViewById(R.id.frg_report_step2_txt5);
        categoryImgContainer1 = root.findViewById(R.id.frg_report_step2_img_container1);
        categoryImgContainer2 = root.findViewById(R.id.frg_report_step2_img_container2);
        categoryImgContainer3 = root.findViewById(R.id.frg_report_step2_img_container3);
        categoryImgContainer4 = root.findViewById(R.id.frg_report_step2_img_container4);
        categoryImgContainer5 = root.findViewById(R.id.frg_report_step2_img_container5);

        ArrowBtnClickListener arrowBtnClickListener = new ArrowBtnClickListener();

        txtReason = root.findViewById(R.id.frg_report_step2_txt_reason);
        integrateListView = root.findViewById(R.id.frg_report_step2_listview_integrate);
        integrateContainer =  root.findViewById(R.id.frg_report_step2_listview_integrate_container);


        reasonContainer = root.findViewById(R.id.frg_report_step2_reason_container);
        noFeatureTextview = root.findViewById(R.id.frg_report_step2_no_features);
        tagsTextview = root.findViewById(R.id.frg_report_step1_tags);
        txtStressLevel = root.findViewById(R.id.txt_stress_level);
        txtStressLevel.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low)));




        // todo 이 파일 안에서 달력에 그리는거 구현해야합니다.
        materialCalendarView = root.findViewById(R.id.calendarView);
        materialCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);
        materialCalendarView.setTileHeightDp(tileHeight);
        materialCalendarView.setTileWidthDp(tileWidth);
        materialCalendarView.addDecorators(
                new DayDecorator(),
                new LowStressDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.low_circle_background)),
                new LittleHighStressDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.littlehigh_circle_background)),
                new HighStressDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.high_circle_background)),
                new LowEMADecorator(ContextCompat.getDrawable(requireContext(), R.drawable.low_circle_background)),
                new LittleHighEMADecorator(ContextCompat.getDrawable(requireContext(), R.drawable.littlehigh_circle_background)),
                new HighEMADecorator(ContextCompat.getDrawable(requireContext(), R.drawable.high_circle_background))
        );
        materialCalendarView.setOnDateChangedListener(this);

        fragmentMeStep2BtnHelp = root.findViewById(R.id.fragment_me_step2_btn_help);
        dateView = root.findViewById(R.id.report_step2_date);
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);
        fragmentMeStep2BtnHelp.setOnClickListener((v) -> {
            Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_HELP);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://haesookim.info/MindScope/"));
            startActivity(intent);
        });

        frg_report_toolbar_btn.setOnClickListener((v)->{
            fragmentMeStep2BtnHelp.setVisibility(View.VISIBLE);
            frg_report_toolbar_btn.setVisibility(View.GONE);
            defaultContainer.setVisibility(View.VISIBLE);
            hiddenContainer.setVisibility(View.INVISIBLE);
        });


        // set up per time-slot stress level button clicks
        // todo STEP1 일때는 태그한 데이터 들이 보여질수 있도록 구현

        arrowResult1.setOnClickListener(arrowBtnClickListener);
        arrowResult2.setOnClickListener(arrowBtnClickListener);
        arrowResult3.setOnClickListener(arrowBtnClickListener);
        arrowResult4.setOnClickListener(arrowBtnClickListener);

//        backArrow.setOnClickListener(view -> {
//            defaultContainer.setVisibility(View.VISIBLE);
//            hiddenContainer.setVisibility(View.INVISIBLE);
//            fragmentMeStep2BtnHelp.setVisibility(View.VISIBLE);
//            frg_report_toolbar_btn.setVisibility(View.GONE);
//        });

        loadAllStressLevelsFromServer();
        loadAllPoints();
        selectedDay = CalendarDay.today();
        materialCalendarView.setSelectedDate(selectedDay);
        loadDailyPoints(selectedDay);
        onDateSelected(materialCalendarView, selectedDay, true);
        return root;
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay day, boolean selected) {
        // [Kevin] 선택된 날짜의 stress 보고서 보여주기
        chooseDay = day;
        Log.d(TAG, "Seleted Date: " + day);
        Calendar c = Calendar.getInstance();
        c.set(day.getYear(), day.getMonth() - 1, day.getDay(), 0, 0, 0);
        Date currentTime = c.getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);

        // dailyTags (step1 hiddenView)
        if(curCondition<1)
            loadDailyTags(c);

        // region (1) daily points
        loadDailyPoints(day);
        // endregion

        // region (2) detailed view
        CheckBox[] checkBoxes = new CheckBox[]{checkBoxEmaOrder1, checkBoxEmaOrder2, checkBoxEmaOrder3, checkBoxEmaOrder4};
        ImageView[] imageViews = new ImageView[]{imageViewSlot1, imageViewSlot2, imageViewSlot3, imageViewSlot4};
        TextView[] textViews = new TextView[]{textViewSlot1, textViewSlot2, textViewSlot3, textViewSlot4};
        ImageButton[] imageButtons = new ImageButton[]{arrowResult1, arrowResult2, arrowResult3, arrowResult4};
        int[] stressLevelTexts = new int[]{R.string.string_low, R.string.string_littlehigh, R.string.string_high};
        int[] stressLevelImageResources = new int[]{R.drawable.icon_low, R.drawable.icon_littlehigh, R.drawable.icon_high};
        Pair<Integer, Integer>[] ranges = new Pair[]{new Pair<>(700, 1100), new Pair<>(1100, 1500), new Pair<>(1500, 1900), new Pair<>(1900, 2300)};
        for (CheckBox checkBox : checkBoxes)
            checkBox.setChecked(false);
        for (ImageView imageView : imageViews)
            imageView.setVisibility(View.INVISIBLE);
        for (TextView textView : textViews)
            textView.setVisibility(View.INVISIBLE);
        for (ImageButton imageButton : imageButtons)
            imageButton.setVisibility(View.INVISIBLE);
        if (dailyStressLevelClusters.containsKey(day)) {
            ArrayList<Triple<Long, Integer, Integer>> stressLevels = dailyStressLevelClusters.get(day);
            assert stressLevels != null;

            for (Triple<Long, Integer, Integer> tsEmaOrderStressLvlTriple : stressLevels) {
                long timestamp = tsEmaOrderStressLvlTriple.getLeft();
                int emaOrder = tsEmaOrderStressLvlTriple.getMiddle();
                int stressLevel = tsEmaOrderStressLvlTriple.getRight();

                if (emaOrder > 0 && emaOrder <= checkBoxes.length) {
                    // self-report
                    checkBoxes[emaOrder - 1].setChecked(true);

                    textViews[emaOrder - 1].setVisibility(View.VISIBLE);
                    textViews[emaOrder - 1].setText(stressLevelTexts[stressLevel]);

                    imageViews[emaOrder - 1].setVisibility(View.VISIBLE);
                    imageViews[emaOrder - 1].setImageResource(stressLevelImageResources[stressLevel]);

                    imageButtons[emaOrder - 1].setVisibility(View.VISIBLE);
                } else {
                    // detection
                    int index = findTimestampIndex(timestamp, ranges);
                    if (index > -1 && index < imageViews.length) {
                        textViews[index].setVisibility(View.VISIBLE);
                        textViews[index].setText(stressLevelTexts[stressLevel]);

                        imageViews[index].setVisibility(View.VISIBLE);
                        imageViews[index].setImageResource(stressLevelImageResources[stressLevel]);

                        imageButtons[index].setVisibility(View.VISIBLE);
                    }
                }
                // endregion
            }
        } else
            for (CheckBox checkBox : checkBoxes)
                checkBox.setChecked(false);
        // endregion

        // region (3) daily comment
        if (Tools.isNetworkAvailable()) {
            new Thread(() -> {

                SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(requireContext().getString(R.string.stress_campaign_id));
                final int DAILY_COMMENT = 33;

                Calendar cal = Calendar.getInstance();
                cal.set(day.getYear(), day.getMonth() - 1, day.getDay(), 0, 0, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long fromTimestamp = cal.getTimeInMillis();
                cal.add(Calendar.DAY_OF_MONTH, 1);
                long tillTimestamp = cal.getTimeInMillis();

                if (dailyAverageStressLevels.containsKey(day)) {
                    int[] stressLevelStrings = new int[]{
                            R.string.string_stress_level_low,
                            R.string.string_stress_level_littlehigh,
                            R.string.string_stress_level_high
                    };
                    int stressLevel = dailyAverageStressLevels.get(day);
                    if(isAdded()){
                        requireActivity().runOnUiThread(() -> {
                            txtStressLevel.setText(Html.fromHtml(getResources().getString(stressLevelStrings[stressLevel])));
                            txtStressLevel.setVisibility(View.VISIBLE);
                        });
                    }


                } else{
                    if(isAdded()){
                        requireActivity().runOnUiThread(() -> {
                            txtStressLevel.setVisibility(View.GONE); // should never happen
                        });
                    }
                }


                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                EtService.RetrieveFilteredDataRecords.Request requestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(DAILY_COMMENT)
                        .setFromTimestamp(fromTimestamp)
                        .setTillTimestamp(tillTimestamp)
                        .build();
                long lastTimestamp = Long.MIN_VALUE;
                String lastComment = "";
                try {
                    EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                    if (responseMessage.getSuccess()){
                        // checkByteString
                        for (ByteString value : responseMessage.getValueList()) {
                            String valueStr = value.toString("UTF-8");
                            String[] cells = valueStr.split(" ");
                            long timestamp = Long.parseLong(valueStr.substring(0, valueStr.indexOf(' ')));
                            String comment = valueStr.substring(valueStr.indexOf(' ') + 1);

                            if (timestamp > lastTimestamp) {
                                lastTimestamp = timestamp;
                                lastComment = comment;
                            }
                        }
                    }
                } catch (StatusRuntimeException | IOException e) {
                    e.printStackTrace();
                }
                channel.shutdown();

                final String finalComment = lastComment;
                if(finalComment.length()==0){
                    comment_container.setVisibility(View.GONE);
                }else{
                    comment_container.setVisibility(View.VISIBLE);
                }
                if(isAdded())
                    requireActivity().runOnUiThread(() -> selectedDayComment.setText(finalComment.length() == 0 ? "[N/A]" : finalComment));
            }).start();
        }
        // endregion

        // region (4) average daily stress level
        int[] stressLvlImageResources = new int[]{
                R.drawable.icon_low,
                R.drawable.icon_littlehigh,
                R.drawable.icon_high
        };
        if (dailyAverageStressLevels.containsKey(day)) {
            dayDetailContainer.setVisibility(View.VISIBLE);
            dailyAverageStressLevelView.setImageResource(stressLvlImageResources[dailyAverageStressLevels.get(day)]);
            dailyAverageStressLevelView.setVisibility(View.VISIBLE);
        } else {
            dayDetailContainer.setVisibility(View.GONE);
            dailyAverageStressLevelView.setVisibility(View.GONE);
        }
        // endregion

        materialCalendarView.invalidateDecorators();
        selectedDay = day;
        Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_DAY, date_text.replace(" ", "_"));
    }

    private int findTimestampIndex(long timestamp, Pair<Integer, Integer>[] range) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        long cell = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE);
        for (int n = 0; n < range.length; n++) {
            Pair<Integer, Integer> pair = range[n];
            if (pair.first < cell && cell <= pair.second)
                return n;
        }
        return -1;
    }

    @Override
    public void onStop() {
        super.onStop();
        lastPagePrefs = requireActivity().getSharedPreferences("LastPage", Context.MODE_PRIVATE);
        SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
        lastPagePrefsEditor.putString("last_open_nav_frg", "report");
        lastPagePrefsEditor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();



        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
    }

    public long getJoinTime() {
        long firstDayTimestamp = 0;
        SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);

        if (Tools.isNetworkAvailable()) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
            ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
            EtService.RetrieveParticipantStats.Request retrieveParticipantStatisticsRequestMessage = EtService.RetrieveParticipantStats.Request.newBuilder()
                    .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                    .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                    .build();
            try {
                EtService.RetrieveParticipantStats.Response responseMessage = stub.retrieveParticipantStats(retrieveParticipantStatisticsRequestMessage);
                if (responseMessage.getSuccess()) {
                    firstDayTimestamp = responseMessage.getCampaignJoinTimestamp();
                }
            } catch (StatusRuntimeException e) {
                e.printStackTrace();
            }
            channel.shutdown();
        } else {
            Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.when_network_unable), Toast.LENGTH_SHORT).show();
        }
        return firstDayTimestamp;
    }

    public void loadAllPoints() {
        if (Tools.isNetworkAvailable()) {
            new Thread(() -> {
                SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(requireContext().getString(R.string.stress_campaign_id));
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
                    if (responseMessage.getSuccess()){
                        // checkByteString
                        for (ByteString value : responseMessage.getValueList()) {
                            String valueStr = value.toString("UTF-8");
                            String[] cells = valueStr.split(" ");
                            if (cells.length != 3)
                                continue;
                            allPointsMaps.put(Long.parseLong(cells[0]), Integer.parseInt(cells[2]));
                            Log.d("test",allPointsMaps.toString());
//                            points += Integer.parseInt(cells[2]);
                        }
                    }
                } catch (StatusRuntimeException | IOException e) {
                    e.printStackTrace();
                }
                channel.shutdown();


                for(Map.Entry<Long, Integer> elem : allPointsMaps.entrySet()){
                    points += elem.getValue();
                }

                final int finalPoints = points;
                if(isAdded())
                    requireActivity().runOnUiThread(() -> sumPointsView.setText(String.format(Locale.getDefault(), "%,d", finalPoints))); // changed sumPoints logic based on server sumPoints
            }).start();
        }
    }
    public void loadDailyTags(Calendar day) {
        Log.d(TAG,"loadDailyTags");
        if (Tools.isNetworkAvailable()) {
            new Thread(() -> {
                HashMap<Long, String> dailyTagMaps = new HashMap<>();
                SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(requireContext().getString(R.string.stress_campaign_id));
                final int REPORT_TAGS = 27;

                Calendar c = day;
                //11:00 ~ 24:00 데이터 가져옴
                c.set(chooseDay.getYear(), chooseDay.getMonth() - 1, chooseDay.getDay(), 11, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                long fromTimestamp = c.getTimeInMillis();
                c.add(Calendar.HOUR_OF_DAY, 13);
                long tillTimestamp = c.getTimeInMillis();

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                EtService.RetrieveFilteredDataRecords.Request requestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(REPORT_TAGS)
                        .setFromTimestamp(fromTimestamp)
                        .setTillTimestamp(tillTimestamp)
                        .build();
                try {
                    EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                    if (responseMessage.getSuccess()){
                        Log.d(TAG,"after responseMessage.getSuccess()");
                        Log.d(TAG,responseMessage.getValueList().toString());
                        // checkByteString
                        for (ByteString value : responseMessage.getValueList()) {
                            String valueStr = value.toString("UTF-8");
                            Log.d(TAG,valueStr);
                            String[] cells = valueStr.split(" ");
                            if (cells.length != 4)
                                continue;
                            if(dailyTagMaps.get(Long.parseLong(cells[2]))!=null){
                                dailyTagMaps.put(Long.parseLong(cells[2]),dailyTagMaps.get(Long.parseLong(cells[2]))+" "+cells[3]);
                            }else{
                                dailyTagMaps.put(Long.parseLong(cells[2]), cells[3]);
                            }
                            Log.d(TAG,dailyTagMaps.toString());
                        }
                    }

                } catch (StatusRuntimeException | IOException e) {
                    e.printStackTrace();
                }
                selectedDailyTags = dailyTagMaps;
                channel.shutdown();
//                if(isAdded())
//                    requireActivity().runOnUiThread(() -> tagsTextview.setText(finalDailyTags));
            }).start();
        }


    }

    public void loadDailyPoints(CalendarDay day) {
        Log.d(TAG,"loadDailyPoints");
        if (Tools.isNetworkAvailable()) {
            new Thread(() -> {
                HashMap<Long, Integer> dailyPointsMaps = new HashMap<>();
                SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(requireContext().getString(R.string.stress_campaign_id));
                final int REWARD_POINTS = 26;

                Calendar c = Calendar.getInstance();
                c.set(day.getYear(), day.getMonth() - 1, day.getDay(), timeTheDayNumIsChanged, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                long fromTimestamp = c.getTimeInMillis();
                c.add(Calendar.DAY_OF_MONTH, 1);
                long tillTimestamp = c.getTimeInMillis();

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                EtService.RetrieveFilteredDataRecords.Request requestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(REWARD_POINTS)
                        .setFromTimestamp(fromTimestamp)
                        .setTillTimestamp(tillTimestamp)
                        .build();
                int dailyPoints = 0;
                try {
                    EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                    if (responseMessage.getSuccess()){
                        // checkByteString
                        for (ByteString value : responseMessage.getValueList()) {
                            String valueStr = value.toString("UTF-8");
                            String[] cells = valueStr.split(" ");
                            if (cells.length != 3)
                                continue;
                            dailyPointsMaps.put(Long.parseLong(cells[0]), Integer.parseInt(cells[2]));
                            Log.d(TAG,dailyPointsMaps.toString());
//                            dailyPoints += Integer.parseInt(cells[2]);
                        }
                        for(Map.Entry<Long, Integer> elem : dailyPointsMaps.entrySet()){
                            dailyPoints += elem.getValue();
                        }
                    }

                } catch (StatusRuntimeException | IOException e) {
                    e.printStackTrace();
                }
                channel.shutdown();
                final int finalDailyPoints = dailyPoints;
                if(isAdded())
                    requireActivity().runOnUiThread(() -> dailyPointsView.setText(String.format(Locale.getDefault(), "%,d", finalDailyPoints)));
            }).start();
        }


    }
    // endregion

    public void loadAllStressLevelsFromServer() {
        if (Tools.isNetworkAvailable()) {
            // [Kevin] load stress levels (submissions & predictions) from gRPC server
            new Thread(() -> {
                SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(requireContext().getString(R.string.stress_campaign_id));

                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_MONTH, 1);
                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                long tillTimestamp = c.getTimeInMillis();
                c.setTimeInMillis(joinTimestamp);
                long fromTimestamp = c.getTimeInMillis();

                SharedPreferences configPrefs = requireActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                final int SUBMITTED = configPrefs.getInt("SELF_STRESS_REPORT", -1), PREDICTION = configPrefs.getInt("STRESS_PREDICTION", -1), SURVEY_EMA = configPrefs.getInt("SURVEY_EMA",-1);
                synchronized (this){
                    stressLevels.clear();
                }
                for (int dataSourceId : new int[]{SUBMITTED, PREDICTION, SURVEY_EMA}) {
                    EtService.RetrieveFilteredDataRecords.Request requestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                            .setUserId(userId)
                            .setEmail(email)
                            .setTargetEmail(email)
                            .setTargetCampaignId(campaignId)
                            .setTargetDataSourceId(dataSourceId)
                            .setFromTimestamp(fromTimestamp)
                            .setTillTimestamp(tillTimestamp)
                            .build();
                    try {
                        EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                        Log.d("testt", responseMessage.toString());
                        if (responseMessage.getSuccess()) {
                            // checkByteString
                            List<ByteString> values = responseMessage.getValueList();
                            List<Long> timestamps = responseMessage.getTimestampList();
                            Log.d(TAG,values.toString());
                            for (int n = 0; n < values.size(); n++) {
                                String value = null;
                                //@jeongin : 마음기록 제대로 안 보이는 문제 수정 20.12.29
//                                if(dataSourceId == SUBMITTED){
//                                    value = values.get(n).toString("UTF-8");
//                                }else{
//                                    value = values.get(n).substring(1, values.get(n).size()-1).toString("UTF-8");
//                                }

                                value = values.get(n).toString("UTF-8");

                                int dayNum = 0;
                                long timestamp = 0;
                                int emaOrder = -1;
                                int stressLevel = 0;
                                if (dataSourceId == SUBMITTED) {
                                    // self-report
                                    String[] cells = value.split(" ");
                                    if (cells.length != 5)
                                        continue;
                                    dayNum = Integer.parseInt(cells[1]);
                                    if (dayNum == 0)
                                        continue;
                                    emaOrder = Integer.parseInt(cells[2]);
                                    timestamp = fixTimestamp(Long.parseLong(cells[0]), emaOrder);
                                    stressLevel = Integer.parseInt(cells[4]);

                                    Log.d("testt", value);

                                } else if(dataSourceId == PREDICTION){
                                    // prediction
                                    try {
                                        JSONObject cells = new JSONObject(value);  
                                        timestamp = fixTimestamp(timestamps.get(n), cells.getJSONObject("1").getInt("ema_order"));
                                        c.setTimeInMillis(timestamp);
                                        timestampStressFeaturesMap.put(c.getTimeInMillis(), cells);
                                        Iterator<String> keys = cells.keys();
                                        do {
                                            String key = keys.next();
                                            if (cells.getJSONObject(key).getBoolean("model_tag"))
                                                stressLevel = Integer.parseInt(key);
                                        }
                                        while (keys.hasNext());
                                    } catch (JSONException e) {
                                        continue;
                                    }
                                }
                                else //step1때 survey_ema 가져오기위해 추가 //2021.1.9 준영
                                {
                                    String[] cells = value.split(" ");
                                    if (cells.length != 7)
                                        continue;
                                    emaOrder = Integer.parseInt(cells[1]);
                                    timestamp = fixTimestamp(Long.parseLong(cells[0]),emaOrder);
                                    stressLevel = Integer.parseInt(cells[6]);
                                }
                                if (stressLevel < 3) {
                                    c.setTimeInMillis(timestamp);
                                    c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), 0, 0);
                                    c.set(Calendar.MILLISECOND, 0);
                                    timestamp = c.getTimeInMillis();

                                    synchronized (this){
                                        if (!stressLevels.containsKey(timestamp))
                                            stressLevels.put(timestamp, new Pair<>(emaOrder, stressLevel));
                                    }
                                }
                            }
                        }
                    } catch (StatusRuntimeException | IOException e) {
                        e.printStackTrace();
                    }
                }
                channel.shutdown();
                applyStressLevelUI();
            }).start();
        }
    }

    private int getEmaOrderHour(int emaOrder) {
        switch (emaOrder) {
            case 1:
                return 11;
            case 2:
                return 15;
            case 3:
                return 19;
            case 4:
                return 23;
            default:
                return -1;
        }
    }

    private long fixTimestamp(long timestamp, int emaOrder) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        if(c.get(Calendar.HOUR_OF_DAY) < timeTheDayNumIsChanged){
            c.add(Calendar.DATE, -1);
        }
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), getEmaOrderHour(emaOrder), 0, 0);
        return c.getTimeInMillis();
    }

    private void applyStressLevelUI() {
        // [Kevin]
        // (1) distribute stress levels to days (day => cluster of stress levels)
        dailyStressLevelClusters.clear();
        for (long timestamp : stressLevels.keySet()) {
            Pair<Integer, Integer> emaOrderStressLevelPair = stressLevels.get(timestamp);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timestamp);
            CalendarDay day = CalendarDay.from(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
            if (dailyStressLevelClusters.containsKey(day)) {
                assert emaOrderStressLevelPair != null;
                dailyStressLevelClusters.get(day).add(Triple.of(timestamp, emaOrderStressLevelPair.first, emaOrderStressLevelPair.second));
            }
            else {
                ArrayList<Triple<Long, Integer, Integer>> stressLevels = new ArrayList<>();
                assert emaOrderStressLevelPair != null;
                stressLevels.add(Triple.of(timestamp, emaOrderStressLevelPair.first, emaOrderStressLevelPair.second));
                dailyStressLevelClusters.put(day, stressLevels);
            }
        }
        // (2) calculate average stress level per day
        dailyAverageStressLevels.clear();
        for (CalendarDay day : dailyStressLevelClusters.keySet()) {
            ArrayList<Triple<Long, Integer, Integer>> cluster = dailyStressLevelClusters.get(day);
            double sum = 0;
            assert cluster != null;
            for (Triple<Long, Integer, Integer> tsEmaOrderStressLvlTriple : cluster)
                sum += tsEmaOrderStressLvlTriple.getRight() + 1;
            float avgStress = (float) (sum / cluster.size());
            if (avgStress < 1.5) {
                dailyAverageStressLevels.put(day, STRESS_LV1);
            } else if (avgStress < 2.5) {
                dailyAverageStressLevels.put(day, STRESS_LV2);
            } else {
                dailyAverageStressLevels.put(day, STRESS_LV3);
            }
        }

        // (3) decorate days with calculated average stress levels
        LowStressDecorator.clearLowStressCalendarDays();
        LittleHighStressDecorator.clearLittleHighStressCalendarDays();
        HighStressDecorator.clearHighStressCalendarDays();
        for (CalendarDay day : dailyAverageStressLevels.keySet()) {
            int stressLevel = dailyAverageStressLevels.get(day);
            if (stressLevel == STRESS_LV1)
                LowStressDecorator.addLowStressCalendarDay(day);
            else if (stressLevel == STRESS_LV2)
                LittleHighStressDecorator.addLittleHighStressCalendarDay(day);
            else if (stressLevel == STRESS_LV3)
                HighStressDecorator.addHighStressCalendarDay(day);
        }

        // (4) make changes on UI
        if(isAdded()){
            requireActivity().runOnUiThread(() -> {
                materialCalendarView.invalidateDecorators();
                materialCalendarView.setSelectedDate(CalendarDay.today());
            });
        }
    }

    private boolean isSubmission(long timestamp) {
        return Objects.requireNonNull(stressLevels.get(timestamp)).first != -1;
    }
    
    
    //@jeongin : step1 일때 태그 보여주는 상세 화면 업데이트  21.01.13
    public void hiddenViewUpdateStep1(int stressLevl,int order){
        Context context = requireContext();

        ConstraintLayout.LayoutParams layoutParams =
                new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
        layoutParams.topToBottom  = hiddenStressLevelView.getId();

        switch (stressLevl) {
            case STRESS_LV1:
                hiddenStressImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                hiddenStressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low)));
                reasonContainer.setBackgroundColor(context.getResources().getColor(R.color.color_low_bg, context.getTheme()));
                break;
            case STRESS_LV2:
                hiddenStressImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                hiddenStressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_littlehigh)));
                reasonContainer.setBackgroundColor(context.getResources().getColor(R.color.color_littlehigh_bg, context.getTheme()));
                break;
            case STRESS_LV3:
                hiddenStressImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                hiddenStressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_high)));
                reasonContainer.setBackgroundColor(context.getResources().getColor(R.color.color_high_bg, context.getTheme()));
                break;
        }

        condition3_container.setVisibility(View.VISIBLE);
        condition2Container.setVisibility(View.GONE);
        integrateContainer.setVisibility(View.GONE);

        layoutParams.bottomToTop = condition3_container.getId();
        txtReason.setLayoutParams(layoutParams);
        txtReason.setText("나의 태그");
        txtReason.setVisibility(View.VISIBLE);

        Log.d(TAG,"아니"+selectedDailyTags.toString());
        Log.d(TAG,"아니"+selectedDailyTags.get((long)order));
        tagsTextview.setText((selectedDailyTags.get((long)order)==null)? "태그가 없어요" : selectedDailyTags.get((long)order));
        tagsTextview.setVisibility(View.VISIBLE);
    }
    //@jeongin: step2일때 스트레스 레벨별 상세화면 업데이트 20.12.26
    public void hiddenViewUpdateStep2(String feature_ids, int stressLevl) {
        Context context = requireContext();

        // 아래 도움말은 child1 기준 약간씩 다름
        //  조건 1, 2, 3 업데이트 해야됨. MeFragmentStep2.java 의 featureViewUpdate()함수 참고  (완)
        //  fragment_care_child3.xml을 fragment_me_step2의 condition UI 참고해서 수정 (완)
        // child1_container1 은 시간이랑 체크박스 있는 UI
        // child1_container2 는 condition3의 UI -> fragment_me_step2.xml의 stress_reason_container 참고해서 수정
        // frg_me_step2.xml 파일의 me_condition2_container 참고해서 condition2의 UI 구성

        //@jeongin : 20.12.26 featureViewUpdate()내용으로 아래 수정

        ArrayList<String> integrateReason = new ArrayList<>();
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
                    String text = String.format(context.getResources().getString(resId), applicationName);
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




        // 스트레스 리스트뷰랑 엮여지는 어댑터
        ArrayAdapter<String> integrateAdapter = new ArrayAdapter<>(
                context, R.layout.item_feature_ids, integrateReason
        );


        if (noFeatures) {
           integrateContainer.setVisibility(View.GONE);
            condition2Container.setVisibility(View.GONE);
            noFeatureTextview.setVisibility(View.VISIBLE);
        } else {
            integrateListView.setAdapter(integrateAdapter);
            condition2Container.setVisibility(View.VISIBLE);
            noFeatureTextview.setVisibility(View.GONE);

            if (integrateReason.isEmpty()) {
              integrateContainer.setVisibility(View.GONE);
            }
            else {
                setListViewHeightBasedOnChildren(integrateListView);
                integrateContainer.setVisibility(View.VISIBLE);
            }

        }

        ConstraintLayout.LayoutParams layoutParams =
                new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
        layoutParams.topToBottom  = hiddenStressLevelView.getId();

        switch (stressLevl) {

            case STRESS_LV1:
                hiddenStressImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_low));
                hiddenStressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low)));
                reasonContainer.setBackgroundColor(context.getResources().getColor(R.color.color_low_bg, context.getTheme()));
                condition3_container.setVisibility(View.VISIBLE);
                condition2Container.setVisibility(View.GONE);
                txtReason.setText(" ");
                break;
            case STRESS_LV2:
                hiddenStressImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_littlehigh));
                hiddenStressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_littlehigh)));
                condition2Layout.setBackgroundColor(context.getResources().getColor(R.color.white, context.getTheme()));
                condition2Container.setVisibility(View.VISIBLE);

                condition3_container.setVisibility(View.GONE);
                txtReason.setVisibility(View.VISIBLE);
                layoutParams.bottomToTop = condition2Container.getId();
                txtReason.setLayoutParams(layoutParams);
                txtReason.setText("제가 참고한 데이터는요,");
                break;
            case STRESS_LV3:
                hiddenStressImg.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.icon_high));
                hiddenStressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_high)));
                reasonContainer.setBackgroundColor(context.getResources().getColor(R.color.color_high_bg, context.getTheme()));
                condition3_container.setVisibility(View.VISIBLE);
                condition2Container.setVisibility(View.GONE);
                txtReason.setVisibility(View.VISIBLE);
                layoutParams.bottomToTop = condition3_container.getId();
                txtReason.setLayoutParams(layoutParams);
                txtReason.setText("당신은 스트레스가 높을 때,");
                break;
        }

    }

    // region decorators
    public static class LowStressDecorator implements DayViewDecorator {
        private static HashSet<CalendarDay> lowStressCalendarDays;
        private final Drawable lowBackgroundDrawable;

        public LowStressDecorator(Drawable stressLevelDrawable) {
            this.lowBackgroundDrawable = stressLevelDrawable;
            if (lowStressCalendarDays == null)
                lowStressCalendarDays = new HashSet<>();
        }

        public static void clearLowStressCalendarDays() {
            lowStressCalendarDays.clear();
        }

        public static void addLowStressCalendarDay(CalendarDay day) {
            lowStressCalendarDays.add(day);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !materialCalendarView.getSelectedDate().equals(day) && lowStressCalendarDays.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(lowBackgroundDrawable);
        }
    }

    public static class LowEMADecorator implements DayViewDecorator {
        private static HashSet<CalendarDay> lowEMACalendarDays;
        private final Drawable lowEMABackgroundDrawable;

        public LowEMADecorator(Drawable stressLevelDrawable) {
            this.lowEMABackgroundDrawable = stressLevelDrawable;
            if (lowEMACalendarDays == null)
                lowEMACalendarDays = new HashSet<>();
        }

        public static void clearLowEMACalendarDays() {
            lowEMACalendarDays.clear();
        }

        public static void addLowStressCalendarDay(CalendarDay day) {
            lowEMACalendarDays.add(day);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !materialCalendarView.getSelectedDate().equals(day) && lowEMACalendarDays.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(lowEMABackgroundDrawable);
        }
    }

    public static class LittleHighStressDecorator implements DayViewDecorator {
        private static HashSet<CalendarDay> littleHighStressCalendarDays;
        private final Drawable littleHighBackgroundDrawable;

        public LittleHighStressDecorator(Drawable stressLevelDrawable) {
            this.littleHighBackgroundDrawable = stressLevelDrawable;
            if (littleHighStressCalendarDays == null)
                littleHighStressCalendarDays = new HashSet<>();
        }

        public static void clearLittleHighStressCalendarDays() {
            littleHighStressCalendarDays.clear();
        }

        public static void addLittleHighStressCalendarDay(CalendarDay day) {
            littleHighStressCalendarDays.add(day);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !materialCalendarView.getSelectedDate().equals(day) && littleHighStressCalendarDays.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(littleHighBackgroundDrawable);
        }
    }

    public static class LittleHighEMADecorator implements DayViewDecorator {
        private static HashSet<CalendarDay> littleHighEMACalendarDays;
        private final Drawable littleHighEMABackgroundDrawable;

        public LittleHighEMADecorator(Drawable stressLevelDrawable) {
            this.littleHighEMABackgroundDrawable = stressLevelDrawable;
            if (littleHighEMACalendarDays == null)
                littleHighEMACalendarDays = new HashSet<>();
        }

        public static void clearLittleHighEMACalendarDays() {
            littleHighEMACalendarDays.clear();
        }

        public static void addLittleHighStressCalendarDay(CalendarDay day) {
            littleHighEMACalendarDays.add(day);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !materialCalendarView.getSelectedDate().equals(day) && littleHighEMACalendarDays.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(littleHighEMABackgroundDrawable);
        }
    }

    public static class HighStressDecorator implements DayViewDecorator {
        private static HashSet<CalendarDay> highStressCalendarDays;
        private final Drawable highBackgroundDrawable;

        public HighStressDecorator(Drawable stressLevelDrawable) {
            this.highBackgroundDrawable = stressLevelDrawable;
            if (highStressCalendarDays == null)
                highStressCalendarDays = new HashSet<>();
        }

        public static void clearHighStressCalendarDays() {
            highStressCalendarDays.clear();
        }

        public static void addHighStressCalendarDay(CalendarDay day) {
            highStressCalendarDays.add(day);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !materialCalendarView.getSelectedDate().equals(day) && highStressCalendarDays.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(highBackgroundDrawable);
        }
    }

    public static class HighEMADecorator implements DayViewDecorator {
        private static HashSet<CalendarDay> highEMACalendarDays;
        private final Drawable highEMABackgroundDrawable;

        public HighEMADecorator(Drawable stressLevelDrawable) {
            this.highEMABackgroundDrawable = stressLevelDrawable;
            if (highEMACalendarDays == null)
                highEMACalendarDays = new HashSet<>();
        }

        public static void clearHighStressCalendarDays() {
            highEMACalendarDays.clear();
        }

        public static void addHighStressCalendarDay(CalendarDay day) {
            highEMACalendarDays.add(day);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !materialCalendarView.getSelectedDate().equals(day) && highEMACalendarDays.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(highEMABackgroundDrawable);
        }
    }

    public class DayDecorator implements DayViewDecorator {
        private final CalendarDay today;
        private final Drawable todayBackgroundDrawable;

        public DayDecorator() {
            today = CalendarDay.today();
            todayBackgroundDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.today_circle_background);
        }


        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return today.equals(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(todayBackgroundDrawable);
        }
    }
    // endregion
}
