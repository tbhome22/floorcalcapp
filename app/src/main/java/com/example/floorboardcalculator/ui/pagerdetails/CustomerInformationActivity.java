package com.example.floorboardcalculator.ui.pagerdetails;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.config.PreferenceItem;
import com.example.floorboardcalculator.core.datamodel.Config;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.core.datamodel.FloorType;
import com.example.floorboardcalculator.ui.mainpg.ZoomAnimation;
import com.example.floorboardcalculator.ui.pagerdetails.fragments.PagerListener;
import com.example.floorboardcalculator.ui.pdf.PDFDoneListener;
import com.example.floorboardcalculator.ui.pdf.PDFExporter;
import com.example.floorboardcalculator.ui.pdf.PrintProcessAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mongodb.MongoClientSettings;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomerInformationActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, PagerListener {
    private static final String TAG = CustomerInformationActivity.class.getSimpleName();
    private static final String APP_PREF = "TWO_BROTHER_SETTING";

    private TabLayout mainTab;
    private ViewPager2 pager2;
    private Button btnPrint, btnBack, btnSelectOk;
    private SwipeRefreshLayout refreshLayout;

    private InformationAdapter adapter;
    private PreferenceItem setting;
    private Customer currentData;
    private Config configData;
    private List<FloorType> products;
    private List<String> exportSelected;

    private ObjectId dataId;
    private boolean inProcess1 = false, inProcess2 = false, inProcess3 = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerdetails);

        initSetting();

        dataId = (ObjectId) getIntent().getSerializableExtra("data_id");

        if(dataId == null) {
            Toast.makeText(this, "Invalid data!", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            mainTab = (TabLayout) findViewById(R.id.detail_tabLayout);
            pager2 = (ViewPager2) findViewById(R.id.detail_pager);
            btnBack = (Button) findViewById(R.id.btn_back);
            btnPrint = (Button) findViewById(R.id.btn_print);
            refreshLayout = (SwipeRefreshLayout) findViewById(R.id.cust_Details_swipe);

            initPager();

            refreshLayout.setOnRefreshListener(this);

            btnBack.setOnClickListener(this::onBackClicked);
            btnPrint.setOnClickListener(this::onPrintClicked);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        this.onRefresh();
    }

    @Override
    public void onRefresh() {
        refreshLayout.setEnabled(true);
        refreshLayout.setRefreshing(true);
        fetchDataAsync();
    }

    @Override
    public void callRefresh() {
        this.onRefresh();
    }

    @Override
    public void onBackPressed() {
        if(pager2.getCurrentItem() > 0)
            pager2.setCurrentItem(pager2.getCurrentItem() - 1);
        else {
            if(inProcess1 || inProcess2 || inProcess3)
                Toast.makeText(this, "Please wait process still loading.", Toast.LENGTH_SHORT).show();
            else
                super.onBackPressed();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        this.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onPrintClicked(View v) {
        if(setting.isPriceExp()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View alertInflated = LayoutInflater.from(this).inflate(R.layout.dialog_selectprice, (ViewGroup) v.getParent(), false);
            LinearLayout selectRadio = (LinearLayout) alertInflated.findViewById(R.id.select_radio_list);

            exportSelected = new ArrayList<String>();

            builder.setView(alertInflated);

            builder.setTitle("Select Product to Export");

            for(FloorType type : products) {
                CheckBox option = new CheckBox(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                option.setLayoutParams(params);
                option.setText(type.getFull());
                option.setOnCheckedChangeListener(this::productSelected);

                selectRadio.addView(option);
            }


            builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                List<FloorType> selectedTypes = new ArrayList<FloorType>();

                for(FloorType t : products) {
                    for(String s : exportSelected) {
                        if(t.getFull().equals(s)) {
                            selectedTypes.add(t);
                            break;
                        }
                    }
                }

                new PDFExporter(this, currentData, new PdfGenerated(), selectedTypes, configData).generate();
            });

            builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {dialog.cancel(); exportSelected = null;});

            AlertDialog dialog = builder.show();

            btnSelectOk = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            btnSelectOk.setEnabled(false);
        }
        else {
            new PDFExporter(this, currentData, new PdfGenerated()).generate();
        }
    }

    private void productSelected(CompoundButton compoundButton, boolean isChecked) {
        if(isChecked) {
            String full = compoundButton.getText().toString();

            for(FloorType type : products) {
                if(type.getFull().equals(full)) {
                    exportSelected.add(type.getFull());
                    break;
                }
            }
        }
        else {
            int position = 0; boolean found = false;

            for(String type : exportSelected) {
                if(type.equals(compoundButton.getText().toString())) {
                    found = true;
                    break;
                }

                position++;
            }

            if(found)
                exportSelected.remove(position);
        }

        if(exportSelected.size() > 0)
            btnSelectOk.setEnabled(true);
        else
            btnSelectOk.setEnabled(false);
    }

    private void onBackClicked(View v) {
        if(inProcess1 || inProcess2 || inProcess3)
            Toast.makeText(this, "Please wait process still loading.", Toast.LENGTH_SHORT).show();
        else
            super.onBackPressed();
    }

    private void initPager() {
        adapter = new InformationAdapter(this, setting);
        adapter.setPagerListener(this);

        pager2.setAdapter(adapter);
        pager2.setPageTransformer(new ZoomAnimation());

        mainTab.setTabMode(TabLayout.MODE_SCROLLABLE);

        new TabLayoutMediator(mainTab, pager2, (tab, position) ->
            tab.setText(adapter.getFragmentTitle(position))
        ).attach();
    }

    private void fetchDataAsync() {
        btnPrint.setEnabled(false);
        btnBack.setEnabled(false);
        inProcess1 = true;
        inProcess2 = true;
        inProcess3 = true;
        adapter.notifyUpdating();

        ScheduledExecutorService svc = Executors.newSingleThreadScheduledExecutor();
        Runnable r = new AwaitProcess(svc);

        svc.scheduleAtFixedRate(r, 0, 500, TimeUnit.MILLISECONDS);

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));

        RemoteMongoCollection<Customer> collection_customer = client.getDatabase(getString(R.string.db_main)).getCollection("Customer", Customer.class).withCodecRegistry(codecRegistry);
        RemoteMongoCollection<Config> collection_config = client.getDatabase(getString(R.string.db_main)).getCollection("Config", Config.class).withCodecRegistry(codecRegistry);
        RemoteMongoCollection<FloorType> collection_product = client.getDatabase(getString(R.string.db_main)).getCollection("FloorType", FloorType.class).withCodecRegistry(codecRegistry);

        final Task<Customer> query_customer = collection_customer.findOne(new Document().append("_id", dataId));

        query_customer.addOnCompleteListener(result -> {
            if(result.isSuccessful()) {
                currentData = result.getResult();

                adapter.setCustomerData(currentData);

                final Task<Config> query_config = collection_config.findOne(new Document().append("configName", "rate_control"));

                query_config.addOnCompleteListener(result2 -> {
                    if(result2.isSuccessful()) {
                        configData = result2.getResult();

                        adapter.setConfigData(configData);
                    }
                    else {
                        Toast.makeText(this, "Error getting customer information!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    inProcess2 = false;
                });

                final Task<List<FloorType>> query_floorType = collection_product.find().into(new ArrayList<FloorType>());

                query_floorType.addOnCompleteListener(result3 -> {
                    if(result3.isSuccessful()) {
                        products = result3.getResult();

                        adapter.setProductData(products);
                    }
                    else {
                        Toast.makeText(this, "Error getting customer information!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    inProcess3 = false;
                });
            }
            else {
                Toast.makeText(this, "Error getting customer information!", Toast.LENGTH_SHORT).show();
                finish();
            }

            inProcess1 = false;
        });
    }

    private void initSetting() {
        SharedPreferences preferences = getSharedPreferences(APP_PREF, MODE_PRIVATE);
        setting = new PreferenceItem();

        setting.setUnit(preferences.getBoolean("unit", false));
        setting.setDoubleUnit(preferences.getBoolean("doubleUnit", false));
        setting.setUnitSelect(preferences.getInt("unitSelect", -1));
        setting.setPriceExp(preferences.getBoolean("priceExp", true));
    }

    public class AwaitProcess implements Runnable {
        private ScheduledExecutorService scheduler;

        public AwaitProcess(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public void run() {
            if(!inProcess1 && !inProcess2 && !inProcess3) {
                runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);
                    refreshLayout.setEnabled(false);
                    btnBack.setEnabled(true);
                    btnPrint.setEnabled(true);
                });

                scheduler.shutdown();
            }
        }
    }

    public class PdfGenerated implements PDFDoneListener {
        @Override
        public void onPdfDone(String fileName, String url) {
            DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.UK);

            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            String jobName = "checklist_" + format.format(new Date());
            printManager.print(jobName, new PrintProcessAdapter(CustomerInformationActivity.this, fileName), new PrintAttributes.Builder().build());

            RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
            RemoteMongoCollection<Document> collection_customer = client.getDatabase(getString(R.string.db_main)).getCollection("Customer");

            Document filter = new Document().append("_id", dataId);
            Document updateDoc = new Document().append("$set", new Document().append("Exported", true));

            collection_customer.updateOne(filter, updateDoc);
        }
    }
}
