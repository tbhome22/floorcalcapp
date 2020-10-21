package com.example.floorboardcalculator.ui.mainpg.addFragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Config;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.core.datamodel.FloorPlan;
import com.example.floorboardcalculator.ui.addon.LoadingBox;
import com.google.android.gms.tasks.Task;
import com.mongodb.MongoClientSettings;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FloorArea extends StepDynamicFragment implements IPlanAdapterListener{
    private TextView txtNotifyLimit, txtTotalArea;
    private Button mAddPlan;
    private AlertDialog dialog;

    private RecyclerView listCurrentPlan;
    private RecyclerView.LayoutManager layoutManager;
    private PlanListAdapter listAdapter;

    private @LayoutRes int resourceId;
    private float minAreafsq = 0.0f;

    public FloorArea(int resourceId, int position) {
        super(resourceId, position);

        this.resourceId = resourceId;
    }

    @Override
    public void onViewCreated(@NotNull View v, Bundle savedInstanceState) {
        txtNotifyLimit = (TextView) v.findViewById(R.id.add_notifyLimit);
        listCurrentPlan = (RecyclerView) v.findViewById(R.id.add_currentPlanList);
        mAddPlan = (Button) v.findViewById(R.id.add_btnAddPlan);
        txtTotalArea = (TextView) v.findViewById(R.id.add_totalPlanArea);

        if(wizardDataProcess != null)
            customer = wizardDataProcess.onDataGet();
        else
            customer = new Customer();

        initList();

        mAddPlan.setOnClickListener(this::btnAddClicked);
    }


    private void initList() {
        listAdapter = new PlanListAdapter(customer.getFloorPlan());
        listAdapter.setAdapterListener(this);
        listCurrentPlan.setAdapter(listAdapter);

        layoutManager = new LinearLayoutManager(context);
        listCurrentPlan.setLayoutManager(layoutManager);

        txtTotalArea.setText(Html.fromHtml("0.0 " + context.getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
        initMinArea();
    }

    private void btnAddClicked(@NotNull View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add New Area");

        View viewInflated = LayoutInflater.from(context).inflate(R.layout.dialog_addplan, (ViewGroup) v.getParent(), false);

        final EditText name = viewInflated.findViewById(R.id.box_areaName);
        final EditText length = viewInflated.findViewById(R.id.box_length);
        final EditText width = viewInflated.findViewById(R.id.box_width);

        builder.setView(viewInflated);

        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
            FloorPlan plan = new FloorPlan();
            plan.setName(name.getText().toString());
            plan.setWidth(Double.parseDouble(length.getText().toString()));
            plan.setHeight(Double.parseDouble(width.getText().toString()));

            customer.getFloorPlan().add(plan);
            listAdapter.notifyDataSetChanged();

            if(customer.getFloorPlan().size() >= 15)
                mAddPlan.setEnabled(false);
            else
                mAddPlan.setEnabled(true);
        });

        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
            dialog.cancel();
        });

        dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkValid(name.getText().toString(), length.getText().toString(), width.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        length.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkValid(name.getText().toString(), length.getText().toString(), width.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkValid(name.getText().toString(), length.getText().toString(), width.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public void onDataUpdated() {
        int current = customer.getFloorPlan().size();
        double totalArea = 0.0;
        boolean lowerMinArea = false;

        for(FloorPlan plan : customer.getFloorPlan()) {
            totalArea += ((plan.getWidth() * plan.getHeight()) / 929f);
        }

        if(current >= 15) {
            if (totalArea > minAreafsq){
                txtNotifyLimit.setText(Html.fromHtml("Area <span style=\"color:#ff0000;\">15/15</span>, Limit Min 1, Max <span style=\"color:#ff0000;\">15</span>, " +
                        "Min Area: " + ((int) minAreafsq) + context.getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
            }
            else{
                txtNotifyLimit.setText(Html.fromHtml("Area <span style=\"color:#ff0000;\">15/15</span>, Limit Min 1, Max <span style=\"color:#ff0000;\">15</span>, " +
                        "<span style=\"color:#ff0000;\">Min Area: " + ((int) minAreafsq) + context.getString(R.string.feetSq) + "</span>", Html.FROM_HTML_MODE_COMPACT));
                lowerMinArea = true;
            }
        }
        else if(current <= 0){
            txtNotifyLimit.setText(Html.fromHtml("Area <span style=\"color:#ff0000;\">00/15</span>, Limit Min <span style=\"color:#ff0000;\">1</span>, Max 15, " +
                    "<span style=\"color:#ff0000;\">Min Area: " + ((int) minAreafsq) + context.getString(R.string.feetSq) + "</span>" , Html.FROM_HTML_MODE_COMPACT));
            lowerMinArea = true;
        }
        else{
            if(totalArea > minAreafsq) {
                txtNotifyLimit.setText(Html.fromHtml("Area <span style=\"color:#00ff00;\">" + current + "/15</span>, Limit Min 1, Max 15, " +
                        "Min Area: " + ((int) minAreafsq) + context.getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
            }
            else {
                txtNotifyLimit.setText(Html.fromHtml("Area <span style=\"color:#00ff00;\">" + current + "/15</span>, Limit Min 1, Max 15, " +
                        "<span style=\"color:#ff0000;\">Min Area: " + ((int) minAreafsq) + context.getString(R.string.feetSq) + "</span>", Html.FROM_HTML_MODE_COMPACT));
                lowerMinArea = true;
            }
        }

        txtTotalArea.setText(Html.fromHtml(String.format("%,.2f", totalArea) + " " + context.getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));

        if(lowerMinArea)
            txtTotalArea.setTextColor(Color.RED);
        else
            txtTotalArea.setTextColor(Color.GREEN);

        if(current >= 1 && !lowerMinArea)
            wizardListener.onStateUpdated(resourceId, true);
        else
            wizardListener.onStateUpdated(resourceId, false);

        datasetUpdated(customer);
    }

    private void checkValid(String name, String len, String wid) {
        switch (possibleCheck(name, len, wid)) {
            case ERR_DIGIT_NEGATIVE:
            case ERR_HAVE_BLANK:
            case ERR_NAME_DUPLICATED:
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                break;

            case TEXT_OK:
            default:
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                break;
        }
    }

    private AlertInput possibleCheck(@NonNull String name, String length, String width) {
        // Check empty
        if(name.isEmpty() || length.isEmpty() || width.isEmpty())
            return AlertInput.ERR_HAVE_BLANK;

        // Check duplicated name
        for(int i=0; i<this.customer.getFloorPlan().size(); i++){
            if(name.toLowerCase().equals(this.customer.getFloorPlan().get(i).getName().toLowerCase()))
                return AlertInput.ERR_NAME_DUPLICATED;
        }

        // Check negative value
        double len = Double.parseDouble(length), wid = Double.parseDouble(width);
        if(len <= 0.000 || wid <= 0.000)
            return AlertInput.ERR_DIGIT_NEGATIVE;

        return AlertInput.TEXT_OK;
    }

    private enum AlertInput {
        ERR_NAME_DUPLICATED,
        ERR_HAVE_BLANK,
        ERR_DIGIT_NEGATIVE,
        TEXT_OK
    }

    private void initMinArea() {
        LoadingBox box = new LoadingBox(context);
        AlertDialog dialog = box.show();

        box.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);
        box.setMessage("Loading rate configuration...");

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, context.getString(R.string.db_service));
        RemoteMongoCollection<Config> collection = client.getDatabase(context.getString(R.string.db_main)).getCollection("Config", Config.class).withCodecRegistry(codecRegistry);

        final Task<Config> query = collection.findOne(new Document().append("configName", "rate_control"));

        query.addOnCompleteListener((@NonNull Task<Config> configTask) -> {
            if (configTask.isSuccessful()) {
                Config config = configTask.getResult();

                minAreafsq = Float.parseFloat(config.getData3()) / 929f;

                txtNotifyLimit.setText(Html.fromHtml("Area <span style=\"color:#ff0000;\">00/15</span>, Limit Min <span style=\"color:#ff0000;\">1</span>, Max 15, <span style=\"color:#ff0000;\">Min Area: " +
                        ((int) minAreafsq) + context.getString(R.string.feetSq) + "</span>", Html.FROM_HTML_MODE_COMPACT));
            }

            dialog.dismiss();
        });
    }
}
