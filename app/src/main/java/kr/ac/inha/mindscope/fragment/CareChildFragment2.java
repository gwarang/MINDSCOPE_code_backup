package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.InterventionSaveActivity;
import kr.ac.inha.mindscope.R;

public class CareChildFragment2 extends Fragment {

    private static final String TAG = "CareChildFragment2";

    ConstraintLayout currentInterventionContainer;

    Button editInterventionBtn;
    Button makeInterventionBtn;
    Button loadInterventionBtn;

    TextView doneInterventionText;
    TextView recommandIntervention1;
    TextView recommandIntervention2;
    TextView recommandIntervention3;
    TextView currentIntervention;

    Switch interventionSwitch;

    public CareChildFragment2() {
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
        View view = inflater.inflate(R.layout.fragment_care_child2, container, false);

        init(view);

        editInterventionBtn.setOnClickListener(clickEditBtn);
        makeInterventionBtn.setOnClickListener(clickMakeBtn);
        loadInterventionBtn.setOnClickListener(clickLoadBtn);

        // TODO 1) 인터벤션 리스트 얻어와서 recommand에 뿌려주기
        //  2) 현재 스트레스 방안 있으면 업데이트하기
        //  3) 현재 수행한 방안 있으면 업데이트하기



        return view;
    }

    public void init(View view){
        currentInterventionContainer = view.findViewById(R.id.intervention_container);
        editInterventionBtn = view.findViewById(R.id.child2_edit_btn);
        makeInterventionBtn = view.findViewById(R.id.child2_make_btn);
        loadInterventionBtn = view.findViewById(R.id.child2_load_btn);

        doneInterventionText = view.findViewById(R.id.child2_txt3);
        recommandIntervention1 = view.findViewById(R.id.child2_recommend1);
        recommandIntervention2 = view.findViewById(R.id.child2_recommend2);
        recommandIntervention3 = view.findViewById(R.id.child2_recommend3);

        currentIntervention = view.findViewById(R.id.child2_intervention);

        interventionSwitch = view.findViewById(R.id.child2_switch);

        SharedPreferences prefs = getActivity().getSharedPreferences("intervention", Context.MODE_PRIVATE);
        String curIntervention = prefs.getString("curIntervention", "");
        if(!curIntervention.equals("")){
            currentIntervention.setText(curIntervention);
            currentInterventionContainer.setVisibility(View.VISIBLE);
        }else{
            currentInterventionContainer.setVisibility(View.INVISIBLE);
        }

        updateRecommendInterventions();

    }

    View.OnClickListener clickEditBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), InterventionSaveActivity.class);
            intent.putExtra("currentIntervention", currentIntervention.getText().toString());
            startActivity(intent);
        }
    };

    View.OnClickListener clickMakeBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), InterventionSaveActivity.class);
            startActivity(intent);

            // TODO 현재 해소방안이 있는 상태라면, 버스 이름 스트레스 해소하기로 변경하고, 누르면 zaturi에서 해소하기 한것처럼 행동하게 만들기
        }
    };

    View.OnClickListener clickLoadBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateRecommendInterventions();
        }
    };

    public void updateRecommendInterventions(){
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(getActivity().getAssets().open("zaturi_interventions.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String interventions;
        String[] interventionSplit = null;
        while(true) {
            try {
                if ((interventions = reader.readLine()) != null)
                    interventionSplit = interventions.split(",");
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//            if (interventionSplit != null)
//                for(String intervention : interventionSplit)
//                    Log.e(TAG, intervention);


        String packName = getContext().getPackageName();
        Random random = new Random();
        int[] randomNums = new int[3];
        for(int i = 0; i<3; i++){
            randomNums[i] = random.nextInt(interventionSplit.length);
        }

        recommandIntervention1.setText("#" + interventionSplit[randomNums[0]]);
        recommandIntervention2.setText("#" + interventionSplit[randomNums[1]]);
        recommandIntervention3.setText("#" + interventionSplit[randomNums[2]]);

    }
}