package com.example.floorboardcalculator.ui.mainpg;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

import com.example.floorboardcalculator.R;

public enum WizardViewModel {

    STEP1(R.string.step_01, R.layout.fragment_addpage_01, R.string.step_title_01),
    STEP2(R.string.step_02, R.layout.fragment_addpage_02, R.string.step_title_02),
    STEP3(R.string.step_03, R.layout.fragment_addpage_03, R.string.step_title_03),
    STEP4(R.string.step_04, R.layout.fragment_addpage_04, R.string.step_title_04),
    STEP5(R.string.step_05, R.layout.fragment_addpage_06, R.string.step_title_05),
    STEP6(R.string.step_06, R.layout.fragment_addpage_05, R.string.step_title_06);

    @LayoutRes
    private int layoutResId;
    @StringRes
    private int titleResId;
    @StringRes
    private int descResId;

    WizardViewModel(@StringRes int titleResId, @LayoutRes int layoutResId, @StringRes int descResId) {
        this.layoutResId = layoutResId;
        this.titleResId = titleResId;
        this.descResId = descResId;
    }

    @LayoutRes
    public int getLayoutResId() {
        return layoutResId;
    }

    @StringRes
    public int getTitleResId() {
        return titleResId;
    }

    @StringRes
    public int getDescResId() { return descResId; }
}
