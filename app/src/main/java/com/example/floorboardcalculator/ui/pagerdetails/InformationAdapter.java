package com.example.floorboardcalculator.ui.pagerdetails;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.datamodel.Config;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.core.datamodel.FloorType;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.Callable;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.ConfigCallable;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.CustomerFragment;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.FloorPlanFragment;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.InformationProcess;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.LocationFragment;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.NotesFragment;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.PagerListener;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.PriceFragment;

import java.util.ArrayList;
import java.util.List;

public class InformationAdapter extends FragmentStateAdapter implements InformationProcess {
    private List<Fragment> fragments;
    private List<String> fragmentTitle;
    private List<Callable> listenerCallable;
    private List<ConfigCallable> listenerConfig;
    private final PreferenceItem setting;
    private Customer currentData;
    private Config configData;
    private List<FloorType> products;
    private PagerListener pagerListener;

    public InformationAdapter(@NonNull FragmentActivity fragmentActivity, PreferenceItem setting) {
        super(fragmentActivity);

        this.setting = setting;

        initFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    @Override
    public Customer getData() {
        return currentData;
    }

    @Override
    public PreferenceItem getSetting() {
        return setting;
    }

    @Override
    public void notifyRefresh() {
        if(pagerListener != null) pagerListener.callRefresh();
    }

    public void setCustomerData(Customer currentData) {
        this.currentData = currentData;

        listenerCallable.forEach(c -> {
            if(c != null){
                if(currentData != null)
                    c.processDone();
                else
                    c.processFailed();
            }
        });
    }

    public void setConfigData(Config currentData) {
        this.configData = currentData;

        listenerConfig.forEach(c -> {
            if(c != null){
                if(configData != null)
                    c.onConfigReached(configData);
                else
                    c.onFailedReach();
            }
        });
    }

    public void setProductData(List<FloorType> currentData) {
        this.products = currentData;

        listenerConfig.forEach(c -> {
            if(c != null) {
                if(products != null)
                    c.onProductReached(products);
                else
                    c.onFailedReach();
            }
        });
    }

    public void notifyUpdating() {
        listenerCallable.forEach(c -> {
            if(c != null)
                c.processInRefresh();
        });
    }

    public void setPagerListener(PagerListener pagerListener) {
        this.pagerListener = pagerListener;
    }

    private void setDataListener(Callable callable) {
        listenerCallable.add(callable);
    }

    private void setConfigListener(ConfigCallable callable) {
        listenerConfig.add(callable);
    }

    public String getFragmentTitle(int position){
        return fragmentTitle.get(position);
    }

    private void initFragment() {
        fragments = new ArrayList<Fragment>();
        fragmentTitle = new ArrayList<String>();
        listenerCallable = new ArrayList<Callable>();
        listenerConfig = new ArrayList<ConfigCallable>();

        CustomerFragment customerFrag = new CustomerFragment();
        LocationFragment locationFrag = new LocationFragment();
        FloorPlanFragment planFrag = new FloorPlanFragment();
        NotesFragment notesFrag = new NotesFragment();

        fragments.add(customerFrag);
        fragments.add(locationFrag);
        fragments.add(planFrag);
        fragments.add(notesFrag);

        fragmentTitle.add("Customer Info");
        fragmentTitle.add("Location Info");
        fragmentTitle.add("Floor Plan");
        fragmentTitle.add("Notes");

        customerFrag.setDataProcess(this);
        locationFrag.setDataProcess(this);
        planFrag.setDataProcess(this);
        notesFrag.setDataProcess(this);

        setDataListener(customerFrag);
        setDataListener(locationFrag);
        setDataListener(planFrag);
        setDataListener(notesFrag);

        setConfigListener(planFrag);

        if(setting.isPriceExp()) {
            PriceFragment priceFrag = new PriceFragment();

            fragments.add(priceFrag);
            fragmentTitle.add("Price List");

            priceFrag.setDataProcess(this);
            setDataListener(priceFrag);
            setConfigListener(priceFrag);
        }
    }
}
