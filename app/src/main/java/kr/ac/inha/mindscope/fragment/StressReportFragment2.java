package kr.ac.inha.mindscope.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;

import kr.ac.inha.mindscope.DbMgr;
import kr.ac.inha.mindscope.MainActivity;
import kr.ac.inha.mindscope.R;
import kr.ac.inha.mindscope.Tools;
import kr.ac.inha.mindscope.dialog.AnalysisSurveyDialog;

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
import static kr.ac.inha.mindscope.Tools.CATEGORY_UNLOCK_DURATION_APP_USAGE;
import static kr.ac.inha.mindscope.services.MainService.STRESS_REPORT_NOTIFI_ID;
import static kr.ac.inha.mindscope.services.StressReportDownloader.SELF_STRESS_REPORT_RESULT;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StressReportFragment2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StressReportFragment2 extends Fragment {

    private static final String TAG = "StressReportFragment2";
    //region variable
    public static final int CONDITION1 = 1;
    public static final int CONDITION2 = 2;
    public static final int CONDITION3 = 3;
    private static final int CONDITION_DURATION1 = 5;
    private static final int CONDITION_DURATION2 = 10;
    private static final int CONDITION_DURATION3 = 15;
    public int condition;
    public int reportAnswer;
    public int stressLevel;
    public int day_num;
    public int order;
    static int lastReportHours;
    public Double accuracy;
    public long reportTimestamp;
    public String feature_ids;
    TextView correctnessView;
    TextView accView;
    TextView dateView;
    TextView timeView;
    TextView reason;
    ImageView stressImg;
    ImageView condition2Img1;
    ImageView condition2Img2;
    ImageView condition2Img3;
    ImageView condition2Img4;
    ImageView condition2Img5;
    TextView stressLevelView;
    TextView condition2txt1;
    TextView condition2txt2;
    TextView condition2txt3;
    TextView condition2txt4;
    TextView condition2txt5;
    ScrollView reasonCondition3Container;
    Button yesBtn;
    Button noBtn;
    Button reportBtn;
    LinearLayout loadingLayout;
    ConstraintLayout analysisSelectContainer;
    ConstraintLayout condition3Container;
    ConstraintLayout condition2Container;
    LinearLayout reasonCondition2Container;
    private int analysisResult = NOT_SELECT_ANALYSIS_CORRECTNESS;
    private static final int NOT_SELECT_ANALYSIS_CORRECTNESS = 5;
    private static final int INCORRECT_ANALYSIS_RESULT = 0;
    private static final int CORRECT_ANALYSIS_RESULT = 1;
    private static final int NO_FEATURES_ANALYSIS_RESULT = 2;
    AnalysisSurveyDialog analysisSurveyDialog;
    RelativeLayout categoryImgContainer1;
    RelativeLayout categoryImgContainer2;
    RelativeLayout categoryImgContainer3;
    RelativeLayout categoryImgContainer4;
    RelativeLayout categoryImgContainer5;
    LinearLayout redirect;
    AppBarLayout frg_report_app_bar;
    //endregion


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

        SharedPreferences datePrefs = requireContext().getSharedPreferences("stepChange", Context.MODE_PRIVATE);
        condition = datePrefs.getInt("condition", 0);

//        condition = CONDITION1;

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
        Context context = requireContext();
        frg_report_app_bar = view.findViewById(R.id.frg_report_app_bar);
        redirect = view.findViewById(R.id.redirect);
        loadingLayout = view.findViewById(R.id.loading_frame_stress_report);
        loadingLayout.setVisibility(View.GONE);
//        correctnessView = view.findViewById(R.id.txt_yes_no);
//        accView = view.findViewById(R.id.prediction_acc);
        stressImg = view.findViewById(R.id.frg_report_step2_img);
        stressLevelView = view.findViewById(R.id.txt_stress_level);
        yesBtn = view.findViewById(R.id.btn_correct);
        noBtn = view.findViewById(R.id.btn_incorrect);
        reportBtn = view.findViewById(R.id.toolbar_report_btn2);
        reasonCondition3Container = view.findViewById(R.id.stress_report_reason_container);
        analysisSelectContainer = view.findViewById(R.id.btn_container);
        reason = view.findViewById(R.id.txt_reason);
        dateView = view.findViewById(R.id.txt_stress_day);
        timeView = view.findViewById(R.id.txt_stress_time);
        condition3Container = view.findViewById(R.id.condition3_container);
        condition2Container = view.findViewById(R.id.condition2_container);
        reasonCondition2Container = view.findViewById(R.id.condition2_layout);
        condition2Img1 = view.findViewById(R.id.stress_report_img1);
        condition2Img2 = view.findViewById(R.id.stress_report_img2);
        condition2Img3 = view.findViewById(R.id.stress_report_img3);
        condition2Img4 = view.findViewById(R.id.stress_report_img4);
        condition2Img5 = view.findViewById(R.id.stress_report_img5);
        condition2txt1 = view.findViewById(R.id.stress_report_txt1);
        condition2txt2 = view.findViewById(R.id.stress_report_txt2);
        condition2txt3 = view.findViewById(R.id.stress_report_txt3);
        condition2txt4 = view.findViewById(R.id.stress_report_txt4);
        condition2txt5 = view.findViewById(R.id.stress_report_txt5);
        categoryImgContainer1 = view.findViewById(R.id.stress_report_img_container1);
        categoryImgContainer2 = view.findViewById(R.id.stress_report_img_container2);
        categoryImgContainer3 = view.findViewById(R.id.stress_report_img_container3);
        categoryImgContainer4 = view.findViewById(R.id.stress_report_img_container4);
        categoryImgContainer5 = view.findViewById(R.id.stress_report_img_container5);


//        if (stressLevel == reportAnswer) {
//            correctnessView.setText(getResources().getString(R.string.string_prediction_correct));
//        } else {
//            correctnessView.setText(getResources().getString(R.string.string_prediction_incorrect));
//        }

//        switch (reportAnswer){
//            case 0:
//                reason.setText(R.string.stress_report_lv0);
//                break;
//            case 1:
//                reason.setText(R.string.stress_report_lv1);
//                break;
//            case 2:
//
//                reason.setText(R.string.stress_report_lv2);
//                break;
//            default:
//        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(reportTimestamp);
        Date currentTime = new Date();
        currentTime.setTime(reportTimestamp);
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 (EE)", Locale.getDefault()).format(currentTime);
        dateView.setText(date_text);

        lastReportHours = cal.get(Calendar.HOUR_OF_DAY);

        if (lastReportHours >= 22) {
            timeView.setText(context.getResources().getString(R.string.time_report_duration4));
        } else if (lastReportHours >= 18) {
            timeView.setText(context.getResources().getString(R.string.time_report_duration3));
        } else if (lastReportHours >= 14) {
            timeView.setText(context.getResources().getString(R.string.time_report_duration2));
        } else if (lastReportHours >= 10) {
            timeView.setText(context.getResources().getString(R.string.time_report_duration1));
        } else {
            timeView.setText(context.getResources().getString(R.string.time_report_duration4));
        }

//        final String accTxt1 = "사실 저는 ";
//        final String accTxt2 = String.format("%.2f", (accuracy.floatValue() * 100), Locale.getDefault()) + "%";
//        final String accTxt3 = "의 확신을 가지고 있었어요.";
//        String accTxtResult = accTxt1 + accTxt2 + accTxt3;
//        Spannable spannable = new SpannableString(accTxtResult);
//        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.textColor_blue)), accTxt1.length(), (accTxt1 + accTxt2).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        accView.setText(spannable, TextView.BufferType.SPANNABLE);

        switch (reportAnswer) {
            case STRESS_LV1:
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_low, requireActivity().getTheme()));
                stressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_result_low)));
                reasonCondition3Container.setBackgroundColor(getResources().getColor(R.color.color_low_container, requireActivity().getTheme()));
