package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

import static kr.ac.inha.mindscope.Tools.timeTheDayNumIsChanged;

public class MeFragmentStep1 extends Fragment {

    private static final String TAG = "MeFragment";
    private static final int[] SUBMIT_HOUR = {11, 15, 19, 23};

    static HashMap<Long, Integer> allPointsMaps = new HashMap<>();
    TextView time1;
    TextView time2;
    TextView time3;
    TextView time4;
    SharedPreferences loginPrefs;
    SharedPreferences lastPagePrefs;
    SharedPreferences configPrefs;
    boolean isNetworkToastMsgAbail;
    Thread loadPointsThread;
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
    static boolean runningUpdateUi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Me1 onCreate");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_me, container, false);
        Context context = requireContext();
        TextView dateView = root.findViewById(R.id.frg_me_date);

        isNetworkToastMsgAbail = true;

        todayPointsView = root.findViewById(R.id.point_today);
        sumPointsView = root.findViewById(R.id.point_my);
        before11Hours = root.findViewById(R.id.frg_me_before_11hours);
        attdView = root.findViewById(R.id.today_survey_attd);
        timeContainer = root.findViewById(R.id.time_container);
//        versionNameTextView = root.findViewById(R.id.version_name_step1);
//        versionNameTextView.setText(getVersionInfo(requireContext()));


        Calendar cal = Calendar.getInstance();

        SharedPreferences stepChangePrefs = context.getSharedPreferences("stepChange", Context.MODE_PRIVATE);
        int step = stepChangePrefs.getInt("stepCheck", 5);

        if (step == 0) {
            before11Hours.setText(context.getResources().getString(R.string.string_frg_me_step0));
            before11Hours.setVisibility(View.VISIBLE);
            dateView.setVisibility(View.INVISIBLE);
            attdView.setVisibility(View.INVISIBLE);
            timeContainer.setVisibility(View.INVISIBLE);
        } else if (cal.get(Calendar.HOUR_OF_DAY) < 11 && cal.get(Calendar.HOUR_OF_DAY) >= timeTheDayNumIsChanged) {
            before11Hours.setVisibility(View.VISIBLE);
            dateView.setVisibility(View.INVISIBLE);
            attdView.setVisibility(View.INVISIBLE);
            timeContainer.setVisibility(View.INVISIBLE);
        } else {
            before11Hours.setVisibility(View.INVISIBLE);
            dateView.setVisibility(View.VISIBLE);
            attdView.setVisibility(View.VISIBLE);
            timeContainer.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = stepChangePrefs.edit();
            editor.putBoolean("step1FirstAfter11o'clock", true);
            editor.apply();
        }

        time1 = root.findViewById(R.id.time1_state);
        time2 = root.findViewById(R.id.time2_state);
        time3 = root.findViewById(R.id.time3_state);
        time4 = root.findViewById(R.id.time4_state);
        time1Btn = root.findViewById(R.id.time1_btn);
        time2Btn = root.findViewById(R.id.time2_btn);
        time3Btn = root.findViewById(R.id.time3_btn);
        time4Btn = root.findViewById(R.id.time4_btn);

