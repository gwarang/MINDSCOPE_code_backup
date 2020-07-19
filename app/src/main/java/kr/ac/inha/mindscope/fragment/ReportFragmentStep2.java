package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONException;
import org.json.JSONObject;

import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.StressReportDBHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportFragmentStep2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportFragmentStep2 extends Fragment implements OnDateSelectedListener {
    // region variables
    private static final String TAG = "ReportFragmentStep2";

    private static final int tileWidth = 45;
    private static final int tileHeight = 45;
    private long joinTimestamp;

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
    TextView selectedDayComment;


    StressReportDBHelper dbHelper;
    ArrayList<StressReportDBHelper.StressReportData> dataArray;
    private static MaterialCalendarView materialCalendarView;

    static HashMap<Long, Pair<Integer, Integer>> stressLevels = new HashMap<>();
    static HashMap<CalendarDay, Integer> dailyAverageStressLevels = new HashMap<>();
    static HashMap<CalendarDay, ArrayList<Triple<Long, Integer, Integer>>> dailyStressLevelClusters = new HashMap<>();
    // endregion

    public ReportFragmentStep2() {
        // Required empty public constructor
    }

    // region override
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new StressReportDBHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_report_step2, container, false);
        joinTimestamp = getJoinTime();
        Log.e(TAG, "join timestamp " + joinTimestamp);

        Calendar firstDayCal = Calendar.getInstance(Locale.KOREA);
        firstDayCal.setTimeInMillis(joinTimestamp);
        String date = DateFormat.format("yyyy-MM-dd", firstDayCal).toString();

        Log.d(TAG, "firstdate calendar " + date);
        dataArray = dbHelper.getStressReportData();
        if (dataArray.isEmpty())
            Log.i(TAG, "dataArray is empty");
        for (StressReportDBHelper.StressReportData temp : dataArray) {
            Log.i(TAG, temp.toString());
            long timestamp = temp.getTimestamp();
            int stress_level = temp.getStress_level();
            int day_num = temp.getDay_num();
            int report_order = temp.getReport_order();

            Calendar tempCal = Calendar.getInstance();
            tempCal.setTimeInMillis(timestamp);
            String tempDate = DateFormat.format("yyyy-MM-dd", tempCal).toString();
            Log.d(TAG, "temp date: " + tempDate);


        }

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
        selectedDayComment = root.findViewById(R.id.selected_day_comment);
        dailyAverageStressLevelView = root.findViewById(R.id.frg_report_step2_img1);
        SharedPreferences prefs = getContext().getSharedPreferences("points", Context.MODE_PRIVATE);
        sumPointsView.setText(String.valueOf(prefs.getInt("sumPoints", 0)));

        txtStressLevel = root.findViewById(R.id.txt_stress_level);
        // TODO 달력에서 날짜 선택하면 해당 날짜 평균 스트레스 레벨로 바꿔줘야함, 캘린더 클릭 이벤트에서 처리할것
        txtStressLevel.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low)));

        materialCalendarView = (MaterialCalendarView) root.findViewById(R.id.calendarView);
        materialCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE);
        materialCalendarView.setTileHeightDp(tileHeight);
        materialCalendarView.setTileWidthDp(tileWidth);
        materialCalendarView.addDecorators(
                new DayDecorator(),
                new LowStressDecorator(ContextCompat.getDrawable(getContext(), R.drawable.low_circle_background)),
                new LittleHighStressDecorator(ContextCompat.getDrawable(getContext(), R.drawable.littlehigh_circle_background)),
                new HighStressDecorator(ContextCompat.getDrawable(getContext(), R.drawable.high_circle_background))
        );
        materialCalendarView.setOnDateChangedListener(this);

        dateView = (TextView) root.findViewById(R.id.report_step2_date);
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);

        loadAllStressLevelsFromServer();
        loadAllPoints();
        materialCalendarView.setSelectedDate(CalendarDay.today());
        loadDailyPoints(CalendarDay.today());
        onDateSelected(materialCalendarView, CalendarDay.today(), true);
        return root;
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay day, boolean selected) {
        // [Kevin] 선택된 날짜의 stress 보고서 보여주기
        Log.i(TAG, "Seleted Date: " + day);

        // region (1) daily points
        loadDailyPoints(day);
        // endregion

        // region (2) average daily stress level
        int[] stressLvlImageResources = new int[]{
                R.drawable.icon_low,
                R.drawable.icon_littlehigh,
                R.drawable.icon_high
        };
        if (dailyAverageStressLevels.containsKey(day)) {
            dailyAverageStressLevelView.setImageResource(stressLvlImageResources[dailyAverageStressLevels.get(day) - 1]);
            dailyAverageStressLevelView.setVisibility(View.VISIBLE);
        } else
            dailyAverageStressLevelView.setVisibility(View.GONE);
        // endregion

        // region (3) detailed view
        CheckBox[] checkBoxes = new CheckBox[]{checkBoxEmaOrder1, checkBoxEmaOrder2, checkBoxEmaOrder3, checkBoxEmaOrder4};
        ImageView[] imageViews = new ImageView[]{imageViewSlot1, imageViewSlot2, imageViewSlot3, imageViewSlot4};
        TextView[] textViews = new TextView[]{textViewSlot1, textViewSlot2, textViewSlot3, textViewSlot4};
        int[] stressLevelTexts = new int[]{R.string.string_low, R.string.string_littlehigh, R.string.string_high};
        int[] stressLevelImageResources = new int[]{R.drawable.icon_low, R.drawable.icon_littlehigh, R.drawable.icon_high};
        Pair<Integer, Integer>[] ranges = new Pair[]{new Pair<>(700, 1100), new Pair<>(1100, 1500), new Pair<>(1500, 1900), new Pair<>(1900, 2300)};
        for (CheckBox checkBox : checkBoxes)
            checkBox.setChecked(false);
        for (ImageView imageView : imageViews)
            imageView.setVisibility(View.INVISIBLE);
        for (TextView textView : textViews)
            textView.setVisibility(View.INVISIBLE);
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
                    textViews[emaOrder - 1].setText(stressLevelTexts[stressLevel - 1]);

                    imageViews[emaOrder - 1].setVisibility(View.VISIBLE);
                    imageViews[emaOrder - 1].setImageResource(stressLevelImageResources[stressLevel - 1]);
                } else {
                    // detection
                    int index = findTimestampIndex(timestamp, ranges);
                    if (index > -1 && index < imageViews.length) {
                        textViews[index].setVisibility(View.VISIBLE);
                        textViews[index].setText(stressLevelTexts[stressLevel - 1]);

                        imageViews[index].setVisibility(View.VISIBLE);
                        imageViews[index].setImageResource(stressLevelImageResources[stressLevel - 1]);
                    }
                }
                // endregion
            }
        } else
            for (CheckBox checkBox : checkBoxes)
                checkBox.setChecked(false);
        // endregion

        // region (4) daily comment
        new Thread(() -> {
            SharedPreferences loginPrefs = getContext().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
            int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
            String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
            int campaignId = Integer.parseInt(getContext().getString(R.string.stress_campaign_id));
            final int DAILY_COMMENT = 84;

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
        // endregion

        materialCalendarView.invalidateDecorators();
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

    // region decorators
    public class DayDecorator implements DayViewDecorator {
        private final CalendarDay today;
        private final Drawable todayBackgroundDrawable;

        public DayDecorator() {
            today = CalendarDay.today();
            todayBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.today_circle_background);
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

    public static class LowStressDecorator implements DayViewDecorator {
        private final CalendarDay today;
        private final Drawable lowBackgroundDrawable;
        private static HashSet<CalendarDay> lowStressCalendarDays;

        public LowStressDecorator(Drawable stressLevelDrawable) {
            this.today = CalendarDay.today();
            this.lowBackgroundDrawable = stressLevelDrawable;
            if (lowStressCalendarDays == null)
                lowStressCalendarDays = new HashSet<>();
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !materialCalendarView.getSelectedDate().equals(day) && lowStressCalendarDays.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(lowBackgroundDrawable);
        }

        public static void clearLowStressCalendarDays() {
            lowStressCalendarDays.clear();
        }

        public static void addLowStressCalendarDay(CalendarDay day) {
            lowStressCalendarDays.add(day);
        }
    }

    public static class LittleHighStressDecorator implements DayViewDecorator {
        private final CalendarDay today;
        private final Drawable littleHighBackgroundDrawable;
        private static HashSet<CalendarDay> littleHighStressCalendarDays;

        public LittleHighStressDecorator(Drawable stressLevelDrawable) {
            this.today = CalendarDay.today();
            this.littleHighBackgroundDrawable = stressLevelDrawable;
            if (littleHighStressCalendarDays == null)
                littleHighStressCalendarDays = new HashSet<>();
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !materialCalendarView.getSelectedDate().equals(day) && littleHighStressCalendarDays.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(littleHighBackgroundDrawable);
        }

        public static void clearLittleHighStressCalendarDays() {
            littleHighStressCalendarDays.clear();
        }

        public static void addLittleHighStressCalendarDay(CalendarDay day) {
            littleHighStressCalendarDays.add(day);
        }
    }

    public static class HighStressDecorator implements DayViewDecorator {
        private final CalendarDay today;
        private final Drawable highBackgroundDrawable;
        private static HashSet<CalendarDay> highStressCalendarDays;

        public HighStressDecorator(Drawable stressLevelDrawable) {
            this.today = CalendarDay.today();
            this.highBackgroundDrawable = stressLevelDrawable;
            if (highStressCalendarDays == null)
                highStressCalendarDays = new HashSet<>();
        }


        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return !materialCalendarView.getSelectedDate().equals(day) && highStressCalendarDays.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(highBackgroundDrawable);
        }

        public static void clearHighStressCalendarDays() {
            highStressCalendarDays.clear();
        }

        public static void addHighStressCalendarDay(CalendarDay day) {
            highStressCalendarDays.add(day);
        }
    }
    // endregion

    // region utility functions
    public static ReportFragmentStep2 newInstance() {
        ReportFragmentStep2 fragment = new ReportFragmentStep2();
        return fragment;
    }

    public long getJoinTime() {
        long firstDayTimestamp = 0;
        SharedPreferences loginPrefs = getActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);

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
            long joinTimestamp = responseMessage.getCampaignJoinTimestamp();
            firstDayTimestamp = joinTimestamp;
        }
        channel.shutdown();
        return firstDayTimestamp;
    }

    public void loadAllPoints() {
        new Thread(() -> {
            SharedPreferences loginPrefs = getContext().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
            int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
            String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
            int campaignId = Integer.parseInt(getContext().getString(R.string.stress_campaign_id));
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

    public void loadDailyPoints(CalendarDay day) {
        new Thread(() -> {
            SharedPreferences loginPrefs = getContext().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
            int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
            String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
            int campaignId = Integer.parseInt(getContext().getString(R.string.stress_campaign_id));
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

    public void loadAllStressLevelsFromServer() {
        // [Kevin] load stress levels (submissions & predictions) from gRPC server
        new Thread(() -> {
            SharedPreferences loginPrefs = getContext().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
            int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
            String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
            int campaignId = Integer.parseInt(getContext().getString(R.string.stress_campaign_id));

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
                        long timestamp = 0;
                        int emaOrder = -1;
                        int stressLevel = 0;
                        if (dataSourceId == SUBMITTED) {
                            // self-report
                            String[] cells = value.split(" ");
                            if (cells.length != 5)
                                continue;
                            timestamp = Long.parseLong(cells[0]);
                            emaOrder = Integer.parseInt(cells[2]);
                            stressLevel = Integer.parseInt(cells[4]);
                        } else {
                            // prediction
                            try {
                                JSONObject cells = new JSONObject(value);
                                Iterator<String> keys = cells.keys();
                                do {
                                    String key = keys.next();
                                    if (cells.getJSONObject(key).getBoolean("model_tag")) {
                                        timestamp = timestamps.get(n);
                                        stressLevel = Integer.parseInt(key);
                                        break;
                                    }
                                }
                                while (keys.hasNext());
                            } catch (JSONException e) {
                                continue;
                            }
                        }
                        if (timestamp + stressLevel > 0) {
                            c.setTimeInMillis(timestamp);
                            c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), 0);
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
                sum += tsEmaOrderStressLvlTriple.getRight();
            dailyAverageStressLevels.put(day, (int) Math.round(sum / cluster.size()));
        }

        // (3) decorate days with calculated average stress levels
        LowStressDecorator.clearLowStressCalendarDays();
        LittleHighStressDecorator.clearLittleHighStressCalendarDays();
        HighStressDecorator.clearHighStressCalendarDays();
        for (CalendarDay day : dailyAverageStressLevels.keySet()) {
            int stressLevel = dailyAverageStressLevels.get(day);
            if (stressLevel == 1)
                LowStressDecorator.addLowStressCalendarDay(day);
            else if (stressLevel == 2)
                LittleHighStressDecorator.addLittleHighStressCalendarDay(day);
            else if (stressLevel == 3)
                HighStressDecorator.addHighStressCalendarDay(day);
        }

        // (4) make changes on UI
        requireActivity().runOnUiThread(() -> {
            materialCalendarView.invalidateDecorators();
            materialCalendarView.setSelectedDate(CalendarDay.today());
        });
    }
    // endregion
}
