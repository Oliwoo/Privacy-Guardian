package com.unict.mobile.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.StringRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.unict.mobile.R;
import com.unict.mobile.components.StatView;
import com.unict.mobile.utils.AsyncDataLoadHelper;
import com.unict.mobile.utils.MicrophoneAccessLogManager;
import com.unict.mobile.utils.ResourcesUtils;

import java.util.Map;

public class DataStoredFragment extends BaseFragment{
    private enum INFO_IDS{STORAGE_SIZE,TOTAL_LOGS}

    private SwipeRefreshLayout refresh;
    private StatView data1, data2;
    private View v;
    private Dialog deleteAllTodayLogsDialog, deleteAllLogsDialog;
    private Button deleteAllTodayLogsBtn;
    private ResourcesUtils resourcesUtils;
    private MicrophoneAccessLogManager microphoneAccessLogManager;
    public static DataStoredFragment newInstance(){
        DataStoredFragment fragment = new DataStoredFragment();
        fragment.setGoBackTarget(HomeFragment.newInstance());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_data_stored, container, false);
        resourcesUtils = new ResourcesUtils(requireContext());
        setTitle(resourcesUtils.getString(R.string.data_stored_title));

        microphoneAccessLogManager = MicrophoneAccessLogManager.getInstance(getContext());
        refresh = v.findViewById(R.id.fragment_data_stored_refresh);
        refresh.setOnRefreshListener(this::refreshData);

        data1 = v.findViewById(R.id.fragment_data_stored_data_1);
        data1.init(R.drawable.data_stored,R.string.loading,R.string.data_stored_size_desc);
        data2 = v.findViewById(R.id.fragment_data_stored_data_2);
        data2.init(R.drawable.microphone,R.string.loading,R.string.data_stored_size_desc);

        deleteAllTodayLogsBtn = v.findViewById(R.id.fragment_data_stored_delete_today_btn);
        deleteAllTodayLogsBtn.setOnClickListener(v -> {if(deleteAllTodayLogsBtn != null) deleteAllTodayLogsDialog.show();});

        Button deleteAllLogsBtn = v.findViewById(R.id.fragment_data_stored_delete_all_btn);
        deleteAllLogsBtn.setOnClickListener(v -> {if(deleteAllLogsDialog != null) deleteAllLogsDialog.show();});

        deleteAllTodayLogsDialog = generateDialog(R.string.data_stored_delete_all_day_log_dialog_title,R.string.data_stored_delete_all_day_log_dialog_title, (d,i) -> {
            microphoneAccessLogManager.clearLogsForToday(); refreshData();
        });
        deleteAllLogsDialog = generateDialog(R.string.data_stored_delete_all_log_dialog_title,R.string.data_stored_delete_all_log_dialog_msg, (d,i) -> {
            microphoneAccessLogManager.clearAllLogs(); refreshData();
        });

        loadData();
        return v;
    }

    private void refreshData(){
        data1.setValue(R.string.loading);
        data2.setValue(R.string.loading);
        v.setBackground(resourcesUtils.getIcon(R.drawable.bg));

        loadData();
    }
    private void loadData(){
        AsyncDataLoadHelper.execute(this, refresh, new AsyncDataLoadHelper.Callback<Map<INFO_IDS,String>>(){
            @Override
            public Map<INFO_IDS,String> getDataRoutine(){
                return Map.of(
                        INFO_IDS.STORAGE_SIZE, microphoneAccessLogManager.getStoredDataSizeString(),
                        INFO_IDS.TOTAL_LOGS, microphoneAccessLogManager.getTotalLogs()+" Log's"
                );
            }
            @Override
            public void onResult(Map<INFO_IDS,String> data){
                data1.setValue(data.get(INFO_IDS.STORAGE_SIZE));
                data2.setValue(data.get(INFO_IDS.TOTAL_LOGS));
            }
        });
    }

    private Dialog generateDialog(@StringRes int title, @StringRes int msg, Dialog.OnClickListener l){
        return new AlertDialog.Builder(getContext())
        .setTitle(title)
        .setMessage(msg)
        .setPositiveButton(android.R.string.ok, l)
        .setNegativeButton(android.R.string.cancel, null)
        .setIconAttribute(android.R.attr.alertDialogIcon).create();
    }
}