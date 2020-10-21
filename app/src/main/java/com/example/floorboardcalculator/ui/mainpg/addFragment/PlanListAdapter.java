package com.example.floorboardcalculator.ui.mainpg.addFragment;

import android.content.Context;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.FloorPlan;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlanListAdapter extends RecyclerView.Adapter<PlanListAdapter.PlanListHolder> {
    private List<FloorPlan> planList;
    private IPlanAdapterListener listener;
    private AlertDialog dialog;

    public PlanListAdapter(@NonNull List<FloorPlan> plans) {
        this.planList = plans;
    }

    @NonNull
    @Override
    public PlanListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_newfloorplan, parent, false);

        PlanListHolder holder = new PlanListHolder(v, parent.getContext());

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlanListHolder holder, int position) {
        Context parent = holder.parent;

        holder.txtAreaLabel.setText(Html.fromHtml("Total Area (" + parent.getString(R.string.feetSq) + ")", Html.FROM_HTML_MODE_COMPACT));
        holder.txtAreaName.setText("Area: " + planList.get(position).getName());
        holder.txtLength.setText(String.format("%.1f", planList.get(position).getWidth()));
        holder.txtWidth.setText(String.format("%.1f", planList.get(position).getHeight()));

        double area = (planList.get(position).getWidth() * planList.get(position).getHeight()) / 929f;

        holder.txtArea.setText(String.format("%,.2f", area));

        if(listener != null) listener.onDataUpdated();

        holder.btnDelete.setOnClickListener((v) -> this.deleteDataPressed(v, holder, position));
        holder.btnEdit.setOnClickListener((v) -> this.editDataPressed(v, holder, position));
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    public void setAdapterListener (IPlanAdapterListener listener) {
        this.listener = listener;
    }

    private void deleteDataPressed(@NotNull View v, @NotNull PlanListHolder holder, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Remove Area Plan?");

        View viewInflated = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_alert, (ViewGroup) v.getParent(), false);

        TextView inf = viewInflated.findViewById(R.id.alert_text);
        inf.setText("Are you sure want to delete?\nArea: " + this.planList.get(position).getName());

        builder.setView(viewInflated);

        builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> {
            planList.remove(position);
            notifyDataSetChanged();

            listener.onDataUpdated();

            dialog.dismiss();
        });

        builder.setNegativeButton(R.string.btn_no, (dialog, which) -> {
            dialog.cancel();
        });

        builder.show();
    }

    private void editDataPressed(@NotNull View v, @NotNull PlanListHolder holder, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("Edit Area Plan");

        View viewInflated = LayoutInflater.from(holder.itemView.getContext()).inflate(R.layout.dialog_addplan, (ViewGroup) v.getParent(), false);

        final EditText name = viewInflated.findViewById(R.id.box_areaName);
        final EditText length = viewInflated.findViewById(R.id.box_length);
        final EditText width = viewInflated.findViewById(R.id.box_width);

        builder.setView(viewInflated);

        name.setEnabled(false);
        name.setText(planList.get(position).getName());
        length.setText(String.valueOf(planList.get(position).getWidth()));
        width.setText(String.valueOf(planList.get(position).getHeight()));

        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
            planList.get(position).setWidth(Double.parseDouble(length.getText().toString()));
            planList.get(position).setHeight(Double.parseDouble(width.getText().toString()));

            notifyDataSetChanged();
            listener.onDataUpdated();

            dialog.dismiss();
        });

        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
            dialog.cancel();
        });

        dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        length.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkValid(length.getText().toString(), width.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkValid(length.getText().toString(), width.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    public class PlanListHolder extends RecyclerView.ViewHolder {
        private TextView txtAreaName, txtLength, txtWidth, txtArea, txtAreaLabel;
        private ImageButton btnEdit, btnDelete;
        private Context parent;

        public PlanListHolder(@NonNull View itemView, @NonNull Context parent) {
            super(itemView);
            this.parent = parent;
            txtAreaName = (TextView) itemView.findViewById(R.id.add_floorplan_name);
            txtLength = (TextView) itemView.findViewById(R.id.add_floorplan_length);
            txtWidth = (TextView) itemView.findViewById(R.id.add_floorplan_width);
            txtArea = (TextView) itemView.findViewById(R.id.add_floorplan_totalArea);
            txtAreaLabel = (TextView) itemView.findViewById(R.id.add_floorplan_totalArea_label);
            btnEdit = (ImageButton) itemView.findViewById(R.id.add_floorplan_edit);
            btnDelete = (ImageButton) itemView.findViewById(R.id.add_floorplan_delete);
        }
    }

    private void checkValid(String len, String wid) {
        switch (possibleCheck(len, wid)) {
            case ERR_DIGIT_NEGATIVE:
            case ERR_HAVE_BLANK:
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                break;

            case TEXT_OK:
            default:
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                break;
        }
    }

    private AlertInput possibleCheck(@NotNull String length, String width) {
        // Check empty
        if(length.isEmpty() || width.isEmpty())
            return AlertInput.ERR_HAVE_BLANK;

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
