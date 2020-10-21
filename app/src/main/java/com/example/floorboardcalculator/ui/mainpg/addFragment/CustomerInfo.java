package com.example.floorboardcalculator.ui.mainpg.addFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.google.android.material.textfield.TextInputLayout;

public class CustomerInfo extends StepDynamicFragment {
    private EditText txtCustName, txtReferralName, txtContactNo;
    private RadioGroup grp_hasReferral;
    private @LayoutRes int resourceId;

    private boolean haveReferral = false;

    public CustomerInfo(int resourceId, int position) {
        super(resourceId, position);

        this.resourceId = resourceId;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        txtCustName = (EditText) v.findViewById(R.id.add_customerName);
        txtReferralName = (EditText) v.findViewById(R.id.add_referralName);
        grp_hasReferral = (RadioGroup) v.findViewById(R.id.add_hasReferral);
        txtContactNo = (EditText) v.findViewById(R.id.add_contactNo);

        if(wizardDataProcess != null)
            customer = wizardDataProcess.onDataGet();
        else
            customer = new Customer();

        initSetting();

        grp_hasReferral.setOnCheckedChangeListener(this::ReferralCheckedChanged);
        txtCustName.addTextChangedListener(new TextListener(TextBox.CUSTOMER_NAME));
        txtContactNo.addTextChangedListener(new TextListener(TextBox.CONTACT_NO));
        txtReferralName.addTextChangedListener(new TextListener(TextBox.REFERRAL));
    }

    private void initSetting() {
        ViewGroup group = (ViewGroup) txtReferralName.getParent();
        TextInputLayout layout = (TextInputLayout) group.getParent();

        layout.setVisibility(View.GONE);
    }

    private void ReferralCheckedChanged(RadioGroup radioGroup, int checkedId) {
        ViewGroup vg1 = (ViewGroup) txtReferralName.getParent();
        TextInputLayout layoutReferral = (TextInputLayout) vg1.getParent();

        switch(checkedId) {
            case R.id.add_hasReferral_Yes:
                layoutReferral.setVisibility(View.VISIBLE);
                txtReferralName.setText("");
                haveReferral = true;
                break;

            case R.id.add_hasReferral_No:
                layoutReferral.setVisibility(View.GONE);
                haveReferral = false;
                break;
        }

        isValid();
    }

    private void isValid() {
        String custName = txtCustName.getText().toString();
        String contactNo = txtContactNo.getText().toString();
        String referral = txtReferralName.getText().toString();

        if(custName.length() >= 5 && (contactNo.length() >= 10 && contactNo.length() <= 11)) {
            if(haveReferral) {
                if(referral.length() >= 5)
                    wizardListener.onStateUpdated(resourceId, true);
                else
                    wizardListener.onStateUpdated(resourceId, false);
            }
            else
                if(wizardListener != null) wizardListener.onStateUpdated(resourceId, true);
        }
        else {
            if(wizardListener != null) wizardListener.onStateUpdated(resourceId, false);
        }

        customer.setCustName(txtCustName.getText().toString());
        customer.setContactNo(txtContactNo.getText().toString());
        customer.setReferral((haveReferral)? txtReferralName.getText().toString() : "-");
        datasetUpdated(customer);
    }

    private class TextListener implements TextWatcher {
        private TextBox mode;

        public TextListener(@NonNull TextBox mode) {
            this.mode = mode;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            isValid();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private enum TextBox {
        CUSTOMER_NAME,
        CONTACT_NO,
        REFERRAL
    }
}
