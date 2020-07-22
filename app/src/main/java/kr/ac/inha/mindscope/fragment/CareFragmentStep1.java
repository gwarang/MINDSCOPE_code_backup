package kr.ac.inha.mindscope.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.R;

public class CareFragmentStep1 extends Fragment {

    private static final String TAG = "CareFragmentStep1";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_care, container, false);
        return root;
    }
}