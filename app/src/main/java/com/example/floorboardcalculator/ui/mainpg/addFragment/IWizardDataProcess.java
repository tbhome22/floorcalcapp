package com.example.floorboardcalculator.ui.mainpg.addFragment;

import com.example.floorboardcalculator.core.datamodel.Customer;

public interface IWizardDataProcess {
    void onDataSend(Customer data);

    Customer onDataGet();
}
