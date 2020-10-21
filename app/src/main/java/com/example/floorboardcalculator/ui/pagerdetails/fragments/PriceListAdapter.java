package com.example.floorboardcalculator.ui.pagerdetails.fragments;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Config;
import com.example.floorboardcalculator.core.datamodel.FloorType;

import java.util.List;

public class PriceListAdapter extends RecyclerView.Adapter<PriceListAdapter.PriceListHolder> {
    private List<FloorType> floorTypes;
    private double totalCalculatedArea;
    private Config configData;

    public PriceListAdapter(@NonNull List<FloorType> floorTypes, @NonNull Config configData, double totalCalculatedArea) {
        this.floorTypes = floorTypes;
        this.totalCalculatedArea = totalCalculatedArea;
        this.configData = configData;
    }

    @NonNull
    @Override
    public PriceListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_product , parent, false);

        return new PriceListHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PriceListHolder holder, int position) {
        holder.txtProductName.setText(floorTypes.get(position).getFull());

        double range1 = Double.parseDouble(configData.data1) / 929f;
        double range2 = Double.parseDouble(configData.data2) / 929f;
        double chargeArea = Double.parseDouble(configData.data4) / 929f;

        if(totalCalculatedArea > range2) {
            holder.txtTotal.setText("RM " + String.format("%,.2f", (totalCalculatedArea * floorTypes.get(position).base_15)));
            holder.txtRate.setText(Html.fromHtml("RM " + String.format("%.2f", floorTypes.get(position).base_15) + "/" + holder.item.getContext().getString(R.string.feetSq) +
                    ", RM " + String.format("%.2f", floorTypes.get(position).base_15 * 10.764) + "/" + holder.item.getContext().getString(R.string.metreSq)
                    , Html.FROM_HTML_MODE_COMPACT));
        }
        else if(totalCalculatedArea > range1) {
            holder.txtTotal.setText("RM " + String.format("%,.2f", (totalCalculatedArea * floorTypes.get(position).base_8)));
            holder.txtRate.setText(Html.fromHtml("RM " + String.format("%.2f", floorTypes.get(position).base_8) + "/" + holder.item.getContext().getString(R.string.feetSq) +
                            ", RM " + String.format("%.2f", floorTypes.get(position).base_8 * 10.764) + "/" + holder.item.getContext().getString(R.string.metreSq)
                    , Html.FROM_HTML_MODE_COMPACT));
        }
        else {
            if(totalCalculatedArea < chargeArea)
                holder.txtTotal.setText("RM " + String.format("%,.2f", (totalCalculatedArea * floorTypes.get(position).base) + 200f));
            else
                holder.txtTotal.setText("RM " + String.format("%,.2f", (totalCalculatedArea * floorTypes.get(position).base)));

            holder.txtRate.setText(Html.fromHtml("RM " + String.format("%.2f", floorTypes.get(position).base) + "/" + holder.item.getContext().getString(R.string.feetSq) +
                            ", RM " + String.format("%.2f", floorTypes.get(position).base * 10.764) + "/" + holder.item.getContext().getString(R.string.metreSq)
                    , Html.FROM_HTML_MODE_COMPACT));
        }

    }

    @Override
    public int getItemCount() {
        return floorTypes.size();
    }

    public class PriceListHolder extends RecyclerView.ViewHolder {
        private TextView txtProductName, txtRate, txtTotal;
        private View item;

        public PriceListHolder(@NonNull View itemView) {
            super(itemView);
            this.item = itemView;

            txtProductName = (TextView) itemView.findViewById(R.id.list_productName);
            txtRate = (TextView) itemView.findViewById(R.id.list_rate);
            txtTotal = (TextView) itemView.findViewById(R.id.list_totalPrice);
        }
    }
}
