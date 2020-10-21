package com.example.floorboardcalculator.ui.mainpg.addFragment;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AlertDialog;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.constant.BuildingType;
import com.example.floorboardcalculator.core.constant.StateList;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.core.datamodel.FloorPlan;
import com.example.floorboardcalculator.ui.addon.LoadingBox;
import com.google.android.gms.tasks.Task;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Confirmation extends StepDynamicFragment {

    private @LayoutRes int resourceId;
    private LinearLayout errorMsg;
    private LinearLayout confMsg;
    private TextView noticeCheck;
    private CheckBox checkConfirm;
    private Button btnSubmit;

    private TextView txt_CustName, txt_buildType, txt_contact, txt_address, txt_city, txt_postcode, txt_state,
            txt_areaMeasure, txt_ttlAreaInput, txt_referral, txt_curvedArea, txt_skirtLen, txt_location;


    public Confirmation(int resourceId, int position) {
        super(resourceId, position);

        this.resourceId = resourceId;
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        confMsg = (LinearLayout) v.findViewById(R.id.add_confirmInformation);
        errorMsg = (LinearLayout) v.findViewById(R.id.add_errorMsg);
        noticeCheck = (TextView) v.findViewById(R.id.notice_check);
        checkConfirm = (CheckBox) v.findViewById(R.id.add_checkConfirm);

        txt_CustName = (TextView) v.findViewById(R.id.add_detail_custName);
        txt_buildType = (TextView) v.findViewById(R.id.add_detail_buildType);
        txt_contact = (TextView) v.findViewById(R.id.add_detail_contactNo);
        txt_address = (TextView) v.findViewById(R.id.add_detail_fullAddr);
        txt_city = (TextView) v.findViewById(R.id.add_detail_city);
        txt_postcode = (TextView) v.findViewById(R.id.add_detail_postalCode);
        txt_state = (TextView) v.findViewById(R.id.add_detail_state);
        txt_areaMeasure = (TextView) v.findViewById(R.id.add_detail_totalArea);
        txt_ttlAreaInput = (TextView) v.findViewById(R.id.add_detail_totalAreaInput);
        txt_referral = (TextView) v.findViewById(R.id.add_detail_referral);
        txt_curvedArea = (TextView) v.findViewById(R.id.add_detail_curvedArea);
        txt_skirtLen = (TextView) v.findViewById(R.id.add_detail_skirtLen);
        txt_location = (TextView) v.findViewById(R.id.add_detail_mapPin);
        btnSubmit = (Button) v.findViewById(R.id.btn_submitData);

        wizardListener.onStateUpdated(resourceId, false);

        noticeCheck.setText(Html.fromHtml(context.getString(R.string.add_notice), Html.FROM_HTML_MODE_COMPACT));
        checkConfirm.setOnCheckedChangeListener(this::agreeCheckedChange);
        checkConfirm.setChecked(false);
        btnSubmit.setOnClickListener(this::submitClicked);
        btnSubmit.setEnabled(false);
    }

    public void notifyDataUpdated(boolean valid) {
        if(valid) {
            errorMsg.setVisibility(View.GONE);
            confMsg.setVisibility(View.VISIBLE);

            if(wizardDataProcess != null)
                customer = wizardDataProcess.onDataGet();
            else
                customer = new Customer();

            initValue();
        }
        else {
            errorMsg.setVisibility(View.VISIBLE);
            confMsg.setVisibility(View.GONE);
        }
    }

    private void agreeCheckedChange(CompoundButton compoundButton, boolean b) {
        if(b)
            wizardListener.onStateUpdated(resourceId, true);
        else
            wizardListener.onStateUpdated(resourceId, false);

        btnSubmit.setEnabled(b);
    }

    private void initValue() {
        int areaCalc = 0;
        double totalAreaInFsq = 0.0, totalAreaInMsq = 0.0;
        double skirtLenMetre, skirtLenFeet;

        txt_CustName.setText(customer.getCustName());
        txt_referral.setText((customer.getReferral().equals("-"))?"No Referral":customer.getReferral());
        txt_contact.setText(customer.getContactNo());
        txt_address.setText(customer.getAddress());
        txt_city.setText(customer.getCity());
        txt_postcode.setText(customer.getPostalCode());
        txt_buildType.setText(BuildingType.getType(customer.getBuildingType() - 1));
        txt_state.setText(StateList.getState(customer.getState()));
        txt_curvedArea.setText((customer.getFloorInf().isCurvedArea())?"Yes" : "No");
        txt_location.setText("Latitude: " + customer.getLat() + "\nLongitude: " + customer.getLng());

        for(FloorPlan plan : customer.getFloorPlan()) {
            totalAreaInFsq += ((plan.getWidth() * plan.getHeight()) / 929f);
            totalAreaInMsq += ((plan.getWidth() * plan.getHeight()) / 10000f);

            areaCalc++;
        }

        skirtLenMetre = customer.getFloorInf().skirtingLen / 100f;
        skirtLenFeet = customer.getFloorInf().skirtingLen / 30.48f;

        StringBuilder txtAM = new StringBuilder(String.format("%,.2f", totalAreaInMsq)).append(context.getString(R.string.metreSq))
                .append(" / ").append(String.format("%,.1f", totalAreaInFsq)).append(context.getString(R.string.feetSq));

        txt_areaMeasure.setText(Html.fromHtml(txtAM.toString() ,Html.FROM_HTML_MODE_COMPACT));
        txt_ttlAreaInput.setText(areaCalc + " area(s)");
        txt_skirtLen.setText(String.format("%.2f", skirtLenMetre) + "m / " + String.format("%.1f", skirtLenFeet) + "ft");
    }

    public void submitClicked(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Proceed?");
        builder.setMessage("Proceed to add?");

        builder.setPositiveButton(R.string.btn_yes, (dialog, which) -> {
            dialog.dismiss();

            submitCustomer();
        });

        builder.setNegativeButton(R.string.btn_no, (dialog, which) -> {
            dialog.cancel();
        });

        builder.create().show();
    }

    private void submitCustomer() {
        LoadingBox box = new LoadingBox(context);
        AlertDialog dialog = box.show();
        box.setMessage("Adding Customer...");
        box.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);

        Date current = new Date();

        if(customer != null) {

            customer.setAddDate(current);

            RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, context.getString(R.string.db_service));
            RemoteMongoCollection<Document> collection = client.getDatabase(context.getString(R.string.db_main)).getCollection("Customer");

            List<BasicDBObject> data = new ArrayList<>();
            customer.getFloorPlan().forEach(i -> {
                DBObject obj = new BasicDBObject();
                obj.put("name", i.name);
                obj.put("width", i.width);
                obj.put("height", i.height);

                data.add((BasicDBObject) obj);
            });

            DBObject obj2 = new BasicDBObject();
            obj2.put("skirtingLen", customer.getFloorInf().getSkirtingLen());
            obj2.put("curvedArea", customer.getFloorInf().isCurvedArea());

            Document newDoc = new Document()
                    .append("CustName", customer.getCustName())
                    .append("AddDate", customer.getAddDate())
                    .append("BuildingType", customer.getBuildingType())
                    .append("Address", customer.getAddress())
                    .append("City", customer.getCity())
                    .append("State", customer.getState())
                    .append("PostalCode", customer.getPostalCode())
                    .append("ContactNo", customer.getContactNo())
                    .append("Referral", customer.getReferral())
                    .append("Lat", customer.getLat())
                    .append("Lng", customer.getLng())
                    .append("FloorPlan", data)
                    .append("FloorInf", obj2);

            final Task<RemoteInsertOneResult> query = collection.insertOne(newDoc);

            query.addOnCompleteListener(result -> {
                if(result.isSuccessful()) {
                    box.setMessage("New customer added!");
                    box.setLoadingType(LoadingBox.MessageType.SUCCESS);
                }
                else {
                    box.setMessage("Add customer failed!");
                    box.setLoadingType(LoadingBox.MessageType.FAILED);
                }

                ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
                Runnable r = new DismissAlert(dialog, svc);

                svc.schedule(r, 3, TimeUnit.SECONDS);
            });
        }
        else {
            box.setMessage("Data Error!");
            box.setLoadingType(LoadingBox.MessageType.FAILED);

            ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
            Runnable r = new DismissAlert(dialog, svc);

            svc.schedule(r, 3, TimeUnit.SECONDS);
        }
    }

    private class DismissAlert implements Runnable {
        private AlertDialog dialog;
        private ScheduledExecutorService svc;

        public DismissAlert(AlertDialog dialog, ScheduledExecutorService svc) {
            this.dialog = dialog;
            this.svc = svc;
        }

        @Override
        public void run(){
            dialog.dismiss();
            svc.shutdown();
            activity.finish();
        }
    }
}
