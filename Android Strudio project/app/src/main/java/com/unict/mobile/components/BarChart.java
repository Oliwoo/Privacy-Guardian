package com.unict.mobile.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.unict.mobile.R;
import com.unict.mobile.utils.ResourcesUtils;

import java.util.ArrayList;
import java.util.List;

public class BarChart<T> extends com.github.mikephil.charting.charts.BarChart {

    public interface ChartMapper<T> {
        float getValue(T item);
        int getColor(T item);
        String getLabel(T item);
    }

    private static final float BAR_RADIUS_DP = 16f;
    private static final float BAR_WIDTH = 0.9f;

    public BarChart(Context context) {
        this(context, null, 0);
    }

    public BarChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void init(){
        super.init();
        getDescription().setEnabled(false);

        getXAxis().setGranularity(1f);
        getXAxis().setAxisMinimum(-0.5f);
        getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        getXAxis().setTextColor(Color.WHITE);
        getXAxis().setDrawGridLines(false);
        getXAxis().setDrawAxisLine(false);
        getXAxis().setTextColor(Color.WHITE);

        getAxisLeft().setEnabled(false);
        getAxisLeft().setDrawAxisLine(false);
        getAxisLeft().setDrawGridLines(false);

        getAxisRight().setEnabled(true);
        getAxisRight().setDrawGridLines(false);
        getAxisRight().setDrawAxisLine(false);
        getAxisRight().setTextColor(Color.GRAY);
        getAxisRight().setXOffset(16f);

        setTouchEnabled(false);

        getLegend().setEnabled(false);

        setExtraLeftOffset(0f);
        setFitBars(true);

        float radiusPx = dpToPx();
        setRenderer(new RoundedBarChartRenderer(this, getAnimator(), getViewPortHandler(), radiusPx));
    }

    public void setDataFromList(List<T> items, ChartMapper<T> mapper) {
        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        getXAxis().setAxisMaximum(items.size());

        for (int i = 0; i < items.size(); i++) {
            T item = items.get(i);
            entries.add(new BarEntry(i, mapper.getValue(item)));
            colors.add(mapper.getColor(item));
            labels.add(mapper.getLabel(item));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(colors);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(BAR_WIDTH);
        setData(barData);

        getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        float sum = 0f;
        for (BarEntry entry : entries) {
            sum += entry.getY();
        }
        float average = entries.isEmpty() ? 0 : sum / entries.size();

        getAxisRight().setDrawLimitLinesBehindData(true);
        LimitLine avgLine = new LimitLine(average);
        avgLine.setLineColor(ResourcesUtils.getColor(getContext(), R.color.white_25));
        avgLine.setLineWidth(2f);
        avgLine.setTextColor(Color.WHITE);
        avgLine.setTextSize(12f);

        getAxisRight().removeAllLimitLines();
        getAxisRight().addLimitLine(avgLine);

        invalidate();
    }


    private float dpToPx() {
        return BarChart.BAR_RADIUS_DP * Resources.getSystem().getDisplayMetrics().density;
    }

    private static class RoundedBarChartRenderer extends BarChartRenderer {
        private final float radius;

        public RoundedBarChartRenderer(BarChart chart, ChartAnimator animator, ViewPortHandler viewPortHandler, float radius) {
            super(chart, animator, viewPortHandler);
            this.radius = radius;
        }

        @Override
        public void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

            mRenderPaint.setStyle(Paint.Style.FILL);
            mRenderPaint.setAntiAlias(true);

            float barWidthHalf = mChart.getBarData().getBarWidth() / 2f;

            for (int i = 0; i < dataSet.getEntryCount(); i++) {
                BarEntry entry = dataSet.getEntryForIndex(i);

                float x = entry.getX();
                float y = entry.getY();

                float left = x - barWidthHalf;
                float right = x + barWidthHalf;
                float top = y >= 0 ? y : 0;
                float bottom = y <= 0 ? y : 0;

                float[] pts = new float[] { left, top, right, bottom };
                trans.pointValuesToPixel(pts);

                RectF rect = new RectF(pts[0], pts[1], pts[2], pts[3]);
                if(rect.top > rect.bottom){
                    float temp = rect.top;
                    rect.top = rect.bottom;
                    rect.bottom = temp;
                }

                mRenderPaint.setColor(dataSet.getColor(i));
                mRenderPaint.setAlpha(255);

                c.drawRoundRect(rect, radius, radius, mRenderPaint);
            }
        }
    }
}
