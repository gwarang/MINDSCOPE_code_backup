package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import kr.ac.inha.mindscope.EMAActivity;
import kr.ac.inha.mindscope.MapsActivity;
import kr.ac.inha.mindscope.R;

public class MeFragmentStep1 extends Fragment {

    private static final String TAG = "MeFragment";

    private MeViewModel meViewModel;
    private ImageButton btnMap;
    private AppBarLayout appBarLayout;
    private Button stepTestBtn;
    private Button emaTestBtn;
    private Button time1Btn;
    private Button time2Btn;
    private Button time3Btn;
    private Button time4Btn;
    private TextView todayPointsView;
    private TextView sumPointsView;
    private TextView before11Hours;
    private TextView attdView;
    private RelativeLayout timeContainer;

    public static MeFragmentStep1 newInstance() {
        return new MeFragmentStep1();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        meViewModel = ViewModelProviders.of(this).get(MeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_me, container, false);
//        final TextView textView = root.findViewById(R.id.text_me);
        TextView date = root.findViewById(R.id.frg_me_date);
        // TODO timeX_state 텍스트 및 timeX_btn background EMA order 상태(설문완료, 미완료, 미도착)에 따라서 변경할 것
        TextView time1 = root.findViewById(R.id.time1_state);
        time1.setText("설문완료");
        TextView time2 = root.findViewById(R.id.time2_state);
        TextView time3 = root.findViewById(R.id.time3_state);
        TextView time4 = root.findViewById(R.id.time4_state);
        todayPointsView = root.findViewById(R.id.point_today);
        sumPointsView = root.findViewById(R.id.point_my);
        before11Hours = root.findViewById(R.id.frg_me_before_11hours);
        attdView = root.findViewById(R.id.today_survey_attd);
        timeContainer = root.findViewById(R.id.time_container);

        Calendar cal = Calendar.getInstance();

        if(cal.get(Calendar.HOUR_OF_DAY) < 11 && cal.get(Calendar.HOUR_OF_DAY) > 3){
            before11Hours.setVisibility(View.VISIBLE);
            date.setVisibility(View.INVISIBLE);
            attdView.setVisibility(View.INVISIBLE);
            timeContainer.setVisibility(View.INVISIBLE);
        }else{
            before11Hours.setVisibility(View.INVISIBLE);
            date.setVisibility(View.VISIBLE);
            attdView.setVisibility(View.VISIBLE);
            timeContainer.setVisibility(View.VISIBLE);
        }


        SharedPreferences prefs = getContext().getSharedPreferences("points", Context.MODE_PRIVATE);

        todayPointsView.setText(String.valueOf(prefs.getInt("todayPoints", 0)));
        sumPointsView.setText(String.valueOf(prefs.getInt("sumPoints", 0)));

        time1Btn = root.findViewById(R.id.time1_btn);
        time1Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_complete));
        time2Btn = root.findViewById(R.id.time2_btn);

        time3Btn = root.findViewById(R.id.time3_btn);
        time3Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_incomplete));
        time4Btn = root.findViewById(R.id.time4_btn);
        time4Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_inarrived));
//        meViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
////                textView.setText(s);
//            }
//        });

        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        date.setText(date_text);


        appBarLayout = (AppBarLayout) root.findViewById(R.id.frg_me_app_bar);
        btnMap = (ImageButton) root.findViewById(R.id.fragment_me_btn_map);



        btnMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                startActivity(intent);
            }
        });


        // TODO 추후 step 시간으로 확인할때는 삭제할 부분
        stepTestBtn = (Button) root.findViewById(R.id.step_test_btn);
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

        emaTestBtn = (Button) root.findViewById(R.id.ema_test_btn);
        emaTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EMAActivity.class);
                intent.putExtra("dont_save_ema", true);
                startActivity(intent);
            }
        });




        return root;
    }

}