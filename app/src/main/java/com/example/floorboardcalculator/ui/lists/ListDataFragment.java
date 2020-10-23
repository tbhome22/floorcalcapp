package com.example.floorboardcalculator.ui.lists;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.ui.addon.DialogDismiss;
import com.example.floorboardcalculator.ui.addon.LoadingBox;
import com.example.floorboardcalculator.ui.pagerdetails.CustomerInformationActivity;
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
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ListDataFragment extends Fragment implements ListOnClickListener, ListOnDataListener {
    private static final int QR_REQUEST = 5001;

    private ImageButton mQrScan;
    private EditText mSearch;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ListDataAdapter mListAdpt;
    private LinearLayoutManager mListLayoutMgr;
    private BottomNavigationView navigationView;

    private LoadingBox mLoaderBox;

    private boolean initial = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_listpage, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_item_recycler);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.list_item_slider);
        mQrScan = (ImageButton) view.findViewById(R.id.list_qrScan);
        navigationView = getView().getRootView().findViewById(R.id.nav_view);
        mSearch = (EditText) view.findViewById(R.id.list_searchTxt);

        mRecyclerView.setHasFixedSize(true);

        swipeContainer.setOnRefreshListener(() -> {
            fetchTimelineAsync(null);
        });

        mQrScan.setOnClickListener(this::QrClicked);
        mSearch.setOnKeyListener(this::SearchBoxEnterClicked);
    }

    @Override
    public void onClick(View v, int position) {
        ObjectId pid = mListAdpt.getDataset().get(position).get_id();

        Intent i = new Intent(getContext(), CustomerInformationActivity.class);
        i.putExtra("data_id", pid);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(i);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("initial_load", initial);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if(savedInstanceState != null)
            initial = savedInstanceState.getBoolean("initial_load");
    }

    public boolean allowBackPressed() {
        return !swipeContainer.isRefreshing();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void fetchTimelineAsync(@Nullable String possible) {
        swipeContainer.setRefreshing(true);
        navigationView.getMenu().setGroupEnabled(R.id.nav_main, false);
        mSearch.setEnabled(false);
        mQrScan.setEnabled(false);

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
        RemoteMongoCollection<Customer> collection = client.getDatabase(getString(R.string.db_main)).getCollection("Customer", Customer.class).withCodecRegistry(codecRegistry);

        Document project = new Document().append("CustName", 1).append("AddDate", 1).append("BuildingType", 1).append("_id", 1);

        Instant now = Instant.now();
        Instant date_m60 = now.minus(Duration.ofDays(60));
        Date before = Date.from(date_m60);

        Document dateRange = new Document().append("$gte", before).append("$lt", Date.from(now));

        final Task<List<Customer>> query;

        if(possible != null){
            Document regex = new Document()
                            .append("$regex", "^(?)" + Pattern.quote(possible))
                            .append("$options", "i");

            query = collection.find(new Document().append("CustName", regex).append("AddDate", dateRange)).projection(project).limit(15)
                    .sort(new Document().append("AddDate", -1)).into(new ArrayList<Customer>());
        }
        else{
            query = collection.find(new Document().append("AddDate", dateRange)).projection(project).limit(15).sort(new Document().append("AddDate", -1)).into(new ArrayList<Customer>());
        }

        query.addOnCompleteListener((@NonNull Task<List<Customer>> task) -> {
            if(task.isSuccessful()) {
                List<Customer> list = task.getResult();

                if(list.size() > 0) {
                    if(mListAdpt == null){
                        mListAdpt = new ListDataAdapter(list, getContext());
                        mRecyclerView.setAdapter(mListAdpt);

                        mListLayoutMgr = new LinearLayoutManager(getContext());
                        mRecyclerView.setLayoutManager(mListLayoutMgr);

                        mListAdpt.setClickListener(ListDataFragment.this);
                        mListAdpt.setDataListener(ListDataFragment.this);
                    }
                    else {
                        mListAdpt.clear();
                        mListAdpt.addAll(list);
                        mListAdpt.notifyDataSetChanged();
                    }
                }
            }
            else {
                Log.e("ERROR", task.getException().getMessage());
            }

            navigationView.getMenu().setGroupEnabled(R.id.nav_main, true);
            swipeContainer.setRefreshing(false);
            mQrScan.setEnabled(true);
            mSearch.setEnabled(true);
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if(!initial) {
            fetchTimelineAsync(null);
            initial = true;
        }
    }

    @Override
    public void dataDeleted() {
        fetchTimelineAsync(null);
    }

    @Override
    public void dataAdded() {

    }

    private boolean SearchBoxEnterClicked(View v, int p, KeyEvent event) {
        if(p == KeyEvent.KEYCODE_ENTER) {
            Log.d("Key test", "Enter pressed!");
            mSearch.clearFocus();
            hideKeyboard();

            if(mSearch.getText().length() > 0) {
                fetchTimelineAsync(mSearch.getText().toString());
            }

        }

        return true;
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
    }

    private void QrClicked(View v) {
        Intent i = new Intent(getContext(), QrScanner.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivityForResult(i, QR_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == QR_REQUEST) {
            if(resultCode == Activity.RESULT_OK && data != null) {
                String scanned = data.getStringExtra("RESULT_DATA");

                CodeCheck(scanned);
            }
        }
    }

    private void CodeCheck(@NonNull String scanned) {
        mLoaderBox = new LoadingBox(getContext());
        AlertDialog dialog = mLoaderBox.show();
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        List<Object> dataDismiss = new ArrayList<>();
        dataDismiss.add(dialog);
        Runnable runnable = new DialogDismiss(service, dataDismiss);

        if(ObjectId.isValid(scanned)) {
            mLoaderBox.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);
            mLoaderBox.setMessage("Finding Customer.");

            ObjectId scannedId = new ObjectId(scanned);

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );
            RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
            RemoteMongoCollection<Customer> collection = client.getDatabase(getString(R.string.db_main)).getCollection("Customer", Customer.class).withCodecRegistry(codecRegistry);

            final Task<Long> query = collection.count(new Document().append("_id", scannedId));

            query.addOnCompleteListener((@NonNull Task<Long> task) -> {
                if(task.isSuccessful()) {
                    Long rowCount = task.getResult();
                    int convert = rowCount.intValue();

                    if(convert > 0) {
                        getActivity().runOnUiThread(() -> {
                            Intent i = new Intent(getContext(), CustomerInformationActivity.class);
                            i.putExtra("data_id", scannedId);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                            startActivity(i);

                            service.schedule(runnable, 300, TimeUnit.MILLISECONDS);
                        });
                    }
                    else {
                        mLoaderBox.setLoadingType(LoadingBox.MessageType.FAILED);
                        mLoaderBox.setMessage("Customer Not Found!");

                        service.schedule(runnable, 3, TimeUnit.SECONDS);
                    }
                }
                else {
                    mLoaderBox.setLoadingType(LoadingBox.MessageType.FAILED);
                    mLoaderBox.setMessage("Server Error!");

                    service.schedule(runnable, 3, TimeUnit.SECONDS);
                }
            });
        }
        else {
            mLoaderBox.setLoadingType(LoadingBox.MessageType.FAILED);
            mLoaderBox.setMessage("Invalid QR Code!");

            service.schedule(runnable, 3, TimeUnit.SECONDS);
        }
    }
}