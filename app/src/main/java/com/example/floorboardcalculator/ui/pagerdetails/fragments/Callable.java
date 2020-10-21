package com.example.floorboardcalculator.ui.pagerdetails.fragments;

public interface Callable {
    void processDone();

    void processFailed();

    void processInRefresh();
}
