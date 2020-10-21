package com.example.floorboardcalculator.ui.addon;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.datamodel.FloorType;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RateDialog extends AlertDialog.Builder {
    private static final String APP_PREF = "TWO_BROTHER_SETTING";

    private EditText centralName, price1, price2, price3;
    private LinearLayout loadingNotify;
    private PreferenceItem setting;
    private FloorType floor;
    private String beforeText = "";
    private OnDialogResultListener listener;
    private Button posButton, negButton;
    private AlertDialog createdDialog;

    public RateDialog(Context context) {
        super(context);

        initSetting();
    }

    public RateDialog(Context context, @Nullable FloorType floorType) {
        super(context);

        this.floor = floorType;

        initSetting();
    }

    public void setOnResultListener(OnDialogResultListener listener) {
        this.listener = listener;
    }

    @SuppressLint("InflateParams")
    @Override
    public AlertDialog show() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_addrate, null);
        setView(v);
        setCancelable(false);

        centralName = (EditText) v.findViewById(R.id.box_productName);
        price1 = (EditText) v.findViewById(R.id.box_section1_price);
        price2 = (EditText) v.findViewById(R.id.box_section2_price);
        price3 = (EditText) v.findViewById(R.id.box_section3_price);
        loadingNotify = (LinearLayout) v.findViewById(R.id.loading_loader);

        if(floor != null) {
            setTitle("Edit Product Rate");
            centralName.setEnabled(false);
            centralName.setText(floor.getFull());
            price1.setText(String.format("%.2f", floor.getBase()));
            price2.setText(String.format("%.2f", floor.getBase_8()));
            price3.setText(String.format("%.2f", floor.getBase_15()));
        }
        else {
            setTitle("Add New Product");
        }

        setPositiveButton(R.string.btn_ok, this::OKDialogClicked);
        setNegativeButton(R.string.btn_cancel, this::CancelDialogClick);

        centralName.setOnFocusChangeListener(this::NameEditTextFocusChange);
        price1.setOnFocusChangeListener(this::PriceEditTextFocusChange);
        price2.setOnFocusChangeListener(this::PriceEditTextFocusChange);
        price3.setOnFocusChangeListener(this::PriceEditTextFocusChange);

        super.create();

        AlertDialog diag = super.show();

        diag.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(this::OKDialogClick2);
        posButton = diag.getButton(AlertDialog.BUTTON_POSITIVE);
        posButton.setEnabled(false);
        negButton = diag.getButton(AlertDialog.BUTTON_NEGATIVE);

        createdDialog = diag;

        return diag;
    }

    /* Leave empty for this */
    private void OKDialogClicked(DialogInterface dialogInterface, int which) {}

    private void OKDialogClick2(View v) {
        disableControl();
        loadingNotify.setVisibility(View.VISIBLE);

        if(floor != null)
            updateRecord();
        else
            addRecord();
    }

    private void allowProcess() {
        String n = centralName.getText().toString();
        String p1 = price1.getText().toString();
        String p2 = price2.getText().toString();
        String p3 = price3.getText().toString();

        if(n.length() < 6 || p1.length() == 0 || p2.length() == 0 || p3.length() == 0)
            posButton.setEnabled(false);
        else
            posButton.setEnabled(true);
    }

    private void CancelDialogClick(@NotNull DialogInterface dialogInterface, int which) {
        dialogInterface.cancel();
    }

    private void PriceEditTextFocusChange(View view, boolean isFocus) {
        if(!isFocus) {
            EditText editor = (EditText) view;
            try{
                double val = Double.parseDouble(editor.getText().toString());

                editor.setText(String.format("%.2f", val));
            }
            catch(Exception ignored) {}

            allowProcess();
        }
    }

    private void NameEditTextFocusChange(View view, boolean isFocus) {
        if(!isFocus) {
            allowProcess();
        }
    }

    private void initSetting() {
        SharedPreferences preferences = getContext().getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        setting = new PreferenceItem();

        setting.setUnit(preferences.getBoolean("unit", false));
        setting.setDoubleUnit(preferences.getBoolean("doubleUnit", false));
        setting.setUnitSelect(preferences.getInt("unitSelect", -1));
        setting.setPriceExp(preferences.getBoolean("priceExp", true));
    }

    private void addRecord() {
        String n = centralName.getText().toString();
        String p1 = price1.getText().toString();
        String p2 = price2.getText().toString();
        String p3 = price3.getText().toString();

        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getContext().getString(R.string.db_service));
        RemoteMongoCollection<Document> collection = client.getDatabase(getContext().getString(R.string.db_main)).getCollection("FloorType");

        Document document = new Document()
                .append("abbr", generateAbbr(n))
                .append("full", n)
                .append("base", Double.parseDouble(p1))
                .append("base_8", Double.parseDouble(p2))
                .append("base_15", Double.parseDouble(p3));

        final Task<RemoteInsertOneResult> task = collection.insertOne(document);

        task.addOnCompleteListener(resultTask -> {
            if(resultTask.isSuccessful()) {
                Log.d("NEW RATE", "SUCCESS ADD NEW RATE!");
                if(listener != null) listener.onResultDone(createdDialog, true);
            }
            else {
                Log.e("ERROR", "Failed to add new rate!");
                if(listener != null) listener.onResultDone(createdDialog, false);
            }
        });
    }

    private void updateRecord() {
        Document document = new Document();
        boolean dataNeedUpdate = false;

        double p1 = Double.parseDouble(price1.getText().toString());
        double p2 = Double.parseDouble(price2.getText().toString());
        double p3 = Double.parseDouble(price3.getText().toString());

        if(p1 != floor.getBase()) {
            document.append("base", p1);
            dataNeedUpdate = true;
        }

        if(p2 != floor.getBase_8()) {
            document.append("base_8", p2);
            dataNeedUpdate = true;
        }

        if(p3 != floor.getBase_15()) {
            document.append("base_15", p3);
            dataNeedUpdate = true;
        }

        if(dataNeedUpdate) {
            RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getContext().getString(R.string.db_service));
            RemoteMongoCollection<Document> collection = client.getDatabase(getContext().getString(R.string.db_main)).getCollection("FloorType");

            Document filter = new Document().append("_id", floor.get_id());

            final Task<RemoteUpdateResult> query = collection.updateOne(filter, new Document().append("$set", document));

            query.addOnCompleteListener(resultTask -> {
                if(resultTask.isSuccessful()) {
                    Log.d("NEW RATE", "SUCCESS EDIT PRODUCT RATE!");
                    if(listener != null) listener.onResultDone(createdDialog, true);
                }
                else {
                    Log.e("ERROR", "FAILED TO EDIT RATE!");
                    if(listener != null) listener.onResultDone(createdDialog, false);
                }
            });
        }
        else {
            if(listener != null) listener.onResultDone(createdDialog, false);
        }
    }

    @NotNull
    private String generateAbbr(@NotNull String fullName) {
        String[] sp = fullName.split(" ");
        String text = "";

        for(String w : sp) {
            text += Character.toLowerCase(w.charAt(0));
        }

        if(text.length() < 3) {
            text = new StringBuilder().append(fullName.toLowerCase().charAt(0)).append(fullName.toLowerCase().charAt(1)).append(fullName.toLowerCase().charAt(2)).toString();
        }

        return text;
    }

    private void disableControl() {
        if(floor == null)
            centralName.setEnabled(false);

        price1.setEnabled(false);
        price2.setEnabled(false);
        price3.setEnabled(false);
        posButton.setEnabled(false);
        negButton.setEnabled(false);
    }
}
