package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.R;

public class CareFragmentStep1 extends Fragment {

    private static final String TAG = "CareFragmentStep1";

    SharedPreferences lastPagePrefs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_care, container, false);
        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
        lastPagePrefs = requireActivity().getSharedPreferences("LastPage", Context.MODE_PRIVATE);
        SharedPreferences.Editor lastPagePrefsEditor = lastPagePrefs.edit();
        lastPagePrefsEditor.putString("last_open_nav_frg", "care");
        lastPagePrefsEditor.apply();
    }
}