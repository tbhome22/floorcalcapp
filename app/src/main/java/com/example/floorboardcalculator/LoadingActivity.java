package com.example.floorboardcalculator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.mongodb.stitch.android.core.Stitch;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadingActivity extends AppCompatActivity {
    private static final String APP_PREF = "TWO_BROTHER_SETTING";
    private static final int PERMISSION_READ_STOR = 1000;
    private static final int PERMISSION_ACCESS_FINE_LOC = 1001;
    private static final int PERMISSION_READ_PHONE_STATE = 1002;
    private static final int PERMISSION_CAMERA_ACCESS = 1003;

    private TextView loadingText;
    private SharedPreferences preferences;
    private boolean isDone = false, hasError = false;
    private boolean[] permissionDone = new boolean[] {false, false, false, false};
    private boolean[] permissionCalled = new boolean[] {false, false, false, false};
    private boolean[] permissionSuccess = new boolean[] {false, false, false, false};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_loading);

        loadingText = findViewById(R.id.txt_loadingtxt);
    }

    @Override
    public void onResume() {
        super.onResume();

        Thread testThread = new Thread(new LoadTest());

        testThread.start();

        ScheduledExecutorService svc = Executors.newSingleThreadScheduledExecutor();
        Runnable r = new ScheduleCheck(svc);

        svc.scheduleAtFixedRate(r, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onPause() {
        setContentView(R.layout.black_screen);

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_STOR:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    permissionSuccess[0] = true;
                }

                permissionDone[0] = true;
                break;

            case PERMISSION_ACCESS_FINE_LOC:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    permissionSuccess[1] = true;
                }

                permissionDone[1] = true;
                break;

            case PERMISSION_READ_PHONE_STATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    permissionSuccess[2] = true;
                }

                permissionDone[2] = true;
                break;

            case PERMISSION_CAMERA_ACCESS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    permissionSuccess[3] = true;
                }

                permissionDone[3] = true;
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public class ScheduleCheck implements Runnable {
        private ScheduledExecutorService service;

        public ScheduleCheck(ScheduledExecutorService service) {
            this.service = service;
        }

        @Override
        public void run() {
            if(isDone) {
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent);

                LoadingActivity.this.finish();

                service.shutdown();
            }
            else {
                if(hasError) {
                    LoadingActivity.this.finish();
                }
            }
        }
    }

    public class LoadTest implements Runnable {
        private TestType currentTest;
        private int conn_retries = 0;

        @Override
        public void run() {
            currentTest = TestType.DELAY;

            try {
                while (true) {
                    switch (currentTest) {
                        case DELAY:
                            runOnUiThread(() -> loadingText.setText("Starting Application"));
                            Thread.sleep(2000);
                            currentTest = TestType.INIT;
                            break;

                        case INIT:
                            runOnUiThread(() -> loadingText.setText("Initializing Application"));
                            Thread.sleep(1000);
                            initializePref();
                            currentTest = TestType.PERMISSION;
                            break;

                        case PERMISSION:
                            runOnUiThread(() -> loadingText.setText("Request Permission"));
                            Thread.sleep(1000);

                            if (ActivityCompat.checkSelfPermission(LoadingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                if(!permissionCalled[0]) {
                                    ActivityCompat.requestPermissions(LoadingActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_READ_STOR);
                                    permissionCalled[0] = true;
                                }
                            }
                            else {
                                permissionDone[0] = true;
                                permissionSuccess[0] = true;
                            }

                            if (ActivityCompat.checkSelfPermission(LoadingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                if(!permissionCalled[1]) {
                                    ActivityCompat.requestPermissions(LoadingActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_FINE_LOC);
                                    permissionCalled[1] = true;
                                }
                            }
                            else {
                                permissionDone[1] = true;
                                permissionSuccess[1] = true;
                            }

                            if (ActivityCompat.checkSelfPermission(LoadingActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                if(!permissionCalled[2]) {
                                    ActivityCompat.requestPermissions(LoadingActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_PHONE_STATE);
                                    permissionCalled[2] = true;
                                }
                            }
                            else {
                                permissionDone[2] = true;
                                permissionSuccess[2] = true;
                            }

                            if (ActivityCompat.checkSelfPermission(LoadingActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                if(!permissionCalled[3]) {
                                    ActivityCompat.requestPermissions(LoadingActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_ACCESS);
                                    permissionCalled[3] = true;
                                }
                            }
                            else {
                                permissionDone[3] = true;
                                permissionSuccess[3] = true;
                            }

                            if(permissionDone[0] && permissionDone[1] && permissionDone[2] && permissionDone[3]) {
                                if(permissionSuccess[0] && permissionSuccess[1] && permissionSuccess[2] && permissionDone[3]) {
                                    Log.d("START", "ALL PERMISSION GRANTED!");
                                    currentTest = TestType.CONN;
                                }
                                else {
                                    Log.d("ERROR", "PERMISSION DECLINED!");
                                    throw new Exception("Permission declined!");
                                }
                            }

                            break;

                        case CONN:
                            runOnUiThread(() -> loadingText.setText("Connecting to Server"));
                            Thread.sleep(1000);

                            if(Stitch.hasAppClient(getString(R.string.db_name)) && Stitch.getDefaultAppClient().getAuth().isLoggedIn()) {
                                currentTest = TestType.DONE;
                            }
                            else {
                                if(conn_retries >= 10) {
                                    throw new Exception("Timeout of connection retries!");
                                }
                                else {
                                    conn_retries++;
                                    runOnUiThread(() -> loadingText.setText("Connect retries " + conn_retries));
                                }
                            }

                            break;
                    }

                    if (currentTest == TestType.DONE) {
                        runOnUiThread(() -> loadingText.setText("Starting Application."));
                        isDone = true;
                        break;
                    }
                }
            }
            catch(Exception e) {
                Log.e("ERROOR", "Test Failed!");
                Toast.makeText(LoadingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                isDone = false;
                hasError = true;
            }
        }
    }

    public void initializePref() {
        preferences = getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);

        if(!preferences.contains("doneInitial")) {
            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean("doneInitial", true);
            editor.putBoolean("unit", false);
            editor.putBoolean("priceExp", true);
            editor.putBoolean("doubleUnit", false);
            editor.putInt("unitSelect", 0);

            editor.apply();
        }
    }

    public enum TestType {
        DELAY,
        INIT,
        PERMISSION,
        CONN,
        LOGIN,
        DONE
    }
}
