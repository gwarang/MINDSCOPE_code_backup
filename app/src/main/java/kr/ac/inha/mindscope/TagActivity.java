package kr.ac.inha.mindscope;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TagActivity extends AppCompatActivity {

    public static final int DIALOG_ENALBE = 1;
    public static final int DIALOG_DISNALBE = 0;
    private static final String TAG = "TagActivity";
    private static final String SAVE = "저장";
    private static final String DONE = "완료";

    TextView tag3hoursView;
    TextView tagNowView;
    Button tagBtn;
    EditText inputTag;

    long timestamp;
    int dayNum;
    int emaOrder;

    LinearLayout hiddenLayout;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        Intent intent = getIntent();
        timestamp = intent.getLongExtra("timestamp", 0);
        dayNum = intent.getIntExtra("daynum", 0);
        emaOrder = intent.getIntExtra("emaorder", 0);

        tag3hoursView = (TextView) findViewById(R.id.tag_3hours_list);
        tagNowView = (TextView) findViewById(R.id.tag_now_my);
        tagBtn = (Button) findViewById(R.id.toolbar_tag_btn);
        inputTag = (EditText) findViewById(R.id.tag_edit);
        hiddenLayout = (LinearLayout) findViewById(R.id.tag_container_my);

        SharedPreferences prefs = getSharedPreferences("hashtags", MODE_PRIVATE);
        String str3hoursago = prefs.getString("lasthashtags", "");
        tag3hoursView.setText(str3hoursago);


        inputTag.setHint("#태그힌트테스트");

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_tag);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        // endregion

        tagBtn.setOnClickListener(clickListener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            hiddenLayout.setVisibility(View.VISIBLE);
            tagBtn.setText(DONE);

            String hashTagStr = String.valueOf(inputTag.getText());
            String patternst = "#[A-Za-z0-9ㄱ-ㅎㅏ-ㅣ가-힣]{1,30}";
            Pattern pattern = Pattern.compile(patternst);
            Matcher matcher = pattern.matcher(hashTagStr);
            List<String> tags = new ArrayList<String>();
            String forMyTags = "";
            int i = 0;
            while(matcher.find()){
                tags.add(matcher.group());
                forMyTags = forMyTags + matcher.group() + " ";
            }
            Log.i(TAG, forMyTags);
            tagNowView.setText(forMyTags);

            SharedPreferences prefs = getSharedPreferences("Configurations", Context.MODE_PRIVATE);
            int dataSourceId = prefs.getInt("REPORT_TAGS", -1);
            assert dataSourceId != -1;
            Log.i(TAG, "REPORT_TAGS dataSourceId: " + dataSourceId);
            for(String tag : tags){
                DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f, timestamp, dayNum, emaOrder, tag);
            }

            SharedPreferences hashtagsPrefs = getSharedPreferences("hashtags", MODE_PRIVATE);
            SharedPreferences.Editor editor = hashtagsPrefs.edit();
            editor.putString("lasthashtags", tagNowView.getText().toString());
            editor.apply();


            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("get_point", true);
            finish();
            startActivity(intent);

        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.toolbar_save:
                // TODO 기록 태그 저장
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
        hiddenLayout.setVisibility(View.GONE);
        super.onDestroy();
    }
}