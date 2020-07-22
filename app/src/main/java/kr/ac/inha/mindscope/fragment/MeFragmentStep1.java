package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Date;
import java.util.List;
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

    private ImageButton btnMap;
    private AppBarLayout appBarLayout;
    private Button stepTestBtn;
    private Button emaTestBtn;
    private Button time1Btn;
    private Button time2Btn;
    private Button time3Btn;
    private Button time4Btn;
    TextView time1;
    TextView time2;
    TextView time3;
    TextView time4;
    private TextView todayPointsView;
    private TextView sumPointsView;
    private TextView before11Hours;
    private TextView attdView;
    private RelativeLayout timeContainer;

    SharedPreferences loginPrefs;
    SharedPreferences configPrefs;

    public static MeFragmentStep1 newInstance() {
        return new MeFragmentStep1();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_me, container, false);
//        final TextView textView = root.findViewById(R.id.text_me);
        TextView date = root.findViewById(R.id.frg_me_date);
        // TODO timeX_state 텍스트 및 timeX_btn background EMA order 상태(설문완료, 미완료, 미도착)에 따라서 변경할 것

        todayPointsView = root.findViewById(R.id.point_today);
        sumPointsView = root.findViewById(R.id.point_my);
        before11Hours = root.findViewById(R.id.frg_me_before_11hours);
        attdView = root.findViewById(R.id.today_survey_attd);
        timeContainer = root.findViewById(R.id.time_container);

        Calendar cal = Calendar.getInstance();

        if(cal.get(Calendar.HOUR_OF_DAY) < 11 && cal.get(Calendar.HOUR_OF_DAY) > 3){
            before11Hours.setVisibility(View.VISIBLE);
            date.setVisibility(View.INVISIBLE);
            attdView.setVisibility(View.INVISIBLE);
            timeContainer.setVisibility(View.INVISIBLE);
        }else{
            before11Hours.setVisibility(View.INVISIBLE);
            date.setVisibility(View.VISIBLE);
            attdView.setVisibility(View.VISIBLE);
            timeContainer.setVisibility(View.VISIBLE);
        }


        SharedPreferences prefs = getContext().getSharedPreferences("points", Context.MODE_PRIVATE);

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

        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        date.setText(date_text);


        appBarLayout = (AppBarLayout) root.findViewById(R.id.frg_me_app_bar);
        btnMap = (ImageButton) root.findViewById(R.id.fragment_me_btn_map);



        btnMap.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            startActivity(intent);
        });


        // TODO 추후 step 시간으로 확인할때는 삭제할 부분
        stepTestBtn = (Button) root.findViewById(R.id.step_test_btn);
        stepTestBtn.setOnClickListener(view -> {
            SharedPreferences stepChange = getActivity().getSharedPreferences("stepChange", getContext().MODE_PRIVATE);
            SharedPreferences.Editor editor = stepChange.edit();

            if(stepChange.getInt("stepCheck", 0) == 1){
                Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
                stepTestBtn.setText("STEP 2");
                editor.putInt("stepCheck", 2);
                editor.apply();
            }
            else{
                Log.i(TAG, "STEP " + stepChange.getInt("stepCheck", 0));
                stepTestBtn.setText("STEP 1");
                editor.putInt("stepCheck", 1);
                editor.apply();
            }
        });

        emaTestBtn = (Button) root.findViewById(R.id.ema_test_btn);
        emaTestBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), EMAActivity.class);
            startActivity(intent);
        });


        loadAllPoints();
        loadDailyPoints();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        loginPrefs = getActivity().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        configPrefs = getActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);

        updateStats();
    }

    public void updateStats() {
        if (Tools.isNetworkAvailable())
            new Thread(new Runnable() {
                @Override
                public void run() {
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

                    // for test hrgoh 7/16 or 17 - 00:00:00~23:59:50
//                    Calendar fromtest = Calendar.getInstance();
//                    fromtest.set(Calendar.DATE, 17);
//                    fromtest.set(Calendar.HOUR_OF_DAY, 0);
//                    fromtest.set(Calendar.MINUTE, 0);
//                    fromtest.set(Calendar.SECOND, 0);
//                    fromtest.set(Calendar.MILLISECOND, 0);
//                    Calendar tilltest = (Calendar) fromtest.clone();
//                    tilltest.set(Calendar.HOUR_OF_DAY, 23);
//                    tilltest.set(Calendar.MINUTE, 59);
//                    tilltest.set(Calendar.SECOND, 59);

                    EtService.RetrieveFilteredDataRecordsRequestMessage retrieveFilteredDataRecordsRequestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                            .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                            .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                            .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                            .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                            .setTargetDataSourceId(configPrefs.getInt("SURVEY_EMA", -1))
                            .setFromTimestamp(fromCal.getTimeInMillis()) //fromtest.getTimeInMillis())
                            .setTillTimestamp(tillCal.getTimeInMillis()) //tilltest.getTimeInMillis())
                            .build();
                    try {
                        final EtService.RetrieveFilteredDataRecordsResponseMessage responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredDataRecordsRequestMessage);
                        if (responseMessage.getDoneSuccessfully()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateEmaResponseView(responseMessage.getValueList());
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    initUserStats(true, 0, 0, null);
                                }
                            });
                        }
                    } catch (StatusRuntimeException e) {
                        Log.e("Tools", "DataCollectorService.setUpHeartbeatSubmissionThread() exception: " + e.getMessage());
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateEmaResponseView(null);
                            }
                        });

                    } finally {
                        channel.shutdown();
                    }
                }
            }).start();
        else {
            Toast.makeText(getContext(), "Please connect to Internet!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmaResponseView(List<String> values){

        // initialize
        TextView[] times = {
                time1,
                time2,
                time3,
                time4
        };
        time1.setText("");
        time2.setText("");
        time3.setText("");
        time4.setText("");
        time1Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_inarrived));
        time2Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_inarrived));
        time3Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_inarrived));
        time4Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_inarrived));

        // time check
        Calendar cal = Calendar.getInstance();
        int curHours = cal.get(Calendar.HOUR_OF_DAY);
        if(curHours >= 11){
            time1.setText(getResources().getString(R.string.string_survey_incomplete));
            time1Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_incomplete));
        }
        if(curHours >= 15){
            time2.setText(getResources().getString(R.string.string_survey_incomplete));
            time2Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_incomplete));
        }
        if(curHours >= 19){
            time3.setText(getResources().getString(R.string.string_survey_incomplete));
            time3Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_incomplete));
        }
        if(curHours >= 23){
            time4.setText(getResources().getString(R.string.string_survey_incomplete));
            time4Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_incomplete));
        }

        // submit check
        if(values != null){
            for(String val : values){
                switch (Integer.parseInt(val.split(Tools.DATA_SOURCE_SEPARATOR)[1])){
                    case 1:
                        time1.setText(Html.fromHtml(getResources().getString(R.string.string_survey_complete)));
                        time1Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_complete));
                        break;
                    case 2:
                        time2.setText(Html.fromHtml(getResources().getString(R.string.string_survey_complete)));
                        time2Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_complete));
                        break;
                    case 3:
                        time3.setText(Html.fromHtml(getResources().getString(R.string.string_survey_complete)));
                        time3Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_complete));
                        break;
                    case 4:
                        time4.setText(Html.fromHtml(getResources().getString(R.string.string_survey_complete)));
                        time4Btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.btn_time_complete));
                        break;
                }
            }
        }

        // if not submit during EMA submit duration, start EMAActivity
        new Thread(() -> {
            for(TextView time : times){
                if(time.getText().equals(getResources().getString(R.string.string_survey_incomplete))){
                    int ema_order = Tools.getEMAOrderFromRangeAfterEMA(cal);
                    if(ema_order != 0){
                        Intent intent = new Intent(getActivity(), EMAActivity.class);
                        intent.putExtra("ema_order", ema_order);
                        startActivity(intent);
                    }
                }
            }
        }).start();
    }

    public void loadAllPoints() {
        new Thread(() -> {
            SharedPreferences loginPrefs = getContext().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
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
            requireActivity().runOnUiThread(() -> sumPointsView.setText(String.format(Locale.getDefault(), "%,d", finalPoints)));
        }).start();
    }

    public void loadDailyPoints() {
        new Thread(() -> {
            SharedPreferences loginPrefs = getContext().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
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
            requireActivity().runOnUiThread(() -> todayPointsView.setText(String.format(Locale.getDefault(), "%,d", finalDailyPoints)));
        }).start();
    }


}