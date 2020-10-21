package com.example.floorboardcalculator.ui.pagerdetails.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CustomerFragment extends Fragment implements Callable{
    private TextView txt_CustName, txt_date, txt_recId, txt_referral, txt_contact;
    private InformationProcess dataProcess;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detailpage_01, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txt_CustName = (TextView) view.findViewById(R.id.detail_custName);
        txt_date = (TextView) view.findViewById(R.id.detail_createDate);
        txt_recId = (TextView) view.findViewById(R.id.detail_recId);
        txt_referral = (TextView) view.findViewById(R.id.detail_referral);
        txt_contact = (TextView) view.findViewById(R.id.detail_contactNo);

        processDone();
    }

    public void setDataProcess(InformationProcess dataProcess) {
        this.dataProcess = dataProcess;
    }

    @Override
    public void processDone() {
        Customer data = dataProcess.getData();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.UK);

        if(getView() != null && data != null) {
            txt_CustName.setText(data.getCustName());
            txt_date.setText(format.format(data.getAddDate()));
            txt_recId.setText(data.get_id().toHexString());
            txt_contact.setText(data.getContactNo());
            txt_referral.setText(data.getReferral().equals("-") ? "No Referral" : data.getReferral());
        }
    }

    @Override
    public void processFailed() {

    }

    @Override
    public void processInRefresh() {
        if(txt_contact != null)
            txt_contact.setText(R.string.loading);

        if(txt_CustName != null)
            txt_CustName.setText(R.string.loading);

        if(txt_date != null)
            txt_date.setText(R.string.loading);

        if(txt_recId != null)
            txt_recId.setText(R.string.loading);

        if(txt_referral != null)
            txt_referral.setText(R.string.loading);
    }
}
