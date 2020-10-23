package com.example.floorboardcalculator.ui.rate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.datamodel.Config;
import com.example.floorboardcalculator.core.datamodel.FloorType;
import com.example.floorboardcalculator.ui.addon.DialogDismiss;
import com.example.floorboardcalculator.ui.addon.LoadingBox;
import com.example.floorboardcalculator.ui.addon.OnDialogResultListener;
import com.example.floorboardcalculator.ui.addon.RateDialog;
import com.example.floorboardcalculator.ui.lists.ListOnClickListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mongodb.MongoClientSettings;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteDeleteResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.sephiroth.android.library.rangeseekbar.RangeSeekBar;

public class RateFragment extends Fragment implements ListOnClickListener, OnDialogResultListener {
    private static final String APP_PREF = "TWO_BROTHER_SETTING";

    private RangeSeekBar slider;
    private TextView tooltip_start, tooltip_end, Section1, Section2, Section3, RangeTh, RateNotice, EditNote;
    private ConstraintLayout.MarginLayoutParams layoutParams;
    private EditText MinInput, ChargeInput;
    private Button btnAdd, btnEdit, btnDelete;
    private RecyclerView listView;
    private ImageButton MinEdit, ChargeEdit, ApplyChange, DiscardChange, AddValue, MinusValue;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.LayoutManager layoutManager;
    private RateListDataAdapter adapter;
    private BottomNavigationView navigationView;
    private LinearLayout changeLayout;
    private LoadingBox loadBox;
    private AlertDialog rateDialog, deleteDialog;

    private Button btnDeletePositive, btnDeleteNegative;

    private PreferenceItem setting;
    private Config rateConfig;
    private List<FloorType> floors;
    private int selectedPosition = -1;

