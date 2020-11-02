package kr.ac.inha.mindscope.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import kr.ac.inha.mindscope.R;

public class PerformedDialog extends Dialog {

    private View.OnClickListener mBtnListener;

    private ImageView img;
    public TextView intervention;
    private TextView textview;
    private static String performedIntervention;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        Objects.requireNonNull(getWindow()).setAttributes(layoutParams);

        setContentView(R.layout.performed_intervention_dialog);

        img = findViewById(R.id.performed_intervention_img);
        intervention = findViewById(R.id.performed_intervention);
        intervention.setText(performedIntervention.replace("_", " "));
        textview = findViewById(R.id.performed_intervention2);
        btn = findViewById(R.id.performed_intervention_btn);

        btn.setOnClickListener(mBtnListener);


    }

    public PerformedDialog(Context context, View.OnClickListener clickListener, String intervention) {
        super(context);
        performedIntervention = intervention;
        this.mBtnListener = clickListener;
    }
}
