package kr.ac.inha.mindscope.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.R;

public class CareChildFragment3 extends Fragment {

    EditText commentEditTextView;


    public CareChildFragment3() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_care_child3, container, false);

        commentEditTextView = view.findViewById(R.id.child3_comment);
        commentEditTextView.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b == false){

                }
            }
        });

        return view;
    }
}