package com.example.floorboardcalculator.ui.mainpg;

import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.core.datamodel.FloorInf;
import com.example.floorboardcalculator.core.datamodel.FloorPlan;
import com.example.floorboardcalculator.ui.mainpg.addFragment.*;

import java.util.ArrayList;
import java.util.List;

public class WizardPageAdapter extends FragmentStateAdapter implements IWizardListener, IWizardDataProcess {
    private Context mContext;
    private IWizardListener listener;
    private List<LayoutStateController> dataValidator;
    private Fragment fragment;
    private Customer customerData = new Customer();

    public WizardPageAdapter(FragmentActivity manager, @NonNull Context ctx) {
        super(manager);
        this.mContext = ctx;
        this.dataValidator = new ArrayList<LayoutStateController>();
        customerData.setFloorPlan(new ArrayList<FloorPlan>());
        customerData.setFloorInf(new FloorInf());

        initResult();
    }

    public void setListener(IWizardListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public int getItemCount() {
        return WizardViewModel.values().length;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        StepDynamicFragment frag = new StepDynamicFragment(WizardViewModel.values()[position].getLayoutResId(), position);
        frag.setWizardListener(this);
        frag.setDataListener(this);

        if(WizardViewModel.values()[position].getLayoutResId() == R.layout.fragment_addpage_05) {
            this.fragment = frag;
        }

        return frag;
    }

    @Override
    public void onStateUpdated(@LayoutRes int layoutId, boolean isValid) {
        if(listener != null) listener.onStateUpdated(layoutId, isValid);

        if(layoutId != R.layout.fragment_addpage_05){
            for(LayoutStateController ctrl : dataValidator) {
                if(ctrl.layoutId == layoutId) {
                    ctrl.valid = isValid;
                    break;
                }
            }
        }
        else
            verifyData();
    }

    @Override
    public void onDataSend(Customer customer) {

    }

    public Customer onDataGet() {
        return customerData;
    }

    public void verifyData() {
        boolean dataIsTrue = true;

        for(LayoutStateController ctrl : dataValidator) {
            if(!ctrl.valid) {
                dataIsTrue = false;
                break;
            }
        }

        StepDynamicFragment child = (StepDynamicFragment) fragment;
        if(child.getInfo() instanceof Confirmation) {
            ((Confirmation) child.getInfo()).notifyDataUpdated(dataIsTrue);
        }
    }

    private void initResult() {
        for(WizardViewModel model : WizardViewModel.values()) {
            if(model.getLayoutResId() != R.layout.fragment_addpage_05){
                LayoutStateController controller = new LayoutStateController();
                controller.layoutId = model.getLayoutResId();
                controller.valid = false;
                dataValidator.add(controller);
            }
        }
    }

    private class LayoutStateController extends ArrayList<LayoutStateController> {
        public @LayoutRes int layoutId;
        public boolean valid = false;
    }
}
