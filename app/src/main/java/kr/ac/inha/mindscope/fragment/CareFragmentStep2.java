package kr.ac.inha.mindscope.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_care_step2, container, false);

        viewPager = (ViewPager2) view.findViewById(R.id.care_step2_container);
        tabLayout = (TabLayout) view.findViewById(R.id.care_step2_tab);

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
        CareViewPagerAdapter adapter = new CareViewPagerAdapter(getActivity());
        return adapter;
    }


}