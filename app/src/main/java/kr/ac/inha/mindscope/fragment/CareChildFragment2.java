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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.InterventionSaveActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;

import static android.content.Context.MODE_PRIVATE;
import static kr.ac.inha.mindscope.Tools.STRESS_DO_DIFF_INTERVENTION;
import static kr.ac.inha.mindscope.Tools.STRESS_DO_INTERVENTION;

public class CareChildFragment2 extends Fragment {

    private static final String TAG = "CareChildFragment2";
    private static final String ACTION_CLICK_MAKE_INTERVENTION = "CLICK_MAKE_INTERVENTION";
    private static final String ACTION_CLICK_LOAD_OTHER_INTERVENTION = "CLICK_LOAD_OTHER_INTERVENTION";
    private static final String ACTION_CLICK_MUTE_TODAY_INTERVENTION = "CLICK_MUTE_TODAY_INTERVENTION";
    private static final String ACTION_CLICK_OTHER_INTERVENTION = "CLICK_OTHER_INTERVENTION";

    ConstraintLayout currentInterventionContainer;

    Button editInterventionBtn;
    Button makeInterventionBtn;
    Button loadInterventionBtn;

    TextView doneInterventionText;
    TextView recommendIntervention1;
    TextView recommendIntervention2;
    TextView recommendIntervention3;
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
        SharedPreferences interventionPrefs = requireActivity().getSharedPreferences("intervention", MODE_PRIVATE);


        interventionSwitch.setChecked(!interventionPrefs.getBoolean("muteToday", false));
        SharedPreferences.Editor editor = interventionPrefs.edit();
        //region clickListener
        editInterventionBtn.setOnClickListener(clickEditBtn);
        makeInterventionBtn.setOnClickListener(clickMakeBtn);
        loadInterventionBtn.setOnClickListener(clickLoadBtn);
//        interventionSwitch.setOnCheckedChangeListener(switchListener);
        interventionSwitch.setOnClickListener(switchClickListener);

