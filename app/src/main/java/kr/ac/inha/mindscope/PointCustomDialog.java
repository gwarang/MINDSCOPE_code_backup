package kr.ac.inha.mindscope;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

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
        todayPointsView.setText(String.valueOf(todayPoints));

        sumPointsView = (TextView) findViewById(R.id.point_sum_value);
        sumPointsView.setText(String.valueOf(sumPoints));
        btn = (Button) findViewById(R.id.point_btn);
        btn.setOnClickListener(mBtnListener);

    }

    public PointCustomDialog(Context context, View.OnClickListener clickListener){
        super(context);
        this.mBtnListener = clickListener;
    }

}
