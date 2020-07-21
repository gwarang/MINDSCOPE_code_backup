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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.InterventionSaveActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.services.InterventionService;

import static android.content.Context.MODE_PRIVATE;

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

    ArrayList<String[]> todayPerformedInterventions;

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
        SharedPreferences interventionPrefs = getActivity().getSharedPreferences("intervention", MODE_PRIVATE);


        editInterventionBtn.setOnClickListener(clickEditBtn);
        makeInterventionBtn.setOnClickListener(clickMakeBtn);
        loadInterventionBtn.setOnClickListener(clickLoadBtn);

        interventionSwitch.setChecked(!interventionPrefs.getBoolean("muteToday", true));


        interventionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                SharedPreferences.Editor editor = interventionPrefs.edit();
                if (isChecked) {
                    editor.putBoolean("muteToday", false);
                } else {
                    editor.putBoolean("muteToday", true);
                }
                editor.apply();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodayPerformedIntervention();
    }

    public void init(View view) {
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
        interventionSwitch.setOnCheckedChangeListener(switchListener);

        SharedPreferences prefs = getActivity().getSharedPreferences("intervention", MODE_PRIVATE);
        String curIntervention = prefs.getString("curIntervention", "");
        if (!curIntervention.equals("")) {
            currentIntervention.setText(curIntervention);
            currentInterventionContainer.setVisibility(View.VISIBLE);
            makeInterventionBtn.setText(getString(R.string.string_child2_do_intervention));
        } else {
            currentInterventionContainer.setVisibility(View.INVISIBLE);
            makeInterventionBtn.setText(getString(R.string.string_child2_make_intervention));
        }

        updateRecommendInterventions();
        loadTodayPerformedIntervention();


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
            if (makeInterventionBtn.getText().toString().equals(getString(R.string.string_child2_make_intervention))) {
                Intent intent = new Intent(getActivity(), InterventionSaveActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getContext(), InterventionService.class);
                intent.putExtra("stress_do_intervention", true);
                intent.putExtra("path", 0);
                requireActivity().startService(intent);

                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                String newPerformed = doneInterventionText.getText() + String.format("%s에 %s을/를 수행하였습니다.\n",dateFormat.format(System.currentTimeMillis()), currentIntervention.getText());

                doneInterventionText.setText(newPerformed);
            }

        }
    };

    View.OnClickListener clickLoadBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateRecommendInterventions();
        }
    };

    CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            SharedPreferences prefs = getActivity().getSharedPreferences("intervention", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            if (isChecked) {
                Log.i(TAG, "오늘의 알림 받기 설정");
                editor.putBoolean("muteToday", true);
            } else {
                Log.i(TAG, "오늘의 알림 받기 설정 해제");
                editor.putBoolean("muteToday", false);
            }
            editor.apply();

        }
    };

    public void updateRecommendInterventions() {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(getActivity().getAssets().open("zaturi_interventions.csv"));
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

//            if (interventionSplit != null)
//                for(String intervention : interventionSplit)
//                    Log.e(TAG, intervention);


        String packName = getContext().getPackageName();
        Random random = new Random();
        int[] randomNums = new int[3];
        for (int i = 0; i < 3; i++) {
            randomNums[i] = random.nextInt(interventionSplit.length);
        }

        recommandIntervention1.setText("#" + interventionSplit[randomNums[0]]);
        recommandIntervention2.setText("#" + interventionSplit[randomNums[1]]);
        recommandIntervention3.setText("#" + interventionSplit[randomNums[2]]);

    }

    public void loadTodayPerformedIntervention() {
        new Thread(() -> {
            todayPerformedInterventions = new ArrayList<>();
            SharedPreferences loginPrefs = getActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
            SharedPreferences configPrefs = getActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

            Calendar fromCalendar = Calendar.getInstance();
            fromCalendar.set(Calendar.MILLISECOND, 0);
            fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
            fromCalendar.set(Calendar.MINUTE, 0);
            fromCalendar.set(Calendar.SECOND, 0);
            Calendar tillCalendar = Calendar.getInstance();
            tillCalendar.set(Calendar.MILLISECOND, 0);
            tillCalendar.set(Calendar.HOUR_OF_DAY, 23);
            tillCalendar.set(Calendar.MINUTE, 59);
            tillCalendar.set(Calendar.SECOND, 59);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Log.i(TAG, "initialize fromCalendar: " + format.format(fromCalendar.getTime()));
            Log.i(TAG, "initialize tillCalendar: " + format.format(tillCalendar.getTime()));

            ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

            ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);

            EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                    .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                    .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                    .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                    .setTargetDataSourceId(configPrefs.getInt("STRESS_INTERVENTION", -1))
                    .setFromTimestamp(fromCalendar.getTimeInMillis())
                    .setTillTimestamp(tillCalendar.getTimeInMillis())
                    .build();

            final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
            if (responseMessage.getDoneSuccessfully()) {
                List<String> values = responseMessage.getValueList();
                if (!values.isEmpty()) {
                    Log.d(TAG, "intervention list " + values);
                    for (String value : values) {
                        String[] splitValue = value.split(" ");
                        if (splitValue[1] != null && !splitValue[1].equals("") && splitValue[1].charAt(0) == '#') {
                            Log.e(TAG, "intervention is : " + splitValue[1]);
                            todayPerformedInterventions.add(splitValue);
                        } else
                            Log.e(TAG, "no intervention");
                    }
                } else {
                    Log.d(TAG, "values empty");
                }
            }

            getActivity().runOnUiThread(() -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                String didInterventionStr = "";
                for (String[] s : todayPerformedInterventions) {
                    String timeText = dateFormat.format(Long.parseLong(s[0]));
                    didInterventionStr += String.format("%s에 \'%s\'을/를 수행하였습니다.\n", timeText, s[1].substring(1));
                }
                if (!didInterventionStr.equals("")) {
                    doneInterventionText.setText(didInterventionStr);
                    doneInterventionText.setVisibility(View.VISIBLE);
                } else {
                    doneInterventionText.setVisibility(View.INVISIBLE);
                }
            });
            channel.shutdown();
        }).start();


//
    }
}