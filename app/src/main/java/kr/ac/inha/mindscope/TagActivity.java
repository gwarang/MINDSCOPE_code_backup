package kr.ac.inha.mindscope;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static kr.ac.inha.mindscope.Tools.timeTheDayNumIsChanged;


public class TagActivity extends AppCompatActivity {

    public static final int DIALOG_ENALBE = 1;
    public static final int DIALOG_DISNALBE = 0;
    private static final String TAG = "TagActivity";
    private static final String SAVE = "저장";
    private static final String DONE = "완료";

    TextView tag3hoursView;
    Button tagBtn;
    EditText inputTag;
    LinearLayout loadingLayout;

    long timestamp;
    long dayNum;
    int emaOrder;
    int answer1, answer2, answer3, answer4, answer5;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        Intent intent = getIntent();
        timestamp = intent.getLongExtra("timestamp", 0);
        dayNum = intent.getLongExtra("daynum", 0);
        emaOrder = intent.getIntExtra("emaorder", 0);
        answer1 = intent.getIntExtra("answer1", 5);
        answer2 = intent.getIntExtra("answer2", 5);
        answer3 = intent.getIntExtra("answer3", 5);
        answer4 = intent.getIntExtra("answer4", 5);
        answer5 = intent.getIntExtra("answer5", 5);



        loadingLayout = findViewById(R.id.loading_frame_tag);
        loadingLayout.setVisibility(View.GONE);
        tag3hoursView = findViewById(R.id.tag_3hours_list);
        tagBtn = findViewById(R.id.toolbar_tag_btn);
        inputTag = findViewById(R.id.tag_edit);
        inputTag.addTextChangedListener(textWatcher);
        inputTag.setOnFocusChangeListener(focusChangeListener);

        SharedPreferences prefs = getSharedPreferences("hashtags", MODE_PRIVATE);
        String str3hoursago = prefs.getString("lasthashtags", "");
        tag3hoursView.setText(str3hoursago);


        inputTag.setHint("#내상태를 #기록해주세요 #을붙이셔야 #적용됩니다.");

        // Toolbar
        toolbar = findViewById(R.id.toolbar_tag);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        // endregion

