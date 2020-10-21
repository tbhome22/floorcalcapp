package com.example.floorboardcalculator.ui.lists;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;
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

import java.util.ArrayList;
import java.util.List;

public class ListDataFragment extends Fragment implements ListOnClickListener, ListOnDataListener {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeContainer;
    private ListDataAdapter mListAdpt;
    private LinearLayoutManager mListLayoutMgr;
    private Thread LoadThread;
    private BottomNavigationView navigationView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_listpage, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_item_recycler);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.list_item_slider);

        navigationView = getView().getRootView().findViewById(R.id.nav_view);

        mRecyclerView.setHasFixedSize(true);

        swipeContainer.setOnRefreshListener(() -> {
            fetchTimelineAsync();
        });
    }

    @Override
    public void onClick(View v, int position) {
        ObjectId pid = mListAdpt.getDataset().get(position).get_id();

        Intent i = new Intent(getContext(), CustomerInformationActivity.class);
        i.putExtra("data_id", pid);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(i);
    }

    public boolean allowBackPressed() {
        return !swipeContainer.isRefreshing();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void fetchTimelineAsync() {
        swipeContainer.setRefreshing(true);
        navigationView.getMenu().setGroupEnabled(R.id.nav_main, false);

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );
        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
        RemoteMongoCollection<Customer> collection = client.getDatabase(getString(R.string.db_main)).getCollection("Customer", Customer.class).withCodecRegistry(codecRegistry);

        Document project = new Document().append("CustName", 1).append("AddDate", 1).append("BuildingType", 1).append("_id", 1);

        final Task<List<Customer>> query = collection.find().projection(project).limit(20).sort(new Document().append("AddDate", -1)).into(new ArrayList<Customer>());

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
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        fetchTimelineAsync();
    }

    @Override
    public void dataDeleted() {
        fetchTimelineAsync();
    }

    @Override
    public void dataAdded() {

    }
}