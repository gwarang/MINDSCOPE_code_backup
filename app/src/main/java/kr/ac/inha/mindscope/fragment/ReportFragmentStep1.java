package kr.ac.inha.mindscope.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;

public class ReportFragmentStep1 extends Fragment {

    private static final String TAG = "ReportFragmentStep1";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report, container, false);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
    }
}