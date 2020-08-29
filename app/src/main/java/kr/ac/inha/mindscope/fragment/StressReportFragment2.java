package kr.ac.inha.mindscope.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import static kr.ac.inha.mindscope.Tools.CATEGORY_ACTIVITY_END_INDEX;
import static kr.ac.inha.mindscope.Tools.CATEGORY_ENTERTAIN_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_FOOD_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_LOCATION_END_INDEX;
import static kr.ac.inha.mindscope.Tools.CATEGORY_SNS_APP_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_SOCIAL_END_INDEX_EXCEPT_SNS_USAGE;
import static kr.ac.inha.mindscope.Tools.CATEGORY_UNLOCK_DURACTION_APP_USAGE;
import static kr.ac.inha.mindscope.services.MainService.STRESS_REPORT_NOTIFI_ID;
import static kr.ac.inha.mindscope.services.StressReportDownloader.SELF_STRESS_REPORT_RESULT;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StressReportFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StressReportFragment2 extends Fragment {

    private static final String TAG = "StressReportFragment2";
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
    ScrollView reasonContainer;
    Button yesBtn;
    Button noBtn;
    Button reportBtn;
    LinearLayout loadingLayout;
    ConstraintLayout analysisSelectContainer;
    private int analysisResult = NOT_SELECT_ANALYSIS_CORRECTNESS;
    private static final int NOT_SELECT_ANALYSIS_CORRECTNESS = 5;
    private static final int INCORRECT_ANALYSIS_RESULT = 0;
    private static final int CORRECT_ANALYSIS_RESULT = 1;
    private static final int NO_FEATURES_ANALYSIS_RESULT = 2;


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
        try {
            bundle.putDouble("accuracy", accuracy);
        } catch (NullPointerException e) {
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
//            this.order = getArguments().getInt("order");
            Calendar cal = Calendar.getInstance();
            this.order = Tools.getReportOrderFromRangeAfterReport(cal);
            this.accuracy = getArguments().getDouble("accuracy");
            this.feature_ids = getArguments().getString("feature_ids"); //
            Log.d(TAG, String.format("%d %d %d %d %d %.2f %s", reportTimestamp, stressLevel, reportAnswer, day_num, order, accuracy, feature_ids));
        } else {
            Log.d(TAG, "getArguments null");
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
        analysisSelectContainer = view.findViewById(R.id.btn_container);

        if (feature_ids != null)
            featureViewUpdate(feature_ids, view);

        if (stressLevel == reportAnswer) {
            correctnessView.setText(getResources().getString(R.string.string_prediction_correct));
        } else {
            correctnessView.setText(getResources().getString(R.string.string_prediction_incorrect));
        }

        final String accTxt1 = "사실 저는 ";
        final String accTxt2 = String.format("%.2f", (accuracy.floatValue() * 100), Locale.getDefault()) + "%";
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
        Context context = requireContext();

        ArrayList<String> phoneReason = new ArrayList<>();
        ArrayList<String> activityReason = new ArrayList<>();
        ArrayList<String> socialReason = new ArrayList<>();
        ArrayList<String> locationReason = new ArrayList<>();
        ArrayList<String> sleepReason = new ArrayList<>();

        boolean noFeatures = false;

        if (feature_ids.equals("") || feature_ids.equals("NO_FEATURES")) {
            Log.d(TAG, "feature_ids is empty");
            noFeatures = true;
        } else {
            String[] featureArray = feature_ids.split(" ");

            for (int i = 0; i < featureArray.length; i++) {
                String[] splitArray = featureArray[i].split("-");
                int category = Integer.parseInt(splitArray[0]);
                if((category == 12 || category == 18 || category == 29) && (splitArray[1].equals("general_1") || splitArray[1].equals("general_0"))){
                    category--;
                }
                String applicationName = "";

                if (splitArray[1].contains("&") && (category == CATEGORY_SNS_APP_USAGE
                        || (category >= CATEGORY_ENTERTAIN_APP_USAGE && category <= CATEGORY_FOOD_APP_USAGE))) {
                    String[] packageSplit = splitArray[1].split("&");
                    splitArray[1] = packageSplit[0];
                    if (packageSplit.length > 1) {
                        String packageName = packageSplit[1];
                        final PackageManager pm = requireActivity().getApplicationContext().getPackageManager();
                        ApplicationInfo ai;
                        try {
                            ai = pm.getApplicationInfo(packageName, 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            ai = null;
                            e.printStackTrace();
                        }
                        applicationName = (String) (ai != null ? "(" + pm.getApplicationLabel(ai) + ")" : "");
                    }
                }

                String strID = "@string/feature_" + splitArray[0] + splitArray[splitArray.length - 1];
                String packName = MainActivity.getInstance().getPackageName();
                int resId = context.getResources().getIdentifier(strID, "string", packName);

                if (category <= CATEGORY_ACTIVITY_END_INDEX) {
                    activityReason.add(context.getResources().getString(resId));
                } else if (category <= CATEGORY_SOCIAL_END_INDEX_EXCEPT_SNS_USAGE) {
                    socialReason.add(context.getResources().getString(resId));
                } else if (category == CATEGORY_SNS_APP_USAGE) {
                    String text = String.format(context.getResources().getString(resId), applicationName);
                    socialReason.add(text);
                } else if (category <= CATEGORY_LOCATION_END_INDEX) {
                    locationReason.add(context.getResources().getString(resId));
                } else if (category <= CATEGORY_UNLOCK_DURACTION_APP_USAGE) {
                    phoneReason.add(context.getResources().getString(resId));
                } else if (category <= CATEGORY_FOOD_APP_USAGE) {
                    String text = String.format(context.getResources().getString(resId), applicationName);
                    phoneReason.add(text);
                } else {
                    sleepReason.add(context.getResources().getString(resId));
                }


                if (i == 4) // maximun number of showing feature is five
                    break;
            }
        }


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

        TextView noFeatureTextview = view.findViewById(R.id.stress_report_no_features);

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

        if (noFeatures) {
            phoneContainer.setVisibility(View.GONE);
            activityContainer.setVisibility(View.GONE);
            socialContainer.setVisibility(View.GONE);
            locationContainer.setVisibility(View.GONE);
            sleepContainer.setVisibility(View.GONE);
            noFeatureTextview.setVisibility(View.VISIBLE);
            analysisResult = NO_FEATURES_ANALYSIS_RESULT;
            analysisSelectContainer.setVisibility(View.INVISIBLE);
        } else {
            phoneListView.setAdapter(phoneAdapter);
            activityListView.setAdapter(activityAdapter);
            socialListView.setAdapter(socialAdapter);
            locationListView.setAdapter(locationAdapter);
            sleepListView.setAdapter(sleepAdapter);
            noFeatureTextview.setVisibility(View.GONE);


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
            if (analysisResult == NOT_SELECT_ANALYSIS_CORRECTNESS) {
                Toast.makeText(getContext(), "분석이 맞았는지 선택해주세요!", Toast.LENGTH_LONG).show();
            } else {
                Context context = requireContext();
                new Thread(() -> requireActivity().runOnUiThread(() -> loadingLayout.setVisibility(View.VISIBLE))).start();

                Tools.updatePoint(context);
                if (order == REPORTNUM4) {
                    SharedPreferences stressReportPrefs = context.getSharedPreferences("stressReport", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = stressReportPrefs.edit();
                    editor.putLong("reportTimestamp", reportTimestamp);
                    editor.putInt("reportAnswer", reportAnswer);
                    editor.putInt("day_num", day_num);
                    editor.putInt("order", order);
                    editor.putFloat("accuracy", accuracy.floatValue());
                    editor.putString("feature_ids", feature_ids);
                    editor.putBoolean("today_last_report", true);
                    editor.apply();

                    SharedPreferences reportSubmitCheckPrefs = context.getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
                    SharedPreferences.Editor reportSubmitEditor = reportSubmitCheckPrefs.edit();
                    String reportSubmit = "self_report_submit_check_" + order;
                    reportSubmitEditor.putBoolean(reportSubmit, true);
                    long timestamp = System.currentTimeMillis();
                    reportSubmitEditor.putInt("reportSubmitDate", day_num);
                    reportSubmitEditor.apply();


                    SharedPreferences prefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
                    int dataSourceId = prefs.getInt("SELF_STRESS_REPORT", -1);
                    assert dataSourceId != -1;
                    Log.d(TAG, "SELF_STRESS_REPORT dataSourceId: " + dataSourceId);
                    DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f, timestamp, day_num, order, analysisResult, reportAnswer);

                    String oneReportWithTimestamp = String.format(Locale.KOREA, "%d,%d,%d,%d,%d\n",
                            timestamp,
                            day_num,
                            order,
                            analysisResult,
                            reportAnswer);
//                                            timestamp + "#" + stressLv + "#" + stressReportJSON.getString(String.valueOf(stressLv));
                    FileOutputStream fileOutputStream;
                    try {
                        fileOutputStream = context.openFileOutput(SELF_STRESS_REPORT_RESULT, Context.MODE_APPEND);
                        fileOutputStream.write(oneReportWithTimestamp.getBytes());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("reportTimestamp", reportTimestamp);
                    intent.putExtra("reportAnswer", reportAnswer);
                    intent.putExtra("day_num", day_num);
                    intent.putExtra("order", order);
                    intent.putExtra("accuracy", accuracy);
                    intent.putExtra("feature_ids", feature_ids);
                    intent.putExtra("get_point", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel(STRESS_REPORT_NOTIFI_ID);
                    }

                    Tools.saveApplicationLog(context, TAG, Tools.ACTION_CLICK_COMPLETE_BUTTON, analysisResult);
                } else {
                    // 그 외는 MainActivity로

                    SharedPreferences stressReportPrefs = context.getSharedPreferences("stressReport", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = stressReportPrefs.edit();
                    editor.putLong("reportTimestamp", reportTimestamp);
                    editor.putInt("reportAnswer", reportAnswer);
                    editor.putInt("day_num", day_num);
                    editor.putInt("order", order);
                    editor.putFloat("accuracy", accuracy.floatValue());
                    editor.putBoolean("today_last_report", false);
                    editor.putString("feature_ids", feature_ids);
                    editor.apply();

                    SharedPreferences reportSubmitCheckPrefs = context.getSharedPreferences("SubmitCheck", Context.MODE_PRIVATE);
                    SharedPreferences.Editor reportSubmitEditor = reportSubmitCheckPrefs.edit();
                    String reportSubmit = "self_report_submit_check_" + order;
                    Log.e(TAG, reportSubmit);
                    reportSubmitEditor.putBoolean(reportSubmit, true);
                    reportSubmitEditor.putInt("reportSubmitDate", day_num);
                    reportSubmitEditor.apply();

                    long timestamp = System.currentTimeMillis();
                    SharedPreferences prefs = context.getSharedPreferences("Configurations", Context.MODE_PRIVATE);
                    int dataSourceId = prefs.getInt("SELF_STRESS_REPORT", -1);
                    assert dataSourceId != -1;
                    Log.d(TAG, "SELF_STRESS_REPORT dataSourceId: " + dataSourceId);
                    DbMgr.saveMixedData(dataSourceId, timestamp, 1.0f, timestamp, day_num, order, analysisResult, reportAnswer);
                    String oneReportWithTimestamp = String.format(Locale.KOREA, "%d,%d,%d,%d,%d\n",
                            timestamp,
                            day_num,
                            order,
                            analysisResult,
                            reportAnswer);
//                                            timestamp + "#" + stressLv + "#" + stressReportJSON.getString(String.valueOf(stressLv));
                    FileOutputStream fileOutputStream;
                    try {
                        fileOutputStream = context.openFileOutput(SELF_STRESS_REPORT_RESULT, Context.MODE_APPEND);
                        fileOutputStream.write(oneReportWithTimestamp.getBytes());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("reportTimestamp", reportTimestamp);
                    intent.putExtra("reportAnswer", reportAnswer);
                    intent.putExtra("day_num", day_num);
                    intent.putExtra("order", order);
                    intent.putExtra("accuracy", accuracy);
                    intent.putExtra("feature_ids", feature_ids);
                    intent.putExtra("get_point", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.cancel(STRESS_REPORT_NOTIFI_ID);
                    }

                    Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_CLICK_COMPLETE_BUTTON, analysisResult);
                }
            }
        }
    };

    View.OnClickListener yesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            analysisResult = CORRECT_ANALYSIS_RESULT;
            yesBtn.setSelected(true);
            yesBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor_blue));
            noBtn.setSelected(false);
            noBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor_light));
        }
    };

    View.OnClickListener noClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            analysisResult = INCORRECT_ANALYSIS_RESULT;
            yesBtn.setSelected(false);
            yesBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor_light));
            noBtn.setSelected(true);
            noBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor_blue));
        }
    };

}