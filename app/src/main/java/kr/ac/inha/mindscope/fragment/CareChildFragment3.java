package kr.ac.inha.mindscope.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Calendar;

import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.DbMgr;
import kr.ac.inha.mindscope.R;

public class CareChildFragment3 extends Fragment {

    private static final String TAG = "CasreChild3Fragment3";

    EditText commentEditTextView;


    public CareChildFragment3() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (DbMgr.getDB() == null)
            DbMgr.init(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_care_child3, container, false);

        commentEditTextView = view.findViewById(R.id.child3_comment);
        SharedPreferences commentPrefs = getActivity().getSharedPreferences("comment", Context.MODE_PRIVATE);

        if(!commentPrefs.getString("daily_comment", "").equals(""))
            commentEditTextView.setText(commentPrefs.getString("daily_comment", ""));

        commentEditTextView.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    SharedPreferences commentPrefs = getActivity().getSharedPreferences("comment", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = commentPrefs.edit();
                    editor.putString("daily_comment", commentEditTextView.getText().toString());

                    Calendar cal = Calendar.getInstance();
                    int date = cal.get(Calendar.DATE);
                    editor.putInt("date_comment", date);
                    editor.apply();

                    String daily_comment = commentPrefs.getString("daily_comment", "");

                    if(!daily_comment.equals("")){
                        long curTimestamp = System.currentTimeMillis();
                        SharedPreferences prefs = getActivity().getSharedPreferences("Configurations", Context.MODE_PRIVATE);
                        int dataSourceId = prefs.getInt("DAILY_COMMENT", -1);
                        assert dataSourceId != -1;
                        Log.i(TAG, "DAILY_COMMENT dataSourceId: " + dataSourceId);
                        DbMgr.saveMixedData(dataSourceId, curTimestamp, 1.0f, curTimestamp, daily_comment);
                    }
                }
            }
        });

        return view;
    }
}