package kr.ac.inha.mindscope.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.StressReportActivity;
import kr.ac.inha.mindscope.Tools;

import static kr.ac.inha.mindscope.StressReportActivity.REPORT_NOTIF_HOURS;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV1;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV2;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV3;
import static kr.ac.inha.mindscope.services.MainService.EMA_NOTIFICATION_ID;

public class StressReportFragment1 extends Fragment {



    private static final String TAG = "StressReportFragment1";

    public static JSONObject[] jsonObjects;
    public static final int REPORT_DURATION = 4;

    TextView dateView;
    TextView timeView;
    TextView stressLvView;

    ImageButton lowBtn;
    ImageButton littleHighBtn;
    ImageButton highBtn;

    ImageView stressLevelImg;
    TextView stressLevelTxt;

    Button btnReport;

    static int currentHours;
    public static int reportAnswer;
    private int reportOrder;
    private long reportDay;

    int day_num;
    int order;
    int accuracy;
    String feature_ids;

    public int stressLevel;

    long reportTimestamp;

    private static final int TIMESTAMP_END_INDEX = 12;
    private static final int RESULTS_START_INDEX = 14;
    private static final int RESULTS_INDICATOR_INDEX = 4;


    public StressReportFragment1() {
        // Required empty public constructor
    }


    public static StressReportFragment1 newInstance(int stressLevel){
        StressReportFragment1 fragment = new StressReportFragment1();
        Bundle bundle = new Bundle();
        bundle.putInt("stressLevel", stressLevel);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            this.stressLevel = getArguments().getInt("stressLevel");
            Log.i(TAG, "stress level from StressReportActivity: " + stressLevel); // TODO test용 추후 삭제
        }




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stress_report1, null);

        String stressReportStr =  gettingStressReportFromGRPC(); // get Stress Report Result from gRPC server;

        jsonObjects = Tools.parsingStressReport(stressReportStr);


        for(short i = 0; i < jsonObjects.length; i++){
            try {
                if(jsonObjects[i].getBoolean("model_tag")){
                    stressLevel = i+1;
                    day_num = jsonObjects[i].getInt("day_num");
                    order = jsonObjects[i].getInt("ema_order");
                    accuracy = jsonObjects[i].getInt("accuracy");
                    feature_ids = jsonObjects[i].getString("feature_ids");
                    SharedPreferences reportPrefs = getActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = reportPrefs.edit();
                    editor.putInt("result", stressLevel);
                    editor.apply();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        init(view);

        return view;
    }

    public void init(View view){


        Calendar cal = Calendar.getInstance();

        stressLevelImg = (ImageView) view.findViewById(R.id.report_step2_img);
        stressLevelTxt = (TextView) view.findViewById(R.id.report_step2_result);

        switch (stressLevel){
            case STRESS_LV1:
                stressLevelImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_low, getActivity().getTheme()));
                stressLevelTxt.setText(Html.fromHtml(getResources().getString(R.string.string_stress_report_low)));
                break;
            case STRESS_LV2:
                stressLevelImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_littlehigh, getActivity().getTheme()));
                stressLevelTxt.setText(Html.fromHtml(getResources().getString(R.string.string_stress_report_littlehigh)));
                break;
            case STRESS_LV3:
                stressLevelImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_high, getActivity().getTheme()));
                stressLevelTxt.setText(Html.fromHtml(getResources().getString(R.string.string_stress_report_high)));
        }

        reportAnswer = 0;

        Log.i(TAG, "Stress Report Order: " + reportOrder);

        // UI
        dateView = (TextView) view.findViewById(R.id.report_step2_date);
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);


        currentHours = cal.get(Calendar.HOUR_OF_DAY);
        Log.i(TAG, "current hours: " + currentHours);
        timeView = (TextView) view.findViewById(R.id.report_step2_time);
        if(currentHours < 11){
            timeView.setText(getResources().getString(R.string.time_step2_duration1));
        }
        else if(currentHours < 15){
            timeView.setText(getResources().getString(R.string.time_step2_duration2));
        }
        else if(currentHours < 19){
            timeView.setText(getResources().getString(R.string.time_step2_duration3));
        }
        else if(currentHours < 23){
            timeView.setText(getResources().getString(R.string.time_step2_duration4));
        }


        btnReport = view.findViewById(R.id.toolbar_report_btn);
        btnReport.setOnClickListener(clickListener);


        lowBtn = view.findViewById(R.id.actual_stress_answer1);
        littleHighBtn = view.findViewById(R.id.actual_stress_answer2);
        highBtn = view.findViewById(R.id.actual_stress_answer3);
        lowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lowBtn.setSelected(true);
                littleHighBtn.setSelected(false);
                highBtn.setSelected(false);
                reportAnswer = 1;
                btnReport.setClickable(true);
            }
        });
        littleHighBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lowBtn.setSelected(false);
                littleHighBtn.setSelected(true);
                highBtn.setSelected(false);
                reportAnswer = 2;
                btnReport.setClickable(true);
            }
        });
        highBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lowBtn.setSelected(false);
                littleHighBtn.setSelected(false);
                highBtn.setSelected(true);
                reportAnswer = 3;
                btnReport.setClickable(true);
            }
        });






    }

    public long getDayNum(){
        long dayNum=0;
        SharedPreferences a = getActivity().getSharedPreferences("firstDate", Context.MODE_PRIVATE);
        String  firstTimeStr = a.getString("firstDaeMillis", "2020-07-09");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date firstDate = format.parse(firstTimeStr);
            Date currentDate = Calendar.getInstance().getTime();
            Log.i(TAG, "first, current: " + firstDate + ", " + currentDate);

            long caldate = firstDate.getTime() - currentDate.getTime();

            dayNum = caldate / (24*60*60*1000);

            dayNum = Math.abs(dayNum);

            Log.i(TAG, "Day num: " + dayNum);

            return dayNum;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dayNum;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(reportAnswer == 0){
                Toast.makeText(getContext(), "실제 스트레스 지수를 선택해주세요!", Toast.LENGTH_LONG).show();
            }
            else{
                try {
                    day_num = jsonObjects[reportAnswer - 1].getInt("day_num");
                    order = jsonObjects[reportAnswer - 1].getInt("ema_order");
                    accuracy = jsonObjects[stressLevel - 1].getInt("accuracy");
                    feature_ids = jsonObjects[reportAnswer - 1].getString("feature_ids");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                final NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(EMA_NOTIFICATION_ID);
                }


                Log.i(TAG, String.format(Locale.KOREA, "data: %d %d %d %d %d %s", stressLevel, reportAnswer, day_num, order, accuracy, feature_ids));
                ((StressReportActivity)getActivity()).replaceFragment(StressReportFragment2.newInstance(reportTimestamp, stressLevel, reportAnswer, day_num, order, accuracy, feature_ids));

            }
        }

    };

    public String gettingStressReportFromGRPC(){
        String stresReportStr = "";
        SharedPreferences loginPrefs = getActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        SharedPreferences configPrefs = getActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

        Calendar fromCalendar = Calendar.getInstance();
        Calendar tillCalendar = Calendar.getInstance();

        int reportOrder = getReportPreviousOrder(fromCalendar);
        // initialize calendar time
        if (reportOrder != 0){
            fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - REPORT_DURATION);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
            tillCalendar.set(Calendar.MINUTE, 59);
            tillCalendar.set(Calendar.SECOND, 59);

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG, "initialize fromCalendar: " + dateFormat.format(fromCalendar.getTime()));
        Log.i(TAG, "initialize tillCalendar: " + dateFormat.format(tillCalendar.getTime()));

        // test
