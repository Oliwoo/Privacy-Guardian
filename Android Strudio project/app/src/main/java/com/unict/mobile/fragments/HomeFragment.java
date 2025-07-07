package com.unict.mobile.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.unict.mobile.R;
import com.unict.mobile.components.HomeCategoryBtn;
import com.unict.mobile.components.HomeCategoryHorizontalBtn;
import com.unict.mobile.components.ScanResult;
import com.unict.mobile.models.AudioTypeLog;
import com.unict.mobile.utils.AsyncDataLoadHelper;
import com.unict.mobile.utils.MicrophoneAccessLogManager;
import com.unict.mobile.utils.ResourcesUtils;

import java.util.Date;
import java.util.Map;

public class HomeFragment extends BaseFragment {
    private enum INFO_ID{RISK_LEVEL,LAST_REPORT}
    private View v;
    private SwipeRefreshLayout refresh;
    private ScanResult result;
    private ResourcesUtils resourcesUtils;
    public static HomeFragment newInstance(){
        HomeFragment fragment = new HomeFragment();
        fragment.setTitle("Home");
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home, container, false);
        resourcesUtils = new ResourcesUtils(requireContext());

        refresh = v.findViewById(R.id.fragment_home_refresh);
        refresh.setOnRefreshListener(this::refreshData);

        result = v.findViewById(R.id.fragment_home_scan_result);
        result.init(R.drawable.shield_warning,R.string.home_scan_result_type);
        result.setResult(ScanResult.SCAN_RESULT.LOADING);
        result.setDesc(R.string.home_scan_result_desc);

        HomeCategoryBtn btn1 = v.findViewById(R.id.fragment_home_category_btn1);
        btn1.init(R.drawable.microphone,R.string.home_btn_today_access_logs_title,R.string.home_btn_today_access_logs_desc, l -> requestFragmentChange(MicAccessDateLogsFragment.newInstance(new Date())));

        HomeCategoryBtn btn2 = v.findViewById(R.id.fragment_home_category_btn2);
        btn2.init(R.drawable.chart_square,R.string.home_btn_stats_title,R.string.home_btn_stats_desc, l -> requestFragmentChange(StatsFragment.newInstance()));

        HomeCategoryBtn btn3 = v.findViewById(R.id.fragment_home_category_btn3);
        btn3.init(R.drawable.calendar,R.string.home_btn_date_stored_logs_title,R.string.home_btn_date_stored_logs_desc,l -> requestFragmentChange(DateStoredAccessLogsFragment.newInstance()));

        HomeCategoryBtn btn4 = v.findViewById(R.id.fragment_home_category_btn4);
        btn4.init(R.drawable.data_stored,R.string.home_btn_stored_data_title,R.string.home_btn_stored_data_desc,l -> requestFragmentChange(DataStoredFragment.newInstance()));

        HomeCategoryHorizontalBtn btn5 = v.findViewById(R.id.fragment_home_category_btn5);
        btn5.init(R.drawable.shield_warning,R.string.home_btn_apps_with_access_title,R.string.home_btn_apps_with_access_desc,l -> requestFragmentChange(AppWithMicrophoneAccessFragment.newInstance()));
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }
    private void refreshData(){
        result.setResult(ScanResult.SCAN_RESULT.LOADING);
        v.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.bg));
        loadData();
    }
    private void loadData(){
        AsyncDataLoadHelper.execute(this, refresh, new AsyncDataLoadHelper.Callback<Map<INFO_ID, Object>>() {
            @Override
            public Map<INFO_ID, Object> getDataRoutine(){
                MicrophoneAccessLogManager microphoneAccessLogManager = MicrophoneAccessLogManager.getInstance(requireContext());
                return Map.of(
                    INFO_ID.RISK_LEVEL, microphoneAccessLogManager.getRiskLevelForToday(),
                    INFO_ID.LAST_REPORT, microphoneAccessLogManager.getLastStoredLogDateStr()
                );
            }

            @Override
            public void onResult(Map<INFO_ID, Object> result) {
                updateRiskLevelUI((String) result.get(INFO_ID.LAST_REPORT),(AudioTypeLog.RiskLevel) result.get(INFO_ID.RISK_LEVEL));
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        loadData();
    }
    private void updateRiskLevelUI(String lastReport, AudioTypeLog.RiskLevel riskLevel){
        result.setLastReport(lastReport);

        if(riskLevel == null) return;
        switch(riskLevel){
            case LOW:
                result.setResult(ScanResult.SCAN_RESULT.LOW);
                v.setBackground(resourcesUtils.getIcon(R.drawable.bg_green));
                break;
            case MEDIUM:
                result.setResult(ScanResult.SCAN_RESULT.MEDIUM);
                v.setBackground(resourcesUtils.getIcon(R.drawable.bg_yellow));
                break;
            case HIGH:
                result.setResult(ScanResult.SCAN_RESULT.HIGH);
                v.setBackground(resourcesUtils.getIcon(R.drawable.bg_red));
                break;
        }
    }
}