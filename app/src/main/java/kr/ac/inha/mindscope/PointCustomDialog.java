package kr.ac.inha.mindscope;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class PointCustomDialog extends Dialog {

    public static int todayPoints;
    public static int sumPoints;
    TextView todayPointsView;
    TextView sumPointsView;
    private Button btn;
    private View.OnClickListener mBtnListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.point_dialog);



        todayPointsView = (TextView) findViewById(R.id.point_today_value);
//        todayPointsView.setText(String.valueOf(todayPoints));
        sumPointsView = (TextView) findViewById(R.id.point_sum_value);
//        sumPointsView.setText(String.valueOf(sumPoints));
        updatePointFromServer();
        btn = (Button) findViewById(R.id.point_btn);
        btn.setOnClickListener(mBtnListener);

    }

    public PointCustomDialog(Context context, View.OnClickListener clickListener){
        super(context);
        this.mBtnListener = clickListener;
    }

    public void updatePointFromServer(){
        // all points
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences loginPrefs = getContext().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(getContext().getString(R.string.stress_campaign_id));
                final int REWARD_POINTS = 58;

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getContext().getString(R.string.grpc_host), Integer.parseInt(getContext().getString(R.string.grpc_port))).usePlaintext().build();
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
                getOwnerActivity().runOnUiThread(() -> sumPointsView.setText(String.format(Locale.getDefault(), "%,d", finalPoints)));
            }
        }).start();

        // daily points
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

        ManagedChannel channel = ManagedChannelBuilder.forAddress(getContext().getString(R.string.grpc_host), Integer.parseInt(getContext().getString(R.string.grpc_port))).usePlaintext().build();
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
        getOwnerActivity().runOnUiThread(() -> todayPointsView.setText(String.format(Locale.getDefault(), "%,d", finalDailyPoints)));
    }
}
