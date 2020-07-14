package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.MapsActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.StressReportActivity;

import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV1;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV2;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV3;

public class MeFragmentStep2 extends Fragment {

    private static final String TAG = "MeFragmentStep2";


    static int currentHours;


    private ImageButton btnMap;
    private Button stepTestBtn;

    private TextView todayPointsView;
    private TextView sumPointsView;

    private TextView dateView;
    private TextView timeView;
    private TextView stressLvView;
    TextView reason1;
    TextView reason2;
    TextView reason3;
    TextView reason4;
    TextView reason5;

    private Button reportBtn;

    public int stressLevel;
    private String feature_ids;

    LinearLayout reasonContainer;
    ImageView stressImg;

    public MeFragmentStep2() {
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
        View view = inflater.inflate(R.layout.fragment_me_step2, container, false);

        SharedPreferences stressReportPrefs = getActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
        feature_ids = stressReportPrefs.getString("feature_ids", "");


        stressLvView = (TextView) view.findViewById(R.id.txt_stress_level);
        // TODO 스트레스 리포트 결과에 따라서  string 변화되게 구현

        stressLevel = stressReportPrefs.getInt("reportAnswer", 0);

        reasonContainer = view.findViewById(R.id.stress_reason_container);
        stressImg = view.findViewById(R.id.frg_me_step2_img1);

        switch (stressLevel){
            case STRESS_LV1:
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low)));
                }
                else{
                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_low), Html.FROM_HTML_MODE_LEGACY));
                }
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_low_bg, getActivity().getTheme()));
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_low, getActivity().getTheme()));
                break;
            case STRESS_LV2:
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_littlehigh)));
                }
                else{
                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_littlehigh), Html.FROM_HTML_MODE_LEGACY));
                }
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_littlehigh_bg, getActivity().getTheme()));
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_littlehigh, getActivity().getTheme()));
                break;
            case STRESS_LV3:
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_high)));
                }
                else{
                    stressLvView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_level_high), Html.FROM_HTML_MODE_LEGACY));
                }
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_high_bg, getActivity().getTheme()));
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_high, getActivity().getTheme()));
                break;
        }

        reason1 = view.findViewById(R.id.txt_phone_reason1);
        reason2 = view.findViewById(R.id.txt_phone_reason2);
        reason3 = view.findViewById(R.id.txt_phone_reason3);
        reason4 = view.findViewById(R.id.txt_location_reason1);
        reason5 = view.findViewById(R.id.txt_location_reason2);
        if(!feature_ids.equals("")){
            String[] featureArray = feature_ids.split(" ");

            // TODO update reason Views after knowing the meaning of feature_ids
            reason1.setText(featureArray[0]);
            reason2.setText(featureArray[1]);
            reason3.setText(featureArray[2]);
            reason4.setText(featureArray[3]);
            reason5.setText(featureArray[4]);
        }



        btnMap = (ImageButton) view.findViewById(R.id.fragment_me_step2_btn_map);
        btnMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });

        dateView = (TextView) view.findViewById(R.id.frg_me_step2_date1);
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);


        Calendar cal = Calendar.getInstance();
        currentHours = cal.get(Calendar.HOUR_OF_DAY);
        Log.i(TAG, "current hours: " + currentHours);
        timeView = (TextView) view.findViewById(R.id.frg_me_step2_time1);
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


//        todayPointsView = root.findViewById(R.id.point_today_step2);
//        sumPointsView = root.findViewById(R.id.point_my_step2);
//        SharedPreferences prefs = getContext().getSharedPreferences("points", Context.MODE_PRIVATE);
//        todayPointsView.setText(String.valueOf(prefs.getInt("todayPoints", 0)));
//        sumPointsView.setText(String.valueOf(prefs.getInt("sumPoints", 0)));


        // TODO 추후 step 시간으로 확인할때는 삭제할 부분
        stepTestBtn = (Button) view.findViewById(R.id.step_test_btn_step2);
        stepTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences stepChange = getActivity().getSharedPreferences("stepChange", getContext().MODE_PRIVATE);
                SharedPreferences.Editor editor = stepChange.edit();

                if(stepChange.getInt("stepCheck", 0) == 1){
                    Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
                    stepTestBtn.setText("STEP 2");
                    editor.putInt("stepCheck", 2);
                    editor.apply();
                }
                else{
                    Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
                    stepTestBtn.setText("STEP 1");
                    editor.putInt("stepCheck", 1);
                    editor.apply();
                }
            }
        });

        reportBtn = (Button) view.findViewById(R.id.report_test_btn);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), StressReportActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
    }
}