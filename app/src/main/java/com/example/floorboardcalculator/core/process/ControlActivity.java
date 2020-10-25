package com.example.floorboardcalculator.core.process;

import android.util.Log;
import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.floorboardcalculator.MainActivity;
import com.example.floorboardcalculator.ui.addon.InternetDialog;
import com.example.floorboardcalculator.ui.addon.InternetStatus;

import org.jetbrains.annotations.NotNull;

public abstract class ControlActivity extends AppCompatActivity {
    protected AlertDialog statusDialogAlert = null;
    protected InternetDialog statusDialog = null;

    @Override
    protected void onStart() {
        super.onStart();

        if(this instanceof MainActivity) {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    public abstract @Nullable Menu getMenu();

    public void tickConnectionStatus(@NotNull InternetStatus status, int counter) {
        runOnUiThread(() -> {
            switch (status) {
                case INTERNET_OK:
                    if(statusDialogAlert != null) {
                        statusDialog.setStatus(InternetStatus.INTERNET_OK);

                        if(statusDialogAlert.isShowing())
                            statusDialogAlert.dismiss();

                        statusDialogAlert = null;
                        statusDialog = null;

                        Log.d("CONNECTION", "Connected!");
                    }

                    break;

                case INTERNET_NOCONNECTED:
                case INTERNET_NOIP:
                    if (statusDialogAlert == null) {
                        statusDialog = new InternetDialog(this);
                        statusDialogAlert = statusDialog.show();

                        Log.d("CONNECTION", "Disconnected!");
                    }

                    statusDialog.setStatus(status, counter);
                    break;
            }
        });
    }
}
