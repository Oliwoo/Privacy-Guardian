package com.unict.mobile.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.unict.mobile.R;
import com.unict.mobile.fragments.BaseFragment;
import com.unict.mobile.fragments.DataStoredFragment;
import com.unict.mobile.fragments.DateStoredAccessLogsFragment;
import com.unict.mobile.fragments.HomeFragment;
import com.unict.mobile.fragments.MicAccessDateLogsFragment;
import com.unict.mobile.fragments.StatsFragment;
import com.unict.mobile.models.AudioTypeLog;
import com.unict.mobile.utils.MicrophoneAccessLogManager;
import com.unict.mobile.utils.ResourcesUtils;

import java.util.Date;

public class BottomBar extends LinearLayout{
    public interface OnBtnClickListener{
        void OnBtnClicked(BaseFragment fragment);
    }
    private final ImageView home;
    private OnBtnClickListener listener;

    public BottomBar(Context context) {
        this(context, null, 0, 0);
    }

    public BottomBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public BottomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BottomBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.component_bottom_bar, this);

        ImageView todayLog = findViewById(R.id.component_bottom_bar_today_logs);
        todayLog.setOnClickListener(l->listener.OnBtnClicked(MicAccessDateLogsFragment.newInstance(new Date())));
        ImageView stats = findViewById(R.id.component_bottom_bar_stats);
        stats.setOnClickListener(l->listener.OnBtnClicked(StatsFragment.newInstance()));
        home = findViewById(R.id.component_bottom_bar_home);
        home.setOnClickListener(l->listener.OnBtnClicked(HomeFragment.newInstance()));
        ImageView allLogs = findViewById(R.id.component_bottom_bar_all_logs);
        allLogs.setOnClickListener(l->listener.OnBtnClicked(DateStoredAccessLogsFragment.newInstance()));
        ImageView storage = findViewById(R.id.component_bottom_bar_storage);
        storage.setOnClickListener(l->listener.OnBtnClicked(DataStoredFragment.newInstance()));
    }

    public void setListener(OnBtnClickListener listener){
        this.listener = listener;
    }

    public void updateHomeIcon(){
        MicrophoneAccessLogManager accessLogManager = MicrophoneAccessLogManager.getInstance(getContext());
        ResourcesUtils resourcesUtils = new ResourcesUtils(getContext());
        AudioTypeLog.RiskLevel riskLevel = accessLogManager.getRiskLevelForToday();
        switch(riskLevel){
            case LOW: this.home.setImageDrawable(resourcesUtils.getIcon(R.drawable.home)); break;
            case MEDIUM: this.home.setImageDrawable(resourcesUtils.getIcon(R.drawable.home_neutral)); break;
            case HIGH: this.home.setImageDrawable(resourcesUtils.getIcon(R.drawable.home_sad)); break;
        }
    }
}
