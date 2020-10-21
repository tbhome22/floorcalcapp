package com.example.floorboardcalculator.ui.addon;

import androidx.appcompat.app.AlertDialog;

public interface OnDialogResultListener {
    void onResultDone(AlertDialog dialog, boolean refreshNeeded);
}
