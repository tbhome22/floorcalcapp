package com.example.floorboardcalculator.ui.pagerdetails.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.datamodel.Customer;
import com.example.floorboardcalculator.ui.addon.DialogDismiss;
import com.example.floorboardcalculator.ui.addon.LoadingBox;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotesFragment extends Fragment implements Callable{
    private TextView mNotes;
    private ImageButton mEditButton, mCancelEdit;
    private EditText mEditBox;
    private AlertDialog dialog;
    private LoadingBox loadingBox;

    private InformationProcess dataProcess;
    private Customer data;

    private boolean editMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detailpage_05, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadingBox = new LoadingBox(view.getContext());

        mNotes = (TextView) view.findViewById(R.id.detail_notes);
        mEditButton = (ImageButton) view.findViewById(R.id.btn_noteEdit);
        mCancelEdit = (ImageButton) view.findViewById(R.id.btn_cancelEdit);
        mEditBox = (EditText) view.findViewById(R.id.detail_editNotes);

        processDone();

        mEditButton.setOnClickListener(this::editButtonClicked);
        mCancelEdit.setOnClickListener(this::discardEdit);
    }

    public void setDataProcess(InformationProcess dataProcess) {
        this.dataProcess = dataProcess;
    }

    @Override
    public void processDone() {
        data = dataProcess.getData();

        if(getView() != null && data != null) {
            if(data.getNotes().length() > 0) {
                mNotes.setText(data.getNotes());
            }
            else {
                mNotes.setText("NO DEFINE");
            }
        }
    }

    @Override
    public void processFailed() {

    }

    @Override
    public void processInRefresh() {
        if(mNotes != null)
            mNotes.setText(R.string.loading);
    }

    private void editButtonClicked(View v) {
        if(editMode) {
            String boxText = mEditBox.getText().toString();

            editMode = false;
            mCancelEdit.setVisibility(View.GONE);
            mEditButton.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.ic_edit));
            mNotes.setVisibility(View.VISIBLE);
            mEditBox.setVisibility(View.GONE);
            mEditBox.getText().clear();

            performEdit(boxText);
        }
        else {
            editMode = true;
            mCancelEdit.setVisibility(View.VISIBLE);
            mEditButton.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.ic_done));
            mNotes.setVisibility(View.GONE);
            mEditBox.setVisibility(View.VISIBLE);
            mEditBox.setText(data.getNotes());
        }
    }

    private void discardEdit(@NotNull View v) {
        editMode = false;
        mEditBox.getText().clear();
        mEditBox.setVisibility(View.GONE);
        mNotes.setVisibility(View.VISIBLE);
        mCancelEdit.setVisibility(View.GONE);
        mEditButton.setImageDrawable(ContextCompat.getDrawable(v.getContext(), R.drawable.ic_edit));
    }

    private void performEdit(@NotNull String text) {
        if(text.equals(data.getNotes())) {
            return;
        }

        AlertDialog load = loadingBox.show();
        loadingBox.setMessage("Editing Notes...");
        loadingBox.setLoadingType(LoadingBox.MessageType.IN_PROGRESS);
        final String newText;

        if(text.length() == 0)
            newText = "";
        else
            newText = text;

        RemoteMongoClient client = Stitch.getDefaultAppClient().getServiceClient(RemoteMongoClient.factory, getString(R.string.db_service));
        RemoteMongoCollection<Document> dbCollection = client.getDatabase(getString(R.string.db_main)).getCollection("Customer");

        Document filter = new Document().append("_id", data.get_id());
        Document updateDoc = new Document().append("$set", new Document().append("Notes", newText).append("Exported", false));

        final Task<RemoteUpdateResult> query = dbCollection.updateOne(filter, updateDoc);

        query.addOnCompleteListener(result -> {
            if(!result.isSuccessful()) {
                loadingBox.setMessage("Failed to update notes!");
                loadingBox.setLoadingType(LoadingBox.MessageType.FAILED);
            }

            dataProcess.notifyRefresh();

            ScheduledExecutorService svc = Executors.newScheduledThreadPool(1);
            List<Object> dismissObj = new ArrayList<>();
            dismissObj.add(load);
            Runnable r = new DialogDismiss(svc, dismissObj);

            svc.schedule(r, 600, TimeUnit.MILLISECONDS);
        });
    }

}