        if(cal.get(Calendar.HOUR_OF_DAY) < timeTheDayNumIsChanged)
            cal.add(Calendar.DATE, -1);
        Date currentTime = cal.getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);


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

        // 이 함수가 제일 중요
        updateEmaResponseView();

        if (firstviewshow == 1 && isFirstStartStep1DialogShowing){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startEmaActivityWhenNotSubmitted();
                }
            }, 1000);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
        if (loadPointsThread != null && loadPointsThread.isAlive())
            loadPointsThread.interrupt();

        lastPagePrefs = requireActivity().getSharedPreferences("LastPage", Context.MODE_PRIVATE);
        SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
        lastPagePrefsEditor.putString("last_open_nav_frg", "me");
        lastPagePrefsEditor.apply();

    }

    private void updateEmaResponseView() {
        Log.d(TAG, "start updateEmaResponseView");
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
                Calendar tillCal = Calendar.getInstance();

                if(fromCal.get(Calendar.HOUR_OF_DAY) < timeTheDayNumIsChanged){
                    fromCal.add(Calendar.DATE, -1);
                }else{
                    tillCal.add(Calendar.DATE, 1);
                }

                fromCal.set(Calendar.HOUR_OF_DAY, timeTheDayNumIsChanged);
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                tillCal.set(Calendar.HOUR_OF_DAY, timeTheDayNumIsChanged - 1);
                tillCal.set(Calendar.MINUTE, 59);
                tillCal.set(Calendar.SECOND, 59);
                Log.d(TAG, "Me1");
                EtService.RetrieveFilteredDataRecords.Request retrieveFilteredDataRecordsRequestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
                        .setUserId(loginPrefs.getInt(AuthenticationActivity.user_id, -1))
                        .setEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetEmail(loginPrefs.getString(AuthenticationActivity.usrEmail, null))
                        .setTargetCampaignId(Integer.parseInt(getString(R.string.stress_campaign_id)))
                        .setTargetDataSourceId(configPrefs.getInt("SURVEY_EMA", -1)) // 여기가 원하는 데이터소스 불러오려고 설정하는 부분
                        .setFromTimestamp(fromCal.getTimeInMillis())
                        .setTillTimestamp(tillCal.getTimeInMillis())
                        .build();
                try {
                    Log.d(TAG, "before responseMessage");
                    final EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(retrieveFilteredDataRecordsRequestMessage);
                    Log.d(TAG, "after responseMessage");
                    if (responseMessage.getSuccess()) {
                        // checkByteString
                        List<ByteString> values = responseMessage.getValueList(); // SURVEY_EMA 의 데이터가 bytestring type의 값들의 리스트로 오게됨

                        //초기화
                        for (short i = 0; i < 4; i++) {
                            editor.putBoolean("ema_submit_check_" + (i + 1), false);
                            editor.apply();
                        }

                        for (ByteString value : values) {
                            String[] splitValue = value.toString("UTF-8").split(" ");
                            editor.putBoolean("ema_submit_check_" + splitValue[1], true);
                            editor.apply();
                        }
                    }
                } catch (StatusRuntimeException | IOException e) {
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
                            if (curHours >= SUBMIT_HOUR[i] || curHours < timeTheDayNumIsChanged) {
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

        // todo 동기화 하는 과정  앱을 삭제하면 SP가 다 날라감. 그래서 나중에는 서버하고 동기화하는 과정을 추가해주셔야한다.
        SharedPreferences pointsPrefs = requireActivity().getSharedPreferences("points", Context.MODE_PRIVATE);
        int localSumPoints = pointsPrefs.getInt("sumPoints", 0);
        long daynum = pointsPrefs.getLong("daynum", 0);
        int localTodayPoints = 0;
        for (int i = 1; i<=4; i++){
            localTodayPoints += pointsPrefs.getInt("day_" + daynum + "_order_" + i, 0);
        }

        sumPointsView.setText(String.format(Locale.getDefault(), "%d", localSumPoints));
        todayPointsView.setText(String.format(Locale.getDefault(), "%d", localTodayPoints));
        Log.d(TAG, "Me1 point");


//        Log.d(TAG, "start loadAllPoints");
//        Context context = requireContext();
//        allPointsMaps.clear();
//        if (Tools.isNetworkAvailable()) {
//
//            if (loadPointsThread != null && loadPointsThread.isAlive())
//                loadPointsThread.interrupt();
//
//            loadPointsThread = new Thread() {
//                @Override
//                public void run() {
//                    while (!loadPointsThread.isInterrupted()) {
//                        int allPoints = 0;
//                        int dailyPoints = 0;
//                        SharedPreferences loginPrefs = context.getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
//                        int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
//                        String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
//                        int campaignId = Integer.parseInt(context.getString(R.string.stress_campaign_id));
//                        final int REWARD_POINTS = 58;
//
//                        ManagedChannel channel = ManagedChannelBuilder.forAddress(getString(R.string.grpc_host), Integer.parseInt(getString(R.string.grpc_port))).usePlaintext().build();
//                        ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);
//                        Calendar c = Calendar.getInstance();
//                        EtService.RetrieveFilteredDataRecords.Request requestMessage = EtService.RetrieveFilteredDataRecords.Request.newBuilder()
//                                .setUserId(userId)
//                                .setEmail(email)
//                                .setTargetEmail(email)
//                                .setTargetCampaignId(campaignId)
//                                .setTargetDataSourceId(REWARD_POINTS)
//                                .setFromTimestamp(0)
//                                .setTillTimestamp(c.getTimeInMillis())
//                                .build();
//                        try {
//                            EtService.RetrieveFilteredDataRecords.Response responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
//                            if (responseMessage.getSuccess()) {
//                                // checkByteString
//                                for (ByteString value : responseMessage.getValueList()) {
//                                    String[] cells = value.toString().split(" ");
//                                    if (cells.length != 3)
//                                        continue;
//                                    allPointsMaps.put(Long.parseLong(cells[0]), Integer.parseInt(cells[2]));
//                                }
//                            }
//                        } catch (IllegalStateException | StatusRuntimeException e) {
//                            e.printStackTrace();
//                        }
//                        channel.shutdown();
//
//                        Calendar todayStartCal = Calendar.getInstance();
//                        Calendar todayEndCal = Calendar.getInstance();
//                        if(todayStartCal.get(Calendar.HOUR_OF_DAY) < timeTheDayNumIsChanged){
//                            todayStartCal.add(Calendar.DATE, -1);
//                        }else{
//                            todayEndCal.add(Calendar.DATE, 1);
//                        }
//                        todayStartCal.set(Calendar.HOUR_OF_DAY, timeTheDayNumIsChanged);
//                        todayStartCal.set(Calendar.MINUTE, 0);
//                        todayStartCal.set(Calendar.SECOND, 0);
//                        todayStartCal.set(Calendar.MILLISECOND, 0);
//                        todayEndCal.set(Calendar.HOUR_OF_DAY, timeTheDayNumIsChanged - 1);
//                        todayEndCal.set(Calendar.MINUTE, 59);
//                        todayEndCal.set(Calendar.SECOND, 59);
//                        todayEndCal.set(Calendar.MILLISECOND, 0);
//                        Calendar pointCal = Calendar.getInstance();
//
//                        long startTimestamp = todayStartCal.getTimeInMillis();
//                        long endTimestamp = todayEndCal.getTimeInMillis();
//                        long pointTimestamp = 0;
//
//                        for (Map.Entry<Long, Integer> elem : allPointsMaps.entrySet()) {
//                            allPoints += elem.getValue();
//                            pointCal.setTimeInMillis(elem.getKey());
//
//                            pointTimestamp = pointCal.getTimeInMillis();
//                            if(startTimestamp <= pointTimestamp && pointTimestamp <= endTimestamp){
//                                dailyPoints += elem.getValue();
//                            }
//
//                        }
//
//                        if (isAdded()) {
//                            int finalAllPoints = allPoints;
//                            int finalDailyPoints = dailyPoints;
//                            requireActivity().runOnUiThread(() -> {
//                                sumPointsView.setText(String.format(Locale.getDefault(), "%d", finalAllPoints));
//                                todayPointsView.setText(String.format(Locale.getDefault(), "%d", finalDailyPoints));
//                            });
//                        }
//                    }
//                }
//            };
//            loadPointsThread.setDaemon(true);
//            loadPointsThread.start();
//        }
    }

    public void startEmaActivityWhenNotSubmitted() {
        SharedPreferences emaSubmitCheckPrefs = requireActivity().getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
        SharedPreferences stepChangePrefs = requireActivity().getSharedPreferences("stepChange", Context.MODE_PRIVATE);
        SharedPreferences.Editor emaSubmitEditor = emaSubmitCheckPrefs.edit();
        boolean[] submits = {
                emaSubmitCheckPrefs.getBoolean("ema_submit_check_1", false),
                emaSubmitCheckPrefs.getBoolean("ema_submit_check_2", false),
                emaSubmitCheckPrefs.getBoolean("ema_submit_check_3", false),
                emaSubmitCheckPrefs.getBoolean("ema_submit_check_4", false),
        };
        Calendar cal = Calendar.getInstance();
        int curHour = cal.get(Calendar.HOUR_OF_DAY);
        if(curHour < timeTheDayNumIsChanged){
            cal.add(Calendar.DATE, -1);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 0);
        }
        int todayDate = cal.get(Calendar.DATE);
        if (todayDate != emaSubmitCheckPrefs.getInt("emaSubmitDate", -1)) {
            for (short i = 0; i < 4; i++) {
                emaSubmitEditor.putBoolean("ema_submit_check_" + (i + 1), false);
                emaSubmitEditor.apply();
            }
        }
        int ema_order = Tools.getEMAOrderFromRangeAfterEMA(cal);
        for (short i = 0; i < 4; i++) {
//            if ((curHour == EMA_NOTIF_HOURS[i] || curHour == EMA_NOTIF_HOURS[i]+1 || curHour < timeTheDayNumIsChanged) && ema_order > 0 && !submits[ema_order - 1]) {
//                if(stepChangePrefs.getBoolean("step1FirstAfter11o'clock", false)){
//                    Intent intent = new Intent(getActivity(), EMAActivity.class);
//                    intent.putExtra("ema_order", ema_order);
//                    startActivity(intent);
//                }
//            }
            if (ema_order > 0 && !submits[ema_order - 1]) {
                if(stepChangePrefs.getBoolean("step1FirstAfter11o'clock", false)){
                    Intent intent = new Intent(getActivity(), EMAActivity.class);
                    intent.putExtra("ema_order", ema_order);
                    startActivity(intent);
                }
            }
        }
        loadAllPoints();
//        updateEmaResponseView();
    }


//    public String getVersionInfo(Context context) {
//        String version = "Unknown";
//        PackageInfo packageInfo;
//
//        if (context == null) {
//            return version;
//        }
//        try {
//            packageInfo = context.getApplicationContext().getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0);
//            version = packageInfo.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return version;
//    }


}