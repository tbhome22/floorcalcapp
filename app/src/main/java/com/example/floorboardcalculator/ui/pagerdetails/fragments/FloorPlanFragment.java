package com.example.floorboardcalculator.ui.pagerdetails.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.datamodel.Config;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.core.datamodel.FloorPlan;
import com.example.floorboardcalculator.core.datamodel.FloorType;
import com.example.floorboardcalculator.ui.addon.LoadingBox;
import com.google.android.gms.tasks.Task;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FloorPlanFragment extends Fragment implements Callable, DeleteOnClickListener, ConfigCallable{
    private TextView txt_areaMeasure, txt_addCalc, txt_ttlArea, mSkirting, mCurved, txt_titleFloorPlan;
    private ImageButton btnAddPlan;
    private RecyclerView mListFloorPlan;
    private ProgressBar mListLoading;
    private LinearLayoutManager listLayoutManager;
    private FloorPlanAdapter planAdapter;
    private AlertDialog dialog;
    private LoadingBox loadingBox;

    private InformationProcess dataProcess;
    private Customer data;
    private PreferenceItem setting;
    private Config rateConfig;

    private double totalArea = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detailpage_03, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingBox = new LoadingBox(view.getContext());

        txt_areaMeasure = (TextView) view.findViewById(R.id.detail_totalArea);
        txt_addCalc = (TextView) view.findViewById(R.id.detail_addCalc);
        txt_ttlArea = (TextView) view.findViewById(R.id.detail_ttlAreaInclAdd);
        mSkirting = (TextView) view.findViewById(R.id.floorplan_skirtingLen);
        mCurved = (TextView) view.findViewById(R.id.floorplan_curved);
        mListFloorPlan = (RecyclerView) view.findViewById(R.id.floorplan_list);
        mListLoading = (ProgressBar) view.findViewById(R.id.floorplan_inprocess);
        txt_titleFloorPlan = (TextView) view.findViewById(R.id.detail_floorPlanTitle);
        btnAddPlan = (ImageButton) view.findViewById(R.id.detail_addFloorPlan);

        btnAddPlan.setEnabled(false);
        processDone();

        btnAddPlan.setOnClickListener(this::AddOnClicked);
    }

    public void setDataProcess(InformationProcess dataProcess) {
        this.dataProcess = dataProcess;
    }

    @Override
    public void processDone() {
        data = dataProcess.getData();
        setting = dataProcess.getSetting();

        if(getView() != null && data != null) {
            double countListArea = 0;
            int inputSize = 0;

            for(int i=0; i<data.getFloorPlan().size(); i++){
                double width = data.getFloorPlan().get(i).width;
                double height = data.getFloorPlan().get(i).height;

                countListArea += (width * height);
                inputSize++;
            }

            totalArea = countListArea;

            double msq = countListArea / 10000.00, fsq = countListArea / 929.00, add1, add2;
            int percentType;

            if(countListArea / 929.00 > 400.00){
                percentType = 3;
                add1 = msq * .05;
                add2 = fsq * .05;
            }
            else if(countListArea / 929.00 > 200.00){
                percentType = 2;
                add1 = msq * .08;
                add2 = fsq * .08;
            }
            else{
                percentType = 1;
                add1 = msq * .1;
                add2 = fsq * .1;
            }

            if(setting.isDoubleUnit()) {
                txt_areaMeasure.setText(Html.fromHtml(String.format("%.2f", msq) + getString(R.string.metreSq) + " / " + String.format("%.2f", fsq)  + getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
                txt_addCalc.setText(Html.fromHtml(String.format("%.2f", add1) + getString(R.string.metreSq) + " / " + String.format("%.2f", add2) + getString(R.string.feetSq)
                        + " (" + ((percentType==1)?"10":(percentType==2)?"8":"5") + "%)", Html.FROM_HTML_MODE_COMPACT));
                txt_ttlArea.setText(Html.fromHtml(String.format("%.2f", msq + add1) + getString(R.string.metreSq) + " / " + String.format("%.2f", fsq + add2)  + getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
            }
            else {
                if(setting.isUnit()){
                    txt_areaMeasure.setText(Html.fromHtml(String.format("%.2f", msq) + getString(R.string.metreSq), Html.FROM_HTML_MODE_COMPACT));
                    txt_addCalc.setText(Html.fromHtml(String.format("%.2f", add1) + getString(R.string.metreSq) + " (" + ((percentType==1)?"10":(percentType==2)?"8":"5") + "%)", Html.FROM_HTML_MODE_COMPACT));
                    txt_ttlArea.setText(Html.fromHtml(String.format("%.2f", msq + add1) + getString(R.string.metreSq), Html.FROM_HTML_MODE_COMPACT));
                }
                else{
                    txt_areaMeasure.setText(Html.fromHtml(String.format("%.2f", fsq) + getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
                    txt_addCalc.setText(Html.fromHtml(String.format("%.2f", add2) + getString(R.string.feetSq) + " (" + ((percentType==1)?"10":(percentType==2)?"8":"5") + "%)", Html.FROM_HTML_MODE_COMPACT));
                    txt_ttlArea.setText(Html.fromHtml(String.format("%.2f", fsq + add2)  + getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
                }
            }

            inputFloorInf();

            planAdapter = new FloorPlanAdapter(data.getFloorPlan(), setting);
            planAdapter.setClickListener(this);
            listLayoutManager = new LinearLayoutManager(getContext());

            mListFloorPlan.setAdapter(planAdapter);
            mListFloorPlan.setLayoutManager(listLayoutManager);

            StringBuilder builder = new StringBuilder("Area Floor Plan").append(" (Current: ").append(inputSize)
                    .append(", Min: 1, Max 15)");
            txt_titleFloorPlan.setText(builder.toString());

            planAdapter.setDeleteEnabled(false);

            mListLoading.setVisibility(View.GONE);
            mListFloorPlan.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void processFailed() {

    }

    @Override
    public void processInRefresh() {
        if(txt_areaMeasure != null)
            txt_areaMeasure.setText(R.string.loading);

        if(txt_addCalc != null)
            txt_addCalc.setText(R.string.loading);

        if(txt_ttlArea != null)
            txt_ttlArea.setText(R.string.loading);

        if(mSkirting != null)
            mSkirting.setText(R.string.loading);

        if(mCurved != null)
            mCurved.setText(R.string.loading);

        if(mListLoading != null && mListFloorPlan != null) {
            mListLoading.setVisibility(View.VISIBLE);
            mListFloorPlan.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDeleteClick(@NonNull String name, @NonNull int dataPosition) {
        if(data.getFloorPlan().size() <= 1) {
            Toast.makeText(getContext(), "Reached minimum limit! (min=1)", Toast.LENGTH_SHORT).show();
            return;
        }

        if(setting.isUnit()) {
            if((totalArea / 10000f) < (Double.parseDouble(rateConfig.data3) / 10000f)) {
                Toast.makeText(getContext(), "Reached minimum area! (min=" + String.format("%.2f", Double.parseDouble(rateConfig.data3) / 10000f) + ")", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            if((totalArea / 929f) < (Double.parseDouble(rateConfig.data3) / 929f)) {
                Toast.makeText(getContext(), "Reached minimum area! (min=" + String.format("%.2f", Double.parseDouble(rateConfig.data3) / 929f) + ")", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        DeletePlan(name);
    }

    public void AddOnClicked(@NotNull View v) {
        if(data.getFloorPlan().size() >= 15) {
            Toast.makeText(v.getContext(), "Reached maximum area limit! (max=15)", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Add New Area");

        View viewInflated = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_addplan, (ViewGroup) v.getParent(), false);

        final EditText name = viewInflated.findViewById(R.id.box_areaName);
        final EditText length = viewInflated.findViewById(R.id.box_length);
        final EditText width = viewInflated.findViewById(R.id.box_width);

        builder.setView(viewInflated);

        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
            FloorPlan plan = new FloorPlan();
            plan.setName(name.getText().toString());
            plan.setWidth(Double.parseDouble(length.getText().toString()));
            plan.setHeight(Double.parseDouble(width.getText().toString()));

            AddPlan(plan);
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

    private void inputFloorInf() {
        String skirtingText = "";
        final float len = data.getFloorInf().skirtingLen;

        if(setting.isDoubleUnit()) {
            skirtingText = (len/100) + " m / " + String.format("%.2f", (len/30.48)) + " ft";
        }
        else {
            int val1_m, val1_cm, val1_mm;

            if(setting.isUnit()) {
                switch(setting.getUnitSelect()) {
                    case 0:
                        val1_m = (int)(len / 100f);
                        val1_cm = (int)(len % 100f);
                        val1_mm = (int)((len % 100.0f) % 10.f);

                        skirtingText = ((val1_m == 0)?"":val1_m + " m ") + ((val1_cm == 0)?"":val1_cm + " cm ") + ((val1_mm == 0.0)?"":val1_mm + " mm");
                        break;

                    case 1:
                        skirtingText = String.format("%.2f", len / 100) + " m";
                        break;

                    case 2:
                        skirtingText = String.format("%.2f", len) + " cm";
                        break;

                    case 3:
                        skirtingText = String.format("%.2f", len * 10) + " mm";
                        break;

                    case 4:
                        val1_m = (int)(len / 100f);
                        val1_cm = (int)(len % 100f);

                        skirtingText = (val1_m) + " m " + val1_cm + " cm";
                        break;
                }
            }
            else {
                double val1_y, val1_f, val1_i;

                switch(setting.getUnitSelect()) {
                    case 0:
                        val1_y = Math.floor((len / 30.48f) / 3f);
                        val1_f = Math.floor((len / 30.48f) % 3f);
                        val1_i = (len / 2.54f) % 12f;

                        skirtingText = ((val1_y == 0.0)?"":(int)val1_y + " yard ") + (int)val1_f + " ft " + ((val1_i == 0.0)?"":String.format("%.2f", val1_i) + " in");
                        break;

                    case 1:
                        skirtingText = String.format("%.2f", (len/30.48)) + " ft";
                        break;

                    case 2:
                        skirtingText = String.format("%.2f", (len/2.54)) + " in";
                        break;

                    case 3:
                        val1_f = Math.floor(len / 30.48f);
                        val1_i = (len / 2.54f) % 12f;

                        skirtingText = ((int)val1_f) + " ft " + ((val1_i == 0.0)?"":String.format("%.2f", val1_i) + " in");

                        break;
                }

            }
        }

        mSkirting.setText(skirtingText);

        if(data.getFloorInf().isCurvedArea())
            mCurved.setText("Yes");
        else
            mCurved.setText("No");
    }

    private void AddPlan(@NonNull FloorPlan item) {
        AlertDialog load = loadingBox.show();
        loadingBox.setMessage("Adding new area...");
        loadingBox.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);

        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
        RemoteMongoCollection<Document> dbCollection = client.getDatabase(getString(R.string.db_main)).getCollection("Customer");

        List<BasicDBObject> data = new ArrayList<>();
        this.data.getFloorPlan().forEach(i -> {
            DBObject obj = new BasicDBObject();
            obj.put("name", i.name);
            obj.put("width", i.width);
            obj.put("height", i.height);

            data.add((BasicDBObject) obj);
        });

        BasicDBObject newItem = new BasicDBObject();
        newItem.put("name", item.name);
        newItem.put("width", item.width);
        newItem.put("height", item.height);

        data.add(newItem);

        Document finder = new Document().append("_id", this.data.get_id());
        Document updateCmd = new Document().append("$set",
                new Document().append("FloorPlan", data));

        final Task<RemoteUpdateResult> tResult = dbCollection.updateOne(finder, updateCmd);

        tResult.addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                loadingBox.setMessage("Floor area added!");
                loadingBox.setLoadingType(LoadingBox.MessageType.SUCCESS);
            }
            else{
                Log.e("ERROR", Objects.requireNonNull(task.getException().getMessage()));
                loadingBox.setMessage("Failed add floor area!");
                loadingBox.setLoadingType(LoadingBox.MessageType.FAILED);
            }

            dataProcess.notifyRefresh();
            ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
            Runnable r = new AlertDismiss(load, svc);

            svc.schedule(r, 3, TimeUnit.SECONDS);
        });
    }

    public void DeletePlan(@NonNull String name) {
        AlertDialog load = loadingBox.show();
        loadingBox.setMessage("Deleting area...");
        loadingBox.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);

        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
        RemoteMongoCollection<Document> dbCollection = client.getDatabase(getString(R.string.db_main)).getCollection("Customer");

        List<BasicDBObject> data = new ArrayList<>();
        int position = 0, dot = 0;

        for(FloorPlan i : this.data.getFloorPlan()) {
            if(!name.equals(i.name)){
                DBObject obj = new BasicDBObject();
                obj.put("name", i.name);
                obj.put("width", i.width);
                obj.put("height", i.height);

                data.add((BasicDBObject) obj);
            }
            else {
                dot = position;
            }

            position++;
        }

        Document finder = new Document().append("_id", this.data.get_id());
        Document updateCmd = new Document().append("$set",
                new Document().append("FloorPlan", data));

        final Task<RemoteUpdateResult> tResult = dbCollection.updateOne(finder, updateCmd);

        int finalPosition = dot;

        tResult.addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                loadingBox.setMessage("Floor area deleted!");
                loadingBox.setLoadingType(LoadingBox.MessageType.SUCCESS);
            }
            else{
                Log.e("ERROR", Objects.requireNonNull(task.getException().getMessage()));
                loadingBox.setMessage("Failed to delete area!");
                loadingBox.setLoadingType(LoadingBox.MessageType.FAILED);
            }

            dataProcess.notifyRefresh();
            ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
            Runnable r = new AlertDismiss(load, svc);

            svc.schedule(r, 3, TimeUnit.SECONDS);
        });
    }

    @Override
    public void onConfigReached(Config config) {
        rateConfig = config;

        if(getView() != null) {
            btnAddPlan.setEnabled(true);
            planAdapter.setDeleteEnabled(true);
        }
    }

    @Override
    public void onProductReached(List<FloorType> products) {

    }

    @Override
    public void onFailedReach() {
        Toast.makeText(getContext(), "Failed to retrieve configuration!", Toast.LENGTH_SHORT).show();
    }

    private static class AlertDismiss implements Runnable {
        private AlertDialog dialog;
        private ScheduledExecutorService svc;

        public AlertDismiss(AlertDialog dialog, ScheduledExecutorService svc) {
            this.dialog = dialog;
            this.svc = svc;
        }

        @Override
        public void run(){
            dialog.dismiss();
            svc.shutdown();
        }
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
        for(int i=0; i<data.getFloorPlan().size(); i++){
            if(name.toLowerCase().equals(data.getFloorPlan().get(i).getName().toLowerCase()))
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
}
