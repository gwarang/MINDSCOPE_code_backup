package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.EMAActivity;
import kr.ac.inha.mindscope.MapsActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;

public class MeFragmentStep1 extends Fragment {

    private static final String TAG = "MeFragment";
    private static final int[] SUBMIT_HOUR = {11, 15, 19, 23};
    TextView time1;
    TextView time2;
    TextView time3;
    TextView time4;
    SharedPreferences loginPrefs;
    SharedPreferences configPrefs;
    private ImageButton btnMap;
    private AppBarLayout appBarLayout;
    private Button stepTestBtn;
    private Button emaTestBtn;
    private Button time1Btn;
    private Button time2Btn;
    private Button time3Btn;
    private Button time4Btn;
    private TextView todayPointsView;
    private TextView sumPointsView;
    private TextView before11Hours;
    private TextView attdView;
    private TextView versionNameTextView;
    private RelativeLayout timeContainer;
    boolean isNetworkToastMsgAbail;

    public static MeFragmentStep1 newInstance() {
        return new MeFragmentStep1();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_me, container, false);
        Context context = requireContext();
//        final TextView textView = root.findViewById(R.id.text_me);
        TextView date = root.findViewById(R.id.frg_me_date);

        isNetworkToastMsgAbail = true;

        todayPointsView = root.findViewById(R.id.point_today);
        sumPointsView = root.findViewById(R.id.point_my);
        before11Hours = root.findViewById(R.id.frg_me_before_11hours);
        attdView = root.findViewById(R.id.today_survey_attd);
        timeContainer = root.findViewById(R.id.time_container);
        versionNameTextView = root.findViewById(R.id.version_name_step1);
        versionNameTextView.setText(getVersionInfo(requireContext()));


        Calendar cal = Calendar.getInstance();

        SharedPreferences stepChangePrefs = context.getSharedPreferences("stepChange", Context.MODE_PRIVATE);
        int step = stepChangePrefs.getInt("stepCheck", 0);

        if(step == 0){
            before11Hours.setText(context.getResources().getString(R.string.string_frg_me_step0));
            before11Hours.setVisibility(View.VISIBLE);
            date.setVisibility(View.INVISIBLE);
            attdView.setVisibility(View.INVISIBLE);
            timeContainer.setVisibility(View.INVISIBLE);
        }
        else if (cal.get(Calendar.HOUR_OF_DAY) < 11 && cal.get(Calendar.HOUR_OF_DAY) > 3) {
            before11Hours.setVisibility(View.VISIBLE);
            date.setVisibility(View.INVISIBLE);
            attdView.setVisibility(View.INVISIBLE);
            timeContainer.setVisibility(View.INVISIBLE);
        } else {
            before11Hours.setVisibility(View.INVISIBLE);
            date.setVisibility(View.VISIBLE);
            attdView.setVisibility(View.VISIBLE);
            timeContainer.setVisibility(View.VISIBLE);
        }


        SharedPreferences prefs = requireActivity().getSharedPreferences("points", Context.MODE_PRIVATE);

//        todayPointsView.setText(String.valueOf(prefs.getInt("todayPoints", 0)));
//        sumPointsView.setText(String.valueOf(prefs.getInt("sumPoints", 0)));


        time1 = root.findViewById(R.id.time1_state);
        time2 = root.findViewById(R.id.time2_state);
        time3 = root.findViewById(R.id.time3_state);
        time4 = root.findViewById(R.id.time4_state);
        time1Btn = root.findViewById(R.id.time1_btn);
        time2Btn = root.findViewById(R.id.time2_btn);
        time3Btn = root.findViewById(R.id.time3_btn);
        time4Btn = root.findViewById(R.id.time4_btn);

        if(cal.get(Calendar.HOUR_OF_DAY) < 3){
            long curTimestmamp = cal.getTimeInMillis();
            long yesterdayCalTimestamp = curTimestmamp - 60 * 60 * 24 * 1000;
            String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(yesterdayCalTimestamp);
            date.setText(date_text);
        }else{
            String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(cal.getTimeInMillis());
            date.setText(date_text);
        }


        appBarLayout = root.findViewById(R.id.frg_me_app_bar);
        btnMap = root.findViewById(R.id.fragment_me_btn_map);


