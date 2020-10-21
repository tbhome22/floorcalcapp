package com.example.floorboardcalculator.ui.mainpg.addFragment;

import androidx.annotation.LayoutRes;

public interface IWizardListener {
    void onStateUpdated(@LayoutRes int layoutId, boolean isValid);
}
