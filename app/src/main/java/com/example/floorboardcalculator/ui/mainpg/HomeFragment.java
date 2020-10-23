package com.example.floorboardcalculator.ui.mainpg;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mongodb.MongoClientSettings;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    private final DateFormat format = new SimpleDateFormat("yyyy", Locale.UK);
    private final Date current = new Date();

    private TextView ttl01, ttl02, ttlCount;
    private Button addCustomer;
    private BottomNavigationView navigationView;

    private boolean isDone_1 = false, isDone_2 = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mainpage, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NotNull View parent, Bundle savedInstanceState) {

        ttl01 = (TextView) parent.findViewById(R.id.main_ttl_02);
        ttl02 = (TextView) parent.findViewById(R.id.main_ttl_04);
        ttlCount = (TextView) parent.findViewById(R.id.main_ttl_count);
        addCustomer = (Button) parent.findViewById(R.id.btn_AddNewCustomer);
        navigationView = getView().getRootView().findViewById(R.id.nav_view);

        String recTitle = "Total Customers in Year " + format.format(current);
        ttl01.setText(recTitle);

        addCustomer.setOnClickListener(this::AddButtonClicked);
    }

    @Override
    public void onStart() {
        super.onStart();

        getReport();
    }

    public boolean allowBackPressed() {
        return isDone_1 && isDone_2;
    }

    private void AddButtonClicked(View v) {
        Intent i = new Intent(getContext(), AddNewActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(i);
    }

    private void getReport() {
        isDone_2 = false; isDone_1 = false;
        navigationView.getMenu().setGroupEnabled(R.id.nav_main, false);
        addCustomer.setEnabled(false);

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = new ViewControl(service);
        service.scheduleAtFixedRate(runnable, 0, 500, TimeUnit.MILLISECONDS);

        try {
            Date current = new Date();
            String currentYear = format.format(current);
            Date start = new SimpleDateFormat("yyyy-MM-dd", Locale.UK).parse(currentYear + "-01-01");

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );
            RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
            RemoteMongoCollection<Customer> collection = client.getDatabase(getString(R.string.db_main)).getCollection("Customer", Customer.class).withCodecRegistry(codecRegistry);

            Document dateFilter = new Document().append("AddDate",
                    new Document().append("$gte", start).append("$lt", current)
            );

            Document project = new Document().append("AddDate", 1);

            final Task<Long> query_count = collection.count(dateFilter);
            final Task<List<Customer>> date_query = collection.find().sort(new Document().append("AddDate", -1))
                    .projection(project).limit(1).into(new ArrayList<Customer>());

            query_count.addOnCompleteListener(result_1 -> {
                if(result_1.isSuccessful()) {
                    int customer_count = result_1.getResult().intValue();

                    getActivity().runOnUiThread(() -> {
                        String c = customer_count + " customer(s)";
                        ttlCount.setText(c);
                    });
                }
                else {
                    getActivity().runOnUiThread(() -> {
                        String c = "Data Error!";
                        ttlCount.setText(c);
                    });
                }

                isDone_1 = true;
            });

            date_query.addOnCompleteListener(result_2 -> {
                if(result_2.isSuccessful()) {
                    List<Customer> data = result_2.getResult();

                    getActivity().runOnUiThread(() -> {
                        DateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.UK);

                        ttl02.setText(fm.format(data.get(0).getAddDate()));
                    });
                }
                else {
                    getActivity().runOnUiThread(() -> {
                        ttl02.setText("Data Error!");
                    });
                }

                isDone_2 = true;
            });
        }
        catch(Exception e) {
            Log.e("ERROR", "Problem parsing data!");

            isDone_2 = true; isDone_1 = true;
            navigationView.getMenu().setGroupEnabled(R.id.nav_main, true);
            addCustomer.setEnabled(true);
        }
    }

    private class ViewControl implements Runnable {
        private ScheduledExecutorService service;

        public ViewControl(ScheduledExecutorService service) {
            this.service = service;
        }

        @Override
        public void run() {
            if(isDone_1 && isDone_2) {
                getActivity().runOnUiThread(() -> {
                    navigationView.getMenu().setGroupEnabled(R.id.nav_main, true);
                    addCustomer.setEnabled(true);
                });

                service.shutdown();
            }
        }
    }
}