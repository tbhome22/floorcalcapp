package com.example.floorboardcalculator.ui.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.ui.addon.LoadingBox;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteDeleteResult;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ListDataAdapter extends RecyclerView.Adapter<ListDataAdapter.ListDataViewHolder> {
    private List<Customer> dataset;
    private ListOnClickListener clickListener;
    private ListOnDataListener dataListener;
    private Context parent;

    public class ListDataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ObjectId mRecordId;
        public TextView mCustomerName;
        public TextView mBuildingType;
        public TextView mCreateDate;
        public ImageButton mDeleteButton;
        public View v;

        public ListDataViewHolder(View v) {
            super(v);

            mCustomerName = (TextView) v.findViewById(R.id.list_custName);
            mBuildingType = (TextView) v.findViewById(R.id.list_buildType);
            mCreateDate = (TextView) v.findViewById(R.id.list_createDate);
            mDeleteButton = (ImageButton) v.findViewById(R.id.list_delete);
            this.v = v;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) clickListener.onClick(v, getAdapterPosition());
        }
    }

    public ListDataAdapter(List<Customer> dataset, @NonNull Context parent) {
        this.dataset = dataset;
        this.parent = parent;
    }

    public void setDataListener(ListOnDataListener dataListener) {
        this.dataListener = dataListener;
    }

    public void setClickListener(ListOnClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Override
    public ListDataAdapter.ListDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        ListDataViewHolder vh = new ListDataViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(ListDataViewHolder holder, int position) {
        holder.mCustomerName.setText("Customer: " + this.dataset.get(position).CustName);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        holder.mCreateDate.setText(format.format(this.dataset.get(position).getAddDate()));

        switch(this.dataset.get(position).getBuildingType()){
            case 1:
                holder.mBuildingType.setText(R.string.build_type_01);
                break;

            case 2:
                holder.mBuildingType.setText(R.string.build_type_02);
                break;

            case 3:
                holder.mBuildingType.setText(R.string.build_type_03);
                break;

            case 4:
                holder.mBuildingType.setText(R.string.build_type_04);
                break;

            case 5:
                holder.mBuildingType.setText(R.string.build_type_05);
                break;
        }

        holder.mDeleteButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(holder.v.getContext());
            builder.setTitle("Remove Record?");

            View viewInflated = LayoutInflater.from(holder.v.getContext()).inflate(R.layout.dialog_alert, (ViewGroup) view.getParent(), false);

            TextView inf = viewInflated.findViewById(R.id.alert_text);
            inf.setText("Are you sure want to delete?\nCustomer: " + this.dataset.get(position).getCustName());

            builder.setView(viewInflated);

            builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                dialog.dismiss();

                deleteCustomer(position);
            });

            builder.setNegativeButton(R.string.btn_no, (dialog, which) -> {
                dialog.cancel();
            });

            builder.show();
        });
    }

    @Override
    public int getItemCount(){
        return dataset.size();
    }

    private void deleteCustomer(int position) {
        LoadingBox box = new LoadingBox(parent);

        AlertDialog dialog = box.show();
        box.setMessage("Deleting customer...");
        box.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);

        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, parent.getString(R.string.db_service));
        RemoteMongoCollection<Document> collection = client.getDatabase(parent.getString(R.string.db_main)).getCollection("Customer");

        final Task<RemoteDeleteResult> query = collection.deleteOne(new Document().append("_id", dataset.get(position).get_id()));

        query.addOnCompleteListener(task -> {
           if(task.isSuccessful()) {
               box.setMessage("Customer deleted!");
               box.setLoadingType(LoadingBox.MessageType.SUCCESS);
           }
           else {
               box.setMessage("Unable to remove customer!");
               box.setLoadingType(LoadingBox.MessageType.FAILED);
           }

            if(dataListener != null) dataListener.dataDeleted();

            ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
            Runnable r = new AlertDismiss(dialog, svc);

            svc.schedule(r, 3, TimeUnit.SECONDS);
        });
    }

    public void clear() {
        this.dataset.clear();
        this.notifyDataSetChanged();
    }

    public void addAll(List<Customer> dataset) {
        this.dataset = dataset;
        this.notifyDataSetChanged();
    }

    public List<Customer> getDataset() {
        return dataset;
    }

    private class AlertDismiss implements Runnable {
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
}
