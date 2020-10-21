package com.example.floorboardcalculator.ui.mainpg.addFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.LayoutRes;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;

import org.jetbrains.annotations.NotNull;

public class FloorAccs extends StepDynamicFragment {
    private EditText txtSkirtLen;
    private Spinner listSkirtLenSelect;
    private RadioGroup rg_hasCurvedArea;

    private boolean hasCurvedArea = false;

    private @LayoutRes int resourceId;

    public FloorAccs(int resourceId, int position) {
        super(resourceId, position);

        this.resourceId = resourceId;
    }

    @Override
    public void onViewCreated(@NotNull View v, Bundle savedInstanceState) {
        txtSkirtLen = (EditText) v.findViewById(R.id.add_skirtLen);
        listSkirtLenSelect = (Spinner) v.findViewById(R.id.add_skirtLenSelect);
        rg_hasCurvedArea = (RadioGroup) v.findViewById(R.id.add_hasCurvedArea);

        if(wizardDataProcess != null)
            customer = wizardDataProcess.onDataGet();
        else
            customer = new Customer();

        initSetting();

        rg_hasCurvedArea.setOnCheckedChangeListener(this::CurvedAreaCheckedChange);
        txtSkirtLen.addTextChangedListener(new TextInput());
        listSkirtLenSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                isValid();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                isValid();
            }
        });
    }

    private void CurvedAreaCheckedChange(RadioGroup radioGroup, int checkedId) {
        switch(checkedId) {
            case R.id.add_hasCurvedArea_Yes:
                hasCurvedArea = true;
                break;

            case R.id.add_hasCurvedArea_No:
                hasCurvedArea = false;
                break;
        }

        isValid();
    }

    private void initSetting() {
        ArrayAdapter<CharSequence> adptSkirtLenSi;

        adptSkirtLenSi = ArrayAdapter.createFromResource(context, R.array.input_select_01, R.layout.support_simple_spinner_dropdown_item);

        adptSkirtLenSi.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        listSkirtLenSelect.setAdapter(adptSkirtLenSi);
    }

    private void isValid() {
        String skirtlen = txtSkirtLen.getText().toString();

        if(skirtlen.length() >= 1) {
            if(Integer.parseInt(skirtlen) > 0)
                wizardListener.onStateUpdated(resourceId, true);
            else
                wizardListener.onStateUpdated(resourceId, false);
        }
        else {
            if(wizardListener != null) wizardListener.onStateUpdated(resourceId, false);
        }

        customer.getFloorInf().setCurvedArea(hasCurvedArea);

        switch(listSkirtLenSelect.getSelectedItemPosition()) {
            case 0:
                customer.getFloorInf().setSkirtingLen((skirtlen.length() >= 1)? (Float.parseFloat(skirtlen) * 30.48f) : 0f);
                break;

            case 1:
                customer.getFloorInf().setSkirtingLen((skirtlen.length() >= 1)? Float.parseFloat(skirtlen) : 0f);
                break;
        }

        datasetUpdated(customer);
    }

    private class TextInput implements TextWatcher {
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
}
