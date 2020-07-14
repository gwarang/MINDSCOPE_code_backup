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
import kr.ac.inha.mindscope.DbMgr;
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
    private static final String STRESS_REPORT_EXAMPLE =
            "1594735308830,\"1\":{" +
            "\"day_num\": 15," +
            "\"ema_order\": 3," +
            "\"accuracy\": 60," +
            "\"feature_ids\": \"1-high 2-low 3-high 4-low 5-low\"," +
            "\"model_tag\": False" +
            "}," +
            "\"2\":{" +
            "\"day_num\": 15," +
            "\"ema_order\": 3," +
            "\"accuracy\": 40," +
            "\"feature_ids\": \"5-high 7-low 3-high 4-low 5-low\"," +
            "\"model_tag\": False" +
            "}," +
            "\"3\":{" +
            "\"day_num\": 15," +
            "\"ema_order\": 3," +
            "\"accuracy\": 20," +
            "\"feature_ids\": \"1-high 2-low 3-high 4-low 5-low\"," +
            "\"model_tag\": True" +
            "}";
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
        parsingStressReport(STRESS_REPORT_EXAMPLE); // TODO gRPS로부터 받아온 Stress Prediction String (stressReportStr)으로 교체할 것

        for(short i = 0; i < jsonObjects.length; i++){
            try {
                if(jsonObjects[i].getBoolean("model_tag")){
                    stressLevel = i+1;
//                    day_num = jsonObjects[i].getInt("day_num");
//                    order = jsonObjects[i].getInt("ema_order");
//                    accuracy = jsonObjects[i].getInt("accuracy");
//                    feature_ids = jsonObjects[i].getString("feature_ids");
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
        reportOrder = Tools.getReportOrderAtExactTime(cal);
        reportDay = getDayNum();
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

//        stressLvView = (TextView) findViewById(R.id.report_step2_result);
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
//            // TODO stress level에 따라서 다른 문장 오도록 변경할것
//            stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_report_low)));
//        }
//        else{
//            stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_report_low), Html.FROM_HTML_MODE_LEGACY));
//        }


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
            }
        });
        littleHighBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lowBtn.setSelected(false);
                littleHighBtn.setSelected(true);
                highBtn.setSelected(false);
                reportAnswer = 2;
            }
        });
        highBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lowBtn.setSelected(false);
                littleHighBtn.setSelected(false);
                highBtn.setSelected(true);
                reportAnswer = 3;
            }
        });

        btnReport = view.findViewById(R.id.toolbar_report_btn);
        btnReport.setOnClickListener(clickListener);




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
            long timestamp = System.currentTimeMillis();
            Calendar curCal = Calendar.getInstance();

            SharedPreferences prefs = getActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

            int dataSourceId = prefs.getInt("SELF_STRESS_REPORT", -1);
            assert dataSourceId != -1;
            Log.i(TAG, "SELF_STRESS_REPORT dataSourceId: " + dataSourceId);
            DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f, timestamp, reportDay, reportOrder, reportAnswer);




            //go to main activity
//            Intent intent = new Intent(getActivity(), MainActivity.class);
//            intent.putExtra("timestamp", timestamp);
//            intent.putExtra("rerpotdaynum", reportDay);
//            intent.putExtra("reportorder", reportOrder);
//            startActivity(intent);


            final NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(EMA_NOTIFICATION_ID);
            }

            try {
                day_num = jsonObjects[reportAnswer - 1].getInt("day_num");
                order = jsonObjects[reportAnswer - 1].getInt("ema_order");
                accuracy = jsonObjects[stressLevel - 1].getInt("accuracy");
                feature_ids = jsonObjects[reportAnswer - 1].getString("feature_ids");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ((StressReportActivity)getActivity()).replaceFragment(StressReportFragment2.newInstance(stressLevel, reportAnswer, day_num, order, accuracy, feature_ids));

//            Toast.makeText(getApplicationContext(), "Response saved", Toast.LENGTH_SHORT).show();
        }
    };

    public String gettingStressReportFromGRPC(){
        // TODO stress report 받아와서 값 parsing
        // TODO get report from gRPC
        String stresReportStr = "";
        SharedPreferences loginPrefs = getActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        SharedPreferences configPrefs = getActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

        Calendar fromCalendar = Calendar.getInstance();
        Calendar tillCalendar = Calendar.getInstance();

        int reportOrder = getReportPreviousOrder(fromCalendar);
        // initialize calendar time
        fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - REPORT_DURATION);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
        tillCalendar.set(Calendar.MINUTE, 59);
        tillCalendar.set(Calendar.SECOND, 59);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG, "initialize fromCalendar: " + dateFormat.format(fromCalendar.getTime()));
        Log.i(TAG, "initialize tillCalendar: " + dateFormat.format(tillCalendar.getTime()));



        ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

        ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

        EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                .setTargetDataSourceId(configPrefs.getInt("SURVEY_EMA", -1)) // TODO change STRESS_PREDICTION
                .setFromTimestamp(fromCalendar.getTimeInMillis())
                .setTillTimestamp(tillCalendar.getTimeInMillis())
                .build();

        final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
        if (responseMessage.getDoneSuccessfully()) {
            List<String> values = responseMessage.getValueList();
            if(!values.isEmpty()){
                Log.i(TAG, "data from gRPC, user answer5: " + values.toString() + ", " + values.get(0).charAt(values.get(0).length() - 1));
                stressLevel = Character.getNumericValue(values.get(0).charAt(values.get(0).length() - 1));
                stresReportStr = values.get(0);
            }

        }

        SharedPreferences reportPrefs = getActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = reportPrefs.edit();
        editor.putInt("result", stressLevel);
        editor.apply();
        // TODO STRESS_PREDICTION parsing
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

    public void parsingStressReport(String originStressReportStr){
        // REPORT Parsing
        String str = originStressReportStr;
        String timestampFromResult = str.substring(0, TIMESTAMP_END_INDEX);
        String exceptTimestamp = str.substring(RESULTS_START_INDEX);
        Log.i(TAG, "PARSINGTEST timestamp from result: " + timestampFromResult);
        Log.i(TAG, "PARSINGTEST other: " + exceptTimestamp);
        String[] resultStringArray = exceptTimestamp.split("(\\},)");
        for(short i = 0; i < resultStringArray.length; i++){
            if(i != resultStringArray.length - 1)
                resultStringArray[i] = resultStringArray[i].substring(RESULTS_INDICATOR_INDEX) + "}";
            else
                resultStringArray[i] = resultStringArray[i].substring(RESULTS_INDICATOR_INDEX);
        }
        jsonObjects = new JSONObject[3];
        try {
            for(short i=0; i<jsonObjects.length; i++){
                jsonObjects[i] = new JSONObject(resultStringArray[i]);
                Log.i(TAG, "PARSINGTEST jsonObjects" + i + ": " + jsonObjects[i].toString());
                Log.i(TAG, "PARSINGTEST jsonObjects acc" + i + ": " + jsonObjects[i].getInt("accuracy"));
            }
//            jsonResult[0] = new JSONObject(resultStringArray[0]);
//            JSONObject obj2 = new JSONObject(resultStringArray[1]);
//            JSONObject obj3 = new JSONObject(resultStringArray[2]);
//            Log.i(TAG, "PARSINGTEST jsonObj1: " + obj1.toString());
//            Log.i(TAG, "PARSINGTEST jsonObj2: " + obj2.toString());
//            Log.i(TAG, "PARSINGTEST jsonObj3: " + obj3.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}