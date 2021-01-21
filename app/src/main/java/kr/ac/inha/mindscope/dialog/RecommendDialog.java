package kr.ac.inha.mindscope.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;

import static android.content.Context.MODE_PRIVATE;

public class RecommendDialog extends Dialog {

    public static final String TAG = "RecommendDialog";
    public static final String ACTION_SHOW_ALL_INTERVENTION = "ACTION_SHOW_ALL_INTERVENTION";


    private View.OnClickListener mBtnListener;

    TextView title;
    RecyclerView recyclerView;
    RadioButton radioButton;
    Button button_confirm, button_cancel;
    Context context;

    public RecommendDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        Objects.requireNonNull(getWindow()).setAttributes(layoutParams);

        setContentView(R.layout.recommend_list_dialog);

        title = findViewById(R.id.intervention_list_title);
        button_confirm = findViewById(R.id.intervention_list_btn);
        button_cancel = findViewById(R.id.intervention_list_btn2);
        button_confirm.setOnClickListener(mBtnListener);
        button_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
                    public void onClick(View view)
            {
                dismiss();
            }
        });

        recyclerView = findViewById(R.id.intervention_list_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        RecommendAdapter adapter = new RecommendAdapter(recommendList());
        recyclerView.setAdapter(adapter);

    }

    public RecommendDialog(Context context, View.OnClickListener clickListener){
        super(context);
        this.context = context;
        this.mBtnListener = clickListener;
    }

    public ArrayList<String> recommendList(){
        ArrayList<String> list = new ArrayList<>();

        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(context.getAssets().open("zaturi_interventions.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String interventions;
        String[] interventionSplit = null;
        while (true) {
            try {
                if ((interventions = reader.readLine()) != null)
                    interventionSplit = interventions.split(",");
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(interventionSplit != null){
            list.addAll(Arrays.asList(interventionSplit));
        }

        Calendar cal = Calendar.getInstance();
        SharedPreferences prefs = context.getSharedPreferences("intervention", MODE_PRIVATE);
        String curIntervention = prefs.getString("curIntervention", "");
        assert curIntervention != null;
        if (curIntervention.contains(" "))
            curIntervention = curIntervention.replace(" ", "_");
        Tools.saveStressIntervention(context, cal.getTimeInMillis(), curIntervention, Tools.STRESS_OTHER_RECOMMENDATION, 0);
        Tools.saveApplicationLog(context, TAG, ACTION_SHOW_ALL_INTERVENTION);

        return list;
    }

}
