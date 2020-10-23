package com.example.floorboardcalculator.ui.mainpg.addFragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;

import org.jetbrains.annotations.NotNull;

public class NotesRecord extends StepDynamicFragment{

    private EditText mNotes;
    private TextView mCharLeft;

    public NotesRecord(int resourceId, int position) {
        super(resourceId, position);
    }

    @Override
    public void onViewCreated(@NotNull View v, Bundle savedInstanceState) {
        mNotes = (EditText) v.findViewById(R.id.add_notes);
        mCharLeft = (TextView) v.findViewById(R.id.add_charsLeft);

        if(wizardDataProcess != null)
            customer = wizardDataProcess.onDataGet();
        else
            customer = new Customer();

        mNotes.addTextChangedListener(new NoteInputResponse());
    }

    private class NoteInputResponse implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int len = mNotes.getText().length();

            mCharLeft.setText((500 - len) + " / 500 characters left");

            if(len == 0)
                customer.setNotes("");
            else
                customer.setNotes(mNotes.getText().toString());

            datasetUpdated(customer);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