        btnMap.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
        });


        // TODO 추후 step 시간으로 확인할때는 삭제할 부분
        stepTestBtn = root.findViewById(R.id.step_test_btn);
        stepTestBtn.setOnClickListener(view -> {
            SharedPreferences stepChange = getActivity().getSharedPreferences("stepChange", getContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = stepChange.edit();

            if (stepChange.getInt("stepCheck", 0) == 1) {
                Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
                stepTestBtn.setText("STEP 2");
                editor.putInt("stepCheck", 2);
                editor.apply();
            } else {
                Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
                stepTestBtn.setText("STEP 1");
                editor.putInt("stepCheck", 1);
                editor.apply();
            }
        });

        emaTestBtn = root.findViewById(R.id.ema_test_btn);
        emaTestBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), EMAActivity.class);
            startActivity(intent);
        });



        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        loginPrefs = requireActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        configPrefs = requireActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
        SharedPreferences firstPref = requireActivity().getSharedPreferences("firstStart", Context.MODE_PRIVATE);
        int firstviewshow = firstPref.getInt("First", 0);
        boolean isFirstStartStep1DialogShowing = firstPref.getBoolean("firstStartStep1", false);
        if (firstviewshow == 1 && isFirstStartStep1DialogShowing)
            startEmaActivityWhenNotSubmitted();
        updateStats();
        loadAllPoints();
        loadDailyPoints();
        updateEmaResponseView();
    }

    public void updateStats() {
        if (Tools.isNetworkAvailable())
            new Thread(() -> {
                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                Calendar fromCal = Calendar.getInstance();
                fromCal.set(Calendar.HOUR_OF_DAY, 0);
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                Calendar tillCal = (Calendar) fromCal.clone();
                tillCal.set(Calendar.HOUR_OF_DAY, 23);
                tillCal.set(Calendar.MINUTE, 59);
                tillCal.set(Calendar.SECOND, 59);

                EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredDataRecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                        .setTargetDataSourceId(configPrefs.getInt("SURVEY_EMA", -1))
                        .setFromTimestamp(fromCal.getTimeInMillis())
                        .setTillTimestamp(tillCal.getTimeInMillis())
                        .build();
                try {
                    final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredDataRecordsRequestMessage);
                    if (responseMessage.getDoneSuccessfully()) {
//                        requireActivity().runOnUiThread(() -> updateEmaResponseView(responseMessage.getValueList()));
                    } else {
                        requireActivity().runOnUiThread(() -> {
//                                    initUserStats(true, 0, 0, null);
                        });
                    }
                } catch (StatusRuntimeException e) {
                    Log.e("Tools", "DataCollectorService.setUpHeartbeatSubmissionThread() exception: " + e.getMessage());
                    e.printStackTrace();
//                    requireActivity().runOnUiThread(() -> updateEmaResponseView(null));

                } finally {
                    channel.shutdown();
                }
            }).start();
        else {
            synchronized (this){
                if(isNetworkToastMsgAbail){
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.when_network_unable), Toast.LENGTH_SHORT).show();
                    isNetworkToastMsgAbail = false;
                }
            }
        }
    }

    private void updateEmaResponseView(/*List<String> values*/) {

        // initialize
        TextView[] times = {
                time1,
                time2,
                time3,
                time4
        };
        Button[] timeBtns = {
                time1Btn,
                time2Btn,
                time3Btn,
                time4Btn,
        };
        for (TextView time : times) {
            time.setText("");
        }
        for (Button timeBtn : timeBtns) {
            timeBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_time_inarrived));
        }

        // time check
        SharedPreferences emaSubmitCheckPrefs = requireActivity().getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
        Calendar cal = Calendar.getInstance();
        int curHours = cal.get(Calendar.HOUR_OF_DAY);
        for (short i = 0; i < 4; i++) {
            if(curHours >= SUBMIT_HOUR[i] || curHours < 3){
                if (!emaSubmitCheckPrefs.getBoolean("ema_submit_check_" + (i + 1), false)) {
                    times[i].setText(getResources().getString(R.string.string_survey_incomplete));
                    timeBtns[i].setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_time_incomplete));
                } else {
                    times[i].setText(Html.fromHtml(getResources().getString(R.string.string_survey_complete)));
                    timeBtns[i].setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_time_complete));
                }
            }
        }
    }

    public void loadAllPoints() {
        Context context = requireContext();
        if(Tools.isNetworkAvailable()){
            new Thread(() -> {
                SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(getContext().getString(R.string.stress_campaign_id));
                final int REWARD_POINTS = 58;

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                Calendar c = Calendar.getInstance();
                EtService.RetrieveFilteredDataRecordsRequestMessage requestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(REWARD_POINTS)
                        .setFromTimestamp(0)
                        .setTillTimestamp(c.getTimeInMillis())
                        .build();
                EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                int points = 0;
                if (responseMessage.getDoneSuccessfully())
                    for (String value : responseMessage.getValueList()) {
                        String[] cells = value.split(" ");
                        if (cells.length != 3)
                            continue;
                        points += Integer.parseInt(cells[2]);
                    }
                channel.shutdown();
                final int finalPoints = points;
                requireActivity().runOnUiThread(() -> sumPointsView.setText(String.format(Locale.getDefault(), "%d", finalPoints)));
            }).start();
        }
    }

    public void loadDailyPoints() {
        Context context = requireContext();
        if(Tools.isNetworkAvailable()){
            new Thread(() -> {
                SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(getContext().getString(R.string.stress_campaign_id));
                final int REWARD_POINTS = 58;

                Calendar fromCal = Calendar.getInstance();
                fromCal.set(Calendar.HOUR_OF_DAY, 0);
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                Calendar tillCal = (Calendar) fromCal.clone();
                tillCal.set(Calendar.HOUR_OF_DAY, 23);
                tillCal.set(Calendar.MINUTE, 59);
                tillCal.set(Calendar.SECOND, 59);

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                EtService.RetrieveFilteredDataRecordsRequestMessage requestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(REWARD_POINTS)
                        .setFromTimestamp(fromCal.getTimeInMillis())
                        .setTillTimestamp(tillCal.getTimeInMillis())
                        .build();
                EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                int dailyPoints = 0;
                if (responseMessage.getDoneSuccessfully())
                    for (String value : responseMessage.getValueList()) {
                        String[] cells = value.split(" ");
                        if (cells.length != 3)
                            continue;
                        dailyPoints += Integer.parseInt(cells[2]);
                    }
                channel.shutdown();
                final int finalDailyPoints = dailyPoints;
                requireActivity().runOnUiThread(() -> todayPointsView.setText(String.format(Locale.getDefault(), "%d", finalDailyPoints)));
            }).start();
        }
    }

    public void startEmaActivityWhenNotSubmitted() {
        SharedPreferences emaSubmitCheckPrefs = requireActivity().getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
        SharedPreferences.Editor emaSubmitEditor = emaSubmitCheckPrefs.edit();
        boolean[] submits = {
                emaSubmitCheckPrefs.getBoolean("ema_submit_check_1", false),
                emaSubmitCheckPrefs.getBoolean("ema_submit_check_2", false),
                emaSubmitCheckPrefs.getBoolean("ema_submit_check_3", false),
                emaSubmitCheckPrefs.getBoolean("ema_submit_check_4", false),
        };
        Calendar cal = Calendar.getInstance();
        int curHour = cal.get(Calendar.HOUR_OF_DAY);
        int todayDate = cal.get(Calendar.DATE);
        if (todayDate != emaSubmitCheckPrefs.getInt("emaSubmitDate", -1)) {
            for (short i = 0; i < 4; i++) {
                emaSubmitEditor.putBoolean("ema_submit_check_" + (i + 1), false);
                emaSubmitEditor.apply();
            }
        }
        for (short i = 0; i < 4; i++) {
            if (curHour == SUBMIT_HOUR[i] && !submits[i]) {
                int ema_order = Tools.getEMAOrderFromRangeAfterEMA(cal);
                if (ema_order != 0) {
                    Intent intent = new Intent(getActivity(), EMAActivity.class);
                    intent.putExtra("ema_order", ema_order);
                    startActivity(intent);
                }
            }
        }
        updateEmaResponseView();
    }

    public String getVersionInfo(Context context){
        String version = "Unknown";
        PackageInfo packageInfo;

        if(context == null){
            return version;
        }
        try {
            packageInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }
}