package com.example.floorboardcalculator.core.database;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.floorboardcalculator.R;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.core.auth.providers.userapikey.UserApiKeyCredential;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainProcessApp extends Application {
    private static final String TAG = MainProcessApp.class.getSimpleName();
    private static final String APP_PREF = "TWO_BROTHER_SETTING";

    private volatile int activityCounter = 0;

    private SharedPreferences preferences;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(
        NUMBER_OF_CORES,
        NUMBER_OF_CORES,
        KEEP_ALIVE_TIME,
        KEEP_ALIVE_TIME_UNIT,
        workQueue
    );

    @Override
    public void onCreate(){
        super.onCreate();

        Log.d(TAG, "Application onCreate() called.");
        initializeRealm();

        initializePref();
    }

    public void incrementAcitvityCounter() {
        if(activityCounter == 0) {
            Log.d(TAG, "Increment: Activity counter was 0, initializing Realm!");
            if(!Stitch.hasAppClient(getString(R.string.db_name))) {
                Log.d(TAG, "Realm 1st started, initializing..");
                initializeRealm();
            }
        }

        activityCounter++;
        Log.d(TAG, "Increment: Activity counter incremented to " + activityCounter + ".");
    }

    public void decrementActivityCounter() {
        activityCounter--;
        Log.d(TAG, "Decrement: Activity counter decremented to " + activityCounter + ".");
        if(activityCounter == 0) {
            if(Stitch.hasAppClient(getString(R.string.db_name))) {
                try {
                    StitchAppClient appClient = Stitch.getDefaultAppClient();
                    appClient.getAuth().logout();
                    Log.e(TAG, "Database disconnected!");
                }
                catch(Exception e){
                    Log.e(TAG, "Error closing database!");
                }
            }

            Log.d(TAG, "Decrement: Activity counter was 0, closed realm.");
            System.exit(0);
        }
    }

    public void initializeRealm() {
        Log.d(TAG, "Log in realm database!");

        if(!Stitch.hasAppClient(getString(R.string.db_name)))
            Stitch.initializeDefaultAppClient(getString(R.string.db_name));

        if(!Stitch.getDefaultAppClient().getAuth().isLoggedIn()) {
            UserApiKeyCredential credential = new UserApiKeyCredential(getString(R.string.db_user));

            Stitch.getDefaultAppClient().getAuth().loginWithCredential(credential)
                    .addOnSuccessListener(stitchUser -> {
                        Log.d(TAG, "Logged in to database server!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error logging in to database server! Reason: " + e.getMessage());
                    });
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

    public int getActivityCounter() {
        return activityCounter;
    }

    public void setActivityCounter(int activityCounter) {
        this.activityCounter = activityCounter;
    }
}