package com.example.floorboardcalculator.ui.pagerdetails.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.datamodel.Config;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.core.datamodel.FloorPlan;
import com.example.floorboardcalculator.core.datamodel.FloorType;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PriceFragment extends Fragment implements Callable, ConfigCallable{
    private TextView mTotalCalculated, mMinAreaCharge, mIsPenalty;
    private RecyclerView mPriceList;
    private LinearLayoutManager layoutManager;
    private ProgressBar mLoading;
    private PriceListAdapter listAdapter;

    private InformationProcess dataProcess;
    private List<FloorPlan> floorPlans;
    private List<FloorType> floorTypes;
    private PreferenceItem settings;
    private Config rateConfig;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailpage_04, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTotalCalculated = (TextView) view.findViewById(R.id.detail_ttlAreaInclAdd);
        mMinAreaCharge = (TextView) view.findViewById(R.id.detail_penaltyArea);
        mIsPenalty = (TextView) view.findViewById(R.id.detail_isPenalty);
        mPriceList = (RecyclerView) view.findViewById(R.id.list_price);
        mLoading = (ProgressBar) view.findViewById(R.id.pricelist_loading);

        processDone();
    }

    @Override
    public void processDone() {
        Customer customer = dataProcess.getData();
        settings = dataProcess.getSetting();

        if(getView() != null && customer != null) {
            floorPlans = customer.getFloorPlan();

            ScheduledExecutorService svc = Executors.newSingleThreadScheduledExecutor();
            Runnable r = new AwaitData(svc);

            svc.scheduleAtFixedRate(r, 0, 1, TimeUnit.SECONDS);
        }
    }

    @Override
    public void processFailed() {

    }

    @Override
    public void processInRefresh() {
        if(mTotalCalculated != null)
            mTotalCalculated.setText(R.string.loading);

        if(mMinAreaCharge != null)
            mMinAreaCharge.setText(R.string.loading);

        if(mIsPenalty != null)
            mIsPenalty.setText(R.string.loading);

        if(mPriceList != null && mLoading != null) {
            mLoading.setVisibility(View.VISIBLE);
            mPriceList.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfigReached(Config config) {
        rateConfig = config;
    }

    @Override
    public void onProductReached(List<FloorType> products) {
        floorTypes = products;
    }

    @Override
    public void onFailedReach() {
        Toast.makeText(getContext(), "Failed to retrieve configuration!", Toast.LENGTH_SHORT).show();
    }

    public void setDataProcess(InformationProcess dataProcess) {
        this.dataProcess = dataProcess;
    }

    public class AwaitData implements Runnable {
        private ScheduledExecutorService scheduler;

        public AwaitData(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public void run() {
            if(getView() != null && rateConfig != null && floorTypes != null) {
                double totalCalculated = 0.0, penaltyArea = Double.parseDouble(rateConfig.getData4());

                for(int i=0; i<floorPlans.size(); i++){
                    double width = floorPlans.get(i).width;
                    double height = floorPlans.get(i).height;

                    totalCalculated += (width * height);
                }

                double msq = totalCalculated / 10000.00, fsq = totalCalculated / 929.00;

                if(totalCalculated / 929.00 > 400.00){
                    msq *= 1.05;
                    fsq *= 1.05;
                }
                else if(totalCalculated / 929.00 > 200.00){
                    msq *= 1.08;
                    fsq *= 1.08;
                }
                else{
                    msq *= 1.1;
                    fsq *= 1.1;
                }

                double finalFsq = fsq;
                double finalMsq = msq;

                getActivity().runOnUiThread(() -> {
                    if(finalFsq > (penaltyArea / 929f))
                        mIsPenalty.setText("NO");
                    else
                        mIsPenalty.setText("YES (RM 200.00 charge applies)");

                    if(settings.isDoubleUnit()) {
                        mTotalCalculated.setText(Html.fromHtml(String.format("%.2f", finalMsq) + getString(R.string.metreSq)
                                + " / " + String.format("%.2f", finalFsq)  + getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
                        mMinAreaCharge.setText(Html.fromHtml("< " + String.format("%.2f", (penaltyArea / 10000f)) + getString(R.string.metreSq)
                                + " / " + String.format("%.1f", (penaltyArea / 929f)) + getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
                    }
                    else {
                        if(settings.isUnit()) {
                            mTotalCalculated.setText(Html.fromHtml(String.format("%.2f", finalMsq) + getString(R.string.metreSq), Html.FROM_HTML_MODE_COMPACT));
                            mMinAreaCharge.setText(Html.fromHtml("< " + String.format("%.2f", (penaltyArea / 10000f)) + getString(R.string.metreSq), Html.FROM_HTML_MODE_COMPACT));
                        }
                        else {
                            mTotalCalculated.setText(Html.fromHtml(String.format("%.2f", finalFsq) + getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
                            mMinAreaCharge.setText(Html.fromHtml("< " + String.format("%.1f", (penaltyArea / 929f)) + getString(R.string.feetSq), Html.FROM_HTML_MODE_COMPACT));
                        }
                    }

                    mLoading.setVisibility(View.GONE);
                    mPriceList.setVisibility(View.VISIBLE);

                    listAdapter = new PriceListAdapter(floorTypes, rateConfig, finalFsq);
                    mPriceList.setAdapter(listAdapter);
                    layoutManager = new LinearLayoutManager(getContext());
                    mPriceList.setLayoutManager(layoutManager);
                });

                scheduler.shutdown();
            }
        }
    }
}
