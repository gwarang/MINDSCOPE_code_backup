package kr.ac.inha.mindscope;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import kr.ac.inha.mindscope.fragment.StressReportFragment1;


public class StressReportActivity extends AppCompatActivity {

    private static final String TAG = "StressReportActivity";
    private static final String SAVE = "저장";
    private static final String DONE = "완료";
    public static final int STRESS_LV1 = 1;
    public static final int STRESS_LV2 = 2;
    public static final int STRESS_LV3 = 3;
    public static final int REPORTNUM1 = 1;
    public static final int REPORTNUM2 = 2;
    public static final int REPORTNUM3 = 3;
    public static final int REPORTNUM4 = 4;
    public static final Short[] REPORT_NOTIF_HOURS = {11, 15, 19, 23};  //in hours of day

    static int currentHours;

    TextView dateView;
    TextView timeView;
    TextView stressLvView;

    ImageButton lowBtn;
    ImageButton littleHighBtn;
    ImageButton highBtn;

    ImageView stressLevelImg;
    TextView stressLevelTxt;

    Button btnReport;

    static int reportAnswer;
    private int reportOrder;
    private long reportDay;

    public static int stressLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stres_report);



        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.frameLayout, StressReportFragment1.newInstance(stressLevel)).commit();

    }

    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment).commit();
    }





}