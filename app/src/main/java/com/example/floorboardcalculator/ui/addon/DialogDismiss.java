package com.example.floorboardcalculator.ui.addon;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class DialogDismiss implements Runnable{
    private ScheduledExecutorService service;
    private List<Object> object;

    public DialogDismiss(ScheduledExecutorService service, @NonNull List<Object> object) {
        this.service = service;

        this.object = object;
    }

    @Override
    public void run() {
        for(Object c : object) {
            if(c instanceof Activity) {
                ((Activity) c).finish();
            }

            if(c instanceof FragmentActivity) {
                ((FragmentActivity) c).finish();
            }

            if(c instanceof AlertDialog) {
                ((AlertDialog) c).dismiss();
            }
        }

        service.shutdown();
    }
}

