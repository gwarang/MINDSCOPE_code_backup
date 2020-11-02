package kr.ac.inha.mindscope.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

import kr.ac.inha.mindscope.R;

public class PointCustomDialog extends Dialog {

    private static final String TAG = "PointCustomDialog";

    public static int todayPoints;
    public static int sumPoints;
    TextView todayPointsView;
    TextView sumPointsView;
    private Button btn;
    private Button btn_stress;
    private View.OnClickListener mBtnListener;
    private View.OnClickListener mBtnListener2;

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
        btn = findViewById(R.id.point_btn);
        btn_stress = findViewById(R.id.point_stress_btn);
        SharedPreferences stepChangePrefs = getContext().getSharedPreferences("stepChange", Context.MODE_PRIVATE);
        SharedPreferences pointsPrefs = getContext().getSharedPreferences("points", Context.MODE_PRIVATE);
        sumPoints = pointsPrefs.getInt("sumPoints", 0);
        long dayNum = pointsPrefs.getLong("daynum", 0);
        todayPoints = 0;
        for(int i=1; i<=4; i++){
            todayPoints += pointsPrefs.getInt("day_" + dayNum + "_order_" + i, 0);
        }
        int step = stepChangePrefs.getInt("stepCheck", 0);

        if(step != 2){
            btn_stress.setVisibility(View.GONE);
        }
        btn.setOnClickListener(mBtnListener);
        btn_stress.setOnClickListener(mBtnListener2);

        todayPointsView.setText(String.valueOf(todayPoints));
        sumPointsView.setText(String.valueOf(sumPoints));
    }

    public PointCustomDialog(Context context, View.OnClickListener clickListener, View.OnClickListener clickListener2) {
        super(context);
        this.mBtnListener = clickListener;
        this.mBtnListener2 = clickListener2;
    }
}
