package com.example.floorboardcalculator.ui.rate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.datamodel.FloorType;
import com.example.floorboardcalculator.ui.lists.ListOnClickListener;

import java.util.List;

public class RateListDataAdapter extends RecyclerView.Adapter<RateListDataAdapter.RateListDataHolder> {
    private List<FloorType> list;
    private Context context;
    private PreferenceItem setting;
    private ListOnClickListener listener;
    private RadioButton selectedRadio;

    public RateListDataAdapter(Context context, List<FloorType> list, PreferenceItem setting) {
        this.list = list;
        this.context = context;
        this.setting = setting;
    }

    @NonNull
    @Override
    public RateListDataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_rate, parent, false);

        return new RateListDataHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RateListDataHolder holder, int position) {
        holder.rateName.setText(list.get(position).full);
        holder.abbrName.setText("Abbr: " + list.get(position).abbr);

        if(setting.isUnit()) {
            holder.baseRate.setText(String.format(
                    context.getString(R.string.rate_word),
                    (list.get(position).base * 10.764),
                    (list.get(position).base_8 * 10.764),
                    (list.get(position).base_15 * 10.764)
            ));
        }
        else {
            holder.baseRate.setText(String.format(
                    context.getString(R.string.rate_word),
                    list.get(position).base,
                    list.get(position).base_8,
                    list.get(position).base_15
            ));
        }

        holder.selectButton.setChecked(list.get(position).isChecked());
        holder.selectButton.setOnClickListener(v -> {
            for(FloorType type : list) {
                type.setChecked(false);
            }

            list.get(position).setChecked(true);

            if(null != selectedRadio && !v.equals(selectedRadio))
                selectedRadio.setChecked(false);

            selectedRadio = (RadioButton) v;
            selectedRadio.setChecked(true);

            if(listener != null) listener.onClick(v, position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setClickListener(ListOnClickListener listener){
        this.listener = listener;
    }

    public class RateListDataHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView rateName, baseRate, abbrName;
        private RelativeLayout mains;
        private View itemView;
        private RadioButton selectButton;

        public RateListDataHolder(@NonNull View itemView) {
            super(itemView);

            rateName = (TextView) itemView.findViewById(R.id.list_rateName);
            baseRate = (TextView) itemView.findViewById(R.id.list_baseRate);
            abbrName = (TextView) itemView.findViewById(R.id.list_abbr);
            mains = (RelativeLayout) itemView.findViewById(R.id.list_rate_layout);
            selectButton = (RadioButton) itemView.findViewById(R.id.list_select_radio);
            this.itemView = itemView;
            itemView.setOnClickListener(this::onClick);
        }

        @Override
        public void onClick(View v) {
            selectButton.performClick();
        }
    }
}
