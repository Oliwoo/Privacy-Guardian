package com.unict.mobile.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.unict.mobile.R;
import com.unict.mobile.utils.ResourcesUtils;

public class ScanResult extends LinearLayout {
    private final ImageView icon;
    private final TextView scanType, result, desc, lastReport;
    private final Button btn;
    private final ResourcesUtils resourcesUtils;

    public ScanResult(Context context) {
        this(context, null, 0,0);
    }

    public ScanResult(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ScanResult(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public ScanResult(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        resourcesUtils = new ResourcesUtils(context);

        LayoutInflater.from(context).inflate(R.layout.component_scan_result,this);
        icon = findViewById(R.id.component_scan_result_icon);
        scanType = findViewById(R.id.component_scan_result_type);
        result = findViewById(R.id.component_scan_result_text);
        desc = findViewById(R.id.component_scan_result_desc);
        lastReport = findViewById(R.id.component_scan_result_last_report);
        btn = findViewById(R.id.component_scan_result_btn);
    }

    public void init(@DrawableRes int icon, String scanType){
        setIcon(icon);
        setScanType(scanType);
    }
    public void init(@DrawableRes int icon, @StringRes int scanType){
        setIcon(icon);
        setScanType(scanType);
    }

    public void setIcon(@DrawableRes int icon){
        this.icon.setImageDrawable(AppCompatResources.getDrawable(getContext(),icon));
    }
    public void setScanType(String type){
        this.scanType.setText(type);
    }
    public void setScanType(@StringRes int type){
        this.scanType.setText(type);
    }
    public void setResult(String result, @ColorRes int color){
        this.result.setText(result);
        this.result.setTextColor(AppCompatResources.getColorStateList(getContext(),color));
    }
    public void setResult(@StringRes int result, @ColorRes int color){
        this.result.setText(result);
        this.result.setTextColor(AppCompatResources.getColorStateList(getContext(),color));
    }
    public void setDesc(String desc){
        this.desc.setText(desc);
    }
    public void setDesc(@StringRes int desc){
        this.desc.setText(desc);
    }
    public void setLastReport(String lastReport){
        this.lastReport.setText(resourcesUtils.getFormattedString(
            R.string.last_report_formatter,lastReport
        ));
    }

    public void showBtn(boolean show){
        this.btn.setVisibility(show?VISIBLE:GONE);
    }

    public void setBtnText(String txt, @ColorRes int txtColor, @ColorRes int bgColor){
        this.btn.setText(txt);
        this.btn.setBackgroundColor(getContext().getColor(bgColor));
        this.btn.setTextColor(AppCompatResources.getColorStateList(getContext(),txtColor));
        this.showBtn(true);
    }
    public void setBtnText(@StringRes int txt, @ColorRes int txtColor, @ColorRes int bgColor){
        this.btn.setText(txt);
        this.btn.setBackgroundColor(getContext().getColor(bgColor));
        this.btn.setTextColor(AppCompatResources.getColorStateList(getContext(),txtColor));
        this.showBtn(true);
    }
    public void setBtnListener(OnClickListener l){
        this.btn.setOnClickListener(l);
    }

    public enum SCAN_RESULT{BAD,MEDIUM,GOOD, LOW, HIGH, LOADING}
    public void setResult(SCAN_RESULT result){
        switch(result){
            case BAD:
                setResult(resourcesUtils.getString(R.string.scan_result_bad),R.color.red);
                break;
            case MEDIUM:
                setResult(resourcesUtils.getString(R.string.scan_result_medium),R.color.yellow);
                break;
            case GOOD:
                setResult(resourcesUtils.getString(R.string.scan_result_good),R.color.green);
                break;
            case LOW:
                setResult(resourcesUtils.getString(R.string.scan_result_low),R.color.green);
                break;
            case HIGH:
                setResult(resourcesUtils.getString(R.string.scan_result_high),R.color.red);
                break;
            case LOADING:
                setResult(resourcesUtils.getString(R.string.scan_result_loading), R.color.white);
                break;
            default:
                setResult(resourcesUtils.getString(R.string.scan_result_undefined),R.color.white);
        }
    }
}