        tagBtn.setOnClickListener(clickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tools.saveApplicationLog(getApplicationContext(), TAG, Tools.ACTION_OPEN_PAGE);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (DbMgr.getDB() == null)
                DbMgr.init(getApplicationContext());

            loadingLayout.setVisibility(View.VISIBLE);
            tagBtn.setText(DONE);

            String hashTagStr = String.valueOf(inputTag.getText());
            String patternst = "#[A-Za-z0-9ㄱ-ㅎㅏ-ㅣ가-힣]{1,30}";
            Pattern pattern = Pattern.compile(patternst);
            Matcher matcher = pattern.matcher(hashTagStr);
            List<String> tags = new ArrayList<>();
            StringBuilder forMyTags = new StringBuilder();
            int i = 0;
            while(matcher.find()){
                tags.add(matcher.group());
                forMyTags.append(matcher.group()).append(" ");
            }
            Log.d(TAG, forMyTags.toString());

            SharedPreferences tagsPrefs = getSharedPreferences("EMA_Tags", MODE_PRIVATE);
            SharedPreferences.Editor tagsPrefsEditor = tagsPrefs.edit();

            long saveTimestamp = fixTimestamp(timestamp, emaOrder);
            String saveTags = "";

            SharedPreferences prefs = getSharedPreferences("Configurations", Context.MODE_PRIVATE);
            int dataSourceId = prefs.getInt("REPORT_TAGS", -1);
            assert dataSourceId != -1;
            long temp = 0;
            for(String tag : tags){

                DbMgr.saveMixedData(dataSourceId, timestamp + temp, 1.0f, timestamp+temp, dayNum, emaOrder, tag);
                Log.d(TAG, "submit test : " + (timestamp + temp) + " " + tag);
                temp++;
                Tools.saveApplicationLog(getApplicationContext(), TAG, "SAVE_EACH_EMA_TAG_IN_LOCAL");
                if(saveTags.equals("")){
                    saveTags = tag;
                }else{
                    saveTags += " " + tag;
                }
            }
            tagsPrefsEditor.putString(String.valueOf(saveTimestamp), answer5 + "_" + saveTags);
            tagsPrefsEditor.apply();


            String answers = String.format(Locale.US, "%d %d %d %d %d",
                    answer1,
                    answer2,
                    answer3,
                    answer4,
                    answer5);

            Log.d(TAG, "answer " + answers);

            int dataSourceId2 = prefs.getInt("SURVEY_EMA", -1);
            assert dataSourceId2 != -1;
            Log.d(TAG, "SURVEY_EMA dataSourceId: " + dataSourceId2);
            if (emaOrder != 0) {
                DbMgr.saveMixedData(dataSourceId2, timestamp, 1.0f, timestamp, emaOrder, answers);
                Tools.saveApplicationLog(getApplicationContext(), TAG, "SAVE_EMA_IN_LOCAL");
            }

            SharedPreferences emaSubmitCheckPrefs = getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
            SharedPreferences.Editor emaSubmitEditor = emaSubmitCheckPrefs.edit();
            String emaSubmit = "ema_submit_check_" + emaOrder;
            Calendar cal = Calendar.getInstance();
            emaSubmitEditor.putBoolean(emaSubmit, true);
            if(cal.get(Calendar.HOUR_OF_DAY) < timeTheDayNumIsChanged){
                cal.add(Calendar.DATE, -1);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 0);
            }
            emaSubmitEditor.putInt("emaSubmitDate", cal.get(Calendar.DATE));
            emaSubmitEditor.apply();

            SharedPreferences hashtagsPrefs = getSharedPreferences("hashtags", MODE_PRIVATE);
            SharedPreferences.Editor editor = hashtagsPrefs.edit();
            editor.putString("lasthashtags", forMyTags.toString());
            editor.apply();

            Context context = getApplicationContext();
            Tools.updatePoint(context);
            Tools.saveApplicationLog(context, TAG, Tools.ACTION_CLICK_COMPLETE_BUTTON, tags);

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("get_point", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            finish();
            startActivity(intent);

        }
    };

    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if(b){
                Log.d(TAG, "focus true");
                String s = inputTag.getText().toString();
                if(inputTag.getText().toString().equals("")){
                    Log.d(TAG, "add first #");
                    inputTag.setText("#");
                    inputTag.setSelection(inputTag.getText().length());
                }
            }
        }
    };

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            if(inputTag.isFocusable() && charSequence.equals("")){
//                inputTag.setText("#");
//            }
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.d(TAG, "onTextChanged");

            if(inputTag.isFocusable()){
//                if(charSequence.length() == 1 && charSequence.toString() != "#" && charSequence.toString() != " "){
//                    inputTag.setText("#"+inputTag.getText());
//                    inputTag.setSelection(inputTag.getText().length());
//                }
                if(charSequence.length() > 0 && charSequence.charAt(charSequence.length()-1) == ' '){
                    inputTag.setText(inputTag.getText().toString().replace(' ', '#'));
                    inputTag.setSelection(inputTag.getText().length());
                    Log.d(TAG, "add #");
                }
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_save:
                break;
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private int getEmaOrderHour(int emaOrder) {
        switch (emaOrder) {
            case 1:
                return 11;
            case 2:
                return 15;
            case 3:
                return 19;
            case 4:
                return 23;
            default:
                return -1;
        }
    }

    private long fixTimestamp(long timestamp, int emaOrder) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        if(c.get(Calendar.HOUR_OF_DAY) < timeTheDayNumIsChanged){
            c.add(Calendar.DATE, -1);
        }
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), getEmaOrderHour(emaOrder), 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}