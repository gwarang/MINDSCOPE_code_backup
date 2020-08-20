package kr.ac.inha.mindscope.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.StressReportActivity;
import kr.ac.inha.mindscope.Tools;

import static kr.ac.inha.mindscope.StressReportActivity.REPORT_NOTIF_HOURS;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV1;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV2;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV3;
import static kr.ac.inha.mindscope.Tools.PREDICTION_DAYNUM_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_FEATUREIDS_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_ORDER_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_STRESSLV_INDEX;
import static kr.ac.inha.mindscope.Tools.PREDICTION_TIMESTAMP_INDEX;
import static kr.ac.inha.mindscope.fragment.MeFragmentStep2.TIMESTAMP_ONE_DAY;
import static kr.ac.inha.mindscope.services.MainService.STRESS_REPORT_NOTIFI_ID;
import static kr.ac.inha.mindscope.services.StressReportDownloader.STRESS_PREDICTION_RESULT;

public class StressReportFragment1 extends Fragment {

    public static final int REPORT_DURATION = 4;
    private static final String TAG = "StressReportFragment1";
    public static int reportAnswer;
    static int currentHours;
    public int stressLevel;
    TextView dateView;
    TextView timeView;
    ImageButton lowBtn;
    ImageButton littleHighBtn;
    ImageButton highBtn;
    ImageView stressLevelImg;
    TextView stressLevelTxt;
    Button btnReport;
    int day_num;
    int order;
    Double accuracy;
    String feature_ids;
    long reportTimestamp;
    ArrayList<String> predictionArray;
    ConstraintLayout answerContainer;
    TextView questionTextView;
    boolean noStressReport;
    final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (noStressReport) {
                Calendar cal = Calendar.getInstance();
                order = Tools.getReportOrderFromRangeAfterReport(cal);
                SharedPreferences reportSubmitCheckPrefs = requireContext().getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
                SharedPreferences.Editor reportSubmitEditor = reportSubmitCheckPrefs.edit();
                String reportSubmit = "self_report_submit_check_" + order;
                reportSubmitEditor.putBoolean(reportSubmit, true);
                int submitDate = cal.get(Calendar.DATE);
                if (cal.get(Calendar.HOUR_OF_DAY) < 1)
                    submitDate--;
                reportSubmitEditor.putInt("reportSubmitDate", submitDate);
                reportSubmitEditor.apply();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                if (reportAnswer == 5) {
                    Toast.makeText(getContext(), "실제 스트레스 지수를 선택해주세요!", Toast.LENGTH_LONG).show();
                } else {
                    if (stressLevel != reportAnswer) {
                        for (String result : predictionArray) {
                            String[] splitResult = result.split(",");
                            if (Integer.parseInt(splitResult[PREDICTION_STRESSLV_INDEX]) == reportAnswer) {
                                reportTimestamp = Long.parseLong(splitResult[PREDICTION_TIMESTAMP_INDEX]);
                                day_num = Integer.parseInt(splitResult[PREDICTION_DAYNUM_INDEX]);
                                if (day_num == 0) {
                                    day_num = getDayNum();
                                }
                                order = Integer.parseInt(splitResult[PREDICTION_ORDER_INDEX]);
                                feature_ids = splitResult[PREDICTION_FEATUREIDS_INDEX];
                                boolean model_tag = Boolean.parseBoolean(splitResult[6]);
                            }
                        }
                    }
                    final NotificationManager notificationManager = (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel(STRESS_REPORT_NOTIFI_ID);
                    }

                    Log.d(TAG, String.format(Locale.KOREA, "data: %d %d %d %d %.2f %s", stressLevel, reportAnswer, day_num, order, accuracy, feature_ids));
                    ((StressReportActivity) requireActivity()).replaceFragment(StressReportFragment2.newInstance(reportTimestamp, stressLevel, reportAnswer, day_num, order, accuracy, feature_ids));

                    Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_CLICK_COMPLETE_BUTTON, reportAnswer);
                }
            }
        }

    };

    public StressReportFragment1() {
        // Required empty public constructor
    }

    public static StressReportFragment1 newInstance(int stressLevel) {
        StressReportFragment1 fragment = new StressReportFragment1();
        Bundle bundle = new Bundle();
        bundle.putInt("stressLevel", stressLevel);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.d(TAG, "stress level from StressReportActivity: " + stressLevel);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stress_report1, null);
        Context context = getContext();
        String stressReportStr = null;
        try {
            assert context != null;
            stressReportStr = getStressResult(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
//                gettingStressReportFromGRPC(); // get Stress Report Result from gRPC server;

        if (stressReportStr == null) {
            noStressReport = true;
            Calendar cal = Calendar.getInstance();
            reportTimestamp = cal.getTimeInMillis();
            order = Tools.getReportOrderFromRangeAfterReport(cal);
        } else {
            String[] splitResult = stressReportStr.split(",");
            reportTimestamp = Long.parseLong(splitResult[0]);
            stressLevel = Integer.parseInt(splitResult[1]);
            day_num = Integer.parseInt(splitResult[2]);
            order = Integer.parseInt(splitResult[3]);
            accuracy = Double.parseDouble(splitResult[4]);
            feature_ids = splitResult[5];
            boolean model_tag = Boolean.parseBoolean(splitResult[6]);
            noStressReport = false;
        }

        init(view);

        return view;
    }

    public void init(View view) {

        Calendar cal = Calendar.getInstance();

        answerContainer = view.findViewById(R.id.actual_answer_container);
        questionTextView = view.findViewById(R.id.txt_actual_stress_question);
        stressLevelImg = view.findViewById(R.id.report_step2_img);
        stressLevelTxt = view.findViewById(R.id.report_step2_result);

        btnReport = view.findViewById(R.id.toolbar_report_btn);
        btnReport.setOnClickListener(clickListener);

        lowBtn = view.findViewById(R.id.actual_stress_answer1);
        littleHighBtn = view.findViewById(R.id.actual_stress_answer2);
        highBtn = view.findViewById(R.id.actual_stress_answer3);

        // UI
        dateView = view.findViewById(R.id.report_step2_date);
        Date currentTime = new Date(reportTimestamp);
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);

        currentHours = cal.get(Calendar.HOUR_OF_DAY);
        timeView = view.findViewById(R.id.report_step2_time);
        switch (order) {
            case 1:
                timeView.setText(getResources().getString(R.string.time_step2_duration1));
                break;
            case 2:
                timeView.setText(getResources().getString(R.string.time_step2_duration2));
                break;
            case 3:
                timeView.setText(getResources().getString(R.string.time_step2_duration3));
                break;
            case 4:
                timeView.setText(getResources().getString(R.string.time_step2_duration4));
                break;
        }

        if (noStressReport) {
            stressLevelImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_low, requireActivity().getTheme()));
            answerContainer.setVisibility(View.INVISIBLE);
            questionTextView.setText(requireContext().getResources().getString(R.string.string_no_stress_report));
        } else {
            switch (stressLevel) {
                case STRESS_LV1:
                    stressLevelImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_low, requireActivity().getTheme()));
                    stressLevelTxt.setText(Html.fromHtml(getResources().getString(R.string.string_stress_report_low)));
                    break;
                case STRESS_LV2:
                    stressLevelImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_littlehigh, requireActivity().getTheme()));
                    stressLevelTxt.setText(Html.fromHtml(getResources().getString(R.string.string_stress_report_littlehigh)));
                    break;
                case STRESS_LV3:
                    stressLevelImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_high, requireActivity().getTheme()));
                    stressLevelTxt.setText(Html.fromHtml(getResources().getString(R.string.string_stress_report_high)));
            }

            reportAnswer = 5; // not selected

            lowBtn.setOnClickListener(view13 -> {
                lowBtn.setSelected(true);
                littleHighBtn.setSelected(false);
                highBtn.setSelected(false);
                reportAnswer = 0;
                btnReport.setClickable(true);
            });
            littleHighBtn.setOnClickListener(view1 -> {
                lowBtn.setSelected(false);
                littleHighBtn.setSelected(true);
                highBtn.setSelected(false);
                reportAnswer = 1;
                btnReport.setClickable(true);
            });
            highBtn.setOnClickListener(view12 -> {
                lowBtn.setSelected(false);
                littleHighBtn.setSelected(false);
                highBtn.setSelected(true);
                reportAnswer = 2;
                btnReport.setClickable(true);
            });
        }
    }

    public String getStressResult(Context context) throws IOException {
        String stressResult = null;
        FileInputStream fis = context.openFileInput(STRESS_PREDICTION_RESULT);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line;
        predictionArray = new ArrayList<>();

        //region set calendar
        Calendar fromCalendar = Calendar.getInstance();
        if(fromCalendar.get(Calendar.HOUR_OF_DAY) < 1){
            fromCalendar.add(Calendar.DATE, -1);
            fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[REPORT_NOTIF_HOURS.length-1]);
        }
        int reportOrder = Tools.getReportPreviousOrder(fromCalendar);
        Calendar tillCalendar = Calendar.getInstance();

        // initialize calendar time
        if (reportOrder < 4) {
            fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - REPORT_DURATION);
            tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
        } else {
            if (fromCalendar.get(Calendar.HOUR_OF_DAY) < REPORT_NOTIF_HOURS[0] - REPORT_DURATION) {
                fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - REPORT_DURATION);
                tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
                long fromTimestampYesterday = fromCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
                long tillTimestampYesterday = tillCalendar.getTimeInMillis() - TIMESTAMP_ONE_DAY;
                fromCalendar.setTimeInMillis(fromTimestampYesterday);
                tillCalendar.setTimeInMillis(tillTimestampYesterday);
            } else {
                fromCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - REPORT_DURATION);
                tillCalendar.set(Calendar.HOUR_OF_DAY, REPORT_NOTIF_HOURS[reportOrder - 1] - 1);
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

            if (fromCalendar.getTimeInMillis() <= timestamp && timestamp <= tillCalendar.getTimeInMillis() && Integer.parseInt(tokens[PREDICTION_ORDER_INDEX]) > 0 ) {
                predictionArray.add(line);
            }
        }
        //endregion

        for (String result : predictionArray) {
            String[] splitResult = result.split(",");
            if (Boolean.parseBoolean(splitResult[6])) {
                stressResult = result;
            }
        }
        return stressResult;
    }



    public int getDayNum() {
        int dayNum;
        SharedPreferences a = getActivity().getSharedPreferences("stepChange", Context.MODE_PRIVATE);
        long joinTimestamp = a.getLong("join_timestamp", 0);

        Calendar cal = Calendar.getInstance();
        if(cal.get(Calendar.HOUR_OF_DAY) < 1){
            cal.add(Calendar.DATE, -1);
        }
        long caldate = joinTimestamp - cal.getTimeInMillis();

        dayNum = (int) (caldate / (24 * 60 * 60 * 1000));

        Log.d(TAG, "Day num: " + dayNum);

        return dayNum;
    }
}
