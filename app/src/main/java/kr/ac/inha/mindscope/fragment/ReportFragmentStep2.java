package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
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
import kr.ac.inha.mindscope.Utils;

import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV1;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV2;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV3;
import static kr.ac.inha.mindscope.fragment.StressReportFragment2.setListViewHeightBasedOnChildren;
import static kr.ac.inha.mindscope.services.StressReportDownloader.SELF_STRESS_REPORT_RESULT;
import static kr.ac.inha.mindscope.services.StressReportDownloader.STRESS_PREDICTION_RESULT;

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
    static HashMap<Long, Pair<Integer, Integer>> stressLevels = new HashMap<>();
    static HashMap<CalendarDay, Integer> dailyAverageStressLevels = new HashMap<>();
    static HashMap<CalendarDay, ArrayList<Triple<Long, Integer, Integer>>> dailyStressLevelClusters = new HashMap<>();
    static HashMap<Long, JSONObject> timestampStressFeaturesMap = new HashMap<>();
    private static MaterialCalendarView materialCalendarView;
    ImageView fragmentMeStep2BtnHelp;
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
    ImageButton backArrow;
    ArrayList<String> predictionArray;
    ArrayList<String> selfReportArray;
    private long joinTimestamp;
    private CalendarDay selectedDay;
    private CalendarDay chooseDay;
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
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_report_step2, container, false);
        joinTimestamp = getJoinTime();
        Log.e(TAG, "join timestamp " + joinTimestamp);

        defaultContainer = root.findViewById(R.id.frg_report_step2_container1);
        dayDetailContainer = root.findViewById(R.id.frg_report_step2_date_container);

        Calendar firstDayCal = Calendar.getInstance(Locale.KOREA);
        firstDayCal.setTimeInMillis(joinTimestamp);
        String date = DateFormat.format("yyyy-MM-dd", firstDayCal).toString();

        Log.d(TAG, "firstdate calendar " + date);

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
        backArrow = root.findViewById(R.id.frg_report_step2_back_arrow);
        phoneListView = root.findViewById(R.id.frg_report_step2_listview_phone);
        activityListView = root.findViewById(R.id.frg_report_step2_listview_activity);
        socialListView = root.findViewById(R.id.frg_report_step2_listview_social);
        locationListView = root.findViewById(R.id.frg_report_step2_listview_location);
        sleepListView = root.findViewById(R.id.frg_report_step2_listview_sleep);
        phoneContainer = root.findViewById(R.id.frg_report_step2_listview_phone_container);
        activityContainer = root.findViewById(R.id.frg_report_step2_listview_activity_container);
        socialContainer = root.findViewById(R.id.frg_report_step2_listview_social_container);
        locationContainer = root.findViewById(R.id.frg_report_step2_listview_location_container);
        sleepContainer = root.findViewById(R.id.frg_report_step2_listview_sleep_container);

        reasonContainer = root.findViewById(R.id.frg_report_step2_stress_reason_container);

        txtStressLevel = root.findViewById(R.id.txt_stress_level);
        txtStressLevel.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low)));

        materialCalendarView = root.findViewById(R.id.calendarView);
        materialCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);
        materialCalendarView.setTileHeightDp(tileHeight);
        materialCalendarView.setTileWidthDp(tileWidth);
        materialCalendarView.addDecorators(
                new DayDecorator(),
                new LowStressDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.low_circle_background)),
                new LittleHighStressDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.littlehigh_circle_background)),
                new HighStressDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.high_circle_background))
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

        // set up per time-slot stress level button clicks
        arrowResult1.setOnClickListener((v) -> {
            // 07:00 ~ 11:00
            Calendar c = Calendar.getInstance();
            c.set(chooseDay.getYear(), chooseDay.getMonth() - 1, chooseDay.getDay(), 11, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            long timestamp = c.getTimeInMillis();

            String hiddenDateStr = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(timestamp);
            hiddenDateView.setText(hiddenDateStr + "의");
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration1));

            JSONObject object = timestampStressFeaturesMap.get(timestamp);
            // TODO HR
            int stressLv = stressLevels.get(timestamp).second;
            String feature_ids = null;
            if (object != null) {
                try {
                    feature_ids = object.getJSONObject(String.valueOf(stressLv)).getString("feature_ids");
                    Log.e(TAG, feature_ids);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (feature_ids != null) {
                defaultContainer.setVisibility(View.INVISIBLE);
                hiddenContainer.setVisibility(View.VISIBLE);
                hiddenViewUpdate(feature_ids, stressLv);
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.string_no_detail_report), Toast.LENGTH_LONG).show();
            }
            Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 1);
        });
        arrowResult2.setOnClickListener((v) -> {
            // 11:00 ~ 15:00
            Calendar c = Calendar.getInstance();
            c.set(selectedDay.getYear(), selectedDay.getMonth() - 1, selectedDay.getDay(), 15, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            long timestamp = c.getTimeInMillis();

            String hiddenDateStr = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(timestamp);
            hiddenDateView.setText(hiddenDateStr + "의");
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration2));

            JSONObject object = timestampStressFeaturesMap.get(timestamp);
            // TODO HR
            int stressLv = stressLevels.get(timestamp).second;
            String feature_ids = null;
            if (object != null) {
                try {
                    feature_ids = object.getJSONObject(String.valueOf(stressLv)).getString("feature_ids");
                    Log.e(TAG, feature_ids);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (feature_ids != null) {
                defaultContainer.setVisibility(View.INVISIBLE);
                hiddenContainer.setVisibility(View.VISIBLE);
                hiddenViewUpdate(feature_ids, stressLv);
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.string_no_detail_report), Toast.LENGTH_LONG).show();
            }
            Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 2);
        });
        arrowResult3.setOnClickListener((v) -> {
            // 15:00 ~ 19:00
            Calendar c = Calendar.getInstance();
            c.set(selectedDay.getYear(), selectedDay.getMonth() - 1, selectedDay.getDay(), 19, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            long timestamp = c.getTimeInMillis();

            String hiddenDateStr = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(timestamp);
            hiddenDateView.setText(hiddenDateStr + "의");
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration3));

            JSONObject object = timestampStressFeaturesMap.get(timestamp);
            // TODO HR
            int stressLv = stressLevels.get(timestamp).second;
            String feature_ids = null;
            if (object != null) {
                try {
                    feature_ids = object.getJSONObject(String.valueOf(stressLv)).getString("feature_ids");
                    Log.e(TAG, feature_ids);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (feature_ids != null) {
                defaultContainer.setVisibility(View.INVISIBLE);
                hiddenContainer.setVisibility(View.VISIBLE);
                hiddenViewUpdate(feature_ids, stressLv);
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.string_no_detail_report), Toast.LENGTH_LONG).show();
            }
            Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 3);
        });
        arrowResult4.setOnClickListener((v) -> {
            // 19:00 ~ 23:00
            Calendar c = Calendar.getInstance();
            c.set(selectedDay.getYear(), selectedDay.getMonth() - 1, selectedDay.getDay(), 23, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            long timestamp = c.getTimeInMillis();

            String hiddenDateStr = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(timestamp);
            hiddenDateView.setText(hiddenDateStr + "의");
            hiddenTimeView.setText(getResources().getString(R.string.time_step2_duration4));

            JSONObject object = timestampStressFeaturesMap.get(timestamp);
            // TODO HR
            int stressLv = stressLevels.get(timestamp).second;
            String feature_ids = null;
            if (object != null) {
                try {
                    feature_ids = object.getJSONObject(String.valueOf(stressLv)).getString("feature_ids");
                    Log.e(TAG, feature_ids);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (feature_ids != null) {
                defaultContainer.setVisibility(View.INVISIBLE);
                hiddenContainer.setVisibility(View.VISIBLE);
                hiddenViewUpdate(feature_ids, stressLv);
            } else {
                Toast.makeText(requireContext(), getResources().getString(R.string.string_no_detail_report), Toast.LENGTH_LONG).show();
            }
            Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_DETAIL_REPORT, 4);
        });
        backArrow.setOnClickListener(view -> {
            defaultContainer.setVisibility(View.VISIBLE);
            hiddenContainer.setVisibility(View.INVISIBLE);
        });

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
        Log.i(TAG, "Seleted Date: " + day);
        Calendar c = Calendar.getInstance();
        c.set(day.getYear(), day.getMonth() - 1, day.getDay(), 0, 0, 0);
        Date currentTime = c.getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);

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
                Utils.logThreadSignature(TAG + " loda daily comment");
                SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(requireContext().getString(R.string.stress_campaign_id));
                final int DAILY_COMMENT = 84;

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
                    requireActivity().runOnUiThread(() -> {
                        txtStressLevel.setText(Html.fromHtml(getResources().getString(stressLevelStrings[stressLevel])));
                        txtStressLevel.setVisibility(View.VISIBLE);
                    });
                } else
                    requireActivity().runOnUiThread(() -> {
                        txtStressLevel.setVisibility(View.GONE); // should never happen
                    });


                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                EtService.RetrieveFilteredDataRecordsRequestMessage requestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(DAILY_COMMENT)
                        .setFromTimestamp(fromTimestamp)
                        .setTillTimestamp(tillTimestamp)
                        .build();
                EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                long lastTimestamp = Long.MIN_VALUE;
                String lastComment = "";
                if (responseMessage.getDoneSuccessfully())
                    for (String value : responseMessage.getValueList()) {
                        String[] cells = value.split(" ");
                        long timestamp = Long.parseLong(value.substring(0, value.indexOf(' ')));
                        String comment = value.substring(value.indexOf(' ') + 1);

                        if (timestamp > lastTimestamp) {
                            lastTimestamp = timestamp;
                            lastComment = comment;
                        }
                    }
                channel.shutdown();

                final String finalComment = lastComment;
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


    // endregion

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
            EtService.RetrieveParticipantStatisticsRequestMessage retrieveParticipantStatisticsRequestMessage = EtService.RetrieveParticipantStatisticsRequestMessage.newBuilder()
                    .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                    .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                    .build();
            EtService.RetrieveParticipantStatisticsResponseMessage responseMessage = stub.retrieveParticipantStatistics(retrieveParticipantStatisticsRequestMessage);
            if (responseMessage.getDoneSuccessfully()) {
                firstDayTimestamp = responseMessage.getCampaignJoinTimestamp();
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
                final int REWARD_POINTS = 58;

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                Calendar c = Calendar.getInstance();
                EtService.RetrieveFilteredDataRecordsRequestMessage requestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(REWARD_POINTS)
                        .setFromTimestamp(0)
                        .setTillTimestamp(c.getTimeInMillis())
                        .build();
                EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                int points = 0;
                if (responseMessage.getDoneSuccessfully())
                    for (String value : responseMessage.getValueList()) {
                        String[] cells = value.split(" ");
                        if (cells.length != 3)
                            continue;
                        points += Integer.parseInt(cells[2]);
                    }
                channel.shutdown();
                final int finalPoints = points;
                requireActivity().runOnUiThread(() -> sumPointsView.setText(String.format(Locale.getDefault(), "%,d", finalPoints)));
            }).start();
        }
    }

    public void loadDailyPoints(CalendarDay day) {
        if (Tools.isNetworkAvailable()) {
            new Thread(() -> {
                SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(requireContext().getString(R.string.stress_campaign_id));
                final int REWARD_POINTS = 58;

                Calendar c = Calendar.getInstance();
                c.set(day.getYear(), day.getMonth() - 1, day.getDay(), 0, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                long fromTimestamp = c.getTimeInMillis();
                c.add(Calendar.DAY_OF_MONTH, 1);
                long tillTimestamp = c.getTimeInMillis();

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                EtService.RetrieveFilteredDataRecordsRequestMessage requestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(REWARD_POINTS)
                        .setFromTimestamp(fromTimestamp)
                        .setTillTimestamp(tillTimestamp)
                        .build();
                EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                int dailyPoints = 0;
                if (responseMessage.getDoneSuccessfully())
                    for (String value : responseMessage.getValueList()) {
                        String[] cells = value.split(" ");
                        if (cells.length != 3)
                            continue;
                        dailyPoints += Integer.parseInt(cells[2]);
                    }
                channel.shutdown();
                final int finalDailyPoints = dailyPoints;
                requireActivity().runOnUiThread(() -> dailyPointsView.setText(String.format(Locale.getDefault(), "%,d", finalDailyPoints)));
            }).start();
        }


    }
    // endregion

    public void loadAllStressLevels(Context context) throws IOException {
        FileInputStream fis = context.openFileInput(STRESS_PREDICTION_RESULT);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String predictionLine;

        FileInputStream fis2 = context.openFileInput(SELF_STRESS_REPORT_RESULT);
        InputStreamReader isr2 = new InputStreamReader(fis2);
        BufferedReader bufferedReader2 = new BufferedReader(isr2);
        String selfStressReportLine;

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        long tillTimestamp = c.getTimeInMillis();
        c.setTimeInMillis(joinTimestamp);
        long fromTimestamp = c.getTimeInMillis();

        stressLevels.clear();

        predictionArray = new ArrayList<>();
        selfReportArray = new ArrayList<>();

        //region load stress prediction
        while ((predictionLine = bufferedReader.readLine()) != null) {
            Log.i(TAG, "readStressReport test: " + predictionLine);
            String[] predictionTokens = predictionLine.split(",");
            long timestamp = Long.parseLong(predictionTokens[0]);

            if (fromTimestamp <= timestamp && timestamp <= tillTimestamp) {
                predictionArray.add(predictionLine);
            }
        }
        //endregion

        //region load self stress report
        while ((selfStressReportLine = bufferedReader2.readLine()) != null) {
            Log.i(TAG, "readStressReport test: " + selfStressReportLine);
            String[] selfReportTokens = selfStressReportLine.split(",");
            long timestamp = Long.parseLong(selfReportTokens[0]);

            if (fromTimestamp <= timestamp && timestamp <= tillTimestamp) {
                selfReportArray.add(selfStressReportLine);
            }
        }
        //endregion


//        for (int n = 0; n < selfReportArray.size(); n++) {
//            String value = selfReportArray.get(n);
//            long timestamp = 0;
//            int emaOrder = -1;
//            int stressLevel = 0;
//            // self-report
//            String[] cells = value.split(",");
//            if (cells.length != 5)
//                continue;
//            emaOrder = Integer.parseInt(cells[SELF_REPORT_ORDER_INDEX]);
//            timestamp = fixTimestamp(Long.parseLong(cells[SELF_REPORT_TIMESTAMP_INDEX]), emaOrder);
//            stressLevel = Integer.parseInt(cells[SELF_REPORT_ANSWER_INDEX]);
//            if (stressLevel < 3) {
//                c.setTimeInMillis(timestamp);
//                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), 0, 0);
//                c.set(Calendar.MILLISECOND, 0);
//                timestamp = c.getTimeInMillis();
//                if (!stressLevels.containsKey(timestamp))
//                    stressLevels.put(timestamp, new Pair<>(emaOrder, stressLevel));
//            }
//        }
//
//        for (int n = 0; n < predictionArray.size(); n++) {
//            String value = predictionArray.get(n);
//            long timestamp = 0;
//            int emaOrder = -1;
//            int stressLevel = 0;

//            // prediction
//            try {
//                JSONObject cells = new JSONObject(value);
//                timestamp = fixTimestamp(timestamps.get(n), cells.getJSONObject("1").getInt("ema_order"));
//                c.setTimeInMillis(timestamp);
//                timestampStressFeaturesMap.put(c.getTimeInMillis(), cells);
//
//                Iterator<String> keys = cells.keys();
//                do {
//                    String key = keys.next();
//                    if (cells.getJSONObject(key).getBoolean("model_tag"))
//                        stressLevel = Integer.parseInt(key);
//                }
//                while (keys.hasNext());
//            } catch (JSONException e) {
//                continue;
//            }
//
//            if (stressLevel < 3) {
//                c.setTimeInMillis(timestamp);
//                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), 0, 0);
//                c.set(Calendar.MILLISECOND, 0);
//                timestamp = c.getTimeInMillis();
//
//                if (!stressLevels.containsKey(timestamp))
//                    stressLevels.put(timestamp, new Pair<>(emaOrder, stressLevel));
//            }
//        }


    }

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

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                final int SUBMITTED = 55, PREDICTION = 56;
                stressLevels.clear();
                for (int dataSourceId : new int[]{SUBMITTED, PREDICTION}) {
                    EtService.RetrieveFilteredDataRecordsRequestMessage requestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                            .setUserId(userId)
                            .setEmail(email)
                            .setTargetEmail(email)
                            .setTargetCampaignId(campaignId)
                            .setTargetDataSourceId(dataSourceId)
                            .setFromTimestamp(fromTimestamp)
                            .setTillTimestamp(tillTimestamp)
                            .build();
                    EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                    if (responseMessage.getDoneSuccessfully()) {
                        List<String> values = responseMessage.getValueList();
                        List<Long> timestamps = responseMessage.getTimestampList();
                        for (int n = 0; n < values.size(); n++) {
                            String value = values.get(n);
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
                                if(dayNum == 0)
                                    continue;
                                emaOrder = Integer.parseInt(cells[2]);
                                timestamp = fixTimestamp(Long.parseLong(cells[0]), emaOrder);
                                stressLevel = Integer.parseInt(cells[4]);
                            } else {
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
                            if (stressLevel < 3) {
                                c.setTimeInMillis(timestamp);
                                c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), 0, 0);
                                c.set(Calendar.MILLISECOND, 0);
                                timestamp = c.getTimeInMillis();

                                if (!stressLevels.containsKey(timestamp))
                                    stressLevels.put(timestamp, new Pair<>(emaOrder, stressLevel));
                            }
                        }
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
            if (dailyStressLevelClusters.containsKey(day))
                dailyStressLevelClusters.get(day).add(Triple.of(timestamp, emaOrderStressLevelPair.first, emaOrderStressLevelPair.second));
            else {
                ArrayList<Triple<Long, Integer, Integer>> stressLevels = new ArrayList<>();
                stressLevels.add(Triple.of(timestamp, emaOrderStressLevelPair.first, emaOrderStressLevelPair.second));
                dailyStressLevelClusters.put(day, stressLevels);
            }
        }
        // (2) calculate average stress level per day
        dailyAverageStressLevels.clear();
        for (CalendarDay day : dailyStressLevelClusters.keySet()) {
            ArrayList<Triple<Long, Integer, Integer>> cluster = dailyStressLevelClusters.get(day);
            double sum = 0;
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
        requireActivity().runOnUiThread(() -> {
            materialCalendarView.invalidateDecorators();
            materialCalendarView.setSelectedDate(CalendarDay.today());
        });
    }

    private boolean isSubmission(long timestamp) {
        return stressLevels.get(timestamp).first != -1;
    }

    public void hiddenViewUpdate(String feature_ids, int stressLevl) {
        Context context = requireContext();

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
                String applicationName = "";

                if (category == 11 || (category >= 19 && category <= 28)) {
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

                String strID = "@string/feature_" + splitArray[0] + splitArray[1];
                String packName = MainActivity.getInstance().getPackageName();
                int resId = context.getResources().getIdentifier(strID, "string", packName);

                if (category <= 5) {
                    activityReason.add(context.getResources().getString(resId));
                } else if (category <= 10) {
                    socialReason.add(context.getResources().getString(resId));
                } else if (category == 11) {
                    String text = String.format(context.getResources().getString(resId), applicationName);
                    socialReason.add(text);
                } else if (category <= 16) {
                    locationReason.add(context.getResources().getString(resId));
                } else if (category <= 18) {
                    phoneReason.add(context.getResources().getString(resId));
                } else if (category <= 28) {
                    String text = String.format(context.getResources().getString(resId), applicationName);
                    phoneReason.add(text);
                } else {
                    sleepReason.add(context.getResources().getString(resId));
                }


                if (i == 4) // maximun number of showing feature is five
                    break;
            }
        }

        Log.d(TAG, "phoneReason" + phoneReason.toString());
        Log.d(TAG, "activityReason" + activityReason.toString());

        ArrayAdapter<String> phoneAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, phoneReason
        );
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, activityReason
        );
        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, socialReason
        );
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, locationReason
        );
        ArrayAdapter<String> sleepAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, sleepReason
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

    // region decorators
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
