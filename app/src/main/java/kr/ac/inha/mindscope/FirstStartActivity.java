package kr.ac.inha.mindscope;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import java.util.ArrayList;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import static kr.ac.inha.mindscope.Tools.ACTION_OPEN_PAGE;

public class FirstStartActivity extends AppCompatActivity {

    private  static final String TAG = "FirstStartActivity";
    private  static final String START_BUTTON_CLCIK = "START_BUTTON_CLICK";
    ViewPager2 viewPager2;
    SpringDotsIndicator springDotsIndicator;
    LinearLayout containerBtn;
    TextView fsv_title;
    Button btnStart;
    CheckBox mCheckbox;

    AlertDialog dialog;

    private ViewPager mPager;
    CheckBox checkBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);

//        Objects.requireNonNull(getSupportActionBar()).hide();

        viewPager2 = findViewById(R.id.viewPager2);
        springDotsIndicator = (SpringDotsIndicator) findViewById(R.id.spring_dots_indicator);
        fsv_title = viewPager2.findViewById(R.id.fsv_title);
        btnStart = findViewById(R.id.fsv_start_btn);
        mCheckbox = findViewById(R.id.fsv_checkbox);

        ArrayList<DataPage> list = new ArrayList<>();

        if(Build.VERSION.SDK_INT >= 23){
            list.add(new DataPage(getString(R.string.app_name), getString(R.string.fsv_contents_one),
                    ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title)));
            list.add(new DataPage(getString(R.string.fsv_step) + " 1", getString(R.string.fsv_contents_two),
                    ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
            list.add(new DataPage(getString(R.string.fsv_step) + " 2", getString(R.string.fsv_contents_three),
                    ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
            list.add(new DataPage(getString(R.string.fsv_point), getString(R.string.fsv_contents_four),
                    ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
            list.add(new DataPage(getString(R.string.fsv_point), getString(R.string.fsv_contents_five),
                    ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
            list.add(new DataPage("", getString(R.string.fsv_contents_six),
                    ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
        }else {
            list.add(new DataPage(getString(R.string.app_name), getString(R.string.fsv_contents_one), ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title)));
            list.add(new DataPage(getString(R.string.fsv_step) + " 1", getString(R.string.fsv_contents_two), ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
            list.add(new DataPage(getString(R.string.fsv_step) + " 2", getString(R.string.fsv_contents_three), ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
            list.add(new DataPage(getString(R.string.fsv_point), getString(R.string.fsv_contents_four), ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
            list.add(new DataPage(getString(R.string.fsv_point), getString(R.string.fsv_contents_five), ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
            list.add(new DataPage("", getString(R.string.fsv_contents_six), ContextCompat.getColor(getApplicationContext(), R.color.color_fsv_title2)));
        }


        viewPager2.setAdapter(new ViewPagerAdapter(list));
        springDotsIndicator.setViewPager2(viewPager2);


        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d(TAG, "current position: " + String.valueOf(position));
                if(position == 0){
                    btnStart.setVisibility(View.INVISIBLE);
                    mCheckbox.setVisibility(View.INVISIBLE);
                }
                else if(position == 5){
                    btnStart.setVisibility(View.VISIBLE);
                    mCheckbox.setVisibility(View.VISIBLE);
                }
                else{
                    btnStart.setVisibility(View.INVISIBLE);
                    mCheckbox.setVisibility(View.INVISIBLE);
//                    fsv_title.setTextColor(getResources().getColor(android.R.color.black));
                }
            }
        });



        mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                btnStart.setEnabled(b);
                if(b){
                    btnStart.setBackground(getDrawable(R.drawable.btn_start_enable));
                }else{
                    btnStart.setBackground(getDrawable(R.drawable.btn_start_disable));
                }

            }
        });


        btnStart.setOnClickListener(view -> {
            int infoFirst = 1;
            SharedPreferences a = getSharedPreferences("firstStart", MODE_PRIVATE);
            SharedPreferences.Editor editor = a.edit();
            editor.putInt("First", infoFirst);
            editor.apply();
            Tools.saveApplicationLog(getApplicationContext(), TAG, START_BUTTON_CLCIK);
            Intent intent = new Intent(FirstStartActivity.this, MapsActivity.class);
            startActivity(intent);
        });


    }

    @Override
    protected void onResume() {

        if (!Tools.hasPermissions(this, Tools.PERMISSIONS)) {
//            dialog = Tools.requestPermissions(FirstStartActivity.this);
            Dialog permissionDialog = Tools.requestPermissionsWithCustomDialog(FirstStartActivity.this);
            permissionDialog.show();
        }
        super.onResume();
        Tools.saveApplicationLog(getApplicationContext(), TAG, ACTION_OPEN_PAGE);
    }
}