//                reasonCondition2Container.setBackgroundColor(getResources().getColor(R.color.color_low_bg, requireActivity().getTheme()));
                break;
            case STRESS_LV2:
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_littlehigh, requireActivity().getTheme()));
                stressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_result_littlehigh)));
                reasonCondition3Container.setBackgroundColor(getResources().getColor(R.color.color_littlehigh_container, requireActivity().getTheme()));
//                reasonCondition2Container.setBackgroundColor(getResources().getColor(R.color.color_littlehigh_bg, requireActivity().getTheme()));
                break;
            case STRESS_LV3:
                stressImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_high, requireActivity().getTheme()));
                stressLevelView.setText(Html.fromHtml(getResources().getString(R.string.string_stress_result_high)));
                reasonCondition3Container.setBackgroundColor(getResources().getColor(R.color.color_high_container, requireActivity().getTheme()));
//                reasonCondition2Container.setBackgroundColor(getResources().getColor(R.color.color_high_bg, requireActivity().getTheme()));
                break;
        }

        yesBtn.setOnClickListener(yesClickListener);
        noBtn.setOnClickListener(noClickListener);
        reportBtn.setOnClickListener(reportClickListener);

        switch (condition){
            case CONDITION1:
                loadingLayout.setVisibility(View.VISIBLE);
                frg_report_app_bar.setVisibility(View.GONE);
                redirect.setVisibility(View.VISIBLE);
                submitReport();
                break;
            case CONDITION2:
                frg_report_app_bar.setVisibility(View.VISIBLE);
                redirect.setVisibility(View.GONE);
                condition2Container.setVisibility(View.VISIBLE);
                condition3Container.setVisibility(View.GONE);
                reason.setText("제가 참고한 데이터는요,");
                Log.d(TAG, "condition2");
                break;
            case CONDITION3:
            default:
                frg_report_app_bar.setVisibility(View.VISIBLE);
                redirect.setVisibility(View.GONE);
                condition2Container.setVisibility(View.GONE);
                condition3Container.setVisibility(View.VISIBLE);
                switch (reportAnswer){
                    case STRESS_LV1:
                        reason.setText("당신은 스트레스가 낮을 때,");
                        break;
                    case STRESS_LV2:
                        reason.setText("당신은 스트레스가 조금 높을 때,");
                        break;
                    case STRESS_LV3:
                        reason.setText("당신은 스트레스가 높을 때,");
                        break;
                }
                Log.d(TAG, "condition3");
                break;
        }



        if (feature_ids != null)
            featureViewUpdate(feature_ids, view);

        return view;
    }

    public void featureViewUpdate(String feature_ids, View view) {
        Context context = requireContext();

        ArrayList<String> integrateReason = new ArrayList<>();

//        ArrayList<String> phoneReason = new ArrayList<>();
//        ArrayList<String> activityReason = new ArrayList<>();
//        ArrayList<String> socialReason = new ArrayList<>();
//        ArrayList<String> locationReason = new ArrayList<>();
//        ArrayList<String> sleepReason = new ArrayList<>();


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
//                    activityReason.add(context.getResources().getString(resId));
                    condition2Img4.setAlpha(1.0f);
                    condition2txt4.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (reportAnswer){
                        case STRESS_LV1:
                            categoryImgContainer4.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer4.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer4.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category <= CATEGORY_SOCIAL_END_INDEX_EXCEPT_SNS_USAGE) {
//                    socialReason.add(context.getResources().getString(resId));
                    condition2Img2.setAlpha(1.0f);
                    condition2txt2.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (reportAnswer){
                        case STRESS_LV1:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category == CATEGORY_SNS_APP_USAGE) {
//                    String text = String.format(context.getResources().getString(resId), applicationName);
//                    socialReason.add(text);
                    condition2Img2.setAlpha(1.0f);
                    condition2txt2.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (reportAnswer){
                        case STRESS_LV1:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer2.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category <= CATEGORY_LOCATION_END_INDEX) {
                    condition2Img3.setAlpha(1.0f);
                    condition2txt3.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (reportAnswer){
                        case STRESS_LV1:
                            categoryImgContainer3.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer3.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer3.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category <= CATEGORY_UNLOCK_DURATION_APP_USAGE) {
                    condition2Img1.setAlpha(1.0f);
                    condition2txt1.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (reportAnswer){
                        case STRESS_LV1:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else if (category <= CATEGORY_FOOD_APP_USAGE) {
//                    String text = String.format(context.getResources().getString(resId), applicationName);
//                    phoneReason.add(text);
                    condition2Img1.setAlpha(1.0f);
                    condition2txt1.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (reportAnswer){
                        case STRESS_LV1:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer1.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                } else {
//                    sleepReason.add(context.getResources().getString(resId));
                    condition2Img5.setAlpha(1.0f);
                    condition2txt5.setTextColor(requireContext().getColor(R.color.textColor_default));
                    switch (reportAnswer){
                        case STRESS_LV1:
                            categoryImgContainer5.setBackgroundColor(requireContext().getColor(R.color.color_low_bg));
                            break;
                        case STRESS_LV2:
                            categoryImgContainer5.setBackgroundColor(requireContext().getColor(R.color.color_littlehigh_bg));
                            break;
                        case STRESS_LV3:
                            categoryImgContainer5.setBackgroundColor(requireContext().getColor(R.color.color_high_bg));
                            break;
                    }
                }

                if( category == CATEGORY_SNS_APP_USAGE || (category <= CATEGORY_FOOD_APP_USAGE && category > CATEGORY_UNLOCK_DURATION_APP_USAGE)){
                    String text = String.format(context.getResources().getString(resId), applicationName);
                    integrateReason.add(text);
                }else{
                    integrateReason.add(context.getResources().getString(resId));
                }

                if (i == 4) // maximun number of showing feature is five
                    break;
            }
        }

        ListView integrateListView = view.findViewById(R.id.listview_integrate);
//        ListView phoneListView = view.findViewById(R.id.listview_phone);
//        ListView activityListView = view.findViewById(R.id.listview_activity);
//        ListView socialListView = view.findViewById(R.id.listview_social);
//        ListView locationListView = view.findViewById(R.id.listview_location);
//        ListView sleepListView = view.findViewById(R.id.listview_sleep);
        LinearLayout integrateContainer = view.findViewById(R.id.listview_integrate_container);
//        LinearLayout phoneContainer = view.findViewById(R.id.listview_phone_container);
//        LinearLayout activityContainer = view.findViewById(R.id.listview_activity_container);
//        LinearLayout socialContainer = view.findViewById(R.id.listview_social_container);
//        LinearLayout locationContainer = view.findViewById(R.id.listview_location_container);
//        LinearLayout sleepContainer = view.findViewById(R.id.listview_sleep_container);

        TextView noFeatureTextview = view.findViewById(R.id.stress_report_no_features);


        ArrayAdapter<String> integrateAdapter = new ArrayAdapter<>(
                requireContext(), R.layout.item_feature_ids, integrateReason
        );
//        ArrayAdapter<String> phoneAdapter = new ArrayAdapter<>(
//                requireContext(), R.layout.item_feature_ids, phoneReason
//        );
//        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(
//                requireContext(), R.layout.item_feature_ids, activityReason
//        );
//        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(
//                requireContext(), R.layout.item_feature_ids, socialReason
//        );
//        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
//                requireContext(), R.layout.item_feature_ids, locationReason
//        );
//        ArrayAdapter<String> sleepAdapter = new ArrayAdapter<>(
//                requireContext(), R.layout.item_feature_ids, sleepReason
//        );

        if(integrateReason.isEmpty())
            integrateContainer.setVisibility(View.GONE);
        else{
            setListViewHeightBasedOnChildren(integrateListView);
            integrateContainer.setVisibility(View.VISIBLE);
        }

        if (noFeatures) {
            integrateContainer.setVisibility(View.GONE);
            noFeatureTextview.setVisibility(View.VISIBLE);
            analysisResult = NO_FEATURES_ANALYSIS_RESULT;
            analysisSelectContainer.setVisibility(View.INVISIBLE);
            condition2Container.setVisibility(View.GONE);
            condition3Container.setVisibility(View.VISIBLE);
        } else {
            integrateListView.setAdapter(integrateAdapter);
//            phoneListView.setAdapter(phoneAdapter);
//            activityListView.setAdapter(activityAdapter);
//            socialListView.setAdapter(socialAdapter);
//            locationListView.setAdapter(locationAdapter);
//            sleepListView.setAdapter(sleepAdapter);
            noFeatureTextview.setVisibility(View.GONE);



//            if (phoneReason.isEmpty())
//                phoneContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(phoneListView);
//                phoneContainer.setVisibility(View.VISIBLE);
//            }
//
//            if (activityReason.isEmpty())
//                activityContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(activityListView);
//                activityContainer.setVisibility(View.VISIBLE);
//            }
//
//            if (socialReason.isEmpty())
//                socialContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(socialListView);
//                socialContainer.setVisibility(View.VISIBLE);
//            }
//
//            if (locationReason.isEmpty())
//                locationContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(locationListView);
//                locationContainer.setVisibility(View.VISIBLE);
//            }
//
//            if (sleepReason.isEmpty())
//                sleepContainer.setVisibility(View.GONE);
//            else {
//                setListViewHeightBasedOnChildren(sleepListView);
//                sleepContainer.setVisibility(View.VISIBLE);
//            }
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
                submitReport();
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

    public boolean checkSurveyCount(){
        SharedPreferences stressReportPrefs = requireContext().getSharedPreferences("stressReport", Context.MODE_PRIVATE);
        SharedPreferences.Editor stressReportPrefsEditor = stressReportPrefs.edit();
        int noCount = stressReportPrefs.getInt("no_count", 0) + 1;
        stressReportPrefsEditor.putInt("no_count", noCount);
        stressReportPrefsEditor.apply();

        // todo 1 -> 3
        if(noCount >= 3){
            stressReportPrefsEditor.putInt("no_count", 0);
            stressReportPrefsEditor.apply();
            return true;
        }

        return false;
    }

    public void surveyDialog(){
        Log.d(TAG, "survey dialog call function");
        analysisSurveyDialog = new AnalysisSurveyDialog(requireContext(), analysisSurveyDismiss, analysisSurveySubmit);
        analysisSurveyDialog.setCancelable(false);
        analysisSurveyDialog.show();
    }

    private View.OnClickListener analysisSurveyDismiss = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            analysisSurveyDialog.dismiss();
            startMainActivity(requireContext());
        }
    };

    private View.OnClickListener analysisSurveySubmit = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            long timestamp = System.currentTimeMillis();
            boolean cb1, cb2, cb3, cb4, cb5;

            String cb5_str = "NON";

            SharedPreferences prefs = requireContext().getSharedPreferences("Configurations", Context.MODE_PRIVATE);
            SharedPreferences stressReportPrefs = requireContext().getSharedPreferences("stressReport", Context.MODE_PRIVATE);

            cb1 = stressReportPrefs.getBoolean("cb1", false);
            cb2 = stressReportPrefs.getBoolean("cb2", false);
            cb3 = stressReportPrefs.getBoolean("cb3", false);
            cb4 = stressReportPrefs.getBoolean("cb4", false);
            cb5 = stressReportPrefs.getBoolean("cb5", false);

            if(cb5){
                cb5_str = stressReportPrefs.getString("cb5_str", "NON");
            }

            int dataSourceId = prefs.getInt("REASON_FOR_ANSWER", -1);
            assert dataSourceId != -1;

            DbMgr.saveMixedData(dataSourceId,
                    timestamp,
                    1.0f,
                    timestamp,
                    cb1,
                    cb2,
                    cb3,
                    cb4,
                    cb5,
                    cb5_str);
            analysisSurveyDialog.dismiss();
            startMainActivity(requireContext());
        }
    };

    private void startMainActivity(Context context){
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
    }

    private void submitReport(){
        Context context = requireContext();
        new Thread(() -> requireActivity().runOnUiThread(() -> loadingLayout.setVisibility(View.VISIBLE))).start();

        Tools.updatePoint(context);

        Log.d(TAG,"order : "+order);
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


            Calendar saveCal = Calendar.getInstance();
            int hour = saveCal.get(Calendar.HOUR_OF_DAY);
            if(hour >= 11 && hour < 15){
                saveCal.set(Calendar.HOUR_OF_DAY, 11);
                saveCal.set(Calendar.MINUTE, 10);
                saveCal.set(Calendar.SECOND, 0);
                saveCal.set(Calendar.MILLISECOND, 0);
            }
            else if(hour >= 15 && hour < 19){
                saveCal.set(Calendar.HOUR_OF_DAY, 15);
                saveCal.set(Calendar.MINUTE, 10);
                saveCal.set(Calendar.SECOND, 0);
                saveCal.set(Calendar.MILLISECOND, 0);
            }
            else if(hour >= 19 && hour < 23){
                saveCal.set(Calendar.HOUR_OF_DAY, 19);
                saveCal.set(Calendar.MINUTE, 10);
                saveCal.set(Calendar.SECOND, 0);
                saveCal.set(Calendar.MILLISECOND, 0);
            }
            else{
                if(hour != 23){
                    saveCal.add(Calendar.DATE, -1);
                }
                saveCal.set(Calendar.HOUR_OF_DAY, 23);
                saveCal.set(Calendar.MINUTE, 10);
                saveCal.set(Calendar.SECOND, 0);
                saveCal.set(Calendar.MILLISECOND, 0);
            }
            long timestamp = saveCal.getTimeInMillis();

            reportSubmitEditor.putBoolean(reportSubmit, true);
            reportSubmitEditor.putInt("reportSubmitDate", saveCal.get(Calendar.DATE));
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

            if(analysisResult == INCORRECT_ANALYSIS_RESULT){
                if(checkSurveyCount()){
                    surveyDialog();
                }else{
                    startMainActivity(context);
                }
            }
            else {
                startMainActivity(context);
            }


            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(STRESS_REPORT_NOTIFI_ID);
            }

            Tools.saveApplicationLog(context, TAG, Tools.ACTION_CLICK_COMPLETE_BUTTON, analysisResult);


        }
        else {
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

            Calendar saveCal = Calendar.getInstance();
            int hour = saveCal.get(Calendar.HOUR_OF_DAY);
            if(hour >= 11 && hour < 15){
                saveCal.set(Calendar.HOUR_OF_DAY, 11);
                saveCal.set(Calendar.MINUTE, 10);
                saveCal.set(Calendar.SECOND, 0);
                saveCal.set(Calendar.MILLISECOND, 0);
            }
            else if(hour >= 15 && hour < 19){
                saveCal.set(Calendar.HOUR_OF_DAY, 15);
                saveCal.set(Calendar.MINUTE, 10);
                saveCal.set(Calendar.SECOND, 0);
                saveCal.set(Calendar.MILLISECOND, 0);
            }
            else if(hour >= 19 && hour < 23){
                saveCal.set(Calendar.HOUR_OF_DAY, 19);
                saveCal.set(Calendar.MINUTE, 10);
                saveCal.set(Calendar.SECOND, 0);
                saveCal.set(Calendar.MILLISECOND, 0);
            }
            else{
                if(hour != 23){
                    saveCal.add(Calendar.DATE, -1);
                }
                saveCal.set(Calendar.HOUR_OF_DAY, 23);
                saveCal.set(Calendar.MINUTE, 10);
                saveCal.set(Calendar.SECOND, 0);
                saveCal.set(Calendar.MILLISECOND, 0);
            }
            long timestamp = saveCal.getTimeInMillis();

            reportSubmitEditor.putBoolean(reportSubmit, true);
            reportSubmitEditor.putInt("reportSubmitDate", saveCal.get(Calendar.DATE));
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

            if(analysisResult == INCORRECT_ANALYSIS_RESULT){
                if(checkSurveyCount()){
                    surveyDialog();
                }else{
                    startMainActivity(context);
                }
            }
            else{
                startMainActivity(context);
            }

            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(STRESS_REPORT_NOTIFI_ID);
            }

            Tools.saveApplicationLog(getContext(), TAG, Tools.ACTION_CLICK_COMPLETE_BUTTON, analysisResult);

        }
    }
}