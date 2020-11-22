package kr.ac.inha.mindscope.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import kr.ac.inha.mindscope.R;

public class AnalysisSurveyDialog extends Dialog {

    private Context context;
    CheckBox cb1, cb2, cb3, cb4, cb5;
    EditText editText;
    Button nextBtn, submitBtn;

    private View.OnClickListener nextBtnListener;
    private View.OnClickListener submitBtnListener;

    SharedPreferences stressReportPrefs;
    SharedPreferences.Editor prefsEditor;

    public AnalysisSurveyDialog(@NonNull Context context, View.OnClickListener clickListener, View.OnClickListener clickListener2) {
        super(context);
        this.context = context;
        nextBtnListener = clickListener;
        submitBtnListener = clickListener2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_dialog);

        stressReportPrefs = context.getSharedPreferences("stressReport", Context.MODE_PRIVATE);
        prefsEditor = stressReportPrefs.edit();

        cb1 = findViewById(R.id.survey_check1);
        cb2 = findViewById(R.id.survey_check2);
        cb3 = findViewById(R.id.survey_check3);
        cb4 = findViewById(R.id.survey_check4);
        cb5 = findViewById(R.id.survey_check5);
        editText = findViewById(R.id.survey_check5_edittext);
        nextBtn = findViewById(R.id.survey_next_btn);
        submitBtn = findViewById(R.id.survey_submit_btn);
        editText.setEnabled(false);

        cb1.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefsEditor.putBoolean("cb1", cb1.isChecked());
                prefsEditor.apply();
            }
        });
        cb2.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefsEditor.putBoolean("cb2", cb2.isChecked());
                prefsEditor.apply();
            }
        });
        cb3.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefsEditor.putBoolean("cb3", cb3.isChecked());
                prefsEditor.apply();
            }
        });
        cb4.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefsEditor.putBoolean("cb4", cb4.isChecked());
                prefsEditor.apply();
            }
        });
        cb5.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefsEditor.putBoolean("cb5", cb5.isChecked());
                prefsEditor.apply();

                if(cb5.isChecked()){
                    editText.setEnabled(true);
                }
                else {
                    editText.setEnabled(false);
                }
            }
        });
//        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                Log.d("survey dialog", "in focus change");
//                if(!b){
//                    Toast.makeText(context, "focus loose", Toast.LENGTH_SHORT).show();
//                    Log.d("survey dialog", "!b 일때 로그");
//                    prefsEditor.putString("cb5_str", editText.getText().toString().replace(" ", "_"));
//                    prefsEditor.apply();
//                }else{
//                    Toast.makeText(context, "get focus", Toast.LENGTH_SHORT).show();
//                    Log.d("survey dialog", "b 일때 로그");
//                }
//            }
//        });
        editText.addTextChangedListener(textWatcher);

        nextBtn.setOnClickListener(nextBtnListener);
        submitBtn.setOnClickListener(submitBtnListener);

    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.d("survey dialog", "onTextChanged");
            if(!editText.getText().toString().equals("")){
                prefsEditor.putString("cb5_str", editText.getText().toString().replace(" ", "_"));
                prefsEditor.apply();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


}
