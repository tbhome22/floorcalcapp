package com.example.floorboardcalculator.ui.mainpg;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.floorboardcalculator.R;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment {
    private TextView ttl01, ttl02;
    private Button addCustomer;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mainpage, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NotNull View parent, Bundle savedInstanceState) {
        ttl01 = (TextView) parent.findViewById(R.id.main_ttl_02);
        ttl02 = (TextView) parent.findViewById(R.id.main_ttl_04);
        addCustomer = (Button) parent.findViewById(R.id.btn_AddNewCustomer);

        addCustomer.setOnClickListener(this::AddButtonClicked);
    }

    private void AddButtonClicked(View v) {
        Intent i = new Intent(getContext(), AddNewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(i);
    }
}