package kr.ac.inha.mindscope.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

import androidx.annotation.NonNull;
import kr.ac.inha.mindscope.R;

public class FirstMapStartDialog extends Dialog {

    private View.OnClickListener mClickListener;

    TextView titleView;
    TextView helpTextView;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        Objects.requireNonNull(getWindow()).setAttributes(layoutParams);

        setContentView(R.layout.first_map_dialog);

        titleView = findViewById(R.id.map_dialog_title);
        helpTextView = findViewById(R.id.map_help_text);

        btn = findViewById(R.id.first_map_dialog_btn);
        btn.setOnClickListener(mClickListener);
    }

    public FirstMapStartDialog(@NonNull Context context, View.OnClickListener clickListener) {
        super(context);
        this.mClickListener = clickListener;
    }
}
