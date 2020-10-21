package com.example.floorboardcalculator.ui.pagerdetails.fragments;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.datamodel.FloorPlan;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.DeleteOnClickListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FloorPlanAdapter extends RecyclerView.Adapter<FloorPlanAdapter.FloorPlanHolder> {
    private List<FloorPlan> listPlan;
    private List<FloorPlanHolder> viewHolder;
    private PreferenceItem setting;
    private DeleteOnClickListener listener;

    public FloorPlanAdapter(List<FloorPlan> plans, PreferenceItem setting){
        this.listPlan = plans;
        this.setting = setting;

        viewHolder = new ArrayList<FloorPlanHolder>();
    }

    @NonNull
    @Override
    public FloorPlanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_floorplan, parent, false);

        return new FloorPlanHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorPlanHolder holder, int position) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 16);

        holder.mainLayout.setLayoutParams(layoutParams);

        DecimalFormat df = new DecimalFormat("#.##");
        String widthText, heightText, totalText;

        if(setting.isDoubleUnit()) {
            widthText = df.format(listPlan.get(position).width * 10) + " mm / " + df.format(listPlan.get(position).width / 30.48) + " ft";
            heightText = df.format(listPlan.get(position).height * 10) + " mm / " + df.format(listPlan.get(position).height / 30.48) + " ft";
            totalText = df.format((listPlan.get(position).getWidth() * listPlan.get(position).getHeight()) / 10000.00) + "m<sup><small>2</small></sup> / "
                    + df.format((listPlan.get(position).getWidth() * listPlan.get(position).getHeight()) / 929.00) + "ft<sup><small>2</small></sup>";
        }
        else {
            if(setting.isUnit()) {
                int val1_m, val1_cm, val1_mm, val2_m, val2_cm, val2_mm;

                switch(setting.getUnitSelect()) {
                    case 0:
                        val1_m = (int)(listPlan.get(position).width / 100);
                        val1_cm = (int)(listPlan.get(position).width % 100);
                        val1_mm = (int)((listPlan.get(position).width % 100.0f) % 10.f);

                        val2_m = (int)(listPlan.get(position).height / 100);
                        val2_cm = (int)(listPlan.get(position).height % 100);
                        val2_mm = (int)((listPlan.get(position).height % 100.0f) % 10.f);

                        widthText = ((val1_m == 0)?"":val1_m + " m ") + ((val1_cm == 0)?"":val1_cm + " cm ") + ((val1_mm == 0.0)?"":val1_mm + " mm");
                        heightText = ((val2_m == 0)?"":val2_m + " m ") + ((val2_cm == 0)?"":val2_cm + " cm ") + ((val2_mm == 0.0)?"":val2_mm + " mm");
                        break;

                    case 1:
                        widthText = df.format(listPlan.get(position).width / 100) + " m";
                        heightText = df.format(listPlan.get(position).height / 100) + " m";
                        break;

                    case 2:
                        widthText = df.format(listPlan.get(position).width) + " cm";
                        heightText = df.format(listPlan.get(position).height) + " cm";
                        break;

                    case 3:
                        widthText = df.format(listPlan.get(position).width * 10) + " cm";
                        heightText = df.format(listPlan.get(position).height * 10) + " cm";
                        break;

                    case 4:
                        val1_m = (int)(listPlan.get(position).width / 100);
                        val1_cm = (int)(listPlan.get(position).width % 100);

                        val2_m = (int)(listPlan.get(position).height / 100);
                        val2_cm = (int)(listPlan.get(position).height % 100);

                        widthText = (val1_m) + " m " + df.format(val1_cm) + " cm";
                        heightText = (val2_m) + " m " + df.format(val2_cm) + " cm";
                        break;

                    default:
                        widthText = "";
                        heightText = "";
                        break;
                }

                totalText = df.format((listPlan.get(position).getWidth() * listPlan.get(position).getHeight()) / 10000.00) + "m<sup><small>2</small></sup>";
            }
            else {
                double val1_y, val1_f, val1_i, val2_y, val2_f, val2_i;

                switch(setting.getUnitSelect()) {
                    case 0:
                        val1_y = Math.floor((listPlan.get(position).width / 30.48f) / 3f);
                        val1_f = Math.floor((listPlan.get(position).width / 30.48f) % 3f);
                        val1_i = (listPlan.get(position).width / 2.54f) % 12f;

                        val2_y = Math.floor((listPlan.get(position).height / 30.48f) / 3f);
                        val2_f = Math.floor((listPlan.get(position).height / 30.48f) % 3f);
                        val2_i = (listPlan.get(position).height / 2.54f) % 12f;

                        widthText = ((val1_y == 0.0)?"":(int)val1_y + " yard ") + (int)val1_f + " ft " + ((val1_i == 0.0)?"":df.format(val1_i) + " in");
                        heightText = ((val2_y == 0.0)?"":(int)val2_y + " yard ") + (int)val2_f + " ft " + ((val2_i == 0.0)?"":df.format(val2_i) + " in");

                        break;

                    case 1:
                        widthText = df.format(listPlan.get(position).width / 30.48) + " ft";
                        heightText = df.format(listPlan.get(position).height / 30.48) + " ft";
                        break;

                    case 2:
                        widthText = df.format(listPlan.get(position).width / 2.54) + " in";
                        heightText = df.format(listPlan.get(position).height / 2.54) + " in";
                        break;

                    case 3:
                        val1_f = Math.floor(listPlan.get(position).width / 30.48f);
                        val1_i = (listPlan.get(position).width / 2.54f) % 12f;

                        val2_f = Math.floor(listPlan.get(position).height / 30.48f);
                        val2_i = (listPlan.get(position).height / 2.54f) % 12f;

                        widthText = ((int)val1_f) + " ft " + ((val1_i == 0.0)?"":df.format(val1_i) + " in");
                        heightText = ((int)val2_f) + " ft " + ((val1_i == 0.0)?"":df.format(val2_i) + " in");
                        break;

                    default:
                        widthText = "";
                        heightText = "";
                        break;
                }

                totalText = df.format((listPlan.get(position).getWidth() * listPlan.get(position).getHeight()) / 929.00) + " ft<sup><small>2</small></sup>";
            }
        }

        holder.name.setText("Area: " + listPlan.get(position).getName());
        holder.length.setText(widthText);
        holder.width.setText(heightText);

        holder.totalArea.setText(Html.fromHtml(totalText));

        holder.delete.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
            builder.setTitle("Remove Area Plan?");

            View viewInflated = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_alert, (ViewGroup) view.getParent(), false);

            TextView inf = viewInflated.findViewById(R.id.alert_text);
            inf.setText("Are you sure want to delete?\nArea: " + this.listPlan.get(position).getName());

            builder.setView(viewInflated);

            builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                if(listener != null) listener.onDeleteClick(this.listPlan.get(position).getName(), position);
            });

            builder.setNegativeButton(R.string.btn_no, (dialog, which) -> {
                dialog.cancel();
            });

            builder.show();
        });

        viewHolder.add(holder);
    }

    @Override
    public int getItemCount() {
        return listPlan.size();
    }

    public void setDeleteEnabled(boolean b) {
        for(FloorPlanHolder holder : viewHolder) {
            holder.delete.setEnabled(b);
        }
    }

    public void setClickListener(DeleteOnClickListener listener){
        this.listener = listener;
    }

    public class FloorPlanHolder extends RecyclerView.ViewHolder {
        private TextView name, width, totalArea, length;
        private ImageButton delete;
        private RelativeLayout mainLayout;

        public FloorPlanHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.floorplan_name);
            length = (TextView) itemView.findViewById(R.id.floorplan_length);
            width = (TextView) itemView.findViewById(R.id.floorplan_width);
            totalArea = (TextView) itemView.findViewById(R.id.floorplan_totalArea);
            delete = (ImageButton) itemView.findViewById(R.id.floorplan_delete);
            mainLayout = (RelativeLayout) itemView.findViewById(R.id.list_item_layout);
        }
    }
}
