package com.example.floorboardcalculator.ui.pagerdetails.fragments;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CustomerFragment extends Fragment implements Callable{
    private TextView txt_CustName, txt_date, txt_recId, txt_referral, txt_contact, txt_whatsApp;
    private InformationProcess dataProcess;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailpage_01, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txt_CustName = (TextView) view.findViewById(R.id.detail_custName);
        txt_date = (TextView) view.findViewById(R.id.detail_createDate);
        txt_recId = (TextView) view.findViewById(R.id.detail_recId);
        txt_referral = (TextView) view.findViewById(R.id.detail_referral);
        txt_contact = (TextView) view.findViewById(R.id.detail_contactNo);
        txt_whatsApp = (TextView) view.findViewById(R.id.detail_whatsapp);

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

            String trimmedPhone = rebuildPhone(data.getContactNo());
            StringBuilder builder = new StringBuilder();

            if(trimmedPhone.charAt(0) == '6') {
                builder.append("<a href=\"http://wa.me/").append(trimmedPhone).append("\">WhatsApp</a>");
            }
            else {
                builder.append("<a href=\"http://wa.me/6").append(trimmedPhone).append("\">WhatsApp</a>");
            }

            txt_whatsApp.setText(Html.fromHtml(builder.toString(), Html.FROM_HTML_MODE_COMPACT));
            txt_whatsApp.setMovementMethod(LinkMovementMethod.getInstance());
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

        if(txt_whatsApp != null)
            txt_whatsApp.setText("");
    }

    private @NotNull String rebuildPhone(@NotNull String raw) {
        StringBuilder builder = new StringBuilder();

        for(int i=0; i<raw.length(); i++) {
            switch(raw.charAt(i)) {
                case ' ':
                case '+':
                case '-':break;

                default:
                    builder.append(raw.charAt(i));
            }
        }

        return builder.toString();
    }
}
