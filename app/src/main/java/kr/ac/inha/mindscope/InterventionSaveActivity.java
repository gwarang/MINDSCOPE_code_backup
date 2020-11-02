package kr.ac.inha.mindscope;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

public class InterventionSaveActivity extends AppCompatActivity {

    private static final String TAG = "IntervSaveActivity";
    Toolbar toolbar;
    EditText interventionEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intervention);


        toolbar = (Toolbar) findViewById(R.id.toolbar_intervention_save);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        interventionEdit = findViewById(R.id.input_intervention);
//        interventionEdit.setOnFocusChangeListener(focusChangeListener);
        interventionEdit.addTextChangedListener(textWatcher);
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            interventionEdit.setHint(intent.getExtras().getString("currentIntervention"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentIntent = NavUtils.getParentActivityIntent(this);
                assert parentIntent != null;
                parentIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(parentIntent);
                finish();
                return true;
            case R.id.toolbar_save:
                if (interventionEdit.getText().toString().equals("")) {
                    Toast.makeText(this, "스트레스 해소 방안을 입력해주세요!", Toast.LENGTH_SHORT).show();
                } else {
                    String interventionString = interventionEdit.getText().toString();
                    interventionString = interventionString.replace(" ", "_");
                    Toast.makeText(this, "새로운 스트레스 해소방안 저장!", Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = getSharedPreferences("intervention", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("curIntervention", interventionString);
                    editor.putBoolean("didIntervention", false);
                    editor.apply();
                    Calendar cal = Calendar.getInstance();
                    String curIntervention = prefs.getString("curIntervention", "WRONG_INTERVENTION");
                    assert curIntervention != null;
                    if (curIntervention.contains(" "))
                        curIntervention = curIntervention.replace(" ", "_");
                    Tools.saveStressIntervention(getApplicationContext(), cal.getTimeInMillis(), curIntervention, Tools.STRESS_CONFIG,0);
                    finish();
                }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_btn_save, menu);
        return true;
    }

//    View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
//        @Override
//        public void onFocusChange(View view, boolean b) {
//            if(b){
//                Log.d(TAG, "focus true");
//                String s = interventionEdit.getText().toString();
//                if(interventionEdit.getText().toString().equals("")){
//                    Log.d(TAG, "add first #");
//                    interventionEdit.setText("#");
//                    interventionEdit.setSelection(interventionEdit.getText().length());
//                }
//            }
//        }
//    };

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

            if(interventionEdit.isFocusable()){
//                if(charSequence.length() == 1 && charSequence.toString() != "#" && charSequence.toString() != " "){
//                    inputTag.setText("#"+inputTag.getText());
//                    inputTag.setSelection(inputTag.getText().length());
//                }
                if(charSequence.length() > 0 && charSequence.charAt(charSequence.length()-1) == ' '){
                    interventionEdit.setText(interventionEdit.getText().toString().replace(' ', '_'));
                    interventionEdit.setSelection(interventionEdit.getText().length());
                    Log.d(TAG, "add #");
                }
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
}