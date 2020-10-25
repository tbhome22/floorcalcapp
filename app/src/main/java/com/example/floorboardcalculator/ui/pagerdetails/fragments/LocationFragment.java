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
import com.example.floorboardcalculator.core.constant.BuildingType;
import com.example.floorboardcalculator.core.constant.StateList;
import com.example.floorboardcalculator.core.datamodel.Customer;

public class LocationFragment extends Fragment implements Callable{
    private TextView txt_buildType, txt_address, txt_city, txt_postcode, txt_state, txt_link;
    private InformationProcess dataProcess;
    private Customer data;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailpage_02, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txt_buildType = (TextView) view.findViewById(R.id.detail_buildType);
        txt_address = (TextView) view.findViewById(R.id.detail_fullAddr);
        txt_city = (TextView) view.findViewById(R.id.detail_city);
        txt_postcode = (TextView) view.findViewById(R.id.detail_postalCode);
        txt_state = (TextView) view.findViewById(R.id.detail_state);
        txt_link = (TextView) view.findViewById(R.id.detail_mapPin);

        processDone();
    }

    public void setDataProcess(InformationProcess dataProcess) {
        this.dataProcess = dataProcess;
    }

    @Override
    public void processDone() {
        data = dataProcess.getData();

        if(getView() != null && data != null) {
            txt_address.setText(data.getAddress());
            txt_city.setText(data.getCity());
            txt_postcode.setText(data.getPostalCode());
            txt_state.setText(StateList.getState(data.getState()));
            txt_buildType.setText(BuildingType.getType(data.getBuildingType() - 1));

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<a href=\"https://www.google.com/maps/place/").append(data.getLat()).append("+").append(data.getLng())
                    .append("/@").append(data.getLat()).append(",").append(data.getLng())
                    .append(",15z\">Map Link</a>");

            txt_link.setText(Html.fromHtml(stringBuilder.toString(), Html.FROM_HTML_MODE_COMPACT));
            txt_link.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void processFailed() {

    }

    @Override
    public void processInRefresh() {
        if(txt_buildType != null)
            txt_buildType.setText(R.string.loading);

        if(txt_address != null)
            txt_address.setText(R.string.loading);

        if(txt_city != null)
            txt_city.setText(R.string.loading);

        if(txt_postcode != null)
            txt_postcode.setText(R.string.loading);

        if(txt_state != null)
            txt_state.setText(R.string.loading);

        if(txt_link != null)
            txt_link.setText(R.string.loading);
    }
}
