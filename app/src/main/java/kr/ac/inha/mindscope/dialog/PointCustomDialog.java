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
        todayPointsView.setText(String.valueOf(todayPoints));
        sumPointsView.setText(String.valueOf(sumPoints));
        btn = findViewById(R.id.point_btn);
        btn.setOnClickListener(mBtnListener);
    }

    public PointCustomDialog(Context context, View.OnClickListener clickListener) {
        super(context);
        this.mBtnListener = clickListener;
    }
}
