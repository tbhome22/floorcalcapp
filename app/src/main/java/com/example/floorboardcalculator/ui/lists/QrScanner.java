package com.example.floorboardcalculator.ui.lists;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.floorboardcalculator.R;
import com.example.floorboardcalculator.core.process.ControlActivity;
import com.example.floorboardcalculator.ui.addon.InternetStatus;
import com.google.zxing.Result;

import org.jetbrains.annotations.NotNull;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class QrScanner extends ControlActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private ImageButton mFlashToggle;
    private Result scannedResult = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qrscan);

        mFlashToggle = (ImageButton) findViewById(R.id.qr_flashlight);
        mScannerView = (ZXingScannerView) findViewById(R.id.qr_main);
        mScannerView.setResultHandler(this);

        mScannerView.startCamera();

        mFlashToggle.setOnClickListener(this::shiftFlash);
    }

    @Override
    public void onDestroy(){
        if(isFinishing()) {
            if(scannedResult == null) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("RESULT_DATA", "");
                setResult(RESULT_CANCELED, returnIntent);
            }
        }

        mScannerView.setFlash(false);
        mScannerView.stopCamera();
        super.onDestroy();
    }

    private void shiftFlash(View v) {
        if(mScannerView.getFlash()){
            mScannerView.setFlash(false);
            mFlashToggle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash_off));
        }
        else {
            mScannerView.setFlash(true);
            mFlashToggle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash_on));
        }
    }

    @Override
    public void handleResult(@NotNull Result result) {
        beep();

        this.scannedResult = result;
        final String data = result.getText();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("RESULT_DATA", data);
        setResult(RESULT_OK, returnIntent);

        finish();
    }

    private void beep() {
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
    }

    @Nullable
    @Override
    public Menu getMenu() {
        return null;
    }

    @Override
    public void tickConnectionStatus(InternetStatus status, int counter) {
        super.tickConnectionStatus(status, counter);
    }
}
