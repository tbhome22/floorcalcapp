package com.example.floorboardcalculator.ui.pagerdetails.fragments;

import com.example.floorboardcalculator.core.datamodel.Config;
import com.example.floorboardcalculator.core.datamodel.FloorType;

import java.util.List;

public interface ConfigCallable {
    void onConfigReached(Config config);

    void onProductReached(List<FloorType> products);

    void onFailedReach();
}
