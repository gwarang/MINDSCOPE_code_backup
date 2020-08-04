package kr.ac.inha.mindscope.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import kr.ac.inha.mindscope.CareViewPagerAdapter;
import kr.ac.inha.mindscope.R;

public class CareFragmentStep2 extends Fragment {

    private static final String TAG = "CareFragmentStep2";

    Dialog todayLastReportDialog;
    RelativeLayout todayLastReportLayout;
    ImageView todayLastReportImg;
    TextView todayLastReportText;

    ViewPager2 viewPager;
    TabLayout tabLayout;
    private static final String[] tabTitleKo = {"스트레스 분석", "스트레스 해소", "오늘의 코멘트"};
    private static final String[] tabTitle = {"Analysis Stress", "Relieve Stress", "Today's comment"};
    String language;

    public CareFragmentStep2() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences stressReportPrefs = requireActivity().getSharedPreferences("stressReport", Context.MODE_PRIVATE);


        if(stressReportPrefs.getBoolean("today_last_report", false)){
            SharedPreferences.Editor editor = stressReportPrefs.edit();
            editor.putBoolean("today_last_report", false);
            editor.apply();
            // last report full screen dialog
            View view = getLayoutInflater().inflate(R.layout.today_last_report_dialog, null);
            todayLastReportDialog = new Dialog(requireContext(), android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
            todayLastReportDialog.setContentView(view);
            todayLastReportLayout = view.findViewById(R.id.today_last_report_layout);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    todayLastReportDialog.dismiss();
                }
            }, 2000);
            todayLastReportLayout.setOnClickListener(view1 -> {
                if(todayLastReportDialog != null)
                    todayLastReportDialog.dismiss();
            });
            todayLastReportImg = view.findViewById(R.id.today_last_report_img);
            todayLastReportText = view.findViewById(R.id.today_last_report_txt);
            todayLastReportDialog.show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_care_step2, container, false);

        viewPager = view.findViewById(R.id.care_step2_container);
        tabLayout = view.findViewById(R.id.care_step2_tab);

        language = Locale.getDefault().getLanguage();

        viewPager.setAdapter(createAapter());
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if(language.equals("ko")){
                    tab.setText(tabTitleKo[position]);
                }else{
                    tab.setText(tabTitle[position]);
                }

            }
        }).attach();



        return view;
    }


    private CareViewPagerAdapter createAapter(){
        CareViewPagerAdapter adapter = new CareViewPagerAdapter(requireActivity());
        return adapter;
    }


}