    private int prevStartPoint = 0, prevEndPoint = 0;
    private SliderPosition currentSliding = SliderPosition.NONE;
    private boolean minIsEdit = false, chargeIsEdit = false, inProcess = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_ratepage, group, false);

        initSetting();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        slider = (RangeSeekBar) view.findViewById(R.id.range_rate);
        tooltip_start = (TextView) view.findViewById(R.id.range_tooltip_start);
        tooltip_end = (TextView) view.findViewById(R.id.range_tooltip_end);
        layoutParams = (ConstraintLayout.MarginLayoutParams) slider.getLayoutParams();
        Section1 = (TextView) view.findViewById(R.id.range_sec_1);
        Section2 = (TextView) view.findViewById(R.id.range_sec_2);
        Section3 = (TextView) view.findViewById(R.id.range_sec_3);
        RangeTh = (TextView) view.findViewById(R.id.range_th_2);
        MinInput = (EditText) view.findViewById(R.id.range_min_input);
        btnAdd = (Button) view.findViewById(R.id.range_btn_add);
        btnEdit = (Button) view.findViewById(R.id.range_btn_edit);
        btnDelete = (Button) view.findViewById(R.id.range_btn_del);
        listView = (RecyclerView) view.findViewById(R.id.rate_list);
        MinEdit = (ImageButton) view.findViewById(R.id.range_edit_min);
        ChargeInput = (EditText) view.findViewById(R.id.range_charge_input);
        ChargeEdit = (ImageButton) view.findViewById(R.id.range_edit_charge);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.rate_swipe);
        RateNotice = (TextView) view.findViewById(R.id.range_ratenotice);
        navigationView = getView().getRootView().findViewById(R.id.nav_view);
        changeLayout = (LinearLayout) view.findViewById(R.id.apply_change_layout);
        ApplyChange = (ImageButton) view.findViewById(R.id.btn_applyRangeChange);
        DiscardChange = (ImageButton) view.findViewById(R.id.btn_discardRangeChange);
        AddValue = (ImageButton) view.findViewById(R.id.btn_range_add);
        MinusValue = (ImageButton) view.findViewById(R.id.btn_range_minus);
        EditNote = (TextView) view.findViewById(R.id.range_edit_info);

        tooltip_end.bringToFront();
        tooltip_start.bringToFront();
        listView.setHasFixedSize(true);

        loadBox = new LoadingBox(getContext());

        if(setting.isUnit()){
            RangeTh.setText(Html.fromHtml("Range Value (" + getString(R.string.metreSq) + ")"));
            MinInput.setHint(Html.fromHtml("Input in " + getString(R.string.metreSq)));
            ChargeInput.setHint(Html.fromHtml("Input in " + getString(R.string.metreSq)));
            RateNotice.setText(Html.fromHtml("(Price per " + getString(R.string.metreSq) + ")"));
        }
        else{
            RangeTh.setText(Html.fromHtml("Range Value (" + getString(R.string.feetSq) + ")"));
            MinInput.setHint(Html.fromHtml("Input in " + getString(R.string.feetSq)));
            ChargeInput.setHint(Html.fromHtml("Input in " + getString(R.string.feetSq)));
            RateNotice.setText(Html.fromHtml("(Price per " + getString(R.string.feetSq) + ")"));
        }

        MinEdit.setOnClickListener(this::minEditButtonClicked);
        ChargeEdit.setOnClickListener(this::chargeEditButtonClicked);
        swipeRefreshLayout.setOnRefreshListener(this::refreshPerform);
        ApplyChange.setOnClickListener(this::saveRangeChange);
        DiscardChange.setOnClickListener(this::discardRangeChange);
        AddValue.setOnClickListener(v -> valueBtnPressed(v, true));
        MinusValue.setOnClickListener(v -> valueBtnPressed(v, false));
        btnAdd.setOnClickListener(this::addNewRate);
        btnEdit.setOnClickListener(this::editCurrentRate);
        btnDelete.setOnClickListener(this::deleteCurrentRate);
    }

    @Override
    public void onStart() {
        super.onStart();

        initSlider();
    }

    @Override
    public void onResume() {
        super.onResume();

        initConfig();
    }

    @Override
    public void onClick(View v, int position) {
        setSelected(position);
    }

    @Override
    public void onResultDone(AlertDialog dialog, boolean refreshNeeded) {
        if(dialog != null)
            dialog.dismiss();

        if(refreshNeeded)
            refreshPerform();
    }

    private void setSelected(int position) {
        if(position == -1) {
            selectedPosition = -1;
            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        }
        else {
            if(floors.size() > 1)
                btnDelete.setEnabled(true);

            selectedPosition = position;
            btnEdit.setEnabled(true);
        }
    }

    private void refreshPerform() {
        swipeRefreshLayout.setRefreshing(false);
        initConfig();

        if(adapter != null) adapter.notifyDataSetChanged();
    }

    private void minEditButtonClicked(View v) {
        minIsEdit = !minIsEdit;

        if(minIsEdit) {
            MinInput.setEnabled(true);
            MinEdit.setImageDrawable(getContext().getDrawable(R.drawable.ic_done));
            EditNote.setVisibility(View.VISIBLE);
        }
        else {
            MinInput.setEnabled(false);
            MinEdit.setImageDrawable(getContext().getDrawable(R.drawable.ic_edit));
            EditNote.setVisibility(View.GONE);
            updateMinArea(v);
        }
    }

    private void chargeEditButtonClicked(View v) {
        chargeIsEdit = !chargeIsEdit;

        if(chargeIsEdit) {
            ChargeInput.setEnabled(true);
            ChargeEdit.setImageDrawable(getContext().getDrawable(R.drawable.ic_done));
            EditNote.setVisibility(View.VISIBLE);
        }
        else {
            ChargeInput.setEnabled(false);
            ChargeEdit.setImageDrawable(getContext().getDrawable(R.drawable.ic_edit));
            EditNote.setVisibility(View.GONE);
            updateLessCharge(v);
        }
    }

    private void initSlider() {
        slider.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onProgressChanged(RangeSeekBar rangeSeekBar, int i, int i1, boolean b) {
                tooltip_start.setText(String.valueOf(i));
                tooltip_end.setText(String.valueOf(i1));
                float x1 = layoutParams.leftMargin + slider.getPaddingLeft()
                        + rangeSeekBar.getThumbStart().getBounds().left - tooltip_start.getWidth()/2;
                float x2 = layoutParams.leftMargin + slider.getPaddingLeft()
                        + rangeSeekBar.getThumbEnd().getBounds().left - tooltip_end.getWidth()/2;
                tooltip_start.setX(x1);
                tooltip_end.setX(x2);
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar rangeSeekBar) {
                tooltip_start.setVisibility(View.VISIBLE);
                tooltip_end.setVisibility(View.VISIBLE);

                tooltip_start.setText(String.valueOf(slider.getProgressStart()));
                tooltip_end.setText(String.valueOf(slider.getProgressEnd()));
                float xa = layoutParams.leftMargin + slider.getPaddingLeft()
                        + rangeSeekBar.getThumbStart().getBounds().left - tooltip_start.getWidth()/2;
                float xb = layoutParams.leftMargin + slider.getPaddingLeft()
                        + rangeSeekBar.getThumbEnd().getBounds().left - tooltip_end.getWidth()/2;
                tooltip_start.setX(xa);
                tooltip_end.setX(xb);

                prevStartPoint = slider.getProgressStart();
                prevEndPoint = slider.getProgressEnd();
            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar rangeSeekBar) {
                tooltip_start.setVisibility(View.GONE);
                tooltip_end.setVisibility(View.GONE);

                float minValue;

                if(setting.isUnit())
                    minValue = Float.parseFloat(rateConfig.data3) / 10000f;
                else
                    minValue = Float.parseFloat(rateConfig.data3) / 929f;

                if(rangeSeekBar.getProgressStart() < minValue) {
                    Toast.makeText(getContext(), "Start area should not less than minimum area!", Toast.LENGTH_SHORT).show();
                    slider.setProgress((int) minValue + 2, rangeSeekBar.getProgressEnd());
                }

                Section1.setText((int)minValue + " to " + (slider.getProgressStart() - 1));
                Section2.setText(slider.getProgressStart() + " to " + (slider.getProgressEnd() - 1));
                Section3.setText("More than " + slider.getProgressEnd());

                if(setting.isUnit()){
                    if((int) slider.getProgressStart() != (int)(Float.parseFloat(rateConfig.data1)/10000f) || (int) slider.getProgressEnd() != (int)(Float.parseFloat(rateConfig.data2)/10000f))
                        changeLayout.setVisibility(View.VISIBLE);
                    else
                        changeLayout.setVisibility(View.GONE);
                }
                else{
                    if((int) slider.getProgressStart() != (int)(Float.parseFloat(rateConfig.data1)/929f) || (int) slider.getProgressEnd() != (int)(Float.parseFloat(rateConfig.data2)/929f))
                        changeLayout.setVisibility(View.VISIBLE);
                    else
                        changeLayout.setVisibility(View.GONE);
                }

                if((prevStartPoint != slider.getProgressStart()) && (prevEndPoint == slider.getProgressEnd())) {
                    currentSliding = SliderPosition.POSITION1;
                }
                else if((prevStartPoint == slider.getProgressStart()) && (prevEndPoint != slider.getProgressEnd())) {
                    currentSliding = SliderPosition.POSITION2;
                }
            }
        });
    }

    private void initSetting() {
        SharedPreferences preferences = getActivity().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        setting = new PreferenceItem();

        setting.setUnit(preferences.getBoolean("unit", false));
        setting.setDoubleUnit(preferences.getBoolean("doubleUnit", false));
        setting.setUnitSelect(preferences.getInt("unitSelect", -1));
        setting.setPriceExp(preferences.getBoolean("priceExp", true));
    }

    private void initConfig() {
        swipeRefreshLayout.setRefreshing(true);
        disableControl();
        Section1.setText(getString(R.string.loading));
        Section2.setText(getString(R.string.loading));
        Section3.setText(getString(R.string.loading));
        inProcess = true;
        if(rateDialog != null) {
            if(rateDialog.isShowing()) rateDialog.dismiss();
        }

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
        RemoteMongoCollection<Config> collection = client.getDatabase(getString(R.string.db_main)).getCollection("Config", Config.class).withCodecRegistry(codecRegistry);

        final Task<Config> query = collection.findOne(new Document().append("configName", "rate_control"));

        query.addOnCompleteListener((@NonNull Task<Config> configTask) -> {
            if(configTask.isSuccessful()) {
                Config config = configTask.getResult();

                if(setting.isUnit()){
                    Log.d("Test", config.configName);
                    float rangeInMsq1 = Float.parseFloat(config.data1) / 10000f;
                    float rangeInMsq2 = Float.parseFloat(config.data2) / 10000f;
                    float minValue = Float.parseFloat(config.data3) / 10000f;
                    float maxTrack = 190f;
                    float chargeValue = Float.parseFloat(config.data4) / 10000f;

                    slider.setMax((int) maxTrack);
                    slider.setProgress((int)rangeInMsq1, (int)rangeInMsq2, true);
                    slider.setStepSize(1);
                    slider.setMinMaxStepSize(10);
                    MinInput.setText(String.format("%.1f", minValue));
                    Section1.setText(String.format("%.1f", minValue) + " to " + String.format("%.1f", rangeInMsq1 - 1f));
                    Section2.setText(String.format("%.1f", rangeInMsq1) + " to " + String.format("%.1f", rangeInMsq2 - 1f));
                    Section3.setText("More than " + String.format("%.1f", rangeInMsq2));
                    ChargeInput.setText(String.format("%.1f", chargeValue));
                }
                else {
                    Log.d("Test", config.configName);
                    float rangeInFsq1 = Float.parseFloat(config.data1) / 929f;
                    float rangeInFsq2 = Float.parseFloat(config.data2) / 929f;
                    float minValue = Float.parseFloat(config.data3) / 929f;
                    float maxTrack = 2000f;
                    float chargeValue = Float.parseFloat(config.data4) / 929f;

                    slider.setMax((int) maxTrack);
                    slider.setProgress((int)rangeInFsq1, (int)rangeInFsq2, true);
                    slider.setStepSize(1);
                    slider.setMinMaxStepSize(20);
                    MinInput.setText(String.format("%.1f", minValue));
                    Section1.setText(String.format("%.1f", minValue) + " to " + String.format("%.1f", rangeInFsq1 - 1f));
                    Section2.setText(String.format("%.1f", rangeInFsq1) + " to " + String.format("%.1f", rangeInFsq2 - 1f));
                    Section3.setText("More than " + String.format("%.1f", rangeInFsq2));
                    ChargeInput.setText(String.format("%.1f", chargeValue));
                }

                rateConfig = config;
            }
            else {
                Log.e("ERROR", Objects.requireNonNull(Objects.requireNonNull(configTask.getException()).getMessage()));
            }

            RemoteMongoCollection<FloorType> collection1 = client.getDatabase(getString(R.string.db_main)).getCollection("FloorType", FloorType.class).withCodecRegistry(codecRegistry);

            final Task<List<FloorType>> task2 = collection1.find().into(new ArrayList<FloorType>());

            task2.addOnCompleteListener((@NonNull Task<List<FloorType>> result) -> {
                if(result.isSuccessful()) {
                    List<FloorType> floors = result.getResult();

                    adapter = new RateListDataAdapter(getContext(), floors, setting);
                    listView.setAdapter(adapter);
                    adapter.setClickListener(RateFragment.this::onClick);

                    layoutManager = new LinearLayoutManager(getContext());
                    listView.setLayoutManager(layoutManager);

                    this.floors = floors;
                }
                else {
                    Log.e("ERROR", Objects.requireNonNull(Objects.requireNonNull(result.getException()).getMessage()));
                }

                setSelected(-1);
                inProcess = false;
                getActivity().runOnUiThread(() -> {
                    navigationView.getMenu().setGroupEnabled(R.id.nav_main, true);
                    enableControl();
                });
            });
        });
    }

    private void enableControl() {
        slider.setEnabled(true);
        MinEdit.setEnabled(true);
        ChargeEdit.setEnabled(true);
        btnAdd.setEnabled(true);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void disableControl() {
        slider.setEnabled(false);
        MinEdit.setEnabled(false);
        ChargeEdit.setEnabled(false);
        btnAdd.setEnabled(false);
        navigationView.getMenu().setGroupEnabled(R.id.nav_main, false);
    }

    private void discardRangeChange(View v) {
        if(setting.isUnit()){
            float rangeInMsq1 = Float.parseFloat(rateConfig.data1) / 10000f;
            float rangeInMsq2 = Float.parseFloat(rateConfig.data2) / 10000f;
            float minValue = Float.parseFloat(rateConfig.data3) / 10000f;
            float maxTrack = 190f;

            slider.setMax((int) maxTrack);
            slider.setProgress((int)rangeInMsq1, (int)rangeInMsq2);
            slider.setStepSize(1);
            slider.setMinMaxStepSize(10);
            Section1.setText(String.format("%.1f", minValue) + " to " + String.format("%.1f", rangeInMsq1 - 1f));
            Section2.setText(String.format("%.1f", rangeInMsq1) + " to " + String.format("%.1f", rangeInMsq2 - 1f));
            Section3.setText("More than " + String.format("%.1f", rangeInMsq2));
        }
        else {
            float rangeInFsq1 = Float.parseFloat(rateConfig.data1) / 929f;
            float rangeInFsq2 = Float.parseFloat(rateConfig.data2) / 929f;
            float minValue = Float.parseFloat(rateConfig.data3) / 929f;
            float maxTrack = 2000f;

            slider.setMax((int) maxTrack);
            slider.setProgress((int)rangeInFsq1, (int)rangeInFsq2);
            slider.setStepSize(1);
            slider.setMinMaxStepSize(20);
            Section1.setText(String.format("%.1f", minValue) + " to " + String.format("%.1f", rangeInFsq1 - 1f));
            Section2.setText(String.format("%.1f", rangeInFsq1) + " to " + String.format("%.1f", rangeInFsq2 - 1f));
            Section3.setText("More than " + String.format("%.1f", rangeInFsq2));
        }

        changeLayout.setVisibility(View.GONE);
        currentSliding = SliderPosition.NONE;
    }

    private void saveRangeChange(View v) {
        AlertDialog load = loadBox.show();
        loadBox.setMessage("Saving Changes...");
        loadBox.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);
        inProcess = true;

        String data1 = "", data2 = "";

        if(setting.isUnit()) {
            data1 = String.valueOf(slider.getProgressStart() * 10000f);
            data2 = String.valueOf(slider.getProgressEnd() * 10000f);
        }
        else {
            data1 = String.valueOf(slider.getProgressStart() * 929f);
            data2 = String.valueOf(slider.getProgressEnd() * 929f);
        }

        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
        RemoteMongoCollection<Document> collection = client.getDatabase(getString(R.string.db_main)).getCollection("Config");

        Document finder = new Document().append("configName", rateConfig.getConfigName());
        Document updateCmd = new Document().append("$set", new Document().append("data1", data1).append("data2", data2));

        final Task<RemoteUpdateResult> resultTask = collection.updateOne(finder, updateCmd);

        final String finalData = data1;
        final String finalData1 = data2;

        resultTask.addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                loadBox.setMessage("Range Updated!");
                loadBox.setLoadingType(LoadingBox.MessageType.SUCCESS);

                rateConfig.setData1(finalData);
                rateConfig.setData2(finalData1);
            }
            else {
                loadBox.setMessage("Failed to update!");
                loadBox.setLoadingType(LoadingBox.MessageType.FAILED);

                if(setting.isUnit()) {
                    float rangeInMsq1 = Float.parseFloat(rateConfig.data1) / 10000f;
                    float rangeInMsq2 = Float.parseFloat(rateConfig.data2) / 10000f;
                    float minValue = Float.parseFloat(rateConfig.data3) / 10000f;
                    float maxTrack = 190f;

                    slider.setMax((int) maxTrack);
                    slider.setProgress((int)rangeInMsq1, (int)rangeInMsq2);
                    Section1.setText(String.format("%.1f", minValue) + " to " + String.format("%.1f", rangeInMsq1 - 1f));
                    Section2.setText(String.format("%.1f", rangeInMsq1) + " to " + String.format("%.1f", rangeInMsq2 - 1f));
                    Section3.setText("More than " + String.format("%.1f", rangeInMsq2));
                }
                else {
                    float rangeInFsq1 = Float.parseFloat(rateConfig.data1) / 929f;
                    float rangeInFsq2 = Float.parseFloat(rateConfig.data2) / 929f;
                    float minValue = Float.parseFloat(rateConfig.data3) / 929f;
                    float maxTrack = 2000f;

                    slider.setMax((int) maxTrack);
                    slider.setProgress((int)rangeInFsq1, (int)rangeInFsq2);
                    Section1.setText(String.format("%.1f", minValue) + " to " + String.format("%.1f", rangeInFsq1 - 1f));
                    Section2.setText(String.format("%.1f", rangeInFsq1) + " to " + String.format("%.1f", rangeInFsq2 - 1f));
                    Section3.setText("More than " + String.format("%.1f", rangeInFsq2));
                }
            }

            ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
            inProcess = false;
            List<Object> dialogDismiss = new ArrayList<>();
            dialogDismiss.add(load);
            Runnable r = new DialogDismiss(svc, dialogDismiss);

            svc.schedule(r, 3, TimeUnit.SECONDS);
        });

        changeLayout.setVisibility(View.GONE);
        currentSliding = SliderPosition.NONE;
    }

    private void valueBtnPressed(View v, boolean pos) {
        float minValue;

        if(setting.isUnit())
            minValue = Float.parseFloat(rateConfig.data3) / 10000f;
        else
            minValue = Float.parseFloat(rateConfig.data3) / 929f;

        switch(currentSliding) {
            case POSITION1:
                if(pos)
                    slider.setProgress(slider.getProgressStart() + 1, slider.getProgressEnd(), true);
                else
                    if(slider.getProgressStart() > minValue + 2)
                        slider.setProgress(slider.getProgressStart() - 1, slider.getProgressEnd(), true);

                break;

            case POSITION2:
                if(pos)
                    slider.setProgress(slider.getProgressStart(), slider.getProgressEnd() + 1, true);
                else
                    slider.setProgress(slider.getProgressStart(), slider.getProgressEnd() - 1, true);

                break;

            case NONE:
                Toast.makeText(getContext(), "Slide first before press manual button!", Toast.LENGTH_SHORT).show();
                break;
        }

        Section1.setText((int)minValue + " to " + (slider.getProgressStart() - 1));
        Section2.setText(slider.getProgressStart() + " to " + (slider.getProgressEnd() - 1));
        Section3.setText("More than " + slider.getProgressEnd());

        if(setting.isUnit()){
            if((int) slider.getProgressStart() != (int)(Float.parseFloat(rateConfig.data1)/10000f) || (int) slider.getProgressEnd() != (int)(Float.parseFloat(rateConfig.data2)/10000f))
                changeLayout.setVisibility(View.VISIBLE);
            else
                changeLayout.setVisibility(View.GONE);
        }
        else{
            if((int) slider.getProgressStart() != (int)(Float.parseFloat(rateConfig.data1)/929f) || (int) slider.getProgressEnd() != (int)(Float.parseFloat(rateConfig.data2)/929f))
                changeLayout.setVisibility(View.VISIBLE);
            else
                changeLayout.setVisibility(View.GONE);
        }
    }

    private void updateMinArea(View v) {
        String text = MinInput.getText().toString();

        if(text.length() > 0){
            float values = Float.parseFloat(text);
            float minVal;

            if(setting.isUnit())
                minVal = Float.parseFloat(rateConfig.data3) / 10000f;
            else
                minVal = Float.parseFloat(rateConfig.data3) / 929f;

            if(values != minVal) {
                final String newValue = String.valueOf((setting.isUnit()) ? (values*10000f) : (values*929f));

                AlertDialog load = loadBox.show();
                loadBox.setMessage("Saving Changes...");
                loadBox.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);
                inProcess = true;

                RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
                RemoteMongoCollection<Document> collection = client.getDatabase(getString(R.string.db_main)).getCollection("Config");

                Document finder = new Document().append("configName", rateConfig.getConfigName());
                Document updateCmd = new Document().append("$set", new Document().append("data3", newValue));

                final Task<RemoteUpdateResult> resultTask = collection.updateOne(finder, updateCmd);

                resultTask.addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        loadBox.setMessage("Minimun Value Updated!");
                        loadBox.setLoadingType(LoadingBox.MessageType.SUCCESS);

                        rateConfig.setData3(newValue);
                    }
                    else {
                        loadBox.setMessage("Failed to update!");
                        loadBox.setLoadingType(LoadingBox.MessageType.FAILED);
                    }

                    ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
                    inProcess = false;
                    List<Object> dialogDismiss = new ArrayList<>();
                    dialogDismiss.add(load);
                    Runnable r = new DialogDismiss(svc, dialogDismiss);

                    svc.schedule(r, 2, TimeUnit.SECONDS);
                    refreshPerform();
                });
            }
        }
        else {
            float minVal;

            if(setting.isUnit())
                minVal = Float.parseFloat(rateConfig.data3) / 10000f;
            else
                minVal = Float.parseFloat(rateConfig.data3) / 929f;

            MinInput.setText(String.format("%.1f", minVal));
        }
    }

    private void updateLessCharge(View v) {
        String text = ChargeInput.getText().toString();

        if(text.length() > 0) {
            float values = Float.parseFloat(text);
            float chargeVal;

            if(setting.isUnit())
                chargeVal = Float.parseFloat(rateConfig.data4) / 10000f;
            else
                chargeVal = Float.parseFloat(rateConfig.data4) / 929f;

            if(values != chargeVal) {
                if(values >= slider.getProgressStart() - 20) {
                    Toast.makeText(getContext(), "Charge value should not more than Section 2 value!", Toast.LENGTH_SHORT).show();
                    ChargeInput.setText(String.format("%.1f", chargeVal));

                    return;
                }


                final String newValue = String.valueOf((setting.isUnit()) ? (values*10000f) : (values*929f));

                AlertDialog load = loadBox.show();
                loadBox.setMessage("Saving Changes...");
                loadBox.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);
                inProcess = true;

                RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
                RemoteMongoCollection<Document> collection = client.getDatabase(getString(R.string.db_main)).getCollection("Config");

                Document finder = new Document().append("configName", rateConfig.getConfigName());
                Document updateCmd = new Document().append("$set", new Document().append("data4", newValue));

                final Task<RemoteUpdateResult> resultTask = collection.updateOne(finder, updateCmd);

                resultTask.addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        loadBox.setMessage("Less Area Charge Value Updated!");
                        loadBox.setLoadingType(LoadingBox.MessageType.SUCCESS);

                        rateConfig.setData3(newValue);
                    }
                    else {
                        loadBox.setMessage("Failed to update!");
                        loadBox.setLoadingType(LoadingBox.MessageType.FAILED);
                    }

                    ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
                    inProcess = false;
                    List<Object> dialogDismiss = new ArrayList<>();
                    dialogDismiss.add(load);
                    Runnable r = new DialogDismiss(svc, dialogDismiss);

                    svc.schedule(r, 2, TimeUnit.SECONDS);
                    refreshPerform();
                });
            }
        }
        else {
            float chargeVal;

            if(setting.isUnit())
                chargeVal = Float.parseFloat(rateConfig.data4) / 10000f;
            else
                chargeVal = Float.parseFloat(rateConfig.data4) / 929f;

            ChargeInput.setText(String.format("%.1f", chargeVal));
        }
    }

    private void addNewRate(View v) {
        RateDialog dialog = new RateDialog(getContext());
        dialog.setOnResultListener(this);
        rateDialog = dialog.show();
    }

    private void editCurrentRate(View v) {
        RateDialog dialog = new RateDialog(getContext(), floors.get(selectedPosition));
        dialog.setOnResultListener(this);
        rateDialog = dialog.show();
    }

    private void deleteCurrentRate(@NotNull View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Remove record?");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_alert, (ViewGroup) v.getParent(), false);
        builder.setView(viewInflated);
        builder.setCancelable(false);

        TextView inf = viewInflated.findViewById(R.id.alert_text);
        LinearLayout loader = viewInflated.findViewById(R.id.loading_loader);
        inf.setText("Are you sure want to delete?\nProduct: " + floors.get(selectedPosition).getFull());

        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> {});
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel() );

        deleteDialog = builder.create();

        deleteDialog.show();

        btnDeletePositive = deleteDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btnDeleteNegative = deleteDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        btnDeletePositive.setOnClickListener(c -> deletePressed(c, loader));
    }

    public void deletePressed(View v, @NotNull LinearLayout loader) {
        btnDeletePositive.setEnabled(false);
        btnDeleteNegative.setEnabled(false);
        loader.setVisibility(View.VISIBLE);

        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
        RemoteMongoCollection<Document> collection = client.getDatabase(getString(R.string.db_main)).getCollection("FloorType");

        final Task<RemoteDeleteResult> query = collection.deleteOne(new Document().append("_id", floors.get(selectedPosition).get_id()));

        query.addOnCompleteListener(resultTask -> {
            if(resultTask.isSuccessful()) {
                Log.d("DELETE RECORD", "Success deleted product!");
            }
            else {
                Log.e("ERROR", "Delete product failed!");
            }

            refreshPerform();
            deleteDialog.dismiss();
        });
    }

    public boolean allowBackPressed() {
        return !swipeRefreshLayout.isRefreshing() || !inProcess;
    }

    private enum SliderPosition {
        NONE,
        POSITION1,
        POSITION2
    }
}
