package com.example.floorboardcalculator.ui.pagerdetails.fragments;

import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.datamodel.Customer;

public interface InformationProcess {
    Customer getData();

    PreferenceItem getSetting();

    void notifyRefresh();
}
