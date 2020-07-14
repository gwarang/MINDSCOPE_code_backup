package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.InterventionSaveActivity;
import kr.ac.inha.mindscope.R;

public class CareChildFragment2 extends Fragment {

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
        }
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
        }
    };

    View.OnClickListener clickLoadBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO 인터벤션 리스트 얻어오고 동적으로 view 뿌려주는 기능 구현
        }
    };
}