//        long fillMillis = 1593554400000l;
//        long tillTime = 1593568801000l;

//        long fillMillis = 1593568801000l;
//        long tillTime = 1593583201000l;

        long fillMillis = 1593583201000l;
        long tillTime = 1593597601000l;

        ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

        ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

        EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                .setTargetDataSourceId(configPrefs.getInt("STRESS_PREDICTION", -1))
                .setFromTimestamp(fillMillis) // TODO change fromCalendar.getTimeInMillis()
                .setTillTimestamp(tillTime) // TODO change tillCalendar.getTimeInMillis()
                .build();


        final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
        if (responseMessage.getDoneSuccessfully()) {
            List<String> values = responseMessage.getValueList();
            List<Long> valuesTimestamp = responseMessage.getTimestampList();
            if(!values.isEmpty()){
                stresReportStr = values.get(0);
                Log.d(TAG, "stressReportStr: " + stresReportStr);
            }else{
                Log.d(TAG, "values empty");
            }
            if(!valuesTimestamp.isEmpty()){
                reportTimestamp = valuesTimestamp.get(0);
                Log.d(TAG, "report timestamp from gRPC is " + reportTimestamp);
            }else{
                Log.d(TAG, "report timestamp from gRPC is empty");
            }
        }
        //end getting data from gRPC

        return stresReportStr;
    }

    public int getReportPreviousOrder(Calendar cal){
        if((REPORT_NOTIF_HOURS[0] - REPORT_DURATION) <= cal.get(Calendar.HOUR_OF_DAY) &&
                cal.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[0]){
            return 0;
        }
        else if((REPORT_NOTIF_HOURS[1] - REPORT_DURATION) <= cal.get(Calendar.HOUR_OF_DAY) &&
                cal.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[1]){
            return 1;
        }
        else if((REPORT_NOTIF_HOURS[2] - REPORT_DURATION) <= cal.get(Calendar.HOUR_OF_DAY) &&
                cal.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[2]){
            return 2;
        }
        else if((REPORT_NOTIF_HOURS[3] - REPORT_DURATION) <= cal.get(Calendar.HOUR_OF_DAY) &&
                cal.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[3]){
            return 3;
        }
        else{
            return 4;
        }
    }


}