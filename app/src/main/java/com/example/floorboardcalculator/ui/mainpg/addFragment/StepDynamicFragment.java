package com.example.floorboardcalculator.ui.mainpg.addFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;

public class StepDynamicFragment extends WizardDataParser {
    @LayoutRes
    private int selectedResource;

    private int position;

    protected Context context;
    protected Activity activity;
    protected IWizardListener wizardListener;
    protected IWizardDataProcess wizardDataProcess;

    private StepDynamicFragment info;

    public StepDynamicFragment(@LayoutRes int resourceId, int position){
        this.selectedResource = resourceId;
        this.position = position;
    }

    public void setWizardListener(IWizardListener wizardListener) {
        this.wizardListener = wizardListener;
    }

    public void setDataListener(IWizardDataProcess wizardDataProcess) {
        this.wizardDataProcess = wizardDataProcess;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(selectedResource, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View parent, Bundle savedInstanceState) {
        info = null;

        switch(selectedResource) {
            case R.layout.fragment_addpage_01:
                info = new CustomerInfo(selectedResource, position);
                break;

            case R.layout.fragment_addpage_02:
                info = new BuildingInfo(selectedResource, position);
                break;

            case R.layout.fragment_addpage_03:
                info = new FloorAccs(selectedResource, position);
                break;

            case R.layout.fragment_addpage_04:
                info = new FloorArea(selectedResource, position);
                break;

            case R.layout.fragment_addpage_05:
                info = new Confirmation(selectedResource, position);
                break;
        }

        info.activity = getActivity();
        info.context = getContext();
        info.wizardListener = wizardListener;
        info.wizardDataProcess = wizardDataProcess;
        info.onViewCreated(parent, savedInstanceState);
    }

    @Override
    protected void datasetUpdated(Customer customer) {
        wizardDataProcess.onDataSend(customer);
    }

    public StepDynamicFragment getInfo() {
        return info;
    }
}
