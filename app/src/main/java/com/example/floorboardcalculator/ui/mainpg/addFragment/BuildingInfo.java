package com.example.floorboardcalculator.ui.mainpg.addFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.constant.StateList;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class BuildingInfo extends StepDynamicFragment {
    private EditText txtFullAddress, txtCity, txtPostCode;
    private Spinner listBuildType, listState;
    private TextView txtLongitude, txtLatitude;
    private ImageButton btn_GetLocation;
    private ProgressBar btn_LoadingLocation;

    private FusedLocationProviderClient locationProviderClient;

    private @LayoutRes int resourceId;

    public BuildingInfo(int resourceId, int position) {
        super(resourceId, position);

        this.resourceId = resourceId;
    }

    @Override
    public void onViewCreated(@NotNull View v, Bundle savedInstanceState) {
        txtFullAddress = (EditText) v.findViewById(R.id.add_fullAddress);
        txtCity = (EditText) v.findViewById(R.id.add_city);
        txtPostCode = (EditText) v.findViewById(R.id.add_postalCode);
        listBuildType = (Spinner) v.findViewById(R.id.add_dropdownBuildType);
        listState = (Spinner) v.findViewById(R.id.add_stateSelect);
        txtLongitude = (TextView) v.findViewById(R.id.add_lngInput);
        txtLatitude = (TextView) v.findViewById(R.id.add_latInput);
        btn_GetLocation = (ImageButton) v.findViewById(R.id.add_btnGetLocation);
        btn_LoadingLocation = (ProgressBar) v.findViewById(R.id.add_btnLoadingLocation);

        if(wizardDataProcess != null)
            customer = wizardDataProcess.onDataGet();
        else
            customer = new Customer();

        initSetting();

        btn_GetLocation.setOnClickListener(this::GetLocationClicked);
        txtCity.addTextChangedListener(new TextInput());
        txtPostCode.addTextChangedListener(new TextInput());
        txtFullAddress.addTextChangedListener(new TextInput());
    }

    private void initSetting() {
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        ArrayAdapter<CharSequence> adptBuildType;
        ArrayAdapter<String> adptState;

        adptBuildType = ArrayAdapter.createFromResource(context, R.array.building_type, R.layout.support_simple_spinner_dropdown_item);
        adptState = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item);

        StateList.getFullList().forEach((k, v) -> {
            adptState.add(v);
        });

        adptBuildType.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        adptState.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        listBuildType.setAdapter(adptBuildType);
        listState.setAdapter(adptState);

        listBuildType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                isValid();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        listState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                isValid();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void GetLocationClicked(View v) {
        btn_GetLocation.setVisibility(View.GONE);
        btn_LoadingLocation.setVisibility(View.VISIBLE);

        locationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if(location != null) {
                        txtLongitude.setText(String.format("%.7f", location.getLongitude()));
                        txtLatitude.setText(String.format("%.7f", location.getLatitude()));

                        AddressPush push = new AddressPush(location.getLongitude(), location.getLatitude());
                        push.start();
                    }
                    else {
                        Log.e("ERROR", "No location get!");
                        activity.finish();
                    }
                })
                .addOnFailureListener(err -> {
                    Log.e("ERROR", Objects.requireNonNull(err.getMessage()));
                });
    }

    private void isValid() {
        String address = txtFullAddress.getText().toString();
        String city = txtCity.getText().toString();
        String postcode = txtPostCode.getText().toString();
        String longitude = txtLongitude.getText().toString();
        String latitude = txtLatitude.getText().toString();

        if(address.length() >= 10 && city.length() >= 4 && postcode.length() == 5 && longitude.length() >= 7 && latitude.length() >= 7) {
            if(wizardListener != null) wizardListener.onStateUpdated(resourceId, true);
        }
        else {
            if(wizardListener != null) wizardListener.onStateUpdated(resourceId, false);
        }

        customer.setAddress(txtFullAddress.getText().toString());
        customer.setCity(txtCity.getText().toString());
        customer.setPostalCode(txtPostCode.getText().toString());

        if(!latitude.toLowerCase().equals("latitude"))
            customer.setLat(Double.parseDouble(latitude));

        if(!longitude.toLowerCase().equals("longitude"))
            customer.setLng(Double.parseDouble(longitude));

        customer.setBuildingType(listBuildType.getSelectedItemPosition() + 1);

        String selectedState = listState.getSelectedItem().toString();

        StateList.getFullList().forEach((k, v) -> {
            if(v.equals(selectedState)){
                customer.setState(k);
            }
        });

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

    private class AddressPush extends Thread {
        private double lng, lat;

        public AddressPush(double lng, double lat) {
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        public void run() {
            Geocoder geocoder = new Geocoder(context);
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                if(addresses.size() > 0) {
                    activity.runOnUiThread(() -> {
                        String addressLine = addresses.get(0).getAddressLine(0);
                        addressLine = addressLine.replace(addresses.get(0).getPostalCode() + " " + addresses.get(0).getLocality(), "");
                        addressLine = addressLine.replace(addresses.get(0).getAdminArea(), "");
                        addressLine = addressLine.replace(addresses.get(0).getCountryName(), "");
                        addressLine = addressLine.replace(",", "");
                        addressLine = addressLine.trim();

                        txtPostCode.setText(addresses.get(0).getPostalCode());
                        txtCity.setText(addresses.get(0).getLocality());
                        txtFullAddress.setText(addressLine);

                        for(int i=0; i<listState.getCount(); i++) {
                            if(listState.getItemAtPosition(i).toString().contains(addresses.get(0).getAdminArea())) {
                                listState.setSelection(i);
                                break;
                            }
                        }

                        btn_GetLocation.setVisibility(View.VISIBLE);
                        btn_LoadingLocation.setVisibility(View.GONE);
                        isValid();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
