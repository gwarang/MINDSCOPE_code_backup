package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.StressReportDBHelper;
import kr.ac.inha.mindscope.Tools;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReportFragmentStep2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportFragmentStep2 extends Fragment implements OnDateSelectedListener {

    private static final String TAG = "ReportFragmentStep2";

    private static final int tileWidth = 45;
    private static final int tileHeight = 45;

    TextView dateView;
    TextView txtStressLevel;
    TextView sumPointsView;

    StressReportDBHelper dbHelper;
    ArrayList<StressReportDBHelper.StressReportData> dataArray;

    private MaterialCalendarView materialCalendarView;

    public ReportFragmentStep2() {
        // Required empty public constructor
    }

    public static ReportFragmentStep2 newInstance() {
        ReportFragmentStep2 fragment = new ReportFragmentStep2();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new StressReportDBHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_report_step2, container, false);
        long firstDayTimestamp = getJoinTime();
        Log.e(TAG, "join timestamp " + firstDayTimestamp);

        Calendar firstDayCal = Calendar.getInstance(Locale.KOREA);
        firstDayCal.setTimeInMillis(firstDayTimestamp);
        String date = DateFormat.format("yyyy-MM-dd", firstDayCal).toString();

        Log.d(TAG, "firstdate calendar " + date);
        dataArray = dbHelper.getStressReportData();
        if(dataArray.isEmpty())
            Log.i(TAG, "dataArray is empty");
        for(StressReportDBHelper.StressReportData temp : dataArray){
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
        SharedPreferences prefs = getContext().getSharedPreferences("points", Context.MODE_PRIVATE);
        sumPointsView.setText(String.valueOf(prefs.getInt("sumPoints", 0)));

        txtStressLevel = root.findViewById(R.id.txt_stress_level);
        // TODO 달력에서 날짜 선택하면 해당 날짜 평균 스트레스 레벨로 바꿔줘야함, 캘린더 클릭 이벤트에서 처리할것
        txtStressLevel.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low)));

        materialCalendarView = (MaterialCalendarView) root.findViewById(R.id.calendarView);
        Calendar cal = Calendar.getInstance();
        materialCalendarView.setSelectedDate(CalendarDay.from(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)));
        materialCalendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_MULTIPLE);
        materialCalendarView.setTileHeightDp(tileHeight);
        materialCalendarView.setTileWidthDp(tileWidth);
        materialCalendarView.addDecorators(new DayDecorator(), new LowStressDecorator(), new HighStressDecorator(), new LittleHighStressDecorator());
        materialCalendarView.setOnDateChangedListener(this);


        dateView = (TextView) root.findViewById(R.id.report_step2_date);
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);

        return root;
    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        Log.i(TAG, "Seleted Date: " + date);
        // TODO 선택된 날짜의 stress 보고서 보여주기
    }

    public class DayDecorator implements DayViewDecorator{
        private final CalendarDay today;
        private final Drawable todayBackgroundDrawable;

        public DayDecorator(){
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

    public class LowStressDecorator implements DayViewDecorator{
        private final CalendarDay today;
        private final Drawable lowBackgroundDrawable;

        public LowStressDecorator(){
            today = CalendarDay.today();
            lowBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.low_circle_background);
        }


        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return false;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(lowBackgroundDrawable);
        }
    }

    public class LittleHighStressDecorator implements DayViewDecorator{
        private final CalendarDay today;
        private final Drawable littleHighBackgroundDrawable;

        public LittleHighStressDecorator(){
            today = CalendarDay.today();
            littleHighBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.littlehigh_circle_background);
        }


        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return false;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(littleHighBackgroundDrawable);
        }
    }

    public class HighStressDecorator implements DayViewDecorator{
        private final CalendarDay today;
        private final Drawable highBackgroundDrawable;

        public HighStressDecorator(){
            today = CalendarDay.today();
            highBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.high_circle_background);
        }


        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return false;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(highBackgroundDrawable);
        }
    }

    public long getJoinTime(){
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
            final long join_timestamp = responseMessage.getCampaignJoinTimestamp();
            firstDayTimestamp = join_timestamp;
        }
        return  firstDayTimestamp;
    }
}



