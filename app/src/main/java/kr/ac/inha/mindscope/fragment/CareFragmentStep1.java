package kr.ac.inha.mindscope.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import kr.ac.inha.mindscope.R;

public class CareFragmentStep1 extends Fragment {

    private CareViewModel careViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        careViewModel =
                ViewModelProviders.of(this).get(CareViewModel.class);
        View root = inflater.inflate(R.layout.fragment_care, container, false);
//        final TextView textView = root.findViewById(R.id.text_care);
//        careViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }
}