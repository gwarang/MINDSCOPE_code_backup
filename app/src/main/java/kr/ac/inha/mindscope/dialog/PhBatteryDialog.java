package kr.ac.inha.mindscope.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PhBatteryDialog extends DialogFragment {

    private static final String ARGS = "dialog_arg";

    public static PhBatteryDialog newInstance(int a_viewId){
        Bundle bundle = new Bundle();
        bundle.putInt(ARGS, a_viewId);

        PhBatteryDialog dialog = new PhBatteryDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

}
