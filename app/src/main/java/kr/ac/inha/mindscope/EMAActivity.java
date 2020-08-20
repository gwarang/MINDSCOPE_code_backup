package kr.ac.inha.mindscope;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static kr.ac.inha.mindscope.fragment.MeFragmentStep1.timeTheDayNumIsChanged;
import static kr.ac.inha.mindscope.services.MainService.EMA_NOTI_ID;

public class EMAActivity extends AppCompatActivity {

    //region Constants
    public static final String TAG = "EMAActivity";
    public static final int EMANUM1 = 1;
    public static final int EMANUM2 = 2;
    public static final int EMANUM3 = 3;
    public static final int EMANUM4 = 4;
    public static final Short[] EMA_NOTIF_HOURS = {11, 15, 19, 23};  //in hours of day
    //endregion

    //region UI  variables
    TextView dateView;
    TextView emaNumView;

    RadioGroup question1_group;
    RadioGroup question2_group;
    RadioGroup question3_group;
    RadioGroup question4_group;
    RadioButton question1_1;
    RadioButton question1_2;
    RadioButton question1_3;
    RadioButton question1_4;
    RadioButton question1_5;
    RadioButton question2_1;
    RadioButton question2_2;
    RadioButton question2_3;
    RadioButton question2_4;
    RadioButton question2_5;
    RadioButton question3_1;
    RadioButton question3_2;
    RadioButton question3_3;
    RadioButton question3_4;
    RadioButton question3_5;
    RadioButton question4_1;
    RadioButton question4_2;
    RadioButton question4_3;
    RadioButton question4_4;
    RadioButton question4_5;
    RelativeLayout question5_1;
    RelativeLayout question5_2;
    RelativeLayout question5_3;
    ImageButton question5_1_btn;
    ImageButton question5_2_btn;
    ImageButton question5_3_btn;

    Button btnSubmit; // 툴바의 저장 버튼으로 대체
    //endregion
    private int emaOrder;
    public int answer1;
    public int answer2;
    public int answer3;
    public int answer4;
    public int answer5;
    public long todayNum;

    private SharedPreferences loginPrefs;

