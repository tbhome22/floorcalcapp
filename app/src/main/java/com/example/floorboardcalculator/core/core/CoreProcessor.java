package com.example.floorboardcalculator.core.core;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.floorboardcalculator.R;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.auth.StitchAuth;
import com.mongodb.stitch.core.auth.providers.userapikey.UserApiKeyCredential;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CoreProcessor extends Application {
    private static final String TAG = CoreProcessor.class.getSimpleName();

    private volatile int activityCounter = 0;

    private SharedPreferences preferences;

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

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

        ProcessCallbacks handler = new ProcessCallbacks(this);
        registerActivityLifecycleCallbacks(handler);
        registerComponentCallbacks(handler);

        Log.d(TAG, "Core Process onCreate() called!");
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
        else {
            Log.d(TAG, "Logged in to database server!");
        }
    }

    public void logoutRealm() {
        if(Stitch.hasAppClient(getString(R.string.db_name))) {
            StitchAuth auth = Stitch.getDefaultAppClient().getAuth();

            if(auth.isLoggedIn()) {
                Log.e(TAG, "Logged out from database!");
                auth.logout();
            }
        }
    }
}