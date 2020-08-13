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

import com.google.android.material.appbar.AppBarLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    boolean isNetworkToastMsgAbail;
    private ImageButton btnMap;
    private AppBarLayout appBarLayout;
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

    Thread loadPointsThread;

    static HashMap<Long, Integer> allPointsMaps = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_me, container, false);
        Context context = requireContext();
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
        int step = stepChangePrefs.getInt("stepCheck", 5);

        if (step == 0) {
            before11Hours.setText(context.getResources().getString(R.string.string_frg_me_step0));
            before11Hours.setVisibility(View.VISIBLE);
            date.setVisibility(View.INVISIBLE);
            attdView.setVisibility(View.INVISIBLE);
            timeContainer.setVisibility(View.INVISIBLE);
        } else if (cal.get(Calendar.HOUR_OF_DAY) < 11 && cal.get(Calendar.HOUR_OF_DAY) > 3) {
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


        appBarLayout = root.findViewById(R.id.frg_me_app_bar);
        btnMap = root.findViewById(R.id.fragment_me_btn_map);


        btnMap.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
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
        loadAllPoints();
        updateEmaResponseView();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
        if(loadPointsThread!=null && loadPointsThread.isAlive())
            loadPointsThread.interrupt();

    }

    private void updateEmaResponseView() {
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


        // sync ema submit & update ema submit check box
        if (Tools.isNetworkAvailable()) {
            new Thread(() -> {
                SharedPreferences emaSubmitCheckPrefs = requireActivity().getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = emaSubmitCheckPrefs.edit();
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
                EtService.RetrieveFilteredDataRecords.Request retrieveFilteredDataRecordsRequestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                        .setTargetDataSourceId(configPrefs.getInt("SURVEY_EMA", -1))
                        .setFromTimestamp(fromCal.getTimeInMillis())
                        .setTillTimestamp(tillCal.getTimeInMillis())
                        .build();
                try {
                    final EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredDataRecordsRequestMessage);
                    if (responseMessage.getSuccess()) {
                        List<String> values = responseMessage.getValueList();
                        for (String value : values) {
                            String[] splitValue = value.split(" ");
                            editor.putBoolean("ema_submit_check_" + splitValue[1], true);
                            editor.apply();
                        }
                    }
                } catch (StatusRuntimeException e) {
                    e.printStackTrace();
                } finally {
                    channel.shutdown();
                }


                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        // time check
                        Calendar cal = Calendar.getInstance();
                        int curHours = cal.get(Calendar.HOUR_OF_DAY);
                        for (short i = 0; i < 4; i++) {
                            if (curHours >= SUBMIT_HOUR[i]) {
                                if (!emaSubmitCheckPrefs.getBoolean("ema_submit_check_" + (i + 1), false)) {
                                    times[i].setText(getResources().getString(R.string.string_survey_incomplete));
                                    timeBtns[i].setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_time_incomplete));
                                } else {
                                    times[i].setText(Html.fromHtml(getResources().getString(R.string.string_survey_complete)));
                                    timeBtns[i].setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.btn_time_complete));
                                }
                            }
                        }
                    });
                }
            }).start();
        }
    }

    public void loadAllPoints() {
        Context context = requireContext();
        allPointsMaps.clear();
        if (Tools.isNetworkAvailable()) {

            if(loadPointsThread!=null && loadPointsThread.isAlive())
                loadPointsThread.interrupt();

            loadPointsThread = new Thread(){
                @Override
                public void run() {
                    while(!loadPointsThread.isInterrupted()){
                        int allPoints = 0;
                        int dailyPoints = 0;
                        SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                        int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                        String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                        int campaignId = Integer.parseInt(context.getString(R.string.stress_campaign_id));
                        final int REWARD_POINTS = 58;

                        ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
                        ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
                        Calendar c = Calendar.getInstance();
                        EtService.RetrieveFilteredDataRecords.Request requestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                                .setUserId(userId)
                                .setEmail(email)
                                .setTargetEmail(email)
                                .setTargetCampaignId(campaignId)
                                .setTargetDataSourceId(REWARD_POINTS)
                                .setFromTimestamp(0)
                                .setTillTimestamp(c.getTimeInMillis())
                                .build();
                        try {
                            EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                            if (responseMessage.getSuccess()) {
                                for (String value : responseMessage.getValueList()) {
                                    String[] cells = value.split(" ");
                                    if (cells.length != 3)
                                        continue;
                                    allPointsMaps.put(Long.parseLong(cells[0]), Integer.parseInt(cells[2]));
                                }
                            }
                        } catch (IllegalStateException | StatusRuntimeException e) {
                            e.printStackTrace();
                        }
                        channel.shutdown();

                        Calendar todayCal = Calendar.getInstance();
                        todayCal.set(Calendar.HOUR_OF_DAY, 0);
                        todayCal.set(Calendar.MINUTE, 0);
                        todayCal.set(Calendar.SECOND, 0);
                        todayCal.set(Calendar.MILLISECOND, 0);
                        Calendar pointCal = Calendar.getInstance();

                        for(Map.Entry<Long, Integer> elem : allPointsMaps.entrySet()){
                            allPoints += elem.getValue();
                            pointCal.setTimeInMillis(elem.getKey());
                            pointCal.set(Calendar.HOUR_OF_DAY, 0);
                            pointCal.set(Calendar.MINUTE, 0);
                            pointCal.set(Calendar.SECOND, 0);
                            pointCal.set(Calendar.MILLISECOND, 0);
                            if( todayCal.compareTo(pointCal) == 0){
                                dailyPoints+=elem.getValue();
                            }
                        }

                        if(isAdded()){
                            int finalAllPoints = allPoints;
                            int finalDailyPoints = dailyPoints;
                            if(isAdded()){
                                requireActivity().runOnUiThread(() -> {
                                    sumPointsView.setText(String.format(Locale.getDefault(), "%d", finalAllPoints));
                                    todayPointsView.setText(String.format(Locale.getDefault(), "%d", finalDailyPoints));
                                });
                            }
                        }
                    }
                }
            };
            loadPointsThread.setDaemon(true);
            loadPointsThread.start();
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

    public String getVersionInfo(Context context) {
        String version = "Unknown";
        PackageInfo packageInfo;

        if (context == null) {
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