    private AlertDialog dialog;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Tools.hasPermissions(this, Tools.PERMISSIONS)) {
//            dialog = Tools.requestPermissions(EMAActivity.this);
            Dialog permissionDialog = Tools.requestPermissionsWithCustomDialog(EMAActivity.this);
            permissionDialog.show();
        }
        loginPrefs = getSharedPreferences("UserLogin", MODE_PRIVATE);
        if (!loginPrefs.getBoolean("logged_in", false)) {
            finish();
        }
        setContentView(R.layout.activity_ema);

        toolbar = findViewById(R.id.toolbar_ema);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }


        init();
    }

    public int getDayNum() {
        long dayNum;
        SharedPreferences stepChangePrefs = getSharedPreferences("stepChange", MODE_PRIVATE);
        long joinTimestamp = stepChangePrefs.getLong("join_timestamp", 0);
        Calendar cal = Calendar.getInstance();
        if(cal.get(Calendar.HOUR_OF_DAY) < timeTheDayNumIsChanged){
            cal.add(Calendar.DATE, -1);
        }
        long caldate = joinTimestamp - cal.getTimeInMillis();
        dayNum = caldate / (24 * 60 * 60 * 1000);
        dayNum = Math.abs(dayNum);
        Log.d(TAG, "Day num: " + dayNum);
        return (int)dayNum;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_save:
                emaSubmit();
                break;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_btn_save, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tools.saveApplicationLog(getApplicationContext(), TAG, Tools.ACTION_OPEN_PAGE);
    }

    public void init() {

//        Question button start
        question1_group = findViewById(R.id.question1_group);
        question2_group = findViewById(R.id.question2_group);
        question3_group = findViewById(R.id.question3_group);
        question4_group = findViewById(R.id.question4_group);
        question1_1 = findViewById(R.id.question1_1);
        question1_2 = findViewById(R.id.question1_2);
        question1_3 = findViewById(R.id.question1_3);
        question1_4 = findViewById(R.id.question1_4);
        question1_5 = findViewById(R.id.question1_5);
        question2_1 = findViewById(R.id.question2_1);
        question2_2 = findViewById(R.id.question2_2);
        question2_3 = findViewById(R.id.question2_3);
        question2_4 = findViewById(R.id.question2_4);
        question2_5 = findViewById(R.id.question2_5);
        question3_1 = findViewById(R.id.question3_1);
        question3_2 = findViewById(R.id.question3_2);
        question3_3 = findViewById(R.id.question3_3);
        question3_4 = findViewById(R.id.question3_4);
        question3_5 = findViewById(R.id.question3_5);
        question4_1 = findViewById(R.id.question4_1);
        question4_2 = findViewById(R.id.question4_2);
        question4_3 = findViewById(R.id.question4_3);
        question4_4 = findViewById(R.id.question4_4);
        question4_5 = findViewById(R.id.question4_5);
        question5_1 = findViewById(R.id.question5_1);
        question5_2 = findViewById(R.id.question5_2);
        question5_3 = findViewById(R.id.question5_3);
        question5_1_btn = findViewById(R.id.icon_low_empty);
        question5_2_btn = findViewById(R.id.icon_littlehigh_empty);
        question5_3_btn = findViewById(R.id.icon_high_empty);
//         endregion

        dateView = findViewById(R.id.ema_date_info);
        emaNumView = findViewById(R.id.ema_number_info);

        emaOrder = getIntent().getIntExtra("ema_order", (short) -1);
        if (DbMgr.getDB() == null)
            DbMgr.init(getApplicationContext());

        todayNum = getDayNum();

        Calendar cal = Calendar.getInstance();
        if(cal.get(Calendar.HOUR_OF_DAY) < 1){
            cal.add(Calendar.DATE, -1);
        }

        Date currentTime = cal.getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);

        answer1 = answer2 = answer3 = answer4 = answer5 = 5; // initialize

        switch (emaOrder) {
            case EMANUM1:
                emaNumView.setText(getResources().getString(R.string.ema_num_info1));
                break;
            case EMANUM2:
                emaNumView.setText(getResources().getString(R.string.ema_num_info2));
                break;
            case EMANUM3:
                emaNumView.setText(getResources().getString(R.string.ema_num_info3));
                break;
            case EMANUM4:
                emaNumView.setText(getResources().getString(R.string.ema_num_info4));
                break;
            default:
                emaNumView.setText(getResources().getString(R.string.ema_num_info1));
        }

        // Answer check
        question1_group.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.question1_1:
                    answer1 = 0;
                    break;
                case R.id.question1_2:
                    answer1 = 1;
                    break;
                case R.id.question1_3:
                    answer1 = 2;
                    break;
                case R.id.question1_4:
                    answer1 = 3;
                    break;
                case R.id.question1_5:
                    answer1 = 4;
                    break;
                default:
                    answer1 = 5;
            }
        });
        question2_group.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.question2_1:
                    answer2 = 0;
                    break;
                case R.id.question2_2:
                    answer2 = 1;
                    break;
                case R.id.question2_3:
                    answer2 = 2;
                    break;
                case R.id.question2_4:
                    answer2 = 3;
                    break;
                case R.id.question2_5:
                    answer2 = 4;
                    break;
                default:
                    answer2 = 5;
            }
        });
        question3_group.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.question3_1:
                    answer3 = 4;
                    break;
                case R.id.question3_2:
                    answer3 = 3;
                    break;
                case R.id.question3_3:
                    answer3 = 2;
                    break;
                case R.id.question3_4:
                    answer3 = 1;
                    break;
                case R.id.question3_5:
                    answer3 = 0;
                    break;
                default:
                    answer3 = 5;
            }
        });
        question4_group.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.question4_1:
                    answer4 = 4;
                    break;
                case R.id.question4_2:
                    answer4 = 3;
                    break;
                case R.id.question4_3:
                    answer4 = 2;
                    break;
                case R.id.question4_4:
                    answer4 = 1;
                    break;
                case R.id.question4_5:
                    answer4 = 0;
                    break;
                default:
                    answer4 = 5;
            }
        });
        question5_1_btn.setOnClickListener(view -> {
            answer5 = 0;
            question5_1_btn.setSelected(true);
            question5_2_btn.setSelected(false);
            question5_3_btn.setSelected(false);
        });
        question5_2_btn.setOnClickListener(view -> {
            answer5 = 1;
            question5_1_btn.setSelected(false);
            question5_2_btn.setSelected(true);
            question5_3_btn.setSelected(false);
        });
        question5_3_btn.setOnClickListener(view -> {
            answer5 = 2;
            question5_1_btn.setSelected(false);
            question5_2_btn.setSelected(false);
            question5_3_btn.setSelected(true);
        });
    }

    public void emaSubmit() {
        long timestamp = System.currentTimeMillis();

        if (answer1 == 5 || answer2 == 5 || answer3 == 5 || answer4 == 5 || answer5 == 5) {
            Toast.makeText(getApplicationContext(), "모든 문항에 응답해주세요!", Toast.LENGTH_LONG).show();
            return;
        }

        //go to tag activity
        Intent intent = new Intent(this, TagActivity.class);
        intent.putExtra("timestamp", timestamp);
        intent.putExtra("daynum", todayNum);
        intent.putExtra("emaorder", emaOrder);
        intent.putExtra("answer1", answer1);
        intent.putExtra("answer2", answer2);
        intent.putExtra("answer3", answer3);
        intent.putExtra("answer4", answer4);
        intent.putExtra("answer5", answer5);
        startActivity(intent);


        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(EMA_NOTI_ID);
        }

        SharedPreferences emaSubmitCheckPrefs = getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
        SharedPreferences.Editor emaSubmitEditor = emaSubmitCheckPrefs.edit();
        String emaSubmit = "ema_submit_check_" + emaOrder;
        Calendar cal = Calendar.getInstance();
        emaSubmitEditor.putBoolean(emaSubmit, true);
        emaSubmitEditor.putInt("emaSubmitDate", cal.get(Calendar.DATE));
        emaSubmitEditor.apply();

//        Toast.makeText(this, "Response saved", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }


}
