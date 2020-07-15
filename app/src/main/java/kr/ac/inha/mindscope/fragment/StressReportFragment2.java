package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.StressReportDBHelper;
import kr.ac.inha.mindscope.Tools;

import static kr.ac.inha.mindscope.StressReportActivity.REPORTNUM4;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV1;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV2;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV3;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StressReportFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StressReportFragment2 extends Fragment {

    private static final String TAG = "StressReportFragment2";
    private static final int YES_BTN = 1;
    private static final int NO_BTN = 2;

    public static StressReportFragment2 newInstance(int stressLevel, int reportAnswer){
        StressReportFragment2 fragment2 = new StressReportFragment2();
        Bundle bundle = new Bundle();
        bundle.putInt("stressLevel", stressLevel);
        bundle.putInt("reportAnswer", reportAnswer);
        fragment2.setArguments(bundle);
        return fragment2;
    }

    public static StressReportFragment2 newInstance(int stressLevel, int reportAnswer, int day_num, int order, int accuracy, String feature_ids){
        StressReportFragment2 fragment2 = new StressReportFragment2();
        Bundle bundle = new Bundle();
        bundle.putInt("stressLevel", stressLevel);
        bundle.putInt("reportAnswer", reportAnswer);
        bundle.putInt("day_num", day_num);
        bundle.putInt("order", order);
        bundle.putInt("accuracy", accuracy);
        bundle.putString("feature_ids", feature_ids);
        fragment2.setArguments(bundle);
        return fragment2;
    }

    public int reportAnswer;
    public int stressLevel;
    public int day_num;
    public int order;
    public int accuracy;
    public String featrue_ids;


    TextView correctnessView;
    TextView accView;
    ImageView stressImg;
    TextView stressLevelView;

    TextView reason1;
    TextView reason2;
    TextView reason3;
    TextView reason4;
    TextView reason5;

    LinearLayout reasonContainer;

    Button yesBtn;
    Button noBtn;

    StressReportDBHelper dbHelper;

    public StressReportFragment2() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new StressReportDBHelper(getContext());

        if(getArguments() != null){
            this.stressLevel = getArguments().getInt("stressLevel");
            this.reportAnswer = getArguments().getInt("reportAnswer");
            this.day_num = getArguments().getInt("day_num");
            this.order = getArguments().getInt("order");
            this.accuracy = getArguments().getInt("accuracy");
            this.featrue_ids = getArguments().getString("feature_ids");
            Log.i(TAG, String.format("%d %d %d %d %d %s", stressLevel, reportAnswer, day_num, order, accuracy, featrue_ids));
            saveStressReport();
        }else{
            Log.i(TAG, "getArguments null");
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stress_report2, container, false);

        correctnessView = view.findViewById(R.id.txt_yes_no);
        accView = view.findViewById(R.id.prediction_acc);
        stressImg = view.findViewById(R.id.frg_report_step2_img);
        stressLevelView = view.findViewById(R.id.txt_stress_level);
        yesBtn = view.findViewById(R.id.btn_correct);
        noBtn = view.findViewById(R.id.btn_incorrect);
        reasonContainer = view.findViewById(R.id.stress_report_reason_container);
        reason1 = view.findViewById(R.id.txt_phone_reason1);
        reason2 = view.findViewById(R.id.txt_phone_reason2);
        reason3 = view.findViewById(R.id.txt_phone_reason3);
        reason4 = view.findViewById(R.id.txt_location_reason1);
        reason5 = view.findViewById(R.id.txt_location_reason2);

        String[] featureArray = featrue_ids.split(" ");
        // TODO update reason Views after knowing the meaning of feature_ids
        reason1.setText(featureArray[0]);
        reason2.setText(featureArray[1]);
        reason3.setText(featureArray[2]);
        reason4.setText(featureArray[3]);
        reason5.setText(featureArray[4]);



        if(stressLevel == reportAnswer){
            correctnessView.setText(getResources().getString(R.string.string_prediction_correct));
        }
        else{
            correctnessView.setText(getResources().getString(R.string.string_prediction_incorrect));
        }

        final String accTxt1 = "사실 저는 ";
        final String accTxt2 = accuracy + "%";
        final String accTxt3 = "의 확신을 가지고 있었어요.";
        String accTxtResult = accTxt1 + accTxt2 + accTxt3;
        Spannable spannable = new SpannableString(accTxtResult);
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.textColor_blue)), accTxt1.length(), (accTxt1 + accTxt2).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        accView.setText(spannable, TextView.BufferType.SPANNABLE);

        // TODO STRESS_PREDICTION 원인으로 UI 업데이트 해주기
        switch(reportAnswer){
            case STRESS_LV1:
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_low, getActivity().getTheme()));
                stressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_result_low)));
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_low_bg, getActivity().getTheme()));
                break;
            case STRESS_LV2:
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_littlehigh, getActivity().getTheme()));
                stressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_result_littlehigh)));
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_littlehigh_bg, getActivity().getTheme()));
                break;
            case STRESS_LV3:
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_high, getActivity().getTheme()));
                stressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_result_high)));
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_high_bg, getActivity().getTheme()));
                break;
        }

        yesBtn.setOnClickListener(yseClickListener);
        noBtn.setOnClickListener(noClickListener);

        return view;
    }

    public void clickBtn(int resultRight){
        Calendar cal = Calendar.getInstance();
        int reportNum = Tools.getReportOrderAtExactTime(cal);

        if(reportNum == REPORTNUM4){
            // TODO 하루의 마지막 리포트이면 '마음케어'로 이동하도록 구현, 지금은 그냥 main으로 이동함

            SharedPreferences stressReportPrefs = getActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = stressReportPrefs.edit();
            editor.putInt("reportAnswer", reportAnswer);
            editor.putInt("day_num", day_num);
            editor.putInt("order", order);
            editor.putInt("accuracy", accuracy);
            editor.putString("feature_ids", featrue_ids);
            editor.apply();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("reportAnswer", reportAnswer);
            intent.putExtra("day_num", day_num);
            intent.putExtra("order", order);
            intent.putExtra("accuracy", accuracy);
            intent.putExtra("feature_ids", featrue_ids);
            startActivity(intent);


        }else{
            // 그 외는 MainActivity로

            SharedPreferences stressReportPrefs = getActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = stressReportPrefs.edit();
            editor.putInt("reportAnswer", reportAnswer);
            editor.putInt("day_num", day_num);
            editor.putInt("order", order);
            editor.putInt("accuracy", accuracy);
            editor.putString("feature_ids", featrue_ids);
            editor.apply();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("reportAnswer", reportAnswer);
            intent.putExtra("day_num", day_num);
            intent.putExtra("order", order);
            intent.putExtra("accuracy", accuracy);
            intent.putExtra("feature_ids", featrue_ids);
            startActivity(intent);
        }
    }

    View.OnClickListener yseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clickBtn(YES_BTN);
        }
    };

    View.OnClickListener noClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            clickBtn(NO_BTN);
        }
    };

    public void saveStressReport(){
        dbHelper.insertStressReportData(reportAnswer, day_num, order, accuracy, featrue_ids);
    }

}