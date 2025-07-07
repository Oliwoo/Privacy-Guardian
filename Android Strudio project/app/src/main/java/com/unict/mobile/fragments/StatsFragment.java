package com.unict.mobile.fragments;

import static android.view.View.GONE;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.unict.mobile.R;
import com.unict.mobile.adapters.HistoryObjectAdapter;
import com.unict.mobile.adapters.models.ItemHistoryObject;
import com.unict.mobile.components.BarChart;
import com.unict.mobile.components.BaseRecyclerViewContainer;
import com.unict.mobile.models.AudioTypeLog;
import com.unict.mobile.models.MicrophoneDayAccessLogs;
import com.unict.mobile.utils.AsyncDataLoadHelper;
import com.unict.mobile.utils.DateTimeUtils;
import com.unict.mobile.utils.decorators.VerticalItemDecorator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class StatsFragment extends BaseFragment {

    private enum INFO_ID{DATA,AVG,APPS_DATA, DATE_START, DATE_END, REC_TYPE_DATA}
    private SwipeRefreshLayout refresh;
    private BarChart<MicrophoneDayAccessLogs> barChart;

    private HistoryObjectAdapter appAccessLogAdapter, recordedAudioTypeLogAdapter;
    private TextView stats_avg_anomalies;

    private Date dateStart, dateEnd;

    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        fragment.setTitle("Latest week stats");
        fragment.setGoBackTarget(HomeFragment.newInstance());
        return fragment;
    }

    public static StatsFragment newInstance(Date dateStart, Date dateEnd) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putString(INFO_ID.DATE_START.toString(), DateTimeUtils.parseDate(dateStart));
        args.putString(INFO_ID.DATE_END.toString(), DateTimeUtils.parseDate(dateEnd));
        fragment.setArguments(args);
        fragment.setGoBackTarget(HomeFragment.newInstance());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            try {
                dateStart = DateTimeUtils.parseDate(getArguments().getString(INFO_ID.DATE_START.toString(), null));
                dateEnd = DateTimeUtils.parseDate(getArguments().getString(INFO_ID.DATE_END.toString(), null));
            } catch (Exception e) {
                dateStart = dateEnd = null;
            }
        }else{
            DateTimeUtils.WeekRange dates = DateTimeUtils.getCurrentWeekDates();
            dateStart = dates.getStart();
            dateEnd = dates.getEnd();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_stats, container, false);

        setTitle(DateTimeUtils.getDayAndMonthLabel(dateStart)+" - "+DateTimeUtils.getDayAndMonthLabel(dateEnd));

        barChart = v.findViewById(R.id.fragment_stats_chart);

        refresh = v.findViewById(R.id.fragment_stats_refresh);
        refresh.setOnRefreshListener(this::refreshLogData);

        stats_avg_anomalies = v.findViewById(R.id.fragment_stats_total_week_anomalies);
        BaseRecyclerViewContainer app_stats = v.findViewById(R.id.fragment_stats_app_stats);
        appAccessLogAdapter = new HistoryObjectAdapter(app_stats);
        app_stats.addItemDecoration(new VerticalItemDecorator(8));
        app_stats.setAdapter(appAccessLogAdapter);
        BaseRecyclerViewContainer rec_type_stats = v.findViewById(R.id.fragment_stats_rec_type_stats);
        recordedAudioTypeLogAdapter = new HistoryObjectAdapter(rec_type_stats);
        rec_type_stats.addItemDecoration(new VerticalItemDecorator(8));
        rec_type_stats.setAdapter(recordedAudioTypeLogAdapter);

        ImageView weekBack = v.findViewById(R.id.fragment_stats_week_back);
        ImageView weekAfter = v.findViewById(R.id.fragment_stats_week_after);

        ImageView selectDate = v.findViewById(R.id.fragment_stats_week_select_btn);
        selectDate.setOnClickListener(l -> showWeekPicker());

        try{
            DateTimeUtils.WeekRange previousWeekRange = DateTimeUtils.getPreviousWeekRange(new DateTimeUtils.WeekRange(dateStart,dateEnd));
            weekBack.setOnClickListener(l -> requestFragmentChange(StatsFragment.newInstance(previousWeekRange.getStart(),previousWeekRange.getEnd())));
        }catch(Exception e){
            weekBack.setVisibility(GONE);
        }

        try{
            DateTimeUtils.WeekRange nextWeekRange = DateTimeUtils.getNextWeekRange(new DateTimeUtils.WeekRange(dateStart,dateEnd));
            weekAfter.setOnClickListener(l -> requestFragmentChange(StatsFragment.newInstance(nextWeekRange.getStart(), nextWeekRange.getEnd())));
        } catch (Exception e){
            weekAfter.setVisibility(GONE);
        }

        loadData();
        return v;
    }


    public void showWeekPicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(new ContextThemeWrapper(getContext(), R.style.DarkDatePickerDialog), (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                selected.setFirstDayOfWeek(Calendar.MONDAY);

                Calendar weekStart = (Calendar) selected.clone();
                int dayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
                int diff = (dayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dayOfWeek;
                weekStart.add(Calendar.DAY_OF_MONTH, diff);

                Calendar weekEnd = (Calendar) weekStart.clone();
                weekEnd.add(Calendar.DATE, 6);

                requestFragmentChange(StatsFragment.newInstance(weekStart.getTime(), weekEnd.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void refreshLogData(){
        loadData();
    }
    private void loadData(){
        AsyncDataLoadHelper.execute(this, refresh, new AsyncDataLoadHelper.Callback<Map<INFO_ID,Object>>() {
            @Override
            public Map<INFO_ID, Object> getDataRoutine(){
                final List<ItemHistoryObject> appsData = new ArrayList<>();
                final List<ItemHistoryObject> recordingTypeData = new ArrayList<>();

                AtomicReference<Double> sum = new AtomicReference<>(0.0);
                List<MicrophoneDayAccessLogs> data = new ArrayList<>();
                List<Date> dateWeek;
                dateWeek = DateTimeUtils.getDatesBetween(dateStart,dateEnd);
                dateStart = dateWeek.get(0); dateEnd = dateWeek.get(dateWeek.size()-1);

                dateWeek.forEach(d -> {
                    MicrophoneDayAccessLogs logs = new MicrophoneDayAccessLogs(d, getContext());

                    logs.getAppsStats(getContext()).forEach(a -> appsData.add(new ItemHistoryObject(
                        a.getIcon(),
                        a.getAppName(),
                        List.of(
                            resourcesUtils.getFormattedString(R.string.fragment_stats_apps_package_formatter,a.getAppPackage()!=null?a.getAppPackage():"(Unknown)"),
                            resourcesUtils.getFormattedString(R.string.fragment_stats_apps_date_formatter,DateTimeUtils.formatDate(d)),
                            resourcesUtils.getFormattedString(R.string.fragment_stats_apps_duration_formatter,DateTimeUtils.getFormattedTimeMillisStr(a.getDuration()))
                        )
                    )));

                    logs.getRecTypeStats(getContext()).forEach(r -> recordingTypeData.add(new ItemHistoryObject(
                        r.getIcon(),
                        r.getType(),
                        List.of(
                            resourcesUtils.getFormattedString(R.string.fragment_stats_rec_type_risk_level_formatter,AudioTypeLog.getRiskLevel(r.getType()).toString()),
                            resourcesUtils.getFormattedString(R.string.fragment_stats_rec_type_date_formatter,DateTimeUtils.formatDate(d)),
                            resourcesUtils.getFormattedString(R.string.fragment_stats_rec_type_duration_formatter,DateTimeUtils.getFormattedTimeMillisStr(r.getDuration()))
                        )
                    )));

                    sum.updateAndGet(v -> v + logs.getAnomaliesCount());
                    data.add(logs);
                });

                double avg = sum.get() / data.size();
                double avgRounded = Math.round(avg * 10.0) / 10.0;

                return Map.of(
                    INFO_ID.DATA, data,
                    INFO_ID.AVG, avgRounded,
                    INFO_ID.APPS_DATA, appsData,
                    INFO_ID.REC_TYPE_DATA, recordingTypeData
                );

            }

            @Override
            public void onResult(Map<INFO_ID, Object> result){
                List<MicrophoneDayAccessLogs> logs = (List<MicrophoneDayAccessLogs>) result.get(INFO_ID.DATA);
                barChart.setDataFromList(logs!=null?logs:new ArrayList<>(), new BarChart.ChartMapper<>() {
                    @Override
                    public float getValue(MicrophoneDayAccessLogs item) {
                        return (float) item.getAnomaliesCount();
                    }

                    @Override
                    public int getColor(MicrophoneDayAccessLogs item) {
                        @ColorRes int color;
                        switch(item.getRiskLevel()){
                            case LOW: color = R.color.green; break;
                            case MEDIUM: color = R.color.yellow; break;
                            case HIGH: color = R.color.red; break;
                            default: color = R.color.white; break;
                        }
                        return resourcesUtils.getColor(color);
                    }

                    @Override
                    public String getLabel(MicrophoneDayAccessLogs item) {
                        return ""+DateTimeUtils.getWeekdayDay(item.getDate()).charAt(0);
                    }
                });
                stats_avg_anomalies.setText(String.valueOf(result.getOrDefault(INFO_ID.AVG,0)));
                appAccessLogAdapter.setData((List<ItemHistoryObject>) result.getOrDefault(INFO_ID.APPS_DATA,new ArrayList<>()));
                recordedAudioTypeLogAdapter.setData((List<ItemHistoryObject>) result.getOrDefault(INFO_ID.REC_TYPE_DATA,new ArrayList<>()));
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        loadData();
    }
}