        recommendIntervention1.setOnClickListener(view1 -> {
            editor.putString("curIntervention", recommendIntervention1.getText().toString());
            editor.putBoolean("didIntervention", false);
            editor.apply();
            Calendar cal = Calendar.getInstance();
            String curIntervention = interventionPrefs.getString("curIntervention", "");
            currentIntervention.setText(curIntervention);
            currentInterventionContainer.setVisibility(View.VISIBLE);
            makeInterventionBtn.setText(getString(R.string.string_child2_do_intervention));
            assert curIntervention != null;
            Tools.saveStressIntervention(requireContext(), cal.getTimeInMillis(), curIntervention, Tools.STRESS_CONFIG, 0);
            Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_OTHER_INTERVENTION, curIntervention);
        });
        recommendIntervention2.setOnClickListener(view12 -> {
            editor.putString("curIntervention", recommendIntervention2.getText().toString());
            editor.putBoolean("didIntervention", false);
            editor.apply();
            Calendar cal = Calendar.getInstance();
            String curIntervention = interventionPrefs.getString("curIntervention", "");
            currentIntervention.setText(curIntervention);
            currentInterventionContainer.setVisibility(View.VISIBLE);
            makeInterventionBtn.setText(getString(R.string.string_child2_do_intervention));
            assert curIntervention != null;
            Tools.saveStressIntervention(requireContext(), cal.getTimeInMillis(), curIntervention, Tools.STRESS_CONFIG, 0);
            Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_OTHER_INTERVENTION, curIntervention);
        });
        recommendIntervention3.setOnClickListener(view13 -> {
            editor.putString("curIntervention", recommendIntervention3.getText().toString());
            editor.putBoolean("didIntervention", false);
            editor.apply();
            Calendar cal = Calendar.getInstance();
            String curIntervention = interventionPrefs.getString("curIntervention", "");
            currentIntervention.setText(curIntervention);
            currentInterventionContainer.setVisibility(View.VISIBLE);
            makeInterventionBtn.setText(getString(R.string.string_child2_do_intervention));
            assert curIntervention != null;
            Tools.saveStressIntervention(requireContext(), cal.getTimeInMillis(), curIntervention, Tools.STRESS_CONFIG, 0);
            Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_OTHER_INTERVENTION, curIntervention);
        });
        //endregion

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodayPerformedIntervention();
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
        SharedPreferences lastPagePrefs = requireActivity().getSharedPreferences("LastPage", Context.MODE_PRIVATE);
        SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
        lastPagePrefsEditor.putInt("last_open_tab_position", 1);
        lastPagePrefsEditor.apply();
    }


    public void init(View view) {
        currentInterventionContainer = view.findViewById(R.id.intervention_container);
        editInterventionBtn = view.findViewById(R.id.child2_edit_btn);
        makeInterventionBtn = view.findViewById(R.id.child2_make_btn);
        loadInterventionBtn = view.findViewById(R.id.child2_load_btn);

        doneInterventionText = view.findViewById(R.id.child2_txt3);
        recommendIntervention1 = view.findViewById(R.id.child2_recommend1);
        recommendIntervention2 = view.findViewById(R.id.child2_recommend2);
        recommendIntervention3 = view.findViewById(R.id.child2_recommend3);

        currentIntervention = view.findViewById(R.id.child2_intervention);

        interventionSwitch = view.findViewById(R.id.child2_switch);

        SharedPreferences prefs = requireActivity().getSharedPreferences("intervention", MODE_PRIVATE);
        String curIntervention = prefs.getString("curIntervention", "");
        assert curIntervention != null;
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
                Tools.saveApplicationLog(getContext(), TAG, ACTION_CLICK_MAKE_INTERVENTION);
            } else {

                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String curPerformed = String.format("%s에 '%s'을/를 수행하였습니다.",dateFormat.format(System.currentTimeMillis()), currentIntervention.getText().toString().replace("#", ""));
                String newPerformedList = doneInterventionText.getText() + curPerformed + '\n';

                Toast.makeText(requireContext(), curPerformed, Toast.LENGTH_SHORT).show();

                Calendar cal = Calendar.getInstance();
                SharedPreferences prefs = requireActivity().getSharedPreferences("intervention", MODE_PRIVATE);
                String curIntervention = prefs.getString("curIntervention", "");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("didIntervention", true);
                editor.putInt("performedDate", cal.get(Calendar.DATE));
                editor.apply();
                assert curIntervention != null;
                Tools.saveStressIntervention(requireContext(), cal.getTimeInMillis(), curIntervention, STRESS_DO_INTERVENTION, 0);
//                loadTodayPerformedIntervention();
                doneInterventionText.setText(newPerformedList);
                doneInterventionText.setVisibility(View.VISIBLE);
            }

        }
    };

    View.OnClickListener clickLoadBtn = view -> updateRecommendInterventions();

    View.OnClickListener switchClickListener = view -> {
        SharedPreferences prefs = requireActivity().getSharedPreferences("intervention", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (!prefs.getBoolean("muteToday", false)) {
            editor.putBoolean("muteToday", true);
            Calendar cal = Calendar.getInstance();
            String curIntervention = prefs.getString("curIntervention", "");
            assert curIntervention != null;
            Tools.saveStressIntervention(requireContext(), cal.getTimeInMillis(), curIntervention, Tools.STRESS_MUTE_TODAY, 0);
        } else {
            editor.putBoolean("muteToday", false);
            Calendar cal = Calendar.getInstance();
            String curIntervention = prefs.getString("curIntervention", "");
            assert curIntervention != null;
            Tools.saveStressIntervention(requireContext(), cal.getTimeInMillis(), curIntervention, Tools.STRESS_UNMUTE_TODAY, 0);
        }
        editor.apply();
    };


    public void updateRecommendInterventions() {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(requireActivity().getAssets().open("zaturi_interventions.csv"));
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

        String packName = requireContext().getPackageName();
        Random random = new Random();
        int[] randomNums = new int[3];
        for (int i = 0; i < 3; i++) {
            assert interventionSplit != null;
            randomNums[i] = random.nextInt(interventionSplit.length);
        }

        recommendIntervention1.setText(String.format("#%s", interventionSplit[randomNums[0]]));
        recommendIntervention2.setText(String.format("#%s", interventionSplit[randomNums[1]]));
        recommendIntervention3.setText(String.format("#%s", interventionSplit[randomNums[2]]));

        Calendar cal = Calendar.getInstance();
        SharedPreferences prefs = requireActivity().getSharedPreferences("intervention", MODE_PRIVATE);
        String curIntervention = prefs.getString("curIntervention", "");
        assert curIntervention != null;
        Tools.saveStressIntervention(requireContext(), cal.getTimeInMillis(), curIntervention, Tools.STRESS_OTHER_RECOMMENDATION, 0);
        Tools.saveApplicationLog(requireContext(), TAG, ACTION_CLICK_LOAD_OTHER_INTERVENTION);

    }

    public void loadTodayPerformedIntervention() {
        if (Tools.isNetworkAvailable()) {
            new Thread(() -> {
                todayPerformedInterventions = new ArrayList<>();
                SharedPreferences loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                SharedPreferences configPrefs = requireActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

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

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Log.d(TAG, "initialize fromCalendar: " + format.format(fromCalendar.getTime()));
                Log.d(TAG, "initialize tillCalendar: " + format.format(tillCalendar.getTime()));

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();

                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);


                EtService.RetrieveFilteredDataRecords.Request retrieveFilteredEMARecordsRequestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                        .setTargetDataSourceId(configPrefs.getInt("STRESS_INTERVENTION", -1))
                        .setFromTimestamp(fromCalendar.getTimeInMillis())
                        .setTillTimestamp(tillCalendar.getTimeInMillis())
                        .build();

                try {
                    final EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredEMARecordsRequestMessage);
                    if (responseMessage.getSuccess()) {
                        List<String> values = responseMessage.getValueList();
                        if (!values.isEmpty()) {
                            Log.d(TAG, "intervention list " + values);
                            for (String value : values) {
                                String[] splitValue = value.split(" ");
                                if (splitValue[1] != null && !splitValue[1].equals("") && splitValue[1].charAt(0) == '#'
                                        && (((Integer.parseInt(splitValue[2]) == STRESS_DO_INTERVENTION)
                                        || (Integer.parseInt(splitValue[2]) == STRESS_DO_DIFF_INTERVENTION)))) {
                                    Log.d(TAG, "intervention is : " + splitValue[1]);
                                    todayPerformedInterventions.add(splitValue);
                                } else
                                    Log.d(TAG, "no intervention");
                            }
                        } else {
                            Log.d(TAG, "values empty");
                        }
                    }
                }catch (StatusRuntimeException | NumberFormatException e){
                    e.printStackTrace();
                }

                if(isAdded()){
                    requireActivity().runOnUiThread(() -> {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        StringBuilder didInterventionStr = new StringBuilder();

                        for (String[] s : todayPerformedInterventions) {
                            String timeText = dateFormat.format(Long.parseLong(s[0]));
                            didInterventionStr.append(String.format("%s에 '%s'을/를 수행하였습니다.\n", timeText, s[1].substring(1)));
                        }
                        if (!didInterventionStr.toString().equals("")) {
                            Log.d(TAG, "load performed intervention : " + didInterventionStr);
                            doneInterventionText.setText(didInterventionStr.toString());
                            doneInterventionText.setVisibility(View.VISIBLE);
                        } else {
                            doneInterventionText.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                channel.shutdown();
            }).start();
        } else {
            Toast.makeText(requireContext(), requireContext().getResources().getString(R.string.when_network_unable), Toast.LENGTH_SHORT).show();
        }


//
    }
}