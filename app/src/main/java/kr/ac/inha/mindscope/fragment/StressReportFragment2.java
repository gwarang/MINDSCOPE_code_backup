package kr.ac.inha.mindscope.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import kr.ac.inha.mindscope.DbMgr;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;

import static kr.ac.inha.mindscope.StressReportActivity.REPORTNUM4;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV1;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV2;
import static kr.ac.inha.mindscope.StressReportActivity.STRESS_LV3;
import static kr.ac.inha.mindscope.services.MainService.STRESS_REPORT_NOTIFI_ID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StressReportFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StressReportFragment2 extends Fragment {

    private static final String TAG = "StressReportFragment2";
    private static final int YES_BTN = 1;
    private static final int NO_BTN = 2;
    public int reportAnswer;
    public int stressLevel;
    public int day_num;
    public int order;
    public Double accuracy;
    public long reportTimestamp;
    public String feature_ids;
    TextView correctnessView;
    TextView accView;
    ImageView stressImg;
    TextView stressLevelView;
    TextView reason1;
    TextView reason2;
    TextView reason3;
    TextView reason4;
    TextView reason5;
    ScrollView reasonContainer;
    Button yesBtn;
    Button noBtn;
    Button reportBtn;
    LinearLayout loadingLayout;
    private int yesOrNo = 2;



    public StressReportFragment2() {
        // Required empty public constructor
    }

    public static StressReportFragment2 newInstance(long reportTimestamp, int stressLevel, int reportAnswer, int day_num, int order, Double accuracy, String feature_ids) {
        StressReportFragment2 fragment2 = new StressReportFragment2();
        Bundle bundle = new Bundle();
        bundle.putLong("reportTimestamp", reportTimestamp);
        bundle.putInt("stressLevel", stressLevel);
        bundle.putInt("reportAnswer", reportAnswer);
        bundle.putInt("day_num", day_num);
        bundle.putInt("order", order);
        try{
            bundle.putDouble("accuracy", accuracy);
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        bundle.putString("feature_ids", feature_ids);
        fragment2.setArguments(bundle);
        return fragment2;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.reportTimestamp = getArguments().getLong("reportTimestamp");
            this.stressLevel = getArguments().getInt("stressLevel");
            this.reportAnswer = getArguments().getInt("reportAnswer");
            this.day_num = getArguments().getInt("day_num");
            this.order = getArguments().getInt("order");
            this.accuracy = getArguments().getDouble("accuracy");
            this.feature_ids = getArguments().getString("feature_ids"); //
            Log.i(TAG, String.format("%d %d %d %d %d %.2f %s", reportTimestamp, stressLevel, reportAnswer, day_num, order, accuracy, feature_ids));
        } else {
            Log.i(TAG, "getArguments null");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_OPEN_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stress_report2, container, false);

        loadingLayout = view.findViewById(R.id.loading_frame_stress_report);
        loadingLayout.setVisibility(View.GONE);
        correctnessView = view.findViewById(R.id.txt_yes_no);
        accView = view.findViewById(R.id.prediction_acc);
        stressImg = view.findViewById(R.id.frg_report_step2_img);
        stressLevelView = view.findViewById(R.id.txt_stress_level);
        yesBtn = view.findViewById(R.id.btn_correct);
        noBtn = view.findViewById(R.id.btn_incorrect);
        reportBtn = view.findViewById(R.id.toolbar_report_btn2);
        reasonContainer = view.findViewById(R.id.stress_report_reason_container);

        if(feature_ids != null)
            featureViewUpdate(feature_ids, view);

        if (stressLevel == reportAnswer) {
            correctnessView.setText(getResources().getString(R.string.string_prediction_correct));
        } else {
            correctnessView.setText(getResources().getString(R.string.string_prediction_incorrect));
        }

        final String accTxt1 = "사실 저는 ";
        final String accTxt2 = String.format("%.2f", accuracy) + "%";
        final String accTxt3 = "의 확신을 가지고 있었어요.";
        String accTxtResult = accTxt1 + accTxt2 + accTxt3;
        Spannable spannable = new SpannableString(accTxtResult);
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.textColor_blue)), accTxt1.length(), (accTxt1 + accTxt2).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        accView.setText(spannable, TextView.BufferType.SPANNABLE);

        switch (reportAnswer) {
            case STRESS_LV1:
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_low, requireActivity().getTheme()));
                stressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_result_low)));
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_low_bg, requireActivity().getTheme()));
                break;
            case STRESS_LV2:
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_littlehigh, requireActivity().getTheme()));
                stressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_result_littlehigh)));
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_littlehigh_bg, requireActivity().getTheme()));
                break;
            case STRESS_LV3:
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_high, requireActivity().getTheme()));
                stressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_result_high)));
                reasonContainer.setBackgroundColor(getResources().getColor(R.color.color_high_bg, requireActivity().getTheme()));
                break;
        }

        yesBtn.setOnClickListener(yesClickListener);
        noBtn.setOnClickListener(noClickListener);
        reportBtn.setOnClickListener(reportClickListener);

        return view;
    }

    public void featureViewUpdate(String feature_ids, View view) {
        String[] featureArray = feature_ids.split(" ");

        ArrayList<String> phoneReason = new ArrayList<>();
        ArrayList<String> activityReason = new ArrayList<>();
        ArrayList<String> socialReason = new ArrayList<>();
        ArrayList<String> locationReason = new ArrayList<>();
        ArrayList<String> sleepReason = new ArrayList<>();


        for (int i = 0; i < featureArray.length; i++) {
            String[] splitArray = featureArray[i].split("-");
            int category = Integer.parseInt(splitArray[0]);
            String strID = "@string/feature_" + splitArray[0] + splitArray[1];
            String packName = requireActivity().getPackageName();
            int resId = getResources().getIdentifier(strID, "string", packName);

            if (category <= 5) {
                activityReason.add(getResources().getString(resId));
            } else if (category <= 11) {
                socialReason.add(getResources().getString(resId));
            } else if (category <= 16) {
                locationReason.add(getResources().getString(resId));
            } else if (category <= 28) {
                phoneReason.add(getResources().getString(resId));
            } else {
                sleepReason.add(getResources().getString(resId));
            }

            if (i == 4) // maximum number of showing feature is five
                break;
        }

        Log.d(TAG, "phoneReason" + phoneReason.toString());
        Log.d(TAG, "activityReason" + activityReason.toString());

        ListView phoneListView = view.findViewById(R.id.listview_phone);
        ListView activityListView = view.findViewById(R.id.listview_activity);
        ListView socialListView = view.findViewById(R.id.listview_social);
        ListView locationListView = view.findViewById(R.id.listview_location);
        ListView sleepListView = view.findViewById(R.id.listview_sleep);
        LinearLayout phoneContainer = view.findViewById(R.id.listview_phone_container);
        LinearLayout activityContainer = view.findViewById(R.id.listview_activity_container);
        LinearLayout socialContainer = view.findViewById(R.id.listview_social_container);
        LinearLayout locationContainer = view.findViewById(R.id.listview_location_container);
        LinearLayout sleepContainer = view.findViewById(R.id.listview_sleep_container);


        ArrayAdapter<String> phoneAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, phoneReason
        );
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, activityReason
        );
        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, socialReason
        );
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, locationReason
        );
        ArrayAdapter<String> sleepAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, sleepReason
        );

        phoneListView.setAdapter(phoneAdapter);
        activityListView.setAdapter(activityAdapter);
        socialListView.setAdapter(socialAdapter);
        locationListView.setAdapter(locationAdapter);
        sleepListView.setAdapter(sleepAdapter);


        if (phoneReason.isEmpty())
            phoneContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(phoneListView);
            phoneContainer.setVisibility(View.VISIBLE);
        }

        if (activityReason.isEmpty())
            activityContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(activityListView);
            activityContainer.setVisibility(View.VISIBLE);
        }

        if (socialReason.isEmpty())
            socialContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(socialListView);
            socialContainer.setVisibility(View.VISIBLE);
        }

        if (locationReason.isEmpty())
            locationContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(locationListView);
            locationContainer.setVisibility(View.VISIBLE);
        }

        if (sleepReason.isEmpty())
            sleepContainer.setVisibility(View.GONE);
        else {
            setListViewHeightBasedOnChildren(sleepListView);
            sleepContainer.setVisibility(View.VISIBLE);
        }

    }

    public static void setListViewHeightBasedOnChildren(@NonNull ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    View.OnClickListener reportClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (yesOrNo == 2) {
                Toast.makeText(getContext(), "분석이 맞았는지 선택해주세요!", Toast.LENGTH_LONG).show();
            } else {
                Context context = requireContext();
                new Thread(() -> getActivity().runOnUiThread(() -> loadingLayout.setVisibility(View.VISIBLE))).start();

                Tools.updatePoint(context);
                if (order == REPORTNUM4) {
                    // TODO 하루의 마지막 리포트이면 '마음케어'로 이동하도록 구현, 지금은 그냥 main으로 이동함

                    SharedPreferences stressReportPrefs = context.getSharedPreferences("stressReport", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = stressReportPrefs.edit();
                    editor.putLong("reportTimestamp", reportTimestamp);
                    editor.putInt("reportAnswer", reportAnswer);
                    editor.putInt("day_num", day_num);
                    editor.putInt("order", order);
                    editor.putFloat("accuracy", accuracy.floatValue());
                    editor.putString("feature_ids", feature_ids);
                    editor.apply();

                    long timestamp = System.currentTimeMillis();

                    SharedPreferences prefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
                    int dataSourceId = prefs.getInt("SELF_STRESS_REPORT", -1);
                    assert dataSourceId != -1;
                    Log.i(TAG, "SELF_STRESS_REPORT dataSourceId: " + dataSourceId);
                    DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f, timestamp, day_num, order, yesOrNo, reportAnswer);


                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("reportTimestamp", reportTimestamp);
                    intent.putExtra("reportAnswer", reportAnswer);
                    intent.putExtra("day_num", day_num);
                    intent.putExtra("order", order);
                    intent.putExtra("accuracy", accuracy);
                    intent.putExtra("feature_ids", feature_ids);
                    intent.putExtra("get_point", true);
                    startActivity(intent);

                    final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel(STRESS_REPORT_NOTIFI_ID);
                    }

                    Tools.saveApplicationLog(context, TAG, Tools.ACTION_CLICK_COMPLETE_BUTTON, yesOrNo);
                } else {
                    // 그 외는 MainActivity로

                    SharedPreferences stressReportPrefs = context.getSharedPreferences("stressReport", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = stressReportPrefs.edit();
                    editor.putLong("reportTimestamp", reportTimestamp);
                    editor.putInt("reportAnswer", reportAnswer);
                    editor.putInt("day_num", day_num);
                    editor.putInt("order", order);
                    editor.putFloat("accuracy", accuracy.floatValue());
                    editor.putString("feature_ids", feature_ids);
                    editor.apply();

                    long timestamp = System.currentTimeMillis();
                    SharedPreferences prefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
                    int dataSourceId = prefs.getInt("SELF_STRESS_REPORT", -1);
                    assert dataSourceId != -1;
                    Log.i(TAG, "SELF_STRESS_REPORT dataSourceId: " + dataSourceId);
                    DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f, timestamp, day_num, order, yesOrNo, reportAnswer);

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("reportTimestamp", reportTimestamp);
                    intent.putExtra("reportAnswer", reportAnswer);
                    intent.putExtra("day_num", day_num);
                    intent.putExtra("order", order);
                    intent.putExtra("accuracy", accuracy);
                    intent.putExtra("feature_ids", feature_ids);
                    intent.putExtra("get_point", true);
                    startActivity(intent);

                    final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel(STRESS_REPORT_NOTIFI_ID);
                    }

                    Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_CLICK_COMPLETE_BUTTON, yesOrNo);
                }
            }
        }
    };

    View.OnClickListener yesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            yesOrNo = 1;
            yesBtn.setSelected(true);
            yesBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor_blue));
            noBtn.setSelected(false);
            noBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor_light));
        }
    };

    View.OnClickListener noClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            yesOrNo = 0;
            yesBtn.setSelected(false);
            yesBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor_light));
            noBtn.setSelected(true);
            noBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor_blue));
        }
    };

}