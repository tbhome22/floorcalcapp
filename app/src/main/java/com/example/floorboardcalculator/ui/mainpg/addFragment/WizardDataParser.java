package com.example.floorboardcalculator.ui.mainpg.addFragment;

import androidx.fragment.app.Fragment;

import com.example.floorboardcalculator.core.datamodel.Customer;

public abstract class WizardDataParser extends Fragment {
    protected Customer customer;

    protected abstract void datasetUpdated(Customer customer);
}
