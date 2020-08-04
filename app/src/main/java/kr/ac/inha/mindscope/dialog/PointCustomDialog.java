package kr.ac.inha.mindscope.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Objects;

import inha.nsl.easytrack.ETServiceGrpc;
import inha.nsl.easytrack.EtService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kr.ac.inha.mindscope.AuthenticationActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Utils;

public class PointCustomDialog extends Dialog {

    private static final String TAG = "PointCustomDialog";

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
        Objects.requireNonNull(getWindow()).setAttributes(layoutParams);

        setContentView(R.layout.point_dialog);

        todayPointsView = findViewById(R.id.point_today_value);
        sumPointsView = findViewById(R.id.point_sum_value);
//        retrievePointFromServer();
        todayPointsView.setText(String.valueOf(todayPoints));
        sumPointsView.setText(String.valueOf(sumPoints));
        btn = findViewById(R.id.point_btn);
        btn.setOnClickListener(mBtnListener);

    }

    public PointCustomDialog(Context context, View.OnClickListener clickListener){
        super(context);
        this.mBtnListener = clickListener;
    }

    public void retrievePointFromServer(){

        Thread retrievePointThread = new Thread(){
            @Override
            public void run() {
                Utils.logThreadSignature(TAG + " retrievePointThread");
                SharedPreferences loginPrefs = getContext().getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
                int userId = loginPrefs.getInt(AuthenticationActivity.user_id, -1);
                String email = loginPrefs.getString(AuthenticationActivity.usrEmail, null);
                int campaignId = Integer.parseInt(getContext().getString(R.string.stress_campaign_id));
                final int REWARD_POINTS = 58;

                ManagedChannel channel = ManagedChannelBuilder.forAddress(getContext().getString(R.string.grpc_host), Integer.parseInt(getContext().getString(R.string.grpc_port))).usePlaintext().build();
                ETServiceGrpc.ETServiceBlockingStub stub = ETServiceGrpc.newBlockingStub(channel);


                //region daily points
                Calendar fromCal = Calendar.getInstance();
                fromCal.set(Calendar.HOUR_OF_DAY, 0);
                fromCal.set(Calendar.MINUTE, 0);
                fromCal.set(Calendar.SECOND, 0);
                fromCal.set(Calendar.MILLISECOND, 0);
                Calendar tillCal = (Calendar) fromCal.clone();
                tillCal.set(Calendar.HOUR_OF_DAY, 23);
                tillCal.set(Calendar.MINUTE, 59);
                tillCal.set(Calendar.SECOND, 59);

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
                todayPoints = dailyPoints;
                //endregion

                //region all points
                Calendar c = Calendar.getInstance();
                requestMessage = EtService.RetrieveFilteredDataRecordsRequestMessage.newBuilder()
                        .setUserId(userId)
                        .setEmail(email)
                        .setTargetEmail(email)
                        .setTargetCampaignId(campaignId)
                        .setTargetDataSourceId(REWARD_POINTS)
                        .setFromTimestamp(0)
                        .setTillTimestamp(c.getTimeInMillis())
                        .build();
                responseMessage = stub.retrieveFilteredDataRecords(requestMessage);
                int points = 0;
                if (responseMessage.getDoneSuccessfully())
                    for (String value : responseMessage.getValueList()) {
                        String[] cells = value.split(" ");
                        if (cells.length != 3)
                            continue;
                        points += Integer.parseInt(cells[2]);
                    }
                sumPoints = points;
                //endregion
                channel.shutdown();
            }
        };

        retrievePointThread.start();

        try {
            retrievePointThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
